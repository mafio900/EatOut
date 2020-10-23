package pl.highelo.eatoutwithstrangers.ProfileActivities.PrivateMessages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.CommonMethods;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.MessagesAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.MessagesModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.PrivateMessagesModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.UsersModel;
import pl.highelo.eatoutwithstrangers.R;

public class PrivateChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private PrivateMessagesModel mPrivateMessagesModel;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText mMessageEditText;
    private ImageButton mSendMessageButton;

    private MessagesAdapter adapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private int PAGE_SIZE = 10;
    private boolean stopRef = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonMethods.validateUser(this);
        setContentView(R.layout.activity_private_chat);
        mPrivateMessagesModel = getIntent().getParcelableExtra("model");
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.private_chat);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupUI(findViewById(R.id.parent));


        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mRecyclerView = findViewById(R.id.private_chat_recyclerview);
        mSwipeRefreshLayout = findViewById(R.id.private_chat_swipelayout);
        mSendMessageButton = findViewById(R.id.private_chat_send_button);
        mMessageEditText = findViewById(R.id.private_chat_message);

        mFirestore.collection("users").document(mPrivateMessagesModel.getUsetID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    UsersModel user = task.getResult().toObject(UsersModel.class);
                    mToolbar.setTitle(getString(R.string.private_chat_with) + user.getfName());
                }
            }
        });

        //Query
        final Query mainQuery = mFirestore.collection("privateMessages").document(mPrivateMessagesModel.getPrivateMessageID()).collection("messages").orderBy("time", Query.Direction.DESCENDING);
        Query query = mainQuery.limit(PAGE_SIZE);
        //RecyclerOptions
        FirestoreRecyclerOptions<MessagesModel> options = new FirestoreRecyclerOptions.Builder<MessagesModel>()
                .setQuery(query, MessagesModel.class)
                .build();
        adapter = new MessagesAdapter(options, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                }, 500);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!stopRef){
                    PAGE_SIZE += 60;
                    Query refQuery = mainQuery.limit(PAGE_SIZE);
                    FirestoreRecyclerOptions<MessagesModel> refOptions = new FirestoreRecyclerOptions.Builder<MessagesModel>()
                            .setQuery(refQuery, MessagesModel.class)
                            .build();
                    adapter.updateOptions(refOptions);
                    adapter.notifyDataSetChanged();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mMessageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && i == KeyEvent.KEYCODE_ENTER){
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void sendMessage() {
        String messageString = mMessageEditText.getText().toString();
        if(!messageString.equals("")) {
            WriteBatch batch = mFirestore.batch();
            Map<String, Object> message = new HashMap<>();
            message.put("userID", mAuth.getUid());
            message.put("time", FieldValue.serverTimestamp());
            message.put("message", mMessageEditText.getText().toString());
            DocumentReference messRef = mFirestore.collection("privateMessages").document(mPrivateMessagesModel.getPrivateMessageID()).collection("messages").document();
            batch.set(messRef, message);
            DocumentReference privRef = mFirestore.collection("privateMessages").document(mPrivateMessagesModel.getPrivateMessageID());
            batch.update(privRef, "timestamp", FieldValue.serverTimestamp());

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mMessageEditText.setText("");
                    }else{
                        Toast.makeText(PrivateChatActivity.this, R.string.sorry_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if(view instanceof EditText){
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(0);
                        }
                    }, 500);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}