package com.example.asdscale;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class choose extends AppCompatActivity {
    Button mbutton,mbutton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose);
        mbutton=findViewById(R.id.button);
        mbutton2=findViewById(R.id.button2);

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(choose.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

     
   

}