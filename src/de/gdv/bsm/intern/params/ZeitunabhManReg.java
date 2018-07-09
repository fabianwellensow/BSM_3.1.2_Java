package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Zeitunabhängige Vorgaben des Unternehmens. Abbild des Blattes <code>zeitunabh.ManReg</code>.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zuständigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschließen, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende Rückmeldungen würde der GDV
 * begrüßen. Der GDV übernimmt aber keine Haftung für die fehlerfreie Funktionalität des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschließlich aller Tabellen, Grafiken und Erläuterungen sind urheberrechtlich
 * geschützt. Die ausschließlichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell © GDV 2016</b>
 */
public class ZeitunabhManReg {
	// B2
	private final double monatZahlung;
	private final double faelligkeitZinstitel;
	private final double steuersatz;
	private final int vereinfachungsStufeBerechnungRisikomarge;
	private final double kapitalkostenmarge;
	private final double rmProzentsatz;
	private final boolean anwendungRueckstellungsTransitional;

	// B10
	private final double bwrGrenzeEq;
	private final double anteilEqY;
	private final double bwrGrenzeRe;
	private final double anteilReY;
	private final int steuerungsMethodeAssetAllokation;
	// B15
	private final double aFiZiel;
	private final double aMinFi;
	private final double zielMindestDaaFi;
	private final double zielAnteilARe;
	private final double zielAnteilReDaa;
	// B20
	private final double daaFaktorRw;
	private final double daaFaktorFiBwr;
	private final double daaFaktorRwVerluste;
	private final double daaFaktorUntergrenzePassiveAktiveReserven;
	private final double abschreibungsGrenzeRW;
	// B25
	private final double faktorKapitalanlagen;
	private final double faktorKapitalanlagenKostenStress;
	private final int strategie;
	private final boolean iJuez;
	// B30
	private final double schalterVerrechnungLebensversicherungsreformgesetz;
	private final String zzrMethodeAltbestand;
	private final double parameter2M;
	private final double pFrfbMin;
	private final double pFrfbMax;
	private final int anzahlJahreDurchschnittlRfbZufuehrung;
	private final int deklarationsMethode;
	private final double zinsToleranz;
	private final double erhoehungBasisStorno;
	private final double erhoehungKapitalAbfindung;

	/**
	 * Erstelle die Datenstruktur aus einer Eingangsdatei.
	 * 
	 * @param dataFile
	 *            Dateiname der Eingangsdatei
	 * @throws IOException
	 *             bei Ein-/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	@SuppressWarnings("unused")
	public ZeitunabhManReg(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// skip header
			csv.readLine();

			monatZahlung = getDouble(csv); // B2
			faelligkeitZinstitel = getDouble(csv);
			steuersatz = getDouble(csv);
			// B5
			vereinfachungsStufeBerechnungRisikomarge = getInt(csv);
			kapitalkostenmarge = getDouble(csv);
			rmProzentsatz = getDouble(csv);
			anwendungRueckstellungsTransitional = getInt(csv) == 1;

			String dummy = getString(csv); // B9 ist obsolet
			// B10
			bwrGrenzeEq = getDouble(csv);
			anteilEqY = getDouble(csv);
			bwrGrenzeRe = getDouble(csv);
			anteilReY = getDouble(csv);
			steuerungsMethodeAssetAllokation = getInt(csv);
			// B15
			aFiZiel = getDouble(csv);
			aMinFi = getDouble(csv);
			zielMindestDaaFi = getDouble(csv);
			zielAnteilARe = getDouble(csv);
			zielAnteilReDaa = getDouble(csv);
			// B20
			daaFaktorRw = getDouble(csv);
			daaFaktorFiBwr = getDouble(csv);
			daaFaktorRwVerluste = getDouble(csv);
			daaFaktorUntergrenzePassiveAktiveReserven = getDouble(csv);
			abschreibungsGrenzeRW = getDouble(csv);
			// B25
			dummy = getString(csv); // B9 ist obsolet
			faktorKapitalanlagen = getDouble(csv);
			faktorKapitalanlagenKostenStress = getDouble(csv);
			strategie = getInt(csv);
			iJuez = getInt(csv) == 0 ? false : true;
			// B30
			schalterVerrechnungLebensversicherungsreformgesetz = getDouble(csv);
			zzrMethodeAltbestand = getString(csv);
			parameter2M = getDouble(csv);
			pFrfbMin = getDouble(csv);
			pFrfbMax = getDouble(csv);
			// B30
			// wir akzeptieren auch double:
			anzahlJahreDurchschnittlRfbZufuehrung = (int) getDouble(csv);
			deklarationsMethode = getInt(csv);
			zinsToleranz = getDouble(csv);
			erhoehungBasisStorno = getDouble(csv);
			erhoehungKapitalAbfindung = getDouble(csv);
		}
	}

	// Felder stehen immer in der zweiten Spalte:
	private int getInt(final CsvReader csv) throws LineFormatException, IOException {
		return csv.readLine().getInt(1);
	}

	// Felder stehen immer in der zweiten Spalte
	private double getDouble(final CsvReader csv) throws IOException, LineFormatException {
		return csv.readLine().getDouble(1);
	}

	// Felder stehen immer in der zweiten Spalte
	private String getString(final CsvReader csv) throws IOException, LineFormatException {
		return csv.readLine().getString(1);
	}

	/**
	 * Zahlungszeitpunkt für Versicherungstechnik. Feld B2.
	 * 
	 * @return der Wert
	 */
	public double getMonatZahlung() {
		return monatZahlung;
	}

	/**
	 * Fälligkeitszeitpunkt für Zinstitel aus dem heutigen KA-Bestand. Feld B3.
	 *
	 * @return der Wert
	 */
	public double getFaelligkeitZinstitel() {
		return faelligkeitZinstitel;
	}

	/**
	 * Steuersatz. Feld B4.
	 *
	 * @return der Wert
	 */
	public double getSteuersatz() {
		return steuersatz;
	}

	/**
	 * Vereinfachungsstufe zur Berechnung der Risikomarge (2-4). Feld B5.
	 *
	 * @return der Wert
	 */
	public int getVereinfachungsStufeBerechnungRisikomarge() {
		return vereinfachungsStufeBerechnungRisikomarge;
	}

	/**
	 * Kapitalkostenmarge (CoC). Feld B6.
	 *
	 * @return der Wert
	 */
	public double getKapitalkostenmarge() {
		return kapitalkostenmarge;
	}

	/**
	 * RM Prozentsatz, der pro LoB festzulegen ist. Feld B7.
	 *
	 * @return der Wert
	 */
	public double getRmProzentsatz() {
		return rmProzentsatz;
	}

	/**
	 * Anwendung des Rückstellungstransitional (0: nein, 1:ja). Feld B8.
	 *
	 * @return der Wert
	 */
	public boolean getAnwendungRueckstellungsTransitional() {
		return anwendungRueckstellungsTransitional;
	}

	/**
	 * BWR-Grenze, ab wann die EQ realisiert werden X%. Feld B10.
	 *
	 * @return der Wert
	 */
	public double getBwrGrenzeEq() {
		return bwrGrenzeEq;
	}

	/**
	 * Anteil an EQ, die realisiert werden Y%. Feld B11.
	 *
	 * @return der Wert
	 */
	public double getAnteilEqY() {
		return anteilEqY;
	}

	/**
	 * BWR-Grenze, ab wann die RE realisiert werden X%. Feld B12.
	 *
	 * @return der Wert
	 */
	public double getBwrGrenzeRe() {
		return bwrGrenzeRe;
	}

	/**
	 * Anteil an RE, die realisiert werden Y%. Feld B13.
	 *
	 * @return der Wert
	 */
	public double getAnteilReY() {
		return anteilReY;
	}

	/**
	 * Steuerungsmethode Asset Allokation. Mögliche Werte:
	 * <ul>
	 * <li>0 – statische AA,</li>
	 * <li>1 – DAA mit Rechnungszins,</li>
	 * <li>2 – DAA, Marktwertsicht).</li>
	 * </ul>
	 * Feld B14.
	 *
	 * @return der Wert
	 */
	public int getSteuerungsMethodeAssetAllokation() {
		return steuerungsMethodeAssetAllokation;
	}

	/**
	 * Zielanteil FI a_FI_Ziel. Feld B15.
	 *
	 * @return der Wert
	 */
	public double getaFiZiel() {
		return aFiZiel;
	}

	/**
	 * Mindestanteil FI a_min_FI. Feld B16.
	 *
	 * @return der Wert
	 */
	public double getaMinFi() {
		return aMinFi;
	}

	/**
	 * Ziel- und Mindestanteil FI, wenn DAA "schlechte wirtschaftl. Situation" auslöst. Feld B17.
	 *
	 * @return der Wert
	 */
	public double getZielMindestDaaFi() {
		return zielMindestDaaFi;
	}

	/**
	 * Zielanteil RE a_RE_Ziel. Feld B18.
	 *
	 * @return der Wert
	 */
	public double getZielAnteilARe() {
		return zielAnteilARe;
	}

	/**
	 * Zielanteil RE, wenn DAA "schlechte wirtschaftl. Situation" auslöst. Feld B19.
	 *
	 * @return der Wert
	 */
	public double getZielAnteilReDaa() {
		return zielAnteilReDaa;
	}

	/**
	 * DAA-Faktor auf RW (DAA-Steuerung 1). Feld B20.
	 *
	 * @return der Wert
	 */
	public double getDaaFaktorRw() {
		return daaFaktorRw;
	}

	/**
	 * DAA-Faktor auf FI-BWR (DAA-Steuerung 1). Feld B21.
	 *
	 * @return der Wert
	 */
	public double getDaaFaktorFiBwr() {
		return daaFaktorFiBwr;
	}

	/**
	 * DAA-Faktor auf RW-Verluste (DAA-Steuerung 1). Feld B22.
	 *
	 * @return der Wert
	 */
	public double getDaaFaktorRwVerluste() {
		return daaFaktorRwVerluste;
	}

	/**
	 * DAA-Faktor: Untergrenze der passiven und aktiven Reserven (DAA-Steuerung 2). Feld B23.
	 *
	 * @return der Wert
	 */
	public double getDaaFaktorUntergrenzePassiveAktiveReserven() {
		return daaFaktorUntergrenzePassiveAktiveReserven;
	}

	/**
	 * Abschreibungsgrenze für Immobilien und Aktien. Feld B24.
	 *
	 * @return der Wert
	 */
	public double getAbschreibungsGrenzeRW() {
		return abschreibungsGrenzeRW;
	}

	/**
	 * Faktor Aufwendungen für Kapitalanlagen. Feld B26.
	 *
	 * @return der Wert
	 */
	public double getFaktorKapitalanlagen() {
		return faktorKapitalanlagen;
	}

	/**
	 * Faktor Aufwendungen für Kapitalanlagen für Kostenstress. Feld B27.
	 *
	 * @return der Wert
	 */
	public double getFaktorKapitalanlagenKostenStress() {
		return faktorKapitalanlagenKostenStress;
	}

	/**
	 * Strategie (1-3). Mögliche Werte sind:
	 * <ul>
	 * <li>(1) Zielverzinsung Eigenkapital</li>
	 * <li>(2) Zielbeteiligung VN an Rohüberschuss</li>
	 * <li>(3) Dynamisch Zielbeteiligung VN am Rohüberschuss</li>
	 * </ul>
	 *
	 * Feld B28.
	 *
	 * @return der Wert
	 */
	public int getStrategie() {
		return strategie;
	}

	/**
	 * Schalter Erhöhung der Zielverzinsung (0: nein, 1: ja). Feld B29.
	 *
	 * @return der Wert
	 */
	public boolean isiJuez() {
		return iJuez;
	}

	/**
	 * Schalter Verrechnung nach Lebensversicherungsreformgesetz (zwischen 0% und 100%). Feld B30.
	 *
	 * @return der Wert
	 */
	public double getSchalterVerrechnungLebensversicherungsreformgesetz() {
		return schalterVerrechnungLebensversicherungsreformgesetz;
	}

	/**
	 * 
	 * ZZR Methode für Altbestand. Feld B31.
	 * 
	 * @return der Wert
	 */
	public String getZzrMethodeAltbestand() {
		return zzrMethodeAltbestand;
	}

	/**
	 * Parameter für Methode 2M: Berücksichtigung des Korrekturterms in Prozent. Feld B32.
	 *
	 * @return der Wert
	 */
	public double getParameter2M() {
		return parameter2M;
	}

	/**
	 * Untergrenze der freien RfB in Prozent der Deckungsrückstellung p_fRfB_min. Feld B33.
	 *
	 * @return der Wert
	 */
	public double getpFrfbMin() {
		return pFrfbMin;
	}

	/**
	 * Obergrenze der freien RfB in Prozent der Deckungsrückstellung p_fRfB_max. Feld B34.
	 *
	 * @return der Wert
	 */
	public double getpFrfbMax() {
		return pFrfbMax;
	}

	/**
	 * Anzahl Jahre zur Bestimmung der durchschnittlichen RfB-Zuführung, m. Feld B35.
	 *
	 * @return der Wert
	 */
	public int getAnzahlJahreDurchschnittlRfbZufuehrung() {
		return anzahlJahreDurchschnittlRfbZufuehrung;
	}

	/**
	 * Deklarationsmethode. Feld B36.
	 * 
	 * @return der Wert
	 */
	public int getDeklarationsMethode() {
		return deklarationsMethode;
	}

	/**
	 * Zinstoleranz der VN delta_zins. Feld B37.
	 *
	 * @return der Wert
	 */
	public double getZinsToleranz() {
		return zinsToleranz;
	}

	/**
	 * Prozentuale Erhöhung des Basisstornosatzes pro 1 % Zinsänderung jenseits der Zinstoleranz der VN. Feld B38.
	 *
	 * @return der Wert
	 */
	public double getErhoehungBasisStorno() {
		return erhoehungBasisStorno;
	}

	/**
	 * prozentuale Erhöhung der Kapitalabfindungswahrscheinlichkeit je Zinsänderung jenseits der Zinstoleranz der VN.
	 * Feld B39.
	 *
	 * @return der Wert
	 */
	public double getErhoehungKapitalAbfindung() {
		return erhoehungKapitalAbfindung;
	}

}
