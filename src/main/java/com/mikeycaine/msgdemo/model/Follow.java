package com.mikeycaine.msgdemo.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class Follow {
    @EmbeddedId
    FollowKey id;
 
    @ManyToOne
    @MapsId("FOLLOWER_ID")
    @JoinColumn(name = "FOLLOWER_ID")
    User follower;
 
    @ManyToOne
    @MapsId("FOLLOWEE_ID")
    @JoinColumn(name = "FOLLOWEE_ID")
    User followee;

	public FollowKey getId() {
		return id;
	}

	public void setId(FollowKey id) {
		this.id = id;
	}

	public User getFollower() {
		return follower;
	}

	public void setFollower(User follower) {
		this.follower = follower;
	}

	public User getFollowee() {
		return followee;
	}

	public void setFollowee(User followee) {
		this.followee = followee;
	}
}
