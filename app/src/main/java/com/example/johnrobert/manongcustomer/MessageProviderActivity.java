/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.johnrobert.manongcustomer;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageProviderActivity extends AppCompatActivity {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        LinearLayout containerMessageInro, messageContainer;
        CircleImageView messengerImageView;
        TextView messagePrice, messageDate;
        TextView textTurnOff;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
            messengerImageView = itemView.findViewById(R.id.messengerImageView);
            containerMessageInro = itemView.findViewById(R.id.container_message_intro);
            messagePrice = itemView.findViewById(R.id.text_price);
            messageDate = itemView.findViewById(R.id.text_date);
            messageContainer = itemView.findViewById(R.id.message_container);
            textTurnOff = itemView.findViewById(R.id.text_turn_off);
        }
    }

    private static final String TAG = "MessageProviderActivity";
    private static final int REQUEST_IMAGE = 2;
    public static final String ANONYMOUS = "Anonymous";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private String mUsername;
    private String mPhotoUrl;

    private MaterialButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    public ImageView mAddMessageImageView;
    private AdView mAdView;
    private TextView textFirstMessage;
    private Toolbar toolbar;

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    private String messagelinkKey, receiverId, isConversationOff;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference messagesRef = rootRef.child("Messages");
    private DatabaseReference disabledChatRef = rootRef.child("Disabled Chat Convo");
    private boolean doneSettingUp;

    private String providerId;
    private String providerName;
    private String providerPhoto;
    private String providerPhoneNumber;

    private Intent intentProviderProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isSlideTransition = getIntent().getBooleanExtra("isSlideTransition", true);
        if (isSlideTransition) {
            setUpEnterTransition();
            setContentView(R.layout.activity_message_provider_slide_transition);
        }else {
            setContentView(R.layout.activity_message_provider);
        }

        mMessageEditText = findViewById(R.id.messageEditText);
        mAddMessageImageView = findViewById(R.id.addMessageImageView);

        isConversationOff = getIntent().getStringExtra("isConversationOff");
        if (isConversationOff != null && isConversationOff.equals("yes")) {
            updateButtonUi(true);
        }else {
            disabledChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(messagelinkKey)) {
                        updateButtonUi(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        // Initialize and request AdMob ad.
        setUpAdMob();

        setUpToolbar();

        textFirstMessage = findViewById(R.id.text_first_message);

        messagelinkKey = getIntent().getStringExtra("messageLinkKey");
        providerId = getIntent().getStringExtra("providerId");
        providerName = getIntent().getStringExtra("providerName");
        providerPhoto = getIntent().getStringExtra("providerPhoto");
        providerPhoneNumber = getIntent().getStringExtra("providerPhoneNumber");
        doneSettingUp = false;

        intentProviderProfile = new Intent(MessageProviderActivity.this, ProviderProfileActivity.class);
        intentProviderProfile.putExtra("providerId", providerId);

        intentProviderProfile.putExtra("providerDisplayName", providerName);
        intentProviderProfile.putExtra("providerPhotoUrl", providerPhoto);
        intentProviderProfile.putExtra("providerPhoneNumber", providerPhoneNumber);

        if (providerName != null) {
            toolbar.setTitle(providerName);
        }

        receiverId = messagelinkKey.replaceAll(mFirebaseUser.getUid(), "").trim();

        if (providerName == null && providerPhoto == null && providerPhoneNumber == null) {
            getProviderProfile(providerId, intentProviderProfile);
        }

//        if (providerProfile == null) {
//            getProviderProfile2(providerId, intentProviderProfile);
//        }

        if (mFirebaseUser != null){
            // Getting the name from Firebase User
            mUsername = mFirebaseUser.getDisplayName();
            // Checking if the username is null,
            // Maybe get the name from the provider data.
            if (mUsername != null && mUsername.trim().equalsIgnoreCase("") || mUsername == null) {
                for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
                    String displayName = userInfo.getDisplayName();
                    if (displayName != null) {
                        mUsername = displayName;
                    }
                }
            }
            // Checking if the display name is still "" or null.
            if (mUsername != null && mUsername.trim().equalsIgnoreCase("") || mUsername == null) {
                // Just name it Anonymous.
                mUsername = ANONYMOUS;
                // Trying to get the User Display Name using the Cloud Function.
                getUserRecord(mFirebaseUser.getUid())
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
                            String displayName = (String) task.getResult().get("displayName");
                            if (displayName != null && displayName.trim().equals("") || displayName == null) {
                                displayName = ANONYMOUS;
                            }
                            mUsername = displayName;
                            if (!mUsername.equalsIgnoreCase(ANONYMOUS)) {
                                // Update all the names in the chat
                                updateAllUserName(mUsername);
                            }
                        });
            }

            // Getting the Photo Url from Firebase User
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            // If the photo is still null.
            if (mPhotoUrl != null && mPhotoUrl.trim().equalsIgnoreCase("") || mPhotoUrl == null) {
                for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
                    Uri photo = userInfo.getPhotoUrl();
                    if (photo != null) {
                        mPhotoUrl = photo.toString();
                    }
                }
            }

        }

        mProgressBar = findViewById(R.id.progressBar);
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Messages")) {
                    rootRef.removeEventListener(this);

                    messagesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(messagelinkKey) && !doneSettingUp) {
                                setUpChatMessage();
                                textFirstMessage.setVisibility(View.GONE);
                            }else {
                                textFirstMessage.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                            // Remove the listener because we now know that their chat message already exist.
                            if (doneSettingUp) {
                                updateUserDisplayName(mUsername, mPhotoUrl, mFirebaseUser.getUid());
                                updateUserDisplayName(providerName, providerPhoto, providerId);
                                messagesRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MessageProviderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    textFirstMessage.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MessageProviderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mAddMessageImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        mSendButton = findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(view -> {
            ManongMessage manongMessage = new ManongMessage(mMessageEditText.getText().toString(), mUsername,
                    mPhotoUrl, null, mFirebaseUser.getUid(), ServerValue.TIMESTAMP, receiverId, MainActivity.userType);
            messagesRef.child(messagelinkKey).push().setValue(manongMessage);
            mMessageEditText.setText("");
        });

    }

    private void updateUserDisplayName(String name, String photo, String uid) {
        if (name != null && photo != null) {
            updateMessageDisplayName(name, photo, uid);
        }else {
            getUserRecord(uid)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                                Toast.makeText(MessageProviderActivity.this, "Error: " + String.valueOf(details), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        String displayName = (String) task.getResult().get("displayName");
                        String photoURL = (String) task.getResult().get("photoURL");
                        if (photoURL != null) {
                            if (photoURL.startsWith("https://graph.facebook.com")) {
                                photoURL = photoURL.concat("?height=100");
                            }
                        }
                        updateMessageDisplayName(displayName, photoURL, uid);
                    });
        }
    }

    private void setUpAdMob() {
        mAdView = findViewById(R.id.adView);

//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        MobileAds.initialize(MessageProviderActivity.this, getString(R.string.banner_app_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("DE559DA4D5770BE11553FFA6B05CDE12").build();
        adRequest.isTestDevice(this);
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e("AD", "LOADED");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("AD", "FAILED - " + String.valueOf(errorCode));
                mAdView.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.e("AD", "OPENED");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.e("AD", "LEFT THE APP");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.e("AD", "CLOSED");
            }
        });
    }

    private void updateMessageDisplayName(String providerNewDisplayname, String photoURL, String uid) {
        Query query = messagesRef.child(messagelinkKey).orderByChild("uid").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    ManongMessage providerMessage = childSnapshot.getValue(ManongMessage.class);
                    if (providerMessage != null) {
                        if (providerMessage.getUid().equals(uid)) {
                            String key = childSnapshot.getKey();
                            if (key != null) {
                                if (providerNewDisplayname != null) {
                                    HashMap<String, Object> newName = new HashMap<>();
                                    newName.put("name", providerNewDisplayname);
                                    messagesRef.child(messagelinkKey).child(key).updateChildren(newName);
                                }
                                if (photoURL != null) {
                                    HashMap<String, Object> newPhoto = new HashMap<>();
                                    newPhoto.put("photoUrl", photoURL);
                                    messagesRef.child(messagelinkKey).child(key).updateChildren(newPhoto);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MessageProviderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpEnterTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide slide;

            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
                slide = new Slide(GravityCompat.getAbsoluteGravity(GravityCompat.END, getResources().getConfiguration().getLayoutDirection()));
            }else {
                slide = new Slide(Gravity.END);
            }

            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(slide);
        }
    }

    private void updateAllUserName(String newName) {
        Query query = messagesRef.child(messagelinkKey).orderByChild("uid").equalTo(mFirebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    messagesRef.child(messagelinkKey).child(Objects.requireNonNull(childSnapshot.getKey())).child("name").setValue(newName)
                            .addOnFailureListener(e -> Toast.makeText(MessageProviderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MessageProviderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpChatMessage() {
        FirebaseRecyclerOptions<ManongMessage> options =
                new FirebaseRecyclerOptions.Builder<ManongMessage>()
                        .setQuery(messagesRef.child(messagelinkKey), ManongMessage.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<ManongMessage, MessageViewHolder>(options) {

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ManongMessage message) {

                if (message.getTurnedOff() != null) {
                    // Message have been turned off.
                    holder.textTurnOff.setVisibility(View.VISIBLE);
                    holder.messageContainer.setVisibility(View.GONE);
                    if (message.getTurnedOff()) {
                        holder.textTurnOff.setText(message.getText());
                    }else {
                        holder.textTurnOff.setText(message.getText());
                    }
                }else {
                    holder.textTurnOff.setVisibility(View.GONE);
                    holder.messageContainer.setVisibility(View.VISIBLE);

                    if (message.getUid().equals(providerId)) {
                        holder.messengerImageView.setOnClickListener(view -> {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                startActivity(intentProviderProfile);
                            } else {
                                final ActivityOptions options = ActivityOptions
                                        .makeSceneTransitionAnimation(MessageProviderActivity.this, view, "iyot_buto_uki");
                                startActivity(intentProviderProfile, options.toBundle());
                            }
                        });
                    }else {
                        holder.messengerImageView.setClickable(false);
                    }


                    if (message.getText() != null) {
                        holder.messageTextView.setText(message.getText());
                        holder.messageTextView.setVisibility(TextView.VISIBLE);
                        holder.messageImageView.setVisibility(ImageView.GONE);
                    } else if (message.getImageUrl() != null) {
                        String imageUrl = message.getImageUrl();
                        if (imageUrl.startsWith("gs://")) {
                            StorageReference storageReference = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageUrl);
                            storageReference.getDownloadUrl().addOnCompleteListener(
                                    task -> {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(getApplicationContext())
                                                    .load(downloadUrl)
                                                    .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                                    .into(holder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    });
                        } else {
                            Glide.with(getApplicationContext())
                                    .load(message.getImageUrl())
                                    .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                    .into(holder.messageImageView);
                        }
                        holder.messageImageView.setVisibility(ImageView.VISIBLE);
                        holder.messageTextView.setVisibility(TextView.GONE);
                    }

                    holder.messengerTextView.setText(message.getName());
                    if (message.getPhotoUrl() == null) {
                        holder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MessageProviderActivity.this,
                                R.mipmap.ic_account_circle_black_36dp));
                    } else {
                        Glide.with(getApplicationContext())
                                .load(message.getPhotoUrl())
                                .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                .into(holder.messengerImageView);
                    }

                    if (message.getQuoteMessage() != null) {
                        holder.containerMessageInro.setVisibility(View.VISIBLE);
                        holder.messagePrice.setText("₱ " + convertNumber(Integer.parseInt(message.getQuoteMessage().get("min_price"))) + " - "
                                + convertNumber(Integer.parseInt(message.getQuoteMessage().get("max_price"))));
                        holder.messageDate.setText(message.getQuoteMessage().get("date"));
                    }else {
                        holder.containerMessageInro.setVisibility(View.GONE);
                    }

                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                }

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mFirebaseAdapter.startListening();
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        doneSettingUp = true;
    }

    private void getProviderProfile(String uid, Intent intentProviderProfile) {
        getUserRecord(uid).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                if (e instanceof FirebaseFunctionsException) {
                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                    FirebaseFunctionsException.Code code = ffe.getCode();
                    Object details = ffe.getDetails();
                    Toast.makeText(MessageProviderActivity.this, "Error: " + String.valueOf(details), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            String displayName = (String) task.getResult().get("displayName");
            String photoURL = (String) task.getResult().get("photoURL");
            String phoneNumber = (String) task.getResult().get("phoneNumber");

            if (photoURL != null) {
                if (photoURL.startsWith("https://graph.facebook.com")) {
                    photoURL = photoURL.concat("?height=130");
                }
            }

            intentProviderProfile.putExtra("providerDisplayName", displayName);
            intentProviderProfile.putExtra("providerPhotoUrl", photoURL);
            intentProviderProfile.putExtra("providerPhoneNumber", phoneNumber);

        });
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
//        if (mFirebaseAdapter != null) {
//            mFirebaseAdapter.stopListening();
//        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (mFirebaseAdapter != null) {
            mFirebaseAdapter.startListening();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mFirebaseAdapter != null) {
            mFirebaseAdapter.stopListening();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    assert uri != null;
                    Log.d(TAG, "Uri: " + uri.toString());

                    ManongMessage tempMessage = new ManongMessage(null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL, mFirebaseUser.getUid(), ServerValue.TIMESTAMP, receiverId, MainActivity.userType);
                    messagesRef.child(messagelinkKey).push()
                            .setValue(tempMessage, (databaseError, databaseReference) -> {
                                if (databaseError == null) {
                                    String key = databaseReference.getKey();
                                    assert key != null;
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser.getUid())
                                            .child(key)
                                            .child(uri.getLastPathSegment());

                                    putImageInStorage(storageReference, uri, key);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            });
                }
            }
        }
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {

        UploadTask uploadTask = storageReference.putFile(uri);

        uploadTask.addOnCompleteListener(MessageProviderActivity.this,
                task -> {
                    if (!task.isSuccessful() || !task.isComplete()) {
                        Log.w(TAG, "Image upload task was not successful.",
                                task.getException());
                    }
                });

        uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return storageReference.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                ManongMessage manongMessage =
                                        new ManongMessage(null, mUsername, mPhotoUrl, task.getResult().toString(), mFirebaseUser.getUid(),
                                                ServerValue.TIMESTAMP, receiverId, MainActivity.userType);
                                messagesRef.child(messagelinkKey).child(key)
                                        .setValue(manongMessage);
                            }
                });

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

    private String convertNumber(Integer number) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(number);
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manong_message_menu, menu);
        MenuItem item = menu.findItem(R.id.message);
        if (isConversationOff != null && isConversationOff.equals("yes")) {
            updateMenuUi(true, item);
        }else {
            disabledChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(messagelinkKey)) {
                        updateMenuUi(true, item);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.message:
                String title = (String) item.getTitle();
                showAlertDialog(title, item);
                return true;
            case R.id.call:
                // call provider
                if (providerPhoneNumber != null) {
                    // make a phone call
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MessageProviderActivity.this, new String[]{
                                Manifest.permission.CALL_PHONE
                        }, ProviderProfileActivity.PERMISSION_REQUEST_CODE);
                    }

                    makePhoneCall();

                }else {
                    Toast.makeText(this, "Service Provider doesn't have Phone Number.", Toast.LENGTH_LONG).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makePhoneCall() {
        if (providerPhoneNumber != null) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + providerPhoneNumber));
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "Please allow the permission in settings to make a phone call.", Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
            dialog.setMessage("Call " + providerPhoneNumber + "?");
            dialog.setPositiveButton("CALL", (dialogInterface, i) -> startActivity(intent));
            dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog outDialog = dialog.create();
            outDialog.show();
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
        }else {
            Toast.makeText(this, "Service Provider doesn't have Phone Number.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ProviderProfileActivity.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            }else {
                Toast.makeText(this, "You can allow the call permission anytime request in the settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showAlertDialog(String title, MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
        dialog.setTitle(title);
        final String[] message = {""};
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("YES", (dialogInterface, i) -> {
            if (title.equalsIgnoreCase("chat off")) {
                disableChatConvo(item);
            }else {
                enableChatConvo(item);
            }
        });
        if (title.equalsIgnoreCase("chat off")) {
            message[0] = "Are you sure to turn off your conversation?";
        }else {
            message[0] = "Are you sure to turn on your conversation?";
        }
        dialog.setMessage(message[0]);
        AlertDialog outDialog = dialog.create();
        outDialog.show();
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorControlActivated));
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorControlActivated));
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void disableChatConvo(MenuItem item) {
        if (mFirebaseUser != null) {
            disabledChatRef.child(messagelinkKey).setValue(mFirebaseUser.getUid())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ManongMessage manongMessage = new ManongMessage("Turned off your conversation.", mFirebaseUser.getDisplayName(),
                                    null, null, mFirebaseUser.getUid(), ServerValue.TIMESTAMP, providerId,
                                    MainActivity.userType);
                            manongMessage.setTurnedOff(true);
                            messagesRef.child(messagelinkKey).push().setValue(manongMessage).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    updateButtonUi(true);
                                    updateMenuUi(true, item);
                                    Toast.makeText(this, "Your conversation is now off.", Toast.LENGTH_LONG).show();
                                }else if (task1.getException() != null) {
                                    Toast.makeText(this, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }else if (task.getException() != null) {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void enableChatConvo(MenuItem item) {
        disabledChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(messagelinkKey) && dataSnapshot.child(messagelinkKey).getValue(String.class) != null &&
                        dataSnapshot.child(messagelinkKey).getValue(String.class).equals(mFirebaseUser.getUid())) {
                    // This user turned off the conversation.
                    // Now the user will turn on the conversation.
                    dataSnapshot.getRef().child(messagelinkKey).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    ManongMessage manongMessage = new ManongMessage("Turned on your conversation.", mFirebaseUser.getDisplayName(),
                                            null, null, mFirebaseUser.getUid(), ServerValue.TIMESTAMP, providerId,
                                            MainActivity.userType);
                                    manongMessage.setTurnedOff(false);
                                    messagesRef.child(messagelinkKey).push().setValue(manongMessage).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            updateButtonUi(false);
                                            updateMenuUi(false, item);
                                            Toast.makeText(MessageProviderActivity.this, "Your conversation is now on.", Toast.LENGTH_LONG).show();
                                        }else if (task1.getException() != null) {
                                            Toast.makeText(MessageProviderActivity.this, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else if (task.getException() != null) {
                                    Toast.makeText(MessageProviderActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }else {
                    Log.e("CHAT STATUS", "This user is not the one who turn off the conversation.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateButtonUi(boolean isDisabled) {
        if (isDisabled) {
            mMessageEditText.setEnabled(false);
            mAddMessageImageView.setEnabled(false);
        }else {
            mMessageEditText.setEnabled(true);
            mAddMessageImageView.setEnabled(true);
        }
    }

    private void updateMenuUi(boolean isDisabled, MenuItem item) {
        if (isDisabled) {
            item.setIcon(getResources().getDrawable(R.drawable.manong_chat_off));
            item.setTitle("Chat On");
        }else {
            item.setIcon(getResources().getDrawable(R.drawable.manong_chat_on));
            item.setTitle("Chat Off");
        }
    }

}
