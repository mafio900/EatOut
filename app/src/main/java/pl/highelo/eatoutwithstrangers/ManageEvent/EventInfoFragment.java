package pl.highelo.eatoutwithstrangers.ManageEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        View v = inflater.inflate(R.layout.fragment_event_info, container, false);
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
                                    mFirestore.collection("events").document(mEventsModel.getItemID()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getActivity(), R.string.successfully_deleted_event, Toast.LENGTH_LONG).show();
                                            getActivity().finish();
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
        return v;
    }
}