package com.example.asdscale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class app2 extends AppCompatActivity {
    Button mbutton;
    TextView mtextviewname,mtextViewproject,mtextViewproject2,mtextViewproject3;
    String id;
    int n,m,o;
    RadioGroup mradiogroup,mradiogroup2,mradiogroup3;
    RadioButton mradiobtn,mradiobtn2,mradiobtn3,mradiobtn4,mradiobtn5,mradiobtn6,mradiobtn7,mradiobtn8,mradiobtn9;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app2);
        Intent intent = getIntent();
        id = intent.getStringExtra("ID");
        n=0;
        m=0;
        o=0;
        mbutton=findViewById(R.id.button);
        mbutton.setEnabled(false);
        mtextviewname=findViewById(R.id.textviewname);
        mtextViewproject=findViewById(R.id.textViewproject);
        mtextViewproject2=findViewById(R.id.textViewproject2);
        mtextViewproject3=findViewById(R.id.textViewproject3);
        mradiogroup = findViewById(R.id.radioGroup);
        mradiogroup2 = findViewById(R.id.radioGroup2);
        mradiogroup3 = findViewById(R.id.radioGroup3);
        mradiobtn = findViewById(R.id.RadioBtn1);
        mradiobtn2 = findViewById(R.id.RadioBtn2);
        mradiobtn3 = findViewById(R.id.RadioBtn3);
        mradiobtn4 = findViewById(R.id.RadioBtn4);
        mradiobtn5 = findViewById(R.id.RadioBtn5);
        mradiobtn6 = findViewById(R.id.RadioBtn6);
        mradiobtn7 = findViewById(R.id.RadioBtn7);
        mradiobtn8 = findViewById(R.id.RadioBtn8);
        mradiobtn9 = findViewById(R.id.RadioBtn9);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        mradiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mradiobtn.getId()) {
                    myRef.child(id).child("量表").child("覆誦語句").child("口語溝通功能(模仿)").setValue("沒有");
                    n = 1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                } else if (checkedId == mradiobtn2.getId()) {
                    myRef.child(id).child("量表").child("覆誦語句").child("口語溝通功能(模仿)").setValue("一般");
                    n = 1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                } else if (checkedId == mradiobtn3.getId()) {
                    myRef.child(id).child("量表").child("覆誦語句").child("口語溝通功能(模仿)").setValue("有");
                    n = 1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                }
            }
        });
        mradiogroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mradiobtn4.getId()) {
                    myRef.child(id).child("量表").child("覆誦語句").child("專注程度").setValue("沒有");
                    m=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                } else if (checkedId == mradiobtn5.getId()){
                    myRef.child(id).child("量表").child("覆誦語句").child("專注程度").setValue("一般");
                    m=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                }else if(checkedId == mradiobtn6.getId()){
                    myRef.child(id).child("量表").child("覆誦語句").child("專注程度").setValue("有");
                    m=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                }

            }
        });

        mradiogroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mradiobtn7.getId()) {
                    myRef.child(id).child("量表").child("覆誦語句").child("參與程度").setValue("沒有");
                    o=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                } else if (checkedId == mradiobtn8.getId()){
                    myRef.child(id).child("量表").child("覆誦語句").child("參與程度").setValue("一般");
                    o=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                } else if(checkedId == mradiobtn9.getId()){
                    myRef.child(id).child("量表").child("覆誦語句").child("參與程度").setValue("有");
                    o=1;
                    if(n==1&&m==1&&o==1){
                        mbutton.setEnabled(true);
                    }
                }
            }
        });

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(app2.this, app3.class);
                intent.putExtra("ID",id);
                startActivity(intent);
                finish();
            }
        });


    }
}