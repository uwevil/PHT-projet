package peerSimTest_v4;

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
import peersim.transport.Transport;

public class SystemIndexProtocol implements EDProtocol{

	private static final String PAR_TRANSPORT = "transport";
	private String prefix;
	private int tid;
	private int nodeIndex;
	private Transport t;
	private int id;
	
	private Hashtable<String, PHT_IndexNode> list = new Hashtable<String, PHT_IndexNode>();
	private Hashtable<Integer, Object> listRequestRemaining = new Hashtable<Integer, Object>();
	private ArrayDeque<Message> fifo = new ArrayDeque<Message>();
	
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	private boolean ok = true;
	private int createNode_OK = 0;
	
	public SystemIndexProtocol(String prefix) {
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
		SystemIndexProtocol s = new SystemIndexProtocol(prefix);
		s.tid = this.tid;
		s.prefix = this.prefix;
		s.nodeIndex = this.nodeIndex;
		s.id = this.id;
		s.recu = this.recu;
		return s;
	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// TODO Auto-generated method stub
		
		t = (Transport) Network.get(nodeIndex).getProtocol(tid);
		Message message = (Message)event;
		
		//*********************************************************************
		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG, true);
		wf.write(message + "\n");
		wf.close();
		//*********************************************************************

		switch(message.getType())
		{
		case "createIndex": //createIndex, Name, sourceID, descID, option
			break;
			
		case "removeIndex": //removeIndex, Name
			break;
			
		case "insert": // add, Name, path, BF
			try
			{
				treatInsert(message, pid);
			} 
			catch (ErrorException e1)
			{
				e1.printStackTrace();
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
			
		case "lookupPathResponse" :
			try 
			{
				treatLookupPathResponse(message, pid);
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
		int requestID = message.getRequestID();
		
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
		int requestID = message.getRequestID();
		
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
		int requestID = message.getRequestID();

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
		
		if (!this.listRequestRemaining.containsKey(requestID))
		{
			this.listRequestRemaining.put(requestID, path);
		}
		else
		{
			this.listRequestRemaining.remove(requestID);
			this.listRequestRemaining.put(requestID, path);
		}
		
		if (serverID == nodeIndex)
		{
			Message rep = new Message();
			
			rep.setType("lookupPathResponse");
			rep.setBF(bf);
			rep.setKey(key);
			rep.setSource(nodeIndex);
			rep.setDestinataire(serverID);
			
			PHT_IndexNode n = this.list.get(path);

			rep.setPath(n.getPath());
			rep.setIsLeafNode(n.isLeafNode());
			rep.setOption(n.isLeafNode() ? n.size() : -1);
			
			rep.setRequestID(requestID);
			treatLookupPathResponse(rep, pid);
			return;
		}
		
		Message rep = new Message();
		
		rep.setType("lookupPath");
		rep.setBF(bf);
		rep.setKey(key);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		rep.setPath(path);
		rep.setRequestID(requestID);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}
	
	private int calculRang(String path)
	{
		if (path == "/")
			return -1;
		
		return path.length() - 1;
	}
	
	private void treatLookupPathResponse(Message message, int pid) throws ErrorException
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		
		String path = (String) message.getPath();
		/*
		if (path == null)
		{
			Message rep = new Message();
			rep.setType("insert");
			rep.setBF(bf);
			rep.setKey(key);
			rep.setPath("/");
			rep.setRequestID(message.getRequestID());
			
			this.fifo.add(rep);
			return;
		}
		*/
		boolean isLeafNode = message.getIsLeafNode();

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
			if (key.equals(bf_tmp))
			{					
				if (isLeafNode)
				{
					insertPath(message, pid);
					return;
				}
				else // !n.isLeafNode()
				{
					path += key.getFragment(this.calculRang(path) + 1, 1);
				}
			}
			else // !key.equals(bf_tmp)
			{
				int rang = this.calculRang(path);

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

		this.lookupPath(bf, key, path, message.getRequestID(), pid);
	}

	private void treatLookupPath(Message message, int pid)
	{
		String path = (String) message.getPath();
		BF bf = message.getBF();
		BF key = message.getKey();
		int source = message.getSource();
		int requestID = message.getRequestID();
		
		if (this.list.containsKey(path))
		{
			PHT_IndexNode n = this.list.get(path);
			
			Message rep = new Message();
			rep.setType("lookupPathResponse");
			rep.setBF(bf);
			rep.setKey(key);
			rep.setPath(n.getPath());
			rep.setIsLeafNode(n.isLeafNode());

			rep.setOption(n.isLeafNode() ? n.size() : -1);
			
			rep.setRequestID(requestID);
			rep.setSource(nodeIndex);
			rep.setDestinataire(source);
			
			t.send(Network.get(nodeIndex), Network.get(source), rep, pid);
		}
		else
		{
			Message rep = new Message();
			rep.setType("lookupPathResponse");
			rep.setRequestID(message.getRequestID());
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		}
	}

	private void launchRequest(int pid) throws ErrorException
	{
		Message message = this.fifo.getFirst();
		
		BF bf = message.getBF();
		BF key = message.getKey();
		int requestID = message.getRequestID();
		
		this.lookupPath(bf, key, "/", requestID, pid);
	}
	
	private void treatInsert(Message message, int pid) throws ErrorException
	{		
		if (ok)
		{
	/**//*	WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp", true);
			wf.write(message + "\n");
			wf.close();
		*/
			this.fifo.add(message);
			
			launchRequest(pid);
			ok = false;
		}
		else
		{
	/**/	/*WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp", true);
			wf.write(message + "\n");
			wf.close();
			*/
			this.fifo.add(message);
		}
	}
	
	private void insertPath(Message message, int pid)
	{
		Message rep = new Message();
		rep.setType("PUT");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
		rep.setPath((String) this.listRequestRemaining.get(message.getRequestID()));
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setRequestID(message.getRequestID());
		
		if (this.listRequestRemaining.containsKey(message.getRequestID()))
			this.listRequestRemaining.remove(message.getRequestID());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
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
	
	private void treatPUT(Message message, int pid) throws ErrorException
	{
		BF bf = message.getBF();
		
		String path = (String) message.getPath();
				
		if (this.list.containsKey(path))
		{
			PHT_IndexNode n = this.list.get(path);
			n.insert(bf);

			if (n.size() > Config.gamma)
			{
				this.split(message, pid);
				return;
			}
		}

		Message rep = new Message();
		rep.setType("PUT_OK");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
		rep.setRequestID(message.getRequestID());
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setPath(path);
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatPUT_OK(Message message, int pid) throws ErrorException
	{
		if (this.fifo.isEmpty())
		{
			ok = true;
			return;
		}
		
/**//*	WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp1", true);
		wf.write(this.fifo.getFirst() + "\n");
		wf.close();
		*/
		this.fifo.removeFirst();
		this.launchRequest(pid);
	}

	
	private void split(Message message, int pid) throws ErrorException
	{		
		createNode_OK = 0;
		String path = (String) message.getPath();

		PHT_IndexNode n = this.list.get(path);
		n.setLeafNode(false);

		DataStore data = n.getDataStore();
		n.setDataStore(null);
				
		if (n.getPath().equals("/"))
		{			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate("0");
			
			BF bf_tmp0 = new BF("0");
			
			ArrayList<BF> listKeys = data.getListKeys();
			ArrayList<BF> l0 = new ArrayList<BF>();
			ArrayList<BF> l1 = new ArrayList<BF>();

			for (int j = 0; j < listKeys.size(); j++)
			{
				BF bf_tmp = listKeys.get(j);
				
				if (bf_tmp.equals(bf_tmp0))
				{
					l0.add(bf_tmp);
				}
				else
				{
					l1.add(bf_tmp);
				}
			}
			
			Message rep = new Message();

			rep.setType("createNode");
			rep.setPath("0");
			rep.setData(l0);
			rep.setOption("0");
			rep.setSource(nodeIndex);
			rep.setDestinataire(serverID0);
			rep.setRequestID(message.getRequestID());
			
			if (serverID0 == nodeIndex)
				treatCreateNode(rep, pid);
			else
				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
						
			Message rep1 = new Message();
			
			int serverID1 = ControlerNw.config_log.getTranslate().translate("1");
			
			rep1.setType("createNode");
			rep1.setPath("1");
			rep1.setData(l1);
			rep1.setOption("1");
			rep1.setSource(nodeIndex);
			rep1.setDestinataire(serverID1);
			rep1.setRequestID(message.getRequestID());
			
			if (serverID1 == nodeIndex)
				treatCreateNode(rep, pid);
			else
				t.send(Network.get(nodeIndex), Network.get(serverID1), rep1, pid);			
		}
		else // !n.getPath().equals("/")
		{			
			String s_tmp0 = this.skey(path + "0");
			
			BF bf_tmp0 = new BF(path + "0");
			
			ArrayList<BF> listKeys = data.getListKeys();
			ArrayList<BF> l0 = new ArrayList<BF>();
			ArrayList<BF> l1 = new ArrayList<BF>();

			for (int j = 0; j < listKeys.size(); j++)
			{
				BF bf_tmp = listKeys.get(j);
				
				if (bf_tmp.equals(bf_tmp0))
				{
					l0.add(bf_tmp);
				}
				else
				{
					l1.add(bf_tmp);
				}
			}
			
			Message rep = new Message();
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate(s_tmp0);
			
			rep.setType("createNode");
			rep.setPath(s_tmp0);
			rep.setData(l0);
			rep.setOption(path + "0");
			rep.setSource(nodeIndex);
			rep.setDestinataire(serverID0);
			rep.setRequestID(message.getRequestID());
			
			if (serverID0 == nodeIndex)
				treatCreateNode(rep, pid);
			else
				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
			
			String s_tmp1 = this.skey(path + "1");
			Message rep1 = new Message();
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID1 = ControlerNw.config_log.getTranslate().translate(s_tmp1);
			
			rep1.setType("createNode");
			rep1.setPath(s_tmp1);
			rep1.setData(l1);
			rep1.setOption(path + "1");
			rep1.setSource(nodeIndex);
			rep1.setDestinataire(serverID1);
			rep1.setRequestID(message.getRequestID());
			
			System.out.println(s_tmp0 + " " + path + "0");
			System.out.println(s_tmp1 + " " + path + "1");
			
			if (serverID1 == nodeIndex)
				treatCreateNode(rep, pid);
			else
				t.send(Network.get(nodeIndex), Network.get(serverID1), rep1, pid);				
		}
		createNode_OK += 2;
	}
	
	private void treatCreateNode_OK(Message message, int pid) throws ErrorException
	{			
		createNode_OK--;
		System.out.println(nodeIndex + " recu " + message.getSource());
		if (this.fifo.isEmpty())
		{
			ok = true;
			return;
		}
		else if (createNode_OK == 0)
		{
		
/**//*	WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp1", true);
		wf.write(this.fifo.getFirst() + "\n");
		wf.close();
		*/
			this.fifo.removeFirst();
			this.launchRequest(pid);	
		}
	}
	
	@SuppressWarnings("unchecked")
	private void treatCreateNode(Message message, int pid) throws ErrorException
	{
		createNode_OK--;
		String path = (String) message.getPath();
		String realPath = (String) message.getOption();
		ArrayList<BF> arrayList = (ArrayList<BF>) message.getData();

		if (this.list.containsKey(path))
		{
			this.list.remove(path);
		}

		PHT_IndexNode n = new PHT_IndexNode(realPath);
		
		if (arrayList != null)
			for (int i = 0; i < arrayList.size(); i++)
				n.insert(arrayList.get(i));
		
		this.list.put(path, n);
		
		if (arrayList != null && arrayList.size() > Config.gamma)
		{
			System.out.println(this.skey(realPath) + " " + realPath + " zzzzzzzzzzz");
			Message rep = new Message();
			rep.setPath(realPath);
			rep.setData(arrayList);
			rep.setSource(nodeIndex);
			rep.setRequestID(message.getRequestID());
			
			this.split2(rep, pid);
			return;
		}

		if (message.getSource() != nodeIndex)
		{
			Message rep = new Message();
			rep.setType("createNode_OK");
			rep.setRequestID(message.getRequestID());
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setPath(path);
		
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void split2(Message message, int pid) throws ErrorException
	{		
		createNode_OK += 2;
		String path = (String) message.getPath();
		ArrayList<BF> listKeys = (ArrayList<BF>) message.getData();
		
		PHT_IndexNode n = this.list.get(this.skey(path));
		n.setLeafNode(false);
		n.setDataStore(null);
		
		String s_tmp0 = this.skey(path + "0");
		
		BF bf_tmp0 = new BF(path + "0");
		
		ArrayList<BF> l0 = new ArrayList<BF>();
		ArrayList<BF> l1 = new ArrayList<BF>();

		for (int j = 0; j < listKeys.size(); j++)
		{
			BF bf_tmp = listKeys.get(j);
			
			if (bf_tmp.equals(bf_tmp0))
			{
				l0.add(bf_tmp);
			}
			else
			{
				l1.add(bf_tmp);
			}
		}
		
		Message rep = new Message();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID0 = ControlerNw.config_log.getTranslate().translate(s_tmp0);
		
		rep.setType("createNode");
		rep.setPath(s_tmp0);
		rep.setData(l0);
		rep.setOption(path + "0");
		rep.setSource(message.getSource());
		rep.setDestinataire(serverID0);
		rep.setRequestID(message.getRequestID());
		
		if (serverID0 == nodeIndex)
			treatCreateNode(rep, pid);
		else
			t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
		
		String s_tmp1 = this.skey(path + "1");
		Message rep1 = new Message();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID1 = ControlerNw.config_log.getTranslate().translate(s_tmp1);
		
		rep1.setType("createNode");
		rep1.setPath(s_tmp1);
		rep1.setData(l1);
		rep1.setOption(path + "1");
		rep1.setSource(message.getSource());
		rep1.setDestinataire(serverID1);
		rep1.setRequestID(message.getRequestID());
		
		System.out.println(s_tmp0 + " " + path + "0");
		System.out.println(s_tmp1 + " " + path + "1");

		if (serverID1 == nodeIndex)
			treatCreateNode(rep1, pid);
		else
			t.send(Network.get(nodeIndex), Network.get(serverID1), rep1, pid);	
		
		System.out.println(nodeIndex + " vs " + serverID0 + " & " + serverID1);
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
			
	/**
	 * Traiter le message de la recherche.
	 * 
	 * @param message
	 * 	<li> indexName
	 * 	<li> {@code Hashtable<String, BF>}
	 * 
	 * @author dcs	
	 * @throws ErrorException 
	 * */
	@SuppressWarnings("unchecked")

	private void treatSearch(Message message, int pid) throws ErrorException
	{		
		/*
		String indexName = message.getIndexName();
		int requestID = message.getRequestID();
		BF bf = message.getBF();
		BF key = bf.getKey(ControlerNw.config_log.sizeOfKey);
						
		if (nodeIndex == message.getSource())
		{									
			if (!ControlerNw.search_log.containsKey(requestID))
			{
				Config config = new Config();
				config.setTime(Calendar.getInstance().getTimeInMillis());
				
				Object[] o = new Object[2];
				int[] recu = new int[Network.size()];
				int[] total = new int[Network.size()];
				
				for (int i = 0; i < Network.size(); i++)
				{
					recu[i] = 0;
					total[i] = 0;
				}
				
				o[0] = recu;
				o[1] = total;
				
				config.putListAnswer(requestID, o);
				ControlerNw.search_log.put(requestID, config);
				
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + requestID, true);
				wf1.write(date + "       Source : " + message.getSource() + "\n");
				wf1.close();
			}
		}
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (this.listSystemIndex.containsKey(indexID))
		{
			SystemIndex systemIndexP2P = (SystemIndex) this.listSystemIndex.get(indexID);
			
			Object o = systemIndexP2P.search(key);	
			
			if (o == null)
			{
				//******************Temps de calcul**********************
				long temps = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();

				ControlerNw.search_log.remove(requestID);
				
				ControlerNw.config_log.addTimeCalcul(requestID, temps);
				//*******************************************************
				return;
			}
			
			if (o.getClass().getName().contains("BF"))
			{
				BF key_tmp = (BF) o;
				
				ControlerNw.config_log.getTranslate().setLength(Network.size());
				int serverID = ControlerNw.config_log.getTranslate().translate(key_tmp.toString());
				
				Message rep = new Message();
				
				rep.setIndexName(message.getIndexName());
				rep.setType("GET");
				rep.setBF(message.getBF());
				rep.setKey(key_tmp);
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID);
				rep.setRequestID(message.getRequestID());
				rep.setOption("SEARCH");
				
				t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
				
				Object[] o_tmp = (Object[]) ControlerNw.search_log.get(requestID).getListAnswer(requestID);
				int[] total = (int[]) o_tmp[1];
				total[serverID] = total[serverID] + 1;
				
				//******************Temps de calcul**********************
				long temps = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();

				ControlerNw.config_log.addTimeCalcul(requestID, temps);
				//*******************************************************
				return;
			}
			
			Iterator<BF> iterator = ((ArrayList<BF>)o).iterator();
						
			System.out.println("Nombre de requête GET : " + ((ArrayList<BF>)o).size());
			
			Object[] o_tmp = (Object[]) ControlerNw.search_log.get(requestID).getListAnswer(requestID);
			int[] total = (int[]) o_tmp[1];
						
			while (iterator.hasNext())
			{
				BF key_tmp = iterator.next();
				
				ControlerNw.config_log.getTranslate().setLength(Network.size());

				int serverID = ControlerNw.config_log.getTranslate().translate(key_tmp.toString());
				
				Message rep = new Message();
				
				rep.setIndexName(message.getIndexName());
				rep.setType("GET");
				rep.setBF(message.getBF());
				rep.setKey(key_tmp);
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID);
				rep.setRequestID(message.getRequestID());
				rep.setOption("SEARCH");
				
				t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
				
				total[serverID] = total[serverID] + 1;
			}
		
			//******************Temps de calcul**********************
			long temps = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();

			ControlerNw.config_log.addTimeCalcul(requestID, temps);
			//*******************************************************
		}
		*/
	}
	
	/**
	 * Traiter le message de la recherche exacte.
	 * 
	 * @param message
	 * 	<li> indexName
	 * 	<li> path
	 * 	<li> bf
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	private void treatSearchExact(Message message, int pid) throws ErrorException
	{		
		/*
		String indexName = message.getIndexName();
		int requestID = message.getRequestID();
		BF key = message.getKey();
						
		if (nodeIndex == message.getSource())
		{									
			if (!ControlerNw.search_log.containsKey(requestID))
			{
				Config config = new Config();
				config.setTime(Calendar.getInstance().getTimeInMillis());
				
				ControlerNw.search_log.put(requestID, config);
				
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + requestID, true);
				wf1.write(date + "       Source : " + message.getSource() + "\n");
				wf1.close();
			}
		}
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (this.listSystemIndex.containsKey(indexID))
		{
			SystemIndex systemIndexP2P = (SystemIndex) this.listSystemIndex.get(indexID);
			
			Object o = systemIndexP2P.searchExact(key);	
			
			if (o == null)
			{
				//******************Temps de calcul**********************
				long temps = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();

				ControlerNw.search_log.remove(requestID);
				
				ControlerNw.config_log.addTimeCalcul(requestID, temps);
				//*******************************************************
				
				return;
			}
			
			BF key_tmp = (BF) o;
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(key_tmp.toString());
			
			Message rep = new Message();
			
			rep.setIndexName(message.getIndexName());
			rep.setType("GET");
			rep.setBF(message.getBF());
			rep.setKey(key_tmp);
			rep.setSource(nodeIndex);
			rep.setDestinataire(serverID);
			rep.setRequestID(message.getRequestID());
			rep.setOption("SEARCH_EXACT");
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
			
			//******************Temps de calcul**********************
			long temps = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(requestID).getTime();

			ControlerNw.config_log.addTimeCalcul(requestID, temps);
			//*******************************************************
		}
		*/
	}

	/**
	 * Capturer l'état du système.
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
		
	private void treatOverview(Message message, int pid) throws ErrorException
	{	
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(message.getIndexName());

				ControlerNw.config_log.setNodePerServer(nodeIndex, list.size());		

				Enumeration<String> enumeration = this.list.keys();
				
				while (enumeration.hasMoreElements())
				{
					String s_tmp = enumeration.nextElement();
					PHT_IndexNode n = this.list.get(s_tmp); 
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
	}
		
}











