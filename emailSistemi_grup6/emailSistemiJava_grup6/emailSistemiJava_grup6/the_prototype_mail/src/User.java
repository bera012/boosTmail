import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<Email> inbox;
    private List<Email> outbox;
    private String profileImage; // Base64 encoded image

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
        this.profileImage = null;
    }

    // Getter metodları
    public String getUsername() {
        return username;
    }

    public List<Email> getInbox() {
        return inbox;
    }

    public List<Email> getOutbox() {
        return outbox;
    }

    public String getProfileImage() {
        return profileImage;
    }

    // Setter metodları
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // E-posta ekleme metodu
    public void addEmail(Email email) {
        inbox.add(email);
    }

    public void addEmailtoOutbox(Email email) {
        outbox.add(email);
    }

    // Kullanıcı doğrulama metodu
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}