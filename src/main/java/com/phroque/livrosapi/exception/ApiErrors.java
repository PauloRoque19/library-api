package com.phroque.livrosapi.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;


public class ApiErrors {

	private List<String> errors;
	
	public ApiErrors(BusinessException ex ) {
		this.errors = new ArrayList<String>();
		errors.add(ex.getMessage());
		
	}

	public ApiErrors(BindingResult bindingResult) {
		this.errors = new ArrayList<>();
		bindingResult.getAllErrors().stream().forEach( error -> this.errors.add(error.getDefaultMessage())); 
	}
	
	public ApiErrors(ResponseStatusException ex) {
		this.errors = new ArrayList<>();
		this.errors.add(ex.getReason());
	}
	
	public List<String> getErrors(){
		return this.errors;
	}
}
