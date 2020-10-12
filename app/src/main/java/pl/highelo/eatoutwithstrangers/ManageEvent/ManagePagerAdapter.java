package pl.highelo.eatoutwithstrangers.ManageEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.highelo.eatoutwithstrangers.ChatFragment;
import pl.highelo.eatoutwithstrangers.EventInfoFragment;
import pl.highelo.eatoutwithstrangers.ParticipantsFragment;
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
            default:
                return EventInfoFragment.newInstance(mEventsModel);
            case 1:
                return ChatFragment.newInstance(mEventsModel);
            case 2:
                return ParticipantsFragment.newInstance(mEventsModel);
            case 3:
                return RequestsFragment.newInstance(mEventsModel);
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
