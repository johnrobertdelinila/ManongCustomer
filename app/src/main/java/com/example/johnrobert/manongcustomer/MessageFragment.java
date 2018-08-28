package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private Activity activity;
    private Context context;
    private int animatedRow = -1;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textNoData;

    private FirebaseUser user = ManongActivity.mUser;

    private DatabaseReference messagesRef = MainActivity.rootRef.child("Messages");

    private FirebaseRecyclerAdapter firebaseAdapter;

    public MessageFragment() {
        // Required empty public constructor
    }

    public class CustomerContact {
        private String providerId, messageLinkKey;

        public CustomerContact() {}

        public CustomerContact(String providerId, String messageLinkKey) {
            this.providerId = providerId;
            this.messageLinkKey = messageLinkKey;
        }

        public String getMessageLinkKey() {
            return messageLinkKey;
        }

        public void setMessageLinkKey(String messageLinkKey) {
            this.messageLinkKey = messageLinkKey;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }
    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_picture;
        TextView text_last_date, text_last_message, text_provider_name;
        CardView temp_image_view;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_picture = itemView.findViewById(R.id.image_profile_picture);
            text_last_date = itemView.findViewById(R.id.text_last_date);
            text_last_message = itemView.findViewById(R.id.text_last_message);
            text_provider_name = itemView.findViewById(R.id.text_provider_name);
            temp_image_view = itemView.findViewById(R.id.temp_image_view);
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        activity = getActivity();
        context = getContext();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        progressBar = view.findViewById(R.id.progress_bar);
        textNoData = view.findViewById(R.id.text_no_data);

        if (user != null) {
            // Database Reference of all the provider's Customer list.
            MainActivity.customerRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("service_providers")) {
                        DatabaseReference customerContactRef = MainActivity.customerRef.child(user.getUid()).child("service_providers");
                        setUpFirebaseRecyclerView(customerContactRef);
                    }else {
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

    private void setUpFirebaseRecyclerView(DatabaseReference customerContactRef) {

        SnapshotParser<CustomerContact> customerParser = snapshot -> {
            CustomerContact customerContact = new CustomerContact();
            String messageLink = snapshot.getValue(String.class);
            customerContact.setProviderId(snapshot.getKey());
            customerContact.setMessageLinkKey(messageLink);
            return customerContact;
        };

        FirebaseRecyclerOptions<CustomerContact> options = new FirebaseRecyclerOptions.Builder<CustomerContact>()
                .setQuery(customerContactRef, customerParser)
                .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<CustomerContact, CustomerViewHolder>(options) {
            @NonNull
            @Override
            public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view1 = LayoutInflater.from(context).inflate(R.layout.list_item_provider, viewGroup, false);
                return new CustomerViewHolder(view1);
            }

            @Override
            protected void onBindViewHolder(@NonNull CustomerViewHolder holder, int position, @NonNull CustomerContact customer) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                Intent intentProviderProfile = new Intent(activity, ProviderProfileActivity.class);

                Intent intent = new Intent(context, MessageProviderActivity.class);
                intent.putExtra("messageLinkKey", customer.getMessageLinkKey());
                intent.putExtra("isSlideTransition", false);
                intent.putExtra("providerId", customer.getProviderId());

//                getProviderProfile(customer.getProviderId(), intentProviderProfile, intent);

                intentProviderProfile.putExtra("providerId", customer.getProviderId());
                holder.profile_picture.setOnClickListener(view -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(intentProviderProfile);
                    } else {
                        final ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(activity, view, "iyot_buto_uki");
                        startActivity(intentProviderProfile, options.toBundle());
                    }
                });
                holder.temp_image_view.setOnClickListener(view -> startActivity(intentProviderProfile));

                checkIfMessageExist(customer.getProviderId(), customer.getMessageLinkKey(), holder.text_last_message, holder.text_last_date, holder.text_provider_name,
                        holder.profile_picture, holder.temp_image_view, intent, intentProviderProfile);

                if (position > animatedRow) {
                    animatedRow = position;
                    long animationDelay = 200L + holder.getAdapterPosition() * 25;

                    holder.itemView.setAlpha(0);
                    holder.itemView.setTranslationY(ScreenUtil.dp2px(8, holder.itemView.getContext()));

                    holder.itemView.animate()
                            .alpha(1)
                            .translationY(0)
                            .setDuration(200)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setStartDelay(animationDelay)
                            .start();
                }

                holder.itemView.setOnClickListener(view -> {
//                    String elementName = getString(R.string.transition_name_navigational_transition);
//                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, elementName);
//                    startActivity(intent, activityOptionsCompat.toBundle());
                    startActivity(intent);
                });

            }
        };

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

//    private void getProviderProfile(String providerId, Intent intent, Intent intent2) {
//        ManongActivity.providerRef.child(providerId).child("my_profile").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                ProviderProfile providerProfile = dataSnapshot.getValue(ProviderProfile.class);
//                intent.putExtra("providerProfile", providerProfile);
//                intent2.putExtra("providerProfile", providerProfile);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void setPhotoAndDisplayName(String providerId, CircleImageView profile_picture, TextView text_provider_name,
                                        CardView temp_image, Intent intent, Intent intentProvider) {
        getUserRecord(providerId)
                .addOnCompleteListener(task -> {
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
                    String displayName = (String) task.getResult().get("displayName");
                    String photoURL = (String) task.getResult().get("photoURL");
                    String phoneNumber = (String) task.getResult().get("phoneNumber");

                    if (activity != null && getContext() != null) {
                        if (photoURL != null) {
                            if (photoURL.startsWith("https://graph.facebook.com")) {
                                photoURL = photoURL.concat("?height=100");
                            }
                            Glide.with(activity.getApplicationContext())
                                    .load(photoURL)
                                    .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            profile_picture.setImageDrawable(resource);
                                            temp_image.setVisibility(CardView.GONE);
                                            profile_picture.setVisibility(CircleImageView.VISIBLE);
                                            return false;
                                        }
                                    })
                                    .into(profile_picture);
                        }else {
                            temp_image.setVisibility(CardView.GONE);
                            profile_picture.setVisibility(CircleImageView.VISIBLE);
                        }
                        if (displayName != null) {
                            text_provider_name.setText(displayName);
                        }else {
                            text_provider_name.setText("Anonymous");
                        }
                    }

                    intent.putExtra("providerName", displayName);
                    intent.putExtra("providerPhoneNumber", phoneNumber);
                    intent.putExtra("providerPhoto", photoURL);

                    intentProvider.putExtra("providerDisplayName", displayName);
                    intentProvider.putExtra("providerPhotoUrl", photoURL);
                    intentProvider.putExtra("providerPhoneNumber", phoneNumber);

                });
    }

    private void checkIfMessageExist(String getProviderId, String messageLinkKey, TextView text_last_message, TextView text_last_date, TextView text_provider_name,
                                     CircleImageView profile_picture, CardView temp_image, Intent intent, Intent intentProvider) {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(messageLinkKey)) {
                    messagesRef.removeEventListener(this);
                    setPhotoAndDisplayName(getProviderId, profile_picture, text_provider_name, temp_image, intent, intentProvider);
                    setProviderInformation(getProviderId, temp_image, messageLinkKey, text_last_message, text_last_date, text_provider_name, profile_picture, intentProvider);
                }else {
                    text_last_message.setText("");
                    text_last_date.setText("");
                    setPhotoAndDisplayName(getProviderId, profile_picture, text_provider_name, temp_image, intent, intentProvider);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProviderInformation(String providerId, CardView temp_image, String messageLinkKey, TextView text_last_message, TextView text_last_date,
                                        TextView text_provider_name, CircleImageView profile_picture, Intent intentProvider) {
        messagesRef.child(messageLinkKey).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    ManongMessage lastMessage = childSnapshot.getValue(ManongMessage.class);
                    if (lastMessage != null) {
                        // TODO: Limit the last text character
                        SimpleDateFormat sfd = new SimpleDateFormat("d MMM y, h:mm a", Locale.getDefault());
                        String dateTime = sfd.format(new Date((Long) lastMessage.getTimestamp()));
                        text_last_message.setText(lastMessage.getText());
                        text_last_date.setText(dateTime);

                        if (lastMessage.getUid().equalsIgnoreCase(providerId)) {
                            intentProvider.putExtra("providerDisplayName", lastMessage.getName());
                            if (lastMessage.getName() != null && !lastMessage.getName().trim().equalsIgnoreCase("")) {
                                text_provider_name.setText(lastMessage.getName());
                            }
                            if (lastMessage.getPhotoUrl() != null && !lastMessage.getPhotoUrl().trim().equalsIgnoreCase("") && !lastMessage.getPhotoUrl().trim().equalsIgnoreCase("null")) {
                                String photoURL = lastMessage.getPhotoUrl();
                                if (photoURL.startsWith("https://graph.facebook.com")) {
                                    photoURL = photoURL.concat("?height=100");
                                }
                                Glide.with(activity.getApplicationContext())
                                        .load(photoURL)
                                        .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                temp_image.setVisibility(CardView.GONE);
                                                profile_picture.setVisibility(CircleImageView.VISIBLE);
                                                temp_image.setVisibility(CardView.GONE);
                                                return false;
                                            }
                                        })
                                        .into(profile_picture);
                                intentProvider.putExtra("providerPhotoUrl", photoURL);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

}
