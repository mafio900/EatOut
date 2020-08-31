package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.checkIfBanned(this);

        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(new NavbarInterface(this));


        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();
        mUser = mAuth.getCurrentUser();

        if(mUser != null && !mUser.isEmailVerified()) {
            Button mVerifyBtn = (Button) findViewById(R.id.verifyBtn);
            mVerifyBtn.setVisibility(View.VISIBLE);
            mVerifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Wysłano e-mail weryfikacyjny", Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(MainActivity.this, "Coś poszło nie tak przy wysyłaniu e-maila, spróbuj ponownie później", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
        mNavigationView.setCheckedItem(R.id.nav_home);
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
