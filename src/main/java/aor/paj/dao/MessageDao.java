package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    public MessageDao() {
        super(MessageEntity.class);
    }

    public List<MessageEntity> findMessagesBetweenUsers(UserEntity sender, UserEntity receiver) {
        TypedQuery<MessageEntity> query = em.createQuery(
                "SELECT m FROM MessageEntity m WHERE (m.sender = :sender AND m.receiver = :receiver) OR (m.sender = :receiver AND m.receiver = :sender) ORDER BY m.sentAt ASC", MessageEntity.class);
        query.setParameter("sender", sender);
        query.setParameter("receiver", receiver);
        return query.getResultList();
    }

    public void markMessagesAsRead(UserEntity receiver, LocalDateTime before) {
        em.createQuery("UPDATE MessageEntity m SET m.isRead = true WHERE m.receiver = :receiver AND m.sentAt < :before AND m.isRead = false")
                .setParameter("receiver", receiver)
                .setParameter("before", before)
                .executeUpdate();
    }
}
