package pl.highelo.eatoutwithstrangers.JoinedEvents;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import pl.highelo.eatoutwithstrangers.ManageEvent.ManagePagerAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedEventPreviewActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EventsModel mEventsModel;

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_joined_event_preview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Przeglądaj wydarzenie");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mEventsModel = intent.getParcelableExtra("model");

        mTabLayout = (TabLayout) findViewById(R.id.joined_preview_tablayout);
        //mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewPager = (ViewPager2) findViewById(R.id.joined_preview_viewpager);
        mViewPager.setAdapter(new ManagePagerAdapter(this, mTabLayout.getTabCount(), mEventsModel));

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.informations);
                        break;
                    case 1:
                        tab.setText(R.string.chat);
                        break;
                    case 2:
                        tab.setText(R.string.people);
                        break;
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }
}