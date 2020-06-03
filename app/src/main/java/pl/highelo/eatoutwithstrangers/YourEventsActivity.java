package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class YourEventsActivity extends AppCompatActivity implements EventsAdapter.OnEventItemClick {

    private static final String TAG = "YourEventsActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mEventsList;
    private EventsAdapter adapter;

    private TextView textView;
    private TextView emptyTV;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_events);

        FirebaseMethods.validateUser(this);
        FirebaseMethods.checkIfBanned(this);

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
        mNavigationView.setCheckedItem(R.id.nav_your_events);

        mEventsList = findViewById(R.id.events_list);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        mFirestore.collection("events").whereEqualTo("userID", mUserID).whereEqualTo("isEnded", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
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
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        //Query
        Query query = mFirestore.collection("events").whereEqualTo("userID", mUserID).whereEqualTo("isEnded", false);
        //Recycler options
        FirestoreRecyclerOptions<EventsModel> options = new FirestoreRecyclerOptions.Builder<EventsModel>()
                .setLifecycleOwner(this)
                .setQuery(query, new SnapshotParser<EventsModel>() {
                    @NonNull
                    @Override
                    public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        EventsModel eventsModel = snapshot.toObject(EventsModel.class);
                        eventsModel.setItemID(snapshot.getId());
                        return eventsModel;
                    }
                })
                .build();

        adapter = new EventsAdapter(options, this);
        Log.d(TAG, "onCreate: " + adapter.getItemCount());
        mEventsList.setLayoutManager(new LinearLayoutManager(this));
        mEventsList.setAdapter(adapter);
        textView = findViewById(R.id.yourEventsTV);
        emptyTV = findViewById(R.id.emptyEvents);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                count++;
                if(count <= 1)
                    changeVisibility();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                count--;
                if(count == 0)
                    changeVisibility();
            }
        });
    }

    private void changeVisibility(){
        if(count == 0){
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
    public void OnItemClick(EventsModel model) {
        Log.d(TAG, "OnItemClick: item clicked " + model.getItemID());
        Intent intent = new Intent(YourEventsActivity.this, EditEventActivity.class);
        intent.putExtra("model", model);
        startActivity(intent);
    }
}



















