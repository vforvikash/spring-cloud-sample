package com.example.bookmark;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/{userId}/bookmarks")
public class BookmarkRestController {
	
	private final BookmarkRepository bookmarkRepository;

	private final AccountRepository accountRepository;
	
	@Autowired
	public BookmarkRestController(BookmarkRepository br, AccountRepository ar) {
		this.bookmarkRepository=br;
		this.accountRepository=ar;
	}
	
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input){
		validateUserAccount(userId);
		return 	this.accountRepository.findByUsername(userId).map(account -> {
			Bookmark newBookmark = this.bookmarkRepository.save(new Bookmark(account, input.uri, input.description));
			
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("{id}").buildAndExpand(newBookmark.getId()).toUri());
			return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
		}).get();
	}
	
	@RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
	Bookmark readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
		this.validateUserAccount(userId);
		return this.bookmarkRepository.findOne(bookmarkId);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	Collection<Bookmark> readBookmarks(@PathVariable String userId) {
		this.validateUserAccount(userId);
		return this.bookmarkRepository.findByAccountUsername(userId);
	}

	private void validateUserAccount(String userId) {
		this.accountRepository.findByUsername(userId).orElseThrow(()->new UserNotFoundException(userId));
	}
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 324984106631866556L;

	public UserNotFoundException(String userId) {
		super("could not find user '" + userId + "'.");
	}
}