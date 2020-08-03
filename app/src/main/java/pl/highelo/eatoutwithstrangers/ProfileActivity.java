package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "ProfileActivity";

    private static final int TAKE_IMAGE_CODE = 1001;
    public static final String FIRSTNAME = "FIRSTNAME";
    public static final String FETCH_DATA = "FETCH_DATA";
    public static final String EMAIL = "EMAIL";
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private ImageView mProfileImageView, mEditButton;
    private EditText mFirstName, mDescription, mBirthDate, mCity;
    private TextView mEmail;
    private Button mBirthDateBtn;
    private Uri mSelectedImage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private String date;
    private String mUserID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.checkIfBanned(this);

        setContentView(R.layout.activity_profile);

        mProfileImageView = (ImageView) findViewById(R.id.profileImageView);
        mFirstName = (EditText) findViewById(R.id.nameProfilET);
        mDescription = (EditText) findViewById(R.id.descriptionMT);
        mBirthDate = (EditText) findViewById(R.id.birthDateET);
        mBirthDateBtn = (Button) findViewById(R.id.setBirthDateBtn);
        mEmail = (TextView) findViewById(R.id.emailTV);
        mCity = (EditText) findViewById(R.id.cityET);
        mEditButton = (ImageView) findViewById(R.id.editButton);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mUserID = mAuth.getCurrentUser().getUid();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.edit_profile);
        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));

        mBirthDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        mNavigationView.setCheckedItem(R.id.nav_profile);

        if(savedInstanceState == null || savedInstanceState.getBoolean(FETCH_DATA)){
            DocumentReference documentReference = mFirestore.collection("users").document(mUserID);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    mFirstName.setText(documentSnapshot.get("fName").toString());
                    mEmail.setText(documentSnapshot.get("email").toString());
                    mCity.setText(documentSnapshot.get("city").toString());
                    mBirthDate.setText(documentSnapshot.get("birthDate").toString());
                    mDescription.setText(documentSnapshot.get("description").toString());
                }
            });

            StorageReference imageRef = mStorageReference.child("profile_images/" + mUserID + "/profile_image");
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ProfileActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.ic_image)
                            .into(mProfileImageView);
                }
            });
        }

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUpdate();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(FETCH_DATA, false);
        outState.putString(EMAIL, mEmail.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mEmail.setText(savedInstanceState.getString(EMAIL));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_IMAGE_CODE){
            switch (resultCode){
                case RESULT_OK:
                    mSelectedImage = data.getData();
                    mProfileImageView.setImageURI(mSelectedImage);
                    break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = year + "-" + (month+1) + "-" + dayOfMonth;
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        mBirthDate.setText(newDate);
    }

    private void showDatePickerDialog(){
        String[] date = mBirthDate.getText().toString().split("\\.");
        Log.d(TAG, "showDatePickerDialog: " + date.length);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Integer.parseInt(date[2]),
                Integer.parseInt(date[1])-1,
                Integer.parseInt(date[0])
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 568025136000L);
        datePickerDialog.show();
    }

    public void imageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    public void handleUpdate(){
        boolean flag = true;
        if(TextUtils.isEmpty(mFirstName.getText().toString()) || mFirstName.getText().toString().length() < 3){
            flag = false;
            mFirstName.setError("Musisz podać imię o długości co najmniej 3 znaków");
        }
        if(TextUtils.isEmpty(mCity.getText().toString()) || mCity.getText().toString().length() < 3){
            flag = false;
            mCity.setError("Musisz podać swoją miejscowość o długości co najmniej 3 znaków");
        }
        if(TextUtils.isEmpty(mBirthDate.getText().toString())){
            flag = false;
            mBirthDate.setError("Musisz podać swoją datę urodzenia!");
        }
        if(flag){
            if(mSelectedImage != null){
                mStorageReference.child("profile_images/" + mUserID + "/profile_image").putFile(mSelectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isCanceled()){
                            Toast.makeText(ProfileActivity.this, "Coś poszło nie tak przy wysyłaniu zdjęcia!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            Map<String, Object> user = new HashMap<>();
            user.put("fName", mFirstName.getText().toString());
            user.put("city", mCity.getText().toString());
            user.put("birthDate", mBirthDate.getText().toString());
            user.put("description", mDescription.getText().toString());

            mFirestore.collection("users").document(mUserID).update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ProfileActivity.this, "Pomyślnie zaktualizowano dane!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(ProfileActivity.this, "Coś poszło nie tak przy aktualizacji danych!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}
