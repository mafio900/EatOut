package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import pl.highelo.eatoutwithstrangers.EventPages.CalendarActivity;
import pl.highelo.eatoutwithstrangers.EventPages.CreateEvent.CreateEventActivity;
import pl.highelo.eatoutwithstrangers.ProfileActivities.ProfileActivity;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.SearchEvent.SearchEventActivity;
import pl.highelo.eatoutwithstrangers.StartActivities.LoginActivity;

public class BottomNavigationInterface {
    private static final String TAG = "BottomNavigationInterface";


    public BottomNavigationInterface(final AppCompatActivity context, final View view) {

        if(!(context instanceof CalendarActivity)){
            ImageView calendar = view.findViewById(R.id.navbar_calendar);
            calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, CalendarActivity.class));
                    context.finish();
                }
            });
        }

        if(!(context instanceof SearchEventActivity)){
            ImageView search = view.findViewById(R.id.navbar_search);
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, SearchEventActivity.class));
                    context.finish();
                }
            });
        }

        if(!(context instanceof CreateEventActivity)){
            FloatingActionButton createEvent = view.findViewById(R.id.navbar_create_event);
            createEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, CreateEventActivity.class));
                }
            });
        }

        if(!(context instanceof ProfileActivity)){
            ImageView profile = view.findViewById(R.id.navbar_profile);
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, ProfileActivity.class));
                    context.finish();
                }
            });
        }

        ImageView logout = view.findViewById(R.id.navbar_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.sure_to_logout)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startActivity(new Intent(context, LoginActivity.class));
                            context.finish();
                            FirebaseAuth.getInstance().signOut();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            }
        });
    }
}
