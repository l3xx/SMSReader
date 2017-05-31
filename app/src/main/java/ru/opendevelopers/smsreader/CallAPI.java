package ru.opendevelopers.smsreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by letunovskiymn on 24.05.17.
 */

public class CallAPI extends AsyncTask<String, String, String> {



    protected String resultString = null;
    public CallAPI(){
        //set context variables if required

    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String myURL = "https://api.opendevelopers.ru/sms-reader/"+params[0];
            String parammetrs = params[1];
            Log.d("send_data",parammetrs);
            byte[] data = null;
            InputStream is = null;
            Log.d("send_data",myURL);
            try {
                URL url = new URL(myURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                OutputStream os = conn.getOutputStream();
                data = parammetrs.getBytes("UTF-8");
                os.write(data);
                data = null;

                conn.connect();
                int responseCode= conn.getResponseCode();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Log.d("send_data",String.valueOf(responseCode));
                if (responseCode == 200) {
                    is = conn.getInputStream();

                    byte[] buffer = new byte[1024]; // Такого вот размера буфер
                    // Далее, например, вот так читаем ответ
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    data = baos.toByteArray();
                    resultString = new String(data, "UTF-8");
                    Log.d("send_data",resultString);
                } else {

                }



            } catch (MalformedURLException e) {
                Log.d("send_data",e.getMessage());
                //resultString = "MalformedURLException:" + e.getMessage();
            } catch (IOException e) {
                Log.d("send_data",e.getMessage());
                //resultString = "IOException:" + e.getMessage();
            } catch (Exception e) {
                Log.d("send_data",e.getMessage());
                //resultString = "Exception:" + e.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


}
