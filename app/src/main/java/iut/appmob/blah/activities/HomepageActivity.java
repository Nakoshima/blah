package iut.appmob.blah.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import iut.appmob.blah.R;
import iut.appmob.blah.adapters.ContactAdapter;
import iut.appmob.blah.data.Message;
import iut.appmob.blah.data.User;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class HomepageActivity extends AppCompatActivity {
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String currentUserMail;
    private ValueEventListener sentMessageListener;
    private ValueEventListener receivedMessageListener;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.currentUserMail = mAuth.getCurrentUser().getEmail();
        setContentView(R.layout.activity_homepage);
        this.setRecyclerViewContacts();
        this.setSearchBar();

        Button btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(this.logoutListener());

        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this.searchButtonListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.getReference("messages").orderByChild("receiver/email")
                .equalTo(this.currentUserMail).removeEventListener(this.receivedMessageListener);
        database.getReference("messages").orderByChild("sender/email")
                .equalTo(this.currentUserMail).removeEventListener(this.sentMessageListener);
        database.getReference("users").removeEventListener(this.userListener);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // log out button listener
    private View.OnClickListener logoutListener() {
        return v -> {
            mAuth.signOut();
            finish();
            Intent intent = getIntent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
        };
    }

    private void setRecyclerViewContacts() {
        RecyclerView recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        ContactAdapter contactAdapter = new ContactAdapter(this, new ArrayList<>());
        recyclerViewContacts.setAdapter(contactAdapter);

        // gets the messages received by the current user
        this.receivedMessageListener = database.getReference("messages").orderByChild("receiver/email")
                .equalTo(this.currentUserMail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // resets the unread message counters
                        contactAdapter.resetUnreadCounts();
                        CountDownLatch latch = new CountDownLatch(1);
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            String contactMail = message.getSender().getEmail();

                            // updates the contact list depending on the messages received
                            if (!contactAdapter.contactMailAdded(contactMail)) {
                                contactAdapter.addContactMail(contactMail);
                                recyclerViewContacts.setAdapter(contactAdapter);
                            }

                            if (!message.isRead()) {
                                // audio en vibrator management
                                // it rings or vibrates when a new message is received
                                if (latch.getCount() == 1) {
                                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                    int mode = am.getRingerMode();
                                    if (mode == AudioManager.RINGER_MODE_VIBRATE) {
                                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator.vibrate(VibrationEffect.createOneShot(300, DEFAULT_AMPLITUDE), new AudioAttributes.Builder()
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                                                    .build());
                                        } else {
                                            vibrator.vibrate(300);
                                        }
                                    } else if (mode == AudioManager.RINGER_MODE_NORMAL) {
                                        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bells);
                                        mp.start();
                                    }
                                    latch.countDown();
                                }
                                // increments the unread counter associated with the sender
                                contactAdapter.incrementUnreadCount(contactMail);

                                recyclerViewContacts.setAdapter(contactAdapter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });


        // gets the messages sent by the current user
        this.sentMessageListener = database.getReference("messages").orderByChild("sender/email")
                .equalTo(this.currentUserMail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            String contactMail = message.getReceiver().getEmail();
                            if (!contactAdapter.contactMailAdded(contactMail)) {
                                contactAdapter.addContactMail(contactMail);
                                recyclerViewContacts.setAdapter(contactAdapter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });

    }

    private void setSearchBar() {
        // initialize the search bar with all the users from our databse
        List<String> allUsers = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allUsers);
        AutoCompleteTextView searchBarContact = findViewById(R.id.contactSearchBar);
        searchBarContact.setAdapter(adapter);

        this.userListener = database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (!userSnapshot.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        allUsers.add(user.getEmail());
                        Collections.sort(allUsers);

                        // sets the drop down list's size depending on the number of users shown
                        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                        if (allUsers.size() < 4) {
                            searchBarContact.setDropDownHeight(allUsers.size() * size);
                        } else {
                            searchBarContact.setDropDownHeight(4 * size);
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

    // listener for the search button
    private View.OnClickListener searchButtonListener() {
        return v -> {
            AutoCompleteTextView contactSearchBar = findViewById(R.id.contactSearchBar);
            database.getReference().child("users").orderByChild("email")
                    .equalTo(contactSearchBar.getText().toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // if the text entered in the search bar exists is an existing user
                            if (dataSnapshot.exists()) {
                                String contactMail = "";
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    contactMail = childSnapshot.getValue(User.class).getEmail();
                                }

                                // can't let the current user chat with themselves
                                if (!contactMail.equals(currentUserMail)) {
                                    Intent intent = getIntent();
                                    intent.setClass(v.getContext(), ChatActivity.class);
                                    intent.putExtra("contactUser", new User(contactMail));
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(HomepageActivity.this, "You cannot chat with yourself!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(HomepageActivity.this, "This mail does not exist", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });
        };
    }
}
