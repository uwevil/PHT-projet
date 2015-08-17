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

public class PHT_save implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Hashtable<String, PHT_Node> listNodes;
	
	public PHT_save(String indexName) {
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
			PHT_Node n = this.listNodes.get(path);

			if (n.getPath().equals("/"))
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
	//			System.out.println("-------IN--------");
				if (key.equals(bf_tmp))
				{					
					if (n.isLeafNode())
					{
	//					System.out.println(path + " " + n.getPath());
						return path;
					}
					else
					{
						int i = 1;
						path += key.getFragment(n.getRang() + i++, Config.sizeOfElement);
					//	while (this.listNodes.containsKey(path))
					//	{
					//		path += path.substring(0, path.length() - 1);
					//	}
						
						/*
						int i = 1;
						while (true)
						{
							if (key.getFragment(n.getRang() + i, Config.sizeOfElement).toString() == "1")
							{
								path += key.getFragment(n.getRang() + i++, Config.sizeOfElement);
								break;
							}
							
							path += key.getFragment(n.getRang() + i++, Config.sizeOfElement);
						}
						*/
	//					System.out.println("aaaaaaaaaaaaaaaa " + path);
					}
				}
				else
				{
	//				System.out.println("qqqq " + "0001" + "0101" + "1101" + "1000");
	//				System.out.println("ssss " + path + " " + n.getPath());
					
					int rang = n.getRang();
					char[] tmp = key.getSubFilter(0, rang*Config.sizeOfElement).toString().toCharArray();
					int i = 0;
					for (i = rang;i >= 0; i--)
					{
						if (tmp[i] != '0')
							break;
					}
	//				System.out.println(rang + " " + i);
					path = key.getSubFilter(0, i).toString();
	//				System.out.println(path);
					
					while (!this.listNodes.containsKey(path))
					{
						path = path.substring(0, path.length() - 1);
					}
	//				System.out.println("zzzz " + path);
				}
	//			System.out.println("-------OUT--------");
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
		else
		{
			String s_tmp = n.getPath().substring(0, 1);
			
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
