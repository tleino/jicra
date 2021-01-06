/* Jicra - Java IRC chat room applet
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

public class Net extends Thread implements Runnable {
    /** Are we connected or not? */
    public boolean connected;
    private Socket socket;
    private Thread thread;
    private BufferedWriter os;
    private BufferedReader is;
    
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
			
		Protocol.parse(buf);
	    } catch (IOException e) {
		Jicra.text.append("Error: " + e + "\n");
		return;
	    }
	}		
    }
   
    /** Connect to the server. */
    public synchronized void connect(String host) {
	if (connected)
	    return;
	  		
	try {
	    socket = new Socket(host, Jicra.port);		   
	} catch (UnknownHostException e) {
	    Jicra.text.append("Error: " + e + "\n");
	    return;
	} catch (IOException e) {
	    Jicra.text.append("Error: " + e + "\n");
	    return;
	}
	  
	try {
	    os = new BufferedWriter(new
		OutputStreamWriter(socket.getOutputStream()));
	    is = new BufferedReader(new
		InputStreamReader(socket.getInputStream()));
	} catch (IOException e) {
	    Jicra.text.append("Error: " + e + "\n");
	    return;
	}
	  
	if (os != null && is != null) {
	    connected = true;
	    notify();
		 
	    if (thread == null)
		start();
	}
    }
   
    /** Wait until we have connected. */
    private void waitConnect() {
	while (!connected) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		Jicra.text.append("Error: " + e + "\n");
		return;
	    }
	}
    }
   
    /** Send a message to the server. */
    public void send(String data) {
	try {
	    os.write(data + "\r\n");
	    os.flush();
	} catch (IOException e) {
	    Jicra.text.append("Error: " + e + "\n");
	    return;
	} catch (NullPointerException e) {
	    Jicra.text.append("Error: " + e + "\n");
	}
    }
   
    /** Disconnect. */
    public synchronized void disconnect() {
	send("QUIT :Leaving");

	connected = false;
	os = null;
	is = null;
	thread.stop();
	thread = null;
	try {
	socket.close();
	} catch (IOException e) {
	    Jicra.text.append("Error: " + e + "\n");
	}
	Jicra.list.removeAll();
    }

    /** Clear variables. */
    public void clear() {
    }
}
