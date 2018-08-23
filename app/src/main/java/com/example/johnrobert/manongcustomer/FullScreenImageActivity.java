package com.example.johnrobert.manongcustomer;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;

public class FullScreenImageActivity extends AppCompatActivity {

    public final static String IMAGE_URL_KEY = "image_url";

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageView = findViewById(R.id.image);
        String image_url = getIntent().getStringExtra(IMAGE_URL_KEY);

        if (image_url != null) {
            Glide.with(this)
                    .asDrawable()
                    .load(image_url)
                    .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                        }
                    });
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {

                private boolean isClosing = false;

                @Override public void onTransitionPause(Transition transition) {}
                @Override public void onTransitionResume(Transition transition) {}
                @Override public void onTransitionCancel(Transition transition) {}

                @Override public void onTransitionStart(Transition transition) {
                    if (isClosing) {
                        addCardCorners();
                    }
                }

                @Override public void onTransitionEnd(Transition transition) {
                    if (!isClosing) {
                        isClosing = true;
                        removeCardCorners();
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= 21 && ProviderProfileActivity.sakuChan != null) {
            getWindow().setStatusBarColor(ProviderProfileActivity.sakuChan.getRgb()); //status bar or the time bar at the top
        }

        imageView.setOnClickListener(view -> onBackPressed());

    }

    private void addCardCorners() {
        final CardView cardView = findViewById(R.id.card);
        cardView.setRadius(20f);
    }

    private void removeCardCorners() {
        final CardView cardView = findViewById(R.id.card);
        ObjectAnimator.ofFloat(cardView, "radius", 0f).setDuration(50).start();
    }

}
