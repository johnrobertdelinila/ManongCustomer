package com.example.johnrobert.manongcustomer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.card.MaterialCardView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.shapeofview.shapes.CutCornerView;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ServiceDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public final static String RESULT_EXTRA_ITEM_ID = "RESULT_EXTRA_ITEM_ID";
    public static final String INTENT_EXTRA_ITEM = "item";
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 2000;

    public static boolean isDone = false;

    private ImplementationItem item;
    private GoogleMap mMap;
    private FrameLayout checklistContainer;
    private RelativeLayout mapContainer;
    private ImageView transparentImage;
    private NestedScrollView rootView;
    private MaterialCardView cardMap;
    private MaterialButton btnMap;
    private MaterialAnimatedSwitch switchLocation;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    // Map
    public GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    private Marker marker;
    private String locationName = null;

    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    rootView.requestDisallowInterceptTouchEvent(true);
                    // Disable touch on transparent view
                    return false;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    rootView.requestDisallowInterceptTouchEvent(false);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    rootView.requestDisallowInterceptTouchEvent(true);
                    return false;

                default:
                    return true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            setContentView(R.layout.activity_service_detail);

            mapContainer = findViewById(R.id.map_container);
            checklistContainer = findViewById(R.id.checklist_container);
            item = getIntent().getParcelableExtra(INTENT_EXTRA_ITEM);
            collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
            toolbar.setNavigationOnClickListener(view -> onBackPressed());

            Bitmap bitmapImage = BitmapFactory.decodeResource(getResources(), item.imageRes);
            Palette.from(bitmapImage).generate(palette -> {
                if (palette == null) {
                    return;
                }

                Palette.Swatch pickedColor = palette.getDarkVibrantSwatch();
                if (pickedColor == null) {
                    pickedColor = palette.getVibrantSwatch();
                    if (pickedColor == null) {
                        pickedColor = palette.getLightMutedSwatch();
                    }
                }

                if (pickedColor != null) {

                    findViewById(R.id.scrim_color).setBackgroundColor(pickedColor.getRgb());
                    findViewById(R.id.scrim_color).getBackground().setAlpha(64);

                    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Expanded);
                }
            });

        }catch (Exception e) {
            Log.e("ServiceDetailActivity", e.getMessage());
        }

        setupViews();

        transparentImage = findViewById(R.id.transparent_image);
        rootView = findViewById(R.id.content_dynamic_durations);
        cardMap = findViewById(R.id.async_card);
        btnMap = findViewById(R.id.btn_toggle_map);
        switchLocation = findViewById(R.id.switch_location);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupViews() {

        ImageView imageView = findViewById(R.id.detail_image);

//        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Expanded);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.Collapsed);

        ImageView allShareRowImage = findViewById(R.id.all_element_share_image);

        imageView.setImageResource(item.imageRes);

//        Bitmap bitmap = ResourceUtil.getBitmap(this, item.imageRes);
//        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
//        circularBitmapDrawable.setCircular(true);

        allShareRowImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));
        TransitionUtils.setSharedElementEnterTransitionEndListenerCompat(getWindow(), transition -> {
            collapsingToolbarLayout.setTitleEnabled(true);
            collapsingToolbarLayout.setTitle(item.title);
            setUpMap();
            checklistContainer.setVisibility(FrameLayout.VISIBLE);
            mapContainer.setVisibility(FrameLayout.VISIBLE);
        });

        ((TextView) findViewById(R.id.all_content_element_share_text)).setText(item.title + " checklist ");

        setupAllElementShared(allShareRowImage);

    }

    private void setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    private void setupAllElementShared(final ImageView rowImage) {
        final MaterialCardView cardView = findViewById(R.id.all_element_share_card);
        cardView.setOnClickListener(v -> {

            Intent intent = new Intent(ServiceDetailActivity.this, ChecklistActivity.class);
            Bundle bundle = new Bundle();
            Service service = item.service;
            // Dummy Lat Lng
            double latitude = 16.6325;
            double longtitude = 120.3181;

            if (mLastLocation != null) {
                service.setLatitude(mLastLocation.getLatitude());
                service.setLongtitude(mLastLocation.getLongitude());
            }else {
                service.setLatitude(null);
                service.setLongtitude(null);
            }
            if (locationName != null) {
                service.setLocationName(locationName);
            }
            bundle.putString("checklist_title", item.title + " checklist");
            bundle.putSerializable("service", service);
            intent.putExtras(bundle);

            ActivityOptionsCompat optionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(ServiceDetailActivity.this, cardView, getString(R.string.transition_name_all_element_share));
            ActivityCompat.startActivity(ServiceDetailActivity.this, intent, optionsCompat.toBundle());

        });
    }


    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }


    void setResultAndFinish() {
        final Intent resultData = new Intent();
        resultData.putExtra(RESULT_EXTRA_ITEM_ID, item.itemId);
        setResult(RESULT_OK, resultData);
        ActivityCompat.finishAfterTransition(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.manong_map_style));

            if (!success) {
                Log.e("ServiceDetailActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("ServiceDetailActivity", "Can't find style. Error: ", e);
        }
//        findViewById(R.id.container_cut_button).setVisibility(CutCornerView.VISIBLE);

        btnMap.setOnClickListener(new View.OnClickListener() {
            boolean isSmall = true;
            @Override
            public void onClick(View view) {
                if (isSmall) {
                    btnMap.setText("MINIMIZE MAP");
                    ViewCompat
                            .animate(cardMap)
                            .scaleX(1.0f)
                            .setDuration(275)
                            .setStartDelay(0)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                    ViewCompat
                            .animate(cardMap)

                            .scaleY(1.0f)
                            .setDuration(350)
                            .setStartDelay(25) // 開始をずらす
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .setListener(new ViewPropertyAnimatorListener() {
                                @Override
                                public void onAnimationStart(View view) {

                                }

                                @Override
                                public void onAnimationEnd(View view) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        cardMap.setElevation(4.0f);
                                    }
                                }

                                @Override
                                public void onAnimationCancel(View view) {

                                }
                            })
                            .start();

                } else {
                    btnMap.setText("MAXIMIZE MAP");
                    ViewCompat
                            .animate(cardMap)
                            .scaleX(0.5f)
                            .setDuration(325)
                            .setStartDelay(50)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                    ViewCompat
                            .animate(cardMap)
                            .scaleY(0.5f)
                            .setStartDelay(0)
                            .setDuration(325)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .setListener(new ViewPropertyAnimatorListener() {
                                @Override
                                public void onAnimationStart(View view) {

                                }

                                @Override
                                public void onAnimationEnd(View view) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        cardMap.setElevation(2.0f);
                                    }
                                }

                                @Override
                                public void onAnimationCancel(View view) {

                                }
                            })
                            .start();
                }
                isSmall = !isSmall;
            }
        });

        transparentImage.setOnTouchListener(touchListener);

        switchLocation.setOnCheckedChangeListener(isChecked -> {
            if (isChecked) {
                setUpLocation();
            }else {
                if (googleApiClient != null && googleApiClient.isConnected()) {
                    googleApiClient.disconnect();
                }
                mLastLocation = null;
                if (marker != null) {
                    marker.remove();
                }
                mMap.animateCamera(CameraUpdateFactory.zoomTo(4.0f));
            }
        });

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        markerOptions.title("Pin location");

        mMap.setOnMapClickListener(latLng -> {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                markerOptions.position(latLng);
                if (marker != null) {
                    marker.remove();
                }
                marker = mMap.addMarker(markerOptions);
                mLastLocation.setLatitude(latLng.latitude);
                mLastLocation.setLongitude(latLng.longitude);
            }else {
                Snackbar.make(findViewById(R.id.content_dynamic_durations), "You need to turn on location to pin a map.", Snackbar.LENGTH_LONG).show();
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("PH").build();
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(16.246178420796923, 120.30559760461585),
                new LatLng(16.95508074477375, 120.58643561731117)));
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Toast.makeText(ServiceDetailActivity.this, place.getAddress(), Toast.LENGTH_SHORT).show();
                locationName = (String) place.getAddress();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(ServiceDetailActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ServiceDetailActivity.this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            if (isGooglePlayServices()) {
                buildGoogleApiClient();
                setUpLocationRequest();
            }
        }
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(ServiceDetailActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private boolean isGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(ServiceDetailActivity.this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(ServiceDetailActivity.this, status, GOOGLE_PLAY_SERVICES_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, "Device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        } else {
            return true;
        }
    }

    private void setUpLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDone) {
            isDone = false;

            ManongActivity.isDoneFromService = true;
//            onBackPressed();

            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
            dialog.setTitle("Request");
            dialog.setMessage("You've just submitted a request. Do you want to view your request now?");
            dialog.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());
            dialog.setPositiveButton("YES", (dialogInterface, i) -> startActivity(new Intent(ServiceDetailActivity.this, ManongActivity.class)));
            AlertDialog outDialog = dialog.create();
            outDialog.show();
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    private void setLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            if (marker != null) {
                marker.remove();
            }

            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions.title("Your location");
            marker = mMap.addMarker(markerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGooglePlayServices()) {
                    buildGoogleApiClient();
                    setUpLocationRequest();
                    setLocation();
                }
            }else {
                Toast.makeText(this, "You can allow the permission request in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        setLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }
}
