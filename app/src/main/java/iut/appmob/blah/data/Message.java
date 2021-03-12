package iut.appmob.blah.data;

import java.util.Date;

public class Message implements Comparable<Message> {
    private String content;
    private Date sentDate;
    private User sender;
    private User receiver;
    private boolean isRead;

    public Message() {
    }

    public Message(String content, User sender, User receiver, Date sentDate, boolean isRead) {
        this.content = content;
        this.sentDate = sentDate;
        this.sender = sender;
        this.receiver = receiver;
        this.isRead = isRead;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public User getSender() {
        return this.sender;
    }

    public User getReceiver() {
        return this.receiver;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Message message = (Message) o;

        return message.getContent().equals(this.content) && message.getSender().equals(this.sender) && message.getReceiver().equals(this.receiver) && message.getSentDate().equals(this.sentDate);
    }

    @Override
    public int compareTo(Message o) {
        return this.sentDate.compareTo(o.getSentDate());
    }
}
