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

package ua.mibal.booking.service.photo.aws.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;
import ua.mibal.booking.model.exception.IllegalPhotoFormatException;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AwsPhoto_UnitTest {

    private AwsPhoto uploadPhoto;
    private AwsPhoto deletePhoto;

    private String name = "name";
    private String folder = "folder";
    private String fileName = "fileName.jpg";
    private byte[] fileBytes = "fileBytes".getBytes(UTF_8);

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setup() throws IOException {
        when(file.getOriginalFilename())
                .thenReturn(fileName);
        when(file.getBytes())
                .thenReturn(fileBytes);

        uploadPhoto = new AwsPhoto(name, folder, file);
        deletePhoto = new AwsPhoto(name, folder);
    }

    @ParameterizedTest
    @CsvSource({"png", "jpg", "jpeg"})
    void constructor(String allowedExtension) {
        String legalFileName = "fileName." + allowedExtension;

        when(file.getOriginalFilename())
                .thenReturn(legalFileName);

        assertDoesNotThrow(
                () -> new AwsPhoto("name", "folder", file));
    }

    @ParameterizedTest
    @CsvSource({"txt", "pdf", "svg", "''"})
    void constructor_should_throw_IllegalPhotoFormatException(String illegalExtension) {
        String illegalFileName = "fileName." + illegalExtension;

        when(file.getOriginalFilename())
                .thenReturn(illegalFileName);

        assertThrows(IllegalPhotoFormatException.class,
                () -> new AwsPhoto("name", "folder", file));
    }

    @Test
    void getContentType() {
        when(file.getOriginalFilename())
                .thenReturn(fileName);

        String contentType = "image/jpg";

        assertEquals(contentType, uploadPhoto.getContentType());
        assertThrows(RuntimeException.class,
                () -> deletePhoto.getContentType());
    }

    @Test
    void getPhoto() throws IOException {
        assertEquals(fileBytes, uploadPhoto.getPhoto());
        assertThrows(RuntimeException.class,
                () -> deletePhoto.getPhoto());
    }

    @Test
    void getKey() {
        String key = folder + "/" + name;

        assertEquals(key, uploadPhoto.getKey());
        assertEquals(key, deletePhoto.getKey());
    }
}
