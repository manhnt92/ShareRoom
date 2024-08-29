package com.manhnt.rtc;

import org.webrtc.MediaStream;

public interface RTCListener {

    void onCallReady();

    void onStatusChanged(String newStatus);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(int endPoint);

}
