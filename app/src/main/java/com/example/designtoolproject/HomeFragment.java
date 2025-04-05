package com.example.designtoolproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
    private ImageView image1, image2, image3, image4;
    private Button generateButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        image1 = v.findViewById(R.id.image1);
        image2 = v.findViewById(R.id.image2);
        image3 = v.findViewById(R.id.image3);
        image4 = v.findViewById(R.id.image4);
        generateButton = v.findViewById(R.id.generateImagesBtn);

        setClickListener(image1, 1);
        setClickListener(image2, 2);
        setClickListener(image3, 3);
        setClickListener(image4, 4);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewImages();
            }
        });
        return v;
    }

    private void generateNewImages() {

    }

    private void setClickListener(ImageView imageView, int i) {
        imageView.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Image " + i + " clicked", Toast.LENGTH_SHORT).show();
            // TODO: Replace image later with generated one
            // imageView.setImageBitmap(generatedBitmap);
        });
    }
}