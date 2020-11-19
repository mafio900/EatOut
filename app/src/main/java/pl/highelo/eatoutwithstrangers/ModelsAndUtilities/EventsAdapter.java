package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.highelo.eatoutwithstrangers.ProfilePreviewActivity;
import pl.highelo.eatoutwithstrangers.R;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> implements Filterable {

    private ArrayList<EventsModel> mEventsList;
    private ArrayList<EventsModel> mEventsListFull;
    private OnEventItemClick mOnEventItemClick;

    private Context mContext;

    public EventsAdapter(ArrayList<EventsModel> eventsList, Context context){
        mEventsList = eventsList;
        mEventsListFull = new ArrayList<>(eventsList);
        mContext = context;
    }

    public void setEventsListFull(ArrayList<EventsModel> arrayList){
        mEventsListFull = new ArrayList<>(arrayList);
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_item, parent, false);
        return new EventsViewHolder(view, mOnEventItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventsViewHolder holder, int position) {
        EventsModel currentItem = mEventsList.get(position);

        FirebaseFirestore.getInstance().collection("users").document(currentItem.getUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                holder.profileName.setText(documentSnapshot.get("fName").toString());
                Glide.with(holder.itemView)
                        .load(documentSnapshot.get("image_thumbnail"))
                        .placeholder(R.drawable.ic_person)
                        .into(holder.profileImage);
                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(holder.itemView.getContext(), ProfilePreviewActivity.class);
                        UsersModel user = documentSnapshot.toObject(UsersModel.class);
                        user.setUserID(documentSnapshot.getId());
                        intent.putExtra("user", user);
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        });

        FirebaseStorage.getInstance().getReference().child("events_images/" + currentItem.getItemID()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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

        holder.eventTheme.setText(mContext.getString(R.string.theme) + ": " + currentItem.getTheme());
        holder.eventCapacity.setText(mContext.getString(R.string.joined) + " " + currentItem.getMembers().size() + "/" + currentItem.getMaxPeople());
        holder.eventDescription.setText(currentItem.getDescription());
        holder.eventAddress.setText(currentItem.getPlaceAddress());
        String newDate = CommonMethods.parseDate(currentItem.getTimeStamp());
        holder.eventDate.setText(newDate);
    }

    @Override
    public int getItemCount() {
        return mEventsList.size();
    }

    @Override
    public Filter getFilter() {
        return eventsFilter;
    }

    private Filter eventsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<EventsModel> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(mEventsListFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(EventsModel item : mEventsListFull){
                    if(item.getTheme().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mEventsList.clear();
            mEventsList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class EventsViewHolder extends RecyclerView.ViewHolder {

        private TextView profileName, eventTheme, eventCapacity, eventDescription, eventAddress, eventDate;
        private ImageView eventImage;
        private CircleImageView profileImage;

        public EventsViewHolder(@NonNull View itemView, final OnEventItemClick listener) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.list_event_image);
            profileImage = itemView.findViewById(R.id.list_event_profile_image);
            profileName = itemView.findViewById(R.id.list_event_profile_name);
            eventTheme = itemView.findViewById(R.id.list_event_theme);
            eventDescription = itemView.findViewById(R.id.list_event_description);
            eventCapacity = itemView.findViewById(R.id.list_event_capacity);
            eventAddress = itemView.findViewById(R.id.list_event_address);
            eventDate = itemView.findViewById(R.id.list_event_date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.OnItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnEventItemClick{
        void OnItemClick(int position);
    }

    public void setOnEventItemClick(OnEventItemClick listener){
        mOnEventItemClick = listener;
    }
}
