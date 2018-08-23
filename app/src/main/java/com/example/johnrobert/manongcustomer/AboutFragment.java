package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private DatabaseReference profileRef;
    private FirebaseRecyclerAdapter firebaseAdapter;

    private Activity activity;
    private Context context;
    private String providerId;
    private String providerPhotoUrl;
    private String requestKey;
    private String serviceKey;

    private ProgressBar progressBar;
    private ArrayList<TextView> textViews;

    private RecyclerView recyclerView;

    public AboutFragment() {
        // Required empty public constructor
    }

    public class About {
        String title, info;

        public About() {}

        public About(String title, String info) {
            this.title = title;
            this.info = info;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    public class AboutViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public AboutViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        activity = getActivity();
        context = getContext();

        providerId = getArguments().getString("providerId");
        providerPhotoUrl = getArguments().getString("providerPhotoUrl");

        requestKey = getArguments().getString("requestKey");
        serviceKey = getArguments().getString("serviceKey");

        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));

        if (ProviderProfileActivity.sakuChan != null) {
            progressBar.getIndeterminateDrawable().setColorFilter(ProviderProfileActivity.sakuChan.getRgb(),
                    android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        if (providerId != null) {
            ManongActivity.providerRef.child(providerId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("my_profile")) {
                        profileRef = dataSnapshot.getRef().child("my_profile");
                        setUpRecylerView();
                        dataSnapshot.getRef().removeEventListener(this);
                    }else {
                        Log.e("TAKKI", "WALA");
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        ProviderProfile providerProfile = (ProviderProfile) getArguments().getSerializable("providerProfile");

//        bookButton.setOnClickListener(view1 -> savedBooked());

        return view;
    }

    private void setUpRecylerView() {

        SnapshotParser<About> aboutParser = snapshot -> new About(snapshot.getKey(), snapshot.getValue(String.class));

        FirebaseRecyclerOptions<About> options = new FirebaseRecyclerOptions.Builder<About>()
                .setQuery(profileRef, aboutParser).build();

        firebaseAdapter = new FirebaseRecyclerAdapter<About, AboutViewHolder>(options) {

            @NonNull
            @Override
            public AboutViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_item_about, viewGroup, false);
                return new AboutViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AboutViewHolder holder, int position, @NonNull About about) {
                holder.title.setText(about.title);
                if (ProviderProfileActivity.sakuChan != null) {
                    holder.title.setTextColor(ColorStateList.valueOf(ProviderProfileActivity.sakuChan.getRgb()));
                }
            }

            @Override
            public int getItemCount() {
                return super.getItemCount() + 1;
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.GONE);
            }
        };

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();

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

//        linearContainer.addView(view);
//        linearContainer.addView(textTitle);
//        linearContainer.addView(textView);

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
//        bookButton.setBackgroundTintList(ColorStateList.valueOf(tintColor));
//        bookButton.setTextColor(ColorStateList.valueOf(textColor));
//        bookButton.setRippleColor(ColorStateList.valueOf(rippleColor));
    }

    private void savedBooked() {
        if (requestKey != null && serviceKey != null) {
            // Checking if it's cancelled
            ManongActivity.requestRef.child(requestKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("isCancelled") && dataSnapshot.child("isCancelled").getValue(Boolean.class) != null) {
                        Toast.makeText(activity, "Sorry but you've already cancelled this request.", Toast.LENGTH_LONG).show();
                    }else {
                        HashMap<String, String> booked = new HashMap<>();
                        booked.put("bookedService", serviceKey);
                        booked.put("bookedProvider", providerId);
                        ManongActivity.requestRef.child(requestKey).child("booked").setValue(booked)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(activity, "You have booked this vendor.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseAdapter != null) {
            firebaseAdapter.stopListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAdapter != null) {
            firebaseAdapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firebaseAdapter != null) {
            firebaseAdapter.startListening();
        }
    }

}
