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

package ua.mibal.booking.application.port.jpa;

import org.junit.jupiter.api.Test;
import ua.mibal.booking.domain.User;
import ua.mibal.test.annotation.UnitTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@UnitTest
class FakeInMemoryUserRepository_UnitTest {

    private final FakeInMemoryUserRepository tested = new FakeInMemoryUserRepository();

    private User user;
    private Optional<User> found;
    private Optional<String> foundPassword;
    private boolean existsResult;
    private int resultCount;

    @Test
    void save() {
        givenUser(1L);

        whenSaveUser();

        thenUserIsSaved(1L);
    }

    @Test
    void findById() {
        givenSavedUser(1L);

        whenFindById(1L);

        thenUsersIsEquals();
    }

    @Test
    void findByIdShouldReturnOptionalEmpty() {
        givenEmptyRepository();

        whenFindById(1L);

        thenResultShouldBeEmpty();
    }

    @Test
    void delete() {
        givenSavedUser(1L);

        whenDelete();

        thenUserIsDeleted(1L);
    }

    @Test
    void findPasswordByEmail() {
        givenSavedUser("user@email", "password");

        whenFindPasswordByEmail("user@email");

        thenPasswordIsEquals("password");
    }

    @Test
    void existsByEmail_true() {
        givenSavedUser("user@email");

        whenExistsByEmail("user@email");

        thenResultIs(true);
    }

    @Test
    void existsByEmail_false() {
        givenEmptyRepository();

        whenExistsByEmail("user@email");

        thenResultIs(false);
    }

    @Test
    void deleteByEmail() {
        givenSavedUser("user@email");

        whenDeleteByEmail("user@email");

        thenUserIsDeleted("user@email");
    }

    @Test
    void updateUserPasswordByEmail() {
        givenSavedUser("user@email", "password");

        whenUpdateUserPasswordByEmail("newPassword", "user@email");

        thenUserPasswordIs("newPassword");
    }

    @Test
    void deleteNotEnabledWithNoTokens() {
        givenNotEnabledSavedUser("user1@email");
        givenNotEnabledSavedUser("user2@email");

        whenDeleteNotEnabledWithNoTokens();

        thenDeletedCountIs(2);
        thenUserIsDeleted("user1@email");
        thenUserIsDeleted("user2@email");
    }

    @Test
    void deleteAll() {
        givenSavedUser("user1@email");
        givenSavedUser("user2@email");

        whenDeleteAll();

        thenUserIsDeleted("user1@email");
        thenUserIsDeleted("user2@email");
    }

    private void givenUser(long id) {
        user = new User();
        user.setId(id);
    }

    private void givenSavedUser(long id) {
        givenUser(id);
        whenSaveUser();
    }

    private void givenSavedUser(String email, String password) {
        user = new User();
        user.setEmail(email);
        user.setPassword(password);
        whenSaveUser();
    }

    private void givenSavedUser(String email) {
        user = new User();
        user.setEmail(email);
        whenSaveUser();
    }

    private void givenNotEnabledSavedUser(String email) {
        user = new User();
        user.setEmail(email);
        user.setEnabled(false);
        whenSaveUser();
    }

    private void givenEmptyRepository() {
        tested.deleteAll();
    }

    private void whenSaveUser() {
        tested.save(user);
    }

    private void whenFindById(long id) {
        found = tested.findById(id);
    }

    private void whenDelete() {
        tested.delete(user);
    }

    private void whenDeleteAll() {
        tested.deleteAll();
    }

    private void whenFindPasswordByEmail(String email) {
        foundPassword = tested.findPasswordByEmail(email);
    }

    private void whenExistsByEmail(String email) {
        existsResult = tested.existsByEmail(email);
    }

    private void whenUpdateUserPasswordByEmail(String newPassword, String email) {
        tested.updateUserPasswordByEmail(newPassword, email);
    }

    private void whenDeleteNotEnabledWithNoTokens() {
        resultCount = tested.deleteNotEnabledWithNoTokens();
    }

    private void thenUserIsSaved(long id) {
        tested.findById(id).ifPresentOrElse(
                u -> assertEquals(user, u),
                () -> fail("User not found")
        );
    }

    private void thenUsersIsEquals() {
        found.ifPresentOrElse(
                u -> assertEquals(user, u),
                () -> fail("User not found")
        );
    }

    private void thenUserIsDeleted(long id) {
        assertThrows(RuntimeException.class,
                () -> tested.findById(id).orElseThrow());
    }

    private void thenUserIsDeleted(String email) {
        assertFalse(tested.existsByEmail(email));
    }

    private void thenPasswordIsEquals(String password) {
        foundPassword.ifPresentOrElse(
                p -> assertEquals(password, p),
                () -> fail("Password not found")
        );
    }

    private void thenResultShouldBeEmpty() {
        assertEquals(Optional.empty(), found);
    }

    private void thenResultIs(boolean b) {
        assertEquals(b, existsResult);
    }

    private void whenDeleteByEmail(String email) {
        tested.deleteByEmail(email);
    }

    private void thenUserPasswordIs(String newPassword) {
        assertEquals(newPassword, user.getPassword());
    }

    private void thenDeletedCountIs(int expected) {
        assertEquals(expected, resultCount);
    }
}