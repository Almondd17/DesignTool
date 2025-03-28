package com.example.designtoolproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DrawingPostActivity extends AppCompatActivity {
    private TextView nameTextView;
    private ImageView imageView;
    private ImageButton editDrawingBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_post);
        editDrawingBtn = findViewById(R.id.editDrawingButton);
        nameTextView = findViewById(R.id.nameTextView);
        imageView = findViewById(R.id.drawingImage);
        nameTextView.bringToFront();
        // Get Data from Intent
        String title = getIntent().getStringExtra("title");
        String base64Image = getIntent().getStringExtra("image bitmap");

        title  = title.substring(0, 1).toUpperCase() + title.substring(1);
        // Set title
        nameTextView.setText(title);

        // Decode Base64 and display image
        if (base64Image != null) {
            Bitmap bitmap = decodeBase64(base64Image);
            imageView.setImageBitmap(bitmap);
        }

        editDrawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDrawing(base64Image);
            }
        });
    }

    private void editDrawing(String base64Image) {
        //figure out a way to send the user to canvas view to modify their drawing
    }

    // Use the same decodeBase64 method from your adapter
    public static Bitmap decodeBase64(String encodedImage) {
        try {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
