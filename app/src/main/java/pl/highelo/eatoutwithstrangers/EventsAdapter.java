package pl.highelo.eatoutwithstrangers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


public class EventsAdapter extends FirestoreRecyclerAdapter<EventsModel, EventsAdapter.EventsViewHolder> {

    private OnEventItemClick mOnEventItemClick;

    public EventsAdapter(@NonNull FirestoreRecyclerOptions<EventsModel> options, OnEventItemClick onEventItemClick) {
        super(options);
        mOnEventItemClick = onEventItemClick;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull EventsModel model) {
        holder.eventTheme.setText("Temat: " + model.getTheme());
        holder.eventName.setText("Miejsce: " + model.getPlaceName());
        holder.eventAddress.setText(model.getPlaceAddress());
        holder.eventDate.setText("Data rozpoczęcia: " + model.getDate() + " " + model.getTime());
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

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView eventTheme, eventName, eventAddress, eventDate;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTheme = itemView.findViewById(R.id.eventTheme);
            eventName = itemView.findViewById(R.id.eventName);
            eventAddress = itemView.findViewById(R.id.eventAddress);
            eventDate = itemView.findViewById(R.id.eventDate);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnEventItemClick.OnItemClick(getItem(getAdapterPosition()));
        }
    }

    public interface OnEventItemClick{
        void OnItemClick(EventsModel model);
    }
}
