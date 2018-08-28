package com.example.johnrobert.manongcustomer;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.shapeofview.shapes.CutCornerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class InfoActivity extends AppCompatActivity {

    private Request request;
    private LinearLayout root_container;
    private TextView textLocationName;
    private RequestOptions requestOptions;
    private MaterialButton cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpEnterTransition();
        setContentView(R.layout.activity_info);

        request = (Request) getIntent().getSerializableExtra("request");
        root_container = findViewById(R.id.container_layout);
        cancelButton = findViewById(R.id.cancel_button);
        textLocationName = findViewById(R.id.text_info_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        requestOptions = new RequestOptions().placeholder(R.drawable.loading_image).override(350, 350).centerCrop();

        if (request != null) {
            generateChecklist(request);
        }

        cancelButton.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(InfoActivity.this, R.style.ManongDialogTheme);
            dialog.setTitle("Cancellation");
            dialog.setMessage("Are you sure to cancel this request?");
            dialog.setPositiveButton("YES", (dialogInterface, i) -> showAlertDialogReason());
            dialog.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog outDialog = dialog.create();
            outDialog.show();
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(Typeface.DEFAULT_BOLD);
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            outDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT_BOLD);
        });

        if (request.getCancelled() != null) {
            Log.e("NULL", String.valueOf(request.getCancelled()));
        }

        if (request != null && request.getCancelled() != null && request.getCancelled()) {
            findViewById(R.id.container_cut_button).setVisibility(CutCornerView.GONE);
        }

    }

    private void showAlertDialogReason() {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
        dialog.setTitle("Cancellation");
        dialog.setMessage("Tell us your reason why you need to cancel.");

        View view = getLayoutInflater().inflate(R.layout.layout_cancellation_reason, null);
        TextInputEditText cancellationEditText = view.findViewById(R.id.reason_edit_text);
        dialog.setView(view);

        dialog.setPositiveButton("DONE", (dialogInterface, i) -> cancelRequest(cancellationEditText.getText().toString().trim()));
        dialog.setNegativeButton("SKIP", (dialogInterface, i) -> cancelRequest(cancellationEditText.getText().toString().trim()));

        android.app.AlertDialog outDialog = dialog.create();
        outDialog.show();
    }

    private void cancelRequest(String cancellationReason) {
        if (request != null && request.getKey() != null) {
            HashMap<String, Object> cancellation = new HashMap<>();
            cancellation.put("isCancelled", true);
            if (!cancellationReason.equals("")) {
                cancellation.put("cancellationReason", cancellationReason);
            }

            ManongActivity.requestRef.child(request.getKey()).updateChildren(cancellation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "This request has been cancelled. Thank you for your feedback.", Toast.LENGTH_LONG).show();
                            cancelButton.setText("CANCELLED");
                            cancelButton.setEnabled(false);
                        }
                    });
        }
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

    private String generateRequestId(String date, String service, String id) {
        StringBuilder builder = new StringBuilder();
        String serviceSub = service.length() > 2 ? service.substring(0, 2).toUpperCase() : service.toUpperCase();
        String idSub = id.substring(id.length() - 3).toUpperCase();
        String yearSub = date.substring(date.length() - 2);
        String dateSub = date.substring(4, 5);

        builder.append(serviceSub);
        builder.append(idSub);
        builder.append("-");
        builder.append(yearSub);
        builder.append("0");
        builder.append(dateSub);

        return builder.toString();
    }

    private void generateChecklist(Request request) {

        // Static View
        if (request.getKey() != null) {

            for (String key: request.getQuestionsAndAnswers().keySet()) {
                if (key.substring(2).equalsIgnoreCase("When do you need it?") || key.substring(2).toLowerCase().startsWith("when do you need")) {
                    String requestID = generateRequestId(request.getQuestionsAndAnswers().get(key), request.getServiceName(), request.getKey());
                    ((TextView) findViewById(R.id.text_info_id)).setText(requestID);
                    break;
                }
            }

        }

        if (request.getTimestamp() != null) {
            Long timestamp = (Long) request.getTimestamp();
            if (timestamp < 0)
                timestamp = Math.abs(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("LLL dd, yyyy h:m a", Locale.getDefault());
            ((TextView) findViewById(R.id.text_request_date)).setText(sdf.format(new Date(timestamp)));
        }

        if (request.getLocation_latlng() != null) {
            //TODO: Get the location name by latlng
            textLocationName.setText("VIEW IN MAP");
            textLocationName.setOnClickListener(view -> {
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("latitude", request.getLocation_latlng().get("latitude"));
                intent.putExtra("longitude", request.getLocation_latlng().get("longtitude"));
                startActivity(intent);
            });
        }else if (request.getLocationName() != null && !request.getLocationName().trim().equalsIgnoreCase("")) {
            textLocationName.setText(request.getLocationName());
        }

        // Dynamic View
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Sorted Hashmap
        Map<String, String> sorted = new TreeMap<>(request.getQuestionsAndAnswers());

        for (Map.Entry<String, String> entry : sorted.entrySet()) {

            TextView question = new TextView(this);
            question.setTextAppearance(getApplicationContext(), R.style.SettingsTextAppearance);
            TextView answer = new TextView(this);
            answer.setTextAppearance(getApplicationContext(), R.style.SettingsTextAppearance);
            TextView hiddenText = new TextView(this);

            question.setText(entry.getKey().substring(2));
            answer.setText(buildAnswerString(entry.getValue()));
            if (entry.getKey().substring(2).equalsIgnoreCase("When do you need it?")
                    || entry.getKey().substring(2).toLowerCase().startsWith("when do you need")) {
                ((TextView) findViewById(R.id.text_info_date)).setText(buildAnswerString(entry.getValue()));
            }
            answer.setTypeface(Typeface.DEFAULT_BOLD);

            question.setLayoutParams(params);
            answer.setLayoutParams(params);
            hiddenText.setLayoutParams(params);
            hiddenText.setVisibility(TextView.INVISIBLE);

            root_container.addView(question);
            root_container.addView(answer);
            root_container.addView(hiddenText);
        }

        if (request.getImages() != null && request.getImages().size() > 0) {
            TextView textImage = new TextView(this);
            TextView hiddenText = new TextView(this);

            hiddenText.setLayoutParams(params);
            hiddenText.setVisibility(TextView.INVISIBLE);
            textImage.setText("Attachment(s)");
            textImage.setTextAppearance(getApplicationContext(), R.style.SettingsTextAppearance);
            textImage.setLayoutParams(params);

            root_container.addView(textImage);

            LinearLayout.LayoutParams linear_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(linear_params);

            LinearLayout.LayoutParams image_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            image_params.setMargins(0, 20, 40, 0);
            for (String key : request.getImages().keySet()) {
                String imageUrl = request.getImages().get(key);

                SquareImageViewForService squareImageViewForService = new SquareImageViewForService(this);
                squareImageViewForService.setLayoutParams(image_params);
                squareImageViewForService.setAdjustViewBounds(true);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.raise);
                    squareImageViewForService.setStateListAnimator(stateListAnimator);
                }

                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            Glide.with(getApplicationContext()).load(downloadUrl).apply(requestOptions).into(squareImageViewForService);
                        }else {
                            Toast.makeText(InfoActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Glide.with(getApplicationContext()).load(imageUrl).apply(requestOptions).into(squareImageViewForService);
                }
                linearLayout.addView(squareImageViewForService);

                squareImageViewForService.setOnClickListener(view -> {
                    Intent intent = new Intent(InfoActivity.this, FullScreenImageRequestActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(FullScreenImageRequestActivity.IMAGE_URL_REQUEST_KEY, imageUrl);
                    intent.putExtras(bundle);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(intent);
                    } else {
                        final ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(InfoActivity.this, view, "shared");
                        startActivity(intent, options.toBundle());
                    }
                });
            }

            LinearLayout.LayoutParams scroll_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 396);
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
            horizontalScrollView.setLayoutParams(scroll_params);
            horizontalScrollView.addView(linearLayout);

            root_container.addView(horizontalScrollView);
            root_container.addView(hiddenText);

        }

    }

    private String buildAnswerString(String answers) {
        StringBuilder output = new StringBuilder();
        ArrayList<String> answerList = new ArrayList<>(Arrays.asList(answers.split(ChecklistActivity.randomString)));
        for (int i = 0; i < answerList.size(); i++) {
            if (answerList.get(i).trim().equalsIgnoreCase("")) {
                output.append("--");
            }else {
                output.append(answerList.get(i));
            }
            if (answerList.size() > 1 && i == answerList.size() - 2) {
                output.append(" and ");
            }else if (answerList.size() > 1 && i != answerList.size() - 1){
                output.append(", ");
            }
        }
        return output.toString();
    }

}
