package com.example.designtoolproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private ImageView image1, image2, image3, image4;
    private Button generateButton;
    private EditText editText;
    private Bitmap generatedBitmap;
    private String generatedImageUrl;

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
        editText = v.findViewById(R.id.imageTextInput);
        generateButton = v.findViewById(R.id.generateImagesBtn);
        setClickListener(image1, 1);
        setClickListener(image2, 2);
        setClickListener(image3, 3);
        setClickListener(image4, 4);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                generateNewImages(text);
            }
        });
        return v;
    }

    private void generateNewImages(String textInput) {
        if (textInput.isEmpty()) {
            editText.setError("Please provide a description");
            return;
        }

        generateButton.setEnabled(false);
        generateButton.postDelayed(() -> generateButton.setEnabled(true), 30000); // 30 sec cooldown

        OkHttpClient client = new OkHttpClient.Builder()//30 sec cooldown for the client
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("prompt", textInput);
            jsonBody.put("n", 1);
            jsonBody.put("size", "256x256");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error creating JSON body", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "API call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "API call error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray data = jsonObject.getJSONArray("data");
                    String imageUrl = data.getJSONObject(0).getString("url");
                    Log.d("IMAGE_URL", imageUrl);

                    requireActivity().runOnUiThread(() -> {
                        Glide.with(requireContext())
                                .asBitmap()
                                .load(imageUrl)
                                .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                        image1.setImageBitmap(resource);
                                        generatedBitmap = resource;
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                                        // Optional: Handle what to do when the image is cleared
                                    }

                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);
                                        Toast.makeText(requireContext(), "Image failed to load", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    });

                } catch (JSONException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getApiKey() {
        try {
            Properties properties = new Properties();
            properties.load(requireContext().getAssets().open("keys.properties"));
            return properties.getProperty("openai.api.key");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setClickListener(ImageView imageView, int i) {
        imageView.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Image " + i + " clicked", Toast.LENGTH_SHORT).show();
            if (i == 1 && generatedBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] byteArray = baos.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

                Intent intent = new Intent(getContext(), CanvasActivity.class);
                intent.putExtra("base64Bitmap", base64Image);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No image loaded yet", Toast.LENGTH_SHORT).show();
            }
            // TODO: Replace image later with generated one
            // imageView.setImageBitmap(generatedBitmap);
        });
    }
}

