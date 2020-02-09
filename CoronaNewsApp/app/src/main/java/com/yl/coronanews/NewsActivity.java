package com.yl.coronanews;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    CoronaNewsAdapter adapter;

    String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Bundle b = getIntent().getExtras();
        countryCode = b.getString("countryCode").replaceAll("'", "");

        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.newsRv);

        // Initialize contacts
        // Create adapter passing in the sample user data
        adapter = new CoronaNewsAdapter(new ArrayList<String>());
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
        String url ="https://wkmt4gruyc.execute-api.us-east-1.amazonaws.com/default/corona-news-endpoint?country_code=" + countryCode;

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        Log.i("test", "Response is: "+ response.substring(0,500));

                        String[] arr = response.split("\\(");
                        List<String> arrResult = new ArrayList<>();
                        for (int i = 1; i < arr.length; i++) {
                            String tmp = arr[i];
                            tmp = tmp.replace("(", "");
                            tmp = tmp.replace(")", "");
                            String[] tmpArr = tmp.split(",");
//                            CoronaStats coronaStats = new CoronaStats(tmpArr[0], tmpArr[1].replaceAll("'", ""), tmpArr[2], tmpArr[3], tmpArr[4].replaceAll("'", ""));

                            arrResult.add(tmpArr[1].replaceAll("'", "").trim());
                        }

                        HashSet set = new HashSet<>(arrResult);
                        adapter.setRes(new ArrayList<String>(set));
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
