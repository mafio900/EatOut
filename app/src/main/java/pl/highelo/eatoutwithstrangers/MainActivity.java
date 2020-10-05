package pl.highelo.eatoutwithstrangers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.BottomNavigationInterface;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonMethods.validateUser(this);

        setContentView(R.layout.activity_main);
        new BottomNavigationInterface(this, findViewById(R.id.parent_layout));
    }

    @Override
    public void onBackPressed() {
        CommonMethods.showDialog(this, getString(R.string.sure_to_leave_app));
    }
}
