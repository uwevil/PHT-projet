package peerSimTest_v4;

import java.util.Hashtable;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class ControlerNw implements Control {
	private static final String PAR_PROTOCOL = "protocol";
	
	public static Config config_log = new Config();
	public static Hashtable<Long, Config> search_log = new Hashtable<Long, Config>();

	@SuppressWarnings("unused")
	private String prefix;
	private int pid;
	@SuppressWarnings("unused")
	private boolean ok = true, ok2 = true, ok3 = false;
	
	public ControlerNw(String prefix)
	{
		this.prefix = prefix;
		pid = Configuration.getPid(prefix+ "." + PAR_PROTOCOL);
	}
	
	
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		
		Node n; 

		if (ok)
		{
			System.out.println("Création de l'index");
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate("/");
			
			Message rep = new Message();
			
			rep.setType("createNode");
			rep.setPath("/");
			rep.setOption("/");
			rep.setDestinataire(serverID);
			
			EDSimulator.add(0, rep, Network.get(serverID), pid);
			 ok = false;
		}
		else if (ok2)
		{
			n = Network.get(23);
			
			Message message = new Message();
			message.setType("insertInit");
			message.setData(160000);
			message.setDestinataire(23);

			EDSimulator.add(0, message, n, pid);
			
			ok2 = false;
				
			/**************/
		//		ok3 = true;
		//		Config.ObserverNw_OK = true;
			/**************/			
		}
		/*else if (ok3)
		{
			System.out.println("Lecture les dernières lignes");

			n = Network.get(23);
			try(BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60")))
			{
				for (int i = 0; i < line; i++)
					reader.readLine();
				
				while (true)
				{
					String s = new String();
					s = reader.readLine();
					if (s == null)
						break;
					String[] tmp = s.split(";");
					
					if (tmp.length >= 2 && tmp[1].length() > 2 )
					{
						@SuppressWarnings("static-access")
						BF bf_tmp = new BF(config_log.sizeOfBF);
						
						bf_tmp.addAll(tmp[1]);
						Message message = new Message();

						message.setType("add");
						message.setIndexName("dcs");
						message.setBF(bf_tmp);
						message.setKey(bf_tmp.getKey(Config.sizeOfKey));
						message.setDestinataire(23);
						line++;
						ControlerNw.config_log.addTotalFilterCreated(1);;
						EDSimulator.add(0, message, n, pid);
					}
				}
				reader.close();
				ok3 = false;
				System.out.println("Fini de lecture " + line + " lignes");
				Config.ObserverNw_OK = true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
			*/
		return false;
	}

}
