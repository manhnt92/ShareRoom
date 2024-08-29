package com.manhnt.shareroom;

import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
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

public class RegisterActivity extends Activity implements OnClickListener {

	private CallbackManager callbackManager;
	private MaterialEditText edt_email, edt_password, edt_confirm_password, edt_first_name, edt_last_name;
	private String facebook_access_token;
	private JSONObject jObj_Facebook_Account;
	private boolean isLoginFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		callbackManager = CallbackManager.Factory.create();
		setContentView(R.layout.register_activity);
		getWidget();
	}
	
	private void getWidget(){
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		edt_email = manager.MaterialEditText(R.id.edt_email, true);
		edt_password = manager.MaterialEditText(R.id.edt_password, true);
		edt_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
		edt_confirm_password = manager.MaterialEditText(R.id.edt_confirm_password, true);
		edt_confirm_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
		edt_first_name = manager.MaterialEditText(R.id.edt_first_name, true);
		edt_last_name = manager.MaterialEditText(R.id.edt_last_name, true);
		manager.TextView(R.id.txt_login_with_facebook, true);
		manager.TextView(R.id.txt_information, true);
		manager.ButtonRectangle(R.id.btn_register, this, true);
		LoginButton btn_login_fb = manager.LoginButton(R.id.btn_login_fb, true);
		btn_login_fb.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));
		btn_login_fb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(final LoginResult result) {
				facebook_access_token = result.getAccessToken().getToken();
				new GraphRequest(result.getAccessToken(),"/"+result.getAccessToken().getUserId(), null,
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
				intent(LoginActivity.class, null, null);
				break;
			case R.id.btn_register:
				if(checkRegisterValid()){
					isLoginFacebook = false;
					RequestAPI.getInstance().context(this).url(Config.URL_CREATE_ACCOUNT)
						.message(getString(R.string.waiting)).isParams(true).isShowToast(true)
						.isShowDialog(true).isAuthorization(false).method(RequestAPI.POST).execute(Listener);
				}
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		intent(LoginActivity.class, null, null);
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
			RequestAPI.getInstance().context(RegisterActivity.this).url(Config.URL_LOGIN)
				.method(RequestAPI.POST).message(getString(R.string.waiting)).isShowToast(true)
				.isShowDialog(true).isParams(true).isAuthorization(false).execute(Listener);
		}
	};

	private RequestAPI.RequestAPIListener Listener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			if(isLoginFacebook) {
				return jObj_Facebook_Account;
			} else {
				JSONObject params = new JSONObject();
				params.put(Config.EMAIL, edt_email.getText().toString());
				params.put(Config.PASSWORD, edt_password.getText().toString());
				params.put(Config.FIRST_NAME, edt_first_name.getText().toString());
				params.put(Config.LAST_NAME, edt_last_name.getText().toString());
				return params;
			}
		}

		@Override
		public String onAuthorization() {
			return null;
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			if(isLoginFacebook) {
				Account account = Config.convertJsonToAccount(new JSONObject(contentMessage));
				intent(MainActivity.class, account, null);
				if (!Config.isServiceRunning(RegisterActivity.this, ChatService.class)) {
					PreferencesManager.getInstance().setMyAccount(RegisterActivity.this, account);
					startService(new Intent(RegisterActivity.this, ChatService.class));
				}
			} else {
				intent(LoginActivity.class, null, edt_email.getText().toString());
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private boolean checkRegisterValid(){
		if(!Config.isInternetConnect(this, true)){
			return false;
		}
		String email = edt_email.getText().toString();
		String password = edt_password.getText().toString();
		String confirm_password = edt_confirm_password.getText().toString();
		String first_name = edt_first_name.getText().toString();
		String last_name = edt_last_name.getText().toString();
		if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm_password)
			&& !TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name)){
			if(!password.equalsIgnoreCase(confirm_password)){
				Config.showCustomToast(RegisterActivity.this, R.mipmap.ic_toast_error, getString(R.string.password_not_equal_confirmPass));
				return false;
			}
		} else {
			String err = getString(R.string.missing_input);
			int count_err = 0;
			if(TextUtils.isEmpty(email)){
				count_err++;
				err += " " + getString(R.string.hint_email);
			}
			if(TextUtils.isEmpty(password)){
				count_err++;
				if(count_err > 1){err += ",";}
				err += " " + getString(R.string.hint_password);
			}
			if(TextUtils.isEmpty(confirm_password)){
				count_err++;
				if(count_err > 1){err += ",";}
				err += " " + getString(R.string.hint_confirmPass);
			}
			if(TextUtils.isEmpty(first_name)){
				count_err++;
				if(count_err > 1){err += ",";}
				err += " " + getString(R.string.hint_first_name);
			}
			if(TextUtils.isEmpty(last_name)){
				count_err++;
				if(count_err > 1){err += ",";}
				err += " " + getString(R.string.hint_last_name);
			}
			Config.showCustomToast(RegisterActivity.this, R.mipmap.ic_toast_error, err);
			return false;
		}
		return true;
	}

	private void intent(Class<? extends Activity> clazz, Account account, String email){
		Intent i = new Intent(RegisterActivity.this, clazz);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		if(account != null){
			i.putExtra(Config.BUNDLE_ACCOUNT, account);
		}
		if(email != null){
			i.putExtra(Config.BUNDLE_EMAIL, email);
		}
		i.putExtra(Config.FROM_ACTIVITY, Config.REGISTER_ACTIVITY);
		startActivity(i);
		overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
	}

}
