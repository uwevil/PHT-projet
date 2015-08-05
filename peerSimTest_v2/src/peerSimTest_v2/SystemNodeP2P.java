package peerSimTest_v2;

import java.io.Serializable;
import java.util.Enumeration;
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
	private Hashtable<Object, Integer> localRoute;
	private HashSet<BFP2P> containerLocal;
	private int limit;
	
	/**
	 * Initiliser un nœud avec le server hébergé, l'identifiant sous forme une chaîne de caractères, le rang et la limite
	 * 	une talbe de routage
	 * */
	
	public SystemNodeP2P(int server, String path, int limit)
	{
		this.server = server;
		this.path = path;
		this.limit = limit;
		localRoute = new Hashtable<Object, Integer>();
		containerLocal = new HashSet<BFP2P>();
	}
	
	/**
	 * Rendre l'identifiant de ce nœud
	 * */
	
	public String getPath()
	{
		return this.path;
	}
	
	/**
	 * Rendre le server hébergé
	 * */
	
	public int getServer()
	{
		return this.server;
	}
		
	/**
	 * Rendre la table de routage de ce nœud
	 * */
	
	public Hashtable<Object, Integer> getLocalRoute()
	{
		return this.localRoute;
	}
	
	/**
	 * Rendre le conteneur local
	 * */
	
	public HashSet<BFP2P> getContainerLocal()
	{
		return this.containerLocal;
	}
	
	/**
	 * Rendre la limite(pour le conteneur local) stockée dans ce nœud
	 * */
	
	public int getLimit()
	{
		return this.limit;
	}
	
	/** Ajouter le filtre dans le nœud.
	 * 	Retourner : soit un message, soit Hashtable< String, HashSet< BFP2P>>.
	 * */
	public Object add(BFP2P bf) throws ErrorException
	{	
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		String longestPrefix ;
				
		if (longestLength != 0)
		{			
			if (!this.localRoute.containsKey((Object)longestLength))
			{
				if (this.containerLocal.size() < this.limit)
				{
					this.containerLocal.add(bf);
					return null;
				}
				
				this.containerLocal.add(bf);
			}
			else // this.localRoute.containsKey((Object)longestLength)
			{
				String path_tmp;
				longestPrefix = longestZero.getLongestPrefix();

				if (this.path == "/")
				{
					path_tmp = longestPrefix;
				}
				else
				{
					path_tmp = this.path + longestPrefix;
				}
				
				BFP2P bf_tmp2 = (new PathToBF(longestZero.getRemainPrefix(), Config.sizeOfFragment)).convert();
				
				Message rep = new Message();
				rep.setType("add");
				rep.setPath(path_tmp);
				rep.setData(bf_tmp2);
				rep.setSource(server);
				rep.setDestinataire(this.localRoute.get((Object)longestLength));
				
				return rep;
			}
		}
		else //longestLength == 0
		{
			longestPrefix = "/" + bf.getFragment(0, Config.sizeOfFragment).toInt();
			
			if (!this.localRoute.containsKey((Object)longestPrefix))
			{
				if (this.containerLocal.size() < this.limit)
				{
					this.containerLocal.add(bf);
					return null;
				}
				
				this.containerLocal.add(bf);
			}
			else //this.localRoute.containsKey((Object)longestPrefix)
			{
				String path_tmp;
				
				if (this.path == "/")
				{
					path_tmp = longestPrefix;
				}
				else
				{
					path_tmp = this.path + longestPrefix;
				}
				
				BFP2P bf_tmp2 = (new PathToBF(longestZero.getRemainPrefix(1), Config.sizeOfFragment)).convert();
				
				Message rep = new Message();
				rep.setType("add");
				rep.setPath(path_tmp);
				rep.setData(bf_tmp2);
				rep.setSource(server);
				rep.setDestinataire(this.localRoute.get(longestPrefix));
				
				return rep;
			}
		}
		
		Hashtable<String, HashSet<BFP2P>> htshsbf = new Hashtable<String, HashSet<BFP2P>>();
		
		Iterator<BFP2P> iterator = this.containerLocal.iterator();
		
		while(iterator.hasNext())
		{
			BFP2P bf_tmp = iterator.next();
			longestZero = new LongestZero(bf_tmp, Config.sizeOfFragment);
			
			longestLength = longestZero.getLongestLength();
			
			BFP2P bf_tmp2;
			
			if (longestLength != 0)
			{
				longestPrefix = longestZero.getLongestPrefix();
				bf_tmp2 = (new PathToBF(longestZero.getRemainPrefix(), Config.sizeOfFragment)).convert();
			}
			else //longestLength == 0
			{
				longestPrefix = "/" + bf_tmp.getFragment(0, Config.sizeOfFragment).toInt();
				bf_tmp2 = (new PathToBF(longestZero.getRemainPrefix(1), Config.sizeOfFragment)).convert();
			}
			
			String path_tmp ;
			if (this.path != "/")
			{
				path_tmp = this.path + longestPrefix;
			}
			else
			{
				path_tmp = longestPrefix;
			}
			
			if (htshsbf.containsKey(path_tmp))
			{
				htshsbf.get(path_tmp).add(bf_tmp2);
			}
			else // !htshsbf.containsKey(longestPrefix)
			{
				HashSet<BFP2P> hsbf = new HashSet<BFP2P>();
				hsbf.add(bf_tmp2);
				htshsbf.put(path_tmp, hsbf);
			}
		}
		
		this.containerLocal = new HashSet<BFP2P>();
		
		return htshsbf;
	}
	
	/**
	 * Ajouter une chaîne de caractère à l'entrée correspondante au filtre 'bf'
	 * */
	
	public void add(String path, int nodeID)
	{
		int rang = (new CalculRangP2P()).getRang(this.path);
				
		PathToBF pathToBF = new PathToBF(path, Config.sizeOfFragment);
		LongestZero longestZero = new LongestZero(pathToBF.convert(), Config.sizeOfFragment);
		
		if (rang == 0)
		{	
			if (longestZero.getLongestLength() != 0)
			{
				this.localRoute.put(longestZero.getLongestLength(), nodeID);
			}
			else // longestLength == 0
			{
				String path_tmp = pathToBF.split(rang+1, Config.sizeOfBF);
				this.localRoute.put(path_tmp, nodeID);
			}
		}
		else // rang != 0
		{
			String path_tmp = pathToBF.split(rang+1, Config.sizeOfBF);
			pathToBF = new PathToBF(path_tmp, Config.sizeOfFragment);
			longestZero = new LongestZero(pathToBF.convert(), Config.sizeOfFragment);
			
			if (longestZero.getLongestLength() != 0)
			{
				this.localRoute.put(longestZero.getLongestLength(), nodeID);
			}
			else // longestLength == 0
			{
				path_tmp = pathToBF.split(1, Config.sizeOfBF);
				this.localRoute.put(path_tmp, nodeID);
			}
		}
	}
	
	/**
	 * Rechercher tous les filtres qui contiennent le filtre de la requete 'bf'
	 * 
	 * Retourner une liste mélangée de filtres et de chaîne de caractères(chemin)
	 * */
	
	public Object search(BFP2P bf)
	{
		HashSet<Object> res = new HashSet<Object>();
		
		Iterator<BFP2P> iterator = this.containerLocal.iterator();
		
		while (iterator.hasNext())
		{
			BFP2P bf_tmp = iterator.next();
			
			if (bf.in(bf_tmp))
				res.add(bf_tmp);
		}
		
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		String longestPrefix ;
		
		if (longestLength != 0)
		{
			longestPrefix = longestZero.getLongestPrefix();
		}
		else //longestLength == 0
		{
			longestPrefix = "/" + bf.getFragment(0, Config.sizeOfFragment).toInt();
		}
		
		Hashtable<String, HashSet<BFP2P>> htshsbf = new Hashtable<String, HashSet<BFP2P>>();
		
		Enumeration<Object> enumeration = this.localRoute.keys();
		/*
		while (enumeration.hasMoreElements())
		{
			Object prefix = enumeration.nextElement();
			int rang = (new CalculRangP2P()).getRang(prefix);
			
			if (longestLength == rang)
			{
				// create message
			}
			else if (longestLength == 0 && rang == 1)
			{
				//create message
			}
		}
		*/
		Object[] o = new Object[2];
		o[0] = res;
		
		return o;
	}
	
	/**
	 * Rechercher le filtre précise
	 * 
	 * Retourner soit vide soit une chaîne de caractère soit un conteneur local
	 * */
	
	public Object searchExact(BFP2P bf)
	{
		
		return null;
	}
	
	/**
	 * Supprimer le filtre dans le nœud
	 * 
	 * Retourner soit vide, soit une chaîne de caractères, soit une table de routage
	 * */
	
	public Object remove(BFP2P bf)
	{
		return null;
	}
	
	/**
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







