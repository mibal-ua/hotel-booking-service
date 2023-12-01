package ua.mibal.booking.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
public record CreateCommentDto(

        @NotBlank
        @Size(min = 1, max = 255)
        String body,

        @NotNull
        @DecimalMin("0")
        @DecimalMax("5")
        Double rate
) {
}
