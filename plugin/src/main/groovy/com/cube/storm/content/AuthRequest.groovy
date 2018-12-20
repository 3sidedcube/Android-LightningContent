package com.cube.storm.content

class AuthRequest {
	String username = ""
	String password = ""

	public AuthRequest(String username, String password) {
		this.username = username
		this.password = password
	}
}
