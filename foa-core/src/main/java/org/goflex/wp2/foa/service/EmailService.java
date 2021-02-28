package org.goflex.wp2.foa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;

@Component
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender emailSender;


    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception ex) {
           LOGGER.error(ex.getLocalizedMessage());
        }
    }

    public void sendMessageWithAttachment(String to,
                                          String subject,
                                          String text,
                                          List<String> pathToAttachment) {
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            for (int i = 0; i < pathToAttachment.size(); i++) {
                FileSystemResource file = new FileSystemResource(new File(pathToAttachment.get(i)));
                helper.addAttachment(("Error Log " + i), file);
            }
            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
