package day10.service;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String USERNAME = "annamalajeshwanth@gmail.com";
    private static final String PASSWORD = "cdvt zerx krgc qtdr";

    public void sendRegistrationEmail(String toEmail, String password) {
        Email email = EmailBuilder.startingBlank()
                .from("Course Registration System", USERNAME)
                .to(toEmail)
                .withSubject("Registration Confirmation")
                .withPlainText("Your registration password is: " + password)
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer(SMTP_HOST, SMTP_PORT, USERNAME, PASSWORD)
                .buildMailer();

        mailer.sendMail(email);
    }
}