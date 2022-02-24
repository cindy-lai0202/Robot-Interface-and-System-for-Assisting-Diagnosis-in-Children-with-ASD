package com.example.start;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

public class MainActivity extends RobotActivity {
    public final static String DOMAIN = "A1DCB4BA3C8342E0B6D6ECC570075347";
    public static int REQUEST_CODE = 1;
    EditText meditname,meditsmallname,meditold,meditsex,meditnumber;
    TextView mtextviewname,mtextviewsmallname,mtextviewold,mtextviewsex,mtextviewnumber;
    Button mbtnname;
    String inputname,number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        robotAPI.robot.jumpToPlan(DOMAIN, "startinteract");
        robotAPI.robot.setExpression(RobotFace.ACTIVE);
        meditname = findViewById(R.id.editname);
        meditsmallname = findViewById(R.id.editsmallname);
        meditold = findViewById(R.id.editold);
        meditsex = findViewById(R.id.editsex);
        meditnumber = findViewById(R.id.editnumber);
        mtextviewname= findViewById(R.id.textviewname);
        mtextviewsmallname= findViewById(R.id.textviewsmallname);
        mtextviewold= findViewById(R.id.textviewold);
        mtextviewsex= findViewById(R.id.textviewsex);
        mtextviewnumber=findViewById(R.id.textviewnumber);
        mbtnname = findViewById(R.id.btnname);

        robotAPI.robot.setExpression(RobotFace.HIDEFACE,"請輸入兒童的基本資料");
        Log.e("--------onCreate","onCreate");

        mbtnname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                myRef.child(meditnumber.getText().toString()).child("姓名").setValue(meditname.getText().toString());
                myRef.child(meditnumber.getText().toString()).child("小名").setValue(meditsmallname.getText().toString());
                myRef.child(meditnumber.getText().toString()).child("年齡").setValue(meditold.getText().toString());
                myRef.child(meditnumber.getText().toString()).child("性別").setValue(meditsex.getText().toString());


                inputname = meditsmallname.getText().toString();
                number=meditnumber.getText().toString();

                try {
                    Thread.sleep(5000); //1000為1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.button");
                Bundle bundle = new Bundle();
                bundle.putString("name",inputname);
                bundle.putString("ID",number);
                intent.putExtras(bundle);   // 記得put進去，不然資料不會帶過去哦
                startActivityForResult(intent,REQUEST_CODE);

            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("--------onResume","onResume");
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