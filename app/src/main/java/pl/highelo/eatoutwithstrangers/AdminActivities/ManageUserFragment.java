package pl.highelo.eatoutwithstrangers.AdminActivities;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.R;
import pl.highelo.eatoutwithstrangers.StartActivities.LoginActivity;

public class ManageUserFragment extends Fragment {

    private FirebaseFirestore mFirestore;

    private UsersModel mUser;

    private int mPosition;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI(view);
        mFirestore = FirebaseFirestore.getInstance();

        Button searchButton = view.findViewById(R.id.manage_user_search_button);
        final Button actionButton = view.findViewById(R.id.manage_user_action_button);
        final TextInputLayout emailInput = view.findViewById(R.id.manage_user_email_input);
        final CircleImageView imageView = view.findViewById(R.id.manage_user_imageview);
        final TextView name = view.findViewById(R.id.manage_user_name);
        final TextView age = view.findViewById(R.id.manage_user_age);
        final TextView description = view.findViewById(R.id.manage_user_description);

        final RelativeLayout relativeLayoutSpinner = view.findViewById(R.id.manage_user_rel_spinner);
        final Spinner spinner = (Spinner) view.findViewById(R.id.manage_user_action_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.manage_user_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mPosition){
                    case 0:
                        banUserDialog();
                        break;
                    case 1:
                        unbanUser();
                        break;
                    case 2:
                        giveAdmin();
                        break;
                    case 3:
                        revokeAdmin();
                        break;
                    default:
                        Toast.makeText(getContext(), "inoperative", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String email = emailInput.getEditText().getText().toString();
                if(TextUtils.isEmpty(email)){
                    emailInput.setError(getString(R.string.email_is_required));
                }else{
                    emailInput.setError(null);
                    mFirestore.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()){
                                imageView.setVisibility(View.VISIBLE);
                                name.setVisibility(View.VISIBLE);
                                age.setVisibility(View.VISIBLE);
                                description.setVisibility(View.VISIBLE);
                                actionButton.setVisibility(View.VISIBLE);
                                relativeLayoutSpinner.setVisibility(View.VISIBLE);
                                for(DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    mUser = doc.toObject(UsersModel.class);
                                    mUser.setUserID(doc.getId());
                                    Glide.with(imageView.getContext())
                                            .load(mUser.getImage_thumbnail())
                                            .placeholder(R.drawable.ic_person)
                                            .into(imageView);
                                    name.setText(mUser.getfName());
                                    age.setText(String.valueOf(CommonMethods.getAge(mUser.getBirthDate())));
                                    description.setText(mUser.getDescription());
                                }

                            }else{
                                emailInput.setError(getString(R.string.user_doesnt_exist));
                            }
                        }
                    });
                }
            }
        });
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof TextInputEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    CommonMethods.hideKeyboard(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void banUserDialog(){
        View view = getLayoutInflater().inflate(R.layout.dialog_ban_user, null);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(getString(R.string.ban_user) + " " + mUser.getfName())
                .setPositiveButton(R.string.ban, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextInputLayout reason = dialog.findViewById(R.id.dialog_ban_user_reason);
                        if(TextUtils.isEmpty(reason.getEditText().getText())){
                            reason.setError(getString(R.string.reason_cannot_be_empty));
                            return;
                        }else{reason.setError(null);}
                        if(reason.getEditText().getText().length() < 5){
                            reason.setError(getString(R.string.reason_must_have_5chars));
                            return;
                        }else{reason.setError(null);}

                        CommonMethods.banUser(mUser.getUserID(), reason.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if(task.isSuccessful() && task.getResult()){
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), R.string.user_banned, Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getContext(), R.string.error_while_banning, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }
        });
        dialog.show();
    }

    private void unbanUser(){
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.unban_user) + " " + mUser.getfName())
                .setMessage(R.string.sure_to_unban_user)
                .setPositiveButton(R.string.unban, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        CommonMethods.unbanUser(mUser.getUserID()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if(task.isSuccessful() && task.getResult()){
                                    Toast.makeText(getContext(), R.string.user_unbanned, Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getContext(), R.string.error_while_unbanning, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void giveAdmin(){
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.give_admin) + " " + mUser.getfName())
                .setMessage(R.string.sure_to_give_admin)
                .setPositiveButton(R.string.give, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        CommonMethods.makeAdmin(mUser.getUserID()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if(task.isSuccessful() && task.getResult()){
                                    Toast.makeText(getContext(), R.string.user_got_admin, Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getContext(), R.string.error_while_giving_admin, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void revokeAdmin(){
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.revoke_admin) + " " + mUser.getfName())
                .setMessage(R.string.sure_to_revoke_admin)
                .setPositiveButton(R.string.revoke, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        CommonMethods.revokeAdmin(mUser.getUserID()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if(task.isSuccessful() && task.getResult()){
                                    Toast.makeText(getContext(), R.string.admin_revoked, Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getContext(), R.string.error_while_revoking_admin, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }
}