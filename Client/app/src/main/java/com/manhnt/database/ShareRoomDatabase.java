package com.manhnt.database;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.manhnt.object.District;
import com.manhnt.object.Province;
import com.manhnt.object.Ward;
import com.manhnt.shareroom.R;

public class ShareRoomDatabase extends SQLiteOpenHelper {

	private static final String DB_NAME = "my_database";
	private static final int DB_VERSION = 1;
	private Context mContext;
	private static final String PROVINCE_TABLE = "province";
	private static final String PROVINCE_ID = "province_id";
	private static final String PROVINCE_NAME ="name";
	private static final String PROVINCE_TYPE = "type";
	private static final String DISTRICT_TABLE = "district";
	private static final String DISTRICT_ID = "district_id";
	private static final String DISTRICT_NAME = "name";
	private static final String DISTRICT_TYPE = "type";
	private static final String DISTRICT_LOCATION ="location";
	private static final String DISTRICT_PROVINCE_ID = "province_id";
	private static final String WARD_TABLE = "ward";
	private static final String WARD_ID = "ward_id";
	private static final String WARD_NAME = "name";
	private static final String WARD_TYPE = "type";
	private static final String WARD_LOCATION ="location";
	private static final String WARD_DISTRICT_ID = "district_id";
	private static ShareRoomDatabase instance;

	public static synchronized ShareRoomDatabase getInstance(Context context){
		if(instance == null){
			instance = new ShareRoomDatabase(context.getApplicationContext());
		}
		return instance;
	}

	private ShareRoomDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_PROVINCE_TABLE = "CREATE TABLE " + PROVINCE_TABLE + " ( " + PROVINCE_ID
			+ " TEXT," + PROVINCE_NAME + " TEXT," + PROVINCE_TYPE
			+ " TEXT )";
		String CREATE_DISTRICT_TABLE = "CREATE TABLE " + DISTRICT_TABLE +" ( " + DISTRICT_ID
			+ " TEXT," + DISTRICT_NAME + " TEXT," + DISTRICT_TYPE+" TEXT,"+ DISTRICT_LOCATION + " TEXT," + DISTRICT_PROVINCE_ID + " TEXT)";
		String CREATE_WARD_TABLE ="CREATE TABLE " + WARD_TABLE +" ( " + WARD_ID
			+ " TEXT," + WARD_NAME + " TEXT," + WARD_TYPE+" TEXT,"+ WARD_LOCATION
			+ " TEXT," + WARD_DISTRICT_ID + " TEXT)";
		db.execSQL(CREATE_PROVINCE_TABLE);
		db.execSQL(CREATE_DISTRICT_TABLE);
		db.execSQL(CREATE_WARD_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS" + PROVINCE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS" + DISTRICT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS" + WARD_TABLE);
		this.onCreate(db);
	}
	
	public ArrayList<Province> getAllProvince(){
		ArrayList<Province> list_province = new ArrayList<>();
		String query = "SELECT * FROM " + PROVINCE_TABLE;
		SQLiteDatabase db = this.getWritableDatabase();
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {
				list_province.add(new Province(cursor.getString(0),cursor.getString(1),cursor.getString(2)));
			} while (cursor.moveToNext());
		}
		db.close();
		return list_province;
		
	}
	
	public ArrayList<District> getListDistrict(String province_id){
		ArrayList<District> list_district = new ArrayList<>();
		list_district.add(new District("", mContext.getResources().getString(R.string.district), "", "", ""));
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT * FROM " +DISTRICT_TABLE + " WHERE "+ DISTRICT_PROVINCE_ID + " = " +"?;";
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{province_id});
		if (cursor.moveToFirst()) {
			do {
				list_district.add(new District(cursor.getString(0), cursor.getString(1), cursor.getString(2)
						,cursor.getString(3), cursor.getString(4)));
			}while(cursor.moveToNext());
		}
		db.close();
		return list_district;
	}
	
	public ArrayList<Ward> getListWard(String district_id){
		ArrayList<Ward> list_ward = new ArrayList<>();
		list_ward.add(new Ward("", mContext.getResources().getString(R.string.ward), "", "", ""));
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT * FROM " + WARD_TABLE + " WHERE "+ WARD_DISTRICT_ID + " = " +"?;";
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{district_id});
		if (cursor.moveToFirst()) {
			do {
				list_ward.add(new Ward(cursor.getString(0), cursor.getString(1), cursor.getString(2)
						,cursor.getString(3), cursor.getString(4)));
			}while(cursor.moveToNext());
		}
		db.close();
		return list_ward;
	}

	public String getProvinceName_FromProvinceID(String district_id){
		String province_name ="";
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT "+ PROVINCE_NAME +" FROM " + PROVINCE_TABLE + " WHERE "+ PROVINCE_ID + " = " +"?;";
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{district_id});
		if(cursor.moveToFirst()){
			do{
				province_name = cursor.getString(0);
			}while(cursor.moveToNext());
		}
		db.close();
		return province_name;
	}

	public String getDistrictName_FromDistrictID(String district_id){
		String district_name ="";
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT "+ DISTRICT_NAME +" FROM " + DISTRICT_TABLE + " WHERE "+ DISTRICT_ID + " = " +"?;";
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{district_id});
		if(cursor.moveToFirst()){
			do{
				district_name = cursor.getString(0);
			}while(cursor.moveToNext());
		}
		db.close();
		return district_name;
	}

	public String getWardName_FromWardID(String ward_id){
		String ward_name ="";
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT "+ WARD_NAME +" FROM " + WARD_TABLE + " WHERE "+ WARD_ID + " = " +"?;";
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{ward_id});
		if(cursor.moveToFirst()){
			do{
				ward_name = cursor.getString(0);
			}while(cursor.moveToNext());
		}
		db.close();
		return ward_name;
	}

}
