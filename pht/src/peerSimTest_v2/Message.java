package peerSimTest_v2;

/**Type de message :
 * <ul>
 * 	<p>
 * 	<li> createIndex
 * 	<li> removeIndex
 * 	<li> add
 * 	<li> remove
 * 	<li> search
 * 	</p>
 * </ul>
 * <ul>
 * 	<p> 
 * 	<li> createNode
 * 	<li> removeNode
 * 	</p>
 * </ul>
 * <ul>
 * 	<li> OK
 * </ul>
 * 
 **/
public class Message {
	
	private String indexName;
	private String type = "";
	private Object data = null;
	private String path;
	private int src;
	private int dest;
	private int requestID;
	private Object option = null;
	
	/**Type de message :
	 * <ul>
	 * 	<p>
	 * 	<li> createIndex
	 * 	<li> removeIndex
	 * 	<li> add
	 * 	<li> remove
	 * 	<li> search
	 * 	</p>
	 * </ul>
	 * <ul>
	 * 	<p> 
	 * 	<li> createNode
	 * 	<li> removeNode
	 * 	</p>
	 * </ul>
	 * <ul>
	 * 	<li> OK
	 * </ul>
	 * 
	 **/
	public Message()
	{
	}
	
	/**Type de message :
	 * <ul>
	 * 	<p>
	 * 	<li> createIndex
	 * 	<li> removeIndex
	 * 	<li> add
	 * 	<li> remove
	 * 	<li> search
	 * 	</p>
	 * </ul>
	 * <ul>
	 * 	<p> 
	 * 	<li> createNode
	 * 	<li> removeNode
	 * 	</p>
	 * </ul>
	 * <ul>
	 * 	<li> OK
	 * </ul>
	 * 
	 **/
	public Message(String type, String indexName, String path, int src, int dest, Object data)
	{
		this.indexName = indexName;
		this.type = type;
		this.data = data;
		this.src = src;
		this.dest = dest;
		this.path = path;
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public void setIndexName(String indexName)
	{
		this.indexName = indexName;
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public Object getData()
	{
		return this.data;
	}
	
	public void setData(Object data)
	{
		this.data = data;
	}
	
	public int getSource()
	{
		return this.src;
	}
	
	public void setSource(int src)
	{
		this.src = src;
	}
	
	public int getDestinataire()
	{
		return this.dest;
	}
	
	public void setDestinataire(int dest)
	{
		this.dest = dest;
	}
	
	public int getRequestID()
	{
		return this.requestID;
	}
	
	public void setRequestID(int requestID)
	{
		this.requestID = requestID;
	}
	
	public Object getOption()
	{
		return this.option;
	}
	
	public void setOption(Object option)
	{
		this.option = option;
	}
	
	
	public String toString()
	{
		return "Message \n  "
				+ "Type         : " + this.getType() + "\n  "
				+ "Index        : " + this.indexName + "\n  "
				+ "Path         : " + this.path      + "\n  "
				+ "Data         : " + this.getData() + "\n  "
				+ "Source       : " + this.getSource() + "\n  "
				+ "Destinataire : " + this.getDestinataire() + "\n  "
				+ "RequestID    : " + this.requestID + "\n  "
				+ "Option       : " + this.option + "\n";
	}
	
}
