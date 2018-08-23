package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private Activity activity;
    private Context context;
    private int animatedRow = -1;
    private boolean isRemoveCancelled = false;

    private FirebaseUser user = ManongActivity.mUser;

    private FirebaseRecyclerAdapter firebaseAdapter;
    private Query queryUserId;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView textNoData;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        activity = getActivity();
        context = getContext();
        firebaseAdapter = null;

        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLinearManager = new LinearLayoutManager(activity);
        mLinearManager.setReverseLayout(true);
        mLinearManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearManager);
        textNoData = view.findViewById(R.id.text_no_data);

        if (user != null) {

            MainActivity.rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Request")) {
                        queryUserId = dataSnapshot.getRef().child("Request").orderByChild("userId").equalTo(user.getUid());
                        queryUserId.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    setUpFirebaseRecycler(queryUserId);
                                    queryUserId.removeEventListener(this);
                                }else {
                                    // No request is available.
                                    progressBar.setVisibility(ProgressBar.GONE);
                                    textNoData.setVisibility(TextView.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        MainActivity.rootRef.removeEventListener(this);
                    }else {
                        // No request is available.
                        progressBar.setVisibility(ProgressBar.GONE);
                        textNoData.setVisibility(TextView.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


            ToggleButton filterButton = view.findViewById(R.id.filter_button);

            filterButton.setOnClickListener(view12 -> {
                if (filterButton.isChecked()) {
                    Log.e("TOGGLE", "CHECK");
                    isRemoveCancelled = true;
                    if (queryUserId != null) {
                        setUpFirebaseRecycler(queryUserId);
                    }
                }else {
                    Log.e("TOGGLE", "UNCHECK");
                    isRemoveCancelled = false;
                    if (queryUserId != null) {
                        setUpFirebaseRecycler(queryUserId);
                    }
                }
            });


        }

        return view;

    }

    private void setUpFirebaseRecycler(Query queryUserId) {

        SnapshotParser<Request> requestParser = snapshot -> {
            Request request = snapshot.getValue(Request.class);
            if (request != null) {
                request.setKey(snapshot.getKey());
                if (snapshot.hasChild("isCancelled")) {
                    request.setCancelled(snapshot.child("isCancelled").getValue(Boolean.class));
                }
            }
            return request;
        };

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(queryUserId, requestParser)
                .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_row_card, viewGroup, false);
                return new RequestViewHolder(view);
            }

//            @NonNull
//            @Override
//            public Request getItem(int position) {
//                return super.getItem(getItemCount() - 1 - position);
//                return super.getItem(getItemCount() - (position + 1));
//            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request request) {

                if (isRemoveCancelled && request.getCancelled() != null && request.getCancelled()) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(1, 1));
                }else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 10, 0, 10);
                    holder.itemView.setLayoutParams(params);

                    // Animation
                    if (animatedRow == -1)
                        animatedRow = position + 1;

                    if (position < animatedRow) {
                        Log.e("EUT", String.valueOf(animatedRow));

                        animatedRow = position;
                        holder.itemView.setAlpha(0);

                        long animationDelay = 500L + holder.getAdapterPosition() * 25;
                        holder.itemView.animate()
                                .alpha(1)
                                .translationY(0)
                                .setDuration(200)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .setStartDelay(animationDelay)
                                .start();
                    }

                    //Service Name
                    holder.serviceName.setText(request.getServiceName());
                    // Date
                    for (String key: request.getQuestionsAndAnswers().keySet()) {
                        if (key.substring(2).equalsIgnoreCase("When do you need it?") || key.substring(2).toLowerCase().startsWith("when do you need")) {
                            holder.date.setText(request.getQuestionsAndAnswers().get(key));
                            break;
                        }
                    }
                    // Quotes
                    if (request.getQuotes() != null) {
                        int numOfQuotes = request.getQuotes().size();
                        String builder = "View Quotes (" + numOfQuotes + ")";
                        holder.quotes.setText(builder);
                        holder.quotes.setTypeface(Typeface.DEFAULT_BOLD);
                        holder.quotes.setTextColor(getResources().getColor(R.color.colorControlActivated));
                    }else {
                        holder.quotes.setText("Your Quotes Are On The Way");
                        holder.quotes.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                        holder.quotes.setTextColor(getResources().getColor(R.color.textColorPrimary));
                    }

                    // Booked Service
                    if (request.getBooked() != null) {
                        holder.booked.setVisibility(TextView.VISIBLE);
                        holder.quotes.setVisibility(TextView.GONE);
                    }else {
                        holder.booked.setVisibility(TextView.GONE);
                        holder.quotes.setVisibility(TextView.VISIBLE);
                    }
                    // Cancelled
                    if (request.getCancelled() != null && request.getCancelled()) {
                        holder.booked.setVisibility(TextView.VISIBLE);
                        holder.quotes.setVisibility(View.GONE);

                        holder.compare_cancelled.setText("Cancelled");
                        holder.container_text.setBackgroundColor(getResources().getColor(R.color.textErrorColor));

                        holder.booked.setText("You've cancelled this request");
                    }else {
                        holder.booked.setVisibility(TextView.GONE);
                        holder.quotes.setVisibility(TextView.VISIBLE);

                        holder.compare_cancelled.setText("Compare");
                        holder.container_text.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                        if (request.getBooked() != null) {
                            Log.e("BOOKED", "MERON");
                            holder.booked.setVisibility(TextView.VISIBLE);
                            holder.quotes.setVisibility(TextView.GONE);
                        }
                    }

                    holder.itemView.setOnClickListener(view -> {
                        Intent intent = new Intent(context, ChildActivity.class);
                        intent.putExtra("request", request);

                        String elementName = getString(R.string.transition_name_navigational_transition);
                        Pair cardSharedElement = Pair.create(view, elementName);
                        Pair serviceNameSharedElement = Pair.create(holder.serviceName, "transition_service_name");
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, cardSharedElement, serviceNameSharedElement);
                        startActivity(intent, activityOptionsCompat.toBundle());
                    });

                }

                progressBar.setVisibility(ProgressBar.INVISIBLE);

            }

        };

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
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
