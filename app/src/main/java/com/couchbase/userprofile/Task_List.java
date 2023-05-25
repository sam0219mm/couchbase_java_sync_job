package com.couchbase.userprofile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

import com.couchbase.userprofile.profile.UserProfileActivity;
import com.couchbase.userprofile.profile.UserProfileContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Task_List extends AppCompatActivity {

    private  ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    RecyclerView RecyclerView_Task;
    ListAdapter ListAdapter;

    private UserProfileContract.UserActionsListener ActionListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
       // Intent intent = getIntent();
     //   Map<String, Object> profile_data = (Map<String, Object>) getIntent().getSerializableExtra("profile_data");
        arrayList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("arrayList");
        RecyclerView_Task = findViewById(R.id.recycleview1);
        RecyclerView_Task.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView_Task.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ListAdapter = new ListAdapter();
        RecyclerView_Task.setAdapter(ListAdapter);
    }




    //////////////////
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tvId,Task,Type,Status;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.textView_Id);
                Task = itemView.findViewById(R.id.Task_V);
                Type = itemView.findViewById(R.id.Type_V);
                Status  = itemView.findViewById(R.id.status_V1);
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

            holder.tvId.setText(arrayList.get(position).get("Id"));
            holder.Task.setText(arrayList.get(position).get("TaskID"));
            holder.Type.setText(arrayList.get(position).get("Type"));
            holder.Status.setText(arrayList.get(position).get("Status"));

            holder.mView.setOnClickListener((v)->{
                Toast.makeText(getBaseContext(),holder.Status.getText(),Toast.LENGTH_SHORT).show();
            });

            MyTextWatcher watcher = new MyTextWatcher(holder);
            watcher.setTask(arrayList.get(position).get("TaskID"));
            watcher.setType(arrayList.get(position).get("Type"));
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

        public MyTextWatcher(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public void setStatus(String status) {
            this.status = status;
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
            taskMap.put("TaskID", task);
            taskMap.put("Status", newStatus);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}