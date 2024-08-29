package com.manhnt.rtc;

import android.content.Context;

import com.manhnt.config.Config;
import com.manhnt.shareroom.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import java.util.LinkedList;

public class Peer implements SdpObserver,PeerConnection.Observer {

    private Context context;
    private PeerConnection connection;
    private String id;
    private int endPoint;
    private RTCClient client;

    public Peer(Context context, RTCClient client, String id, int endPoint){
        this.context = context;
        this.client = client;
        this.id = id;
        this.endPoint = endPoint;
        PeerConnectionFactory peerConnectionFactory = client.getFactory();
        LinkedList<PeerConnection.IceServer> iceServers = client.getIceServers();
        MediaConstraints mediaConstraints = client.getConstraints();
        connection = peerConnectionFactory.createPeerConnection(iceServers, mediaConstraints, this);
        connection.addStream(client.getLocalMediaStream());
        client.getListener().onStatusChanged(context.getString(R.string.status_connected));
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            removeStream();
            client.getListener().onStatusChanged(context.getString(R.string.status_disconnect));
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {}

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        try {
            JSONObject content = new JSONObject();
            content.put("label", iceCandidate.sdpMLineIndex);
            content.put("id", iceCandidate.sdpMid);
            content.put("candidate", iceCandidate.sdp);

            JSONObject message = new JSONObject();
            message.put(Config.SOCKET_TO, id);
            message.put(Config.SOCKET_CALL_EVENT, Config.SOCKET_CALL_CANDIDATE_EVENT);
            message.put(Config.SOCKET_CONTENT, content);
            client.getSocket().emit(Config.SOCKET_CALL_MESSAGE, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        client.getListener().onAddRemoteStream(mediaStream, endPoint+1);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        removeStream();
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {}

    @Override
    public void onRenegotiationNeeded() {}

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        try {
            JSONObject content = new JSONObject();
            content.put(Config.SOCKET_CALL_EVENT, sessionDescription.type.canonicalForm());
            content.put(Config.SOCKET_SESSION_DESCRIPTION, sessionDescription.description);
            JSONObject message = new JSONObject();
            message.put(Config.SOCKET_TO, id);
            message.put(Config.SOCKET_CALL_EVENT, sessionDescription.type.canonicalForm());
            message.put(Config.SOCKET_CONTENT, content);
            client.getSocket().emit(Config.SOCKET_CALL_MESSAGE, message);
            connection.setLocalDescription(this, sessionDescription);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSetSuccess() {}

    @Override
    public void onCreateFailure(String s) {}

    @Override
    public void onSetFailure(String s) {}

    private void removeStream(){
        Peer peer = client.getPeers().get(id);
        client.getListener().onRemoveRemoteStream(peer.endPoint);
        peer.getConnection().close();
        client.getPeers().remove(peer.id);
        client.getEndPoints()[peer.endPoint] = false;
    }

    public PeerConnection getConnection(){
        return connection;
    }

}
