package com.example.foodybiker;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setup extends AppCompatActivity {

    private CircleImageView profilePicture;
    private ImageButton save;
    private FloatingActionButton editImage;
    private EditText name, email, phoneNumber;
    private TextView errorName;
    private TextView errorMail;
    private TextView errorPhone;
    private TextView errorAddress;
    private TextView monday, thursday, wednesday, tuesday, friday, saturday, sunday, address;
    private CheckBox monC, thuC, wedC, tueC, friC, satC, sunC;
    private final int GALLERY_REQUEST_CODE = 1;
    private final int REQUEST_CAPTURE_IMAGE = 100;
    private final String PROFILE_IMAGE = "ProfileImage.jpg";
    private final String PLACEHOLDER_CAMERA="PlaceCamera.jpg";
    private String placeholderPath;
    private File storageDir;
    private TextView tv;
    private int caller;
    private boolean unchanged, addressCheck, nameCheck, numberCheck, mailCheck;
    private String dialogCode = "ok";
    private String openHour, closeHour;
    private AlertDialog dialogDism;
    private TimePickerDialog timePicker;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor edit;
    private String pathImage;
    private final String MAIN_DIR = "user_utils";

    class Position {
        public String address;
        public Double latitude, longitude;

        public Position(String address) {
            this.address = address;
            this.latitude = null;
            this.longitude = null;
        }
    }

    private Position pos;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 9;
    private ImageButton callActivityAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        firebaseAuth = FirebaseAuth.getInstance();
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        init();
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickImageDialog();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", name.getText().toString());
        outState.putString("email", email.getText().toString());
        outState.putString("address", address.getText().toString());
        outState.putString("phoneNumber", phoneNumber.getText().toString());
        outState.putString("monTime", monday.getText().toString());
        outState.putString("tueTime", tuesday.getText().toString());
        outState.putString("wedTime", wednesday.getText().toString());
        outState.putString("thuTime", thursday.getText().toString());
        outState.putString("friTime", friday.getText().toString());
        outState.putString("satTime", saturday.getText().toString());
        outState.putString("sunTime", sunday.getText().toString());
        outState.putBoolean("monState", monC.isChecked());
        outState.putBoolean("tueState", tueC.isChecked());
        outState.putBoolean("wedState", wedC.isChecked());
        outState.putBoolean("thuState", thuC.isChecked());
        outState.putBoolean("friState", friC.isChecked());
        outState.putBoolean("satState", satC.isChecked());
        outState.putBoolean("sunState", sunC.isChecked());
        outState.putString("dialog", dialogCode);
        outState.putString("openHour", openHour);
        outState.putInt("caller", caller);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        File f = new File(storageDir, PLACEHOLDER_CAMERA);

        if(f.exists())
            profilePicture.setImageURI(Uri.fromFile(f));

        name.setText(savedInstanceState.getString("name", getResources().getString(R.string.name_hint)));
        email.setText(savedInstanceState.getString("email", getResources().getString(R.string.email_hint)));
        address.setText(savedInstanceState.getString("address", getResources().getString(R.string.address_hint)));
        phoneNumber.setText(savedInstanceState.getString("phoneNumber", getResources().getString(R.string.phone_hint)));
        monday.setText(savedInstanceState.getString("monTime", getResources().getString(R.string.free)));
        tuesday.setText(savedInstanceState.getString("tueTime", getResources().getString(R.string.free)));
        wednesday.setText(savedInstanceState.getString("wedTime", getResources().getString(R.string.free)));
        thursday.setText(savedInstanceState.getString("thuTime", getResources().getString(R.string.free)));
        friday.setText(savedInstanceState.getString("friTime", getResources().getString(R.string.free)));
        saturday.setText(savedInstanceState.getString("satTime", getResources().getString(R.string.free)));
        sunday.setText(savedInstanceState.getString("sunTime", getResources().getString(R.string.free)));
        monC.setChecked(savedInstanceState.getBoolean("monState", false));
        tueC.setChecked(savedInstanceState.getBoolean("tueState", false));
        wedC.setChecked(savedInstanceState.getBoolean("wedState", false));
        thuC.setChecked(savedInstanceState.getBoolean("thuState", false));
        friC.setChecked(savedInstanceState.getBoolean("friState", false));
        satC.setChecked(savedInstanceState.getBoolean("satState", false));
        sunC.setChecked(savedInstanceState.getBoolean("sunState", false));
        caller = savedInstanceState.getInt("caller", 0);
        openHour = savedInstanceState.getString("openHour", null);

        String dialogPrec = savedInstanceState.getString("dialog");

        if (dialogPrec != null && dialogPrec.compareTo("ok") != 0) {
            if (dialogPrec.compareTo("pickImage") == 0) {
                showPickImageDialog();
            } else if (dialogPrec.compareTo("back") == 0) {
                onBackPressed();
            } else if (dialogPrec.compareTo("firstTime") == 0) {
                if (caller != 0) {
                    showPickTime(findViewById(caller));
                }
            } else if (dialogPrec.compareTo("secondTime") == 0){
                if (caller != 0) {
                    showSecondPicker();
                }
            }
        }

        updateButtons();
        name.clearFocus();
        email.clearFocus();
        address.clearFocus();
        phoneNumber.clearFocus();
    }

    protected void onPause(){
        super.onPause();
        if (dialogDism != null){
            dialogDism.dismiss();
        }
        if (timePicker != null){
            timePicker.dismiss();
        }
    }

    private void updateSave(){
        if(nameCheck && numberCheck && addressCheck && mailCheck){
            save.setImageResource(R.drawable.save_white);
            save.setEnabled(true);
            save.setClickable(true);
        }else{
            save.setImageResource(R.drawable.save_dis);
            save.setEnabled(false);
            save.setClickable(false);
        }
    }

    private void checkName(){
        String username = name.getText().toString();
        String regx = "^[\\p{L} .'-]+$";
        View errorLine = findViewById(R.id.name_error_line);
        Pattern regex = Pattern.compile(regx);
        Matcher matcher = regex.matcher(username);

        if(!matcher.matches()){
            nameCheck = false;
            errorName.setText(getResources().getString(R.string.error_name));
            errorLine.setBackgroundColor(getResources().getColor(R.color.errorColor,this.getTheme()));
            errorLine.setAlpha(1);

        }else{
            nameCheck = true;
            errorName.setText("");
            errorLine.setAlpha(0.2f);
            errorLine.setBackgroundColor(Color.BLACK);
        }

        updateSave();
    }

    private void checkNumber(){
        String regexpPhone = "^(([+]|00)39)?(3[1-6][0-9])(\\d{7})$";
        final String userNumber = phoneNumber.getText().toString();

        View errorLine = findViewById(R.id.number_error_line);

        if(!Pattern.compile(regexpPhone).matcher(userNumber).matches()){
            numberCheck = false;
            errorPhone.setText(getResources().getString(R.string.error_number));
            errorLine.setBackgroundColor(getResources().getColor(R.color.errorColor,this.getTheme()));
            errorLine.setAlpha(1);
        }else{
            numberCheck = true;
            errorPhone.setText("");
            errorLine.setAlpha(0.2f);
            errorLine.setBackgroundColor(Color.BLACK);
        }

        updateSave();
    }

    private void checkMail(){
        View errorLine = findViewById(R.id.email_error_line);
        String regexpEmail = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        final String emailToCheck = email.getText().toString();

        if(!Pattern.compile(regexpEmail).matcher(emailToCheck).matches()) {
            errorMail.setText(getResources().getString(R.string.error_email));
            mailCheck = false;
            errorLine.setBackgroundColor(getResources().getColor(R.color.errorColor, this.getTheme()));
            errorLine.setAlpha(1);
        }else{
            mailCheck = true;
            errorMail.setText("");
            errorLine.setAlpha(0.2f);
            errorLine.setBackgroundColor(Color.BLACK);
        }

        updateSave();
    }

    private void checkAddress(){
        View errorLine = findViewById(R.id.address_error_line);
        String regexpAddress = "^(?=\\s*\\S).*$";
        final String addressToCheck = address.getText().toString();

        if(!Pattern.compile(regexpAddress).matcher(addressToCheck).matches()) {
            errorAddress.setText(getResources().getString(R.string.error_address));
            addressCheck = false;
            errorLine.setBackgroundColor(getResources().getColor(R.color.errorColor, this.getTheme()));
            errorLine.setAlpha(1);
        }else{
            addressCheck = true;
            errorAddress.setText("");
            errorLine.setAlpha(0.2f);
            errorLine.setBackgroundColor(Color.BLACK);
        }

        updateSave();
    }

    private  void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private void pickFromCamera(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager())!= null){

            File photoFile = createOrReplacePlaceholder();

            if(photoFile!=null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.foodybiker",
                        photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    private void init(){
        addressCheck = true;
        numberCheck = true;
        nameCheck = true;
        mailCheck = true;
        unchanged = true;
        this.profilePicture = findViewById(R.id.profilePicture);
        this.editImage = findViewById(R.id.edit_profile_picture);
        this.name = findViewById(R.id.userName);
        this.email = findViewById(R.id.emailAddress);
        this.address = findViewById(R.id.address);
        this.phoneNumber = findViewById(R.id.phoneNumber);
        this.monday = findViewById(R.id.timeMonday);
        this.tuesday = findViewById(R.id.timeTuesday);
        this.wednesday = findViewById(R.id.timeWednesday);
        this.thursday = findViewById(R.id.timeThursday);
        this.friday = findViewById(R.id.timeFriday);
        this.saturday = findViewById(R.id.timeSaturday);
        this.sunday = findViewById(R.id.timeSunday);
        this.monC = findViewById(R.id.checkMonday);
        this.tueC = findViewById(R.id.checkTuesday);
        this.wedC = findViewById(R.id.checkWednesday);
        this.thuC = findViewById(R.id.checkThursday);
        this.friC = findViewById(R.id.checkFriday);
        this.satC = findViewById(R.id.checkSaturday);
        this.sunC = findViewById(R.id.checkSunday);

        //setup of the Shared Preferences to save value in (key, value) format
        //Shared Preferences definition
        Context context = getApplicationContext();
        sharedPref = context.getSharedPreferences("myPreference", MODE_PRIVATE);
        edit = sharedPref.edit();

        this.errorName = findViewById(R.id.name_error);
        this.errorMail = findViewById(R.id.email_error);
        this.errorPhone = findViewById(R.id.number_error);
        this.errorAddress = findViewById(R.id.address_error);
        this.save = findViewById(R.id.saveButton);

        errorName.setText("");
        errorMail.setText("");
        errorPhone.setText("");
        errorAddress.setText("");

        pathImage = null;
        if(sharedPref.contains("imgLocale"))
            pathImage = sharedPref.getString("imgLocale", "");

        if(pathImage != null){
            File profileImage = new File(pathImage);
            RequestOptions options = new RequestOptions();
            options.signature(new ObjectKey(profileImage.getName()+" "+profileImage.lastModified()));

            Glide
                    .with(profilePicture.getContext())
                    .setDefaultRequestOptions(options)
                    .load(profileImage)
                    .into(profilePicture);
        }else{
            Glide
                    .with(profilePicture.getContext())
                    .load(R.drawable.profile_placeholder)
                    .into(profilePicture);
        }

        name.setText(sharedPref.getString("name", ""));
        email.setText(sharedPref.getString("email", ""));
        address.setText(sharedPref.getString("address", ""));
        phoneNumber.setText(sharedPref.getString("phoneNumber", ""));
        monday.setText(sharedPref.getString("monTime", getResources().getString(R.string.free)));
        tuesday.setText(sharedPref.getString("tueTime", getResources().getString(R.string.free)));
        wednesday.setText(sharedPref.getString("wedTime", getResources().getString(R.string.free)));
        thursday.setText(sharedPref.getString("thuTime", getResources().getString(R.string.free)));
        friday.setText(sharedPref.getString("friTime", getResources().getString(R.string.free)));
        saturday.setText(sharedPref.getString("satTime", getResources().getString(R.string.free)));
        sunday.setText(sharedPref.getString("sunTime", getResources().getString(R.string.free)));
        monC.setChecked(sharedPref.getBoolean("monState", false));
        tueC.setChecked(sharedPref.getBoolean("tueState", false));
        wedC.setChecked(sharedPref.getBoolean("wedState", false));
        thuC.setChecked(sharedPref.getBoolean("thuState", false));
        friC.setChecked(sharedPref.getBoolean("friState", false));
        satC.setChecked(sharedPref.getBoolean("satState", false));
        sunC.setChecked(sharedPref.getBoolean("sunState", false));
        edit.apply();

        ImageButton mon = findViewById(R.id.editMonday);
        if (!monC.isChecked())
            mon.setClickable(false);
        ImageButton tue = findViewById(R.id.editTuesday);
        if (!tueC.isChecked())
            tue.setClickable(false);
        ImageButton wed = findViewById(R.id.editWednesday);
        if (!wedC.isChecked())
            wed.setClickable(false);
        ImageButton thu = findViewById(R.id.editThursday);
        if (!thuC.isChecked())
            thu.setClickable(false);
        ImageButton fri = findViewById(R.id.editFriday);
        if (!friC.isChecked())
            fri.setClickable(false);
        ImageButton sat = findViewById(R.id.editSaturday);
        if (!satC.isChecked())
            sat.setClickable(false);
        ImageButton sun = findViewById(R.id.editSunday);
        if (sunC.isChecked())
            sun.setClickable(false);

        //onTextChange to notify the user that there are fields that are not saved
        this.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkName();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String check = sharedPref.getString("name", null);
                if (check != null && check.compareTo(editable.toString()) != 0){
                    unchanged = false;
                }
            }
        });
        this.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkMail();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String check = sharedPref.getString("email", null);
                if (check != null && check.compareTo(editable.toString()) != 0){
                    unchanged = false;
                }
            }
        });
        this.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkNumber();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String check = sharedPref.getString("phoneNumber", null);
                if (check != null && check.compareTo(editable.toString()) != 0){
                    unchanged = false;
                }
            }
        });
        this.address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkAddress();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String check = sharedPref.getString("address", null);
                if (check != null && check.compareTo(editable.toString()) != 0){
                    unchanged = false;
                }

                for(int i = editable.length(); i > 0; i--) {

                    if(editable.subSequence(i-1, i).toString().equals("\n"))
                        editable.replace(i-1, i, "");
                }
            }
        });

        if(pos == null)
            pos = new Position(sharedPref.getString("address", ""));

        this.callActivityAddress = findViewById(R.id.searchAddress);

        callActivityAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressActivity();
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressActivity();
            }
        });

        updateSave();
    }

    public void addressActivity() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.ApiKey);
        }

        final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getApplicationContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){

                case REQUEST_CAPTURE_IMAGE:
                    File f = new File(placeholderPath);
                    startCrop(Uri.fromFile(f));
                    break;

                case GALLERY_REQUEST_CODE:
                    if(data !=null){
                        Uri imageUri = data.getData();

                        if(imageUri != null)
                            startCrop(imageUri);
                    }
                    break;

                case  UCrop.REQUEST_CROP:
                    Bitmap bitmap = getBitmapFromFile();

                    if(bitmap != null){
                        profilePicture.setImageBitmap(bitmap);
                        File placeholder = new File(storageDir, PLACEHOLDER_CAMERA);
                        saveBitmap(bitmap, placeholder.getPath());
                        unchanged = false;
                    }
                    break;

                case AUTOCOMPLETE_REQUEST_CODE:
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    pos.address = place.getAddress();
                    pos.latitude = place.getLatLng().latitude;
                    pos.longitude = place.getLatLng().longitude;
                    address.setText(place.getAddress());
                    break;
            }
        }
    }

    private File createOrReplacePlaceholder(){

        File f = new File(storageDir, PLACEHOLDER_CAMERA);

        if(f.exists())
            this.deleteFile(f.getName());

        f = new File(storageDir, PLACEHOLDER_CAMERA);

        placeholderPath = f.getPath();

       return f;
    }

    private Bitmap getBitmapFromFile(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        File dest = new File(storageDir, PLACEHOLDER_CAMERA);
        if(!dest.exists())
            return null;
        return  BitmapFactory.decodeFile(dest.getPath(), options);
    }

    private void showPickImageDialog(){
        final Item[] items = {
                new Item(getString(R.string.alert_dialog_image_gallery), R.drawable.collections_black),
                new Item(getString(R.string.alert_dialog_image_camera), R.drawable.camera_black)
        };
        ListAdapter arrayAdapter = new ArrayAdapter<Item>(
                this,
                R.layout.alert_dialog_item,
                R.id.tv1,
                items){
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ImageView iv = v.findViewById(R.id.iv1);
                iv.setImageDrawable(getDrawable(items[position].getIcon()));
                return v;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogCode = "ok";
                dialog.dismiss();
            }
        });
        builder.setTitle(getResources().getString(R.string.alert_dialog_image_title));
        builder.setCancelable(false);
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        pickFromGallery();
                        dialogCode = "ok";
                        break;
                    case 1:
                        pickFromCamera();
                        dialogCode = "ok";
                        break;
                }
            }
        });
        dialogCode = "pickImage";
        dialogDism = builder.show();
    }

    private void startCrop(@NonNull Uri uri){
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(storageDir, PLACEHOLDER_CAMERA)));
        uCrop.withAspectRatio(1,1);
        uCrop.withMaxResultSize(450,450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(Setup.this);
    }

    private UCrop.Options getCropOptions(){
        UCrop.Options options= new UCrop.Options();

        options.setCompressionQuality(100);
        options.setHideBottomControls(true);
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
        options.setCircleDimmedLayer(true);
        options.setToolbarTitle(getResources().getString(R.string.crop_image));
        return options;
    }

    public void backToProfile(View view) {
        if (unchanged){
            super.onBackPressed();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogCode = "ok";
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogCode = "ok";
                    Setup.super.onBackPressed();
                }
            });
            builder.setTitle(getResources().getString(R.string.alert_dialog_back_title));
            builder.setMessage(getResources().getString(R.string.alert_dialog_back_message));
            builder.setCancelable(false);
            dialogCode = "back";
            dialogDism = builder.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (unchanged){
            super.onBackPressed();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogCode = "ok";
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogCode = "ok";
                    Setup.super.onBackPressed();
                }
            });
            builder.setTitle(getResources().getString(R.string.alert_dialog_back_title));
            builder.setMessage(getResources().getString(R.string.alert_dialog_back_message));
            builder.setCancelable(false);
            dialogCode = "back";
            dialogDism = builder.show();
        }
    }

    public void savedProfile(View view) {

        File f = new File(storageDir, PLACEHOLDER_CAMERA);
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(f.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),bmOptions);
            pathImage = "images/bikers/"+user.getUid() + System.currentTimeMillis()+".jpeg";
            File root = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File directory = new File(root.getPath()+File.separator+MAIN_DIR);
            final File profile = new File(sharedPref.getString("imgLocale",directory.getPath()+File.separator+firebaseAuth.getCurrentUser().getUid()+".jpg"));
            saveBitmap(bitmap, profile.getPath());

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(pathImage);
            ref.putFile(Uri.fromFile(profile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sharedPref.edit().putString("imgLocale",profile.getPath()).apply();
                        sharedPref.edit().putString("imgRemote",pathImage).apply();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Bikers/" + user.getUid());
        HashMap<String, Object> child = new HashMap<>();
        ArrayList<String> days = new ArrayList<>();
        days.add(monday.getText().toString());
        days.add(tuesday.getText().toString());
        days.add(wednesday.getText().toString());
        days.add(thursday.getText().toString());
        days.add(friday.getText().toString());
        days.add(saturday.getText().toString());
        days.add(sunday.getText().toString());
        BikerInfo info = new BikerInfo(name.getText().toString(), email.getText().toString(), address.getText().toString(), phoneNumber.getText().toString(), days);
        if(f.exists()){
            info.setPath(pathImage);
        }else{
            info.setPath(sharedPref.getString("imgRemote",""));
        }
        child.put("info", info);
        database.updateChildren(child);

        sharedPref.edit().putString("name", name.getText().toString()).apply();
        sharedPref.edit().putString("email", email.getText().toString()).apply();
        sharedPref.edit().putString("address", address.getText().toString()).apply();
        sharedPref.edit().putString("phoneNumber", phoneNumber.getText().toString()).apply();
        sharedPref.edit().putString("monTime", monday.getText().toString()).apply();
        sharedPref.edit().putString("tueTime", tuesday.getText().toString()).apply();
        sharedPref.edit().putString("wedTime", wednesday.getText().toString()).apply();
        sharedPref.edit().putString("thuTime", thursday.getText().toString()).apply();
        sharedPref.edit().putString("friTime", friday.getText().toString()).apply();
        sharedPref.edit().putString("satTime", saturday.getText().toString()).apply();
        sharedPref.edit().putString("sunTime", sunday.getText().toString()).apply();
        sharedPref.edit().putBoolean("monState", monC.isChecked()).apply();
        sharedPref.edit().putBoolean("tueState", tueC.isChecked()).apply();
        sharedPref.edit().putBoolean("wedState", wedC.isChecked()).apply();
        sharedPref.edit().putBoolean("thuState", thuC.isChecked()).apply();
        sharedPref.edit().putBoolean("friState", friC.isChecked()).apply();
        sharedPref.edit().putBoolean("satState", satC.isChecked()).apply();
        sharedPref.edit().putBoolean("sunState", sunC.isChecked()).apply();

        if(pos.latitude != null && pos.longitude != null){
            DatabaseReference databaseLoc = FirebaseDatabase.getInstance().getReference()
                    .child("Bikers").child(user.getUid()).child("info");
            HashMap<String, Object> childLoc = new HashMap<>();
            childLoc.put("latitude", pos.latitude);
            childLoc.put("longitude", pos.longitude);
            databaseLoc.updateChildren(childLoc);
        }

        Toast.makeText(getApplicationContext(), R.string.save, Toast.LENGTH_SHORT).show();
        unchanged = true;
    }

    private void saveBitmap(Bitmap bitmap,String path){
        if(bitmap!=null){
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path); //here is set your file path where you want to save or also here you can set file object directly

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showPickTime(View view) {
        int hour = 0;
        int minute = 0;
        caller = view.getId();
        switch(caller) {
            case R.id.editMonday:
                tv = findViewById(R.id.timeMonday);
                break;
            case R.id.editTuesday:
                tv = findViewById(R.id.timeTuesday);
                break;
            case R.id.editWednesday:
                tv = findViewById(R.id.timeWednesday);
                break;
            case R.id.editThursday:
                tv = findViewById(R.id.timeThursday);
                break;
            case R.id.editFriday:
                tv = findViewById(R.id.timeFriday);
                break;
            case R.id.editSaturday:
                tv = findViewById(R.id.timeSaturday);
                break;
            case R.id.editSunday:
                tv = findViewById(R.id.timeSunday);
                break;
        }

        TimePickerDialog timePicker;

        timePicker = new TimePickerDialog(this, R.style.DateTimeDialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String selHour = ""+selectedHour;
                String selMinute = ""+selectedMinute;
                if(selectedHour < 10)
                    selHour = "0"+selectedHour;
                if(selectedMinute < 10)
                    selMinute = "0"+selectedMinute;
                openHour = selHour + ":" + selMinute;
                dialogCode = "secondTime";
                showSecondPicker();
            }
        }, hour, minute, true);
        timePicker.setTitle(getResources().getString(R.string.opening_time));
        timePicker.setCancelable(false);
        timePicker.setButton(DialogInterface.BUTTON_POSITIVE,getResources().getString(R.string.okButton), timePicker);
        timePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogCode = "ok";
            }
        });
        dialogCode = "firstTime";
        timePicker.show();
    }

    private void showSecondPicker(){
        int hour = 0;
        int minute = 0;

        switch(caller) {
            case R.id.editMonday:
                tv = findViewById(R.id.timeMonday);
                break;
            case R.id.editTuesday:
                tv = findViewById(R.id.timeTuesday);
                break;
            case R.id.editWednesday:
                tv = findViewById(R.id.timeWednesday);
                break;
            case R.id.editThursday:
                tv = findViewById(R.id.timeThursday);
                break;
            case R.id.editFriday:
                tv = findViewById(R.id.timeFriday);
                break;
            case R.id.editSaturday:
                tv = findViewById(R.id.timeSaturday);
                break;
            case R.id.editSunday:
                tv = findViewById(R.id.timeSunday);
                break;
        }

        TimePickerDialog timePicker2;
        timePicker2 = new TimePickerDialog(this, R.style.DateTimeDialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String selHour = ""+selectedHour;
                String selMinute = ""+selectedMinute;
                if(selectedHour < 10)
                    selHour = "0"+selectedHour;
                if(selectedMinute < 10)
                    selMinute = "0"+selectedMinute;
                closeHour = selHour + ":" + selMinute;
                unchanged = false;
                dialogCode = "ok";
                String defHour = openHour + " - " + closeHour;
                caller = 0;
                tv.setText(defHour);
            }
        }, hour, minute, true);
        timePicker2.setTitle(getResources().getString(R.string.closing_time));
        timePicker2.setCancelable(false);
        timePicker2.setButton(DialogInterface.BUTTON_POSITIVE,getResources().getString(R.string.okButton), timePicker2);
        timePicker2.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogCode = "ok";
            }
        });
        dialogCode = "secondTime";
        timePicker2.show();
    }

    public void lockUnlock(View view) {
        String standardTime = "08:00" + " - " + "23:00";
        unchanged = false;
        CheckBox cb = findViewById(view.getId());
        switch(view.getId()) {
            case R.id.checkMonday:
                if (cb.isChecked()) {
                    monday.setText(standardTime);
                    findViewById(R.id.editMonday).setClickable(true);
                } else {
                    monday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editMonday).setClickable(false);
                }
                break;
            case R.id.checkTuesday:
                if (cb.isChecked()) {
                    tuesday.setText(standardTime);
                    findViewById(R.id.editTuesday).setClickable(true);
                } else {
                    tuesday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editTuesday).setClickable(false);
                }
                break;
            case R.id.checkWednesday:
                if (cb.isChecked()) {
                    wednesday.setText(standardTime);
                    findViewById(R.id.editWednesday).setClickable(true);
                } else {
                    wednesday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editWednesday).setClickable(false);
                }
                break;
            case R.id.checkThursday:
                if (cb.isChecked()) {
                    thursday.setText(standardTime);
                    findViewById(R.id.editThursday).setClickable(true);
                } else {
                    thursday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editThursday).setClickable(false);
                }
                break;
            case R.id.checkFriday:
                if (cb.isChecked()) {
                    friday.setText(standardTime);
                    findViewById(R.id.editFriday).setClickable(true);
                } else {
                    friday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editFriday).setClickable(false);
                }
                break;
            case R.id.checkSaturday:
                if (cb.isChecked()) {
                    saturday.setText(standardTime);
                    findViewById(R.id.editSaturday).setClickable(true);
                } else {
                    saturday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editSaturday).setClickable(false);
                }
                break;
            case R.id.checkSunday:
                if (cb.isChecked()) {
                    sunday.setText(standardTime);
                    findViewById(R.id.editSunday).setClickable(true);
                } else {
                    sunday.setText(getResources().getString(R.string.free));
                    findViewById(R.id.editSunday).setClickable(false);
                }
                break;
        }
    }

    private void updateButtons() {
        CheckBox cb;
        cb =findViewById(R.id.checkMonday);
        if (cb.isChecked()) {
            findViewById(R.id.editMonday).setClickable(true);
        } else {
            findViewById(R.id.editMonday).setClickable(false);
        }
        cb=findViewById(R.id.checkTuesday);
        if (cb.isChecked()) {
            findViewById(R.id.editTuesday).setClickable(true);
        } else {
            findViewById(R.id.editTuesday).setClickable(false);
        }
        cb = findViewById(R.id.checkWednesday);
        if (cb.isChecked()) {
            findViewById(R.id.editWednesday).setClickable(true);
        } else {
            findViewById(R.id.editWednesday).setClickable(false);
        }
        cb = findViewById(R.id.checkThursday);
        if (cb.isChecked()) {
            findViewById(R.id.editThursday).setClickable(true);
        } else {
            findViewById(R.id.editThursday).setClickable(false);
        }
        cb = findViewById(R.id.checkFriday);
        if (cb.isChecked()) {
            findViewById(R.id.editFriday).setClickable(true);
        } else {
            findViewById(R.id.editFriday).setClickable(false);
        }
        cb=findViewById(R.id.checkSaturday);
        if (cb.isChecked()) {
            findViewById(R.id.editSaturday).setClickable(true);
        } else {
            findViewById(R.id.editSaturday).setClickable(false);
        }
        cb=findViewById(R.id.checkSunday);
        if (cb.isChecked()) {
            findViewById(R.id.editSunday).setClickable(true);
        } else {
            findViewById(R.id.editSunday).setClickable(false);
        }
    }
}