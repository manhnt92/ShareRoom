package com.manhnt.shareroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gc.materialdesign.views.ButtonRectangle;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.config.Config;
import com.manhnt.config.WidgetManager;
import com.manhnt.rtc.PeerConnectionParams;
import com.manhnt.rtc.RTCClient;
import com.manhnt.rtc.RTCListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends Activity implements View.OnClickListener, RTCListener {

    private static final int CALL_PREPARE_TIME_OUT = 30000;
    private static final int CALL_END_TIME = 3000;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private boolean isCaller;
    private String call_UserName;
    private String call_SocketID;

    private ImageButton btn_speaker, btn_micro, btn_silence;
    private TextView status, txt_speaker, txt_micro, txt_timer;
    private LinearLayout ll_out_going_call, ll_in_coming_call, ll_accept_or_reject;
    private ButtonRectangle btn_end_call, btn_accept, btn_reject;

    private boolean isSpeaker, isMicro, isSilence;
    private PowerManager.WakeLock mWakeLock;
    private AudioManager mAudioManager;
    private Vibrator vibrator;
    private AudioManagerReceiver audioManagerReceiver;
    private MediaPlayer player;
    private CountDownTimer timeout;
    private long startTimeCall;
    private boolean isCall = false;
    private Socket mSocket;
    private RTCClient client;

    private int AudioManager_Mode_RollBack;
    private boolean SpeakerOnPhone_RollBack, MicrophoneMute_RollBack;
    private int VolumeControlStream_RollBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.call_activity);
        mSocket = ((MyApplication) getApplication()).getSocket();
        mSocket.on(Config.SOCKET_END_CALL, EnCallListener);
        mSocket.on(Config.SOCKET_CALL_REJECT, RejectCallListener);
        getExtraBundle();
        getControlManager();
        getWidget();
        getTimer();
    }

    private void getExtraBundle(){
        isCaller = getIntent().getExtras().getBoolean(Config.BUNDLE_IS_CALLER);
        call_UserName = getIntent().getExtras().getString(Config.BUNDLE_CALL_USER_NAME);
        call_SocketID = getIntent().getExtras().getString(Config.BUNDLE_CALL_SOCKET_ID);
    }

    private void getWidget(){
        WidgetManager manager = WidgetManager.getInstance(this);
        ll_out_going_call = (LinearLayout) findViewById(R.id.ll_out_going_call);
        ll_in_coming_call = (LinearLayout) findViewById(R.id.ll_in_coming_call);
        ll_accept_or_reject = (LinearLayout) findViewById(R.id.ll_accept_or_reject);

        manager.TextView(R.id.title, true);
        TextView user_name = manager.TextView(R.id.user_name, true);
        user_name.setText(call_UserName);
        status = manager.TextView(R.id.status, true);
        txt_timer = manager.TextView(R.id.txt_timer, true);

        btn_speaker = manager.ImageButton(R.id.btn_speaker, this, true);
        txt_speaker = manager.TextView(R.id.txt_speaker, true);
        btn_micro = manager.ImageButton(R.id.btn_micro, this, true);
        txt_micro = manager.TextView(R.id.txt_micro, true);

        btn_silence = manager.ImageButton(R.id.btn_silence, this, true);
        manager.TextView(R.id.txt_silence, true);

        btn_end_call = manager.ButtonRectangle(R.id.btn_end_call, this, true);
        btn_reject = manager.ButtonRectangle(R.id.btn_reject, this, true);
        btn_accept = manager.ButtonRectangle(R.id.btn_accept, this, true);

        if(isCaller){
            status.setText(getString(R.string.status_out_going_call));
            playCallSound(true);
            ll_out_going_call.setVisibility(View.VISIBLE);
            ll_in_coming_call.setVisibility(View.GONE);
            btn_end_call.setVisibility(View.VISIBLE);
            ll_accept_or_reject.setVisibility(View.GONE);
        } else {
            status.setText(getString(R.string.status_in_coming_call));
            playCallSound(false);
            ll_out_going_call.setVisibility(View.GONE);
            ll_in_coming_call.setVisibility(View.VISIBLE);
            btn_end_call.setVisibility(View.GONE);
            ll_accept_or_reject.setVisibility(View.VISIBLE);
        }
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParams params = new PeerConnectionParams(false, false, displaySize.x, displaySize.y, 30, 1,
            VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        client = new RTCClient(this, this, mSocket, params);
        client.start();
    }

    private void getTimer(){
        timeout = new CountDownTimer(CALL_PREPARE_TIME_OUT, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                if(vibrator.hasVibrator()) {
                    vibrator.cancel();
                }
                ReleaseMediaPlayer();
                endCall(getString(R.string.status_no_reply));
            }
        };
        timeout.start();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(isCall) {
                    int seconds = (int) ((System.currentTimeMillis() - startTimeCall) / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    @SuppressLint("DefaultLocale") final String time = String.format("%02d:%02d", minutes, seconds);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_timer.setText(time);
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    @SuppressLint("InlinedApi")
    private void getControlManager() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        AudioManager_Mode_RollBack = mAudioManager.getMode();
        SpeakerOnPhone_RollBack = mAudioManager.isSpeakerphoneOn();
        MicrophoneMute_RollBack = mAudioManager.isMicrophoneMute();
        VolumeControlStream_RollBack = getVolumeControlStream();

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, getLocalClassName());
        if(!mWakeLock.isHeld()){
            mWakeLock.acquire();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_speaker:
                txt_speaker.setText(isSpeaker ? getString(R.string.turn_off_speaker) : getString(R.string.turn_on_speaker));
                btn_speaker.setImageResource(isSpeaker ? R.mipmap.ic_speaker : R.mipmap.ic_speaker_press);
                mAudioManager.setSpeakerphoneOn(!isSpeaker);
                isSpeaker = !isSpeaker;
                break;
            case R.id.btn_micro:
                txt_micro.setText(isMicro ? getString(R.string.turn_on_micro) : getString(R.string.turn_off_micro));
                btn_micro.setImageResource(isMicro ? R.mipmap.ic_mic : R.mipmap.ic_mic_press);
                mAudioManager.setMicrophoneMute(!isMicro);
                isMicro = !isMicro;
                break;
            case R.id.btn_silence:
                if(!isSilence) {
                    isSilence = true;
                    if(vibrator.hasVibrator()) {
                        vibrator.cancel();
                    }
                    ReleaseMediaPlayer();
                    btn_silence.setImageResource(R.mipmap.ic_silence_press);
                }
                break;
            case R.id.btn_end_call:
                mSocket.emit(Config.SOCKET_END_CALL, call_SocketID);
                endCall(getString(R.string.status_disconnect));
                break;
            case R.id.btn_accept:
                mSocket.emit(Config.SOCKET_CALL_READY, call_SocketID);
                mAudioManager.setSpeakerphoneOn(false);
                if(vibrator.hasVibrator()) {
                    vibrator.cancel();
                }
                timeout.cancel();
                ReleaseMediaPlayer();
                ll_out_going_call.setVisibility(View.VISIBLE);
                ll_in_coming_call.setVisibility(View.GONE);
                btn_end_call.setVisibility(View.VISIBLE);
                ll_accept_or_reject.setVisibility(View.GONE);
                break;
            case R.id.btn_reject:
                mSocket.emit(Config.SOCKET_CALL_REJECT, call_SocketID);
                if(vibrator.hasVibrator()) {
                    vibrator.cancel();
                }
                timeout.cancel();
                ReleaseMediaPlayer();
                endCall(getString(R.string.status_reject_call));
                break;
            default:
                break;
        }
    }

    private void playCallSound(boolean isOutGoingCall) {
        if(isOutGoingCall) {
            mAudioManager.setSpeakerphoneOn(false);
            player = MediaPlayer.create(this, R.raw.rings);
        } else {
            long pattern[] = { 0, 100, 200, 300, 400 };
            vibrator.vibrate(pattern, 0);
            mAudioManager.setSpeakerphoneOn(true);
            player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.start();
            }
        });
        player.setLooping(true);
    }

    @Override
    public void onBackPressed() {}

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null){
            client.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(client != null) {
            client.onResume();
        }
        if(audioManagerReceiver == null) {
            audioManagerReceiver = new AudioManagerReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(audioManagerReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if(client != null) {
            client.onDestroy();
        }
        if(mWakeLock.isHeld()){
            mWakeLock.release();
        }
        if(vibrator.hasVibrator()) {
            vibrator.cancel();
        }
        ReleaseMediaPlayer();
        unregisterReceiver(audioManagerReceiver);
        mSocket.off(Config.SOCKET_CALL_READY);
        mSocket.off(Config.SOCKET_END_CALL);
        mSocket.off(Config.SOCKET_CALL_REJECT);
        mAudioManager.setMode(AudioManager_Mode_RollBack);
        mAudioManager.setSpeakerphoneOn(SpeakerOnPhone_RollBack);
        mAudioManager.setMicrophoneMute(MicrophoneMute_RollBack);
        setVolumeControlStream(VolumeControlStream_RollBack);
        super.onDestroy();
    }

    @Override
    public void onCallReady() {
        if(isCaller){
            mSocket.on(Config.SOCKET_CALL_READY, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    timeout.cancel();
                    ReleaseMediaPlayer();
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put(Config.SOCKET_TO, call_SocketID);
                        jObj.put(Config.SOCKET_CALL_EVENT, Config.SOCKET_CALL_INIT_EVENT);
                        mSocket.emit(Config.SOCKET_CALL_MESSAGE, jObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                isCall = newStatus.equalsIgnoreCase(getString(R.string.status_connected));
                if(isCall){
                    status.setText(newStatus);
                    startTimeCall = System.currentTimeMillis();
                } else {
                    endCall(newStatus);
                }
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {}

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {}

    @Override
    public void onRemoveRemoteStream(int endPoint) {}

    private void ReleaseMediaPlayer(){
        try {
            if(player.isPlaying()){
                player.stop();
                player.release();
            }
        } catch (IllegalStateException ignored){}
    }

    @SuppressLint("SetTextI18n")
    private void endCall(final String stt){
        CountDownTimer call_end = new CountDownTimer(CALL_END_TIME, 1000) {
            @Override
            public void onTick(long l) {
                status.setText(stt);
                btn_end_call.setEnabled(false);
                btn_accept.setEnabled(false);
                btn_reject.setEnabled(false);
                btn_speaker.setEnabled(false);
                btn_micro.setEnabled(false);
                btn_silence.setEnabled(false);
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        call_end.start();
    }

    private Emitter.Listener EnCallListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    endCall(call_UserName + " " + getString(R.string.status_disconnect));
                }
            });
        }
    };

    private Emitter.Listener RejectCallListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    endCall(call_UserName + " " + getString(R.string.status_reject_call));
                }
            });
        }
    };

    private class AudioManagerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("android.intent.action.HEADSET_PLUG")) {
                int state = intent.getIntExtra("state", -1);
                if (state == 1) {
                    isSpeaker = true;
                    mAudioManager.setSpeakerphoneOn(false);
                    btn_speaker.setImageResource(R.mipmap.ic_speaker);
                    txt_speaker.setText(getString(R.string.turn_off_speaker));
                }
            }
        }
    }

}
