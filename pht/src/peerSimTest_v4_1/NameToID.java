package peerSimTest_v4_1;

import java.security.MessageDigest;

/**
 * Traduire une chaîne de caractères en nombre entier.
 * 
 * @author dcs
 * */
public class NameToID {
	
	private int range;
	
	/**
	 * Initialise avec l'interval de 0 à {@code range}, inclus.
	 * */
	public NameToID(int range) {
		// TODO Auto-generated constructor stub
		this.range = range;
	}
	
	/**
	 * Transforme une chaîne en nombre entier.
	 * */
	public int translate(String s)
	{
		MessageDigest md;
		int res = -1;
		try 
		{
			md = MessageDigest.getInstance("SHA-1");
			byte[] tmp = md.digest(s.getBytes("UTF-8"));
			double n = 0;

			for (int j = 0; j < tmp.length; j++)
			{
				n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.range + n;
			}
			res =  (int) (n % this.range);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Change l'interval.
	 * */
	public void setRange(int range)
	{
		this.range = range;
	}

}
