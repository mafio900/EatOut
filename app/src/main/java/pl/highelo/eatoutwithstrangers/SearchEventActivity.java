package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SearchEventActivity extends AppCompatActivity {
    private static final String TAG = "SearchEventActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mEventsList;
    private EventsAdapter mAdapter2;

    private ArrayList<EventsModel> mEventsModelArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.checkIfBanned(this);

        setContentView(R.layout.activity_search_event);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));
        mNavigationView.setCheckedItem(R.id.nav_search_events);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        //Query
        CollectionReference collectionReference = mFirestore.collection("events");
        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(51.7211, 18.1021), 10.0);
        Query query = geoQuery.getQueries().get(0).whereEqualTo("isEnded", false);
        //Recycler options
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        String d = document.getString("date");
                        String t = document.getString("time");
                        String dt = d + " " + t;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                        try {
                            Date ed = simpleDateFormat.parse(dt);
                            Date cd = new Date();
                            long diff = ed.getTime() - cd.getTime();
                            if(diff <= 0){
                                mFirestore.collection("events").document(document.getId()).update("isEnded", true);
                            }
                            else{
                                EventsModel ci = new EventsModel();
                                ci.setPlaceName((String) document.get("placeName"));
                                ci.setTheme((String) document.get("theme"));
                                ci.setPlaceAddress((String) document.get("placeAddress"));
                                ci.setDate((String) document.get("date"));
                                ci.setTime((String) document.get("time"));
                                mEventsModelArrayList.add(ci);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    mEventsList = findViewById(R.id.events_list);
                    mAdapter2 = new EventsAdapter(mEventsModelArrayList);
                    mAdapter2.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
                        @Override
                        public void OnItemClick(int position) {
                            //todo
                            //Napisać Activity gdzie będzie podgląd eventu
                        }
                    });
                    mEventsList.setLayoutManager(new LinearLayoutManager(SearchEventActivity.this));
                    mEventsList.setAdapter(mAdapter2);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_events, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setQueryHint("Szukaj tematów");
        searchView.setIconified(false);

        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.rgb(180,180,180));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CommonMethods.hideKeyboard(SearchEventActivity.this);
                mAdapter2.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                CommonMethods.hideKeyboard(SearchEventActivity.this);
                menu.findItem(R.id.app_bar_search).collapseActionView();
                mAdapter2.getFilter().filter("");
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_settings) {
            Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}