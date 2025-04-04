package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DrawingPostActivity extends AppCompatActivity {
    private TextView nameTextView;
    private ImageView imageView;
    private ImageButton editDrawingBtn, deleteDrawingBtn;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_post);
        editDrawingBtn = findViewById(R.id.editDrawingButton);
        deleteDrawingBtn = findViewById(R.id.deleteDrawingBtn);
        nameTextView = findViewById(R.id.nameTextView);
        imageView = findViewById(R.id.drawingImage);
        user = FirebaseAuth.getInstance().getCurrentUser();

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
        deleteDrawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDrawing();
            }
        });

        editDrawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDrawing(base64Image);
            }
        });
    }

    private void deleteDrawing() {
        new AlertDialog.Builder(this)
            .setTitle("Are you sure you want to delete this drawing?")
            .setMessage("This will permanently erase your work.")
            .setPositiveButton("Delete Drawing", (dialog, which) -> {
                String drawingId = getIntent().getStringExtra("id");
                if (user != null && drawingId != null) {//just to make sure
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference()//get drawing item db reference
                            .child("drawings")
                            .child(user.getUid())
                            .child(drawingId);

                    ref.removeValue().addOnCompleteListener(task -> {//remove from db
                        if (task.isSuccessful()) {
                            Toast.makeText(DrawingPostActivity.this, "Drawing deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DrawingPostActivity.this, HomePage.class);//go to the home page
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DrawingPostActivity.this, "Failed to delete drawing", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(DrawingPostActivity.this, "Error: Missing drawing ID or user", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Go Back", (dialog, which) -> dialog.dismiss())
            .setCancelable(false)
            .show();
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
