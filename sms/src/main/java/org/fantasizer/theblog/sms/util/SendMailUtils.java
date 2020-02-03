package org.fantasizer.theblog.sms.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class SendMailUtils {

    @Value(value = "${spring.mail.username}")
    public String SENDER;
    @Autowired
    private JavaMailSenderImpl mailSender;

    /**
     * 发送邮件
     *
     * @param receiver
     * @param text
     */
    public void sendEmail(String receiver, String text) throws MessagingException {
        //创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setSubject("蘑菇博客网站验证邮件");

        helper.setText(text, true);
        helper.setTo(receiver);
        helper.setFrom(SENDER);
        mailSender.send(mimeMessage);

        System.out.println("邮件发送成功");
    }
} 