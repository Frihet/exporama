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

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet for handling image (logo) requests
 */

public final class ImageServlet extends EventServlet {

    PreparedStatement getStatement = null;

    protected void doRequest( HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException {

	int id = 0; // 0 is the "emtpy" image

	HttpSession session = request.getSession();
	if( session == null )
	    return;

	String kind = request.getParameter( "kind" );
	if( kind == null || kind.equals("") ) {
	    Integer uploadId = (Integer)session.getAttribute( "picture_id" );
	    if( uploadId != null ) {
		id = uploadId.intValue();
		// consume the session attribute
		session.removeAttribute( "picture_id" );
	    }
	}
	else {
	    AdminEventInfo eventInfo = null;
	    AdminHandler handler =
		(AdminHandler)session.getAttribute( "adminhandler" );

	    if( handler != null && handler instanceof AdminEdit ) {

		AdminEdit editHandler = (AdminEdit)handler;
		eventInfo = editHandler.getEventInfo();

	    }
	    else
		eventInfo = 
		    (AdminEventInfo)session.getAttribute( "userevent" );


	    if( eventInfo != null ) {
		if( kind.equals("logo") )
		    id = eventInfo.getLogoId();
		else if( kind.equals("blogo") )
		    id = eventInfo.getBadgeLogoId();
	    }
	}

	try {
	    writeImage( id, response );
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}
    }

    /**
     * Write a given image
     * @param id primary key identifying an image
     * @param r response 
     */

    private void writeImage( int id, HttpServletResponse r )
	throws SQLException, IOException
    {
	if( getStatement == null )
	    getStatement = dbconn.prepareStatement( "SELECT mime,contents FROM fileobject WHERE id=?" );

	getStatement.setInt( 1, id );
	ResultSet result = getStatement.executeQuery();
	if( result != null ) {
	    result.first();

	    String mime = result.getString( 1 );
	    byte[] image = result.getBytes( 2 );

	    r.setContentType( mime );
	    OutputStream output = r.getOutputStream();
	    output.write( image );
	}

	result.close();
    }
}
