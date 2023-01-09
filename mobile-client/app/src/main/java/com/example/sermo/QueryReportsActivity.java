package com.example.sermo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;

import okhttp3.ResponseBody;
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
        chip.setSelected(true);
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
        for (String tag : this.tags) {
            Log.d("TAGS: ", tag);
        }
        API api = RetrofitClient.getInstance().getAPI();
        QueryBody queryBody = new QueryBody(this.tags);
        Call<ResponseBody> upload = api.queryByTags(queryBody);
        upload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("RES", response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ERROR", t.toString());
            }
        });
    }
}