package com.example.sermo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class GenerateReportActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> resultLauncher;
    ImageButton btSelect;

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
                        Log.d("SELECTED FILE", sPath + ", " + sPath + ", " + filePath);
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
