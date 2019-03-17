package com.mightyjava.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.mightyjava.config.MessageConfig;
import com.mightyjava.dto.Book;
import com.mightyjava.utils.ErrorUtils;

@Controller
@RequestMapping("book")
public class BookController {

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/form")
	public String bookForm(Model model) {
		model.addAttribute("isNew", true);
		model.addAttribute("bookForm", new Book());
		return "/book/form";
	}

	@GetMapping("/list")
	public String bookList(Model model, Pageable pageable) {
		ResponseEntity<Collection<Book>> response = restTemplate.exchange("http://localhost:8081/rest/books",
				HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Book>>() {
				});
		model.addAttribute("books", response.getBody());
		return "/book/list";
	}

	@GetMapping("/edit/{id}")
	public String bookOne(@PathVariable Long id, Model model) {
		model.addAttribute("isNew", false);
		ResponseEntity<Book> response = restTemplate.exchange("http://localhost:8081/rest/books/" + id, HttpMethod.GET,
				null, new ParameterizedTypeReference<Book>() {
				});
		model.addAttribute("bookForm", response.getBody());
		return "/book/form";
	}
	
	@Autowired
	private MessageConfig messageConfig;
	
	@PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String bookAdd(@Valid @RequestBody Book book, BindingResult result) throws JSONException {
		if(result.hasErrors()) {
			return ErrorUtils.customErrors(result.getAllErrors());
		} else {
			String message = null;
			ResponseEntity<Book> response = null;
			JSONObject jsonObject = new JSONObject();
			
			HttpEntity<Book> request = new HttpEntity<Book>(book);
			if(book.getId() == null) {
				message = messageConfig.getMessage("label.added");
				response = restTemplate.exchange("http://localhost:8081/rest/books",
							HttpMethod.POST, request, new ParameterizedTypeReference<Book>() {
						});
			} else {
				message = messageConfig.getMessage("label.updated");
				response = restTemplate.exchange("http://localhost:8081/rest/books",
							HttpMethod.PUT, request, new ParameterizedTypeReference<Book>() {
						});
			}
			jsonObject.put("status", "success");
			
			String[] msg = {message};
			jsonObject.put("title", messageConfig.getMessage("label.confirmation", msg));
			
			Book respBook = response.getBody();
			String[] msg2 = {respBook.getTitle(), message};
			jsonObject.put("message", messageConfig.getMessage("book.save.success", msg2));
			return jsonObject.toString();
		}
	}
	
	@GetMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String bookDelete(@PathVariable Long id) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		ResponseEntity<Book> response = restTemplate.exchange("http://localhost:8081/rest/books/" + id, HttpMethod.DELETE,
				null, new ParameterizedTypeReference<Book>() {
				});
		Book respBook = response.getBody();
		if(respBook != null) {
			jsonObject.put("message", messageConfig.getMessage("book.delete.success"));
		}
		return jsonObject.toString();
	}
}
