package com.phroque.livrosapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {

	private String isbn;
	private  String customer;
	
}
