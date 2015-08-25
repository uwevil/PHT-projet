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
	private Hashtable<String, SystemNode> listNode;
	
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
		listNode = new Hashtable<String, SystemNode>();
		listNode.put("/", new SystemNode("/", Config.gamma));
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public ArrayList<SystemNode> getListNode() throws ErrorException
	{
		ArrayList<SystemNode> res = new ArrayList<SystemNode>();
		
		Enumeration<String> enumeration = this.listNode.keys();
	
		while (enumeration.hasMoreElements())
		{
			res.add(this.listNode.get(enumeration.nextElement()));
		}
		
		return res;
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
	 * @throws ErrorException 
	 * */
	
	@SuppressWarnings("unchecked")
	public void add(BF key) throws ErrorException
	{				
		SystemNode n = this.listNode.get("/");
		Object o = n.insert(key);
		
		while (o != null)
		{
			if (o.getClass().getName().contains("String"))
			{
				if (!this.listNode.containsKey(o))
				{
					SystemNode systemNode = new SystemNode((String) o, Config.gamma);
					systemNode.insert(key);
									
					this.listNode.put((String) o, systemNode);
					return;
				}
				else // this.listNode.containsKey(o)
				{
					n = this.listNode.get(o);
					o = n.insert(key);
				}
			}
			else // o == ArrayList<BF>
			{
				Iterator<BF> iterator = ((ArrayList<BF>) o).iterator();
				
				while (iterator.hasNext())
				{
					BF bf_tmp = iterator.next();
					
					String path;
					
					if (n.getRang() == -1)
					{
						path = bf_tmp.getFragment(0, Config.sizeOfElement).toString();
					}
					else
					{
						path = n.getPath() + bf_tmp.getFragment(n.getRang(), Config.sizeOfElement).toString();
					}

					if (!this.listNode.containsKey(path))
					{
						SystemNode systemNode = new SystemNode(path, Config.gamma);
						this.listNode.put(path, systemNode);
				
						n.addPathToChildNodes(path);
					}
					
					this.add(bf_tmp);
				}
				return;
			}
		}
	}
		
	/**
	 * Rechercher le filtre dans le chemin précis.
	 * 
	 * @param bf
	 * @param path
	 * @return
	 * 	<li> {@code null}
	 * 	<li> {@link BF}
	 * 	<li> {@code ArrayList<BF>}
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 **/
	@SuppressWarnings("unchecked")
	public Object search(BF key) throws ErrorException
	{
		SystemNode n = this.listNode.get("/");
		Object o = n.search(key);
		
		if (o == null)
			return null;
		
		if (o.getClass().getName().contains("BF"))
			return (BF) o;
		
		ArrayList<Object> arrayList = (ArrayList<Object>) o;
		ArrayList<BF> res = new ArrayList<BF>();
		
		if (arrayList.size() == 0)
			return null;
		
		int i = 0;
		while (i < arrayList.size())
		{
			Object o_tmp = arrayList.get(i);
			
			if (o_tmp.getClass().getName().equals("java.lang.String"))
			{
				n = this.listNode.get(o_tmp);
				Object o_tmp2 = n.search(key);
				
				if (o_tmp2 != null)
				{
					if (o_tmp2.getClass().getName().contains("BF"))
					{	
						res.add((BF) o_tmp2);
					}
					else
					{
						arrayList.addAll((ArrayList<Object>) o_tmp2);
					}
				}
			}
			else // BF
			{
				res.add((BF) o_tmp);
			}
			i++;
		}
		
		return res;
	}
	 
	/**
	 * Rechercher le filtre précise
	 * 
	 * @param bf
	 * @param path
	 * @return
	 * 	<ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link BF}
	 * 	</ul>
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	public Object searchExact(BF key) throws ErrorException
	{		
		SystemNode n = (SystemNode)this.listNode.get("/");
		Object o = n.searchExact(key);
		
		if (o == null)
			return null;
				
		if (o.getClass().getName().contains("BF"))
			return o;
		
		while (true)
		{
			n = this.listNode.get((String)o);
			o = n.searchExact(key);
			
			if (o == null)
				return null;
					
			if (o.getClass().getName().contains("BF"))
				return o;
		}
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
