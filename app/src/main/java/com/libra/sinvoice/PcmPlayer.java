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

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import com.libra.sinvoice.Buffer.BufferData;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PcmPlayer {
    private final static String TAG = "PcmPlayer";
    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;

    private int mState;
    private AudioTrack mAudio;
    private AudioTrack mAudio2;
    private long mPlayedLen;
    private Listener mListener;
    private Callback mCallback;

    public static interface Listener {
        void onPlayStart();

        void onPlayStop();
    }

    public static interface Callback {
        BufferData getPlayBuffer();

        void freePlayData(BufferData data);
    }

    public PcmPlayer(Callback callback, int sampleRate, int channel, int format, int bufferSize) {
        mCallback = callback;
        mAudio = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, format, bufferSize, AudioTrack.MODE_STREAM);
        mAudio2 = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, format, bufferSize, AudioTrack.MODE_STATIC);
        mState = STATE_STOP;
        mPlayedLen = 0;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }


    public int  soundsize = 0;
    public List<byte[]> fileByteList = null;
    public void start() {
        LogHelper.d(TAG, "start");
        soundsize = 0;
        fileByteList = new ArrayList<>();
        if (STATE_STOP == mState && null != mAudio) {
            mPlayedLen = 0;

            if (null != mCallback) {
                mState = STATE_START;
                LogHelper.d(TAG, "start");
                if (null != mListener) {
                    mListener.onPlayStart();
                }
                while (STATE_START == mState) {
                    LogHelper.d(TAG, "start getbuffer");

                    BufferData data = mCallback.getPlayBuffer();
                    if (null != data) {
                        if (null != data.mData) {
                            //
//                            getFile(data.mData, Environment.getExternalStorageDirectory().getAbsolutePath()+"/test","1ddddd.pcm");

//                            try {
//                              writeFileToSDCard(data.mData);
//                               }catch (Exception e){
//                                  e.printStackTrace();
//                              }
                            Log.d("mylog","pcmplay" + data.mData + "--len:"+data.mData.length + "--totolsize:" + soundsize);
                            soundsize += data.mData.length;
                            fileByteList.add(data.mData);
                            int len = mAudio.write(data.mData, 0, data.getFilledSize());

                            if (0 == mPlayedLen) {
                                mAudio.play();
                            }
                            mPlayedLen += len;
                            mCallback.freePlayData(data);
                        } else {
                            // it is the end of input, so need stop
                            LogHelper.d(TAG, "it is the end of input, so need stop");
                            break;
                        }
                    } else {
                        LogHelper.e(TAG, "get null data");
                        break;
                    }
                }
                //
                Log.d("mylog","stop play");
                try{
                    writeSoudToSDCard(fileByteList);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Log.d("mylog","replay pcm file");
                try{
                    FileInputStream fis = new FileInputStream(
                            new File(Environment.getExternalStorageDirectory().getPath()+"/zhangphil/1.pcm"));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int n;
                    while ((n=fis.read(b)) != -1){
                        bos.write(b,0,n);
                    }
                    mAudio2.write(bos.toByteArray() , 0, bos.toByteArray().length);
                    mAudio2.play();
                }catch (Exception e){
                    e.printStackTrace();
                }


                if (null != mAudio) {
                    mAudio.pause();
                    mAudio.flush();
                    mAudio.stop();
                }
                mState = STATE_STOP;
                if (null != mListener) {
                    mListener.onPlayStop();
                }
                LogHelper.d(TAG, "end");
            }
        }
    }

    public void stop() {
        if (STATE_START == mState && null != mAudio) {
            mState = STATE_STOP;
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
    private void writeSoudToSDCard(List<byte[]> dataArray) throws IOException {
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
            Log.d("mylog", "file is exist,delete");
            file.delete();
        }

        // 创建这个文件，如果不存在
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);

//        String data = "hello,world! Zhang Phil @ CSDN";
//        byte[] buffer = data;

        // 开始写入数据到这个文件。
        for (byte[] data:dataArray){
            fos.write(data, 0, data.length);
        }

        fos.flush();
        fos.close();

        Log.d("文件写入", "成功");
    }
    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath+"\\"+fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
