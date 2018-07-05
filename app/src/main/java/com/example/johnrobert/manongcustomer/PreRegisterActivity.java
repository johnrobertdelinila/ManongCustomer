package com.example.johnrobert.manongcustomer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.design.button.MaterialButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.Objects;

public class PreRegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Intent intent;
    private ProgressBar progressBar;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpEnterTransition();
        setUpExitTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register);
        setUpToolbar();

        progressBar = findViewById(R.id.email_progress);
        TextInputEditText emailEditText = findViewById(R.id.email_edit_text);
        TextInputLayout emailTextInput = findViewById(R.id.email_text_input);
        nextButton = findViewById(R.id.next_button);

        intent = new Intent(PreRegisterActivity.this, RegisterActivity.class);

        nextButton.setOnClickListener(view -> {
            if (isEmpty(emailEditText.getText())) {
                emailTextInput.setError(getString(R.string.manong_error_email_register));
                return;
            }else {
                emailTextInput.setError(null);
            }

            if (!isEmailValid(emailEditText.getText())) {
                emailTextInput.setError("Email Address must be valid.");
                return;
            }else {
                emailTextInput.setError(null);
            }

            checkEmailIfExist(emailEditText.getText().toString());

        });

    }

    private void registerAccount() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            this.startActivity(intent, bundle);
        }else {
            startActivity(intent);
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setUpEnterTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide slide = new Slide(Gravity.END);
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(slide);
        }
    }

    private void setUpExitTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Slide fade = new Slide(Gravity.START);
            getWindow().setExitTransition(fade);
        }
    }

    private void checkEmailIfExist(String email) {
        showProgressbar(true);
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult providers = task.getResult();

                        if (providers != null && providers.getSignInMethods() != null && providers.getSignInMethods().size() > 0) {
                            // password, google.com, facebook.com
                            // multiple auth provider is not supported yet.
//                            Toast.makeText(this, "This email is already registered in the app.", Toast.LENGTH_LONG).show();
                            showProgressbar(false);
                            Snackbar.make(findViewById(R.id.rootContainer), "This Email Address is already used in the app.", 2750).show();
                        } else {
                            // Register account.
                            Log.e("PreRegisterActivity", "You can now register this account.");
                            intent.putExtra("email", email);
                            showProgressbar(false);
                            registerAccount();
                        }

                    }else {
                        showProgressbar(false);
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isEmailValid(Editable text) {
        return text != null && text.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    private boolean isEmpty(Editable text) {
        return text == null || text.length() == 0;
    }

    private void showProgressbar(boolean show) {
        if (show) {
//            progressBar.setVisibility(ProgressBar.VISIBLE);
//            nextButton.setVisibility(MaterialButton.GONE);
            nextButton.setEnabled(false);
            nextButton.setText(R.string.manong_button_loading);
        }else {
            nextButton.setEnabled(true);
            nextButton.setText("NEXT");
        }
    }

}
