package pl.highelo.eatoutwithstrangers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
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

        Glide.with(this)
                .load(mUsersModel.getImage())
                .placeholder(R.drawable.ic_person)
                .into(userImage);
        userName.setText(mUsersModel.getfName() + ",");
        userAge.setText(String.valueOf(CommonMethods.getAge(mUsersModel.getBirthDate())));
        userCity.setText(getString(R.string.live_in) + " " + mUsersModel.getCity());
        userDescription.setText(mUsersModel.getDescription());
    }
}