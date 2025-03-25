package com.example.designtoolproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        settings.add(new SettingItem("Allow notifications", "Receive notifications from the app", 1));
        settings.add(new SettingItem("Language", "Change the app language", 2));

        //user settings
        if (user != null) {
            settings.add(new SettingItem("Privacy Settings", "", 0));
            settings.add(new SettingItem("Enable Notifications", "Get app alerts", 1));
            settings.add(new SettingItem("Change password", "Edit your display name", 2));
            settings.add(new SettingItem("Sign out", "log out of your user into login screen", 2));
        }
        //not registered users
        else {
            settings.add(new SettingItem("Register now", "go to register", 2));
        }
        //general settings (optional)
        settings.add(new SettingItem("Community Settings", "", 0));
        settings.add(new SettingItem("Allow Messages", "Receive messages from others", 1));

        SettingsAdapter adapter = new SettingsAdapter(settings);
        recyclerView.setAdapter(adapter);

        //handle item clicks in the adapter
        adapter.setOnItemClickListener(new SettingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (settings.get(position).getTitle().equals("Sign out")) {
                    showSignOutDialog();
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

    private void logOut() {
        // Your log out implementation here
        // You might want to clear user data or session, etc.
        // Example: FirebaseAuth.getInstance().signOut();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}