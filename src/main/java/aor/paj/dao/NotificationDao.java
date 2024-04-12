package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public List<NotificationEntity> getNotifications(UserEntity user) {
        TypedQuery<NotificationEntity> query = em.createNamedQuery("NotificationEntity.getNotifications", NotificationEntity.class)
                .setParameter("user", user);
        return query.getResultList();
    }
}
