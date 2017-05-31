package ru.opendevelopers.smsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by letunovskiymn on 30.05.17.
 */

public class SmsListener extends BroadcastReceiver {
    private SharedPreferences sPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        sPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String savedText = sPref.getString(MainActivity.KEY_OF_SMS_READER, "");
        String savedTextIv = sPref.getString(MainActivity.IV_OF_SMS_READER, "");



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
                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }
}
