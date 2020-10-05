package pl.highelo.eatoutwithstrangers.EventPages;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CalendarPageAdapter extends FragmentStateAdapter {

    int numOfTabs;

    public CalendarPageAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            default:
                return new YourEventsFragment();
            case 1:
                return new JoinedEventsFragment();
            case 2:
                return new RequestedEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
