package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.model.Voucher;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String id;

	private String username;

	private String role;

	private List<Voucher> vouchers;

	private String email;

	private String phoneNumber;

	@Builder.Default
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	private boolean active = true;

	private String authProvider;

}
