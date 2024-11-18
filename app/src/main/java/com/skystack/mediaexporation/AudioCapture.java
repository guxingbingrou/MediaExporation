package com.skystack.mediaexporation;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioCapture {
    private static final String TAG = AudioCapture.class.getName();
    private final int mSampleRate;
    private final int mChannels;
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private AudioCaptureCallback mCallback;

    private AudioRecordThread mRecordThread = null;
    private byte[] mBuffer = null;

    public AudioCapture(AudioCaptureCallback callback, int mSampleRate, int mChannels) {
        this.mCallback = callback;
        this.mSampleRate = mSampleRate;
        this.mChannels = mChannels;

        InitCapture();
    }

    private int ChannelCountToConfiguration(int channels) {
        return (channels == 1 ? android.media.AudioFormat.CHANNEL_IN_MONO : android.media.AudioFormat.CHANNEL_IN_STEREO);
    }

    @SuppressLint("MissingPermission")
    public void InitCapture() {
        int channelConfig = ChannelCountToConfiguration(mChannels);

        int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, channelConfig, mAudioFormat);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "AudioRecord.getMinBufferSize failed: " + bufferSize);
            return;
        }

        mBuffer = new byte[bufferSize];
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, channelConfig, mAudioFormat, bufferSize);

        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG,"Failed to create a new AudioRecord instance");
            ReleaseAudioResources();
            return;
        }

    }

    public boolean StartRecord(){
        if(mAudioRecord == null) return false;

        if(mRecordThread != null) return false;

        try {
            mAudioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.e(TAG,"AudioRecord.startRecording failed: " + e.getMessage());
            return false;
        }
        if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            Log.e(TAG, "AudioRecord.startRecording failed - incorrect state :"
                    + mAudioRecord.getRecordingState());
            return false;
        }
        mRecordThread = new AudioRecordThread();
        mRecordThread.start();

        return true;
    }

    public boolean StopRecording() {
        Log.d(TAG, "stopRecording");
        if(mRecordThread == null)
            return true;
        mRecordThread.StopThread();
        try {
            mRecordThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mRecordThread = null;
        Log.d(TAG, "stopRecording done");
        return true;
    }

    public boolean DestroyRecording(){
        StopRecording();
        ReleaseAudioResources();
        return true;
    }

    private void ReleaseAudioResources() {
        Log.d(TAG, "releaseAudioResources");
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if(mBuffer != null){
            mBuffer = null;
        }
    }


    private class AudioRecordThread extends Thread {
        private volatile boolean mKeepAlive = true;

        @Override
        public void run() {
            while (mKeepAlive){
                int bytesRead = mAudioRecord.read(mBuffer, 0, mBuffer.length);
                if(bytesRead == mBuffer.length){
                    if(mCallback != null){
                        mCallback.OnAudioDataAvailable(mBuffer);
                    }
                } else {
                    String errorMessage = "AudioRecord.read failed: " + bytesRead;
                    Log.e(TAG, errorMessage);
                    if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        mKeepAlive = false;
                        Log.e(TAG, errorMessage);
                    }
                }
            }

            try {
                if(mAudioRecord != null){
                    mAudioRecord.stop();
                }
            }catch (IllegalStateException e){
                Log.e(TAG, "AudioRecord.stop failed: " + e.getMessage());
            }
        }

        public void StopThread() {
            Log.d(TAG, "stopThread");
            mKeepAlive = false;
        }
    }

    public interface AudioCaptureCallback{
        void OnAudioDataAvailable(byte[] data);
    }

}
