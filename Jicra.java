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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

/**
 * The main class.
 *
 * @author namhas
 * @version 1.0
 */

public class Jicra extends Applet implements ActionListener, ItemListener {
    public static String version = "1.2.2";
    public static TextArea text = null;
    public static TextField input = null;
    public static List list = null;
    public static Net net = null;
    public static String channel = "";
    public static String topic = "";
    public static String nick = "";
    public static String realname = "";
    public static String user = "";
    public static int port = 0;
    public static String query_nick = null;
    
    public void init() {
	input = new TextField("", 80);
	list = new List();
	text = new TextArea("*** Jicra "+Jicra.version+
			    " (c) 2000 Tommi Leino <namhas@taikatech.com>\n",
			    20, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setFont(new Font("fixed", 0, 12));
	setLayout(gridBag);

	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.gridwidth = GridBagConstraints.RELATIVE;
	text.setFont(new Font("fixed", 0, 12));
	text.setEditable(false);
	gridBag.setConstraints(text, c);

	list.addItemListener(this);
	list.setFont(new Font("fixed", 0, 12));
	c.fill = GridBagConstraints.VERTICAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weighty = 1.0;
	c.weightx = 0.0;
	gridBag.setConstraints(list, c);

	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	c.weighty = 0.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	input.setFont(new Font("fixed", 0, 12));
	input.addActionListener(this);
	gridBag.setConstraints(input, c);

	add(text);
	add(list);
	add(input);

	setSize(703, 410);

	nick = getParameter("nick");
	realname = getParameter("realname");
	   
	if (nick.length() > 9) {
	   nick = nick.substring(0, 8);
	}
	nick = nick.replace(' ', '_');
	   
	if (getParameter("port") == null)
	    Jicra.port = 6667;
	else {
	    Integer i = new Integer(getParameter("port"));
	    Jicra.port = i.intValue();
	}

	if (getParameter("user") == null)
	    Jicra.user = Jicra.nick;
	else
	    Jicra.user = getParameter("user");
	
	net = new Net();
	text.append("*** Connecting IRC server...\n");
	net.connect(getCodeBase().getHost());
	net.send("USER "+user+" * * :"+realname+"");
	net.send("NICK "+nick+"");
	Jicra.channel = getParameter("channel");
    }
    
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == input && !e.getActionCommand().equals("") &&
	    e.getActionCommand() != null) {
	    if (e.getActionCommand().charAt(0) == '/') {
		text.append("*** No IRC commands supported.\n");
	    }
	    else if (e.getActionCommand().charAt(0) == '*' &&
		e.getActionCommand().charAt(e.getActionCommand().length()-1) == '*') {
		text.append("* "+Jicra.nick+" "+e.getActionCommand().substring(1, e.getActionCommand().length()-1)+"\n");
		net.send("PRIVMSG "+channel+" :"+(char) 0x01+"ACTION "+
			 e.getActionCommand().substring(1, e.getActionCommand().length()-1)+""+(char) 0x01);
	    }
	    else if (query_nick != null) {
		net.send("PRIVMSG "+query_nick+" :"+e.getActionCommand()+"");
		text.append("-> *"+query_nick+"* "+e.getActionCommand()+"\n");
	    } else {
		net.send("PRIVMSG "+channel+" :"+e.getActionCommand()+"");
		text.append("<"+Jicra.nick+"> "+e.getActionCommand()+"\n");
	    }
	    input.setText("");
	    text.setCaretPosition((text.getText()).length());
	}
    }

    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    if (list.getSelectedItem() == query_nick) {
		list.deselect(list.getSelectedIndex());
		text.append("*** No longer speaking in private.\n");
		query_nick = null;
	    } else {
		query_nick = list.getSelectedItem();
		text.append("*** Now speaking to "+query_nick+
			    " in private.\n");
	    }
	}
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void stop() {
	repaint();
    }

    public synchronized void destroy() {
	if (net.connected == true)
	    net.disconnect();
	if (net != null)
	    net = null;
	if (text != null)
	    text = null;
	if (input != null)
	    input = null;
	if (list != null)
	    list = null;
	if (query_nick != null)
	    query_nick = null;
    }
}
