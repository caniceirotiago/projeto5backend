package aor.paj.service;

import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@Stateless
public class EmailService {

    private final String USERNAME = "antnestservice@gmail.com";
    private final String PASSWORD = "xnowuqeoyylbxzjw";

    public void sendConfirmationEmail(String toEmail, String confirmationToken) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("antnestservice@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmação de Conta");
            message.setText("Para confirmar sua conta, por favor clique no link abaixo:\n" + "http://localhost:3000/confirm?token=" + confirmationToken);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // Configurações do servidor SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Inicia uma sessão de e-mail
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Cria uma nova mensagem de e-mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("antnestservice@gmail.com")); // O e-mail do remetente
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // O e-mail do destinatário
            message.setSubject("Redefinição de Senha"); // O assunto do e-mail
            // O texto do e-mail, incluindo o link com o token para redefinição de senha
            String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
            message.setText("Você solicitou a redefinição de sua senha. Por favor, clique no link abaixo para criar uma nova senha:\n" + resetUrl);

            // Envia o e-mail
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
