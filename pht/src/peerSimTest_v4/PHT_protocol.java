package peerSimTest_v4;

import java.util.ArrayList;
import java.util.Hashtable;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import pht_v3_1_3.BF;

public class PHT_protocol implements EDProtocol{

	public PHT_protocol()
	{
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
				
			case "PUT": // PUT, indexName, BF, path
				treatPUT(message, pid);
				break;
				
			case "GET": //GET, indexName, path
				treatGET(message, pid);
				break;
				
			case "GET_Response":
				treatGet_Response(message, pid);
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
				
			case "searchResponse":
				treatSearchResponse(message, pid);
				break;
				
			case "searchExact":
				try {
					treatSearchExact(message, pid);
				} catch (ErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case "searchExactResponse":
				treatSearchExactResponse(message, pid);
				break;
				
			default : 
				break;
			}
		}
	}

}
