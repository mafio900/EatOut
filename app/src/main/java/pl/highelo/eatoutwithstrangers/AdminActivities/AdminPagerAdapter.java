package pl.highelo.eatoutwithstrangers.AdminActivities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminPagerAdapter extends FragmentStateAdapter {

    private int numOfTabs;

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            default:
                return new ReportsFragment();
            case 1:
                return new BanUserFragment();
            case 2:
                return new GiveAdminFragment();
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
