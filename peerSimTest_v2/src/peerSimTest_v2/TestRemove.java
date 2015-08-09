package peerSimTest_v2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class TestRemove implements Control {
	private static final String PAR_PROTOCOL = "protocol";
	private int pid;
	private boolean ok = true, ok2 = false, ok3 = false;
	private int line = 0;
	
	public TestRemove(String prefix)
	{
		pid = Configuration.getPid(prefix+ "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		
		if (ok)
		{
			ok = false;
			ok2 = true;
			return false;
		}
		else if (ok2 && Config.ObserverNw_OK )
		{
			Node n = Network.get(37);
			
			System.out.println("Begin remove");
			
			String essai = "ExactAll";
			String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
			Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/Essai" + essai + "/" + "_log";
			Config.peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date + "/Essai" + essai + "/" + "_resultat_log";
			Config.peerSimLOG_path = "/Users/dcs/vrac/test/" + date + "/Essai" + essai + "/" + "_path_log";
			
			try 
			{		
				ControlerNw.config_log.setConfig_OK(false);
				
				n = Network.get(23);
				BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60"));
				
				while (true)
				{
					String s = new String();
					s = reader.readLine();
					if (s == null)
						break;
					String[] tmp = s.split(";");
					
					if (tmp.length >= 2 && tmp[1].length() > 2 )
					{
						BFP2P bf_tmp = new BFP2P(Config.sizeOfBF);
						bf_tmp.addAll(tmp[1]);

						Message message = new Message();

						ControlerNw.config_log.getTranslate().setLength(Config.requestRang);
						int requestID = ControlerNw.config_log.getTranslate().translate(bf_tmp.toString());
						
				//		WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				//		wf.write(requestID + "\n");
				//		wf.close();
						
						message.setType("remove");
						message.setIndexName("dcs");
						message.setPath("/");
						message.setData(bf_tmp);
						message.setDestinataire(23);
						message.setSource(23);
						message.setRequestID(requestID);
						line++;

						EDSimulator.add(0, message, n, pid);
					}
					
					if (line == 1580)
						break;
				}
				reader.close();
				ok2 = false;
				ok3 = true;
				System.out.println("Fini de remove " + line + " lignes");	
			}	
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (ok3)
		{
			Node n = Network.get(23);
			
			System.out.println("Begin search Exact");
			
			try 
			{		
				BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60"));
				
				int i = 0;
				while (true)
				{
					String s = new String();
					s = reader.readLine();
					if (s == null)
						break;
					
					String[] tmp = s.split(";");
					
					if (tmp.length >= 2 && tmp[1].length() > 2 )
					{
						BFP2P bf_tmp = new BFP2P(Config.sizeOfBF);
						bf_tmp.addAll(tmp[1]);

						Message message = new Message();

						ControlerNw.config_log.getTranslate().setLength(Config.requestRang);
						int requestID = ControlerNw.config_log.getTranslate().translate(bf_tmp.toString());
						
						message.setType("searchExact");
						message.setIndexName("dcs");
						message.setData(bf_tmp);
						message.setPath("/");
						message.setRequestID(requestID);
						message.setSource(23);
						message.setDestinataire(23);	
						i++;
						EDSimulator.add(0, message, n, pid);
					}
					
					if (i == 1600)
						break;
				}
				reader.close();
				ok3 = false;
								
				System.out.println("Fini de searchExact " + i + " lignes");	
			}	
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (ControlerNw.config_log.getEnd_OK())
		{	
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_time", false);
			wf.write("RequeteID temps(ms)\n");
			
			Enumeration<Integer> enumeration = ControlerNw.config_log.getTimeGlobal().keys();
			long time = 0;
			int size = 0;
			
			while (enumeration.hasMoreElements())
			{
				Integer i = enumeration.nextElement();
				
				long tmp = ControlerNw.config_log.getTimeGlobal().get(i);
				time += tmp;
				if (i.toString().length() == 5)
				{
					wf.write(i + "   " + tmp + " ms\n");
				}
				else if (i.toString().length() == 4)
				{
					wf.write(i + "    " + tmp + " ms\n");
				}
				else if (i.toString().length() == 6)
				{
					wf.write(i + "  " + tmp + " ms\n");
				}
				size++;
			}
			
			long i = time/size;
			long hours = TimeUnit.MILLISECONDS.toHours(i);
			i -= TimeUnit.HOURS.toMillis(hours);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(i);
			i -= TimeUnit.MINUTES.toMillis(minutes);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(i);
			i -= TimeUnit.SECONDS.toMillis(seconds);
			
			wf.write("\n"
					+ "Temps total        : " + time + " ms\n"
					+ "Nombre de requetes : " + size + " requêtes\n"
					+ "Temps moyen        : " + time/size + " ms == "
					+ hours + " h " + minutes + " m " + seconds + " s " + i + " ms"
					+ "\n\n");
			
			wf.close();
			
			ControlerNw.config_log.setEnd_OK(false);
		}
		return false;
	}
}
