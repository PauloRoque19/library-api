package com.phroque.livrosapi.repository;

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
		Book book = Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
		entityManager.persist(book);
		
		boolean exists = this.repository.existsByIsbn(isbn);
		
		Assertions.assertThat(exists).isTrue();
	}
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando não existir um livro na base com o isbn informado")
	public void returnFalseWhenIsbnExists() {
		String isbn = "123";
		
		
		boolean exists = this.repository.existsByIsbn(isbn);
		
		Assertions.assertThat(exists).isFalse();
	}
}
