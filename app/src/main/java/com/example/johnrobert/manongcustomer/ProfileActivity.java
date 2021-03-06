package com.example.johnrobert.manongcustomer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_CODE = 1000;
    private static final int RC_RE_AUTH_GOOGLE = 2000;
    private static final int RC_RE_AUTH_FB = 3000;
    private static final String TAG = "ProfileActivity";

    private EditText editDisplayName;
    private TextInputLayout emailTextInput, phoneTextInput, addressTextInput;
    private TextInputEditText emailEditText, phoneEditText, addressEditText;
    private CircleImageView userPhotoUrl;
    private MaterialButton updateButton, changePassButton;
    private MaterialDialog loading;

    private FirebaseUser user = ManongActivity.mUser;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;

    public String displayName;
    public String photoURL;
    public String phoneNumber;
    public String email;
    private String providerId;
    private String originalProviderEmail;

    private String updateEmail;
    private String updatePhoneNumber;
    private String updateDisplayName;

    private PhoneAuthCredential updatePhoneCredential;

    private String updatedPhotoURL;

    public GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private List<AuthUI.IdpConfig> providers;
    private String providerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpEnterTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setUpToolbar();
        init();

        editDisplayName.setBackgroundColor(Color.TRANSPARENT);
        disableEditText();

        if (user != null) {

            displayName = user.getDisplayName();
            photoURL = String.valueOf(user.getPhotoUrl());
            phoneNumber = user.getPhoneNumber();
            email = user.getEmail();

            if (email != null && !email.trim().equalsIgnoreCase("")) {
                getUserEmailProvider(email);
            }

            int test = 0;

            for (UserInfo profile : user.getProviderData()) {
                //password, google.com, facebook.com, twitter.com
                providerId = profile.getProviderId();
                if (profile.getEmail() != null && test == 1) {
                    originalProviderEmail = profile.getEmail();
                }
                if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().trim().equalsIgnoreCase("") && !profile.getPhoneNumber().trim().equalsIgnoreCase("null")
                       && phoneNumber == null || phoneNumber != null && phoneNumber.trim().equalsIgnoreCase("") || phoneNumber != null && phoneNumber.trim().equalsIgnoreCase("null")) {
                    phoneNumber = profile.getPhoneNumber();
                }
                test++;
            }


            if (providerId.equalsIgnoreCase("password")) {
                changePassButton.setVisibility(MaterialButton.VISIBLE);
            }

            if (displayName == null) {
                displayName = "Anonymous";
            }
            if (photoURL != null && !photoURL.trim().equals("")) {
                if (photoURL.startsWith("https://graph.facebook.com")) {
                    photoURL = photoURL.concat("?height=100");
                }else if (photoURL.startsWith("https://pbs.twimg.com")) {
                    photoURL = photoURL.replace("_normal", "").trim();
                }
                Log.e("USER PHOTO", photoURL);
            }

            setUpTheViews(photoURL, displayName, email, phoneNumber);
        }

        updateButton.setOnClickListener(view -> {
            String text = (String) updateButton.getText();
            if (text.equalsIgnoreCase("EDIT")) {
                openUpdate();
            }else if (text.equalsIgnoreCase("SAVE")) {
                updateProfile();
            }
        });

        userPhotoUrl.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        changePassButton.setOnClickListener(view -> showChangePassDialog());

        findViewById(R.id.btn_logout).setOnClickListener(view1 -> {
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(ProfileActivity.this);
            dialog.setTitle("Logout");
            dialog.setMessage("Are you sure, you want to logout?");
            dialog.setPositiveButton("YES", (dialogInterface, i) -> {
                MainActivity.customerRef.child(user.getUid()).child("isSignedIn").setValue(false);
                AuthUI.getInstance()
                        .signOut(ProfileActivity.this)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        });
            });
            dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

            android.app.AlertDialog dialogg = dialog.create();
            dialogg.show();

        });

    }

    private void openUpdate() {
        enableEditText();
        updateButton.setText("SAVE");
        updateButton.setEnabled(true);
    }

    private void updateProfile() {
        if (isEmpty(editDisplayName.getText())) {
            Toast.makeText(this, "Your name must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEmpty(emailEditText.getText())) {
            emailTextInput.setError(getString(R.string.manong_error_email_register));
            return;
        }else {
            emailTextInput.setError(null);
        }
        if (!isEmailValid(emailEditText.getText())) {
            emailTextInput.setError(getString(R.string.manong_error_email2));
            return;
        }else {
            emailTextInput.setError(null);
        }
        if (phoneNumber != null && !phoneNumber.trim().equalsIgnoreCase("") && isEmpty(phoneEditText.getText())) {
            phoneTextInput.setError(getString(R.string.manong_error_phone));
            return;
        }else {
            phoneTextInput.setError(null);
        }
        if (phoneNumber != null && !phoneNumber.trim().equalsIgnoreCase("") && !isPhoneValid(phoneEditText.getText())) {
            phoneTextInput.setError(getString(R.string.manong_phone_valid_error));
            return;
        }else {
            phoneTextInput.setError(null);
        }

        updateEmail = emailEditText.getText().toString();
        updatePhoneNumber = phoneEditText.getText().toString();
        if (!updatePhoneNumber.trim().equalsIgnoreCase("")) {
            updatePhoneNumber = "+63" + updatePhoneNumber;
        }
        updateDisplayName = editDisplayName.getText().toString();

        updateProfileLoading();
        checkEmailIfExist();
        sendHomeAddressUpdate();
    }

    private void enableEditText() {
        emailTextInput.setEnabled(true);
        emailEditText.setEnabled(true);
        phoneTextInput.setEnabled(true);
        phoneEditText.setEnabled(true);
        editDisplayName.setEnabled(true);
        addressEditText.setEnabled(true);
        addressTextInput.setEnabled(true);

        emailTextInput.setFocusableInTouchMode(true);
        emailEditText.setFocusableInTouchMode(true);
        phoneTextInput.setFocusableInTouchMode(true);
        phoneEditText.setFocusableInTouchMode(true);
        addressTextInput.setFocusableInTouchMode(true);
        addressEditText.setFocusableInTouchMode(true);
        editDisplayName.setFocusableInTouchMode(true);
        emailTextInput.setFocusable(true);
        emailEditText.setFocusable(true);
        phoneTextInput.setFocusable(true);
        phoneEditText.setFocusable(true);
        addressTextInput.setFocusable(true);
        addressEditText.setFocusable(true);
        editDisplayName.setFocusable(true);
        editDisplayName.requestFocus();
        editDisplayName.setSelection(editDisplayName.getText().length());
    }

    private void disableEditText() {
        emailTextInput.setFocusable(false);
        emailEditText.setFocusable(false);
        phoneTextInput.setFocusable(false);
        phoneEditText.setFocusable(false);
        addressEditText.setFocusable(false);
        editDisplayName.setFocusable(false);

        emailTextInput.setEnabled(false);
        emailEditText.setEnabled(false);
        phoneTextInput.setEnabled(false);
        phoneEditText.setEnabled(false);
        editDisplayName.setEnabled(false);
        addressTextInput.setEnabled(false);
        addressEditText.setEnabled(false);
    }

    private void init() {
        editDisplayName = findViewById(R.id.profile_user_name);
        emailTextInput = findViewById(R.id.email_text_input);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneTextInput = findViewById(R.id.phone_text_input);
        phoneEditText = findViewById(R.id.phone_edit_text);
        addressTextInput = findViewById(R.id.address_text_input);
        addressEditText = findViewById(R.id.address_edit_text);
        userPhotoUrl = findViewById(R.id.profile_user_image);
        updateButton = findViewById(R.id.update_button);
        changePassButton = findViewById(R.id.change_pass_button);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ProfileActivity.this)
                .content(getString(R.string.manong_please_wait))
                .progress(true, 0);
        loading = builder.build();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                updatePhoneCredential = phoneAuthCredential;
                updatePhoneNumber(null, null, null, null, null);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loading.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(ProfileActivity.this, "Invalid Request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(ProfileActivity.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(ProfileActivity.this, "Verification code has been sent to your phone number.", Toast.LENGTH_LONG).show();
                showPromptVerificationCode();
            }
        };

        providers = Collections.singletonList(
                new AuthUI.IdpConfig.FacebookBuilder().build());

    }

    private void setUpTheViews(String photoURL, String displayName, String email, String phoneNumber) {
        findViewById(R.id.container_loading).setVisibility(RelativeLayout.GONE);
        findViewById(R.id.main_container).setVisibility(ScrollView.VISIBLE);

        editDisplayName.setText(displayName);
        emailEditText.setText(email);
        if (phoneNumber != null && !phoneNumber.equalsIgnoreCase("") && !phoneNumber.equalsIgnoreCase("null")) {
            phoneEditText.setText(phoneNumber.substring(3));
        }else {
            for (UserInfo info: user.getProviderData()) {
                if (info.getPhoneNumber() != null && !phoneNumber.equalsIgnoreCase("") && !phoneNumber.equalsIgnoreCase("null")) {
                    phoneEditText.setText(info.getPhoneNumber().substring(3));
                }
            }
        }
        if (photoURL != null && !photoURL.trim().equals("") && !photoURL.equals("null")) {
            Glide.with(getApplicationContext()).load(photoURL).into(userPhotoUrl);
        }
        if (ManongActivity.homeAddress != null) {
            addressEditText.setText(ManongActivity.homeAddress);
        }

        userPhotoUrl.setVisibility(CircleImageView.VISIBLE);
        findViewById(R.id.temp_image_view).setVisibility(CardView.GONE);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.length() == 0;
    }

    private boolean isPhoneValid(Editable text) {
        return text != null && text.length() == 10;
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean isPasswordValid(Editable text) {
        return text == null || text.length() >= 6;
    }

    private boolean isPasswordEqual(Editable password, Editable confirmPassword) {
        return password.toString().equals(confirmPassword.toString());
    }

    //SEQUENCE: CHECK EMAIL -> SEND VERIFICATION -> DISPLAY NAME

    private void checkEmailIfExist() {
        if (!updateEmail.equals(email)) {
            MainActivity.mAuth.fetchSignInMethodsForEmail(updateEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult providers = task.getResult();

                            if (providers != null && providers.getSignInMethods() != null && providers.getSignInMethods().size() > 0) {
                                // password, google.com, facebook.com
                                // FIXME: multiple auth provider is not supported yet.

                                if (!updateEmail.trim().equalsIgnoreCase("") && originalProviderEmail != null && updateEmail.equalsIgnoreCase(originalProviderEmail)){
                                    sendVerificationCode();
                                }else {
                                    emailTextInput.setError("This email is already in used. Try another email instead.");
                                    openUpdate();
                                }
                            } else {
                                // Send phone verification code.
                                sendVerificationCode();
                            }

                        }else {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            openUpdate();
                        }
                    });
        }else {
            sendVerificationCode();
        }
    }

    // SEQUENCE: PHONE NUMBER -> EMAIL -> DISPLAY NAME

    private void updatePhoneNumber(PinEntryEditText codeInput, MaterialButton phoneButton,
                                   LinearLayout linearLayout, ProgressBar progressBar, AlertDialog outDialog) {
        if (phoneNumber != null && !phoneNumber.equals(updatePhoneNumber) || phoneNumber == null && !updatePhoneNumber.trim().equalsIgnoreCase("")) {
            user.updatePhoneNumber(updatePhoneCredential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (outDialog != null) {
                                outDialog.dismiss();
                            }
                            loading.show();
                            updateEmail();
                        }else {
                            if (phoneButton != null) {
                                phoneButton.setEnabled(false);
                            }
                            if (codeInput != null) {
                                codeInput.setText(null);
                            }
                            if (linearLayout != null && progressBar != null) {
                                linearLayout.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(ProfileActivity.this, "The verification code entered was invalid. Please try again.", Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }else {
            // Skip the update of phone number.
            updateEmail();
        }
    }

    private void updateEmail() {
        if (!email.equals(updateEmail)) {
            user.updateEmail(updateEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateDisplayName();
                        }else {
                            loading.dismiss();
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                    // Re-authenticate user to update email
                                    Toast.makeText(this, "User needs to re-authenticate their account to continue profile update.", Toast.LENGTH_SHORT).show();
                                    showReAuthConfirmation();
                                }else {
                                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                openUpdate();
                            }
                        }
                    });
        }else {
            // Skip the update of email.
            updateDisplayName();
        }
    }

    private void updateDisplayName() {
        if (!updateDisplayName.equals(displayName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(updateDisplayName)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> closeUpdate());
        }else {
            closeUpdate();
        }
    }

    private void sendVerificationCode() {
        if (phoneNumber != null && !phoneNumber.equals(updatePhoneNumber) || phoneNumber == null && !updatePhoneNumber.trim().equalsIgnoreCase("")) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    updatePhoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    ProfileActivity.this,
                    mCallbacks
            );
        }else {
            // Skip the sending of verification.
            updateEmail();
        }
    }

    private void showPromptVerificationCode() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
        dialog.setTitle("Phone number validation");
        dialog.setMessage("Enter the 6-digit code we sent to +63" + phoneEditText.getText());
        dialog.setCancelable(false);
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            loading.dismiss();
            enableEditText();
            updateButton.setEnabled(true);
            updateButton.setText("SAVE");
        });
        View view = getLayoutInflater().inflate(R.layout.layout_validation_code, null);
        PinEntryEditText editVerificationCode = view.findViewById(R.id.text_verification_code);
        MaterialButton phoneButton = view.findViewById(R.id.phone_button);
        MaterialButton resendButton = view.findViewById(R.id.resend_button);
        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        dialog.setView(view);

        new Handler().postDelayed(() -> {
            if (resendButton != null) {
                resendButton.setEnabled(true);
                resendButton.setTextColor(getResources().getColor(R.color.textColorPrimary));
                resendButton.setText("RESEND CODE");
            }
        }, 20000);

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

    private void verifyPhoneNumberWithCode(String code, PinEntryEditText codeInput, MaterialButton phoneButton,
                                           LinearLayout linearLayout, ProgressBar progressBar, AlertDialog outDialog) {
        updatePhoneCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
        updatePhoneNumber(codeInput, phoneButton, linearLayout, progressBar, outDialog);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken mResendToken) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                ProfileActivity.this,
                mCallbacks,
                mResendToken
        );
    }

    private void closeUpdate() {
        disableEditText();
        Toast.makeText(this, "Successfully updated the profile.", Toast.LENGTH_SHORT).show();
        updateButton.setText("EDIT");
        updateButton.setEnabled(true);

        //Sync new user profile
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            phoneNumber = user.getPhoneNumber();
            displayName = user.getDisplayName();
            photoURL = String.valueOf(user.getPhotoUrl());
        }
        MoreFragment.isUpdated = true;
        loading.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Ask user
                    showChangeImageConfimation(uri);
                }
            }
        }else if (requestCode == RC_RE_AUTH_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                reAuthGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == RC_RE_AUTH_FB) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully re-authenticated.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null && user.getEmail().equals(email)) {
                    Toast.makeText(this, "User account re-authenticated successfully.", Toast.LENGTH_LONG).show();
                    // Continue with task
                    updateEmail();
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                if (response != null && response.getError() != null) {
                    Toast.makeText(this, "Error: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void reAuthGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Your account has been re-authenticated successfully.", Toast.LENGTH_SHORT).show();
                        updateEmail();
                    }else {
                        if (task.getException() != null) {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showChangePassDialog() {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
        dialog.setTitle("Change Password");
        View view = getLayoutInflater().inflate(R.layout.layout_change_password, null);
        TextInputLayout oldTextInput = view.findViewById(R.id.old_password_text_input);
        TextInputLayout newTextInput = view.findViewById(R.id.new_password_text_input);
        TextInputLayout confirmTextInput = view.findViewById(R.id.confirm_password_text_input);
        TextInputEditText oldEditText = view.findViewById(R.id.old_password_edit_text);
        TextInputEditText newEditText = view.findViewById(R.id.new_password_edit_text);
        TextInputEditText confirmEditText = view.findViewById(R.id.confirm_password_edit_text);
        dialog.setView(view);
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("DONE", null);

        android.app.AlertDialog outputDialog = dialog.create();
        outputDialog.show();

        outputDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.manong_please_wait));
            if (isEmpty(oldEditText.getText())) {
                oldTextInput.setError("Old password must not be empty.");
                return;
            }else {
                oldTextInput.setError(null);
            }
            if (isEmpty(newEditText.getText())) {
                newTextInput.setError(getString(R.string.manong_password_error));
                return;
            }else {
                newTextInput.setError(null);
            }
            if (isEmpty(confirmEditText.getText())) {
                confirmTextInput.setError(getString(R.string.manong_confirm_password_error));
                return;
            }else {
                confirmTextInput.setError(null);
            }
            if (!isPasswordInvalid(oldEditText.getText())) {
                oldTextInput.setError(getString(R.string.manong_error_password));
                return;
            } else {
                oldTextInput.setError(null);
            }
            if (!isPasswordInvalid(newEditText.getText())) {
                newTextInput.setError(getString(R.string.manong_error_password));
                return;
            } else {
                newTextInput.setError(null);
            }
            if (!isPasswordInvalid(confirmEditText.getText())) {
                confirmTextInput.setError(getString(R.string.manong_error_password));
                return;
            } else {
                confirmTextInput.setError(null);
            }
            if (!isPasswordEqual(newEditText.getText(), confirmEditText.getText())) {
                newTextInput.setError("Password is not the same.");
                confirmTextInput.setError("Confirm password is not the same.");
                return;
            }else {
                newTextInput.setError(null);
                confirmTextInput.setError(null);
            }

            progressDialog.show();

            // Change password
            reAuthPassword(oldEditText.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Task returnTask = task.getResult();
                            if (returnTask.isSuccessful()) {
                                // User re-authenticated successfully.
                                user.updatePassword(newEditText.getText().toString())
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(this, "Your password changed successfully.", Toast.LENGTH_SHORT).show();
                                                outputDialog.dismiss();
                                            }else {
                                                progressDialog.dismiss();
                                                if (task1.getException() != null) {
                                                    Toast.makeText(this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else {
                                progressDialog.dismiss();
                                if (returnTask.getException() != null) {
                                    if (returnTask.getException() instanceof FirebaseAuthInvalidUserException) {
                                        Toast.makeText(this, "This user's account has been disabled or deleted.", Toast.LENGTH_LONG).show();
                                    }else if (returnTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(this, "The old password provided is invalid to this user's account. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                        }else {
                            progressDialog.dismiss();
                            if (task.getException() != null) {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

    }

    private Task<Task> reAuthPassword(String old_password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, old_password);
        return user.reauthenticate(credential)
                .continueWith(task -> task);
    }

    private void getAuthGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_RE_AUTH_GOOGLE);
    }

    private void getAuthFacebook() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_RE_AUTH_FB);
    }

    private void getAuthPassword() {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
        dialog.setTitle("Re-authenticate Account");
        dialog.setMessage("Enter your password.");
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.layout_reauth_password, null);
        TextInputLayout oldTextInput = view.findViewById(R.id.old_password_text_input);
        TextInputEditText oldEditText = view.findViewById(R.id.old_password_edit_text);
        dialog.setView(view);
        dialog.setPositiveButton("DONE", null);
        android.app.AlertDialog outDialog = dialog.create();
        outDialog.show();
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
            if (isEmpty(oldEditText.getText())) {
                oldTextInput.setError("Password must not be empty.");
                return;
            }else {
                oldTextInput.setError(null);
            }
            if (!isPasswordValid(oldEditText.getText())) {
                oldTextInput.setError(getString(R.string.manong_error_password));
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldEditText.getText().toString());
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account re-authenticated successfully.", Toast.LENGTH_SHORT).show();
                    outDialog.dismiss();
                    updateEmail();
                }else {
                    if (task.getException() != null) {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void updateProfileLoading() {
        disableEditText();
        updateButton.setText(R.string.text_button_saving);
        updateButton.setEnabled(false);
    }

    private boolean isPasswordInvalid(Editable text) {
        return text != null && text.length() >= 6;
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent();
//        String updatedDisplayName = editDisplayName.getText().toString();
//        intent.putExtra("displayName", updatedDisplayName);
//        intent.putExtra("photoURL", updatedPhotoURL);
//        setResult(REQUEST_CODE, intent);
        finish();
    }

    private void showReAuthConfirmation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
        dialog.setTitle("Re-authenticate Account");
        dialog.setMessage("Some security-sensitive actions such as setting a primary email address and changing a password " +
                "require that the user has recently signed in. Do you want to re-authenticate your account to continue profile update?");
        dialog.setPositiveButton("YES", (dialogInterface, i) -> {
            // perform re-auth
            if (providerEmail.equalsIgnoreCase("facebook.com")) {
                getAuthFacebook();
            }else if (providerEmail.equalsIgnoreCase("google.com")) {
                getAuthGoogle();
            }else if (providerEmail.equalsIgnoreCase("password")) {
                getAuthPassword();
            }
        });
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            loading.dismiss();
            enableEditText();
            updateButton.setEnabled(true);
            updateButton.setText("SAVE");
        });
        AlertDialog outDialog = dialog.create();
        outDialog.show();
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void getUserEmailProvider(String email) {
        MainActivity.mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult providers = task.getResult();

                        if (providers != null && providers.getSignInMethods() != null && providers.getSignInMethods().size() > 0) {
                            for (String provider : providers.getSignInMethods()) {
                                providerEmail = provider;
                            }
                        }

                    }else {
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showChangeImageConfimation(Uri uri) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.ManongDialogTheme);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Set new image?");
        dialog.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("OKAY", (dialogInterface, i) -> {
            userPhotoUrl.setVisibility(CircleImageView.GONE);
            findViewById(R.id.temp_image_view).setVisibility(CardView.VISIBLE);

            Toast.makeText(this, R.string.manong_image_loading, Toast.LENGTH_SHORT).show();

            StorageReference userStorageReference = FirebaseStorage.getInstance().getReference()
                    .child("User profile picture").child(user.getUid());
            putImageInStorage(userStorageReference, uri);
            MoreFragment.isUpdated = true;
        });
        AlertDialog outDialog = dialog.create();
        if(ProfileActivity.this.isFinishing()) {
            return;
        }
        outDialog.show();
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void setUpEnterTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide slide;
            Slide slide1;
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
                slide = new Slide(GravityCompat.getAbsoluteGravity(GravityCompat.END, getResources().getConfiguration().getLayoutDirection()));
                slide1 = new Slide(GravityCompat.getAbsoluteGravity(GravityCompat.START, getResources().getConfiguration().getLayoutDirection()));
            }else {
                slide = new Slide(Gravity.END);
                slide1 = new Slide(Gravity.START);
            }
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);

            getWindow().setEnterTransition(slide);
            getWindow().setExitTransition(slide1);
        }
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri) {

        UploadTask uploadTask = storageReference.putFile(uri);

        uploadTask.addOnCompleteListener(ProfileActivity.this,
                task -> {
                    if (!task.isSuccessful() || !task.isComplete()) {
                        Log.w(TAG, "Image upload task was not successful.",
                                task.getException());
                    }
                });

        uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        userPhotoUrl.setVisibility(CircleImageView.VISIBLE);
                        findViewById(R.id.temp_image_view).setVisibility(CardView.GONE);
                    }
                    return storageReference.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String url = task.getResult().toString();
                        // Update profile
                        updateProfilePicture(url, uri);
                    }else {
                        if (task.getException() != null) {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        userPhotoUrl.setVisibility(CircleImageView.VISIBLE);
                        findViewById(R.id.temp_image_view).setVisibility(CardView.GONE);
                    }
                });
    }

    private void updateProfilePicture(String urlStorage, Uri offlineImage) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(urlStorage))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Glide.with(getApplicationContext()).load(offlineImage).into(userPhotoUrl);
                        userPhotoUrl.setVisibility(CircleImageView.VISIBLE);
                        findViewById(R.id.temp_image_view).setVisibility(CardView.GONE);
                        Toast.makeText(this, "Profile Photo successfully updated.", Toast.LENGTH_SHORT).show();
                    }else {
                        if (task.getException() != null) {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        userPhotoUrl.setVisibility(CircleImageView.VISIBLE);
                        findViewById(R.id.temp_image_view).setVisibility(CardView.GONE);
                    }
                });
    }

    private void sendHomeAddressUpdate() {
        Editable editAddress = addressEditText.getText();
        if (editAddress != null && !editAddress.toString().equals(ManongActivity.homeAddress)) {
            MainActivity.customerRef.child(user.getUid()).child("address").setValue(editAddress.toString());
        }
    }

}
