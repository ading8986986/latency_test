package com.example.latencytest;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.example.latencytest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private long caperTimeElapsed;
    private String caperLog = "";
    private int caperCount = 1;
    private MediaPlayer caperMediaPlayer = new MediaPlayer();

    private long simpleTimeElapsed;
    private String simpleLog = "";
    private int simpleCount = 1;
    private MediaPlayer simpleMediaPlayer;

    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss.SSSXXX");
    private Handler handler;
    private HandlerThread mVoiceReminderThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mVoiceReminderThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_FOREGROUND);
        mVoiceReminderThread.start();
        handler = new Handler(mVoiceReminderThread.getLooper());

        binding.simpleButton.setOnClickListener(view -> {
            simpleTimeElapsed = (new Date()).getTime();
            simpleMediaPlayer = MediaPlayer.create(this, R.raw.alert_scan);
            Log.d(TAG, "MediaPlayer.create = " + sdf3.format(new Timestamp(caperTimeElapsed)));
            simpleMediaPlayer.setOnPreparedListener(this::onSimpleMediaPrepared);
        });

        binding.caperButton.setOnClickListener(view -> {
            caperTimeElapsed = (new Date()).getTime();
            Log.d(TAG, "MediaPlayer.create = " + sdf3.format(new Timestamp(caperTimeElapsed)));
            caperMediaPlayer.setOnPreparedListener(this::onCaperMediaPrepared);
            prepareMediaPlayer(R.raw.alert_scan);
        });
    }

    private void onCaperMediaPrepared(MediaPlayer mediaPlayer) {
        long nowTime = (new Date()).getTime();
        Log.d(TAG, "MediaPlayer.ready to play = " + sdf3.format(new Timestamp(nowTime)));

        caperTimeElapsed = nowTime - caperTimeElapsed;
        caperLog = caperLog + " the " + caperCount + " time takes"  + caperTimeElapsed + " ms \n";
        caperCount++;
        binding.caperContent.setText(caperLog);
        handler.post(mediaPlayer::start);
    }

    private void onSimpleMediaPrepared(MediaPlayer mediaPlayer) {
        long nowTime = (new Date()).getTime();
        Log.d(TAG, "MediaPlayer.ready to play = " + sdf3.format(new Timestamp(nowTime)));

        simpleTimeElapsed = nowTime - simpleTimeElapsed;
        simpleLog = simpleLog + " the " + simpleCount + " time takes" + simpleTimeElapsed + " ms \n";
        simpleCount++;
        binding.simpleContent.setText(simpleLog);
        simpleMediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        caperMediaPlayer.release();
        simpleMediaPlayer.release();
        handler.removeCallbacksAndMessages(null);
    }


    private void prepareMediaPlayer(int resourceId) {
        caperMediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        caperMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            caperMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                           assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            caperMediaPlayer.setLooping(false);
            caperMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Play voice reminder fail");
        }
    }
}