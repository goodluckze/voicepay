/*
 * Copyright (C) 2013 gujicheng
 * 
 * Licensed under the GPL License Version 2.0;
 * you may not use this file except in compliance with the License.
 * 
 * If you have any question, please contact me.
 * 
 *************************************************************************
 **                   Author information                                **
 *************************************************************************
 ** Email: gujicheng197@126.com                                         **
 ** QQ   : 29600731                                                     **
 ** Weibo: http://weibo.com/gujicheng197                                **
 *************************************************************************
 */
package com.libra.sinvoice;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.libra.sinvoice.Buffer.BufferData;

public class Encoder implements SinGenerator.Listener, SinGenerator.Callback {
    private final static String TAG = "Encoder";
    private final static int STATE_ENCODING = 1;
    private final static int STATE_STOPED = 2;

    // index 0, 1, 2, 3, 4, 5, 6
    //取样数
    // sampling point Count 31, 28, 25, 22, 19, 15, 10
    private final static int[] CODE_FREQUENCY = { 1422, 1575, 1764, 2004, 2321, 2940, 4410 };
    //fwz
    // 支持0-9的数字 ,第一个是开始标志a，最后一个是结束标志z,a:第11个，z第12个
//    private final static int[] CODE_FREQUENCY = { 2940, 1575, 1680, 1764, 1876, 1949, 2004, 2168, 2257,2321,
//
//            1422, 4410 };

    private int mState;

    private SinGenerator mSinGenerator;
    private Listener mListener;
    private Callback mCallback;

    public static interface Listener {
        void onStartEncode();

        void onEndEncode();
    }

    public static interface Callback {
        void freeEncodeBuffer(BufferData buffer);

        BufferData getEncodeBuffer();
    }

    public Encoder(Callback callback, int sampleRate, int bits, int bufferSize) {
        mCallback = callback;
        mState = STATE_STOPED;
        mSinGenerator = new SinGenerator(this, sampleRate, bits, bufferSize);
        mSinGenerator.setListener(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public final static int getMaxCodeCount() {
        return CODE_FREQUENCY.length;
    }

    public final boolean isStoped() {
        return (STATE_STOPED == mState);
    }

    // content of input from 0 to (CODE_FREQUENCY.length-1)
    public void encode(List<Integer> codes, int duration) {
        encode(codes, duration, 0);
    }

    //
    public void encode(List<Integer> codes, int duration, int muteInterval) {
        if (STATE_STOPED == mState) {
            mState = STATE_ENCODING;

            if (null != mListener) {
                mListener.onStartEncode();
            }

            mSinGenerator.start();
            for (int index : codes) {
                if (STATE_ENCODING == mState) {
                    LogHelper.d(TAG, "encode:" + index);
                    Log.d("mylog","encode:" + index);
                    if (index >= 0 && index < CODE_FREQUENCY.length) {
                        mSinGenerator.gen(CODE_FREQUENCY[index], duration);
//                        Log.d("mylog",sdata.toString());
//                        try {
//                           writeFileToSDCard(sdata);
//                           }catch (Exception e){
//                               e.printStackTrace();
//                         }
                    } else {
                        LogHelper.e(TAG, "code index error");
                    }
                } else {
                    LogHelper.d(TAG, "encode force stop");
                    break;
                }
            }

//            try {
//                writeFileToSDCard(getGenBuffer().mData);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            // for mute
            if (STATE_ENCODING == mState) {
                mSinGenerator.gen(0, muteInterval);
            } else {
                LogHelper.d(TAG, "encode force stop");
            }
            stop();

            if (null != mListener) {
                mListener.onEndEncode();
            }
        }
    }

    public void stop() {
        if (STATE_ENCODING == mState) {
            mState = STATE_STOPED;

            mSinGenerator.stop();
        }
    }

    @Override
    public void onStartGen() {
        LogHelper.d(TAG, "start gen codes");
    }

    @Override
    public void onStopGen() {
        LogHelper.d(TAG, "end gen codes");
    }

    @Override
    public BufferData getGenBuffer() {
        if (null != mCallback) {
            return mCallback.getEncodeBuffer();
        }
        return null;
    }

    @Override
    public void freeGenBuffer(BufferData buffer) {
        if (null != mCallback) {
            mCallback.freeEncodeBuffer(buffer);
        }
    }


    public List<String> fileLIst;
    String getFileName(){
        if(fileLIst == null){
            fileLIst = new ArrayList<>();
        }

        return "" + (fileLIst.size() + 1) + ".pcm";
    }
    // 写一个文件到SDCard
    private void writeFileToSDCard(byte[] data) throws IOException {
        // 比如可以将一个文件作为普通的文档存储，那么先获取系统默认的文档存放根目录
        //File parent_path = Environment.getExternalStorageDirectory();

        // 可以建立一个子目录专门存放自己专属文件
//        File dir = new File(Environment.getDataDirectory().getPath(), "zhangphil");
        File dir = new File(Environment.getExternalStorageDirectory().getPath(), "zhangphil");
        if(dir.mkdirs()){
            Log.d("创建文件夹失败了", dir.getAbsolutePath());
        }else
            Log.d("创建文件夹成功了", dir.getAbsolutePath());

        File file = new File(dir.getAbsoluteFile(), getFileName());
        if(file.exists()){
            Log.d("mylog", "文件已存在");
            return;
        }
        Log.d("文件路径", file.getAbsolutePath());

        // 创建这个文件，如果不存在
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);

//        String data = "hello,world! Zhang Phil @ CSDN";
//        byte[] buffer = data;

        // 开始写入数据到这个文件。
        fos.write(data, 0, data.length);
        fos.flush();
        fos.close();
        fileLIst.add(file.getName());
        Log.d("mylog", "add file:"+file.getName());
    }
}
