package com.example.johnrobert.manongcustomer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.twitter.sdk.android.core.models.Card;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ChildActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "Anonymous";

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference quotesRef = rootRef.child("Quotes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpExitTransition();
        setContentView(R.layout.activity_child);

        Request request = (Request) getIntent().getSerializableExtra("request");
        setUpToolbar();

        if (request != null) {
            ((TextView) findViewById(R.id.text_service_name)).setText(request.getServiceName());
            ((TextView) findViewById(R.id.text_service_key)).setText(request.getKey());

            if (request.getQuotes() != null) {
                findViewById(R.id.second_container).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.scroll_provider_container).setVisibility(ScrollView.VISIBLE);
                // TODO: Generate List Of Service Providers
                generateServiceProviders(request.getQuotes());
            }

        }

        Intent intent = new Intent(ChildActivity.this, InfoActivity.class);
        intent.putExtra("request", request);
        intent.putExtra("isSlideTransition", true);

        findViewById(R.id.text_request_info).setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                this.startActivity(intent, bundle);
            }else {
                startActivity(intent);
            }
        });
//        findViewById(R.id.image_info_logo).setOnClickListener(view -> {
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
//                this.startActivity(intent, bundle);
//            }else {
//                startActivity(intent);
//            }
//        });

    }

    private void generateServiceProviders(HashMap<String, String> serviceProviders) {
        LinearLayout rootContainer = findViewById(R.id.service_provider_container);
        for (String providerId: serviceProviders.keySet()) {

            View cardView = getLayoutInflater().inflate(R.layout.list_card_serviceprovider, null);
            CircleImageView profileImage = cardView.findViewById(R.id.service_profile_picture);
            TextView providerName = cardView.findViewById(R.id.service_provider_name);
            TextView date = cardView.findViewById(R.id.quote_date);
            TextView price = cardView.findViewById(R.id.quote_price);
            TextView lump_sum = cardView.findViewById(R.id.text_lump_sum);
            CardView temp_image = cardView.findViewById(R.id.temp_image_view);
            MaterialRatingBar rating = cardView.findViewById(R.id.service_provider_ratingbar);

            rating.setRating(4.5f);

            rootContainer.addView(cardView);

            String quoteId = serviceProviders.get(providerId);
            if (user != null) {
                String messageLinkKey = user.getUid() + providerId;
                Intent intent = new Intent(this, MessageProviderActivity.class);
                intent.putExtra("messageLinkKey", messageLinkKey);
                cardView.setOnClickListener(view -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
                        setProviderName(providerName, profileImage, providerId, temp_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChildActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setProviderName(TextView providerName, CircleImageView profileImage, String uid, CardView temp_image) {
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

                    if (displayName != null && displayName.trim().equalsIgnoreCase("") || displayName == null) {
                        providerName.setText(ANONYMOUS);
                    }else {
                        providerName.setText(displayName);
                    }
                    if (photoURL != null && photoURL.trim().length() > 1) {
                        if (photoURL.startsWith("https://graph.facebook.com")) {
                            photoURL = photoURL.concat("?height=100");
                        }
                        Glide.with(ChildActivity.this)
                                .load(photoURL)
                                .into(profileImage);
                    }
                    profileImage.setVisibility(CircleImageView.VISIBLE);
                    temp_image.setVisibility(CardView.GONE);

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
        return mFunctions.getHttpsCallable("getUserRecord").call(data)
                .continueWith(task -> {
                    Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
//                    return (String) result.get("displayName");
                    return result;
                });

    }

}
