package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
    private String requestKey;
    private String serviceKey;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MaterialButton bookButton;

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

        TextView title, info;

        public AboutViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            info = itemView.findViewById(R.id.text_info);
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
        requestKey = getArguments().getString("requestKey");
        serviceKey = getArguments().getString("serviceKey");

        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        bookButton = view.findViewById(R.id.book_button);

        if (ChildActivity.bookedProviderId != null && providerId.equals(ChildActivity.bookedProviderId)) {
            bookButton.setText("ACCEPTED");
            bookButton.setEnabled(false);
        }

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
                        view.findViewById(R.id.text_no_profile).setVisibility(View.GONE);
                    }else {
                        Log.e("TAKKI", "WALA");
                        view.findViewById(R.id.text_no_profile).setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (requestKey == null) {
            view.findViewById(R.id.container_cut_button).setVisibility(View.GONE);
        }

        if (ProviderProfileActivity.sakuChan != null) {
            changeButtonDesign(ProviderProfileActivity.sakuChan.getRgb(), ProviderProfileActivity.sakuChan.getBodyTextColor(), ProviderProfileActivity.sakuChan.getTitleTextColor());
        }

        bookButton.setOnClickListener(view1 -> showSavedBookConfirmation());

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
                if (ProviderProfileActivity.sakuChan != null) {
                    holder.title.setTextColor(ColorStateList.valueOf(ProviderProfileActivity.sakuChan.getRgb()));
                }
                String title = "";
                if (about.title.equalsIgnoreCase("about")) {
                    title = "About";
                }else if (about.title.equalsIgnoreCase("achievements")) {
                    title = "My Achievements";
                }else if (about.title.equalsIgnoreCase("address")) {
                    title = "Address";
                }else if (about.title.equalsIgnoreCase("services")) {
                    title = "My Services";
                }
                holder.title.setText(title);
                holder.info.setText(about.info);
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

    private void changeButtonDesign(int tintColor, int textColor, int rippleColor) {
        bookButton.setBackgroundTintList(ColorStateList.valueOf(tintColor));
        bookButton.setTextColor(ColorStateList.valueOf(textColor));
        bookButton.setRippleColor(ColorStateList.valueOf(rippleColor));
    }

    private void showSavedBookConfirmation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.ManongDialogTheme);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Are you sure to hire this service provider?");
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("YES", (dialogInterface, i) -> savedBooked());
        AlertDialog outDialog = dialog.create();
        outDialog.show();
        int color = getResources().getColor(R.color.colorControlActivated);
        if (ProviderProfileActivity.sakuChan != null) {
            color = ProviderProfileActivity.sakuChan.getRgb();
        }
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ColorStateList.valueOf(color));
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ColorStateList.valueOf(color));
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
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
                                        bookButton.setEnabled(false);
                                        bookButton.setText("ACCEPTED");
                                    }
                                });
                        if (ChildActivity.isRequestBooked == null) {
                            MainActivity.customerRef.child(ManongActivity.mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.hasChild("booked")) {
                                        MainActivity.customerRef.child(ManongActivity.mUser.getUid()).child("booked").setValue(1);
                                    } else {
                                        int currBooked = dataSnapshot.child("booked").getValue(Integer.class);
                                        if (dataSnapshot.child("booked").getValue(Integer.class) != null) {
                                            MainActivity.customerRef.child(ManongActivity.mUser.getUid()).child("booked").setValue(currBooked + 1);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            ChildActivity.isRequestBooked = true;
                        }
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
