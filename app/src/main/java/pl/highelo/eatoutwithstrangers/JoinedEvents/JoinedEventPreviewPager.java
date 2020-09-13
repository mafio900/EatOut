package pl.highelo.eatoutwithstrangers.JoinedEvents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.highelo.eatoutwithstrangers.ManageEvent.ChatFragment;
import pl.highelo.eatoutwithstrangers.ManageEvent.EventInfoFragment;
import pl.highelo.eatoutwithstrangers.ManageEvent.JoinedPeopleFragment;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;

public class JoinedEventPreviewPager extends FragmentStateAdapter {

    private int numOfTabs;
    private EventsModel mEventsModel;

    public JoinedEventPreviewPager(@NonNull FragmentActivity fragmentActivity, int numOfTabs, EventsModel eventsModel) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
        this.mEventsModel = eventsModel;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            default:
                return EventInfoFragment.newInstance(mEventsModel);
            case 1:
                return ChatFragment.newInstance(mEventsModel);
            case 2:
                return JoinedPeopleFragment.newInstance(mEventsModel);
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
