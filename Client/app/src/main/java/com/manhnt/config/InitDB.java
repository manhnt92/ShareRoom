package com.manhnt.config;

import android.content.Context;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.manhnt.database.ShareRoomDatabase;
import com.manhnt.object.Province;
import com.manhnt.shareroom.R;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class InitDB extends AsyncTask<Void, Void, Void>{

    private Context mContext;
    private ShareRoomDatabase db;
    private ArrayList<Province> ListProvince;
    private String[] ListProvinceName;
    private MaterialDialog mDialog;

    public InitDB(Context context, ShareRoomDatabase db, ArrayList<Province> ListProvince, String[] ListProvinceName){
        this.mContext = context;
        this.db = db;
        this.ListProvince = ListProvince;
        this.ListProvinceName = ListProvinceName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = DialogManager.getInstance().progressDialog(mContext, mContext.getResources().getString(R.string.waiting));
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        init("province.txt");init("district.txt");init("district1.txt");
        init("ward.txt");init("ward1.txt");init("ward2.txt");init("ward3.txt");
        init("ward4.txt");init("ward5.txt");init("ward6.txt");init("ward7.txt");
        init("ward8.txt");init("ward9.txt");init("ward10.txt");init("ward11.txt");
        init("ward12.txt");init("ward13.txt");init("ward14.txt");init("ward15.txt");
        init("ward16.txt");init("ward17.txt");init("ward18.txt");init("ward19.txt");
        init("ward20.txt");init("ward21.txt");init("ward22.txt");
        db.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        ListProvince = db.getAllProvince();
        ListProvinceName = new String[ListProvince.size()];
        for(int i = 0; i < ListProvince.size(); i++){
            ListProvinceName[i] = ListProvince.get(i).getName();
        }
        mDialog.dismiss();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void init(String filename){
        try {
            InputStream input = mContext.getAssets().open("db/" + filename);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            String query = new String(buffer);
            db.getWritableDatabase().execSQL(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
