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
import java.lang.*;

public class ForwardServer {
    public static String host;
    public static int rport;
    public static int lport;
    public static String version = "1.0.0";

    public static void main(String argv[]) throws IOException {
	ServerSocket serverSocket = null;
	boolean listening = true;

	System.out.println("ForwardServer "+version+" (c) 2000 Tommi Leino <namhas@taikatech.com>");

	if (argv.length == 3) {
	    host = argv[0];
	    Integer i = new Integer(argv[1]);
	    rport = i.intValue();
	    i = new Integer(argv[2]);
	    lport = i.intValue();
	    System.out.println("Forwarding local port "+lport+" to "+host+" "+rport);
	} else {
	    System.out.println("Usage: ForwardServer <host> <remote port> <local port>\n");
	    System.out.println("For example, use \"ForwardServer irc.stealth.net 6667 6667\" to forward local\nport 6667 to host irc.stealth.net's port 6667.");
	    System.exit(-1);
	}

	try {
	    serverSocket = new ServerSocket(lport);
	} catch (IOException e) {
	    System.err.println("Error: "+e);
	    System.exit(-1);
	}

	while (listening)
	    new ForwardServerThread(serverSocket.accept()).start();

	serverSocket.close();
    }
}
