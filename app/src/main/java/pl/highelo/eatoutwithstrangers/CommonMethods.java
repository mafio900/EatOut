package pl.highelo.eatoutwithstrangers;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CommonMethods {

//    public static void validateUser(final AppCompatActivity t) {
//        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(mUser != null){
//            mUser.reload().addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    if (e instanceof FirebaseAuthInvalidUserException) {
//                        Toast.makeText(t, R.string.acc_deleted_or_failed, Toast.LENGTH_LONG).show();
//                        FirebaseAuth.getInstance().signOut();
//                        t.startActivity(new Intent(t.getApplicationContext(), LoginActivity.class));
//                        t.finish();
//                    }
//                }
//            });
//        }
//    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void checkIfBanned(final AppCompatActivity t){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            String userID = mUser.getUid();
            DocumentReference documentReference = mFirestore.collection("users").document(userID);
            documentReference.addSnapshotListener(t, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.get("isBanned") == null || documentSnapshot.get("isBanned").toString().equals("true")){
                        Toast.makeText(t, R.string.acc_banned, Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        t.startActivity(new Intent(t.getApplicationContext(), StartActivity.class));
                        t.finish();
                    }
                }
            });
        }
    }

    public static String parseDate(String inputDateString, SimpleDateFormat inputDateFormat, SimpleDateFormat outputDateFormat) {
        Date date = null;
        String outputDateString = null;
        try {
            date = inputDateFormat.parse(inputDateString);
            outputDateString = outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDateString;
    }

    public static int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.MONTH) <= dob.get(Calendar.MONTH)){
            age--;
        }
        return age;
    }
}
