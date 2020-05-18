package router;

public class Router
{
    public static final int brokerPort = 5000;
	public static final int marketPort = 5001;
		
	private static String brokerMessages = "";
	private static String marketMessages = "";
	
     public static void main(String[] args) 
	 {
		Server broker= new Server(brokerPort, Component.Broker);
		Server market = new Server(marketPort, Component.Market);
		
		broker.start();
		market.start();
		 
		while (true)
		{
			try
			{
				brokerMessages = broker.getMessages();

				if (brokerMessages.isEmpty())
				{	
					System.out.println("No messages");	
				}
				else
				{
					String[] arr = brokerMessages.split("\\|");
					String id = String.format("56=%s", market.getComponentId());
					
					market.sendMessage(arr[0]+"|"+arr[1]+"|"+arr[2]+"|"+arr[3]+"|"+arr[4]+"|"+arr[5]+"|"+id+"|"+arr[7]+"|"+arr[8]+"|"+arr[9]+"|"+arr[10]+"|"+arr[11]+"|"+arr[12]+"|"+"|"+arr[13]+"|"+arr[14]+"|"+arr[15]+"|");
					
					brokerMessages = "";
				}

				System.out.println("Processing order...");
				
				marketMessages = market.getMessages();
				
				if (marketMessages.isEmpty())
				{
					broker.sendMessage(marketMessages);
				}
				else
				{
					broker.sendMessage(marketMessages);
					marketMessages = "";
					
					System.out.println("Order processed");
				}

			}
			catch(Exception e) { }
		}
     }
}
