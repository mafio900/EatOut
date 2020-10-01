package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import pl.highelo.eatoutwithstrangers.AdminActivities.AdminActivity;
import pl.highelo.eatoutwithstrangers.JoinedEvents.JoinedEventsActivity;
import pl.highelo.eatoutwithstrangers.MainActivity;
import pl.highelo.eatoutwithstrangers.ManageEvent.YourEventsActivity;
import pl.highelo.eatoutwithstrangers.ProfileActivities.ChangePasswordActivity;
import pl.highelo.eatoutwithstrangers.ProfileActivities.ProfileActivity;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.SearchEvent.SearchEventActivity;
import pl.highelo.eatoutwithstrangers.StartActivities.StartActivity;

public class NavbarInterface implements NavigationView.OnNavigationItemSelectedListener {

    private AppCompatActivity t;

    public NavbarInterface(AppCompatActivity tt, final Menu menu){
        t = tt;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            user.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    if(getTokenResult.getClaims().get("admin") != null && ((Boolean) getTokenResult.getClaims().get("admin"))){
                        menu.findItem(R.id.nav_admin_page).setVisible(true);
                    }
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                if(!(t instanceof MainActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), MainActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_admin_page:
                if(!(t instanceof AdminActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), AdminActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_search_events:
                if(!(t instanceof SearchEventActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), SearchEventActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_joined_events:
                if(!(t instanceof JoinedEventsActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), JoinedEventsActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_your_events:
                if(!(t instanceof YourEventsActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), YourEventsActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_profile:
                if(!(t instanceof ProfileActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), ProfileActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_change_password:
                if(!(t instanceof ChangePasswordActivity)){
                    t.startActivity(new Intent(t.getApplicationContext(), ChangePasswordActivity.class));
                    t.finish();
                }
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                t.startActivity(new Intent(t.getApplicationContext(), StartActivity.class));
                t.finish();
                break;
        }
        return true;
    }
}
