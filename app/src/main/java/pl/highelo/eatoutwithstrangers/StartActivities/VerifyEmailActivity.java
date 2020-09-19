package pl.highelo.eatoutwithstrangers.StartActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.highelo.eatoutwithstrangers.MainActivity;
import pl.highelo.eatoutwithstrangers.R;

public class VerifyEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user.isEmailVerified()){
            startActivity(new Intent(VerifyEmailActivity.this, MainActivity.class));
            finish();
            return;
        }

        Button logoutButton = findViewById(R.id.verify_email_logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(VerifyEmailActivity.this, StartActivity.class));
                finish();
            }
        });

        final Button checkButton = findViewById(R.id.verify_email_check_button);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful() && user.isEmailVerified()){
                            Toast.makeText(VerifyEmailActivity.this, R.string.email_verified, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(VerifyEmailActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(VerifyEmailActivity.this, R.string.email_not_verified, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        final Button verificationButton = (Button) findViewById(R.id.verify_email_button);
        verificationButton.setVisibility(View.VISIBLE);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            verificationButton.setClickable(false);
                            Toast.makeText(VerifyEmailActivity.this, R.string.verification_email_sent, Toast.LENGTH_SHORT).show();
                            verificationButton.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    verificationButton.setClickable(true);
                                }
                            }, 60000);
                            checkButton.setVisibility(View.VISIBLE);
                        } else{
                            Toast.makeText(VerifyEmailActivity.this, R.string.send_email_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}