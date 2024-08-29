package com.manhnt.object;

public class Result {

	private int statusCode;
	private boolean isSuccess;
	private String message;
	private String contentMessage;
	
	public Result(int statusCode, boolean isSuccess, String message, String contentMessage){
		this.statusCode = statusCode;
		this.isSuccess = isSuccess;
		this.message = message;
		this.contentMessage = contentMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}

	public String getMessage() {
		return message;
	}

	public String getContentMessage() {
		return contentMessage;
	}

}
