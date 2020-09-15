package pl.highelo.eatoutwithstrangers.SearchEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.R;

public class EventPreviewActivity extends AppCompatActivity {
    private static final String TAG = "EventPreviewActivity";

    private Toolbar mToolbar;

    private TextView mEventTheme, mEventName, mEventAddress, mEventDate, mEventMaxPeople, mEventJoinedPeople;
    private CircleImageView mUserImage;
    private TextView mUserName, mUserAge, mUserDescription;
    private Button mActionButton;

    private EventsModel mEventsModel;
    private String mItemID;
    private String mCreatorID;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private List<String> requestsList;
    private List<String> membersList;
    private String mStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_preview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Podgląd wydarzenia");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEventTheme = (TextView) findViewById(R.id.event_preview_theme);
        mEventName = (TextView) findViewById(R.id.event_preview_name);
        mEventAddress = (TextView) findViewById(R.id.event_preview_address);
        mEventDate = (TextView) findViewById(R.id.event_preview_date);
        mEventMaxPeople = (TextView) findViewById(R.id.event_preview_max_people);
        mEventJoinedPeople = (TextView) findViewById(R.id.event_preview_joined_people);

        mUserImage = (CircleImageView) findViewById(R.id.event_preview_profile_image);
        mUserName = (TextView) findViewById(R.id.event_preview_username);
        mUserAge = (TextView) findViewById(R.id.event_preview_user_age);
        mUserDescription = (TextView) findViewById(R.id.event_preview_user_description);

        mActionButton = (Button) findViewById(R.id.event_preview_action_button);

        Intent intent = getIntent();
        mEventsModel = intent.getParcelableExtra("model");
        mItemID = mEventsModel.getItemID();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();
        mCreatorID = mEventsModel.getUserID();

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

        requestsList = mEventsModel.getRequests();
        membersList = mEventsModel.getMembers();


        final DocumentReference userRef = mFirestore.collection("users").document(mCreatorID);
        userRef.addSnapshotListener(EventPreviewActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mUserName.setText(documentSnapshot.get("fName").toString() + ",");
                mUserAge.setText(String.valueOf(CommonMethods.getAge(documentSnapshot.get("birthDate").toString())));
                mUserDescription.setText(documentSnapshot.get("description").toString());
                Glide.with(EventPreviewActivity.this)
                        .load(documentSnapshot.get("image_thumbnail"))
                        .placeholder(R.drawable.ic_person)
                        .into(mUserImage);
            }
        });

        refreshUI();

        mActionButton.setOnClickListener(new View.OnClickListener() {
            DocumentReference eventsRef = mFirestore.collection("events").document(mItemID);
            DocumentReference usersRef = mFirestore.collection("users").document(mCurrentUserID);
            WriteBatch batch = mFirestore.batch();
            @Override
            public void onClick(View view) {
                mActionButton.setClickable(false);
                switch(mStage){
                    case "neutral":
                        batch.update(eventsRef, "requests", FieldValue.arrayUnion(mCurrentUserID));
                        batch.update(usersRef, "requests", FieldValue.arrayUnion(mItemID));
                        break;
                    case "request":
                        batch.update(eventsRef, "requests", FieldValue.arrayRemove(mCurrentUserID));
                        batch.update(usersRef, "requests", FieldValue.arrayRemove(mItemID));
                        break;
                    case "joined":
                        batch.update(eventsRef, "members", FieldValue.arrayRemove(mCurrentUserID));
                        batch.update(usersRef, "joinedEvents", FieldValue.arrayRemove(mItemID));
                        break;
                }
                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(EventPreviewActivity.this, "Coś poszło nie tak, spróbuj ponownie", Toast.LENGTH_LONG).show();
                        }
                        mActionButton.setClickable(true);
                    }
                });
            }
        });
    }

    public void refreshUI(){
        mActionButton.setClickable(false);
        DocumentReference eventsRef = mFirestore.collection("events").document(mItemID);
        eventsRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                EventsModel ev = value.toObject(EventsModel.class);
                mEventJoinedPeople.setText(getString(R.string.already_joined_preview)+ ": " + ev.getMembers().size());
                if(ev.getRequests() != null && ev.getRequests().contains(mCurrentUserID)){
                    mActionButton.setText(R.string.cancel_request);
                    mStage = "request";
                }
                else if(ev.getMembers() != null && ev.getMembers().contains(mCurrentUserID)){
                    mActionButton.setText(R.string.leave_event);
                    mStage = "joined";
                }
                else{
                    mActionButton.setText(R.string.join_event);
                    mStage = "neutral";
                }
                mActionButton.setClickable(true);
            }
        });
    }
}