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
import java.util.List;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.*;

/**
 * Servlet for handling all file uploads. Stores uploaded file in database
 */

public final class UploadServlet extends EventServlet {

    PreparedStatement insertStatement = null;

    protected void doRequest( HttpServletRequest request,
			    HttpServletResponse response )
	throws IOException, ServletException {

	try {
	    if( insertStatement == null )
		insertStatement = dbconn.prepareStatement( "INSERT INTO fileobject (id,mime,contents) VALUES (?, ?, ?)" );
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}

	HttpSession session = request.getSession();
	if( session == null )
	    return;

	PrintWriter writer = response.getWriter();
	response.setContentType( "text/html" );

	boolean haveFile = false;

	// Is there a file to be uploaded?
	if( FileUpload.isMultipartContent(request) ) {
	    DiskFileUpload upload = new DiskFileUpload();

	    upload.setSizeThreshold( 512*1024 + 1 );
	    upload.setSizeMax( 512*1024  );

	    List items;
	    try {
		items = upload.parseRequest( request );
	    }
	    catch( FileUploadException e ) {
		throw new ServletException( e );
	    }

	    Iterator iter = items.iterator();
	    while( iter.hasNext() ) {
		FileItem item = (FileItem)iter.next();

		if( !item.isFormField() && item.getSize() > 0 ) {
		    String fieldName = item.getFieldName();
		    String fileName = item.getName();
		    String contentType = item.getContentType();
		    boolean isInMemory = item.isInMemory();
		    long sizeInBytes = item.getSize();
		    boolean isPicture = contentType.startsWith( "image" );
		    byte[] contents = item.get();

		    int id;
		    try {
			id = getNextId();
			if( id <= 0 )
			    break;
		    			
			insertStatement.setInt( 1, id );
			insertStatement.setString( 2, contentType );
			insertStatement.setBytes( 3, contents );
			insertStatement.executeUpdate();
		    }
		    catch( SQLException e ) {
			throw new ServletException( e );
		    }

		    haveFile = true;

		    writer.print( "Filen \"" +fileName + "\"" );
		    writer.print( " (" + contentType + ", " );
		    writer.print( sizeInBytes + " bytes) ble mottatt og lagret. " );
		    writer.println( "Trykk OK for &aring; fortsette" );

		    writer.println( "<form method=\"post\" action=\"admin\">" );
		    writer.println( "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"submit\" value=\"OK\"/>" );
		    writer.println( "</form>" );

		    if( isPicture ) {
			session.setAttribute( "picture_id", new Integer(id) );
			writer.println( "<img src=\"img\"/>" );
		    }
		    /*
		    else {
			writer.println( "<pre>" );
			writer.println( new String(contents) );
			writer.println( "</pre>" );
		    }
		    */

		    break;
		}
	    }
	}
	else if( false ) /* if debug */ {
	    writer.println( "<form method=\"post\" action=\"\" enctype=\"multipart/form-data\">" );
	    writer.println( "<input type=\"file\" name=\"filnavn\" size=\"48\"/>" );
	    writer.println( "<input type=\"submit\" value=\"send\"/>" );
	    writer.println( "</form>" );
	}

	if( !haveFile ) {
	    writer.println( "Filen ble ikke motatt<br/>" );
	    writer.println( "<form method=\"post\" action=\"admin\">" );
	    writer.println( "<input type=\"submit\" value=\"OK\"/>" );
	    writer.println( "</form>" );
	}
    }

    private int getNextId()
	throws SQLException
    {
	ResultSet result;
	Statement statement = dbconn.createStatement();
	
	result = statement.executeQuery( "SELECT nextval('fileobject_id_seq')" );

	if( result != null ) {
	    result.first();
	    int id = result.getInt( "nextval" );
	    result.close();
	    return id;
	}

	return -1;
    }
}
