package pl.highelo.eatoutwithstrangers.StartActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.MainActivity;
import pl.highelo.eatoutwithstrangers.R;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "RegisterActivity";
    private Toolbar mToolbar;

    private TextInputLayout mFirstName, mEmail, mPassword, mConfirmPassword, mCity, mBirthDate;
    private Button mRegisterBtn;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mFirestore;
    String userID;
    String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupUI(findViewById(R.id.parent));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.signing_in);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        mFirstName = (TextInputLayout) findViewById(R.id.register_name);
        mEmail = (TextInputLayout) findViewById(R.id.register_email);
        mPassword = (TextInputLayout) findViewById(R.id.register_password);
        mConfirmPassword = (TextInputLayout) findViewById(R.id.register_confirm_password);
        mCity = (TextInputLayout) findViewById(R.id.register_city);
        mRegisterBtn = (Button) findViewById(R.id.register_button);
        mBirthDate = (TextInputLayout) findViewById(R.id.register_birth_date);
        mProgressBar = (ProgressBar) findViewById(R.id.register_progressBar);

        mBirthDate.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();
                String confirmPassword = mConfirmPassword.getEditText().getText().toString().trim();
                final String firstName = mFirstName.getEditText().getText().toString().trim();
                final String city = mCity.getEditText().getText().toString().trim();
                boolean flag = true;

                if(TextUtils.isEmpty(firstName) || firstName.length() < 3){
                    mFirstName.setError(getString(R.string.name_must_have_three_letters));
                    flag = false;
                }else{mFirstName.setError(null);}
                if(TextUtils.isEmpty(email)){
                    mEmail.setError(getString(R.string.error_email));
                    flag = false;
                }else{mEmail.setError(null);}
                if(TextUtils.isEmpty(password)){
                    mPassword.setError(getString(R.string.password_required));
                    flag = false;
                } else if(password.length() < 6){
                    mPassword.setError(getString(R.string.password_required_more_than_6_chars));
                    flag = false;
                }else{mPassword.setError(null);}
                if(!confirmPassword.equals(password)){
                    mConfirmPassword.setError(getString(R.string.passwords_must_be_same));
                    flag = false;
                }else{mConfirmPassword.setError(null);}
                if(TextUtils.isEmpty(city) || city.length() < 3){
                    mCity.setError(getString(R.string.city_must_have_three_letters));
                    flag = false;
                }else{mCity.setError(null);}
                if(TextUtils.isEmpty(date)){
                    mBirthDate.setError(getString(R.string.birth_date_required));
                    flag = false;
                }else{mBirthDate.setError(null);}

                if(flag){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRegisterBtn.setClickable(false);

                    //register user

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                userID = mAuth.getCurrentUser().getUid();
                                Log.d(TAG, "onComplete: " + userID);
                                DocumentReference documentReference = mFirestore.collection("users").document(userID);
                                Map<String, Object> user = new HashMap<>();
                                user.put("fName", firstName);
                                user.put("email", email);
                                user.put("city", city);
                                user.put("description", "");
                                SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
                                SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                                String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
                                user.put("birthDate", newDate);
                                documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(RegisterActivity.this, R.string.account_created, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            mAuth.getCurrentUser().sendEmailVerification();
                                            mAuth.signOut();
                                            finish();
                                        } else{
                                            mAuth.getCurrentUser().delete();
                                            mAuth.signOut();
                                            Toast.makeText(RegisterActivity.this, R.string.account_failure, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                if(!task.isSuccessful()) {
                                    try {
                                        throw task.getException();
                                    } catch(FirebaseAuthWeakPasswordException e) {
                                        mPassword.setError(getString(R.string.given_password_too_weak));
                                        mPassword.requestFocus();
                                    } catch(FirebaseAuthInvalidCredentialsException e) {
                                        mEmail.setError(getString(R.string.incorrect_email));
                                        mEmail.requestFocus();
                                    } catch(FirebaseAuthUserCollisionException e) {
                                        mEmail.setError(getString(R.string.email_extists));
                                        mEmail.requestFocus();
                                    } catch(Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                            }
                            mProgressBar.setVisibility(View.GONE);
                            mRegisterBtn.setClickable(true);
                        }
                    });
                }
            }
        });
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 568025136000L);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = year + "-" + (month+1) + "-" + dayOfMonth;
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        mBirthDate.getEditText().setText(newDate);
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    CommonMethods.hideKeyboard(RegisterActivity.this);
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
