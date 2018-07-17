package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = mDatabase.getReference();
    private DatabaseReference providerRef = rootRef.child("Providers");
    private DatabaseReference requestRef = rootRef.child("Request");
    private DatabaseReference profileRef;

    private Activity activity;
    private Context context;
    private String providerId;
    private String providerPhotoUrl;
    private String requestKey;
    private String serviceKey;

    private MaterialButton bookButton;
    private LinearLayout linearContainer;
    private RelativeLayout loadingContainer;
    private ArrayList<TextView> textViews;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        activity = getActivity();
        context = getContext();
        providerId = getArguments().getString("providerId");
        providerPhotoUrl = getArguments().getString("providerPhotoUrl");
        requestKey = getArguments().getString("requestKey");
        serviceKey = getArguments().getString("serviceKey");
        profileRef = providerRef.child(providerId).child("my_profile");

        linearContainer = view.findViewById(R.id.linear_container);
        loadingContainer = view.findViewById(R.id.loading_container);
        bookButton = view.findViewById(R.id.book_button);

        ProviderProfile providerProfile = (ProviderProfile) getArguments().getSerializable("providerProfile");

        if (providerProfile != null) {
            setProviderValue(providerProfile);
        }else {
            setupProviderProfile();
        }

        if (providerPhotoUrl != null) {
            getImageColorPalette(providerPhotoUrl);
        }else {
            getUserRecord(providerId).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                        Toast.makeText(activity, "Error: " + String.valueOf(details), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                String photoURL = (String) task.getResult().get("photoURL");
                if (photoURL != null) {
                    if (photoURL.startsWith("https://graph.facebook.com")) {
                        photoURL = photoURL.concat("?height=130");
                    }
                    getImageColorPalette(photoURL);
                }
            });
        }

        bookButton.setOnClickListener(view1 -> savedBooked());

        return view;
    }

    private void addView(String text, String title, LinearLayout.LayoutParams titleParams, LinearLayout.LayoutParams textParams, LinearLayout.LayoutParams viewParams) {

        titleParams.setMargins(0, 5, 0, 35);
        viewParams.setMargins(0, 20, 0, 20);

        View view = new View(activity);
        view.setBackground(ContextCompat.getDrawable(context, android.R.drawable.divider_horizontal_bright));
        view.setLayoutParams(viewParams);

        TextView textTitle = new TextView(activity);
        textTitle.setText(title);
        textTitle.setTextAppearance(context, R.style.ProfileTitleTextAppearance);
        textTitle.setLayoutParams(titleParams);

        textViews.add(textTitle);

        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setPadding(10, 0, 0, 0);
        textView.setTextAppearance(context, R.style.ProfileTextAppearance);
        textView.setLayoutParams(titleParams);

        linearContainer.addView(view);
        linearContainer.addView(textTitle);
        linearContainer.addView(textView);

    }

    private void setupProviderProfile() {
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProviderProfile providerProfile = dataSnapshot.getValue(ProviderProfile.class);
                if (providerProfile != null) {
                    setProviderValue(providerProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProviderValue(ProviderProfile providerProfile) {
        textViews = new ArrayList<>();

        LinearLayout.LayoutParams textview_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams view_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (providerProfile.getAbout() != null) {
            addView(providerProfile.getAbout(), "About", textview_params, textview_params, view_params);
        }
        if (providerProfile.getServices() != null) {
            addView(providerProfile.getServices(), "My Services", textview_params, textview_params, view_params);
        }
        if (providerProfile.getAchievements() != null) {
            addView(providerProfile.getAchievements(), "My Achievements", textview_params, textview_params, view_params);
        }
        if (providerProfile.getAddress() != null) {
            addView(providerProfile.getAddress(), "Address", textview_params, textview_params, view_params);
        }
    }

    private void changeButtonDesign(int tintColor, int textColor, int rippleColor) {
        bookButton.setBackgroundTintList(ColorStateList.valueOf(tintColor));
        bookButton.setTextColor(ColorStateList.valueOf(textColor));
        bookButton.setRippleColor(ColorStateList.valueOf(rippleColor));
    }

    private void getImageColorPalette(String url) {

        Glide.with(activity.getApplicationContext())
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        Palette.from(bitmap)
                                .generate(palette -> {
                                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

                                    if (vibrantSwatch == null) {
                                        vibrantSwatch = palette.getMutedSwatch();
                                        if (vibrantSwatch == null) {
                                            vibrantSwatch = palette.getLightVibrantSwatch();
                                            if (vibrantSwatch == null) {
                                                vibrantSwatch = palette.getLightMutedSwatch();
                                            }
                                        }
                                    }

                                    if (vibrantSwatch == null) {
                                        loadingContainer.setVisibility(RelativeLayout.GONE);
                                        linearContainer.setVisibility(LinearLayout.VISIBLE);
                                        return;
                                    }

                                    changeButtonDesign(vibrantSwatch.getRgb(), vibrantSwatch.getBodyTextColor(), vibrantSwatch.getTitleTextColor());

                                    if (textViews != null && textViews.size() > 0) {
                                        for (TextView textView : textViews) {
                                            textView.setTextColor(ColorStateList.valueOf(vibrantSwatch.getRgb()));
                                        }
                                    }

                                    loadingContainer.setVisibility(RelativeLayout.GONE);
                                    linearContainer.setVisibility(LinearLayout.VISIBLE);

                                });
                    }
                });
    }

    private void savedBooked() {
        if (requestKey != null && serviceKey != null) {
            requestRef.child(requestKey).child("bookedService").setValue(serviceKey)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(activity, "You have booked this vendor.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private Task<Map<String, Object>> getUserRecord(String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        return mFunctions.getHttpsCallable("getUserRecord").call(data)
                .continueWith(task -> {
                    Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                    return result;
                });

    }

}
