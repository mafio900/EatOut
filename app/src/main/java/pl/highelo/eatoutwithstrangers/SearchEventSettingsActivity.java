package pl.highelo.eatoutwithstrangers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SearchEventSettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RadioGroup mRadioGroup;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String DISTANCE = "distance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Ustawienia szukania");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRadioGroup = (RadioGroup) findViewById(R.id.settings_radio_group);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        switch (sharedPreferences.getInt(DISTANCE, 10)){
            case 10:
                ((RadioButton) findViewById(R.id.settings_radio_button_10km)).setChecked(true);
                break;
            case 15:
                ((RadioButton) findViewById(R.id.settings_radio_button_15km)).setChecked(true);
                break;
            case 20:
                ((RadioButton) findViewById(R.id.settings_radio_button_20km)).setChecked(true);
                break;
            case 30:
                ((RadioButton) findViewById(R.id.settings_radio_button_30km)).setChecked(true);
                break;
        }
    }

    public void checkButton(View view) {
        int radioId = mRadioGroup.getCheckedRadioButtonId();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (radioId){
            case R.id.settings_radio_button_10km:
                editor.putInt(DISTANCE, 10);
                break;
            case R.id.settings_radio_button_15km:
                editor.putInt(DISTANCE, 15);
                break;
            case R.id.settings_radio_button_20km:
                editor.putInt(DISTANCE, 20);
                break;
            case R.id.settings_radio_button_30km:
                editor.putInt(DISTANCE, 30);
                break;
        }
        editor.apply();
    }
}