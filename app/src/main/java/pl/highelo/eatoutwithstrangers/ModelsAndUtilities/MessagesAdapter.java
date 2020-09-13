package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.R;

public class MessagesAdapter extends FirestoreRecyclerAdapter<MessagesModel, MessagesAdapter.MessagesViewHolder> {

    private FirebaseFirestore mFirestore;
    private HashMap<String, UsersModel> mUsersMap = new HashMap<>();

    public MessagesAdapter(@NonNull FirestoreRecyclerOptions<MessagesModel> options) {
        super(options);
        mFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message_item, parent, false);
        return new MessagesViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position, @NonNull final MessagesModel model) {
        if(mUsersMap.containsKey(model.getUserID())){
            holder.mName.setText(mUsersMap.get(model.getUserID()).getfName());
            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(mUsersMap.get(model.getUserID()).getImage_thumbnail())
                    .into(holder.mImage);
        }else{
            mFirestore.collection("users").document(model.getUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    final UsersModel user = documentSnapshot.toObject(UsersModel.class);
                    user.setUserID(documentSnapshot.getId());
                    mUsersMap.put(user.getUserID(), user);
                    holder.mName.setText(documentSnapshot.get("fName").toString());

                    if(user.getImage_thumbnail() != null){
                        Glide.with(holder.itemView.getContext())
                                .asBitmap()
                                .load(user.getImage_thumbnail())
                                .into(holder.mImage);
                    }
                }
            });
        }
        holder.mMessage.setText(model.getMessage());
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{

        private TextView mName;
        private TextView mMessage;
        private CircleImageView mImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.message_name);
            mMessage = itemView.findViewById(R.id.message_textview);
            mImage = itemView.findViewById(R.id.message_image);
        }
    }
}
