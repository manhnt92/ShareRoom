package com.manhnt.widget;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import com.manhnt.config.WidgetManager;
import com.manhnt.shareroom.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconHandler;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

public class EmojiconWidget {

    public static final int LINEAR_LAYOUT = 0;
    public static final int RELATIVE_LAYOUT = 1;
    private Activity activity;
    private int rootType;
    private int resRootView;
    private int resBtn_emojicon;
    private int resEditText;
    private int resBtn_send;
    private ImageButton btn_emojicon;
    private MaterialEditText editText;
    private EmojiconsPopup popup;
    private SendListener sendListener;
    private boolean isAnim;

    public EmojiconWidget(Activity activity, int rootType, int resRootView, int resBtn_emojicon,
        int resEditText, int resBtn_send, boolean isAnim){
        this.activity = activity;
        this.rootType = rootType;
        this.resRootView = resRootView;
        this.resBtn_emojicon = resBtn_emojicon;
        this.resEditText = resEditText;
        this.resBtn_send = resBtn_send;
        this.isAnim = isAnim;

        init();
    }

    private void init(){
        switch (rootType){
            case LINEAR_LAYOUT:
                LinearLayout ll_rootView = (LinearLayout) activity.findViewById(resRootView);
                popup = new EmojiconsPopup(ll_rootView, activity);
                break;
            case RELATIVE_LAYOUT:
                RelativeLayout rl_rootView = (RelativeLayout) activity.findViewById(resRootView);
                popup = new EmojiconsPopup(rl_rootView, activity);
                break;
            default:
                break;
        }
        WidgetManager manager = WidgetManager.getInstance(activity);
        btn_emojicon = manager.ImageButton(resBtn_emojicon, BtnEmojiconClick, isAnim);
        editText = manager.MaterialEditText(resEditText, isAnim);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EmojiconHandler.addEmojis(activity, editText.getText(), 28);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        manager.ImageButton(resBtn_send, BtnSendClick, isAnim);
        popup.setSizeForSoftKeyboard();
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(btn_emojicon, R.drawable.ic_smiley_pressed);
            }
        });
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {}

            @Override
            public void onKeyboardClose() {
                if(popup.isShowing()) {
                    popup.dismiss();
                }
            }
        });
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (editText == null || emojicon == null) {
                    return;
                }
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                if (start < 0) {
                    editText.append(emojicon.getEmoji());
                } else {
                    editText.getText().replace(Math.min(start, end), Math.max(start, end),
                            emojicon.getEmoji(), 0, emojicon.getEmoji().length());
                }
            }
        });
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {
            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                editText.dispatchKeyEvent(event);
            }
        });
    }

    private void changeEmojiKeyboardIcon(ImageButton iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    private View.OnClickListener BtnEmojiconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!popup.isShowing()){
                if(popup.isKeyBoardOpen()){
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(btn_emojicon, R.drawable.ic_keyboard_pressed);
                } else{
                    editText.setFocusableInTouchMode(true);
                    editText.requestFocus();
                    popup.showAtBottomPending();
                    InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(btn_emojicon, R.drawable.ic_keyboard_pressed);
                }
            } else{
                popup.dismiss();
            }
        }
    };

    private View.OnClickListener BtnSendClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendListener.onBtnSendClick(view);
        }
    };

    public interface SendListener {
        void onBtnSendClick(View view);
    }

    public void setSendListener(SendListener sendListener) {
        this.sendListener = sendListener;
    }

    public MaterialEditText getEditText() {
        return editText;
    }
}
