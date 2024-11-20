package com.skystack.mediaexporation.Activties;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.skystack.mediaexporation.Capture.AudioCapture;
import com.skystack.mediaexporation.Capture.AudioCaptureCallback;
import com.skystack.mediaexporation.Capture.AudioCaptureService;
import com.skystack.mediaexporation.databinding.ActivityCaptureBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CaptureActivity extends AppCompatActivity implements AudioCaptureCallback {
    private static final String TAG = CaptureActivity.class.getName();
    private ActivityCaptureBinding binding;

    private AudioCapture mAudioCapture;
    private File mAudioFile;
    private FileOutputStream mAudioOutputStream;
    ActivityResultLauncher mLauncher;
    Intent mAudioCaptureIntent;

    public static Intent NewIntent(Context context){
        Intent intent = new Intent(context, CaptureActivity.class);
        return intent;
    }
    public static void IntentTo(Context context){
        context.startActivity(NewIntent(context));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CaptureActivity captureActivity = this;

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK){
                            if(result.getData() != null){
                                mAudioCaptureIntent = new Intent(captureActivity, AudioCaptureService.class);
                                mAudioCaptureIntent.putExtra("resultCode", result.getResultCode());
                                mAudioCaptureIntent.putExtra("data", result.getData());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(mAudioCaptureIntent);
                                }

                                Log.i(TAG, "获取屏幕录制权限成功");
                                binding.buttonMedia.setText("停止采集");

                            }
                        }else{
                            Log.e(TAG, "获取屏幕录制权限失败");
                            binding.buttonMedia.setText("采集媒体");
                        }
                    }
                }
        );

        InitButtons();

    }

    private void InitButtons(){
        CaptureActivity captureActivity = this;
        binding.buttonMic.setOnClickListener(new View.OnClickListener() {
            boolean mIsCapture = false;
            @Override
            public void onClick(View v) {
                mIsCapture = !mIsCapture;

                if(mIsCapture){
                    if(mAudioCapture == null){
                        mAudioCapture = new AudioCapture(captureActivity, 44100, 2);
                        mAudioCapture.InitCapture();
                    }

                    if(mAudioFile == null) {
                        mAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "audio_mic.pcm");
                    }
                    if(mAudioOutputStream == null){
                        try {
                            mAudioOutputStream = new FileOutputStream(mAudioFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    mAudioCapture.StartRecord();
                    binding.buttonMic.setText("停止采集");
                }else{
                    mAudioCapture.StopRecord();
                    binding.buttonMic.setText("采集Mic");

                    if(mAudioOutputStream != null){
                        try {
                            mAudioOutputStream.close();
                            mAudioOutputStream = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });

        binding.buttonMedia.setOnClickListener(new View.OnClickListener() {
            boolean mIsCapture = false;
            @Override
            public void onClick(View v) {
                mIsCapture = !mIsCapture;

                if(mIsCapture){
                    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                    Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
                    mLauncher.launch(screenCaptureIntent);

                }else{
                    if(mAudioCaptureIntent != null){
                        stopService(mAudioCaptureIntent);
                        mAudioCaptureIntent = null;
                    }

                    binding.buttonMedia.setText("采集媒体");
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioCapture != null) {
            mAudioCapture.DestroyRecord();
            mAudioCapture = null;
        }

        if(mAudioOutputStream != null){
            try {
                mAudioOutputStream.close();
                mAudioOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void OnAudioDataAvailable(byte[] data) {
        Log.i(TAG, "OnAudioDataAvailable： " + data.length);
        if(mAudioOutputStream != null){
            try {
                mAudioOutputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}