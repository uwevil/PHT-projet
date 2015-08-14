package peerSimTest_v3_1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SystemNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private boolean isLeafNode = true;
	private ArrayList<BF> listKey;
	private ArrayList<String> childNodes;
	private int limit;
	private int count = 0;
	
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
	
	public SystemNode(String path, int limit)
	{
		this.path = path;
		this.limit = limit;
		listKey  = new ArrayList<BF>();
		childNodes = new ArrayList<String>();
	}
	
	/**
	 * Rendre l'identifiant du nœud.
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public String getPath()
	{
		return this.path;
	}
	
	public int getRang() throws ErrorException
	{
		if (this.path == "/")
			return -1;
		
		return this.path.length() / Config.sizeOfElement;
	}
	
	public boolean isLeafNode()
	{
		return this.isLeafNode;
	}
	
	public ArrayList<BF> getListKey()
	{
		return this.listKey;
	}
	
	public ArrayList<String> getChildNodes()
	{
		return this.childNodes;
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
	 * 	<li> soit {@link String}
	 * 	<li> soit {@code ArrayList<String>}.
	 * 
	 * @author dcs
	 * */
	
	public Object insert(BF key) throws ErrorException
	{	
		if (this.getRang() == Config.numberOfFragment*Config.numberOfBits/Config.sizeOfElement - 1)
		{
			count++;
			return null;
		}
		
		if (isLeafNode && this.listKey.size() < this.limit )
		{
			if (!this.listKey.contains(key))
				this.listKey.add(key);
			
			return null;
		}
		else if (isLeafNode) // this.listKey.size() == this.limit && ok == true
		{
			isLeafNode = false;
			ArrayList<BF> res = this.listKey;
			res.add(key);
			
			this.listKey = null;

			return res;
		}
		
		Iterator<String> iterator = this.childNodes.iterator();
		
		while (iterator.hasNext())
		{			
			String s = iterator.next();
			BF bf_tmp = new BF(s);
			
			if (key.equals(bf_tmp))
				return s;
		}
		
		String path;
		if (this.getRang() == -1) // racine
		{
			path = key.getSubFilter(0, Config.sizeOfElement - 1).toString();
		}
		else
		{
			path = this.path + key.getFragment(this.getRang(), Config.sizeOfElement).toString();
		}

		this.addPathToChildNodes(path);
		return path;
	}
	
	/**
	 * Ajout un chemin dans la liste de route.
	 * 
	 * @param path
	 * @author dcs
	 * */
	
	public void addPathToChildNodes(String path)
	{
		if (this.childNodes.contains(path))
			return;
		
		this.childNodes.add(path);
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
	 * Retourne le nombre de clés stockés si ce nœud est une feuille terminant du chemin.
	 * 
	 * @return int
	 * @author dcs
	 * */
	
	public int getCount()
	{
		return this.count;
	}
	
	/**
	 * Rechercher tous les filtres qui contiennent le filtre(sous-filtre).
	 * 
	 * @param key
	 * @return
	 * <li> {@link null}
	 * <li> {@link BF}
	 * <li> {@code ArrayList<String>}
	 * <li> {@code ArrayList<BF>}
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	public Object search(BF key) throws ErrorException
	{
		if (this.getRang() == Config.numberOfFragment*Config.numberOfBits/Config.sizeOfElement - 1)
		{
			BF res = new BF(this.path);
			if (key.in(res))
				return res;
			return null;
		}
		
		if (isLeafNode)
		{
			ArrayList<BF> res = new ArrayList<BF>();
			
			Iterator<BF> iterator = this.listKey.iterator();
			
			while (iterator.hasNext())
			{
				BF bf_tmp = iterator.next();
				if (key.in(bf_tmp))
					res.add(bf_tmp);
			}
			return res;
		}
		
		ArrayList<String> res = new ArrayList<String>();
		
		Iterator<String> iterator = this.childNodes.iterator();
		
		while (iterator.hasNext())
		{
			String path_tmp = iterator.next();
			BF bf_tmp = new BF(path_tmp);

			if (key.in(bf_tmp))
				res.add(path_tmp);
		}
		return res;
	}
	
	/**
	 * Rechercher le filtre précis.
	 * 
	 * @param bf
	 * @return 
	 * 	<li> {@link null}
	 * 	<li> {@link String}
	 * 	<li> {@link BF}
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	public Object searchExact(BF key) throws ErrorException
	{
		if (this.getRang() == Config.numberOfFragment*Config.numberOfBits/Config.sizeOfElement - 1)
		{
			BF res = new BF(this.path);
			if (key.equals(res))
				return res;
			return null;
		}
		
		if (isLeafNode)
		{
			Iterator<BF> iterator = this.listKey.iterator();
			
			while (iterator.hasNext())
			{
				BF bf_tmp = iterator.next();
				if (key.equals(bf_tmp))
					return bf_tmp;
			}
			return null;
		}
		
		Iterator<String> iterator = this.childNodes.iterator();
		while (iterator.hasNext())
		{
			String path_tmp = iterator.next();
			BF bf_tmp = new BF(path_tmp);
			if (key.equals(bf_tmp))
				return path_tmp;
		}
		
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







