package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ManagePagerAdapter extends FragmentStateAdapter {

    private int numOfTabs;

    public ManagePagerAdapter(@NonNull FragmentActivity fragmentActivity, int numOfTabs) {
        super(fragmentActivity);
        this.numOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new EventInfoFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new JoinedPeopleFragment();
            case 3:
                return new RequestsFragment();
            default:
                return new EventInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
