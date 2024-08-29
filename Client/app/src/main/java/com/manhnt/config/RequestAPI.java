package com.manhnt.config;

import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import com.manhnt.object.Result;
import com.manhnt.shareroom.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestAPI {

    private static RequestAPI instance;
    private MaterialDialog mDialog;
    private Context context;
    private String message;
    private String method;
    private String url;
    private boolean isAuthorization;
    private boolean isParams;
    private boolean isShowToast;
    private boolean isShowDialog;
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    private RequestAPI(){}

    public static synchronized RequestAPI getInstance(){
        if(instance == null){
            instance = new RequestAPI();
        }
        return instance;
    }

    public static synchronized RequestAPI newInstance(){
        return new RequestAPI();
    }

    public interface RequestAPIListener {
        JSONObject onRequest() throws JSONException;
        String onAuthorization();
        void onResult(String contentMessage) throws JSONException;
        void onError(Exception e);
    }

    private RequestAPIListener requestListener;

    public void execute(RequestAPIListener requestListener){
        this.requestListener = requestListener;
        new APIAsyncTask(this).execute();
    }

    public void onPreExecute(){
        if(isShowDialog) {
            mDialog = DialogManager.getInstance().progressDialog(context, message);
            mDialog.show();
        }
    }

    public Result doInBackground() throws JSONException {
        JSONObject params = requestListener.onRequest();
        String authorization = requestListener.onAuthorization();
        return getResult(url, method, isParams, params, isAuthorization, authorization);
    }

    public void onPostExecute(Result result) throws JSONException {
        if(isShowDialog) {
            mDialog.dismiss();
        }
        if(result != null) {
            boolean isSuccess = result.isSuccess();
            String message = result.getMessage();
            if (isSuccess) {
                if(isShowToast && message.length() > 0) {
                    Config.showCustomToast(context, R.mipmap.ic_toast_success, message);
                }
                requestListener.onResult(result.getContentMessage());
            } else {
                if(isShowToast && message.length() > 0) {
                    Config.showCustomToast(context, R.mipmap.ic_toast_error, message);
                }
            }
        }
    }

    public void onError(Exception e){
        requestListener.onError(e);
    }

    private Result getResult(String url, String method, boolean isParams, JSONObject params, boolean isAuthorization, String authorization){
        try{
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            if(isAuthorization){
                conn.setRequestProperty("Authorization", authorization);
            }
            conn.connect();
            if(isParams){
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                bw.write(params.toString());
                bw.flush();
                bw.close();
            }
            int statusCode = conn.getResponseCode();
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            JSONObject jRs = new JSONObject(Config.convertInputStreamToString(inputStream));
            return new Result(statusCode, jRs.optBoolean(Config.SUCCESS), jRs.optString(Config.MESSAGE),
                jRs.optString(Config.CONTENT_MESSAGE));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RequestAPI context(Context context){
        this.context = context;
        return this;
    }

    public RequestAPI message(String message){
        this.message = message;
        return this;
    }

    public RequestAPI method(String method){
        this.method = method;
        return this;
    }

    public RequestAPI url(String url){
        this.url = url;
        return this;
    }

    public RequestAPI isParams(boolean isParams){
        this.isParams = isParams;
        return this;
    }

    public RequestAPI isAuthorization(boolean isAuthorization){
        this.isAuthorization = isAuthorization;
        return this;
    }

    public RequestAPI isShowToast(boolean isShowToast){
        this.isShowToast = isShowToast;
        return this;
    }

    public RequestAPI isShowDialog(boolean isShowDialog){
        this.isShowDialog = isShowDialog;
        return this;
    }

}
