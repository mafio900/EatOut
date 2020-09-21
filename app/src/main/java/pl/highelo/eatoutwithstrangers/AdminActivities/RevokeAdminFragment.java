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

public class RevokeAdminFragment extends Fragment {

    private FirebaseFirestore mFirestore;

    private UsersModel mUser;

    public RevokeAdminFragment() {
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
        return inflater.inflate(R.layout.fragment_revoke_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();

        Button searchButton = view.findViewById(R.id.revoke_admin_search_button);
        final Button revokeButton = view.findViewById(R.id.revoke_admin_revoke_button);
        final TextInputLayout emailInput = view.findViewById(R.id.revoke_admin_email);
        final CircleImageView imageView = view.findViewById(R.id.revoke_admin_imageview);
        final TextView name = view.findViewById(R.id.revoke_admin_name);
        final TextView age = view.findViewById(R.id.revoke_admin_age);
        final TextView description = view.findViewById(R.id.revoke_admin_description);

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
                                revokeButton.setVisibility(View.VISIBLE);
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

        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.revoke_admin)
                        .setMessage(R.string.sure_to_revoke_admin)
                        .setPositiveButton(R.string.revoke, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CommonMethods.revokeAdmin(mUser.getUserID()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Boolean> task) {
                                        if(task.isSuccessful() && task.getResult()){
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), R.string.admin_revoked, Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(getContext(), R.string.error_while_revoking_admin, Toast.LENGTH_LONG).show();
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