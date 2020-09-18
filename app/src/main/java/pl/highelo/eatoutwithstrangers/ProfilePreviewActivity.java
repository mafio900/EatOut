package pl.highelo.eatoutwithstrangers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
        mToolbar.setTitle("Podgląd profilu");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersModel = getIntent().getParcelableExtra("user");

        ImageView userImage = findViewById(R.id.profile_preview_image);
        TextView userName = findViewById(R.id.profile_preview_name);
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
                final EditText taskEditText = new EditText(ProfilePreviewActivity.this);
                AlertDialog dialog = new AlertDialog.Builder(ProfilePreviewActivity.this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("TAG", "onClick: " + taskEditText.getText());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });
    }
}