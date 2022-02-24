package com.example.button;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.WheelLights;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

public class MainActivity extends RobotActivity {
    public static int REQUEST_CODE = 1;
    Button mbtnrepeat,mbtnaskquestion,mbtnfruittest,mbtnshaling,mbtnbubble,mbtndance,mbtnsayhi;
    static String inputname,number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mbtnsayhi = findViewById(R.id.btnsayhi);
        mbtndance = findViewById(R.id.btndance);
        mbtnaskquestion = findViewById(R.id.btnaskquestion);
        mbtnbubble = findViewById(R.id.btnbubble);
        mbtnfruittest = findViewById(R.id.btnfruittest);
        mbtnrepeat = findViewById(R.id.btnrepeat);
        mbtnshaling = findViewById(R.id.btnshaling);
        Bundle bundle = this.getIntent().getExtras();
        number = bundle.getString("ID");
        inputname = bundle.getString("name");
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        mbtnsayhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.sayhi");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent,REQUEST_CODE);
                mbtnsayhi.setEnabled(false);
                mbtnsayhi.setBackgroundColor(Color.GRAY);
            }
        });
        mbtnshaling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.shaling");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent,REQUEST_CODE);

                mbtnshaling.setEnabled(false);
                mbtnshaling.setBackgroundColor(Color.GRAY);
            }
        });

        mbtndance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.littlestar");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                intent.putExtras(bundle);
                startActivityForResult(intent,REQUEST_CODE);

                mbtndance.setEnabled(false);
                mbtndance.setBackgroundColor(Color.GRAY);
            }
        });

        mbtnrepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.repeatapp");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                startActivityForResult(intent,REQUEST_CODE);
                mbtnrepeat.setEnabled(false);
                mbtnrepeat.setBackgroundColor(Color.GRAY);

            }
        });

        mbtnfruittest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.chooseimage_zenbo");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent,REQUEST_CODE);
                mbtnfruittest.setEnabled(false);
                mbtnfruittest.setBackgroundColor(Color.GRAY);
            }
        });

        mbtnbubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.bubble2");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent,REQUEST_CODE);
                mbtnbubble.setEnabled(false);
                mbtnbubble.setBackgroundColor(Color.GRAY);

            }
        });

        mbtnaskquestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.askquestion");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent,REQUEST_CODE);
                mbtnaskquestion.setEnabled(false);
                mbtnaskquestion.setBackgroundColor(Color.GRAY);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("--------onResume","onResume");
        robotAPI.utility.playAction(1);
        robotAPI.cancelCommand(RobotCommand.MOTION_PLAY_ACTION.getValue());
        robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x000000FF);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("--------onPause","onPause");
        // robotAPI.robot.stopSpeakAndListen();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("--------onStop","onStop");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("--------onStart","onStarte");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("--------onRestart","onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("--------onDestroy","onDestroy");
    }

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


    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }


}