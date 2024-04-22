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
        TypedQuery<MessageEntity> query = em.createNamedQuery("MessageEntity.findMessagesBetweenUsers", MessageEntity.class)
                .setParameter("sender", sender)
                .setParameter("receiver", receiver);
        return query.getResultList();
    }
    public List<MessageEntity> getMessages(UserEntity user) {
        TypedQuery<MessageEntity> query = em.createNamedQuery("MessageEntity.getMessages", MessageEntity.class)
                .setParameter("user", user);
        return query.getResultList();
    }

    public boolean markMessagesAsRead(List<Long> messagesIds) {
        System.out.println("Marking messages as read: " + messagesIds);
        em.createNamedQuery("MessageEntity.markMessagesAsRead")
                .setParameter("ids", messagesIds)
                .executeUpdate();
        return true;
    }
    public List<MessageEntity> findMessagesByIds(List<Long> messagesIds) {
        TypedQuery<MessageEntity> query = em.createQuery("SELECT m FROM MessageEntity m WHERE m.id IN :ids", MessageEntity.class)
                .setParameter("ids", messagesIds);
        return query.getResultList();
    }
    public boolean deleteMessageById(Long messageId) {
        em.createNamedQuery("MessageEntity.deleteMessageById")
                .setParameter("id", messageId)
                .executeUpdate();
        return true;
    }
}
