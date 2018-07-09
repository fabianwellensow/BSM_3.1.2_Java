package de.gdv.bsm.vu.berechnung;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import de.gdv.bsm.intern.applic.Pair;
import de.gdv.bsm.intern.math.Newton;
import de.gdv.bsm.intern.params.BwAktivaFi;
import de.gdv.bsm.intern.params.GenussNachrang;
import de.gdv.bsm.intern.params.HgbBilanzdaten;
import de.gdv.bsm.intern.params.LobMapping;
import de.gdv.bsm.intern.params.MW;
import de.gdv.bsm.intern.params.ReferenzZinssatz;
import de.gdv.bsm.intern.params.Save2csv;
import de.gdv.bsm.intern.params.SzenarioMappingZeile;
import de.gdv.bsm.intern.params.VUHistorie;
import de.gdv.bsm.intern.params.VtFlvZeile;
import de.gdv.bsm.intern.params.VtKlassikZeile;
import de.gdv.bsm.intern.params.VtOStress;
import de.gdv.bsm.intern.params.VuParameter;
import de.gdv.bsm.intern.params.ZeitabhManReg;
import de.gdv.bsm.intern.params.ZeitunabhManReg;
import de.gdv.bsm.intern.rechnung.CheckData;
import de.gdv.bsm.intern.rechnung.ResultNotFinite;
import de.gdv.bsm.intern.szenario.Szenario;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;
import de.gdv.bsm.vu.module.KaModellierung;

/**
 * Zahlen aus dem Blatt rgz, die sich in einem Schritt rechen lassen
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
 * <b>Simulationsmodell � GDV 2017</b>
 */
public class Berechnung {
	final int szenarioId;
	final String szenarioName;
	private int aktuellerPfad = 0;
	/** soll flv gerechnet werden? */
	public final boolean flvRechnen;
	/** sollen Zwischenergebnisse aus agg und rzg ausgegeben werden? */
	public final boolean ausgabe;
	final VuParameter vuParameter;
	final LobMapping lobMapping;
	final VtOStress vtOStress;
	final ReferenzZinssatz referenzZinssatz;
	final HgbBilanzdaten hgbBilanzdaten;
	final VUHistorie vuHistorie;
	final GenussNachrang genussNachrang;
	final BwAktivaFi bwAktivaFi;
	private final ZeitunabhManReg zeitunabhManReg;
	private final ZeitabhManReg zeitabhManReg;
	final Szenario szenario;

	/** Konstante UEB. */
	public static final String LOB_MAP_UEB = "UEB";
	/** Konstante Klassik. " */
	public static final String LOB_MAP_KAT_KLASSIK = "Klassik";
	/** Konstante FLV. */
	public static final String LOB_MAP_KAT_FLV = "FLV";

	// Ergebnisse der Berechnung dieses Pfades
	// Pfadweise Kennzahlen aggregiert
	private KennzahlenPfadweise kennzahlenPfadweise = null;
	// lobs in der auftretenden Reihenfolge
	private List<String> lobs = new ArrayList<>();
	// Kennzahlen aggregiert nur auf LoB Ebene
	private List<KennzahlenPfadweiseLoB> kennzahlenPfadweiseLoBs = null;
	// Mittelwerte mit CE aggregiert auf LoB Ebene
	// Lob -> Zeit -> Mittelwerte
	private Map<String, Map<Integer, MittelwerteUndCe>> mittelwerteUndCe = null;
	// Mittelwerte ohne CE aggregiert auf LoB Ebene
	// Lob -> Zeit -> Mittelwerte
	private Map<String, Map<Integer, MittelwerteNurCe>> mittelwerteNurCe = null;

	// Blatt VT Klassik MW
	// =================================================================
	// Zeitunabh�ngige Zeilen
	/**
	 * VT Klassik MW, Zeile 3, Spalten ab F. Achtung: Indiziert ab 0 statt 1!
	 */
	public final double[] leistGar;
	/**
	 * VT Klassik MW, Zeile 4, Spalten ab F. Achtung: Indiziert ab 0 statt 1!
	 */
	public final double[] restGar;
	/**
	 * VT Klassik MW, Zeile 5, Spalten ab F. Achtung: Indiziert ab 0 statt 1!
	 */
	public final double[] aufwendungenKa0;

	// flv-Zeilen mit der selben Zeit, f�r die Kummulierung in agg
	private final List<List<FlvZeile>> aggFlvZeilen;

	// das Blatt FI Ausfall
	// ================================================================
	// indiziert �ber die Zeit, allerdings ist f�r zeit = 0 nix drin
	private final List<FiAusfallZeile> fiAusfall;

	// Entspricht dem Blatt rzg
	// ============================================================
	// Maps Lob -> Zinsgeneration -> Zeilen(Zeit)
	private final Map<String, Map<Integer, Map<String, Map<String, List<RzgZeile>>>>> rzgMap = new HashMap<>();
	// alle Zeilen des rzg-Blattes in Original aufsteigender Reihenfolge
	private final List<RzgZeile> rzgZeilen;
	// alle Zeilen des rzg-Blattes in umkeherter Reihenfolge
	private final List<RzgZeile> rzgZeilenReversed;

	// Entspricht dem Blatt flv
	// ============================================================
	// Maps Lob -> zins -> altNeu -> Zeilen(Zeit)
	private final Map<String, Map<Integer, Map<String, List<FlvZeile>>>> flvMap = new HashMap<>();
	private final List<FlvZeile> flvZeilen = new ArrayList<>();

	/** Das Blatt agg indiziert �ber die Zeit. */
	private final List<AggZeile> aggList;

	// ========================================================================
	// Sondervariablen, die in Excel in FI Ausfall stehen

	/** SA-SP-Modifikationsfaktor f�r Buchwerte. FI Ausfall!$U$2. L 1. */
	double cfFiAkt = Double.NaN;
	/** Ausfallwahrscheinlichkeit. FI Ausfall!$U$4. L 1. */
	final double q;
	/** Duration. FI Ausfall!$U$9. L 1. */
	final double durationKaBestand;
	/** MW. FI Ausfall!$U$6. L 1. */
	double Barwert_KA_Bestand = Double.NaN;

	// ========================================================================
	// Sondervariablen, die in Excel in agg stehen:

	/** FLV Buchwert Kapitalanlagen EQ_RE_FI. agg $AA$5. */
	public final double eqReFi;
	/** EQ Buchwert, Klassik, zum Bewertungsstichtag. agg $AB$5. */
	public final double eqBuchwertKlassic;
	/** RE Buchwert, Klassik, zum Bewertungsstichtag. agg $AC$5. */
	public final double reBuchwertKlassic;
	/** FI Buchwert, aktueller Bestand. agg $AD$5. */
	public final double fiBuchwertBestand;
	/** Sonstige Aktiva - Sonstige Passiva Buchwert. agg $AE$5. */
	public final double bwSaSp;
	/** ARAP: Mieten. agg $AF$5 */
	public final double arapMieten;
	// Spalte AG steht wirklich in AggZeile
	/** FLV Marktwert Kapitalanlagen, EQ_RE_FI, zum Bewertungsstichtag. agg $AH$5. */
	public final double flvMarktWert;
	/** EQ Marktwert, Klassik, zum Bewertungsstichtag. agg $AI$5. */
	public final double mwEq0;
	/** RE Marktwert, Klassik, zum Bewertungsstichtag. agg $AJ$5. */
	public final double mwRe0;
	/** FI Marktwert, Klassik, zum Bewertungsstichtag. agg $AK$5. */
	public final double mwFi0;
	/** Sonstige Aktiva - Sonstige Passiva Marktwert. agg $AL$5. */
	public final double mwSaSp;

	/** Maximale Projektionsl�nge (Zeitpunkte). Feld agg!BD. */
	public final int zeitHorizont;
	/** Projektionl�nge (Versicherungstechnik). agg BE. */
	public final int laengeProjektionDr;

	/** Maximale rlzNeuNalage Restlaufzeit */
	public final int maxRlzNeuAnl;

	/**
	 * Erstelle eine Berechnung und berechne die pfadunabh�ngigen Daten.
	 * 
	 * @param szenarioId
	 *            des zu berechnenden Szenarios
	 * @param flvRechnen
	 *            soll FLV gerechnet werden?
	 * @param negAusfall
	 *            soll mit negativer Ausfallwahrscheinlichkeit weiter gerechnet werden?
	 * @param ausgabe
	 *            sollen Zwischenergebnisse aus agg und rzg ausgegeben werden
	 * @param vuParameter
	 *            Sammlung aller VU-Parameter
	 * @param szenario
	 *            die Daten des Zinsszenarios
	 */
	public Berechnung(final int szenarioId, final boolean flvRechnen, final boolean negAusfall, final boolean ausgabe,
			final VuParameter vuParameter, final Szenario szenario) {
		this.szenarioId = szenarioId;
		this.szenarioName = vuParameter.getSzenarioMapping().getSzenarionMapping(szenarioId).getName();
		this.flvRechnen = flvRechnen;
		this.ausgabe = ausgabe;

		this.vuParameter = vuParameter;
		this.lobMapping = vuParameter.getLobMapping();
		this.vtOStress = vuParameter.getVtOStress();
		this.referenzZinssatz = vuParameter.getReferenzZinssatz();
		this.hgbBilanzdaten = vuParameter.getHgbBilanzdaten();
		this.vuHistorie = vuParameter.getVuHistorie();
		this.genussNachrang = vuParameter.getGenussNachrang();
		this.bwAktivaFi = vuParameter.getBwAktivaFi();
		this.zeitunabhManReg = vuParameter.getZeitunabhManReg();
		this.zeitabhManReg = vuParameter.getZeitabhManReg();
		this.szenario = szenario;

		// Initialisierung diverser Werte:
		eqReFi = hgbBilanzdaten.getEqReFiBuchwert();
		eqBuchwertKlassic = hgbBilanzdaten.getEqBuchwert();
		reBuchwertKlassic = hgbBilanzdaten.getReBuchwert();
		fiBuchwertBestand = hgbBilanzdaten.getFiBuchwert();
		bwSaSp = hgbBilanzdaten.getSaFvvBuchwert() - hgbBilanzdaten.getSpBuchwert();
		arapMieten = hgbBilanzdaten.getRapMietenBuchwert();

		final SzenarioMappingZeile szenarioMappingZeile = vuParameter.getSzenarioMapping()
				.getSzenarionMapping(szenarioId);

		final int marktWerteId = szenarioMappingZeile.getMarktwerte();

		try {
			flvMarktWert = vuParameter.getMw().get(marktWerteId, MW.EQ_RE_FI).getMarktwert();
		} catch (NullPointerException ne) {
			throw new IllegalArgumentException("In Tabellenblatt MW: Daten f�r Stressszenario " + getSzenarioId()
					+ " konnten nicht gefunden werden.");
		}

		mwEq0 = vuParameter.getMw().get(marktWerteId, MW.EQ).getMarktwert();
		mwRe0 = vuParameter.getMw().get(marktWerteId, MW.RE).getMarktwert();
		mwFi0 = vuParameter.getMw().get(marktWerteId, MW.FI).getMarktwert();
		mwSaSp = vuParameter.getMw().get(marktWerteId, MW.SA_F_VV).getMarktwert()
				- vuParameter.getMw().get(marktWerteId, MW.SP).getMarktwert();

		// Map von LoB, Zinsgeneration und Zeit auf die Zeilen
		final List<VtKlassikZeile> lobMapKlassik = vuParameter.getVtKlassik()
				.getSzenarioListe(szenarioMappingZeile.getProjektionVtKlassik());

		rzgZeilen = new ArrayList<RzgZeile>();

		// rzg-Zeilen mit der selben Zeit, f�r die Kummulierung in agg
		List<List<RzgZeile>> aggZeilen = new ArrayList<List<RzgZeile>>();
		int maxZeit = 0;

		RzgZeile rzgVg = null;
		for (VtKlassikZeile zeile : lobMapKlassik) {
			final RzgZeile zn = new RzgZeile(this, zeile, zeitunabhManReg, rzgVg);
			if (rzgVg != null)
				rzgVg.setNf(zn);
			rzgVg = zn;
			rzgZeilen.add(zn);

			if (zn.zeit > maxZeit) {
				maxZeit = zn.zeit;
			}

			// Packe Zeilen mit der gleichen Zeit in die Liste der AGG
			while (aggZeilen.size() <= zn.zeit) {
				aggZeilen.add(new ArrayList<>());
			}
			aggZeilen.get(zn.zeit).add(zn);

			if (!rzgMap.containsKey(zn.lob)) {
				// neuen LoB gefunden
				lobs.add(zn.lob);
				rzgMap.put(zn.lob, new HashMap<>());
			}
			if (!rzgMap.get(zn.lob).containsKey(zn.zinsGeneration)) {
				rzgMap.get(zn.lob).put(zn.zinsGeneration, new HashMap<>());
			}
			if (!rzgMap.get(zn.lob).get(zn.getZinsGeneration()).containsKey(zn.altNeuBestand)) {
				rzgMap.get(zn.lob).get(zn.getZinsGeneration()).put(zn.altNeuBestand, new HashMap<>());
			}
			if (!rzgMap.get(zn.lob).get(zn.getZinsGeneration()).get(zn.altNeuBestand).containsKey(zn.deckungsStock)) {
				rzgMap.get(zn.lob).get(zn.getZinsGeneration()).get(zn.altNeuBestand).put(zn.deckungsStock,
						new ArrayList<>());
			}
			rzgMap.get(zn.lob).get(zn.zinsGeneration).get(zn.altNeuBestand).get(zn.deckungsStock).add(zn);
		}

		this.zeitHorizont = maxZeit;
		int maxRlzNeuAnlTemp = 0;
		for (int t = 1; t <= zeitHorizont; t++) {
			if (maxRlzNeuAnlTemp < this.zeitabhManReg.get(t).getRlzNeuAnl()) {
				maxRlzNeuAnlTemp = this.zeitabhManReg.get(t).getRlzNeuAnl();
			}
		}
		maxRlzNeuAnl = maxRlzNeuAnlTemp;

		aggFlvZeilen = new ArrayList<>(zeitHorizont + 1);

		if (flvRechnen) {
			// Falls gew�nscht, die FLV-Daten hinzuspielen.
			// diese Daten werden einfach als rzg-Zeilen an die obigen aus VT Klassik angeh�ngt

			// Weiter werden in diesem Fall auch die FlvZeilen erstellt

			// Map von LoB, Zinsgeneration und Zeit auf die Zeilen
			FlvZeile flvVg = null;

			for (int i = 0; i <= zeitHorizont; ++i) {
				aggFlvZeilen.add(new ArrayList<>());
			}

			final int projektionVtFlv = vuParameter.getSzenarioMapping().getSzenarionMapping(getSzenarioId())
					.getProjektionVtFlv();
			final List<VtFlvZeile> lobFlvZeilen = vuParameter.getVtFlv().getSzenarioZeilen(projektionVtFlv);
			if (lobFlvZeilen == null) {
				throw new IllegalArgumentException(
						"VT FLV: Daten f�r Stressszenario " + getSzenarioId() + " konnten nicht gefunden werden.");
			}

			for (String deckungsStock : new String[] { RzgZeile.DECKUNGS_STOCK_KDS, RzgZeile.DECKUNGS_STOCK_FONDS }) {
				for (VtFlvZeile zeile : lobFlvZeilen) {
					// zuerst erstellen wir die rzg-Zeilen
					final RzgZeile zn = new RzgZeile(this, deckungsStock, zeile, zeitunabhManReg, rzgVg);
					if (rzgVg != null)
						rzgVg.setNf(zn);
					rzgVg = zn;
					rzgZeilen.add(zn);

					// Packe Zeilen mit der gleichen Zeit in die Liste der AGG
					aggZeilen.get(zn.zeit).add(zn);

					if (deckungsStock.equals(RzgZeile.DECKUNGS_STOCK_KDS)) {
						// nun folgen die speziellen flv-Zeilen:
						final FlvZeile flvZeile = new FlvZeile(this, zeile, flvVg);
						flvVg = flvZeile;
						flvZeilen.add(flvZeile);

						// f�r die Sonderrechnung der flv werden die Zeilen mit
						// gleicher Zeit zusammengefasst:
						aggFlvZeilen.get(flvZeile.zeit).add(flvZeile);

						// Maps Szenario-Id -> Lob -> altNeu -> Zeilen(Zeit)
						if (!flvMap.containsKey(flvZeile.lob)) {
							flvMap.put(flvZeile.lob, new HashMap<>());
						}
						Map<Integer, Map<String, List<FlvZeile>>> flvMapZins = flvMap.get(flvZeile.lob);
						if (!flvMapZins.containsKey(flvZeile.zinsGeneration)) {
							flvMapZins.put(flvZeile.zinsGeneration, new HashMap<>());
						}
						Map<String, List<FlvZeile>> flvMapAltNeu = flvMapZins.get(flvZeile.zinsGeneration);
						if (!flvMapAltNeu.containsKey(flvZeile.altNeu)) {
							flvMapAltNeu.put(flvZeile.altNeu, new ArrayList<>());
						}
						flvMapAltNeu.get(flvZeile.altNeu).add(flvZeile);
					}

					if (!rzgMap.containsKey(zn.lob)) {
						// neuen LoB gefunden
						lobs.add(zn.lob);
						rzgMap.put(zn.lob, new HashMap<>());
					}
					if (!rzgMap.get(zn.lob).containsKey(zn.zinsGeneration)) {
						rzgMap.get(zn.lob).put(zn.zinsGeneration, new HashMap<>());
					}
					if (!rzgMap.get(zn.lob).get(zn.getZinsGeneration()).containsKey(zn.altNeuBestand)) {
						rzgMap.get(zn.lob).get(zn.getZinsGeneration()).put(zn.altNeuBestand, new HashMap<>());
					}
					if (!rzgMap.get(zn.lob).get(zn.getZinsGeneration()).get(zn.altNeuBestand)
							.containsKey(zn.deckungsStock)) {
						rzgMap.get(zn.lob).get(zn.getZinsGeneration()).get(zn.altNeuBestand).put(zn.deckungsStock,
								new ArrayList<>());
					}
					rzgMap.get(zn.lob).get(zn.zinsGeneration).get(zn.altNeuBestand).get(zn.deckungsStock).add(zn);
				}
			}
		}

		rzgZeilenReversed = new ArrayList<RzgZeile>(rzgZeilen);
		Collections.reverse(rzgZeilenReversed);

		{
			aggList = new ArrayList<AggZeile>(zeitHorizont + 1);
			AggZeile vg = null;
			for (int i = 0; i <= zeitHorizont; ++i) {
				final List<FlvZeile> flvZeilen = aggFlvZeilen != null && i < aggFlvZeilen.size() ? aggFlvZeilen.get(i)
						: null;
				final AggZeile z = new AggZeile(this, aggZeilen.get(i), flvZeilen, vg);
				aggList.add(z);
				if (vg != null)
					vg.setNachfolger(z);
				vg = z;
			}
		}

		// dies ist in Excel berechnet mit Laenge_Projektion_DR(C5;"KDS";BD5;O5;J5;K5)
		laengeProjektionDr = (int) aggList.stream()
				.filter(agg -> !(agg.hgbDrAgg == 0.0 && (agg.rueAgg + agg.kueAgg) == 0.0)).count() - 1;

		cfFiAkt = KaModellierung.bwFiAkt(0, 0.0, 0.0, 0.0, fiBuchwertBestand, eqBuchwertKlassic, reBuchwertKlassic,
				aggList.get(1).bwEq, aggList.get(1).bwRe, bwSaSp, 0.0, 0.0) / fiBuchwertBestand;

		// tempor�re Bestimmung der Konstanten f�r den Newton:
		final double[] k = new double[zeitHorizont + 1];
		{
			// jetzt k�nnen erste Teile des Blattes FI_Ausfall berechnet werden
			fiAusfall = new ArrayList<>(zeitHorizont + 1);
			// null f�r t=0:
			fiAusfall.add(null);
			FiAusfallZeile vg = null;
			for (int i = 1; i <= zeitHorizont; ++i) {
				final FiAusfallZeile fz = new FiAusfallZeile(this, vg, i, bwAktivaFi.get(i));
				fiAusfall.add(fz);
				k[i] = fz.df * fz.cfFioAusfall;
				vg = fz;
			}
		}

		{
			final double faelligZins = getZeitunabhManReg().getFaelligkeitZinstitel();

			// Funktion, zu der die Nullstelle gesucht wird
			// Achtung: Zielwert getAggZeile(1).mwFianfangJ muss in AggZeile im Konstruktor berechnet werden
			final Function<Double, Double> f = q -> {
				double r = -getAggZeile(1).mwFianfangJ;
				for (int i = 1; i <= zeitHorizont; ++i) {
					r += k[i] * Math.pow(1 - q, i - 1 + faelligZins / 12);
				}
				return r;
			};

			// Ableitung der Funktion:
			final Function<Double, Double> df = q -> {
				double r = 0.0;
				for (int i = 1; i <= zeitHorizont; ++i) {
					r -= k[i] * (i - 1 + faelligZins / 12) * Math.pow(1 - q, i - 2 + faelligZins / 12);
				}
				return r;
			};

			// Ermitteln der Nullstelle
			q = Newton.solve(f, df, 0.01, 0.001, 20, 0.1, 2.0, false);
			if (!Double.isFinite(q)) {
				throw new IllegalStateException("Ausfallwahrscheinlichkeit konnte nicht ermittelt werden!");
			} else if (q < 0.0 && !negAusfall) {
				throw new IllegalStateException("Berechnete Ausfallwahrscheinlichkeit ist negativ!");
			}
		}

		{
			// jetzt folgen die weiteren Zahlen aus FI_Ausfall
			final List<FiAusfallZeile> reversed = new ArrayList<>(fiAusfall);
			Collections.reverse(reversed);
			FiAusfallZeile nf = null;
			for (FiAusfallZeile z : reversed) {
				if (z != null) {
					z.berechnungLevel01(this, nf);
					final List<String> errors = CheckData.checkFinite(z);
					if (errors.size() > 0) {
						final String header = "In Szenario " + szenarioId + " (" + szenarioName + "), pfad = "
								+ getAktuellerPfad() + ", zeit = " + z.zeit + " traten �berl�ufe auf in den Feldern";
						final String felder = errors.stream().reduce("", (x, y) -> (x.isEmpty() ? x : x + ", ") + y);
						throw new ResultNotFinite(header + ": " + felder, header, felder);
					}
				}
				nf = z;
			}

			// nun k�nnen wir auch die Duration bestimmen:
			double mw = 0.0;
			double mwT = 0.0;
			double vuZeitpunkt = zeitunabhManReg.getFaelligkeitZinstitel();
			for (FiAusfallZeile faz : fiAusfall) {
				if (faz != null) {
					mw += faz.df * faz.cfFimitAusfall;
					mwT += faz.df * faz.cfFimitAusfall * (faz.zeit - 1 + vuZeitpunkt / 12.0);
				}
			}
			durationKaBestand = mwT / mw;
		}

		// einige Zahlen aus rzg ben�tigen eine chronologische Umkehr:
		{
			for (RzgZeile z : rzgZeilenReversed) {
				z.berechnungReversed00();
			}
		}

		leistGar = new double[szenario.projektionsHorizont];
		restGar = new double[szenario.projektionsHorizont];
		aufwendungenKa0 = new double[szenario.projektionsHorizont];

		final double yKaPassiv = zeitunabhManReg.getFaktorKapitalanlagen()
				* (eqBuchwertKlassic + reBuchwertKlassic + fiBuchwertBestand + bwSaSp)
				/ (aggList.get(0).hgbDrAgg + aggList.get(0).eigenkapitalFortschreibung);
		for (int i = 0; i < szenario.projektionsHorizont; ++i) {
			if (i < zeitHorizont) {
				final AggZeile az = aggList.get(i + 1);

				leistGar[i] = az.lTodAgg + az.kaAgg + az.lSonstErlAgg + az.rkAgg;
				restGar[i] = az.kAgg - az.bAgg + az.rueAgg + az.kueAgg + az.cfEvuRvu;
				aufwendungenKa0[i] = yKaPassiv * az.hgbDrAgg * (1.0 + zeitabhManReg.get(i + 1).getEkZiel());
			} else {
				leistGar[i] = 0.0;
				restGar[i] = 0.0;
				aufwendungenKa0[i] = 0.0;
			}
		}
	}

	/**
	 * Ausf�hren der pfadabh�ngigen Berechnung. Diese Funktion kann mehrfach f�r verschiedene Pfade aufgerufen werden.
	 * Die Ergebnisse werden jeweils �berschrieben.
	 * 
	 * @param pfad
	 *            der gew�nschte Pfad
	 * @throws FileNotFoundException
	 *             falls die Ausgabe nicht geschrieben werden kann
	 * @throws IllegalAccessException
	 *             falls Zugriffe auf Felder scheitern
	 * @throws IllegalArgumentException
	 *             falls Zugriffe auf Felder scheitern
	 */
	public void berechnung(final int pfad)
			throws FileNotFoundException, IllegalArgumentException, IllegalAccessException {
		berechnung(pfad, Optional.empty());
	}

	/**
	 * Ausf�hren der pfadabh�ngigen Berechnung. Diese Funktion kann mehrfach f�r verschiedene Pfade aufgerufen werden.
	 * Die Ergebnisse werden jeweils �berschrieben.
	 * 
	 * @param pfad
	 *            der gew�nschte Pfad
	 * @param transferDir
	 *            Transfer-Dir, wenn abweichend von den VuParametern
	 * @throws FileNotFoundException
	 *             falls die Ausgabe nicht geschrieben werden kann
	 * @throws IllegalAccessException
	 *             falls Zugriffe auf Felder scheitern
	 * @throws IllegalArgumentException
	 *             falls Zugriffe auf Felder scheitern
	 */
	public void berechnung(final int pfad, final Optional<File> transferDir)
			throws FileNotFoundException, IllegalArgumentException, IllegalAccessException {

		aktuellerPfad = pfad;
		for (AggZeile aggZeile : aggList) {
			aggZeile.berechnungLevel01(pfad);
		}

		// nun m�ssen wir rekursiv die Zeitschritte rechnen:
		for (AggZeile aggZeile : aggList) {
			if (flvRechnen) {
				for (FlvZeile flvZeile : aggFlvZeilen.get(aggZeile.zeit)) {
					// Bei der Berechnung FLV flie�t nur der Vorg�nger ein:
					flvZeile.rechnen(aggZeile.vg);
					if (flvZeile.zeit > 0) {
						final List<String> errors = CheckData.checkFinite(flvZeile);
						if (errors.size() > 0) {
							final String header = "In Szenario " + szenarioId + " (" + szenarioName + "), pfad = "
									+ getAktuellerPfad() + ", " + flvZeile.keyText()
									+ " traten �berl�ufe auf in den Feldern";
							final String felder = errors.stream().reduce("",
									(x, y) -> (x.isEmpty() ? x : x + ", ") + y);
							throw new ResultNotFinite(header + ": " + felder, header, felder);
						}
					}
				}
			}

			aggZeile.zeitRekursionL01(pfad);
			aggZeile.zeitRekursionL02(pfad);
			aggZeile.zeitRekursionL03();

			// jetzt ist die aktuelle Zeile gerechnet, wir pr�fen nun die Daten
			// (bis auf die Ausnahme surplugFond)
			aggZeile.checkFinite();
		}
		for (RzgZeile z : rzgZeilen) {
			z.surplusFondRueckwaerts(getAggZeile(z.zeit));
		}

		kennzahlenPfadweise = new KennzahlenPfadweise(szenarioId, pfad, aggList, zeitunabhManReg.getMonatZahlung());

		kennzahlenPfadweiseLoBs = new ArrayList<>();
		mittelwerteUndCe = new HashMap<>();
		mittelwerteNurCe = new HashMap<>();
		for (String lob : lobs) {
			final Map<Integer, Map<String, Map<String, List<RzgZeile>>>> rzgMapLob = rzgMap.get(lob);
			final KennzahlenPfadweiseLoB kpl = new KennzahlenPfadweiseLoB(szenarioId, pfad, lob, rzgMapLob, aggList,
					zeitunabhManReg.getMonatZahlung());
			kennzahlenPfadweiseLoBs.add(kpl);

			final HashMap<Integer, MittelwerteUndCe> mwUndCeLocal = new HashMap<>();
			mittelwerteUndCe.put(lob, mwUndCeLocal);

			final HashMap<Integer, MittelwerteNurCe> mwNurCeLocal = new HashMap<>();
			mittelwerteNurCe.put(lob, mwNurCeLocal);
			final Map<Integer, List<RzgZeile>> zeitMap = new HashMap<>();
			for (int zins : rzgMapLob.keySet()) {
				for (String altNeu : rzgMapLob.get(zins).keySet()) {
					for (String deckungsStock : rzgMapLob.get(zins).get(altNeu).keySet()) {
						for (RzgZeile rzg : rzgMapLob.get(zins).get(altNeu).get(deckungsStock)) {
							if (!zeitMap.containsKey(rzg.zeit)) {
								zeitMap.put(rzg.zeit, new ArrayList<>());
							}
							zeitMap.get(rzg.zeit).add(rzg);
						}
					}
				}
			}
			for (int zeit : zeitMap.keySet()) {
				final MittelwerteUndCe mittelwerteUndCe = new MittelwerteUndCe(pfad, szenarioName, szenarioId, lob,
						zeit, zeitMap.get(zeit), getAggZeile(zeit), zeitunabhManReg.getMonatZahlung());
				mwUndCeLocal.put(zeit, mittelwerteUndCe);

				final MittelwerteNurCe mittelwerteNurCe = new MittelwerteNurCe(pfad, szenarioName, szenarioId, lob,
						zeit, zeitMap.get(zeit), getAggZeile(zeit), zeitunabhManReg.getMonatZahlung());
				mwNurCeLocal.put(zeit, mittelwerteNurCe);
			}
		}

		if (ausgabe) {
			// Ausgabe von Spalten aus agg und rzg in csv-Dateien:
			final Save2csv save2csv = vuParameter.getSave2csv();

			final Map<Integer, Map<Integer, List<Pair<String, Pair<String, String>>>>> fields = save2csv.getFields();
			if (fields.containsKey(szenarioId) && fields.get(szenarioId).containsKey(pfad)) {

				final List<Pair<String, Pair<String, String>>> list = fields.get(szenarioId).get(pfad);

				final String fileName = VuParameter.AUSGABE_SAVE2CSV.replace("%s", String.valueOf(szenarioId))
						.replace("%p", String.valueOf(pfad));
				// Transferverzeichnis ist entweder vorgegeben oder aus den VuParametern
				final File transferDirUsed;
				if (transferDir.isPresent()) {
					transferDirUsed = transferDir.get();
				} else {
					transferDirUsed = vuParameter.getTransferDir();
				}
				final File ausgabe = new File(transferDirUsed, fileName);
				try (final PrintStream out = new PrintStream(
						new BufferedOutputStream(new FileOutputStream(ausgabe), 8 * 1024))) {

					// Titel der Felder rausschreiben:
					boolean first = true;
					for (Pair<String, Pair<String, String>> p : list) {
						first = separator(out, first);
						out.print(p.a);
					}
					out.println();

					// Daten rausschreiben:
					for (int i = 0; i < rzgZeilen.size(); ++i) {
						first = true;
						for (Pair<String, Pair<String, String>> p : list) {
							Pair<String, String> f = p.b;
							first = separator(out, first);
							if (f.a.equals("agg")) {
								if (i < aggList.size()) {
									writeField(out, aggList.get(i), save2csv.getAggField(f.b));
								}
							} else if (f.a.equals("rzg")) {
								writeField(out, rzgZeilen.get(i), save2csv.getRzgField(f.b));
							}
						}
						out.println();
					}
				}
			}
		}
	}

	private boolean separator(final PrintStream out, final boolean first) {
		if (!first) {
			out.print(";");
		}
		return false;
	}

	private void writeField(final PrintStream out, final Object zeile, final Field field)
			throws IllegalAccessException {
		// jetzt wert rausschreiben:
		final boolean accessible = field.isAccessible();
		field.setAccessible(true);
		if (field.getType() == char.class) {
			out.print(field.getChar(zeile));
		} else if (field.getType() == int.class) {
			out.print(field.getInt(zeile));
		} else if (field.getType() == double.class) {
			final double w = field.getDouble(zeile);
			if (Double.isFinite(w) && !Double.isNaN(w)) {
				out.format("%.2f", w);
			}
		} else {
			out.print(field.get(zeile));
		}
		field.setAccessible(accessible);
	}

	/**
	 * Summiert Werte zum gew�nschten �b/N�b und zu gegebener Zeit. Falls FLV-Berechnung nicht gew�nscht ist, ergibt
	 * diese Funktion automatisch 0.
	 * 
	 * @param uebNueb
	 *            der gew�nscht ist
	 * @param zeit
	 *            die gew�nschte Zeit
	 * @param f
	 *            ergibt den zu summierenden Wert
	 * @return die Summe
	 */
	public double summeFlvZuUeb(final String uebNueb, final int zeit, final Function<FlvZeile, Double> f) {
		double summe = 0.0;
		for (String lob : flvMap.keySet()) {
			for (int zins : flvMap.get(lob).keySet()) {
				for (String altNeu : flvMap.get(lob).get(zins).keySet()) {
					final FlvZeile z = flvMap.get(lob).get(zins).get(altNeu).get(zeit);
					if (z.uebNueb.equals(uebNueb)) {
						summe += f.apply(z);
					}
				}
			}
		}
		return summe;
	}

	/**
	 * Ermittle eine Zeile aus den rzg.
	 * 
	 * @param lob
	 *            Line of Business
	 * @param zins
	 *            Zinsgeneration
	 * @param altNeu
	 *            Alt/Neubestand
	 * @param deckungsStock
	 *            KDS oder Fonds
	 * @param zeit
	 *            Zeitschritt
	 * @return die Zeile
	 */
	public RzgZeile getRzgZeile(final String lob, final int zins, final String altNeu, final String deckungsStock,
			final int zeit) {
		return rzgMap.get(lob).get(zins).get(altNeu).get(deckungsStock).get(zeit);
	}

	/**
	 * Ermittle eine Zeile aus den rzg.
	 * 
	 * @param lob
	 *            Line of Business
	 * @param zins
	 *            Zinsgeneration
	 * @param altNeu
	 *            Alt/Neubestand
	 * @param deckungsStock
	 *            KDS oder Fonds
	 * @return die Zeile
	 */
	public List<RzgZeile> getRzgZeilen(final String lob, final int zins, final String altNeu,
			final String deckungsStock) {
		return rzgMap.get(lob).get(zins).get(altNeu).get(deckungsStock);
	}

	/**
	 * Ermittle alle rzg-Zeilen
	 * 
	 * @return eine Liste mit allen Zeilen
	 */
	public List<RzgZeile> getRzgZeilen() {
		return Collections.unmodifiableList(rzgZeilen);
	}

	/**
	 * Eine agg-Zeile.
	 * 
	 * @param zeit
	 *            Zeitschritt der gew�nschten Zeile
	 * @return die Zeile
	 */
	public AggZeile getAggZeile(final int zeit) {
		return aggList.get(zeit);
	}

	/**
	 * Ermittle alle agg-Zeilen.
	 * 
	 * @return Liste mit allen Zeilen
	 */
	public List<AggZeile> getAggZeilen() {
		return Collections.unmodifiableList(aggList);
	}

	/**
	 * Ermittle eine Zeile aus FLV.
	 * 
	 * @param lob
	 *            Line of Business
	 * @param zins
	 *            Zinsgeneration
	 * @param altNeu
	 *            Alt/Neubestand
	 * @param zeit
	 *            Zeitschritt
	 * @return die Zeile, oder null, wenn nicht vorhanden
	 */
	public FlvZeile getFlvZeile(final String lob, final int zins, final String altNeu, final int zeit) {
		try {
			return flvMap.get(lob).get(zins).get(altNeu).get(zeit);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Liste mit allen FLV-Zeilen.
	 * 
	 * @return die Liste
	 */
	public List<FlvZeile> getFlvZeilen() {
		return Collections.unmodifiableList(flvZeilen);
	}

	/**
	 * Ermittle eine FI-Ausfall Zeile
	 * 
	 * @param zeit
	 *            Zeitschritt der gew�nschten Zeile
	 * @return die Zeile
	 */
	public FiAusfallZeile getFiAusfall(final int zeit) {
		return fiAusfall.get(zeit);
	}

	/**
	 * Liste mit allen Zeilen des Blattes FI-Ausfall. Achtung: die Zeile 0 ist null und wird nicht mit �bergeben.
	 * 
	 * @return die Liste
	 */
	public List<FiAusfallZeile> getFiAusfallZeilen() {
		final List<FiAusfallZeile> l = new ArrayList<>();
		for (int i = 1; i < fiAusfall.size(); ++i)
			l.add(fiAusfall.get(i));
		return Collections.unmodifiableList(l);
	}

	/**
	 * Die Daten des Blattes der zeitunabh�ngigen Management-Regeln.
	 * 
	 * @return die Regeln
	 */
	public ZeitunabhManReg getZeitunabhManReg() {
		return zeitunabhManReg;
	}

	/**
	 * Die Daten des Blattes der zeitabh�ngigen Management-Regeln.
	 * 
	 * @return die Paramter
	 */
	public ZeitabhManReg getZeitabhManReg() {
		return zeitabhManReg;
	}

	/**
	 * ID des zu berechnenden Szenarios.
	 * 
	 * @return die ID
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * Name des zugrunde liegenden Szanarios.
	 * 
	 * @return der Name
	 */
	public String getSzenarioName() {
		return szenarioName;
	}

	/**
	 * Zusammenfassung aller VU-Parameter.
	 * 
	 * @return die Paramter
	 */
	public VuParameter getVuParameter() {
		return vuParameter;
	}

	/**
	 * Der aktuell zu rechnende Pfad.
	 * 
	 * @return der Pfad
	 */
	public int getAktuellerPfad() {
		return aktuellerPfad;
	}

	/**
	 * Die Ergebnisliste der pfadweisen Kennzahlen.
	 * 
	 * @return die Kennzahlen
	 */
	public KennzahlenPfadweise getKennzahlenPfadweise() {
		return kennzahlenPfadweise;
	}

	/**
	 * Die Ergebnisse der pfadweisen Berechnung pro LoB.
	 * 
	 * @return die Ergebnisse
	 */
	public List<KennzahlenPfadweiseLoB> getKennzahlenPfadweiseLoB() {
		return Collections.unmodifiableList(kennzahlenPfadweiseLoBs);
	}

	/**
	 * Die Ergebnisse der Berechnung der Mittelwerte und CE.
	 * 
	 * @return die Ergebnisse
	 */
	public Map<String, Map<Integer, MittelwerteUndCe>> getMittelwerteUndCe() {
		return mittelwerteUndCe;
	}

	/**
	 * Die Ergebnisse der Berechnung der Mittelwerte (nur CE).
	 * 
	 * @return die Mittelwerte
	 */
	public Map<String, Map<Integer, MittelwerteNurCe>> getMittelwerteNurCe() {
		return mittelwerteNurCe;
	}

	/**
	 * Ist der angegebene Pfad im Szenario enthalten?
	 * 
	 * @param pfad
	 *            der Pfad
	 * @return ja oder nein
	 */
	public boolean isPfadKorrekt(final int pfad) {
		return szenario.getPfad(pfad) != null;
	}

	/**
	 * Die Ausfallwahrscheinlichkeit. In Excel FI Ausfall!$U$4.
	 * 
	 * @return der Wert
	 */
	public double getAusfallWahrscheinlichkeitQ() {
		return q;
	}

	/**
	 * Duration. in Excel FI Ausfall!$U$9.
	 * 
	 * @return der Wert
	 */
	public double getDurationKaBestand() {
		return durationKaBestand;
	}
}
