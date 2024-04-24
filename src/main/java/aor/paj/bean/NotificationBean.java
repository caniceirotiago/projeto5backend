package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import aor.paj.service.websocket.NotificationsWebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Stateless
public class NotificationBean {
    private static final Logger LOGGER = LogManager.getLogger(NotificationBean.class);
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
    public HashMap<String, List<NotificationDto>> getAgrupatedNotifications(String token) throws UserNotFoundException {
        UserEntity user = userBean.getUserByToken(token);
        if(user == null){
            LOGGER.warn("User not found");
            throw new UserNotFoundException("User not found");
        }
        List<NotificationEntity> notifications = notificationDao.getNotifications(user);
        HashMap<String, List<NotificationDto>> agrupatedNotifications = new HashMap<>();

        for (NotificationEntity notification : notifications) {
            if(notification.isRead()){
                continue;
            }
            String username = notification.getContent();
            NotificationDto notificationDto = convertEntetieToDto(notification);

            if (!agrupatedNotifications.containsKey(username)) {
                agrupatedNotifications.put(username, new ArrayList<>());
            }
            agrupatedNotifications.get(username).add(notificationDto);
        }
        return agrupatedNotifications;
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
        NotificationsWebSocket.sendNotification(convertEntetieToDto(notification));
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
    public NotificationDto convertEntetieToDto(NotificationEntity notification) {
        return new NotificationDto(notification.getId(), notification.getUser().getUsername(), notification.getType(),
                notification.getContent(), notification.getSentAt().toString(), notification.isRead());
    }
    public void markAsRead(String token, String id) throws UserNotFoundException {
        System.out.println("Marking NOTIFICATIONS as read: " + id);
        UserEntity user = userBean.getUserByToken(token);
        if(user == null){
            LOGGER.warn("User not found");
            throw new UserNotFoundException("User not found");
        }
        List<NotificationEntity> notifications = notificationDao.getNotifications(user);
        for (NotificationEntity notification : notifications) {
            if (notification.getContent().equals(id)) {
                notification.setRead(true);
                notificationDao.merge(notification);
            }
        }
    }
}
