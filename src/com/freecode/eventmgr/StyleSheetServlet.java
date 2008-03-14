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
 * Servlet for handling css (theme) requests
 */

public final class StyleSheetServlet extends EventServlet {

    PreparedStatement getStatement = null;

    protected void doRequest( HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException {

	int id = 1; // 1 is the default

	HttpSession session = request.getSession();
	if( session == null )
	    return;

	AdminEventInfo eventInfo = 
	    (AdminEventInfo)session.getAttribute( "userevent" );


	if( eventInfo != null )
	    id = eventInfo.getThemeId();

	try {
	    writeCss( id, response );
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}
    }

    private void writeCss( int id, HttpServletResponse r )
	throws SQLException, IOException
    {
	if( getStatement == null )
	    getStatement = dbconn.prepareStatement( "SELECT mime,contents FROM fileobject WHERE id=?" );

	getStatement.setInt( 1, id );
	ResultSet result = getStatement.executeQuery();
	if( result != null ) {
	    result.first();

	    String mime = result.getString( 1 );
	    byte[] css = result.getBytes( 2 );

	    r.setContentType( mime );
	    OutputStream output = r.getOutputStream();
	    output.write( css );
	}

	result.close();
    }
}
