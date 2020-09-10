package pl.highelo.eatoutwithstrangers.JoinedEvents;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pl.highelo.eatoutwithstrangers.ManageEvent.ManageEventActivity;
import pl.highelo.eatoutwithstrangers.ManageEvent.YourEventsActivity;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.SearchEvent.EventPreviewActivity;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static com.google.firebase.firestore.DocumentChange.Type.REMOVED;

public class JoinedEventsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    private RecyclerView mJoinedEventsRecyclerView;
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
        final View v = inflater.inflate(R.layout.fragment_joined_events, container, false);
        mJoinedEventsRecyclerView = v.findViewById(R.id.joined_events_recyclerview);
        mJoinedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        mAdapter = new EventsAdapter(mEventsModelArrayList);
        mJoinedEventsRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnEventItemClick(new EventsAdapter.OnEventItemClick() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(v.getContext(), EventPreviewActivity.class);
                intent.putExtra("model", mEventsModelArrayList.get(position));
                startActivity(intent);
            }
        });

        CollectionReference collectionReference = mFirestore.collection("events");

        collectionReference.whereArrayContains("members", mUserID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
            }
        });

        return v;
    }
}