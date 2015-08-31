package pht_v3_1_3;

public class ErrorException_Central extends Exception {
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
	public ErrorException_Central(String message)
	{
		System.err.println(message);
	}
}
