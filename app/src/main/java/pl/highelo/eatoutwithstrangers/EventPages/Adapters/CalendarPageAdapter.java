package pl.highelo.eatoutwithstrangers.EventPages.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.highelo.eatoutwithstrangers.EventPages.JoinedEventsFragment;
import pl.highelo.eatoutwithstrangers.EventPages.RequestedEventsFragment;
import pl.highelo.eatoutwithstrangers.EventPages.YourEventsFragment;

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
