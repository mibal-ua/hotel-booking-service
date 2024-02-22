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

package ua.mibal.booking.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.mibal.booking.application.component.TemplateEmailFactory;
import ua.mibal.booking.application.dto.RegistrationForm;
import ua.mibal.booking.application.exception.EmailAlreadyExistsException;
import ua.mibal.booking.application.exception.IllegalPasswordException;
import ua.mibal.booking.application.exception.NotAuthorizedException;
import ua.mibal.booking.application.exception.UserNotFoundException;
import ua.mibal.booking.application.port.email.EmailSendingService;
import ua.mibal.booking.application.port.email.model.Email;
import ua.mibal.booking.domain.Token;
import ua.mibal.booking.domain.User;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailSendingService emailSendingService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEmailFactory emailFactory;

    /**
     * @param username User's username to authenticate
     * @param password User's password to authenticate
     * @return String representation of JWT token
     */
    public String login(String username, String password) {
        try {
            return loginBy(username, password);
        } catch (UserNotFoundException | IllegalPasswordException hidden) {
            // To hide from a client what is incorrect: username or password
            throw new NotAuthorizedException();
        }
    }

    @Transactional
    public void register(RegistrationForm registrationForm) {
        validateEmailDoesNotExist(registrationForm.email());
        User user = userService.save(registrationForm);
        Token token = tokenService.generateAndSaveTokenFor(user);
        Email email = emailFactory.getAccountActivationEmail(token);
        emailSendingService.send(email);
    }

    public void activateNewAccountBy(String tokenValue) {
        Token token = tokenService.getOneByValue(tokenValue);
        Long userId = token.getUser().getId();
        userService.activateUserById(userId);
    }

    @Transactional
    public void restore(String email) {
        try {
            restoreUserPassword(email);
        } catch (UserNotFoundException hidden) {
            // To hide from a client that user not found
        }
    }

    @Transactional
    public void setNewPassword(String tokenValue, String newPassword) {
        Token token = tokenService.getOneByValue(tokenValue);
        Long userId = token.getUser().getId();
        userService.setNewPasswordForUser(userId, newPassword);
    }

    private void restoreUserPassword(String email) {
        User user = userService.getOne(email);
        if (!user.isEnabled()) {
            return;
        }
        Token token = tokenService.generateAndSaveTokenFor(user);
        Email emailMessage = emailFactory.getPasswordChangingEmail(token);
        emailSendingService.send(emailMessage);
    }

    private String loginBy(String username, String password) {
        User user = userService.getOne(username);
        validatePasswordCorrect(password, user.getPassword());
        return jwtTokenService.generateJwtToken(user);
    }

    private void validatePasswordCorrect(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new IllegalPasswordException();
        }
    }

    private void validateEmailDoesNotExist(String email) {
        if (userService.isExistsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
