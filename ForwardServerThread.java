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

import java.net.*;
import java.io.*;

public class ForwardServerThread extends Thread {
    private Socket socket = null;

    public ForwardServerThread(Socket socket) {
	super("ForwardServerThread");
	this.socket = socket;
    }

    public void run() {
	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
						   new InputStreamReader(
						   socket.getInputStream()));

	    String inputLine = "", outputLine = "";
	    
	    ForwardServerNet net = new ForwardServerNet();
	    net.connect(ForwardServer.host, ForwardServer.rport, out);

	    while ((inputLine = in.readLine()) != null) {
		net.send(inputLine + "\r\n");
	    }
	    net.disconnect();
	    out.close();
	    in.close();
	    socket.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
