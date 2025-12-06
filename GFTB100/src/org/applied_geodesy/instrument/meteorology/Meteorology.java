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
 * <p>Zur Bestimmung der ersten Geschwindigkeitskorrektur aus meteorologischen Messungen
 * stellt diese Klasse entsprechende Methoden zur Verfuegung. Die Formeln zur Berechnung
 * entstammen:</p>
 * 
 * <ul>
 * <li>Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
 * 4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999.</li>
 * <li>Joeckel, R., Stober, M., Huep, W.: Elektronische Entfernungs- und Richtungsmessung, 
 * 5. Auflage, Wichmann, Berlin/Heidelberg, 2008.</li>
 * <li>R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
 * 3. Auflage, Springer, Berlin/Heidelberg, 1990</li>
 * <li>Ciddor, P.E.: Refractive index of air: new equations for the visible and near infrared, 
 * Applied Optics, Vol. 35, No 9, 1566-1573, 1996</li>
 * </ul>
 * 
 * @author Michael Loesler
 *
 */
public class Meteorology {
	private final static double CIDDOR_1996_A  = 295.23500*1.022; // cf = 1.022 (Ciddor 1996)
	private final static double CIDDOR_1996_B  =  2.642200*1.022;
	private final static double CIDDOR_1996_C  = -0.032380*1.022;
	private final static double CIDDOR_1996_D  =  0.004028*1.022;

	private final static double EDLEN_1953_A = 287.569;
	private final static double EDLEN_1953_B =  1.6206;
	private final static double EDLEN_1953_C =  0.0139;

	private final static double EDLEN_1966_A = 287.583;
	private final static double EDLEN_1966_B =  1.6134;
	private final static double EDLEN_1966_C =  0.0144;

	private final static double IAG_1999_A = 287.6155;
	private final static double IAG_1999_B =  4.88660/3.0; // ~1.62887;
	private final static double IAG_1999_C =  0.01360;

	private final static double BARRELL_AND_SEARS_1939_A = 287.604;
	private final static double BARRELL_AND_SEARS_1939_B =  1.6288;
	private final static double BARRELL_AND_SEARS_1939_C =  0.0136;

	private final static double OWENS_1967_A = 6487.31e-2;
	private final static double OWENS_1967_B =  58.058e-2;
	private final static double OWENS_1967_C = -0.7115e-2;
	private final static double OWENS_1967_D = 0.08851e-2;


	private Meteorology() { };

	/**
	 * <p>Liefert den Saettigungsdampfdruck <code>E</code> fuer die uebergebene Temperatur <code>t</code></p>
	 * <p><code>E(T = t + 273.15 | t &ge; 0) = exp(A*T*T + B*T + C + D/T)</code></p>
	 * <p><code>E(T = t + 273.15 | t &lt; 0) = 10<sup>(E/T + F)</sup></code></p>
	 * 
	 * <ul>
	 * <li>A =  1.2378847E-5</li>
	 * <li>B = -1.9121316E-2</li>
	 * <li>C = 33.93711047</li>
	 * <li>D = -6.3431645E3</li>
	 * <li>E = -2663.5</li>
	 * <li>F =  12.537</li>
	 * </ul>
	 * 
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 * 
	 * @param t   Temperatur  [&#8451;]
	 * @return E  Saettigungsdampfdruck [hPa]
	 */
	public static double getSaturationVapourPressure(double t) {
		double T = t + 273.15;
		double A =  1.2378847E-5;
		double B = -1.9121316E-2;
		double C = 33.93711047;
		double D = -6.3431645E3;
		double E = -2663.5;
		double F =  12.537;

		// in hPa
		return t >= 0 ? 0.01*Math.exp(A*T*T + B*T + C + D/T) : 0.01*Math.pow(10, E/T + F);
	}

	/**
	 * <p>Liefert den Gruppenbrechungsindex <code>n<sub>Gr</sub></code></p> 
	 * 
	 * <p><code>n<sub>Gr</sub> = 1.0 + 10<sup>-6</sup>*(a + 3*b/&lambda;<sub>T</sub><sup>2</sup> + 5*c/&lambda;<sub>T</sub><sup>4</sup> + 7*d/&lambda;<sub>T</sub><sup>6</sup>)</sup></code></p>
	 * <p>Die Parameter <code>a</code>, <code>b</code>, <code>c</code> und <code>d</code> haengen vom gewaehlten Modell ab.</p>
	 * 
	 * <p>Edlen (1953)</p>
	 * <ul>
	 * <li>a = 287.569</li>
	 * <li>b =  1.6206</li>
	 * <li>c =  0.0139</li>
	 * <li>d =  0.0</li>
	 * </ul>
	 * 
	 * <p>Edlen (1966)</p>
	 * <ul>
	 * <li>a = 287.583</li>
	 * <li>b =  1.6134</li>
	 * <li>c =  0.0144</li>
	 * <li>d =  0.0</li>
	 * </ul>
	 * 
	 * <p>IAG (1999)</p>
	 * <ul>
	 * <li>a = 287.6155</li>
	 * <li>b =  1.62887</li>
	 * <li>c =  0.01360</li>
	 * <li>d =  0.0</li>
	 * </ul>
	 * 
	 * <p>CIDDOR (1999)</p>
	 * <ul>
	 * <li>a = 295.235 * cf</li>
	 * <li>b =  2.6422/3 * cf</li>
	 * <li>c = -0.032380/5 * cf</li>
	 * <li>d =  0.004028/7 * cf</li>
	 * </ul>
	 * mit cf = 1.022 * 10<sup>-2</sup>
	 * 
	 * <p>Barrell &amp; Sears (1939)</p>
	 * <ul>
	 * <li>a = 287.604</li>
	 * <li>b =  1.6288</li>
	 * <li>c =  0.0136</li>
	 * <li>d =  0.0</li>
	 * </ul>
	 *
	 * <p>Formel ist definiert fuer</p>
	 * <ul>
	 * <li>t = 0&#8451;</li>
	 * <li>p = 1013.25hPa</li>
	 * <li>CO<sub>2</sub> = 0.03% (IAG (1999) 0.0375%)</li>
	 * <li>trockene Luft</li>
	 * </ul>
	 * 
	 * <p>Geltungsbereich des Dispersionsmodells</p>
	 * <ul>
	 * <li>0.18 &lt; &lambda;<sub>T</sub> &lt; 0.65 Edlen (1953)</li> 
	 * <li>0.18 &lt; &lambda;<sub>T</sub> &lt; 2.10 Edlen (1966)</li>
	 * <li>0.43 &lt; &lambda;<sub>T</sub> &lt; 0.65 Barrell &amp; Sears (1939)</li>
	 * </ul>
	 * 
	 * @see Joeckel, R., Stober, M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                               4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                               Seite 72f
	 * @see Joeckel, R., Stober, M., Huep, W.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                               5. Auflage, Wichmann, Berlin/Heidelberg, 2008
	 *                               Seite 97f
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 * @see R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
	 *                         3. Auflage, Springer, Berlin/Heidelberg, 1990
	 *                         Seite 222ff 
	 *                    
	 * @param model           Empirische Parameter zur Bestimmung des Gruppenbrechungsindex
	 * @param lamdaT          Traegerwellenlaenge &lambda;<sub>T</sub> [&mu;m]
	 * @return n<sub>Gr</sub> Brechungsindex
	 */
	private static double getGroupRefractiveIndex(DispersionModel model, double lamdaT) {
		double a,b,c,d;
		switch(model) {
		case EDLEN_1953:
			a = EDLEN_1953_A;
			b = EDLEN_1953_B;
			c = EDLEN_1953_C;
			d = 0.0;
			break;
		case EDLEN_1966:
			a = EDLEN_1966_A;
			b = EDLEN_1966_B;
			c = EDLEN_1966_C;
			d = 0.0;
			break;
		case IAG_1999:
			a = IAG_1999_A;
			b = IAG_1999_B; // ~1.62887;
			c = IAG_1999_C;
			d = 0.0;
			break;
		case OWENS_1967:
			a = OWENS_1967_A;
			b = OWENS_1967_B;
			c = OWENS_1967_C;
			d = OWENS_1967_D;
			break;
		case CIDDOR_1996:
		case CIDDOR_2002:
			a = CIDDOR_1996_A*1E-2;
			b = CIDDOR_1996_B*1E-2;
			c = CIDDOR_1996_C*1E-2;
			d = CIDDOR_1996_D*1E-2;
			break;
		default: // BARRELL_AND_SEARS_1939
			a = BARRELL_AND_SEARS_1939_A;
			b = BARRELL_AND_SEARS_1939_B;
			c = BARRELL_AND_SEARS_1939_C;
			d = 0.0;
			break;
		}
		return 1.0 + 1.0E-6*(a + 3.0*b/Math.pow(lamdaT,2) + 5.0*c/Math.pow(lamdaT,4) + 7.0*d/Math.pow(lamdaT,6));
	}

	/**
	 * <p>Liefert den Partialdruck des Wasserdampfs <code>e</code></p>
	 * 
	 * <p><code>e = E<sub>w</sub> - (t-tw) * K/1006.6*p</code> mit Saettigungsdampfdruck
	 * <code>E<sub>w</sub></code> und <code>K = 0.67</code></p>
	 * 
	 * @see Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                              4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                              Seite 75
	 *                              
	 * @param t  Trockentemperatur [&#8451;]
	 * @param tw Feuchttemperatur [&#8451;]
	 * @param p  Luftdruck [hPa]
	 * @return e Partialdruck [hPa]
	 */
	public static double getPartialPressure(double t, double tw, double p) {
		double K = 0.67;
		double Ew = getSaturationVapourPressure(tw);
		return Ew - (t-tw)*K/1006.6*p;
	}

	/**
	 * <p>Liefert den Partialdruck des Wasserdampfs <code>e</code></p>
	 * 
	 * <p><code>e = E * r/100</code> mit Saettigungsdampfdruck <code>E</code></p>
	 * 
	 * @see Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                              4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                              Seite 76
	 *                              
	 * @param t  Trockentemperatur [&#8451;]
	 * @param r  Relative Luftfeuchte [%]
	 * @return e Partialdruck [hPa]
	 */
	public static double getPartialPressure(double t, double r) {
		double E = getSaturationVapourPressure(t);
		return E * 0.01*r;
	}

	/**
	 * <p>Liefert den Brechungsindex <code>n<sub>L</sub></code></p>
	 * <p><code>n<sub>L</sub> = 1 + (n<sub>Gr</sub>-1) * 273.15/1013.25 * p/(t+273.15) - 11.27*10<sup>-6</sup>/(t+273.15) * e</code></p>
	 * <p>mit Partialdruck des Wasserdampfs <code>e</code>, Temperatur <code>t</code>, Druck <code>p</code> und Gruppenbrechungsindex <code>n<sub>Gr</sub></code></p>
	 * <p>Fuer Owens (1967) und Ciddor (1996) werden individuelle Gleichungen verwendet.
	 * 
	 * @see R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
	 *                         3. Auflage, Springer, Berlin/Heidelberg, 1990
	 *                         Seite 55               
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 * @see Ciddor, P.E.: Refractive index of air: 3. The roles of CO2, 
	 *                    H2O, and refractivity virials, 
	 *                    Applied Optics, Vol. 41, No 12, 2292-2298, 2002
	 * @see R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
	 *                         3. Auflage, Springer, Berlin/Heidelberg, 1990
	 *                         Seite 222ff 
	 *
	 * @param model   Dispersionsmodel
	 * @param lamdaT  Traegerwellenlaenge [&mu;m]
	 * @param t       Temperatur [&#8451;]
	 * @param p       Luftdruck [hPa]
	 * @param CO2     C02-Konzentration [ppm]
	 * @param e       Partialdruck des Wasserdampfs [hPa]
	 * @return nL     Brechungsindex
	 */
	public static double getRefractiveIndex(DispersionModel model, double lamdaT, double t, double p, double CO2, double e) {
		switch(model) {
		case CIDDOR_1996:
			return getCiddor1996RefractiveIndex(lamdaT, t, p, CO2, e);
		case CIDDOR_2002:
			return getCiddor2002RefractiveIndex(lamdaT, t, p, CO2, e);
		case OWENS_1967:
			return getOwens1967RefractiveIndex(lamdaT, t, p, e);
		default:
			double ng = getGroupRefractiveIndex(model, lamdaT);
			double T = t + 273.15;
			return 1.0 + (ng - 1.0) * 273.15/1013.25 * p/T - 11.27E-6/T * e;
		}
	}

	/**
	 * <p>Liefert den Brechungsindex <code>n<sub>L</sub></code></p>
	 *
	 * @see R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
	 *                         3. Auflage, Springer, Berlin/Heidelberg, 1990
	 *                         Seite 222ff 
	 *
	 * @param lamdaT  Traegerwellenlaenge [&mu;m]
	 * @param t       Temperatur [&#8451;]
	 * @param p       Luftdruck [hPa]
	 * @param e       Partialdruck des Wasserdampfs [hPa]
	 * @return nL     Brechungsindex
	 */
	private static double getOwens1967RefractiveIndex(double lamdaT, double t, double p, double e) {
		double ng = getGroupRefractiveIndex(DispersionModel.OWENS_1967, lamdaT);

		double T = t + 273.15;
		double s2 = 1.0/lamdaT/lamdaT;

		double k0  = 238.0185;
		double k1  = 1646386.0;
		double k2  = 57.362;
		double k3  = 47729.9;

		double ps = p - e;
		double Ds = ps/T * (1.0 + ps * (57.90E-8 - 9.3250E-4/T + 0.25844/T/T));
		double Dw =  e/T * (1.0 + e  * (1.0 + 3.7E-4*e) * (-2.37321E-3 + 2.23366/T - 710.792/T/T + 7.75141E4/T/T/T));

		double ns = 1E-8*(k1*(k0 + s2)/(k0 - s2)/(k0 - s2) + k3*(k2 + s2)/(k2 - s2)/(k2 - s2)) + 1.0;

		return (ns - 1.0)*Ds + (ng - 1.0)*Dw + 1.0;
	}

	/**
	 * <p>Liefert den Brechungsindex <code>n<sub>L</sub></code></p>
	 * 
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 *
	 * @param lamdaT  Traegerwellenlaenge [&mu;m]
	 * @param t       Temperatur [&#8451;]
	 * @param p       Luftdruck [hPa]
	 * @param e       Partialdruck des Wasserdampfs [hPa]
	 * @param CO2     C02-Konzentration [ppm]
	 * @return nL     Brechungsindex
	 */
	private static double getCiddor1996RefractiveIndex(double lamdaT, double t, double p, double CO2, double e) {
		double T = t + 273.15;
		double s2 = 1.0/lamdaT/lamdaT;

		final double k0 = 238.0185;
		final double k1 = 5792105.0;
		final double k2 = 57.362;
		final double k3 = 167917.0;

		final double Ma = 1E-3*(28.9635 + 12.011E-6*(CO2 - 400));
		final double Mw = 0.018015;
		final double R  = 8.314510;

		double rhoaxs = getDensityOfMoistAir(15.0, 1013.25, 0.0, Ma, Mw);
		double rhows  = getDensityOfMoistAir(20.0,  13.330, 1.0, Ma, Mw);
		double nw = 1E-8*(CIDDOR_1996_A + CIDDOR_1996_B*s2 + CIDDOR_1996_C*s2*s2 + CIDDOR_1996_D*s2*s2*s2) + 1.0;
		double na = 1E-8*(k1/(k0 - s2) + k3/(k2 - s2));
		na = na * (1.0 + 0.534E-6 * (CO2 - 450)) + 1.0;

		double xw = getMolarFractionOfWaterVaporInMoistAir(t, p, e);
		double Z  = getCompressibilityOfMoistAir(t, p, xw);

		double ZRT = Z*R*T;
		double rhoa = 100.0*p * Ma * (1.0 - xw)/ZRT;
		double rhow = 100.0*p * Mw * xw/ZRT;

		double La = ((na - 1.0)*(na - 1.0) + 2.0*(na - 1.0))/((na - 1.0)*(na - 1.0) + 2.0*(na - 1.0) + 3.0);
		double Lw = ((nw - 1.0)*(nw - 1.0) + 2.0*(nw - 1.0))/((nw - 1.0)*(nw - 1.0) + 2.0*(nw - 1.0) + 3.0);
		//		double La = (na*na - 1.0) / (na*na + 2.0);
		//		double Lw = (nw*nw - 1.0) / (nw*nw + 2.0);
		double LL = (rhoa/rhoaxs)*La + (rhow/rhows)*Lw;
		double nLL = Math.sqrt((1.0 + 2.0*LL) / (1.0 - LL));

		double dnasx = 2.0/lamdaT *  (k1/(k0 - s2)/(k0 - s2) + k3/(k2 - s2)/(k2 - s2)) * (1.0 + 0.534E-6 * (CO2 - 450)) * 1e-8;
		double dnws  = 2.0/lamdaT * (CIDDOR_1996_B + 2.0*CIDDOR_1996_C*s2 + 3.0*CIDDOR_1996_D*s2*s2) * 1e-8;

		double DLas = 6.0*na/(na*na + 2.0)/(na*na + 2.0) * dnasx;
		double DLvs = 6.0*nw /(nw*nw+ 2.0)/(nw*nw + 2.0) * dnws;

		return nLL + (nLL*nLL + 2.0)*(nLL*nLL + 2.0) / lamdaT / 6.0 / nLL * (DLas*rhoa/rhoaxs + DLvs*rhow/rhows);
	}

	/**
	 * <p>Liefert den Brechungsindex <code>n<sub>L</sub></code></p>
	 * 
	 * @see Ciddor, P.E.: Refractive index of air: 3. The roles of CO2, 
	 *                    H2O, and refractivity virials, 
	 *                    Applied Optics, Vol. 41, No 12, 2292-2298, 2002
	 *
	 * @param lamdaT  Traegerwellenlaenge [&mu;m]
	 * @param t       Temperatur [&#8451;]
	 * @param p       Luftdruck [hPa]
	 * @param e       Partialdruck des Wasserdampfs [hPa]
	 * @param CO2     C02-Konzentration [ppm]
	 * @return nL     Brechungsindex
	 */
	private static double getCiddor2002RefractiveIndex(double lamdaT, double t, double p, double CO2, double e) {
		double T = t + 273.15;
		double s2 = 1.0/lamdaT/lamdaT;

		final double k0 = 238.0185;
		final double k1 = 5792105.0;
		final double k2 = 57.362;
		final double k3 = 167917.0;
		
		final double kc0 = 154.489;
		final double kc1 = 0.0584738;
		final double kc2 = 8309192.7;
		final double kc3 = 210.92417;
		final double kc4 = 287641.9;
		final double kc5 = 60.122959;

		final double R  = 8.314510;
		final double Md = 28.9575e-3;
		final double Mc = 44.010e-3;
		final double Mw = 18.015e-3;

		double xw = getMolarFractionOfWaterVaporInMoistAir(t, p, e);
		double xc = CO2*1e-6;
		double xd = 1.0 - xw - xc;

		double Z  = getCompressibilityOfMoistAir(t, p, xw);
		double ZRT = Z*R*T;

		double rhod = p*Md*xd/ZRT;
		double rhow = p*Mw*xw/ZRT;
		double rhoc = p*Mc*xc/ZRT;
		
		double td = 15.0;
		double Td = td + 273.15;
		double pd = 1013.25; // 101325Pa
		double Zd = getCompressibilityOfMoistAir(td, pd, 0.0);
		double ZRTd = Zd*R*Td;

		double tw = 20.0;
		double Tw = tw + 273.15;
		double pw = 13.33; // 1333Pa
		double Zw = getCompressibilityOfMoistAir(tw, pw, 1.0);
		double ZRTw = Zw*R*Tw;

		double tc = 0.0;
		double Tc = tc + 273.15;
		double pc = 0.01; // 1Pa
		double Zc = getCompressibilityOfMoistAir(tc, pc, 1.0);
		double ZRTc = Zc*R*Tc;
		
		double rhods = pd * Md/ZRTd;
		double rhows = pw * Mw/ZRTw;
		double rhocs = pd * Mc/ZRTc;

		double Dd = rhod/rhods;
		double Dw = rhow/rhows;
		double Dc = rhoc/rhocs;

		xc = 0;
		double Nas  = k1/(k0 - s2) + k3/(k2 - s2);
		double dnas = 2.0/lamdaT * (k1/(k0 - s2)/(k0 - s2) + k3/(k2 - s2)/(k2 - s2)) * 1e-8;
		double Nds  = (1.0 + 0.534 * (xc - 0.000450)) * Nas;
		double dnds = (1.0 + 0.534 * (xc - 0.000450)) * dnas;

		double Nws  = CIDDOR_1996_A + CIDDOR_1996_B*s2 + CIDDOR_1996_C*s2*s2 + CIDDOR_1996_D*s2*s2*s2;
		double dnws = 2.0/lamdaT * (CIDDOR_1996_B + 2.0*CIDDOR_1996_C*s2 + 3.0*CIDDOR_1996_D*s2*s2) * 1e-8;
		
		double Ncs  = kc0/(kc1 - s2) + kc2/(kc3 - s2) + kc4/(kc5 - s2);
		double dncs = 2.0/lamdaT * (kc0/(kc1 - s2)/(kc1 - s2) + kc2/(kc3 - s2)/(kc3 - s2) + kc4/(kc5 - s2)/(kc5 - s2)) * 1e-8;

		double nds1 = Nds*1e-8;
		double nws1 = Nws*1e-8;
		double ncs1 = Ncs*1e-8;
		
		double Ld = (nds1*nds1 + 2.0*nds1)/(nds1*nds1 + 2.0*nds1 + 3.0);
		double Lw = (nws1*nws1 + 2.0*nws1)/(nws1*nws1 + 2.0*nws1 + 3.0);
		double Lc = (ncs1*ncs1 + 2.0*ncs1)/(ncs1*ncs1 + 2.0*ncs1 + 3.0);
		
		double L = Ld*Dd + Lw*Dw + Lc*Dc;
		double nLL = Math.sqrt( (1.0 + 2.0*L) / (1.0 - L) );
		
		double nds = 1.0 + Nds*1e-8;
		double nws = 1.0 + Nws*1e-8;
		double ncs = 1.0 + Ncs*1e-8;

		double dLds = 6.0*nds/(nds*nds + 2.0)/(nds*nds + 2.0) * dnds;
		double dLws = 6.0*nws/(nws*nws + 2.0)/(nws*nws + 2.0) * dnws;
		double dLcs = 6.0*ncs/(ncs*ncs + 2.0)/(ncs*ncs + 2.0) * dncs;
		
		return nLL + (nLL*nLL + 2.0)*(nLL*nLL + 2.0)/lamdaT/6.0/nLL * (dLds*Dd + dLws*Dw + dLcs*Dc);
	}

	/**
	 * <p>Konvertiert aus der Trockentemperatur, dem Luftdruck und der relativen Luftfeuchte die zugehoerige Feuchttemperatur.</p>
	 * <p>Umkehrung der Berechnung <code>r = 100*e/E</code> mit dem Saettigungsdampfdruck <code>E</code> und dem Partialdruck <code>e</code>.</p>
	 * 
	 * @param temperature      Trockentemperatur [&#8451;]
	 * @param pressure         Luftdruck [hPa]
	 * @param relativeHumidity Relative Luftfeuchte [%] 
	 * @return tw              Feuchttemperatur  [&#8451;]
	 * @throws ArithmeticException Wenn max. Anzahl an Iterationen (100) erreicht sind.
	 */
	public static double humidity2WetBulbTemperature(double temperature, double pressure, double relativeHumidity) throws ArithmeticException {
		if (relativeHumidity < 0)
			return 0.0;

		final double EPS = 1.0E-5;
		int maxIteration = 100;
		double currentHumidity = 100.0;

		double twUpper = temperature;
		double twLower = temperature;
		double E = getSaturationVapourPressure(temperature);

		// Bestimmung des unteren Grenzbereiches twLower
		while ( currentHumidity > relativeHumidity && maxIteration-- > 0) {
			twLower -= 10;
			double e = getPartialPressure(temperature, twLower, pressure);
			currentHumidity = e/E*100.0;
		}

		if(maxIteration<0)
			throw new ArithmeticException("Fehler beim Berechnen der Feuchttemperatur! Differenz: " + relativeHumidity);

		maxIteration = 100;
		// Berechnung der Feuchttemperatur
		do {
			double tw = 0.5*(twUpper+twLower);
			double e = getPartialPressure(temperature, tw, pressure);
			currentHumidity = e/E*100.0;

			if (currentHumidity > relativeHumidity)
				twUpper = tw;
			else if (currentHumidity < relativeHumidity) 
				twLower = tw;
			// System.out.println( (currentHumidity - relativeHumidity)+"  "+currentHumidity + "  " +relativeHumidity);
		}
		while ( Math.abs(currentHumidity - relativeHumidity) > EPS && maxIteration-- > 0);

		if(maxIteration<0)
			throw new ArithmeticException("Fehler beim Berechnen der Feuchttemperatur! Differenz: " + Math.abs(currentHumidity - relativeHumidity));
		return 0.5*(twUpper+twLower);
	}

	/**
	 * <p>Konvertiert aus der Trockentemperatur, dem Luftdruck und der Feuchttemperatur die zugehoerige relative Luftfeuchte.</p>
	 * <p><code>r = 100*e/E</code> mit dem Saettigungsdampfdruck <code>E</code> und dem Partialdruck <code>e</code>.</p>
	 * 
	 * @param temperature        Trockentemperatur [&#8451;]
	 * @param pressure           Luftdruck [hPa]
	 * @param wetBulbTemperature Feuchttemperatur  [&#8451;]
	 * @return relativeHumidity  Relative Luftfeuchte [%]
	 */
	public static double wetBulbTemperature2Humidity(double temperature, double pressure, double wetBulbTemperature) {
		double E = getSaturationVapourPressure(temperature);
		double e = getPartialPressure(temperature, wetBulbTemperature, pressure);
		return e/E*100.0;
	}

	/**
	 * Liefert die Dichte von feuchter Luft
	 * @param t    Temperatur [&#8451;]
	 * @param p    Luftdruck [hPa]
	 * @param xw   Molanteil von Wasserdampf in feuchter Luft
	 * @param Ma   Molmasse von trockener Luft [kg/mol]
	 * @param Mw   Molmasse von Wasserdampf [kg/mol]
	 * @return rho
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 */
	private static double getDensityOfMoistAir(double t, double p, double xw, double Ma, double Mw) {
		double pp = 100.0*p; // in Pascal
		double T = t + 273.15;
		double R = 8.314510;
		double Z = getCompressibilityOfMoistAir(t, p, xw);
		double ZRT = Z*R*T;
		return pp*Ma/ZRT * (1.0 - xw*(1.0-Mw/Ma));
	}

	/**
	 * Liefert die Kompressibilit&auml;t von feuchter Luft
	 * @param t Temperatur [&#8451;]
	 * @param p Luftdruck [hPa]
	 * @param xw Molanteil von Wasserdampf in feuchter Luft
	 * @see Ciddor, P.E.: Refractive index of air: new equations 
	 *                    for the visible and near infrared, 
	 *                    Applied Optics, Vol. 35, No 9, 1566-1573, 1996
	 * @return c
	 */
	private static double getCompressibilityOfMoistAir(double t, double p, double xw) {
		double a0 = 1.58123e-6, a1 = -2.9331e-8, a2 = 1.1043e-10;
		double b0 = 5.707e-6,   b1 = -2.051e-8;
		double c0 = 1.9898e-4,  c1 = -2.376e-6;
		double d  = 1.83e-11,   e  = -0.765e-8;

		double pp = 100.0*p; // in Pascal
		double T = t + 273.15;
		return 1.0 - (pp/T)*(a0 + a1*t + a2*t*t + (b0 + b1*t)*xw + (c0 + c1*t)*xw*xw) + (pp*pp/T/T)*(d + e*xw*xw);
	}

	/** 
	 * Bestimmt den Molanteil von Wasserdampf in feuchter Luft
	 * @param t Temperatur [&#8451;]
	 * @param p Luftdruck [hPa]
	 * @param h Feuchte [%]
	 * @return xw
	 */
	private static double getMolarFractionOfWaterVaporInMoistAir(double t, double p, double e) {
		double alpha = 1.00062;
		double beta  = 3.14e-6;
		double gamma = 5.60e-7;
		double f = alpha + beta*p + gamma*t*t;	
		return f*e/p; // f*0.01*h * svp/p, mit h = relative Luftfeuchte
		
		
	}

	public static void main (String args[]) {
		double lamdaT = 0.633;
		double t =   20.0;
		double p = 1200.0;
		double r =    0.0;
		double xCO2 = 800;
		double e = getPartialPressure(t, r);

		double nCiddor = getRefractiveIndex(DispersionModel.CIDDOR_1996, lamdaT, t, p, xCO2, e);
		System.out.println( (nCiddor-1)*1E8);
		
		nCiddor = getRefractiveIndex(DispersionModel.CIDDOR_2002, lamdaT, t, p, xCO2, e);
		System.out.println( (nCiddor-1)*1E8);
		
		double nOwens = getRefractiveIndex(DispersionModel.OWENS_1967, lamdaT, t, p, xCO2, e);
		System.out.println( (nOwens-1)*1E8);
		
	}

}
