package org.saadahmed.snowcrystal;


/**
 *
 * @author Saad Ahmed
 */
public class SnowCrystalException extends RuntimeException {

	private static final long version = 1L;

	private Throwable cause;


	public SnowCrystalException(String message) {
		super(message);
	}

	public SnowCrystalException(Throwable cause) {
		super(cause.getMessage());
		this.cause = cause;
	}


	public Throwable getCause() {
		return this.cause;
	}

}
