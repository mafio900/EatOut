package pl.highelo.eatoutwithstrangers.ManageEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.NavbarInterface;
import pl.highelo.eatoutwithstrangers.R;

public class YourEventsActivity extends AppCompatActivity {

    private static final String TAG = "YourEventsActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mEventsList;
    private EventsAdapter mAdapter;
    private ArrayList<EventsModel> mEventsModelArrayList = new ArrayList<>();

    private TextView textView;
    private TextView emptyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_events);

        CommonMethods.validateUser(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.your_events);
        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));
        mNavigationView.setCheckedItem(R.id.nav_your_events);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        textView = findViewById(R.id.yourEventsTV);
        emptyTV = findViewById(R.id.emptyEvents);

        CollectionReference collectionReference = mFirestore.collection("events");

        collectionReference.whereEqualTo("userID", mUserID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    mEventsModelArrayList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp currentTime = Timestamp.now();
                        Timestamp documentTime = document.getTimestamp("timeStamp");
                        long diff = documentTime.getSeconds() - currentTime.getSeconds();
                        if (diff <= 0) {
                            mFirestore.collection("events").document(document.getId()).delete();
                        } else {
                            EventsModel ci = document.toObject(EventsModel.class);
                            ci.setItemID(document.getId());
                            mEventsModelArrayList.add(ci);
                        }
                    }
                    mAdapter = new EventsAdapter(mEventsModelArrayList, getApplicationContext());
                    mAdapter.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
                        @Override
                        public void OnItemClick(int position) {
                            Intent intent = new Intent(YourEventsActivity.this, ManageEventActivity.class);
                            intent.putExtra("model", mEventsModelArrayList.get(position));
                            startActivity(intent);
                        }
                    });
                    mEventsList = findViewById(R.id.events_list);
                    mEventsList.setLayoutManager(new LinearLayoutManager(YourEventsActivity.this));
                    mEventsList.setAdapter(mAdapter);
                    changeVisibility();
                }
            }
        });

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YourEventsActivity.this, CreateEventActivity.class);
                startActivity(intent);
            }
        });
    }

    private void changeVisibility(){
        if(mEventsModelArrayList.size() == 0){
            textView.setVisibility(View.GONE);
            mEventsList.setVisibility(View.GONE);
            emptyTV.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.VISIBLE);
            mEventsList.setVisibility(View.VISIBLE);
            emptyTV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            CommonMethods.showDialog(this, getString(R.string.sure_to_leave_app));
        }
    }
}



















