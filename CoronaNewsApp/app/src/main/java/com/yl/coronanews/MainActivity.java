package com.yl.coronanews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CoronaMainAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.mainRv);

        // Initialize contacts
        // Create adapter passing in the sample user data
        adapter = new CoronaMainAdapter(new ArrayList<CoronaStats>());
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    makeGetRequest();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void makeGetRequest() throws MalformedURLException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://9i9apdrxr1.execute-api.us-east-1.amazonaws.com/default/corona-endpoint";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        Log.i("test", "Response is: "+ response.substring(0,500));

                            String[] arr = response.split("\\(");
                            List<CoronaStats> arrResult = new ArrayList<>();
                            for (int i = 1; i < arr.length; i++) {
                                String tmp = arr[i];
                                tmp = tmp.replace("(", "");
                                tmp = tmp.replace(")", "");
                                String[] tmpArr = tmp.split(",");
                                CoronaStats coronaStats = new CoronaStats(tmpArr[0], tmpArr[1].replaceAll("'", ""), tmpArr[2], tmpArr[3], tmpArr[4].replaceAll("'", ""));
                                arrResult.add(coronaStats);
                            }
                            adapter.setRes(arrResult);
                            adapter.notifyDataSetChanged();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }




}
