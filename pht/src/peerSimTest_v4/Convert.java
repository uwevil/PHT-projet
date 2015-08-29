package peerSimTest_v4;

import java.util.Hashtable;

public class Convert {

	private Hashtable<Long, Long> h1 = new Hashtable<Long, Long>();
	private Hashtable<Long, Long> h2 = new Hashtable<Long, Long>();

	public Convert()
	{
	}
	
	public void add (long a, long b)
	{
		if (this.h1.containsKey(a) || this.h2.containsKey(b))
		{
			return;
		}
		
		h1.put(a, b);
		h2.put(b, a);
	}
	
	public long get(long a)
	{
		if (this.h1.containsKey(a))
			return this.h1.get(a);
		if (this.h2.containsKey(a))
			return this.h2.get(a);
		
		return a;
	}

}
