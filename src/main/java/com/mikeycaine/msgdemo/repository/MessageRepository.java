package com.mikeycaine.msgdemo.repository;

import org.springframework.data.repository.CrudRepository;

import com.mikeycaine.msgdemo.model.Message;

public interface MessageRepository extends CrudRepository<Message, Long> {}
