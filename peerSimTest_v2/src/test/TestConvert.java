package test;

public class TestConvert {

	public static String computeEntry(String path)
	{
		String s_tmp = new String();
		
		if (path.length() > 1)
		{
			char[] tmp = path.toCharArray();
			
			int i = 0;
			for (i = tmp.length - 1; i > 0; i--)
			{
				if (tmp[i] != tmp[i - 1])
					break;
			}
			
			if (i != -1)
				s_tmp = path.substring(0, i + 1);
		}
		else
		{
			return path;
		}
		
		return s_tmp;
	}

	public static void main(String[] args) {
		System.out.println(computeEntry("01101011111"));
		System.out.println(computeEntry("01101000000"));
		System.out.println(computeEntry("01101010101"));
		System.out.println(computeEntry("01101010110"));
		System.out.println(computeEntry("0"));
		System.out.println(computeEntry("1"));
		System.out.println(computeEntry("011"));
		System.out.println(computeEntry("100"));



	}
	
}
