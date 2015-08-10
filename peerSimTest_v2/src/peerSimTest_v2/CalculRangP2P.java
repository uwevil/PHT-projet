package peerSimTest_v2;

public class CalculRangP2P {
	
	/**
	 * Calcul le rang d'un nœud à partir de son chemin d'identifiant.
	 * 
	 * @author dcs
	 * */
	public CalculRangP2P() 
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Retourne le rang d'un nœud à partir de son chemin d'identifiant.
	 * 
	 * @return int
	 * @author dcs
	 * */
	public int getRang(String path)
	{
		if (path == "/")
			return 0;
		
		String[] tmp = path.split("/");
		return tmp.length - 1;
	}

}
