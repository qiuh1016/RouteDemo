package com.cetcme.routedemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.qiuhong.qhlibrary.Utils.DensityUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by qiuhong on 9/20/16.
 */
public class OfflineMapActivity extends Activity implements MKOfflineMapListener {

    private String TAG = "OfflineMapActivity";

    private MKOfflineMap mOffline = null;

    @BindView(R.id.cityid)
    TextView cityIDTextView;
    @BindView(R.id.city)
    EditText cityNameTextView;
    @BindView(R.id.state)
    TextView stateView;
    /**
     * 已下载的离线地图信息列表
     */
    private ArrayList<MKOLUpdateElement> localMapList = null;
    private LocalMapAdapter lAdapter = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);
        ButterKnife.bind(this);
        mOffline = new MKOfflineMap();
        mOffline.init(this);
        initView();

    }

    private void initView() {

        ListView hotCityList = (ListView) findViewById(R.id.hotcitylist);
        final ArrayList<Map<String, Object>> hotCities = new ArrayList<>();
        // 获取热闹城市列表
        ArrayList<MKOLSearchRecord> records1 = mOffline.getHotCityList();
        if (records1 != null) {
            for (MKOLSearchRecord r : records1) {
                Map<String, Object> map = new Hashtable<>();
                map.put("cityName", r.cityName);
                map.put("cityID", r.cityID);
                map.put("dataSize", this.formatDataSize(r.size));
                hotCities.add(map);
            }
        }
        SimpleAdapter hAdapter = new SimpleAdapter(OfflineMapActivity.this, hotCities,
                        R.layout.offline_map_list,
                        new String[] {"cityID", "cityName", "dataSize"},
                        new int[] {R.id.id, R.id.name, R.id.size});
        hotCityList.setAdapter(hAdapter);
        hotCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityIDTextView.setText(hotCities.get(position).get("cityID").toString());
                cityNameTextView.setText(hotCities.get(position).get("cityName").toString());
            }
        });

        ListView allCityList = (ListView) findViewById(R.id.allcitylist);
        // 获取所有支持离线地图的城市
        final ArrayList<Map<String, Object>> allCities = new ArrayList<>();
        ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
        if (records1 != null) {
            for (MKOLSearchRecord r : records2) {
                Map<String, Object> map = new Hashtable<>();
                map.put("cityName", r.cityName);
                map.put("cityID", r.cityID);
                map.put("dataSize", this.formatDataSize(r.size));
                allCities.add(map);
            }
        }
        SimpleAdapter aAdapter = new SimpleAdapter(OfflineMapActivity.this, allCities,
                R.layout.offline_map_list,
                new String[] {"cityID", "cityName", "dataSize"},
                new int[] {R.id.id, R.id.name, R.id.size});
        allCityList.setAdapter(aAdapter);
        allCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityIDTextView.setText(allCities.get(position).get("cityID").toString());
                cityNameTextView.setText(allCities.get(position).get("cityName").toString());
            }
        });


        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

        // 获取已下过的离线地图信息
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<>();
        }

        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        lAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(lAdapter);

    }

    /**
     * 切换至城市列表
     *
     * @param view
     */
    public void clickCityListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

    }

    /**
     * 切换至下载管理列表
     *
     * @param view
     */
    public void clickLocalMapListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.VISIBLE);
        cl.setVisibility(View.GONE);
    }

    /**
     * 搜索离线需市
     *
     * @param view
     */
    public void search(View view) {
        ArrayList<MKOLSearchRecord> records = mOffline.searchCity(cityNameTextView
                .getText().toString());
        if (records == null || records.size() != 1) {
            return;
        }
        cityIDTextView.setText(String.valueOf(records.get(0).cityID));
    }

    /**
     * 开始下载
     *
     * @param view
     */
    public void start(View view) {
        int cityID = Integer.parseInt(cityIDTextView.getText().toString());
        mOffline.start(cityID);
        clickLocalMapListButton(null);
        Toast.makeText(this, "开始下载离线地图. cityID: " + cityID, Toast.LENGTH_SHORT)
                .show();
        updateView();
    }

    /**
     * 暂停下载
     *
     * @param view
     */
    public void stop(View view) {
        int cityID = Integer.parseInt(cityIDTextView.getText().toString());
        mOffline.pause(cityID);
        Toast.makeText(this, "暂停下载离线地图. cityID: " + cityID, Toast.LENGTH_SHORT)
                .show();
        updateView();
    }

    /**
     * 删除离线地图
     *
     * @param view
     */
    public void remove(View view) {
        int cityID = Integer.parseInt(cityIDTextView.getText().toString());
        mOffline.remove(cityID);
        Toast.makeText(this, "删除离线地图. cityID: " + cityID, Toast.LENGTH_SHORT)
                .show();
        updateView();
    }

    /**
     * 更新状态显示
     */
    public void updateView() {
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<>();
        }
        lAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        int cityID = Integer.parseInt(cityIDTextView.getText().toString());
        MKOLUpdateElement temp = mOffline.getUpdateInfo(cityID);
        if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
            mOffline.pause(cityID);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public String formatDataSize(int size) {
        String ret;
        if (size < (1024 * 1024)) {
            ret = String.format("%dKB", size / 1024);
        } else {
            ret = String.format("%.1fMB", size / (1024 * 1024.0));
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        /**
         * 退出时，销毁离线地图模块
         */
        mOffline.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    stateView.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                    updateView();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d(TAG, String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
            default:
                break;
        }

    }

    /**
     * 离线地图管理列表适配器
     */
    public class LocalMapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localMapList.size();
        }

        @Override
        public Object getItem(int index) {
            return localMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            view = View.inflate(OfflineMapActivity.this, R.layout.offline_localmap_list, null);
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            Button remove = (Button) view.findViewById(R.id.remove);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView update = (TextView) view.findViewById(R.id.update);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            DonutProgress donutProgress = (DonutProgress) view.findViewById(R.id.donut_progress);

            donutProgress.setUnfinishedStrokeWidth(DensityUtil.dip2px(OfflineMapActivity.this, 2));
            donutProgress.setFinishedStrokeWidth(DensityUtil.dip2px(OfflineMapActivity.this, 2));
            donutProgress.setTextSize(DensityUtil.dip2px(OfflineMapActivity.this, 7));
            donutProgress.setProgress(e.ratio);

            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.update) {
                update.setText("可更新");
            } else {
                update.setText("最新");
            }

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mOffline.remove(e.cityID);
                    updateView();
                }
            });
        }

    }
}
