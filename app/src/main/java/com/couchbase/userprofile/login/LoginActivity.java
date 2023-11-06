package com.couchbase.userprofile.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MaintenanceType;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.userprofile.R;
import com.couchbase.userprofile.profile.UserProfileActivity;
import com.couchbase.userprofile.util.DatabaseManager;

import java.util.List;

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
                usernameInput.setText("test111@mail");
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

//            Database database = DatabaseManager.getUserProfileDatabase();
//            Query query = QueryBuilder.select(SelectResult.all())
//                    .from(DataSource.database(database));


            // 以下註解可看lite中doc內容
//            String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();
//            Query query1 = QueryBuilder.select(SelectResult.all())
//                    .from(DataSource.database(database))
//                    .where(Meta.id.equalTo(Expression.string(docId)));
//            try {
//                ResultSet result = query1.execute();
//                int rowCount = 0;
//                for (Result row : result) {
//                    Log.i("Query Result_1", row.toMap().toString());
//                    rowCount++;
//                }
//                Log.i("Query Result", "Number of rows ::  " + rowCount);
//            } catch (CouchbaseLiteException e) {
//                e.printStackTrace();
//                // Handle the exception
//            }

           // DatabaseManager.compactUserDatabases(context);
            DatabaseManager.startPushAndPullReplicationForCurrentUser(username, password, new Runnable() {
                @Override
                public void run() {
                    // 同步完成后的回調
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });

        }
    }


}
