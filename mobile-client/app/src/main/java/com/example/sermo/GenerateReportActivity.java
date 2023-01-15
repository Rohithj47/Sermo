package com.example.sermo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateReportActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;

    enum RecordingType {
        PATIENT_RECORDING, SURGERY_CONVERSATION, DOCTOR_REVIEW
    }

    RecordingType selectedFile;
    HashMap<RecordingType, Uri> selectedFileUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);
        selectedFileUris = new HashMap<>();
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri sUri = data.getData();
                        File file = new File(sUri.getPath());
                        selectedFileUris.put(selectedFile, sUri);
                        TextView filepathDisplay;
                        switch (selectedFile.toString()) {
                            case "PATIENT_RECORDING":
                                filepathDisplay = findViewById(R.id.PatientInformationFilePath);
                                filepathDisplay.setText(file.getName());
                                break;
                            case "SURGERY_CONVERSATION":
                                filepathDisplay = findViewById(R.id.SurgeryRecordingFilePath);
                                filepathDisplay.setText(file.getName());
                                break;
                            case "DOCTOR_REVIEW":
                                filepathDisplay = findViewById(R.id.DoctorReviewFilePath);
                                filepathDisplay.setText(file.getName());
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    public void onPatientInformationButtonClick(View view) {
        selectedFile = RecordingType.PATIENT_RECORDING;
        handleFileRead();
    }

    public void onSurgeryRecordingButtonClick(View view) {
        selectedFile = RecordingType.SURGERY_CONVERSATION;
        handleFileRead();
    }

    public void onDoctorReviewButtonClick(View view) {
        selectedFile = RecordingType.DOCTOR_REVIEW;
        handleFileRead();
    }

    public void onGenerateButtonClick(View view) {
        TextView patientFullNameField = findViewById(R.id.PatientFullNameField);
        TextView patientAgeField = findViewById(R.id.PatientAgeField);
        RadioGroup patientGenderRadioGroup = findViewById(R.id.PatientGenderRadioGroup);
        int radioButtonID = patientGenderRadioGroup.getCheckedRadioButtonId();

        String fullName = patientFullNameField.getText().toString();
        String age = patientAgeField.getText().toString();

        if (fullName.equals("") || age.equals("") || radioButtonID == -1
                || !selectedFileUris.containsKey(RecordingType.PATIENT_RECORDING)
                || !selectedFileUris.containsKey(RecordingType.SURGERY_CONVERSATION)
                || !selectedFileUris.containsKey(RecordingType.DOCTOR_REVIEW)) {
            Toast toast = Toast.makeText(GenerateReportActivity.this,
                    "\nPlease fill all fields!\n", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        RadioButton radioButton = patientGenderRadioGroup.findViewById(radioButtonID);
        String gender = radioButton.getText().toString();
        RequestBody fullNameReqBod = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody phoneReqBod = RequestBody.create(MediaType.parse("text/plain"), age);
        RequestBody managerReqBod = RequestBody.create(MediaType.parse("text/plain"), gender);

        for (Map.Entry<RecordingType, Uri> entry : selectedFileUris.entrySet()) {
            Log.d(entry.getKey().toString(), entry.getValue().toString());
        }
        File patientRecording = new File(selectedFileUris.get(RecordingType.PATIENT_RECORDING)
                .getPath());
        File surgeryConversation = new File(selectedFileUris.get(RecordingType.SURGERY_CONVERSATION)
                .getPath());
        File doctorReview = new File(selectedFileUris.get(RecordingType.DOCTOR_REVIEW)
                .getPath());

        MediaType mediaType = MediaType.parse("audio/*");
        MultipartBody.Part[] fileParts = new MultipartBody.Part[3];
        RequestBody fileBody;

        try {
            fileBody = RequestBody.create(mediaType, readFileFromUri(
                    GenerateReportActivity.this,
                    selectedFileUris.get(RecordingType.PATIENT_RECORDING)));
            fileParts[0] = MultipartBody.Part.createFormData("patient_recording",
                    patientRecording.getName(), fileBody);

            fileBody = RequestBody.create(mediaType, readFileFromUri(
                    GenerateReportActivity.this,
                    selectedFileUris.get(RecordingType.SURGERY_CONVERSATION)));
            fileParts[1] = MultipartBody.Part.createFormData("surgery_conversation",
                    surgeryConversation.getName(), fileBody);

            fileBody = RequestBody.create(mediaType, readFileFromUri(
                    GenerateReportActivity.this,
                    selectedFileUris.get(RecordingType.DOCTOR_REVIEW)));
            fileParts[2] = MultipartBody.Part.createFormData("doctor_review",
                    doctorReview.getName(), fileBody);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button generateButton = findViewById(R.id.GenerateButton);
        ProgressBar reportGeneratingSpinner = findViewById(R.id.ReportGeneratingSpinner);
        generateButton.setVisibility(View.INVISIBLE);
        reportGeneratingSpinner.setVisibility(View.VISIBLE);

        API api = RetrofitClient.getInstance().getAPI();
        Call<ResponseBody> upload = api.generateReport(fileParts, fullNameReqBod, phoneReqBod, managerReqBod);
        upload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    generateButton.setVisibility(View.VISIBLE);
                    reportGeneratingSpinner.setVisibility(View.INVISIBLE);
                    Toast.makeText(GenerateReportActivity.this,
                            "\nSuccessfully generated the report!\n",
                            Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(getIntent());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ERROR", t.getMessage());
                generateButton.setVisibility(View.VISIBLE);
                reportGeneratingSpinner.setVisibility(View.INVISIBLE);
                int toastDuration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(GenerateReportActivity.this,
                        "\nEncountered an issue, please try again later!\n", toastDuration);
                toast.show();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            selectFile();
        } else {
            Toast.makeText(getApplicationContext(), "\nPermission Denied\n", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private static byte[] readFileFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor pdf = context.getContentResolver().openFileDescriptor(uri, "r");
        assert pdf != null;
        assert pdf.getStatSize() <= Integer.MAX_VALUE;
        byte[] data = new byte[(int) pdf.getStatSize()];
        FileDescriptor fd = pdf.getFileDescriptor();
        FileInputStream fileStream = new FileInputStream(fd);
        fileStream.read(data);
        return data;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        if (resultLauncher != null)
            resultLauncher.launch(intent);
    }

    private void handleFileRead() {
        if (ActivityCompat.checkSelfPermission(GenerateReportActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GenerateReportActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            selectFile();
        }
    }
}
