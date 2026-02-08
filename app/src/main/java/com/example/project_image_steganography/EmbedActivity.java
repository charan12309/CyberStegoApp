package com.example.project_image_steganography;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.io.OutputStream;

public class EmbedActivity extends AppCompatActivity {
    private ShakeDetector shakeDetector;
    private ImageView imageView;
    private TextView statusText;
    private Bitmap originalBitmap;
    private Bitmap secretBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embed);

        imageView = findViewById(R.id.image_view);
        Button btnPick = findViewById(R.id.btn_pick_image);
        EditText inputBinary = findViewById(R.id.input_binary_code);
        Button btnHide = findViewById(R.id.btn_hide_data);
        Button btnSave = findViewById(R.id.btn_save_image); // New Button
        statusText = findViewById(R.id.status_text);

        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            originalBitmap = BitmapFactory.decodeStream(inputStream);
                            // Make it mutable so we can edit pixels
                            originalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            imageView.setImageBitmap(originalBitmap);
                            statusText.setText("Status: Image Loaded!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnHide.setOnClickListener(v -> {
            if (originalBitmap == null) {
                Toast.makeText(this, "Please pick an image first!", Toast.LENGTH_SHORT).show();
                return;
            }
            String binaryData = inputBinary.getText().toString();
            if (binaryData.isEmpty()) {
                Toast.makeText(this, "Please paste binary code!", Toast.LENGTH_SHORT).show();
                return;
            }

            statusText.setText("Status: Hiding data...");
            // Hide the data
            secretBitmap = ImageSteganography.embedText(originalBitmap, binaryData);

            imageView.setImageBitmap(secretBitmap);
            statusText.setText("Status: Message Hidden! Now Click SAVE.");
            Toast.makeText(this, "Hidden! Don't forget to SAVE.", Toast.LENGTH_LONG).show();
        });

        // SAVE BUTTON LOGIC
        btnSave.setOnClickListener(v -> {
            if (secretBitmap == null) {
                Toast.makeText(this, "Hide message first!", Toast.LENGTH_SHORT).show();
                return;
            }
            saveImageToGallery(secretBitmap);
        });

        // Add this inside onCreate, usually at the bottom
        shakeDetector = new ShakeDetector(this);
        shakeDetector.setOnShakeListener(() -> {
            originalBitmap = null;
            secretBitmap = null;
            imageView.setImageResource(R.drawable.ic_placeholder); // Put the lock icon back
            statusText.setText("Status: Cleared due to Shake!");
            Toast.makeText(this, "Evidence Destroyed!", Toast.LENGTH_SHORT).show();
        });

        // Make the back button work
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

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
    private void saveImageToGallery(Bitmap bitmap) {
        // We MUST save as PNG. JPG compresses the image and destroys the hidden code!
        String filename = "Stego_" + System.currentTimeMillis() + ".png";

        OutputStream fos;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Steganography"); // Save to specific folder

            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            fos = getContentResolver().openOutputStream(imageUri);

            // 100% Quality PNG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(this, "Saved to Gallery!", Toast.LENGTH_LONG).show();
            statusText.setText("Status: Saved to Pictures/Steganography");
        } catch (Exception e) {
            Toast.makeText(this, "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}