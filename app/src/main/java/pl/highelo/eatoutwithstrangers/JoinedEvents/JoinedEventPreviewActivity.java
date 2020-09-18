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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

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
        FirebaseFirestore.getInstance().collection("events").document(mEventsModel.getItemID()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.exists() || !((List<String>)value.get("members")).contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    finish();
                    Toast.makeText(JoinedEventPreviewActivity.this, "Zostałeś wyrzucony albo wydarzenie już nie istnieje!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mTabLayout = (TabLayout) findViewById(R.id.joined_preview_tablayout);
        mViewPager = (ViewPager2) findViewById(R.id.joined_preview_viewpager);
        mViewPager.setAdapter(new JoinedEventPreviewPager(this, mTabLayout.getTabCount(), mEventsModel));

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