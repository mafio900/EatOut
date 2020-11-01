package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ProfilePreviewActivity;
import pl.highelo.eatoutwithstrangers.R;

public class EventsPaginationAdapter extends FirestorePagingAdapter<EventsModel, EventsPaginationAdapter.EventsPaginationViewHolder> {

    private static final String TAG = "EventsPaginationAdapter";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OnItemClickListener mListener;

    public EventsPaginationAdapter(@NonNull FirestorePagingOptions<EventsModel> options) {
        super(options);
    }

    public EventsPaginationAdapter(@NonNull FirestorePagingOptions<EventsModel> options, SwipeRefreshLayout swipeRefreshLayout, OnItemClickListener listener) {
        super(options);
        mSwipeRefreshLayout = swipeRefreshLayout;
        mListener = listener;

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    @Override
    protected void onBindViewHolder(@NonNull final EventsPaginationViewHolder holder, int position, @NonNull EventsModel currentItem) {
        FirebaseFirestore.getInstance().collection("users").document(currentItem.getUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final UsersModel user = documentSnapshot.toObject(UsersModel.class);
                user.setUserID(documentSnapshot.getId());
                holder.profileName.setText(user.getfName());
                Glide.with(holder.itemView)
                        .load(documentSnapshot.get("image_thumbnail"))
                        .placeholder(R.drawable.ic_person)
                        .into(holder.profileImage);
                if(!user.getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    holder.profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(holder.itemView.getContext(), ProfilePreviewActivity.class);
                            intent.putExtra("user", user);
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }
            }
        });

        FirebaseStorage.getInstance().getReference().child("events_images" + currentItem.getItemID()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Glide.with(holder.itemView)
                            .load(task.getResult())
                            .placeholder(R.drawable.placeholder_img)
                            .into(holder.eventImage);
                }
            }
        });

        holder.eventTheme.setText(holder.itemView.getContext().getString(R.string.theme) + ": " + currentItem.getTheme());
        holder.eventCapacity.setText(holder.itemView.getContext().getString(R.string.joined) + " " + currentItem.getMembers().size() + "/" + currentItem.getMaxPeople());
        holder.eventDescription.setText(currentItem.getDescription());
        holder.eventAddress.setText(currentItem.getPlaceAddress());
        String newDate = CommonMethods.parseDate(currentItem.getTimeStamp());
        holder.eventDate.setText(newDate);

        if(FirebaseAuth.getInstance().getUid().equals(currentItem.getUserID()) && currentItem.getRequests().size() > 0){
            holder.eventNotification.setVisibility(View.VISIBLE);
            holder.eventNotification.setText(String.valueOf(currentItem.getRequests().size()));
        }else{
            holder.eventNotification.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public EventsPaginationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_item, parent, false);
        return new EventsPaginationViewHolder(view, mListener);
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        if(mSwipeRefreshLayout != null){
            switch (state) {
                case LOADING_INITIAL:
                case LOADING_MORE:
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;

                case LOADED:
                case FINISHED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;

                case ERROR:
                    Toast.makeText(
                            mSwipeRefreshLayout.getContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                    ).show();

                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
        else{
            super.onLoadingStateChanged(state);
        }
    }


    public class EventsPaginationViewHolder extends RecyclerView.ViewHolder {

        private TextView profileName, eventTheme, eventCapacity, eventDescription, eventAddress, eventDate, eventNotification;
        private ImageView eventImage;
        private CircleImageView profileImage;

        public EventsPaginationViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.list_event_image);
            profileImage = itemView.findViewById(R.id.list_event_profile_image);
            profileName = itemView.findViewById(R.id.list_event_profile_name);
            eventTheme = itemView.findViewById(R.id.list_event_theme);
            eventDescription = itemView.findViewById(R.id.list_event_description);
            eventCapacity = itemView.findViewById(R.id.list_event_capacity);
            eventAddress = itemView.findViewById(R.id.list_event_address);
            eventDate = itemView.findViewById(R.id.list_event_date);
            eventNotification = itemView.findViewById(R.id.list_event_notification);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(getItem(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot item);
    }
}
