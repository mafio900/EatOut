package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.StartActivities.LoginActivity;

public class CommonMethods {

    public static void validateUser(final AppCompatActivity t) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            mUser.reload().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        if(((FirebaseAuthInvalidUserException) e).getErrorCode().equals("ERROR_USER_DISABLED")){
                            Toast.makeText(t, R.string.acc_banned, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(t, R.string.password_changed, Toast.LENGTH_LONG).show();
                        }
                        FirebaseAuth.getInstance().signOut();
                        t.startActivity(new Intent(t.getApplicationContext(), LoginActivity.class));
                        t.finish();
                    }
                }
            });
        }
    }
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

    public Task<Boolean> makeAdmin(String uid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("makeAdmin")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        boolean result = (Boolean) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static Task<Boolean> revokeAdmin(String uid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("revokeAdmin")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        boolean result = (Boolean) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static Task<Boolean> banUser(String uid, String reason) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("reason", reason);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("banUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        boolean result = (Boolean) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static Task<Boolean> unbanUser(String uid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("unBanUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        boolean result = (Boolean) task.getResult().getData();
                        return result;
                    }
                });
    }

//    public static void checkIfBanned(final AppCompatActivity t){
//        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
//        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(mUser != null){
//            String userID = mUser.getUid();
//            DocumentReference documentReference = mFirestore.collection("users").document(userID);
//            documentReference.addSnapshotListener(t, new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                    if(documentSnapshot.get("isBanned") == null || documentSnapshot.get("isBanned").toString().equals("true")){
//                        Toast.makeText(t, R.string.acc_banned, Toast.LENGTH_LONG).show();
//                        FirebaseAuth.getInstance().signOut();
//                        t.startActivity(new Intent(t.getApplicationContext(), StartActivity.class));
//                        t.finish();
//                    }
//                }
//            });
//        }
//    }

    public static String parseDate(Timestamp timestamp) {
        GregorianCalendar d = new GregorianCalendar(TimeZone.getTimeZone("Europe/Warsaw"));
        d.setTime(timestamp.toDate());
        String inputDateString = d.get(Calendar.DAY_OF_MONTH)+"."
                +(d.get(Calendar.MONTH)+1)+"."
                +d.get(Calendar.YEAR)+" "
                +d.get(Calendar.HOUR_OF_DAY)+":"
                +d.get(Calendar.MINUTE);
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("d.M.yyyy H:m", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

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

    public static void showDialog(final AppCompatActivity t, String message){
        new AlertDialog.Builder(t)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        t.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

    public static class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Remove the string out of destination that is to be replaced
                String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
                // Add the new string in
                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
                int input = Integer.parseInt(newVal);
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }
    }
}
