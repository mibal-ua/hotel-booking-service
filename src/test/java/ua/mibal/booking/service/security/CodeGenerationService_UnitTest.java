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

package ua.mibal.booking.service.security;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ua.mibal.booking.config.properties.ActivationCodeProps;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CodeGenerationService.class)
@TestPropertySource(locations = "classpath:application.yaml")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CodeGenerationService_UnitTest {

    @Autowired
    private CodeGenerationService service;

    @MockBean
    private ActivationCodeProps activationCodeProps;

    @ParameterizedTest
    @CsvSource({"0", "1", "10", "20", "100", "1_000", "100_000"})
    void generateCode_length_props(int codeLength) {
        when(activationCodeProps.length()).thenReturn(codeLength);

        var actual = service.generateCode();

        assertEquals(codeLength, actual.length());
    }

    @Test
    void generateCode_should_not_contain_url_specific_chars() {
        int codeLength = 100_000;

        when(activationCodeProps.length()).thenReturn(codeLength);

        var actual = service.generateCode();

        String encodedCode = URLEncoder.encode(actual, StandardCharsets.UTF_8);
        assertEquals(actual, encodedCode);
    }
}