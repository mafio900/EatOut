package pl.highelo.eatoutwithstrangers.ProfileActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.MainActivity;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.NavbarInterface;
import pl.highelo.eatoutwithstrangers.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private TextInputLayout mOldPassword, mNewPassword, mConfirmNewPassword;
    private Button mSaveButton;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_change_password);

        setupUI(findViewById(R.id.parent));

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.changing_password);
        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mUser = mAuth.getCurrentUser();

        mOldPassword = (TextInputLayout) findViewById(R.id.change_old_password);
        mNewPassword = (TextInputLayout) findViewById(R.id.change_new_password);
        mConfirmNewPassword = (TextInputLayout) findViewById(R.id.change_confirm_new_password);
        mSaveButton = (Button) findViewById(R.id.change_button);
        mProgressBar = findViewById(R.id.change_progressBar);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null){
                    String oldPassword = mOldPassword.getEditText().getText().toString().trim();
                    final String newPassword = mNewPassword.getEditText().getText().toString().trim();
                    String confirmNewPassword = mConfirmNewPassword.getEditText().getText().toString().trim();
                    boolean flag = true;
                    if(TextUtils.isEmpty(oldPassword)){
                        mOldPassword.setError(getString(R.string.password_required));
                        flag = false;
                    }else{mOldPassword.setError(null);}
                    if(TextUtils.isEmpty(newPassword)){
                        mNewPassword.setError(getString(R.string.password_required));
                        flag = false;
                    } else if(newPassword.length() < 6){
                        mNewPassword.setError(getString(R.string.password_required_more_than_6_chars));
                        flag = false;
                    }else{mNewPassword.setError(null);}
                    if(!confirmNewPassword.equals(newPassword)){
                        mConfirmNewPassword.setError(getString(R.string.passwords_must_be_same));
                        flag = false;
                    }else{mConfirmNewPassword.setError(null);}
                    if(flag){
                        mProgressBar.setVisibility(ProgressBar.VISIBLE);
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(mUser.getEmail(), oldPassword);

                        mUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mProgressBar.setVisibility(ProgressBar.GONE);
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ChangePasswordActivity.this, R.string.password_changed, Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ChangePasswordActivity.this, R.string.something_went_wrong_while_chaning_passwd, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(ChangePasswordActivity.this, R.string.incorrect_old_password, Toast.LENGTH_LONG).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            CommonMethods.showDialog(this, getString(R.string.sure_to_leave_app));
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    CommonMethods.hideKeyboard(ChangePasswordActivity.this);
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
