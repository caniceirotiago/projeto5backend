package aor.paj.dto;

import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private String sentAt;
    private boolean isRead;

    // Constructors
    public MessageDto() {}

    public MessageDto(Long id, String senderUsername, String receiverUsername, String content, LocalDateTime sentAt, boolean isRead) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content = content;
        this.sentAt = sentAt.toString();
        this.isRead = isRead;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return LocalDateTime.parse(sentAt);
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt.toString();
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "id=" + id +
                ", senderUsername='" + senderUsername + '\'' +
                ", receiverUsername='" + receiverUsername + '\'' +
                ", content='" + content + '\'' +
                ", sentAt=" + sentAt +
                ", isRead=" + isRead +
                '}';
    }
}
