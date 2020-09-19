package pl.highelo.eatoutwithstrangers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;

public class ProfilePreviewActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private UsersModel mUsersModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_preview);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.profile_preview);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersModel = getIntent().getParcelableExtra("user");

        ImageView userImage = findViewById(R.id.profile_preview_image);
        final TextView userName = findViewById(R.id.profile_preview_name);
        TextView userAge = findViewById(R.id.profile_preview_age);
        TextView userCity = findViewById(R.id.profile_preview_city);
        TextView userDescription = findViewById(R.id.profile_preview_description);
        ImageButton reportButton = findViewById(R.id.profile_preview_report_button);

        Glide.with(this)
                .load(mUsersModel.getImage())
                .placeholder(R.drawable.ic_person)
                .into(userImage);
        userName.setText(mUsersModel.getfName() + ",");
        userAge.setText(String.valueOf(CommonMethods.getAge(mUsersModel.getBirthDate())));
        userCity.setText(getString(R.string.live_in) + " " + mUsersModel.getCity());
        userDescription.setText(mUsersModel.getDescription());

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.dialog_report, null);
                final AlertDialog dialog = new AlertDialog.Builder(ProfilePreviewActivity.this)
                        .setView(view)
                        .setPositiveButton(R.string.report, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                ((TextView)view.findViewById(R.id.dialog_report_title)).setText(getString(R.string.report_user) + " " + mUsersModel.getfName());
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TextInputLayout theme = dialog.findViewById(R.id.dialog_report_theme);
                                TextInputLayout message = dialog.findViewById(R.id.dialog_report_message);
                                if(TextUtils.isEmpty(theme.getEditText().getText())){
                                    theme.setError(getString(R.string.theme_cannot_be_empty));
                                    return;
                                }else{theme.setError(null);}
                                if(message.getEditText().getText().length() < 10){
                                    message.setError(getString(R.string.message_must_have_atleast_10_chars));
                                    return;
                                }else{message.setError(null);}

                                HashMap<String, Object> report = new HashMap<>();
                                report.put("theme", theme.getEditText().getText().toString());
                                report.put("message", message.getEditText().getText().toString());
                                report.put("timeStamp", Timestamp.now());
                                report.put("reportedUser", mUsersModel.getUserID());
                                report.put("reportingUser", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                FirebaseFirestore.getInstance().collection("reports").add(report).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(ProfilePreviewActivity.this, R.string.user_has_been_reported, Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                        else{
                                            Toast.makeText(ProfilePreviewActivity.this, R.string.error_while_reporting, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }
}