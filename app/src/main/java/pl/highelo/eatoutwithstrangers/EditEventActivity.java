package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "EditEventActivity";

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int LOCATION_REQUEST_CODE = 1000;

    private Toolbar mToolbar;
    private EventsModel mEventsModel;

    private TextView mName, mAddress, mEventDate, mMaxPeopleTextView;
    private EditText mTheme;
    private Button mEditEventButton;
    private SeekBar mMaxPeopleSeekBar;
    private ProgressBar mProgressBar;

    private Place mPlace;
    private String date;
    private int mYear, mMonth, mDay, mHour, mMinute = -1;
    private int maxPeople = 10, peopleStep = 1;
    private int currentPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mEventsModel = (EventsModel) intent.getParcelableExtra("model");

        mName = findViewById(R.id.placeName);
        mAddress = findViewById(R.id.placeAddress);
        mEventDate = findViewById(R.id.eventDate);
        mTheme = findViewById(R.id.eventTheme);
        mEditEventButton = findViewById(R.id.editEventButton);
        mProgressBar = findViewById(R.id.progressBar);
        mMaxPeopleSeekBar = findViewById(R.id.maxPeopleSeekBar);
        mMaxPeopleTextView = (TextView) findViewById(R.id.maxPopleTextView);
        mMaxPeopleSeekBar.setMax(maxPeople / peopleStep);
        mMaxPeopleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentPeople = progress * peopleStep;
                mMaxPeopleTextView.setText(String.valueOf(currentPeople));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if(isServicesOK()){
            initMaps();
        }

        mEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mName.setText(mEventsModel.getPlaceName());
        mAddress.setText(mEventsModel.getPlaceAddress());
        mTheme.setText(mEventsModel.getTheme());
        mEventDate.setText(mEventsModel.getDate() + " " + mEventsModel.getTime());
        currentPeople = mEventsModel.getMaxPeople();
        mMaxPeopleSeekBar.setProgress(currentPeople);
        Log.d(TAG, "onCreate: "+mEventsModel.getPlaceLatLng());

        mEditEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEvent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOCATION_REQUEST_CODE){
            if(resultCode == RESULT_OK && data != null){
                mPlace = data.getParcelableExtra("place");

                mName.setText(mPlace.getName());
                mAddress.setText(mPlace.getAddress());
            }
        }
    }

    private void initMaps(){
        Button mapBtn = (Button) findViewById(R.id.mapButton);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEventActivity.this, MapActivity.class);
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        });
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
        mEventDate.setText(newDate);
    }

    private void updateEvent(){
        boolean flag = true;

        if(TextUtils.isEmpty(mTheme.getText().toString()) || mTheme.getText().toString().length() < 3 ){
            flag = false;
            mTheme.setError("Temat musi mieć co najmniej 3 znaki!");
        }
        if(mMaxPeopleTextView.getText().toString().equals("0")){
            flag = false;
            mMaxPeopleTextView.setError("Ilość minimalna osób musi wynosić 1!");
        }else{mMaxPeopleTextView.setError(null);}

        if(flag){
            mProgressBar.setVisibility(View.VISIBLE);
            Map<String, Object> event = new HashMap<>();
            if(mPlace != null){
                event.put("placeName", mPlace.getName());
                event.put("placeAddress", mPlace.getAddress());
                event.put("placeLatLng", new GeoPoint(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude));
            }
            if(date != null){
                SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
                SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                String newDate = CommonMethods.parseDate(mYear+"-"+(mMonth+1)+"-"+mDay, oldFormat, newFormat);
                event.put("date", newDate);
                oldFormat = new SimpleDateFormat("H:m", Locale.US);
                newFormat = new SimpleDateFormat("HH:mm", Locale.US);
                String newTime = CommonMethods.parseDate(mHour+":"+mMinute, oldFormat, newFormat);
                event.put("time", newTime);
            }
            event.put("theme", mTheme.getText().toString());
            event.put("maxPeople", Integer.parseInt(mMaxPeopleTextView.getText().toString()));

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("events").document(mEventsModel.getItemID()).update(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(EditEventActivity.this, "Wydarzenie zostało pomyślnie zedytowane!", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                    } else{
                        Toast.makeText(EditEventActivity.this, "Coś poszło nie tak przy edycji wydarzenia!", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
