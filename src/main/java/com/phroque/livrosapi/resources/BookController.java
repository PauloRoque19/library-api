package com.phroque.livrosapi.resources;


import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	
	@GetMapping("{id}")
	public BookDTO get(@PathVariable Long id) {
		return service
				.getById(id)
				.map(book -> modelMapper.map(book, BookDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));	
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(book);
	}
	
	@PutMapping("{id}")
	public BookDTO update(@PathVariable Long id, BookDTO dto) {
		return service.getById(id)
			.map((book) -> {
				book.setAuthor(dto.getAuthor());
				book.setTitle(dto.getTitle());
				book = service.update(book);
				return modelMapper.map(book, BookDTO.class);
			}
			).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));			
	}
	
	@GetMapping
	public Page<BookDTO> find (BookDTO dto, Pageable pageRequest) {
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = service.find(filter, pageRequest);
		List<BookDTO> list = result.getContent()
				.stream()
				.map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<BookDTO>(list,pageRequest,result.getTotalElements());
	}
	
	

}
