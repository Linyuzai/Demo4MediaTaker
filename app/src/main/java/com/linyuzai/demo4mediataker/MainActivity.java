package com.linyuzai.demo4mediataker;

import android.Manifest;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.linyuzai.mediataker.MediaTaker;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.net.URI;

import io.reactivex.functions.Consumer;
import kotlin.jvm.functions.Function0;

public class MainActivity extends AppCompatActivity {

    MediaTaker mMediaTaker;
    ImageView mImageView;
    VideoView mVideoView;
    Button mTakePhotoButton;
    Button mSelectPictureButton;
    Button mSelectVideoButton;
    Button mRecordVideoButton;
    RxPermissions mRxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.image);
        mVideoView = (VideoView) findViewById(R.id.video);
        mTakePhotoButton = (Button) findViewById(R.id.take_photo);
        mSelectPictureButton = (Button) findViewById(R.id.select_picture);
        mSelectVideoButton = (Button) findViewById(R.id.select_video);
        mRecordVideoButton = (Button) findViewById(R.id.record_video);
        mRxPermissions = new RxPermissions(this);
        mMediaTaker = new MediaTaker.Builder()
                .authorities(BuildConfig.APPLICATION_ID + "." + this.getClass().getSimpleName())
                .filenameGenerator(new Function0<String>() {
                    @Override
                    public String invoke() {
                        return String.valueOf(System.currentTimeMillis());
                    }
                })
                .cacheDir(getExternalCacheDir())
                //.maxCount(1)
                .create();
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxPermissions.request(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean)
                                    mMediaTaker.takePhoto(MainActivity.this);
                                else
                                    Toast.makeText(MainActivity.this, "you need the permission", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        mSelectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean)
                                    mMediaTaker.selectPicture(MainActivity.this);
                                else
                                    Toast.makeText(MainActivity.this, "you need the permission", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        mSelectVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean)
                                    mMediaTaker.selectVideo(MainActivity.this);
                                else
                                    Toast.makeText(MainActivity.this, "you need the permission", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        mRecordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxPermissions.request(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean)
                                    mMediaTaker.recordVideo(MainActivity.this);
                                else
                                    Toast.makeText(MainActivity.this, "you need the permission", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path = "@null";
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MediaTaker.TAKE_PHOTO:
                    //Log.d("getFilePath", path);
                    //path = mMediaTaker.getFilePath(this, data, MediaTaker.TAKE_PHOTO);
                    //path = mMediaTaker.getSelectedPath(this, mMediaTaker.getTokenPhoto());
                    //Log.d("getFilePath", path);
                    //mImageView.setImageBitmap(mMediaTaker.getTokenPhoto(this));
                    //Glide.with(this).load(mMediaTaker.getTokenPhoto()).into(mImageView);
                    try {
                        Glide.with(this).load(mMediaTaker.getFile(this, data, MediaTaker.TAKE_PHOTO)).into(mImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MediaTaker.SELECT_PHOTO:
                    //Log.d("getFilePath", path);
                    //path = mMediaTaker.getFilePath(this, data, MediaTaker.SELECT_PHOTO);
                    //path = mMediaTaker.getSelectedPath(this, mMediaTaker.getSelectedUri(data));
                    //Log.d("getFilePath", path);
                    //Glide.with(this).load(mMediaTaker.getSelectedUri(data)).into(mImageView);
                    try {
                        Glide.with(this).load(mMediaTaker.getFile(this, data, MediaTaker.SELECT_PHOTO)).into(mImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MediaTaker.SELECT_VIDEO:
                    //Log.d("getFilePath", path);
                    //path = mMediaTaker.getFilePath(this, data, MediaTaker.SELECT_VIDEO);
                    //path = mMediaTaker.getSelectedPath(this, mMediaTaker.getSelectedUri(data));
                    //Log.d("getFilePath", path);
                    ///document/video:76
                    //Log.d("SELECT_VIDEO", data.getData().getPath());
                    //content://com.android.providers.media.documents/document/video%3A76
                    //Log.d("SELECT_VIDEO", data.getData().toString());
                    //Glide.with(this).load(mMediaTaker.getSelectedUri(data)).into(mImageView);
                    try {
                        Glide.with(this).load(mMediaTaker.getFile(this, data, MediaTaker.SELECT_VIDEO)).into(mImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //mVideoView.setVideoURI(mMediaTaker.getSelectedUri(data));
                    //mVideoView.start();
                    break;
                case MediaTaker.RECORD_VIDEO:
                    //Log.d("getFilePath", path);
                    //path = mMediaTaker.getFilePath(this, data, MediaTaker.RECORD_VIDEO);
                    //path = mMediaTaker.getSelectedPath(this, mMediaTaker.getRecordVideo());
                    //Log.d("getFilePath", path);
                    ///root_path/storage/emulated/0/Android/data/com.linyuzai.demo4mediataker/cache/1510023048921.mp4
                    //Log.d("RECORD_VIDEO", mMediaTaker.getRecordVideo().getPath());
                    //content://com.linyuzai.demo4mediataker.MainActivity/root_path/storage/emulated/0/Android/data/com.linyuzai.demo4mediataker/cache/1510023048921.mp4
                    //Log.d("RECORD_VIDEO", mMediaTaker.getRecordVideo().toString());
                    //Glide.with(this).load(mMediaTaker.getRecordVideo());
                    //mVideoView.setVideoURI(mMediaTaker.getRecordVideo());
                    //mVideoView.start();
                    try {
                        Glide.with(this).load(mMediaTaker.getFile(this, data, MediaTaker.RECORD_VIDEO)).into(mImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
