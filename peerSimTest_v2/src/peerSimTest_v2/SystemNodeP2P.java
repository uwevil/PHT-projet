package peerSimTest_v2;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class SystemNodeP2P implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int server;
	private String path;
	private Hashtable<Object, Integer> localRoute;
	private HashSet<BFP2P> localContainer;
	private int limit;
	
	/**
	 * Initiliser un nœud avec le server hébergé, l'identifiant sous forme une chaîne de caractères, le rang et la limite
	 * 	une talbe de routage
	 * */
	
	public SystemNodeP2P(int server, String path, int limit)
	{
		this.server = server;
		this.path = path;
		this.limit = limit;
		localRoute = new Hashtable<Object, Integer>();
		localContainer = new HashSet<BFP2P>();
	}
	
	/**
	 * Rendre l'identifiant de ce nœud
	 * */
	
	public String getPath()
	{
		return this.path;
	}
	
	/**
	 * Rendre le server hébergé
	 * */
	
	public int getServer()
	{
		return this.server;
	}
		
	/**
	 * Rendre la table de routage de ce nœud
	 * */
	
	public Hashtable<Object, Integer> getLocalRoute()
	{
		return this.localRoute;
	}
	
	/**
	 * Rendre le conteneur local
	 * */
	
	public HashSet<BFP2P> getLocalContainer()
	{
		return this.localContainer;
	}
	
	/**
	 * Rendre la limite(pour le conteneur local) stockée dans ce nœud
	 * */
	
	public int getLimit()
	{
		return this.limit;
	}
	
	/** Ajouter le filtre dans le nœud.
	 * <p>	
	 * Retourner : 
	 * <ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	<li> soit {@code Hashtable<String, HashSet<BFP2P>>}.
	 * </ul>
	 * </p>
	 * */
	public Object add(BFP2P bf) throws ErrorException
	{	
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		String longestPrefix ;
					
		if (longestLength != 0)
		{			
			if (!this.localRoute.containsKey(longestLength))
			{
				/*
				//***********************LOG***************
				WriteFile wf = new WriteFile(Config.peerSimLOG+"_containerLocal_" + server, true);
				Iterator<BFP2P> iterator = this.containerLocal.iterator();
				
				while (iterator.hasNext())
				{
					wf.write(" " + iterator.next().toPath(0, 100)+ "\n");
				}
				wf.close();
				//******************************************
				*/
				Iterator<BFP2P> iterator = this.localContainer.iterator();
				while (iterator.hasNext())
				{
					if (((BFP2P)iterator.next()).equals(bf))
						return null;
				}
				
				if (this.localContainer.size() < this.limit)
				{
					this.localContainer.add(bf);
					return null;
				}
				
				this.localContainer.add(bf);
			}
			else // this.localRoute.containsKey(longestLength)
			{
				String path_tmp;
				longestPrefix = longestZero.getLongestPrefix();

				if (this.path == "/")
				{
					path_tmp = longestPrefix;
				}
				else
				{
					path_tmp = this.path + longestPrefix;
				}
				
				BFP2P bf_tmp2 = (new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(), 0, Config.numberOfFragment, Config.sizeOfFragment);
				
				Message rep = new Message();
				rep.setType("add");
				rep.setPath(path_tmp);
				rep.setData(bf_tmp2);
				rep.setDestinataire(this.localRoute.get((Object)longestLength));
				
				return rep;
			}
		}
		else //longestLength == 0
		{
			longestPrefix = "/" + bf.getFragment(0, Config.sizeOfFragment).toInt();
						
			if (!this.localRoute.containsKey(longestPrefix))
			{
				Iterator<BFP2P> iterator = this.localContainer.iterator();
				while (iterator.hasNext())
				{
					if (((BFP2P)iterator.next()).equals(bf))
						return null;
				}
				
				if (this.localContainer.size() < this.limit)
				{	
					this.localContainer.add(bf);
					return null;
				}
				
				this.localContainer.add(bf);
			}
			else //this.localRoute.containsKey((Object)longestPrefix)
			{
				String path_tmp;
				
				if (this.path == "/")
				{
					path_tmp = longestPrefix;
				}
				else
				{
					path_tmp = this.path + longestPrefix;
				}
				
				BFP2P bf_tmp2 = (new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment, Config.sizeOfFragment);
				
				Message rep = new Message();
				rep.setType("add");
				rep.setPath(path_tmp);
				rep.setData(bf_tmp2);
				rep.setDestinataire(this.localRoute.get(longestPrefix));
				
				return rep;
			}
		}
		
		Hashtable<String, HashSet<BFP2P>> htshsbf = new Hashtable<String, HashSet<BFP2P>>();
		
		Iterator<BFP2P> iterator = this.localContainer.iterator();
		
		while(iterator.hasNext())
		{
			BFP2P bf_tmp = iterator.next();
			longestZero = new LongestZero(bf_tmp, Config.sizeOfFragment);
			
			longestLength = longestZero.getLongestLength();
			
			BFP2P bf_tmp2;
			
			if (longestLength != 0)
			{
				longestPrefix = longestZero.getLongestPrefix();
				bf_tmp2 = (new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(), 0, Config.numberOfFragment, Config.sizeOfFragment);
			}
			else //longestLength == 0
			{
				longestPrefix = "/" + bf_tmp.getFragment(0, Config.sizeOfFragment).toInt();
				bf_tmp2 = (new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment, Config.sizeOfFragment);
			}
			
			String path_tmp ;
			if (this.path != "/")
			{
				path_tmp = this.path + longestPrefix;
			}
			else
			{
				path_tmp = longestPrefix;
			}
			
			if (htshsbf.containsKey(path_tmp))
			{
				htshsbf.get(path_tmp).add(bf_tmp2);
			}
			else // !htshsbf.containsKey(longestPrefix)
			{
				HashSet<BFP2P> hsbf = new HashSet<BFP2P>();
				hsbf.add(bf_tmp2);
				htshsbf.put(path_tmp, hsbf);
			}
		}
		
		this.localContainer = new HashSet<BFP2P>();
		
		return htshsbf;
	}
	
	/**
	 * Ajouter l'identifiant du nœud et l'id du serveur dans la table de routage.
	 * */
	
	public void add(String path, int nodeID)
	{
		int rang = (new CalculRangP2P()).getRang(this.path);
				
		BFP2P bf = (new BFP2P()).pathToBF(path, 0, Config.numberOfFragment, Config.sizeOfFragment);
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);

		if (rang == 0)
		{	
			if (longestZero.getLongestLength() != 0)
			{
				this.localRoute.put(longestZero.getLongestLength(), nodeID);
			}
			else // longestLength == 0
			{		
				this.localRoute.put(path, nodeID);
			}
		}
		else // rang != 0
		{
			String path_tmp = bf.toPath(rang, Config.numberOfFragment);
			bf = (new BFP2P()).pathToBF(path_tmp, 0, Config.numberOfFragment, Config.sizeOfFragment);
			longestZero = new LongestZero(bf, Config.sizeOfFragment);
			
			if (longestZero.getLongestLength() != 0)
			{
				this.localRoute.put(longestZero.getLongestLength(), nodeID);
			}
			else // longestLength == 0
			{
				this.localRoute.put(path_tmp, nodeID);
			}
		}
	}
	
	/**
	 * Rechercher tous les filtres qui contiennent le filtre(sous-filtre).
	 * <p>
	 * Retourner : 
	 * <ul>
	 * 	{@link Object[]}
	 * 	<li> {@code o[0] = HashSet<BFP2P>}
	 * 	<li> {@code o[1] = Hashtable<Integer,Hashtable<String,BFP2P>>}.
	 * </ul>
	 * </p>
	 * */
	
	public Object search(BFP2P bf)
	{
		HashSet<BFP2P> res = new HashSet<BFP2P>();
		
		Iterator<BFP2P> iterator = this.localContainer.iterator();
		
		while (iterator.hasNext())
		{
			BFP2P bf_tmp = iterator.next();
			
			if (bf.in(bf_tmp))
				res.add(bf_tmp);
		}
		
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		
		Hashtable<Integer, Hashtable<String, BFP2P>> res2 = new Hashtable<Integer, Hashtable<String,BFP2P>>();
		
		Enumeration<Object> enumeration = this.localRoute.keys();
		
		while (enumeration.hasMoreElements())
		{
			Object prefix = enumeration.nextElement();
			
			if (longestLength != 0)
			{
				if (prefix.getClass().getName().contains("Integer"))
				{
					if (longestLength >= (Integer)prefix)
					{
						String path_tmp = new String();
						if (this.path == "/")
						{
							path_tmp = longestZero.getLongestPrefix((Integer)prefix);
						}
						else // this.path != "/"
						{
							path_tmp = this.path + longestZero.getLongestPrefix((Integer)prefix);
						}
						
						if (res2.containsKey(this.localRoute.get(prefix)))
						{
							(res2.get(this.localRoute.get(prefix))).put(path_tmp,
									(new BFP2P()).pathToBF(longestZero.getRemainPrefix((Integer)prefix), 0, 
											Config.numberOfFragment,
											Config.sizeOfFragment));
						}
						else // res2 not contains nodeID
						{
							Hashtable<String, BFP2P> htshsbf = new Hashtable<String, BFP2P>();
							htshsbf.put(path_tmp, 
									(new BFP2P()).pathToBF(longestZero.getRemainPrefix((Integer)prefix), 0, 
											Config.numberOfFragment,
											Config.sizeOfFragment));
							res2.put(this.localRoute.get(prefix), htshsbf);
						}
					}
				}
				else // prefix type String
				{
					String path_tmp = new String();
					if (this.path == "/")
					{
						path_tmp = (String) prefix;
					}
					else
					{
						path_tmp = this.path + (String) prefix;
					}
					
					if (res2.containsKey(this.localRoute.get(prefix)))
					{
						(res2.get(this.localRoute.get(prefix))).put(path_tmp,  (new BFP2P())
								.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment,
										Config.sizeOfFragment));
					}
					else // res2 not contains nodeID
					{
						Hashtable<String, BFP2P> htshsbf = new Hashtable<String, BFP2P>();
						htshsbf.put(path_tmp,  (new BFP2P())
								.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment,
										Config.sizeOfFragment));
						res2.put(this.localRoute.get(prefix), htshsbf);
					}
				}
			}
			else // longestLength == 0
			{
				if (!prefix.getClass().getName().equals("java.lang.Integer"))
				{
					FragmentP2P f = bf.getFragment(0, Config.sizeOfFragment);					
					FragmentP2P f_tmp = (new FragmentP2P(Config.sizeOfFragment)).pathToFragment((String)prefix);
					
					if (f.in(f_tmp))
					{
						String path_tmp = new String();
						if (this.path == "/")
						{
							path_tmp = (String) prefix;
						}
						else
						{
							path_tmp = this.path + (String) prefix;
						}
						
						if (res2.containsKey(this.localRoute.get(prefix)))
						{
							res2.get(this.localRoute.get(prefix)).put(path_tmp, (new BFP2P())
									.pathToBF(bf.toPath(1, Config.numberOfFragment), 0, Config.numberOfFragment, 
											Config.sizeOfFragment));
						}
						else // not contains
						{
							Hashtable<String, BFP2P> htshsbf = new Hashtable<String, BFP2P>();
							htshsbf.put(path_tmp, (new BFP2P())
									.pathToBF(bf.toPath(1, Config.numberOfFragment), 0, Config.numberOfFragment, 
											Config.sizeOfFragment));
							res2.put(this.localRoute.get(prefix), htshsbf);
						}
					}
				}
			}	
		}
		/*
		//*****************************LOG*****************************
		WriteFile wf = new WriteFile(Config.peerSimLOG+"_"+"test", true);
		
		Enumeration<Integer> enumeration2 = res2.keys();
		wf.write("--------------"+this.path+"----------------\n");
		while(enumeration2.hasMoreElements())
		{
			Integer i = enumeration2.nextElement();
			wf.write(i + "\n");

			Enumeration<String> enumeration3 = (res2.get(i)).keys();
			while (enumeration3.hasMoreElements())
			{
				String s = enumeration3.nextElement();
				
				wf.write("    " + s + " = " + (res2.get(i)).get(s).toPath(0, 100)+ "\n");
			}
		}
		wf.write("\n\n");
		wf.close();
		//*************************************************************
		*/
		Object[] o = new Object[2];
		o[0] = res;
		o[1] = res2;
		
		return o;
	}
	
	/**
	 * Rechercher le filtre précise
	 * <p>
	 * Retourner 
	 * 	<ul>
	 * 	<li> soit vide {@link null}
	 * 	<li> soit un message {@link Message}
	 * 	</ul>
	 * </p>
	 * */
	
	public Object searchExact(BFP2P bf)
	{
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		
		if (longestLength != 0)
		{
			if (this.localRoute.containsKey(longestLength))
			{
				String path_tmp;
				
				if (this.path == "/")
				{
					path_tmp = longestZero.getLongestPrefix();
				}
				else // this.path != "/"
				{
					path_tmp = this.path + longestZero.getLongestPrefix();
				}
				
				Message rep = new Message();
				
				rep.setType("searchExact");
				rep.setPath(path_tmp);
				rep.setDestinataire(this.localRoute.get(longestLength));
				rep.setData((new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(), 0, Config.numberOfFragment, Config.sizeOfFragment));
				
				return rep;
			}
		}
		else // longestLength == 0
		{
			FragmentP2P f = bf.getFragment(0, Config.sizeOfFragment);
			
			if (this.localRoute.containsKey(f.toPath()))
			{
				String path_tmp;

				if (this.path == "/")
				{
					path_tmp = f.toPath();
				}
				else // this.path != "/"
				{
					path_tmp = this.path + f.toPath();
				}
				
				Message rep = new Message();
				
				rep.setType("searchExact");
				rep.setPath(path_tmp);
				rep.setDestinataire(this.localRoute.get(f.toPath()));
				rep.setData((new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment, Config.sizeOfFragment));
				
				return rep;
			}
		}
		
		Iterator<BFP2P> iterator = this.localContainer.iterator();
		while (iterator.hasNext())
		{
			BFP2P bf_tmp = (BFP2P)iterator.next();
			if (bf_tmp.equals(bf))
			{
				Message rep = new Message();
				
				rep.setType("searchExact_OK");
				rep.setPath(this.path);
				rep.setData(bf_tmp);
				
				return rep;
			}
		}
		return null;
	}
	
	/**
	 * Supprimer le filtre dans le nœud
	 * <p>
	 * Retourner soit vide, soit une chaîne de caractères, soit une table de routage
	 * */
	
	public Object remove(BFP2P bf)
	{
		LongestZero longestZero = new LongestZero(bf, Config.sizeOfFragment);
		
		int longestLength = longestZero.getLongestLength();
		
		if (longestLength != 0)
		{
			if (this.localRoute.containsKey(longestLength))
			{
				String path_tmp;
				
				if (this.path == "/")
				{
					path_tmp = longestZero.getLongestPrefix();
				}
				else // this.path != "/"
				{
					path_tmp = this.path + longestZero.getLongestPrefix();
				}
				
				Message rep = new Message();
				
				rep.setType("remove");
				rep.setPath(path_tmp);
				rep.setDestinataire(this.localRoute.get(longestLength));
				rep.setData((new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(), 0, Config.numberOfFragment, Config.sizeOfFragment));

				return rep;
			}
		}
		else // longestLength == 0
		{
			FragmentP2P f = bf.getFragment(0, Config.sizeOfFragment);

			if (this.localRoute.containsKey(f.toPath()))
			{
				String path_tmp;

				if (this.path == "/")
				{
					path_tmp = f.toPath();
				}
				else // this.path != "/"
				{
					path_tmp = this.path + f.toPath();
				}
				
				Message rep = new Message();
				
				rep.setType("remove");
				rep.setPath(path_tmp);
				rep.setDestinataire(this.localRoute.get(f.toPath()));
				rep.setData((new BFP2P())
						.pathToBF(longestZero.getRemainPrefix(1), 0, Config.numberOfFragment, Config.sizeOfFragment));
				
				return rep;
			}
		}

		Iterator<BFP2P> iterator = this.localContainer.iterator();
		while (iterator.hasNext())
		{
			BFP2P bf_tmp = (BFP2P)iterator.next();
			if (bf_tmp.equals(bf))
			{
				this.localContainer.remove(bf_tmp);
				return null;
			}
		}
		return null;
	}

	public String toString()
	{
		return null;
	}
}







