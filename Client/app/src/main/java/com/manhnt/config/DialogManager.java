package com.manhnt.config;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.DefaultAdapter;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.manhnt.shareroom.R;

public class DialogManager {

    private static DialogManager instance;

    private DialogManager(){}

    public static synchronized DialogManager getInstance(){
        if(instance == null){
            instance = new DialogManager();
        }
        return instance;
    }

    public MaterialDialog progressDialog(Context context, String message){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).content(message)
            .backgroundColor(Config.COLOR_DIALOG).contentColor(Color.WHITE).typeface(font, font)
            .progress(true, 0);
        MaterialDialog mDialog = builder.build();
        mDialog.setCancelable(false);
        return mDialog;
    }

    public interface YesNoDialogListener {
        void onYes(MaterialDialog dialog);
        void onNo(MaterialDialog dialog);
    }

    public MaterialDialog YesNoDialog(Context context, int title, int question, int yes, int no,
        final YesNoDialogListener yesNoDialogListener, boolean isAnim){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title)
            .titleColor(Color.WHITE).content(question).backgroundColor(Config.COLOR_DIALOG)
            .contentColor(Color.WHITE).negativeText(yes).negativeColor(Color.WHITE)
            .positiveText(no).positiveColor(Color.WHITE).typeface(font, font)
            .onNegative(new MaterialDialog.SingleButtonCallback() {

                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    yesNoDialogListener.onYes(dialog);
                }

            }).onPositive(new MaterialDialog.SingleButtonCallback() {

                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    yesNoDialogListener.onNo(dialog);
                }

            });
        MaterialDialog dialog = builder.build();
        View view = dialog.getView();
        WidgetManager manager = WidgetManager.getInstance((Activity) context);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.title, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultNegative, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultPositive, isAnim);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.content, isAnim);
        return dialog;
    }

    public MaterialDialog InformationDialog(Context context, int title, int content, boolean isAnim){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title)
            .content(content).titleColor(Color.WHITE).positiveColor(Color.WHITE).contentColor(Color.WHITE)
            .backgroundColor(Config.COLOR_DIALOG)
            .positiveText(R.string.OK).typeface(font, font)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            });
        MaterialDialog dialog = builder.build();
        View view = dialog.getView();
        WidgetManager manager = WidgetManager.getInstance((Activity) context);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.title, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultPositive, isAnim);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.content, isAnim);
        return dialog;
    }

    public interface ListOneChoiceDialogListener {
        void onChoice(MaterialDialog dialog, int index);
    }

    public MaterialDialog ListOneChoiceDialog(Context context, int title, String[] content, int index,
        boolean isSingleChoiceCallBack, boolean isAnim, final ListOneChoiceDialogListener listOneChoiceDialogListener){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title)
            .titleColor(Color.WHITE).backgroundColor(Config.COLOR_DIALOG).items(content)
            .itemsColor(Color.WHITE).typeface(font, font)
            .scaleListener(new DefaultAdapter.ScaleSpringListener() {

                @Override
                public void onItemTouchDown(View view) {}

                @Override
                public void onItemTouchUp(View view) {}

            });
        if(isSingleChoiceCallBack){
            builder.itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                @Override
                public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    listOneChoiceDialogListener.onChoice(dialog, which);
                    return true;
                }
            }).positiveText(R.string.choose).positiveColor(Color.WHITE);
        } else {
            builder.itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    listOneChoiceDialogListener.onChoice(dialog, which);
                }
            });
        }
        MaterialDialog dialog = builder.build();
        View view = dialog.getView();
        WidgetManager manager = WidgetManager.getInstance((Activity) context);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.title, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultPositive, isAnim);
        return dialog;
    }

    public interface InputDialogListener{
        void onInput(MaterialDialog dialog, CharSequence input);
    }

    public MaterialDialog InputDialog(final Context context, int title, final int inputType, String hint,
        String text, boolean isAnim, final InputDialogListener inputDialogListener){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog dialog = new MaterialDialog.Builder(context).title(title).titleColor(Color.WHITE)
            .backgroundColor(Config.COLOR_DIALOG).typeface(font, font).inputType(inputType).autoDismiss(false)
            .contentColor(Color.WHITE).positiveColor(Color.WHITE)
            .input(hint, text, new MaterialDialog.InputCallback() {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    if(inputType == InputType.TYPE_CLASS_NUMBER){
                        try {
                            Integer.parseInt(input.toString());
                        } catch(NumberFormatException | NullPointerException e) {
                            Config.showCustomToast(context, 0, context.getString(R.string.invalidNumber));
                            return;
                        }
                    }
                    inputDialogListener.onInput(dialog, input);
                }
            }).build();
        View view = dialog.getView();
        WidgetManager manager = WidgetManager.getInstance((Activity) context);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.title, isAnim);
        manager.EditText(view, android.R.id.input, true);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultPositive, isAnim);
        return dialog;
    }

    public interface CustomViewListener {
        void onAttachCustomView(View view);
        void onOK(MaterialDialog dialog);
        void onCancel(MaterialDialog dialog);
    }

    public MaterialDialog CustomViewDialog(Context context, int title, int customView,
        boolean isAnim, final CustomViewListener customViewListener){
        Typeface font = Config.getTypeface(context.getAssets());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(title)
            .titleColor(Color.WHITE).backgroundColor(Config.COLOR_DIALOG).typeface(font, font)
            .customView(customView, true).autoDismiss(false).negativeText(R.string.OK).negativeColor(Color.WHITE)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    customViewListener.onOK(dialog);
                }
            }).positiveText(R.string.back).positiveColor(Color.WHITE)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    customViewListener.onCancel(dialog);
                }
            });
        MaterialDialog dialog = builder.build();
        View view = dialog.getView();
        WidgetManager manager = WidgetManager.getInstance((Activity) context);
        manager.TextView(view, com.manhnt.shareroomlibrary.R.id.title, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultNegative, isAnim);
        manager.MDButton(view, com.manhnt.shareroomlibrary.R.id.buttonDefaultPositive, isAnim);
        View v = dialog.getCustomView();
        customViewListener.onAttachCustomView(v);
        return dialog;
    }

}
