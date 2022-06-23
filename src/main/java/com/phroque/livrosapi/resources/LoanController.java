package com.phroque.livrosapi.resources;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.phroque.livrosapi.dto.LoanDTO;
import com.phroque.livrosapi.exception.ApiErrors;
import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.model.entity.Loan;
import com.phroque.livrosapi.services.BookService;
import com.phroque.livrosapi.services.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

	private final LoanService service;
	private final BookService bookService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO dto) {
		
		Book book =  bookService.getBookByIsbn(dto.getIsbn())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));
		
		Loan entity = Loan.builder()
							.book(book)
							.customer(dto.getCustomer())
							.loanDate(LocalDate.now())
						.build();
		entity = service.save(entity);
		
		return entity.getId();
	}
	
	
}
