package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_CODE = 1000;
    public static boolean isUpdated = false;

    private FirebaseUser user = ManongActivity.mUser;

    private Activity activity;

    private Switch switch_quotation, switch_messages, switch_jobs;
    private CircleImageView userProfileImage;
    private TextView userDisplayName, viewEditProfile;
    private CardView tempImage;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        switch_quotation = view.findViewById(R.id.switch_quotation);
        switch_messages = view.findViewById(R.id.switch_messages);
        switch_jobs = view.findViewById(R.id.switch_completed_jobs);
        userProfileImage = view.findViewById(R.id.user_profile_picture);
        userDisplayName = view.findViewById(R.id.user_display_name);
        viewEditProfile = view.findViewById(R.id.user_view_edit);
        tempImage = view.findViewById(R.id.temp_image_view);

        activity = getActivity();

        getUserProfile();

        if (ManongActivity.jobs != null){
            switch_jobs.setChecked(ManongActivity.jobs);
            Log.e("SETTINGS", "JOBS: " + String.valueOf(ManongActivity.jobs));
        }
        if (ManongActivity.messages != null) {
            switch_messages.setChecked(ManongActivity.messages);
            Log.e("SETTINGS", "MESSAGES: " + String.valueOf(ManongActivity.messages));
        }
        if (ManongActivity.quotation != null) {
            switch_quotation.setChecked(ManongActivity.quotation);
            Log.e("SETTINGS", "QUOTATION: " + String.valueOf(ManongActivity.quotation));
        }

        switch_quotation.setOnCheckedChangeListener(this);
        switch_messages.setOnCheckedChangeListener(this);
        switch_jobs.setOnCheckedChangeListener(this);

        view.findViewById(R.id.profile_container).setOnClickListener(profileview -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
//            activity.startActivityForResult(intent, REQUEST_CODE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle();
                this.startActivity(intent, bundle);
            }else {
                startActivity(intent);
            }
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(view1 -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setTitle("Logout");
            dialog.setMessage("Are you sure, you want to logout?");
            dialog.setPositiveButton("YES", (dialogInterface, i) -> AuthUI.getInstance()
                    .signOut(activity)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.finish();
                            startActivity(intent);
                        }
                    }));
            dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialogg = dialog.create();
            dialogg.show();

        });

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        view.findViewById(R.id.btn_developers).setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.VALUE, "CLICKED");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        });

        return view;
    }

    private void updateSettingsProfile(String value, boolean b) {
        if (user != null) {
            MainActivity.customerRef.child(user.getUid()).child("settings").child(value).setValue(b);
        }
    }

    private void getUserProfile() {
        if (user != null) {

            String displayName = user.getDisplayName();
            String photoURL = String.valueOf(user.getPhotoUrl());

            if (activity != null && getContext() != null) {
                if (displayName != null && displayName.equalsIgnoreCase("") || displayName == null) {
                    displayName = "Anonymous";
                }
                userDisplayName.setText(displayName);
                viewEditProfile.setText(getResources().getString(R.string.manong_view_profile));

                if (photoURL != null && !photoURL.trim().equals("") && !photoURL.equals("null")) {
                    if (photoURL.startsWith("https://graph.facebook.com")) {
                        photoURL = photoURL.concat("?height=100");
                    }else if (photoURL.startsWith("https://pbs.twimg.com")) {
                        photoURL = photoURL.replace("_normal", "").trim();
                    }
                    Glide.with(activity.getApplicationContext())
                            .load(photoURL)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    userProfileImage.setImageDrawable(resource);
                                    tempImage.setVisibility(CardView.GONE);
                                    userProfileImage.setVisibility(CircleImageView.VISIBLE);
                                    return false;
                                }
                            })
                            .into(userProfileImage);
                }else {
                    userProfileImage.setImageDrawable(ContextCompat.getDrawable(activity,
                            R.mipmap.ic_account_circle_black_36dp));
                    tempImage.setVisibility(CardView.GONE);
                    userProfileImage.setVisibility(CircleImageView.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        String value = compoundButton.getText().toString();
        updateSettingsProfile(value, b);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUpdated) {
            getUserProfile();
            isUpdated = false;
        }
    }
}
