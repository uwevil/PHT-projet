package peerSimTest_v3_1_1;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import peerSimTest_v3_1_1.Config;

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
	
	public void serializeListNodes(String fileName)
	{
		Serializer serializer = new Serializer();
		serializer.writeObject(listNodes, fileName);
	}
	
	@SuppressWarnings("unchecked")
	public void deserializeListNodes(String fileName)
	{
		Serializer serializer = new Serializer();
		this.listNodes = (Hashtable<String, PHT_Node>) serializer.readObject(fileName);
	}
	
	/**
	 * Recherche l'identifiant du nœud correspondant.
	 * 
	 * @return {@link String}
	 * 	
	 * @author dcs
	 * */
	
	private String lookup_insert(BF key) throws ErrorException
	{
		String path = "/";
		
		while (true)
		{
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

					String s = new String();
					for (int i = 0; i <= rang; i++)
					{
						if (key.getBit(i) == bf_tmp.getBit(i))
							s += (key.getBit(i)) ? "1" : "0";
						
						if (key.getBit(i) == !bf_tmp.getBit(i))
						{
							s += (key.getBit(i)) ? "1" : "0";
							break;
						}
					}
					
					path = s;
				}
			}
		}
	}
	
	public void insert(BF key) throws ErrorException
	{				
		String path = this.lookup_insert(key);
		
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
			
			String s_tmp = this.skey(path + "0");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new0);
			
			s_tmp = this.skey(path + "1");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new1);
		}
		
		for (int j = 0; j < listKeys.size(); j++)
			this.insert(listKeys.get(j));
		
	}
	
	private String skey(String path)
	{		
		String rootPath = "/";
		String zeroSeq = "0*";
		String oneSeq = "1*";
		
		if (path.length() <= 0)
			return null;
		
		if (path.equals(rootPath))
			return path;
		
		if (path.matches(zeroSeq))
			return "0";
		
		if (path.matches(oneSeq))
			return "1";
		
		if (path.charAt(path.length() - 1) == '1')
			return this.lpp(path, "01");
		
		return this.lpp(path, "10");
	}
	
	private String lpp(String str, String seq)
	{		
		if (str == null)
			return null;
		
		if (str.length() <= seq.length())
		{
			if (str.equals(seq))
				return str;
			return null;
		}
		else
		{
			int occ = str.lastIndexOf(seq);
			
			if (occ != -1)
				return str.substring(0, occ + seq.length());
			
			return null;
		}
	}
	
	private LookUpRep lookup(String path)
	{
		TestSystemIndex_v3_1_1_all.config_log.addNodeVisited(1);
		PHT_Node n;
		if (path.equals("/"))
		{
			n = this.listNodes.get(path);
		}
		else
		{
			n = this.listNodes.get(path.substring(1, path.length()));
		}
		
		if (n == null)
			return new LookUpRep("ExternalNode", null);
		
		if (n.isLeafNode())
			return new LookUpRep("LeafNode", "/" + n.getPath());
		
		return new LookUpRep("InternalNode", "/" + n.getPath());
	}
	
	private int nextZeroPos(BF key, int pos)
	{
		for (int i = pos + 1; i < key.size(); i++)
			if (!key.getBit(i))
				return i;
		
		return -1;
	}
	
	private int nextZeroEnd(BF key, int pos)
	{
		int res = 0;
		for (int i = pos + 1; i < key.size(); i++)
		{
			if (!key.getBit(i))
				res++;
			if (res != 0 && key.getBit(i))
				break;
		}
		if (res == 0)
			return -1;
		
		return pos + res;
	}
	
	public ArrayList<BF> get(String path)
	{
		return this.listNodes.get(path).getListKeys();
	}

	public ArrayList<BF> ssSearch(BF key) throws ErrorException
	{		
		ArrayList<String> leafNodes = new ArrayList<String>();
		String rootName = "/";
		LookUpRep rep = this.lookup(rootName);
		
		if (rep.status.equals("LeafNode"))
		{
			leafNodes.add(rootName);
		}
		else
		{
			ArrayList<String> sbTrees = new ArrayList<String>();
			sbTrees.add(rootName);
			
			while (sbTrees.size() != 0)
			{
				ArrayList<String> csbTrees = new ArrayList<String>(sbTrees);
				sbTrees.clear();
				
				for (int i = 0; i < csbTrees.size(); i++)
				{
					String sb = csbTrees.get(i);
					int l = sb.length() - 1;
					int start = this.nextZeroPos(key, l);
					if (start >= 0)
					{
						int end = this.nextZeroEnd(key, l);
						int nbZero = end - start + 1;
						String prefix = sb + key.toString().substring(l, start);
						ArrayList<String> root1 = search1TerRoots(prefix, nbZero, leafNodes);
						ArrayList<String> root0 = search0TerRoots(prefix, nbZero, leafNodes);
						
						sbTrees.addAll(root1);
						sbTrees.addAll(root0);
					}
					else
					{
						String temp = key.toString().substring(l, key.toString().length());
						String prefix = sb + temp;
						
						LookUpRep rep_tmp = this.lookup(prefix);
						if (rep_tmp.status.equals("LeafNode"))
							leafNodes.add(prefix);
					}
				}
			}
		}
		
		ArrayList<BF> bfs = new ArrayList<BF>();
		
		if (!leafNodes.isEmpty())
		{
			Iterator<String> iterator = leafNodes.iterator();
			
			while (iterator.hasNext())
			{
				String path = iterator.next();
				String tmp;
				if (path.equals("/"))
				{
					tmp = path;
				}
				else
				{
					tmp = this.skey(path.substring(1, path.length()));
				}
				
				ArrayList<BF> storedBF = this.get(tmp);
				for (int i = 0; i < storedBF.size(); i++)
				{
					BF bf = storedBF.get(i);
					if (key.in(bf))
						bfs.add(bf);
				}
			}
		}
		
		return bfs;
	}
	
	private ArrayList<String> search1TerRoots(String ancestor, int nbZero, ArrayList<String> leafNodes)
	{
		ArrayList<String> newRoots = new ArrayList<String>();
		ArrayDeque<String> candidates = new ArrayDeque<String>();
		candidates.add(ancestor);
		
		int n = ancestor.length() + nbZero;
	//	System.out.println(ancestor);
		while (!candidates.isEmpty())
		{
			String anc = candidates.poll();
	//		System.out.println(anc);
			String prefix = anc + "1";
			LookUpRep rep = this.lookup(prefix);

			String label = rep.label;
			if (label != null)
			{
				if (label.length() <= n)
				{
					leafNodes.add(label);
				}
				else
				{
					String sb = label.substring(0, n + 1);
					newRoots.add(sb);
					label = sb.substring(0, n);
				}
				
				String sibling = label;
				sibling = sibling.substring(0, sibling.length() - 1) + "0";
				candidates.add(sibling);
	//			System.out.println(sibling);
				if (label.length() > anc.length())
				{
					String spx = label.substring(0, label.length() - 1);
					while (spx.length() > anc.length())
					{
						spx = spx.substring(0, spx.length() - 1) + "0";
						candidates.add(spx);
		//				System.out.println("   " + spx);
						spx = spx.substring(0, spx.length() - 1);
					}
				}
	//			System.out.println();
			}
			
		// candidates.remove(anc)
		}
		
		return newRoots;
	}
	
	private ArrayList<String> search0TerRoots(String ancestor, int nbZero, ArrayList<String> leafNodes)
	{
		ArrayList<String> newRoots = new ArrayList<String>();
		ArrayDeque<String> candidates = new ArrayDeque<String>();
		candidates.add(ancestor);
		int n = ancestor.length() + nbZero;
		
		while (!candidates.isEmpty())
		{
			String anc = candidates.poll();
			String prefix = anc + "0";
			LookUpRep rep = this.lookup(prefix);
			String label = rep.label;
			if (label != null)
			{
				if (label.length() <= n)
				{
					leafNodes.add(label);
				}
				else
				{
					String sb = label.substring(0, n + 1);
					sb = sb.substring(0, sb.length() - 1) + "1";
					newRoots.add(sb);
					sb = sb.substring(0, sb.length() - 1);
					label = sb;
				}
				label = label.substring(0, label.length() - 1) + "1";
				candidates.add(label);
				if (label.length() > anc.length())
				{
					String spx = label.substring(0, label.length() - 1);
					while (spx.length() > anc.length())
					{
						spx = spx.substring(0, spx.length() - 1) + "1";
						candidates.add(spx);
						spx = spx.substring(0, spx.length() - 1);
					}
				}
			}
			
		//	candidates.remove(anc);
		}
		return newRoots;
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
