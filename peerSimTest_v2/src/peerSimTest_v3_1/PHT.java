package peerSimTest_v3_1;

import java.io.Serializable;
import java.util.ArrayList;
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
 * 
 * @author dcs
 **/

public class PHT implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Hashtable<String, PHT_Node> listNodes;
	
	public PHT(String indexName) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		listNodes = new Hashtable<String, PHT_Node>();
		listNodes.put("/", new PHT_Node("/"));
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public Hashtable<String, PHT_Node> getListNodes()
	{	
		return this.listNodes;
	}
	
	private boolean containsNode(String path)
	{
		return this.listNodes.containsKey(path);
	}
	
	private boolean getNodeStatus(PHT_Node systemNode)
	{
		return systemNode.isLeafNode();
	}
	
	/**
	 * Recherche l'identifiant du nœud correspondant.
	 * 
	 * @return {@link String}
	 * 	
	 * @author dcs
	 * */
	
	private String lookup(BF key) throws ErrorException
	{
		String path = "/";
		
		while (true)
		{
			PHT_Node n = this.listNodes.get(path);

			if (n.getPath() == "/")
			{
				if (n.isLeafNode())
				{
			//		System.out.println(path + " " + n.getPath());
					return path;
				}
				
				path = key.getFragment(0, Config.sizeOfElement).toString();
			}
			else
			{				
				BF bf_tmp = new BF(n.getPath());
				System.out.println("-------IN--------");
				if (key.equals(bf_tmp))
				{					
					if (n.isLeafNode())
					{
						System.out.println(path + " " + n.getPath());
						return path;
					}
					else
					{
						path += key.getFragment(n.getRang() + 1, Config.sizeOfElement);
						System.out.println("aaaaaaaaaaaaaaaa " + path);
					}
				}
				else
				{
					System.out.println("qqqq " + "0001" + "0101" + "1101" + "1000");
					System.out.println("ssss " + path + " " + n.getPath());
					
					int rang = n.getRang();
					char[] tmp = key.getSubFilter(0, rang*Config.sizeOfElement).toString().toCharArray();
					int i = 0;
					for (i = rang;i >= 0; i--)
					{
						if (tmp[i] != '0')
							break;
					}
					System.out.println(rang + " " + i);
					path = key.getSubFilter(0, i).toString();
					System.out.println(path);
					
					while (!this.listNodes.containsKey(path))
					{
						path = path.substring(0, path.length() - 1);
					}
					System.out.println("zzzz " + path);
				}
				System.out.println("-------OUT--------");
			}
		}
	}
	
	public void insert(BF key) throws ErrorException
	{				
		String path = this.lookup(key);
		
		PHT_Node systemNode = this.listNodes.get(path);
		systemNode.insert(key);
		
		if (systemNode.size() > Config.gamma)
		{
			systemNode.setLeafNode(false);
			split(systemNode);
		}
	}
	
	private void split(PHT_Node n) throws ErrorException
	{		
		ArrayList<BF> listKeys = n.getListKeys();
		n.setListKey(null);
		
		if (n.getPath() == "/")
		{
			PHT_Node new0 = new PHT_Node("0");
			PHT_Node new1 = new PHT_Node("1");
			
			this.listNodes.put("0", new0);
			this.listNodes.put("1", new1);
		}
		else
		{
			String s_tmp = "0";
			
			String path = n.getPath();
			
			if (path.length() > 1)
			{
				char[] tmp = path.toCharArray();
				
				int i = 0;
				for (i = tmp.length - 1; i >= 0; i--)
				{
					if (tmp[i] != '0')
						break;
				}
				
				if (i != -1)
					s_tmp = path.substring(0, i + 1);
			}

			PHT_Node new0 = new PHT_Node(path + "0");
			PHT_Node new1 = new PHT_Node(path + "1");
			
			this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new0);
			this.listNodes.put(path + "1", new1);
		}
		
		for (int j = 0; j < listKeys.size(); j++)
			this.insert(listKeys.get(j));
		
	}

	public Object search(BF key) throws ErrorException
	{
		return null;
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
		return null;
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
	
	public Object remove(BF key)
	{
		return null;
	}
	
	public int size()
	{
		return this.listNodes.size();
	}
	
	public String toString()
	{
		return null;
	}
}
