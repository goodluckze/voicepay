package com.example.sinvoicedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.fangwuze.sinvioce_copy.R;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;
import com.libra.sinvoice.SinVoiceRecognition;

public class MainActivity extends Activity implements SinVoiceRecognition.Listener, SinVoicePlayer.Listener {
    private final static String TAG = "MainActivity";
    private final static int MAX_NUMBER = 5;
    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;

    private final static String CODEBOOK = "12345";

    private Handler mHanlder;
    private SinVoicePlayer mSinVoicePlayer;
    private SinVoiceRecognition mRecognition;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button sndButton =(Button)this.findViewById(R.id.snd);

        Button sndMallButton =(Button)this.findViewById(R.id.snd_mall);
        Button sndCorButton =(Button)this.findViewById(R.id.snd_cor);
        Button sndEggButton =(Button)this.findViewById(R.id.snd_egg);


        Button regButton = (Button)this.findViewById(R.id.reg);
        Button sndRedBagButton =(Button)this.findViewById(R.id.snd_redbag);
        sndButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                startActivity(intent);
//                AudioTrack mAudio = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, format, bufferSize, AudioTrack.MODE_STATIC);
//
//                int len = mAudio.write(data.mData, 0, data.getFilledSize());
//
////                if (0 == mPlayedLen) {
//                    mAudio.play();
////                }
            }
        });

        sndMallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                intent.putExtra("key","mall");
                startActivity(intent);
            }
        });

        sndCorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                intent.putExtra("key","cor");
                startActivity(intent);
            }
        });

        sndEggButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                intent.putExtra("key","egg");
                startActivity(intent);
            }
        });


        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReceiveWebViewActivity.class);
                startActivity(intent);
            }
        });


        sndRedBagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendRedBagActivity.class);
                startActivity(intent);
            }
        });


        //
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,ReceiveWebViewActivity.class);
                startActivity(intent);
            }

            }, 500);

/*
        mSinVoicePlayer = new SinVoicePlayer(CODEBOOK);
        mSinVoicePlayer.setListener(this);

        mRecognition = new SinVoiceRecognition(CODEBOOK);
        mRecognition.setListener(this);

        final TextView playTextView = (TextView) findViewById(R.id.playtext);
        TextView recognisedTextView = (TextView) findViewById(R.id.regtext);
        mHanlder = new RegHandler(recognisedTextView);

        Button playStart = (Button) this.findViewById(R.id.start_play);
        playStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = genText(1);
                playTextView.setText(text);
                mSinVoicePlayer.play(text, true, 1000);
            }
        });

        Button playStop = (Button) this.findViewById(R.id.stop_play);
        playStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSinVoicePlayer.stop();
            }
        });

        Button recognitionStart = (Button) this.findViewById(R.id.start_reg);
        recognitionStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.start();
            }
        });

        Button recognitionStop = (Button) this.findViewById(R.id.stop_reg);
        recognitionStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.stop();
            }
        });

        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,ReceiveWebViewActivity.class);
                startActivity(intent);
            }

        }, 500);
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

    private static class RegHandler extends Handler {
        private StringBuilder mTextBuilder = new StringBuilder();
        private TextView mRecognisedTextView;

        public RegHandler(TextView textView) {
            mRecognisedTextView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
//            System.out.print(msg.what);
            switch (msg.what) {
            case MSG_SET_RECG_TEXT:
                char ch = (char) msg.arg1;
                mTextBuilder.append(ch);
                if (null != mRecognisedTextView) {
                    mRecognisedTextView.setText(mTextBuilder.toString());
                }
                break;

            case MSG_RECG_START:
                mTextBuilder.delete(0, mTextBuilder.length());
                break;

            case MSG_RECG_END:
                LogHelper.d(TAG, "recognition end");
                break;
            case 999:
                Intent intent = new Intent(mContext, SendActivity.class);
                mContext.startActivity(intent);
                break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onRecognitionStart() {
        mHanlder.sendEmptyMessage(MSG_RECG_START);
    }

    @Override
    public void onRecognition(char ch) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onRecognitionEnd() {
        mHanlder.sendEmptyMessage(MSG_RECG_END);
    }

    @Override
    public void onPlayStart() {
        LogHelper.d(TAG, "start play");
    }

    @Override
    public void onPlayEnd() {
        LogHelper.d(TAG, "stop play");
    }

}
