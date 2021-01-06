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

/**
 * The protocol class. This class parses the protocol.
 *
 * @author namhas
 * @version 1.0
 */

public class Protocol {
    private static int nick_count = 0;

    /** Parse the protocol string. */
    public static void parse(String buf) {
	String prefix = "", command = "", params = "";
	int new_i = 0, last_i = 0;
	   	
	if (buf.charAt(0) == ':') {
	    new_i = buf.indexOf(' ', last_i+1);
	    prefix = buf.substring(last_i+1, new_i);
	    last_i = new_i+1;
	}
	new_i = buf.indexOf(' ', last_i);
	command = buf.substring(last_i, new_i);
	last_i = new_i;

	new_i = buf.length();
	params = buf.substring(last_i+1, new_i);
	last_i = new_i;

	parseCommand (prefix, command, params);
    }
    
    /** Parse a command. */
    private static void parseCommand(String prefix, String command,
				     String params) {
	if (command.equals("001")) {
	    String from = "";
	    from = prefix.substring(0, prefix.length());
	    Jicra.nick = params.substring(0, params.indexOf(" "));
	    Jicra.net.send ("JOIN "+Jicra.channel+"");
	    nick_count = 0;
	    Jicra.text.append("*** Connected to IRC server "+from+" - Welcome!\n");
	    Jicra.text.append("*** Joining channel "+Jicra.channel+"...\n");
	}
	if (command.equals("251")) {
	}
	if (command.equals("NOTICE")) {
	    String from = "", to = "", what = "";
	    if (prefix.indexOf("!") != -1) {
		from = prefix.substring(0, prefix.indexOf("!"));
	    }
	    else {
		from = prefix;
	    }
	    to = params.substring(0, params.indexOf(":")-1);
            what = params.substring(params.indexOf(":")+1, params.length());
	    Jicra.text.append("-"+from+"- " + what + "\n");
	}   
	if (command.equals("PRIVMSG")) {
	    String from = "", to = "", what = "";
	    from = prefix.substring(0, prefix.indexOf("!"));
	    to = params.substring(0, params.indexOf(":")-1);
            what = params.substring(params.indexOf(":")+1, params.length());
	    if (what.charAt(0) == 1 && what.charAt(what.length()-1) == 1)
		parseCTCP(to, from, what.substring(1, what.length()-1));
	    else if (to.equals(Jicra.channel))
		Jicra.text.append("<"+from+"> " + what + "\n");
	    else
		Jicra.text.append("*"+from+"* " + what + "\n");
	}
	if (command.equals("PING")) {
	    Jicra.net.send("PONG "+params+"");
	}
	if (command.equals("JOIN")) {
	    String from = "";
	    from = prefix.substring(0, prefix.indexOf("!"));
	    if (from.equals(Jicra.nick)) {
		Jicra.channel = params.substring(params.indexOf(":")+1,
						 params.length());
	    } else {
		Jicra.text.append("*** " + from + " joined the channel.\n");
		Jicra.list.add(from);
	    }
	}
	if (command.equals("PART")) {
	    String from = "";
	    from =  prefix.substring(0, prefix.indexOf("!"));
	    if (from.equals(Jicra.nick)) {
		Jicra.channel = "";
		Jicra.list.removeAll();
	    } else {
		Jicra.text.append("*** " + from + " left the channel.\n");
		Jicra.list.remove(from);
		if (Jicra.query_nick != null &&
		    Jicra.query_nick.equals(from)) {
		    Jicra.text.append("*** No longer speaking in private.\n");
		    Jicra.query_nick = null;
		}
	    }
	}
	if (command.equals("QUIT")) {
	    String from = "";
	    from =  prefix.substring(0, prefix.indexOf("!"));
	    Jicra.text.append("*** " + from + " left the IRC network.\n");
	    Jicra.list.remove(from);
	    if (Jicra.query_nick != null && Jicra.query_nick.equals(from)) {
		Jicra.text.append("*** No longer speaking in private.\n");
		Jicra.query_nick = null;
	    }
	}
	if (command.equals("332") || command.equals("TOPIC")) {
	    Jicra.topic = params.substring(params.indexOf(":")+1,
					   params.length());
	    Jicra.text.append("*** Topic for channel "+Jicra.channel+": "+
			      Jicra.topic+ "\n");
	}
        if (command.equals("331")) {
	    Jicra.topic = "";
            Jicra.text.append("*** No topic is set for channel "+Jicra.channel+"\n");
        }
	if (command.equals("366")) {
	    Jicra.text.append("*** Channel "+Jicra.channel+" joined.\n");
	}
	if (command.equals("353")) {
	    int last_j = params.indexOf(":")+1, new_j = 0;
	    while (true) {
		new_j = params.indexOf(" ", last_j);
		if (new_j == -1)
		    break;
		if (params.substring(last_j, new_j).charAt(0) == '@')
		    Jicra.list.add(params.substring(last_j+1, new_j));
		else
		    Jicra.list.add(params.substring(last_j, new_j));
		last_j = new_j+1;
	    }
	}
	if (command.equals("465")) {
	    Jicra.text.append ("*** "+params.substring(1, params.length())+".\n");
	    Jicra.net.disconnect();
	}
	if (command.equals("433")) {
	    nick_count++;
	    Jicra.text.append ("*** Nickname "+Jicra.nick+
			       " is already in use, trying "+Jicra.nick+
			       ""+nick_count+" instead!\n");   
	    if (Jicra.nick.length() > 8 && nick_count == 1) {
		Jicra.nick = Jicra.nick.substring(0, 7);
	    } 
	    Jicra.net.send ("NICK "+Jicra.nick+""+nick_count+"");
	}
        if (command.equals("432")) {
	    Jicra.text.append("*** Nickname "+Jicra.nick+" is erroneous, trying to use guest nickname.\n");
	    Jicra.nick = "guest";
	    Jicra.net.send("NICK "+Jicra.nick+"");
	}
	if (command.equals("401")) {
	    Jicra.text.append ("*** "+
			       params.substring(0, params.indexOf(" "))+
			       " is no longer on the IRC.\n");
	    if (Jicra.query_nick != null && Jicra.query_nick.equals(params.substring(0, params.indexOf(" ")))) {
		Jicra.text.append("*** No longer speaking in private.\n");
		Jicra.query_nick = null;
	    }
	}
	if (command.equals("NICK")) {
	    int i=0;
	    String from = prefix.substring(0, prefix.indexOf("!"));
	    String to = params.substring(1, params.length());

	    Jicra.text.append("*** "+from+" is now known as "+to+".\n");
	    for (i=0;i<Jicra.list.getItemCount();i++) {
		if (Jicra.list.getItem(i).equals(from)) {
		    Jicra.list.replaceItem(to, i);
		    Jicra.list.select(i);
		    break;
		}
	    }

	    if (Jicra.query_nick != null && Jicra.query_nick.equals(from)) {
                Jicra.query_nick = to;
                Jicra.text.append("*** Now speaking to "+to+" in private.\n"); 
		Jicra.list.select(i);
	    }
	}
	if (command.equals("KICK")) {
	    String from = prefix.substring(0, prefix.indexOf("!"));
	    String channel = params.substring(0, params.indexOf(" "));
	    String to = params.substring(params.indexOf(" ")+1,
					 params.length());

	    if (to.indexOf(" ") != -1)
		to = to.substring(0, to.indexOf(" "));
	    Jicra.text.append("*** "+to+" is kicked out of the channel by "+
			      from+".\n");
	    Jicra.list.remove(to);
	    if (Jicra.query_nick != null && Jicra.query_nick.equals(to)) {
		Jicra.text.append("*** No longer speaking in private.\n");
		Jicra.query_nick = null;
	    }
	}   
	if (command.equals("ERROR")) {
	    Jicra.text.append("*** "+params.substring(1, params.length())+"\n");
	}
    }

    /** Parse a CTCP command. */
    private static void parseCTCP(String to, String from, String what) {
	if (what.equals("VERSION")) {
	    Jicra.net.send("NOTICE "+from+" :"+(char) 0x01+"VERSION "+
			   System.getProperty("java.vendor")+" "+
			   System.getProperty("java.version")+" "+
			   System.getProperty("os.name")+"/"+
			   System.getProperty("os.arch")+
			   " : Jicra "+Jicra.version+" "+
                           "http://www.majik3d.org/~namhas/jicra/"+
			   (char) 0x01);
	} else if (what.equals("CLIENTINFO")) {
	    Jicra.net.send("NOTICE "+from+" :"+(char) 0x01+
			   "CLIENTINFO CLIENTINFO VERSION ACTION"+(char) 0x01);
	} else if (what.length() > 5 &&
		   what.substring(0, 6).equals("ACTION")) {
	    Jicra.text.append("* " + from + " " +
			      what.substring(7, what.length()) + "\n");
	}
    }
}
