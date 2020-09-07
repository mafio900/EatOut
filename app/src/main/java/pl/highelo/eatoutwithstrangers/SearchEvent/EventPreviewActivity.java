package pl.highelo.eatoutwithstrangers.SearchEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
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
        mEventJoinedPeople.setText(getString(R.string.already_joined_preview)+ ": " + mEventsModel.getJoinedPeople());

        requestsList = (List<String>) mEventsModel.getRequests();
        membersList = (List<String>) mEventsModel.getMembers();


        DocumentReference userRef = mFirestore.collection("users").document(mCreatorID);
        userRef.addSnapshotListener(EventPreviewActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mUserName.setText(documentSnapshot.get("fName").toString() + ",");
                mUserAge.setText(String.valueOf(CommonMethods.getAge(documentSnapshot.get("birthDate").toString())));
                mUserDescription.setText(documentSnapshot.get("description").toString());
            }
        });

        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + mCreatorID + "/profile_image_thumbnail");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(EventPreviewActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(mUserImage);
            }
        });

        if(requestsList != null && requestsList.contains(mCurrentUserID)){
            mActionButton.setText(R.string.cancel_request);
            mStage = "request";
        }
        else if(membersList != null && membersList.contains(mCurrentUserID)){
            mActionButton.setText(R.string.leave_event);
            mStage = "joined";
        }
        else{
            mActionButton.setText(R.string.join_event);
            mStage = "neutral";
        }

        mActionButton.setOnClickListener(new View.OnClickListener() {
            DocumentReference eventsRef = mFirestore.collection("events").document(mItemID);
            DocumentReference usersRef = mFirestore.collection("users").document(mCurrentUserID);
            @Override
            public void onClick(View view) {
                switch(mStage){
                    case "neutral":
                        eventsRef.update("requests", FieldValue.arrayUnion(mCurrentUserID));
                        mActionButton.setText(R.string.cancel_request);
                        mStage = "request";
                        break;
                    case "request":
                        eventsRef.update("requests", FieldValue.arrayRemove(mCurrentUserID));
                        mActionButton.setText(R.string.join_event);
                        mStage = "neutral";
                        break;
                    case "joined":
                        eventsRef.update("members", FieldValue.arrayRemove(mCurrentUserID));
                        usersRef.update("joinedEvents", FieldValue.arrayRemove(mItemID));
                        mActionButton.setText(R.string.leave_event);
                        mStage = "neutral";
                        break;
                }
            }
        });
    }
}