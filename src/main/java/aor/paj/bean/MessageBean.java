package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class MessageBean {
    @EJB
    private MessageDao messageDao;
    @EJB
    private UserBean userBean;
    @EJB
    private NotificationBean notificationBean;

    public MessageEntity sendMessage(MessageDto messageDto) {
        MessageEntity message = convertDtoToEntity(messageDto);
        messageDao.persist(message);
        return message;
    }

    public List<MessageDto> getMessagesBetweenUsers(String sender, String receiver) {
        List<MessageEntity> messages = messageDao.findMessagesBetweenUsers(userBean.getUserByUsername(sender),
                userBean.getUserByUsername(receiver));
        return messages.stream().map(this::convertEntityToDto).collect(Collectors.toList());
    }

    private MessageEntity convertDtoToEntity(MessageDto dto) {
        MessageEntity entity = new MessageEntity();
        entity.setSender(userBean.getUserByUsername(dto.getSenderUsername()));
        entity.setReceiver(userBean.getUserByUsername(dto.getReceiverUsername()));
        entity.setContent(dto.getContent());
        entity.setSentAt(dto.getSentAt());
        entity.setRead(dto.isRead());
        return entity;
    }

    public MessageDto convertEntityToDto(MessageEntity entity) {
        return new MessageDto(
                entity.getId(),
                entity.getSender().getUsername(),
                entity.getReceiver().getUsername(),
                entity.getContent(),
                entity.getSentAt(),
                entity.isRead()
        );
    }
    public boolean markMessagesAsRead(List<Long> messagesIds) {
        return messageDao.markMessagesAsRead(messagesIds);
    }
    public List<MessageDto> getMessagesByIds(List<Long> messagesIds) {
        List<MessageEntity> messages = messageDao.findMessagesByIds(messagesIds);
        return messages.stream().map(this::convertEntityToDto).collect(Collectors.toList());
    }
}