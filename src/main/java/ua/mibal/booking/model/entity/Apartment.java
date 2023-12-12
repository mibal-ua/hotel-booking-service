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

package ua.mibal.booking.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.type.NumericBooleanConverter;
import ua.mibal.booking.model.entity.embeddable.ApartmentOptions;
import ua.mibal.booking.model.entity.embeddable.Photo;
import ua.mibal.booking.model.entity.embeddable.Price;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "apartments")
public class Apartment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(
            name = "prices",
            joinColumns = @JoinColumn(
                    name = "apartment_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "prices_apartment_id_fk")
            ),
            indexes = @Index(
                    name = "prices_apartment_id_idx",
                    columnList = "apartment_id"
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "prices_apartment_id_and_person_uq",
                    columnNames = {"apartment_id", "person"}
            ))
    private List<Price> prices = new LinkedList<>();

    @Embedded
    private ApartmentOptions options;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(nullable = false)
    private Boolean published;

    @Column
    private Double rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApartmentClass apartmentClass;

    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(
            name = "apartment_photos",
            joinColumns = @JoinColumn(
                    name = "apartment_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "apartment_photos_apartment_id_fk")
            ),
            indexes = @Index(
                    name = "apartment_photos_photo_link_idx",
                    columnList = "photo_link",
                    unique = true
            ))
    @OrderColumn
    @Setter(PRIVATE)
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(mappedBy = "apartment")
    @Setter(PRIVATE)
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "apartment")
    @Setter(PRIVATE)
    private List<ApartmentInstance> apartmentInstances = new ArrayList<>();

    @OneToMany(mappedBy = "apartment")
    @Setter(PRIVATE)
    private List<Comment> comments = new ArrayList<>();

    public Apartment() {
         this.setOptions(ApartmentOptions.DEFAULT);
         this.setPublished(false);
    }

    public void addApartmentInstance(ApartmentInstance apartmentInstance) {
        apartmentInstance.setApartment(this);
        this.apartmentInstances.add(apartmentInstance);
    }

    public void removeApartmentInstance(ApartmentInstance apartmentInstance) {
        if (this.apartmentInstances.contains(apartmentInstance)) {
            this.apartmentInstances.remove(apartmentInstance);
            apartmentInstance.setApartment(null);
        }
    }

    public void addComment(Comment comment) {
        comment.setApartment(this);
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        if (this.comments.contains(comment)) {
            this.comments.remove(comment);
            comment.setApartment(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apartment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void addPhoto(Photo photo) {
        this.photos.add(photo);
    }

    public boolean deletePhoto(Photo photo) {
        return this.photos.remove(photo);
    }

    public Optional<Price> getPriceForPeople(Integer people) {
        return getPrices().stream()
                .filter(pr -> people.equals(pr.getPerson()))
                .findFirst();
    }

    public enum ApartmentClass {
        COMFORT, STANDARD
    }
}
