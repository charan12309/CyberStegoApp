package com.example.stegoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stegoapp.LoginActivity;
import com.example.stegoapp.SessionManager;
import com.example.stegoapp.data.AppDatabase;
import com.example.stegoapp.data.UserDao;
import com.example.stegoapp.databinding.FragmentUserInfoBinding;

import com.example.stegoapp.util.ExecutorProvider;

public class UserInfoFragment extends Fragment {
    private FragmentUserInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false);
        SessionManager session = new SessionManager(requireContext());
        UserDao dao = AppDatabase.get(requireContext()).userDao();
        String username = session.getLoggedInUser() != null ? session.getLoggedInUser() : "";
        binding.textUsername.setText(username);
        binding.btnLogout.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        binding.btnDeleteAccount.setOnClickListener(v -> {
            ExecutorProvider.get().execute(() -> {
                dao.deleteByUsername(username);
                session.logout();
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                });
            });
        });
        return binding.getRoot();
    }
}
