package ru.opendevelopers.smsreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {


    SharedPreferences sPref ;
    static final String KEY_OF_SMS_READER = "key_of_message";
    static final String IV_OF_SMS_READER = "iv_of_message";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sPref = getSharedPreferences("myPrefs",MODE_PRIVATE);
        String savedText = sPref.getString(KEY_OF_SMS_READER, "");
        String savedTextIv = sPref.getString(IV_OF_SMS_READER, "");

        if (savedText.length()==0 || savedTextIv.length()==0)
        {
            View button2 = findViewById(R.id.button2);
            button2.setVisibility(View.GONE);
        }


        TextView etText = (TextView) findViewById(R.id.textView);
        etText.setText(savedText);

//
//        TextView etTextIv = (TextView) findViewById(R.id.textView2);
//        etTextIv.setText(savedTextIv);
    }




    public void onClickOpenTelegramLink(View v){

        String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Uri uri = Uri.parse("https://telegram.me/SMSReaderBot?start="+Base64.encodeToString(android_id.getBytes(),Base64.DEFAULT));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

    public void onClickTestEncrypt(View v) throws GeneralSecurityException, UnsupportedEncodingException {


        String text = "Зашифрованное сообщение";
        String from = "+79998393459";

        String codeFromPref = sPref.getString(KEY_OF_SMS_READER, "");
        String iv = sPref.getString(IV_OF_SMS_READER, "");

        String code = codeFromPref+codeFromPref+codeFromPref+"z";


        SecretKeySpec secretKeySpec = new SecretKeySpec(code.getBytes(), "AES");

        byte[] str = encrypt(secretKeySpec, text.getBytes("UTF-8"), iv.getBytes());
        String resultString = Base64.encodeToString(str, Base64.DEFAULT);
        Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_SHORT).show();

        CallAPI mt = new CallAPI();
        String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mt.execute("set-message/"+android_id,"from="+
                URLEncoder.encode(from,"UTF-8")+"&message="+
                URLEncoder.encode(resultString,"UTF-8"));

        String result = null;
        try {
            result = mt.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();


    }


    public void onClickSendCrypt(View v) throws ExecutionException, InterruptedException, JSONException {
//        CallAPI mt = new CallAPI();
////        String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
////                Settings.Secure.ANDROID_ID);
////        mt.execute("decrypt/ccdc84ca5d167831/123","T4LhX1QBzjy52zl/uKH+rcfAcUaYpt94Aw==");
//        mt.execute("decrypt/ccdc84ca5d167831/123/TqPgZ1U+zw+538kX38huXJSRFC3yyY4bUYY=","");
//        String result = mt.get();
//
//



//        Toast.makeText(MainActivity.this, String.valueOf(i1), Toast.LENGTH_SHORT).show();
    }



    public String registration()
    {
        String result = null;
        CallAPI mt = new CallAPI();
        String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mt.execute("reg/"+android_id,"");
        try {
            result = mt.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void onClickNewPass(View v) throws ExecutionException, InterruptedException, JSONException {

        String result = registration();
        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        JSONObject json = new JSONObject(result);
        String guid = json.getString("guid");
        String id = json.getString("id");
        String iv = json.getString("iv");

        Random r = new Random();
        int i1 = r.nextInt(100000 - 9999) + 1000;

        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(KEY_OF_SMS_READER, String.valueOf(i1).toString());
        ed.putString(IV_OF_SMS_READER, iv);
        ed.commit();


        View button2 = findViewById(R.id.button2);
        button2.setVisibility(View.VISIBLE);

        TextView etText = (TextView) findViewById(R.id.textView);
        etText.setText(String.valueOf(i1));

    }

    public static byte[] encrypt(SecretKey secret, byte[] buffer,  byte[] ivData) throws GeneralSecurityException
    {
    /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(ivData));
        byte[] ciphertext = cipher.doFinal(buffer);

        return ciphertext;
    }
}
