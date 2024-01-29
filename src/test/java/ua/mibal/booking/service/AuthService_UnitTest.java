/*
 * Copyright (c) 2023. Mykhailo Balakhon mailto:9mohapx9@gmail.com
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

package ua.mibal.booking.service;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ua.mibal.booking.model.dto.auth.AuthResponseDto;
import ua.mibal.booking.model.dto.auth.ForgetPasswordDto;
import ua.mibal.booking.model.dto.auth.RegistrationDto;
import ua.mibal.booking.model.entity.ActivationCode;
import ua.mibal.booking.model.entity.User;
import ua.mibal.booking.model.exception.EmailAlreadyExistsException;
import ua.mibal.booking.model.exception.entity.UserNotFoundException;
import ua.mibal.booking.model.mapper.UserMapper;
import ua.mibal.booking.service.email.EmailSendingService;
import ua.mibal.booking.service.security.JwtTokenService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthService.class)
@TestPropertySource(locations = "classpath:application.yaml")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthService_UnitTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private JwtTokenService jwtTokenService;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private ActivationCodeService activationCodeService;
    @MockBean
    private EmailSendingService emailSendingService;

    @Mock
    private User user;
    @Mock
    private ActivationCode activationCode;
    @Mock
    private AuthResponseDto expectedAuthDto;
    @Mock
    private RegistrationDto registrationDto;
    @Mock
    private ForgetPasswordDto forgetPasswordDto;

    @Test
    void login() {
        String token = "test_token";
        when(jwtTokenService.generateJwtToken(user)).thenReturn(token);
        when(userMapper.toAuthResponse(user, token)).thenReturn(expectedAuthDto);

        var actual = authService.login(user);

        assertEquals(expectedAuthDto, actual);
    }

    @Test
    void register() {
        String notExistingEmail = "not_existing_email";
        String password = "test_pass";
        when(registrationDto.email()).thenReturn(notExistingEmail);
        when(registrationDto.password()).thenReturn(password);

        when(userService.isExistsByEmail(notExistingEmail))
                .thenReturn(false);
        when(userService.save(registrationDto))
                .thenReturn(user);
        when(activationCodeService.generateAndSaveCodeFor(user))
                .thenReturn(activationCode);

        authService.register(registrationDto);

        verify(emailSendingService, times(1))
                .sendAccountActivationEmail(activationCode);
    }

    @Test
    void register_should_throw_EmailAlreadyExistsException() {
        String existingEmail = "existing_email";
        when(registrationDto.email()).thenReturn(existingEmail);

        when(userService.isExistsByEmail(existingEmail))
                .thenReturn(true);

        verifyNoMoreInteractions(userService);

        EmailAlreadyExistsException e = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(registrationDto)
        );
        assertEquals(
                new EmailAlreadyExistsException(existingEmail).getMessage(),
                e.getMessage()
        );
        verifyNoInteractions(activationCodeService, emailSendingService);
    }

    @Test
    void activateNewAccountBy() {
        String code = "CODE";
        long id = 1L;
        when(activationCodeService.getOneByCode(code))
                .thenReturn(activationCode);
        when(activationCode.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(id);

        authService.activateNewAccountBy(code);

        verify(userService, times(1))
                .activateUserById(id);
    }

    @Test
    void restore() {
        String email = "email";
        when(userService.getOne(email)).thenReturn(user);
        when(activationCodeService.generateAndSaveCodeFor(user))
                .thenReturn(activationCode);

        assertDoesNotThrow(
                () -> authService.restore(email)
        );
        verify(emailSendingService, times(1))
                .sendPasswordChangingEmail(activationCode);
    }

    @Test
    void restore_should_not_throw_exception_if_user_not_found() {
        String email = "not_existing_email";
        when(userService.getOne(email)).thenThrow(UserNotFoundException.class);

        verifyNoMoreInteractions(userService);

        assertDoesNotThrow(
                () -> authService.restore(email)
        );

        verifyNoInteractions(activationCodeService, emailSendingService);
    }

    @Test
    void setNewPassword() {
        String password = "pass";
        when(forgetPasswordDto.password()).thenReturn(password);

        String code = "CODE";
        long id = 1L;
        when(activationCodeService.getOneByCode(code))
                .thenReturn(activationCode);
        when(activationCode.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(id);

        authService.setNewPassword(code, forgetPasswordDto);

        verify(userService, times(1))
                .setNewPasswordForUser(id, password);
    }
}
