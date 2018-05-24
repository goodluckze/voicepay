package com.example.sinvoicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fangwuze.sinvioce_copy.R;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoiceRecognition;

public class ReceiveWebViewActivity extends Activity implements SinVoiceRecognition.Listener {
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

    private WebView mWebView;

    private static String url_recevice = "http://52.80.155.176:8888/cmbmain.aspx";
    private String test_url = "file:///android_asset/js.html";

    private static String rec_key = "";

    private static Context mContext = null;

    private boolean mRecFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_webview);

//        mRecognition = new SinVoiceRecognition(CODEBOOK);
//        mRecognition.setListener(this);

        mContext = this;

        mWebView = (WebView)findViewById(R.id.rec_webview);

        WebSettings webSettings = mWebView.getSettings();

        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(true);
        // name 与js代码里的window.[name]对应
        mWebView.addJavascriptInterface(new JS(), "android");

        mWebView.loadUrl(url_recevice);
//        mWebView.loadUrl(test_url);

        mHanlder = new RegHandler(mWebView);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                LogHelper.d("mylog",url);
                return true;
//                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogHelper.d("mylog","url:" + url);
                LogHelper.d("mylog","webview:"+view.getUrl());
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(ReceiveWebViewActivity.this);
                b.setTitle("听一听");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }



        });
        //
        mWebView.getSettings().setDomStorageEnabled(true);


//        Button recognitionStart = (Button) this.findViewById(R.id.start_reg);
//        recognitionStart.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                mRecognition.start();
//            }
//        });

//        Button recognitionStop = (Button) this.findViewById(R.id.stop_reg);
//        recognitionStop.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                mRecognition.stop();
//                finish();
//            }
//        });
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public void finish() {
        mRecognition.stop();
        super.finish();

    }

    class JS {
        @JavascriptInterface
        public void startRec() {
            // 处理从js调用过来的方法
            Log.d("mylog","从js调用过来的，开始识别");
//            if(mRecFlag){
//                Log.d("mylog","识别进程已开启，不会重新开启进程");
//                return;
//            }
            mRecognition = new SinVoiceRecognition(CODEBOOK);
            mRecognition.setListener((ReceiveWebViewActivity)mContext);
            mHanlder.sendEmptyMessage(99);
            mRecognition.start();
            mRecFlag = true;

        }
        @JavascriptInterface
        public void stopRec(){
            Log.d("mylog","从js调用过来的，停止识别");
            mRecognition.stop();
            mHanlder.sendEmptyMessage(100);
            mRecFlag = false;
        }
        @JavascriptInterface
        public void back(){
            Log.d("mylog","从js调用过来的，删掉activity");
            mHanlder.sendEmptyMessage(101);
        }

    }

    private static class RegHandler extends Handler {
        private StringBuilder mTextBuilder = new StringBuilder();
        //fwz
//        private TextView mRecognisedTextView;
        private WebView mWebView;

        public RegHandler(WebView webView) {

            mWebView = webView;
            rec_key = new String();
        }

        @Override
        public void handleMessage(Message msg) {
//            System.out.print(msg.what);
            switch (msg.what) {
            case MSG_SET_RECG_TEXT:
                char ch = (char) msg.arg1;
                mTextBuilder.append(ch);
//fwz
//                if (null != mRecognisedTextView) {
//                    mRecognisedTextView.setText(mTextBuilder.toString());
//                }
                rec_key = mTextBuilder.toString();
                Log.d("mylog","keystirng:" +rec_key);
                if(rec_key.length() == 5){
//                    Toast.makeText(mContext,"识别到事件号:"+rec_key+",即将跳转...",Toast.LENGTH_SHORT).show();
                    LogHelper.d("mylog", "识别成功，即将跳转");
                    LogHelper.d("mylog", "当前url："+mWebView.getUrl());
//                    if (mWebView.getUrl() .equals("http://52.80.155.176:8888/VoiceReciveMain.aspx")){
//                        mWebView.loadUrl("http://52.80.155.176:8888/VoiceReciveMain.aspx");
//                    }

                    mWebView.loadUrl("javascript:ReceiveKey("+rec_key+")");
//                    mWebView.loadUrl("http://52.80.155.176:8888/VoiceReciveMain.aspx?key="+rec_key+"");
                }
                if(rec_key.length() > 5){
//                    Toast.makeText(mContext,"识别到事件号:"+rec_key+",即将跳转...",Toast.LENGTH_SHORT).show();
                    LogHelper.d("mylog", "识别太多了，清空");
                    rec_key = "";
                    mTextBuilder.delete(0, mTextBuilder.length());

                }
                break;

            case MSG_RECG_START:
                rec_key = "";
                mTextBuilder.delete(0, mTextBuilder.length());
//                Toast.makeText(ReceiveActivity.this,"开始抢红包",Toast.LENGTH_SHORT).show();
                break;

            case MSG_RECG_END:

                LogHelper.d("mylog", "recognition end");
                break;
            case 99:
//                Toast.makeText(mContext,"开始识别",Toast.LENGTH_SHORT).show();

                break;
            case 100:
//                Toast.makeText(mContext,"停止识别",Toast.LENGTH_SHORT).show();
                break;
            case 101:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
//                ((Activity)mContext).finish();
//                Toast.makeText(mContext,"返回主页",Toast.LENGTH_SHORT).show();
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
        //fwz
        // 识别到结束符号
        LogHelper.d("mylog", "识别到结束符号了,rec_key:"+rec_key);
        if(rec_key.length() < 5){
            LogHelper.d("mylog", "rec_key:不够长度，继续识别，不停止。重新识别");
        }else{
            mRecognition.stop();
        }
//        mRecognition.stop();
//        Toast.makeText(getApplicationContext(),"识别结束！",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//监听返回键，如果可以后退就后退
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;

            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
