package iut.appmob.blah.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import iut.appmob.blah.R;
import iut.appmob.blah.data.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final List<Message> messages;
    private Context context;

    public MessageAdapter(Context context, List<Message> messages) {
        this.setContext(context);
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        Collections.sort(messages);
    }

    public boolean messageAdded(Message message) {
        return this.messages.contains(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageAdapter.ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_message_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textViewMessage.setText(message.getContent());
        String date = new SimpleDateFormat("HH:mm · dd/MM/yy ").format(message.getSentDate());
        holder.textViewTimestamp.setText(date);

        TextView txtContact = ((Activity) this.getContext()).findViewById(R.id.txtContact);

        // adapts the chat bubbles depending on the receiver
        if (message.getReceiver().toString().equals(txtContact.getText().toString())) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                    getContext().getResources().getDisplayMetrics());
            int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                    getContext().getResources().getDisplayMetrics());
            int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getContext().getResources().getDisplayMetrics());
            int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                    getContext().getResources().getDisplayMetrics());
            params.gravity = Gravity.RIGHT;
            params.setMargins(left, top, right, bottom);
            holder.cardView.setLayoutParams(params);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.message));
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getContext().getResources().getDisplayMetrics());
            int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                    getContext().getResources().getDisplayMetrics());
            int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                    getContext().getResources().getDisplayMetrics());
            int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                    getContext().getResources().getDisplayMetrics());
            params.gravity = Gravity.LEFT;
            params.setMargins(left, top, right, bottom);
            holder.cardView.setLayoutParams(params);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMessage;
        public TextView textViewTimestamp;
        public CardView cardView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.textViewMessage = view.findViewById(R.id.textMessage);
            this.textViewTimestamp = view.findViewById(R.id.textTimestamp);
            this.cardView = view.findViewById(R.id.cardMessage);
        }
    }
}
