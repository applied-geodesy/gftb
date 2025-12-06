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

package org.applied_geodesy.instrument.meteorology.gftb.test;

import org.applied_geodesy.instrument.meteorology.MeteorologyParameters;
import org.applied_geodesy.instrument.meteorology.gftb.GFTB100Sensor;
import org.applied_geodesy.io.rxtx.rxtxcomm.RxTxCommunicator;

import gnu.io.CommPortIdentifier;

public class GFTB100RxTxComm {

	public static void main(String[] args) throws Exception {
		System.out.println("Starte Messung via GFTB100RxTxComm");
		RxTxCommunicator comm = new RxTxCommunicator();
		comm.setCommPortIdentifier(CommPortIdentifier.getPortIdentifier("COM6"));
		comm.setBaudRate(4800); // fixed rate!!!
		GFTB100Sensor meteoSensor = new GFTB100Sensor(comm);
		
		comm.addReceiver(meteoSensor);

		if (comm.open()) {
			try {
				int i=1;
				while (i++ < 20) {
					MeteorologyParameters parameters = meteoSensor.getMeteorologyParameters();
					System.out.println(parameters);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				comm.close();
				comm.removeReceiver(meteoSensor);
			}
		}
	}
}
