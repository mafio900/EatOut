package pl.highelo.eatoutwithstrangers.PrivateMessages;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.BottomNavigationInterface;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.PrivateMessagesAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.PrivateMessagesModel;
import pl.highelo.eatoutwithstrangers.R;

public class PrivateMessagesActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private PrivateMessagesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.validateUser(this);
        setContentView(R.layout.activity_private_messages);
        new BottomNavigationInterface(this, findViewById(R.id.parent_layout));
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.private_messages);
        setSupportActionBar(mToolbar);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mRecyclerView = findViewById(R.id.private_messages_recycler_view);

        Query query = mFirestore.collection("privateMessages").whereArrayContains("users", mAuth.getCurrentUser().getUid()).orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        FirestoreRecyclerOptions<PrivateMessagesModel> options = new FirestoreRecyclerOptions.Builder<PrivateMessagesModel>()
                .setQuery(query, new SnapshotParser<PrivateMessagesModel>() {
                    @NonNull
                    @Override
                    public PrivateMessagesModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        PrivateMessagesModel model = snapshot.toObject(PrivateMessagesModel.class);
                        model.setPrivateMessageID(snapshot.getId());
                        return model;
                    }
                })
                .build();
        mAdapter = new PrivateMessagesAdapter(options);
        mAdapter.setOnItemClickListener(new PrivateMessagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(PrivateMessagesActivity.this, PrivateChatActivity.class);
                intent.putExtra("model", mAdapter.getItem(position));
                startActivity(intent);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}