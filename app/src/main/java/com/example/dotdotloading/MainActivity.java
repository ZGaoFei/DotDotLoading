package com.example.dotdotloading;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.dotdotloading.view.DotLoadingView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DotLoadingView loadingView = findViewById(R.id.dot_loading_view);
        loadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingView.allAnimatorStart();
            }
        });
    }
}
