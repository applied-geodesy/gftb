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

public class MeteorologyParameters {

	private final double dryTemp, wetTemp, pressure, humidity;
	
	public MeteorologyParameters(double pressure, double dryTemp, double wetTemp, double humidity) {
		this.pressure = pressure;
		this.dryTemp  = dryTemp;
		this.wetTemp  = wetTemp;
		this.humidity = humidity;
	}
	
	/**
	 * Liefert Trockentemperatur [°C]
	 * @return dryTemp
	 *
	 */
	public double getDryTemperature() {
		return this.dryTemp;
	}
	
	/**
	 * Liefert Feuchttemperatur [°C]
	 * @return wetTemp
	 *
	 */
	public double getWetTemperature() {
		return this.wetTemp;
	}
	
	/**
	 * Liefert Druck [mbar]
	 * @return pressure
	 *
	 */
	public double getPressure() {
		return this.pressure;
	}
	
	/**
	 * Liefert relative Luftfeuchte [%]
	 * @return humidity
	 *
	 */
	public double getHumidity() {
		return this.humidity;
	}

	@Override
	public String toString() {
		return "MeteorologyParameters [Temperature=" + dryTemp + ", Wet-Temperature=" + wetTemp + ", Pressure=" + pressure
				+ ", Humidity=" + humidity + "]";
	}	
}
