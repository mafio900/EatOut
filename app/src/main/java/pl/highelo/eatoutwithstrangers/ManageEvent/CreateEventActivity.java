package pl.highelo.eatoutwithstrangers.ManageEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import org.imperiumlabs.geofirestore.GeoFirestore;

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

    private TextInputLayout mName, mAddress, mEventDate, mTheme, mMaxPeople;
    private Button mCreateEventButton;
    private ProgressBar mProgressBar;

    //vars
    private LatLng mLatLng;
    private String mPlaceName;
    private String mPlaceAddress;
    private String date;
    private int mYear, mMonth, mDay, mHour, mMinute = -1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_create_event);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.creating_event);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = findViewById(R.id.create_event_place_name);
        mAddress = findViewById(R.id.create_event_place_address);
        mEventDate = findViewById(R.id.create_event_date);
        mTheme = findViewById(R.id.create_event_theme);
        mCreateEventButton = findViewById(R.id.create_event_button);
        mProgressBar = findViewById(R.id.progressBar);
        mMaxPeople = findViewById(R.id.create_event_max_people);

        mMaxPeople.getEditText().setFilters(new InputFilter[]{ new CommonMethods.InputFilterMinMax("1", "10")});

        mEventDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        if(isServicesOK()){
            initMaps();
        }

        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOCATION_REQUEST_CODE){
            if(resultCode == RESULT_OK && data != null){
                mPlaceName = data.getStringExtra("placeName");
                mPlaceAddress = data.getStringExtra("placeAddress");
                mLatLng = data.getParcelableExtra("placeLatLng");

                mName.getEditText().setText(mPlaceName);
                mAddress.getEditText().setText(mPlaceAddress);
            }
        }
    }

    private void showDatePickerDialog(){
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
        date = year + "-" + (month+1) + "-" + dayOfMonth;
        mYear = year;
        mMonth = month;
        mDay = dayOfMonth;
        showTimePickerDialog();
    }

    private void showTimePickerDialog(){
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
        date += " " + hourOfDay + ":" + minute;
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d H:m", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        mEventDate.getEditText().setText(newDate);
    }

    private void initMaps(){
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEventActivity.this, MapActivity.class);
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        };
        mName.getEditText().setOnClickListener(onClickListener);
        mAddress.getEditText().setOnClickListener(onClickListener);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything ok
            Log.d(TAG, "isServicesOK: google services are working");
            return true;

        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //we can resovle it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "Nie można otworzyć map google!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void saveEvent(){
        boolean flag = true;

        if(TextUtils.isEmpty(mName.getEditText().getText().toString())){
            flag = false;
            mName.setError("Wybierz lokalizację!");
            mAddress.setError("Wybierz lokalizację!");
        }else{mName.setError(null);mAddress.setError(null);}
        if(TextUtils.isEmpty(mTheme.getEditText().getText().toString()) || mTheme.getEditText().getText().toString().length() < 3 ){
            flag = false;
            mTheme.setError("Temat musi mieć co najmniej 3 znaki!");
        }else{mTheme.setError(null);}
        if(TextUtils.isEmpty(mEventDate.getEditText().getText().toString())){
            flag = false;
            mEventDate.setError("Podaj datę!");
        }else{mEventDate.setError(null);}
        if(mMaxPeople.getEditText().getText().toString().equals("")){
            flag = false;
            mMaxPeople.setError("Ilość minimalna osób musi wynosić 1!");
        }else{mMaxPeople.setError(null);}
        if(flag){
            mProgressBar.setVisibility(View.VISIBLE);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReference = mFirestore.collection("events");
            final GeoFirestore geoFirestore = new GeoFirestore(collectionReference);

            Map<String, Object> event = new HashMap<>();
            event.put("userID", mAuth.getUid());
            event.put("placeName", mPlaceName);
            event.put("placeAddress", mPlaceAddress);
            //event.put("placeLatLng", new GeoPoint(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude));
            event.put("theme", mTheme.getEditText().getText().toString());
            event.put("maxPeople", Integer.parseInt(mMaxPeople.getEditText().getText().toString()));
            GregorianCalendar dd = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            dd.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
            event.put("timeStamp", new Timestamp(dd.getTime()));
            event.put("requests", new ArrayList<String>());
            event.put("members", new ArrayList<String>());

            collectionReference.add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    geoFirestore.setLocation(documentReference.getId(), new GeoPoint(mLatLng.latitude, mLatLng.longitude));
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateEventActivity.this, R.string.create_event_successful, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), YourEventsActivity.class));
                    finish();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Toast.makeText(CreateEventActivity.this, "Błąd przy tworzeniu wydarzenia", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}