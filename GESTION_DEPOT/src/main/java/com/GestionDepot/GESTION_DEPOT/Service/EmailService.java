package com.GestionDepot.GESTION_DEPOT.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Injectez le TemplateEngine

    @Value("${spring.mail.username}") // Injecte l'adresse email de l'expéditeur depuis la configuration
    private String senderEmail;

    public void sendWelcomeEmail(String to, String subject, String name, String email) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // Créez un contexte Thymeleaf et ajoutez les variables
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", email);


            // Traitez le template Thymeleaf
            String content = templateEngine.process("welcome-email", context); // "welcome-email" est le nom du fichier HTML (sans l'extension)
            helper.setText(content, true); // true indique que le contenu est du HTML

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de la création du message : " + e.getMessage(), e);
        }

        mailSender.send(message);
    }
}