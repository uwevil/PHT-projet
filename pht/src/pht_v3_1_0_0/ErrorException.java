package pht_v3_1_0_0;

public class ErrorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Affiche l'erreur dans la sortie standard.
	 * 
	 * @param message
	 * @author dcs
	 * */
	public ErrorException(String message)
	{
		System.err.println(message);
	}
}
