package day11.service;

import io.vertx.core.json.JsonObject;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class EmailService {
    private final Mailer mailer;
    private final String fromEmail;

    public EmailService(JsonObject config) {
        JsonObject emailConfig = config.getJsonObject("email");
        this.fromEmail = emailConfig.getString("username"); // Use Gmail address as from email

        this.mailer = MailerBuilder
                .withSMTPServer(
                        emailConfig.getString("host", "smtp.gmail.com"),
                        emailConfig.getInteger("port", 587),
                        emailConfig.getString("username"),
                        emailConfig.getString("password"))
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000) // 10 seconds timeout
                .withDebugLogging(true) // Enable debug logging
                .buildMailer();
    }

    public void sendWelcomeEmail(String toEmail, String name, String password) {
        var email = EmailBuilder.startingBlank()
                .from(fromEmail)
                .to(toEmail)
                .withSubject("Welcome to Event Ticket System")
                .withPlainText("Hello " + name + ",\n\n" +
                        "Welcome to the Event Ticket System! Here are your login credentials:\n" +
                        "Email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please change your password after your first login.\n\n" +
                        "Best regards,\nEvent Ticket System Team")
                .buildEmail();

        mailer.sendMail(email, true); // Add true for async sending
    }

    public void sendBookingConfirmation(String toEmail, String name, String eventName, String tokenCode) {
        var email = EmailBuilder.startingBlank()
                .from(fromEmail)
                .to(toEmail)
                .withSubject("Booking Confirmation - " + eventName)
                .withPlainText("Hello " + name + ",\n\n" +
                        "Your booking for " + eventName + " has been confirmed!\n\n" +
                        "Your unique token code is: " + tokenCode + "\n\n" +
                        "Please keep this code safe and present it at the event.\n\n" +
                        "Best regards,\nEvent Ticket System Team")
                .buildEmail();

        mailer.sendMail(email, true); // Add true for async sending
    }
}
