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

package ua.mibal.booking.service.email.model;

import ua.mibal.booking.model.entity.Token;
import ua.mibal.booking.model.exception.marker.ApiException;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
public record EmailConfiguration(
        EmailType type,
        String recipients,
        Object... args
) {
    public static EmailConfiguration exceptionReportOf(EmailType type, String recipients, ApiException e) {
        return new EmailConfiguration(type, recipients, e);
    }

    public static EmailConfiguration activationEmailOf(EmailType type, Token token) {
        return new EmailConfiguration(type, token.getUser().getEmail(), token);
    }
}
