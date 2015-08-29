package peerSimTest_v4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.edsim.Heap;
import peersim.edsim.PriorityQ;
import peersim.transport.Transport;

public class SystemIndexProtocol_saved implements EDProtocol{

	public static int init;
	
	private static final String PAR_TRANSPORT = "transport";
	private String prefix;
	private int tid;
	private int nodeIndex;
	private Transport t;
	private int id;
	
	private Hashtable<String, PHT_Node> list = new Hashtable<String, PHT_Node>();
	private Hashtable<Integer, String> listPath = new Hashtable<Integer, String>();
	private ArrayDeque<BF> insertFIFO = new ArrayDeque<BF>();
	private ArrayDeque<BF> searchFIFO = new ArrayDeque<BF>();
	
	private Hashtable<Integer, ArrayList<Message>> splitData = new Hashtable<Integer, ArrayList<Message>>();
	
	private int[] listCreateNode = new int[Network.size()];
	private int[] listPUT = new int[Network.size()];
	private ArrayDeque<Object> listSplit = new ArrayDeque<Object>();
	
	private boolean split = false;
	
	private boolean problem = false;
	
	private ArrayDeque<Message> messageWaitingForResponse = new ArrayDeque<Message>();
				
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	private boolean insert_OK = true;
	private boolean search_OK = true;

	public SystemIndexProtocol_saved(String prefix) {
		// TODO Auto-generated constructor stub
		this.prefix = prefix;
		tid = Configuration.getPid(prefix+ "." + PAR_TRANSPORT);
		
		for (int i = 0; i < Network.size(); i++)
			recu[i] = 0;
		
	}
	
	public void setID(int id)
	{
		this.id = id;
	}
	
	public void setNodeIndex(int nodeIndex)
	{
		this.nodeIndex = nodeIndex;
	}
	
	public Object clone()
	{
		SystemIndexProtocol_saved s = new SystemIndexProtocol_saved(prefix);
		s.tid = this.tid;
		s.prefix = this.prefix;
		s.nodeIndex = this.nodeIndex;
		s.id = this.id;
		s.recu = this.recu;
		return s;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// TODO Auto-generated method stub
		
		t = (Transport) Network.get(nodeIndex).getProtocol(tid);
		Message message = (Message)event;

		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG, true);
		wf.write(message + "\n");
		wf.close();
	
		
		switch(message.getType())
		{
		case "createIndex": //createIndex, Name, sourceID, descID, option
			break;
			
		case "removeIndex": //removeIndex, Name
			break;
			
		case "insertInit" :
			try
			{
				treatInsertInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case "searchInit" :
			try
			{
				treatSearchInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case "PUT": // PUT, indexName, BF, path
			try
			{
				treatPUT(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "PUT_OK" :
			try
			{
				treatPUT_OK(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "createNode_OK" :
			try
			{
				treatCreateNode_OK(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
			
		case "GET": //GET, indexName, path
			treatGET(message, pid);
			break;
			
		case "GET_OK":
			treatGet_OK(message, pid);
			break;
			
		case "lookupPath_OK" :
			try 
			{
				treatLookupPath_OK(message, pid);
			} 
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
		case "lookupPath" :
			treatLookupPath(message, pid);
			break;
			
		case "createNode" :
			try
			{
				treatCreateNode(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
						
			
			
		case "overview": // nœud 0 balance la requete vers tous les autres nœuds
			try
			{
				treatOverview(message, pid);
			}
			catch (ErrorException e)
			{
				e.printStackTrace();
			}
			break;
			
		case "overview_OK": // tous les nœuds répond au nœud 0
			treatOverview_OK(message, pid);
			break;
			
		default : 
			break;
		}
	}

	private void treatGet_OK(Message message, int pid)
	{
		if (message.getOption().equals("SEARCH"))
		{
			treatSearch_OK(message, pid);
		}
		else
		{
			treatSearchExact_OK(message, pid);
		}
	}

	/**
	 * Traiter la réponse pour la recherche.
	 * 
	 * @param message
	 *  <li> indexName
	 *  <li> filtre de la requête
	 *  <li> liste des filtres trouvés
	 *  <li> liste des identifiants des nœuds en attente de la réponse.
	 *  @param pid
	 *  
	 *  @author dcs
	 * */
	
	@SuppressWarnings({ "unchecked" })
	private void treatSearch_OK(Message message, int pid)
	{		
		long requestID = message.getRequestID();
		
		if (message.getData() != null && ((ArrayList<BF>)message.getData()) != null)
		{
			ArrayList<BF> data1 = (ArrayList<BF>)message.getData();
			
			if (!data1.isEmpty())
			{
				if (ControlerNw.search_log.get(requestID) == null)
				{
					System.out.println("error(treatSearch_OK) : search_log == null");
					return;
				}
				
				//*************LOG le résultat******************
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				Iterator<BF> iterator = data1.iterator();
				
				BF bf = message.getBF();
				
				int size = 0;
				
				while (iterator.hasNext())
				{
					BF bf_tmp = iterator.next();
					if (bf.in(bf_tmp))
					{
						size++;
						WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_"+requestID, true);
						wf.write(bf_tmp.toString()+ "\n");
						wf.close();

					}
				}
				
				if (size != 0)
				{
					//*************Compter le nombre de filtres trouvés pour cette requête
					ControlerNw.search_log.get(requestID).addNumberOfFilters(size);
					//********************************************************************
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + requestID, true);
					wf1.write(date + "       Source : " + message.getSource() + "\n");
					wf1.write("                              " 
								+ ControlerNw.search_log.get(requestID).getNumberOfFilters() + " (" + size +")\n");
					wf1.close();
					//***********************************************
				}
			}
		}
		
		if (treatListAnswer(message)) // si toutes les réponses sont reçues
		{
			//******************Compter le temps total de la recherche*******************
			long time = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();
			ControlerNw.config_log.getTimeGlobal().put(requestID, time);
			//***************************************************************************
			
			//******************LOG le résultat*****************************************
			WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_resume_"+requestID, true);
			
			wf1.write("Nombre de chemins visités : " + ControlerNw.search_log.get(requestID).getNodeVisited() + " nœuds\n");
			wf1.write("Nombre de chemins matched : " + ControlerNw.search_log.get(requestID).sizeNodeMatched() + " nœuds\n");
			
			int j = 0;
			for (int i = 0; i < Network.size(); i++)
			{
				if (((int[])((Object[])ControlerNw.search_log.get(requestID).getListAnswer(requestID))[0])[i] > 0)
					j++;		
			}
			
			wf1.write("Nombre de pairs visités : " + j + " pairs (ou " + (j + 1) +" pairs si le pair qui"
					+ " reçoit la requête gère la racine du système d'indexation)\n");
			
			if (time >= 1000)
			{
				long i = time;
				long hours = TimeUnit.MILLISECONDS.toHours(i);
				i -= TimeUnit.HOURS.toMillis(hours);
				long minutes = TimeUnit.MILLISECONDS.toMinutes(i);
				i -= TimeUnit.MINUTES.toMillis(minutes);
				long seconds = TimeUnit.MILLISECONDS.toSeconds(i);
				i -= TimeUnit.SECONDS.toMillis(seconds);
				
				wf1.write("\nTemps de recherche : " + time + "ms == " 
						+ hours + ":" + minutes + ":" + seconds + "." + i
						+ "\n");
			}
			else
			{
				wf1.write("\nTemps de recherche : " + time + "ms\n");
			}
			
			wf1.write("Temps de calcul    : " + ControlerNw.search_log.get(requestID).getTime_calcul() + "ms\n\n");
			
			wf1.write("Trouvé : " + ControlerNw.search_log.get(requestID).getNumberOfFilters() + " filtres\n\n");
			
			wf1.write("Liste des chemins matched: " + ControlerNw.search_log.get(requestID).getNodeMatched() + "\n");
			wf1.close();
			
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setIndexName(message.getIndexName());
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
			//*******************************************************************************
						
			//************Supprimer la requête dans la table de requête
			ControlerNw.search_log.remove(requestID);

			/*
			System.out.println("\n\n");
			Enumeration<Integer> test = ControlerNw.search_log.keys();
			while (test.hasMoreElements())
				System.out.println(test.nextElement());
				*/
			//**********************************************************
		}

		if (ControlerNw.search_log.isEmpty()) // si il n'y a aucune requête en cours
		{
			ControlerNw.config_log.setEnd_OK(true);
		}
	}

	/**
	 * Traiter le message de la recherche exacte.
	 * 
	 * @param message
	 * 	<li> filtre de la requête
	 * 	<li> filtre trouvé
	 * @param pid
	 * @author dcs
	 * */
	
	@SuppressWarnings("unchecked")
	private void treatSearchExact_OK(Message message, int pid)
	{		
		long requestID = message.getRequestID();
		
		if (message.getData() == null)
		{
			//**********Compter le temps total de la recherche exacte*************
			long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();
			ControlerNw.config_log.getTimeGlobal().put(requestID, time);
			//********************************************************************
		}
		else // data1 != null
		{	
			ArrayList<BF> data1 = (ArrayList<BF>) message.getData();
			
			//**********Compter le temps total de la recherche exacte*************
			long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();		
			ControlerNw.config_log.getTimeGlobal().put(requestID, time);
			//********************************************************************
			
			Iterator<BF> iterator = data1.iterator();
			
			BF bf = message.getBF();
			
			while (iterator.hasNext())
			{
				BF bf_tmp = iterator.next();
				
				if (bf.equals(bf_tmp))
				{
					//*******LOG*******
					ControlerNw.search_log.get(requestID).addNumberOfFilters(1);
					ControlerNw.search_log.get(requestID).addNodeMatched(message.getKey().toString());
					
					WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "Exact_" + requestID, true);
					wf.write("BF source " + message.getSource() + "\n"
							+ bf.toString() + "\n"
							+ bf_tmp.toString()
							+ "\n\n");
					
					wf.write("Nœuds visités   : " + ControlerNw.search_log.get(requestID).getNodeVisited() + "\n");
					wf.write("Nœuds matched   : " + ControlerNw.search_log.get(requestID).getNodeMatched().size() + "\n\n");
					
					wf.write("Temps de recherche : " + time + "ms\n\n");
					wf.write("Trouvé : " + ControlerNw.search_log.get(requestID).getNumberOfFilters() + " filtres\n\n");
					wf.write("Liste des chemins matched: " + ControlerNw.search_log.get(requestID).getNodeMatched() + "\n");
					
					wf.close();
					//*****************
				}
			}
			
			
			
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setIndexName(message.getIndexName());
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}
		//***************Supprimer la requête dans la liste de requête en cours**
		ControlerNw.search_log.remove(requestID);
		//*********************************************************
		
		if (ControlerNw.search_log.isEmpty()) // si in n'y a aucune requête en cours, signaler
		{
			ControlerNw.config_log.setEnd_OK(true);
		}
	}

	/**
	 * Traiter le message reçu appellé par treatSearch_OK(recherche globale).
	 * 
	 * @param message
	 * @return {@link Boolean} true si il n'y a plus de réponses en attente, false sinon.
	 * 
	 * @author dcs
	 * */
	
	private boolean treatListAnswer(Message message)
	{		
		long requestID = message.getRequestID();

		// si cette requête a été enregistrée avant le lancement de la recherche
		if (ControlerNw.search_log.get(requestID).containsKeyListAnswer(requestID))
		{
			Object[] o = (Object[]) ControlerNw.search_log.get(requestID).getListAnswer(requestID);
			int[] received = (int[])o[0];
			int[] total = (int[])o[1];
			
		//	if (message.getSource() != nodeIndex) // si le serveur répond n'est pas l'enquêteur
			received[message.getSource()] += 1;			
			
			return testOK(received, total, Network.size());
		}
				
		return false;	
	}
	
	/**
	 * @return {@link Boolean} true si 2 tables sont identiques, false sinon.
	 * */
	
	private boolean testOK(int[] a, int[] b, int size)
	{
		/*
		//*******LOG*************
		String date = (new SimpleDateFormat("HH-mm-ss-SSS")).format(new Date());
		WriteFile wf = new WriteFile(Config.peerSimLOG + "_testOK", true);
		wf.write(date + "\n");
		wf.write(" received  total \n");
				
		for (int i = 0; i < Network.size(); i++)
		{
			if (a[i] != 0 || b[i] != 0)
				wf.write(i + " " + a[i] + " " + b[i] + "\n");
		}
		wf.write("\n");
		wf.close();
		//************************
		*/
		for (int i = 0; i < size; i++)
		{
			if (a[i] != b[i])
				return false;
		}

		return true;
	}
	
	/**
	 * Calcule la clé de stockage associée avec le chemin "path".
	 * 
	 * @return {@link String}
	*
	 * */
	
	private String skey(String path) throws ErrorException
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
	
	/**
	 * Calcule le plus long préfix de {@code str} qui matche avec la séquence {@code seq}.
	 * 
	 * @return {@link String}
	*
	 * */
	
	private String lpp(String str, String seq) throws ErrorException
	{		
		if (str == null)
			return null;
		
		if (str.length() < seq.length())
		{
			throw new ErrorException("lpp : str.length <= seq.length");
		}
		else
		{
			int occ = str.lastIndexOf(seq);
			
			if (occ != -1)
				return str.substring(0, occ + seq.length());
			
			return null;
		}
	}
	
	/**
	 * Retourne la position de bit '0' à partir de la position 'pos' précise (inclus).
	 * 
	 * @return int
	 * 
	*
	 * */
	
	private int nextZero(BF key, int pos)
	{
		for (int i = pos; i < key.size(); i++)
			if (!key.getBit(i))
				return i;
		
		return -1;
	}
	
	/**
	 * Retourne la position du dernier bit à 1 du filtre de Bloom.
	 * 
	 * @return int
	*
	 * */
	
	private int lastBitSet(BF key)
	{
		int i;
		for (i = key.size() - 1; i >= 0; i--)
		{
			if (key.getBit(i))
				return i;
		}
		
		return -1;
	}
	
	private void lookupPath(BF bf, BF key, String path, int requestID, int pid) throws ErrorException
	{
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
	//	if (serverID == nodeIndex)
	//	{
	//		lookupLocalPath(bf, key, path, requestID, pid);	
	//	}
	//	else
		{
			if (this.listPath.containsKey(requestID))
			{
				this.listPath.remove(requestID);
				this.listPath.put(requestID, path);
			}
			else
			{
				this.listPath.put(requestID, path);
			}
			
			Message rep = new Message();
			rep.setType("lookupPath");
			rep.setBF(bf);
			rep.setKey(key);
			rep.setPath(path);
			rep.setRequestID(requestID);
			rep.setSource(nodeIndex);
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		}
	}
/*	
	private void lookupLocalPath(BF bf, BF key, String path, int requestID, int pid) throws ErrorException
	{
		PHT_Node n = this.list.get(path);
		
		if (n.getPath().equals("/"))
		{
			if (n.isLeafNode())
			{
				insertLocalPath(bf, key, path, requestID, pid);
				return;
			}
			path = key.getFragment(0, 1).toString();
		}
		else // !n.getPath().equals("/")
		{				
			BF bf_tmp = new BF(n.getPath());
			if (key.equals(bf_tmp))
			{					
				if (n.isLeafNode())
				{
					insertLocalPath(bf, key, path, requestID, pid);
					return;
				}
				else // !n.isLeafNode()
				{
					path += key.getFragment(n.getRang() + 1, 1);
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
		
		this.lookupPath(bf, key, path, requestID, pid);
	}
*/
	/*
	private void insertLocalPath(BF bf, BF key, String path, int requestID, int pid) throws ErrorException
	{
		PHT_Node n = this.list.get(path);
		n.insert(bf);
		
		if (n.size() > Config.gamma)
		{
			n.setLeafNode(false);
			splitLocal(n, pid);
		}
	}
*/
	private void splitLocal(PHT_Node n, int pid) throws ErrorException
	{
		this.split = true;
		
		DataStore data = n.getDataStore();
		ArrayList<BF> listKeys = data.getListKeys();
		
		n.setDataStore(null);
		
		if (n.getPath().equals("/"))
		{
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate("0");
			int serverID1 = ControlerNw.config_log.getTranslate().translate("1");
			
			BF key_tmp0 = new BF("0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					Message rep = new Message();
					rep.setType("PUT");
					rep.setPath("0");
					rep.setOption("0");
					rep.setBF(bf_tmp);
					rep.setKey(key);
					rep.setSource(nodeIndex);
					rep.setDestinataire(serverID0);
					
					if (this.splitData.containsKey(serverID0))
					{
						this.splitData.get(serverID0).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID0, arrayList);
					}
				}
				else
				{
					Message rep = new Message();
					rep.setType("PUT");
					rep.setPath("1");
					rep.setOption("1");
					rep.setBF(bf_tmp);
					rep.setKey(key);
					rep.setSource(nodeIndex);
					rep.setDestinataire(serverID1);
					
					if (this.splitData.containsKey(serverID1))
					{
						this.splitData.get(serverID1).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID1, arrayList);
					}
				}
			}
			
			if (serverID0 == nodeIndex)
			{
				PHT_Node n0 = new PHT_Node("0");
				this.list.put("0", n0);
			}
			else
			{
				Message rep = new Message();
				rep.setType("createNode");
				rep.setPath("0");
				rep.setOption("0");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID0);
				
				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
			
				this.listCreateNode[serverID0]++;
			}
			
			if (serverID1 == nodeIndex)
			{
				PHT_Node n1 = new PHT_Node("1");
				this.list.put("1", n1);
			}
			else // serverID1 == nodeIndex
			{
				Message rep = new Message();
				rep.setType("createNode");
				rep.setPath("1");
				rep.setOption("1");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID1);
				
				t.send(Network.get(nodeIndex), Network.get(serverID1), rep, pid);
				
				this.listCreateNode[serverID1]++;
			}
		}
		else // !n.getPath().equals("/")
		{
			String path = n.getPath();
			
			String path_tmp0 = this.skey(path + "0");
			String path_tmp1 = this.skey(path + "1");
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate(path_tmp0);
			int serverID1 = ControlerNw.config_log.getTranslate().translate(path_tmp1);
						
			BF key_tmp0 = new BF(path + "0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					Message rep = new Message();
					rep.setType("PUT");
					rep.setPath(path_tmp0);
					rep.setOption(path + "0");
					rep.setBF(bf_tmp);
					rep.setKey(key);
					rep.setSource(nodeIndex);
					rep.setDestinataire(serverID0);
					
					if (this.splitData.containsKey(serverID0))
					{
						this.splitData.get(serverID0).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID0, arrayList);
					}
				}
				else
				{
					Message rep = new Message();
					rep.setType("PUT");
					rep.setPath(path_tmp1);
					rep.setOption(path + "1");
					rep.setBF(bf_tmp);
					rep.setKey(key);
					rep.setSource(nodeIndex);
					rep.setDestinataire(serverID1);
					
					if (this.splitData.containsKey(serverID1))
					{
						this.splitData.get(serverID1).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID1, arrayList);
					}
				}
			}
			
			if (serverID0 == nodeIndex)
			{
				PHT_Node n0 = new PHT_Node(path + "0");
				
				if (this.list.containsKey(path_tmp0))
					this.list.remove(path_tmp0);
				
				this.list.put(path_tmp0, n0);
				
				Message rep1 = new Message();
				rep1.setType("createNode");
				rep1.setPath(path_tmp1);
				rep1.setOption(path + "1");
				rep1.setSource(nodeIndex);
				rep1.setDestinataire(serverID1);
				
				t.send(Network.get(nodeIndex), Network.get(serverID1), rep1, pid);
								
				this.listCreateNode[serverID1]++;
			}
			else // serverID1 == nodeIndex
			{
				PHT_Node n1 = new PHT_Node(path + "1");
				
				if (this.list.containsKey(path_tmp1))
					this.list.remove(path_tmp1);
				
				this.list.put(path_tmp1, n1);
				
				Message rep = new Message();
				rep.setType("createNode");
				rep.setPath(path_tmp0);
				rep.setOption(path + "0");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID0);
				
				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
				
				this.listCreateNode[serverID0]++;
			}
		}
	}

	private int calculRang(String path)
	{
		if (path == "/")
			return -1;
		
		return path.length() - 1;
	}
	
	private void treatLookupPath_OK(Message message, int pid) throws ErrorException
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		String path = (String) message.getOption();
		boolean isLeafNode = message.getIsLeafNode();
		int requestID = message.getRequestID();
		
		if (path.equals("/"))
		{
			if (isLeafNode)
			{
				insertPath(message, pid);
				return;
			}
			
			path = key.getFragment(0, 1).toString();
		}
		else // !n.getPath().equals("/")
		{				
			BF bf_tmp = new BF(path);
			int range = this.calculRang(path);
			
			if (key.equals(bf_tmp))
			{					
				if (isLeafNode)
				{
					insertPath(message, pid);
					return;
				}
				else // !n.isLeafNode()
				{
					path += key.getFragment(range + 1, 1);
				}
			}
			else // !key.equals(bf_tmp)
			{
				String s = new String();
				for (int i = 0; i <= range; i++)
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
		
		this.lookupPath(bf, key, path, requestID, pid);
	}

	private void treatLookupPath(Message message, int pid)
	{		
		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp", true);
		wf.write("requestID = " + message.getRequestID() + "\n");
		wf.close();
		
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		Message rep = new Message();
		
		rep.setType("lookupPath_OK");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
		rep.setPath(path);
		rep.setIsLeafNode(n.isLeafNode());
		rep.setRequestID(message.getRequestID());
		rep.setOption(n.getPath());
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);		
	}

	private void launchInsert(int pid) throws ErrorException
	{		
		if (nodeIndex == init && insertFIFO.isEmpty())
		{
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
		//		t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}
		
		if (insert_OK)
		{
			insert_OK = false;
			if (insertFIFO.isEmpty())
				return;
			
			BF bf = insertFIFO.poll();
			BF key = bf.getKey(Config.sizeOfKey);
			int requestID = insertFIFO.size();
			
			for (int i = 0; i < Network.size(); i++)
			{
				this.listCreateNode[i] = 0;
			}
			
			this.lookupPath(bf, key, "/", requestID, pid);
		}
	}
	
	private void initInsertFIFO(Message message)
	{
		try(BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60")))
		{
			int line = 0;
			while (true)
			{
				String s = new String();
				s = reader.readLine();
				if (s == null)
					break;
				String[] tmp = s.split(";");
				
				if (tmp.length >= 2 && tmp[1].length() > 2 )
				{
					BF bf_tmp = new BF(Config.sizeOfBF);
					bf_tmp.addAll(tmp[1]);

					insertFIFO.add(bf_tmp);
					line++;
				}
				
				System.out.println(line);
				if (line == (int)message.getData())
					break;
			}
			reader.close();
			System.out.println("Fini de lecture " + line + " lignes");
			
			long time = System.currentTimeMillis();
			
			System.out.println("Désérialisation");
	//		Serializer serializer = new Serializer();
	//		serializer.writeObject(this.insertFIFO, "/Users/dcs/vrac/test/fifo_" + ControlerNw.config_log.version);
			
			System.out.println("Fin de désérialisation " + (System.currentTimeMillis() - time) + " ms");			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void treatInsertInit(Message message, int pid) throws ErrorException
	{
		System.out.println("Lecture n°1");
		
		init = nodeIndex;
		
		this.initInsertFIFO(message);
	/*	
		long time = System.currentTimeMillis();

		System.out.println("Désérialisation");
		Serializer serializer = new Serializer();
		this.insertFIFO = (ArrayDeque<BF>) serializer.readObject("/Users/dcs/vrac/test/fifo_" + ControlerNw.config_log.version);
		
		System.out.println("Fin de désérialisation " + (System.currentTimeMillis() - time) + " ms");
		*/
		this.launchInsert(pid);
	}
	
	private void insertPath(Message message, int pid)
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		String path = message.getPath();
		int requestID = message.getRequestID();

		Message rep = new Message();
		
		rep.setType("PUT");
		rep.setBF(bf);
		rep.setKey(key);
		rep.setPath(path);
		rep.setOption(message.getOption());
		rep.setRequestID(requestID);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		
		this.listPUT[message.getSource()]++;
	}
	
	/**
	 * Traiter le message de type "PUT".
	 * 
	 * @param message
	 *  <li> indexName
	 *  <li> path
	 *  <li> BF
	 *  
	 *  @author dcs
	 * @throws ErrorException 
	 * */
	
	@SuppressWarnings("static-access")
	private void treatPUT(Message message, int pid) throws ErrorException
	{
		messageWaitingForResponse.add(message);
		
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		n.insert(message.getBF());
		
		if (n.size() > ControlerNw.config_log.gamma)
		{
			n.setLeafNode(false);
			this.splitLocal(n, pid);
		}
		else
		{
			Message tmp = this.messageWaitingForResponse.removeLast();
			
			System.out.println(nodeIndex + " " + tmp.getSource());

			Message rep = new Message();
			
			rep.setType(tmp.getType() + "_OK");
			rep.setPath(tmp.getPath());
			rep.setOption(tmp.getOption());
			rep.setSource(nodeIndex);
			rep.setDestinataire(tmp.getSource());
			rep.setRequestID(tmp.getRequestID());
			
			t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);			
		}
	}
	
	private boolean testOK(int[] tab, int size)
	{
		for (int i = 0; i < size; i++)
		{
			if (tab[i] != 0)
				return false;
		}
		
		return true;
	}
	
	private void treatPUT_OK(Message message, int pid) throws ErrorException
	{
		if (this.split)
		{
			if (!this.listSplit.isEmpty())
			{
				int[] split_tmp = (int[]) this.listSplit.getLast();
				
				if (split_tmp[message.getSource()] == 0)
				{
					this.problem = true;
					ArrayDeque<Object> save = new ArrayDeque<Object>();
					while (true)
					{
						this.listSplit.removeLast();
						int[] split_tmp2 = (int[]) this.listSplit.removeLast();
						
						if (split_tmp2[message.getSource()] != 0)
						{
							split_tmp2[message.getSource()]--;
							save.add(split_tmp2);
							break;
						}
						save.add(split_tmp2);
					}
					
					for (int i = 0; i < save.size(); i++)
					{
						this.listSplit.add(save.removeLast());
					}
					
					this.listSplit.add(split_tmp);
					
				}
				else
				{
					split_tmp[message.getSource()]--;
				}	
				
				for (int i = 0; i < 1000; i++)
					if (split_tmp[i] != 0)
					System.out.print(i + " : " + split_tmp[i]);
				System.out.println(" PUT_OK");
				
				if (this.testOK(split_tmp, Network.size()))
				{	
					Message tmp = this.messageWaitingForResponse.removeLast();
					
					Message rep = new Message();
					rep.setType(tmp.getType() + "_OK");
					rep.setPath(tmp.getPath());
					rep.setOption(tmp.getOption());
					rep.setSource(nodeIndex);
					rep.setDestinataire(tmp.getSource());
					rep.setRequestID(tmp.getRequestID());
					
					t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);
					
					this.listSplit.removeLast();					
				}
				
				while (this.problem && !this.listSplit.isEmpty())
				{
					split_tmp = (int[]) this.listSplit.getLast();
					
					if (this.testOK(split_tmp, Network.size()))
					{	
						Message tmp = this.messageWaitingForResponse.removeLast();
						
						Message rep = new Message();
						rep.setType(tmp.getType() + "_OK");
						rep.setPath(tmp.getPath());
						rep.setOption(tmp.getOption());
						rep.setSource(nodeIndex);
						rep.setDestinataire(tmp.getSource());
						rep.setRequestID(tmp.getRequestID());
						
						t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);
						
						this.listSplit.removeLast();		
					}
					else
					{
						break;
					}
				}
				
				if (this.listSplit.isEmpty())
				{
					this.problem = false;
					this.split = false;
				}
				else
					return;
			}
		}

		if (!this.split)
		{
			if (this.listPUT[message.getSource()] == 0)
				return;
			
			this.listPUT[message.getSource()]--;

			if (this.testOK(listPUT, Network.size()))
			{
			/*	if (!this.messageWaitingForResponse.isEmpty())
				{
					Message tmp = this.messageWaitingForResponse.removeLast();
					
					Message rep = new Message();
					
					rep.setType(tmp.getType() + "_OK");
					rep.setPath(tmp.getPath());
					rep.setOption(tmp.getOption());
					rep.setSource(nodeIndex);
					rep.setDestinataire(tmp.getSource());
					rep.setRequestID(tmp.getRequestID());
					
					t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);					
				}
				else
					*/
				{
					insert_OK = true;
					this.launchInsert(pid);
				}
			}
		}
					
	}
	
	private void treatCreateNode_OK(Message message, int pid) throws ErrorException
	{		
		this.listCreateNode[message.getSource()]--;
		
		if (this.testOK(this.listCreateNode, Network.size()))
		{
			if (!this.splitData.isEmpty())
			{
				Enumeration<Integer> enumeration = this.splitData.keys();
				
				int[] tmp = new int[Network.size()];
				System.out.println("ssssssssssssssss");
				while (enumeration.hasMoreElements())
				{
					Integer serverID = enumeration.nextElement();
					ArrayList<Message> arrayList = this.splitData.get(serverID);
					
					for (int i = 0; i < arrayList.size(); i++)
					{
						Message rep = arrayList.get(i);
						
						t.send(Network.get(nodeIndex), Network.get(rep.getDestinataire()), rep, pid);
						
						tmp[rep.getDestinataire()]++;
					}
				}
				
				this.listSplit.add(tmp);			
				for (int i = 0; i < 1000; i++)
					if (tmp[i] != 0)
					System.out.println(i + " : " + tmp[i]);
				System.out.println("createNode_OK");
				this.splitData = new Hashtable<Integer, ArrayList<Message>>();
			}
		}
	}
	
	private void treatCreateNode(Message message, int pid) throws ErrorException
	{		
		String path = message.getPath();
		String realPath = (String) message.getOption();
		
		PHT_Node n = new PHT_Node(realPath);
		
		if (this.list.containsKey(path))
			this.list.remove(path);
		
		this.list.put(path, n);
		
		Message rep = new Message();
		rep.setType("createNode_OK");
		rep.setPath(path);
		rep.setOption(realPath);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
			
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);	
	}

	/**
	 * Traiter le message de type "GET".
	 * 
	 *  @param message
	 *  <li> indexName
	 *  <li> path
	 *  
	 * @author dcs
	 * */
	
	private void treatGET(Message message, int pid)
	{
		
	}
			
	private void treatSearchInit(Message message, int pid) throws ErrorException
	{
		try 
		{
			ReadFile rf = new ReadFile("/Users/dcs/vrac/test/wikiDocs<60_500_request");
			
			/*
			String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
			Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/" + experience + "_log";
			Config.peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date + "/" + experience + "_resultat_log";
			Config.peerSimLOG_path = "/Users/dcs/vrac/test/" + date + "/" + experience + "_path_log";
			*/

			for (int i = 0; i < rf.size(); i++)
			{									
				BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(i));
			
				this.searchFIFO.add(bf);
			}
			
			System.out.println("NOMBRE de requete = " + rf.size());
			
			this.launchSearch(pid);
			
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}	
		
	}
	
	private void launchSearch(int pid) throws ErrorException
	{		
		if (search_OK)
		{
			search_OK = false;
			if (searchFIFO.isEmpty())
				return;
			
			BF bf = searchFIFO.poll();
			int requestID = searchFIFO.size();
			
			this.supersetSearch(bf, requestID, pid);
		}
	}
	
	private void supersetSearch(BF bf, int requestID, int pid)
	{
		ArrayList<BF> bfs = new ArrayList<BF>();
		BF q = bf.getKey(Config.sizeOfKey);
		String sbroot = "/";
		int nextZ = this.nextZero(q, 0);
		int nbOnes = 0;
		if (nextZ < 0)
		{
			nbOnes = q.size();
		}
		else
		{
			nbOnes = nextZ;
		}
		if (nbOnes > 0)
		{
			sbroot += q.toString().substring(0, nbOnes);
		}
		this.exploreSubtree(sbroot, q, bf, bfs, requestID);
	}

	private void exploreSubtree(String sbroot, BF q, BF bf, ArrayList<BF> bfs, int requestID)
	{
		
	}	

	/**
	 * Capturer l'état du système.
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
		
	private void treatOverview(Message message, int pid) throws ErrorException
	{	
		ControlerNw.config_log.setNodePerServer(nodeIndex, list.size());		

		Enumeration<String> enumeration = this.list.keys();
				
		while (enumeration.hasMoreElements())
		{
			String s_tmp = enumeration.nextElement();
			PHT_Node n = this.list.get(s_tmp); 
			if (n.isLeafNode())
			{
				ControlerNw.config_log.getFilterPerNode().put(n.getPath(), n.size());
			}
									
			if (!ControlerNw.config_log.getIndexHeight().contains(n.getRang()))
				ControlerNw.config_log.getIndexHeight().put(n.getRang(), n.getPath());
		}
			
		
		
		Message rep = new Message();
		rep.setType("overview_OK");
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		
	}

	@SuppressWarnings("static-access")
	private void treatOverview_OK(Message message, int pid)
	{
		if (recu_OK)
			return;
		
		if (recu[message.getSource()] != 0 && recu_OK)
		{
			System.out.println(message.getSource() + " != 0");
		}
		
		recu[message.getSource()] = 1;
		for (int i = 0; i < Network.size(); i++)
		{
			if (recu[i] == 0)
				return;
		}
		
		ControlerNw.config_log.setConfig_OK(false);
				
		//*******************
		WriteFile wf = new WriteFile(Config.peerSimLOG+"_indexHeight", false);
		Enumeration<Integer> enumeration = ControlerNw.config_log.getIndexHeight().keys();
		
		wf.write("Taille du filtre    : " + ControlerNw.config_log.sizeOfBF + "\n");
		wf.write("Gamma               : " + ControlerNw.config_log.gamma + "\n\n");
		
		while (enumeration.hasMoreElements())
		{
			Integer i = enumeration.nextElement();
			if (i <= 9)
			{
				wf.write(i + "  " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}
			else
			{
				wf.write(i + " " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}			
		}
		wf.write("\n");
		
		enumeration = ControlerNw.config_log.getRealIndexHeight().keys();
		
		while (enumeration.hasMoreElements())
		{
			Integer i = enumeration.nextElement();
			if (i <= 9)
			{
				wf.write(i + "  " + ControlerNw.config_log.getRealIndexHeight().get(i) + "\n");
			}
			else
			{
				wf.write(i + " " + ControlerNw.config_log.getRealIndexHeight().get(i) + "\n");
			}	
		}
		
		wf.close();
		//*******************
		
		Hashtable<Integer, Integer> tab = new Hashtable<Integer, Integer>();
		
		WriteFile wf1 = new WriteFile(Config.peerSimLOG + "_nodePerServer", true);
		
		int j = 0;
		int k = 0;
		for (int i = 0; i < Network.size(); i++)
		{
			if (ControlerNw.config_log.getNodePerServer(i) == 0)
				continue;
			
			j += ControlerNw.config_log.getNodePerServer(i);

			if (tab.containsKey(ControlerNw.config_log.getNodePerServer(i)))
			{
				int p = tab.get(ControlerNw.config_log.getNodePerServer(i)) + 1;
				tab.remove(ControlerNw.config_log.getNodePerServer(i));
				tab.put(ControlerNw.config_log.getNodePerServer(i), p);
			}
			else
			{
				tab.put(ControlerNw.config_log.getNodePerServer(i), 1);
			}
			
			if (i < 10)
			{
				wf1.write(i + "       " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			else if (i < 100)
			{ 
				wf1.write(i + "      " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			else
			{					
				wf1.write(i + "     " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			k++;
		}
		
		wf1.write("Total : " + k + " pairs " + j+ " nœuds\n");
		
		Enumeration<Integer> enumeration2 = tab.keys();
		
		while (enumeration2.hasMoreElements())
		{
			int q = enumeration2.nextElement();
			
			float tmp = (float) ((float)(tab.get(q))/(float)k)*100;
			float tmp2 = (float) ((float)(tab.get(q)*q)/(float)j)*100;
			wf1.write(tab.get(q) + " pairs contient "+ q + " nœuds = " + tmp + "% de pairs " + tmp2 +"% de nœuds\n");
		}
			
		wf1.close();
		
		wf1 = new WriteFile(Config.peerSimLOG + "_filterPerNode", true);
		
		Enumeration<String> enumeration4 = ControlerNw.config_log.getFilterPerNode().keys();
		
		int i = 0;
		j = 0;
		k = 0;
				
		Hashtable<Integer, ArrayList<String>> hias = new Hashtable<Integer, ArrayList<String>>();
		while (enumeration4.hasMoreElements())
		{
			String s_tmp = enumeration4.nextElement();
			
			WriteFile wf2 = new WriteFile(Config.peerSimLOG + "_node", true);
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			if (s_tmp != "/")
			{
				wf2.write(s_tmp + "        " + ControlerNw.config_log.getTranslate().translate(s_tmp) + "\n");
			}
			else
			{
				wf2.write(s_tmp + "\n");
			}
			wf2.close();
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) == 0)
			{
				i++;
				continue;
			}
			
			j += ControlerNw.config_log.getFilterPerNode().get(s_tmp);
			
			if (i < 10)
			{
				wf1.write(i + "       " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp +  "\n");
			}
			else if (i < 100)
			{ 
				wf1.write(i + "      " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			else
			{					
				wf1.write(i + "     " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			i++;
			k++;
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 50)
			{
				if (hias.containsKey(50))
				{
					hias.get(50).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(50, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 100)
			{
				if (hias.containsKey(100))
				{
					hias.get(100).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(100, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 200)
			{
				if (hias.containsKey(200))
				{
					hias.get(200).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(200, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 500)
			{
				if (hias.containsKey(500))
				{
					hias.get(500).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(500, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 1000)
			{
				if (hias.containsKey(1000))
				{
					hias.get(1000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(1000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 5000)
			{
				System.out.println("<5000 = " + ControlerNw.config_log.getFilterPerNode().get(s_tmp));
				if (hias.containsKey(5000))
				{
					hias.get(5000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(5000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 10000)
			{
				if (hias.containsKey(10000))
				{
					hias.get(10000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(10000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 15000)
			{
				if (hias.containsKey(15000))
				{
					hias.get(15000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(15000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 20000)
			{
				if (hias.containsKey(20000))
				{
					hias.get(20000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(20000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 30000)
			{
				if (hias.containsKey(30000))
				{
					hias.get(30000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(30000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 40000)
			{
				if (hias.containsKey(40000))
				{
					hias.get(40000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(40000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) >= 40000)
			{
				if (hias.containsKey(50000))
				{
					hias.get(50000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(50000, als);
				}
			}
		}
		
		wf1.write("Total : " + k + "/" + i+ "\n");
		wf1.write("Moyen : " + j + "/" + k + " = " + (j/k) + "\n\n");
		
		float n = 0;

		int m = hias.get(50) == null ? 0 : hias.get(50).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("    <50 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(100) == null ? 0 : hias.get(100).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <100 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(200) == null ? 0 : hias.get(200).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <200 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(500) == null ? 0 : hias.get(500).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <500 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(1000) == null ? 0 : hias.get(1000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <1000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(5000) == null ? 0 : hias.get(5000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <5000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(5000).toString() + "\n");
		
		m = hias.get(10000) == null ? 0 : hias.get(10000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <10000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(10000).toString() + "\n");
		
		m = hias.get(15000) == null ? 0 : hias.get(15000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <15000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(15000).toString() + "\n");
		
		m = hias.get(20000) == null ? 0 : hias.get(20000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <20000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(20000).toString() + "\n");
		
		m = hias.get(30000) == null ? 0 : hias.get(30000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <30000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(30000).toString() + "\n");
		
		m = hias.get(40000) == null ? 0 : hias.get(40000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <40000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(40000).toString() + "\n");
		
		m = hias.get(50000) == null ? 0 : hias.get(50000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(">=40000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(50000).toString() + "\n");
		
		wf1.write("            = " + n + "%\n");
		
		wf1.close();
		
		recu_OK = true;
		
	//	Config.ObserverNw_OK = true;
	}
		
}











