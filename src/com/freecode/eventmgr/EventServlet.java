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
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Generic servlet routines shared by all servlets.
 */

abstract class EventServlet extends HttpServlet {

    protected Connection dbconn; // persistent database connection
    protected boolean debug = false;

    /**
     * Get configuration
     */

    public void init() 
	throws ServletException {

	// Query the webserver for database configuration
	// (all config info is located in web.xml)

	ServletContext servletConfig = getServletContext();

	String dbDrv  = servletConfig.getInitParameter( "dbdrv" );
	String dbName = servletConfig.getInitParameter( "dbname" );
	String dbUser = servletConfig.getInitParameter( "dbuser" );
	String dbPass = servletConfig.getInitParameter( "dbpass" );
	
	// Try to load jdbc driver and connect

	try {
	    Class.forName( dbDrv );
	    dbconn = DriverManager.getConnection( dbName, dbUser, dbPass );
	}
	catch( Exception e ) {
	    throw new ServletException( e );
	}
    }

    /**
     * Close database connection
     */

    public void destroy()
    {
	try {
	    dbconn.close();
	}
	catch( Exception e ) {
	    // don't care, not much we can do about it
	}
	finally {
	    dbconn = null;
	}
    }

    /**
     * Servlets override this
     */

    abstract protected void doRequest( HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException;


    /**
     * Add some headers before answering request
     */

    private void internalDoRequest(  HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException
    {
	response.setHeader( "Cache-Control", "no-cache" );
	response.setDateHeader( "Expires", 0 );
	response.setHeader( "Pragma", "No-cache" );

	doRequest( request, response );
    }

    /**
     * We treat post and get equally
     */

    public void doPost( HttpServletRequest request,
			HttpServletResponse response )
	throws IOException, ServletException {

	internalDoRequest( request, response );
    }

    /**
     * We treat post and get equally
     */

    public void doGet( HttpServletRequest request,
		       HttpServletResponse response )
	throws IOException, ServletException {

	internalDoRequest( request, response );
    }
}
