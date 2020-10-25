package pl.highelo.eatoutwithstrangers.SearchEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckdroid.geofirequery.GeoQuery;
import com.ckdroid.geofirequery.model.Distance;
import com.ckdroid.geofirequery.utils.BoundingBoxUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.BottomNavigationInterface;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.LocationResolver;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

import static pl.highelo.eatoutwithstrangers.EventPages.CreateEvent.MapActivity.COARSE_LOCATION;
import static pl.highelo.eatoutwithstrangers.EventPages.CreateEvent.MapActivity.FINE_LOCATION;
import static pl.highelo.eatoutwithstrangers.EventPages.CreateEvent.MapActivity.LOCATION_PERMISSIONS_REQUEST_CODE;

public class SearchEventActivity extends AppCompatActivity {
    private static final String TAG = "SearchEventActivity";

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String DISTANCE = "distance";

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mEventsList;
    private LinearLayoutManager mLinearLayoutManager;
    private EventsAdapter mAdapter;
    private DocumentSnapshot mLastVisible;
    private boolean mIsScrolling;
    private boolean mIsLastItemReached = false;
    private static final int PAGINATION_LIMIT = 6;
    private ArrayList<EventsModel> mEventsModelArrayList = new ArrayList<>();
    private ProgressBar mProgressBar;

    private Button mLocalizationButton;
    private Location mCurrentLocation;

    private RadioGroup mRadioGroup;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_search_event);
        new BottomNavigationInterface(this, findViewById(R.id.parent_layout));
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.search_events);
        setSupportActionBar(mToolbar);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        mEventsList = findViewById(R.id.events_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mEventsList.setLayoutManager(mLinearLayoutManager);

        mLocalizationButton = findViewById(R.id.search_event_location_button);
        getLocationPermissions();
        mLocalizationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermissions();
            }
        });
        mProgressBar = findViewById(R.id.search_event_progress_bar);
    }

    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setLocation();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (!(i == PackageManager.PERMISSION_GRANTED)) {
                            Toast.makeText(this, R.string.location_permissions_denied, Toast.LENGTH_LONG).show();
                            mLocalizationButton.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
                            return;
                        }
                    }
                    mLocalizationButton.setVisibility(View.GONE);
                    setLocation();
                }
                break;
        }
    }

    public void setLocation() {
        mLocalizationButton.setVisibility(View.GONE);
        LocationResolver.LocationResult locationResult = new LocationResolver.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                mCurrentLocation = location;
                getData();
            }
        };
        LocationResolver locationResolver = new LocationResolver();
        locationResolver.getLocation(this, locationResult, 20000);
    }

    public void getData() {
        mProgressBar.setVisibility(View.VISIBLE);
        //Query
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mEventsModelArrayList.clear();

        Distance distance = new Distance(sharedPreferences.getInt(DISTANCE, 10), BoundingBoxUtils.DistanceUnit.KILOMETERS);
        final GeoQuery geoQuery = new GeoQuery()
                .collection("events")
                .whereNearToLocation(mCurrentLocation, distance, "l");

        mAdapter = new EventsAdapter(mEventsModelArrayList, getApplicationContext());
        mAdapter.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
            @Override
            public void OnItemClick(final int position) {
                final EventsModel item = mEventsModelArrayList.get(position);
                if(item.getMembers().size() == item.getMaxPeople()){
                    new AlertDialog.Builder(SearchEventActivity.this)
                            .setTitle(R.string.event_is_full)
                            .setMessage(R.string.event_full_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }else{
                    final AlertDialog dialog = new AlertDialog.Builder(SearchEventActivity.this)
                            .setTitle(getString(R.string.join_event))
                            .setMessage(R.string.sure_to_join_event)
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String currentUserID = mAuth.getCurrentUser().getUid();
                                    DocumentReference eventsRef = mFirestore.collection("events").document(item.getItemID());
                                    DocumentReference usersRef = mFirestore.collection("users").document(currentUserID);
                                    WriteBatch batch = mFirestore.batch();
                                    batch.update(eventsRef, "requests", FieldValue.arrayUnion(currentUserID));
                                    batch.update(usersRef, "requests", FieldValue.arrayUnion(item.getItemID()));
                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                dialog.dismiss();
                                                mEventsModelArrayList.remove(position);
                                                mAdapter.notifyDataSetChanged();
                                                Toast.makeText(SearchEventActivity.this, "Pomyślnie wysłano prośbę o dołączenie", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(SearchEventActivity.this, "Wystąpił błąd, spróbuj ponownie później", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                    dialog.show();
                }
            }
        });
        mEventsList.setAdapter(mAdapter);

        Query query = geoQuery.limit(PAGINATION_LIMIT).getQuery();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                mProgressBar.setVisibility(View.GONE);
                TextView emptyText = findViewById(R.id.search_event_empty_text);
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        EventsModel model = document.toObject(EventsModel.class);
                        model.setItemID(document.getId());
                        if (!mUserID.equals(model.getUserID())) {
                            Timestamp currentTime = Timestamp.now();
                            Timestamp documentTime = model.getTimeStamp();
                            long diff = documentTime.getSeconds() - currentTime.getSeconds();
                            if (diff <= 0) {
                                mFirestore.collection("events").document(document.getId()).delete();
                            } else if (model.getRequests().contains(mUserID) || model.getMembers().contains(mUserID)) {
                                continue;
                            } else {
                                mEventsModelArrayList.add(model);
                            }
                        }
                    }
                    if(mEventsModelArrayList.isEmpty()){
                        emptyText.setVisibility(View.VISIBLE);
                    } else{
                        emptyText.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();

                        mLastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);

                        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    mIsScrolling = true;
                                }
                            }

                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                                int visibleItemCount = mLinearLayoutManager.getChildCount();
                                int totalItemCount = mLinearLayoutManager.getItemCount();

                                if (mIsScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !mIsLastItemReached) {
                                    mIsScrolling = false;

                                    Query nextQuery = geoQuery.startAfter(mLastVisible).limit(PAGINATION_LIMIT).getQuery();
                                    nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful() && task.getResult().size() > 0) {
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    if (!mUserID.equals(document.get("userID"))) {
                                                        Timestamp currentTime = Timestamp.now();
                                                        Timestamp documentTime = document.getTimestamp("timeStamp");
                                                        long diff = documentTime.getSeconds() - currentTime.getSeconds();
                                                        if (diff <= 0) {
                                                            mFirestore.collection("events").document(document.getId()).delete();
                                                        } else {
                                                            EventsModel ci = document.toObject(EventsModel.class);
                                                            ci.setItemID(document.getId());
                                                            ci.setRequests((List<String>) document.get("requests"));
                                                            ci.setMembers((List<String>) document.get("members"));
                                                            mEventsModelArrayList.add(ci);
                                                        }
                                                    }
                                                }
                                                mAdapter.notifyDataSetChanged();
                                                mAdapter.setEventsListFull(mEventsModelArrayList);
                                                mLastVisible = task.getResult().getDocuments()
                                                        .get(task.getResult().size() - 1);
                                                if (task.getResult().size() < PAGINATION_LIMIT) {
                                                    mIsLastItemReached = true;
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        };
                        mEventsList.addOnScrollListener(onScrollListener);
                    }
                } else {
                    Toast.makeText(SearchEventActivity.this, R.string.error_while_searching_area, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_settings) {
            View view = getLayoutInflater().inflate(R.layout.dialog_search_settings, null);
            final AlertDialog dialog = new AlertDialog.Builder(SearchEventActivity.this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mEventsModelArrayList.clear();
                            setLocation();
                        }
                    })
                    .create();
            dialog.show();
            mRadioGroup = view.findViewById(R.id.dialog_search_settings_radio_group);
            sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            switch (sharedPreferences.getInt(DISTANCE, 10)) {
                case 10:
                    ((RadioButton) view.findViewById(R.id.dialog_search_settings_radio_button_10km)).setChecked(true);
                    break;
                case 15:
                    ((RadioButton) view.findViewById(R.id.dialog_search_settings_radio_button_15km)).setChecked(true);
                    break;
                case 20:
                    ((RadioButton) view.findViewById(R.id.dialog_search_settings_radio_button_20km)).setChecked(true);
                    break;
                case 30:
                    ((RadioButton) view.findViewById(R.id.dialog_search_settings_radio_button_30km)).setChecked(true);
                    break;
            }
        }
        return true;
    }

    public void checkButton(View view) {
        int radioId = mRadioGroup.getCheckedRadioButtonId();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (radioId) {
            case R.id.dialog_search_settings_radio_button_10km:
                editor.putInt(DISTANCE, 10);
                break;
            case R.id.dialog_search_settings_radio_button_15km:
                editor.putInt(DISTANCE, 15);
                break;
            case R.id.dialog_search_settings_radio_button_20km:
                editor.putInt(DISTANCE, 20);
                break;
            case R.id.dialog_search_settings_radio_button_30km:
                editor.putInt(DISTANCE, 30);
                break;
        }
        editor.apply();
    }

    @Override
    public void onBackPressed() {
            CommonMethods.showDialog(this, getString(R.string.sure_to_leave_app));

    }
}