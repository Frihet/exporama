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
 * Superclass for all fields
 */

abstract class AdminEventField
{
    int id;
    String name;
    String internalName;
    boolean optional;
    int badgeFieldIndex = -1;

    abstract void setStringValue( String val );
    abstract String getStringValue();
    abstract String getAsHtml();
    abstract boolean isEmpty();

    abstract void dbRemove( Connection dbconn )
	throws SQLException;

    abstract void dbCreateOrUpdate( Connection dbconn, int parentID )
	throws SQLException;

    abstract void dbRestore( Connection dbconn, int thisID )
	throws SQLException;

    /**
     * Gets string value for the field in a format safe for database queries
     * @return an "sql-safe" string
     */

    String getSqlSafeStringValue()
    {
	String safeValue = new String( getStringValue() );
	
	return safeValue.replaceAll( "'", "\\'" );
    }

    /**
     * The internal name is used in html forms
     */ 

    String getInternalName()
    {
	if( internalName == null )
	    return "field_" + name.replaceAll( "\\s", "_" );
	else
	    return internalName;
    }

    /**
     * Set a specific internal name
     */ 

    void setInternalName( String iname )
    {
	internalName = iname.trim();
    }

    /**
     * Set the visible field name
     */

    void setName( String n )
    {
	String tmpName = n.trim();
	tmpName = tmpName.replaceAll( "['\"<>]", "_" );

	name = tmpName;
    }

    /**
     * Ask the field for its visible name
     */

    String getName()
    {
	return name;
    }

    /**
     * Set how this field maps onto the badge 
     * @param b eventinfo badge
     * @param i badge field number
     */

    void setBadgeField( Badge b, int i )
    {
	b.removeField( this );
	b.setField( i, this );
	badgeFieldIndex = i;
    }

    int getBadgeField()
    {
	return badgeFieldIndex;
    }
}
