package peerSimTest_v2;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import peerSimTest_v1.Config;

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
	
	
	public SystemNodeP2P(int server, String path, int rang, int limit) {
		// TODO Auto-generated constructor stub
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
	
	public ArrayList<BFP2P> getContainerLocal()
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
	
	public Object add(BFP2P bf)
	{	
		int long0 = 0;
		String prefix0 = new String();
		
		for (int i = 0; i < Config.numberOfFragment - rang; i++)
		{
			if (bf.getFragment(i + rang).toInt() != 0)
				break;
			
			long0++;
			prefix0 += "/" + bf.getFragment(i + rang);
		}
		
		if (long0 == 0)
		{
			prefix0 = "/" + bf.getFragment(rang);
			if (localRoute.containsKey(prefix0))
			{
				//send
			}
			else
				
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
		
		
		return rep;
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
		
	}
	
	/*
	 * Supprimer l'entrée 'f' dans la table de routage
	 * 
	 * Retourner true si réussit, false sinon
	 * */
	
	public boolean remove(FragmentP2P f)
	{
		

	}

	public String toString()
	{
		
	}
}







