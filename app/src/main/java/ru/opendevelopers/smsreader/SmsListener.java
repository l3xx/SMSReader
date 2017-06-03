package ru.opendevelopers.smsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.provider.Settings.Secure;

/**
 * Created by letunovskiymn on 30.05.17.
 */

public class SmsListener extends BroadcastReceiver {
    private SharedPreferences sPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        sPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);


        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        sendMessageToserver(msg_from,msgBody, context);
                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }

    public String sendMessageToserver (String from, String text, Context Context) throws UnsupportedEncodingException, GeneralSecurityException {

        String codeFromPref = sPref.getString(MainActivity.KEY_OF_SMS_READER, "");
        String iv = sPref.getString(MainActivity.IV_OF_SMS_READER, "");

        String code = codeFromPref+codeFromPref+codeFromPref+"z";
        SecretKeySpec secretKeySpec = new SecretKeySpec(code.getBytes(), "AES");

        byte[] str = encrypt(secretKeySpec, text.getBytes("UTF-8"), iv.getBytes());
        String resultString = Base64.encodeToString(str, Base64.DEFAULT);

        CallAPI mt = new CallAPI();
        String android_id = Settings.Secure.getString(Context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mt.execute("set-message/"+android_id,"from="+
                URLEncoder.encode(from,"UTF-8")+"&message="+
                URLEncoder.encode(resultString,"UTF-8")+
                "&hash="+URLEncoder.encode(md5(text),"UTF-8"));

        String result = null;
        try {
            result = mt.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] encrypt(SecretKey secret, byte[] buffer, byte[] ivData) throws GeneralSecurityException {
    /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(ivData));
        byte[] ciphertext = cipher.doFinal(buffer);
        return ciphertext;
    }

    public final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
