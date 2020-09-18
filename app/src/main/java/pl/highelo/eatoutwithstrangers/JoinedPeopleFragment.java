package pl.highelo.eatoutwithstrangers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
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
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;

public class JoinedPeopleFragment extends Fragment {
    private static final String TAG = "JoinedPeopleFragment";

    private static final String ARG_EVENTS_MODEL = "model";

    private EventsModel mEventsModel;
    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private AlertDialog dialog;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    public JoinedPeopleFragment() {
        // Required empty public constructor
    }
    public static JoinedPeopleFragment newInstance(EventsModel eventsModel) {
        JoinedPeopleFragment fragment = new JoinedPeopleFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENTS_MODEL, eventsModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEventsModel = getArguments().getParcelable(ARG_EVENTS_MODEL);
            mFirestore = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_joined_people, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Query query = mFirestore.collection("users").whereArrayContains("joinedEvents", mEventsModel.getItemID());
        FirestoreRecyclerOptions<UsersModel> options = new FirestoreRecyclerOptions.Builder<UsersModel>()
                .setQuery(query, new SnapshotParser<UsersModel>() {
                    @NonNull
                    @Override
                    public UsersModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        UsersModel usersModel = snapshot.toObject(UsersModel.class);
                        usersModel.setUserID(snapshot.getId());
                        return usersModel;
                    }
                })
                .build();
        mAdapter = new UsersAdapter(options);
        mAdapter.setOnUsersItemClick(new UsersAdapter.OnUsersItemClick() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(getContext(), ProfilePreviewActivity.class);
                intent.putExtra("user", mAdapter.getItem(position));
                startActivity(intent);
            }
        });
        if (mAuth.getCurrentUser().getUid().equals(mEventsModel.getUserID())) {
            mAdapter.setOnUsersCancelClick(new UsersAdapter.OnUsersCancelClick() {
                @Override
                public void OnCancelClick(final int position) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.delete_user);
                    builder.setMessage(R.string.sure_to_delete_user_from_event);
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WriteBatch batch = mFirestore.batch();
                                    DocumentReference evRef = mFirestore.collection("events").document(mEventsModel.getItemID());
                                    batch.update(evRef, "members", FieldValue.arrayRemove(mAdapter.getItem(position).getUserID()));
                                    Log.d(TAG, "onClick: " + mAdapter.getItem(position).getUserID());
                                    DocumentReference userRef = mFirestore.collection("users").document(mAdapter.getItem(position).getUserID());
                                    batch.update(userRef, "joinedEvents", FieldValue.arrayRemove(mEventsModel.getItemID()));

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getContext(), R.string.successfully_deleted_user, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    dialog = builder.create();
                    dialog.show();
                }
            });
        }
        mRecyclerView = view.findViewById(R.id.manage_joined_people_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}