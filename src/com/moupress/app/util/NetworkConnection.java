package com.moupress.app.util;

import java.net.InetAddress;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkConnection {

	String hostName;
	Context ctx;
	
	public NetworkConnection (String host, Context context)
	{
		hostName = host;
		ctx = context;
	}
	
	/**
	 * Convert Host Name to integer IP address
	 * @param hostname host address
	 * @return
	 */
	public  int lookupHost() {
		
	    InetAddress inetAddress;
	    try {
	        inetAddress = InetAddress.getByName(hostName);
	    } catch (Exception e) {
	        return -1;
	    }
	    byte[] addrBytes;
	    int addr;
	    addrBytes = inetAddress.getAddress();
	    addr = ((addrBytes[3] & 0xff) << 24)
	            | ((addrBytes[2] & 0xff) << 16)
	            | ((addrBytes[1] & 0xff) << 8)
	            |  (addrBytes[0] & 0xff);
//	    addr = ipToInt(inetAddress.getHostAddress());
	    return addr;
	}
	
	/**
	 * @return true connected to Internet
	 *         false not connected Internet
	 */
	public boolean checkInternetConnection() {
		System.out.println("Before get System Service !");
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    System.out.println("After get System Service !");
	    // test for connection
	    if (cm.getActiveNetworkInfo() != null
	            && cm.getActiveNetworkInfo().isAvailable()
	            && cm.getActiveNetworkInfo().isConnected()
	            && cm.getBackgroundDataSetting()
	           // && cm.requestRouteToHost(cm.getActiveNetworkInfo().getType(), lookupHost())
	            ) {
	    	
	        return true;
	    } else {
	        return false;
	    }
	}
}
