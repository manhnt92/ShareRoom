package com.manhnt.shareroom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cloudinary.Cloudinary;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.Image;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import com.manhnt.object.Room_Images;
import com.manhnt.adapter.ImageAdapter;
import com.manhnt.adapter.ImageAdapter.ImageAdapterClickListener;

public class PostRoom_GetImages extends Activity implements OnClickListener, ImageAdapterClickListener {

	private int position_Province_ID, position_District_ID, position_Ward_ID;
	private Account mAccount;
	private Room room;
	private Room_Images room_images;
	private ImageButton btn_save;
	private ImageAdapter image_adapter;
	private ArrayList<Image> list_Room_Images;
	private String img_name;
	private Cloudinary cloudinary;
	private MyRooms myRooms;
	private boolean isEdit;
	private HashMap<Integer,String> hash_map_delete_image = new HashMap<>();
	private HashMap<String,String> hash_map_insert_image = new HashMap<>();
	private HashMap<Integer, String> hash_map_update_image = new HashMap<>();
	private ArrayList<Image> list_Room_Images_RollBack;
	private boolean isAutoIntentBack = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_room_get_images);
		getExtraBundle();
		getWidget();
		initCloudinary();
		room_images = room.getRoom_images();
		if(room_images != null){
			list_Room_Images.clear();
			list_Room_Images.addAll(room_images.getList_images());
			cloneArrayList(list_Room_Images);
			image_adapter.notifyDataSetChanged();
		}
		if(isEdit){
			list_Room_Images.add(new Image(null, null, false, false));
		}
	}
	
	private void cloneArrayList(ArrayList<Image> list_room_image){
		list_Room_Images_RollBack = new ArrayList<>(list_room_image.size());
		for(Image img : list_room_image){
			try {
				list_Room_Images_RollBack.add((Image)img.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		image_adapter.notifyDataSetChanged();
	}

	private void initCloudinary() {
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", Config.CLOUD_NAME);
		config.put("api_key", Config.CLOUD_API_KEY);
		config.put("api_secret", Config.CLOUD_API_SECRET);
		cloudinary = new Cloudinary(config);
	}

	private void getExtraBundle() {
		int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
		if(from == Config.POST_ROOM_GET_PROPERTIES || from == Config.POST_ROOM_GET_AMENITIES){
			room = (Room) getIntent().getExtras().getSerializable(Config.BUNDLE_ROOM);
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			position_Province_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_PROVINCE_ID);
			position_District_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_DISTRICT_ID);
			position_Ward_ID = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_WARD_ID);
			isEdit = false;
		} else if(from == Config.ROOM_DETAIL_ACTIVITY){
			mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
			myRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
			if(myRooms != null) {
				room = myRooms.getList_room().get(myRooms.getPosition());
			}
			isEdit = true;
		}
	}
	
	private void getWidget() {
		WidgetManager manager = WidgetManager.getInstance(this);
		manager.TextView(R.id.title, true);
		manager.ImageButton(R.id.btn_back, this, true);
		ImageButton btn_next = manager.ImageButton(R.id.btn_next, this, true);
		btn_save = manager.ImageButton(R.id.btn_save, this, true);
		btn_save.setVisibility(View.GONE);
		if(isEdit){
			btn_next.setVisibility(View.GONE);
		}else {
			btn_next.setVisibility(View.VISIBLE);
		}
		GridView grid_view_img_room = (GridView) findViewById(R.id.grid_view_img_room);
		list_Room_Images = new ArrayList<>();
		list_Room_Images.add(new Image(null, null, false, false));
		image_adapter = new ImageAdapter(this, 0, list_Room_Images);
		grid_view_img_room.setAdapter(image_adapter);
		image_adapter.setAdapterListener(this);
	}
	
	@Override
	public void onBackPressed() {
		if(isEdit){
			checkBtnSave();
		} else {
			intentBack();
		}
	}
	
	private void checkBtnSave(){
		if(btn_save.getVisibility() == View.GONE){
			intentBackEdit();
		}else {
			DialogManager.getInstance().YesNoDialog(this, R.string.room_images_title,
				R.string.question_update, R.string.OK, R.string.NO, saveListener, true).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				if(isEdit){
					checkBtnSave();
				}else {
					intentBack();
				}
				break;
			case R.id.btn_next:
				HashMap<Integer, String> hm_img = new HashMap<>();
				for(int i = 0; i < list_Room_Images.size(); i++){
					if(!list_Room_Images.get(i).isUrl() && list_Room_Images.get(i).isCloseButton()){
						hm_img.put(i, list_Room_Images.get(i).getPath());
					}
				}
				if (hm_img.size() > 0) {
					new AsyncTaskUpLoadImages(hm_img).execute();
				} else {
					intentNext();
				}
				break;
			case R.id.btn_save:
				update();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBtnDeleteClickListener(final int index) {
		DialogManager.getInstance().YesNoDialog(this, R.string.room_images_title, R.string.question_remove_image,
		R.string.OK, R.string.back, new DialogManager.YesNoDialogListener() {
			@Override
			public void onYes(MaterialDialog dialog) {
				if(list_Room_Images.get(index).isUrl()){
					if(isEdit){
						hash_map_delete_image.put(list_Room_Images.get(index).getId(), list_Room_Images.get(index).getPath());
						btn_save.setVisibility(View.VISIBLE);
					}else {
						String path = list_Room_Images.get(index).getPath();
						String image_name = (path.split("/")[path.split("/").length - 1]);
						String public_id = image_name.split("\\.")[0];
						new AsyncTaskRemoveImage().execute(public_id);
					}
					list_Room_Images.remove(index);
					image_adapter.notifyDataSetChanged();
				}else {
					list_Room_Images.remove(index);
					image_adapter.notifyDataSetChanged();
					Config.showCustomToast(PostRoom_GetImages.this, R.mipmap.ic_toast_success, getString(R.string.delete_success));
				}
				dialog.dismiss();
			}

			@Override
			public void onNo(MaterialDialog dialog) {
				dialog.dismiss();
			}
		}, true).show();
	}

	@Override
	public void onBtnNoteClickListener(final int index) {
		DialogManager.getInstance().InputDialog(this, R.string.add_note, InputType.TYPE_CLASS_TEXT,
		getString(R.string.add_note_hint), list_Room_Images.get(index).getNote(), true,
		new DialogManager.InputDialogListener() {
			@Override
			public void onInput(MaterialDialog dialog, CharSequence input) {
				if(isEdit){
					if(list_Room_Images.get(index).getId() != 0){
						hash_map_update_image.put(list_Room_Images.get(index).getId(), input.toString());
						btn_save.setVisibility(View.VISIBLE);
					}
					list_Room_Images.get(index).setNote(input.toString());
					image_adapter.notifyDataSetChanged();
					dialog.dismiss();
				} else {
					list_Room_Images.get(index).setNote(input.toString());
					image_adapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			}
		}).show();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onImageRoomClickListener(int index) {
		DialogManager.getInstance().ListOneChoiceDialog(this, R.string.add_image,
		getResources().getStringArray(R.array.list_edit_image), -1, false, true,
		new DialogManager.ListOneChoiceDialogListener() {
			@Override
			public void onChoice(MaterialDialog dialog, int index) {
				if(index == Config.CAMERA){
					Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					img_name = ""+System.currentTimeMillis();
					File f = new File(Config.PATH,""+ img_name + Config.PNG);
					i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(i, Config.CAMERA);
				}else if (index == Config.GALLERY){
					Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, Config.GALLERY);
				}else {
					dialog.dismiss();
				}
			}
		}).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String real_path_img = "";
		if(resultCode == RESULT_OK) {
			if(requestCode == Config.CAMERA){
				File f = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
				for (File temp : f.listFiles()) {
					if (temp.getName().equals(img_name + Config.PNG)) {
						f = temp;
						real_path_img = f.getAbsolutePath();
						break;
					}
				}
			} else if (requestCode == Config.GALLERY) {
				if (Build.VERSION.SDK_INT < 11){
					real_path_img = Config.getRealPathFromURI_BelowAPI11(this, data.getData());
				} else {
					real_path_img = Config.getRealPathFromURI_AboveAPI11(this, data.getData());
				}
			}
			if(isEdit){
				btn_save.setVisibility(View.VISIBLE);
			}
			list_Room_Images.remove(list_Room_Images.size() - 1);
			list_Room_Images.add(new Image(real_path_img, "", false, true));
			list_Room_Images.add(new Image(null, null, false, false));
			image_adapter.notifyDataSetChanged();
		}
	}

	private class AsyncTaskUpLoadImages extends AsyncTask<Void, Void, Void> {
    	
    	private MaterialDialog mDialog;
    	private HashMap<Integer, String> hm_img;

		public AsyncTaskUpLoadImages (HashMap<Integer, String> hm_img){
			this.hm_img = hm_img;
		}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(PostRoom_GetImages.this, getString(R.string.waiting));
			mDialog.show();
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			for (Entry<Integer, String> entry : hm_img.entrySet()){
				int key = entry.getKey();
				String value = entry.getValue();
				File f = new File(value);
				Bitmap b = BitmapFactory.decodeFile(value);
				FileOutputStream fOut;
				Bitmap out = scaleBitmap(b, 500, 300);
				try {
				    fOut = new FileOutputStream(f);
				    out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				    fOut.flush();
				    fOut.close();
				    b.recycle();
				    out.recycle();               
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					JSONObject jObj = cloudinary.uploader().upload(f, Cloudinary.emptyMap());
					value = jObj.optString("url");
					hm_img.put(key, value);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			for (Entry<Integer, String> entry : hm_img.entrySet()){
				int key = entry.getKey();
				String value = entry.getValue();
				list_Room_Images.get(key).setPath(value);
				list_Room_Images.get(key).setUrl(true);
			}
			Config.showCustomToast(PostRoom_GetImages.this, R.mipmap.ic_toast_success, getString(R.string.upload_success) + " " + hm_img.size() + " " + getString(R.string.image));
			if(isEdit){
				for(int i = 0; i< list_Room_Images.size();i++){
					if(list_Room_Images.get(i).getId() == 0 && list_Room_Images.get(i).isUrl()){
						hash_map_insert_image.put(list_Room_Images.get(i).getPath(), list_Room_Images.get(i).getNote());
					}
				}
				RequestAPI.getInstance().context(PostRoom_GetImages.this).isParams(true).isAuthorization(true)
					.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
					.message(getString(R.string.waiting)).method(RequestAPI.PUT)
					.isShowDialog(true).isShowToast(true).execute(updateRoomImagesListener);
			}else {
				intentNext();
			}
		}

    }
    
    private class AsyncTaskRemoveImage extends AsyncTask<String, Void, String> {
    	
    	private MaterialDialog mDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
			mDialog = DialogManager.getInstance().progressDialog(PostRoom_GetImages.this, getString(R.string.waiting));
			mDialog.show();
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			try {
				JSONObject jObj = cloudinary.uploader().destroy(params[0], Cloudinary.emptyMap());
				return jObj.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			Config.showCustomToast(PostRoom_GetImages.this, R.mipmap.ic_toast_success, getString(R.string.delete_success));
		}

	}
    
    private Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (width > height) {
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            height = maxHeight;
            width = maxWidth;
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

	private DialogManager.YesNoDialogListener saveListener = new DialogManager.YesNoDialogListener() {

		@Override
		public void onYes(MaterialDialog dialog) {
			isAutoIntentBack = true;
			update();
			dialog.dismiss();
		}

		@Override
		public void onNo(MaterialDialog dialog) {
			myRooms.getList_room().get(myRooms.getPosition()).getRoom_images().setList_images(list_Room_Images_RollBack);
			intentBackEdit();
			dialog.dismiss();
		}

	};

	private RequestAPI.RequestAPIListener updateRoomImagesListener = new RequestAPI.RequestAPIListener() {

		@Override
		public JSONObject onRequest() throws JSONException {
			if(hash_map_delete_image.size() > 0){
				for(Entry<Integer, String> entry : hash_map_delete_image.entrySet()) {
					String path = entry.getValue();
					String image_name = (path.split("/")[path.split("/").length - 1]);
					String public_id = image_name.split("\\.")[0];
					try {
						cloudinary.uploader().destroy(public_id, Cloudinary.emptyMap());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			JSONObject jObj = new JSONObject();
			JSONArray jDelete = new JSONArray();
			JSONArray jUpDate = new JSONArray();
			JSONArray jInsert = new JSONArray();
			if(hash_map_delete_image.size() > 0){
				for(Entry<Integer, String> entry : hash_map_delete_image.entrySet()) {
					int id = entry.getKey();
					jDelete.put(new JSONObject().put(Config.ID, id));
				}
			}
			if(hash_map_update_image.size() > 0){
				for(Entry<Integer, String> entry : hash_map_update_image.entrySet()) {
					int id = entry.getKey();
					String note = entry.getValue();
					jUpDate.put(new JSONObject().put(Config.ID, id).put(Config.NOTE, note));
				}
			}
			if(hash_map_insert_image.size() > 0){
				for(Entry<String, String> entry : hash_map_insert_image.entrySet()){
					String path = entry.getKey();
					String note = entry.getValue();
					jInsert.put(new JSONObject().put(Config.LINK, path).put(Config.NOTE, note));
				}
			}
			jObj.put("Delete", jDelete);
			jObj.put("Update", jUpDate);
			jObj.put("Insert", jInsert);
			jObj.put(Config.UPDATE_TYPE, Config.UPDATE_ROOM_IMAGES);
			return jObj;
		}

		@Override
		public String onAuthorization() {
			return mAccount.getApi_key();
		}

		@Override
		public void onResult(String contentMessage) throws JSONException {
			JSONArray jArray = new JSONArray(contentMessage);
			list_Room_Images = new ArrayList<>();
			for(int i = 0; i < jArray.length(); i++){
				JSONObject jObj = jArray.optJSONObject(i);
				list_Room_Images.add(new Image(jObj.optInt(Config.ID), jObj.optString(Config.LINK),
					jObj.optString(Config.NOTE), true, true));
			}
			room_images.setList_images(list_Room_Images);
			room.setRoom_images(room_images);
			btn_save.setVisibility(View.GONE);
			if(isAutoIntentBack){
				myRooms.getList_room().set(myRooms.getPosition(), room);
				intentBackEdit();
			} else {
				cloneArrayList(list_Room_Images);
			}
		}

		@Override
		public void onError(Exception e) {}
	};

	private void update(){
		HashMap<Integer, String> hm_image = new HashMap<>();
		for(int i = 0; i < list_Room_Images.size(); i++){
			if(!list_Room_Images.get(i).isUrl() && list_Room_Images.get(i).isCloseButton()){
				hm_image.put(i, list_Room_Images.get(i).getPath());
			}
		}
		if(hm_image.size() > 0){
			new AsyncTaskUpLoadImages(hm_image).execute();
		} else {
			RequestAPI.getInstance().context(PostRoom_GetImages.this).isParams(true).isAuthorization(true)
				.url(Config.URL_UPDATE_ROOM + myRooms.getList_room().get(myRooms.getPosition()).getId())
				.message(getString(R.string.waiting)).method(RequestAPI.PUT).isShowToast(true)
				.isShowDialog(true).execute(updateRoomImagesListener);
		}
	}

	private void intentBack(){
		room_images = new Room_Images(list_Room_Images);
		room.setRoom_images(room_images);
		Intent intent_back = new Intent(PostRoom_GetImages.this, PostRoom_GetProperties.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
		intent_back.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_ROOM, room);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_IMAGES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void intentNext(){
		room_images = new Room_Images(list_Room_Images);
		room.setRoom_images(room_images);
		Intent intent_next = new Intent(PostRoom_GetImages.this, PostRoom_GetAmenities.class);
		intent_next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_next.putExtra(Config.BUNDLE_POSITION_PROVINCE_ID, position_Province_ID);
		intent_next.putExtra(Config.BUNDLE_POSITION_DISTRICT_ID, position_District_ID);
		intent_next.putExtra(Config.BUNDLE_POSITION_WARD_ID, position_Ward_ID);
		intent_next.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_next.putExtra(Config.BUNDLE_ROOM, room);
		intent_next.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_IMAGES);
		startActivity(intent_next);
		overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
	}

	private void intentBackEdit(){
		Intent intent_back = new Intent(PostRoom_GetImages.this, MyRoomDetailActivity.class);
		intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_back.putExtra(Config.BUNDLE_ACCOUNT, mAccount);
		intent_back.putExtra(Config.BUNDLE_MY_ROOMS, myRooms);
		intent_back.putExtra(Config.FROM_ACTIVITY, Config.POST_ROOM_GET_IMAGES);
		startActivity(intent_back);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
