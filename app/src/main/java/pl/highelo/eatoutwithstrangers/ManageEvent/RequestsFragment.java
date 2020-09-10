package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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
    private ArrayList<UsersModel> mUsersList = new ArrayList<>();
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
        mFirestore.collection("users").whereArrayContains("requests", mEventsModel.getItemID()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    UsersModel usersModel = dc.getDocument().toObject(UsersModel.class);
                    usersModel.setUserID(dc.getDocument().getId());
                    switch (dc.getType()) {
                        case ADDED:
                            if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }
                            mUsersList.add(usersModel);
                            mAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:

                            break;
                        case REMOVED:
                            if(dialog != null &&dialog.isShowing()){
                                dialog.dismiss();
                            }
                            int p = 0;
                            for(UsersModel u : mUsersList){
                                if(u.getUserID().equals(usersModel.getUserID())){
                                    mUsersList.remove(p);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                }
                                p++;
                            }
                            break;
                    }
                }
            }
        });
        mAdapter = new UsersAdapter(mUsersList);
        mAdapter.setOnUsersItemClick(new UsersAdapter.OnUsersItemClick() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(getContext(), ProfilePreviewActivity.class);
                intent.putExtra("user", mUsersList.get(position));
                startActivity(intent);
            }
        });
        mAdapter.setOnUsersAcceptClick(new UsersAdapter.OnUsersAcceptClick() {
            @Override
            public void OnAcceptClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Dodaj użytkownika");
                builder.setMessage("Czy na pewno chcesz dodać tego użytkownika do wydarzenia?");
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, Object> eventMap = new HashMap<>();
                                eventMap.put("requests", FieldValue.arrayRemove(mUsersList.get(position).getUserID()));
                                eventMap.put("members", FieldValue.arrayUnion(mUsersList.get(position).getUserID()));
                                mFirestore.collection("events").document(mEventsModel.getItemID()).update(eventMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("requests", FieldValue.arrayRemove(mEventsModel.getItemID()));
                                        userMap.put("joinedEvents", FieldValue.arrayUnion(mEventsModel.getItemID()));
                                        mFirestore.collection("users").document(mUsersList.get(position).getUserID()).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Pomyślnie dodano użytkownika", Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
                builder.setTitle("Usuń prośbę");
                builder.setMessage("Czy na pewno chcesz usunąć prośbę tego użytkownika?");
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFirestore.collection("events").document(mEventsModel.getItemID()).update("requests", FieldValue.arrayRemove(mUsersList.get(position).getUserID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mFirestore.collection("users").document(mUsersList.get(position).getUserID()).update("requests", FieldValue.arrayRemove(mEventsModel.getItemID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Pomyślnie usunięto prośbę o dodanie", Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
        mRecyclerView = v.findViewById(R.id.manage_requests_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }
}