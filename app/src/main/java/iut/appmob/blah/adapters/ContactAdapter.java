package iut.appmob.blah.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import iut.appmob.blah.R;
import iut.appmob.blah.activities.ChatActivity;
import iut.appmob.blah.data.User;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private Context context;
    private final List<String> contactsMail;
    private List<Integer> unreadMessagesCounts;

    public ContactAdapter(Context context, List<String> contactsMail) {
        this.setContext(context);
        this.contactsMail = contactsMail;
        this.unreadMessagesCounts = new ArrayList<>();
    }

    public void addContactMail(String contactMail) {
        this.contactsMail.add(contactMail);
        this.unreadMessagesCounts.add(0);
    }

    public boolean contactMailAdded(String contactMail) {
        return this.contactsMail.contains(contactMail);
    }

    public void incrementUnreadCount(String contactMail) {
        int index = this.contactsMail.indexOf(contactMail);
        unreadMessagesCounts.set(index, unreadMessagesCounts.get(index) + 1);
    }

    public void resetUnreadCounts() {
        this.unreadMessagesCounts = new ArrayList<>(Collections.nCopies(this.contactsMail.size(), 0));
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_contact_row, parent, false), this.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        String contact = contactsMail.get(position);
        holder.textView.setText(contact);

        // color alternation for each contact row
        if (position % 2 == 0) {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background));
        } else {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        }

        // show the number of unread messages
        if (unreadMessagesCounts.get(position) != 0) {
            holder.txtCounter.setText(unreadMessagesCounts.get(position) + " unread message");
            if (unreadMessagesCounts.get(position) != 1){
                holder.txtCounter.append("s");
            }
            holder.txtCounter.setVisibility(View.VISIBLE);
        } else {
            holder.txtCounter.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contactsMail.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CardView cardView;
        public LinearLayout linearCard;
        public TextView txtCounter;
        public Context context;

        public ViewHolder(@NonNull View view, Context context) {
            super(view);
            this.textView = view.findViewById(R.id.textContact);
            this.cardView = view.findViewById(R.id.cardContact);
            this.linearCard = view.findViewById(R.id.linearCard);
            this.txtCounter = view.findViewById(R.id.txtCounter);
            this.context = context;

            this.linearCard.setOnClickListener(v -> {
                String contactMail = textView.getText().toString();

                // opens ChatActivity and sets the contact chosen by the user to chat with
                Intent intent = ((Activity) context).getIntent();
                intent.setClass(v.getContext(), ChatActivity.class);
                intent.putExtra("contactUser", new User(contactMail));
                context.startActivity(intent);
                ((Activity) context).finish();
            });
        }
    }
}
