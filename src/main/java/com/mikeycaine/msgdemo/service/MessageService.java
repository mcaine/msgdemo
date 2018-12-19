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
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final Comparator<Message> REVERSE_CHRONOLOGICAL = Comparator.comparing(Message::getCreated).reversed();
	
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
		return userRepository
			.findByName(userName).orElseThrow(() -> new UserNotFoundException())
			.getMessages()
			.stream()
			.sorted(REVERSE_CHRONOLOGICAL)
			.collect(Collectors.toList());
	}
	
	public void createFollow(String userName, String followeeName) {
		User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException());
		User followee = userRepository.findByName(followeeName).orElseThrow(() -> new UserNotFoundException());
		
		if (user.getFollowing().stream().noneMatch(followed -> followed.getName().equals(userName))) {
			user.getFollowing().add(followee);
			userRepository.save(user);
		}
	}
	
	public List<Message> timelineFor(String userName) {
		User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException());
		 
		return user.getFollowing().stream()
			.flatMap(followee -> followee.getMessages().stream())
			.sorted(REVERSE_CHRONOLOGICAL)
			.collect(Collectors.toList());
	}
}
