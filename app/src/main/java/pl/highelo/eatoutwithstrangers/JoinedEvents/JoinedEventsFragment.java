package pl.highelo.eatoutwithstrangers.JoinedEvents;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedEventsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mJoinedEventsRecyclerView;
    private TextView mEmptyText;
    private ProgressBar mProgressBar;
    private EventsAdapter mAdapter;
    private ArrayList<EventsModel> mEventsModelArrayList = new ArrayList<>();

    public JoinedEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_joined_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mJoinedEventsRecyclerView = view.findViewById(R.id.joined_events_recyclerview);
        mEmptyText = view.findViewById(R.id.joined_events_empty_text);
        mProgressBar = view.findViewById(R.id.joined_events_progressbar);
        mJoinedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new EventsAdapter(mEventsModelArrayList, getContext());
        mJoinedEventsRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(view.getContext(), JoinedEventPreviewActivity.class);
                intent.putExtra("model", mEventsModelArrayList.get(position));
                startActivity(intent);
            }
        });

        CollectionReference collectionReference = mFirestore.collection("events");

        collectionReference.whereArrayContains("members", mUserID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mProgressBar.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.GONE);
                for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
                    Timestamp currentTime = Timestamp.now();
                    Timestamp documentTime = document.getDocument().getTimestamp("timeStamp");
                    long diff = documentTime.getSeconds() - currentTime.getSeconds();
                    if(diff <= 0){
                        mFirestore.collection("events").document(document.getDocument().getId()).delete();
                    }
                    else{
                        EventsModel ci = document.getDocument().toObject(EventsModel.class);
                        ci.setItemID(document.getDocument().getId());
                        switch (document.getType()) {
                            case ADDED:
                                mEventsModelArrayList.add(ci);
                                mAdapter.notifyDataSetChanged();
                                break;
                            case MODIFIED:

                                break;
                            case REMOVED:
                                int p = 0;
                                for(EventsModel u : mEventsModelArrayList){
                                    if(u.getItemID().equals(u.getItemID())){
                                        mEventsModelArrayList.remove(p);
                                        mAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                    p++;
                                }
                                break;
                        }
                    }
                }
                if(mEventsModelArrayList.isEmpty()){
                    mEmptyText.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}