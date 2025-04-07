package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.settingsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        List<SettingItem> settings = new ArrayList<>();
        //local settings
        settings.add(new SettingItem("App Settings", "", 0));
        settings.add(new SettingItem("Dark Mode", "Enable dark mode", 1));
        settings.add(new SettingItem("Default Drawing Mode", "Change the default drawing mode for your art", 2));
        settings.add(new SettingItem("Canvas Color", "Select your working canvas's color", 2));

        //user settings
        if (user != null) {
            settings.add(new SettingItem("Privacy Settings", "", 0));
            settings.add(new SettingItem("Show Email", "display your email", 1));
            settings.add(new SettingItem("Change Password", "Edit your display name", 2));
            settings.add(new SettingItem("Sign Out", "log out of your user into login screen", 2));
        }

        //not registered users
        else {
            settings.add(new SettingItem("Register now", "go to register", 2));
        }

        SettingsAdapter adapter = new SettingsAdapter(settings);
        recyclerView.setAdapter(adapter);

        //handle item clicks in the adapter
        adapter.setOnItemClickListener(new SettingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (settings.get(position).getTitle().equals("Sign out")) {
                    showSignOutDialog();
                }
                else if (settings.get(position).getTitle().equals("Show Email")) {
                    //show user email
                }
                else if (settings.get(position).getTitle().equals("Default Drawing Mode")) {
                    showDrawingModeDialog();
                }
                else if (settings.get(position).getTitle().equals("Change Password")) {
                   //change password
                }
                else if (settings.get(position).getTitle().equals("Canvas Color")) {
                    showCanvasColorDialog();
                }
                if (settings.get(position).getTitle().equals("Register now")) {
                    Intent intent = new Intent(getContext(), RegisterActivity.class);
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure?")
                .setMessage("Do you really want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Call your log-out function here
                    logOut();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showDrawingModeDialog() {
        String[] modes = {"pencil", "circle", "rectangle", "line", "edit"};
        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedMode = prefs.getString("default_drawing_mode", "pencil");

        int selected = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(savedMode)) {
                selected = i;
                break;
            }
        }

        final int[] tempSelection = {selected};

        new AlertDialog.Builder(getContext())
                .setTitle("Default Drawing Mode")
                .setSingleChoiceItems(modes, selected, (d, i) -> tempSelection[0] = i)
                .setPositiveButton("Save", (d, i) -> prefs.edit().putString("default_drawing_mode", modes[tempSelection[0]].toLowerCase()).apply())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCanvasColorDialog() {
        String[] colors = {"White", "Black", "Gray"};
        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedColor = prefs.getString("canvas_color", "white");

        int selected = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equalsIgnoreCase(savedColor)) {
                selected = i;
                break;
            }
        }

        final int[] tempSelection = {selected};

        new AlertDialog.Builder(getContext())
                .setTitle("Canvas Color")
                .setSingleChoiceItems(colors, selected, (d, i) -> tempSelection[0] = i)
                .setPositiveButton("Save", (d, i) -> prefs.edit().putString("canvas_color", colors[tempSelection[0]].toLowerCase()).apply())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}