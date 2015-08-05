package peerSimTest_v2;

public class ErrorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorException(String message)
	{
		System.err.println(message);
	}
}
