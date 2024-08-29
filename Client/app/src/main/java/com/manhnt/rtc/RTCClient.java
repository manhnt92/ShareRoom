package com.manhnt.rtc;

import android.content.Context;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.config.Config;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.LinkedList;

public class RTCClient {

    private Context context;
    private Socket mSocket;
    private RTCListener mListener;
    private PeerConnectionParams params;
    private LinkedList<PeerConnection.IceServer> iceServers;
    private MediaConstraints constraints;
    private PeerConnectionFactory factory;
    private MediaStream LocalMediaStream;
    private VideoSource videoSource;

    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private HashMap<String, Peer> peers;

    private MessageHandler messageHandler;

    public RTCClient(Context context, RTCListener listener, Socket socket, PeerConnectionParams params){
        this.context = context;
        this.mListener = listener;
        this.params = params;
        messageHandler = new MessageHandler(this);
        this.mSocket = socket;
        mSocket.on(Config.SOCKET_CALL_MESSAGE, onMessageListener);
        iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        PeerConnectionFactory.initializeAndroidGlobals(listener, true, true, params.videoCodecHwAcceleration);
        factory = new PeerConnectionFactory();

        peers = new HashMap<>();

    }

    public void onPause(){
        if(videoSource != null){
            videoSource.stop();
        }
    }

    public void onResume(){
        if(videoSource != null){
            videoSource.restart();
        }
    }

    public void onDestroy(){
        for (Peer peer : peers.values()) {
            peer.getConnection().dispose();
        }
        if(videoSource != null) {
            videoSource.stop();
            videoSource = null;
//            videoSource.dispose();
        }
        factory = null;
//        factory.dispose();
        mSocket.off(Config.SOCKET_CALL_MESSAGE);
    }

    public void start(){
        LocalMediaStream = factory.createLocalMediaStream("ARDAMS");

        if(params.videoCallEnabled){
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(params.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(params.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(params.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(params.videoFps)));
            videoCapturer = VideoCapturer.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice());
            videoSource = factory.createVideoSource(videoCapturer, videoConstraints);
            VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
            LocalMediaStream.addTrack(videoTrack);
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        LocalMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
        mListener.onLocalStream(LocalMediaStream);

        mListener.onCallReady();
    }

    private VideoCapturer videoCapturer;

    public void switchCamera(){
        VideoCapturerAndroid videoCapturerAndroid = (VideoCapturerAndroid) videoCapturer;
        videoCapturerAndroid.switchCamera(new VideoCapturerAndroid.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {}

            @Override
            public void onCameraSwitchError(String s) {}
        });
    }

    Emitter.Listener onMessageListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String from = data.getString(Config.SOCKET_FROM);
                String event = data.getString(Config.SOCKET_CALL_EVENT);
                JSONObject payload = null;
                if(!event.equalsIgnoreCase(Config.SOCKET_CALL_INIT_EVENT)){
                    payload = data.getJSONObject(Config.SOCKET_CONTENT);
                }
                if(!peers.containsKey(from)){
                    int endPoint = findEndPoint();
                    Peer peer = new Peer(context, RTCClient.this, from, endPoint);
                    peers.put(from, peer);
                    endPoints[endPoint] = true;
                    messageHandler.getCommandMap().get(event).execute(from, payload);
                } else {
                    messageHandler.getCommandMap().get(event).execute(from, payload);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public int findEndPoint() {
        for(int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    public LinkedList<PeerConnection.IceServer> getIceServers() {
        return iceServers;
    }

    public PeerConnectionFactory getFactory() {
        return factory;
    }

    public MediaConstraints getConstraints() {
        return constraints;
    }

    public MediaStream getLocalMediaStream() {
        return LocalMediaStream;
    }

    public HashMap<String, Peer> getPeers() {
        return peers;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public RTCListener getListener() {
        return mListener;
    }

    public boolean[] getEndPoints() {
        return endPoints;
    }

}
