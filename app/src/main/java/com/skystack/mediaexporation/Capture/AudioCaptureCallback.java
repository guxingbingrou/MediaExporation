package com.skystack.mediaexporation.Capture;

public interface AudioCaptureCallback {
    void OnAudioDataAvailable(byte[] data);
}
