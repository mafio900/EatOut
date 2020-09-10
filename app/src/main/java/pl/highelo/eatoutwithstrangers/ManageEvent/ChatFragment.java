package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.ProfileActivities.ProfileActivity;
import pl.highelo.eatoutwithstrangers.R;

public class ChatFragment extends Fragment {

    private static final String ARG_EVENTS_MODEL = "model";

    private EventsModel mEventsModel;
    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private ArrayList<UsersModel> mUsersList = new ArrayList<>();

    private FirebaseFirestore mFirestore;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment




        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}