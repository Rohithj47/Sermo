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

import java.util.List;

public class QueryReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_reports);
    }

    public void addChipToGroup(View view) {
        TextView textView = findViewById(R.id.searchTextView);
        Chip chip = new Chip(QueryReportsActivity.this);
        chip.setText(textView.getText());
        chip.setCloseIconVisible(true);
        chip.setOnClickListener(v -> {
            ViewParent parent = v.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(v);
            }
        });
        ChipGroup chipGroup = findViewById(R.id.searchChipGroup);
        chipGroup.addView(chip);
        textView.setText("");
    }

    public void onQueryButtonClick(View view) {
        ChipGroup chipGroup = findViewById(R.id.searchChipGroup);
        chipGroup.chi
        List<Integer> chipIds = chipGroup.getCheckedChipIds();
        for (int id : chipIds) {
            Log.d("CHIP ID: ", Integer.toString(id));
        }
    }
}