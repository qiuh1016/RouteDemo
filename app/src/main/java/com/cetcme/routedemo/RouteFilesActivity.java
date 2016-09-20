package com.cetcme.routedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.qiuhong.qhlibrary.Dialog.QHDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteFilesActivity extends AppCompatActivity {

    @BindView(R.id.route_files_listView)
    ListView listView;

    private List<Map<String, Object>> dataList = new ArrayList<>();
    private SimpleAdapter simpleAdapter;

    private EditText newFileNameEditText;

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
                qhDialog.setNegativeButton("绘图", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        drawRoute(position);
                    }
                });
                qhDialog.setPositiveButton("重命名", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        renameFile(position);
                    }
                });
                qhDialog.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                deleteFile(position);
                return false;
            }
        });
    }

    private List<Map<String, Object>> getData() {
        dataList = FileUtil.getFilesData();
        Collections.sort(dataList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                return Long.valueOf(lhs.get("lastModifyStamp").toString()).compareTo(Long.valueOf(rhs.get("lastModifyStamp").toString()));
            }
        });
        return dataList;
    }

    public void drawRoute(int Position) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("fileName", dataList.get(Position).get("fileName").toString());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void renameFile(final int Position) {
        Log.i(TAG, "renameFile: " + Position);
        QHDialog renameDialog = new QHDialog(RouteFilesActivity.this);
        renameDialog.setTitle("重命名");
        View view = LayoutInflater.from(RouteFilesActivity.this).inflate(R.layout.rename_edit_text, null);
        newFileNameEditText = (EditText) view.findViewById(R.id.new_fileName_editText);
        final String oldFileName = dataList.get(Position).get("fileName").toString();
        newFileNameEditText.setText(oldFileName.replace(".txt", ""));
        renameDialog.setContextView(view);
        renameDialog.setPositiveButton("重命名", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!newFileNameEditText.getText().toString().isEmpty()) {
                    dialog.dismiss();
                    String newFileName = newFileNameEditText.getText().toString() + ".txt";
                    FileUtil.renameFile(RouteFilesActivity.this, oldFileName, newFileName);
                    dataList.get(Position).put("fileName", newFileName);
                    simpleAdapter.notifyDataSetChanged();
                }
            }
        });
        renameDialog.setNegativeButton("取消", null);
        renameDialog.show();
    }

    private void deleteFile(final int Position) {
        QHDialog deleteDialog = new QHDialog(RouteFilesActivity.this, "提示", "确认删除\"" + dataList.get(Position).get("fileName") +"\"?");
        deleteDialog.setPositiveButton("删除", R.drawable.button_background_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FileUtil.deleteFile(dataList.get(Position).get("fileName").toString());
                dataList.remove(Position);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        deleteDialog.setNegativeButton("取消", null);
        deleteDialog.show();
    }


}
