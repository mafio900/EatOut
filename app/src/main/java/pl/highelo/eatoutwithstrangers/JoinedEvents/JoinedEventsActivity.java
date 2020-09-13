package pl.highelo.eatoutwithstrangers.JoinedEvents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.NavbarInterface;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedEventsActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private TabLayout mJoinedEventsTabLayout;
    private ViewPager2 mJoinedEventsViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.validateUser(this);
        setContentView(R.layout.activity_joined_events);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Dołączone wydarzenia");
        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));
        mNavigationView.setCheckedItem(R.id.nav_joined_events);

        mJoinedEventsTabLayout = findViewById(R.id.joined_events_tablayout);
        mJoinedEventsViewPager = findViewById(R.id.joined_events_viewpager);
        mJoinedEventsViewPager.setAdapter(new JoinedEventsPagerAdapter(this, mJoinedEventsTabLayout.getTabCount()));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                mJoinedEventsTabLayout, mJoinedEventsViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.joined);
                        break;
                    case 1:
                        tab.setText(R.string.pendings);
                        break;
                }
            }
        }
        );
        tabLayoutMediator.attach();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            CommonMethods.showDialog(this, "Czy na pewno chcesz wyjść z aplikacji?");
        }
    }
}