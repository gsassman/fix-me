package router;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class Router
{
    public static final int brokerPort = 5000;
    public static final int marketPort = 5001;	
	static private String brokerMessages = "";
	static private String marketMessages = "";
	
    /*Colours*/
    public static final String GREEN = "\u001B[32m";
    public static final String RESET_CO = "\u001B[0m";
	public static final String RED = "\u001B[31m";
 	/*Colours*/

     public static void main(String[] args) 
	 {
         Server broker= new Server(brokerPort, "BROKER");
         broker.start();
         Server market = new Server(marketPort, "MARKET");
         market.start();
		 
		    while (true) {
            try {
                
               brokerMessages = broker.getMessages();
                if(brokerMessages.isEmpty())
				{	
					System.out.println("nothing to send");	
				}
				else
				{
					String[] arr = brokerMessages.split("\\|");
					String temp = "56="+market.getID();
					market.sendMessage(arr[0]+"|"+arr[1]+"|"+arr[2]+"|"+arr[3]+"|"+arr[4]+"|"+arr[5]+"|"+temp+"|"+arr[7]+"|"+arr[8]+"|"+arr[9]+"|"+arr[10]+"|"+arr[11]+"|"+arr[12]+"|"+"|"+arr[13]+"|"+arr[14]+"|"+arr[15]+"|");
					brokerMessages = "";
				}
				System.out.println(RED+"Your Order is being processed"+RESET_CO);
			    marketMessages = market.getMessages();
				
				if(marketMessages.isEmpty())
				{
					broker.sendMessage(marketMessages);
					
				}
				else
				{
					System.out.println(GREEN+"Order processed and sent"+RESET_CO);
					broker.sendMessage(marketMessages);
					marketMessages = "";
				}

			}
			catch(Exception e)
			{
				
			}
			}
     }
}