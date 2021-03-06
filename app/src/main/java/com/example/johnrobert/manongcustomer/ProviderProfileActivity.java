package com.example.johnrobert.manongcustomer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ProviderProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    public static final int PERMISSION_REQUEST_CODE = 1996;

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
    private View scrim;

    private String providerPhoneNumber;
    private int kulayNgAlertDialogButton = 0;
    private String providerDisplayName;
    private String providerId;
    private String providerPhotoUrl;
    private Boolean isWithoutTransition;
    public static Palette.Swatch sakuChan;
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_profile);

        sakuChan = null;

        providerPhotoUrl = getIntent().getStringExtra("providerPhotoUrl");
        providerId = getIntent().getStringExtra("providerId");

        if (providerPhotoUrl != null) {
            configCollapsingLayout(providerPhotoUrl);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                postponeEnterTransition();
            }
        } else {
            isWithoutTransition = true;
            fetchProviderProfile(providerId);
        }

        scrim = findViewById(R.id.scrim);

        providerDisplayName = getIntent().getStringExtra("providerDisplayName");

        providerImageView = findViewById(R.id.providerImage);
        fab = findViewById(R.id.fab_call);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        tabLayout = findViewById(R.id.tabs);
        appBarLayout = findViewById(R.id.appBar);
        temp_layout = findViewById(R.id.loading_container);
        providerRating = findViewById(R.id.service_provider_ratingbar);

        setupToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(new android.transition.Transition.TransitionListener() {
                @Override
                public void onTransitionStart(android.transition.Transition transition) {
                    Log.e("TRANSITION", "START");
                }

                @Override
                public void onTransitionEnd(android.transition.Transition transition) {
                    setUpAfterEnterTask();
                    Log.e("TRANSITION", "END");
                }

                @Override
                public void onTransitionCancel(android.transition.Transition transition) {

                }

                @Override
                public void onTransitionPause(android.transition.Transition transition) {

                }

                @Override
                public void onTransitionResume(android.transition.Transition transition) {
                    Log.e("TRANSITION", "RESUME");
                }
            });
        }

    }


    private void setUpAfterEnterTask() {

        if (providerDisplayName != null) {
            toolbar.setTitle(providerDisplayName);
        }

        providerPhoneNumber = getIntent().getStringExtra("providerPhoneNumber");
        String requestKey = getIntent().getStringExtra("requestKey");
        String serviceKey = getIntent().getStringExtra("serviceKey");

        Bundle bundle = new Bundle();
        bundle.putString("providerId", providerId);

        GalleryFragment galleryFragment = new GalleryFragment();
        galleryFragment.setArguments(bundle);

        AboutFragment aboutFragment = new AboutFragment();
        bundle.putString("requestKey", requestKey);
        bundle.putString("serviceKey", serviceKey);

        aboutFragment.setArguments(bundle);

        ViewPager mViewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(aboutFragment, "Profile");
        mViewPagerAdapter.addFragment(galleryFragment, "Gallery");
        mViewPager.setAdapter(mViewPagerAdapter);

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

        new Handler().postDelayed(() -> fab.show(), 810);

        mViewPager.setVisibility(View.VISIBLE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            }else {
                Toast.makeText(this, "You can allow the call permission anytime request in the settings.", Toast.LENGTH_LONG).show();
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
            dialog.setMessage("Call " + providerPhoneNumber + "?");
            dialog.setPositiveButton("CALL", (dialogInterface, i) -> startActivity(intent));
            dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog outDialog = dialog.create();
            outDialog.show();
            if (kulayNgAlertDialogButton == 0) {
                kulayNgAlertDialogButton = getResources().getColor(R.color.colorPrimaryDark);
            }
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(kulayNgAlertDialogButton);
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(kulayNgAlertDialogButton);
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
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
            providerPhotoUrl = photoURL;
            providerPhoneNumber = (String) task.getResult().get("phoneNumber");

            if (displayName != null) {
                // Set title
                toolbar.setTitle(displayName);
            }else {
                toolbar.setTitle("Service Provider");
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
                providerImageView.setImageDrawable(getResources().getDrawable(R.drawable.manong_customer_logo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                    startPostponedEnterTransition();
                }else {
                    setUpAfterEnterTask();
                }
            }

        });
    }

    private void configCollapsingLayout(String photoUrl) {

        Glide.with(getApplicationContext())
                .asBitmap()
                .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                .load(photoUrl)
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.e("PHOTO", "FAIL ANALYZING");
                        Glide.with(getApplicationContext())
                                .load(photoUrl)
                                .apply(new RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE).skipMemoryCache(true))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            temp_layout.setElevation(0);
                                        }
                                        temp_layout.setVisibility(RelativeLayout.GONE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                                            startPostponedEnterTransition();
                                        }else {
                                            setUpAfterEnterTask();
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            temp_layout.setElevation(0);
                                        }
                                        temp_layout.setVisibility(RelativeLayout.GONE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                                            startPostponedEnterTransition();
                                        }else {
                                            setUpAfterEnterTask();
                                        }
                                        return false;
                                    }
                                })
                                .into(providerImageView);
                    }

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

                                    if (vibrantSwatch == null || mutedLightSwatch == null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            temp_layout.setElevation(0);
                                        }
                                        temp_layout.setVisibility(RelativeLayout.GONE);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                                            startPostponedEnterTransition();
                                        }else {
                                            setUpAfterEnterTask();
                                        }

                                        return;
                                    }

                                    collapsingToolbarLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    collapsingToolbarLayout.setContentScrimColor(vibrantLightSwatch.getRgb());
                                    kulayNgAlertDialogButton = vibrantSwatch.getRgb();
                                    if (Build.VERSION.SDK_INT >= 21) {
                                        getWindow().setStatusBarColor(vibrantSwatch.getRgb()); //status bar or the time bar at the top
                                    }

                                    tabLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    appBarLayout.setBackgroundColor(vibrantLightSwatch.getRgb());
                                    tabLayout.setTabRippleColor(ColorStateList.valueOf(vibrantSwatch.getRgb()));
                                    tabLayout.setSelectedTabIndicatorColor(vibrantLightSwatch.getBodyTextColor());
                                    tabLayout.setTabTextColors(vibrantLightSwatch.getBodyTextColor(), vibrantLightSwatch.getTitleTextColor());

                                    toolbar.setTitleTextColor(vibrantLightSwatch.getBodyTextColor());
                                    Drawable backArrow = ContextCompat.getDrawable(getApplicationContext(), R.drawable.manong_back);
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
                                    providerImageView.setImageBitmap(bitmap);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isWithoutTransition == null) {
                                        startPostponedEnterTransition();
                                    }else {
                                        setUpAfterEnterTask();
                                    }

                                    Palette.Swatch nakitaikurai = palette.getVibrantSwatch();

                                    if (nakitaikurai == null) {
                                        nakitaikurai = palette.getMutedSwatch();
                                        if (nakitaikurai == null) {
                                            nakitaikurai = palette.getLightVibrantSwatch();
                                            if (nakitaikurai == null) {
                                                nakitaikurai = palette.getLightMutedSwatch();
                                            }
                                        }
                                    }

                                    if (nakitaikurai != null) {
                                        sakuChan = nakitaikurai;
                                    }

                                });
                    }
                });

    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.manong_back);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private Task<Map<String, Object>> getUserRecord(String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        return ManongActivity.mFunctions.getHttpsCallable("getUserRecord").call(data)
                .continueWith(task -> (Map<String, Object>) task.getResult().getData());
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int totalScrollRange = appBarLayout.getTotalScrollRange();
        int value = Math.abs(i);
        float result = (float) value / (float) totalScrollRange;

        fab.animate().scaleX(1 - result).scaleY(1 - result).setDuration(0).start();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (!isBackPressed) {
            scrim.animate().alpha(0.6f).setDuration(0);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        scrim.animate().alpha(0).setDuration(0);
        super.onResume();
    }

    @Override
    protected void onStart() {
        scrim.animate().alpha(0).setDuration(0);
        super.onStart();
    }



}
