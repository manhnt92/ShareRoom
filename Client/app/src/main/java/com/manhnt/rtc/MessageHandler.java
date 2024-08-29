package com.manhnt.rtc;

import com.manhnt.config.Config;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import java.util.HashMap;

public class MessageHandler {

    private HashMap<String, Command> commandMap;
    private RTCClient rtcClient;

    public MessageHandler(RTCClient rtcClient) {
        this.rtcClient = rtcClient;
        commandMap = new HashMap<>();
        commandMap.put(Config.SOCKET_CALL_INIT_EVENT, new CreateOfferCommand());
        commandMap.put(Config.SOCKET_CALL_OFFER_EVENT, new CreateAnswerCommand());
        commandMap.put(Config.SOCKET_CALL_ANSWER_EVENT, new SetRemoteSDPCommand());
        commandMap.put(Config.SOCKET_CALL_CANDIDATE_EVENT, new AddIceCandidateCommand());
    }

    public interface Command{
        void execute(String peerId, JSONObject content) throws JSONException;
    }

    private class CreateOfferCommand implements Command {
        @Override
        public void execute(String peerId, JSONObject content) throws JSONException {
            Peer peer = rtcClient.getPeers().get(peerId);
            peer.getConnection().createOffer(peer, rtcClient.getConstraints());
        }
    }

    private class CreateAnswerCommand implements Command {
        @Override
        public void execute(String peerId, JSONObject content) throws JSONException {
            Peer peer = rtcClient.getPeers().get(peerId);
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(content.getString(Config.SOCKET_CALL_EVENT)),
                content.getString(Config.SOCKET_SESSION_DESCRIPTION)
            );
            peer.getConnection().setRemoteDescription(peer, sdp);
            peer.getConnection().createAnswer(peer, rtcClient.getConstraints());
        }
    }

    private class SetRemoteSDPCommand implements Command {
        @Override
        public void execute(String peerId, JSONObject content) throws JSONException {
            Peer peer = rtcClient.getPeers().get(peerId);
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(content.getString(Config.SOCKET_CALL_EVENT)),
                    content.getString(Config.SOCKET_SESSION_DESCRIPTION)
            );
            peer.getConnection().setRemoteDescription(peer, sdp);
        }
    }

    private class AddIceCandidateCommand implements Command{
        @Override
        public void execute(String peerId, JSONObject content) throws JSONException {
            PeerConnection peerConnection = rtcClient.getPeers().get(peerId).getConnection();
            if (peerConnection.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(content.getString("id"),
                    content.getInt("label"), content.getString("candidate")
                );
                peerConnection.addIceCandidate(candidate);
            }
        }
    }

    public HashMap<String, Command> getCommandMap() {
        return commandMap;
    }
}
