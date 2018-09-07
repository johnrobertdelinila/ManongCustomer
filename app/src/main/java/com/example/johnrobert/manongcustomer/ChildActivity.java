package com.example.johnrobert.manongcustomer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ChildActivity extends AppCompatActivity {

    public class RequestQuotes {
        private String providerId, service;

        public RequestQuotes() {}

        public RequestQuotes(String providerId, String service) {
            this.providerId = providerId;
            this.service = service;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }
    }

    public class RequestQuotesViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView providerName, date, price, lump_sum;
        CardView temp_image;
        MaterialRatingBar rating;
        ImageView booked;

        public RequestQuotesViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.service_profile_picture);
            providerName = itemView.findViewById(R.id.service_provider_name);
            date = itemView.findViewById(R.id.quote_date);
            price = itemView.findViewById(R.id.quote_price);
            lump_sum = itemView.findViewById(R.id.text_lump_sum);
            temp_image = itemView.findViewById(R.id.temp_image_view);
            rating = itemView.findViewById(R.id.service_provider_ratingbar);
            booked = itemView.findViewById(R.id.bookmark_icon);
        }

    }

    private FirebaseUser user = ManongActivity.mUser;

    private DatabaseReference quotesRef = MainActivity.rootRef.child("Quotes");
    private DatabaseReference requestQuoteRef;

    private FirebaseRecyclerAdapter firebaseAdapter;

    private LinearLayout rootContainer;
    private RecyclerView recyclerView;

    public static final String ANONYMOUS = "Anonymous";
    private String requestDate;
    private Request request;
    private boolean isDoneShowing = false, isDoneSettingUp = false;
    public static String bookedProviderId;
    public static Boolean isRequestBooked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        isRequestBooked = null;

        request = (Request) getIntent().getSerializableExtra("request");
        rootContainer = findViewById(R.id.service_provider_container);
        recyclerView = findViewById(R.id.recycler_view);
        bookedProviderId = null;
        requestQuoteRef = ManongActivity.requestRef.child(request.getKey()).child("quotes");

        if (request.getBooked() != null)
            isRequestBooked = true;

        LinearLayoutManager mLinearManager = new LinearLayoutManager(this);
        mLinearManager.setReverseLayout(true);
        mLinearManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearManager);

        setUpToolbar();

        Intent intent = new Intent(ChildActivity.this, InfoActivity.class);

        findViewById(R.id.text_request_info).setOnClickListener(view -> {
            intent.putExtra("request", request);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                this.startActivity(intent, bundle);
            }else {
                startActivity(intent);
            }
        });

        cancelledListener(request);
        quotesListener();

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            onEnterAnimationComplete();
        }

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        if (request != null && !isDoneShowing) {
            isDoneShowing = true;
            ((TextView) findViewById(R.id.text_service_name)).setText(request.getServiceName());

            for (String key: request.getQuestionsAndAnswers().keySet()) {
                if (key.substring(2).equalsIgnoreCase("When do you need it?") || key.substring(2).toLowerCase().startsWith("when do you need")) {
                    String requestID = generateRequestId(request.getQuestionsAndAnswers().get(key), request.getServiceName(), request.getKey());
                    ((TextView) findViewById(R.id.text_service_key)).setText(requestID);
                    requestDate = request.getQuestionsAndAnswers().get(key);
                    break;
                }
            }

            generateServiceProvider(request);

        }
    }

    private void cancelledListener(Request request) {
        ManongActivity.requestRef.child(request.getKey()).child("isCancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean isCancelled = dataSnapshot.getValue(Boolean.class);
                if (isCancelled != null && isCancelled) {
                    request.setCancelled(true);
                    generateServiceProvider(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void quotesListener() {
        requestQuoteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isDoneSettingUp && request.getQuotes() == null && dataSnapshot.hasChildren()) {
                    setUpFirebaseRecyclerView();
                    dataSnapshot.getRef().removeEventListener(this);
                    findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.GONE);
                    rootContainer.setVisibility(View.VISIBLE);
                    setUpFirebaseRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void generateServiceProvider(Request request) {
        if (request.getCancelled() != null && request.getCancelled()) {
            findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
            rootContainer.setVisibility(View.GONE);
        }else if (request.getQuotes() != null) {
            setUpFirebaseRecyclerView();
            findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.GONE);
            rootContainer.setVisibility(View.VISIBLE);
        }else {
            rootContainer.setVisibility(View.GONE);
        }
    }

    private String generateRequestId(String date, String service, String id) {
        StringBuilder builder = new StringBuilder();
        String serviceSub = service.length() > 2 ? service.substring(0, 2).toUpperCase() : service.toUpperCase();
        String idSub = id.substring(id.length() - 3).toUpperCase();
        String yearSub = date.substring(date.length() - 2);
        String dateSub = date.substring(4, 5);

        builder.append(serviceSub);
        builder.append(idSub);
        builder.append("-");
        builder.append(yearSub);
        builder.append("0");
        builder.append(dateSub);

        return builder.toString();
    }

    private void setProviderName(TextView providerName, CircleImageView profileImage, String uid, CardView temp_image,
                                 Intent intent, Intent intentProviderProfile) {

        profileImage.setVisibility(CircleImageView.GONE);
        temp_image.setVisibility(CardView.VISIBLE);

        getUserRecord(uid)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                            Toast.makeText(this, "Error: " + String.valueOf(details), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    Map<String, Object> result = task.getResult();
                    String displayName = (String) result.get("displayName");
                    String photoURL = (String) result.get("photoURL");
                    String phoneNumber = (String) result.get("phoneNumber");

                    if (displayName != null && displayName.trim().equalsIgnoreCase("") || displayName == null) {
                        providerName.setText(ANONYMOUS);
                    }else {
                        providerName.setText(displayName);
                    }
                    if (photoURL != null && photoURL.trim().length() > 1) {
                        if (photoURL.startsWith("https://graph.facebook.com")) {
                            photoURL = photoURL.concat("?height=100");
                        }
                        Glide.with(getApplicationContext())
                                .load(photoURL)
                                .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        profileImage.setImageDrawable(resource);
                                        profileImage.setVisibility(CircleImageView.VISIBLE);
                                        temp_image.setVisibility(CardView.GONE);
                                        return false;
                                    }
                                })
                                .into(profileImage);
                    }else {
                        profileImage.setImageDrawable(ContextCompat.getDrawable(ChildActivity.this, R.mipmap.ic_account_circle_black_36dp));
                        profileImage.setVisibility(CircleImageView.VISIBLE);
                        temp_image.setVisibility(CardView.GONE);
                    }

                    intent.putExtra("providerName", displayName);
                    intent.putExtra("providerPhoto", photoURL);
                    intent.putExtra("providerPhoneNumber", phoneNumber);

                    intentProviderProfile.putExtra("providerDisplayName", displayName);
                    intentProviderProfile.putExtra("providerPhotoUrl", photoURL);
                    intentProviderProfile.putExtra("providerPhoneNumber", phoneNumber);

                });
    }

    private String convertNumber(Integer number) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(number);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private Task<Map<String, Object>> getUserRecord(String uid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        // Call callable function from Firebase Functions
        return ManongActivity.mFunctions.getHttpsCallable("getUserRecord").call(data)
                .continueWith(task -> {
                    Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
//                    return (String) result.get("displayName");
                    return result;
                });

    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.GONE);
        findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
//        findViewById(R.id.scroll_provider_container).setVisibility(ScrollView.GONE);
        rootContainer.setVisibility(View.GONE);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ManongActivity.requestRef.child(request.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("booked")) {
                    dataSnapshot.getRef().child("booked").child("bookedProvider").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bookedProviderId = dataSnapshot.getValue(String.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpFirebaseRecyclerView() {

        isDoneSettingUp = true;

        SnapshotParser<RequestQuotes> quoteParser = snapshot -> {
            RequestQuotes requestQuotes = new RequestQuotes();
            requestQuotes.setProviderId(snapshot.getKey());
            requestQuotes.setService(snapshot.getValue(String.class));
            return requestQuotes;
        };

        FirebaseRecyclerOptions<RequestQuotes> options = new FirebaseRecyclerOptions.Builder<RequestQuotes>()
                .setQuery(requestQuoteRef, quoteParser)
                .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<RequestQuotes, RequestQuotesViewHolder>(options) {

            @NonNull
            @Override
            public RequestQuotesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(ChildActivity.this).inflate(R.layout.list_card_serviceprovider, viewGroup, false);
                return new RequestQuotesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestQuotesViewHolder holder, int position, @NonNull RequestQuotes requestQuotes) {
                Intent intentProviderProfile = new Intent(ChildActivity.this, ProviderProfileActivity.class);
                intentProviderProfile.putExtra("providerId", requestQuotes.getProviderId());
                intentProviderProfile.putExtra("requestKey", request.getKey());
                intentProviderProfile.putExtra("serviceKey", requestQuotes.getService());

                Intent intent = new Intent(ChildActivity.this, MessageProviderActivity.class);

                setProviderName(holder.providerName, holder.profileImage, requestQuotes.getProviderId(), holder.temp_image, intent, intentProviderProfile);

                quotesRef.child(requestQuotes.getService()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Quotes quote = dataSnapshot.getValue(Quotes.class);
                        if (quote != null) {
                            if (requestDate != null) {
                                holder.date.setText("Service Date: " + requestDate);
                            }
                            holder.price.setText("₱ " + convertNumber(quote.getQuotePrice().get("minimum")) + " - " + convertNumber(quote.getQuotePrice().get("maximum")));
                            holder.lump_sum.setText("Lump Sum");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ChildActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                if (request.getBooked() != null && request.getBooked().get("bookedProvider").equals(requestQuotes.getProviderId())) {
                    holder.booked.setVisibility(ImageView.VISIBLE);
                }else {
                    holder.booked.setVisibility(ImageView.GONE);
                }

                holder.rating.setRating(4.5f);

                holder.profileImage.setOnClickListener(view -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(intentProviderProfile);
                    } else {
                        final ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(ChildActivity.this, view, "iyot_buto_uki");
                        startActivity(intentProviderProfile, options.toBundle());
                    }
                });
                holder.temp_image.setOnClickListener(view -> startActivity(intentProviderProfile));

                if (user != null) {
                    String messageLinkKey = user.getUid() + requestQuotes.getProviderId();
                    intent.putExtra("messageLinkKey", messageLinkKey);
                    intent.putExtra("isSlideTransition", true);
                    intent.putExtra("providerId", requestQuotes.getProviderId());
                    holder.itemView.setOnClickListener(view -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(ChildActivity.this).toBundle();
                            startActivity(intent, bundle);
                        }else {
                            startActivity(intent);
                        }
                    });
                }

            }

        };

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();

    }

}
