package peerSimTest_v3;

import java.text.SimpleDateFormat;
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
	
	private Hashtable<Integer, SystemIndex> listSystemIndex = new Hashtable<Integer, SystemIndex>();
	private Hashtable<Integer, Hashtable<String, ArrayList<BF>>> database = new Hashtable<Integer, Hashtable<String,ArrayList<BF>>>();
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	
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
				
		switch(message.getType())
		{
		case "createIndex": //createIndex, Name, sourceID, descID, option
			treatCreateIndex(message, pid);		
			break;
			
		case "removeIndex": //removeIndex, Name
			treatRemoveIndex(message, pid);
			break;
			
		case "add": // add, Name, path, BF
			try {
				treatAdd(message, pid);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "PUT": // PUT, indexName, BF, path
			treatPUT(message, pid);
			break;
			
		case "GET": //GET, indexName, path
			treatGET(message, pid);
			break;
			
		case "GET_OK":
			treatGet_OK(message, pid);
			break;
			
		case "remove": //remove, Name, BF
			treatRemove(message, pid);
			break;
			
		case "search": //search, Name, tableau contient BF et liste des paths
			try {
				treatSearch(message, pid);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "searchExact":
			try {
				treatSearchExact(message, pid);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "overview": // nœud 0 balance la requete vers tous les autres nœuds
			try {
				treatOverview(message, pid);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
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
	 * Traiter le message de création d'un système index.
	 * 
	 * @author dcs
	 * */
	
	private void treatCreateIndex(Message message, int pid)
	{
		String indexName = message.getIndexName();
				
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (!listSystemIndex.containsKey(indexID)) //not contains indexID
		{
			SystemIndex systemIndex =  new SystemIndex(indexName, nodeIndex);
			
			listSystemIndex.put(indexID,systemIndex);
		}	
	}
	
	/**
	 * Traiter le message d'ajout dans le système.
	 *
	 * @param message
	 * 	<li> indexName
	 * 	<li> Path
	 * 	<li> bf
	 * 
	 * @param pid
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	private void treatAdd(Message message, int pid) throws ErrorException
	{
		String indexName = message.getIndexName();
		BF bf = (BF) message.getBF();
		BF key = bf.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(key.toString());
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);

		Message rep = new Message();
		
		rep.setIndexName(indexName);
		rep.setType("PUT");
		rep.setKey(key);
		rep.setBF(bf);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		
		if (this.listSystemIndex.containsKey(indexID))
		{
			SystemIndex systemIndex = (SystemIndex)this.listSystemIndex.get(indexID);
			systemIndex.add(key);
		}
		
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
	 * */
	
	private void treatPUT(Message message, int pid)
	{
		String indexName = message.getIndexName();
		BF bf = (BF) message.getBF();
		BF key = message.getKey();
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (!this.database.containsKey(indexID))
		{
			Hashtable<String, ArrayList<BF>> data = new Hashtable<String, ArrayList<BF>>();
			ArrayList<BF> arrayList = new ArrayList<BF>();
			
			arrayList.add(bf);
			
			data.put(key.toString(), arrayList);
			
			this.database.put(indexID,data);
			
			return;
		}
		
		Hashtable<String, ArrayList<BF>> data = 
				(Hashtable<String, ArrayList<BF>>) this.database.get(indexID);
		
		if (data.containsKey(key.toString()))
		{
			data.get(key.toString()).add(bf);
		}
		else
		{
			ArrayList<BF> arrayList = new ArrayList<BF>();
			arrayList.add(bf);
			data.put(key.toString(), arrayList);
		}
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
		String indexName = message.getIndexName();
		BF key = message.getKey();
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (this.database.containsKey(indexID))
		{			
			Hashtable<String, ArrayList<BF>> data = (Hashtable<String, ArrayList<BF>>) this.database.get(indexID);
			ArrayList<BF> arrayList = data.get(key.toString());
						
			Message rep = new Message();
			
			rep.setIndexName(indexName);
			rep.setType("GET_OK");
			rep.setKey(key);
			rep.setBF(message.getBF());
			rep.setRequestID(message.getRequestID());
			rep.setData(arrayList);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setOption(message.getOption());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		}
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
		String indexName = message.getIndexName();
		int requestID = message.getRequestID();
		BF bf = message.getBF();
		BF key = bf.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);
						
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
	}
	
	/**
	 * Traiter le message de suppression d'un système index.
	 * <p>
	 * Le nœud qui gère la racine diffuse aux autres pour supprimer le système d'index.
	 * 
	 * @author dcs
	 **/
	private void treatRemoveIndex(Message message, int pid)
	{
		String indexName = message.getIndexName();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (serverID == nodeIndex)
		{	
			if (listSystemIndex.containsKey(indexID))
			{
				listSystemIndex.remove(indexID);
				
				Message rep = new Message();

				rep.setType("removeIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("REMOVED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}
			else
			{
				Message rep = new Message();

				rep.setType("removeIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("NOT_EXISTED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}
						
			for (int i = 0; i < Network.size(); i++)
			{
				Message rep = new Message();

				rep = new Message();
				rep.setType("removeIndexSuite");
				rep.setIndexName(indexName);
				rep.setSource(message.getSource());
				rep.setDestinataire(i);
				
				if (i != nodeIndex)
					t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
			
			/*
			//********test********
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("removeIndex "+ indexID+ " node "+ nodeIndex  + "\n"
					+ rep.toString()
					+ "\n");
			wf.close();
			//********************
			*/
		}
		else // forward
		{
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
			/*
			//********test********
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("removeIndex forward "+ indexID+ " node "+ nodeIndex  + "\n"
					+ message.toString()
					+ "\n");
			wf.close();
			//********************
		*/
		}	
	}
	
	
	
	/**
	 * Traiter le message de suppression d'un filtre du système index
	 * 
	 * @author dcs
	 * */
	private void treatRemove(Message message, int pid)
	{
		/*
		String indexName = message.getIndexName();
		BF bf = (BF) message.getData();
		String path = (String) message.getPath();
		int requestID = message.getRequestID();
				
		if (message.getData() == null)
		{
			Message rep = new Message();
			rep.setType("remove_KO");
			rep.setIndexName(indexName);
			rep.setPath(path);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData("list path ou path == null");
			rep.setRequestID(requestID);
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			return;
		}
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (this.listSystemIndex.containsKey(indexID))
		{
			SystemIndex systemIndexP2P = (SystemIndex) this.listSystemIndex.get(indexID);
			Object o = systemIndexP2P.remove(bf, path);
			treatRemove(o, indexName, message, pid);
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
		
		if (this.listSystemIndex.containsKey(indexID))
		{
			// procedure
			SystemIndex systemIndex = (SystemIndex) this.listSystemIndex.get(indexID);
			
			if (systemIndex != null)
			{
				ControlerNw.config_log.setNodePerServer(nodeIndex, systemIndex.size());		

				ArrayList<SystemNode> arrayList = systemIndex.getListNode();
				Iterator<SystemNode> iterator = arrayList.iterator();
				
				while (iterator.hasNext())
				{
					SystemNode s = iterator.next();
									
					if (s.isLeafNode())
					{
						ControlerNw.config_log.getFilterPerNode().put(s.getPath(), s.getListKey().size());
					}
									
					if (!ControlerNw.config_log.getIndexHeight().contains(s.getRang()))
						ControlerNw.config_log.getIndexHeight().put(s.getRang(), s.getPath());
				}
			}
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
		wf.write("Nombre de fragments : " + ControlerNw.config_log.numberOfFragment + "\n");
		wf.write("Taille du fragment  : " 
				+ ControlerNw.config_log.sizeOfBF/ControlerNw.config_log.numberOfFragment+ "\n");
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











