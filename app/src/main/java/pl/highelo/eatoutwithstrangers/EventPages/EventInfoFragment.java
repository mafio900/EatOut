package pl.highelo.eatoutwithstrangers.EventPages;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class EventInfoFragment extends Fragment {
    private static final String ARG_EVENTS_MODEL = "model";
    private static final String TAG = "EventInfoFragment";

    private EventsModel mEventsModel;

    private TextView mEventTheme, mEventName, mEventAddress, mEventDate, mEventMaxPeople, mEventJoinedPeople;
    private Button mActionButton;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private BroadcastReceiver receiverUpdateDownload;

    public EventInfoFragment() {
        // Required empty public constructor
    }

    public static EventInfoFragment newInstance(EventsModel eventsModel) {
        EventInfoFragment fragment = new EventInfoFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mEventTheme = (TextView) v.findViewById(R.id.event_info_theme);
        mEventName = (TextView) v.findViewById(R.id.event_info_description);
        mEventAddress = (TextView) v.findViewById(R.id.event_info_address);
        mEventDate = (TextView) v.findViewById(R.id.event_info_date);
        mEventMaxPeople = (TextView) v.findViewById(R.id.event_info_max_people);
        mEventJoinedPeople = (TextView) v.findViewById(R.id.event_info_already_joined);
        mActionButton = (Button) v.findViewById(R.id.event_info_delete_button);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        receiverUpdateDownload = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mEventsModel = intent.getParcelableExtra("model");
                setData();
            }
        };
        IntentFilter filter = new IntentFilter("event_broadcast");
        getActivity().registerReceiver(receiverUpdateDownload, filter);
        setData();

        if(mAuth.getCurrentUser().getUid().equals(mEventsModel.getUserID())) {
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.delete_event);
                    builder.setMessage(R.string.sure_to_delete_event);
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mFirestore.collection("users").whereArrayContains("joinedEvents", mEventsModel.getItemID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<QuerySnapshot> task1) {
                                            if(task1.isSuccessful()){
                                                mFirestore.collection("users").whereArrayContains("requests", mEventsModel.getItemID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                                        if(task2.isSuccessful()){
                                                            WriteBatch batch = mFirestore.batch();

                                                            for(DocumentSnapshot document : task1.getResult()){
                                                                DocumentReference userDel = mFirestore.collection("users").document(document.getId());
                                                                batch.update(userDel, "joinedEvents", FieldValue.arrayRemove(mEventsModel.getItemID()));
                                                            }
                                                            for(DocumentSnapshot document : task1.getResult()){
                                                                DocumentReference userDel = mFirestore.collection("users").document(document.getId());
                                                                batch.update(userDel, "requests", FieldValue.arrayRemove(mEventsModel.getItemID()));
                                                            }
                                                            DocumentReference evDel = mFirestore.collection("events").document(mEventsModel.getItemID());
                                                            batch.delete(evDel);

                                                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Toast.makeText(getContext(), R.string.successfully_deleted_event, Toast.LENGTH_LONG).show();
                                                                        getActivity().finish();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
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
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
        else{
            mActionButton.setText(R.string.leave_event);
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActionButton.setClickable(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.leave_event);
                    builder.setMessage(R.string.sure_to_leave_event);
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WriteBatch batch = mFirestore.batch();
                                    DocumentReference eventsRef = mFirestore.collection("events").document(mEventsModel.getItemID());
                                    DocumentReference usersRef = mFirestore.collection("users").document(mAuth.getCurrentUser().getUid());
                                    batch.update(eventsRef, "members", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
                                    batch.update(usersRef, "joinedEvents", FieldValue.arrayRemove(mEventsModel.getItemID()));

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getContext(), R.string.you_left_event, Toast.LENGTH_LONG).show();
                                                getActivity().finish();
                                            }else{
                                                Toast.makeText(getContext(), R.string.something_went_wrong_while_deleting_event, Toast.LENGTH_LONG).show();
                                                mActionButton.setClickable(true);
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
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    private void setData(){
        mEventTheme.setText(getString(R.string.theme) + ": " + mEventsModel.getTheme());
        mEventName.setText(getString(R.string.description)+ ": " + mEventsModel.getDescription());
        mEventAddress.setText(mEventsModel.getPlaceAddress());
        String newDate = CommonMethods.parseDate(mEventsModel.getTimeStamp());
        mEventDate.setText(getString(R.string.date_of_beginning)+ ": " + newDate);
        mEventMaxPeople.setText(getString(R.string.max_participants)+ ": " + mEventsModel.getMaxPeople());
        mEventJoinedPeople.setText(getString(R.string.already_joined)+ ": " + mEventsModel.getMembers().size());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiverUpdateDownload != null) {
            try {
                getActivity().unregisterReceiver(receiverUpdateDownload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}