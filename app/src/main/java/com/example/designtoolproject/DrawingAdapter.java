package com.example.designtoolproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
public class DrawingAdapter extends RecyclerView.Adapter<DrawingAdapter.DrawingViewHolder> {

    private List<Drawing> drawingList;
    private Context context;

    public DrawingAdapter(List<Drawing> drawingList, Context context) {
        this.drawingList = drawingList;
        this.context = context;
    }

    @NonNull
    @Override
    public DrawingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.drawing_item, parent, false);
        return new DrawingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrawingViewHolder holder, int position) {
        Drawing drawing = drawingList.get(position);
        holder.drawingName.setText(drawing.getName());
        String base64String = drawing.getBase64Image();
        Bitmap decodedByte = decodeBase64(base64String);

        //reset the image to avoid old image being displayed
        holder.drawingImage.setImageDrawable(null);

        holder.drawingImage.setImageBitmap(decodedByte);

        holder.itemView.setOnClickListener(v -> {
            //on item click (go to drawing post activity)
            // Toast.makeText(context, "clicked drawing: "+drawing.getName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, DrawingPostActivity.class);
            intent.putExtra("id", drawing.getId());
            intent.putExtra("title", drawing.getName());
//            intent.putExtra("image bitmap", base64String);
            context.startActivity(intent);
        });
    }

    public static Bitmap decodeBase64(String encodedImage) {
        try {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap == null) {
                Log.e("BitmapDecoding", "Decoded bitmap is NULL!");
            }
            return bitmap;
        } catch (Exception e) {
            Log.e("BitmapDecoding", "Error decoding image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return drawingList.size();
    }

    public static class DrawingViewHolder extends RecyclerView.ViewHolder {

        TextView drawingName;
        ImageView drawingImage;

        public DrawingViewHolder(View itemView) {
            super(itemView);
            drawingName = itemView.findViewById(R.id.drawingNameTextView);
            drawingImage = itemView.findViewById(R.id.drawingImageView);
        }
    }

}
