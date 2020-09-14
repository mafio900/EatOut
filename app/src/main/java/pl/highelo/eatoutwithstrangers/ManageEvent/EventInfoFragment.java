package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class EventInfoFragment extends Fragment {
    private static final String ARG_EVENTS_MODEL = "model";
    private static final String ARG_EVENTS_CREATOR = "creator";

    private EventsModel mEventsModel;

    private TextView mEventTheme, mEventName, mEventAddress, mEventDate, mEventMaxPeople, mEventJoinedPeople;
    private Button mActionButton;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

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
        mEventName = (TextView) v.findViewById(R.id.event_info_name);
        mEventAddress = (TextView) v.findViewById(R.id.event_info_address);
        mEventDate = (TextView) v.findViewById(R.id.event_info_date);
        mEventMaxPeople = (TextView) v.findViewById(R.id.event_info_max_people);
        mEventJoinedPeople = (TextView) v.findViewById(R.id.event_info_already_joined);
        mActionButton = (Button) v.findViewById(R.id.event_info_delete_button);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mEventTheme.setText(getString(R.string.theme_preview) + ": " + mEventsModel.getTheme());
        mEventName.setText(getString(R.string.place_preview)+ ": " + mEventsModel.getPlaceName());
        mEventAddress.setText(mEventsModel.getPlaceAddress());
        GregorianCalendar d = new GregorianCalendar(TimeZone.getTimeZone("Europe/Warsaw"));
        d.setTime(mEventsModel.getTimeStamp().toDate());
        String date = d.get(Calendar.DAY_OF_MONTH)+"."
                +(d.get(Calendar.MONTH)+1)+"."
                +d.get(Calendar.YEAR)+" "
                +d.get(Calendar.HOUR_OF_DAY)+":"
                +d.get(Calendar.MINUTE);
        SimpleDateFormat oldFormat = new SimpleDateFormat("d.M.yyyy H:m", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        mEventDate.setText(getString(R.string.date_of_begining_preview)+ ": " + newDate);
        mEventMaxPeople.setText(getString(R.string.max_people_preview)+ ": " + mEventsModel.getMaxPeople());
        mEventJoinedPeople.setText(getString(R.string.already_joined_preview)+ ": " + mEventsModel.getMembers().size());

        if(mAuth.getCurrentUser().getUid().equals(mEventsModel.getUserID())) {
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Usuń wydarzenie");
                    builder.setMessage("Czy na pewno chcesz usunąć to wydarzenie?");
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Opuść wydarzenie");
                    builder.setMessage("Czy na pewno chcesz opuścić to wydarzenie?");
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

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
}