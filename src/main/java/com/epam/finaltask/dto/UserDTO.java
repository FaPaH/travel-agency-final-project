package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Voucher;

import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.validation.annotation.EnumValidator;
import com.epam.finaltask.validation.annotation.UniqueEmail;
import com.epam.finaltask.validation.annotation.UniqueUsername;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String id;

	@NotBlank(message = "{validation.auth.username.required}")
	@Size(min = 2, max = 16, message = "{validation.user.username.size}")
	private String username;

	@Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
			message = "{validation.user.name.format}")
	@Size(min = 2, max = 16, message = "{validation.user.name.size}")
	private String firstName;

	@Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
			message = "{validation.user.name.format}")
	@Size(min = 2, max = 16, message = "{validation.user.name.size}")
	private String lastName;

	private String role;

	//private List<Voucher> vouchers;

	@Pattern(regexp = "^$|^[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
			message = "{validation.user.phone.format}")
	private String phoneNumber;

	@Pattern(regexp = "^$|^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}$",
			flags = Pattern.Flag.CASE_INSENSITIVE,
			message = "{validation.user.email.format}")
	@UniqueEmail(message = "{validation.email.exists}")
	private String email;

	@Builder.Default
	@NotNull(message = "{validation.payment.amount.required}")
	@PositiveOrZero(message = "{validation.payment.amount.positive}")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	private boolean active = true;

	@EnumValidator(enumClass = AuthProvider.class, message = "{validation.enum.invalid}")
	private String authProvider;

}
