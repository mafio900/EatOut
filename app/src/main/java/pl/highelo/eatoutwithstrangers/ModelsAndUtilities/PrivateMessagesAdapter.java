package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.R;

public class PrivateMessagesAdapter extends FirestoreRecyclerAdapter<PrivateMessagesModel, PrivateMessagesAdapter.PrivateMessagesViewHolder> {

    private FirebaseFirestore mFirestore;

    private OnItemClickListener mOnItemClickListener;

    private ListenerRegistration ref;

    public PrivateMessagesAdapter(@NonNull FirestoreRecyclerOptions<PrivateMessagesModel> options) {
        super(options);
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull final PrivateMessagesViewHolder holder, int position, @NonNull PrivateMessagesModel model) {
        mFirestore.collection("users").document(model.getUsetID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    UsersModel user = task.getResult().toObject(UsersModel.class);
                    holder.userName.setText(user.getfName());
                    Glide.with(holder.itemView.getContext())
                            .asBitmap()
                            .placeholder(R.drawable.ic_person)
                            .load(user.getImage_thumbnail())
                            .into(holder.image);
                } else {

                }
            }
        });

        ref = mFirestore.collection("privateMessages").document(model.getPrivateMessageID()).collection("messages").orderBy("time", Query.Direction.DESCENDING).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    MessagesModel messModel = value.getDocuments().get(0).toObject(MessagesModel.class);
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String annot = userID.equals(messModel.getUserID()) ? holder.itemView.getContext().getString(R.string.you) : "";
                    holder.lastMessage.setText(annot + messModel.getMessage());
                }else{
                    ref.remove();
                }

            }
        });
    }

    @NonNull
    @Override
    public PrivateMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_private_messages, parent, false);
        return new PrivateMessagesViewHolder(view, mOnItemClickListener);
    }

    public class PrivateMessagesViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, lastMessage;
        private CircleImageView image;

        public PrivateMessagesViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            userName = itemView.findViewById(R.id.list_private_messages_name);
            lastMessage = itemView.findViewById(R.id.list_private_messages_last_message);
            image = itemView.findViewById(R.id.list_private_messages_image);

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                });
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
