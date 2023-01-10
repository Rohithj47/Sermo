package com.example.sermo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ViewReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        Intent intent = getIntent();
        TextView textView = findViewById(R.id.reportTitle);
        Log.d("Object", String.valueOf(textView));
        textView.setText(intent.getStringExtra("tag"));
    }
}