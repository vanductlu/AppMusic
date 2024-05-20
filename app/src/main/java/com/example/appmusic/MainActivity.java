package com.example.appmusic;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ImageButton btnplay, btnstop, btnnext, btnprev,btnrewind,btnforward;
    SeekBar seekBar;
    TextView currentTime, durationTime;
    ImageView imageView;
    Boolean flag = true;
    MyService myService;
    boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            updateSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnplay = findViewById(R.id.btnplay);
        btnstop = findViewById(R.id.btnstop);
        btnnext = findViewById(R.id.btnnext);
        btnprev = findViewById(R.id.btnprev);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        durationTime = findViewById(R.id.durationTime);
        imageView = findViewById(R.id.imageView);
        btnrewind = findViewById(R.id.btnrewind);
        btnforward = findViewById(R.id.btnforward);
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    if (myService.isPlaying()) {
                        myService.pauseMusic();
                        btnplay.setImageResource(R.drawable.play);
                    } else {
                        myService.playMusic();
                        btnplay.setImageResource(R.drawable.pause);
                    }
                }
            }
        });

        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    myService.stopMusic();
                    btnplay.setImageResource(R.drawable.play);
                    flag = true;
                }
            }
        });
        btnrewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    int newPosition = Math.max(0, myService.getCurrentPosition() - 10000); // tua 10 giây về trước
                    myService.seekTo(newPosition);
                }
            }
        });

        btnforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    int newPosition = Math.min(myService.getDuration(), myService.getCurrentPosition() + 10000); // tua 10 giây về sau
                    myService.seekTo(newPosition);
                }
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    myService.nextTrack();
                    updateSeekBar();
                }
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    myService.previousTrack();
                    updateSeekBar();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    myService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        registerReceiver(broadcastReceiver, new IntentFilter("UPDATE_SEEKBAR"));
        registerReceiver(imageReceiver, new IntentFilter("UPDATE_IMAGE"));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra("position", 0);
            int duration = intent.getIntExtra("duration", 0);
            seekBar.setMax(duration);
            seekBar.setProgress(position);
            currentTime.setText(formatTime(position));
            durationTime.setText(formatTime(duration));
        }
    };

    private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int imageResId = intent.getIntExtra("image_res_id", 0);
            imageView.setImageResource(imageResId);
        }
    };

    private void updateSeekBar() {
        if (isBound) {
            seekBar.setMax(myService.getDuration());
            durationTime.setText(formatTime(myService.getDuration()));
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(imageReceiver);
    }
}