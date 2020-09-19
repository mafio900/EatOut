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

    public ReportsAdapter(@NonNull FirestoreRecyclerOptions<ReportsModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportsViewHolder holder, int position, @NonNull ReportsModel model) {
        holder.mTheme.setText(holder.itemView.getContext().getString(R.string.theme)+": "+model.getTheme());
        GregorianCalendar d = new GregorianCalendar(TimeZone.getTimeZone("Europe/Warsaw"));
        d.setTime(model.getTimeStamp().toDate());
        String date = d.get(Calendar.DAY_OF_MONTH)+"."
                +(d.get(Calendar.MONTH)+1)+"."
                +d.get(Calendar.YEAR)+" "
                +d.get(Calendar.HOUR_OF_DAY)+":"
                +d.get(Calendar.MINUTE);
        SimpleDateFormat oldFormat = new SimpleDateFormat("d.M.yyyy H:m", Locale.US);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
        String newDate = CommonMethods.parseDate(date, oldFormat, newFormat);
        holder.mDate.setText(holder.itemView.getContext().getString(R.string.report_date)+": "+newDate);
    }

    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_report_item, parent, false);
        return new ReportsViewHolder(view);
    }

    public class ReportsViewHolder extends RecyclerView.ViewHolder{

        private TextView mTheme;
        private TextView mDate;

        public ReportsViewHolder(@NonNull View itemView) {
            super(itemView);
            mTheme = itemView.findViewById(R.id.report_theme);
            mDate = itemView.findViewById(R.id.report_date);
        }
    }
}
