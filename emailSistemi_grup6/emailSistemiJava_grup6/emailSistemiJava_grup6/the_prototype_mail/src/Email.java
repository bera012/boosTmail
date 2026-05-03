import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Email {
    private String sender;
    private String recipient;
    private String subject;
    private String content;
    private String timestamp;
    private boolean isRead;
    private boolean isStarred;

    public Email(String sender, String recipient, String subject, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        this.timestamp = LocalDateTime.now().format(formatter);
        this.isRead = false;
        this.isStarred = false;
    }

    // Getter metodları
    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isStarred() {
        return isStarred;
    }

    // Setter metodları
    public void markAsRead() {
        this.isRead = true;
    }

    public void toggleStar() {
        this.isStarred = !this.isStarred;
    }

    public void setStarred(boolean starred) {
        this.isStarred = starred;
    }

    // Önizleme metodu
    public String getPreview() {
        return String.format("%s - %s: %s",
                getTimestamp(),
                sender,
                subject.length() > 30 ? subject.substring(0, 30) + "..." : subject
        );
    }
}