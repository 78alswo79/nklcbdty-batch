package com.nklcbdty.batch.nklcbdty.batch.crawler.common;

public class ApiException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 60930602035586634L;
	
	public ApiException(String message) {
        super(message);
    }
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
