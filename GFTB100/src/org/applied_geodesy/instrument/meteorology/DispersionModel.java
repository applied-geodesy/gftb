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

package org.applied_geodesy.instrument.meteorology;

/**
 * Liste mit unterschiedlichen Autoren, die empirische Parameter fuer das von der IAG 1960 bzw. IAG 1999 
 * festgelegte Dispersionsmodel veroeffentlicht haben.
 * 
 * @author Michael Loesler
 *
 */
public enum DispersionModel {
	BARRELL_AND_SEARS_1939 ("Barrel \u0026 Sears (1939)"),
	EDLEN_1953  ("Edlen (1953)"),
	EDLEN_1966  ("Edlen (1966)"),
	OWENS_1967  ("Owens (1967)"),
	IAG_1999    ("IAG (1999)"),
	CIDDOR_1996 ("Ciddor (1996)"),
	CIDDOR_2002 ("Ciddor (2002)");
	
	private final String value;
	
	private DispersionModel(String value) {
		this.value = value;
	}
	
	public String toString() {
		return this.value;
	}
}
