package pl.highelo.eatoutwithstrangers.EventPages.ManageEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pl.highelo.eatoutwithstrangers.EventPages.Adapters.ManagePagerAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class ManageEventActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mManageTabLayout;
    private ViewPager2 mManageViewPager;

    private EventsModel mEventsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.validateUser(this);
        setContentView(R.layout.activity_manage_event);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.manage_event);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mEventsModel = intent.getParcelableExtra("model");

        mManageTabLayout = (TabLayout) findViewById(R.id.manage_tab_layout);
        mManageTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mManageViewPager = (ViewPager2) findViewById(R.id.manage_view_pager);
        mManageViewPager.setAdapter(new ManagePagerAdapter(this, mManageTabLayout.getTabCount(), mEventsModel));

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                mManageTabLayout, mManageViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
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
                    case 3:
                        tab.setText(R.string.request_to_join);
                        break;
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }
}
