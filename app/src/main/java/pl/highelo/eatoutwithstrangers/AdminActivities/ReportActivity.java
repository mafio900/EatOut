package pl.highelo.eatoutwithstrangers.AdminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.ReportsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.ProfilePreviewActivity;
import pl.highelo.eatoutwithstrangers.R;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "ReportActivity";

    private Toolbar mToolbar;

    private FirebaseFirestore mFirestore;
    private ReportsModel mReportsModel;
    private UsersModel mReportingUser;
    private UsersModel mReportedUser;
    private int mPosition;

    //Reporting user
    private CircleImageView mReportingUserImage;
    private TextView mReportingUserName, mReportingUserAge, mReportingUserDescription;

    //Reported user
    private CircleImageView mReportedUserImage;
    private TextView mReportedUserName, mReportedUserAge, mReportedUserDescription;

    //Report info
    private TextView mReportTheme, mReportDate, mReportMessage;

    private Button mActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.report_preview);

        Intent intent = getIntent();
        mReportsModel = intent.getParcelableExtra("model");

        mReportingUserImage = findViewById(R.id.report_reporting_user_image);
        mReportingUserName = findViewById(R.id.report_reporting_user_name);
        mReportingUserAge = findViewById(R.id.report_reporting_user_age);
        mReportingUserDescription = findViewById(R.id.report_reporting_user_description);

        mReportedUserImage = findViewById(R.id.report_reported_user_image);
        mReportedUserName = findViewById(R.id.report_reported_user_name);
        mReportedUserAge = findViewById(R.id.report_reported_user_age);
        mReportedUserDescription = findViewById(R.id.report_reported_user_description);

        mReportTheme = findViewById(R.id.report_theme);
        mReportDate = findViewById(R.id.report_date);
        mReportMessage = findViewById(R.id.report_message);

        mActionButton = findViewById(R.id.report_action_button);

        mReportTheme.setText(getString(R.string.theme) + ": " + mReportsModel.getTheme());
        mReportDate.setText(getString(R.string.report_date) + ": " + CommonMethods.parseDate(mReportsModel.getTimeStamp()));
        mReportMessage.setText(getString(R.string.report_message) + ": " + mReportsModel.getMessage());

        mFirestore = FirebaseFirestore.getInstance();

        mFirestore.collection("users").document(mReportsModel.getReportingUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    mReportingUser = task.getResult().toObject(UsersModel.class);
                    mReportingUser.setUserID(task.getResult().getId());
                    setReportingUserUI();
                }
            }
        });

        mFirestore.collection("users").document(mReportsModel.getReportedUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    mReportedUser = task.getResult().toObject(UsersModel.class);
                    mReportedUser.setUserID(task.getResult().getId());
                    setReportedUserUI();
                }
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.report_action_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.reports_option, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mPosition){
                    case 0:
                        deleteReport();
                        break;
                    case 1:
                        banUser("reported");
                        break;
                    case 2:
                        banUser("reporting");
                        break;
                }
            }
        });
    }

    private void setReportingUserUI(){
        if(mReportingUser != null){
            mReportingUserName.setText(mReportingUser.getfName());
            mReportingUserAge.setText(String.valueOf(CommonMethods.getAge(mReportingUser.getBirthDate())));
            mReportingUserDescription.setText(mReportingUser.getDescription());
            Glide.with(this)
                    .load(mReportingUser.getImage_thumbnail())
                    .placeholder(R.drawable.ic_person)
                    .into(mReportingUserImage);
            mReportingUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ReportActivity.this, ProfilePreviewActivity.class);
                    intent.putExtra("user", mReportingUser);
                    startActivity(intent);
                }
            });
        }
    }

    private void setReportedUserUI(){
        if(mReportedUser != null){
            mReportedUserName.setText(mReportedUser.getfName());
            mReportedUserAge.setText(String.valueOf(CommonMethods.getAge(mReportedUser.getBirthDate())));
            mReportedUserDescription.setText(mReportedUser.getDescription());
            Glide.with(this)
                    .load(mReportedUser.getImage_thumbnail())
                    .placeholder(R.drawable.ic_person)
                    .into(mReportedUserImage);
            mReportedUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ReportActivity.this, ProfilePreviewActivity.class);
                    intent.putExtra("user", mReportedUser);
                    startActivity(intent);
                }
            });
        }
    }

    private void deleteReport(){
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_report)
            .setMessage(R.string.delete_report_ask)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mFirestore.collection("reports").document(mReportsModel.getReportID()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ReportActivity.this, R.string.delete_report_successfully, Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(ReportActivity.this, R.string.delete_report_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
    }

    private void banUser(String whichUser){
        final UsersModel user;
        if(whichUser.equals("reported")){
            user = mReportedUser;
        }else if(whichUser.equals("reporting")){
            user = mReportingUser;
        }else{
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_ban_user, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.ban, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        ((TextView)view.findViewById(R.id.dialog_ban_user_title)).setText(getString(R.string.ban_user) + " " + user.getfName());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextInputLayout reason = dialog.findViewById(R.id.dialog_ban_user_reason);
                        if(TextUtils.isEmpty(reason.getEditText().getText())){
                            reason.setError(getString(R.string.reason_cannot_be_empty));
                            return;
                        }else{reason.setError(null);}
                        if(reason.getEditText().getText().length() < 5){
                            reason.setError(getString(R.string.reason_must_have_5chars));
                            return;
                        }else{reason.setError(null);}

                        CommonMethods.banUser(user.getUserID(), reason.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if(task.isSuccessful() && task.getResult()){
                                    dialog.dismiss();
                                    Toast.makeText(ReportActivity.this, R.string.user_banned, Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(ReportActivity.this, R.string.error_while_banning, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }
        });
        dialog.show();
    }
}