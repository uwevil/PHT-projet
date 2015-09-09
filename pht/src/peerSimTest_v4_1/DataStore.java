package peerSimTest_v4_1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Lieu où on stocke les filtres, géré par un nœud.
 * */

public class DataStore implements Serializable{

	private static final long serialVersionUID = 1L;
	private ArrayList<BF> listBFs;
	
	/**
	 * Initialise {@link DataStore}.
	 * */
	public DataStore()
	{
		this.listBFs  = new ArrayList<BF>();
	}
	
	/**
	 * Retourne la liste des filtres stockés.
	 * */
	public ArrayList<BF> getListBFs()
	{
		return this.listBFs;
	}
	
	/**
	 * Remplace la liste des filtres stockés par une autre.
	 * */
	public void setListBFs(ArrayList<BF> listBFs)
	{
		this.listBFs = listBFs;
	}
	
	/**
	 * Ajoute un filtre dans {@link DataStore}.
	 * */
	@SuppressWarnings("static-access")
	public void insert(BF bf)
	{
		if (this.listBFs.contains(bf))
			return;
		
		ControlerNw.config_log.totalFilterAdded++;
		this.listBFs.add(bf);
	}
	
	/**
	 * Retourne le nombre de filtres stockés dans {@link DataStore}.
	 * */
	public int size()
	{
		return this.listBFs.size();
	}

	public String toString()
	{
		return this.listBFs.toString();
	}

	
}
