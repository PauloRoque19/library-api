package com.phroque.livrosapi.resources;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phroque.livrosapi.dto.BookDTO;
import com.phroque.livrosapi.exception.BusinessException;
import com.phroque.livrosapi.model.entity.Book;
import com.phroque.livrosapi.services.BookService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {
	
	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService service;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso.")
	public void createBookTest() throws Exception {
		
		BookDTO dto = BookDTO.builder().author("Arthur").title("As aventuras").isbn("001").build();
		Book savedBook = Book.builder().id(10l).author("Arthur").title("As aventuras").isbn("001").build();
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
			.andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
			.andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
			.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()))
			;
			
	}
	
	
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficiente para criação do livro.")
	public void createInvalidBookTest() throws Exception{
		String json = new ObjectMapper().writeValueAsString(new BookDTO());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors",Matchers.hasSize(3)));
		
	}
	
	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro om isbn utilizado por outro.")
	public void createBookWithDuplicatedIsbn() throws Exception{
	
		BookDTO dto = createNewBook();
		String msgErro = "Isbn já cadastrado.";
		
		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given(service.save(Mockito.any(Book.class)))
					.willThrow(new BusinessException(msgErro));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", Matchers.hasSize(1)))
			.andExpect(jsonPath("errors[0]").value(msgErro));
	}
	
	@Test
	@DisplayName("Deve obter informações de um livro.")
	public void getBookDetailsTest() throws Exception{
		Long id = 1l;
		
		Book book = Book.builder()
				.id(id)
				.title(createNewBook().getTitle())
				.author(createNewBook().getAuthor())
				.isbn(createNewBook().getIsbn())
		.build();
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
		
		MockHttpServletRequestBuilder request =  MockMvcRequestBuilders
			.get(BOOK_API.concat("/"+id))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(id))
			.andExpect(jsonPath("title").value(createNewBook().getTitle()))
			.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
			.andExpect(jsonPath("isbn").value(createNewBook().getIsbn()))
			;
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando o livro procurado não existir")
	public void bookNotFoundTest() throws Exception{
		Long id = 1l;
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.empty());
		MockHttpServletRequestBuilder request =  MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc
		.perform(request)
		.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception {
		BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.delete(BOOK_API.concat("/" + 1))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isNoContent());
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception {
		Long id = 1l;
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		
		Book updatingBook = Book.builder().id(1l).title("some title").author("some author").isbn("321").build();
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
		
		Book updatedBook = Book.builder().id(id).author("Artur").title("As aventuras").isbn("321").build();
		BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value("321"));
	}
	
	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente.")
	public void updateInexistentBookTest() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
				.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve filtrar livros")
	public void findBooksTest() throws Exception {
		Long id = 1l;
		
		Book book = Book.builder()
						.id(id)
						.title(createNewBook().getTitle())
						.author(createNewBook().getAuthor())
						.isbn(createNewBook().getIsbn())
					.build();
		
		List<Book> list = new ArrayList<Book>();
		list.add(book);
		
		BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
			.willReturn(new PageImpl<Book>(list, PageRequest.of(0, 100), 1));
		
		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(BOOK_API.concat(queryString))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("content", Matchers.hasSize(1)))
			.andExpect(jsonPath("totalElements").value(1))
			.andExpect(jsonPath("pageable.pageSize").value(100))
			.andExpect(jsonPath("pageable.pageNumber").value(0));
		
	}
	
	private BookDTO createNewBook() {
		return BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
	}
	
}
