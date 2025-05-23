package com.example.designtoolproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InventoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private DrawingAdapter adapter;
    private List<Drawing> drawingList = new ArrayList<>();
    ProgressBar progressBar;
    private FirebaseUser user; //to check status
    public InventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        progressBar = view.findViewById(R.id.progressBar2);
        container = view.findViewById(R.id.container);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);//not loading any drawings

            //create a TextView
            TextView noUserTextView = new TextView(getContext());
            noUserTextView.setText("Please log in to view your drawings.");
            noUserTextView.setTextSize(18);
            noUserTextView.setTextColor(getResources().getColor(android.R.color.black)); // Customize text color
            noUserTextView.setPadding(16, 16, 16, 16); // Optional padding

            //create a Button for login page
            Button loginButton = new Button(getContext());
            loginButton.setText("Go to Login");
            loginButton.setBackgroundResource(R.drawable.button_design); // Set the custom XML background
            loginButton.setTextColor(getResources().getColor(android.R.color.white)); // Customize text color
            loginButton.setPadding(16, 16, 16, 16); // Optional padding

            //set a click listener on the button
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), LoginActivity.class); // Replace LoginActivity with your login activity
                startActivity(intent);
            });

            // Add TextView and Button to the container
            container.addView(noUserTextView);
            container.addView(loginButton);
        }

        Button myButton = view.findViewById(R.id.goToPage);
        myButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CanvasActivity.class);
                startActivity(intent);
            } else {
                Log.e("InventoryFragment", "Fragment is not attached to an activity.");
            }
        });

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        //create adapter
        adapter = new DrawingAdapter(drawingList, getContext());
        // Fetch data from Firebase
        fetchDrawings();
        recyclerView.setAdapter(adapter);


        return view;
    }

    private void fetchDrawings() {
        if (user == null) {
            //not logged in (no user)
            return;
        }
        //query Firebase for the user's drawings
        String userId = user.getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("drawings")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        drawingList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String drawingName = snapshot.child("name").getValue(String.class);
                            String drawingBase64 = snapshot.child("data").getValue(String.class);
                            String drawingId = snapshot.getKey();
                            Log.d("FetchDrawings", "Name: " + drawingName);
                            Log.d("FetchDrawings", "Base64: " + (drawingBase64 != null ? drawingBase64.substring(0, Math.min(drawingBase64.length(), 100)) : "null"));
                            Drawing drawing = new Drawing(drawingId, drawingName, drawingBase64);
                            drawingList.add(drawing);
                        }
                        //update adapter
                        adapter.notifyDataSetChanged();
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load drawings", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}