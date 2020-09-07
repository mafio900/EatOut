package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ManageEventActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mManageTabLayout;
    private ViewPager2 mManageViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Zarządzanie spotkaniem");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mManageTabLayout = (TabLayout) findViewById(R.id.manage_tab_layout);
        mManageViewPager = (ViewPager2) findViewById(R.id.manage_view_pager);
        mManageViewPager.setAdapter(new ManagePagerAdapter(this, mManageTabLayout.getTabCount()));

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
                        tab.setText(R.string.people);
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
