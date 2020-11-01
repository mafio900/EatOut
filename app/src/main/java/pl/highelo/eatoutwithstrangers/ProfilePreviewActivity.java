package pl.highelo.eatoutwithstrangers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.PrivateMessagesModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.PrivateMessages.PrivateChatActivity;

public class ProfilePreviewActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private UsersModel mUsersModel;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_preview);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.profile_preview);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersModel = getIntent().getParcelableExtra("user");
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView userImage = findViewById(R.id.profile_preview_image);
        final TextView userName = findViewById(R.id.profile_preview_name);
        TextView userAge = findViewById(R.id.profile_preview_age);
        TextView userCity = findViewById(R.id.profile_preview_city);
        TextView userDescription = findViewById(R.id.profile_preview_description);
        ImageButton reportButton = findViewById(R.id.profile_preview_report_button);
        ImageButton messageButton = findViewById(R.id.profile_preview_message);

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
                setReportDialog();
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMessage();
            }
        });
    }

    private void setMessage() {
        mFirestore.collection("privateMessages").whereArrayContains("users", mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){
                        showMessageDialog();
                    }else{
                        for(DocumentSnapshot doc : task.getResult().getDocuments()){
                            List<String> users = (List<String>) doc.get("users");
                            if(users.contains(mUsersModel.getUserID())){
                                Intent intent = new Intent(ProfilePreviewActivity.this, PrivateChatActivity.class);
                                PrivateMessagesModel model = doc.toObject(PrivateMessagesModel.class);
                                model.setPrivateMessageID(doc.getId());
                                intent.putExtra("model", model);
                                startActivity(intent);
                                return;
                            }
                        }
                        showMessageDialog();
                    }
                }else{
                    Toast.makeText(ProfilePreviewActivity.this, R.string.sorry_try_again, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMessageDialog(){
        final View view = getLayoutInflater().inflate(R.layout.dialog_send_message, null);
        new AlertDialog.Builder(ProfilePreviewActivity.this)
                .setView(view)
                .setTitle("Send message to " + mUsersModel.getfName())
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Object> privMess = new HashMap<>();
                        List<String> users = new ArrayList<>();
                        users.add(mUsersModel.getUserID());
                        users.add(mAuth.getCurrentUser().getUid());
                        privMess.put("users", users);
                        privMess.put("timestamp", FieldValue.serverTimestamp());
                        mFirestore.collection("privateMessages").add(privMess).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if(task.isSuccessful()){
                                    HashMap<String, Object> message = new HashMap<>();
                                    message.put("userID", mAuth.getCurrentUser().getUid());
                                    message.put("time", FieldValue.serverTimestamp());
                                    message.put("message", ((TextInputLayout)view.findViewById(R.id.dialog_send_message)).getEditText().getText().toString());
                                    mFirestore.collection("privateMessages").document(task.getResult().getId()).collection("messages").add(message).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ProfilePreviewActivity.this, "Message has been sent", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(ProfilePreviewActivity.this, R.string.sorry_try_again, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(ProfilePreviewActivity.this, R.string.sorry_try_again, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setReportDialog(){
        View view = getLayoutInflater().inflate(R.layout.dialog_report, null);
        final AlertDialog dialog = new AlertDialog.Builder(ProfilePreviewActivity.this)
                .setView(view)
                .setTitle(getString(R.string.report_user) + " " + mUsersModel.getfName())
                .setPositiveButton(R.string.report, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
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
}