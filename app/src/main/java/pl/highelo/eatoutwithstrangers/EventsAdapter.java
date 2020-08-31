package pl.highelo.eatoutwithstrangers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder2> implements Filterable {

    private ArrayList<EventsModel> mEventsList;
    private ArrayList<EventsModel> mEventsListFull;
    private OnEventItemClick mOnEventItemClick;

    public EventsAdapter(ArrayList<EventsModel> eventsList){
        mEventsList = eventsList;
        mEventsListFull = new ArrayList<>(eventsList);
    }

    @NonNull
    @Override
    public EventsViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_item, parent, false);
        return new EventsAdapter.EventsViewHolder2(view, mOnEventItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder2 holder, int position) {
        EventsModel currentItem = mEventsList.get(position);

        holder.eventTheme.setText("Temat: " + currentItem.getTheme());
        holder.eventName.setText("Miejsce: " + currentItem.getPlaceName());
        holder.eventAddress.setText(currentItem.getPlaceAddress());
        holder.eventDate.setText("Data rozpoczęcia: " + currentItem.getDate() + " " + currentItem.getTime());
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

    public class EventsViewHolder2 extends RecyclerView.ViewHolder {

        private TextView eventTheme, eventName, eventAddress, eventDate;

        public EventsViewHolder2(@NonNull View itemView, final OnEventItemClick listener) {
            super(itemView);
            eventTheme = itemView.findViewById(R.id.eventTheme);
            eventName = itemView.findViewById(R.id.eventName);
            eventAddress = itemView.findViewById(R.id.eventAddress);
            eventDate = itemView.findViewById(R.id.eventDate);
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
