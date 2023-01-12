package com.example.sermo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.Serializable;

public class ViewReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        Intent intent = getIntent();
        Report report = (Report) intent.getSerializableExtra("report");

        TextView patientNameDisplay = findViewById(R.id.PatientNameDisplay);
        patientNameDisplay.setText(patientNameDisplay.getText() + " " + report.getFull_name());

        TextView patientAgeDisplay = findViewById(R.id.PatientAgeDisplay);
        patientAgeDisplay.setText(patientAgeDisplay.getText() + " " + report.getAge());

        TextView patientGenderDisplay = findViewById(R.id.PatientGenderDisplay);
        patientGenderDisplay.setText(patientGenderDisplay.getText() + " " + report.getGender());

        TextView consultationSummaryContent = findViewById(R.id.ConsultationSummaryContent);
        consultationSummaryContent.setText(report.getConsultation_summary());

        TextView operationSummaryContent = findViewById(R.id.OperationSummaryContent);
        operationSummaryContent.setText(report.getOperation_summary());

        TextView doctorReviewContent = findViewById(R.id.DoctorReviewContent);
        doctorReviewContent.setText(report.getReview_summary());
    }
}