package com.lawencon.leaf.community.pojo.profile.social.media;

import com.lawencon.leaf.community.pojo.file.PojoFileRes;
import com.lawencon.leaf.community.pojo.social.media.PojoSocialMediaRes;

public class PojoProfileSocialMediaRes {
	private String id;
	private Integer ver;
	private String socialMediaId;
	private String profileId;
	private String username;
	private PojoFileRes file;
	private String profileLink;
	private PojoSocialMediaRes socialMedia;
	
	

	public PojoSocialMediaRes getSocialMedia() {
		return socialMedia;
	}
	public void setSocialMedia(PojoSocialMediaRes socialMedia) {
		this.socialMedia = socialMedia;
	}
	public PojoFileRes getFile() {
		return file;
	}
	public void setFile(PojoFileRes file) {
		this.file = file;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getVer() {
		return ver;
	}
	public void setVer(Integer ver) {
		this.ver = ver;
	}
	public String getSocialMediaId() {
		return socialMediaId;
	}
	public void setSocialMediaId(String socialMediaId) {
		this.socialMediaId = socialMediaId;
	}
	public String getProfileId() {
		return profileId;
	}
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getProfileLink() {
		return profileLink;
	}
	public void setProfileLink(String profileLink) {
		this.profileLink = profileLink;
	}

	
	
}
