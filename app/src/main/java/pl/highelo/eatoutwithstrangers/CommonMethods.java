package pl.highelo.eatoutwithstrangers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
//                        t.startActivity(new Intent(t.getApplicationContext(), Login.class));
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
        String userID = mUser.getUid();

        DocumentReference documentReference = mFirestore.collection("users").document(userID);
        documentReference.addSnapshotListener(t, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.get("isBanned") == null || documentSnapshot.get("isBanned").toString().equals("true")){
                    Toast.makeText(t, R.string.acc_banned, Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    t.startActivity(new Intent(t.getApplicationContext(), Login.class));
                    t.finish();
                }
            }
        });
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
}
