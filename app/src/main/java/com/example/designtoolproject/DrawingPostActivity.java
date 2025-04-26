package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DrawingPostActivity extends AppCompatActivity {
    private TextView nameTextView;
    private ImageView imageView;
    private ImageButton editDrawingBtn, deleteDrawingBtn;
    FirebaseUser user;

    private String base64Image = "";
    private String drawingId;

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
        drawingId = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        //String base64Image = getIntent().getStringExtra("image bitmap");

        title  = title.substring(0, 1).toUpperCase() + title.substring(1);
        // Set title
        nameTextView.setText(title);

        DatabaseReference drawingRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("drawings")
                .child(user.getUid())
                .child(drawingId);

        drawingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    base64Image = snapshot.child("data").getValue(String.class);
                    if (base64Image != null && !base64Image.isEmpty()) {
                        Bitmap bitmap = decodeBase64(base64Image);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);

                        } else {
                            Toast.makeText(DrawingPostActivity.this, "Failed to decode drawing image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DrawingPostActivity.this, "Drawing data is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DrawingPostActivity.this, "Drawing not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading drawing: " + error.getMessage());
                Toast.makeText(DrawingPostActivity.this, "Error loading drawing", Toast.LENGTH_SHORT).show();
            }
        });


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
        Intent intent = new Intent(DrawingPostActivity.this, CanvasActivity.class);
        intent.putExtra("drawingId", drawingId);
        startActivity(intent);
        finish();
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
