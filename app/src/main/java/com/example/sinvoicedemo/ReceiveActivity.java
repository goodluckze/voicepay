package com.example.sinvoicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fangwuze.sinvioce_copy.R;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;
import com.libra.sinvoice.SinVoiceRecognition;

import org.w3c.dom.Text;

public class ReceiveActivity extends Activity implements SinVoiceRecognition.Listener {
    private final static String TAG = "MainActivity";
    private final static int MAX_NUMBER = 5;
    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;

    //fwz
    private final static String CODEBOOK = "12345";
//    private final static String CODEBOOK = "1234567890";

    private Handler mHanlder;
    private SinVoiceRecognition mRecognition;
    private TextView mRecognisedTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recevie);

        mRecognition = new SinVoiceRecognition(CODEBOOK);
        mRecognition.setListener(this);

        mRecognisedTextView = (TextView) findViewById(R.id.regtext);
        mHanlder = new RegHandler(mRecognisedTextView);



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
                finish();
            }
        });
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
                mRecognisedTextView.setText("");
                mTextBuilder.delete(0, mTextBuilder.length());
//                Toast.makeText(ReceiveActivity.this,"开始抢红包",Toast.LENGTH_SHORT).show();
                break;

            case MSG_RECG_END:
                LogHelper.d(TAG, "recognition end");
                break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onRecognitionStart() {
//        Toast.makeText(getApplicationContext(),"开始抢红包",Toast.LENGTH_SHORT).show();

        mHanlder.sendEmptyMessage(MSG_RECG_START);
    }

    @Override
    public void onRecognition(char ch) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onRecognitionEnd() {
        mHanlder.sendEmptyMessage(MSG_RECG_END);
        mRecognition.stop();
//        Toast.makeText(getApplicationContext(),"识别结束！",Toast.LENGTH_SHORT).show();
    }


}
