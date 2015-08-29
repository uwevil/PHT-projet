package peerSimTest_v4;

import java.io.Serializable;
import java.util.ArrayList;

public class DataStore implements Serializable{

	private static final long serialVersionUID = 1L;
	private ArrayList<BF> listKeys;
	
	public DataStore()
	{
		this.listKeys  = new ArrayList<BF>();
	}
	
	public ArrayList<BF> getListKeys()
	{
		return this.listKeys;
	}
	
	public void setListKey(ArrayList<BF> listKeys)
	{
		this.listKeys = listKeys;
	}
	
	@SuppressWarnings("static-access")
	public void insert(BF key)
	{
		if (this.listKeys.contains(key))
			return;
		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp", true);
		wf.write("Total filters added : " + ControlerNw.config_log.totalFilterAdded++ + "\n");
		wf.close();
		this.listKeys.add(key);
	}
	
	public int size()
	{
		return this.listKeys.size();
	}

	public String toString()
	{
		return this.listKeys.toString();
	}

	
}
