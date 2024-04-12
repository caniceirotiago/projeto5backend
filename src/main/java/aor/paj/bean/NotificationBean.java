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
        List<NotificationDto> notificationDtos = new ArrayList<>();

        return notificationDtos;
    }
}
