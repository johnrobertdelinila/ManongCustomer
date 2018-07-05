package com.example.johnrobert.manongcustomer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class ChecklistAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> arrayList, selectedAnswers;
    private LayoutInflater inflater;
    private Boolean isInput, isCheckBox = null, isTextField = null, isSetLast = false, resetEditText = false, isAttachment;
    private String editTextValue = "", randomStringsForGetView = "FA859Zr8bJ";
    private ArrayList<Integer> selectedCheck;
    private DatePickerDialog datePicker;
    private int selectedPosition = -1, stepNumber;
    private ArrayList<Bitmap> selectedImages;
    private Bitmap imageFromActivity;

    private ArrayList<ImageView> imageViews;
    public ImageView imageView1, imageView2, imageView3;

    public static int init = 0;
    private static final int EDIT_TEXT_CODE = -1996, PICK_IMAGE_REQUEST_CODE = 2006, TYPE_EDITTEXT = 0, TYPE_REGULAR = 1, TYPE_TEXTFIELD = 2, TYPE_DATE = 3, TYPE_ATTACHMENT = 4;

    public ChecklistAdapter(Context context, Boolean isTextField, Boolean isCheckBox, ArrayList<String> checkList, Boolean isInput, Boolean isAttachment, int stepNumber) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        selectedAnswers = new ArrayList<>();
        this.isCheckBox = isCheckBox;
        this.isInput = isInput;
        this.isTextField = isTextField;
        this.isAttachment = isAttachment;
        this.stepNumber = stepNumber;
        if (checkList != null && isCheckBox != null) {
            // RADIO OR CHECK
            this.arrayList = checkList;
            if (isCheckBox) {
                selectedCheck = new ArrayList<>();
            }
        }else {
            // TEXT_FIELD, DATE OR ATTACHMENTS
            this.arrayList = new ArrayList<>();
            arrayList.add(randomStringsForGetView);
            this.selectedImages = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                Bitmap bitmap = null;
                this.selectedImages.add(bitmap);
            }
            this.imageViews = new ArrayList<>();
        }
        if (isInput && arrayList.size() > 0 && !arrayList.get(0).equals(randomStringsForGetView)) {
            if (init == 0) {
                arrayList.add(randomStringsForGetView);
//                Toast.makeText(context, "YAYAYAYAYAYAYAYYAYAYAYA", Toast.LENGTH_LONG).show();
            }
        }

        init++;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        if (isAttachment != null && isAttachment && arrayList.get(0).equals(randomStringsForGetView)) {
            return TYPE_ATTACHMENT;
        }else {
            if (isTextField != null && isCheckBox == null  && isTextField) {
                // TEXT FIELD VIEW
                return TYPE_TEXTFIELD;
            }else if (isTextField != null && isCheckBox == null  && !isTextField) {
                // DATE PICKER VIEW
                return TYPE_DATE;
            } else {
                if (isInput && position == getCount() - 1) {
                    // INPUT VIEW
                    return TYPE_EDITTEXT;
                }else {
                    // RADIO OR CHECK
                    return TYPE_REGULAR;
                }
            }
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {

            viewHolder = new ViewHolder();

            if (getItemViewType(position) == TYPE_ATTACHMENT) {
                view = inflater.inflate(R.layout.list_attachment_item, parent, false);
                viewHolder.btnAttachment = view.findViewById(R.id.btn_attachment);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    viewHolder.btnAttachment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_black__24dp, 0, 0, 0);
                }
                viewHolder.imageView11 = view.findViewById(R.id.imageView1);
                viewHolder.imageView22 = view.findViewById(R.id.imageView2);
                viewHolder.imageView33 = view.findViewById(R.id.imageView3);
                imageViews.add(viewHolder.imageView11);
                imageViews.add(viewHolder.imageView22);
                imageViews.add(viewHolder.imageView33);

                chokChakChenes();

            }else {
                if (getItemViewType(position) == TYPE_TEXTFIELD) {
                    view = inflater.inflate(R.layout.list_textfield_item, parent, false);
                    viewHolder.textFieldDetail = view.findViewById(R.id.text_field_detail);
                    viewHolder.editTextDetail = view.findViewById(R.id.edit_text_detail);
                }else if (getItemViewType(position) == TYPE_DATE) {
                    view = inflater.inflate(R.layout.list_date_item, parent, false);
                    viewHolder.checkListText = view.findViewById(R.id.date_text);
                    setDatePicker(viewHolder.checkListText);
                } else {
                    if (getItemViewType(position) == TYPE_EDITTEXT) {
                        view = inflater.inflate(R.layout.list_input_item, parent, false);
                        viewHolder.textFieldDetail = view.findViewById(R.id.editText);
                        viewHolder.editTextDetail = view.findViewById(R.id.extended_edit_text);
                    } else {
                        if (isCheckBox != null && isCheckBox) {
                            view = inflater.inflate(R.layout.list_check_item, parent, false);
                            viewHolder.checkBox = view.findViewById(R.id.checkbox);
                            viewHolder.checkListText = view.findViewById(R.id.textView);
                        }else {
                            view = inflater.inflate(R.layout.list_radio_item, parent, false);
                            viewHolder.radioButton = view.findViewById(R.id.radioButton);
                            viewHolder.checkListText = view.findViewById(R.id.textView);
                        }
                    }
                }
            }

            view.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (getItemViewType(position) == TYPE_ATTACHMENT) {
            viewHolder.btnAttachment.setOnClickListener(v -> {
                if (ChecklistActivity.resultStepNumber == -1) {
                    ChecklistActivity.resultStepNumber = stepNumber;
                }
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                }
                ((Activity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
            });
            for (int i = 0; i < imageViews.size(); i++) {
                final int hello = i;
                imageViews.get(i).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        resetImage(imageViews.get(hello), hello);
                        return false;
                    }
                });
            }
        }else {
            if (getItemViewType(position) == TYPE_TEXTFIELD) {
                viewHolder.textFieldDetail.setSimpleTextChangeWatcher((s, b) -> {
                    editTextValue = s.trim();
                    changeItemChecked(EDIT_TEXT_CODE);
                });
            }else if (getItemViewType(position) == TYPE_DATE) {
                viewHolder.checkListText.setOnClickListener(v -> datePicker.show());
            } else {
                if (getItemViewType(position) == TYPE_EDITTEXT) {
                    if (resetEditText) {
                        viewHolder.editTextDetail.setText("");
                        resetEditText = false;
                    }
                    viewHolder.textFieldDetail.setSimpleTextChangeWatcher((s, b) -> {
                        editTextValue = s.trim();
                        changeItemChecked(EDIT_TEXT_CODE);
                        if (!editTextValue.equals("") && isCheckBox != null && !isCheckBox) {
                            viewHolder.editTextDetail.requestFocus();
                        }
                    });
                    viewHolder.editTextDetail.setOnClickListener(v -> {
                        Log.e("CLICK", "IM CLICK");
                        viewHolder.textFieldDetail.setHasFocus(true);
                    });
                }else {
                    if (isCheckBox != null && isCheckBox) {
                        viewHolder.checkListText.setText(arrayList.get(position));
                        viewHolder.checkBox.setChecked(selectedCheck.contains(position));
                        viewHolder.checkBox.setTag(position);
                        viewHolder.checkBox.setOnClickListener(v -> changeItemChecked((Integer) v.getTag()));
                    }else {
                        viewHolder.checkListText.setText(arrayList.get(position));
                        viewHolder.radioButton.setChecked(position == selectedPosition);
                        viewHolder.radioButton.setTag(position);
                        viewHolder.radioButton.setOnClickListener(v -> changeItemChecked((Integer) v.getTag()));
                    }
                }
            }
        }

        return view;
    }

    private void chokChakChenes() {
        addImageInArray(imageFromActivity);
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i) != null) {
                changeImage(imageViews.get(i), selectedImages.get(i), i);
            }else {
                resetImage(imageViews.get(i), i);
            }
        }
    }

    public void changeItemChecked(int position) {
        if (isTextField != null && isCheckBox == null && !isTextField) {
            selectedAnswers.clear();
            selectedAnswers.add(editTextValue);
        }else if(isTextField != null && isCheckBox == null && isTextField) {
            selectedAnswers.clear();
            if (!editTextValue.equals("")) {
                selectedAnswers.add(editTextValue);
            }
        }else {
            if (position != EDIT_TEXT_CODE) {
                if (isCheckBox != null && isCheckBox) {
                    if (selectedCheck.contains(position)) {
                        selectedCheck.remove(Integer.valueOf(position));
                    }else {
                        selectedCheck.add(position);
                    }
                    addAnswer(arrayList, selectedCheck, isInput, editTextValue, 0);
                }else {
                    selectedPosition = position;
                    editTextValue = "";
                    addAnswer(arrayList, null, isInput, editTextValue, selectedPosition);
                }
                notifyDataSetChanged();
            }else {
                if (!editTextValue.equals("")) {
                    if (isCheckBox != null && isCheckBox) {
                        if (isSetLast) {
                            selectedAnswers.set(selectedAnswers.size() - 1, editTextValue);
                        }else {
                            selectedAnswers.add(editTextValue);
                            isSetLast = true;
                        }
                    }else {

                        selectedAnswers.clear();
                        selectedAnswers.add(editTextValue);

                        selectedPosition = -1;
                        notifyDataSetChanged();
                    }
                }else if (editTextValue.equals("")){
                    if (isSetLast && selectedAnswers.size() > 0) {
                        selectedAnswers.remove(selectedAnswers.size() - 1);
                        isSetLast = false;
                    }else {
                        if (selectedPosition < 0) {
                            selectedAnswers.clear();
                        }
                    }
                }

            }
        }
    }

    private void addAnswer(ArrayList<String> questions, ArrayList<Integer> positions, Boolean isThereInput, String editTextValue, int selectedRadio) {
        selectedAnswers.clear();
        if (positions != null) {
            for (int position: positions) {
                selectedAnswers.add(questions.get(position));
            }
            if (isThereInput) {
                if (!editTextValue.equals("")) {
                    selectedAnswers.add(editTextValue);
                }
            }
        }else {
            selectedAnswers.add(questions.get(selectedRadio));
            if (isThereInput) {
                if (!editTextValue.equals("")) {
                    selectedAnswers.clear();
                    selectedAnswers.add(editTextValue);
                    resetEditText = true;
                }else {
                    Log.e("ADD", "ADD - " + String.valueOf(selectedPosition));
                    resetEditText = true;
                }
            }else {
                resetEditText = true;
            }
        }
    }

    private class ViewHolder {
        TextView checkListText;
        RadioButton radioButton;
        CheckBox checkBox;
        TextFieldBoxes textFieldDetail;
        ExtendedEditText editTextDetail;
        Button btnAttachment;
        ImageView imageView11;
        ImageView imageView22;
        ImageView imageView33;
    }

    private void setDatePicker(final TextView textView) {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final String[] month_name = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        datePicker = new DatePickerDialog(context, R.style.DatePickerDialogTheme,
                (view, year, month, dayOfMonth) -> {
                    String datePick = Arrays.asList(month_name).get(month) + " " + String.valueOf(dayOfMonth) + ", " + String.valueOf(year);
                    textView.setText(datePick);
                    editTextValue = datePick;
                    selectedAnswers.clear();
                    selectedAnswers.add(editTextValue);
                }, mYear, mMonth, mDay);
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    public ArrayList<String> getSelectedData() {
        return selectedAnswers;
    }

    public ArrayList<Bitmap> getSelectedImages() {
        ArrayList<Bitmap> final_images = new ArrayList<>();
        for (Bitmap image: selectedImages) {
            if (image != null) {
                final_images.add(image);
            }
        }
        if (final_images.size() == 0 || selectedImages == null) {
            return null;
        }else {
            return final_images;
        }
    }

//    public void setImage(Bitmap image) {
//        if (selectedImages.get(0) == null) {
//            changeImage(imageView1, image);
//        }else if (selectedImages.get(1) == null) {
//            changeImage(imageView2, image);
//        }else if (selectedImages.get(2) == null) {
//            changeImage(imageView3, image);
//        }
//    }

    private void addImageInArray(Bitmap bitmap) {
        if (bitmap != null) {
            for (int i = 0; i < selectedImages.size(); i++) {
                if (selectedImages.get(i) == null) {
                    selectedImages.set(i, bitmap);
                    break;
                }
            }
        }
    }

    private void changeImage(ImageView imageView, Bitmap image, int count) {
        imageView.setImageDrawable(null);
        imageView.setImageBitmap(image);
        selectedImages.set(count, image);
        selectedAnswers.add("");
    }

    private void resetImage(ImageView imageView, int count) {
        imageView.setImageDrawable(null);
        imageView.setBackgroundResource(R.drawable.no_image);
        selectedImages.set(count, null);
        if (selectedAnswers.size() != 0) {
            selectedAnswers.remove(selectedAnswers.size() - 1);
        }
    }

    public ImageView getImage() {
        return imageView1;
    }

    public void insertBitmap(Bitmap image) {
        imageFromActivity = image;
        chokChakChenes();
    }


}
