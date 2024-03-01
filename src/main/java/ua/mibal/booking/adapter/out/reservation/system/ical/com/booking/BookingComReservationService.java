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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.mibal.booking.adapter.IcalMapper;
import ua.mibal.booking.adapter.out.reservation.system.ical.WebContentReader;
import ua.mibal.booking.application.port.reservation.system.ReservationSystem;
import ua.mibal.booking.domain.ApartmentInstance;
import ua.mibal.booking.domain.Event;
import ua.mibal.booking.domain.ReservationRequest;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RequiredArgsConstructor
@Service
public class BookingComReservationService implements ReservationSystem {
    private final WebContentReader webContentReader;
    private final IcalMapper icalMapper;

    @Override
    public boolean isFreeForReservation(ApartmentInstance apartmentInstance,
                                        ReservationRequest reservationRequest) {
        List<Event> events = getEventsFor(apartmentInstance);
        Predicate<Event> eventIntersectsWithReservation =
                event -> event.getEnd().isAfter(reservationRequest.from()) &&
                         event.getStart().isBefore(reservationRequest.to());
        return events.stream()
                .noneMatch(eventIntersectsWithReservation);
    }

    @Override
    public List<Event> getEventsFor(ApartmentInstance apartmentInstance) {
        Optional<String> calendarUrl = apartmentInstance.getBookingICalUrl();
        if (calendarUrl.isEmpty()) {
            return emptyList();
        }
        return getEventsByCalendarUrl(calendarUrl.get());
    }

    private List<Event> getEventsByCalendarUrl(String calendarUrl) {
        String icalFile = webContentReader.read(calendarUrl);
        return icalMapper.getEvents(icalFile);
    }
}