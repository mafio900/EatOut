package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.ProfilePreviewActivity;
import pl.highelo.eatoutwithstrangers.R;

public class RequestsFragment extends Fragment {
    private static final String TAG = "RequestsFragment";

    private static final String ARG_EVENTS_MODEL = "model";

    private EventsModel mEventsModel;
    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private AlertDialog dialog;

    private FirebaseFirestore mFirestore;

    public RequestsFragment(){}

    public static RequestsFragment newInstance(EventsModel eventsModel) {
        RequestsFragment fragment = new RequestsFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_requests, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Query query = mFirestore.collection("users").whereArrayContains("requests", mEventsModel.getItemID());
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
        mAdapter.setOnUsersAcceptClick(new UsersAdapter.OnUsersAcceptClick() {
            @Override
            public void OnAcceptClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(R.string.add_user);
                builder.setMessage(R.string.sure_to_add_user_to_event);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WriteBatch batch = mFirestore.batch();

                                Map<String, Object> eventMap = new HashMap<>();
                                eventMap.put("requests", FieldValue.arrayRemove(mAdapter.getItem(position).getUserID()));
                                eventMap.put("members", FieldValue.arrayUnion(mAdapter.getItem(position).getUserID()));
                                DocumentReference evRef = mFirestore.collection("events").document(mEventsModel.getItemID());
                                batch.update(evRef, eventMap);

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("requests", FieldValue.arrayRemove(mEventsModel.getItemID()));
                                userMap.put("joinedEvents", FieldValue.arrayUnion(mEventsModel.getItemID()));
                                DocumentReference userRef = mFirestore.collection("users").document(mAdapter.getItem(position).getUserID());
                                batch.update(userRef, userMap);

                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getContext(), R.string.user_has_been_added, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog = builder.create();
                dialog.show();
            }
        });
        mAdapter.setOnUsersCancelClick(new UsersAdapter.OnUsersCancelClick() {
            @Override
            public void OnCancelClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(R.string.delete_request);
                builder.setMessage(R.string.sure_to_delete_users_request);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WriteBatch batch = mFirestore.batch();
                                DocumentReference evRef = mFirestore.collection("events").document(mEventsModel.getItemID());
                                batch.update(evRef,"requests", FieldValue.arrayRemove(mAdapter.getItem(position).getUserID()));
                                DocumentReference userRef = mFirestore.collection("users").document(mAdapter.getItem(position).getUserID());
                                batch.update(userRef,"requests", FieldValue.arrayRemove(mEventsModel.getItemID()));

                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), R.string.successfully_deleted_users_request, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog = builder.create();
                dialog.show();
            }
        });
        mRecyclerView = view.findViewById(R.id.manage_requests_recyclerview);
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