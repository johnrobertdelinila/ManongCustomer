package com.example.johnrobert.manongcustomer;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;

public class FullScreenImageRequestActivity extends AppCompatActivity {

    public final static String IMAGE_URL_REQUEST_KEY = "image_url";

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_request);

        imageView = findViewById(R.id.image);
        String image_url = getIntent().getStringExtra(IMAGE_URL_REQUEST_KEY);
        if (image_url != null) {
            Glide.with(this)
                    .asDrawable()
                    .load(image_url)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                        }
                    });
        }

        imageView.setOnClickListener(view -> onBackPressed());

    }
}
