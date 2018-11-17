package com.dev.christian.app.Pkg_Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dev.christian.app.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //New Test Commit
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }
        }).start();
    }
}
