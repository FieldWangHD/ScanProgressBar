package com.kky.wangfang.scanprogress;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.kky.wangfang.scanprogress.scanprogress.ScanProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ScanProgress progress = (ScanProgress) findViewById(R.id.my_progress);
        List<Drawable> drawables = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            drawables.add(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        progress.bindGroup((ViewGroup) findViewById(R.id.activity_main), drawables);
        progress.post(new Runnable() {
            @Override
            public void run() {
                progress.startProgress();
            }
        });

    }
}
