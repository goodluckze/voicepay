package com.example.sinvoicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.fangwuze.sinvioce_copy.R;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;

/**
 * Created by fangwuze on 2018/4/15.
 */

public class SendWebViewActivity extends Activity implements SinVoicePlayer.Listener{
    private final static String TAG = "MainActivity";
    private final static int MAX_NUMBER = 5;
    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;

    private final static String CODEBOOK = "12345";

    private SinVoicePlayer mSinVoicePlayer;

    private WebView mWebView;
    private Button mBtnCallJs;

    private String url_recevice = "http://52.80.155.176:8888/VoiceReciveMain.aspx";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snd_webview);

        mWebView = (WebView)findViewById(R.id.snd_webview);

        mBtnCallJs = (Button)findViewById(R.id.btn_test_call_js);

        WebSettings webSettings = mWebView.getSettings();

        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);


        // name 与js代码里的window.[name]对应
        mWebView.addJavascriptInterface(new JS(), "android");

        // 先载入JS代码
        // 格式规定为:file:///android_asset/文件名.html
//        mWebView.setWebViewClient(new WebViewClient());
//        mWebView.loadUrl("http://172.16.34.66:86/SendRedPackets.aspx");
        mWebView.loadUrl(url_recevice);

        mBtnCallJs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:ReceiveKey("+"12152"+")");
                    }
                });
            }
        });


        // 由于设置了弹窗检验调用结果,所以需要支持js对话框
        // webview只是载体，内容的渲染需要使用webviewChromClient类去实现
        // 通过设置WebChromeClient对象处理JavaScript的对话框
        //设置响应js 的Alert()函数
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(SendWebViewActivity.this);
                b.setTitle("Alert");
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
//        mWebView.loadData();


        mSinVoicePlayer = new SinVoicePlayer(CODEBOOK);
        mSinVoicePlayer.setListener(this);

/*
        final TextView playTextView = (TextView) findViewById(R.id.playtext);
        Button playStart = (Button) this.findViewById(R.id.start_play);
        playStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = genText(3);
                playTextView.setText(text);
                mSinVoicePlayer.play(text, true, 1000);
            }
        });

        Button playStop = (Button) this.findViewById(R.id.stop_play);
        playStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSinVoicePlayer.stop();
            }
        });

        */
    }


    class JS {
        @JavascriptInterface
        public void buildSoud(String p) {
            // 处理从js调用过来的方法
            //
            System.out.println("打印" + p);
        }

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

}
