package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String id;

	private String username;

	private String firstName;

	private String lastName;

	private String role;

	//private List<Voucher> vouchers;

	private String email;

	private String phoneNumber;

	@Builder.Default
	@NotNull(message = "Sum cant be empty")
	@PositiveOrZero(message = "Sum must be more than 0 or 0")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	private boolean active = true;

	private String authProvider;

}
