package peerSimTest_v3;

import java.io.Serializable;
import java.util.ArrayList;

public class SystemNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BF path;
	private boolean ok = true;
	private ArrayList<BF> listKey = new ArrayList<BF>();
	private int limit;
	
	/**
	 * Initiliser un nœud avec le server hébergé, l'identifiant sous forme une chaîne de caractères, le rang et la limite
	 * 	une talbe de routage.
	 * 
	 * @param server
	 * @param path
	 * @param limit
	 * 
	 * @author dcs
	 * */
	
	public SystemNode(BF path, int limit)
	{
		this.path = path;
		this.limit = limit;
	}
	
	/**
	 * Rendre l'identifiant du nœud.
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public BF getPath()
	{
		return this.path;
	}
	
	/**
	 * Rendre la limite (pour le conteneur local) stockée dans le nœud.
	 * 
	 * @return {@link int}
	 * @author dcs
	 * */
	
	public int getLimit()
	{
		return this.limit;
	}
	
	/** Ajouter le filtre dans le nœud.
	 * 
	 * @param key
	 * @throws ErrorException
	 * @return 
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link BF}
	 * 	<li> soit {@code ArrayList<BF>}.
	 * 
	 * @author dcs
	 * */
	
	public Object add(BF key)
	{	
		try
		{
			if (this.path.getRang(Config.sizeOfElement) == (Config.numberOfBits*Config.numberOfFragment - 1))
			{
				this.listKey.add(key);
				return null;
			}
			
			if (this.listKey.size() < this.limit && ok)
			{
				this.listKey.add(key);
				return null;
			}
			else if (ok)
			{
				ok = false;
				this.listKey.add(key);
				
				ArrayList<BF> res = this.listKey;
				this.listKey = new ArrayList<BF>();
				return res;
			}
			
			if (this.path.getRang(Config.sizeOfElement) == -1)
			{
				return new BF(key.getFragment(0, Config.sizeOfElement).toString());				
			}
			
			String s = this.path.toString() 
					+ (key.getFragment(this.path.getRang(Config.sizeOfElement) + 1, Config.sizeOfElement));
					
			return new BF(s);
		}
		catch (ErrorException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retourne le nombre d'éléments dans le nœud.
	 * 
	 * @return int
	 * @author dcs
	 * */

	public int size()
	{
		return this.listKey.size();
	}
	
	/**
	 * Rechercher tous les filtres qui contiennent le filtre(sous-filtre).
	 * 
	 * @param key
	 * @return
	 * 
	 * 	{@link Object[]}
	 * 	<li> {@code o[0] = HashSet<BFP2P>}
	 * 	<li> {@code o[1] = Hashtable<Integer,Hashtable<String,BFP2P>>}.
	 * 
	 * @author dcs
	 * */
	
	public Object search(BF key)
	{
		
		return null;
	}
	
	/**
	 * Rechercher le filtre précis.
	 * 
	 * @param bf
	 * @return 
	 * 	<li> {@link null}
	 * 	<li> {@link Message}
	 * 
	 * @author dcs
	 * */
	
	public Object searchExact(BF bf)
	{
		
		
		return null;
	}
	
	/**
	 * Supprimer le filtre dans le nœud
	 * <p>
	 * Retourner soit vide, soit une chaîne de caractères, soit une table de routage
	 * */
	
	public Object remove(BF bf)
	{
		
		
		return null;
	}

	public String toString()
	{
		return null;
	}
}







