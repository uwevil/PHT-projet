package peerSimTest_v3_1_1;

public class BinaryArray
{
	char[] array;
	
	public BinaryArray(int nbbits)
	{
		array = new char[nbbits];
	}
	
	public void setFirstBits(int nb)
	{
		for(int i = 0; i < array.length; i++)
			array[i] = (i < nb) ? '1' : '0';
	}
	
	public boolean hasNext() {
		int accu = 0;
		for(int i = array.length - 1; i > 0; i--)
		{
			if (array[i] == '0' && array[i-1] == '1')
			{
				array[i] = '1';
				array[i-1] = '0';
				
				for(int j = 1; j <= accu; j++)
					array[i+j]='1';
				
				return true;
			}
			
			if (array[i] == '1')
			{
				array[i] = '0';
				accu++;
			}
		}
		return false;
	}
	
	public char[] next()
	{
		return array;
	}
	/*
	public static void main (String[] args) throws ErrorException
	{
		ArrayList<BF> res = new ArrayList<BF>();
		BinaryArray binaryArray = new BinaryArray(8);
		int j = 0;
		for (int i = 0; i <= 7; i++)
		{		
			binaryArray.setFirstBits(i);
			do
			{
				String s = String.copyValueOf(binaryArray.next());
				System.out.println(s);
				j++;
			//	res.add(new BF(s));
			} 
			while(binaryArray.hasNext());
		}
		System.out.println(j);
		System.out.println(res);
	}
	
	*/
}
