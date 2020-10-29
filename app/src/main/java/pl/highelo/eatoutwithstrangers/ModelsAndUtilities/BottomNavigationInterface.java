package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pl.highelo.eatoutwithstrangers.EventPages.CalendarActivity;
import pl.highelo.eatoutwithstrangers.EventPages.CreateEvent.CreateEventActivity;
import pl.highelo.eatoutwithstrangers.PrivateMessages.PrivateMessagesActivity;
import pl.highelo.eatoutwithstrangers.ProfileActivities.ProfileActivity;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.SearchEvent.SearchEventActivity;

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

        if(!(context instanceof PrivateMessagesActivity)){
            ImageView profile = view.findViewById(R.id.navbar_message);
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, PrivateMessagesActivity.class));
                    context.finish();
                }
            });
        }
    }
}
