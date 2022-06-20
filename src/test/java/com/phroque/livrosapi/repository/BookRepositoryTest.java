package com.phroque.livrosapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.model.repository.BookRepository;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository repository;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
	public void returnTrueWhenIsbnExists() {
		String isbn = "123";
		Book book = createNewBook(isbn);
		entityManager.persist(book);
		
		boolean exists = this.repository.existsByIsbn(isbn);
		
		Assertions.assertThat(exists).isTrue();
	}

	private Book createNewBook(String isbn) {
		return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
	}
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando não existir um livro na base com o isbn informado")
	public void returnFalseWhenIsbnExists() {
		String isbn = "123";
		
		boolean exists = this.repository.existsByIsbn(isbn);
		
		Assertions.assertThat(exists).isFalse();
	}
	
	@Test
	@DisplayName("Deve obter um livro por id.")
	public void findByIdTest() {
		String isbn = "123";
		Book book = createNewBook(isbn);
		entityManager.persist(book);
		
		Optional<Book> foundBook = repository.findById(book.getId());
		Assertions.assertThat(foundBook.isPresent()).isTrue();
		Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		
	}
	
	@Test
	@DisplayName("Deve salvar um livro.")
	public void saveBookTest() {
		Book book = createNewBook("123");
		
		Book savedBook = repository.save(book);
		
		assertThat(savedBook.getId()).isNotNull();
	}
	
	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() {
		Book book = createNewBook("123");
		entityManager.persist(book);
		
		Book foundBook = entityManager.find(Book.class, book.getId());
		
		repository.delete(foundBook);
		
		Book deleteBook = entityManager.find(Book.class, book.getId());
		
		assertThat(deleteBook).isNull();
		
	}
}
