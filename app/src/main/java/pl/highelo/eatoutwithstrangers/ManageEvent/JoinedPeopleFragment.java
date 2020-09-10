package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.ProfilePreviewActivity;
import pl.highelo.eatoutwithstrangers.R;

public class JoinedPeopleFragment extends Fragment {
    private static final String TAG = "JoinedPeopleFragment";

    private static final String ARG_EVENTS_MODEL = "model";

    private EventsModel mEventsModel;
    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private ArrayList<UsersModel> mUsersList = new ArrayList<>();
    private AlertDialog dialog;

    private FirebaseFirestore mFirestore;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_joined_people, container, false);
        mFirestore.collection("users").whereArrayContains("joinedEvents", mEventsModel.getItemID()).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            Bundle result = new Bundle();
                            result.putInt("joinedPeople", mUsersList.size());
                            break;
                        case MODIFIED:

                            break;
                        case REMOVED:
                            if(dialog.isShowing()){
                                dialog.dismiss();
                            }
                            int p = 0;
                            for(UsersModel u : mUsersList){
                                if(u.getUserID().equals(usersModel.getUserID())){
                                    Log.d(TAG, "onEvent: " + mUsersList.get(p).getfName());
                                    mUsersList.remove(p);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                }
                                p++;
                            }
                            p=0;
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
        mAdapter.setOnUsersCancelClick(new UsersAdapter.OnUsersCancelClick() {
            @Override
            public void OnCancelClick(final int position) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Usuń użytkownika");
                builder.setMessage("Czy na pewno chcesz usunąć użytkownika z wydarzenia?");
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFirestore.collection("events").document(mEventsModel.getItemID()).update("members", FieldValue.arrayRemove(mUsersList.get(position).getUserID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mFirestore.collection("users").document(mUsersList.get(position).getUserID()).update("joinedEvents", FieldValue.arrayRemove(mEventsModel.getItemID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Pomyślnie usunięto użytkownika", Toast.LENGTH_LONG).show();
                                            }
                                        });
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
        mRecyclerView = v.findViewById(R.id.manage_joined_people_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }
}