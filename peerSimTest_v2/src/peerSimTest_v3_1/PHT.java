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
	//		System.out.println(path);
			PHT_Node n = this.listNodes.get(path);

			if (n.getPath().equals("/"))
			{
				if (n.isLeafNode())
					return path;
				
				path = key.getFragment(0, Config.sizeOfElement).toString();
			}
			else // !n.getPath().equals("/")
			{				
				BF bf_tmp = new BF(n.getPath());
				if (key.equals(bf_tmp))
				{					
					if (n.isLeafNode())
					{
						return path;
					}
					else // !n.isLeafNode()
					{
						int i = 1;
						path += key.getFragment(n.getRang() + i++, Config.sizeOfElement);
					}
				}
				else // !key.equals(bf_tmp)
				{
					int rang = n.getRang();
			//		System.out.println("sssss" + path + " " + rang + " " + n.getPath());

					String s = new String();
					for (int i = 0; i <= rang; i++)
					{
						if (key.getBit(i) == bf_tmp.getBit(i))
							s += (key.getBit(i)) ? "1" : "0";
						
						if (key.getBit(i) == !bf_tmp.getBit(i))
						{
							s += (key.getBit(i)) ? "1" : "0";
			//				System.out.println("sssszzzzzzzzzzs" + s);

							break;
						}
					}
			//		System.out.println("ssssssszz" + s);

					path = s;
				}
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
		
		if (n.getPath().equals("/"))
		{
			PHT_Node new0 = new PHT_Node("0");
			PHT_Node new1 = new PHT_Node("1");
			
			this.listNodes.put("0", new0);
			this.listNodes.put("1", new1);
		}
		else // !n.getPath().equals("/")
		{
			String path = n.getPath();
			
			PHT_Node new0 = new PHT_Node(path + "0");
			PHT_Node new1 = new PHT_Node(path + "1");
			
			String s_tmp = this.computeEntry(path + "0");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new0);
			
			s_tmp = this.computeEntry(path + "1");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new1);
		}
		
		for (int j = 0; j < listKeys.size(); j++)
			this.insert(listKeys.get(j));
		
	}
	
	private String computeEntry(String path)
	{
		String s_tmp = new String();
		
		if (path.length() > 1)
		{
			char[] tmp = path.toCharArray();
			
			int i = 0;
			for (i = tmp.length - 1; i > 0; i--)
			{
				if (tmp[i] != tmp[i - 1])
					break;
			}
			
			if (i != -1)
				s_tmp = path.substring(0, i + 1);
		}
		else
		{
			return path;
		}
		
		return s_tmp;
	}
	
	private ArrayList<BF> generatorBF(int size) throws ErrorException
	{
		ArrayList<BF> res = new ArrayList<BF>();
		BinaryArray binaryArray = new BinaryArray(size);

		for (int i = 0; i <= size; i++)
		{		
			binaryArray.setFirstBits(i);
			do
			{
				String s = String.copyValueOf(binaryArray.next());
				res.add(new BF(s));
			} 
			while(binaryArray.hasNext());
		}

		return res;
	}
	
	private String reduce(String path)
	{
		if (path.length() <= 1)
			return path;

		char[] tmp = path.toCharArray();
		
		int i = 0;
		for (i = tmp.length - 1; i >= 0; i--)
		{
			if (tmp[i] != '0')
				break;
		}
		
		return path.substring(0, i + 1);
	}

	public Object search(BF key) throws ErrorException
	{
		int pos = 0;
		
		ArrayList<BF> res = new ArrayList<BF>();
		ArrayList<String> listPaths = new ArrayList<String>();
		
		listPaths.add("/");
		int k = 0;
		while (k < listPaths.size())
		{
			String path = listPaths.get(k);
			PHT_Node n = this.listNodes.get(path);
			
			if (path == "/")
			{
				if (n != null && n.isLeafNode())
				{
					ArrayList<BF> listKeys = n.getListKeys();
					if (listKeys != null && listKeys.size() != 0)
					{
						for (int j = 0; j < listKeys.size(); j++)
						{
							BF bf_tmp = listKeys.get(j);
							
							if (key.in(bf_tmp))
								res.add(bf_tmp);
						}

						return res;
					}
				}
				else // !n.isLeafNode
				{
					String s_tmp = key.getFragment(0, Config.sizeOfElement).toString();

					if (s_tmp.equals("0"))
					{
						listPaths.add("0");
						listPaths.add("1");
					}
					else
					{
						listPaths.add("1");
					}
					pos++;
				}
			}
			else // path != "/"
			{
				if (n != null && n.isLeafNode())
				{
					if (key.in(new BF(n.getPath())))
					{
			//			System.out.println(key.toString());
			//			System.out.println(path);
			//			System.out.println(n.getPath() + "\n");
						ArrayList<BF> listKeys = n.getListKeys();
						for (int i = 0; i < listKeys.size(); i++)
						{
							BF bf_tmp = listKeys.get(i);
							
							if (key.in(bf_tmp))
								res.add(bf_tmp);
						}
					}

					if (key.in(new BF(n.getPath() + "1")))
					{
						listPaths.add(n.getPath() + "1");
					}
				}
				else if (n != null)
				{
					if (key.in(new BF(n.getPath() + "1")))
						listPaths.add(n.getPath() + "1");
				}

				if (pos < Config.sizeOfBF/Config.sizeOfElement - 1)
				{
					String s_tmp = key.getFragment(pos, Config.sizeOfElement).toString();
					
					if (s_tmp.equals("0"))
					{
						int i = 0;
						for (i = pos + 1; i < Config.sizeOfBF/Config.sizeOfElement; i++)
						{
							if (key.getFragment(i, Config.sizeOfElement).toString() != "0")
								break;
						}
						
						if (!listPaths.contains(key.getSubFilter(0, i).toString()))
							listPaths.add(key.getSubFilter(0, i).toString());
						
						listPaths.add(key.getSubFilter(0, (pos - 1)*Config.sizeOfElement).toString() + "1");
					}
					else
					{
						listPaths.add(key.getSubFilter(0, pos*Config.sizeOfElement).toString());
					}
					
					ArrayList<BF> tmp = this.generatorBF(n.getRang());
					System.out.println(pos + " " + tmp.size());
					for (int i = 0; i < tmp.size(); i++)
					{
						if (key.in(tmp.get(i)))
							listPaths.add(this.reduce(tmp.get(i).toString()));
					}
					
					pos++;
				}
			}
			k++;
		}
		System.out.println(listPaths);
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
