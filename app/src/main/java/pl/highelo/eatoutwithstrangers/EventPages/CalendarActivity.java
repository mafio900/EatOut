package pl.highelo.eatoutwithstrangers.EventPages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pl.highelo.eatoutwithstrangers.EventPages.Adapters.CalendarPageAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.BottomNavigationInterface;
import pl.highelo.eatoutwithstrangers.R;

public class CalendarActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        new BottomNavigationInterface(this, findViewById(R.id.parent));

        mTabLayout = (TabLayout) findViewById(R.id.calendar_tab_layout);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewPager = (ViewPager2) findViewById(R.id.calendar_view_pager);
        mViewPager.setAdapter(new CalendarPageAdapter(this, mTabLayout.getTabCount()));

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.your_events);
                        break;
                    case 1:
                        tab.setText(R.string.joined_events);
                        break;
                    case 2:
                        tab.setText(R.string.requested_events);
                        break;
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }
}