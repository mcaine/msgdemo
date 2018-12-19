package com.mikeycaine.msgdemo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void testPostMessages() throws Exception {
		mvc.perform(post("/messageApi/Mike/createMessage").content("Message Number One"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.text").value("Message Number One"))
			.andExpect(jsonPath("$.user.name").value("Mike"))
			.andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	public void testPostMessageWithEmptyUserName() throws Exception {
		mvc.perform(post("/messageApi//createMessage").content("Message Number Two"))
			.andExpect(status().is4xxClientError());
	}
		
	@Test
	public void testWall() throws Exception {
		mvc.perform(post("/messageApi/Fluff/createMessage").content("Fluff Message Number One"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Fluff/createMessage").content("Fluff Message Number Two"))
			.andExpect(status().isOk());
		
		mvc.perform(get("/messageApi/Fluff/wall"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].text").value(hasItems("Fluff Message Number One", "Fluff Message Number Two")))
			.andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	public void testWallForNonExistentUser() throws Exception {
		mvc.perform(get("/messageApi/Billy/wall"))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testWallIsInReverseChronologicalOrder() throws Exception {
		
		for (int i = 1; i <= 10; ++i) {
			mvc.perform(post("/messageApi/Sophie/createMessage").content("Message " + i))
			.andExpect(status().isOk());
		}

		MvcResult mvcResult = mvc.perform(get("/messageApi/Sophie/wall"))
			.andExpect(status().isOk())
			.andReturn();
		
		//System.out.println("GOT CONTENT: " + mvcResult.getResponse().getContentAsString());
		
		DocumentContext context = JsonPath.parse(mvcResult.getResponse().getContentAsString());
		assertThat(context.read("$.length()"), is(10));
		
		List<String> timestamps = (List<String>)context.read("$[*].created");
		assertThat(timestamps.size(), is(10));
		
		assertTrue(timestampsAreInOrder(timestamps));
	}
	
	// Check that a List of timestamp strings are in chronologically reverse order ie from latest to earliest
	private boolean timestampsAreInOrder(List<String> timestamps) {
		return IntStream.range(0, timestamps.size() - 1).allMatch(idx -> { 
			return LocalDateTime.parse(timestamps.get(idx))
					.isAfter(LocalDateTime.parse(timestamps.get(idx + 1)));
		});
	}
	
	@Test
	public void testTimeStampsAreInOrderHelper() {
		List<String> inOrder = new ArrayList<>();
		inOrder.add("2018-12-19T12:44:01.999");
		inOrder.add("2018-12-19T12:44:01.001");
		assertTrue(timestampsAreInOrder(inOrder));
		
		List<String> outOfOrder = new ArrayList<>();
		outOfOrder.add("2018-12-19T12:44:01.001");
		outOfOrder.add("2018-12-19T12:44:01.999");
		assertFalse(timestampsAreInOrder(outOfOrder));
	}
	
	@Test
	public void testTimeline() throws Exception {
		
		mvc.perform(post("/messageApi/Theresa/createMessage").content("Theresa Message Number One"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Angela/createMessage").content("Angela Message Number One"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Emmanuel/createMessage").content("Emmanuel Message Number One"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Angela/createMessage").content("Angela Message Number Two"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Theresa/follow/Angela"))
			.andExpect(status().isOk());
		
		mvc.perform(post("/messageApi/Theresa/follow/Emmanuel"))
			.andExpect(status().isOk());
		
		MvcResult mvcResult = mvc.perform(get("/messageApi/Theresa/timeline"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$[*].text").value(hasItems("Angela Message Number One", "Emmanuel Message Number One", "Angela Message Number Two")))
		.andDo(MockMvcResultHandlers.print())
		.andReturn();
		
		DocumentContext context = JsonPath.parse(mvcResult.getResponse().getContentAsString());
		assertThat(context.read("$.length()"), is(3));
		
		List<String> timestamps = (List<String>)context.read("$[*].created");
		assertThat(timestamps.size(), is(3));
		
		assertTrue(timestampsAreInOrder(timestamps));
	}
	
//	@Test
//	public void testTimelineForMissingUser() throws Exception {
//		mvc.perform(get("/messageApi/Donald/timeline"))
//		.andExpect(status().is4xxClientError());
//	}
}
