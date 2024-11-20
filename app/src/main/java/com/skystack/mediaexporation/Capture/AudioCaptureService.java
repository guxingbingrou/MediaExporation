package com.skystack.mediaexporation.Capture;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.skystack.mediaexporation.Activties.CaptureActivity;
import com.skystack.mediaexporation.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioCaptureService extends Service implements AudioCaptureCallback {
    private static final String TAG = AudioCaptureService.class.getName();
    private MediaProjection mMediaProjection;
    private AudioCapture mAudioCapture;
    private File mAudioFile;
    private FileOutputStream mAudioOutputStream;

    public AudioCaptureService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 创建通知通道
     *
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);

        if (intent != null) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            if (mediaProjectionManager != null) {
                Bundle bundle = intent.getExtras();
                mMediaProjection = mediaProjectionManager.getMediaProjection(
                        bundle.getInt("resultCode", -1), bundle.getParcelable("data"));

                if (mMediaProjection == null) {
                    Log.e(TAG, "获取屏幕录制失败");
                } else {
                    Log.i(TAG, "获取屏幕录制成功");
                }
            }
        }

        InitCapture();

        return super.onStartCommand(intent, flags, startId);
    }

    private void InitCapture() {

        if (mAudioCapture == null) {
            mAudioCapture = new AudioCapture(this, 44100, 2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mAudioCapture.InitCapture(mMediaProjection);
            } else {
                mAudioCapture.SetAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
                mAudioCapture.InitCapture();
            }
        }

        if (mAudioFile == null) {
            mAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "audio_media.pcm");
        }
        if (mAudioOutputStream == null) {
            try {
                mAudioOutputStream = new FileOutputStream(mAudioFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        mAudioCapture.StartRecord();
    }


    @Override
    public void onDestroy() {
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
        if (mAudioOutputStream != null) {
            try {
                mAudioOutputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}