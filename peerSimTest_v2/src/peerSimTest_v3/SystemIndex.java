package peerSimTest_v3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

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
 * 
 * @author dcs
 **/

public class SystemIndex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Hashtable<BF, SystemNode> listNode;
	
	/**
	 * Initialiser le système avec 
	 * <ul>
	 * 	<li> indexName
	 * 	<li> serverID
 	 *	<li> gamma
 	 *	<li> listNode
	 * </ul>
	 * 
	 * @author dcs
	 * */
	
	public SystemIndex(String indexName, int serverID) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		listNode = new Hashtable<BF, SystemNode>();
		listNode.put(new BF(), new SystemNode(new BF(), Config.gamma));
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	/**
	 * Ajouter un filtre dans le nœud identifié par 'path'
	 * 
	 * @param bf
	 * @param path
	 *	
	 * @return
	 * <ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	<li> soit {@code Hashtable<String, HashSet<BF>>}
	 * </ul>
	 * 
	 * @author dcs
	 * */
	
	@SuppressWarnings("unchecked")
	public void add(BF bf)
	{
		try
		{
			SystemNode n =  (SystemNode)listNode.get(new BF());
			Object o = n.add(bf);
			
			while (o != null)
			{
				if (o.getClass().getName().contains("BF"))
				{
					n = (SystemNode)listNode.get((BF)o);
					o = n.add(bf);
				}
				else // ArrayList<BF>
				{
					Iterator<BF> iterator = ((ArrayList<BF>)o).iterator();
					int rang = n.getPath().getRang(Config.sizeOfElement);
					
					while (iterator.hasNext())
					{
						BF bf_tmp = iterator.next();
						BF path = new BF(n.getPath().toString()+bf_tmp.getFragment(rang, Config.sizeOfElement));
						
						if (!this.listNode.containsKey(path))
						{
							this.listNode.put(path, new SystemNode(path, Config.gamma));
						}
						
						n = this.listNode.get(path);
						o = n.add(bf_tmp);
					}
				}
			}
		}
		catch (ErrorException e)
		{
			e.printStackTrace();
		}			
	}
		
	/**
	 * Rechercher le filtre dans le chemin précis.
	 * 
	 * @param bf
	 * @param path
	 * @return
	 * <ul>
	 * 	{@link Object[]}
	 * 	<li> {@code o[0] = HashSet<BF>}
	 * 	<li> {@code o[1] = Hashtable<Integer,Hashtable<String,BF>>}.
	 * </ul>
	 * 
	 * @author dcs
	 **/
	public Object search(BF bf, String path)
	{
		SystemNode n = (SystemNode)listNode.get(path);
		
		if (n == null)
			return null;

		return n.search(bf);
	}
	 
	/**
	 * Rechercher le filtre précise
	 * 
	 * @param bf
	 * @param path
	 * @return
	 * 	<ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	</ul>
	 * 
	 * @author dcs
	 * */
	public Object searchExact(BF bf, String path)
	{		
		SystemNode n = (SystemNode)listNode.get(path);
		
		if (n == null)
			return null;
				
		return n.searchExact(bf);
	}
	
	/**
	 * Supprimer le filtre dans le chemin précis.
	 * 
	 * @param bf
	 * @param path
	 * 
	 * @return
	 * 	<li> soit null
	 * 	<li> soit un message vers le serveur hébergé
	 * 		<ul>
				<li> type : remove(supprimer le filtre), removeNode(supprimer un nœud)
				<li> une chaîne de caractères
			</ul>
	 * 
	 * @author dcs
	 * */
	
	public Object remove(BF bf, String path)
	{
		SystemNode n = (SystemNode)listNode.get(path);

		if (n == null)
			return null;
		return n.remove(bf);
	}
	
	public int size()
	{
		return this.listNode.size();
	}
	
	public String toString()
	{
		String s = new String();
		
		Enumeration<SystemNode> e = listNode.elements();
		
		while(e.hasMoreElements())
			s += (e.nextElement()).toString() + "\n";
		
		return s;
	}
}
