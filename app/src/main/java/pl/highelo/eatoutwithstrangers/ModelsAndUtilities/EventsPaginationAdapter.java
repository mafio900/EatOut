package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

import pl.highelo.eatoutwithstrangers.R;

public class EventsPaginationAdapter extends FirestorePagingAdapter<EventsModel, EventsPaginationAdapter.EventsPaginationViewHolder> {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public EventsPaginationAdapter(@NonNull FirestorePagingOptions<EventsModel> options) {
        super(options);
    }

    public EventsPaginationAdapter(@NonNull FirestorePagingOptions<EventsModel> options, SwipeRefreshLayout swipeRefreshLayout) {
        super(options);
        mSwipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventsPaginationViewHolder holder, int position, @NonNull EventsModel model) {
        holder.eventTheme.setText(model.getTheme());
        holder.eventAddress.setText(model.getPlaceAddress());
        holder.eventDate.setText(CommonMethods.parseDate(model.getTimeStamp()));
    }

    @NonNull
    @Override
    public EventsPaginationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_manage_item, parent, false);
        return new EventsPaginationViewHolder(view);
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


    public static class EventsPaginationViewHolder extends RecyclerView.ViewHolder {

        public TextView eventTheme, eventAddress, eventDate;

        public EventsPaginationViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTheme = itemView.findViewById(R.id.list_event_theme);
            eventAddress = itemView.findViewById(R.id.list_event_address);
            eventDate = itemView.findViewById(R.id.list_event_date);
        }
    }

}
