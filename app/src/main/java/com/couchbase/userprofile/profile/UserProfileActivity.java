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
    EditText departmentInput;



   // TextView universityText;
    ImageView imageView;

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
        departmentInput =findViewById(R.id.departmentInput);
        imageView = findViewById(R.id.imageView);

        mActionListener = new UserProfilePresenter(this);

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

//    public void onSaveTapped(View view) {
//        // tag::userprofile[]
//        profile_data.remove("jobs");
//        Map<String, Object> profile = new HashMap<>();
//        profile.put("name", nameInput.getText().toString());
//        profile.put("email", emailInput.getText().toString());
//        profile.put("address", addressInput.getText().toString());
//        profile.put("university", universityText.getText().toString());
//        profile.put("type", "user");
//        byte[] imageViewBytes = getImageViewBytes();
//
//        profile.put("jobs", arrayList);
//
//
//        if (imageViewBytes != null) {
//            profile.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
//        }
//        // end::userprofile[]
//
//        mActionListener.saveProfile(profile);
//
//        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();
//    }



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
//        String university = (String)profile.get("university");

        List<Map<String, Object>> jobList = (List<Map<String, Object>>) profile.get("jobs");
        arrayList.clear();
        int i = 0;
        for (Map<String, Object> job : jobList) {
            HashMap<String, String> jobMap = new HashMap<>();
 //           jobMap.put("Id","JOB："+String.format("%02d",i+1));
            jobMap.put("Task", (String) job.get("Task"));
            jobMap.put("Task_id", (String) job.get("Task_id"));
            jobMap.put("Create_time", (String) job.get("Create_time"));
            jobMap.put("Type", (String) job.get("Type"));
            jobMap.put("Address", (String) job.get("Address"));
            jobMap.put("Status", (String) job.get("Status"));
            arrayList.add(jobMap);
            i=i+1;
        }

//        if (university != null && !university.isEmpty()) {
//            universityText.setText(university);
//        }

        Blob imageBlob = (Blob)profile.get("imageData");

        if (imageBlob != null) {
            Drawable d = Drawable.createFromStream(imageBlob.getContentStream(), "res");
            imageView.setImageDrawable(d);
        }

    }

}
