package com.mikeycaine.msgdemo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mikeycaine.msgdemo.model.Follow;
import com.mikeycaine.msgdemo.model.FollowKey;

public interface FollowRepository extends CrudRepository <Follow, FollowKey> {
	List<Follow> findByFollowerId(long followerId);
}
