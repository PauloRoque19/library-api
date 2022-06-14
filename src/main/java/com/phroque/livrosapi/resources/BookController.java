package com.phroque.livrosapi.resources;


import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.phroque.livrosapi.dto.BookDTO;
import com.phroque.livrosapi.exception.ApiErrors;
import com.phroque.livrosapi.exception.BusinessException;
import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.services.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {
	
	private BookService service;
	private ModelMapper modelMapper;
	
	public BookController(BookService service, ModelMapper mapper) {
		this.service = service;
		this.modelMapper = mapper;
	}


	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create(@RequestBody @Valid BookDTO dto) {	
		Book entity = modelMapper.map(dto, Book.class);
		
		entity = service.save(entity);
		
		BookDTO response = modelMapper.map(entity, BookDTO.class);

		return response;
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST )
	public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();
		return new ApiErrors(bindingResult);
	}
	
	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST )
	public ApiErrors handleBusinessExceptions(BusinessException ex) {
		return new ApiErrors(ex);
	}

}
