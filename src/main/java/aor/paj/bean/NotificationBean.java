package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.NotificationDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.dto.User;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class NotificationBean {
    @EJB
    private UserBean userBean;
    @EJB
    private NotificationDao notificationDao;
    public List<NotificationDto> getNotifications(String token) {
        UserEntity user = userBean.getUserByToken(token);
        if (user == null) {
            return null;
        }
        List<NotificationEntity> notifications = notificationDao.getNotifications(user);
        List<NotificationDto> notificationDtos = convertEntetiesToDtos(notifications);

        return notificationDtos;
    }
    public NotificationEntity createNotification(String username, String type, String content) {
        UserEntity user = userBean.getUserByUsername(username);
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setContent(content);
        notification.setType(type);
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());
        notificationDao.persist(notification);
        System.out.println("Notification created: " + notification.getId() + " " + notification.getContent());
        return notification;
    }
    public List<NotificationDto> convertEntetiesToDtos(List<NotificationEntity> notifications) {
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for (NotificationEntity notification : notifications) {
            NotificationDto notificationDto = new NotificationDto(notification.getId(),
                    notification.getUser().getUsername(), notification.getType(), notification.getContent(),
                    notification.getSentAt().toString(), notification.isRead());
            notificationDtos.add(notificationDto);
        }
        return notificationDtos;
    }
}
