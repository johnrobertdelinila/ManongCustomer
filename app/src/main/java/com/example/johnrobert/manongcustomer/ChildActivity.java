package com.example.johnrobert.manongcustomer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.Image;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ChildActivity extends AppCompatActivity implements ChildEventListener {

    public static final String ANONYMOUS = "Anonymous";
    private String requestDate;

    private FirebaseUser user = ManongActivity.mUser;

    private DatabaseReference quotesRef = MainActivity.rootRef.child("Quotes");

    private LinearLayout rootContainer;

    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setUpExitTransition();
        setContentView(R.layout.activity_child);

        request = (Request) getIntent().getSerializableExtra("request");
        rootContainer = findViewById(R.id.service_provider_container);

//        requestRef.child(request.getKey()).child("quotes").addChildEventListener(this);

        setUpToolbar();

        if (request != null) {
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

    private void generateServiceProvider(Request request) {
        if (request.getCancelled() != null && request.getCancelled()) {
            findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.VISIBLE);
            findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.scroll_provider_container).setVisibility(ScrollView.GONE);
        }else if (request.getQuotes() != null) {
            generateServiceProviders(request.getQuotes());
            findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.cancelled_container).setVisibility(RelativeLayout.GONE);
            findViewById(R.id.scroll_provider_container).setVisibility(ScrollView.VISIBLE);
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

    private void generateServiceProviders(HashMap<String, String> serviceProviders) {
        int iterator = 0;
        for (String providerId: serviceProviders.keySet()) {
            String quoteId = serviceProviders.get(providerId);
            LinearLayout.LayoutParams card_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            card_params.setMargins(0, 10, 0, 10);
            LinearLayout.LayoutParams invisible_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            invisible_params.setMargins(0, 40 , 0 ,40);

            View cardView = getLayoutInflater().inflate(R.layout.list_card_serviceprovider, null);
            CircleImageView profileImage = cardView.findViewById(R.id.service_profile_picture);
            TextView providerName = cardView.findViewById(R.id.service_provider_name);
            TextView date = cardView.findViewById(R.id.quote_date);
            TextView price = cardView.findViewById(R.id.quote_price);
            TextView lump_sum = cardView.findViewById(R.id.text_lump_sum);
            CardView temp_image = cardView.findViewById(R.id.temp_image_view);
            MaterialRatingBar rating = cardView.findViewById(R.id.service_provider_ratingbar);
            ImageView booked = cardView.findViewById(R.id.bookmark_icon);

            if (request.getBooked() != null && request.getBooked().get("bookedProvider").equals(providerId)) {
                booked.setVisibility(ImageView.VISIBLE);
            }

            rating.setRating(4.5f);

            Intent intentProviderProfile = new Intent(ChildActivity.this, ProviderProfileActivity.class);
            intentProviderProfile.putExtra("providerId", providerId);
            intentProviderProfile.putExtra("requestKey", request.getKey());
            intentProviderProfile.putExtra("serviceKey", quoteId);

            profileImage.setOnClickListener(view -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intentProviderProfile);
                } else {
                    final ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ChildActivity.this, view, "iyot_buto_uki");
                    startActivity(intentProviderProfile, options.toBundle());
                }
            });
            temp_image.setOnClickListener(view -> startActivity(intentProviderProfile));

            cardView.setLayoutParams(card_params);
            rootContainer.addView(cardView);

            if (iterator == (serviceProviders.size() - 1)) {
                View invi_view = new View(this);
                invi_view.setVisibility(View.INVISIBLE);
                invi_view.setLayoutParams(invisible_params);
                rootContainer.addView(invi_view);
            }

            Intent intent = new Intent(this, MessageProviderActivity.class);

            getProviderProfile(providerId, intentProviderProfile, intent);

            if (user != null) {
                String messageLinkKey = user.getUid() + providerId;
                intent.putExtra("messageLinkKey", messageLinkKey);
                intent.putExtra("isSlideTransition", true);
                intent.putExtra("providerId", providerId);
                cardView.setOnClickListener(view -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                        this.startActivity(intent, bundle);
                    }else {
                        startActivity(intent);
                    }
                });
            }

            quotesRef.child(quoteId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Quotes quote = dataSnapshot.getValue(Quotes.class);
                    if (quote != null) {
                        if (requestDate != null) {
                            date.setText("Service Date: " + requestDate);
                        }
                        price.setText("₱ " + convertNumber(quote.getQuotePrice().get("minimum")) + " - " + convertNumber(quote.getQuotePrice().get("maximum")));
                        lump_sum.setText("Lump Sum");
                        setProviderName(providerName, profileImage, providerId, temp_image, intent, intentProviderProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChildActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            iterator++;

        }
    }

    private void generateEachProvider(String providerId, String quoteId) {
        LinearLayout.LayoutParams card_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        card_params.setMargins(0, 10, 0, 10);
        LinearLayout.LayoutParams invisible_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        invisible_params.setMargins(0, 40 , 0 ,40);

        View cardView = getLayoutInflater().inflate(R.layout.list_card_serviceprovider, null);
        CircleImageView profileImage = cardView.findViewById(R.id.service_profile_picture);
        TextView providerName = cardView.findViewById(R.id.service_provider_name);
        TextView date = cardView.findViewById(R.id.quote_date);
        TextView price = cardView.findViewById(R.id.quote_price);
        TextView lump_sum = cardView.findViewById(R.id.text_lump_sum);
        CardView temp_image = cardView.findViewById(R.id.temp_image_view);
        MaterialRatingBar rating = cardView.findViewById(R.id.service_provider_ratingbar);
        ImageView booked = cardView.findViewById(R.id.bookmark_icon);


        rating.setRating(4.5f);

        Intent intentProviderProfile = new Intent(ChildActivity.this, ProviderProfileActivity.class);
        intentProviderProfile.putExtra("providerId", providerId);
        intentProviderProfile.putExtra("requestKey", request.getKey());
        intentProviderProfile.putExtra("serviceKey", quoteId);

        profileImage.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                startActivity(intentProviderProfile);
            } else {
                final ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(ChildActivity.this, view, "iyot_buto_uki");
                startActivity(intentProviderProfile, options.toBundle());
            }
        });
        temp_image.setOnClickListener(view -> startActivity(intentProviderProfile));

        cardView.setLayoutParams(card_params);
        rootContainer.addView(cardView);

        Intent intent = new Intent(this, MessageProviderActivity.class);

        getProviderProfile(providerId, intentProviderProfile, intent);

        if (user != null) {
            String messageLinkKey = user.getUid() + providerId;
            intent.putExtra("messageLinkKey", messageLinkKey);
            intent.putExtra("isSlideTransition", true);
            intent.putExtra("providerId", providerId);
            cardView.setOnClickListener(view -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                    this.startActivity(intent, bundle);
                }else {
                    startActivity(intent);
                }
            });
        }

        quotesRef.child(quoteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Quotes quote = dataSnapshot.getValue(Quotes.class);
                if (quote != null) {
                    date.setText("Service Date: " + quote.getDate());
                    price.setText("₱ " + convertNumber(quote.getQuotePrice().get("minimum")) + " - " + convertNumber(quote.getQuotePrice().get("maximum")));
                    lump_sum.setText("Lump Sum");
                    setProviderName(providerName, profileImage, providerId, temp_image, intent, intentProviderProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChildActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProviderProfile(String providerId, Intent intent, Intent intent2) {
        ManongActivity.providerRef.child(providerId).child("my_profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProviderProfile providerProfile = dataSnapshot.getValue(ProviderProfile.class);
                intent.putExtra("providerProfile", providerProfile);
                intent2.putExtra("providerProfile", providerProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProviderName(TextView providerName, CircleImageView profileImage, String uid,
                                 CardView temp_image, Intent intent, Intent intentProviderProfile) {
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
                    }

                    intent.putExtra("providerName", displayName);
                    intent.putExtra("providerPhoto", photoURL);
                    intent.putExtra("providerPhoneNumber", phoneNumber);

                    intentProviderProfile.putExtra("providerDisplayName", displayName);
                    intentProviderProfile.putExtra("providerPhotoUrl", photoURL);
                    intentProviderProfile.putExtra("providerPhoneNumber", phoneNumber);

                });
    }

    private void setUpExitTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide fade = new Slide(Gravity.START);
            getWindow().setExitTransition(fade);
        }
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
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        String providerId = dataSnapshot.getKey();
        String quoteId = dataSnapshot.getValue(String.class);
        generateEachProvider(providerId, quoteId);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
