package com.mikeycaine.msgdemo.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mikeycaine.msgdemo.model.Message;
import com.mikeycaine.msgdemo.model.User;
import com.mikeycaine.msgdemo.repository.MessageRepository;
import com.mikeycaine.msgdemo.repository.UserRepository;

@Service
public class MessageService {
	
	Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	private UserRepository userRepository;
	private MessageRepository messageRepository;
	
	public MessageService(UserRepository userRepository, MessageRepository messageRepository) {
		this.userRepository = userRepository;
		this.messageRepository = messageRepository;
	}
	
	public Message createMessage(String userName, String messageText) {
		
		// Create a new User if necessary
		User user = userRepository.findByName(userName).orElseGet(() -> userRepository.save(new User(userName)));
		
		logger.debug("User is " + user);
		
		Message message = new Message();
		message.setUser(user);
		message.setText(messageText);
		
		return messageRepository.save(message);
	}
	
	public List<Message> wallFor(String userName) {
		User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException());
		
		return user.getMessages()
				.stream()
				.sorted(Comparator.comparing(Message::getCreated).reversed())
				.collect(Collectors.toList());
	}
	
	public void createFollow(String userName, String followee) {
		userRepository.findByName(userName).ifPresent(user -> {
			userRepository.findByName(followee).ifPresent(other -> {
				if (user.getFollowing().contains(other)) {
					logger.info(userName + " is already following " + other);
				} else {
					user.getFollowing().add(other);
					userRepository.save(user);
				}
			});
		});
	}
	
	public List<Message> timelineFor(String userName) {
		User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException());
		 
		return user.getFollowing().stream()
			.flatMap((User followee) -> followee.getMessages().stream())
			.sorted(Comparator.comparing(Message::getCreated).reversed())
			.collect(Collectors.toList());
	}
}
