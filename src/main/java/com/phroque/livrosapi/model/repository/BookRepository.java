package com.phroque.livrosapi.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phroque.livrosapi.model.entity.Book;


public interface BookRepository extends JpaRepository<Book, Long>{

	boolean existsByIsbn(String isbn);

}