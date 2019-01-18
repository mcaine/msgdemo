package com.mikeycaine.msgdemo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FollowKey implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "FOLLOWER_ID")
    Long followerId;
 
    @Column(name = "FOLLOWEE_ID")
    Long followeeId;
    
    public FollowKey() {
    }
    
    public FollowKey(long followerId, long followeeId) {
    	this.followerId = followerId;
    	this.followeeId = followeeId;
    }

	public Long getFollowerId() {
		return followerId;
	}

	public void setFollowerId(Long followerId) {
		this.followerId = followerId;
	}

	public Long getFolloweeId() {
		return followeeId;
	}

	public void setFolloweeId(Long followeeId) {
		this.followeeId = followeeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((followeeId == null) ? 0 : followeeId.hashCode());
		result = prime * result + ((followerId == null) ? 0 : followerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FollowKey other = (FollowKey) obj;
		if (followeeId == null) {
			if (other.followeeId != null)
				return false;
		} else if (!followeeId.equals(other.followeeId))
			return false;
		if (followerId == null) {
			if (other.followerId != null)
				return false;
		} else if (!followerId.equals(other.followerId))
			return false;
		return true;
	}
}
