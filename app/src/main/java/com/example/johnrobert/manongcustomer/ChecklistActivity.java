package com.example.johnrobert.manongcustomer;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class ChecklistActivity extends AppCompatActivity implements VerticalStepperForm {

    public VerticalStepperFormLayout verticalStepperFormLayout;
    public int currentStepNumber = -1;
    public static final int PICK_IMAGE_REQUEST_CODE = 2006;
    public static int resultStepNumber = -1;
    public static final int FINISH_ACTIVITY = 3000;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    public Service service;
    private String[] mySteps;
    private String[] subTitles;
    private ArrayList<Boolean> isInput, isOptional;
    private ArrayList<ArrayList<String>> answers;
    private ArrayList<Integer> viewTypes;
    private ArrayList<ChecklistAdapter> checklistAdapters;
    private ArrayList<String> downloadUrls;
    public ArrayList<Bitmap> bitmap_images;

    public static final String randomString = "xjXgGdHTpi";

    private ProgressDialog progressDialog;

    private FirebaseUser user = ManongActivity.mUser;

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private FloatingActionButton fabLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        String checklist_title = getIntent().getStringExtra("checklist_title");
        if (checklist_title != null) {
            ((TextView) findViewById(R.id.all_content_element_share_text)).setText(checklist_title);
        }

        ImageView rowImage = findViewById(R.id.all_element_share_image);
        rowImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_black_24dp));
        rowImage.setOnClickListener(view -> onBackPressed());

        fabLogin = findViewById(R.id.fab_login);
//        fabLogin.setOnClickListener(view -> {
//            Intent intent = new Intent(this, LoginActivity.class);
//            int color = ContextCompat.getColor(this, R.color.colorControlActivated);
//            if (AndroidVersionUtil.isGreaterThanL()) {
//                FabTransform.addExtras(intent, color, R.drawable.ic_person_black_24dp);
//            }
//            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
//                    .makeSceneTransitionAnimation(this,
//                            view,
//                            getString(R.string.transition_name_login));
//            ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
//        });

        progressDialog = new ProgressDialog(this, R.style.ManongDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.manong_please_wait));

        service = (Service) getIntent().getSerializableExtra("service");
        checklistAdapters = new ArrayList<>();

//        service.getTitle().add("Summary");
//        service.getSubtitle().add("Confirm Request");

        mySteps = service.getTitle().toArray(new String[service.getTitle().size()]);
        subTitles = service.getSubtitle().toArray(new String[service.getSubtitle().size()]);
        isInput = service.getIsInput();
        isOptional = service.getIsOptional();
        viewTypes = service.getViewTypes();
        answers = service.getAnswers();

//        isInput.add(false);
//        viewTypes.add(6);
//        answers.add(null);

        downloadUrls = new ArrayList<>();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        if (verticalStepperFormLayout == null) {
            verticalStepperFormLayout = findViewById(R.id.all_element_share_no_share_text);

            int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
            int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
            int colorAccent = ContextCompat.getColor(getApplicationContext(), R.color.colorControlActivated);

            VerticalStepperFormLayout.Builder.newInstance(verticalStepperFormLayout, mySteps, this, this)
                    .primaryColor(colorPrimaryDark)
                    .primaryDarkColor(colorAccent)
                    .displayBottomNavigation(false)
                    .showVerticalLineWhenStepsAreCollapsed(false)
                    .stepsSubtitles(subTitles)
                    .init();
        }

        if (user == null) {
            fabLogin.animate().scaleX(1).scaleY(1).setDuration(150).start();
        }else {
            fabLogin.animate().scaleX(0).scaleY(0).setDuration(350).start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        fabLogin.animate().scaleX(0).scaleY(0).setDuration(150).start();
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view;
        view = createView(viewTypes.get(stepNumber), answers.get(stepNumber), isInput.get(stepNumber), stepNumber);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(int viewType, ArrayList<String> answers, Boolean isInput, final int stepNumber) {

//        if (viewType == 6) {
//            LayoutInflater inflater = LayoutInflater.from(getBaseContext());
//            checklistAdapters.add(new ChecklistAdapter(ChecklistActivity.this, null, null, answers, isInput, null, stepNumber));
//            return inflater.inflate(R.layout.layout_summary, null, false);
//        }else {
//            Boolean isTextField = null;
//            Boolean isCheckBox = null;
//            Boolean isaAttachment = null;
//            if (viewType == 1) {
//                isCheckBox = true;
//                isTextField = null;
//            }else if (viewType == 2) {
//                isCheckBox = false;
//                isTextField = null;
//            }else  if (viewType == 3) {
//                answers = null;
//                isTextField = true;
//            }else if (viewType == 4) {
//                answers = null;
//                isTextField = false;
//            }else if (viewType == 5) {
//                answers = null;
//                isaAttachment = true;
//            }
//
//            ChecklistAdapter checklistAdapter = new ChecklistAdapter(ChecklistActivity.this, isTextField, isCheckBox, answers, isInput, isaAttachment, stepNumber);
//            checklistAdapters.add(checklistAdapter);
//
//            // STARTS HERE
//            LayoutInflater inflater = LayoutInflater.from(getBaseContext());
//            final LinearLayout view;
//
//            if (isTextField != null && !isTextField) {
//                view = (LinearLayout) inflater.inflate(R.layout.layout_listview_fordate, null, false);
//            }else if (answers != null && answers.size() <= 4){
//                view = (LinearLayout) inflater.inflate(R.layout.layout_listview_small, null, false);
//            } else {
//                view = (LinearLayout) inflater.inflate(R.layout.layout_listview, null, false);
//            }
//
//            ListView listView = view.findViewById(R.id.listView);
//            listView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
//
//            if (isTextField == null) {
//                // Setting on Touch Listener for handling the touch inside ScrollView
//                listView.setOnTouchListener((v, event) -> {
//                    // Disallow the touch request for parent scroll on touch of child view
//                    v.getParent().requestDisallowInterceptTouchEvent(true);
//                    return false;
//                });
//                listView.setOnItemClickListener((parent, view1, position, id) -> checklistAdapters.get(stepNumber).changeItemChecked(position));
//            }
//
//            listView.setAdapter(checklistAdapters.get(stepNumber));
//            return view;
//        }

        Boolean isTextField = null;
        Boolean isCheckBox = null;
        Boolean isaAttachment = null;
        if (viewType == 1) {
            isCheckBox = true;
            isTextField = null;
        }else if (viewType == 2) {
            isCheckBox = false;
            isTextField = null;
        }else  if (viewType == 3) {
            answers = null;
            isTextField = true;
        }else if (viewType == 4) {
            answers = null;
            isTextField = false;
        }else if (viewType == 5) {
            answers = null;
            isaAttachment = true;
        }

        ChecklistAdapter checklistAdapter = new ChecklistAdapter(ChecklistActivity.this, isTextField, isCheckBox, answers, isInput, isaAttachment, stepNumber);
        checklistAdapters.add(checklistAdapter);

        // STARTS HERE
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        final LinearLayout view;

        if (isTextField != null && !isTextField) {
            view = (LinearLayout) inflater.inflate(R.layout.layout_listview_fordate, null, false);
        }else if (answers != null && answers.size() <= 4){
            view = (LinearLayout) inflater.inflate(R.layout.layout_listview_small, null, false);
        } else {
            view = (LinearLayout) inflater.inflate(R.layout.layout_listview, null, false);
        }

        ListView listView = view.findViewById(R.id.listView);
        listView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);

        if (isTextField == null) {
            // Setting on Touch Listener for handling the touch inside ScrollView
            listView.setOnTouchListener((v, event) -> {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
            listView.setOnItemClickListener((parent, view1, position, id) -> checklistAdapters.get(stepNumber).changeItemChecked(position));
        }

        listView.setAdapter(checklistAdapters.get(stepNumber));
        return view;

    }

    @Override
    public void onStepOpening(int stepNumber) {
        saveCurrentStepNumber(stepNumber);
        checkIfOptional();
        verticalStepperFormLayout.setActiveStepAsCompleted();
    }

    private void saveCurrentStepNumber(int stepNumber) {
        if (stepNumber < Arrays.asList(subTitles).size()) {
            currentStepNumber = stepNumber;
        }
    }

    private void checkIfOptional() {
        if (isOptional != null && currentStepNumber < isOptional.size()) {
            if (!isOptional.get(currentStepNumber)) {
                // IF REQUIRED
                if (checklistAdapters.get(currentStepNumber).getSelectedData().size() > 0) {
                    verticalStepperFormLayout.setActiveStepAsCompleted();
                }else {
                    verticalStepperFormLayout.setActiveStepAsUncompleted(null);
                }
            }else {
                // IF OPTIONAL
                verticalStepperFormLayout.setActiveStepAsCompleted();
            }
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!isOptional.get(currentStepNumber)) {
                        // IF REQUIRED
                        if (checklistAdapters.get(currentStepNumber).getSelectedData().size() > 0) {
                            verticalStepperFormLayout.setActiveStepAsCompleted();
                        }else {
                            verticalStepperFormLayout.setActiveStepAsUncompleted(null);
                        }
                    }else {
                        // IF OPTIONAL
                        verticalStepperFormLayout.setActiveStepAsCompleted();
                    }
                    handler.postDelayed(this, 400);
                }
            };
            handler.postDelayed(runnable, 400);
        }
    }

    @Override
    public void sendData() {

        if (user == null) {
            fabLogin.performClick();
            return;
        }

        boolean fucking_go_on = true;
        for (int i = 0; i < checklistAdapters.size(); i++) {
            if (checklistAdapters.get(i).getSelectedData().size() <= 0 && Arrays.asList(subTitles).get(i).equals("")) {
                verticalStepperFormLayout.setStepAsUncompleted(i, "required");
                fucking_go_on = false;
                break;
            }
        }
        if (!fucking_go_on) {
            return;
        }

        Request request = new Request();
        request.setQuestionsAndAnswers(getAllQuestionsAndAnswers());
        request.setLocation_latlng(getLocationLatLng());
        request.setServiceName(service.getServiceName());
        request.setUserId(user.getUid());
        if (service.getLocationName() != null) {
            request.setLocationName(service.getLocationName());
        }

        if (bitmap_images != null) {
            bitmap_images.clear();
        }else {
            bitmap_images = new ArrayList<>();
        }

        for (int i = 0; i < Arrays.asList(mySteps).size(); i++) {
            if (viewTypes.get(i) == 5) {
                bitmap_images = checklistAdapters.get(i).getSelectedImages();
                break;
            }
        }

        View view = getLayoutInflater().inflate(R.layout.layout_checklist_summary, null);

        LinearLayout root_container = view.findViewById(R.id.container_layout);
        // Dynamic View
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ((TextView) view.findViewById(R.id.text_service_name)).setText(request.getServiceName());
        if (request.getLocationName() != null && !request.getLocationName().trim().equalsIgnoreCase("")) {
            ((TextView)view.findViewById(R.id.text_info_location)).setText(request.getLocationName());
        }else if (request.getLocation_latlng() != null && request.getLocation_latlng().get("latitude") != null
                && request.getLocation_latlng().get("longtitude") != null) {
            Log.e("LATLNG", String.valueOf(request.getLocation_latlng()));
            ((TextView)view.findViewById(R.id.text_info_location)).setText("SET IN MAP");
        }

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
            if (entry.getKey().substring(2).equalsIgnoreCase("Date") ||
                    entry.getKey().substring(2).toLowerCase().startsWith("when do you")) {
                ((TextView) view.findViewById(R.id.text_info_date)).setText(buildAnswerString(entry.getValue()));
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

        if (bitmap_images != null && bitmap_images.size() > 0) {
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

            for (Bitmap image : bitmap_images) {

                SquareImageViewForService squareImageViewForService = new SquareImageViewForService(this);
                squareImageViewForService.setLayoutParams(image_params);
                squareImageViewForService.setAdjustViewBounds(true);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.raise);
                    squareImageViewForService.setStateListAnimator(stateListAnimator);
                }

                squareImageViewForService.setImageBitmap(image);
                linearLayout.addView(squareImageViewForService);
            }

            LinearLayout.LayoutParams scroll_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 396);
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
            horizontalScrollView.setLayoutParams(scroll_params);
            horizontalScrollView.addView(linearLayout);

            root_container.addView(horizontalScrollView);

        }


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirm Request");
        dialog.setMessage("Please confirm your request details");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.setIcon(getDrawable(R.drawable.manong_logo));
        }else {
            dialog.setIcon(R.drawable.manong_logo);
        }

        dialog.setView(view);

        AlertDialog dialogg = dialog.create();

        LinearLayout.LayoutParams params_buttons = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        View view2 = getLayoutInflater().inflate(R.layout.layout_button_dialog, null);
        view2.findViewById(R.id.cancel_button).setOnClickListener(hehe -> dialogg.dismiss());
        view2.findViewById(R.id.submit_button).setOnClickListener(buto_uki -> {
            doobyDoobyDapDap(request);
            dialogg.dismiss();
        });
        view2.setLayoutParams(params_buttons);
        root_container.addView(view2);

        dialogg.show();
        dialogg.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

    private String buildAnswerString(String answers) {
        StringBuilder output = new StringBuilder();
        ArrayList<String> answerList = new ArrayList<>(Arrays.asList(answers.split(randomString)));
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
    
    private void doobyDoobyDapDap(Request request) {

        int max_images = 0;
        if (bitmap_images != null && bitmap_images.size() > 0) {
            max_images = bitmap_images.size();
            HashMap<String, String> images = new HashMap<>();
            for (int i = 0; i < max_images; i++) {
                images.put("image_" + i, LOADING_IMAGE_URL);
            }
            request.setImages(images);
        }

        request.setTimestamp(ServerValue.TIMESTAMP);

        progressDialog.show();
        final int finalMax_images = max_images;
        // Insert data to database
        ManongActivity.requestRef.push().setValue(request, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                // ASIKASUHIN ANG PAG UPLOAD
                if (finalMax_images > 0) {
                    for (int i = 0; i < bitmap_images.size(); i++) {
                        uploadImage(bitmap_images.get(i), finalMax_images, databaseReference.getKey(), i);
                    }
                }else {
                    finishActivityOnSuccess();
                }
            }else {
                Toast.makeText(ChecklistActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishActivityOnSuccess() {
        ServiceDetailActivity.isDone = true;
        progressDialog.dismiss();
        Toast.makeText(ChecklistActivity.this, "Request sent successfully. Please wait for the vendor respond.", Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    private void uploadImage(Bitmap bitmap, final int maxImages, final String key, int count) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final StorageReference imageUpload = firebaseStorage.getReference().child("images").child(randomName() + ".jpg");
        UploadTask uploadTask = imageUpload.putBytes(data);

        uploadTask
                .addOnFailureListener(e -> Log.e("ERROR UPLOADING", e.getMessage()));

        // Get all the download url and upload to database
        uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get download URL
                    return imageUpload.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    downloadUrls.add(task.getResult().toString());
                    if (downloadUrls.size() == maxImages) {
                        Log.e("URL SIZE", String.valueOf(downloadUrls.size()));
                        uploadImageUrlsToFirebase(key);
                    }
                });

        if ((count + 1) == maxImages) {
            finishActivityOnSuccess();
        }
    }

    private void uploadImageUrlsToFirebase(String key) {
        for (int i = 0; i < downloadUrls.size(); i++) {
            HashMap<String, Object> image = new HashMap<>();
            image.put("image_" + i, downloadUrls.get(i));
            ManongActivity.requestRef.child(key).child("images")
                    .updateChildren(image)
                    .addOnFailureListener(e -> Log.e("FAILED", e.getMessage()));
        }
    }

    private String randomName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private String combineAnswers(ArrayList<String> data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            if (i != data.size() - 1) {
                builder.append(data.get(i));
                builder.append(randomString);
            }else {
                builder.append(data.get(i));
            }
        }
        return builder.toString();
    }

    private HashMap<String, String> getAllQuestionsAndAnswers() {
        HashMap<String, String> questions = new HashMap<>();
        int incremental = 1, iteration = 0;
        for(String question: subTitles) {
            if (!question.toLowerCase().startsWith("you may attach up to") && !question.toLowerCase().startsWith("attachments")) {
                questions.put(incremental + "-" + question, combineAnswers(checklistAdapters.get(iteration).getSelectedData()));
                incremental++;
            }
            iteration++;
        }
        return questions;
    }

    private HashMap<String, Double> getLocationLatLng() {
        HashMap<String, Double> location_latlng = new HashMap<>();
        location_latlng.put("latitude", service.getLatitude());
        location_latlng.put("longtitude", service.getLongtitude());
        return location_latlng;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(ChecklistActivity.this.getContentResolver(), filePath);
//                selectedImages.add(image);
                checklistAdapters.get(resultStepNumber).insertBitmap(image);
            }catch (Exception e) {

                e.printStackTrace();
                Log.e("IMAGE ERROR", e.getMessage());
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }
}
