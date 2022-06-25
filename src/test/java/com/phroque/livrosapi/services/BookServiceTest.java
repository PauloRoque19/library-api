package com.phroque.livrosapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
	
	@Test
	@DisplayName("Deve obter um livro por id")
	public void bookFoundByIdTest() {
		Long id = 1l;
		
		Book book = createValidBook();
		book.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));
		
		Optional<Book> foundBook =  service.getById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		
	}
	
	@Test
	@DisplayName("Dever retornar vazio ao obter um livro por id quando ele não existe na base")
	public void bookNotFoundByIdTest() {
		Long id = 1l;
	
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Book> foundBook =  service.getById(id);
		
		assertThat(foundBook.isPresent()).isFalse();
		;
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() {
		Book book = Book.builder().id(1l).build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));
		
		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
	public void deleteInvalidBookTest() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));
		
		Mockito.verify(repository, Mockito.never()).delete(book);
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
	public void updateInvalidBookTest() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void bookUpdateTest() {
		Long id = 1l;
		
		Book bookUpdating = createValidBook();
		bookUpdating.setId(id);
		bookUpdating.setAuthor("José");
		
		Book bookUpdate = Book.builder().id(id).isbn("123").author("José").title("As aventuras").build();
		
		Mockito.when(repository.save(bookUpdating)).thenReturn(bookUpdate);
		
		Book bookUpdated = service.update(bookUpdating);
				
		assertThat(bookUpdated.getId()).isEqualTo(id);
		assertThat(bookUpdated.getAuthor()).isEqualTo(bookUpdating.getAuthor());
		assertThat(bookUpdated.getIsbn()).isEqualTo(bookUpdating.getIsbn());
		assertThat(bookUpdated.getTitle()).isEqualTo(bookUpdating.getTitle());
		
	}
	
	
	@Test
	@DisplayName("Deve filtrar livros pelas propriedades")
	public void findBookTest() {
		
		Book book = createValidBook();
		List<Book> list = new ArrayList<>();
		list.add(book);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		Page<Book> page = new PageImpl<>(list, pageRequest, 1);
		when(repository.findAll(Mockito.any(Example.class),Mockito.any(PageRequest.class))).thenReturn(page);
		
		Page<Book> result = service.find(book, pageRequest);
		
		
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
	
	@Test
	@DisplayName("deve obter um livro pelo isbn")
	public void getBookByIsbnTest() {
		String isbn = "1230";
		when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).title("As aventuras").author("Fulano").build()));
		
		Optional<Book> book = service.getBookByIsbn(isbn);
		
		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(1l);
		assertThat(book.get().getIsbn()).isEqualTo(isbn);
		
		verify(repository, times(1)).findByIsbn(isbn);
	}
}
