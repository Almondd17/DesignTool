package com.example.designtoolproject;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CanvasActivity extends AppCompatActivity {
private BottomNavigationView optionMenu;
private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_page);
        canvasView = findViewById(R.id.DrawView);
        optionMenu = findViewById(R.id.optionMenu);

        //set selected menu item listeners and change selected drawing mode
        //optionMenu.setOnItemSelectedListener();

    }
}