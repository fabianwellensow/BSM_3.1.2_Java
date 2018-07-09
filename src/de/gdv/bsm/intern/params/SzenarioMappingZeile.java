package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.intern.szenario.Szenario;

/**
 * Eine Zeile des Szenario-Mappings.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust�ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie�en, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R�ckmeldungen w�rde der GDV
 * begr��en. Der GDV �bernimmt aber keine Haftung f�r die fehlerfreie Funktionalit�t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie�lich aller Tabellen, Grafiken und Erl�uterungen sind urheberrechtlich
 * gesch�tzt. Die ausschlie�lichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell � GDV 2016</b>
 */
public class SzenarioMappingZeile {
	private final int id;
	private final String name;
	private final int marktwerte;
	private final int projektionVtKlassik;
	private final int projektionVtFlv;
	private final int zinskurve;
	private final boolean aktiv;

	/**
	 * Ertselle eine Zeile des Szenario-Mappings.
	 * 
	 * @param felder
	 *            Liste der Felder dieser Zeile
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	SzenarioMappingZeile(final CsvZeile felder) throws LineFormatException {
		id = felder.getInt(0);
		name = felder.getString(1);
		marktwerte = felder.getInt(2);
		projektionVtKlassik = felder.getInt(3);
		projektionVtFlv = felder.getInt(4);
		zinskurve = felder.getInt(5);
		aktiv = felder.getBoolean(6);
	}

	/**
	 * Die ID dieses Szenarios. (Excel Spalte sznr-mapping!A)
	 * 
	 * @return die ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Textueller Name des Szenarios. (Excel Spalte sznr-mapping!B)
	 * 
	 * @return der Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * ID der zu verwendenden Marktwerte. Siehe {@link MW}. (Excel Spalte sznr-mapping!C)
	 * 
	 * @return die Id
	 */
	public int getMarktwerte() {
		return marktwerte;
	}

	/**
	 * ID der Projektion in VT Klassik. Siehe {@link VtKlassik}. (Excel Spalte sznr-mapping!D)
	 * 
	 * @return die Id
	 */
	public int getProjektionVtKlassik() {
		return projektionVtKlassik;
	}

	/**
	 * Id des Stresszenarios in VT FLV. Siehe {@link VtFlv}. (Excel Spalte sznr-mapping!E)
	 * 
	 * @return die Id
	 */
	public int getProjektionVtFlv() {
		return projektionVtFlv;
	}

	/**
	 * Szenariensatz, aus dem die Zinskurve bestimmt wird. (Excel Spalte sznr-mapping!F) Es gibt zur Zeit drei
	 * {@linkplain Szenario Szenarien}. Diese werden anhand des Dateinamens unterschieden, der die hier angegebene
	 * Nummer enth�lt.
	 * 
	 * @return die Id des Szenariensatzes
	 */
	public int getZinskurve() {
		return zinskurve;
	}

	/**
	 * Soll dieses Szenario aktiv sein? (Excel Spalte sznr-mapping!G)
	 * 
	 * @return das Szenario
	 */
	public boolean isAktiv() {
		return aktiv;
	}

}
