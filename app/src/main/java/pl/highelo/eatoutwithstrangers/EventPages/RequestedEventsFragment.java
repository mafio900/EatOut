package pl.highelo.eatoutwithstrangers.EventPages;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsPaginationAdapter;
import pl.highelo.eatoutwithstrangers.R;

public class RequestedEventsFragment extends Fragment {

    private FirebaseFirestore mFirestore;
    private EventsPaginationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public RequestedEventsFragment() {
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
        return inflater.inflate(R.layout.fragment_requested_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.requested_events_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.requested_events_swipe_layout);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(5)
                .setPageSize(5)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        Query query = mFirestore.collection("events").whereArrayContains("requests", FirebaseAuth.getInstance().getCurrentUser().getUid()).orderBy("timeStamp", Query.Direction.ASCENDING);

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
            public void onItemClick(final DocumentSnapshot i) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.join_event))
                        .setMessage(R.string.sure_to_join_event)
                        .setPositiveButton(android.R.string.yes, null)
                        .setNegativeButton(android.R.string.no, null)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EventsModel item = i.toObject(EventsModel.class);
                                item.setItemID(i.getId());
                                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DocumentReference eventsRef = mFirestore.collection("events").document(item.getItemID());
                                DocumentReference usersRef = mFirestore.collection("users").document(currentUserID);
                                WriteBatch batch = mFirestore.batch();
                                batch.update(eventsRef, "requests", FieldValue.arrayRemove(currentUserID));
                                batch.update(usersRef, "requests", FieldValue.arrayRemove(item.getItemID()));
                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            dialog.dismiss();
                                            mAdapter.refresh();
                                            Toast.makeText(getContext(), "Pomyślnie anulowano prośbę", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie później", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                dialog.show();
            }
        };
        mAdapter = new EventsPaginationAdapter(options, mSwipeRefreshLayout, listener);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        final TextView emptyText = view.findViewById(R.id.requested_events_empty_text);
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