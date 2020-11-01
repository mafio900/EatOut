package pl.highelo.eatoutwithstrangers.EventPages;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.EventsModel;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.MessagesAdapter;
import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.MessagesModel;
import pl.highelo.eatoutwithstrangers.R;

public class ChatFragment extends Fragment {

    private static final String ARG_EVENTS_MODEL = "model";

    private EventsModel mEventsModel;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText mMessageEditText;
    private ImageButton mSendMessageButton;

    private MessagesAdapter adapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private int PAGE_SIZE = 10;
    private boolean stopRef = false;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(EventsModel eventsModel) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENTS_MODEL, eventsModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEventsModel = getArguments().getParcelable(ARG_EVENTS_MODEL);
            mFirestore = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.chat_recyclerview);
        mMessageEditText = view.findViewById(R.id.chat_message);
        mSendMessageButton = view.findViewById(R.id.chat_send_button);
        setupUI(view);

        //Query
        Query query = mFirestore.collection("events").document(mEventsModel.getItemID()).collection("chat").orderBy("time", Query.Direction.DESCENDING).limit(PAGE_SIZE);
        //RecyclerOptions
        FirestoreRecyclerOptions<MessagesModel> options = new FirestoreRecyclerOptions.Builder<MessagesModel>()
                .setQuery(query, MessagesModel.class)
                .build();
        adapter = new MessagesAdapter(options, getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
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

        mSwipeRefreshLayout = view.findViewById(R.id.chat_swipelayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!stopRef){
                    PAGE_SIZE += 60;
                    Query refQuery = mFirestore.collection("events").document(mEventsModel.getItemID()).collection("chat").orderBy("time", Query.Direction.DESCENDING).limit(PAGE_SIZE);
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

    private void sendMessage() {
        String messageString = mMessageEditText.getText().toString();
        if(!messageString.equals("")) {
            Map<String, Object> message = new HashMap<>();
            message.put("userID", mAuth.getUid());
            message.put("time", FieldValue.serverTimestamp());
            message.put("message", mMessageEditText.getText().toString());
            mFirestore.collection("events").document(mEventsModel.getItemID()).collection("chat").add(message).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        mMessageEditText.setText("");
                    } else {
                        Toast.makeText(getContext(), R.string.sorry_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }, 500);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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