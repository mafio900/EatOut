package pl.highelo.eatoutwithstrangers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class EventsAdapter extends FirestoreRecyclerAdapter<EventsModel, EventsAdapter.EventsViewHolder> {

    private static final String TAG = "EventsAdapter";

    private OnEventItemClick mOnEventItemClick;

    public EventsAdapter(@NonNull FirestoreRecyclerOptions<EventsModel> options, OnEventItemClick onEventItemClick) {
        super(options);
        mOnEventItemClick = onEventItemClick;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull EventsModel model) {
        holder.eventName.setText(model.getPlaceName());
        holder.eventAddress.setText(model.getPlaceAddress());
        holder.eventDate.setText(model.getDate() + " " + model.getTime());
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_item, parent, false);
        return new EventsViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private TextView eventName, eventAddress, eventDate;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventAddress = itemView.findViewById(R.id.eventAddress);
            eventDate = itemView.findViewById(R.id.eventDate);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            mOnEventItemClick.OnItemClick(getItem(getAdapterPosition()));
            return true;
        }
    }

    public interface OnEventItemClick{
        void OnItemClick(EventsModel model);
    }
}
