package com.mikeycaine.msgdemo.controller;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mikeycaine.msgdemo.model.Message;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest2 {
	
	@Autowired
	private MockMvc mvc;

	private void postMessage(String user, String message) throws Exception {
		mvc.perform(post("/messageApi/" + user + "/createMessage").content(message)).andExpect(status().isOk());
	}
	
	private List<Message> parseWallReply(String text) throws Exception {
		ObjectMapper mapper = 
				new ObjectMapper()
					.registerModule(new Jdk8Module())
					.registerModule(new JavaTimeModule());
			
			TypeReference<List<Message>> typeReference = new TypeReference<List<Message>>(){};
			
			List<Message> messages = mapper.readValue(text, typeReference);
			return messages;
	}
	
	private List<Message> getTimelineForUser(String user) throws Exception {
		AtomicReference<String> reply = new AtomicReference<>();
		
		mvc.perform(get("/messageApi/" + user + "/timeline"))
		.andExpect(status().isOk())
		.andDo(result -> reply.set(result.getResponse().getContentAsString()));
		
//		String text = reply.get();
//		System.out.println("Got content: " + text);
		
		List<Message> messages = parseWallReply(reply.get());
		return messages;
	}
	
	private List<Message> getWallForUser(String user) throws Exception {
		AtomicReference<String> reply = new AtomicReference<>();
		
		mvc.perform(get("/messageApi/" + user + "/wall"))
		.andExpect(status().isOk())
		.andDo(result -> reply.set(result.getResponse().getContentAsString()));
		
//		String text = reply.get();
//		System.out.println("Got content: " + text);
		
		List<Message> messages = parseWallReply(reply.get());
		return messages;
	}
	
//	private void checkWallForUser(String user) throws Exception {
//		List<Message> messages = getWallForUser(user);
//		
////		messages.forEach(message -> {
////			System.out.println("ID: " + message.getId());
////			System.out.println("Text: " + message.getText());
////			System.out.println("Created: " + message.getCreated());
////			System.out.println();
////		});
//		
//		assertNewestMessagesFirst(messages);
//	}
	
	private void follow(String user, String followee) throws Exception {
		mvc.perform(post("/messageApi/" + user + "/follow/" + followee)).andExpect(status().isOk());
	}
	
	private void assertNewestMessagesFirst(List<Message> messages) {
		if (messages.isEmpty()) {
			return;
		}
		
		Message prev = messages.get(0);
		for (int i = 1; i < messages.size(); ++i) {
			assertTrue(messages.get(i).getCreated().isBefore(prev.getCreated()));
			prev = messages.get(i);
		}
	}
	
	@Test
	public void test() throws Exception {
		postMessage("Mike", "Hello from Mike");
		postMessage("Mike", "Another message from Mike");
		postMessage("Mike", "Yet Another message from Mike");
		postMessage("Mike", "That Mike sure does love his Messages");
		
		List<Message> messages = getWallForUser("Mike");
		assertNewestMessagesFirst(messages);
	}
	
	@Test
	public void testFollow() throws Exception {
		postMessage("Fluff", "Fluff has something to say");
		
		postMessage("Mike", "Hello again from Mike");
		
		postMessage("Fran", "Hello from Fran");
		postMessage("Fran", "Another message from Fran");
		postMessage("Fran", "Yet Another message from Fran");
		postMessage("Fran", "That Fran sure does love her Messages");
		List<Message> messages = getWallForUser("Fran");
		assertNewestMessagesFirst(messages);
		
		messages = getTimelineForUser("Mike");
		assertTrue(messages.isEmpty());
		
		follow("Mike", "Fran");
		messages = getTimelineForUser("Mike");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(4));
		
		postMessage("Sophie", "Sophie sticks his oar in");
		
		follow("Mike", "Sophie");
		messages = getTimelineForUser("Mike");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(5));
		
		follow("Mike", "Fluff");
		messages = getTimelineForUser("Mike");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(6));
		
		messages = getTimelineForUser("Fran");
		assertTrue(messages.isEmpty());
		
		follow("Fran", "Fluff");
		follow("Fran", "Sophie");
		messages = getTimelineForUser("Fran");
		assertThat(messages.size(), is(2));
	}
	
	private String userName(int i) {
		return String.format("User%03d", i);
	}
	
	@Test
	public void testBigFollow() throws Exception {
		for (int i = 1; i < 100; ++i) {
			String user = userName(i);
			postMessage(user, "Hello from " + user);
			
			for (int j = i - 1; j > 0; --j) {
				String followee = userName(j);
				follow(user, followee);
				//System.out.println(user + " follows " + followee);
			}
		}
		
		//List<Message> user001messages = getWallForUser("User001");
		//user001messages.forEach(message -> System.out.println("USER001: " + message.getText()));
		
		//List<Message> user099messages = getWallForUser("User099");
		//user099messages.forEach(message -> System.out.println("USER099: " + message.getText()));
		
		System.out.println("HERE I AM");
		
		
		for (int i = 1; i < 100; ++i) {
			String user = userName(i);
			List<Message> timeline = getTimelineForUser(user);	
			assertNewestMessagesFirst(timeline);
			assertThat(timeline.size(), is(i - 1));
		}
		
		//user099timeline.forEach(message -> System.out.println("TIMELINE: " + message.getText()));
		//assertTrue(user099timeline.size() == 98);
		
	}

}
