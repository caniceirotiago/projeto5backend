package aor.paj.dto;

public class NotificationDto {
    private Long id;
    private String userId;
    private String type;
    private String content;
    private String sentAt;
    private boolean isRead;
    private String photoUrl;


    public NotificationDto(Long id, String userId, String type, String content, String sentAt, boolean isRead, String photoUrl) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sentAt='" + sentAt + '\'' +
                ", isRead=" + isRead +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}
