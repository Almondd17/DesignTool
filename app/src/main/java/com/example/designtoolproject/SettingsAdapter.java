package com.example.designtoolproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnItemClickListener listener;
    private final List<SettingItem> settingsList;

    public SettingsAdapter(List<SettingItem> settingsList) {
        this.settingsList = settingsList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return settingsList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView title = view.findViewById(android.R.id.text1);

            // Set the top margin
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) title.getLayoutParams();
            layoutParams.topMargin = (int) (8 * parent.getContext().getResources().getDisplayMetrics().density); // Adjust the margin as needed
            title.setLayoutParams(layoutParams);

            return new TitleViewHolder(view);        }

        else {
            return new NavigationViewHolder(inflater.inflate(R.layout.item_navigation, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = settingsList.get(position);
        if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).title.setText(item.getTitle());
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"));//light Gray Background
        } else if (holder instanceof NavigationViewHolder) {
            ((NavigationViewHolder) holder).title.setText(item.getTitle());
            ((NavigationViewHolder) holder).description.setText(item.getDescription());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TitleViewHolder(View view) {
            super(view);
            title = view.findViewById(android.R.id.text1);
        }
    }

    static class NavigationViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        NavigationViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.navTitle);
            description = view.findViewById(R.id.navDescription);
        }
    }
}
