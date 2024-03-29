package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        EventsModel currentItem = mEventsList.get(position);

        holder.eventTheme.setText(mContext.getString(R.string.theme) + ": " + currentItem.getTheme());
        holder.eventName.setText(currentItem.getPlaceName());
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

        private TextView eventTheme, eventName, eventAddress, eventDate;

        public EventsViewHolder(@NonNull View itemView, final OnEventItemClick listener) {
            super(itemView);
            eventTheme = itemView.findViewById(R.id.list_event_theme);
            eventName = itemView.findViewById(R.id.list_event_name);
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
