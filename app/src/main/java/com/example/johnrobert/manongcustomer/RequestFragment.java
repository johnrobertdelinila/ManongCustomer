package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = mDatabase.getReference();
    private DatabaseReference requestRef = rootRef.child("Request");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseRecyclerAdapter firebaseAdapter;

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

        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLinearManager = new LinearLayoutManager(activity);
//        mLinearManager.setReverseLayout(true);
//        mLinearManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearManager);
        textNoData = view.findViewById(R.id.text_no_data);

        if (user != null) {

            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Request")) {
                        Query queryUserId = requestRef.orderByChild("userId").equalTo(user.getUid());
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
//                                Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        rootRef.removeEventListener(this);
                    }else {
                        // No request is available.
                        progressBar.setVisibility(ProgressBar.GONE);
                        textNoData.setVisibility(TextView.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

            @NonNull
            @Override
            public Request getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request request) {

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                holder.serviceName.setText(request.getServiceName());
                if (request.getQuotes() != null) {
                    int numOfQuotes = request.getQuotes().size();
                    String builder = "View Quotes (" + numOfQuotes + ")";
                    holder.quotes.setText(builder);
                    holder.quotes.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.quotes.setTextColor(getResources().getColor(R.color.colorControlActivated));
                }

                for (String key: request.getQuestionsAndAnswers().keySet()) {
                    if (key.substring(2).equalsIgnoreCase("When do you need it?")) {
                        holder.date.setText(request.getQuestionsAndAnswers().get(key));
                        break;
                    }
                }

                if (request.getBookedService() != null ||
                        request.getBookedService() != null && !request.getBookedService().trim().equalsIgnoreCase("")) {
                    holder.booked.setVisibility(TextView.VISIBLE);
                }

                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(context, ChildActivity.class);
                    intent.putExtra("request", request);
                    String elementName = getString(R.string.transition_name_navigational_transition);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, elementName);
                    startActivity(intent, activityOptionsCompat.toBundle());
                });

                // Animation
                if (position > animatedRow) {
                    animatedRow = position;
                    long animationDelay = 200L + holder.getAdapterPosition() * 25;

                    holder.itemView.setAlpha(0);
//                    holder.itemView.setTranslationY(ScreenUtil.dp2px(8, holder.itemView.getContext()));

                    holder.itemView.animate()
                            .alpha(1)
                            .translationY(0)
                            .setDuration(200)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setStartDelay(animationDelay)
                            .start();
                }

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
