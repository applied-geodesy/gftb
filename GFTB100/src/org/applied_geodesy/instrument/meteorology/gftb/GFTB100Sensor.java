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

package org.applied_geodesy.instrument.meteorology.gftb;

import java.io.IOException;
import java.util.ArrayList;

import org.applied_geodesy.instrument.meteorology.MeteorologyParameters;
import org.applied_geodesy.instrument.meteorology.MeteorologySensor;
import org.applied_geodesy.instrument.meteorology.gftb.util.CRC;
import org.applied_geodesy.io.rxtx.ReceiveDataType;
import org.applied_geodesy.io.rxtx.ReceiverExchangeable;
import org.applied_geodesy.io.rxtx.RxTx;
import org.applied_geodesy.io.rxtx.RxTxReturnable;

public class GFTB100Sensor implements MeteorologySensor, RxTxReturnable, ReceiverExchangeable {
	public final static int P  = 1,      // Druck [mbar]
			T  = 2,      // Temperatur [°C]
			RH = 3,      // relative Luftfeuchte  [%]
			TD = 4,   	 // Taupunkttemperatur [°C]
			TW = 5,      // Feuchttemperatur [°C] 
			AH = 6,      // Atmosphaerische Feuchte [g/kg]
			ABSH = 7;    // Absolute Feuchte [g/m/m/m]
	
	private RxTx connRxTx;
	private double outdblFloatValue = 0;
	private long timeOut = 2000L;
	private int responseData[] = new int[6];
	private int responseLength = -1;
	private ArrayList<Integer> response = new ArrayList<Integer>(9);
	
	public GFTB100Sensor(RxTx connRxTx) {
		this.connRxTx = connRxTx;
		this.connRxTx.setReceiveDataType(ReceiveDataType.INTEGER);
	}
	
	/**
	 * Weise den GFTB-Sensor an, Messwerte zu erfassen und
	 * diese in seinem internen Puffer abzulegen.
	 * 
	 * @param sensortType
	 * @throws IOException
	 */
	private void sendMeasuredValueRequest(int sensortType) throws IOException {
		short byte0 = (short)((0xFF - sensortType) & 0xFF); //Adresse = Sensor T, P usw.
		short byte1 = 0x00 & 0xFF; // F1-Code
		short byte2 = CRC.calc(byte0, byte1);
		
		byte[] byteData = new byte[] {
				(byte)byte0, (byte)byte1, (byte)byte2
		};
		this.response.clear();
		this.responseData = new int[6];
		this.connRxTx.transmit(byteData);
	}
	
	private void sendUnitRequest(int sensortType) throws IOException {
		short byte0 = (short)((0xFF - sensortType) & 0xFF); //Adresse = Sensor T, P usw.
		short byte1 = 0xF2 & 0xFF; // F1-Code
		short byte2 = CRC.calc(byte0, byte1);
		short byte3 = 0xFF - 0xCA;
		short byte4 = 0x00;
		short byte5 = CRC.calc(byte3, byte4);
		
		byte[] byteData = new byte[] {
				(byte)byte0, (byte)byte1, (byte)byte2,
				(byte)byte3, (byte)byte4, (byte)byte5
		};
		this.response.clear();
		this.responseData = new int[9];
		this.connRxTx.transmit(byteData);
	}
	
	@Override
	public void receive(byte[] bytesRX) throws IOException {
		throw new IOException("Error, unsupported method call. Use receive(int intRX) for data transfer.");
	}

	@Override
	public void receive(int intRX) throws IOException {
		this.response.add(intRX & 0xff);
		
		// Bestimme Laenge der Nachricht (6 oder 9)
		if (this.response.size() == 2) {
			String bits = String.format("%8s", Integer.toBinaryString(intRX)).replace(' ', '0');
			this.responseLength = bits.substring(5, 7).equals("01") ? 6 : 9; // 01 == 6; 10 == 9
		}

		if (this.responseLength == this.response.size()) {
			// Kopiere Daten um in einArray
			this.responseData = new int[this.responseLength];
			for (int i = 0; i < this.responseLength; i++) {
				this.responseData[i] = this.response.get(i).intValue();
			}
			
			this.response.clear();
			this.responseLength = -1;
			synchronized(this.connRxTx){
				this.connRxTx.notify();
        	}
		}		
	}
	
	/**
	 * 2 Übertragungs-Bytes zu unsigned 16 Bit Integer zusammenfassen
	 * @param inbyByteA
	 * @param inbyByteB
	 * @return u16Int
	 */
	private int UInt16Decodieren(short inbyByteA, short inbyByteB) { 
		return (int)((int)((255 - inbyByteA) << 8) | inbyByteB);
	}
	
	/**
	 * 2 Bytes aus Übertragung in Messwert oder Fehlercode umrechnen
	 * @param inbyByte3
	 * @param inbyByte4
	 * @return messwert
	 */
	private int decodeValue16 (short inbyByte3, short inbyByte4) {
		this.outdblFloatValue = 0; 
		int outi16DezimalPunktPosition = 0; 
		int ui16Integer = UInt16Decodieren(inbyByte3, inbyByte4); 
		outi16DezimalPunktPosition = (int)((ui16Integer & 0xC000) >> 14); 
		ui16Integer = (int)(ui16Integer & 0x3FFF); 
		if ((ui16Integer >= 0x3FE0) && (ui16Integer <= 0x3FFF)) { 
			this.outdblFloatValue = (double)ui16Integer - (double)16352.0; 
			return -36; /* Rückgabewert ist Fehlercode */ 
		} 
		
		long i32Nenner = (long)Math.pow(10.0, outi16DezimalPunktPosition); 
		long i32Zaehler = (long)((double)ui16Integer - (double)2048.0); 
		this.outdblFloatValue = (double)((double)i32Zaehler / (double)i32Nenner); 
		return 0; /* Rückgabewert ist kein Fehler, OK */ 
	}
	
	public synchronized String getUnitAbbr(int sensorType) {
		int unit = -1;
		try {
			// Ermittle Messwerte
			this.outdblFloatValue = 0;
			this.sendUnitRequest(sensorType);
			synchronized( this.connRxTx ) {
				try {
					if (this.timeOut > 0)
						this.connRxTx.wait(this.timeOut);
					else
						this.connRxTx.wait();
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}
			unit = UInt16Decodieren((short)responseData[6], (short)responseData[7]);
			if (!CRC.calc(this.responseData, this.responseData.length)) {
				System.err.println(this.getClass().getSimpleName()+" GFTB100-Uebertragungsfehler, Widerspruch in CRC-Byte!");
				return this.decodeUnitAbbr(-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return this.decodeUnitAbbr(unit);
	}
	
	private String decodeUnitAbbr(int unitId) {
		switch(unitId) {
		case 1:
			return "°C";
		case 61:
			return "km/h";
		case 122:
			return "kOhm";
		case 2:
			return "°F";
		case 62:
			return "mph";
		case 123:
			return "MOhm";
		case 3:
			return "K";
		case 63:
			return "Knoten";
		case 125:
			return "kOhm*cm";
		case 126:
			return "MOhm*cm";
		case 10:
			return "% r.F.";
		case 70:
			return "mm";
		case 71:
			return "m";
		case 72:
			return "inch";
		case 130:
			return "cd";
		case 73:
			return "ft";
		case 131:
			return "lx";
		case 74:
			return "cm";
		case 132:
			return "lm";
		case 75:
			return "km";
		case 190:
			return "sone";
		case 191:
			return "phon";
		case 192:
			return "μPa";
		case 18:
			return "inHg(0°C)";
		case 193:
			return "dB(SPL)";
		case 19:
			return "inHg(60°F)";
		case 79:
			return "l/s";
		case 20:
			return "bar";
		case 80:
			return "l/h";
		case 21:
			return "mbar";
		case 81:
			return "l/min";
		case 22:
			return "Pascal";
		case 82:
			return "m^3/h";
		case 23:
			return "hPascal";
		case 83:
			return "m^3/min";
		case 24:
			return "kPascal";
		case 84:
			return "nm^3/h";
		case 25:
			return "MPascal";
		case 85:
			return "ml/s";
		case 26:
			return "kg/cm^2";
		case 86:
			return "ml/min";
		case 27:
			return "mmHg";
		case 87:
			return "ml/h";
		case 28:
			return "PSI";
		case 88:
			return "m^3/s";
		case 29:
			return "mm H20";
		case 30:
			return "S/cm";
		case 90:
			return "g";
		case 31:
			return "mS/cm";
		case 91:
			return "kg";
		case 32:
			return "μS/cm";
		case 92:
			return "N";
		case 150:
			return "%";
		case 93:
			return "Nm";
		case 151:
			return "°";
		case 94:
			return "t";
		case 152:
			return "ppm";
		case 153:
			return "ppb";
		case 40:
			return "pH";
		case 100:
			return "A";
		case 42:
			return "rH";
		case 101:
			return "mA";
		case 102:
			return "μA";
		case 160:
			return "g/kg";
		case 161:
			return "g/m^3";
		case 45:
			return "mg/l O2";
		case 105:
			return "V";
		case 162:
			return "mg/m^3";
		case 46:
			return "% Sat O2";
		case 106:
			return "mV";
		case 163:
			return "μg/m^3";
		case 47:
			return "% O2";
		case 107:
			return "μV";
		case 50:
			return "U/min";
		case 111:
			return "W";
		case 112:
			return "kW";
		case 53:
			return "Hz";
		case 170:
			return "kJ/kg";
		case 115:
			return "Wh";
		case 171:
			return "kcal/kg";
		case 55:
			return "Impulse";
		case 116:
			return "kWh";
		case 172:
			return "mg/l";
		case 117:
			return "mW/cm²";
		case 173:
			return "g/l";
		case 175:
			return "dB";
		case 119:
			return "Wh/m2";
		case 176:
			return "dBm";
		case 120:
			return "mOhm";
		case 177:
			return "dBA";
		case 60:
			return "m/s";
		case 121:
			return "Ohm";
		default:
			return "N/A";
		}
	}
	
	private synchronized int detectMeasuredValue(int sensorType) {
		int retCode = -36;
		try {
			// Ermittle Messwerte
			this.outdblFloatValue = 0;
			this.sendMeasuredValueRequest(sensorType);
			synchronized( this.connRxTx ) {
				try {
					if (this.timeOut > 0)
						this.connRxTx.wait(this.timeOut);
					else
						this.connRxTx.wait();
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}
			
			retCode = this.decodeValue16((short)this.responseData[3], (short)this.responseData[4]);
			if (!CRC.calc(this.responseData, this.responseData.length) || retCode < 0) {
				System.err.println(this.getClass().getSimpleName()+" GFTB100-Uebertragungsfehler, Widerspruch in CRC-Byte!");
				retCode = -36;	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return retCode;	
	}

	
	@Override
	public MeteorologyParameters getMeteorologyParameters() {
		double dryTemp = 0, humidity = 0, pressure = 0, wetTemp = 0;
		
		int returnCode = -36;

		// Bestimme Temperatur
		returnCode = this.detectMeasuredValue(GFTB100Sensor.T);
		if (returnCode < 0)
			return null;
		dryTemp = this.outdblFloatValue;

		// Bestimme Druck
		returnCode = this.detectMeasuredValue(GFTB100Sensor.P);
		if (returnCode < 0)
			return null;	
		pressure = this.outdblFloatValue;

		// Bestimme Luftfeuchte
		returnCode = this.detectMeasuredValue(GFTB100Sensor.RH);
		if (returnCode < 0)
			return null;
		humidity = this.outdblFloatValue;

		// Bestimme Feuchttemperatur
		returnCode = this.detectMeasuredValue(GFTB100Sensor.TW);
		if (returnCode < 0)
			return null;
		wetTemp = this.outdblFloatValue;

		return new MeteorologyParameters(pressure, dryTemp, wetTemp, humidity);
	}
	
	@Override
	public RxTx getRxTx() {
		return this.connRxTx;
	}
}
