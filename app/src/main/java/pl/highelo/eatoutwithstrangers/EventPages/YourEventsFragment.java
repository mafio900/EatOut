package pl.highelo.eatoutwithstrangers.EventPages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsPaginationAdapter;
import pl.highelo.eatoutwithstrangers.R;

public class YourEventsFragment extends Fragment {

    private FirebaseFirestore mFirestore;
    private EventsPaginationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public YourEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_your_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.your_events_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.your_events_swipe_layout);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(4)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        Query query = mFirestore.collection("events").whereEqualTo("userID", FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("timeStamp", Query.Direction.ASCENDING);

        FirestorePagingOptions options = new FirestorePagingOptions.Builder<EventsModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<EventsModel>() {
                    @NonNull
                    @Override
                    public EventsModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        EventsModel model = snapshot.toObject(EventsModel.class);
                        model.setItemID(snapshot.getId());
                        return model;
                    }
                })
                .build();
        mAdapter = new EventsPaginationAdapter(options, mSwipeRefreshLayout);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
            }
        });

    }
}