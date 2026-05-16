package com.example.stegoapp.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stegoapp.R;
import com.example.stegoapp.crypto.CryptoUtils;
import com.example.stegoapp.databinding.FragmentEmbeddingBinding;
import com.example.stegoapp.stego.StegoUtils;
import com.example.stegoapp.util.ImageUtils;

public class EmbeddingFragment extends Fragment {
    private FragmentEmbeddingBinding binding;
    private Bitmap originalBitmap;
    private Bitmap embeddedBitmap;
    private ActivityResultLauncher<String> picker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String type = requireContext().getContentResolver().getType(uri);
                        if (type == null || !"image/png".equals(type)) {
                            Toast.makeText(requireContext(), getString(R.string.pick_png_error), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        originalBitmap = ImageUtils.loadPngSoftware(requireContext(), uri);
                        if (binding != null) binding.imagePreview.setImageURI(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmbeddingBinding.inflate(inflater, container, false);
        binding.btnSelectImage.setOnClickListener(v -> picker.launch("*/*"));
        binding.btnEmbed.setOnClickListener(v -> {
            String msg = binding.inputMessage.getText() != null ? binding.inputMessage.getText().toString() : "";
            String key = binding.inputKey.getText() != null ? binding.inputKey.getText().toString() : "";
            if (originalBitmap == null) {
                Toast.makeText(requireContext(), "Select a PNG image", Toast.LENGTH_SHORT).show();
                return;
            }
            if (msg.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a message", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                byte[] ct = CryptoUtils.encryptAES(msg, key);
                embeddedBitmap = StegoUtils.embedCiphertext(originalBitmap, ct);
                binding.imagePreview.setImageBitmap(embeddedBitmap);
                binding.btnSave.setEnabled(true);
                binding.btnShare.setEnabled(true);
                Toast.makeText(requireContext(), "Embedded", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), e.getMessage() != null ? e.getMessage() : "Error", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnSave.setOnClickListener(v -> {
            if (embeddedBitmap == null) return;
            Uri uri = ImageUtils.savePng(requireContext(), embeddedBitmap, "embedded.png");
            Toast.makeText(requireContext(), uri != null ? "Saved" : "Save failed", Toast.LENGTH_SHORT).show();
        });
        binding.btnShare.setOnClickListener(v -> {
            if (embeddedBitmap == null) return;
            Uri uri = ImageUtils.savePng(requireContext(), embeddedBitmap, "embedded.png");
            if (uri == null) { Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show(); return; }
            android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
            share.setType("*/*");
            share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            share.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(share, "Share embedded image"));
        });
        return binding.getRoot();
    }
}
