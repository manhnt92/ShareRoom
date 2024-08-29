package com.manhnt.config;

import android.os.AsyncTask;

import com.manhnt.object.Result;

import org.json.JSONException;

public class APIAsyncTask extends AsyncTask<Void, Void, Result>{

    private RequestAPI requestManager;

    public APIAsyncTask(RequestAPI requestManager){
        this.requestManager = requestManager;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        requestManager.onPreExecute();
    }

    @Override
    protected Result doInBackground(Void... voids) {
        try {
            return requestManager.doInBackground();
        } catch (JSONException e) {
            requestManager.onError(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        try {
            requestManager.onPostExecute(result);
        } catch (JSONException e) {
            requestManager.onError(e);
        }
    }
}
