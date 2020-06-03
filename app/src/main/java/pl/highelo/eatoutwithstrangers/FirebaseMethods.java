package pl.highelo.eatoutwithstrangers;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
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

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    public static void validateUser(final AppCompatActivity t) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            mUser.reload().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        Log.d(TAG, "user doesn't exist anymore");
                        Toast.makeText(t, R.string.acc_deleted_or_failed, Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        t.startActivity(new Intent(t.getApplicationContext(), Login.class));
                        t.finish();
                    }
                }
            });
        }
    }

    public static void checkIfBanned(final AppCompatActivity t){
        Log.d(TAG, "checkIfBanned: starts");
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
