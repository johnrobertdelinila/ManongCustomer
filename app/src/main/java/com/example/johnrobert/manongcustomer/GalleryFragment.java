package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    private Activity activity;
    private Context context;
    private String providerId;
    private String providerPhotoUrl;

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = mDatabase.getReference();
    private DatabaseReference providerRef = rootRef.child("Providers");
    private DatabaseReference photosRef;

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private ProgressBar progressBar;


    public GalleryFragment() {
        // Required empty public constructor
    }

    public class Photos {
        String key, url;

        public Photos(){}

        public Photos(String key, String url) {
            this.key = key;
            this.url = url;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        LoaderImageView temp_image;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.implementation_imge);
            temp_image = itemView.findViewById(R.id.implementation_imge_temp);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        activity = getActivity();
        context = getContext();
        providerId = getArguments().getString("providerId");
        providerPhotoUrl = getArguments().getString("providerPhotoUrl");

        recyclerView = view.findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        progressBar = view.findViewById(R.id.progress_bar);

        if (providerPhotoUrl != null) {
            setProgressbarColor(providerPhotoUrl);
        }else {
            setUpProgressBar();
        }

        if (providerId != null) {
            photosRef = providerRef.child(providerId).child("photos");

            DatabaseReference checkRef = providerRef.child(providerId);
            // Checking if the user have photos
            checkRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("photos")) {
                        setupFirebaseRecyclerView(photosRef);

                        view.findViewById(R.id.loading_container).setVisibility(RelativeLayout.GONE);
                        recyclerView.setVisibility(RecyclerView.VISIBLE);
                        checkRef.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        return view;
    }

    private void setUpProgressBar() {
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
                setProgressbarColor(photoURL);
            }
        });
    }

    private void setupFirebaseRecyclerView(DatabaseReference photosRef) {

        SnapshotParser<Photos> photoParser = snapshot -> {
            Photos photo = new Photos();
            photo.setKey(snapshot.getKey());
            photo.setUrl(snapshot.getValue(String.class));
            return photo;
        };

        FirebaseRecyclerOptions<Photos> options = new FirebaseRecyclerOptions.Builder<Photos>()
                .setQuery(photosRef, photoParser)
                .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<Photos, PhotoViewHolder>(options) {
            @NonNull
            @Override
            public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(activity).inflate(R.layout.layout_photo, viewGroup, false);
                return new PhotoViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PhotoViewHolder holder, int position, @NonNull Photos photo) {
                holder.temp_image.resetLoader();
                holder.itemView.setOnClickListener(view -> showFullScreenImage(photo.getUrl()));
                Glide.with(activity.getApplicationContext())
                        .load(photo.url)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.temp_image.setVisibility(LoaderImageView.INVISIBLE);

                                firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                    @Override
                                    public void onItemRangeInserted(int positionStart, int itemCount) {
                                        super.onItemRangeInserted(positionStart, itemCount);
//                                        int friendlyMessageCount = firebaseAdapter.getItemCount();
//                                        int lastVisiblePosition = gridLayoutManager.findLastCompletelyVisibleItemPosition();
//                                        // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
//                                        // to the bottom of the list to show the newly added message.
//                                        if (lastVisiblePosition == -1 ||
//                                                (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
//                                            recyclerView.scrollToPosition(positionStart);
//                                        }
                                        recyclerView.scrollToPosition(positionStart);
                                    }
                                });

                                return false;
                            }
                        })
                        .into(holder.image);
            }
        };

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        final float spaceSize = ScreenUtil.dp2px(16, activity);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int adapterPosition = parent.getChildViewHolder(view).getAdapterPosition();
                GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
                int spanSize = spanSizeLookup.getSpanSize(adapterPosition);
                if (spanSize == 2) {
                    return;
                }
                int spanIndex = spanSizeLookup.getSpanIndex(adapterPosition, gridLayoutManager.getSpanCount());
                if (spanIndex == 0) {
                    outRect.set((int) spaceSize, (int) spaceSize, ((int) (spaceSize / 2)), 0);
                } else {
                    outRect.set(((int) (spaceSize / 2)), (int) spaceSize, (int) spaceSize, 0);
                }
            }
        });

        recyclerView.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();

    }

    private void showFullScreenImage(String url) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(activity);
        View view = getLayoutInflater().inflate(R.layout.layout_photo_dialog, null);
        ImageView imageView = view.findViewById(R.id.image);
        Glide.with(activity.getApplicationContext()).load(url).into(imageView);
        dialog.setView(view);
        dialog.setPositiveButton("CLOSE", (dialogInterface, i) -> {});

        android.app.AlertDialog dialogg = dialog.create();
        dialogg.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAdapter != null) {
            firebaseAdapter.stopListening();
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
    public void onResume() {
        super.onResume();
        if (firebaseAdapter != null) {
            firebaseAdapter.startListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAdapter != null) {
            firebaseAdapter.startListening();
        }
    }

    private void setProgressbarColor(String url) {

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
                                    }

                                    if (vibrantSwatch == null) {
                                        return;
                                    }
                                    progressBar.getIndeterminateDrawable().setColorFilter(vibrantSwatch.getRgb(),
                                            android.graphics.PorterDuff.Mode.MULTIPLY);

                                });
                    }
                });
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
