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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MaintenanceType;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.userprofile.R;
import com.couchbase.userprofile.Task_List;
import com.couchbase.userprofile.login.LoginActivity;
import com.couchbase.userprofile.universities.UniversitiesActivity;
import com.couchbase.userprofile.util.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import android.widget.VideoView;


public class UserProfileActivity
        extends AppCompatActivity
        implements UserProfileContract.View {

    static final int PICK_UNIVERSITY = 2;

    private UserProfileContract.UserActionsListener mActionListener;

    EditText nameInput;
    EditText emailInput;
    EditText departmentInput;
    Button button_task;
    TextView address;
    EditText address_v;


   // TextView universityText;
    ImageView imageView;
    VideoView videoView;

    private Map<String, Object> profile_data = new HashMap<>();

    private  ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;


    ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    switch (resultCode)
                    {
//                        case PICK_UNIVERSITY:
//                        {
//                            universityText.setText(data.getStringExtra("result"));
//                        }
//                        break;
                        default:
                        {
                            if (data != null) {
                                Uri selectedImage = data.getData();
                                ///////
                                try {
                                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                                    byte[] imageViewBytes = getBytes(imageStream);
                                    if (imageViewBytes != null) {
                                        profile_data.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
                                        mActionListener.saveProfile(profile_data);
                                    }
                                } catch (Exception e) {
                                    // Handle exception
                                }
//////
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
        Database database = DatabaseManager.getUserProfileDatabase();

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        departmentInput =findViewById(R.id.departmentInput);
        imageView = findViewById(R.id.imageView);
        address= findViewById(R.id.Address);
        address_v = findViewById(R.id.Address_V);
        button_task = (Button) findViewById(R.id.button_task);

        mActionListener = new UserProfilePresenter(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionListener.fetchProfile();
            }
        });

        // 查詢50分鐘內過期的DOCUMENT
        Instant fiveMinutesFromNow = Instant.now().plus(50, ChronoUnit.MINUTES);
        Query expiryQuery = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Meta.expiration.lessThan(Expression.doubleValue(fiveMinutesFromNow.toEpochMilli())));

        // 查询结果改變監聽器並print
        expiryQuery.addChangeListener(new QueryChangeListener() {
            @Override
            public void changed(QueryChange change) {
                ResultSet rows = change.getResults();
                Result row;
                while ((row = rows.next()) != null) {
                    System.out.println("Document ID: " + row.getString("id") + " will expire in less than 50 minutes.");
                }
            }
        });

        // 執行查詢
        try {
            expiryQuery.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static final int PICK_IMAGE = 1;

    public void onUploadPhotoTapped(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mainActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
//        byte[] imageViewBytes = getImageViewBytes();
//        if (imageViewBytes != null) {
//            profile_data.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
//            mActionListener.saveProfile(profile_data);
//        }
    }

//    public void onUniversityTapped(View view) {
//        Intent intent = new Intent(getApplicationContext(), UniversitiesActivity.class);
//        intent.setAction(Intent.ACTION_PICK);
//        mainActivityResultLauncher.launch(intent);
//    }

    public void onLogoutTapped(View view) {
        DatabaseManager.stopAllReplicationForCurrentUser();
        DatabaseManager.getSharedInstance().closePrebuiltDatabase();
        DatabaseManager.getSharedInstance().closeDatabaseForUser();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }




    public void Task_Tapped(View view) {
        Intent intent = new Intent(UserProfileActivity.this, Task_List.class);
        intent.putExtra("arrayList", arrayList);
        startActivity(intent);

    }

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
        profile_data=profile;
        nameInput.setText((String)profile.get("name"));
        emailInput.setText((String)profile.get("email"));
        departmentInput.setText((String)profile.get("department"));

        String address_Value = (String)profile.get("address");

        if(address_Value == null ){
            address.setVisibility(View.GONE);
            address_v.setVisibility(View.GONE);
        }
        else {
            // 如果EditText有值，則顯示TextView並將其內容設定為EditText的值
            address.setVisibility(View.VISIBLE);
            address_v.setVisibility(View.VISIBLE);
            address.setText("Address:");
            address_v.setText((String)profile.get("address"));

        }

//        String university = (String)profile.get("university");
        int originalJobCount = arrayList.size();

        List<Map<String, Object>> jobList = (List<Map<String, Object>>) profile.get("jobs");
       // int jobCount = jobList.size();
        arrayList.clear();
        int i = 0;
        int activeJobCount = 0;
        for (Map<String, Object> job : jobList) {
            HashMap<String, String> jobMap = new HashMap<>();

            jobMap.put("Task", (String) job.get("Task"));
            jobMap.put("Task_id", (String) job.get("Task_id"));
            jobMap.put("Create_time", (String) job.get("Create_time"));
            jobMap.put("level", (String) job.get("level"));
            jobMap.put("Address", (String) job.get("Address"));
            jobMap.put("Status", (String) job.get("Status"));

            arrayList.add(jobMap);
            i=i+1;

            String Status = (String) job.get("Status");
            if (!"Completed".equalsIgnoreCase(Status) && !"Done".equalsIgnoreCase(Status)) {
                activeJobCount++;
            }
        }

        button_task.setText("待辦事項" + String.format("(%d)", activeJobCount));

//        if (university != null && !university.isEmpty()) {
//            universityText.setText(university);
//        }

        Blob imageBlob = (Blob)profile.get("imageData");
        if (imageBlob != null) {
            Drawable d = Drawable.createFromStream(imageBlob.getContentStream(), "res");
            imageView.setImageDrawable(d);
        }
    }

    private File createTempFileFromInputStream(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("video", ".mp4"); // 創建臨時文件
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}
