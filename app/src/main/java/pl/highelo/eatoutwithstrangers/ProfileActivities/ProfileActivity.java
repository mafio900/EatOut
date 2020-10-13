package pl.highelo.eatoutwithstrangers.ProfileActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.BottomNavigationInterface;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.R;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private Toolbar mToolbar;

    private TextView mProfileName, mProfileAge, mProfileCity, mProfileDescription;
    private ImageView mProfileImageView;
    private Uri mSelectedImage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private String mUserID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.validateUser(this);
        setContentView(R.layout.activity_profile);
        new BottomNavigationInterface(this, findViewById(R.id.parent_layout));
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.your_profile);
        setSupportActionBar(mToolbar);

        mProfileImageView = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileAge = (TextView) findViewById(R.id.profile_age);
        mProfileCity = (TextView) findViewById(R.id.profile_city);
        mProfileDescription = (TextView) findViewById(R.id.profile_description);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mUserID = mAuth.getCurrentUser().getUid();

        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageClick();
            }
        });
        DocumentReference documentReference = mFirestore.collection("users").document(mUserID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mProfileName.setText(documentSnapshot.get("fName").toString() + ",");
                mProfileAge.setText(String.valueOf(CommonMethods.getAge(documentSnapshot.get("birthDate").toString())));
                mProfileCity.setText(getString(R.string.live_in) + ": " + documentSnapshot.get("city").toString());
                mProfileDescription.setText(documentSnapshot.get("description").toString());
                Glide.with(ProfileActivity.this)
                        .load(documentSnapshot.get("image"))
                        .placeholder(R.drawable.ic_person)
                        .into(mProfileImageView);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_edit) {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mSelectedImage = result.getUri();
                mProfileImageView.setImageURI(mSelectedImage);
                handleUpdate();
            }
        }
    }

    public void imageClick() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setMinCropResultSize(500, 500)
                .start(this);
    }

    public void handleUpdate() {
        if (mSelectedImage != null) {
            final File thumbFile = new File(mSelectedImage.getPath());
            final WriteBatch batch = mFirestore.batch();

            mStorageReference.child("profile_images/" + mUserID + "/profile_image").putFile(mSelectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        try {
                            Bitmap thumb_bitmap = new Compressor(ProfileActivity.this)
                                    .setMaxHeight(200)
                                    .setMaxWidth(200)
                                    .setQuality(75)
                                    .compressToBitmap(thumbFile);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            final byte[] thumb_byte = baos.toByteArray();
                            mStorageReference.child("profile_images/" + mUserID + "/profile_image").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final DocumentReference ref = mFirestore.collection("users").document(mUserID);
                                    batch.update(ref, "image", uri.toString());
                                    mStorageReference.child("profile_images/" + mUserID + "/profile_image_thumbnail").putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                mStorageReference.child("profile_images/" + mUserID + "/profile_image_thumbnail").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        batch.update(ref, "image_thumbnail", uri.toString());
                                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    mProfileImageView.setImageURI(mSelectedImage);
                                                                    Toast.makeText(ProfileActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
                                                                }else{
                                                                    Toast.makeText(ProfileActivity.this, R.string.send_image_error, Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(ProfileActivity.this, R.string.send_image_error, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(ProfileActivity.this, R.string.send_image_error, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, R.string.send_image_error, Toast.LENGTH_LONG).show();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, R.string.send_image_error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        CommonMethods.showDialog(this, getString(R.string.sure_to_leave_app));
    }
}
