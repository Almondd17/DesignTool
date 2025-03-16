package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;


public class CanvasActivity extends AppCompatActivity {
    private BottomNavigationView optionMenu;
    private CanvasView canvasView;
    private ImageButton toggleButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_page);
        canvasView = findViewById(R.id.DrawView);
        optionMenu = findViewById(R.id.optionMenu);
        toggleButton = findViewById(R.id.toggleButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        user = FirebaseAuth.getInstance().getCurrentUser();

        optionMenu.setOnItemSelectedListener(item -> {
            Map<Integer, String> idModeMap = new HashMap<>();
            idModeMap.put(R.id.pencil, "pencil");
            idModeMap.put(R.id.circle, "circle");
            idModeMap.put(R.id.rectangle, "rectangle");
            idModeMap.put(R.id.line, "line");
            idModeMap.put(R.id.editMode, "edit");

            String mode = idModeMap.get(item.getItemId());

            if (mode != null) {
                canvasView.setMode(mode);
            }

            return true;
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.brush1) {
                    canvasView.setCurrentPaint(1);//change brush thickness...
                    return true;
                } else if (id == R.id.brush2) {
                    canvasView.setCurrentPaint(2);
                    return true;
                } else if (id == R.id.brush3) {
                    canvasView.setCurrentPaint(3);
                    return true;
                } else if (id == R.id.palate) {
                    openColorPickerDialog();//allow color chooser
                    return true;
                } else {
                    showNameInputDialog();//start saving process
                    return id == R.id.item3;
                }
            }
        });
    }

    private void showNameInputDialog() {
        if (this.user == null) {
            Toast.makeText(this, "You need to log in!", Toast.LENGTH_SHORT).show();
            return;//cant save without an account :)
        }

        //edit text for user input
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Enter drawing name");

        //dialog for naming a drawing
        new AlertDialog.Builder(this)
                .setTitle("Enter Drawing Name")
                .setView(nameInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String drawingName = nameInput.getText().toString().trim();
                    if (!drawingName.isEmpty()) {
                        onSave(drawingName);//pass the custom name to the onSave method
                    } else {
                        Toast.makeText(CanvasActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void onSave(String drawingName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID
        String drawingId = FirebaseDatabase.getInstance().getReference().child("drawings").child(userId).push().getKey();

        if (drawingId == null) {
            Toast.makeText(this, "Error saving drawing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Wait for the layout to finish to get the correct size
        canvasView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //remove the listener to prevent it from being called repeatedly
                canvasView.getViewTreeObserver().removeOnPreDrawListener(this);

                //get width and height of the CanvasView
                int width = canvasView.getWidth();
                int height = canvasView.getHeight();

                //create a bitmap with the correct size (match the canvas size)
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                //draw the CanvasView content onto the bitmap
                canvasView.draw(canvas); // Make sure the entire drawing is rendered onto the bitmap

                //compress and encode the bitmap as before
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // Compress as PNG
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.e("Encoding", "base64 string during save: " + base64String);

                Map<String, Object> drawingData = new HashMap<>();
                drawingData.put("name", drawingName);
                drawingData.put("timestamp", System.currentTimeMillis());
                drawingData.put("data", base64String);

                //save to Firebase Realtime DB as string
                FirebaseDatabase.getInstance().getReference()
                        .child("drawings")
                        .child(userId)
                        .child(drawingId)
                        .setValue(drawingData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CanvasActivity.this, "Drawing saved!", Toast.LENGTH_SHORT).show();
                            // Go back to inventory after successful saving with the drawing name
                            Intent intent = new Intent(CanvasActivity.this, InventoryFragment.class);
                            intent.putExtra("drawingId", drawingId); // Pass the drawingId
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Toast.makeText(CanvasActivity.this, "Failed to save drawing", Toast.LENGTH_SHORT).show());

                return true; // Allow the drawing to continue
            }
        });
    }


    @Override
    public void onBackPressed() {//prevent "easy" exit during drawing
        new AlertDialog.Builder(this)
                .setTitle("Exit Drawing?")
                .setMessage("Would you like to continue drawing or discard and exit?")
                .setPositiveButton("Continue Drawing", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton("Discard and Exit", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setCancelable(false)
                .show();
    }

    private void openColorPickerDialog() {//implemented color picker dialog
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, 0xff000000, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                canvasView.setColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }


}