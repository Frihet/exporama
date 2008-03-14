/*
 * Copyright 2005 FreeCode AS
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Or get it at http://www.gnu.org/licenses/gpl.html
 *
 */

package com.freecode.eventmgr;

import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Main servlet for administrator pages. Hands requests over to
 * handlers for processing.
 */

public final class AdminServlet extends EventServlet {

    /**
     * Respond to a request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */

    protected void doRequest( HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException {

	// Figure out what the user wants to do and dispatch
	// the request to a handler object

	HttpSession session = request.getSession(); 
	AdminHandler handler = (AdminHandler)session.getAttribute( "adminhandler" );

	String userCommand = request.getParameter( "cmd" );
	if( userCommand != null && userCommand.equals("reset") ) {
	    session.removeAttribute( "adminhandler" );
	    handler = null;
	    userCommand = null;
	}

	if( handler == null ) {
	    
	    if( userCommand == null || userCommand.equals("view") )
		handler = new AdminView( dbconn );
	    else if( userCommand.equals("edit") ) {
		handler = new AdminEdit( dbconn );
		session.setAttribute( "adminhandler", handler );
	    }
	    else if( userCommand.equals("register") ) {
		handler = new AdminRegister( dbconn );
		session.setAttribute( "adminhandler", handler );
	    }
	    else if( userCommand.equals("report") )
		handler = new AdminReport( dbconn );
	}

	if( handler == null )
	    throw new ServletException( "Unrecognized request: " + 
					userCommand );

	PrintWriter writer = response.getWriter();

	if( handler instanceof AdminReport ) {
	    response.setContentType( "text/csv" );
	    handler.entry( request, response );
	}
	else {
	    response.setContentType("text/html");

	    // Write header

	    writer.println( "<html>" );
	    writer.println( "<head>" );
	    writer.println( "<title>admin</title>" );
	    writer.println( "<meta http-equiv=\"pragma\" content=\"no-cache\"/>" );
	    writer.println( "<meta name=\"robots\" content=\"none\"/>" );
	    writer.println( "<link rel=\"stylesheet\" href=\"admin.css\"/>" );
	    writer.println( "</head>" );
	    writer.println( "<body>" );

	    writer.println( "<table width=\"100%\">" );
	    writer.println( "<tr>" );
	    writer.println( "<td>" );
	    writer.println( "<img src=\"left.png\"/>" );
	    writer.println( "</td>" );
	    writer.println( "<td>" );
	    writer.println( "<img src=\"right.png\"/>" );
	    writer.println( "</td>" );
	    writer.println( "</tr>" );
	    writer.println( "</table>" );

// 	    writer.println( "<p>" );
// 	    writer.println( "<div class=\"left\">" );
//  	    writer.println( "<img src=\"left.png\"/>" );
// 	    writer.println( "</div>" );

// 	    writer.println( "<div class=\"left\">" );
//  	    writer.println( "<img src=\"right.png\"/>" );
// 	    writer.println( "</div>" );
// 	    writer.println( "</p>" );

	    writer.println( "<h1>Administrasjonsside</h1>" );
	    writer.println( "<p/>" );

	    
	    // Let appropriate handler handle user request
	    
	    handler.entry( request, response );
	    
	    // Write debug
	    
	    if( debug ) {
		
		writer.println( "<p>" );
		writer.println( "<hr/>" );
		Enumeration e = request.getParameterNames();
		while( e.hasMoreElements() ) {
		    String name = (String)e.nextElement();
		    writer.println( "<em>" + name + "</em>: " +
				    request.getParameter(name) );
		writer.println( "<br/>" );
		}
		writer.println( "</p>" );
	    }

	    // Write footer
	    
	    writer.println( "</body>" );
	    writer.println( "</html>" );
	}
    }
}
