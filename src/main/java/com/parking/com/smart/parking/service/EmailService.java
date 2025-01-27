package com.parking.com.smart.parking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;
    @Value("${spring.mail.username}")
    private String senderEmail;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);


    public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine) {
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    public void sendHtmlEmail(String to, String subject, String username, String gracePeriod) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Prepare the context for Thymeleaf
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("gracePeriod", gracePeriod);

            // Process the template with the context
            String htmlContent = springTemplateEngine.process("email-template", context);

            // Set email details
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Error sending email to " + to + ": " + e.getMessage(), e);
        }
    }

    public void sendPaymentEmail(
            String to,
            String subject,
            String username,
            String gracePeriod,
            String parkingSpot,
            String date,
            String startTime,
            String endTime,
            String vehicleNumber,
            String paymentRef,
            double amount,
            String spotLocation,
            double parkingDuration
    ) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Prepare the context for Thymeleaf
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("gracePeriod", gracePeriod);
            context.setVariable("parkingSpot", parkingSpot);
            context.setVariable("date", date);
            context.setVariable("startTime", startTime);
            context.setVariable("endTime", endTime);
            context.setVariable("vehicleNumber", vehicleNumber);
            context.setVariable("paymentRef", vehicleNumber);
            context.setVariable("amount", amount);
            context.setVariable("spotLocation", spotLocation);
            context.setVariable("parkingDuration", parkingDuration);

            // Process the template with the context
            String htmlContent = springTemplateEngine.process("receipt-template", context);

            // Set email details
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Error sending email to " + to + ": " + e.getMessage(), e);
        }
    }


}