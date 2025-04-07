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
            return new TitleViewHolder(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
        }
        else if (viewType == 1) {
            return new SwitchViewHolder(inflater.inflate(R.layout.item_switch, parent, false));
        }
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
        } else if (holder instanceof SwitchViewHolder) {
            ((SwitchViewHolder) holder).title.setText(item.getTitle());
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

    static class SwitchViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Switch switchButton;
        SwitchViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.switchTitle);
            switchButton = view.findViewById(R.id.switchToggle);
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
