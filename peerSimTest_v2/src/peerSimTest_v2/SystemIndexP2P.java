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
		{
			this.listNode.put(path, node);
		}
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
		
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(bf.toString());
		//****************Compteur un nœud visité************
		ControlerNw.search_log.get(key).addNodeVisited(1);
		//***************************************************
		
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
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(bf.toString());
		
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
			return null;
		
		ControlerNw.search_log.get(key).addNodeMatched(path);
		
		return n.searchExact(bf);
	}
	
	/*
	 * Supprimer le filtre dans le chemin précis
	 * 
	 * Retourner soit null, soit un message vers le serveur hébergé
	 * il y a 2 type de message : remove(supprimer le filtre) et removeNode(supprimer un nœud)
	 * 	contient une chaîne de caractères
	 * */
	
	public Object remove(BFP2P bf, String path)
	{
		/*
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		if (n == null)
			return null;
		
		Object o = n.remove(bf);
		if (o == null)
			return null;
		if (path == "/")
			return null;
		
		Message rep = new Message();
		
		while (o != null)
		{
			if (((o.getClass()).getName()).equals("java.lang.String"))
			{
				if (!listNode.containsKey((String)o))
				{
					rep.setData(o);
					rep.setOption1("remove");
					return rep;
				}
				else
				{
					n = listNode.get((String)o);
					o = n.remove(bf);
				}
			}else{ // localRoute
				String path_tmp = n.getPath();
				int rang_tmp = n.getRang();

				if (path_tmp == "/")
					return null;
				
				listNode.remove(path_tmp);

				int endIndex = path_tmp.lastIndexOf('/');
				
				if (!this.listNode.containsKey(path_tmp.substring(0, endIndex)))
				{
					rep.setData(path_tmp.substring(0, endIndex));
					rep.setOption1("removeNode");
					return rep;
				}
				
				n = (SystemNodeP2P)listNode.get(path_tmp.substring(0, endIndex));
				
				while(true)
				{
					if (n.remove(bf.getFragment(rang_tmp)))
					{
						return null;
					}
					else
					{
						path_tmp = n.getPath();
						rang_tmp = n.getRang();

						if (path_tmp == "/")
							return null;
						
						listNode.remove(path);

						endIndex = path.lastIndexOf('/');
						
						if (!this.listNode.containsKey(path_tmp.substring(0, endIndex)))
						{
							rep.setData(path_tmp.substring(0, endIndex));
							rep.setOption1("removeNode");
							return rep;
						}
						
						n = (SystemNodeP2P)listNode.get(path.substring(0, endIndex));
					}
				}
			}
		}
		*/
		return null;
	}
	
	/*
	 * Supprimer le nœud précis dans le système
	 * 
	 * Retourner soit null, soit une chaîne de caractères
	 * */
	
	public String removeNode(FragmentP2P f, String path)
	{
		String path_tmp = path;
		
		while (true)
		{
			SystemNodeP2P n = (SystemNodeP2P)listNode.get(path_tmp);
			if (n == null)
				return null;
			
			if (n.remove(f))
			{
				return null;
			}
				
			int endIndex = path_tmp.lastIndexOf('/');	

			path_tmp = path_tmp.substring(0, endIndex);
					
			if (path_tmp == "/")
				return null;
			
			if (!this.listNode.containsKey(path_tmp))
			{
				return path_tmp;
			}
		}
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
