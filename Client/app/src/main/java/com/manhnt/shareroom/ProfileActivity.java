package com.manhnt.shareroom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.cloudinary.Cloudinary;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.PreferencesManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ProfileActivity extends FragmentActivity implements OnClickListener, OnDateSetListener, LocationListener,
	GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private ImageLoader mImageLoader;
	private DisplayImageOptions options;
	private ImageView img_avatar;
	private String img_avatar_name;
	private String real_path_img_avatar;
	private Account mAccount, cloneAccount;
	private TextView txt_name, txt_password_content, txt_age_content, txt_gender_content, txt_birthday_content,
		txt_phonenumber_content, txt_occupation_content, txt_address_content, txt_description_content;
	private ImageButton btn_save;
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private LatLng mCurrentLocation;
	private String first_name, last_name;
	private Cloudinary cloudinary;
	private boolean isIntentBack = false;
	private TextView curTextView;
	private MaterialEditText edit_first_name, edit_last_name, edit_password, edit_new_password,
		edit_confirm_new_password;
	private WidgetManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_activity);
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.mipmap.ic_empty_icon)
			.showImageForEmptyUri(R.mipmap.ic_empty_icon)
			.showImageOnFail(R.mipmap.ic_empty_icon)
			.bitmapConfig(Bitmap.Config.ARGB_8888).imageScaleType(ImageScaleType.EXACTLY)
			.displayer(new RoundedBitmapDisplayer(10)).build();
		mImageLoader = ImageLoader.getInstance();
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.defaultDisplayImageOptions(options)
			.diskCacheExtraOptions(200, 200, null)
			.memoryCache(new WeakMemoryCache()).build());
		getExtraBundle();
		getWidget();
		initCloudinary();
		if (isGooglePlayServicesAvailable()) {
			initCurrentLocation();
		}
		cloneAccount();
	}

	private void cloneAccount(){
		try {
			cloneAccount = (Account) mAccount.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private void initCloudinary() {
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", Config.CLOUD_NAME);
		config.put("api_key", Config.CLOUD_API_KEY);
		config.put("api_secret", Config.CLOUD_API_SECRET);
		cloudinary = new Cloudinary(config);
	}

	private void getExtraBundle() {
		mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
	}

	@SuppressLint("SetTextI18n")
	private void getWidget() {
		manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		btn_save = manager.ImageButton(R.id.btn_save, this, true);
		btn_save.setVisibility(View.INVISIBLE);
		img_avatar = manager.ImageView(R.id.img_avatar, this, true);
		txt_name = manager.TextView(R.id.txt_name, true);
		TextView txt_email_content = manager.TextView(R.id.txt_email_content, true);
		LinearLayout ll_password = (LinearLayout) findViewById(R.id.ll_password);
		manager.TextView(R.id.txt_password, true);
		txt_password_content = manager.TextView(R.id.txt_password_content, true);
		manager.TextView(R.id.txt_age, true);
		manager.TextView(R.id.txt_gender, true);
		manager.TextView(R.id.txt_birthday, true);
		manager.TextView(R.id.txt_phonenumber, true);
		manager.TextView(R.id.txt_occupation, true);
		manager.TextView(R.id.txt_address, true);
		manager.TextView(R.id.txt_description, true);
		txt_age_content = manager.TextView(R.id.txt_age_content, true);
		txt_gender_content = manager.TextView(R.id.txt_gender_content, true);
		txt_birthday_content = manager.TextView(R.id.txt_birthday_content, true);
		txt_phonenumber_content = manager.TextView(R.id.txt_phonenumber_content, true);
		txt_occupation_content = manager.TextView(R.id.txt_occupation_content, true);
		txt_address_content = manager.TextView(R.id.txt_address_content, true);
		txt_description_content = manager.TextView(R.id.txt_description_content, true);
		manager.ImageButton(R.id.btn_edit_name, this, true);
		manager.ImageButton(R.id.btn_edit_password, this, true);
		manager.ImageButton(R.id.btn_edit_age, this, true);
		manager.ImageButton(R.id.btn_edit_gender, this, true);
		manager.ImageButton(R.id.btn_edit_birthday, this, true);
		manager.ImageButton(R.id.btn_edit_phonenumber, this, true);
		manager.ImageButton(R.id.btn_edit_occupation, this, true);
		manager.ImageButton(R.id.btn_edit_address, this, true);
		manager.ImageButton(R.id.btn_edit_description, this, true);

		if (mAccount != null) {
			if (!TextUtils.isEmpty(mAccount.getAvatar()) && !mAccount.getAvatar().equalsIgnoreCase("null")) {
				mImageLoader.displayImage(mAccount.getAvatar(), img_avatar, options);
			} else {
				boolean gender = !mAccount.getGender().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getGender());
				int resID = gender ? (mAccount.getGender().equalsIgnoreCase(getString(R.string.female)) ?
					R.drawable.ic_user_female_press : R.drawable.ic_user_male_press) : R.drawable.ic_user_male_press;
				img_avatar.setImageResource(resID);
			}
			first_name = mAccount.getFirst_name();
			last_name = mAccount.getLast_name();
			txt_name.setText(first_name + " " + last_name);
			txt_email_content.setText(mAccount.getEmail());
			boolean pass = mAccount.getAccount_type() == Config.ACCOUNT_NORMAL;
			ll_password.setVisibility(pass ? View.VISIBLE : View.INVISIBLE);
			txt_password_content.setText(pass ? mAccount.getPassword() : "");
			String no_content = getString(R.string.no_content);
			boolean age = mAccount.getAge() > 0;
			txt_age_content.setText(age ? "" + mAccount.getAge() : no_content);
			boolean gender = !mAccount.getGender().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getGender());
			txt_gender_content.setText(gender ? mAccount.getGender() : no_content);
			boolean birthday = !mAccount.getBirthday().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getBirthday());
			txt_birthday_content.setText(birthday ? mAccount.getBirthday() : no_content);
			boolean phoneNum = !mAccount.getPhoneNumber().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getPhoneNumber());
			txt_phonenumber_content.setText(phoneNum ? mAccount.getPhoneNumber() : no_content);
			boolean occupation = !mAccount.getOccupation().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getOccupation());
			txt_occupation_content.setText(occupation ? mAccount.getOccupation() : no_content);
			boolean address = !mAccount.getAddress().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getAddress());
			txt_address_content.setText(address ? mAccount.getAddress() : no_content);
			boolean description = !mAccount.getDescription().equalsIgnoreCase("null") && !TextUtils.isEmpty(mAccount.getDescription());
			txt_description_content.setText(description ? mAccount.getDescription() : no_content);
		}
	}

	@Override
	public void onBackPressed() {
		if(!checkBtnSave()) {
			super.onBackPressed();
			intentBack(mAccount);
		}
	}

	private boolean checkBtnSave(){
		if(btn_save.getVisibility() == View.VISIBLE){
			DialogManager.getInstance().YesNoDialog(ProfileActivity.this, R.string.update_account,
			R.string.question_update, R.string.OK, R.string.NO, saveListener, true).show();
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				if(!checkBtnSave()) {
					intentBack(mAccount);
				}
				break;
			case R.id.btn_save:
				if (Config.isInternetConnect(this, true)) {
					new AsyncTaskUpdateAccount().execute();
				}
				break;
			case R.id.img_avatar:
				DialogManager.getInstance().ListOneChoiceDialog(this, R.string.edit_avatar,
					getResources().getStringArray(R.array.list_edit_image), -1, true, false, avatarListener).show();
				break;
			case R.id.btn_edit_name:
				DialogManager.getInstance().CustomViewDialog(this, R.string.edit_name, R.layout.edit_name_dialog,
					true, EditNameListener).show();
				break;
			case R.id.btn_edit_password:
				DialogManager.getInstance().CustomViewDialog(this, R.string.edit_password, R.layout.edit_password_dialog,
					true, EditPasswordListener).show();
				break;
			case R.id.btn_edit_age:
				curTextView = txt_age_content;
				DialogManager.getInstance().InputDialog(this, R.string.edit_age, InputType.TYPE_CLASS_NUMBER,
					getString(R.string.hint_age), txt_age_content.getText().toString(), true, inputListener).show();
				break;
			case R.id.btn_edit_gender:
				DialogManager.getInstance().ListOneChoiceDialog(this, R.string.choice_gender,
					getResources().getStringArray(R.array.array_gender), -1, false, true, genderListener).show();
				break;
			case R.id.btn_edit_birthday:
				Calendar calendar = Calendar.getInstance();
				DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH), false, Config.getTypeface(getAssets()));
				datePickerDialog.setVibrate(false);
				datePickerDialog.setYearRange(1985, 2028);
				datePickerDialog.setCloseOnSingleTapDay(false);
				datePickerDialog.show(getSupportFragmentManager(), "TAG");
				break;
			case R.id.btn_edit_phonenumber:
				curTextView = txt_phonenumber_content;
				DialogManager.getInstance().InputDialog(this, R.string.edit_phonenumber, InputType.TYPE_CLASS_NUMBER,
					getString(R.string.hint_phonenumber), txt_phonenumber_content.getText().toString(), true, inputListener).show();
				break;
			case R.id.btn_edit_occupation:
				curTextView = txt_occupation_content;
				DialogManager.getInstance().InputDialog(this, R.string.edit_occupation, InputType.TYPE_CLASS_TEXT,
					getString(R.string.hint_occupation), txt_occupation_content.getText().toString(), true, inputListener).show();
				break;
			case R.id.btn_edit_address:
				curTextView = txt_address_content;
				MaterialDialog dialog = DialogManager.getInstance().InputDialog(this, R.string.edit_address, InputType.TYPE_CLASS_TEXT,
					getString(R.string.hint_my_address), txt_address_content.getText().toString(), true, inputListener);
				dialog = dialog.getBuilder().negativeText(getString(R.string.getCurrentLocation)).negativeColor(Color.WHITE).onNegative(new SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						if (mCurrentLocation != null) {
							new GetLocationNameFromLatLng(dialog.getInputEditText()).execute(mCurrentLocation.latitude, mCurrentLocation.longitude);
						} else {
							Config.showCustomToast(ProfileActivity.this, 0, getString(R.string.cannot_getCurLocation));
						}
					}
				}).build();
				manager.MDButton(dialog.getView(), com.manhnt.shareroomlibrary.R.id.buttonDefaultNegative, true);
				dialog.show();
				break;
			case R.id.btn_edit_description:
				curTextView = txt_description_content;
				DialogManager.getInstance().InputDialog(this, R.string.edit_description, InputType.TYPE_CLASS_TEXT,
					getString(R.string.hint_description), txt_description_content.getText().toString(), true, inputListener).show();
				break;
			default:
				break;
		}
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
		String date = "";
		String link = "/";
		if (month >= 9 && day >= 10) {
			date = day + link + (month + 1) + link + year;
		} else if (month >= 9 && day < 10) {
			date = "0" + day + link + (month + 1) + link + year;
		} else if (month < 9 && day >= 10) {
			date = day + link + "0" + (month + 1) + link + year;
		} else if (month < 9 && day < 10) {
			date = "0" + day + link + "0" + (month + 1) + link + year;
		}
		txt_birthday_content.setText(date);
		setVisibilityButtonSave();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult arg0) {}

	@Override
	public void onConnected(@Nullable Bundle arg0) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {}

	@Override
	public void onLocationChanged(Location arg0) {
		mCurrentLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
		mGoogleApiClient.disconnect();
	}
	
	@SuppressWarnings("deprecation")
	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}
	
	private void initCurrentLocation(){
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(1000 * 10);
		mLocationRequest.setFastestInterval(1000 * 5);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		if (mGoogleApiClient == null) {
		    mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(LocationServices.API)
		        .build();
		    mGoogleApiClient.connect();
		}
	}

	private class GetLocationNameFromLatLng extends AsyncTask<Double, Void, String>{
		
		private MaterialDialog mDialog;
		private EditText mEditText;

		public GetLocationNameFromLatLng(EditText edt){
			this.mEditText = edt;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(ProfileActivity.this, getString(R.string.waiting));
			mDialog.show();
		}
		
		@Override
		protected String doInBackground(Double... params) {
			try {
				String url = "http://maps.google.com/maps/api/geocode/json?latlng=" + params[0] + "," + params[1] + "&sensor=false";
				url = url.replace(" ", "%20");
				URL mUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json");
		        conn.connect();
		        InputStream inputStream = new BufferedInputStream(conn.getInputStream());
	            String rs = Config.convertInputStreamToString(inputStream);
	            JSONObject jObj = new JSONObject(rs);
	            JSONArray jArray = jObj.optJSONArray("results");
	            String address = jArray.getJSONObject(0).getString("formatted_address");
	            address = address.replace(", " + address.split(", ")[address.split(", ").length - 1], "");
	            return address;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result != null){
				mEditText.setText(result);
			}
			mDialog.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == Config.CAMERA){
				File f = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
				for (File temp : f.listFiles()) {
					if (temp.getName().equals(img_avatar_name + Config.PNG)) {
						f = temp;
						real_path_img_avatar = f.getAbsolutePath();
						break;
					}
				}
				img_avatar.setImageURI(Uri.fromFile(f));
			} else if (requestCode == Config.GALLERY) {
				 if (Build.VERSION.SDK_INT < 11){
					 real_path_img_avatar = Config.getRealPathFromURI_BelowAPI11(this, data.getData());
				 } else {
					 real_path_img_avatar = Config.getRealPathFromURI_AboveAPI11(this, data.getData());
				 }
				assert real_path_img_avatar != null;
				img_avatar.setImageURI(Uri.fromFile(new File(real_path_img_avatar)));
			}
			if(btn_save.getVisibility() == View.INVISIBLE){
				btn_save.setVisibility(View.VISIBLE);
			}
		}
	}
	
    private class AsyncTaskUpdateAccount extends AsyncTask<String, Void, String> {
    	
    	private MaterialDialog mDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(ProfileActivity.this, getString(R.string.waiting));
			mDialog.show();
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			if(real_path_img_avatar != null){
				File f = new File(real_path_img_avatar);
				try {
					JSONObject jObj = cloudinary.uploader().upload(f, Cloudinary.emptyMap());
					return jObj.optString("url");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return mAccount.getAvatar();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			mAccount.setAvatar(result);
			RequestAPI.getInstance().context(ProfileActivity.this).method(RequestAPI.PUT)
				.message(getString(R.string.waiting)).isShowToast(true).isShowDialog(true).isParams(true)
				.url(Config.URL_UPDATE_ACCOUNT).isAuthorization(true).execute(updateAccountListener);
		}
    	
    }

	private DialogManager.CustomViewListener EditPasswordListener = new DialogManager.CustomViewListener() {
		@Override
		public void onAttachCustomView(View view) {
			edit_password = manager.MaterialEditText(view, R.id.edt_password, true);
			edit_new_password = manager.MaterialEditText(view, R.id.edt_new_password, true);
			edit_confirm_new_password = manager.MaterialEditText(view, R.id.edt_confirm_new_password, true);
		}

		@Override
		public void onOK(MaterialDialog dialog) {
			if (edit_password.getText().toString().equalsIgnoreCase(txt_password_content.getText().toString())) {
				String new_pass = edit_new_password.getText().toString();
				String confirm_new_pass = edit_confirm_new_password.getText().toString();
				if (TextUtils.isEmpty(new_pass) || TextUtils.isEmpty(confirm_new_pass)) {
					String err = getString(R.string.missing_input);
					int count = 0;
					if (TextUtils.isEmpty(new_pass)) {
						count++;
						err += " " + getString(R.string.hint_new_password);
					}
					if (TextUtils.isEmpty(confirm_new_pass)) {
						err += count > 0 ? ", " : " ";
						err += getString(R.string.hint_confirm_new_password);
					}
					Config.showCustomToast(ProfileActivity.this, R.mipmap.ic_toast_error, err);
				} else {
					if (new_pass.equalsIgnoreCase(confirm_new_pass)) {
						txt_password_content.setText(edit_new_password.getText().toString());
						dialog.dismiss();
						setVisibilityButtonSave();
					} else {
						Config.showCustomToast(ProfileActivity.this, R.mipmap.ic_toast_error, getString(R.string.password_not_equal_confirmPass));
					}
				}
			} else {
				Config.showCustomToast(ProfileActivity.this, R.mipmap.ic_toast_error, getString(R.string.invalid_old_password));
			}
		}

		@Override
		public void onCancel(MaterialDialog dialog) {
			dialog.dismiss();
		}
	};

	private DialogManager.CustomViewListener EditNameListener = new DialogManager.CustomViewListener() {
		@Override
		public void onAttachCustomView(View view) {
			edit_first_name = manager.MaterialEditText(view, R.id.edt_first_name, true);
			edit_last_name = manager.MaterialEditText(view, R.id.edt_last_name, true);
		}

		@Override
		public void onOK(MaterialDialog dialog) {
			String first = edit_first_name.getText().toString();
			String last = edit_last_name.getText().toString();
			if (!TextUtils.isEmpty(first) && !TextUtils.isEmpty(last)) {
				String name = first + " " + last;
				if (!name.equalsIgnoreCase(txt_name.getText().toString())) {
					setVisibilityButtonSave();
				}
				first_name = first;
				last_name = last;
				txt_name.setText(name);
				dialog.dismiss();
			} else {
				String err = getString(R.string.missing_input);
				int count = 0;
				if (TextUtils.isEmpty(first)) {
					count++;
					err += " " + getString(R.string.hint_first_name);
				}
				if (TextUtils.isEmpty(last)) {
					err += (count > 0) ? ", " : " ";
					err += getString(R.string.hint_last_name);
				}
				Config.showCustomToast(ProfileActivity.this, R.mipmap.ic_toast_error, err);
			}
		}

		@Override
		public void onCancel(MaterialDialog dialog) {
			dialog.dismiss();
		}
	};

	private DialogManager.InputDialogListener inputListener = new DialogManager.InputDialogListener() {
		@Override
		public void onInput(MaterialDialog dialog, CharSequence input) {
			if (!curTextView.getText().toString().equalsIgnoreCase(input.toString())) {
				curTextView.setText(input);
				setVisibilityButtonSave();
			}
			dialog.dismiss();
		}
	};

	private DialogManager.ListOneChoiceDialogListener avatarListener = new DialogManager.ListOneChoiceDialogListener() {
		@Override
		public void onChoice(MaterialDialog dialog, int index) {
			if (index == Config.CAMERA) {
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				img_avatar_name = "" + System.currentTimeMillis();
				File f = new File(Config.PATH, "" + img_avatar_name + Config.PNG);
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(i, Config.CAMERA);
			} else if (index == Config.GALLERY) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, Config.GALLERY);
			} else {
				dialog.dismiss();
			}
		}
	};

	private DialogManager.ListOneChoiceDialogListener genderListener = new DialogManager.ListOneChoiceDialogListener() {
		@Override
		public void onChoice(MaterialDialog dialog, int index) {
			if (index != -1) {
				txt_gender_content.setText(getResources().getStringArray(R.array.array_gender)[index]);
				setVisibilityButtonSave();
			}
			dialog.dismiss();
		}
	};

	private DialogManager.YesNoDialogListener saveListener =  new DialogManager.YesNoDialogListener() {
		@Override
		public void onYes(MaterialDialog dialog) {
			isIntentBack = true;
			new AsyncTaskUpdateAccount().execute();
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			dialog.dismiss();
			intentBack(cloneAccount);
		}
	};

	private RequestAPI.RequestAPIListener updateAccountListener = new RequestAPI.RequestAPIListener() {
		@Override
		public JSONObject onRequest() throws JSONException {
			JSONObject params = new JSONObject();
			if(mAccount.getAccount_type() == Config.ACCOUNT_NORMAL){
				params.put(Config.PASSWORD, txt_password_content.getText().toString());
			} else {
				params.put(Config.PASSWORD, "null");
			}
			params.put(Config.FIRST_NAME, first_name);
			params.put(Config.LAST_NAME, last_name);
			params.put(Config.GENDER, txt_gender_content.getText().toString());
			params.put(Config.BIRTHDAY, txt_birthday_content.getText().toString());
			if(!txt_age_content.getText().toString().equalsIgnoreCase(getString(R.string.no_content))) {
				params.put(Config.AGE, txt_age_content.getText().toString());
			} else {
				params.put(Config.AGE, 0);
			}
			params.put(Config.ADDRESS, txt_address_content.getText().toString());
			params.put(Config.OCCUPATION, txt_occupation_content.getText().toString());
			params.put(Config.DESCRIPTION, txt_description_content.getText().toString());
			params.put(Config.PHONENUMBER, txt_phonenumber_content.getText().toString());
			params.put(Config.AVATAR, mAccount.getAvatar());
			params.put(Config.ACCOUNT_TYPE, mAccount.getAccount_type());
			return params;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			if(mAccount.getAccount_type() == Config.ACCOUNT_NORMAL){
				mAccount.setPassword(txt_password_content.getText().toString());
			}
			mAccount.setFirst_name(first_name);
			mAccount.setLast_name(last_name);
			mAccount.setGender(txt_gender_content.getText().toString());
			mAccount.setBirthday(txt_birthday_content.getText().toString());
			if(!txt_age_content.getText().toString().equalsIgnoreCase(getString(R.string.no_content))) {
				mAccount.setAge(Integer.parseInt(txt_age_content.getText().toString()));
			} else {
				mAccount.setAge(0);
			}
			mAccount.setAddress(txt_address_content.getText().toString());
			mAccount.setOccupation(txt_occupation_content.getText().toString());
			mAccount.setDescription(txt_description_content.getText().toString());
			mAccount.setPhoneNumber(txt_phonenumber_content.getText().toString());
			PreferencesManager.getInstance().setMyAccount(ProfileActivity.this, mAccount);
			btn_save.setVisibility(View.INVISIBLE);
			if(isIntentBack){
				intentBack(mAccount);
			} else {
				cloneAccount();
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void intentBack(Account account){
		Intent intent_back = new Intent(ProfileActivity.this, MainActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.PROFILE_ACTIVITY);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, account);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void setVisibilityButtonSave(){
		if (btn_save.getVisibility() == View.INVISIBLE) {
			btn_save.setVisibility(View.VISIBLE);
		}
	}

}
