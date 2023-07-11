package com.couchbase.userprofile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.lite.DataSource;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Database;
import com.couchbase.lite.Meta;
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
            jobMap.put("Type", (String) job.get("Type"));
            jobMap.put("Address", (String) job.get("Address"));
            jobMap.put("Status", (String) job.get("Status"));
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
        List<Map<String, String>> current_jobs = new ArrayList<>();
        ListAdapter adapter = (ListAdapter) RecyclerView_Task.getAdapter();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListAdapter.ViewHolder viewHolder = (ListAdapter.ViewHolder) adapter.createViewHolder(RecyclerView_Task, adapter.getItemViewType(i));
            adapter.bindViewHolder(viewHolder, i);
            Map<String, String> task = new HashMap<>();
            task.put("Task", viewHolder.Task.getText().toString());
            task.put("Task_id", arrayList.get(i).get("Task_id"));
            task.put("Create_time", viewHolder.Create_time.getText().toString());
            task.put("Type", viewHolder.Type.getText().toString());
            task.put("Address", viewHolder.Address.getText().toString());
            task.put("Status", viewHolder.Status.getText().toString());
            current_jobs.add(task);
        }
        profile_data.put("jobs", current_jobs);

        presenter.saveProfile(profile_data);
        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();

        // 更新 UserProfilePresenter 中的 profile
//        UserProfilePresenter userProfilePresenter = new UserProfilePresenter(this);
//        userProfilePresenter.saveProfile(current_jobs);
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
            public TextView tvId,Task,Type,Status,Address,Create_time;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.textView_Id);
                Task = itemView.findViewById(R.id.Task_V);
                Create_time = itemView.findViewById(R.id.Create_time);
                Type = itemView.findViewById(R.id.Type_V);
                Status  = itemView.findViewById(R.id.status_V1);
                Address = itemView.findViewById(R.id.address_V);
                mView  = itemView;

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

            String type = arrayList.get(position).get("Type");
            holder.tvId.setText(arrayList.get(position).get("Id"));
            holder.Task.setText(arrayList.get(position).get("Task"));
            holder.Create_time.setText(arrayList.get(position).get("Create_time"));
            holder.Type.setText(arrayList.get(position).get("Type"));
            holder.Address.setText(arrayList.get(position).get("Address"));
            holder.Status.setText(arrayList.get(position).get("Status"));


 //           String type = arrayList.get(position).get("Type");
 //           String status =arrayList.get(position).get("Status");
//            if ("urgent".equalsIgnoreCase(type)&& "Pending".equalsIgnoreCase(status)) {
//                holder.tvId.setTextColor(Color.RED);
//            } else {
//                holder.tvId.setTextColor(Color.BLACK);
//            }


 //           holder.itemView.invalidate();
            if ("urgent".equalsIgnoreCase(type)) {
                holder.Type.setTextColor(Color.BLUE);
            } else {
                holder.Type.setTextColor(Color.BLACK);
            }


            holder.mView.setOnClickListener((v)->{
                Toast.makeText(getBaseContext(),holder.Status.getText(),Toast.LENGTH_SHORT).show();
            });

            MyTextWatcher watcher = new MyTextWatcher(holder);
            watcher.setTask(arrayList.get(position).get("Task"));
            watcher.setCreate_time(arrayList.get(position).get("Create_time"));
            watcher.setType(arrayList.get(position).get("Type"));
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
        private String type;
        private String task;
        private String status;
        private String Address;
        private String Create_time;

        public MyTextWatcher(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        public void setType(String type) {
            this.type = type;
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
            taskMap.put("Type", type);
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