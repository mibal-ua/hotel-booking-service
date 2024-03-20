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

package ua.mibal.booking.adapter.out.reservation.system.ical.com.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import ua.mibal.booking.adapter.out.reservation.system.ical.WebContentReader;
import ua.mibal.booking.application.model.ReservationForm;
import ua.mibal.booking.domain.ApartmentInstance;
import ua.mibal.booking.domain.Event;
import ua.mibal.test.annotation.UnitTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@TestMethodOrder(OrderAnnotation.class)
@UnitTest
class BookingComReservationService_UnitTest {
    private final static String calendarUrl =
            "file:/Users/admin/IdeaProjects/hotel-booking-service/" +
            "src/test/resources/test.ics";

    private BookingComReservationService service;

    @Mock
    private WebContentReader webContentReader;

    @Mock
    private ApartmentInstance apartmentInstance;
    @Mock
    private Event event;

    @BeforeEach
    void setup() {
        service = new BookingComReservationService(webContentReader);
    }

    @Test
    @Order(1)
    void getEventsFor() {
        when(apartmentInstance.getBookingICalUrl())
                .thenReturn(Optional.of(calendarUrl));
        when(webContentReader.read(calendarUrl))
                .thenReturn("FILE CONTENT");

        List<Event> actual = service.getEventsFor(apartmentInstance);

        assertEquals(emptyList(), actual);
    }

    @Test
    @Order(2)
    void getEventsFor_should_NOT_throw_if_apartmentInstance_has_not_ical_url() {
        when(apartmentInstance.getBookingICalUrl()).thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> service.getEventsFor(apartmentInstance)
        );
    }

    @ParameterizedTest
    @Order(4)
    @MethodSource("ua.mibal.test.util.DataGenerator#eventsFactory")
    void isFreeForReservation(List<Event> events, LocalDateTime from, LocalDateTime to, boolean expected) {
        when(apartmentInstance.getBookingICalUrl())
                .thenReturn(Optional.of(calendarUrl));
        when(webContentReader.read(calendarUrl))
                .thenReturn("FILE CONTENT");

        boolean actual = service.isFreeForReservation(apartmentInstance, new ReservationForm(from, to, -1, -1L, "ignored"));

        assertEquals(true, actual);
    }
}
