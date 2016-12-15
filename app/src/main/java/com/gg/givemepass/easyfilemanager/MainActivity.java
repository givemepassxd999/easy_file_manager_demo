package com.gg.givemepass.easyfilemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String TOP_LEVEL = "/";
    private static final String PRE_LEVEL = "..";
    public static final int FIRST_ITEM = 0;
    public static final int SECOND_ITEM = 1;
    private static final String[] ACTION = {"修改", "刪除"};
    private String IMG_ITEM = "image";
    private String NAME_ITEM = "name";
    private File[] files;
    private int[] fileImg = {
            R.drawable.directory,
            R.drawable.file};
    private String nowPath;
    private TextView createDir;
    private List<Item> itemList;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initView() {
        mAdapter = new MyAdapter();
        mAdapter.setData(itemList);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        createDir = (TextView) findViewById(R.id.new_dir);
        createDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDir();
            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<Item> mData;

         class ViewHolder extends RecyclerView.ViewHolder {
             TextView mTextView;
             ImageView mImageView;
             ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.text);
                mImageView = (ImageView) v.findViewById(R.id.image);
             }
        }

        MyAdapter() {
            mData = new ArrayList<>();
        }

        void setData(List<Item> item){
            mData = item;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position).getFileName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position >= itemList.size()){return;}
                    String target = itemList.get(position).getFilePath();
                    if(target.equals(ROOT)){
                        nowPath = itemList.get(position).getFilePath();
                        getFileDirectory(ROOT);
                        mAdapter.notifyDataSetChanged();
                    } else if(target.equals(PRE_LEVEL)){
                        nowPath = itemList.get(position).getFilePath();
                        getFileDirectory(new File(nowPath).getParent());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        File file = new File(target);
                        if (file.canRead()) {
                            if (file.isDirectory()) {
                                nowPath = itemList.get(position).getFilePath();
                                getFileDirectory(itemList.get(position).getFilePath());
                                mAdapter.notifyDataSetChanged();
                            } else{
                                Toast.makeText(MainActivity.this, R.string.is_not_directory, Toast.LENGTH_SHORT).show();
                            }
                        } else{
                            Toast.makeText(MainActivity.this, R.string.can_not_read, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setItems(ACTION, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String path = itemList.get(position).getFilePath();
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
                            mAdapter.notifyDataSetChanged();

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
                                mAdapter.notifyDataSetChanged();
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
                        mAdapter.setData(itemList);
                        mAdapter.notifyDataSetChanged();
                    } else{
                        Toast.makeText(MainActivity.this, R.string.create_dir_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .show();
    }

    private void initData() {
        nowPath = ROOT;
        itemList = new ArrayList<>();
        getFileDirectory(ROOT);
    }

    private void getFileDirectory(String path){
        itemList.clear();
        if(!path.equals(ROOT)){
            //回根目錄
            Item rootItem = new Item();
            rootItem.setFileName(TOP_LEVEL);
            rootItem.setFileIcon(fileImg[0]);
            rootItem.setFilePath(ROOT);
            itemList.add(rootItem);
            //回上一層
            Item preLevelItem = new Item();
            preLevelItem.setFileName(PRE_LEVEL);
            preLevelItem.setFileIcon(fileImg[0]);
            preLevelItem.setFilePath(new File(path).getParent());
            itemList.add(preLevelItem);

        }

        files = new File(path).listFiles();
        if(files != null) {
            for(int i = 0; i < files.length; i++) {
                Item item = new Item();
                item.setFileName(files[i].getName());
                item.setFilePath(files[i].getPath());
                if (files[i].isDirectory()) {
                    item.setFileIcon(fileImg[0]);
                } else {
                    item.setFileIcon(fileImg[1]);
                }
                itemList.add(item);
            }
        }
    }

    private static class Item{
        private String fileName;
        private int fileIcon;
        private String filePath;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

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
