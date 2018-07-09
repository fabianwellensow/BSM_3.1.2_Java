package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Zusammenfassung aller VU-Parameter.
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
public class VuParameter {
	/** Verzeichnis, in dem die csv-Exporte aus Excel lagern und die Ausgaben geschrieben werden. */
	private final File transferDir;

	/** Dateiname der Exportierten Eingabedaten. */
	public static final String EINGABE_FILENAME = "Eingabe.csv";
	static final String SZENARIO_MAPPING_FILENAME = "sznr-mapping.csv";
	static final String LOB_MAPPING_FILENAME = "LoB-mapping.csv";
	static final String REFERENZ_ZINSSATZ_FILENAME = "Referenzzinssatz.csv";
	static final String HGB_BILANZDATEN_FILENAME = "HGB Bilanzdaten.csv";
	static final String VU_HISTORIE_FILENAME = "VU Historie.csv";
	static final String VT_KLASSIK_FILENAME = "VT Klassik.csv";
	static final String VT_O_STRESS_FILENAME = "VT o.Stress.csv";
	static final String VT_FLV_FILENAME = "VT FLV.csv";
	static final String MW_FILENAME = "MW.csv";
	static final String GENUSS_NACHRANG_FILENAME = "Genussr+Nachrang.csv";
	static final String BW_AKTIVA_FI_FILENAME = "BW Aktiva FI.csv";
	static final String ZEITUNABH_MAN_REG_FILENAME = "zeitunabh.ManReg.csv";
	static final String ZEITABH_MAN_REG_FILENAME = "zeitabh.ManReg.csv";
	static final String SAVE_2_CSV = "save2CSV.csv";

	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String RZG = "rzg.csv";
	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String AGG = "agg.csv";

	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String SCHAETZER_MITTELWERTE = "Schaetzer Mittelwerte.csv";
	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String KENNZAHLEN_PFADWEISE = "Kennzahlen pfadweise.csv";
	/** Dateiname f�r die Ausgabe der Stochastischen Kennzahlen. */
	public static final String STOCHASTISCHE_KENNZAHLEN = "Stoch. Kennzahlen.csv";
	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String SCHAETZER_MITTELWERTE_LOB = "Schaetzer Mittelwerte LoB.csv";
	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String KENNZAHLEN_PFADWEISE_LOB = "Kennzahlen pfadweise LoB.csv";
	/** Dateiname f�r die Ausgabe der Kennzahlen pfadweise. */
	public static final String KENNZAHLEN_MITTELWERTE = "Mittelwerte zeitschrittig.csv";
	/** Dateiname f�r die Ausgabe der Stochastischen Kennzahlen pro LoB. */
	public static final String STOCHASTISCHE_KENNZAHLEN_LOB = "Stoch. Kennzahlen LoB.csv";
	/** Dateiname f�r das Blatt Ausgabe. */
	public static final String AUSGABE = "Ausgabe.csv";
	/** Dateinames-Muster f�r die Ausgaben aus agg und rzg. */
	public static final String AUSGABE_SAVE2CSV = "Ausgabe_%s_%p.csv";
	/** Dateiname f�r das Ausf�hrungsprotokoll. */
	public static final String AUSFUEHRUNGS_LOG = "AusfuehrungsLog.csv";

	private final SzenarioMapping szenarioMapping;
	private final LobMapping lobMapping;
	private final ReferenzZinssatz referenzZinssatz;
	private final HgbBilanzdaten hgbBilanzdaten;
	private final VUHistorie vuHistorie;
	private final VtKlassik vtKlassik;
	private final VtOStress vtOStress;
	private final VtFlv vtFlv;
	private final MW mw;
	private final GenussNachrang genussNachrang;
	private final BwAktivaFi bwAktivaFi;
	private final ZeitunabhManReg zeitunabhManReg;
	private final ZeitabhManReg zeitabhManReg;
	private final Save2csv save2csv;
	private final List<String> lobs;

	/**
	 * Erstelle die Zusammenstellung.
	 * 
	 * @param transferVerzeichnis
	 *            das die exportierten csv-Dateien enth�lt
	 * @throws IOException
	 *             bei Ein/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Strukturfehlern in der csv-Datei
	 */
	public VuParameter(final File transferVerzeichnis) throws IOException, LineFormatException {
		this.transferDir = transferVerzeichnis;
		szenarioMapping = new SzenarioMapping(new File(transferVerzeichnis, SZENARIO_MAPPING_FILENAME));
		lobMapping = new LobMapping(new File(transferVerzeichnis, LOB_MAPPING_FILENAME));
		referenzZinssatz = new ReferenzZinssatz(new File(transferVerzeichnis, REFERENZ_ZINSSATZ_FILENAME));
		hgbBilanzdaten = new HgbBilanzdaten(new File(transferVerzeichnis, HGB_BILANZDATEN_FILENAME));
		vuHistorie = new VUHistorie(new File(transferVerzeichnis, VU_HISTORIE_FILENAME));
		vtKlassik = new VtKlassik(new File(transferVerzeichnis, VT_KLASSIK_FILENAME));
		vtOStress = new VtOStress(new File(transferVerzeichnis, VT_O_STRESS_FILENAME));
		vtFlv = new VtFlv(new File(transferVerzeichnis, VT_FLV_FILENAME));
		mw = new MW(new File(transferVerzeichnis, MW_FILENAME));
		genussNachrang = new GenussNachrang(new File(transferVerzeichnis, GENUSS_NACHRANG_FILENAME));
		bwAktivaFi = new BwAktivaFi(new File(transferVerzeichnis, BW_AKTIVA_FI_FILENAME));
		zeitunabhManReg = new ZeitunabhManReg(new File(transferVerzeichnis, ZEITUNABH_MAN_REG_FILENAME));
		zeitabhManReg = new ZeitabhManReg(new File(transferVerzeichnis, ZEITABH_MAN_REG_FILENAME));
		save2csv = new Save2csv(new File(transferVerzeichnis, SAVE_2_CSV));

		final Set<String> lobSet = new HashSet<>();
		final List<String> lobs = new ArrayList<>();
		for (String lob : vtKlassik.getLobs()) {
			lobSet.add(lob);
			lobs.add(lob);
		}
		for (String lob : vtFlv.getLobs()) {
			if (!lobSet.contains(lob)) {
				lobSet.add(lob);
				lobs.add(lob);
			}
		}
		this.lobs = Collections.unmodifiableList(lobs);
	}

	/**
	 * Mapping der Daten f�r die einzelnen Szenarien. Abbild des Blattes <code>sznr-mapping</code>.
	 * 
	 * @return das SzenarioMapping
	 */
	public SzenarioMapping getSzenarioMapping() {
		return szenarioMapping;
	}

	/**
	 * Mapping der LOB auf Kategorie und �B/N�B.
	 * 
	 * @return das Mapping
	 */
	public LobMapping getLobMapping() {
		return lobMapping;
	}

	/**
	 * Tabelle der Referenzzinss�tze. Abbild des Blattes <code>Referenzzinssatz</code>.
	 * 
	 * @return die Tabelle
	 */
	public ReferenzZinssatz getReferenzZinssatz() {
		return referenzZinssatz;
	}

	/**
	 * HGB Bilanzdaten des Unternehmens. Abbild des Blattes <code>HGB Bilanzdaten</code>.
	 * 
	 * @return die Tabelle
	 */
	public HgbBilanzdaten getHgbBilanzdaten() {
		return hgbBilanzdaten;
	}

	/**
	 * VU-Historie f�r chronologisch zur�ckliegende Zeitschritte. Abbild des Blattes <code>VU Historie</code>.
	 * 
	 * @return die Historie
	 */
	public VUHistorie getVuHistorie() {
		return vuHistorie;
	}

	/**
	 * VT Klassik Daten des Unternehmens. Abbild des Blattes <code>VT Klassik</code>.
	 * 
	 * @return die Gesamtdaten
	 */
	public VtKlassik getVtKlassik() {
		return vtKlassik;
	}

	/**
	 * VT ohne Stress Daten des Unternehmens. Abbild des Blattes <code>VT o.Stress</code>.
	 * 
	 * @return die Daten
	 */
	public VtOStress getVtOStress() {
		return vtOStress;
	}

	/**
	 * VT Flv Daten des Unternehmens. Abbild des Blattes <code>VT FLV</code>.
	 * 
	 * @return die Daten
	 */
	public VtFlv getVtFlv() {
		return vtFlv;
	}

	/**
	 * Marktwerte.
	 * 
	 * @return die Werte
	 */
	public MW getMw() {
		return mw;
	}

	/**
	 * Tabelle Genuss und Nachrang. Abbild des Blattes <code>Genuss+Nachrang</code>.
	 * 
	 * @return die Tabelle
	 */
	public GenussNachrang getGenussNachrang() {
		return genussNachrang;
	}

	/**
	 * Tabelle BW Aktiva FI. Abbild des Blattes <code>BW Aktiva FI</code>.
	 * 
	 * @return die Tabelle
	 */
	public BwAktivaFi getBwAktivaFi() {
		return bwAktivaFi;
	}

	/**
	 * Zeitunabh�ngige Vorgaben des Unternehmens. Abbild des Blattes <code>zeitunabh.ManReg</code>.
	 * 
	 * @return die Daten
	 */
	public ZeitunabhManReg getZeitunabhManReg() {
		return zeitunabhManReg;
	}

	/**
	 * Zeitabh�ngige Managemen Daten des Unternehmens. Abbild des Blattes <code>zeitabh.ManReg</code>.
	 * 
	 * @return die Managementdaten
	 */
	public ZeitabhManReg getZeitabhManReg() {
		return zeitabhManReg;
	}

	/**
	 * Lobs in Reihenfolge ihres Auftretens.
	 * 
	 * @return die Lobs
	 */
	public List<String> getLobs() {
		return lobs;
	}

	/**
	 * Unterverzeichnis, in dem die csv-Dateien liegen und die Ausgaben geschrieben werden.
	 * 
	 * @return das Verzeichnis
	 */
	public File getTransferDir() {
		return transferDir;
	}

	/**
	 * Paramter zur Ausgabe von Zwischenergebnissen aus agg und rzg.
	 * 
	 * @return die Parameter
	 */
	public Save2csv getSave2csv() {
		return save2csv;
	}

}
