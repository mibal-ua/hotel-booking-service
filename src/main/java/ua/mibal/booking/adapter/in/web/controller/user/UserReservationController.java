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

package ua.mibal.booking.adapter.in.web.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.mibal.booking.adapter.in.web.mapper.ReservationDtoMapper;
import ua.mibal.booking.adapter.in.web.model.ReservationDto;
import ua.mibal.booking.adapter.in.web.model.ReservationFormDto;
import ua.mibal.booking.adapter.in.web.model.ReservationRejectingFormDto;
import ua.mibal.booking.adapter.in.web.security.annotation.UserAllowed;
import ua.mibal.booking.application.ReservationService;
import ua.mibal.booking.application.mapper.ReservationFormMapper;
import ua.mibal.booking.application.model.ReservationForm;
import ua.mibal.booking.application.model.ReservationRejectingForm;
import ua.mibal.booking.domain.Reservation;
import ua.mibal.booking.domain.id.ApartmentId;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@RequiredArgsConstructor
@UserAllowed
@RestController
@RequestMapping("/api")
public class UserReservationController {
    private final ReservationService reservationService;
    private final ReservationDtoMapper reservationDtoMapper;
    private final ReservationFormMapper reservationFormMapper;

    @GetMapping("/users/me/reservations")
    public Page<ReservationDto> getAllByUser(Authentication authentication, Pageable pageable) {
        Page<Reservation> reservations =
                reservationService.getAllByUser(authentication.getName(), pageable);
        return reservationDtoMapper.toDtos(reservations);
    }

    @PatchMapping("/apartments/{apartmentId}/reserve")
    @ResponseStatus(CREATED)
    public void reserve(@PathVariable String apartmentId,
                        @Valid ReservationFormDto dto,
                        Authentication authentication) {
        ReservationForm form = reservationFormMapper.toForm(
                dto, new ApartmentId(apartmentId), authentication.getName());
        reservationService.reserve(form);
    }

    @PatchMapping("/reservations/{id}/reject")
    @ResponseStatus(NO_CONTENT)
    public void rejectReservation(@PathVariable("id") Long id,
                                  @Valid @RequestBody ReservationRejectingFormDto dto,
                                  Authentication authentication) {
        ReservationRejectingForm form =
                reservationFormMapper.toRejectingForm(dto, id, authentication.getName());
        reservationService.rejectReservation(form);
    }
}