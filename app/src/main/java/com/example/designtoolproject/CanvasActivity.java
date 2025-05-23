package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
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
    private ImageButton redoBtn, undoBtn, homeButton;
    private FirebaseUser user;
    private ConstraintLayout TopBarLayout;
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CanvasActivity", "onCreate started"); // Add this
        setContentView(R.layout.activity_canvas_page);
        canvasView = findViewById(R.id.DrawView);
        optionMenu = findViewById(R.id.optionMenu);
        toggleButton = findViewById(R.id.toggleButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        user = FirebaseAuth.getInstance().getCurrentUser();
        redoBtn = findViewById(R.id.redoButton);
        undoBtn = findViewById(R.id.undoButton);
        TopBarLayout = findViewById(R.id.topBarLayout);
        homeButton = findViewById(R.id.homeButton);

        //shared preference
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String defaultMode = prefs.getString("default_drawing_mode", "pencil");
        String canvasColor = prefs.getString("canvas_color", "white");
        //default drawing mode from sp
        canvasView.setMode(defaultMode);
        int selectedItemId;
        switch (defaultMode) {
            case "circle":
                selectedItemId = R.id.circle;
                break;
            case "rectangle":
                selectedItemId = R.id.rectangle;
                break;
            case "line":
                selectedItemId = R.id.line;
                break;
            case "edit":
                selectedItemId = R.id.editMode;
                break;
            case "pencil":
            default:
                selectedItemId = R.id.pencil;
                break;
        }
        optionMenu.setSelectedItemId(selectedItemId);//visually select the sp item

        //default canvas color from sp
        int color = Color.WHITE; // default
        switch (canvasColor) {
            case "black":
                color = Color.BLACK;
                break;
            case "gray":
                color = Color.GRAY;
                break;
            case "white":
        }
        int finalColor = color;
        if (canvasColor.equals("black")) {
            redoBtn.setBackgroundColor(Color.WHITE);
            undoBtn.setBackgroundColor(Color.WHITE);
        } else {
            redoBtn.setBackgroundColor(Color.TRANSPARENT);
            undoBtn.setBackgroundColor(Color.TRANSPARENT);
        }

        //loading external drawing as a bitmap
        String externalDrawingId = getIntent().getStringExtra("drawingId");
        Log.d("CanvasActivity", "Received id: " + externalDrawingId);

        //only proceed if we actually have an external drawing ID
        if (externalDrawingId != null && !externalDrawingId.isEmpty()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference drawingRef = FirebaseDatabase.getInstance().getReference()
                        .child("drawings")
                        .child(user.getUid())
                        .child(externalDrawingId)
                        .child("data"); // <- Only get the base64 "data" field

                drawingRef.get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String base64String = dataSnapshot.getValue(String.class);
                        if (base64String != null && !base64String.isEmpty()) {
                            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            canvasView.setBitmap(decodedBitmap);
                        } else {
                            Log.e("CanvasActivity", "Base64 string is empty or null");
                        }
                    } else {
                        Log.e("CanvasActivity", "Drawing not found in database");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("CanvasActivity", "Failed to load drawing: " + e.getMessage());
                });
            }
        }
        else {
            //wait for canvas view to be drew measured
            canvasView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    canvasView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Bitmap coloredBitmap = createColoredBitmap(canvasView.getWidth(), canvasView.getHeight(), finalColor);
                    canvasView.setBitmap(coloredBitmap);
                }
            });
        }

        homeButton.setOnClickListener(new View.OnClickListener() {//home page go back button
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CanvasActivity.this, HomePage.class);
                startActivity(intent);
            }
        });

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

        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.undo();
            }
        });

        redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.redo();
            }
        });
    }

    private Bitmap createColoredBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);
        return bitmap;
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String drawingId = FirebaseDatabase.getInstance().getReference().child("drawings").child(userId).push().getKey();

        if (drawingId == null) {
            Toast.makeText(this, "Error saving drawing", Toast.LENGTH_SHORT).show();
            return;
        }

        canvasView.post(() -> {
            //get the display metrics
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            //create a bitmap of the exact screen resolution (scaled for high-res displays)
            Bitmap scaledBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            Canvas bitmapCanvas = new Canvas(scaledBitmap);

            //scale the canvas to match the screen resolution, considering the screen's density
            canvasView.layout(0, 0, screenWidth, screenHeight);
            canvasView.draw(bitmapCanvas);

            //compress the bitmap into a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Log.e("encoding", "base64 string: "+base64String);

            //save the drawing data
            Map<String, Object> drawingData = new HashMap<>();
            drawingData.put("name", drawingName);
            drawingData.put("timestamp", System.currentTimeMillis());
            drawingData.put("data", base64String);

            FirebaseDatabase.getInstance().getReference()
                    .child("drawings")
                    .child(userId)
                    .child(drawingId)
                    .setValue(drawingData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CanvasActivity.this, "Drawing saved!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CanvasActivity.this, HomePage.class);
                        intent.putExtra("drawingId", drawingId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(CanvasActivity.this, "Failed to save drawing", Toast.LENGTH_SHORT).show());
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