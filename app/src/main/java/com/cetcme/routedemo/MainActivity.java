package com.cetcme.routedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recordButton)
    Button recordButton;

    @BindView(R.id.showUserButton)
    Button showUserButton;

    @BindView(R.id.setButton)
    Button setButton;

    @BindView(R.id.locationButton)
    Button locationButton;

    @BindView(R.id.editText)
    EditText GPSSpanEditText;

    @BindView(R.id.editText2)
    EditText locationInfoEditText;

    @BindView(R.id.bmapView)
    MapView mapView;

    private BaiduMap mBaiduMap;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private int defaultGPSSpan = 5;
    private List<LatLng> routePointsWhileRecording = new ArrayList<>();
    private JSONArray routePoints = new JSONArray();
    private boolean routeRecording = false;
    private boolean showUser = true;
    private boolean isFirstGetLocation = true;
    private BDLocation userLocation;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
        Log.i(TAG, "onCreate: " + FileUtil.getFileNames());
    }

    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();

        if (routePoints.length() != 0) {
            FileUtil.saveFile(getDate(), routePoints.toString());
        }

    }

    private void init() {
        mBaiduMap = mapView.getMap();
        GPSSpanEditText.setText(defaultGPSSpan + "");

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        initMyLocation();
        mLocationClient.start();
    }

    public void recordButtonTapped(View view) {
        routeRecording = !routeRecording;
        recordButton.setText(routeRecording ? "STOP" : "RECORD");
    }

    public void showUserButtonTapped(View view) {
        showUser = !showUser;
        showUserButton.setText(showUser ? "Hide" : "show");
        mBaiduMap.setMyLocationEnabled(showUser);
    }

    public void setButtonTapped(View view) {
        defaultGPSSpan = Integer.parseInt(GPSSpanEditText.getText().toString());
        initLocation();
    }

    public void locationButtonTapped(View view) {
        if (userLocation == null) {
            return;
        }
        MapStatus mapStatus = new MapStatus.Builder()
                .target(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        mBaiduMap.animateMapStatus(mapStatusUpdate);

    }

    public void routeButtonTapped(View view) {
        String lastFileName = FileUtil.getLastFileName();
        String arrayString = FileUtil.readFile(lastFileName.replace(".txt", ""));

        locationInfoEditText.setText(lastFileName + "\n"+ arrayString);
        List<LatLng> route = new ArrayList<>();
        if (!arrayString.equals("")) {
            try {
                JSONArray array = new JSONArray(arrayString);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = (JSONObject) array.get(i);
                    double lat = json.getDouble("latitude");
                    double lng = json.getDouble("longitude");
                    route.add(new LatLng(lat, lng));
                }
                drawRoute(route);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = defaultGPSSpan * 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlongitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());

            updateMyLocation(location);
            locationInfoEditText.setText(sb.toString());



            // qh
            if (isFirstGetLocation) {
                setMapStatus(new LatLng(location.getLatitude(), location.getLongitude()), 17);
                isFirstGetLocation = false;
            }
            userLocation = location;

            if (routeRecording) {
                drawRouteWhileRecording(location);
                routePoints.put(BDLocationToJson(location));
            }

        }
    }

    private JSONObject BDLocationToJson(BDLocation location) {
        JSONObject locationJSON = new JSONObject();
        try {
            locationJSON.put("latitude", location.getLatitude());
            locationJSON.put("longitude", location.getLongitude());
            return  locationJSON;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }

    private void drawRouteWhileRecording(BDLocation location) {

        if (routePointsWhileRecording.size() == 0) {
            routePointsWhileRecording.add(new LatLng(location.getLatitude(), location.getLongitude()));
        } else  if (routePointsWhileRecording.size() == 1) {
            routePointsWhileRecording.add(new LatLng(location.getLatitude(), location.getLongitude()));
            drawRoute(routePointsWhileRecording);
        } else {
            routePointsWhileRecording.remove(0);
            routePointsWhileRecording.add(new LatLng(location.getLatitude(), location.getLongitude()));
            drawRoute(routePointsWhileRecording);
        }
    }

    private void initMyLocation() {
        mBaiduMap.setMyLocationEnabled(true);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_user_location);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);
        // 当不需要定位图层时关闭定位图层
        //mBaiduMap.setMyLocationEnabled(false);
    }

    private void updateMyLocation(BDLocation mBDLocation) {
//        setMapStatus(new LatLng(mBDLocation.getLatitude(), mBDLocation.getLongitude()), 0);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(mBDLocation.getRadius())
                .direction(mBDLocation.getDirection())
                .latitude(mBDLocation.getLatitude())
                .longitude(mBDLocation.getLongitude())
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
    }

    private void setMapStatus(LatLng latLng, int zoomLevel) {
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(zoomLevel) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }

    public void drawRoute(List<LatLng> points) {

        //构建分段颜色索引数组
        List<Integer> colors = new ArrayList<>();
        if (true) {
//            colors.add(R.color.colorRouteConved);
            colors.add(0xFF167CF3); //纠偏成功显示蓝色
        } else {
//            colors.add(R.color.colorRouteunConved);
            colors.add(0xFFE27575); //纠偏失败显示红色
        }

        OverlayOptions ooPolyline = new PolylineOptions()
                .points(points)
                .width(5)
                .colorsValues(colors);
        //添加在地图中
        mBaiduMap.addOverlay(ooPolyline);

        //起点终点标注
        //构建Marker图标
//        BitmapDescriptor startBitmap = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_start);
//        BitmapDescriptor endBitmap = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_end);
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions startMaker = new MarkerOptions()
//                .position(latLngs.get(0))
//                .icon(startBitmap);
//        OverlayOptions endMaker = new MarkerOptions()
//                .position(latLngs.get(latLngs.size() - 1))
//                .icon(endBitmap);
//        //在地图上添加Marker，并显示
//        baiduMap.addOverlay(startMaker);
//        baiduMap.addOverlay(endMaker);

        //showMediaPoint
        /*
        if (showMediaPoint && latLngs.size() < maxMediaPointMarkerNum) {

            BitmapDescriptor mediaBitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_point);
            for (int i = 1; i < latLngs.size() - 1; i++) {
                OverlayOptions mediaMaker = new MarkerOptions()
                        .position(latLngs.get(i))
                        .icon(mediaBitmap);
                baiduMap.addOverlay(mediaMaker);
            }
        }
        */

    }

    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        Log.i(TAG, "getDate: " + time);
        return time;
    }


}
