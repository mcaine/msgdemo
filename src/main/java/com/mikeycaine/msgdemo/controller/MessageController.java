package com.mikeycaine.msgdemo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mikeycaine.msgdemo.model.Message;
import com.mikeycaine.msgdemo.service.MessageService;

@RestController
public class MessageController {
	
	Logger logger = LoggerFactory.getLogger(MessageController.class);
	
	private MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}
	
	@PostMapping(value="/messageApi/{userName}/createMessage")
	public ResponseEntity<Message> createMessage(
		@PathVariable String userName,
		@RequestBody String messageText) {
		
		logger.info("Creating message from User " + userName + ", with text '" + messageText + "'");
		
		Message message = messageService.createMessage(userName, messageText);
		return new ResponseEntity(message, HttpStatus.OK);
	}
	
	@GetMapping(value="/messageApi/{userName}/wall") 
	public ResponseEntity<List<Message>> wall(
		@PathVariable String userName) {
		List<Message> messages = messageService.wallFor(userName);
		return new ResponseEntity(messages, HttpStatus.OK);
	}
	
	@PostMapping(value="/messageApi/{userName}/follow/{followee}")
	public ResponseEntity follow(
		@PathVariable String userName,
		@PathVariable String followee) {
		messageService.createFollow(userName, followee);
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@GetMapping(value="/messageApi/{userName}/timeline")
	public ResponseEntity<List<Message>> timeline (@PathVariable String userName) {
		List<Message> messages = messageService.timelineFor(userName);
		return new ResponseEntity(messages, HttpStatus.OK);
	}
}
