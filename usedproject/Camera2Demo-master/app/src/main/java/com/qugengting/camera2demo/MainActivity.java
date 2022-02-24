package com.qugengting.camera2demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends CheckPermissionsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();

    }

}
