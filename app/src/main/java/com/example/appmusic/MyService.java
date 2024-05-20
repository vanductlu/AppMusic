package com.example.appmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private MediaPlayer myMusic;
    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler();
    private List<Integer> playlist;
    private List<Integer> imageList;
    private int currentTrackIndex;

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playlist = new ArrayList<>();
        imageList = new ArrayList<>();
        playlist.add(R.raw.nhanduyentiendinh);
        imageList.add(R.drawable.nhanduyentiendinh);

        playlist.add(R.raw.trentinhbanduoitinhyeu);
        imageList.add(R.drawable.trentinhbanduoitinhyeu);

        playlist.add(R.raw.nhuanhdathayem);
        imageList.add(R.drawable.nhuanhdathayem);


        // Add more songs and their corresponding images

        currentTrackIndex = 0;
        initializePlayer();
    }

    private void initializePlayer() {
        if (myMusic != null) {
            myMusic.release();
        }
        myMusic = MediaPlayer.create(this, playlist.get(currentTrackIndex));
        myMusic.setLooping(false);
        myMusic.setOnCompletionListener(mp -> nextTrack());
        broadcastCurrentTrackImage();
    }

    private void broadcastCurrentTrackImage() {
        Intent intent = new Intent("UPDATE_IMAGE");
        intent.putExtra("image_res_id", imageList.get(currentTrackIndex));
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (myMusic != null && myMusic.isPlaying()) {
            myMusic.pause();
        } else if (myMusic != null) {
            myMusic.start();
            updateSeekBar();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myMusic != null) {
            myMusic.stop();
            myMusic.release();
        }
    }

    public void playMusic() {
        if (myMusic != null && !myMusic.isPlaying()) {
            myMusic.start();
            updateSeekBar();
        }
    }

    public void pauseMusic() {
        if (myMusic != null && myMusic.isPlaying()) {
            myMusic.pause();
        }
    }

    public void stopMusic() {
        if (myMusic != null) {
            myMusic.stop();
            initializePlayer();
        }
    }

    public boolean isPlaying() {
        return myMusic != null && myMusic.isPlaying();
    }

    public int getDuration() {
        return myMusic != null ? myMusic.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return myMusic != null ? myMusic.getCurrentPosition() : 0;
    }

    public void seekTo(int position) {
        if (myMusic != null) {
            myMusic.seekTo(position);
        }
    }

    public void nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        initializePlayer();
        playMusic();
    }

    public void previousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        initializePlayer();
        playMusic();
    }

    private void updateSeekBar() {
        handler.postDelayed(() -> {
            if (myMusic != null && myMusic.isPlaying()) {
                Intent intent = new Intent("UPDATE_SEEKBAR");
                intent.putExtra("position", myMusic.getCurrentPosition());
                intent.putExtra("duration", myMusic.getDuration());
                sendBroadcast(intent);
                updateSeekBar();
            }
        }, 1000);
    }
}