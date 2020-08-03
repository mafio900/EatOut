package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Register extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "Register";
    private Toolbar mToolbar;

    private EditText mFirstName, mEmail, mPassword, mCity, mBirthDate;
    private Button mRegisterBtn, mBirthDateButton;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private TextView mLogin;
    private FirebaseFirestore mFirestore;
    String userID;
    String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mFirestore = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mFirstName = (EditText) findViewById(R.id.nameRgET);
        mEmail = (EditText) findViewById(R.id.emailRgET);
        mPassword = (EditText) findViewById(R.id.passwordRgET);
        mCity = (EditText) findViewById(R.id.cityRgET);
        mRegisterBtn = (Button) findViewById(R.id.registerRgBT);
        mBirthDate = (EditText) findViewById(R.id.birthDateRgET);
        mBirthDateButton = (Button) findViewById(R.id.setBirthDateRgButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarRg);
        mLogin = (TextView) findViewById(R.id.loginRgTV);

        mBirthDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String firstName = mFirstName.getText().toString().trim();
                final String city = mCity.getText().toString().trim();
                boolean flag = true;

                if(TextUtils.isEmpty(firstName) || firstName.length() < 3){
                    mFirstName.setError("Musisz podać imię o długości co najmniej 3 znaków");
                    flag = false;
                }
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email jest wymagany");
                    flag = false;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Hasło jest wymagane");
                    flag = false;
                }
                if(password.length() < 6){
                    mPassword.setError("Hasło musi posiadać 6 lub więcej znaków");
                    flag = false;
                }
                if(TextUtils.isEmpty(city) || city.length() < 3){
                    mCity.setError("Musisz podać swoją miejscowość o długości co najmniej 3 znaków");
                    flag = false;
                }
                if(TextUtils.isEmpty(date)){
                    mBirthDate.setError("Musisz podać swoją datę urodzenia!");
                    flag = false;
                }

                if(flag){
                    mProgressBar.setVisibility(View.VISIBLE);

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
                                user.put("isBanned", false);
                                user.put("description", "");
                                SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-M-d", Locale.US);
                                SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                                String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
                                user.put("birthDate", newDate);
                                documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Register.this, R.string.account_created, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        } else{
                                            mAuth.getCurrentUser().delete();
                                            mAuth.signOut();
                                            Toast.makeText(Register.this, R.string.account_failure, Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            }else{
                                if(task.getException() instanceof FirebaseAuthEmailException)
                                    Toast.makeText(Register.this, R.string.incorrect_email, Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Register.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
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
        mBirthDate.setText(newDate);
    }
}
