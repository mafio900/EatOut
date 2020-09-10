package pl.highelo.eatoutwithstrangers.ManageEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;

public class ManagePagerAdapter extends FragmentStateAdapter {

    private int numOfTabs;
    private EventsModel mEventsModel;

    public ManagePagerAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs, EventsModel eventsModel) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
        mEventsModel = eventsModel;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return EventInfoFragment.newInstance(mEventsModel);
            case 1:
                return new ChatFragment();
            case 2:
                return JoinedPeopleFragment.newInstance(mEventsModel);
            case 3:
                return RequestsFragment.newInstance(mEventsModel);
            default:
                return EventInfoFragment.newInstance(mEventsModel);
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
