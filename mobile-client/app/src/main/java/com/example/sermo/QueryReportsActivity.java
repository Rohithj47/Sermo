package com.example.sermo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueryReportsActivity extends AppCompatActivity {

    private HashSet<String> tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_reports);
        this.tags = new HashSet<>();
    }

    public void addChipToGroup(View view) {
        TextView textView = findViewById(R.id.searchTextView);
        Chip chip = new Chip(QueryReportsActivity.this);
        chip.setText(textView.getText());
        this.tags.add(textView.getText().toString());
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.primary_text)));
        chip.setOnClickListener(v -> {
            ViewParent parent = v.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(v);
                String chipText = ((Chip) v).getText().toString();
                this.tags.remove(chipText);
            }
        });
        ChipGroup chipGroup = findViewById(R.id.searchChipGroup);
        chipGroup.addView(chip);
        textView.setText("");
    }

    public void onQueryButtonClick(View view) {
        Button queryButton = findViewById(R.id.QueryButton);
        ProgressBar reportQueryingSpinner = findViewById(R.id.ReportQueryingSpinner);
        queryButton.setVisibility(View.INVISIBLE);
        reportQueryingSpinner.setVisibility(View.VISIBLE);
        TextView noDataDisplay = findViewById(R.id.NoDataDisplay);
        noDataDisplay.setVisibility(View.INVISIBLE);

        API api = RetrofitClient.getInstance().getAPI();
        QueryBody queryBody = new QueryBody(this.tags);

        Call<List<ReportsCollection>> upload = api.searchReports(queryBody);
        upload.enqueue(new Callback<List<ReportsCollection>>() {
            @Override
            public void onResponse(Call<List<ReportsCollection>> call,
                                   Response<List<ReportsCollection>> response) {
                queryButton.setVisibility(View.VISIBLE);
                reportQueryingSpinner.setVisibility(View.INVISIBLE);
                if (response.body().get(0).getReports().size() == 0) {
                    LinearLayout searchResultsContainer = findViewById(R.id.searchResultsContainer);
                    searchResultsContainer.removeAllViews();
                    noDataDisplay.setVisibility(View.VISIBLE);
                } else {
                    populateSearchResultsContainer(view, response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ReportsCollection>> call, Throwable t) {
                LinearLayout searchResultsContainer = findViewById(R.id.searchResultsContainer);
                searchResultsContainer.removeAllViews();
                Log.d("ERROR", t.getMessage());
                queryButton.setVisibility(View.VISIBLE);
                reportQueryingSpinner.setVisibility(View.INVISIBLE);
                noDataDisplay.setVisibility(View.VISIBLE);
                int toastDuration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(QueryReportsActivity.this,
                        "\nEncountered an issue, please try again later!\n", toastDuration);
                toast.show();
            }
        });
    }

    private void populateSearchResultsContainer(View view, List<ReportsCollection> reportsCollection) {
        LinearLayout searchResultsContainer = findViewById(R.id.searchResultsContainer);
        searchResultsContainer.removeAllViews();
        for (Report report : reportsCollection.get(0).getReports()) {
            TextView textView = new TextView(QueryReportsActivity.this);
            String textViewContent = ""
                    .concat("Name: ").concat(report.getFull_name()).concat("\n")
                    .concat("Age (").concat(report.getAge()).concat("), ")
                    .concat("Gender (").concat(report.getGender()).concat(")")
                    .concat("\n").concat("\n");
            textView.setText(textViewContent);
            textView.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            textView.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewReportActivity.class);
                intent.putExtra("report", report);
                startActivity(intent);
            });
            searchResultsContainer.addView(textView);
        }
    }
}