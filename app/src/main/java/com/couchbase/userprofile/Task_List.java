package com.couchbase.userprofile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Database;
import com.couchbase.lite.MaintenanceType;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.userprofile.profile.UserProfileActivity;
import com.couchbase.userprofile.profile.UserProfileContract;
import com.couchbase.userprofile.profile.UserProfilePresenter;
import com.couchbase.userprofile.util.DatabaseManager;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Task_List extends AppCompatActivity implements UserProfileContract.View {

    private  ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    RecyclerView RecyclerView_Task;
    ListAdapter ListAdapter;

    private UserProfileContract.UserActionsListener presenter;
    private Map<String, Object> profile_data = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //清除Cache folder.
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = getCacheDir();
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        // 如果文件是建立超過1分鐘，就删除
                        if (System.currentTimeMillis() - file.lastModified() > 60 * 1000) {
                            file.delete();
                        }
                    }
                }
            }
        }).start();
      //  Intent intent = getIntent();
     //   Map<String, Object> profile_data = (Map<String, Object>) getIntent().getSerializableExtra("profile_data");
        arrayList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("arrayList");
        RecyclerView_Task = findViewById(R.id.recycleview1);
        RecyclerView_Task.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView_Task.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ListAdapter = new ListAdapter();
        RecyclerView_Task.setAdapter(ListAdapter);

        // 建立 UserProfilePresenter
        this.presenter = new UserProfilePresenter(this);
        this.presenter.fetchProfile();

    }


    /////
    public void showProfile(Map<String, Object> profile) {
        profile_data=profile;

 //       deleteOldBlobs();

        // 將 profile 轉換為 arrayList，然後更新 RecyclerView 的數據
        List<Map<String, Object>> jobList = (List<Map<String, Object>>) profile.get("jobs");
        arrayList.clear();
 //       int i=0;
        for (Map<String, Object> job : jobList) {
            HashMap<String, String> jobMap = new HashMap<>();
            jobMap.put("Id", (String) job.get("Id"));
            jobMap.put("Task", (String) job.get("Task"));
            jobMap.put("Task_id", (String) job.get("Task_id"));
            jobMap.put("Create_time", (String) job.get("Create_time"));
            jobMap.put("level", (String) job.get("level"));
            jobMap.put("Address", (String) job.get("Address"));
            jobMap.put("Status", (String) job.get("Status"));
            String videoString = "";
            if (job.containsKey("VideoData")) {
                Blob videoData = (Blob) job.get("VideoData");
                if (videoData != null) {
                    byte[] videoBytes = videoData.getContent();
                    videoString = Base64.encodeToString(videoBytes, Base64.DEFAULT);
                }
            }
            if (job.containsKey("videoTimestamp")) {
                jobMap.put("videoTimestamp", (String) job.get("Create_time")); // Convert Long to String
            }
            jobMap.put("VideoData", videoString);

            arrayList.add(jobMap);

//            ListAdapter.notifyItemChanged(i);
 //           i++;
        }
        // 刷新 RecyclerView
//        RecyclerView_Task.getRecycledViewPool().clear();
        ListAdapter.notifyDataSetChanged();
    }

    public void onSaveTapped_task(View view) {

        //profile_data.remove("jobs");
        // 收集當前的 job status
//        List<Map<String, String>> current_jobs = new ArrayList<>();
//        ListAdapter adapter = (ListAdapter) RecyclerView_Task.getAdapter();
//        for (int i = 0; i < adapter.getItemCount(); i++) {
//            ListAdapter.ViewHolder viewHolder = (ListAdapter.ViewHolder) adapter.createViewHolder(RecyclerView_Task, adapter.getItemViewType(i));
//            adapter.bindViewHolder(viewHolder, i);
//            Map<String, String> task = new HashMap<>();
//            task.put("Task", viewHolder.Task.getText().toString());
//            task.put("Task_id", arrayList.get(i).get("Task_id"));
//            task.put("Create_time", viewHolder.Create_time.getText().toString());
//            task.put("Type", viewHolder.Type.getText().toString());
//            task.put("Address", viewHolder.Address.getText().toString());
//            task.put("Status", viewHolder.Status.getText().toString());
//            current_jobs.add(task);
//        }
        List<Map<String, Object>> current_jobs = new ArrayList<>();
        ListAdapter adapter = (ListAdapter) RecyclerView_Task.getAdapter();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListAdapter.ViewHolder viewHolder = (ListAdapter.ViewHolder) adapter.createViewHolder(RecyclerView_Task, adapter.getItemViewType(i));
            adapter.bindViewHolder(viewHolder, i);
            Map<String, Object> task = new HashMap<>();
            task.put("Task", viewHolder.Task.getText().toString());
            task.put("Task_id", arrayList.get(i).get("Task_id"));
            task.put("Create_time", viewHolder.Create_time.getText().toString());
            task.put("level", viewHolder.level.getText().toString());
            task.put("Address", viewHolder.Address.getText().toString());
            task.put("Status", viewHolder.Status.getText().toString());
            // 將 Base64 編碼的 String 轉換回 Blob
            String videoString = arrayList.get(i).get("VideoData");
            Blob videoData = null;
            if (videoString != null && !videoString.isEmpty()) {
                byte[] videoBytes = Base64.decode(videoString, Base64.DEFAULT);
                videoData = new Blob("video/mp4", videoBytes); // MP4格式
                task.put("videoTimestamp", arrayList.get(i).get("videoTimestamp"));
            }
            task.put("VideoData", videoData);
            current_jobs.add(task);
        }


        profile_data.put("jobs", current_jobs);

        presenter.saveProfile(profile_data);
        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();

        // 更新 UserProfilePresenter 中的 profile
//        UserProfilePresenter userProfilePresenter = new UserProfilePresenter(this);
//        userProfilePresenter.saveProfile(current_jobs);
    }

    private void deleteOldBlobs() {
        long currentTime = System.currentTimeMillis();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 所有的JOB
        for (Map<String, Object> job : (List<Map<String, Object>>) profile_data.get("jobs")) {
            // 檢查是否有影片&videoTimestamp key
            if (job.get("VideoData") != null && job.get("videoTimestamp") != null) {
                try {

                    java.util.Date date = sdf.parse((String) job.get("videoTimestamp"));

                    // 將 Date轉毫秒
                    long timestamp = date.getTime();

                    // 時間比較
                    if (timestamp < currentTime - 10 * 60 * 1000) {
                        job.put("VideoData", null);
                        job.remove("videoTimestamp");
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // 保存更新後的 profile
        Database database = DatabaseManager.getUserProfileDatabase();
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();
        MutableDocument mutableDocument = new MutableDocument(docId, profile_data);
        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // COMPACT
        try {
            database.performMaintenance(MaintenanceType.COMPACT);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }



/////List<Map<String, String>> jobs = new ArrayList<>();
//        MyListAdapter adapter = (MyListAdapter) mRecyclerView.getAdapter();
//        for (int i = 0; i < adapter.getItemCount(); i++) {
//            MyListAdapter.ViewHolder viewHolder = (MyListAdapter.ViewHolder) adapter.createViewHolder(mRecyclerView, adapter.getItemViewType(i));
//            adapter.bindViewHolder(viewHolder, i);
//            Map<String, String> task = new HashMap<>();
//            task.put("Task", viewHolder.Task.getText().toString());
//            task.put("Type", viewHolder.Type.getText().toString());
//            task.put("Status", viewHolder.Status.getText().toString());
//            jobs.add(task);
//        }



    //////////////////
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tvId,Task,level,Status,Address,Create_time;
            private View mView;
            Button buttonVideo;
            int currentPosition;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.textView_Id);
                Task = itemView.findViewById(R.id.Task_V);
                Create_time = itemView.findViewById(R.id.Create_time);
                level = itemView.findViewById(R.id.Type_V);
                Status  = itemView.findViewById(R.id.status_V1);
                Address = itemView.findViewById(R.id.address_V);
                mView  = itemView;
                buttonVideo = itemView.findViewById(R.id.button_video);
                buttonVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        HashMap<String, String> currentJobMap = arrayList.get(currentPosition);
                        // 從 HashMap 中獲取 VideoData
                        String videoData = currentJobMap.get("VideoData");

                        //轉換blob to mp4
                        byte[] videoBytes = Base64.decode(videoData, Base64.DEFAULT);
                        File tempFile = null;
                        try {
                            tempFile = File.createTempFile("video", ".mp4", getCacheDir());
                            tempFile.deleteOnExit();
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(videoBytes);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 獲取檔案的URI
                        Uri videoUri = Uri.fromFile(tempFile);

                        // 將URI作為額外數據
                        Intent intent = new Intent(v.getContext(), Play_video.class);
                        intent.putExtra("VideoUri", videoUri.toString());

                        v.getContext().startActivity(intent);
                    }
                });

            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_item,parent,false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // int avgS = Integer.parseInt(arrayList.get(position).get("Avg"));

                String level = arrayList.get(position).get("level");
            holder.tvId.setText(arrayList.get(position).get("Id"));
            holder.Task.setText(arrayList.get(position).get("Task"));
            holder.Create_time.setText(arrayList.get(position).get("Create_time"));
            holder.level.setText(arrayList.get(position).get("level"));
            holder.Address.setText(arrayList.get(position).get("Address"));
            holder.Status.setText(arrayList.get(position).get("Status"));
            holder.currentPosition = position;



 //           String type = arrayList.get(position).get("Type");
 //           String status =arrayList.get(position).get("Status");
//            if ("urgent".equalsIgnoreCase(type)&& "Pending".equalsIgnoreCase(status)) {
//                holder.tvId.setTextColor(Color.RED);
//            } else {
//                holder.tvId.setTextColor(Color.BLACK);
//            }
            String videoData = arrayList.get(position).get("VideoData");
            // 如果 VideoData 有值，顯示按钮
            if (videoData != null && !videoData.isEmpty()) {
                holder.buttonVideo.setVisibility(View.VISIBLE);
            } else {
                holder.buttonVideo.setVisibility(View.GONE);
            }

 //           holder.itemView.invalidate();
            if ("urgent".equalsIgnoreCase(level)) {
                holder.level.setTextColor(Color.BLUE);
            } else {
                holder.level.setTextColor(Color.BLACK);
            }


            holder.mView.setOnClickListener((v)->{
                Toast.makeText(getBaseContext(),holder.Status.getText(),Toast.LENGTH_SHORT).show();
            });

            MyTextWatcher watcher = new MyTextWatcher(holder);
            watcher.setTask(arrayList.get(position).get("Task"));
            watcher.setCreate_time(arrayList.get(position).get("Create_time"));
            watcher.setlevel(arrayList.get(position).get("level"));
            watcher.setAddress(arrayList.get(position).get("Address"));
            watcher.setStatus(arrayList.get(position).get("Status"));

            holder.Status.addTextChangedListener(watcher);



        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    public class MyTextWatcher implements TextWatcher {
        private RecyclerView.ViewHolder viewHolder;
        private String level;
        private String task;
        private String status;
        private String Address;
        private String Create_time;

        public MyTextWatcher(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        public void setlevel(String level) {
            this.level = level;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public void setCreate_time(String Create_time) {
            this.Create_time = Create_time;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setAddress(String Address) {
            this.Address = Address;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 監聽 EditText 的內容更改
            String newStatus = s.toString();
            // 更新 task 中的數據
            Map<String, String> taskMap = arrayList.get(viewHolder.getAdapterPosition());
            taskMap.put("level", level);
            taskMap.put("Task", task);
            taskMap.put("Create_time", Create_time);
            taskMap.put("Address", Address);
            taskMap.put("Status", newStatus);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}