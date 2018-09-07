package com.example.johnrobert.manongcustomer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class ManongActivity extends AppCompatActivity implements NavigationHost {

    private static final String more = "more";
    public static boolean isDoneFromService = false;
    private int isUserDisabled = 0;
    private String currentFragment = "services";
    public static Boolean jobs, messages, quotation;
    public static String homeAddress;

    public static FirebaseUser mUser = MainActivity.mAuth.getCurrentUser();

    public static DatabaseReference providerRef = MainActivity.rootRef.child("Providers");
    public static DatabaseReference requestRef = MainActivity.rootRef.child("Request");
    private ValueEventListener settingsListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.e("SETTINGS", "CHANGED!");
            quotation = dataSnapshot.child(getString(R.string.manong_quotations)).getValue(Boolean.class);
            messages = dataSnapshot.child(getString(R.string.manong_messages)).getValue(Boolean.class);
            jobs = dataSnapshot.child(getString(R.string.manong_jobs)).getValue(Boolean.class);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public static FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    private View scrim;
    private Toolbar toolbar;
    private NavigationIconClickListener customNavigation;
    private ImageView toolbarNavigationIcon;
    private Drawable closeIcon, openIcon;
    private LinearLayout backdropContainer;
    private ArrayList<View> allButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manong);

        mUser = MainActivity.mAuth.getCurrentUser();

        if (!isUserAuthenticated()) {
            return;
        }

        if (quotation != null || messages != null || jobs != null) {
            quotation = null; messages = null; jobs = null;
        }

        scrim = findViewById(R.id.scrim);
        closeIcon = getResources().getDrawable(R.drawable.manong_close_menu);
        openIcon = getResources().getDrawable(R.drawable.manong_logo_nav);

        setUpToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Add shape
            findViewById(R.id.service_grid).setBackground(getDrawable(R.drawable.manong_container_grid_background_shape));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scrim.setBackground(getDrawable(R.drawable.manong_scrim_background_shape));
        }

        // Set default Fragment
        if (savedInstanceState == null && !isDoneFromService) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new ServiceFragment())
                    .commit();
        }else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new RequestFragment())
                    .commit();

            String title = "request";
            currentFragment = title;
            toolbar.setTitle(title);
            setNewMarker(allButtons, backdropContainer, findViewById(R.id.nav_request_button));

            isDoneFromService = false;
        }
        // FIXME: PUTANG INA HINDI MAPALITAN UNG SHAPE

        scrim.setAlpha(0);
        scrim.setClickable(false);
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbarNavigationIcon = (ImageView) toolbar.getChildAt(1);

        this.setSupportActionBar(toolbar);

        customNavigation = new NavigationIconClickListener(this, findViewById(R.id.service_grid),
                new AccelerateDecelerateInterpolator(),
                openIcon,
                closeIcon,
                scrim,
                findViewById(R.id.container));

        toolbar.setNavigationOnClickListener(customNavigation);

        backdropContainer = findViewById(R.id.backdrop_container);
        allButtons = backdropContainer.getTouchables();

        for(View navBtn: allButtons) {
            navBtn.setOnClickListener(view -> {
                customNavigation.onClick(toolbarNavigationIcon);
                setNewMarker(allButtons, backdropContainer, view);
                String btnText = (String) ((MaterialButton) view).getText();

                if (btnText.equalsIgnoreCase("request") && !currentFragment.equalsIgnoreCase(btnText)) {
                    navigateFragment(new RequestFragment());
                    currentFragment = btnText;
                    toolbar.setTitle(btnText);
                }else if (btnText.equalsIgnoreCase("services") && !currentFragment.equalsIgnoreCase(btnText)) {
                    currentFragment = btnText;
                    navigateFragment(new ServiceFragment());
                    toolbar.setTitle(btnText);
                }else if (btnText.equalsIgnoreCase("inbox") && !currentFragment.equalsIgnoreCase(btnText)) {
                    navigateFragment(new MessageFragment());
                    currentFragment = btnText;
                    toolbar.setTitle(btnText);
                }else if (btnText.equalsIgnoreCase(more) && !currentFragment.equalsIgnoreCase(btnText)) {
                    navigateFragment(new MoreFragment());
                    currentFragment = btnText;
                    toolbar.setTitle(btnText);
                }else if (btnText.equalsIgnoreCase("notification") && !currentFragment.equalsIgnoreCase(btnText)) {
                    navigateFragment(new NotificationFragment());
                    currentFragment = btnText;
                    toolbar.setTitle(btnText);
                }


            });
        }

        scrim.setOnClickListener(view -> customNavigation.onClick(toolbarNavigationIcon));

        if (mUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private void navigateFragment(final Fragment fragment) {
        int defaultDelay = 345;
        if (currentFragment.equalsIgnoreCase("services"))
            defaultDelay = 350;
        if (isDoneFromService) {
            defaultDelay = 0;
            isDoneFromService = false;
        }
        new Handler().postDelayed(() -> navigateTo(fragment, false), defaultDelay);
    }

    private void setNewMarker(ArrayList<View> allButtons, LinearLayout backdropContainer, View excluded) {
        for(View button: allButtons) {
            if (button != excluded) {
                backdropContainer.getChildAt(backdropContainer.indexOfChild(button) + 1).setVisibility(View.GONE);
            }else {
                backdropContainer.getChildAt(backdropContainer.indexOfChild(button) + 1).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction();

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Initialize the menu
        getMenuInflater().inflate(R.menu.manong_toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:

                if (!currentFragment.equalsIgnoreCase(more)) {

                    customNavigation.onClickMore(toolbarNavigationIcon);
                    setNewMarker(allButtons, backdropContainer, findViewById(R.id.nav_more_button));
                    navigateFragment(new MoreFragment());
                    currentFragment = more;
                    toolbar.setTitle(more);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToLoginPage() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.customerRef.child(mUser.getUid()).child("settings").addListenerForSingleValueEvent(settingsListener);
        isUserAuthenticated();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser != null) {
            // Send the registration key for push notification
            sendRegistrationToken();
            // Fetch the settings details for push notification
            MainActivity.customerRef.child(mUser.getUid()).child("settings").addValueEventListener(settingsListener);
        }
        fetchHomeAddress();
        isUserAuthenticated();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (settingsListener != null && mUser != null) {
            // Stops listening to the changes of the Settings node
            MainActivity.customerRef.child(mUser.getUid()).child("settings").removeEventListener(settingsListener);
        }
        homeAddress = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        homeAddress = null;
    }

    private boolean isUserAuthenticated() {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            goToLoginPage();
            return false;
        }else {
            mUser.getIdToken(true)
                    .addOnFailureListener(e -> {
                        Log.e("DISABLED", String.valueOf(e));
                        if (e instanceof FirebaseAuthInvalidUserException && isUserDisabled == 0) {
                            // The user account has been disabled by an administrator
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            isUserDisabled++;
                            goToLoginPage();
                        }
                    });
            return true;
        }
    }

    private void sendRegistrationToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult().getToken();
                Log.e("FCM TOKEN", fcmToken);
                MainActivity.customerRef.child(mUser.getUid()).child("fcmTokenDevice").setValue(fcmToken);
            }else if (task.getException() != null) {
                Toast.makeText(ManongActivity.this, "Token: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchHomeAddress() {
        if (mUser != null) {
            MainActivity.customerRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("address")) {
                        homeAddress = dataSnapshot.child("address").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//        if (isDoneFromService) {
//
//            String title = "request";
//            navigateFragment(new RequestFragment());
//            currentFragment = title;
//            toolbar.setTitle(title);
//            setNewMarker(allButtons, backdropContainer, findViewById(R.id.nav_request_button));
//            searchMenu.setVisible(true);
//        }
//    }
}
