package pl.highelo.eatoutwithstrangers.EventPages;

import android.content.Intent;
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
import android.widget.TextView;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pl.highelo.eatoutwithstrangers.EventPages.JoinedEventPreview.JoinedEventPreviewActivity;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsPaginationAdapter;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedEventsFragment extends Fragment {

    private FirebaseFirestore mFirestore;
    private EventsPaginationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public JoinedEventsFragment() {
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
        return inflater.inflate(R.layout.fragment_joined_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.joined_events_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.joined_events_swipe_layout);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(5)
                .setPageSize(5)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        Query query = mFirestore.collection("events").whereArrayContains("members", FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("timeStamp", Query.Direction.ASCENDING);

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
        EventsPaginationAdapter.OnItemClickListener listener = new EventsPaginationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot item) {
                EventsModel model = item.toObject(EventsModel.class);
                model.setItemID(item.getId());
                Intent intent = new Intent(getContext(), JoinedEventPreviewActivity.class);
                intent.putExtra("model", model);
                startActivity(intent);
            }
        };
        mAdapter = new EventsPaginationAdapter(options, mSwipeRefreshLayout, listener);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        final TextView emptyText = view.findViewById(R.id.joined_events_empty_text);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
                if(mAdapter.getCurrentList().size() == 0){
                    emptyText.setVisibility(View.VISIBLE);
                }
                else {
                    emptyText.setVisibility(View.GONE);
                }
            }
        });
    }
}