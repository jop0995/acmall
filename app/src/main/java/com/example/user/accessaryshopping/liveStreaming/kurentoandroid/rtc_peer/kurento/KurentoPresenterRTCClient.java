package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.rtc_peer.kurento;

import android.util.Log;

import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.RTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by nhancao on 7/18/17.
 */

public class KurentoPresenterRTCClient implements RTCClient {


    //방번호 꺼내오기 위해서
    String currentroom;

    private static final String TAG = KurentoPresenterRTCClient.class.getSimpleName();

    private SocketService socketService;

    public KurentoPresenterRTCClient(SocketService socketService) {
        //mContext = this;
        this.socketService = socketService;
    }

    public void connectToRoom(String host, BaseSocketCallback socketCallback) {
        socketService.connect(host, socketCallback);
    }

    //방번호를 받는다.
    public void sendRoom(String room){
        currentroom=room;
        Log.e("roomname111111111", currentroom);
    }

    @Override
    public void sendOfferSdp(SessionDescription sdp) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("id", "presenter");
            obj.put("sdpOffer", sdp.description);
            obj.put("room",currentroom);

            socketService.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAnswerSdp(SessionDescription sdp) {
        Log.e(TAG, "sendAnswerSdp: ");
    }

    @Override
    public void sendLocalIceCandidate(IceCandidate iceCandidate) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "onIceCandidate");
            JSONObject candidate = new JSONObject();
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            obj.put("candidate", candidate);

            socketService.sendMessage(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {
        Log.e(TAG, "sendLocalIceCandidateRemovals: ");
    }

}
