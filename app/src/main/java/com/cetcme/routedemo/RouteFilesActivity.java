package com.cetcme.routedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteFilesActivity extends AppCompatActivity {

    @BindView(R.id.route_files_listView)
    ListView listView;

    private List<Map<String, Object>> dataList = new ArrayList<>();
    private SimpleAdapter simpleAdapter;

    private String TAG = "RouteFilesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_files);
        ButterKnife.bind(this);
        initListView();
    }

    private void initListView() {
        simpleAdapter = new SimpleAdapter(
                RouteFilesActivity.this,
                getData(),
                R.layout.route_file_listview_cell,
                new String[] {"fileName", "fileLength", "lastModifyTime"},
                new int[] {R.id.file_name, R.id.file_length, R.id.last_modify_time});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: fileName:" + dataList.get(position).get("fileName"));
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("fileName", dataList.get(position).get("fileName").toString());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private List<Map<String, Object>> getData() {
        dataList = FileUtil.getFilesData();
        return dataList;
    }

}
