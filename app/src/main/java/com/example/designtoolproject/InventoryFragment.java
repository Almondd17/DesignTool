package com.example.designtoolproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class InventoryFragment extends Fragment {


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

        Button myButton = view.findViewById(R.id.goToPage);
        myButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CanvasActivity.class);
            startActivity(intent);
        });

        return view;
    }


}