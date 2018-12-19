package com.mikeycaine.msgdemo.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Message {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String text;
	
	@CreationTimestamp
	private LocalDateTime created;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
	
	public Message() {
	}
	
	public Message(Long id, String text) {
		this.id = id;
		this.text = text;
	}
	
	public Message(User user, String text) {
		this.user = user;
		this.text = text;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public LocalDateTime getCreated() {
		return created;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
