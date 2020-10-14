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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

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
    protected void onBindViewHolder(@NonNull EventsPaginationViewHolder holder, int position, @NonNull EventsModel model) {
        holder.eventTheme.setText(model.getTheme());
        holder.eventAddress.setText(model.getPlaceAddress());
        holder.eventDate.setText(CommonMethods.parseDate(model.getTimeStamp()));

        if(FirebaseAuth.getInstance().getUid().equals(model.getUserID()) && model.getRequests().size() > 0){
            holder.eventRequests.setVisibility(View.VISIBLE);
            holder.eventRequests.setText(String.valueOf(model.getRequests().size()));
            holder.itemView.findViewById(R.id.list_event_background).setBackground(holder.itemView.getContext().getDrawable(R.drawable.circle_background));
        }
    }

    @NonNull
    @Override
    public EventsPaginationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event_manage_item, parent, false);
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

        public TextView eventTheme, eventRequests, eventAddress, eventDate;

        public EventsPaginationViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            eventTheme = itemView.findViewById(R.id.list_event_theme);
            eventRequests = itemView.findViewById(R.id.list_event_requests);
            eventAddress = itemView.findViewById(R.id.list_event_address);
            eventDate = itemView.findViewById(R.id.list_event_date);
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
