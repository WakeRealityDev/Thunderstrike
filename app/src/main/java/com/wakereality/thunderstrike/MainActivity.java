package com.wakereality.thunderstrike;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wakereality.thunderstrike.storypresentation.RemoteSimpleActivity;

/*
There is nothing of learning importance on this page. It merely checks that external (/sdcard/) file
  permissions are granted and offers a choice of which activity to open.
 */
public class MainActivity extends AppCompatActivity {

    protected View rootView;
    public static boolean applicationPermissionWriteStorageConfirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.activity_main);

        permissionCheck();
        checkIfPermissionsReady();
    }

    public void launchClick(View view) {
        Activity parentActivity = this;
        Intent storyActivityIntent = new Intent(parentActivity, RemoteSimpleActivity.class);
        switch (view.getId()) {
            case R.id.launchClickButton0:
                break;
            case R.id.launchClickButton1:
                break;
        }
        parentActivity.startActivity(storyActivityIntent);
        Log.i("MainActivity", "[startActivity] startActivity");
    }


    public void checkIfPermissionsReady()
    {
        if (applicationPermissionWriteStorageConfirmed)
        {
            if (rootView != null) {
                rootView.findViewById(R.id.launchClickButton1).setVisibility(View.VISIBLE);
                //  rootView.findViewById(R.id.button_permissions_ready0).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.button_permissions_ready0).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("MainActivity", "click on GO! button");
                        launchClick(v);
                    }
                });
                rootView.findViewById(R.id.message_file_permissions_need0).setVisibility(View.GONE);
                rootView.findViewById(R.id.message_file_permissions_ready0).setVisibility(View.VISIBLE);
            }
        }
    }


    protected void fastRecreate(final long timeDelay) {
        if (rootView != null) {
            rootView.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, timeDelay);
        } else {
            Log.e("MainActivity", "rootView0 is null, no fastRecreate() of Activity");
        }
    }


    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;

    protected void permissionCheck() {
        if (! checkPermissionForExternalStorage(getApplicationContext())) {
            Log.w("MainActivity", "[enginePrep][activityPrep] Need file permission");
            requestPermissionForExternalStorage();
        } else {
            Log.i("MainActivity", "[enginePrep][activityPrep] file permission good!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissionForExternalStorage(getApplicationContext())) {
            Log.i("MainActivity", "[enginePrep][activityPrep] file permission granted!");
            fastRecreate(300L);
        } else {
            Log.w("MainActivity", "[enginePrep][activityPrep] file permission denied!");
        }
    }

    public static boolean checkPermissionForExternalStorage(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "[internalConfig] applicationPermissionWriteStorageConfirmed on EventBus");
            // postStick so starting service gets it
            applicationPermissionWriteStorageConfirmed = true;
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.w("AndroidPermission", "External Storage permission needed. Please allow in App Settings for additional functionality.");
            Toast.makeText(this, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

}
