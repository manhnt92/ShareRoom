package com.manhnt.shareroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.manhnt.config.Config;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.service.ChatService;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

public class LoginActivity extends Activity implements View.OnClickListener {

    private CallbackManager callbackManager;
    private MaterialEditText edt_email, edt_password;
    private String facebook_access_token;
    private JSONObject jObj_Facebook_Account;
    private boolean isLoginFacebook;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.login_activity);
        getWidget();
        getExtraBundle();
    }

    private void getExtraBundle(){
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getInt(Config.FROM_ACTIVITY) == Config.REGISTER_ACTIVITY) {
                edt_email.setText(getIntent().getExtras().getString(Config.BUNDLE_EMAIL));
            }
        }
    }

    private void getWidget() {
        WidgetManager manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.title, true);
        manager.ImageButton(R.id.btn_back, this, true);
        edt_email = manager.MaterialEditText(R.id.edt_email, true);
        edt_password = manager.MaterialEditText(R.id.edt_password, true);
        edt_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        manager.ButtonRectangle(R.id.btn_login, this, true);
        manager.ButtonRectangle(R.id.btn_register, this, true);
        manager.TextView(R.id.txt_login, true);
        manager.TextView(R.id.txt_login_with_facebook, true);
        manager.TextView(R.id.txt_register, true);
        LoginButton btn_login_fb = manager.LoginButton(R.id.btn_login_fb, true);
        btn_login_fb.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));
        btn_login_fb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult result) {
                facebook_access_token = result.getAccessToken().getToken();
                new GraphRequest(result.getAccessToken(), "/" + result.getAccessToken().getUserId(), null,
                    HttpMethod.GET, callback).executeAsync();
            }

            @Override
            public void onError(FacebookException error) {}

            @Override
            public void onCancel() {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                intent(MainActivity.class, false, null);
                break;
            case R.id.btn_login:
                if(checkLoginValid()){
                    isLoginFacebook = false;
                    RequestAPI.getInstance().context(this).url(Config.URL_LOGIN).method(RequestAPI.POST)
                        .message(getString(R.string.waiting)).isParams(true).isShowToast(true)
                        .isShowDialog(true).isAuthorization(false).execute(loginListener);
                }
                break;
            case R.id.btn_register:
                intent(RegisterActivity.class, true, null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        intent(MainActivity.class, false, null);
    }

    private GraphRequest.Callback callback = new GraphRequest.Callback() {
        public void onCompleted(GraphResponse response) {
            JSONObject jObj = response.getJSONObject();
            String gender = jObj.optString(Config.GENDER);
            if(gender.equalsIgnoreCase(getString(R.string.male_eng))){
                gender = getString(R.string.male);
            }else if (gender.equalsIgnoreCase(getString(R.string.female_eng))){
                gender = getString(R.string.female);
            }
            String birthday = Config.convertBirthDayFacebook(jObj.optString(Config.BIRTHDAY));
            String avatar = "http://graph.facebook.com/" + jObj.optString(Config.ID) + "/picture?type=large";
            try {
                jObj_Facebook_Account = new JSONObject();
                jObj_Facebook_Account.put(Config.FACEBOOK_ID, jObj.optString(Config.ID));
                jObj_Facebook_Account.put(Config.FACEBOOK_ACCESS_TOKEN, facebook_access_token);
                jObj_Facebook_Account.put(Config.EMAIL, jObj.optString(Config.EMAIL));
                jObj_Facebook_Account.put(Config.FIRST_NAME, jObj.optString(Config.FIRST_NAME));
                jObj_Facebook_Account.put(Config.LAST_NAME, jObj.optString(Config.LAST_NAME));
                jObj_Facebook_Account.put(Config.GENDER, gender);
                jObj_Facebook_Account.put(Config.BIRTHDAY, birthday);
                jObj_Facebook_Account.put(Config.AVATAR, avatar);
                jObj_Facebook_Account.put(Config.ACCOUNT_TYPE, Config.ACCOUNT_FACEBOOK);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isLoginFacebook = true;
            RequestAPI.getInstance().context(LoginActivity.this).url(Config.URL_LOGIN)
                .method(RequestAPI.POST).message(getString(R.string.waiting))
                .isShowDialog(true).isShowToast(true).isParams(true).isAuthorization(false).execute(loginListener);
        }
    };

    private RequestAPI.RequestAPIListener loginListener = new RequestAPI.RequestAPIListener() {
        @Override
        public JSONObject onRequest() throws JSONException {
            if(isLoginFacebook){
                return jObj_Facebook_Account;
            } else {
                JSONObject params = new JSONObject();
                params.put(Config.EMAIL, edt_email.getText().toString());
                params.put(Config.PASSWORD, edt_password.getText().toString());
                params.put(Config.ACCOUNT_TYPE, Config.ACCOUNT_NORMAL);
                return params;
            }
        }

        @Override
        public String onAuthorization() {
            return null;
        }

        @Override
        public void onResult(String contentMessage) throws JSONException {
            Account account = Config.convertJsonToAccount(new JSONObject(contentMessage));
            intent(MainActivity.class, false, account);
            if(!Config.isServiceRunning(LoginActivity.this, ChatService.class)){
                PreferencesManager.getInstance().setMyAccount(LoginActivity.this, account);
                startService(new Intent(LoginActivity.this, ChatService.class));
            }
        }

        @Override
        public void onError(Exception e) {}
    };

    private boolean checkLoginValid(){
        if(!Config.isInternetConnect(this, true)){
            return false;
        }
        String email = edt_email.getText().toString();
        String password = edt_password.getText().toString();
        String err = getString(R.string.missing_input);
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            err += " " + getString(R.string.hint_email) + " " + getString(R.string.and)
                + " " + getString(R.string.hint_password);
            Config.showCustomToast(LoginActivity.this, R.mipmap.ic_toast_error, err);
            return false;
        } else if (TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            err += " " + getString(R.string.hint_email);
            Config.showCustomToast(LoginActivity.this, R.mipmap.ic_toast_error, err);
            return false;
        } else if (!TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            err += " " + getString(R.string.hint_password);
            Config.showCustomToast(LoginActivity.this, R.mipmap.ic_toast_error, err);
            return false;
        }
        return true;
    }

    private void intent(Class<? extends Activity> clazz, boolean isIntentNext, Account account){
        Intent i = new Intent(LoginActivity.this, clazz);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(account != null){
            i.putExtra(Config.BUNDLE_ACCOUNT, account);
        }
        i.putExtra(Config.FROM_ACTIVITY, Config.LOGIN_ACTIVITY);
        startActivity(i);
        if(isIntentNext){
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

}
