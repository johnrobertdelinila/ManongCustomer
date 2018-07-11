package com.example.johnrobert.manongcustomer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Random;

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
    private ArrayList<Boolean> isInput;
    private ArrayList<ArrayList<String>> answers;
    private ArrayList<Integer> viewTypes;
    private ArrayList<ChecklistAdapter> checklistAdapters;

    private static final String randomString = "xjXgGdHTpi";

    private ProgressDialog progressDialog;

    DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("Request");
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private ArrayList<String> downloadUrls;
    public ArrayList<Bitmap> bitmap_images;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FloatingActionButton fabLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        String checklist_title = getIntent().getStringExtra("checklist_title");
        if (checklist_title != null) {
            ((TextView) findViewById(R.id.all_content_element_share_text)).setText(checklist_title);
        }
        final ImageView rowImage = findViewById(R.id.all_element_share_image);
        rowImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_black_24dp));
        rowImage.setOnClickListener(view -> onBackPressed());

        fabLogin = findViewById(R.id.fab_login);
        fabLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            int color = ContextCompat.getColor(this, R.color.colorControlActivated);
            if (AndroidVersionUtil.isGreaterThanL()) {
                FabTransform.addExtras(intent, color, R.drawable.ic_person_black_24dp);
            }
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this,
                            view,
                            getString(R.string.transition_name_login));
            ActivityCompat.startActivity(this,
                    intent,
                    optionsCompat.toBundle());
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.manong_please_wait));

        service = (Service) getIntent().getSerializableExtra("service");
        checklistAdapters = new ArrayList<>();

        service.getTitle().add("Summary");
        service.getSubtitle().add("Confirm Request");

        mySteps = service.getTitle().toArray(new String[service.getTitle().size()]);
        subTitles = service.getSubtitle().toArray(new String[service.getSubtitle().size()]);
        isInput = service.getIsInput();
        viewTypes = service.getViewTypes();
        answers = service.getAnswers();

        isInput.add(false);
        viewTypes.add(6);
        answers.add(null);

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

        fabLogin.animate().scaleX(0).scaleY(0).setDuration(150).start();
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view;
        view = createView(viewTypes.get(stepNumber), answers.get(stepNumber), isInput.get(stepNumber), stepNumber);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(int viewType, ArrayList<String> answers, Boolean isInput, final int stepNumber) {

        if (viewType == 6) {
            LayoutInflater inflater = LayoutInflater.from(getBaseContext());
            checklistAdapters.add(new ChecklistAdapter(ChecklistActivity.this, false, false, answers, isInput, false, stepNumber));
            return inflater.inflate(R.layout.layout_summary, null, false);
        }else {
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
        if (currentStepNumber < Arrays.asList(subTitles).size()) {
            if (Arrays.asList(subTitles).get(currentStepNumber).equals("")) {
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
                    if (Arrays.asList(subTitles).get(currentStepNumber).equals("")) {
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

        if (bitmap_images != null) {
            bitmap_images.clear();
        }else {
            bitmap_images = new ArrayList<>();
        }
        for (int i = 0; i < Arrays.asList(mySteps).size() - 1; i++) {
            if (viewTypes.get(i) == 5) {
                bitmap_images = checklistAdapters.get(i).getSelectedImages();
                break;
            }
        }

        Request request = new Request();
        int max_images = 0;
        if (bitmap_images != null && bitmap_images.size() > 0) {
            max_images = bitmap_images.size();
            HashMap<String, String> images = new HashMap<>();
            for (int i = 0; i < max_images; i++) {
                images.put("image_" + i, LOADING_IMAGE_URL);
            }
            request.setImages(images);
        }
        request.setQuestionsAndAnswers(getAllQuestionsAndAnswers());
        request.setLocation_latlng(getLocationLatLng());
        request.setServiceName(service.getServiceName());
        request.setUserId(user.getUid());
        request.setTimestamp(ServerValue.TIMESTAMP);
        if (service.getLocationName() != null) {
            request.setLocationName(service.getLocationName());
        }

        progressDialog.show();
        final int finalMax_images = max_images;
        // Insert data to database
        requestRef.push().setValue(request, (databaseError, databaseReference) -> {
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
        progressDialog.dismiss();
        Intent intent = new Intent(ChecklistActivity.this, ServiceDetailActivity.class);
        intent.putExtra("iyot", "iyot");
        setResult(RESULT_OK, intent);
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
            requestRef.child(key).child("images")
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
        for(String question: mySteps) {
            if (!question.equalsIgnoreCase("Attachments (optional)") && !question.equalsIgnoreCase("Summary")) {
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
