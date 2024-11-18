package com.skystack.mediaexporation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.skystack.mediaexporation.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'mediaexporation' library on application startup.
    static {
        System.loadLibrary("mediaexporation");
    }

    private ActivityMainBinding binding;

    private final static String TAG = MainActivity.class.getName();
    static private final String[] PERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private final static int RequestCodePermissions = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (!CheckPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSION, RequestCodePermissions);
        }else{
            Init();
        }

    }

    private boolean CheckPermission(){
        boolean ret = true;
        for (String str : PERMISSION) {
            ret = ret && (ActivityCompat.checkSelfPermission(this, str) == PackageManager.PERMISSION_GRANTED);
        }
        return ret;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCodePermissions) {
            if (!CheckPermission()) {
                Log.e(TAG, "request permissions denied");
                Toast.makeText(this, "request permissions denied", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Init();
            }
        }
    }

    private void Init(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.capture:
                Toast.makeText(this, "capture", Toast.LENGTH_SHORT).show();
                CaptureActivity.IntentTo(this);
                break;
            case R.id.setting:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}