package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.R;

public class UsersAdapter extends FirestoreRecyclerAdapter<UsersModel, UsersAdapter.UsersViewHolder> {

    private OnUsersItemClick mOnUsersItemClick;
    private OnUsersAcceptClick mOnUsersAcceptClick;
    private OnUsersCancelClick mOnUsersCancelClick;

    public UsersAdapter(@NonNull FirestoreRecyclerOptions<UsersModel> options) {
        super(options);
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_users_item, parent, false);
        return new UsersViewHolder(view, mOnUsersItemClick, mOnUsersAcceptClick, mOnUsersCancelClick);
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull UsersModel model) {
        Glide.with(holder.mImage.getContext())
                .load(model.getImage_thumbnail())
                .placeholder(R.drawable.ic_person)
                .into(holder.mImage);
        holder.mName.setText(model.getfName() + ",");
        holder.mAge.setText(String.valueOf(CommonMethods.getAge(model.getBirthDate())));
        holder.mDescription.setText(model.getDescription());
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        private TextView mName, mAge, mDescription;
        private CircleImageView mImage;
        private ImageButton mAccept, mCancel;

        public UsersViewHolder(@NonNull View itemView, final OnUsersItemClick onUsersItemClick, final OnUsersAcceptClick onUsersAcceptClick, final OnUsersCancelClick onUsersCancelClick) {
            super(itemView);
            mName = itemView.findViewById(R.id.list_users_name);
            mAge = itemView.findViewById(R.id.list_users_age);
            mDescription = itemView.findViewById(R.id.list_users_description);
            mImage = itemView.findViewById(R.id.list_users_image);
            mAccept = itemView.findViewById(R.id.list_users_accept);
            mCancel = itemView.findViewById(R.id.list_users_cancel);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onUsersItemClick != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onUsersItemClick.OnItemClick(position);
                        }
                    }
                }
            });
            if(onUsersAcceptClick != null) {
                mAccept.setVisibility(View.VISIBLE);
                mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onUsersAcceptClick.OnAcceptClick(position);
                        }
                    }
                });
            }
            if(onUsersCancelClick != null){
                mCancel.setVisibility(View.VISIBLE);
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onUsersCancelClick.OnCancelClick(position);
                        }
                    }
                });
            }
        }
    }

    public interface OnUsersItemClick {
        void OnItemClick(int position);
    }

    public interface OnUsersAcceptClick {
        void OnAcceptClick(int position);
    }

    public interface OnUsersCancelClick {
        void OnCancelClick(int position);
    }

    public void setOnUsersItemClick(OnUsersItemClick listener) {
        mOnUsersItemClick = listener;
    }

    public void setOnUsersAcceptClick(OnUsersAcceptClick listener) {
        mOnUsersAcceptClick = listener;
    }

    public void setOnUsersCancelClick(OnUsersCancelClick listener) {
        mOnUsersCancelClick = listener;
    }
}
