package com.example.sermo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateReportActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;
    ImageButton btSelect;

    private static byte[] readUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor pdf = context.getContentResolver().openFileDescriptor(uri, "r");

        assert pdf != null;
        assert pdf.getStatSize() <= Integer.MAX_VALUE;
        byte[] data = new byte[(int) pdf.getStatSize()];

        FileDescriptor fd = pdf.getFileDescriptor();
        FileInputStream fileStream = new FileInputStream(fd);
        fileStream.read(data);

        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        btSelect = findViewById(R.id.PatientInformationSearchButton);

        resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Initialize result data
                    Intent data = result.getData();
                    // When data is not equal to empty
                    if (data != null) {
                        // Get PDf uri
                        Uri sUri = data.getData();
                        // Get PDF path
                        String sPath = sUri.getPath();
                        File file = new File(sUri.getPath());
                        final String[] split = file.getPath().split(":");
                        String filePath = split[1];
                        Log.d("SELECTED FILE", sPath + ", " + sPath + ", " + filePath + ", " + sUri);
                        Cursor returnCursor =
                                getContentResolver().query(sUri, null, null, null, null);
                        /*
                         * Get the column indexes of the data in the Cursor,
                         * move to the first row in the Cursor, get the data,
                         * and display it.
                         */
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                        Log.d("SELECTED FILE INFO", returnCursor.getString(nameIndex) + ", " + Long.toString(returnCursor.getLong(sizeIndex)));
                        TextView tv = findViewById(R.id.PatientInformationFilePath);
                        tv.setText(returnCursor.getString(nameIndex));
                        File f = new File(returnCursor.getString(nameIndex));
                        Log.d("SELECTED FILE PATH", f.getAbsolutePath());
                        RequestBody reqBody = null;
                        try {
                            reqBody = RequestBody.create(MediaType.parse("multipart/form-file"), readUri(GenerateReportActivity.this, sUri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MultipartBody.Part partImage = MultipartBody.Part.createFormData("file", file.getName(), reqBody);
                        API api = RetrofitClient.getInstance().getAPI();
                        Call<ResponseBody> upload = api.uploadImage(partImage);
                        upload.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.isSuccessful()) {
                                    Toast.makeText(GenerateReportActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                    Intent main = new Intent(GenerateReportActivity.this, GenerateReportActivity.class);
                                    main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(main);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(GenerateReportActivity.this, "Request failed" + t.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When permission is not granted
                if (ActivityCompat.checkSelfPermission(GenerateReportActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Result permission
                    ActivityCompat.requestPermissions(
                            GenerateReportActivity.this,
                            new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                            1);
                }
                // When permission is granted
                else {
                    // Create method
                    selectFile();
                }
            }
        });
    }

    private void selectFile() {
        // Initialize intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType("application/pdf");
        // Launch intent
        if(resultLauncher !=null)
            resultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // When permission is granted
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            // Call method
            selectFile();
        }
        // When permission is denied
        else {
            // Display toast
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
