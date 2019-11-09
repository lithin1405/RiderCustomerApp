package com.cands.delini.asynctask;

/**
 * Created by Swamy on 3/4/2016.
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;


import com.cands.delini.listener.AsyncTaskCompleteListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


/**
 * Created by Yakaswamy.g on 2/22/2016.
 */

public class ImageAsyncTask extends AsyncTask<String, Void, String> {

    private String jsonData;
    private AsyncTaskCompleteListener asyncTaskCompleteListener;
    private int connectionTimeout;
    private int socketTimeout;
    private Bitmap bitmap;

    public ImageAsyncTask(Bitmap bitmap, AsyncTaskCompleteListener asyncTaskCompleteListener, int connectionTimeout, int socketTimeout) {
        this.bitmap = bitmap;
        this.asyncTaskCompleteListener = asyncTaskCompleteListener;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL myurl=new URL(params[0]);
            httpURLConnection = (HttpURLConnection) myurl.openConnection();

            //setting properties to httpURLConnection
            httpURLConnection.setConnectTimeout(connectionTimeout);
            httpURLConnection.setReadTimeout(socketTimeout);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            //httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpURLConnection.connect();

            /*OutputStream os = httpURLConnection.getOutputStream();
            os.write(jsonData.getBytes("UTF-8"));
            os.close();*/


            OutputStream output = httpURLConnection.getOutputStream();
            bitmap.compress(CompressFormat.JPEG, 50, output);
            output.close();

            Scanner result = new Scanner(httpURLConnection.getInputStream());
            String response = result.nextLine();
            Log.e("ImageUploader", "Error uploading image: " + response);

            result.close();

            //setting request data to httpURLConnection
            /*OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(jsonData);
            writer.flush();
            writer.close();*/


            /*Log.i("AsyncTask", "url :" + params[0]);
            Log.i("AsyncTask", "request data :" + jsonData);

            InputStream inputStream = httpURLConnection.getInputStream();
            String responseStr = Utility.convertStreamToString(inputStream);*/
            return response;
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        asyncTaskCompleteListener.onAsynComplete(result);
    }
}


