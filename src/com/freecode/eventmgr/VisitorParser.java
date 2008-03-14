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

/**
 * Parse data scanned from visitors badge. It's a binary and
 * undocumented database file format. Great.
 */

class VisitorParser
{
    private byte[] input = null;
    private int inputPosition = 0;

    private int colDate = -1;
    private int colTimeHour = -1;
    private int colTimeMinute = -1;
    private int colTimeSecond = -1;
    private int colScanner = -1;
    private int colParticipant = -1;


    /**
     * Date when visitor was scanned
     */

    public String getColumnDate()
    {
	String date = "" + colDate;
	String year = date.substring( 0, 4 );
	String month = date.substring( 4, 6 );
	String day = date.substring( 6, 8 );

	return year + "-" + month + "-" + day;
    }

    /**
     * Time when visitor was scanned
     */

    public String getColumnTime()
    {
	String hour = "";
	String minute = "";
	String second = "";

	if( colTimeHour < 10 )
	    hour += "0";
	hour += colTimeHour;

	if( colTimeMinute < 10 )
	    minute += "0";
	minute += colTimeMinute;

	if( colTimeSecond < 10 )
	    second += "0";
	second += colTimeSecond;

	return hour + ":" + minute + ":" + second;
    }

    /**
     * Scanner id visitor was scanned with
     */

    public int getColumnScanner()
    {
	return colScanner;
    }

    /**
     * Visitors participant id
     */

    public int getColumnParticipant()
    {
	return colParticipant;
    }

    /**
     * It's a kind of row cursor for the database file
     */

    private byte[] getNextRow()
    {
	if( inputPosition >= input.length )
	    return null;

	byte[] inputRow = null;
	int pos = inputPosition;

	for( pos = inputPosition; 
	     pos < input.length && input[pos] != 0xa; pos++ );

	int length = pos - inputPosition;
	inputRow = new byte[length];
	System.arraycopy( input, inputPosition, inputRow, 0, length );
	inputPosition = pos + 1;

	return inputRow;
    }

    /**
     * Read digits from the database file
     * @param b raw byte input buffer
     * @param pos start position in b
     * @param len how many digits to read
     */

    private int getDigits( byte[] b, int pos, int len )
    {
	boolean valid = true;
	boolean variableLength = false;
	int readLength = 0;
	final int zero = '0';
	int value = 0;

	if( pos+len >= b.length )
	    return -1;
	
	if( pos < 0 )
	    pos += b.length;

	if( len < 0 ) {
	    len = b.length - pos;
	    variableLength = true;
	}

	for( int i = 0; i < len; i++ ) {
	    if( Character.isDigit((char)b[pos+i]) )
		value = value * 10 + (int)b[pos+i] - zero;
	    else {
		valid = false;
		break;
	    }

	    readLength++;
	}

	if( !valid && !variableLength || readLength <= 0 )
	    return -1; // assume valid values are all positive

	return value;
    }

    /**
     * Read a row of data from the database file and extract information
     */

    private boolean parseRow( byte[] r )
    {
	int tmpValue;

	// this is a date
	colDate = getDigits( r, 14, 8 );
	if( colDate < 0 )
	    return false;

	// this is the same date
	tmpValue = getDigits( r, 27, 8 );
	if( colDate != tmpValue )
	    return false;

	// this is the same date
	tmpValue = getDigits( r, 40, 8 );
	if( colDate != tmpValue )
	    return false;

	// this is time (hour)
	colTimeHour = getDigits( r, 50, 2 );
	if( colTimeHour < 0 )
	    return false;

	// this is time (minute)
	colTimeMinute = getDigits( r, 53, 2 );
	if( colTimeMinute < 0 )
	    return false;

	// this is time (second)
	colTimeSecond = getDigits( r, 56, 2 );
	if( colTimeSecond < 0 )
	    return false;

	// this is scanner id
	colScanner = getDigits( r, 63, 1 );
	if( colScanner < 0 )
	    return false;

	// the date once again
	tmpValue = getDigits( r, 66, 8 );
	if( colDate != tmpValue )
	    return false;

	// the last date
	tmpValue = getDigits( r, -25, 8 );
	if( colDate != tmpValue )
	    return false;

	// find beginning of participant id (just before date)
	int start = r.length - 28;
	int pos = start;
	while( Character.isDigit(((char)r[pos])) )
	    pos--;

	if( pos == start )
	    return false;

	colParticipant = getDigits( r, pos+1, -1 );
	if( colParticipant < 0 )
	    return false;

	return true;
    }

    /**
     * Parse another row
     * @return false if no more data, true otherwise
     */

    public boolean parseNextRow()
    {
	byte[] row;

	do {
	    row = getNextRow();
	    if( row == null )
		return false;

	} while( !parseRow(row) );

	return true;
    }

    VisitorParser( byte[] b )
    {
	input = b;
    }
}
