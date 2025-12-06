/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.instrument.meteorology.gftb.util;

public final class CRC {
	public static short calc(short inbyByte0, short inbyByte1) { 
		int ui16Integer = (int)((inbyByte0 << 8) | inbyByte1); 
		for (int ui16Zaehler = 0; ui16Zaehler < 16; ui16Zaehler++) { 
			if ((ui16Integer & 0x8000) == 0x8000) { 
				ui16Integer = (int)((ui16Integer << 1) ^ 0x0700); 
			} 
			else { 
				ui16Integer = (int)(ui16Integer << 1);
			} 
		} 
		return (short)((255 - (ui16Integer >> 8)) & 0xFF); 
	} 
	
	public static  boolean calc(int[] inByteArray, int inLaenge) { 
		short array[] = new short[inByteArray.length];
		for (int i=0; i<inByteArray.length; i++)
			array[i] = (short)inByteArray[i];
		return calc(array, inLaenge);
	}
	
	public static boolean calc(short[] inByteArray, int inLaenge) { 
		if (inLaenge >= 3) { 
			if (inByteArray[2] != calc(inByteArray[0], inByteArray[1]))
				return false; 
		} 
		
		if (inLaenge >= 6) { 
			if (inByteArray[5] != calc(inByteArray[3], inByteArray[4])) 
				return false; 
		} 
		
		if (inLaenge >= 9) { 
			if (inByteArray[8] != calc(inByteArray[6], inByteArray[7]))
				return false;  
		} 
		
		if (inLaenge >= 12) { 
			if (inByteArray[11] != calc(inByteArray[9], inByteArray[10])) 
				return false; 
		} 
		
		return true; 
	}
}
