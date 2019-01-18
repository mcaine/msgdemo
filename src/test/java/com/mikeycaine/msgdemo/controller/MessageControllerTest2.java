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
		
		List<Message> messages = parseWallReply(reply.get());
		return messages;
	}
	
	private List<Message> getWallForUser(String user) throws Exception {
		AtomicReference<String> reply = new AtomicReference<>();
		
		mvc.perform(get("/messageApi/" + user + "/wall"))
		.andExpect(status().isOk())
		.andDo(result -> reply.set(result.getResponse().getContentAsString()));
		
		List<Message> messages = parseWallReply(reply.get());
		return messages;
	}
	
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
		postMessage("James", "Hello from James");
		postMessage("James", "Another message from James");
		postMessage("James", "Yet Another message from James");
		postMessage("James", "That James sure does love his Messages");
		
		List<Message> messages = getWallForUser("James");
		assertNewestMessagesFirst(messages);
	}
	
	@Test
	public void testFollow() throws Exception {
		postMessage("Fluff", "Fluff has something to say");
		
		postMessage("James", "Hello again from James");
		
		postMessage("Jasper", "Hello from Jasper");
		postMessage("Jasper", "Another message from Jasper");
		postMessage("Jasper", "Yet Another message from Jasper");
		postMessage("Jasper", "That Jasper sure does love his Messages");
		List<Message> messages = getWallForUser("Jasper");
		assertNewestMessagesFirst(messages);
		
		messages = getTimelineForUser("James");
		assertTrue(messages.isEmpty());
		
		follow("James", "Jasper");
		messages = getTimelineForUser("James");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(4));
		
		postMessage("Sophie", "Sophie sticks his oar in");
		
		follow("James", "Sophie");
		messages = getTimelineForUser("James");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(5));
		
		follow("James", "Fluff");
		messages = getTimelineForUser("James");
		assertNewestMessagesFirst(messages);
		assertThat(messages.size(), is(6));
		
		messages = getTimelineForUser("Jasper");
		assertTrue(messages.isEmpty());
		
		follow("Jasper", "Fluff");
		follow("Jasper", "Sophie");
		messages = getTimelineForUser("Jasper");
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
			}
		}
		
		for (int i = 1; i < 100; ++i) {
			String user = userName(i);
			List<Message> timeline = getTimelineForUser(user);	
			assertNewestMessagesFirst(timeline);
			assertThat(timeline.size(), is(i - 1));
		}
	}
}
