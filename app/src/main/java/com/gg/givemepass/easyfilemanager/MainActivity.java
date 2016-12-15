package com.gg.givemepass.easyfilemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PRE_LEVEL = "..";
    public static final int FIRST_ITEM = 0;
    public static final int SECOND_ITEM = 1;
    private String IMG_ITEM = "image";
    private String NAME_ITEM = "name";
    private List<Map<String, Object>> filesList;
    private List<String> names;
    private List<String> paths;
    private File[] files;
    private Map<String, Object> filesMap;
    private int[] fileImg = {
            R.drawable.directory,
            R.drawable.file};
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private String nowPath;
    private TextView createDir;
    private static final String[] ACTION = {"修改", "刪除"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initView() {
        simpleAdapter = new SimpleAdapter(this,
                filesList, R.layout.adapter_item, new String[]{IMG_ITEM, NAME_ITEM},
                new int[]{R.id.image, R.id.text});
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String target = paths.get(position);
                if(target.equals(ROOT)){
                    nowPath = paths.get(position);
                    getFileDirectory(ROOT);
                    simpleAdapter.notifyDataSetChanged();
                } else if(target.equals(PRE_LEVEL)){
                    nowPath = paths.get(position);
                    getFileDirectory(new File(nowPath).getParent());
                    simpleAdapter.notifyDataSetChanged();
                } else {
                    File file = new File(target);
                    if (file.canRead()) {
                        if (file.isDirectory()) {
                            nowPath = paths.get(position);
                            getFileDirectory(paths.get(position));
                            simpleAdapter.notifyDataSetChanged();
                        } else{
                            Toast.makeText(MainActivity.this, R.string.is_not_directory, Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        Toast.makeText(MainActivity.this, R.string.can_not_read, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(ACTION, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String path = paths.get(position);
                                switch(which){
                                    case 0:
                                        rename(path);
                                        break;
                                    case 1:
                                        delFile(path);
                                        break;
                                }
                            }
                        })
                        .show();
                return true;
            }
        });
        createDir = (TextView) findViewById(R.id.new_dir);
        createDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDir();
            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.text);
                mImageView = (ImageView) v.findViewById(R.id.image);
            }
        }

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private void rename(final String path) {
        final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_new_dir, null);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.input_you_rename)
                .setView(item)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) item.findViewById(R.id.edittext);
                        if(editText.getText().equals("")){
                            Toast.makeText(MainActivity.this, R.string.input_dir_name, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String newPath = nowPath + File.separator + editText.getText();
                        File f = new File(path);
                        if(f.renameTo(new File(newPath))){
                            Toast.makeText(MainActivity.this, R.string.modify_success, Toast.LENGTH_SHORT).show();
                            getFileDirectory(nowPath);
                            simpleAdapter.notifyDataSetChanged();
                        } else{
                            Toast.makeText(MainActivity.this, R.string.modify_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void delFile(final String path) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.make_sure_del)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(path);
                        if(file.exists()){
                            if(file.delete()){
                                Toast.makeText(MainActivity.this, R.string.del_success, Toast.LENGTH_SHORT).show();
                                getFileDirectory(nowPath);
                                simpleAdapter.notifyDataSetChanged();
                            } else{
                                Toast.makeText(MainActivity.this, R.string.del_fail, Toast.LENGTH_SHORT).show();
                            }
                        } else{
                            Toast.makeText(MainActivity.this, R.string.file_is_not_exist, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .show();
    }

    private void addNewDir(){
        final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_new_dir, null);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.input_dir_name)
                .setView(item)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) item.findViewById(R.id.edittext);
                        if(editText.getText().equals("")){
                            Toast.makeText(MainActivity.this, R.string.input_dir_name, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String filePath = nowPath + File.separator + editText.getText().toString();
                        File f = new File(filePath);
                        if(f.mkdir()){
                            Toast.makeText(MainActivity.this, getString(R.string.create_dir_success) + filePath, Toast.LENGTH_SHORT).show();
                            getFileDirectory(nowPath);
                            simpleAdapter.notifyDataSetChanged();
                        } else{
                            Toast.makeText(MainActivity.this, R.string.create_dir_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void initData() {
        nowPath = ROOT;
        filesList = new ArrayList<>();
        names = new ArrayList<>();
        paths = new ArrayList<>();
        getFileDirectory(ROOT);
    }

    private void getFileDirectory(String path){
        filesList.clear();
        paths.clear();
        if(!path.equals(ROOT)){
            //回根目錄
            filesMap = new HashMap<>();
            names.add(ROOT);
            paths.add(FIRST_ITEM, ROOT);
            filesMap.put(IMG_ITEM, fileImg[0]);
            filesMap.put(NAME_ITEM, ROOT);
            filesList.add(filesMap);
            //回上一層
            filesMap = new HashMap<>();
            names.add(PRE_LEVEL);
            paths.add(SECOND_ITEM, new File(path).getParent());
            filesMap.put(IMG_ITEM, fileImg[0]);
            filesMap.put(NAME_ITEM, PRE_LEVEL);
            filesList.add(filesMap);
        }

        files = new File(path).listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                filesMap = new HashMap<>();
                names.add(files[i].getName());
                paths.add(files[i].getPath());
                if (files[i].isDirectory()) {
                    filesMap.put(IMG_ITEM, fileImg[0]);
                } else {
                    filesMap.put(IMG_ITEM, fileImg[1]);
                }
                filesMap.put(NAME_ITEM, files[i].getName());
                filesList.add(filesMap);
            }
        }
    }

    private static class Item{
        private String fileName;
        private int fileIcon;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getFileIcon() {
            return fileIcon;
        }

        public void setFileIcon(int fileIcon) {
            this.fileIcon = fileIcon;
        }
    }
}
