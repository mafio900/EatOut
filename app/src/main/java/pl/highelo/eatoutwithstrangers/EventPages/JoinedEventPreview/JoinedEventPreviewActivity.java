package pl.highelo.eatoutwithstrangers.EventPages.JoinedEventPreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pl.highelo.eatoutwithstrangers.EventPages.Adapters.JoinedEventPreviewPageAdapter;
import pl.highelo.eatoutwithstrangers.EventPages.Adapters.ManagePagerAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedEventPreviewActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    private EventsModel mEventsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_event_preview);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.event_preview);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mEventsModel = intent.getParcelableExtra("model");

        mTabLayout = (TabLayout) findViewById(R.id.joined_event_preview_tab_layout);
        //mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewPager = (ViewPager2) findViewById(R.id.joined_event_preview_view_pager);
        mViewPager.setAdapter(new JoinedEventPreviewPageAdapter(this, mTabLayout.getTabCount(), mEventsModel));

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
                        tab.setText(R.string.participants);
                        break;
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }
}