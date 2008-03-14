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
import java.util.Enumeration;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This handler takes care of creating and editing events
 */

class AdminEdit implements AdminHandler
{
    Connection dbconn = null;
    AdminEventInfo eventInfo = null;
    Badge badge = new Badge();
    
    /**
     * Entrypoint from (admin) servlet. Does everything, and needs
     * some splitting up
     * @param req request passed on from invoking servlet
     * @param resp response passed on from invoking servlet
     * @throws ServletException for an invalid request
     * @throws IOException (passed on)
     */

    public void entry( HttpServletRequest req,  HttpServletResponse resp )
	throws IOException, ServletException
    {
	PrintWriter writer = resp.getWriter();

	if( eventInfo == null ) {

	    // We do not have an event association, it's because
	    // we are creating a new event, or that we're about
	    // to edit one.

	    String eventID = req.getParameter( "event_id" );
	    if( eventID == null || eventID.equals("") ) {
		// new event

		String templateString = "";
		String templateID = req.getParameter( "template" );
		if( templateID != null && !templateID.equals("") ) {
		    try {
			Statement statement = dbconn.createStatement();
			ResultSet result = statement.executeQuery( "SELECT template FROM inputtemplate WHERE id=" + Integer.parseInt(templateID) );
			if( result != null ) {
			    result.first();
			    templateString = result.getString( "template" );
			}
		    }
		    catch( SQLException e ) {
			throw new ServletException( e );
		    }
		}

		if( templateString != null && !templateString.equals("") )
		    eventInfo = new AdminEventInfo( templateString.split("!") );
		else
		    eventInfo = new AdminEventInfo();
	    }
	    else {
		eventInfo = new AdminEventInfo();
		try {
		    eventInfo.dbRestore( dbconn, Integer.parseInt(eventID) );

		    // Sync field data with badge
		    Enumeration e = eventInfo.getUserFields();
		    while( e.hasMoreElements() ) {
			AdminEventField f = (AdminEventField)e.nextElement();
			badge.syncField( f );
		    }
		}
		catch( SQLException e ) {
		    throw new ServletException( e );
		}
	    }
	}
	else if( req.getParameter("save_event") != null ) {
	    // Write this session to db and finish
	    try {
		eventInfo.dbCommit( dbconn );
	    }
	    catch( SQLException e ) {
		throw new ServletException( e );
	    }

	    writer.println( "<p>Informasjon lagret</p>" );
	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"reset\"/>" );
	    writer.println( "<input type=\"submit\" value=\"OK\"/>" );
	    writer.println( "</form>" ); 
	    return;
	}

	writer.println( "<h2>Generell informasjon</h2>" );
	writer.println( "<p>" );
	writer.println( "<table>" );
	writer.println( "<form method=\"post\" action=\"\" enctype=\"\">" );

	writer.println( "<tr rowspan=\"99\">" );
	writer.println( "<td/>" );
	writer.println( "<td/>" );
	writer.println( "<td/>" );
	writer.println( "<td align=\"right\" width=\"300px\" rowspan=\"99\">" );
	writer.println( "<img src=\"img?kind=logo\"/>" );
	writer.println( "</td>" );
	writer.println( "</tr>" );

	boolean missing = false;
	Enumeration e = eventInfo.getAdminFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement(); 
	    String iname = f.getInternalName();
	    String value = req.getParameter( iname );

	    if( value != null && !value.equals("") )
		f.setStringValue( value );

	    if( f.isEmpty() && !f.optional )
		missing = true;

	    writer.println( "<tr>" );
	    writer.println( f.getAsHtml() );
	    //writer.println( "<td>" + f.getName() + "</td>" );
	    writer.println( "</tr>" );
	}
	writer.println( "<tr>" );
	writer.println( "<td/>" );
	writer.println( "<td>" );
	writer.println( "<input type=\"submit\" value=\"Oppdater\"/>" );
	writer.println( "</td>" );
	writer.println( "</tr>" );
	writer.println( "</form>" );


	// Upload fields

	String logoIndex = req.getParameter( "logo" );
	if( logoIndex != null && !logoIndex.equals("") )
	    eventInfo.setLogoId( Integer.parseInt(logoIndex) );

	String blogoIndex = req.getParameter( "blogo" );
	if( blogoIndex != null && !blogoIndex.equals("") )
	    eventInfo.setBadgeLogoId( Integer.parseInt(blogoIndex) );

	String themeIndex = req.getParameter( "theme" );
	if( themeIndex != null && !themeIndex.equals("") )
	    eventInfo.setThemeId( Integer.parseInt(themeIndex) );

	writer.println( "<tr>" );
	writer.println( "<td align=\"right\">" );
	writer.println( "Tema" );
	writer.println( "</td>" );
	writer.println( "<form method=\"post\" action=\"upload\" enctype=\"multipart/form-data\">" );
	writer.println( "<td>" );
	writer.println( "<input type=\"file\" name=\"theme\" size=\"48\"/>" );
	writer.println( "</td>" );
	writer.println( "<td>" );
	writer.println( "<input type=\"submit\" value=\"Send\"/>" );
	writer.println( "</td>" );
	writer.println( "</form>" );
	writer.println( "<tr>" );

	writer.println( "<tr>" );
	writer.println( "<td align=\"right\">" );
	writer.println( "Logo" );
	writer.println( "</td>" );
	writer.println( "<form method=\"post\" action=\"upload\" enctype=\"multipart/form-data\">" );
	writer.println( "<td>" );
	writer.println( "<input type=\"file\" name=\"logo\" size=\"48\"/>" );
	writer.println( "</td>" );
	writer.println( "<td>" );
	writer.println( "<input type=\"submit\" value=\"Send\"/>" );
	writer.println( "</td>" );
	writer.println( "</form>" );
	writer.println( "<tr>" );

	writer.println( "<tr>" );
	writer.println( "<td align=\"right\">" );
	writer.println( "Adgangskortlogo" );
	writer.println( "</td>" );
	writer.println( "<form method=\"post\" action=\"upload\" enctype=\"multipart/form-data\">" );
	writer.println( "<td>" );
	writer.println( "<input type=\"file\" name=\"blogo\" size=\"48\"/>" );
	writer.println( "</td>" );
	writer.println( "<td>" );
	writer.println( "<input type=\"submit\" value=\"Send\"/>" );
	writer.println( "</td>" );
	writer.println( "</form>" );
	writer.println( "<tr>" );


	writer.println( "</table>" );
	writer.println( "</p>" );

	
	writer.println( "<p>" );
	if( !missing ) {
	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "<input type=\"hidden\" name=\"save_event\" value=\"true\"/>" );
	    writer.println( "<input type=\"submit\" value=\"Lagre\"/>" );
	    writer.println( "</form>" ); 
	}
	
	writer.println( "<form method=\"post\" action=\"\">" );
	writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"reset\"/>" );
	writer.println( "<input type=\"submit\" value=\"Avbryt\"/>" );
	writer.println( "</form>" ); 

	writer.println( "</p>" );

	writer.println( "<div class=\"left\">" );
	writer.println( "<h2 id=\"pfields\">Deltagerinformasjon</h2>" );
	writer.println( "<p>" );
	writer.println( "<table>" );
	writer.println( "<tr>" );
	writer.println( "<th>Tittel</th><th>Type</th><th>St&oslash;rrelse</th><th>Valgfritt</th><th>Felt</th>" );
	writer.println( "</tr>" );

	// process added field (if any)
	String newfieldTitle = req.getParameter( "newfield_title" );
	String newfieldType = req.getParameter( "newfield_type" );
	if( newfieldTitle != null && !newfieldTitle.equals("") &&
	    newfieldType != null ) {
	    if( newfieldType.equals("text") )
		eventInfo.addUserField( new AdminEventTextField(newfieldTitle,"",60,false) );
	    else if( newfieldType.equals("menu") )
		eventInfo.addUserField( new AdminEventMenuContainerField(newfieldTitle,false) );
	    else if( newfieldType.equals("checkbox") )
		eventInfo.addUserField( new AdminEventCheckboxField(newfieldTitle,false) );
	}

	// loop
	int fieldnum = 0;
	String removefieldID = req.getParameter( "removefield_id" );
	if( removefieldID != null && !removefieldID.equals("") ) {
	    int removefield = Integer.parseInt( removefieldID );
	    try {
		eventInfo.removeUserField( removefield, dbconn );
	    } 
	    catch( SQLException ex ) {
		throw new ServletException( ex );
	    }
	}
	int editfield = -1;
	String editfieldID = req.getParameter( "editfield_id" );
	if( editfieldID != null && !editfieldID.equals("") )
	    editfield = Integer.parseInt( editfieldID );
	e = eventInfo.getUserFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement(); 

	    if( f instanceof AdminEventTextField ) {
		AdminEventTextField tf = (AdminEventTextField)f;

		if( editfield == fieldnum ) {
		    // edit this field
		    String newTitle = req.getParameter( "editfield_title" );
		    String newSize = req.getParameter( "editfield_size" );
		    String newBadge = req.getParameter( "editfield_badge" );
		    String newOptional = req.getParameter( "editfield_optional" );

		    if( newTitle != null && !newTitle.equals("") )
			tf.setName( newTitle );
		    if( newSize != null && !newSize.equals("") )
			tf.size = Integer.parseInt( newSize );
		    if( newBadge != null && !newBadge.equals("") )
			tf.setBadgeField( badge, Integer.parseInt(newBadge) );
		    if( newOptional != null && !newOptional.equals("") )
			tf.optional = true;
		    else
			tf.optional = false;
		}

		writer.println( "<tr>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );

		writer.println( "<td>" );
		writer.println( "<input type=\"text\" name=\"editfield_title\" value=\"" + tf.name + "\" size=\"12\"/>" );
		writer.println( "</td>" );

		writer.println( "<td>" );
		writer.println( "Tekst" );
		writer.println( "</td>" );

		writer.println( "<td>" );
		writer.println( "<input type=\"text\" name=\"editfield_size\" value=\"" + tf.size + "\" size=\"2\"/>" );
		writer.println( "</td>" );

		writer.println( "<td>" );
		writer.print( "<input type=\"checkbox\" name=\"editfield_optional\"" );
		if( tf.optional )
		    writer.print( "checked" );
		writer.println( "/>" );
		writer.println( "</td>" );

		writeBadgeSelect( writer, tf, "editfield_badge" );

		writer.println( "<td>" );
		writer.println( "<input type=\"submit\" value=\"Endre\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<td>" );		
		writer.println( "<input type=\"hidden\" name=\"removefield_id\" value=\"" + fieldnum  + "\"/>" );
		writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "</tr>" );
	    }
	    if( f instanceof AdminEventCheckboxField ) {
		AdminEventCheckboxField cf = (AdminEventCheckboxField)f;

		if( editfield == fieldnum ) {
		    // edit this field
		    String newTitle = req.getParameter( "editfield_title" );

		    if( newTitle != null && !newTitle.equals("") )
			cf.setName( newTitle );
		}

		writer.println( "<tr>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );

		writer.println( "<td>" );
		writer.println( "<input type=\"text\" name=\"editfield_title\" value=\"" + cf.name + "\" size=\"12\"/>" );
		writer.println( "</td>" );

		writer.println( "<td>" );
		writer.println( "Avkryssing" );
		writer.println( "</td>" );

		writer.println( "<td/>" );
		writer.println( "<td/>" );
		writer.println( "<td/>" );

		writer.println( "<td>" );
		writer.println( "<input type=\"submit\" value=\"Endre\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<td>" );		
		writer.println( "<input type=\"hidden\" name=\"removefield_id\" value=\"" + fieldnum  + "\"/>" );
		writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "</tr>" );
	    }
	    else if( f instanceof AdminEventMenuContainerField ) {
		AdminEventMenuContainerField mf = 
		    (AdminEventMenuContainerField)f;

		if( editfield == fieldnum ) {
		    String editfieldNewElementTitle = 
			req.getParameter( "editfield_newelm_title" );
		    String editTitle = 
			req.getParameter( "editfield_title" );
		    String editRemoveSubfield = 
			req.getParameter( "editfield_removesubfield_id" );
		    String editSubfieldID =
			req.getParameter( "editfield_editsubfield_id" );
		    String editSubfieldCategory = 
			req.getParameter( "editfield_editsubfield_category" );
		    String editSubfieldTitle =
			req.getParameter( "editfield_editsubfield_title" );

		    if( editfieldNewElementTitle != null &&
			!editfieldNewElementTitle.equals("") ) {
			mf.addMenuItem( new AdminEventMenuElementField(editfieldNewElementTitle) );
		    }
		    else if( editTitle != null &&
			     !editTitle.equals("") ) {
			mf.setName( editTitle );

			String editBadge = 
			    req.getParameter( "editfield_badge" );
			if( editBadge != null && !editBadge.equals("") )
			    mf.setBadgeField( badge, Integer.parseInt(editBadge) );

			String editOptional = 
			    req.getParameter( "editfield_optional" );
			if( editOptional != null && !editOptional.equals("") )
			    f.optional = true;
			else
			    f.optional = false;
		    }
		    else if( editRemoveSubfield != null &&
			     !editRemoveSubfield.equals("") )
			try {
			    mf.removeMenuItem( Integer.parseInt(editRemoveSubfield), dbconn );
			}
			catch( SQLException ex ) {
			    throw new ServletException( ex );
			}
		    else if( editSubfieldID != null &&
			     !editSubfieldID.equals("") ) {
			int subfieldid = Integer.parseInt( editSubfieldID );
			AdminEventMenuElementField me = mf.getMenuItem( subfieldid );
			if( editSubfieldTitle != null &&
			    !editSubfieldTitle.equals("") )
			    me.setName( editSubfieldTitle );

			if( editSubfieldCategory != null &&
			    !editSubfieldCategory.equals("") )
			    me.setCategory( editSubfieldCategory.charAt(0) );
		    }
		    
		}

		writer.println( "<tr>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<td>" );
		writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );
		writer.println( "<input type=\"text\" name=\"editfield_title\" value=\"" + f.name + "\" size=\"12\"/>" );
		writer.println( "</td>" );

		writer.println( "<td>" );
		writer.println( "Meny" );
		writer.println( "</td>" );
		writer.println( "<th>" );
		writer.println( "Kategori" );
		writer.println( "</th>" );

		writer.println( "<td>" );
		writer.print( "<input type=\"checkbox\" name=\"editfield_optional\"" );
		if( f.optional )
		    writer.print( "checked" );
		writer.println( "/>" );
		writer.println( "</td>" );

		writeBadgeSelect( writer, mf, "editfield_badge" );

		writer.println( "<td>" );
		writer.println( "<input type=\"submit\" value=\"Endre\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<td>" );		
		writer.println( "<input type=\"hidden\" name=\"removefield_id\" value=\"" + fieldnum  + "\"/>" );
		writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		writer.println( "</td>" );
		writer.println( "</form>" );

		writer.println( "</tr>" );

		int subfieldnum = 0;
		Enumeration menuElements = mf.getMenuItems();
		while( menuElements.hasMoreElements() ) {
		    AdminEventMenuElementField me = 
			(AdminEventMenuElementField)
			menuElements.nextElement();

		    writer.println( "<tr>" );

		    writer.println( "<td/>" );
		    writer.println( "<form method=\"post\" action=\"#pfields\">" );
		    writer.println( "<td>" );
		    writer.println( "<input type=\"text\" size=\"12\" name=\"editfield_editsubfield_title\" value=\"" + me.name + "\"/>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<input type=\"text\" size=\"2\" name=\"editfield_editsubfield_category\" value=\"" + me.getCategory() + "\"/>" );
		    writer.println( "</td>" );

		    writer.println( "<td/>" );
		    writer.println( "<td/>" );
		    writer.println( "<td>" );
		    writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"editfield_editsubfield_id\" value=\"" + subfieldnum + "\"/>" );
		    writer.println( "<input type=\"submit\" value=\"Endre\"/>" );
		    writer.println( "</td>" );
		    writer.println( "</form>" );

		    writer.println( "<form method=\"post\" action=\"#pfields\">" );
		    writer.println( "<td>" );
		    writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"editfield_removesubfield_id\" value=\"" + subfieldnum + "\"/>" );
		    writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		    writer.println( "</td>" );
		    writer.println( "</form>" );

		    writer.println( "</tr>" );

		    subfieldnum++;
		}

		writer.println( "<form method=\"post\" action=\"#pfields\">" );
		writer.println( "<tr>" );
		writer.println( "<td>" );
		writer.println( "<input type=\"hidden\" name=\"editfield_id\" value=\"" + fieldnum + "\"/>" );
		writer.println( "</td>" );
		writer.println( "<td>" );
		writer.println( "<input type=\"text\" name=\"editfield_newelm_title\"/>" );
		writer.println( "</td>" );
		writer.println( "<td/>" );
		writer.println( "<td/>" );
		writer.println( "<td/>" );
		writer.println( "<td>" );
		writer.println( "<input type=\"submit\" value=\"Ny\"/>" );
		writer.println( "</td>" );
		writer.println( "</tr>" );
		writer.println( "</form>" );
		writer.println( "<tr>" );
	    }

	    fieldnum++;
	}

	writer.println( "<form method=\"post\" action=\"#pfields\">" );
	writer.println( "<tr>" );

	writer.println( "<td>" );
	writer.println( "<input type=\"text\" name=\"newfield_title\" size=/>" );
	writer.println( "</td>" );

	writer.println( "<td>" );
	writer.println( "<select name=\"newfield_type\"><option value=\"text\" selected>Tekstfelt</option><option value=\"menu\">Meny</option><option value=\"checkbox\">Avkryssing</option></select>" );
	writer.println( "</td>" );

	writer.println( "<td/><td/><td/>" );
	writer.println( "<td>" );
	writer.println( "<input type=\"submit\" value=\"Ny\"/>" );
	writer.println( "</td>" );

	writer.println( "</tr>" );
	
	writer.println( "</form>" );	

	writer.println( "</table>" );
	writer.println( "</p>" );

	if( !missing ) {
	    writer.println( "<p>" );
	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "<input type=\"hidden\" name=\"save_event\" value=\"true\"/>" );
	    writer.println( "<input type=\"submit\" value=\"Lagre\"/>" );
	    writer.println( "</form>" ); 
	    writer.println( "</p>" );
	}
	
	writer.println( "</div>" );
	writer.println( "<div class=\"badge\">" );
	writer.println( "<h2>Adgangskort</h2>" );
	writer.println( "<p>" );
	writer.println( "<img src=\"img?kind=blogo\"/><br/>" );
	writer.println( badge.makeHtmlPreview() );
	writer.println( "</p>" );
	writer.println( "</div>" );
    }

    /**
     * Just for convenience. Writes the menu for selecting badge fields
     * @param writer for writing the html
     * @param f the field in question; the field we want user to map to badge
     * @param label so we can find it in subsequent requests
     */

    private void writeBadgeSelect( PrintWriter writer, AdminEventField f, String label )
    {
	writer.println( "<td>" );
	writer.println( "<select name=\"" + label + "\">" );
	writer.println( "<option value=\"-1\"/>" );
	for( int i = 0; i < badge.getFieldCount(); i++ ) {
	    writer.print( "<option value=\"" + i + "\"" );
	    if( f.getBadgeField() == i )
		writer.println( "selected" );
	    writer.println( ">" + i + "</option>" );
	}
	writer.println( "</select>" );
	writer.println( "</td>" );
    }

    /**
     * Access method
     * @return our private eventInfo, incase someone needs it
     */

    public AdminEventInfo getEventInfo()
    {
	return eventInfo;
    }
    
    AdminEdit( Connection c )
    {
	dbconn = c;
    }
}
