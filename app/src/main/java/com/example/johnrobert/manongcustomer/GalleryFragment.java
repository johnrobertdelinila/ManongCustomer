package com.example.johnrobert.manongcustomer;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private DatabaseReference photosRef;
    private FirebaseRecyclerAdapter firebaseAdapter;

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView textNoPhoto;


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

        recyclerView = view.findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        progressBar = view.findViewById(R.id.progress_bar);
        textNoPhoto = view.findViewById(R.id.text_no_photo);

        if (ProviderProfileActivity.sakuChan != null) {
            progressBar.getIndeterminateDrawable().setColorFilter(ProviderProfileActivity.sakuChan.getRgb(),
                    android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        if (providerId != null) {
            photosRef = ManongActivity.providerRef.child(providerId).child("photos");

            DatabaseReference checkRef = ManongActivity.providerRef.child(providerId);
            // Checking if the user have photos
            checkRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("photos")) {
                        setupFirebaseRecyclerView(photosRef);

                        textNoPhoto.setVisibility(View.GONE);
                        recyclerView.setVisibility(RecyclerView.VISIBLE);

                        checkRef.removeEventListener(this);
                    }else {
                        textNoPhoto.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.progress_bar).setVisibility(RelativeLayout.VISIBLE);
                        recyclerView.setVisibility(RecyclerView.VISIBLE);

                        progressBar.setVisibility(View.GONE);
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
                holder.itemView.setOnClickListener(view -> showFullScreenImage(photo.getUrl(), view));
                try {
                    Glide.with(activity.getApplicationContext())
                            .load(photo.url)
                            .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(true))
//                        .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .skipMemoryCache(true).override(800, 800))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.temp_image.setVisibility(LoaderImageView.INVISIBLE);
                                    holder.image.setImageDrawable(resource);
                                    return false;
                                }
                            })
                            .into(holder.image);
                }catch (Exception e) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
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

    private void showFullScreenImage(String url, View view) {

        Intent intent = new Intent(activity, FullScreenImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(FullScreenImageActivity.IMAGE_URL_KEY, url);
        intent.putExtras(bundle);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent);
        } else {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(activity, view, "shared");
            activity.startActivity(intent, options.toBundle());
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

}
