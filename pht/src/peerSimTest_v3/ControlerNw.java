package peerSimTest_v3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class ControlerNw implements Control {
	private static final String PAR_PROTOCOL = "protocol";
	
	public static Config config_log = new Config();
	public static Hashtable<Integer, Config> search_log = new Hashtable<Integer, Config>();

	@SuppressWarnings("unused")
	private String prefix;
	private int pid;
	private boolean ok = true, ok2 = true, ok3 = false;
	private int line = 0;
	
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
			Message message = new Message();

			 n = Network.get(23);
			 message.setType("createIndex");
			 message.setIndexName("dcs");
			 message.setSource(23);
			 message.setDestinataire(23);
		
			 ok = false;
			 EDSimulator.add(0, message, n, pid);
		}
		else if (ok2)
		{
			System.out.println("Lecture n°1");
			n = Network.get(23);
			try(BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60")))
			{
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
						message.setDestinataire(23);
						line++;
						ControlerNw.config_log.addTotalFilterCreated(1);;
						EDSimulator.add(0, message, n, pid);
					}
					
					if (line == 1600000)
						break;
				}
				reader.close();
				ok2 = false;
				
				/**************/
				ok3 = true;
		//		Config.ObserverNw_OK = true;
				/**************/
				
				System.out.println("Fini de lecture " + line + " lignes");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (ok3)
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
			
		return false;
	}

}
