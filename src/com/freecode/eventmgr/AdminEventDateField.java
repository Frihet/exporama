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
import java.util.Calendar;

/**
 * Our abstract view of a date input field
 */

class AdminEventDateField extends AdminEventField
{
    int day;
    int month;
    int year;
    boolean empty = true;

    /**
     * The current date of this field as a string
     * @return Date as a string
     */

    String getStringValue()
    {
	String monthString;
	String dayString;

	if( month < 10 )
	    monthString = "0" + month;
	else
	    monthString = "" + month;

	if( day < 10 )
	    dayString = "0" + day;
	else
	    dayString = "" + day;

	return year + "-" + monthString + "-" + dayString;
    }

    /**
     * Setting the date, with a string
     * @param val checkbox string value (true or on, if it's selected)
     */

    void setStringValue( String val )
    {
	String[] datePart = val.split( "\\s" );
	String[] tokens = datePart[0].split( "-" );
	if( tokens.length != 3 ) // parse error
	    return;

	/* FIXME: check valid */
	year = Integer.parseInt( tokens[0] );
	month = Integer.parseInt( tokens[1] );
	day = Integer.parseInt( tokens[2] );

	/* FIXME: if valid */
	empty = false;
    }

    /**
     * Query if this field has been set
     * @return true if not set, false if it has a value
     */

    boolean isEmpty()
    {
	return empty;
    }

    /**
     * Html markup for this field
     * @return Html for the date field
     */

    String getAsHtml()
    {
	String htmlStr = "<td align=\"right\">" + name;

	if( isEmpty() )
	    htmlStr += "(*)";
	

	htmlStr += "</td><td><input type=\"text\" name=\"" + 
	    getInternalName()  + "\" value=\"" + 
	    getStringValue() + "\" size=\"12\"\"/></td>";

	return htmlStr;
    }

    /**
     * @param cal a date to become our new value
     */

    void setDate( Calendar cal )
    {
	year = cal.get( Calendar.YEAR );
	month = cal.get( Calendar.MONTH ) + 1;
	day = cal.get( Calendar.DAY_OF_MONTH );
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

    void dbRestore( Connection dbconn, int id )
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
     * Create a date field with a given date
     */

    AdminEventDateField( String n, Calendar v, boolean o )
    {
	name = new String( n );
	optional = o;

	setDate( v );
    }

    /**
     * Create a date field with the current date
     */

    AdminEventDateField( String n, boolean o )
    {
	name = new String( n );
	optional = o;

	setDate( Calendar.getInstance() );
    }
}
