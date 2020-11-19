package pl.highelo.eatoutwithstrangers.EventPages.CreateEvent;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ckdroid.geofirequery.ExtensionKt;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.R;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "CreateEventActivity";

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int LOCATION_REQUEST_CODE = 1000;

    private Toolbar mToolbar;

    private TextInputLayout mAddress, mEventDate, mTheme, mDescription, mMaxPeople;
    private TextView mImageText;
    private ImageView mImage;
    private Uri mImageUri;
    private Button mCreateEventButton;
    private ProgressBar mProgressBar;

    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    //vars
    private LatLng mLatLng;
    private String mPlaceAddress;
    private String mDate;
    private int mYear, mMonth, mDay, mHour, mMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_create_event);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.creating_event);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddress = findViewById(R.id.create_event_place_address);
        mEventDate = findViewById(R.id.create_event_date);
        mTheme = findViewById(R.id.create_event_theme);
        mDescription = findViewById(R.id.create_event_description);
        mCreateEventButton = findViewById(R.id.create_event_button);
        mProgressBar = findViewById(R.id.create_event_progressbar);
        mMaxPeople = findViewById(R.id.create_event_max_people);
        mImage = findViewById(R.id.create_event_image);
        mImageText = findViewById(R.id.create_event_image_text);

        mMaxPeople.getEditText().setFilters(new InputFilter[]{new CommonMethods.InputFilterMinMax("1", "10")});

        mEventDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        if (isServicesOK()) {
            initMaps();
        }

        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageClick();
            }
        });
    }

    public void imageClick() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(16, 9)
                .setMinCropResultSize(640, 360)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mImage.setImageURI(mImageUri);
            }
        }
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                mPlaceAddress = data.getStringExtra("placeAddress");
                mLatLng = data.getParcelableExtra("placeLatLng");

                mAddress.getEditText().setText(mPlaceAddress);
            }
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                mYear != -1 ? mYear : c.get(Calendar.YEAR),
                mMonth != -1 ? mMonth : c.get(Calendar.MONTH),
                mDay != -1 ? mDay : c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDate = year + "-" + (month + 1) + "-" + dayOfMonth;
        mYear = year;
        mMonth = month;
        mDay = dayOfMonth;
        showTimePickerDialog();
    }

    private void showTimePickerDialog() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this,
                mHour != -1 ? mHour : c.get(Calendar.HOUR),
                mMinute != -1 ? mMinute : c.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        mDate += " " + hourOfDay + ":" + minute;
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d H:m", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
        String newDate = CommonMethods.parseDate(mDate, oldFormat, newFormat);
        mEventDate.getEditText().setText(newDate);
    }

    private void initMaps() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEventActivity.this, MapActivity.class);
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        };
        mAddress.getEditText().setOnClickListener(onClickListener);
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything ok
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //we can resovle it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, R.string.cannot_open_googles_map, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void saveEvent() {
        boolean flag = true;

        if (TextUtils.isEmpty(mAddress.getEditText().getText().toString())) {
            flag = false;
            mAddress.setError(getString(R.string.choose_localization));
        } else {
            mAddress.setError(null);
        }
        if (TextUtils.isEmpty(mTheme.getEditText().getText().toString()) || mTheme.getEditText().getText().toString().length() < 3) {
            flag = false;
            mTheme.setError(getString(R.string.theme_must_have_atleast_3_chars));
        } else {
            mTheme.setError(null);
        }
        if (TextUtils.isEmpty(mDescription.getEditText().getText().toString()) || mDescription.getEditText().getText().toString().trim().length() < 5) {
            flag = false;
            mDescription.setError(getString(R.string.description_must_have_atleast_5_chars));
        } else {
            mDescription.setError(null);
        }
        if (TextUtils.isEmpty(mEventDate.getEditText().getText().toString())) {
            flag = false;
            mEventDate.setError(getString(R.string.set_date));
        } else {
            mEventDate.setError(null);
        }
        if (mMaxPeople.getEditText().getText().toString().equals("")) {
            flag = false;
            mMaxPeople.setError(getString(R.string.max_participants_count));
        } else {
            mMaxPeople.setError(null);
        }
        if(mImageUri == null){
            flag = false;
            mImageText.setError("Musisz wgrać jakiś obrazek");
        }else{
            mImageText.setError(null);
        }
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
            final ScrollView scrollView = findViewById(R.id.create_event_scroll_view);
            scrollView.setVisibility(View.INVISIBLE);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mStorage = FirebaseStorage.getInstance();
            mStorageReference = mStorage.getReference();

            final CollectionReference collectionReference = mFirestore.collection("events");

            Map<String, Object> event = new HashMap<>();
            event.put("userID", mAuth.getUid());
            event.put("placeAddress", mPlaceAddress);
            event.put("theme", mTheme.getEditText().getText().toString());
            event.put("description", mDescription.getEditText().getText().toString());
            event.put("maxPeople", Integer.parseInt(mMaxPeople.getEditText().getText().toString()));
            GregorianCalendar dd = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            dd.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
            event.put("timeStamp", new Timestamp(dd.getTime()));
            event.put("requests", new ArrayList<String>());
            event.put("members", new ArrayList<String>());

            collectionReference.add(event).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull final Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        mStorageReference.child("events_images/" + task.getResult().getId()).putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task1) {
                                if(task1.isSuccessful()){
                                    ExtensionKt.setLocation(task.getResult(), mLatLng.latitude, mLatLng.longitude, "l", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(CreateEventActivity.this, R.string.create_event_successful, Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(CreateEventActivity.this, R.string.error_while_creating_event, Toast.LENGTH_LONG).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                scrollView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(CreateEventActivity.this, R.string.error_while_creating_event, Toast.LENGTH_LONG).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    scrollView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }else{
                        mProgressBar.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        Toast.makeText(CreateEventActivity.this, R.string.error_while_creating_event, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
