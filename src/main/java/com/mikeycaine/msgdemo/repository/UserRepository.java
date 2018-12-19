package com.mikeycaine.msgdemo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.mikeycaine.msgdemo.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
	public Optional<User> findByName(String name);
}