package com.example.johnrobert.manongcustomer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private static final String activityName = "RegisterActivity";

    private TextInputEditText emailEditText, fullNameEditText, phoneEditText, passwordEditText, confirmEditText;
    private TextInputLayout emailTextInput, fullNameTextInput, phoneTextInput, passwordTextInput, confirmTextInput;
    private MaterialButton register_button;
    private MaterialDialog loading;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAnalytics mFirebaseAnalytics;

    private String mVerificationId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpEnterTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpToolbar();
        init();

        email = getIntent().getStringExtra("email");
        if (email != null) {
            emailEditText.setText(email);
            emailEditText.setEnabled(false);
            fullNameTextInput.setFocusable(true);
            fullNameEditText.setFocusable(true);
        }

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                createEmailAndPassword(phoneAuthCredential, null, null, null, null, null);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loading.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(RegisterActivity.this, "Invalid Request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(RegisterActivity.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                mVerificationId = s;
                mResendToken = forceResendingToken;

                loading.dismiss();
                Toast.makeText(RegisterActivity.this, "The code has been sent your phone number.", Toast.LENGTH_LONG).show();
                showPromptVerificationCode();
            }
        };

        register_button.setOnClickListener(view -> {

            if (!isFormValid()) {
                return;
            }

            loading.show();

            if (!isEmpty(phoneEditText.getText())) {
                String phoneNumber = "+63" + Objects.requireNonNull(phoneEditText.getText()).toString().trim();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        RegisterActivity.this,
                        mCallbacks
                );
            }else {
                createEmailAndPassword(null, null, null, null, null, null);
            }

        });

    }

    private void showPromptVerificationCode() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Phone Validation");
        dialog.setMessage("Enter the 6-digit code we sent to +63" + phoneEditText.getText());

//        dialog.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.layout_validation_code, null);
        PinEntryEditText editVerificationCode = view.findViewById(R.id.text_verification_code);
        MaterialButton phoneButton = view.findViewById(R.id.phone_button);
        MaterialButton resendButton = view.findViewById(R.id.resend_button);
        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        dialog.setView(view);

//        new Handler().postDelayed(() -> {
////            if (resendButton != null) {
////                resendButton.setEnabled(true);
////                resendButton.setTextColor(getResources().getColor(R.color.textColorPrimary));
////                resendButton.setText("RESEND CODE");
////            }
////        }, 20000);

        new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (resendButton != null) {
                    resendButton.setText("RESEND CODE IN " + millisUntilFinished / 1000);
                }
            }

            public void onFinish() {
                if (resendButton != null) {
                    resendButton.setEnabled(true);
                    resendButton.setTextColor(getResources().getColor(R.color.textColorPrimary));
                    resendButton.setText("RESEND CODE");
                }
            }
        }.start();
        editVerificationCode.setOnPinEnteredListener(str -> phoneButton.setEnabled(true));
        editVerificationCode.setOnKeyListener((view1, i, keyEvent) -> {
            if (editVerificationCode.getText().length() < 6) {
                phoneButton.setEnabled(false);
            }
            return false;
        });

        AlertDialog outDialog = dialog.create();

        phoneButton.setOnClickListener(view1 -> {
            linearLayout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            verifyPhoneNumberWithCode(editVerificationCode.getText().toString(), editVerificationCode, phoneButton, linearLayout, progressBar, outDialog);
        });
        resendButton.setOnClickListener(view1 -> {
            outDialog.dismiss();
            resendVerificationCode("+63" + phoneEditText.getText().toString(), mResendToken);
            loading.show();
        });

        outDialog.show();
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorControlActivated));
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void init() {
        emailEditText = findViewById(R.id.email_edit_text);
        emailTextInput = findViewById(R.id.email_text_input);
        fullNameEditText = findViewById(R.id.fullname_edit_text);
        fullNameTextInput = findViewById(R.id.fullname_text_input);
        phoneEditText = findViewById(R.id.phone_edit_text);
        phoneTextInput = findViewById(R.id.phone_text_input);
        passwordEditText = findViewById(R.id.password_edit_text);
        passwordTextInput = findViewById(R.id.password_text_input);
        confirmEditText = findViewById(R.id.confirmpassword_edit_text);
        confirmTextInput = findViewById(R.id.confirmpassword_text_input);
        register_button = findViewById(R.id.register_button);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(RegisterActivity.this)
                .content(getString(R.string.manong_please_wait))
                .progress(true, 0);
        loading = builder.build();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private boolean isFormValid() {
        if (isEmpty(emailEditText.getText())) {
            emailTextInput.setError(getString(R.string.manong_error_email_register));
            return false;
        }else {
            emailTextInput.setError(null);
        }

        if (!isEmailValid(emailEditText.getText())) {
            emailTextInput.setError(getString(R.string.manong_error_email2));
            return false;
        }else {
            emailTextInput.setError(null);
        }

        if (isEmpty(fullNameEditText.getText())) {
            fullNameTextInput.setError(getString(R.string.manong_error_fullname));
            return false;
        }else {
            fullNameTextInput.setError(null);
        }

//        if (isEmpty(phoneEditText.getText())) {
//            phoneTextInput.setError(getString(R.string.manong_error_phone));
//            return false;
//        }else {
//            phoneTextInput.setError(null);
//        }

        if (!isEmpty(phoneEditText.getText()) && !isPhoneValid(phoneEditText.getText())) {
            phoneTextInput.setError(getString(R.string.manong_phone_valid_error));
            return false;
        }else {
            phoneTextInput.setError(null);
        }

        if (isEmpty(passwordEditText.getText())) {
            passwordTextInput.setError(getString(R.string.manong_password_error));
            return false;
        }else {
            passwordTextInput.setError(null);
        }

        if (isEmpty(confirmEditText.getText())) {
            confirmTextInput.setError(getString(R.string.manong_confirm_password_error));
            return false;
        }else {
            confirmTextInput.setError(null);
        }

        if (!isPasswordValid(passwordEditText.getText())) {
            passwordTextInput.setError(getString(R.string.manong_error_password));
            return false;
        } else {
            passwordTextInput.setError(null);
        }

        if (!isPasswordValid(confirmEditText.getText())) {
            confirmTextInput.setError(getString(R.string.manong_error_password));
            return false;
        }else {
            confirmTextInput.setError(null);
        }

        if (!isPasswordEqual(passwordEditText.getText(), confirmEditText.getText())) {
            passwordTextInput.setError("Password is not the same.");
            confirmTextInput.setError("Confirm password is not the same.");
            return false;
        }else {
            passwordTextInput.setError(null);
            confirmTextInput.setError(null);
        }

        return true;
    }

    private boolean isPasswordValid(Editable text) {
        return text == null || text.toString().trim().length() >= 6;
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.toString().trim().length() != 0 && Patterns.EMAIL_ADDRESS.matcher(text.toString().trim()).matches();
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.toString().trim().length() == 0;
    }

    private boolean isPhoneValid(Editable text) {
        return text != null && text.toString().trim().length() == 10;
    }

    private boolean isPasswordEqual(Editable password, Editable confirmPassword) {
        return password.toString().trim().equals(confirmPassword.toString().trim());
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
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

    private void verifyPhoneNumberWithCode(String code, PinEntryEditText codeInput, MaterialButton phoneButton,
                                           LinearLayout linearLayout, ProgressBar progressBar, AlertDialog outDialog) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        createEmailAndPassword(credential, codeInput, phoneButton, linearLayout, progressBar,outDialog);

    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken mResendToken) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                RegisterActivity.this,
                mCallbacks,
                mResendToken
        );
    }

    private void createEmailAndPassword(PhoneAuthCredential credential, PinEntryEditText codeInput, MaterialButton phoneButton,
                                        LinearLayout linearLayout, ProgressBar progressBar, AlertDialog outDialog) {
        MainActivity.mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
                .addOnSuccessListener(authResult -> {
                    // Successfully created a user.
                    Log.e(activityName, "Successfully created a user.");
                    FirebaseUser user = authResult.getUser();

                    if (credential != null && phoneEditText.getText() != null && phoneEditText.getText().length() > 0) {
                        user.updatePhoneNumber(credential).addOnCompleteListener(task2 -> {
                            if (!task2.isSuccessful() && task2.getException() != null) {
                                Log.e(activityName, task2.getException().getMessage());
                                user.delete();

                                if (task2.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    if (phoneButton != null) {
                                        phoneButton.setEnabled(false);
                                    }
                                    Toast.makeText(RegisterActivity.this, "The verification code entered was invalid. Please try again.", Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(this, task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                if (codeInput != null) {
                                    codeInput.setText(null);
                                }
                                if (linearLayout != null && progressBar != null) {
                                    linearLayout.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }else {
                                // Successfully updated the phone number of the user too.

                                if (outDialog != null) {
                                    outDialog.dismiss();
                                }
                                loading.show();
                                Log.e(activityName, "Successfully updated the phone number of the user.");
                                updateDisplayName(user);
                                // Add the excess information of the user to the database.
                            }
                        });
                    }else {
                        updateDisplayName(user);
                    }

                })
                .addOnFailureListener(e -> {
                    loading.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateDisplayName(FirebaseUser user) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullNameEditText.getText().toString())
                .build();
        user.updateProfile(profileUpdate).addOnCompleteListener(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                user.delete();
                loading.dismiss();
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }else {
                updateCustomerAccount(user.getUid());
            }
        });
    }

    private void updateCustomerAccount(String uid) {
        MainActivity.customerRef.child(uid).child("isSignedIn").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully updated the user.
                        // Go to next activity.
                        logEventSignUp(uid);
                        Toast.makeText(this, "Thank you for joining. Welcome to One Tap Manong!", Toast.LENGTH_SHORT).show();
                        Intent homeIntent = new Intent(this, ManongActivity.class);
                        finish();
                        startActivity(homeIntent);
                        loading.dismiss();
                    }else {
                        // Show error.
                        loading.dismiss();
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logEventSignUp(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, uid);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }

}
