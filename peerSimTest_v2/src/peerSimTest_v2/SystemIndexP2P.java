package peerSimTest_v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import peersim.config.Configuration;
import peersim.core.Network;

@SuppressWarnings("unused")
public class SystemIndexP2P implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String indexName;
	private int serverID;
	private int gamma;
	private Hashtable<String, SystemNodeP2P> listNode;
	
	/*
	 * Initialiser le système avec l'indexName, le serveur hébergé, gamma et une liste des nœuds qu'il gère
	 * */
	
	public SystemIndexP2P(String indexName, int serverID, int gamma) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		this.serverID = serverID;
		this.gamma = gamma;
		listNode = new Hashtable<String, SystemNodeP2P>();
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	/*
	 * Créer le nœud root "/"
	 * */
	
	public void createRoot()
	{
		listNode.put("/", new SystemNodeP2P(serverID, "/", 0, gamma));
	}
	
	/*
	 * Ajouter un filtre dans le nœud identifié par 'path'
	 * 
	 * Retourner soit null, soit une chaîne de caractère, soit un conteneur local(split)
	 * */
	
	public Object add(BFP2P bf, String path)
	{
		SystemNodeP2P n =  (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
		{
			n = new SystemNodeP2P(serverID, path, (new CalculRangP2P()).getRang(path), gamma);
			n.add(bf);
			this.listNode.put(path, n);
			return null;
		}
		else
		{
			Object o = n.add(bf);
			
			if (o == null)
				return null;
			
			while (o != null)
			{
				if (((o.getClass()).getName()).equals("java.lang.String"))
				{	
					if (listNode.containsKey(o))
					{
						n = (SystemNodeP2P)listNode.get(o);
						o = n.add(bf);
					}
					else
					{
						return o;
					}
				}
				else
				{		
					o = this.split(n, (ContainerLocalP2P)o);
					return o;
				} 
			}
		}
		return null;
	}

	/*
	 * Split prend 2 arguments comme le nœud père et le conteneur local
	 * 
	 * Retourner soit null, soit un message vers le serveur hébergé contient: 
	 * - indexName
	 * - une chaîne de caractère (chemin)
	 * - le conteneur local
	 * */
	
	private Object split(SystemNodeP2P father, ContainerLocalP2P c)
	{
		Iterator<BFP2P> iterator = c.iterator();
		BFP2P bf = c.get(0);
		FragmentP2P f = bf.getFragment(father.getRang());
		
		String path;
		if (father.getPath() == "/")
		{
			path = father.getPath() + f.toInt();
		}
		else
		{
			path = father.getPath() + "/" + f.toInt();
		}
		
		Message rep = new Message();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int tmp_serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		father.add(bf,path);
		
		if (tmp_serverID == serverID)
		{
			SystemNodeP2P n = new SystemNodeP2P(serverID, path, father.getRang() + 1, gamma);
			
			int rang = n.getRang();
			
			//***********************Calculer le profondeur du système************
			if (!ControlerNw.config_log.getIndexHeight().containsKey(rang))
			{
				ControlerNw.config_log.getIndexHeight().put(rang, n.getPath());
			}
			//********************************************************************
			/*
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_createNode", true);
			wf.write("createNode "+ indexName + " node "+ serverID + "\n"
					+ "PathLocal : " + path
					+ "\n\n");
			wf.close();
			//*****************
			*/
		
			while (iterator.hasNext())
			{
				bf = iterator.next();
				this.add(bf, path);	
			}
			
			this.listNode.put(path, n);
			return null;
		}
		else
		{ // rep to noeud local : creer SystemNodeP2P, path, containerlocal
			rep.setIndexName(indexName);
			rep.setData(c);
			rep.setPath(path);
		}
		
		return rep;
	}
	
	/*
	 * Ajouter un nœud dans le système
	 * */
	
	public void addSystemNodeP2P(String path, SystemNodeP2P node)
	{
		if (!this.listNode.containsKey(path))
		{
			this.listNode.put(path, node);
		}
	}
	
	@SuppressWarnings("unchecked")
	
	/*
	 * Rechercher le filtre dans le chemin précis
	 * */
	// search RETURN tableau 
	// 0 : ArrayList<BFP2P>
	// 1 : Hashtable<Integer, ArrayList<String>>
	
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
		Object[] resultat = new Object[2];
		resultat[0] = new ArrayList<BFP2P>();
		resultat[1] = new Hashtable<Integer, ArrayList<String>>();
		
		ArrayList<Object> list = (ArrayList<Object>) n.search(bf);
		
		int i = 0;
		while (i < list.size())
		{
			Object o = list.get(i);
			
			if (((o.getClass()).getName()).equals("java.lang.String"))
			{
				if (!this.listNode.containsKey((String)o))
				{
					ControlerNw.config_log.getTranslate().setLength(Network.size());
					int serverID_tmp = ControlerNw.config_log.getTranslate().translate((String)o);
					
					if (((Hashtable<Integer, ArrayList<String>>) resultat[1]).containsKey(serverID_tmp))
					{
						ArrayList<String> als = (((Hashtable<Integer, ArrayList<String>>) resultat[1]).get(serverID_tmp));
						if (!als.contains((String)o))
								als.add((String)o);
					}
					else // not contains serverID_tmp
					{
						ArrayList<String> al = new ArrayList<String>();
						al.add((String)o);
						((Hashtable<Integer, ArrayList<String>>) resultat[1]).put(serverID_tmp, al);
					}
				}
				else // this.listNode.containsKey((String)o)
				{
					SystemNodeP2P node_tmp = (SystemNodeP2P)listNode.get((String)o);
					//****************Compteur un nœud visité************
					ControlerNw.search_log.get(key).addNodeVisited(1);
					//***************************************************
					list.addAll((ArrayList<Object>) node_tmp.search(bf));
				}
			}
			else // o == BF
			{
				((ArrayList<BFP2P>) resultat[0]).add((BFP2P)o);
			}
			i++;
		}
		
		return resultat;
	}
	 
	/*
	 * Rechercher exact le filtre dans le chemin précis
	 * 
	 * Retourner soit une chaîne de caractères(chemin), soit un filtre
	 * */
	
	public Object searchExact(BFP2P bf, String path)
	{
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(bf.toString());
		
		SystemNodeP2P n = (SystemNodeP2P)listNode.get(path);
		
		if (n == null)
			return null;
		
		ControlerNw.search_log.get(key).addNodeMatched(path);
		
		Object o = n.searchExact(bf);
		
		while(o != null)
		{
			if (((o.getClass()).getName()).equals("java.lang.String"))
			{
				if (!listNode.containsKey((String)o))
				{
					return o;
				}
				else
				{
					n = listNode.get((String)o);
					
					ControlerNw.search_log.get(key).addNodeVisited(1);
					ControlerNw.search_log.get(key).addNodeMatched((String)o);
					o = n.searchExact(bf);
				}
			}else{
				Iterator<BFP2P> iterator = ((ContainerLocalP2P)o).iterator();
				
				while (iterator.hasNext())
				{
					BFP2P tmp = iterator.next();
					if (bf.equals(tmp))
					{
						//***********Compter un filtre trouvé***************
						ControlerNw.search_log.get(key).addNumberOfFilters(1);
						//***************************************************
						return tmp;
					}
				}
			}
		}
		return null;
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
