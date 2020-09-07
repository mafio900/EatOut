package pl.highelo.eatoutwithstrangers.ProfileActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.R;

public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Toolbar mToolbar;

    private TextInputLayout mEditFirstName, mEditCity, mEditDescription, mEditBirthDate;
    private Button mEditButton;

    String date;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String mUserID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.checkIfBanned(this);

        setContentView(R.layout.activity_edit_profile);

        setupUI(findViewById(R.id.parent));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.editing_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        mEditFirstName = (TextInputLayout) findViewById(R.id.edit_firstname);
        mEditCity = (TextInputLayout) findViewById(R.id.edit_city);
        mEditDescription = (TextInputLayout) findViewById(R.id.edit_description);
        mEditBirthDate = (TextInputLayout) findViewById(R.id.edit_birthdate);
        mEditButton = (Button) findViewById(R.id.edit_button);

        DocumentReference documentReference = mFirestore.collection("users").document(mUserID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mEditFirstName.getEditText().setText(documentSnapshot.get("fName").toString());
                mEditCity.getEditText().setText(documentSnapshot.get("city").toString());
                mEditDescription.getEditText().setText(documentSnapshot.get("description").toString());
                mEditBirthDate.getEditText().setText(documentSnapshot.get("birthDate").toString());
            }
        });

        mEditBirthDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUpdate();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = year + "-" + (month+1) + "-" + dayOfMonth;
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        mEditBirthDate.getEditText().setText(newDate);
    }

    private void showDatePickerDialog(){
        String[] date = mEditBirthDate.getEditText().getText().toString().split("\\.");
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

    public void handleUpdate(){
        boolean flag = true;
        if(TextUtils.isEmpty(mEditFirstName.getEditText().getText().toString()) || mEditFirstName.getEditText().getText().toString().length() < 3){
            flag = false;
            mEditFirstName.setError(getString(R.string.name_must_have_three_letters));
        }
        if(TextUtils.isEmpty(mEditCity.getEditText().getText().toString()) || mEditCity.getEditText().getText().toString().length() < 3){
            flag = false;
            mEditCity.setError(getString(R.string.city_must_have_three_letters));
        }
        if(TextUtils.isEmpty(mEditBirthDate.getEditText().getText().toString())){
            flag = false;
            mEditBirthDate.setError(getString(R.string.need_set_birthdate));
        }
        if(flag){
            Map<String, Object> user = new HashMap<>();
            user.put("fName", mEditFirstName.getEditText().getText().toString());
            user.put("city", mEditCity.getEditText().getText().toString());
            user.put("birthDate", mEditBirthDate.getEditText().getText().toString());
            user.put("description", mEditDescription.getEditText().getText().toString());

            mFirestore.collection("users").document(mUserID).update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(EditProfileActivity.this, "Pomyślnie zaktualizowano dane!", Toast.LENGTH_LONG).show();
                        finish();
                    }else{
                        Toast.makeText(EditProfileActivity.this, "Coś poszło nie tak przy aktualizacji danych!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    CommonMethods.hideKeyboard(EditProfileActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}