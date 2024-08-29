package com.manhnt.shareroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.manhnt.config.Config;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class PosterActivity extends Activity implements  View.OnClickListener {

    private Account account_post;
    private ImageLoader mImageLoader;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poster_activity);
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
    }

    private void getExtraBundle(){
        if(getIntent().getExtras() != null){
            int from = getIntent().getExtras().getInt(Config.FROM_ACTIVITY);
            switch (from){
                case Config.SEARCH_ROOM_VIEW_DETAIL:
                    account_post = Config.ACCOUNT_POST;
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getWidget() {
        WidgetManager manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.title, true);
        manager.ImageButton(R.id.btn_back, this, true);
        ImageView img_avatar = manager.ImageView(R.id.img_avatar, this, true);
        TextView txt_name = manager.TextView(R.id.txt_name, true);
        TextView txt_email = manager.TextView(R.id.txt_email_content, true);
        manager.TextView(R.id.txt_age, true);
        manager.TextView(R.id.txt_gender, true);
        manager.TextView(R.id.txt_birthday, true);
        manager.TextView(R.id.txt_phonenumber, true);
        manager.TextView(R.id.txt_occupation, true);
        manager.TextView(R.id.txt_address, true);
        manager.TextView(R.id.txt_description, true);
        TextView txt_age_content = manager.TextView(R.id.txt_age_content, true);
        TextView txt_gender_content = manager.TextView(R.id.txt_gender_content, true);
        TextView txt_birthday_content = manager.TextView(R.id.txt_birthday_content, true);
        TextView txt_phonenumber_content = manager.TextView(R.id.txt_phonenumber_content, true);
        TextView txt_occupation_content = manager.TextView(R.id.txt_occupation_content, true);
        TextView txt_address_content = manager.TextView(R.id.txt_address_content, true);
        TextView txt_description_content = manager.TextView(R.id.txt_description_content, true);
        if (account_post != null) {
            if (!TextUtils.isEmpty(account_post.getAvatar()) && !account_post.getAvatar().equalsIgnoreCase("null")) {
                mImageLoader.displayImage(account_post.getAvatar(), img_avatar, options);
            } else {
                boolean gender = !account_post.getGender().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getGender());
                int resID = gender ? (account_post.getGender().equalsIgnoreCase(getString(R.string.female)) ?
                        R.drawable.ic_user_female_press : R.drawable.ic_user_male_press) : R.drawable.ic_user_male_press;
                img_avatar.setImageResource(resID);
            }
            txt_name.setText(account_post.getFirst_name() + " " + account_post.getLast_name());
            txt_email.setText(account_post.getEmail());
            String no_content = getString(R.string.no_content);
            boolean age = account_post.getAge() > 0;
            txt_age_content.setText(age ? "" + account_post.getAge() : no_content);
            boolean gender = !account_post.getGender().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getGender());
            txt_gender_content.setText(gender ? account_post.getGender() : no_content);
            boolean birthday = !account_post.getBirthday().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getBirthday());
            txt_birthday_content.setText(birthday ? account_post.getBirthday() : no_content);
            boolean phoneNum = !account_post.getPhoneNumber().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getPhoneNumber());
            txt_phonenumber_content.setText(phoneNum ? account_post.getPhoneNumber() : no_content);
            boolean occupation = !account_post.getOccupation().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getOccupation());
            txt_occupation_content.setText(occupation ? account_post.getOccupation() : no_content);
            boolean address = !account_post.getAddress().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getAddress());
            txt_address_content.setText(address ? account_post.getAddress() : no_content);
            boolean description = !account_post.getDescription().equalsIgnoreCase("null") && !TextUtils.isEmpty(account_post.getDescription());
            txt_description_content.setText(description ? account_post.getDescription() : no_content);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}
