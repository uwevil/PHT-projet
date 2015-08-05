package peerSimTest_v2;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class SystemNodeP2P implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int server;
	private String path;
	private int rang;
	private Hashtable<String, String> localRoute;
	private HashSet<BFP2P> containerLocal;
	private int limit;
	
	/*
	 * Initiliser un nœud avec le server hébergé, l'identifiant sous forme une chaîne de caractères, le rang et la limite
	 * 	une talbe de routage
	 * */
	
	public SystemNodeP2P(int server, String path, int rang, int limit)
	{
		this.server = server;
		this.path = path;
		this.limit = limit;
		this.rang = rang;
		localRoute = new Hashtable<String, String>();
		containerLocal = new HashSet<BFP2P>();
	}
	
	/*
	 * Rendre l'identifiant de ce nœud
	 * */
	
	public String getPath()
	{
		return this.path;
	}
	
	/*
	 * Rendre le server hébergé
	 * */
	
	public int getServer()
	{
		return this.server;
	}
	
	/*
	 * Rendre le rang de ce nœud
	 * */
	
	public int getRang()
	{
		return this.rang;
	}
	
	/*
	 * Rendre la table de routage de ce nœud
	 * */
	
	public Hashtable<String, String> getLocalRoute()
	{
		return this.localRoute;
	}
	
	/*
	 * Rendre le conteneur local
	 * */
	
	public HashSet<BFP2P> getContainerLocal()
	{
		return this.containerLocal;
	}
	
	/*
	 * Rendre la limite(pour le conteneur local) stockée dans ce nœud
	 * */
	
	public int getLimit()
	{
		return this.limit;
	}
	
	/*
	 * Ajouter le filtre dans le nœud
	 * 
	 * Retourner null si réussit, sinon soit une chaîne de caractères soit un conteneur local
	 * */
	
	public Object add(BFP2P bf) throws ErrorException
	{	
		if (this.containerLocal.size() < this.limit)
		{
			this.containerLocal.add(bf);
			return null;
		}
		
		this.containerLocal.add(bf);
		
		Hashtable<String, HashSet<BFP2P>> htshsbf = new Hashtable<String, HashSet<BFP2P>>();
		
		Iterator<BFP2P> iterator = this.containerLocal.iterator();
		while(iterator.hasNext())
		{
			BFP2P bf_tmp = iterator.next();
			BFToPath bfToPath = new BFToPath(bf_tmp, Config.sizeOfFragment);
			LongestZero longestZero = new LongestZero(bf_tmp, Config.sizeOfFragment);
			
			int longestLength = longestZero.getLongestLength();
			String longestPrefix ;
			
			if (longestLength != 0)
			{
				longestPrefix = longestZero.getLongestPrefix();
			}
			else //longestLength == 0
			{
				longestPrefix = "/" + bf_tmp.getFragment(rang).toInt();
			}
			
			if (htshsbf.containsKey(longestPrefix))
			{
				BFP2P bf_tmp2 = (new PathToBF(bfToPath.split(longestLength, Config.numberOfFragment), 
								Config.sizeOfFragment)).convert();
				htshsbf.get(longestPrefix).add(bf_tmp2);
			}
			else // !htshsbf.containsKey(longestPrefix)
			{
				BFP2P bf_tmp2 = (new PathToBF(bfToPath.split(longestLength, Config.numberOfFragment), 
						Config.sizeOfFragment)).convert();
				HashSet<BFP2P> hsbf = new HashSet<BFP2P>();
				hsbf.add(bf_tmp2);
				htshsbf.put(longestPrefix, hsbf);
			}
		}
		
		return null;
	}
	
	/*
	 * Ajouter une chaîne de caractère à l'entrée correspondante au filtre 'bf'
	 * */
	
	public void add(BFP2P bf, String path)
	{

	}
	
	/*
	 * Rechercher tous les filtres qui contiennent le filtre de la requete 'bf'
	 * 
	 * Retourner une liste mélangée de filtres et de chaîne de caractères(chemin)
	 * */
	
	public Object search(BFP2P bf)
	{
		
		
		return null;
	}
	
	/*
	 * Rechercher le filtre précise
	 * 
	 * Retourner soit vide soit une chaîne de caractère soit un conteneur local
	 * */
	
	public Object searchExact(BFP2P bf)
	{
		
		return null;
	}
	
	/*
	 * Supprimer le filtre dans le nœud
	 * 
	 * Retourner soit vide, soit une chaîne de caractères, soit une table de routage
	 * */
	
	public Object remove(BFP2P bf)
	{
		return null;
	}
	
	/*
	 * Supprimer l'entrée 'f' dans la table de routage
	 * 
	 * Retourner true si réussit, false sinon
	 * */
	
	public boolean remove(FragmentP2P f)
	{
		return true;

	}

	public String toString()
	{
		return null;
	}
}







