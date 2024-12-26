package com.example.designtoolproject;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

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
        optionMenu.setOnItemSelectedListener(item -> {
            Map<Integer, String> idModeMap = new HashMap<>();
            idModeMap.put(R.id.pencil, "pencil");
            idModeMap.put(R.id.brush, "brush");
            idModeMap.put(R.id.circle, "circle");
            idModeMap.put(R.id.rectangle, "rectangle");
            idModeMap.put(R.id.line, "line");

            String mode = idModeMap.get(item.getItemId());
            if (mode != null) {
                canvasView.setMode(mode);
            }

            return true;
        });
    }
}