package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
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
import java.util.List;
import java.util.Locale;

public class SearchEventActivity extends AppCompatActivity {
    private static final String TAG = "SearchEventActivity";

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String DISTANCE = "distance";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
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

        mEventsList = findViewById(R.id.events_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mEventsList.setLayoutManager(mLinearLayoutManager);

        //Query
        CollectionReference collectionReference = mFirestore.collection("events");
        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(51.7211, 18.1021), sharedPreferences.getInt(DISTANCE, 10));
        final Query mainQuery = geoQuery.getQueries().get(0);
        Query query = mainQuery.limit(PAGINATION_LIMIT);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && task.getResult().size() > 0){
                    for(DocumentSnapshot document : task.getResult()){
                        if(!mUserID.equals(document.get("userID"))){
                            Timestamp currentTime = Timestamp.now();
                            Timestamp documentTime = document.getTimestamp("timeStamp");
                            long diff = documentTime.getSeconds() - currentTime.getSeconds();
                            if(diff <= 0){
                                mFirestore.collection("events").document(document.getId()).delete();
                            }
                            else{
                                EventsModel ci = document.toObject(EventsModel.class);
                                ci.setItemID(document.getId());
                                mEventsModelArrayList.add(ci);
                            }
                        }
                    }
                    mAdapter = new EventsAdapter(mEventsModelArrayList);
                    mAdapter.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
                        @Override
                        public void OnItemClick(int position) {
                            Intent preview = new Intent(SearchEventActivity.this, EventPreviewActivity.class);
                            preview.putExtra("itemID", mEventsModelArrayList.get(position).getItemID());
                            startActivity(preview);
                        }
                    });
                    mEventsList.setAdapter(mAdapter);

                    mLastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() -1);

                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                mIsScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = mLinearLayoutManager.getChildCount();
                            int totalItemCount = mLinearLayoutManager.getItemCount();

                            if(mIsScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !mIsLastItemReached){
                                mIsScrolling = false;

                                Query nextQuery = mainQuery.startAfter(mLastVisible).limit(PAGINATION_LIMIT);
                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful() && task.getResult().size() > 0){
                                            for(DocumentSnapshot document : task.getResult()){
                                                if(!mUserID.equals(document.get("userID"))){
                                                    Timestamp currentTime = Timestamp.now();
                                                    Timestamp documentTime = document.getTimestamp("timeStamp");
                                                    long diff = documentTime.getSeconds() - currentTime.getSeconds();
                                                    if(diff <= 0){
                                                        mFirestore.collection("events").document(document.getId()).delete();
                                                    }
                                                    else{
                                                        EventsModel ci = document.toObject(EventsModel.class);
                                                        ci.setItemID(document.getId());
                                                        mEventsModelArrayList.add(ci);
                                                    }
                                                }
                                            }
                                            mAdapter.notifyDataSetChanged();
                                            mAdapter.setEventsListFull(mEventsModelArrayList);
                                            mLastVisible = task.getResult().getDocuments()
                                                    .get(task.getResult().size() -1);
                                            if(task.getResult().size() < PAGINATION_LIMIT){
                                                mIsLastItemReached = true;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    };
                    mEventsList.setOnScrollListener(onScrollListener);
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
                mAdapter.getFilter().filter(query);
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
                mAdapter.getFilter().filter("");
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_settings) {
            startActivity(new Intent(this, SearchEventSettingsActivity.class));
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