package peerSimTest_v4;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

@SuppressWarnings("unused")
public class ObserverNw implements Control {

	private static final String PAR_PROTOCOL = "protocol";
	private int pid;
	private boolean ok = true, ok2 = false;
	private int experience = 0;
	
	public ObserverNw(String prefix)
	{
		pid = Configuration.getPid(prefix+ "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		if (ok && Config.ObserverNw_OK)
		{
			ok = false;
			ok2 = true;
			return false;
		}
		else if (ok2 && Config.ObserverNw_OK)
		{			
			System.out.println("Expérience n° " + experience);
			
			String essai = "0" + "_" + Config.version;
			String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
			Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/Essai" + essai 
					+ "/" + experience + "_log";
			Config.peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date + "/Essai" + essai 
					+ "/" + experience + "_resultat_log";
			Config.peerSimLOG_path = "/Users/dcs/vrac/test/" + date + "/Essai" + essai 
					+ "/" + experience + "_path_log";
			
			Node n = Network.get(23);
			
			Message message = new Message();
			message.setType("searchInit");
			message.setDestinataire(23);

			EDSimulator.add(0, message, n, pid);
			
				
			/*
			Message message = new Message();
			message.setIndexName("dcs");
			message.setSource(23);
			message.setDestinataire(23);
			
			message.setType("searchExact");
			
			BF bf = new BF(Config.sizeOfBF);
			
			bf.addAll("this,list,characters,ayn,rands,novel,atlas,shrugged");
			
			ControlerNw.config_log.getTranslate().setLength(Config.requestRang);
			int requestID = ControlerNw.config_log.getTranslate().translate(bf.toString());
			
			message.setPath("/");
			message.setData(bf);
			message.setRequestID(requestID);
			
			EDSimulator.add(0, message, n, pid);
			ok2 = false;
			ControlerNw.config_log.setExperience_OK(false);
			*/
		}
		
		if (ControlerNw.config_log.getEnd_OK())
		{
			/*
			//*******************
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_indexHeight", false);
			Enumeration<Integer> enumeration = ControlerNw.config_log.getIndexHeight().keys();
			
			while (enumeration.hasMoreElements())
			{
				Integer i = enumeration.nextElement();
				if (i <= 9)
				{
					wf.write(i + "  " + Config.indexHeight.get(i) + "\n");
				}
				else
				{
					wf.write(i + " " + Config.indexHeight.get(i) + "\n");
				}
			}
			
			wf.close();
			//*******************
			*/
			
			
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_time", false);
			wf.write("RequeteID temps(ms)\n");
			
			Enumeration<Integer> enumeration = ControlerNw.config_log.getTimeGlobal().keys();
			long time = 0, time2 = 0;
			int size = 0;
			
			while (enumeration.hasMoreElements())
			{
				Integer i = enumeration.nextElement();
				
				long tmp = ControlerNw.config_log.getTimeGlobal().get(i);
				time += tmp;
				time2 +=  ControlerNw.config_log.getTimeCalcul(i);
				
				if (i.toString().length() == 5)
				{
					wf.write(i + "   " + tmp + "  " + ControlerNw.config_log.getTimeCalcul(i) + "\n");
				}
				else if (i.toString().length() == 4)
				{
					wf.write(i + "    " + tmp + "  " + ControlerNw.config_log.getTimeCalcul(i) + "\n");
				}
				else if (i.toString().length() == 6)
				{
					wf.write(i + "  " + tmp + "  " + ControlerNw.config_log.getTimeCalcul(i) + "\n");
				}
				size++;
			}
			
			if (size == 0)
				size = 1;
			long i = time/size;
			long hours = TimeUnit.MILLISECONDS.toHours(i);
			i -= TimeUnit.HOURS.toMillis(hours);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(i);
			i -= TimeUnit.MINUTES.toMillis(minutes);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(i);
			i -= TimeUnit.SECONDS.toMillis(seconds);
			
			wf.write("\n"
					+ "Temps total        : " + time + " ms " + time2 + "ms\n"
					+ "Nombre de requetes : " + size + " requêtes\n"
					+ "Temps moyen        : " + time/size + " ms == "
					+ hours + " h " + minutes + "m" + seconds + "s" + i + "ms " + time2/size + "ms"
					+ "\n\n");
			
			wf.close();
			
			ControlerNw.config_log.setEnd_OK(false);
		}
		
		return false;
	}

}
