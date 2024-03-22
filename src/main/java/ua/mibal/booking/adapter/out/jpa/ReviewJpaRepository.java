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

package ua.mibal.booking.adapter.out.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.mibal.booking.application.port.jpa.ReviewRepository;
import ua.mibal.booking.domain.Review;

import java.util.List;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
public interface ReviewJpaRepository extends JpaRepository<Review, Long>, ReviewRepository {

    @Override
    @Query("""
            select r from Review r
                join fetch r.user
            where r.apartment.id = ?1
             """)
    List<Review> findByApartmentIdFetchUser(Long apartmentId, Pageable pageable);

    @Override
    @Query("""
            select r from Review r
                join fetch r.user
            order by r.createdAt desc
             """)
    List<Review> findLatestFetchUser(Pageable pageable);
}