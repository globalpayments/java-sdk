package com.example.restservice.entities.accessToken;

public class AccessTokenInput {

	private String appId;
	private String appKey;
	private String[] permissions;

	public AccessTokenInput() {

	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}

}