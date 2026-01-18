package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Voucher;

import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.validation.annotation.EnumValidator;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String id;

	@NotBlank(message = "Please provide username")
	@Size(min = 2, max = 16, message = "Min length 2, max 16")
	private String username;

	@Pattern(regexp = "^[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
			message = "Invalid name format")
	@Size(min = 2, max = 16, message = "Min length 2, max 16")
	private String firstName;

	@Pattern(regexp = "^[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
			message = "Invalid name format")
	@Size(min = 2, max = 16, message = "Min length 2, max 16")
	private String lastName;

	private String role;

	//private List<Voucher> vouchers;

	@Pattern(regexp = "^$|^[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
			message = "Invalid phone format")
	private String phoneNumber;

	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}",
			flags = Pattern.Flag.CASE_INSENSITIVE,
			message = "Invalid mail format")
	private String email;

	@Builder.Default
	@NotNull(message = "Sum cant be empty")
	@PositiveOrZero(message = "Sum must be more than or equals zero")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	private boolean active = true;

	@EnumValidator(enumClass = AuthProvider.class)
	private String authProvider;

}
