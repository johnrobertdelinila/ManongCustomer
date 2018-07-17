package com.example.johnrobert.manongcustomer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ProviderProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final int PERMISSION_REQUEST_CODE = 1996;

    String sakurako1 = "http://img2-ak.lst.fm/i/u/arO/3513dabe28dd41a1fbb9d4f1a6048ec7";
    String sakurako2 = "https://terminal-clipkit-cdn.s3-ap-northeast-1.amazonaws.com/articles/images/000/003/250/original/8a8dce84-75b3-43be-a56e-6397b671668b.jpg";
    String sakurako3 = "https://www.yunikavision.jp/topics/images/180201_Sakurako%20Ohara_A.jpg";
    String sakurako4 = "https://lastfm-img2.akamaized.net/i/u/300x300/9b6be9204080e5bf7998d42310f33c95.png";
    String sakurako5 = "https://instagram.fmnl6-1.fna.fbcdn.net/vp/2018d60c37c1f1e2114570242de1be99/5BCDDA86/t51.2885-15/e35/31270327_410375222768921_4762962046562074624_n.jpg?efg=eyJ1cmxnZW4iOiJ1cmxnZW5fZnJvbV9pZyJ9";

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CircleImageView providerImageView;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private AppBarLayout appBarLayout;
    private RelativeLayout temp_layout;
    private MaterialRatingBar providerRating;

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    private String providerPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_profile);

        String providerId = getIntent().getStringExtra("providerId");
        String providerDisplayName = getIntent().getStringExtra("providerDisplayName");
        String providerPhotoUrl = getIntent().getStringExtra("providerPhotoUrl");
        ProviderProfile providerProfile = (ProviderProfile) getIntent().getSerializableExtra("providerProfile");
        providerPhoneNumber = getIntent().getStringExtra("providerPhoneNumber");
        String requestKey = getIntent().getStringExtra("requestKey");
        String serviceKey = getIntent().getStringExtra("serviceKey");

        fab = findViewById(R.id.fab_call);
        appBarLayout = findViewById(R.id.appBar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        providerImageView = findViewById(R.id.providerImage);
        temp_layout = findViewById(R.id.loading_container);
        providerRating = findViewById(R.id.service_provider_ratingbar);

        setupToolbar();

        if (providerDisplayName != null && providerPhoneNumber != null) {
            // Set title
            ProviderProfileActivity.this.setTitle(providerDisplayName);
        } else {
            fetchProviderProfile(providerId);
        }

        if (providerPhotoUrl != null) {
            configCollapsingLayout(providerPhotoUrl);
        } else {
            fetchProviderProfile(providerId);
        }

        Bundle bundle = new Bundle();
        bundle.putString("providerId", providerId);
        if (providerPhotoUrl != null) {
            bundle.putString("providerPhotoUrl", providerPhotoUrl);
        }

        GalleryFragment galleryFragment = new GalleryFragment();
        galleryFragment.setArguments(bundle);

        AboutFragment aboutFragment = new AboutFragment();
        bundle.putSerializable("providerProfile", providerProfile);
        bundle.putString("requestKey", requestKey);
        bundle.putString("serviceKey", serviceKey);
        aboutFragment.setArguments(bundle);

        ViewPager mViewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(aboutFragment, "Profile");
        mViewPagerAdapter.addFragment(galleryFragment, "Gallery");
        mViewPager.setAdapter(mViewPagerAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        appBarLayout.addOnOffsetChangedListener(this);

        fab.setOnClickListener(view -> {
            if (providerPhoneNumber != null) {
                // make a phone call
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProviderProfileActivity.this, new String[]{
                            Manifest.permission.CALL_PHONE
                    }, PERMISSION_REQUEST_CODE);
                }

                makePhoneCall();

            }else {
                Toast.makeText(this, "Service Provider doesn't have Phone Number.", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            }else {
                Toast.makeText(this, "You can allow the permission request in the settings.", Toast.LENGTH_LONG).show();
            }
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
            startActivity(intent);
        }else {
            Toast.makeText(this, "Service Provider doesn't have Phone Number.", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchProviderProfile(String providerId) {
        getUserRecord(providerId).addOnCompleteListener(task -> {
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
            String photoURL = (String) task.getResult().get("photoURL");
            providerPhoneNumber = (String) task.getResult().get("phoneNumber");

            if (displayName != null) {
                // Set title
                ProviderProfileActivity.this.setTitle(displayName);
            }else {
                ProviderProfileActivity.this.setTitle("Service Provider");
            }

            if (photoURL != null) {
                if (photoURL.startsWith("https://graph.facebook.com")) {
                    photoURL = photoURL.concat("?height=130");
                }
                configCollapsingLayout(photoURL);
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    temp_layout.setElevation(0);
                }
                temp_layout.setVisibility(RelativeLayout.GONE);
            }

        });
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

//        configCollapsingLayout(sakurako5);
    }

    private void configCollapsingLayout(String photoUrl) {

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(photoUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        Palette.from(bitmap)
                                .generate(palette -> {
                                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                    Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
                                    Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();

                                    Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                                    Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();
                                    Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();

                                    boolean isNoVibrantSwatch = false;

                                    if (vibrantLightSwatch == null) {
                                        vibrantSwatch = mutedSwatch;
                                        vibrantLightSwatch = mutedLightSwatch;
                                        if (palette.getVibrantSwatch() != null) {
                                            mutedLightSwatch = palette.getVibrantSwatch();
                                        }else if (vibrantDarkSwatch != null) {
                                            mutedLightSwatch = vibrantDarkSwatch;
                                        }else if (mutedDarkSwatch != null) {
                                            mutedLightSwatch = mutedDarkSwatch;
                                        }else {
                                            mutedLightSwatch = palette.getVibrantSwatch();
                                        }
                                        isNoVibrantSwatch = true;
                                    }else if (vibrantSwatch == null) {
                                        vibrantSwatch = mutedSwatch;
                                        vibrantLightSwatch = mutedLightSwatch;
                                        if (mutedSwatch != null) {
                                            mutedLightSwatch = mutedSwatch;
                                        }else if (vibrantDarkSwatch != null) {
                                            mutedLightSwatch = vibrantDarkSwatch;
                                        }else if (mutedDarkSwatch != null) {
                                            mutedLightSwatch = mutedDarkSwatch;
                                        }else {
                                            mutedLightSwatch = palette.getLightVibrantSwatch();
                                        }
                                        isNoVibrantSwatch = true;
                                    }

                                    if (mutedLightSwatch == null) {
                                        mutedLightSwatch = palette.getLightMutedSwatch();
                                        if (mutedLightSwatch == null) {
                                            mutedLightSwatch = mutedSwatch;
                                            if (mutedLightSwatch == null) {
                                                mutedLightSwatch = mutedDarkSwatch;
                                                if (mutedLightSwatch == null) {
                                                    mutedLightSwatch = vibrantSwatch;
                                                }
                                            }
                                        }
                                    }

                                    if (vibrantSwatch == null && mutedLightSwatch == null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            temp_layout.setElevation(0);
                                        }
                                        temp_layout.setVisibility(RelativeLayout.GONE);
                                        return;
                                    }

                                    collapsingToolbarLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    collapsingToolbarLayout.setContentScrimColor(vibrantLightSwatch.getRgb());
                                    if (Build.VERSION.SDK_INT >= 21) {
                                        getWindow().setStatusBarColor(vibrantSwatch.getRgb()); //status bar or the time bar at the top
                                    }

                                    tabLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    appBarLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    tabLayout.setTabRippleColor(ColorStateList.valueOf(vibrantSwatch.getRgb()));
                                    tabLayout.setSelectedTabIndicatorColor(vibrantLightSwatch.getBodyTextColor());
                                    tabLayout.setTabTextColors(vibrantLightSwatch.getTitleTextColor(), vibrantLightSwatch.getBodyTextColor());

                                    toolbar.setTitleTextColor(vibrantLightSwatch.getBodyTextColor());
                                    Drawable backArrow = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_twotone_arrow_back_24px);
//                                    backArrow.setColorFilter(vibrantLightSwatch.getBodyTextColor(), PorterDuff.Mode.SRC_ATOP);
                                    backArrow.setColorFilter(new PorterDuffColorFilter(vibrantLightSwatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY));
                                    toolbar.setNavigationIcon(backArrow);
                                    toolbar.setNavigationOnClickListener(view -> onBackPressed());

                                    fab.setBackgroundTintList(ColorStateList.valueOf(mutedLightSwatch.getRgb()));
                                    fab.setColorFilter(mutedLightSwatch.getTitleTextColor());
                                    if (isNoVibrantSwatch) {
                                        if (palette.getLightVibrantSwatch() != null) {
                                            fab.setRippleColor(palette.getLightVibrantSwatch().getRgb());
                                        }else {
                                            fab.setRippleColor(palette.getLightMutedSwatch().getRgb());
                                        }
                                    }else {
                                        fab.setRippleColor(vibrantSwatch.getRgb());
                                    }

                                    if (palette.getVibrantSwatch() != null) {
                                        providerRating.setSupportProgressTintList(ColorStateList.valueOf(palette.getVibrantSwatch().getRgb()));
                                        providerRating.setSupportSecondaryProgressTintList(ColorStateList.valueOf(mutedLightSwatch.getTitleTextColor()));
                                    }else if (palette.getMutedSwatch() != null) {
                                        providerRating.setSupportProgressTintList(ColorStateList.valueOf(palette.getMutedSwatch().getRgb()));
                                        providerRating.setSupportSecondaryProgressTintList(ColorStateList.valueOf(mutedLightSwatch.getTitleTextColor()));
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        temp_layout.setElevation(0);
                                    }
                                    temp_layout.setVisibility(RelativeLayout.GONE);

                                });
                        providerImageView.setImageBitmap(bitmap);
                    }
                });

    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
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

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int totalScrollRange = appBarLayout.getTotalScrollRange();
        int value = Math.abs(i);
        float result = (float) value / (float) totalScrollRange;

        fab.animate().scaleX(1 - result).scaleY(1 - result).setDuration(0).start();
    }

}
