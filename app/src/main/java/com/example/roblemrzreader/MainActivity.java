package com.example.roblemrzreader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cl.roblelabs.tools.scans.mrzreader.CaptureActivity;
import cl.roblelabs.tools.scans.mrzreader.model.MrzResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button initMrzBtn = findViewById(R.id.initMrzBtn);
        initMrzBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMrz();
            }
        });
    }

    public void initMrz(){
        Intent mrzAction = new Intent(this, CaptureActivity.class);
        startActivityForResult(mrzAction, 873);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            MrzResult resultado = (MrzResult) data.getSerializableExtra(CaptureActivity.RESULT_KEY);
            Log.d("Roble", "");
        }

    }
}