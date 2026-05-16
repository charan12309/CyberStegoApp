package com.example.stegoapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.stegoapp.databinding.ActivityMainBinding;
import com.example.stegoapp.ui.EmbeddingFragment;
import com.example.stegoapp.ui.ExtractionFragment;
import com.example.stegoapp.ui.UserInfoFragment;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private float lastAccel = 0f;
    private float accel = 0f;
    private float lastAccelCurrent = 0f;
    private float shakeThreshold = 12f;
    private boolean loggedOutByShake = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (session.getLoggedInUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Fragment[] fragments = new Fragment[]{new EmbeddingFragment(), new ExtractionFragment(), new UserInfoFragment()};
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments[position];
            }
            @Override
            public int getItemCount() {
                return fragments.length;
            }
        });
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) tab.setText(R.string.tab_embedding);
            else if (position == 1) tab.setText(R.string.tab_extraction);
            else tab.setText(R.string.tab_user_info);
        }).attach();
        binding.viewPager.setOffscreenPageLimit(3);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = 0.0f;
        lastAccel = SensorManager.GRAVITY_EARTH;
        lastAccelCurrent = SensorManager.GRAVITY_EARTH;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loggedOutByShake = false;
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            lastAccelCurrent = lastAccel;
            lastAccel = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = lastAccel - lastAccelCurrent;
            accel = accel * 0.9f + delta;
            if (accel > shakeThreshold && !loggedOutByShake) {
                loggedOutByShake = true;
                SessionManager session = new SessionManager(this);
                session.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }
    }
}
