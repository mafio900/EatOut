package pl.highelo.eatoutwithstrangers.ModelsAndUtilities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import pl.highelo.eatoutwithstrangers.R;

public class ReportsAdapter extends FirestoreRecyclerAdapter<ReportsModel, ReportsAdapter.ReportsViewHolder> {

    private OnReportItemClick mOnReportItemClick;

    public ReportsAdapter(@NonNull FirestoreRecyclerOptions<ReportsModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportsViewHolder holder, int position, @NonNull ReportsModel model) {
        holder.mTheme.setText(holder.itemView.getContext().getString(R.string.theme)+": "+model.getTheme());
        String newDate = CommonMethods.parseDate(model.getTimeStamp());
        holder.mDate.setText(holder.itemView.getContext().getString(R.string.report_date)+": "+newDate);
    }

    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_report_item, parent, false);
        return new ReportsViewHolder(view, mOnReportItemClick);
    }

    public class ReportsViewHolder extends RecyclerView.ViewHolder{

        private TextView mTheme;
        private TextView mDate;

        public ReportsViewHolder(@NonNull View itemView, final OnReportItemClick onReportItemClick) {
            super(itemView);
            mTheme = itemView.findViewById(R.id.report_theme);
            mDate = itemView.findViewById(R.id.report_date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onReportItemClick != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            onReportItemClick.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnReportItemClick{
        void onItemClick(int position);
    }

    public void setOnReportItemClick(OnReportItemClick onReportItemClick) {
        mOnReportItemClick = onReportItemClick;
    }
}
