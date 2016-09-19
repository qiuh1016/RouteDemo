package com.cetcme.routedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.qiuhong.qhlibrary.Dialog.QHDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteFilesActivity extends AppCompatActivity {

    @BindView(R.id.route_files_listView)
    ListView listView;

    @BindView(R.id.file_name_textView)
    TextView fileNameTextView;

    private List<Map<String, Object>> dataList = new ArrayList<>();
    private SimpleAdapter simpleAdapter;

    private int selectedFilePosition = -1;

    private String TAG = "RouteFilesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_files);
        ButterKnife.bind(this);
        initListView();
//        FileUtil.renameFile("2016-09-18 19-20-12.txt", "18号晚回家.txt");
        fileNameTextView.setText("请选择文件");
    }

    private void initListView() {
        simpleAdapter = new SimpleAdapter(
                RouteFilesActivity.this,
                getData(),
                R.layout.route_file_listview_cell,
                new String[] {"fileName", "fileLength"},
                new int[] {R.id.file_name, R.id.file_length});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String msg = "文件名称: " + dataList.get(position).get("fileName") + "\n" +
                        "文件大小: " + dataList.get(position).get("fileLength") + "\n" +
                        "修改时间: " + dataList.get(position).get("lastModifyTime");
                QHDialog qhDialog = new QHDialog(RouteFilesActivity.this, "文件详情" , msg);
                qhDialog.setNegativeButton("选择", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        selectedFilePosition = position;
                        fileNameTextView.setText("已选择: " + dataList.get(position).get("fileName"));
                    }
                });
                qhDialog.setPositiveButton("取消", null);
                qhDialog.show();
            }
        });
    }

    private List<Map<String, Object>> getData() {
        dataList = FileUtil.getFilesData();
        return dataList;
    }

    public void drawButtonTapped(View view) {
        if (selectedFilePosition == -1) {
            noFileSelected();
            return;
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("fileName", dataList.get(selectedFilePosition).get("fileName").toString());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void renameButtonTapped(View view) {
        if (selectedFilePosition == -1) {
            noFileSelected();
            return;
        }
    }

    public void deleteButtonTapped(View view) {
        if (selectedFilePosition == -1) {
            noFileSelected();
            return;
        }

        QHDialog deleteDialog = new QHDialog(RouteFilesActivity.this, "提示", "确认删除" + dataList.get(selectedFilePosition).get("fileName") +"?");
        deleteDialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FileUtil.deleteFile(dataList.get(selectedFilePosition).get("fileName").toString());
                dataList.remove(selectedFilePosition);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        deleteDialog.setNegativeButton("取消", null);
        deleteDialog.show();
    }

    private void noFileSelected() {
        new QHDialog(RouteFilesActivity.this, "提示", "请选择一个轨迹文件").show();
    }

}
