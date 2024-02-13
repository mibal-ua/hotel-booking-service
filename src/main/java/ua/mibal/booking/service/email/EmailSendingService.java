/*
 * Copyright (c) 2024. Mykhailo Balakhon mailto:9mohapx9@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.mibal.booking.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.mibal.booking.config.properties.EmailProps;
import ua.mibal.booking.model.entity.Token;
import ua.mibal.booking.model.exception.marker.InternalServerException;
import ua.mibal.booking.model.exception.service.EmailSentFailedException;
import ua.mibal.booking.service.email.component.EmailBuilder;
import ua.mibal.booking.service.email.model.Email;
import ua.mibal.booking.service.email.model.EmailConfiguration;

import static ua.mibal.booking.service.email.model.EmailType.ACCOUNT_ACTIVATION;
import static ua.mibal.booking.service.email.model.EmailType.EXCEPTION_REPORT;
import static ua.mibal.booking.service.email.model.EmailType.PASSWORD_CHANGING;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RequiredArgsConstructor
@Service
public class EmailSendingService {
    private final EmailBuilder emailBuilder;
    private final EmailProps emailProps;

    public void sendAccountActivationEmail(Token token) {
        send(EmailConfiguration.activationEmailOf(ACCOUNT_ACTIVATION, token));
    }

    public void sendPasswordChangingEmail(Token token) {
        send(EmailConfiguration.activationEmailOf(PASSWORD_CHANGING, token));
    }

    public void sendErrorEmailToDevelopers(InternalServerException e) {
        send(EmailConfiguration.exceptionReportOf(EXCEPTION_REPORT, emailProps.developers(), e));
    }

    private void send(EmailConfiguration configuration) {
        Email email = emailBuilder.buildEmailBy(configuration);
        sendAsync(email);
    }

    // TODO use ExecutorService
    private void sendAsync(Email email) {
        new Thread(() -> {
            try {
                send(email);
            } catch (MessagingException e) {
                throw new EmailSentFailedException(e);
            }
        }, "Email-sending-Thread").start();
    }

    private synchronized void send(Email email) throws MessagingException {
        Transport.send(email, emailProps.username(), emailProps.password());
    }
}
