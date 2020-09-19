package pl.highelo.eatoutwithstrangers.AdminActivities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.ReportsAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.ReportsModel;
import pl.highelo.eatoutwithstrangers.R;

public class ReportsFragment extends Fragment {

    private FirebaseFirestore mFirestore;

    private ReportsAdapter mReportsAdapter;
    private RecyclerView mRecyclerView;

    public ReportsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mRecyclerView = view.findViewById(R.id.reports_recyclerview);

        Query query = mFirestore.collection("reports").orderBy("timeStamp", Query.Direction.ASCENDING).limit(10);
        FirestoreRecyclerOptions<ReportsModel> options = new FirestoreRecyclerOptions.Builder<ReportsModel>()
                .setQuery(query, ReportsModel.class)
                .build();

        mReportsAdapter = new ReportsAdapter(options);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mReportsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mReportsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mReportsAdapter.stopListening();
    }
}