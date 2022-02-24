package com.example.littlestar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.VideoView;
import android.widget.MediaController;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.WheelLights;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

public class MainActivity extends RobotActivity {
    private VideoView mVideoView;
    private String uri,inputname;
    private Handler timerUpdateHandler;
    private boolean timerRunning = false;
    private int currentTimer = 0;

    public MainActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = (VideoView) findViewById(R.id.VideoView1);
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        //（1) 指定和啟動播放的檔案video.mp4
        uri="android.resource://" +getPackageName()+"/"+R.raw.littlestarcut;
        mVideoView.setVideoURI(Uri.parse(uri));
        Bundle bundle = this.getIntent().getExtras();
        inputname = bundle.getString("name");
        robotAPI.robot.setExpression(RobotFace.ACTIVE,inputname+"我唱歌給你聽");
        robotAPI.robot.setExpression(RobotFace.ACTIVE,"我們來跳舞吧~");
        timerUpdateHandler = new Handler();
        if (!timerRunning) {
            timerRunning = true;
            currentTimer = 7;
            timerUpdateHandler.post(timerUpdateTask);
        }
        //robotAPI.robot.setExpression(RobotFace.SINGING);
        //robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFFFFFF00);
        //robotAPI.wheelLights.startBlinking(WheelLights.Lights.SYNC_BOTH, 0xff, 42, 42, 0);
        //robotAPI.utility.playAction(3);
        //(2)處理無限播放的功能
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(false);
            }
        });
        //(3) 設定顯示在播放器上的選單
        MediaController mediaController=new MediaController(MainActivity.this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediaController);

        //(4) 檢測影片是否播放完成
      /*  mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
                finish();
            }
        });*/
    }
    private final Runnable timerUpdateTask = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub


            if (currentTimer > 0) {
                currentTimer--;
                timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            } else {
                robotAPI.robot.setExpression(RobotFace.SINGING);
                mVideoView.start();

                robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFFFFFF00);
                robotAPI.wheelLights.startBlinking(WheelLights.Lights.SYNC_BOTH, 0xff, 42, 42, 0);
                robotAPI.utility.playAction(24);
                Log.e("--------play","dance");
                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        robotAPI.cancelCommand(RobotCommand.MOTION_PLAY_ACTION.getValue());
                        robotAPI.cancelCommand(RobotCommand.WHEEL_LIGHTS_SET_COLOR.getValue());
                        robotAPI.cancelCommand(RobotCommand.WHEEL_LIGHTS_SET_ACTIVE.getValue());
                        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                        Log.e("--------onCompletion","danceonCompletion");
                        robotAPI.robot.setExpression(RobotFace.HIDEFACE,"播放完畢");
                        finish();
                        //System.exit(0);
                    }

                });
            }
        }


    };
    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };
    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("--------onDestroy","onDestroy");

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("--------onPause","onPause");
        robotAPI.robot.stopSpeakAndListen();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("--------onStop","onStop");
    }

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }


    protected void onResume() {
        super.onResume();

    }

}
