package com.couchbase.userprofile.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Blob;
import com.couchbase.userprofile.R;
import com.couchbase.userprofile.Task_List;
import com.couchbase.userprofile.login.LoginActivity;
import com.couchbase.userprofile.universities.UniversitiesActivity;
import com.couchbase.userprofile.util.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.annotation.NonNull;
import android.view.ViewGroup;
import android.view.LayoutInflater;


public class UserProfileActivity
        extends AppCompatActivity
        implements UserProfileContract.View {

    static final int PICK_UNIVERSITY = 2;

    private UserProfileContract.UserActionsListener mActionListener;

    EditText nameInput;
    EditText emailInput;
    EditText addressInput;
    TextView universityText;
    ImageView imageView;

    //////////////////////////
     RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    private  ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    private Map<String, Object> profile_data = new HashMap<>();
///////////////////////////////

    ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    switch (resultCode)
                    {
                        case PICK_UNIVERSITY:
                        {
                            universityText.setText(data.getStringExtra("result"));
                        }
                        break;
                        default:
                        {
                            if (data != null) {
                                Uri selectedImage = data.getData();
                                if (selectedImage != null) {
                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                                        imageView.setImageBitmap(bitmap);
                                    } catch (IOException ex) {
                                        Log.i("SelectPhoto", ex.getMessage());
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        addressInput = findViewById(R.id.addressInput);
        universityText = findViewById(R.id.universityText);
        imageView = findViewById(R.id.imageView);

        mActionListener = new UserProfilePresenter(this);

        //////////////////////////


        mRecyclerView = findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        myListAdapter = new MyListAdapter();

         ////////////////////////////
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionListener.fetchProfile();
            }
        });
    }

    public static final int PICK_IMAGE = 1;

    public void onUploadPhotoTapped(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mainActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    public void onUniversityTapped(View view) {
        Intent intent = new Intent(getApplicationContext(), UniversitiesActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        mainActivityResultLauncher.launch(intent);
    }

    public void onLogoutTapped(View view) {
        DatabaseManager.stopAllReplicationForCurrentUser();
        DatabaseManager.getSharedInstance().closePrebuiltDatabase();
        DatabaseManager.getSharedInstance().closeDatabaseForUser();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onSaveTapped(View view) {
        // tag::userprofile[]
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", nameInput.getText().toString());
        profile.put("email", emailInput.getText().toString());
        profile.put("address", addressInput.getText().toString());
        profile.put("university", universityText.getText().toString());
        profile.put("type", "user");
        byte[] imageViewBytes = getImageViewBytes();

        //////////


        List<Map<String, String>> jobs = new ArrayList<>();
        MyListAdapter adapter = (MyListAdapter) mRecyclerView.getAdapter();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            MyListAdapter.ViewHolder viewHolder = (MyListAdapter.ViewHolder) adapter.createViewHolder(mRecyclerView, adapter.getItemViewType(i));
            adapter.bindViewHolder(viewHolder, i);
            Map<String, String> task = new HashMap<>();
            task.put("Task", viewHolder.Task.getText().toString());
            task.put("Type", viewHolder.Type.getText().toString());
            task.put("Status", viewHolder.Status.getText().toString());
            jobs.add(task);
        }
// 將 tasks 存入 profile Map 中
        profile.put("jobs", jobs);

        /////////


        if (imageViewBytes != null) {
            profile.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
        }
        // end::userprofile[]

        mActionListener.saveProfile(profile);

        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();
    }


    //////
    public void Task_Tapped(View view) {
        Intent intent = new Intent(UserProfileActivity.this, Task_List.class);
     //   intent.putExtra("profile_data", (Serializable) profile_data);
        intent.putExtra("arrayList", arrayList);
        startActivity(intent);

    }


    //////


    private byte[] getImageViewBytes() {
        byte[] imageBytes = null;

        BitmapDrawable bmDrawable = (BitmapDrawable) imageView.getDrawable();

        if (bmDrawable != null) {
            Bitmap bitmap = bmDrawable.getBitmap();

            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageBytes = baos.toByteArray();
            }
        }
        return imageBytes;
    }

    @Override
    public void showProfile(Map<String, Object> profile) {
        nameInput.setText((String)profile.get("name"));
        emailInput.setText((String)profile.get("email"));
        addressInput.setText((String)profile.get("address"));

        String university = (String)profile.get("university");

        if (university != null && !university.isEmpty()) {
            universityText.setText(university);
        }

        Blob imageBlob = (Blob)profile.get("imageData");

        if (imageBlob != null) {
            Drawable d = Drawable.createFromStream(imageBlob.getContentStream(), "res");
            imageView.setImageDrawable(d);
        }

    }

    @Override
    public void makeData(Map<String, Object> profile) {

        profile_data=profile;

        List<Map<String, Object>> jobList = (List<Map<String, Object>>) profile.get("job");
        arrayList.clear();
        int i = 0;
        for (Map<String, Object> job : jobList) {
            HashMap<String, String> jobMap = new HashMap<>();
            jobMap.put("Id","JOB："+String.format("%02d",i+1));
            jobMap.put("TaskID", (String) job.get("TaskID"));
            jobMap.put("Type", (String) job.get("Type"));
            jobMap.put("Status", (String) job.get("Status"));
            arrayList.add(jobMap);
            i=i+1;
        }
//        mRecyclerView = findViewById(R.id.recycleview);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        myListAdapter = new MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);

    }


    //////////////////////
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{

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
