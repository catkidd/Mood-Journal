package com.moodjournal.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around JavaMailSender that sends password-reset emails.
 * Tokens are NEVER written to any log output.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an HTML password-reset email to the given address.
     *
     * @param to        recipient email address
     * @param resetLink full HTTPS reset URL (token is embedded only inside the link)
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🌿 MoodJournal — Reset Your Password");
            helper.setText(buildEmailBody(resetLink), true); // true = HTML

            mailSender.send(message);
            // Log recipient but NEVER the token/link
            log.info("Password reset email dispatched to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Could not send reset email. Please try again later.", e);
        }
    }

    private String buildEmailBody(String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Reset Your Password</title>
                </head>
                <body style="margin:0;padding:0;background:#f8f9fa;font-family:'Inter',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f9fa;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="520" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:16px;overflow:hidden;
                                      box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#2d6a4f,#52b788);
                                       padding:36px 40px;text-align:center;">
                              <div style="font-size:48px;margin-bottom:8px;">🌿</div>
                              <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;
                                         letter-spacing:-0.5px;">MoodJournal</h1>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td style="padding:40px 40px 32px;">
                              <h2 style="margin:0 0 16px;color:#1b1b1b;font-size:20px;font-weight:600;">
                                Reset Your Password
                              </h2>
                              <p style="margin:0 0 20px;color:#555;font-size:15px;line-height:1.6;">
                                We received a request to reset the password for your MoodJournal account.
                                Click the button below to choose a new password.
                              </p>
                              <div style="text-align:center;margin:32px 0;">
                                <a href="%s"
                                   style="display:inline-block;background:linear-gradient(135deg,#2d6a4f,#52b788);
                                          color:#ffffff;text-decoration:none;padding:14px 36px;
                                          border-radius:50px;font-size:16px;font-weight:600;
                                          letter-spacing:0.3px;">
                                  Reset Password
                                </a>
                              </div>
                              <p style="margin:0 0 8px;color:#888;font-size:13px;line-height:1.6;">
                                This link expires in <strong>30 minutes</strong>.
                                If you didn't request a password reset, you can safely ignore this email —
                                your password will not be changed.
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td style="background:#f8f9fa;padding:20px 40px;text-align:center;
                                       border-top:1px solid #e9ecef;">
                              <p style="margin:0;color:#aaa;font-size:12px;">
                                © 2025 MoodJournal · Track your emotions, grow your mind
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(resetLink);
    }
}
