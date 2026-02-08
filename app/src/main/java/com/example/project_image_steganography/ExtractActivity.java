package com.example.project_image_steganography;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;

public class ExtractActivity extends AppCompatActivity {
    private ShakeDetector shakeDetector;
    private ImageView imageView;
    private EditText outputText;
    private Bitmap selectedBitmap;
    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.stop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract);

        imageView = findViewById(R.id.img_extract_view);
        outputText = findViewById(R.id.output_extracted_binary);
        Button btnPick = findViewById(R.id.btn_pick_extract);
        Button btnExtract = findViewById(R.id.btn_extract_now);

        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            // Important: Make sure bitmap is mutable (editable) copy
                            Bitmap temp = BitmapFactory.decodeStream(inputStream);
                            selectedBitmap = temp.copy(Bitmap.Config.ARGB_8888, true);
                            imageView.setImageBitmap(selectedBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );


        shakeDetector = new ShakeDetector(this);
        shakeDetector.setOnShakeListener(() -> {
            selectedBitmap = null;
            imageView.setImageResource(R.drawable.ic_placeholder);
            outputText.setText("");
            Toast.makeText(this, "Image Removed!", Toast.LENGTH_SHORT).show();
        });
        // Make the back button work
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());


        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnExtract.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Pick an image first!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String hiddenBinary = ImageSteganography.extractText(selectedBitmap);
                outputText.setText(hiddenBinary);
                Toast.makeText(this, "Hidden Message Found!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                outputText.setText("Error: Could not find hidden data.");
            }
        });
    }
}