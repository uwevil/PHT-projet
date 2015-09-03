package peerSimTest_v4_1;

import peersim.config.Configuration;
import peersim.core.Control;

public class ObserverNw_tmp implements Control {

	private static final String PAR_PROTOCOL = "protocol";
	@SuppressWarnings("unused")
	private int pid;
	
	public ObserverNw_tmp(String prefix)
	{
		pid = Configuration.getPid(prefix+ "." + PAR_PROTOCOL);
	}
	
	@SuppressWarnings("static-access")
	@Override
	public boolean execute() {
		
		if (!ControlerNw.config_log.ObserverNw_tmp_OK)
			return false;
		
		for (int i = 0; i < ControlerNw.config_log.listRequestID.size(); i++)
		{
			long requestID = ControlerNw.config_log.listRequestID.get(i);
			
			WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG_resultat + requestID + "_tmp.xml", false);
			String s = "<nbRetrieves>" + ControlerNw.config_log.getRetrieves(requestID) + "</nbRetrieves>\n";
			s += "<nbGetStatus>" + ControlerNw.config_log.getGetStatus(requestID) + "</nbGetStatus>\n";
			s += "<totalFiltersRetrieved>" + ControlerNw.config_log.getNbFiltersRetrieved(requestID) + "</totalFiltersRetrieved>\n";
			wf.write(s);
			wf.close();	
		}
		
		return false;
	}

}
