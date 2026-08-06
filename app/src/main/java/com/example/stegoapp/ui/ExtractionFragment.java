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
import com.example.stegoapp.databinding.FragmentExtractionBinding;
import com.example.stegoapp.stego.StegoUtils;
import com.example.stegoapp.util.ExecutorProvider;
import com.example.stegoapp.util.ImageUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class ExtractionFragment extends Fragment {
    private FragmentExtractionBinding binding;
    private Bitmap stegoBitmap;
    private ActivityResultLauncher<String> picker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        stegoBitmap = ImageUtils.loadPngSoftware(requireContext(), uri);
                        if (binding != null) binding.imageStegoPreview.setImageURI(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExtractionBinding.inflate(inflater, container, false);
        binding.btnSelectStegoImage.setOnClickListener(v -> picker.launch("*/*"));
        binding.btnDecrypt.setOnClickListener(v -> {
            String key = binding.inputExtractKey.getText() != null ? binding.inputExtractKey.getText().toString() : "";
            if (stegoBitmap == null) {
                Toast.makeText(requireContext(), "Select a PNG image", Toast.LENGTH_SHORT).show();
                return;
            }
            // Key derivation (PBKDF2) is deliberately slow, so run crypto off the UI thread.
            final Bitmap source = stegoBitmap;
            ExecutorProvider.get().execute(() -> {
                try {
                    byte[] ct = StegoUtils.extractCiphertext(source);
                    String msg = CryptoUtils.decryptAES(ct, key);
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        if (binding != null) binding.outputMessage.setText(msg);
                    });
                } catch (Exception e) {
                    // AEADBadTagException (wrong key or tampered image) extends BadPaddingException.
                    boolean isBadKey = e instanceof BadPaddingException
                            || e instanceof IllegalBlockSizeException
                            || (e.getMessage() != null && e.getMessage().toUpperCase().contains("BAD_DECRYPT"));
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        if (binding != null) binding.outputMessage.setText("");
                        Toast.makeText(requireContext(),
                                isBadKey ? "Incorrect key or modified image"
                                         : (e.getMessage() != null ? e.getMessage() : "Error"),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        return binding.getRoot();
    }
}
