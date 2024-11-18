package com.skystack.mediaexporation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.skystack.mediaexporation.databinding.ActivityCaptureBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CaptureActivity extends AppCompatActivity implements AudioCapture.AudioCaptureCallback{
    private static final String TAG = CaptureActivity.class.getName();
    private ActivityCaptureBinding binding;

    private AudioCapture mAudioCapture;
    private File mAudioFile;
    private FileOutputStream mAudioOutputStream;

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

        mAudioCapture = new AudioCapture(this, 44100, 2);

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            boolean mIsCapture = false;
            @Override
            public void onClick(View v) {
                mIsCapture = !mIsCapture;

                if(mIsCapture){
                    if(mAudioFile == null) {
                        mAudioFile = new File(Environment.getExternalStorageDirectory(), "audio.pcm");
                    }
                    if(mAudioOutputStream == null){
                        try {
                            mAudioOutputStream = new FileOutputStream(mAudioFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    mAudioCapture.StartRecord();
                    binding.buttonStart.setText("停止采集");
                }else{
                    mAudioCapture.StopRecording();
                    binding.buttonStart.setText("采集音频");

                    if(mAudioFile != null){
                        try {
                            mAudioOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });

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