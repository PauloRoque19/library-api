package com.phroque.livrosapi.services;

import org.springframework.stereotype.Service;

import com.phroque.livrosapi.exception.BusinessException;
import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.model.repository.BookRepository;

@Service
public class BookServiceImpl implements BookService {
	
	private BookRepository repository;
	
	public BookServiceImpl(BookRepository repository) {	
		this.repository = repository;
	}

	@Override
	public Book save(Book book) {
		if(repository.existsByIsbn(book.getIsbn())) {
			throw new BusinessException("Isbn já cadastrado.");
		}
		return repository.save(book);
	}

}
