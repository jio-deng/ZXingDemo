package com.dz.zxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dz.zxing.gsydemo.activity.NormalVideoActivity;
import com.google.zxing.client.android.CaptureActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_zxing).setOnClickListener(this);
        findViewById(R.id.btn_second).setOnClickListener(this);
        findViewById(R.id.btn_texture).setOnClickListener(this);
        findViewById(R.id.btn_gsy).setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, CaptureActivity.class));
            }
        } else if (requestCode == 2) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, LiveCameraActivity.class));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_zxing:
                if (PermissionUtils.getPermission(this, Manifest.permission.CAMERA, 1)) {
                    startActivity(new Intent(this, CaptureActivity.class));
                }
                break;
            case R.id.btn_second:
                startActivity(new Intent(this, SecondActivity.class));
                break;
            case R.id.btn_texture:
                if (PermissionUtils.getPermission(this, Manifest.permission.CAMERA, 2)) {
                    startActivity(new Intent(this, LiveCameraActivity.class));
                }
                break;
            case R.id.btn_gsy:
                if (PermissionUtils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)) {
                    startActivity(new Intent(this, NormalVideoActivity.class));
                }
                break;
        }
    }
}
