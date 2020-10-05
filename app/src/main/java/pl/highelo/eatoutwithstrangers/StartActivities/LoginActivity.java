package pl.highelo.eatoutwithstrangers.StartActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.MainActivity;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.SearchEvent.SearchEventActivity;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mEmail, mPassword;
    private Button mLoginBtn, mForgot;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupUI(findViewById(R.id.parent));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.loging_in);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mEmail = (TextInputLayout) findViewById(R.id.emailLgET);
        mLoginBtn = (Button) findViewById(R.id.loginLgBT);
        mPassword = (TextInputLayout) findViewById(R.id.passwordLgET);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLg);
        mForgot = (Button) findViewById(R.id.forgotPasswdLgBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError(getString(R.string.email_is_required));
                    return;
                } else {
                    mEmail.setError(null);
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError(getString(R.string.password_required));
                    return;
                } else {
                    mPassword.setError(null);
                }
                if (password.length() < 6) {
                    mPassword.setError(getString(R.string.password_required_more_than_6_chars));
                    return;
                } else {
                    mPassword.setError(null);
                }

                mLoginBtn.setClickable(false);
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (task.isSuccessful()) {
                                            String deviceToken = task.getResult().getToken();
                                            FirebaseFirestore.getInstance().collection("users").document(mAuth.getCurrentUser().getUid()).update("tokenId", deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        startActivity(new Intent(getApplicationContext(), SearchEventActivity.class));
                                                        finish();
                                                    }else {
                                                        mAuth.signOut();
                                                        Toast.makeText(LoginActivity.this, "Coś poszło nie tak podczas logowania", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }else {
                                            mAuth.signOut();
                                            Toast.makeText(LoginActivity.this, "Coś poszło nie tak podczas logowania", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                mAuth.signOut();
                                mEmail.setError(getString(R.string.email_not_verified));
                            }
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                if (e.getErrorCode().equals("ERROR_USER_DISABLED")) {
                                    Toast.makeText(LoginActivity.this, R.string.acc_banned, Toast.LENGTH_LONG).show();
                                    FirebaseFirestore.getInstance().collection("bannedUsers").whereEqualTo("email", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            CommonMethods.showErrorDialog(LoginActivity.this,
                                                    getString(R.string.acc_banned),
                                                    getString(R.string.reason) + ": " + queryDocumentSnapshots.getDocuments().get(0).get("reason").toString());
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.wrong_login, Toast.LENGTH_SHORT).show();
                                }
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(LoginActivity.this, R.string.wrong_login, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e("TAG", e.getMessage());
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                        mLoginBtn.setClickable(true);
                    }
                });
            }
        });

        mForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), SearchEventActivity.class));
            finish();
        }
        super.onResume();
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    CommonMethods.hideKeyboard(LoginActivity.this);
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
