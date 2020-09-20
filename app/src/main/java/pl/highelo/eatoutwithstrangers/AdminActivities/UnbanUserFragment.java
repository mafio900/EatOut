package pl.highelo.eatoutwithstrangers.AdminActivities;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.R;

public class UnbanUserFragment extends Fragment {

    private FirebaseFirestore mFirestore;

    private UsersModel mUser;

    public UnbanUserFragment() {
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
        return inflater.inflate(R.layout.fragment_unban_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();

        Button searchButton = view.findViewById(R.id.unban_user_search_button);
        final Button unbanButton = view.findViewById(R.id.unban_user_unban_button);
        final TextInputLayout emailInput = view.findViewById(R.id.unban_user_email_input);
        final CircleImageView imageView = view.findViewById(R.id.unban_user_imageview);
        final TextView name = view.findViewById(R.id.unban_user_name);
        final TextView age = view.findViewById(R.id.unban_user_age);
        final TextView description = view.findViewById(R.id.unban_user_description);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String email = emailInput.getEditText().getText().toString();
                if(TextUtils.isEmpty(email)){
                    emailInput.setError(getString(R.string.email_is_required));
                }else{
                    emailInput.setError(null);
                    mFirestore.collection("bannedUsers").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()){
                                mFirestore.collection("users").document(task.getResult().getDocumentChanges().get(0).getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            imageView.setVisibility(View.VISIBLE);
                                            name.setVisibility(View.VISIBLE);
                                            age.setVisibility(View.VISIBLE);
                                            description.setVisibility(View.VISIBLE);
                                            unbanButton.setVisibility(View.VISIBLE);
                                            mUser = task.getResult().toObject(UsersModel.class);
                                            mUser.setUserID(task.getResult().getId());
                                            Glide.with(imageView.getContext())
                                                    .load(mUser.getImage_thumbnail())
                                                    .placeholder(R.drawable.ic_person)
                                                    .into(imageView);
                                            name.setText(mUser.getfName());
                                            age.setText(String.valueOf(CommonMethods.getAge(mUser.getBirthDate())));
                                            description.setText(mUser.getDescription());
                                        }

                                        else{
                                            emailInput.setError(getString(R.string.user_doesnt_exist));
                                        }
                                    }
                                });

                            }else{
                                emailInput.setError(getString(R.string.email_not_banned));
                            }
                        }
                    });
                }
            }
        });

        unbanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.unban_user)
                        .setMessage(R.string.sure_to_unban_user)
                        .setPositiveButton(R.string.unban, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CommonMethods.unbanUser(mUser.getUserID()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Boolean> task) {
                                        if(task.isSuccessful() && task.getResult()){
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), R.string.user_unbanned, Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(getContext(), R.string.error_while_unbanning, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }
}