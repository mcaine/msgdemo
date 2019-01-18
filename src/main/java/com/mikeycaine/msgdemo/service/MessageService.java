package com.mikeycaine.msgdemo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mikeycaine.msgdemo.model.Follow;
import com.mikeycaine.msgdemo.model.FollowKey;
import com.mikeycaine.msgdemo.model.Message;
import com.mikeycaine.msgdemo.model.User;
import com.mikeycaine.msgdemo.repository.FollowRepository;
import com.mikeycaine.msgdemo.repository.MessageRepository;
import com.mikeycaine.msgdemo.repository.UserRepository;

@Service
public class MessageService {
	
	Logger logger = LoggerFactory.getLogger(MessageService.class);
	private final Comparator<Message> REVERSE_CHRONOLOGICAL = Comparator.comparing(Message::getCreated).reversed();
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final FollowRepository followRepository;
	
	public MessageService(UserRepository userRepository, 
							MessageRepository messageRepository, 
							FollowRepository followRepository) {
		this.userRepository = userRepository;
		this.messageRepository = messageRepository;
		this.followRepository = followRepository;
	}
	
	public Message createMessage(String userName, String messageText) {
		User user = userRepository.findByName(userName).orElseGet(() -> userRepository.save(new User(userName)));
		return messageRepository.save(new Message(user, messageText));
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
		
		FollowKey followKey = new FollowKey(user.getId(), followee.getId());
		
		Optional<Follow> optFollow = followRepository.findById(followKey);
		if (!optFollow.isPresent()) {
			Follow follow = new Follow();
			follow.setFollower(user);
			follow.setFollowee(followee);
			follow.setId(followKey);
			followRepository.save(follow);
		}
	}
	
	public List<Message> timelineFor(String userName) {
		User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException());
		
		List<Follow> followsForUser = followRepository.findByFollowerId(user.getId());
		
		return followsForUser.stream().flatMap((Follow follow) -> {
			Optional<User> optFollowee = userRepository.findById(follow.getId().getFolloweeId());
			
			return userRepository
				.findById(follow.getId().getFolloweeId())
				.map(followee -> followee.getMessages().stream())
				.orElse(Stream.empty());
		})
		.sorted(REVERSE_CHRONOLOGICAL)
		.collect(Collectors.toList());
	}
}
