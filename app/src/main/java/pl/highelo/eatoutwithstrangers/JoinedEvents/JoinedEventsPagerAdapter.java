package pl.highelo.eatoutwithstrangers.JoinedEvents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class JoinedEventsPagerAdapter extends FragmentStateAdapter {

    private int numOfTabs;

    public JoinedEventsPagerAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            default:
                return new JoinedEventsFragment();
            case 1:
                return new RequestedEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
