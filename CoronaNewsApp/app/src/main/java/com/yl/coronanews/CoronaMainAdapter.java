package com.yl.coronanews;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CoronaMainAdapter extends
        RecyclerView.Adapter<CoronaMainAdapter.ViewHolder> {

    List<CoronaStats> res;

    public CoronaMainAdapter(List<CoronaStats> list) {
        res = list;
    }

    public void setRes(List<CoronaStats> res) {
        this.res = res;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.custom_main_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        CoronaStats contact = res.get(position);

        // Set item views based on your views and data model
        TextView country = holder.country;
        country.setText(contact.getCountry());

        TextView confirmed = holder.confirmed;
        confirmed.setText(contact.getConfirmed());

        TextView deaths = holder.deaths;
        deaths.setText(contact.getDeaths());

        TextView recovered = holder.recovered;
        recovered.setText(contact.getRecovered());

        final String countryCode = contact.getCountryCode();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), NewsActivity.class);
                intent.putExtra("countryCode", countryCode);
                view.getContext().startActivity(intent);
            }
        });

        holder.viewNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), NewsActivity.class);
                intent.putExtra("countryCode", countryCode);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return res.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public View itemView;
        public TextView country, confirmed, deaths, recovered, countryCode;
        public Button viewNews;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            this.itemView = itemView;

            viewNews = itemView.findViewById(R.id.view_news);
            country = (TextView) itemView.findViewById(R.id.country_ph);
            confirmed = (TextView) itemView.findViewById(R.id.confirmed_ph);
            deaths = (TextView) itemView.findViewById(R.id.deaths_ph);
            recovered = (TextView) itemView.findViewById(R.id.recovered_ph);
            countryCode = (TextView) itemView.findViewById(R.id.country_code);

        }
    }
}