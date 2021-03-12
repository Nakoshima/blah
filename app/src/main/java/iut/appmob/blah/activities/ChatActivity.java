package iut.appmob.blah.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import iut.appmob.blah.R;
import iut.appmob.blah.adapters.MessageAdapter;
import iut.appmob.blah.data.Message;
import iut.appmob.blah.data.User;

public class ChatActivity extends AppCompatActivity {
    private static boolean isVisible;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ValueEventListener sentMessageListener;
    private ValueEventListener receivedMessageListener;
    private User contact;
    private String key;

    private String XOREncrypt(String message) {
        // encrypt a message
        // or decrypt an encrypted message
        String encryptedMsg = "";
        for (int i = 0, j = 0; i < message.length(); i++) {
            encryptedMsg += (char) ((int) message.charAt(i) ^ (int) this.key.charAt(j));
            j++;
            if (j >= this.key.length())
                j = 0;
        }
        return encryptedMsg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve the contact the current user is talking to from the intent
        this.contact = (User) getIntent().getSerializableExtra("contactUser");
        String currentUID = mAuth.getCurrentUser().getUid();

        // create a key for the message encryption
        database.getReference("users").orderByChild("email").equalTo(contact.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String contactUID = childSnapshot.getKey();
                    if (currentUID.compareTo(contactUID) > 0)
                        key = currentUID + contactUID;
                    else
                        key = contactUID + currentUID;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        setContentView(R.layout.activity_chat);

        TextView txtContact = findViewById(R.id.txtContact);
        txtContact.setText(this.contact.toString());

        this.setRecyclerViewMessages();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this.backListener());

        ImageButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(sendListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        ChatActivity.isVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        ChatActivity.isVisible = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        User currentUser = new User(mAuth.getCurrentUser().getEmail());
        database.getReference("messages").orderByChild("sender/email")
                .equalTo(currentUser.getEmail()).removeEventListener(this.sentMessageListener);
        database.getReference("messages").orderByChild("sender/email")
                .equalTo(contact.getEmail()).removeEventListener(this.receivedMessageListener);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = getIntent();
        intent.setClass(this, HomepageActivity.class);
        startActivity(intent);
    }

    private void setRecyclerViewMessages() {
        User currentUser = new User(mAuth.getCurrentUser().getEmail());
        RecyclerView recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        MessageAdapter messageAdapter = new MessageAdapter(this, new ArrayList<>());
        recyclerViewMessages.setAdapter(messageAdapter);

        // gets the messages sent by the current user and received by the contact user
        this.sentMessageListener = database.getReference("messages").orderByChild("sender/email")
                .equalTo(currentUser.getEmail())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);

                            // decrypt the message retrieved from the database
                            message.setContent(XOREncrypt(message.getContent()));
                            if (message.getReceiver().getEmail().equals(contact.getEmail()) && !messageAdapter.messageAdded(message)) {
                                messageAdapter.addMessage(message);
                                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                                recyclerViewMessages.setAdapter(messageAdapter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });

        // gets the messages sent by the contact user and received by the current user
        this.receivedMessageListener = database.getReference("messages").orderByChild("sender/email")
                .equalTo(contact.getEmail())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);

                            // decrypt the message retrieved from the database
                            message.setContent(XOREncrypt(message.getContent()));
                            if (message.getReceiver().getEmail().equals(currentUser.getEmail()) && !messageAdapter.messageAdded(message)) {
                                messageAdapter.addMessage(message);
                                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                                recyclerViewMessages.setAdapter(messageAdapter);

                                // mark unread message received as read
                                if (ChatActivity.isVisible && !message.isRead()) {
                                    message.setRead(true);
                                    HashMap<String, Object> values = new HashMap<>();
                                    values.put("read", true);
                                    messageSnapshot.getRef().updateChildren(values);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });
    }

    // back button listener
    private View.OnClickListener backListener() {
        return v -> {
            finish();
            Intent intent = getIntent();
            intent.setClass(this, HomepageActivity.class);
            startActivity(intent);
        };
    }

    // send button listener
    private View.OnClickListener sendListener() {
        return new View.OnClickListener() {
            final User currentUser = new User(mAuth.getCurrentUser().getEmail());

            @Override
            public void onClick(View v) {
                TextView textMessage = findViewById(R.id.inputMessage);
                if (textMessage.getText().length() != 0) {
                    // encrypt the message to be sent before writing it in the database
                    String encryptedMsg = XOREncrypt(textMessage.getText().toString());
                    Message message = new Message(encryptedMsg, currentUser, contact, new Date(), false);
                    database.getReference("messages")
                            .push()
                            .setValue(message)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    textMessage.setText("");
                                } else {
                                    Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        };
    }
}