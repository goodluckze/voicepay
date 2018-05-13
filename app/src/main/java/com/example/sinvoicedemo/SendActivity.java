package com.example.sinvoicedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fangwuze.sinvioce_copy.R;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;
import com.libra.sinvoice.SinVoiceRecognition;

/**
 * Created by fangwuze on 2018/4/15.
 */

public class SendActivity extends Activity implements SinVoicePlayer.Listener{
    private final static String TAG = "MainActivity";
    private final static int MAX_NUMBER = 5;
    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;

    //fwz
    private final static String CODEBOOK = "12345";

    private EditText mSoundText = null;

    private SinVoicePlayer mSinVoicePlayer;

    private Context mContext = null;
    private final static int CODE_LEN = 5;

    private boolean mPlayFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snd);

        mSinVoicePlayer = new SinVoicePlayer(CODEBOOK);
        mSinVoicePlayer.setListener(this);

        mSoundText = (EditText)findViewById(R.id.soundtext);

        mContext = this;


        final TextView playTextView = (TextView) findViewById(R.id.playtext);
        mSoundText.setText(getKeyId(getIntent().getStringExtra("key")));
        Button playStart = (Button) this.findViewById(R.id.start_play);
        playStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = mSoundText.getText().toString().trim();
                if(text.equals("")){
                    text = genText(CODE_LEN);
                }else
                {
                    if(!checkLegal(text)){
                        Toast.makeText(getApplicationContext(),"输入编码id不合法",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                playTextView.setText(text);
                mSinVoicePlayer.play(text, true, 1000);
                mPlayFlag = true;
            }
        });

        Button playStop = (Button) this.findViewById(R.id.stop_play);
        playStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSinVoicePlayer.stop();
                mPlayFlag = false;
            }
        });


        Button btn_set_mallKey = (Button)findViewById(R.id.btn_set_mall_key);
        Button btn_set_corKey = (Button)findViewById(R.id.btn_set_cor_key);
        Button btn_set_eggKey = (Button)findViewById(R.id.btn_set_egg_key);

        BtnOnclickListener listener = new BtnOnclickListener();
        btn_set_mallKey.setOnClickListener(listener);
        btn_set_corKey.setOnClickListener(listener);
        btn_set_eggKey.setOnClickListener(listener);


    }

    @Override
    protected void onStop() {
        if(mPlayFlag)
            mSinVoicePlayer.stop();
        super.onStop();

    }

    private String getKeyId(String key){
        if(key == null || key.equals("")){
            return "";
        }
        String defaultKeyId = null;
        if (key.equals("mall")){
            defaultKeyId = CommonTools.MALL_KEY;
        }
        if (key.equals("cor")){
            defaultKeyId = CommonTools.COR_KEY;
        }
        if (key.equals("egg")){
            defaultKeyId = CommonTools.EGG_KEY;
        }

        return getSharedPreferences(CommonTools.SharedPreferencesFileName, MODE_PRIVATE).
                getString(key,defaultKeyId);
    }

    private boolean checkLegal(String text){

        if(text.length() <= CODE_LEN){
            char[]  chars = text.toCharArray();
            for (int i = 0; i< chars.length; i++){
                if(chars[i] < '0' || chars[i] > '9'){
                    return false;
                }else {
                    if(i > 0 && chars[i] == chars[i-1]){
                        return false;
                    }
                }
            }

            return true;
        }


        return false;
    }
    private String genText(int count) {
        StringBuilder sb = new StringBuilder();
        int pre = 0;
        while (count > 0) {
            int x = (int) (Math.random() * MAX_NUMBER + 1);
            if (Math.abs(x - pre) > 0) {
                sb.append(x);
                --count;
                pre = x;
            }
        }

        return sb.toString();
    }



    @Override
    public void onPlayStart() {
        LogHelper.d(TAG, "start play");
    }

    @Override
    public void onPlayEnd() {
        LogHelper.d(TAG, "stop play");
    }


    class BtnOnclickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String key = mSoundText.getText().toString().trim();
            for (int i=0;i<key.length();i++){
                if( key.charAt(i) < '1' || key.charAt(i) >'5'){
                    Toast.makeText(mContext,"keyID仅支持1-5数字:" + key,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            switch (v.getId()){
                case R.id.btn_set_mall_key:
                    if( ! key.equals("") ){
                        getSharedPreferences(
                                CommonTools.SharedPreferencesFileName, MODE_PRIVATE).edit().
                                putString("mall",key).commit();
                        Toast.makeText(mContext,"设置商场场景keyID:" + key,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_set_cor_key:
                    if( ! key.equals("") ){
                        getSharedPreferences(
                                CommonTools.SharedPreferencesFileName, MODE_PRIVATE).edit().
                                putString("cor",key).commit();
                        Toast.makeText(mContext,"设置企业场景keyID:" + key,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_set_egg_key:
                    if( ! key.equals("") ){
                        getSharedPreferences(
                                CommonTools.SharedPreferencesFileName, MODE_PRIVATE).edit().
                                putString("egg",key).commit();
                        Toast.makeText(mContext,"设置彩蛋场景keyID:" + key,Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}
