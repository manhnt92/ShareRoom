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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
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
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import java.util.Timer;
import java.util.TimerTask;

public class VideoCallActivity extends Activity implements RTCListener, View.OnClickListener, SpringListener{

    private static final int CALL_PREPARE_TIME_OUT = 30000;
    private static final int CALL_END_TIME = 3000;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final int X = 0;
    private static int Y = 0;
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    private boolean isCaller;
    private String call_UserName;
    private String call_SocketID;

    private Socket mSocket;
    private boolean isCall = false;
    private long startTimeCall;
    private RTCClient client;
    private AudioManager mAudioManager;
    private Vibrator vibrator;
    private MediaPlayer player;
    private CountDownTimer timeout;
    private AudioManagerReceiver audioManagerReceiver;
    private VideoRenderer.Callbacks localRender, remoteRender;

    private GLSurfaceView LocalSurfaceView, RemoteSurfaceView;
    private int screenHeight;

    /** Spring */
    private Spring LocalStreamSpring, ControlSpring;//ActionBarSpring
    private int ControlHeight;//ActionBarHeight
    private boolean isShowControlLayout;

    private RelativeLayout rl_end_call;//rl_action_bar
    private LinearLayout ll_call, ll_out_going_call, ll_in_coming_call, ll_accept_or_reject, ll_control;
    private ImageButton btn_switch_camera, btn_speaker, btn_micro, btn_silence;
    private ButtonRectangle btn_end_call, btn_accept, btn_reject;
    private TextView status, txt_speaker, txt_micro, txt_timer;
    private boolean isSpeaker, isMicro, isSilence;

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
        setContentView(R.layout.video_call_activity);
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


    private void getControlManager(){
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        AudioManager_Mode_RollBack = mAudioManager.getMode();
        SpeakerOnPhone_RollBack = mAudioManager.isSpeakerphoneOn();
        MicrophoneMute_RollBack = mAudioManager.isMicrophoneMute();
        VolumeControlStream_RollBack = getVolumeControlStream();
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void getWidget(){
        WidgetManager manager = WidgetManager.getInstance(this);
        /** Action Bar */
//        rl_action_bar = (RelativeLayout) findViewById(R.id.rl_action_bar);
//        rl_action_bar.post(new Runnable() {
//            @Override
//            public void run() {
//                ActionBarHeight = rl_action_bar.getHeight();
//            }
//        });
        btn_switch_camera = manager.ImageButton(R.id.btn_switch_camera, this, true);
        manager.TextView(R.id.title, true);
        txt_timer = manager.TextView(R.id.txt_timer, true);

        ll_call = (LinearLayout) findViewById(R.id.ll_call);
        /** Out Going Call */
        ll_out_going_call = (LinearLayout) findViewById(R.id.ll_out_going_call);
        btn_speaker = manager.ImageButton(R.id.btn_speaker, this, true);
        txt_speaker = manager.TextView(R.id.txt_speaker, true);
        btn_micro = manager.ImageButton(R.id.btn_micro, this, true);
        txt_micro = manager.TextView(R.id.txt_micro, true);
        rl_end_call = (RelativeLayout) findViewById(R.id.rl_end_call);
        btn_end_call = manager.ButtonRectangle(R.id.btn_end_call, this, true);
        /** In Coming Call */
        TextView txt_username = manager.TextView(R.id.txt_username, true);
        txt_username.setText(call_UserName);
        status = manager.TextView(R.id.txt_status, true);
        ll_in_coming_call = (LinearLayout) findViewById(R.id.ll_in_coming_call);
        btn_silence = manager.ImageButton(R.id.btn_silence, this, true);
        manager.TextView(R.id.txt_silence, true);
        ll_accept_or_reject = (LinearLayout) findViewById(R.id.ll_accept_or_reject);
        btn_accept = manager.ButtonRectangle(R.id.btn_accept, this, true);
        btn_reject = manager.ButtonRectangle(R.id.btn_reject, this, true);

        /** Local & Remote Stream */
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int screenWidth = displaySize.x;
        screenHeight = displaySize.y;
        LocalSurfaceView = (GLSurfaceView) findViewById(R.id.LocalSurfaceView);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth /5, screenHeight/5);
        LocalSurfaceView.setX(screenWidth /10);
        LocalSurfaceView.setY(screenHeight/10 + 30);
        LocalSurfaceView.setLayoutParams(layoutParams);
        LocalSurfaceView.setPreserveEGLContextOnPause(true);
        LocalSurfaceView.setKeepScreenOn(true);
        RemoteSurfaceView = (GLSurfaceView) findViewById(R.id.RemoteSurfaceView);
        RemoteSurfaceView.setPreserveEGLContextOnPause(true);
        RemoteSurfaceView.setKeepScreenOn(true);
        RemoteSurfaceView.setOnClickListener(this);
        RemoteSurfaceView.setVisibility(View.GONE);
        ll_control = (LinearLayout) findViewById(R.id.ll_control);
        ll_control.post(new Runnable() {
            @Override
            public void run() {
                ControlHeight = ll_control.getHeight();
            }
        });
        /** RTC Client */
        PeerConnectionParams params = new PeerConnectionParams(true, false, screenWidth, screenHeight, 30, 1,
            VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        client = new RTCClient(this, this, mSocket, params);

        VideoRendererGui.setView(LocalSurfaceView, null);
        localRender = VideoRendererGui.create(X, Y, WIDTH, HEIGHT, scalingType, true);
        VideoRendererGui.setView(RemoteSurfaceView, null);
        remoteRender = VideoRendererGui.create(X, Y, WIDTH, HEIGHT, scalingType, true);
        client.start();

        if(isCaller){
            ll_out_going_call.setVisibility(View.VISIBLE);
            rl_end_call.setVisibility(View.VISIBLE);
            ll_in_coming_call.setVisibility(View.GONE);
            ll_accept_or_reject.setVisibility(View.GONE);
            status.setText(getString(R.string.status_out_going_call));
            playCallSound(true);
        } else {
            ll_out_going_call.setVisibility(View.GONE);
            rl_end_call.setVisibility(View.GONE);
            ll_in_coming_call.setVisibility(View.VISIBLE);
            ll_accept_or_reject.setVisibility(View.VISIBLE);
            btn_switch_camera.setVisibility(View.INVISIBLE);
            LocalSurfaceView.setVisibility(View.GONE);
            status.setText(getString(R.string.status_in_coming_call));
            playCallSound(false);
        }

        SpringSystem springSystem = SpringSystem.create();
//        ActionBarSpring = springSystem.createSpring();
//        ActionBarSpring.setCurrentValue(-ActionBarHeight).setAtRest();
//        ActionBarSpring.addListener(this);
        LocalStreamSpring = springSystem.createSpring();
        LocalStreamSpring.setCurrentValue(screenHeight/10).setAtRest();
        LocalStreamSpring.addListener(this);
        ControlSpring = springSystem.createSpring();
        ControlSpring.setCurrentValue(0).setAtRest();
        ControlSpring.addListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_switch_camera:
                client.switchCamera();
                break;
            case R.id.RemoteSurfaceView:
//                ActionBarSpring.setEndValue(isShowControlLayout ? 0 : -ActionBarHeight);
                LocalStreamSpring.setEndValue(isShowControlLayout ? screenHeight/10 + 30 : screenHeight/10);
                ControlSpring.setEndValue(isShowControlLayout ? 0 : ControlHeight);
                isShowControlLayout = !isShowControlLayout;
                break;
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
                if(vibrator.hasVibrator()) {
                    vibrator.cancel();
                }
                timeout.cancel();
                ReleaseMediaPlayer();
                if(!mAudioManager.isSpeakerphoneOn()){
                    txt_speaker.setText(getString(R.string.turn_on_speaker));
                    btn_speaker.setImageResource(R.mipmap.ic_speaker_press);
                    mAudioManager.setSpeakerphoneOn(true);
                    isSpeaker = true;
                }
                ll_out_going_call.setVisibility(View.VISIBLE);
                rl_end_call.setVisibility(View.VISIBLE);
                ll_in_coming_call.setVisibility(View.GONE);
                ll_accept_or_reject.setVisibility(View.GONE);
                btn_switch_camera.setVisibility(View.VISIBLE);
                LocalSurfaceView.setVisibility(View.VISIBLE);
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
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            long pattern[] = { 0, 100, 200, 300, 400 };
            vibrator.vibrate(pattern, 0);
            mAudioManager.setSpeakerphoneOn(true);
            txt_speaker.setText(getString(R.string.turn_on_speaker));
            btn_speaker.setImageResource(R.mipmap.ic_speaker_press);
            isSpeaker = true;
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!mAudioManager.isSpeakerphoneOn()){
                                txt_speaker.setText(getString(R.string.turn_on_speaker));
                                btn_speaker.setImageResource(R.mipmap.ic_speaker_press);
                                mAudioManager.setSpeakerphoneOn(true);
                                isSpeaker = true;
                            }
                        }
                    });
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
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender, X, Y, WIDTH, HEIGHT, scalingType, true);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RemoteSurfaceView.setVisibility(View.VISIBLE);
                ll_call.setVisibility(View.GONE);
//                ActionBarSpring.setEndValue(isShowControlLayout ? 0 : -ActionBarHeight);
                LocalStreamSpring.setEndValue(isShowControlLayout ? screenHeight/10 + 30 : screenHeight/10);
                ControlSpring.setEndValue(isShowControlLayout ? 0 : ControlHeight);
                isShowControlLayout = !isShowControlLayout;
            }
        });
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(localRender, X, Y, WIDTH, HEIGHT, scalingType, true);
        VideoRendererGui.update(remoteRender, X, Y, WIDTH, HEIGHT, scalingType, true);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RemoteSurfaceView.setVisibility(View.GONE);
                ll_call.setVisibility(View.VISIBLE);
            }
        });
        VideoRendererGui.update(localRender, X, Y, WIDTH, HEIGHT, scalingType, true);
    }

    private void ReleaseMediaPlayer(){
        try {
            if(player.isPlaying()){
                player.stop();
                player.release();
            }
        } catch (IllegalStateException ignored){}
    }

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
                btn_switch_camera.setEnabled(false);
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        call_end.start();
    }

    @Override
    public void onSpringUpdate(Spring spring) {
//        float Pos_Y_ActionBarLayout = (float) ActionBarSpring.getCurrentValue();
        float Pos_Y_LocalStreamLayout = (float) LocalStreamSpring.getCurrentValue();
        float Pos_Y_ControlLayout = (float) ControlSpring.getCurrentValue();
//        rl_action_bar.setTranslationY(Pos_Y_ActionBarLayout);
        LocalSurfaceView.setTranslationY(Pos_Y_LocalStreamLayout);
        ll_control.setTranslationY(Pos_Y_ControlLayout);
    }

    @Override
    public void onSpringAtRest(Spring spring) {}

    @Override
    public void onSpringActivate(Spring spring) {}

    @Override
    public void onSpringEndStateChange(Spring spring) {}

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
