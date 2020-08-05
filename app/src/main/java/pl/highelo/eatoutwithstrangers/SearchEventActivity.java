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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchEventActivity extends AppCompatActivity implements EventsAdapter.OnEventItemClick {
    private static final String TAG = "SearchEventActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mEventsList;
    private EventsAdapter adapter;

    private int count = 0;
    private List<DocumentSnapshot> list;

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
        FirestoreRecyclerOptions<EventsModel> options = new FirestoreRecyclerOptions.Builder<EventsModel>()
                .setLifecycleOwner(this)
                .setQuery(query, EventsModel.class)
                .build();
        mEventsList = findViewById(R.id.events_list);
        adapter = new EventsAdapter(options, this);
        mEventsList.setLayoutManager(new LinearLayoutManager(this));
        mEventsList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_events, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setQueryHint("Szukaj tematów");
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CommonMethods.hideKeyboard(SearchEventActivity.this);
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
    public void OnItemClick(EventsModel model) {
        Log.d(TAG, "OnItemClick: item clicked " + model.getItemID());
//        Intent intent = new Intent(YourEventsActivity.this, EditEventActivity.class);
//        intent.putExtra("model", model);
//        startActivity(intent);
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