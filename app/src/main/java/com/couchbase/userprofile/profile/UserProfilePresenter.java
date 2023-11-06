package com.couchbase.userprofile.profile;

import com.couchbase.lite.Array;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MaintenanceType;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.URLEndpoint;
import com.couchbase.userprofile.util.DatabaseManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfilePresenter implements UserProfileContract.UserActionsListener {
    private UserProfileContract.View mUserProfileView;

    private Replicator replicator;

    public UserProfilePresenter(UserProfileContract.View mUserProfileView) {
        this.mUserProfileView = mUserProfileView;
        // 建一个 Replicator 對象
        Database database = DatabaseManager.getUserProfileDatabase();
        try {
            Endpoint targetEndpoint = new URLEndpoint(new URI("ws://192.168.11.108:4984"));
            ReplicatorConfiguration config = new ReplicatorConfiguration(database, targetEndpoint);
            replicator = new Replicator(config);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }



    // tag::fetchProfile[]
    public void fetchProfile()
    // end::fetchProfile[]
    {
        Database database = DatabaseManager.getUserProfileDatabase();

        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        // tag::livequerybuilder[]
        Query query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(database))
                        .where(Meta.id.equalTo(Expression.string(docId))); // <1>
        // end::livequerybuilder[]

        // tag::livequery[]
        query.addChangeListener(new QueryChangeListener() {

            @Override
            public void changed(QueryChange change) { // <1>
                ResultSet rows = change.getResults();

                Result row = null;
                Map<String, Object> profile = new HashMap<>(); // <2>

                profile.put("email", DatabaseManager.getSharedInstance().currentUser);

                while ((row = rows.next()) != null) {
                    Dictionary dictionary = row.getDictionary("userprofile"); // <3>

                    if (dictionary != null) {
                        profile.put("name", dictionary.getString("name")); // <4>
                        profile.put("address", dictionary.getString("address")); // <4>
                        profile.put("imageData", dictionary.getBlob("imageData")); // <4>
                        profile.put("university", dictionary.getString("university")); // <4>
                        profile.put("type", dictionary.getString("type")); // <4>
                        profile.put("Phone", dictionary.getString("Phone"));
                        profile.put("department", dictionary.getString("department"));
                        profile.put("VideoData", dictionary.getBlob("VideoData"));


                        ///////////////
                        // Get the "job" array from the document
//                        Array jobArray = dictionary.getArray("jobs");
//
//                        // Convert the job array to a List of Maps
//                        List<Map<String, Object>> jobList = new ArrayList<>();
//                        for (int i = 0; i < jobArray.count(); i++) {
//                            Dictionary jobDict = jobArray.getDictionary(i);
//                            Map<String, Object> jobMap = new HashMap<>();
//                            jobMap.put("Id","JOB："+String.format("%02d",i+1));
//                            jobMap.put("Task_id", jobDict.getString("Task_id"));
//                            jobMap.put("Task", jobDict.getString("Task"));
//                            jobMap.put("Address", jobDict.getString("Address"));
//                            jobMap.put("Type", jobDict.getString("Type"));
//                            jobMap.put("Status", jobDict.getString("Status"));
//                            jobMap.put("Create_time", jobDict.getString("Create_time"));
//                            jobMap.put("VideoData", jobDict.getBlob("VideoData"));
//                            jobMap.put("videoTimestamp", jobDict.getString("videoTimestamp"));
//                            jobList.add(jobMap);
//                        }
/////////////////////////////////
                        System.out.println("Document content: " + dictionary.toMap());
                        Array jobArray = dictionary.getArray("jobs");

// Convert the job array to a List of Maps
                        List<Map<String, Object>> jobList = new ArrayList<>();
                        for (int i = 0; i < jobArray.count(); i++) {
                            String taskId = jobArray.getString(i);

                            // Get the task document
                            Document taskDoc = database.getDocument(taskId);
                            if (taskDoc != null) {
                                // Convert the task document to a map
                                Map<String, Object> taskMap = taskDoc.toMap();

                                // Add the task map to the job list
                                jobList.add(taskMap);
                            }
                        }
                        profile.put("jobs", jobList);
                        //////////////
                    }
                }

                mUserProfileView.showProfile(profile);

            }
        });
        // end::livequery[]

        // 執行查詢
        try {
            query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }


    public void saveProfile(Map<String,Object> profile)
    {
        Database database = DatabaseManager.getUserProfileDatabase();
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();
        MutableDocument mutableDocument = new MutableDocument(docId, profile);

        try {
            database.save(mutableDocument);
            Instant ttl = Instant.now().plus(1, ChronoUnit.MINUTES);
            database.setDocumentExpiration(docId, new Date(ttl.toEpochMilli()));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }


    }


}

