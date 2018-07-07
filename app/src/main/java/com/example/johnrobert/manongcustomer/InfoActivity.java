package com.example.johnrobert.manongcustomer;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class InfoActivity extends AppCompatActivity {

    private Request request;
    private LinearLayout root_container;
    private static final String andSeparator = "xjXgGdHTpi";
    private TextView textLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpEnterTransition();
        setContentView(R.layout.activity_info);

        request = (Request) getIntent().getSerializableExtra("request");
        root_container = findViewById(R.id.container_layout);
        textLocationName = findViewById(R.id.text_info_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        if (request != null) {
            generateChecklist(request);
        }

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

    private void generateChecklist(Request request) {

        // Static View
        if (request.getKey() != null) {
            ((TextView) findViewById(R.id.text_info_id)).setText(request.getKey());
        }
        if (request.getLocation_latlng() != null) {
            //TODO: Get the location name by latlng
            textLocationName.setText(String.valueOf(request.getLocation_latlng().get("latitude")));
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
            if (entry.getKey().substring(2).equalsIgnoreCase("When do you need it?")) {
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
            image_params.setMargins(0, 0, 10, 0);
            for (String key : request.getImages().keySet()) {
                String imageUrl = request.getImages().get(key);

                SquareImageViewForService squareImageViewForService = new SquareImageViewForService(this);
                squareImageViewForService.setLayoutParams(image_params);

                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            Picasso.get().load(downloadUrl).placeholder(R.drawable.loading_image).into(squareImageViewForService);
                        }else {
                            Toast.makeText(InfoActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.loading_image).into(squareImageViewForService);
                }
                linearLayout.addView(squareImageViewForService);
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
        ArrayList<String> answerList = new ArrayList<>(Arrays.asList(answers.split(andSeparator)));
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
