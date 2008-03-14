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

/**
 * Our abstract view of a file(-name) input field
 */

class AdminEventFileField extends AdminEventField
{
    String value;
    int size;

    /**
     * Does not apply for this field, but is required by the interface
     */

    void setStringValue( String v )
    {
	// FIXME: check string
	if( v != null )
	    value = v.trim();
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    String getStringValue()
    {
	return "[FIXME]";
    }

    /**
     * Query if this field has been set
     * @return true if not set, false if it has a value
     */

    boolean isEmpty()
    {
	return value == null || value.equals("");
    }

    /**
     * Html markup for this field
     * @return Html for the date field
     */

    String getAsHtml()
    {
	String htmlStr = "<td align=\"right\">" + name;

	htmlStr += "</td><td><input type=\"file\" name=\"" + 
	    getInternalName()  + "\" value=\"" + 
	    getStringValue() + "\" size=\"48\"/></td>";

	return htmlStr;
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    void dbCreateOrUpdate( Connection dbconn, int parentID )
	throws SQLException
    {
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    void dbRestore( Connection dbconn, int thisID )
	throws SQLException
    {
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    void dbRemove( Connection dbconn )
	throws SQLException
    {
    }

    /**
     * Create an empty nameless file field
     */

    AdminEventFileField()
    {
	name = "";
	value = "";
	optional = true;
    }

    /**
     * Create a named file field
     */

    AdminEventFileField( String n )
    {
	setName( n );
	value = "";
	optional = true;
    }
}
