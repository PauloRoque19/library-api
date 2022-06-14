package com.phroque.livrosapi.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.phroque.livrosapi.exception.BusinessException;
import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.model.repository.BookRepository;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

	BookService service;
	
	@MockBean
	BookRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		Book book = Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
		
		Mockito.when(repository.save(book)).thenReturn(
				Book.builder()
					.id(1l)
					.isbn("123")
					.title("As aventuras")
					.author("Fulano")
				.build());
		
		Book savedBook = service.save(book);
		
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
	}
	
	private Book createValidBook() {
		return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
	}
	
	@Test
	@DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
	public void shouldNotSaveABookWithDuplicatedISBN() {
		Book book = createValidBook();
		
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		
		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Isbn já cadastrado.");
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
}
