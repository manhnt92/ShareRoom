package com.manhnt.service;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import com.github.nkzawa.socketio.client.Socket;
import com.manhnt.adapter.EmojiBroadCastPagerAdapter;
import com.manhnt.adapter.MessagePagerAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.ChatMessage;
import com.manhnt.shareroom.ChatActivity;
import com.manhnt.shareroom.MyApplication;
import com.manhnt.shareroom.R;
import com.manhnt.widget.EmojiconBroadCastGridView;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import github.ankushsachdeva.emojicon.EmojiconHandler;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import github.ankushsachdeva.emojicon.emoji.Nature;
import github.ankushsachdeva.emojicon.emoji.Objects;
import github.ankushsachdeva.emojicon.emoji.People;
import github.ankushsachdeva.emojicon.emoji.Places;
import github.ankushsachdeva.emojicon.emoji.Symbols;

public class ChatBroadCastReceiver extends BroadcastReceiver implements View.OnClickListener{

    private WidgetManager manager;
    private Account mAccount;
    private Socket mSocket;
    private ChatMessage chatMessage;
    private Context mContext;
    private View dialogView;
    private AlertDialog alert;
    private ImageButton btn_back;
    private RelativeLayout rl_emojicon;
    private MaterialEditText EdtAnswer;
    private ImageButton btn_emojicon;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        manager = WidgetManager.getInstance(null);
        manager.setFont(Config.getTypeface(context.getAssets()));
        mSocket = ((MyApplication) mContext.getApplicationContext()).getSocket();
        if (intent.getAction().equals(Config.CHAT_ACTION)) {
            mAccount = (Account) intent.getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
            chatMessage = (ChatMessage) intent.getExtras().getSerializable(Config.BUNDLE_CHAT_MESSAGE);
            if(chatMessage.getFrom_id() != mAccount.getId()) {
                if (Config.IS_PUSH_NOTIFICATION) {
                    showAlertDialog();
                    pushNotification(context, chatMessage);
                } else if (Config.CHAT_WITH != chatMessage.getFrom_id() && Config.CHAT_WITH != 0) {
                    /** Đang chat vs 1 người, có người khác gửi tin nhắn đến => pushNotification*/
                    pushNotification(context, chatMessage);
                }
            }
        }
    }
    @SuppressLint("NewApi")
    private void pushNotification(Context mContext, ChatMessage chatMessage){
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.notification_item);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.
            Builder(mContext).setSmallIcon(R.mipmap.ic_chat).setTicker(mContext.getResources()
            .getString(R.string.ticker)).setAutoCancel(true).setContent(remoteView);
        Intent i = new Intent(mContext, ChatActivity.class);
        i.putExtra(Config.BUNDLE_CHAT_MESSAGE, chatMessage);
        i.putExtra(Config.FROM_ACTIVITY, Config.CHAT_BROADCAST_RECEIVER);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        remoteView.setImageViewResource(R.id.app_icon, R.mipmap.ic_chat);
        Notification notification = mBuilder.build();
        if(Config.LAST_CHAT_MESSAGE.getFrom_id() == chatMessage.getFrom_id()){
            String total_message = Config.convertHexStringToString(Config.LAST_CHAT_MESSAGE.getMessage())
                + "\n" + Config.convertHexStringToString(chatMessage.getMessage());
            Config.LAST_CHAT_MESSAGE.setMessage(total_message);
            remoteView.setTextViewText(R.id.notification_title, chatMessage.getUserName());
            remoteView.setTextViewText(R.id.notification_content, total_message);
            notificationManager.notify(Config.LAST_NOTIFICATION_ID, notification);
        } else {
            Config.LAST_NOTIFICATION_ID = NotificationID.getID();
            Config.LAST_CHAT_MESSAGE = chatMessage;
            remoteView.setTextViewText(R.id.notification_title, chatMessage.getUserName());
            remoteView.setTextViewText(R.id.notification_content,
                Config.convertHexStringToString(chatMessage.getMessage()));
            notificationManager.notify(Config.LAST_NOTIFICATION_ID, notification);
        }
    }

    @SuppressLint("InflateParams")
    private void showAlertDialog(){
        if(!Config.IS_SHOW_DIALOG){
            Config.LIST_CHAT_MESSAGE.clear();
            Config.LIST_CHAT_MESSAGE.add(chatMessage);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.full_screen_dialog);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            dialogView = inflater.inflate(R.layout.message_dialog, null);
            builder.setView(dialogView);
            alert = builder.create();
            Window window = alert.getWindow();
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                |WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            alert.show();
            getWidget();
            Config.IS_SHOW_DIALOG = true;
        } else {
            Config.MESSAGE_BTN_NEXT.setVisibility(View.VISIBLE);
            Config.LIST_CHAT_MESSAGE.add(chatMessage);
            Config.MESSAGE_PAGER_ADAPTER.notifyDataSetChanged();
            setPageText();
        }
    }

    @SuppressLint("SetTextI18n")
    private void getWidget(){
        manager.ButtonRectangle(dialogView, R.id.btn_close, this, true);
        manager.ButtonRectangle(dialogView, R.id.btn_view, this, true);
        Config.MESSAGE_VIEW_PAGER = (ViewPager) dialogView.findViewById(R.id.message_viewPager);
        Config.MESSAGE_PAGER_ADAPTER = new MessagePagerAdapter(mContext, Config.LIST_CHAT_MESSAGE);
        Config.MESSAGE_VIEW_PAGER.setAdapter(Config.MESSAGE_PAGER_ADAPTER);
        Config.MESSAGE_TXT_USERNAME = manager.TextView(dialogView, R.id.txt_username, true);
        Config.MESSAGE_TXT_USERNAME.setText(chatMessage.getUserName());
        Config.MESSAGE_TXT_PAGE = manager.TextView(dialogView, R.id.txt_page, true);
        btn_back = manager.ImageButton(dialogView, R.id.btn_back, this, true);
        btn_back.setVisibility(View.INVISIBLE);
        Config.MESSAGE_BTN_NEXT = manager.ImageButton(dialogView, R.id.btn_next, this, true);
        Config.MESSAGE_BTN_NEXT.setVisibility(View.INVISIBLE);
        getEmojiconWidget();
        Config.MESSAGE_TXT_USERNAME.setText(Config.LIST_CHAT_MESSAGE.get(0).getUserName());
        if(Config.LIST_CHAT_MESSAGE.size() > 9){
            Config.MESSAGE_TXT_PAGE.setText("0" + (Config.MESSAGE_VIEW_PAGER.getCurrentItem() + 1)
                + "/" + Config.LIST_CHAT_MESSAGE.size());
        } else {
            Config.MESSAGE_TXT_PAGE.setText("0" + (Config.MESSAGE_VIEW_PAGER.getCurrentItem() + 1)
                + "/0" + Config.LIST_CHAT_MESSAGE.size());
        }
        Config.MESSAGE_VIEW_PAGER.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setPageText();
                if(position == 0){
                    btn_back.setVisibility(View.INVISIBLE);
                    Config.MESSAGE_BTN_NEXT.setVisibility(View.VISIBLE);
                } else if (position == Config.LIST_CHAT_MESSAGE.size() - 1){
                    btn_back.setVisibility(View.VISIBLE);
                    Config.MESSAGE_BTN_NEXT.setVisibility(View.INVISIBLE);
                } else {
                    btn_back.setVisibility(View.VISIBLE);
                    Config.MESSAGE_BTN_NEXT.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_close:
                Config.IS_SHOW_DIALOG = false;
                alert.dismiss();
                break;
            case R.id.btn_view:
                Intent i = new Intent(mContext, ChatActivity.class);
                i.putExtra(Config.BUNDLE_CHAT_MESSAGE, Config.LIST_CHAT_MESSAGE
                    .get(Config.MESSAGE_VIEW_PAGER.getCurrentItem()));
                i.putExtra(Config.FROM_ACTIVITY, Config.CHAT_BROADCAST_RECEIVER);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
                alert.dismiss();
                break;
            case R.id.btn_back:
                int currentItem = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
                Config.MESSAGE_VIEW_PAGER.setCurrentItem(currentItem - 1, true);
                break;
            case R.id.btn_next:
                int curItem = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
                Config.MESSAGE_VIEW_PAGER.setCurrentItem(curItem + 1, true);
                break;
            case R.id.btn_emojicon:
                if(rl_emojicon.getVisibility() == View.VISIBLE){
                    rl_emojicon.setVisibility(View.GONE);
                    btn_emojicon.setImageResource(R.drawable.ic_smiley_pressed);
                    InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(EdtAnswer, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                } else {
                    rl_emojicon.setVisibility(View.VISIBLE);
                    btn_emojicon.setImageResource(R.drawable.ic_keyboard_pressed);
                    InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
                break;
            case R.id.edt_answer:
                rl_emojicon.setVisibility(View.GONE);
                btn_emojicon.setImageResource(R.drawable.ic_smiley_pressed);
                InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                break;
            case R.id.btn_send_message:
                String message = EdtAnswer.getText().toString();
                String hex_result = Config.convertStringToHexString(message);
                EdtAnswer.setText("");
                if(!TextUtils.isEmpty(hex_result)){
                    int cur = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put(Config.CHAT_ID, Config.LIST_CHAT_MESSAGE.get(cur).getChat_id());
                        jObj.put(Config.FROM_ID, mAccount.getId());
                        jObj.put(Config.TO_ID, Config.LIST_CHAT_MESSAGE.get(cur).getFrom_id());
                        jObj.put(Config.MESSAGE, hex_result);
                        mSocket.emit(Config.SOCKET_PRIVATE_MESSAGE, jObj.toString());
                        removeView();
                        setPageText();
                        updateButton();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void getEmojiconWidget() {
        rl_emojicon = (RelativeLayout) dialogView.findViewById(R.id.rl_emojicon);
        rl_emojicon.setVisibility(View.GONE);
        btn_emojicon = manager.ImageButton(dialogView, R.id.btn_emojicon, this, true);
        EdtAnswer = manager.MaterialEditText(dialogView, R.id.edt_answer, this, true);
        EdtAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EmojiconHandler.addEmojis(mContext, EdtAnswer.getText(), 28);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        manager.ImageButton(dialogView, R.id.btn_send_message, this, true);
        final ViewPager emojisPager = (ViewPager) dialogView.findViewById(R.id.emojis_pager);
        EmojiBroadCastPagerAdapter mEmojisAdapter = new EmojiBroadCastPagerAdapter(
            Arrays.asList(
                new EmojiconBroadCastGridView(mContext, People.DATA),
                new EmojiconBroadCastGridView(mContext, Nature.DATA),
                new EmojiconBroadCastGridView(mContext, Objects.DATA),
                new EmojiconBroadCastGridView(mContext, Places.DATA),
                new EmojiconBroadCastGridView(mContext, Symbols.DATA)
            )
        );
        mEmojisAdapter.setListener(new EmojiBroadCastPagerAdapter.Listener() {
            @Override
            public void onEmojiconClick(Emojicon emojicon) {
                if (EdtAnswer == null || emojicon == null) { return; }
                int start = EdtAnswer.getSelectionStart();
                int end = EdtAnswer.getSelectionEnd();
                if (start < 0) {
                    EdtAnswer.append(emojicon.getEmoji());
                } else {
                    EdtAnswer.getText().replace(Math.min(start, end), Math.max(start, end),
                        emojicon.getEmoji(), 0, emojicon.getEmoji().length());
                }
            }
        });
        emojisPager.setAdapter(mEmojisAdapter);
        View[] mEmojiTabs = new View[5];
        mEmojiTabs[0] = dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_tab_1_people);
        mEmojiTabs[1] = dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_tab_2_nature);
        mEmojiTabs[2] = dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_tab_3_objects);
        mEmojiTabs[3] = dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_tab_4_cars);
        mEmojiTabs[4] = dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_tab_5_punctuation);
        for (int i = 0; i < mEmojiTabs.length; i++) {
            final int position = i;
            mEmojiTabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }
        dialogView.findViewById(com.manhnt.shareroomlibrary.R.id.emojis_backspace).setOnTouchListener(
            new EmojiconsPopup.RepeatListener(1000, 50, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0,
                        0, 0, KeyEvent.KEYCODE_ENDCALL);
                    EdtAnswer.dispatchKeyEvent(event);
                }
            }));
    }

    @SuppressLint("SetTextI18n")
    private void setPageText(){
        int currentItem = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
        int size = Config.LIST_CHAT_MESSAGE.size();
        if((currentItem + 1) >= 10 && size >= 10) {
            Config.MESSAGE_TXT_PAGE.setText((currentItem + 1) + "/" + size);
        } else {
            String text = "0" + (currentItem + 1);
            text += (size >= 10) ? "/" + size : "/0" + size;
            Config.MESSAGE_TXT_PAGE.setText(text);
        }
        Config.MESSAGE_TXT_USERNAME.setText(Config.LIST_CHAT_MESSAGE.get(currentItem)
            .getUserName());
    }

    private static class NotificationID {
        static AtomicInteger c = new AtomicInteger(0);

        public static int getID() {
            return c.incrementAndGet();
        }
    }

    private void removeView(){
        if(Config.LIST_CHAT_MESSAGE.size() == 1){
            Config.IS_SHOW_DIALOG = false;
            alert.dismiss();
        } else {
            int item = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
            Config.LIST_CHAT_MESSAGE.remove(item);
            Config.MESSAGE_PAGER_ADAPTER.notifyDataSetChanged();
        }
    }

    private void updateButton(){
        int item = Config.MESSAGE_VIEW_PAGER.getCurrentItem();
        if(Config.LIST_CHAT_MESSAGE.size() == 1){
            btn_back.setVisibility(View.INVISIBLE);
            Config.MESSAGE_BTN_NEXT.setVisibility(View.INVISIBLE);
        } else {
            if(item == Config.LIST_CHAT_MESSAGE.size() - 1){
                btn_back.setVisibility(View.VISIBLE);
                Config.MESSAGE_BTN_NEXT.setVisibility(View.INVISIBLE);
            } else if (item == 0){
                btn_back.setVisibility(View.INVISIBLE);
                Config.MESSAGE_BTN_NEXT.setVisibility(View.VISIBLE);
            } else {
                btn_back.setVisibility(View.VISIBLE);
                Config.MESSAGE_BTN_NEXT.setVisibility(View.VISIBLE);
            }
        }
    }

}
