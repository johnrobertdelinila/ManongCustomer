package com.example.johnrobert.manongcustomer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1996;
    public static final String userType = "customer";
    private Intent intent, intent_register;

    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private AuthUI authUI = AuthUI.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    public static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    public static DatabaseReference rootRef = mDatabase.getReference();
    public static DatabaseReference customerRef = rootRef.child("Customers");

    private FirebaseAnalytics mFirebaseAnalytics;

    private ProgressBar loginProgress;
    private MaterialButton loginButton;
    private MaterialDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.johnrobert.manongcustomer",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        intent = new Intent(MainActivity.this, ManongActivity.class);
        if (user != null) {
            startActivity(intent);
            finish();
            return;
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FirebaseInstanceId.getInstance();

        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                .content(R.string.manong_text_moment)
                .progress(true, 0);
        loading = builder.build();

        intent_register = new Intent(MainActivity.this, PreRegisterActivity.class);

        loginProgress = findViewById(R.id.login_progress);

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        final TextInputLayout passwordTextInput = findViewById(R.id.password_text_input);
        TextInputLayout emailTextInput = findViewById(R.id.email_text_input);

        final TextInputEditText passwordEditText = findViewById(R.id.password_edit_text);
        TextInputEditText emailEditText = findViewById(R.id.email_edit_text);

        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(view -> {

            if (emailEditText.getText() == null || emailEditText.getText().length() == 0) {
                emailTextInput.setError(getString(R.string.manong_error_email));
                return;
            }else {
                emailEditText.setError(null);
            }

            if (!isEmailValid(emailEditText.getText())) {
                emailTextInput.setError(getString(R.string.manong_error_email2));
                return;
            }else {
                emailTextInput.setError(null);
            }

            if (!isPasswordInvalid(passwordEditText.getText())) {
                passwordTextInput.setError(getString(R.string.manong_error_password));
                return;
            } else {
                passwordTextInput.setError(null);
            }

            showProgressbar(true, loginButton);
            mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        // Checking if the user is customer.
                        verifyUserCustomer(uid, false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, Objects.requireNonNull(e.getMessage()), Toast.LENGTH_LONG).show();
                        showProgressbar(false, loginButton);
                    });
        });

        passwordEditText.setOnKeyListener((view, i, keyEvent) -> {
            if (isPasswordInvalid(passwordEditText.getText())) {
                passwordTextInput.setError(null);
            }
            return false;
        });

        emailEditText.setOnKeyListener((view, i, keyEvent) -> {
            if (isEmailValid(emailEditText.getText())) {
                emailTextInput.setError(null);
            }
            return false;
        });

        findViewById(R.id.text_social_media).setOnClickListener(view ->
                startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.manong_logo)
                        .setTheme(R.style.Theme_Manong_Firebase)
                        .build(),
                RC_SIGN_IN));
        findViewById(R.id.text_guest).setOnClickListener(view ->
                startActivity(intent));

        findViewById(R.id.register_button).setOnClickListener(view -> startActivity(intent_register));

        findViewById(R.id.text_forgot_password).setOnClickListener(view -> showAlertDialogReset());

    }

    private void showAlertDialogReset() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.manong_please_wait));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Reset Password");
        dialog.setMessage("Enter your Email address to reset your password.");
        View view = getLayoutInflater().inflate(R.layout.layout_reset_password, null);
        TextInputLayout forgotTextInput = view.findViewById(R.id.email_text_input);
        TextInputEditText forgotEditText = view.findViewById(R.id.email_edit_text);
        dialog.setView(view);
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("DONE", null);

        AlertDialog outputDialog = dialog.create();
        outputDialog.show();

        outputDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
            if (isEmpty(forgotEditText.getText())) {
                forgotTextInput.setError(getString(R.string.manong_error_email_register));
                return;
            }else {
                forgotTextInput.setError(null);
            }

            if (!isEmailValid(forgotEditText.getText())) {
                forgotTextInput.setError("Email Address must be valid.");
                return;
            }else {
                forgotTextInput.setError(null);
            }

            progressDialog.show();

            mAuth.sendPasswordResetEmail(forgotEditText.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(this, "An email reset password has been sent to your email.", Toast.LENGTH_LONG).show();
                            outputDialog.dismiss();
                        }else {
                            progressDialog.dismiss();
                            if (task.getException() != null) {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    loading.show();
                    Log.e("SOCIAL MEDIA", "SUCCESS");
                    verifyUserCustomer(user.getUid(), true);
                }else {
                    Log.e("SOCIAL MEDIA", "NULL");
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                if (response != null && response.getError() != null) {
                    Toast.makeText(this, "Error: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SOCIAL MEDIA", String.valueOf(response.getError()));
                }
            }
        }
    }

    private boolean isPasswordInvalid(Editable text) {
        return text != null && text.length() >= 6;
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.length() == 0;
    }

    private void showProgressbar(boolean isShow, MaterialButton materialButton) {
        if (isShow) {
            materialButton.setVisibility(MaterialButton.INVISIBLE);
            loginProgress.setVisibility(ProgressBar.VISIBLE);
        }else {
            materialButton.setVisibility(MaterialButton.VISIBLE);
            loginProgress.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private void verifyUserCustomer(String uid, boolean isSocialMedia) {
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    // User is verified as a customer.
                    // Go to next activity.
                    logEventLogin(uid);
                    updateCustomerAccount(uid);
                }else {
                    // Not a customer
                    if (isSocialMedia) {
                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("Providers")) {
                                    dataSnapshot.getRef().child("Providers").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.hasChild(uid)) {
                                                // New Customer using social media
                                                logEventSignUp(uid);
                                                updateCustomerAccount(uid);
                                            }else {
                                                // Successfully signed in using the social media of the service provider - phone number
                                                // Sign out because the user is provider.
                                                signOut(true);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            loading.dismiss();
                                            showProgressbar(false, loginButton);
                                        }
                                    });
                                }else {
                                    // new Customer using social media
                                    logEventSignUp(uid);
                                    updateCustomerAccount(uid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                loading.dismiss();
                                showProgressbar(false, loginButton);
                            }
                        });
                    }else {
                        // user is Service Provider using email and password.
                        signOut(false);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                startActivity(intent);
                loading.dismiss();
                showProgressbar(false, loginButton);
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut(boolean isSocialMedia) {
        authUI.signOut(MainActivity.this)
                .addOnCompleteListener(task -> {
                    loading.dismiss();
                    if (task.isSuccessful()) {
                        if (isSocialMedia) {
                            Toast.makeText(MainActivity.this, "Sorry but the account your trying to login " +
                                    "is not authorized as customer.", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(MainActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showProgressbar(false, loginButton);
                });
    }

    private void updateCustomerAccount(String uid) {
        // Update customer signed in value
        customerRef.child(uid).child("isSignedIn").setValue(true);
        // Go to next activity.
        loading.dismiss();
        showProgressbar(false, loginButton);
        finish();
        startActivity(intent);
    }

    private void logEventSignUp(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, uid);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }

    private void logEventLogin(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, uid);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

}
