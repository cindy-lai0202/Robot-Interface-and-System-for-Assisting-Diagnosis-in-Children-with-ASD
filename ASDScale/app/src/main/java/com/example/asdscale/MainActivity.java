package com.example.asdscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Button mbutton;
    TextView mtextViewid,mtextviewtitle,mtextviewerr;
    EditText meditText;
    String id,a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mbutton=findViewById(R.id.button);
        meditText=findViewById(R.id.editText);
        mtextViewid=findViewById(R.id.textViewproject);
        mtextviewtitle=findViewById(R.id.textviewname);
        mtextviewerr=findViewById(R.id.textviewerr);
        mbutton.setEnabled(false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = meditText.getText().toString();
                myRef.child(id).child("姓名").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        a = dataSnapshot.getValue(String.class);
                        if(a!=null){
                            Intent intent = new Intent(MainActivity.this, app1.class);
                            intent.putExtra("ID", id);
                            startActivity(intent);
                            finish();
                        }else{
                            mtextviewerr.setText("病例代碼錯誤");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
// Failed to read value
                    }
                });
            }
        });
        meditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(meditText.getText().toString().matches("")){
                    mbutton.setEnabled(false);
                    mtextviewerr.setText("");
                }else{
                    mbutton.setEnabled(true);
                }
            }
        });

    }

     
   

}