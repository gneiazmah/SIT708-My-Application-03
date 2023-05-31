package com.example.sit708_my_application_01;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    TextView intropage, fitonetitle, timerValue, btnexercise, minutesdisplay, restwatch, restLabel;
    EditText duration;
    View divpage, bgprogress;
    LinearLayout fitone;
    ImageView imgTimer;

    TextView btnStart, btnPause, btnContinue;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis;

    private static final String CHANNEL_ID = "my_channel";
    private static final int NOTIFICATION_ID = 123;

    private ProgressBar progressBar;

    Animation btthree, bttfour, ttbone, ttbtwo, alphago;



    private boolean isRestRunning = false;
    private long startRestTime = 0L;
    private long timeRestInMillis = 0L;
    private long timeSwapBuff = 0L;
    private Handler handler = new Handler();


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timeRestInMillis = SystemClock.uptimeMillis() - startRestTime;
            long updatedTime = timeSwapBuff + timeRestInMillis;
            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            int secondsOutput = seconds % 60;
            int minutesOutput = minutes % 60;
            int hoursOutput = hours % 60;
            restwatch.setText(String.format("%02d:%02d:%02d", hoursOutput, minutesOutput, secondsOutput));
            handler.postDelayed(this, 0);
        }
    };


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        getSupportActionBar().hide();

        //Load Animations
        btthree = AnimationUtils.loadAnimation(this, R.anim.btthree);
        bttfour = AnimationUtils.loadAnimation(this, R.anim.bttfour);
        ttbone = AnimationUtils.loadAnimation(this, R.anim.ttbone);
        ttbtwo = AnimationUtils.loadAnimation(this, R.anim.ttbtwo);
        alphago = AnimationUtils.loadAnimation(this, R.anim.alphago);


        btnStart = (TextView) findViewById(R.id.btnStart);
        btnPause = (TextView) findViewById(R.id.btnPause);
        btnContinue = (TextView) findViewById(R.id.btnContinue);
        intropage = (TextView) findViewById(R.id.intropage);
        fitonetitle = (TextView) findViewById(R.id.fitonetitle);
        timerValue = (TextView) findViewById(R.id.timerValue);
        btnexercise = (TextView) findViewById(R.id.btnexercise);
        duration = (EditText) findViewById(R.id.editTextTime);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        restwatch = (TextView) findViewById(R.id.restwatch);
        restLabel = (TextView) findViewById(R.id.restlabel);

        progressBar.setProgress(100);
        divpage = (View) findViewById(R.id.divpage);
        bgprogress = (View) findViewById(R.id.bgprogress);

        fitone = (LinearLayout) findViewById(R.id.fitone);

        imgTimer = (ImageView) findViewById(R.id.imgtimer);

        //assign animation
        fitone.startAnimation(ttbone);
        intropage.startAnimation(ttbtwo);
        divpage.startAnimation(ttbtwo);
        timerValue.startAnimation(alphago);
        imgTimer.startAnimation(alphago);
        duration.startAnimation(ttbtwo);
        minutesdisplay.startAnimation(ttbtwo);
        btnStart.startAnimation(bttfour);
        btnPause.startAnimation(bttfour);
        btnContinue.startAnimation(bttfour);
        progressBar.startAnimation(bttfour);
        restwatch.startAnimation(bttfour);
        restLabel.startAnimation(bttfour);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
                updateProgressBar();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();

                startRestTimer();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueTimer();

                pauseRestTimer();
            }
        });
    }

    private void startTimer() {

        String durationInput = duration.getText().toString().trim();

        if (durationInput.isEmpty()) {
            Toast.makeText(this, "Please Enter a Workout Duration", Toast.LENGTH_SHORT).show();
            return;
        }

        long millisInput = Long.parseLong(durationInput) * 60 * 1000;

        if (millisInput == 0) {
            return;
        }

        timeLeftInMillis = millisInput;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnPause.setVisibility(View.VISIBLE);
                btnContinue.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                timerValue.setText("00:00");
                duration.setText(" ");
                Toast.makeText(HomeActivity.this, "Workout Completed", Toast.LENGTH_LONG).show();
                showNotification();
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        btnStart.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.INVISIBLE);
    }


    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        btnPause.setVisibility(View.INVISIBLE);
        btnContinue.setVisibility(View.VISIBLE);
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerValue.setText(timeLeftFormatted);
    }

    private void continueTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnPause.setVisibility(View.VISIBLE);
                btnContinue.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                timerValue.setText("00:00");
                duration.setText(" ");
                Toast.makeText(HomeActivity.this, "Workout Completed", Toast.LENGTH_LONG).show();
                showNotification();
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        btnPause.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.INVISIBLE);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerValue.setText(timeLeftFormatted);
    }


    private void startRestTimer() {
        if (!isRestRunning) {
            isRestRunning = true;
            startRestTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
        }
    }

    private void pauseRestTimer() {
        if (isRestRunning) {
            isRestRunning = false;
            timeSwapBuff += timeRestInMillis;
            handler.removeCallbacks(runnable);
        }
    }

    private void updateProgressBar() {

        if(!duration.getText().toString().trim().equals("")){

            String durationInput = duration.getText().toString().trim();
            int durationInMinutes = Integer.parseInt(durationInput);
            timeLeftInMillis = durationInMinutes * 60 * 1000;

            int progress = (int) (((float) timeLeftInMillis  / (timeLeftInMillis / 1000) / 60) * 100);
            progressBar.setProgress(progress);

        }
    }



    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel for Android Oreo and above
            CharSequence channelName = "My Channel";
            String channelDescription = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.power)
                .setContentTitle("My Workout")
                .setContentText("Congratulations!!, You finished your Workout Successfully!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri);

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


}
