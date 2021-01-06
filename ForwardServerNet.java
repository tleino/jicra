/* ForwardServer - Simple port forwarding server written in Java
 * Copyright (C) 2000  Tommi leino <namhas@taikatech.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.io.*;
import java.net.*;

/**
 * This class handles all the network stuff.
 * 
 * @author namhas
 * @version 1.0
 */

public class ForwardServerNet extends Thread implements Runnable {
    public boolean connected;
    private Socket socket;
    private Thread thread;
    private BufferedWriter os;
    private BufferedReader is;
    private PrintWriter out;
    
    /** Start a thread. */
    public void start() {
	if (thread == null) {
	    clear();
		 
	    thread = new Thread(this);
	    thread.start();
	}
    }
   
    /** The main loop. */
    public void run() {
	String buf;
	  	  
	while (Thread.currentThread() == thread) {
	    try {
		buf = is.readLine();
			
		if (buf == null)
		    return;
			
		out.write(buf + "\r\n");
		out.flush();
	    } catch (IOException e) {
		System.out.println("Error: " + e);
		return;
	    }
	}		
    }
   
    /** Connect to the server. */
    public synchronized void connect(String host, int port, PrintWriter o) {
	if (connected)
	    return;
	  		
        out = o;

	try {
	    socket = new Socket(host, port);
	} catch (UnknownHostException e) {
	    System.out.println("Error: " + e);
	    return;
	} catch (IOException e) {
	    System.out.println("Error: " + e);
	    return;
	}
	  
	try {
	    os = new BufferedWriter(new
		OutputStreamWriter(socket.getOutputStream()));
	    is = new BufferedReader(new
		InputStreamReader(socket.getInputStream()));
	} catch (IOException e) {
	    System.out.println("Error: " + e + "\n");
	    return;
	}

	if (os != null && is != null) {
	    connected = true;
	    notify();
		 
	    if (thread == null)
		start();
	}
    }
   
    /** Send a message to the server. */
    public void send(String data) {
	try {
	    os.write(data);
	    os.flush();
	} catch (IOException e) {
	    System.out.println("Error: " + e);
	    return;
	} catch (NullPointerException e) {
	    System.out.println("Error: " + e);
	}
    }
   
    /** Disconnect. */
    public synchronized void disconnect() {
	connected = false;
	os = null;
	is = null;
	out = null;
	thread.stop();
	thread = null;
	try {
	socket.close();
	} catch (IOException e) {
	    System.out.println("Error: " + e);
	}
    }

    /** Clear variables. */
    public void clear() {
    }
}
