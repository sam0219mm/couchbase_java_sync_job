package com.couchbase.userprofile.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.couchbase.userprofile.R;
import com.couchbase.userprofile.profile.UserProfileActivity;
import com.couchbase.userprofile.util.DatabaseManager;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    EditText passwordInput;
    AppCompatImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        //設定icon大小
        imageView = findViewById(R.id.imageViewLogo);
        Drawable envelope = getResources().getDrawable(R.drawable.envelope);
        envelope.setBounds(0,0,80,80);
        usernameInput.setCompoundDrawables(envelope,null,null,null);

        Drawable key = getResources().getDrawable(R.drawable.key);
        key.setBounds(0,0,80,80);
        passwordInput.setCompoundDrawables(key,null,null,null);

        //makes logging in easier for testing
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameInput.setText("sam@com");
                passwordInput.setText("password");
            }
        });
    }

    public void onLoginTapped(View view) {
        if (usernameInput.length() > 0 && passwordInput.length() > 0) {
            DatabaseManager dbMgr = DatabaseManager.getSharedInstance();
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            Context context = getApplicationContext();

            dbMgr.initCouchbaseLite(context);
            dbMgr.openPrebuiltDatabase(context);
            dbMgr.openOrCreateDatabaseForUser(context, username);

            DatabaseManager.startPushAndPullReplicationForCurrentUser(username, password);

            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
