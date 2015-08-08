package peerSimTest_v2;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * SystèmeIndexP2P gère les nœuds
 * <p>
 * Variable locale : 
 * <ul>
 * 	<li> indexName
 * 	<li> serverID
 * 	<li> listeNode
 * </ul>
 **/

public class SystemIndexP2P implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String indexName;
	private int serverID;
	private Hashtable<String, SystemNodeP2P> listNode;
	
	/**
	 * Initialiser le système avec 
	 * <ul>
	 * 	<li> indexName
	 * 	<li> serverID
 	 *	<li> gamma
 	 *	<li> listNode
	 * </ul>
	 * */
	
	public SystemIndexP2P(String indexName, int serverID) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		this.serverID = serverID;
		listNode = new Hashtable<String, SystemNodeP2P>();
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	/**
	 * Créer le nœud root "/"
	 * */
	
	public void createRoot()
	{
		listNode.put("/", new SystemNodeP2P(serverID, "/", Config.gamma));
	}
	
	/**
	 * Ajouter un filtre dans le nœud identifié par 'path'
	 * 
	 * <p>	
	 * Retourner : 
	 * <ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	<li> soit {@code Hashtable<String, HashSet<BFP2P>>}.
	 * </ul>
	 * </p>
	 * */
	
	public Object add(BFP2P bf, String path)
	{
		SystemNodeP2P n =  (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
		{
			n = new SystemNodeP2P(serverID, path, Config.gamma);
			try {
				n.add(bf);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.listNode.put(path, n);
			return null;
		}
		
		Object o;
		try {
			o = n.add(bf);
			
			return o;
		} 
		catch (ErrorException e)
		{
			e.printStackTrace();
		}
	
		return null;
	}
	
	/**
	 * Ajouter un nœud dans le système
	 * */
	
	public void addSystemNodeP2P(String path, SystemNodeP2P node)
	{
		if (!this.listNode.containsKey(path))
			this.listNode.put(path, node);
	}
	
	/**
	 * Ajout nodeID à localRoute du nœud
	 * 
	 * */
	
	public void addPathNodeID(String path_father, String path, int nodeID)
	{
		this.listNode.get(path_father).add(path, nodeID);
	}
		
	/**
	 * Rechercher le filtre dans le chemin précis
	 *  <p>
	 * Retourner : 
	 * <ul>
	 * 	{@link Object[]}
	 * 	<li> {@code o[0] = HashSet<BFP2P>}
	 * 	<li> {@code o[1] = Hashtable<Integer,Hashtable<String,BFP2P>>}.
	 * </ul>
	 * </p>
	 **/
	public Object search(BFP2P bf, String path)
	{
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
			return null;

		return n.search(bf);
	}
	 
	/**
	 * Rechercher le filtre précise
	 * <p>
	 * Retourner 
	 * 	<ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	</ul>
	 * </p>
	 * */
	public Object searchExact(BFP2P bf, String path)
	{		
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
			return null;
				
		return n.searchExact(bf);
	}
	
	/**
	 * Supprimer le filtre dans le chemin précis
	 * <p>
	 * Retourner soit null, soit un message vers le serveur hébergé
	 * il y a 2 type de message : remove(supprimer le filtre) et removeNode(supprimer un nœud)
	 * 	contient une chaîne de caractères
	 * */
	
	public Object remove(BFP2P bf, String path)
	{
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
			return null;
		return n.remove(bf);
	}
	
	public int size()
	{
		return this.listNode.size();
	}
	
	/*
	 * Rendre une liste des nœuds stockés dans le système
	 * */
	
	public Hashtable<String, SystemNodeP2P> getListNode()
	{
		return this.listNode;
	}
	
	public String toString()
	{
		String s = new String();
		
		Enumeration<SystemNodeP2P> e = listNode.elements();
		
		while(e.hasMoreElements())
			s += (e.nextElement()).toString() + "\n";
		
		return s;
	}
}
