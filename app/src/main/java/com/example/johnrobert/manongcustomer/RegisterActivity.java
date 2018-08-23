package com.example.johnrobert.manongcustomer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private static final String activityName = "RegisterActivity";

    private TextInputEditText emailEditText, fullNameEditText, phoneEditText, passwordEditText, confirmEditText;
    private TextInputLayout emailTextInput, fullNameTextInput, phoneTextInput, passwordTextInput, confirmTextInput;
    private MaterialButton register_button;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

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
                createEmailAndPassword(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
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

                Toast.makeText(RegisterActivity.this, "The code has been sent your phone number.", Toast.LENGTH_LONG).show();
                showPromptVerificationCode();
            }
        };

        register_button.setOnClickListener(view -> {

            if (!isFormValid()) {
                return;
            }

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
                createEmailAndPassword(null);
            }
        });

    }

    private void showPromptVerificationCode() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
        dialog.setTitle("Phone Number Validation");
        dialog.setMessage("Enter the verification code that been sent to your device.");
        dialog.setCancelable(false);
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        View view = getLayoutInflater().inflate(R.layout.layout_validation_code, null);
        PinEntryEditText editVerificationCode = view.findViewById(R.id.text_verification_code);
        dialog.setView(view);
        editVerificationCode.setOnPinEnteredListener(str -> verifyPhoneNumberWithCode(str.toString(), editVerificationCode));
        AlertDialog outDialog = dialog.create();
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
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//            Slide slide = new Slide(Gravity.END);
//            slide.excludeTarget(android.R.id.statusBarBackground, true);
//            slide.excludeTarget(android.R.id.navigationBarBackground, true);
//            getWindow().setEnterTransition(slide);
//        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide slide = new Slide(Gravity.END);
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);

            Slide slide1 = new Slide(Gravity.START);

            getWindow().setEnterTransition(slide);
            getWindow().setExitTransition(slide1);
        }
    }

    private void verifyPhoneNumberWithCode(String code, PinEntryEditText codeInput) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        MainActivity.mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> authResult.getUser().delete()
                        .addOnCompleteListener(task -> {
                            // Successfully deleted the user and validate the Phone Number.
                            // Register the user.
                            Log.e(activityName, "Successfully deleted the user and validate the Phone Number.");
                            createEmailAndPassword(credential);
                        }))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(RegisterActivity.this, "The verification code entered was invalid.", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    codeInput.setText(null);
                });
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

    private void createEmailAndPassword(PhoneAuthCredential credential) {
        MainActivity.mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
                .addOnSuccessListener(authResult -> {
                    // Successfully created a user.
                    Log.e(activityName, "Successfully created a user.");
                    FirebaseUser user = authResult.getUser();
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullNameEditText.getText().toString())
                            .build();
                    user.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }else {
                            // Successfully updated the display name of the user.
                            Log.e(activityName, "Successfully updated the display name of the user.");
                            // Now update the phone number of the user.
                            if (credential != null) {
                                user.updatePhoneNumber(credential).addOnCompleteListener(task2 -> {
                                    if (!task2.isSuccessful() && task2.getException() != null) {
                                        Toast.makeText(this, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else {
                                        // Successfully updated the phone number of the user too.
                                        Log.e(activityName, "Successfully updated the phone number of the user too.");
                                        // Add the excess information of the user to the database.
                                        updateCustomerAccount(user.getUid());
                                    }
                                });
                            }else {
                                updateCustomerAccount(user.getUid());
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCustomerAccount(String uid) {
        MainActivity.customerRef.child(uid).child("customer").setValue("大原櫻子")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully updated the user.
                        // Go to next activity.
                        Toast.makeText(this, "Registered successfully.", Toast.LENGTH_SHORT).show();
                        Intent homeIntent = new Intent(this, ManongActivity.class);
                        finish();
                        startActivity(homeIntent);
//                        showProgressbar(false, loginButton);
                    }else {
                        // Show error.
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//                        showProgressbar(false, loginButton);
                    }
                });
    }

}
