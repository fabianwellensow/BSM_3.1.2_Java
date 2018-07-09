package de.gdv.bsm.vu.berechnung;

import static de.gdv.bsm.vu.berechnung.AggZeile.DOUBLE_INIT;

import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.applic.TableField.TestOption;
import de.gdv.bsm.intern.params.VtFlv;
import de.gdv.bsm.intern.params.VtFlvZeile;
import de.gdv.bsm.intern.params.VtKlassik;
import de.gdv.bsm.intern.params.VtKlassikZeile;
import de.gdv.bsm.intern.params.VtOStressZeile;
import de.gdv.bsm.intern.params.ZeitunabhManReg;
import de.gdv.bsm.vu.module.Bilanzpositionen;
import de.gdv.bsm.vu.module.Deklaration;
import de.gdv.bsm.vu.module.Flv;
import de.gdv.bsm.vu.module.Functions;
import de.gdv.bsm.vu.module.KaModellierung;
import de.gdv.bsm.vu.module.Kundenverhalten;
import de.gdv.bsm.vu.module.Rohueberschuss;

/**
 * Simuliert das Excel-Blatt rzg.
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
public class RzgZeile {
	/** M�glicher Ihnalt des Feldes Deckungsstock. */
	public static final String DECKUNGS_STOCK_KDS = "KDS";
	/** M�glicher Ihnalt des Feldes Deckungsstock. */
	public static final String DECKUNGS_STOCK_FONDS = "Fonds";

	/** Zugrundeliegende Berechnung. */
	final Berechnung berechnung;
	/** Vorg�nger (chronologisch) dieser Zeile */
	final RzgZeile vg;
	/** Nachfolger (chronologisch) dieser Zeile */
	private RzgZeile nf = null;

	/** Stressszenario. */
	@TableField(testColumn = "A")
	final String szenario;
	/** Stressszenario ID. */
	@TableField(testColumn = "B")
	final int szenarioId;
	/** LoB. */
	@TableField(testColumn = "C")
	final String lob;
	/** Zeit. */
	@TableField(testColumn = "D")
	public final int zeit;
	/** Rechnungszinsgeneration. */
	@TableField(testColumn = "E")
	final int zinsGeneration;
	/** Alt-/Neubestand (a / n). */
	@TableField(testColumn = "F")
	final String altNeuBestand;
	/** �B/N�B. */
	@TableField(testColumn = "G")
	final String uebNueb;
	/** Klassik/FLV. */
	@TableField(testColumn = "H")
	final String klassikFlv;
	/** Deckungsstock (KDS/Fonds). */
	@TableField(testColumn = "I")
	final String deckungsStock;
	/** VN-Verhalten zinssensitiv. */
	@TableField(testColumn = "J")
	final String vnZinsSensitiv;
	/** Stress, in dem gestresste KA-Kostenfaktoren verwendet werden. */
	@TableField(testColumn = "K")
	final int kaKostenstressDerLob;
	/** Kosten. */
	@TableField(testColumn = "L", nachKomma = 3)
	final double kosten;
	/** Pr�mien. */
	@TableField(testColumn = "M")
	final double praemien;
	/** Leistungen beim Tod. */
	@TableField(testColumn = "N")
	final double lTod;
	/** Kapital-abfindungen, nur Renten-versicherung. */
	@TableField(testColumn = "O")
	final double lKa;
	/** Sonstige Erlebens-fallleistungen. */
	@TableField(testColumn = "P")
	final double sonstigeErlebensfallLeistungen;
	/** R�ckkauf. */
	@TableField(testColumn = "Q", nachKomma = 0)
	final double lRkw;
	/** Risiko�bersch�sse. */
	@TableField(testColumn = "R", nachKomma = 0)
	final double risikoErgebnis;
	/** Kosten�bersch�sse. */
	@TableField(testColumn = "S", nachKomma = 0)
	final double uebrigesErgebnis;
	/** CF EVU -> RVU (nicht LE-abh�ngig). */
	@TableField(testColumn = "T", nachKomma = 0)
	final double cfEvuRvu;
	/** Zinsratenzuschlag. */
	@TableField(testColumn = "U", nachKomma = 0)
	final double zinsratenZuschlag;
	/** Rechnungsm�ssiger Zinsaufwand. */
	@TableField(testColumn = "V", nachKomma = 0)
	final double zinsaufwand;
	/** HGB DRSt inkl. Ansammlungs-guthaben ohne ZZR. */
	@TableField(testColumn = "W", nachKomma = 0)
	final double drDet;
	/** Aufwand je Basispunkt. */
	@TableField(testColumn = "X", nachKomma = 5)
	final double aufwand;
	/** Korrekturterm ZZR wegen Rechnungsgrundlagen. */
	@TableField(testColumn = "Y", nachKomma = 3)
	final double korrekturZzr;
	/** Startwert Referenzzins. */
	@TableField(testColumn = "Z", nachKomma = 2)
	final double startWertRefZins;

	// =================================================================================
	// Spalten A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A

	/** S�AF. */
	@TableField(testColumn = "AA", testOption = TestOption.START, nachKomma = 0)
	double sUeAfRzg = DOUBLE_INIT;
	/** ZZR. */
	@TableField(testColumn = "AB", testOption = TestOption.START, nachKomma = 0)
	double zzrJ = DOUBLE_INIT;
	/** S�AF-Bewegung aus dem FLV-S�AF, Aufschubzeit in den FLV-S�AF, Rentenbezug. */
	@TableField(testColumn = "AC", testOption = TestOption.START, nachKomma = 0)
	double sueAfFlvBewegungAus = DOUBLE_INIT;
	/** S�AF-Bewegung in den FLV-S�AF, Rentenbezug aus dem FLV-S�AF, Aufschubzeit */
	@TableField(testColumn = "AD", testOption = TestOption.START, nachKomma = 0)
	double sueafFlvBewegungIn = DOUBLE_INIT;
	/** Projektionsl�nge, Versicherungstechnik. AC. */
	@TableField(testColumn = "AE", nachKomma = 0)
	int laengeProjektionDr = Integer.MIN_VALUE;
	/** Beitr�ge, stochastisch. */
	@TableField(testColumn = "AF", testOption = TestOption.START, nachKomma = 0)
	double beitraegeStoch = DOUBLE_INIT;
	/** Kosten, stochastisch. */
	@TableField(testColumn = "AG", testOption = TestOption.START, nachKomma = 0)
	double kostenStoch = DOUBLE_INIT;
	/** Risiko-�bersch�sse, stochastische. */
	@TableField(testColumn = "AH", testOption = TestOption.START, nachKomma = 0)
	double risikoUebStoch = DOUBLE_INIT;
	/** Kosten-�bersch�sse, stochastisch. */
	@TableField(testColumn = "AI", testOption = TestOption.START, nachKomma = 0)
	double kostenUebStoch = DOUBLE_INIT;
	/** Kosten�berschuss (Bestand) - �EB. */
	@TableField(testColumn = "AJ", testOption = TestOption.START, nachKomma = 0)
	double kostenueberschussBestand = DOUBLE_INIT;
	/** CF EVU -> RVU (nicht LE-abh�ngig), stochastisch. */
	@TableField(testColumn = "AK", testOption = TestOption.START, nachKomma = 0)
	double cfRvStoch = DOUBLE_INIT;
	/** Zinsratenzuschlag, stochastisch. */
	@TableField(testColumn = "AL", testOption = TestOption.START, nachKomma = 0)
	double ziRaZuStoch = DOUBLE_INIT;
	/** garantierte Leistungen, ohne SonstErl. */
	@TableField(testColumn = "AM", nachKomma = 0)
	double lGarantiertOSonstErl = DOUBLE_INIT;
	/** Leistungsbarwert SonstErll, deterministisch. */
	@TableField(testColumn = "AN", nachKomma = 0)
	double lbwSonstErl = DOUBLE_INIT;
	/** Leistungsbarwert ohne SonstErl, deterministisch. */
	@TableField(testColumn = "AO", nachKomma = 0)
	double lbwGarOSonstErl = DOUBLE_INIT;
	/** S�mtliche garantierte Leistungen, mit Kapitalwahl, ohne Storno. */
	@TableField(testColumn = "AP", testOption = TestOption.START, nachKomma = 0)
	double lGarantiert = DOUBLE_INIT;
	/** s�mtliche garantierte Leistungen, deterministisch. */
	@TableField(testColumn = "AQ", nachKomma = 0)
	double lGarantiertDet = DOUBLE_INIT;
	/** rmZ_Tarif. */
	@TableField(testColumn = "AR", testOption = TestOption.START, nachKomma = 0)
	double rmZTarif = DOUBLE_INIT;
	/** Delta ZZR. */
	@TableField(testColumn = "AS", testOption = TestOption.START, nachKomma = 0)
	double deltaZZR = DOUBLE_INIT;
	/** Deckungsr�ckstellung vor Deklaration. */
	@TableField(testColumn = "AT", testOption = TestOption.START, nachKomma = 0)
	double drVorDekl = DOUBLE_INIT;
	/** LBW gar. */
	@TableField(testColumn = "AU", testOption = TestOption.START, nachKomma = 0)
	double lbwGar = DOUBLE_INIT;
	/** Dekl rest */
	@TableField(testColumn = "AV", testOption = TestOption.START, nachKomma = 0)
	double deklRzgRest = DOUBLE_INIT;
	/** Deklaration */
	@TableField(testColumn = "AW", testOption = TestOption.START, nachKomma = 0)
	double deklRzg = DOUBLE_INIT;
	/** S�AF_Zuf durch RfB �berlauf */
	@TableField(testColumn = "AX", testOption = TestOption.START, nachKomma = 0)
	double sueafZufFrfbUeberlauf = DOUBLE_INIT;
	/** S�AF_Zuf. */
	@TableField(testColumn = "AY", testOption = TestOption.START, nachKomma = 0)
	double sUeAfzuf = DOUBLE_INIT;
	/** Barauszahlung. */
	@TableField(testColumn = "AZ", testOption = TestOption.START, nachKomma = 0)
	double bar = DOUBLE_INIT;

	// =================================================================================
	// Spalten B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B

	/** Lock-In (Garantierte Leistung) */
	@TableField(testColumn = "BA", testOption = TestOption.START, nachKomma = 0)
	double lockIn = DOUBLE_INIT;
	/** Leistungs-erh�hung Lock-In, aggregiert. */
	@TableField(testColumn = "BB", testOption = TestOption.START, nachKomma = 10)
	double leLockInAggr = DOUBLE_INIT;
	/** Leistungsanpassung FLV. */
	@TableField(testColumn = "BC", testOption = TestOption.START, nachKomma = 0)
	double leistungsAnpassungFlv = DOUBLE_INIT;
	/** Leistungserh�hung Lock-In. */
	@TableField(testColumn = "BD", testOption = TestOption.START, nachKomma = 0)
	double leLockInAggrFlv = DOUBLE_INIT;
	/** Cashflow der garantierten Leistungen inkl Lock-In. */
	@TableField(testColumn = "BE", testOption = TestOption.START, nachKomma = 0)
	double lGarStoch = DOUBLE_INIT;
	/** S�AF Entnahme. */
	@TableField(testColumn = "BF", testOption = TestOption.START, nachKomma = 0)
	double sUeAfEntnahme = DOUBLE_INIT;
	/** S�AF 56b Entnahme. */
	@TableField(testColumn = "BG", testOption = TestOption.START, nachKomma = 0)
	double sUeAf56bEntnahmeRzg = DOUBLE_INIT;
	/** Leistungserh�hung S�AF. */
	@TableField(testColumn = "BH", testOption = TestOption.START, nachKomma = 0)
	double leSUeAf = DOUBLE_INIT;
	/** Leistungs-erh�hung Gesamt. */
	@TableField(testColumn = "BI", testOption = TestOption.START, nachKomma = 0)
	double leGesamtAggr = DOUBLE_INIT;
	/** Leistungen Gesamt. */
	@TableField(testColumn = "BJ", testOption = TestOption.START, nachKomma = 0)
	double lGesamt = DOUBLE_INIT;
	/** Leistungen durch Endzahlung. */
	@TableField(testColumn = "BK", testOption = TestOption.START, nachKomma = 0)
	double endZahlung = DOUBLE_INIT;
	/** Deckungsr�ckstellung Lock-In. */
	@TableField(testColumn = "BL", testOption = TestOption.START, nachKomma = 0)
	double drLockInRzg = DOUBLE_INIT;
	/** Deckungsr�ckstellung Gesamt. */
	@TableField(testColumn = "BM", testOption = TestOption.START, nachKomma = 0)
	double drGesamtRzg = DOUBLE_INIT;
	/** Basisstorno. */
	@TableField(testColumn = "BN", nachKomma = 5, percent = true)
	double sBasis = DOUBLE_INIT;
	/** Zinsdifferenz. */
	@TableField(testColumn = "BO", testOption = TestOption.START, nachKomma = 5, percent = true)
	double deltaI = DOUBLE_INIT;
	/** Zus�tzlicher Storno. */
	@TableField(testColumn = "BP", testOption = TestOption.START, nachKomma = 8)
	double lambdaStorno = DOUBLE_INIT;
	/** Kapitalwahl�nderung. */
	@TableField(testColumn = "BQ", testOption = TestOption.START, nachKomma = 4)
	double lambdaKa = DOUBLE_INIT;
	/** Gesamt Storno. */
	@TableField(testColumn = "BR", testOption = TestOption.START, nachKomma = 8)
	double lambda = DOUBLE_INIT;
	/** R�ckkaufswerte Excess Betrag. */
	@TableField(testColumn = "BS", testOption = TestOption.START, nachKomma = 4)
	double rkwXs = DOUBLE_INIT;
	/** Kapitalabfindung Excess Betrag. */
	@TableField(testColumn = "BT", testOption = TestOption.START, nachKomma = 5)
	double kaGarXs = DOUBLE_INIT;
	/** Leistungsanpassung Kapitalwahl. */
	@TableField(testColumn = "BU", testOption = TestOption.START, nachKomma = 9)
	double laKapWahlXsAggr = DOUBLE_INIT;
	/** Aufwendungen f�r KA, Anteilsm��ig. */
	@TableField(testColumn = "BV", testOption = TestOption.START, nachKomma = 0)
	double kostenKaRzg = DOUBLE_INIT;
	/** Surplusfond. */
	@TableField(testColumn = "BW", testOption = TestOption.START, nachKomma = 0, checkFinite = false)
	double surplusFondRzg = DOUBLE_INIT;
	/** Delta Leistungen. */
	@TableField(testColumn = "BX", testOption = TestOption.START, nachKomma = 0)
	double deltaLRzg = DOUBLE_INIT;
	/** Deckungsr�ckstellung, k�nftige Pr�mien. */
	@TableField(testColumn = "BY", nachKomma = 0)
	double drstKp = DOUBLE_INIT;
	/** Roh�berschuss, k�nftige Pr�mien, nur positive Beitr�ge. */
	@TableField(testColumn = "BZ", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpRzg = DOUBLE_INIT;

	// =================================================================================
	// Spalten C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C

	/** Roh�berschuss, k�nftige Pr�mien, nur positive Beitr�ge. */
	@TableField(testColumn = "CA", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpRzgBY = DOUBLE_INIT;
	/** Roh�berschuss, k�nftige Pr�mien, nur negative Beitr�ge. */
	@TableField(testColumn = "CB", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpRzgNeg = DOUBLE_INIT;
	/** Jahres�berschuss geschl�sselt auf k�nftige Pr�mien. */
	@TableField(testColumn = "CC", testOption = TestOption.START, nachKomma = 0)
	double jueVnKpRzg = DOUBLE_INIT;
	/** Summe der Beitr�ge zum Roh�berschuss, nur positive Teile, �ber m Jahre, nur �E und RE. */
	@TableField(testColumn = "CD", testOption = TestOption.START, nachKomma = 0)
	double beitragRueRzg = DOUBLE_INIT;
	/** Anteil an Deklaration 2.Methode. */
	@TableField(testColumn = "CE", testOption = TestOption.START, nachKomma = 0, percent = true)
	double anteilDekl = DOUBLE_INIT;
	/** Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung. */
	@TableField(testColumn = "CF", testOption = TestOption.START, nachKomma = 0)
	double cashflowZuebRzg = DOUBLE_INIT;
	/** Optionen'= addRange(cellsReady, "BC", "BC");_rzg(AN5;BP4;AE5;AI5;AD5;AO5;M5;L5;T5;BR5;BQ5;BA4). */
	@TableField(testColumn = "CG", testOption = TestOption.START, nachKomma = 0)
	double cashflowOptionenRzg = DOUBLE_INIT;
	/** GCR. */
	@TableField(testColumn = "CH", testOption = TestOption.START, nachKomma = 0)
	double cfGcrRzg = DOUBLE_INIT;
	/** KBM. */
	@TableField(testColumn = "CI", testOption = TestOption.START, nachKomma = 0)
	double kbmRzg = DOUBLE_INIT;

	/**
	 * Konstruktion und initiale Berechnung einer Zeile. Datenbasis ist {@link VtKlassik}.
	 * 
	 * @param berechnung
	 *            Basis der Rechnung
	 * @param base
	 *            Daten aus VT Klasik
	 * @param zeitunabhManReg
	 *            Managementregeln
	 * @param vg
	 *            chronologische Vorg�ngerzeile
	 */
	public RzgZeile(final Berechnung berechnung, final VtKlassikZeile base, final ZeitunabhManReg zeitunabhManReg,
			final RzgZeile vg) {
		this.berechnung = berechnung;
		this.vg = vg;
		szenario = berechnung.getSzenarioName();
		szenarioId = berechnung.getSzenarioId();
		lob = base.getLob();
		zeit = base.getZeit();
		zinsGeneration = base.getZinsGeneration();
		altNeuBestand = base.getAltNeuBestand();
		uebNueb = berechnung.lobMapping.getLobMapping(lob).getUebNueb();
		klassikFlv = berechnung.lobMapping.getLobMapping(lob).getKategorie();
		deckungsStock = DECKUNGS_STOCK_KDS;
		vnZinsSensitiv = berechnung.getVuParameter().getLobMapping().getLobMapping(lob).getZinsSensitiv();
		kaKostenstressDerLob = berechnung.getVuParameter().getLobMapping().getLobMapping(lob).getKaKostenstressDerLob();
		kosten = base.getKosten();
		praemien = base.getPraemien();
		lTod = base.getLeistungBeiTod();
		lKa = base.getKapitalAbfindung();
		sonstigeErlebensfallLeistungen = base.getSonstigeErlebensfallLeistungen();
		lRkw = base.getRueckKauf();
		risikoErgebnis = base.getRisikoErgebnis();
		uebrigesErgebnis = base.getUebrigesErgebnis();
		cfEvuRvu = base.getCfEvuRvu();
		zinsratenZuschlag = base.getZinsratenZuschlag();
		zinsaufwand = base.getZinsaufwand();
		drDet = base.getDrDet();

		try {
			final List<VtOStressZeile> vtOStress = berechnung.vtOStress.getMap().get(lob).get(altNeuBestand)
					.get(zinsGeneration);
			aufwand = vtOStress.get(zeit).getZzrJeBasis();
			korrekturZzr = vtOStress.get(zeit).getKorrekturZzr();
			startWertRefZins = vtOStress.get(0).getStartWertRefZins();
		} catch (NullPointerException n) {
			throw new IllegalArgumentException(
					"Eingaben im Blatt VT o.Stress unvollst�ndig f�r Szenario " + szenarioId + "; lob = " + lob
							+ ", zins = " + zinsGeneration + ", altNeu = " + altNeuBestand + ", zeit = " + zeit);
		}

		initBerechnung(berechnung, zeitunabhManReg, vg);
	}

	/**
	 * Konstruktion und initiale Berechnung einer Zeile. Datenbasis ist {@link VtFlv}.
	 * 
	 * @param berechnung
	 *            Basis der Rechnung
	 * @param deckungsStockP
	 *            Deckungsstock dieser Zeile
	 * @param base
	 *            Daten aus VT FLV
	 * @param zeitunabhManReg
	 *            Managementregeln
	 * @param vg
	 *            chronologische Vorg�ngerzeile
	 */
	public RzgZeile(final Berechnung berechnung, final String deckungsStockP, final VtFlvZeile base,
			final ZeitunabhManReg zeitunabhManReg, final RzgZeile vg) {
		this.berechnung = berechnung;
		this.vg = vg;
		szenario = berechnung.szenarioName;
		szenarioId = berechnung.szenarioId;
		lob = base.getLob();
		zeit = base.getZeit();
		zinsGeneration = base.getRechnungsZinsGeneration();
		altNeuBestand = base.getAltNeuBestand();
		uebNueb = berechnung.lobMapping.getLobMapping(lob).getUebNueb();
		klassikFlv = berechnung.lobMapping.getLobMapping(lob).getKategorie();
		deckungsStock = deckungsStockP;
		vnZinsSensitiv = berechnung.getVuParameter().getLobMapping().getLobMapping(lob).getZinsSensitiv();
		kaKostenstressDerLob = berechnung.getVuParameter().getLobMapping().getLobMapping(lob).getKaKostenstressDerLob();
		if (deckungsStockP.equals(DECKUNGS_STOCK_KDS)) {
			kosten = base.getKostenRentenbezug();
			praemien = base.getPraemienVerrentendesKapital();
			lTod = base.getLeistungenBeimTodRentenbezug();
			lKa = 0.0;
			sonstigeErlebensfallLeistungen = base.getSonstigeErlebensFallLeistungenRentenbezug();
			lRkw = 0.0;
			risikoErgebnis = base.getRiskoErgebnisKlassischeRenteRentenbezug();
			uebrigesErgebnis = base.getUebrigesErgebnisKlassischeRentenRentenbezug();
			cfEvuRvu = 0.0;
			zinsaufwand = base.getRechnungsmae�igerZinsaufwandRentenbezug();
			drDet = base.getHgbDrStAnsammlungsguthabenRentenbezug();
		} else {
			kosten = 0.0;
			praemien = 0.0;
			lTod = 0.0;
			lKa = 0.0;
			sonstigeErlebensfallLeistungen = 0.0;
			lRkw = 0.0;
			risikoErgebnis = base.getRiskoergebnisFlvAufschubzeit();
			uebrigesErgebnis = base.getUebrigesErgebnisFlvAufschubzeit();
			cfEvuRvu = 0.0;
			zinsaufwand = 0.0;
			drDet = 0.0;
		}
		zinsratenZuschlag = 0.0;
		try {
			final List<VtOStressZeile> vtOStress = berechnung.vtOStress.getMap().get(lob).get(altNeuBestand)
					.get(zinsGeneration);
			aufwand = vtOStress.get(zeit).getZzrJeBasis();
			korrekturZzr = vtOStress.get(zeit).getKorrekturZzr();
			startWertRefZins = vtOStress.get(0).getStartWertRefZins();
		} catch (NullPointerException n) {
			throw new IllegalArgumentException(
					"Eingaben im Blatt VT o.Stress unvollst�ndig f�r Szenario " + szenarioId + "; lob = " + lob
							+ ", zins = " + zinsGeneration + ", altNeu = " + altNeuBestand + ", zeit = " + zeit);
		}

		initBerechnung(berechnung, zeitunabhManReg, vg);
	}

	/**
	 * Initiale Berechnung grundlegender nicht zeitrekursiver Werte.
	 * 
	 * @param berechnung
	 *            zugrunde liegende Berechnung
	 * @param zeitunabhManReg
	 *            Vorgaben des Unternehmens
	 * @param vg
	 *            der Vorg�nger
	 */
	private void initBerechnung(final Berechnung berechnung, final ZeitunabhManReg zeitunabhManReg, final RzgZeile vg) {

		if (zeit == 0) {
			if (!klassikFlv.equals("FLV") || deckungsStock.equals("Fonds")) {
				sUeAfRzg = berechnung.vtOStress.getMap().get(lob).get(altNeuBestand).get(zinsGeneration).get(0)
						.getSueaf();
				zzrJ = berechnung.vtOStress.getMap().get(lob).get(altNeuBestand).get(zinsGeneration).get(0).getZzr();
			} else {
				sUeAfRzg = 0.0;
				zzrJ = 0.0;
			}
		}

		sBasis = Kundenverhalten.sBasis(lRkw, drDet, zinsGeneration, zeitunabhManReg.getMonatZahlung());
		lGarantiertOSonstErl = Kundenverhalten.lGarantiertOSonstErl(lTod, lKa, lRkw);
		lGarantiertDet = Rohueberschuss.lGarantiertDet(lGarantiertOSonstErl, sonstigeErlebensfallLeistungen);

		final double vuZeit = berechnung.getZeitunabhManReg().getMonatZahlung();
		if (zeit == 0) {
			drstKp = 0.0;
		} else {
			drstKp = Bilanzpositionen.drstKp(vg.drstKp, praemien, zinsratenZuschlag, zinsGeneration, vuZeit,
					zinsaufwand, drDet, vg.drDet, zeit);
		}
		if (zeit > 0) {
			kbmRzg = Bilanzpositionen.KbmRzg(deckungsStock, risikoErgebnis, uebrigesErgebnis);
		}
	}

	/**
	 * Setze den chronologischen Nachfolger dieser Zeile.
	 * 
	 * @param nf
	 *            der Nachfolger
	 */
	public void setNf(final RzgZeile nf) {
		this.nf = nf;
	}

	/**
	 * Berechnung chonologisch inverser Werte.
	 */
	public void berechnungReversed00() {
		if (zeit == berechnung.zeitHorizont) {
			// sonst gibt es im unteren else ein Zeithorizont
			lbwSonstErl = 0.0;
			lbwGarOSonstErl = 0.0;
		} else {
			lbwSonstErl = Kundenverhalten.lbwSonstErl(zinsGeneration, nf.lbwSonstErl, zeit, berechnung.zeitHorizont,
					nf.sonstigeErlebensfallLeistungen, berechnung.getZeitunabhManReg().getMonatZahlung());
			lbwGarOSonstErl = Kundenverhalten.lbwGarOSonstErl(zinsGeneration, nf.lbwGarOSonstErl, zeit,
					berechnung.zeitHorizont, nf.lGarantiertOSonstErl,
					berechnung.getZeitunabhManReg().getMonatZahlung());
		}
	}

	/**
	 * Chronologische Rekursione auf der ersten Ebene.
	 * 
	 * @param agg
	 *            die zeitlich zugeh�rige agg-Zeile
	 */
	public void zeitRekursionL01(final AggZeile agg) {
		final List<RzgZeile> zeilen = berechnung.getRzgZeilen(lob, zinsGeneration, altNeuBestand, deckungsStock);
		laengeProjektionDr = KaModellierung.laengeProjektionDR(deckungsStock, zeilen);
		// final FlvZeile flvZeile = berechnung.getFlvZeile(lob, zinsGeneration, altNeuBestand, zeit);
		if (zeit == 0) {
			lambda = Kundenverhalten.lambdaStartwert();
			lbwGar = Deklaration.lbwGar(lbwGarOSonstErl, lbwSonstErl, laKapWahlXsAggr, lambda);
			drVorDekl = Rohueberschuss.drVorDekl(zeit, drDet, 0.0, laKapWahlXsAggr, lbwSonstErl, lbwGar, lambda);
		}
		if (zeit > 0) {
			lambdaStorno = Kundenverhalten.lambdaStorno(vnZinsSensitiv, vg.deltaI,
					berechnung.getZeitunabhManReg().getZinsToleranz(),
					berechnung.getZeitunabhManReg().getErhoehungBasisStorno(), sBasis, drDet);
			lambda = Kundenverhalten.lambda(vnZinsSensitiv, vg.lambda, lambdaStorno, sBasis);
			lambdaKa = Kundenverhalten.lambdaKa(vnZinsSensitiv, vg.deltaI,
					berechnung.getZeitunabhManReg().getZinsToleranz(),
					berechnung.getZeitunabhManReg().getErhoehungKapitalAbfindung(), drDet, lKa, lambdaStorno, sBasis);

			final FlvZeile flvZeile;
			if (klassikFlv.equals("FLV") && deckungsStock.equals("KDS")) {
				flvZeile = berechnung.getFlvZeile(lob, zinsGeneration, altNeuBestand, zeit);
			} else {
				flvZeile = null;
			}
			beitraegeStoch = Flv.beitraegeStoch(vg.lambda, praemien, klassikFlv, deckungsStock, flvZeile);
			kostenStoch = Kundenverhalten.kostenStoch(kosten, vg.lambda);
			risikoUebStoch = Kundenverhalten.risikoUebStoch(risikoErgebnis, vg.lambda);
			kostenUebStoch = Kundenverhalten.kostenUebStoch(uebrigesErgebnis, vg.lambda);
			kostenueberschussBestand = Rohueberschuss.kostenueberschussBestand(kostenUebStoch,
					berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft());

			cfRvStoch = Kundenverhalten.cfRvStoch(cfEvuRvu, vg.lambda);
			ziRaZuStoch = Kundenverhalten.ziRaZuStoch(zinsratenZuschlag, vg.lambda);
			lGarantiert = Rohueberschuss.lGarantiert(lGarantiertOSonstErl, sonstigeErlebensfallLeistungen,
					vg.laKapWahlXsAggr);

			lGarStoch = Deklaration.lGarStoch(lGarantiert, vg.leLockInAggrFlv, vg.lambda);

			beitragRueRzg = Deklaration.beitragRueRzg(uebNueb, risikoUebStoch, kostenUebStoch);
			kaGarXs = Kundenverhalten.kaGarXs(lKa, vg.lambda, lambdaKa);
			laKapWahlXsAggr = Kundenverhalten.laKapWahlXsAggr(vg.laKapWahlXsAggr, kaGarXs, lbwSonstErl, zinsGeneration,
					lambda, berechnung.getZeitunabhManReg().getMonatZahlung());

			lbwGar = Deklaration.lbwGar(lbwGarOSonstErl, lbwSonstErl, laKapWahlXsAggr, lambda);
			rkwXs = Kundenverhalten.rkwXs(sBasis, lambda, vg.lambda, lambdaStorno, drDet, vg.leLockInAggrFlv, lbwGar,
					vg.laKapWahlXsAggr, lbwSonstErl, zinsGeneration, berechnung.getZeitunabhManReg().getMonatZahlung());
			leistungsAnpassungFlv = Flv.leistungsAnpassungFlv(vg.leistungsAnpassungFlv, praemien, beitraegeStoch,
					lbwGar, zinsGeneration, klassikFlv, berechnung.getZeitunabhManReg().getMonatZahlung(), zeit);
			cashflowZuebRzg = Bilanzpositionen.cashflowZuebRzg(vg.bar, vg.leGesamtAggr, lGarantiert, vg.lambda, kaGarXs,
					vg.lbwGar, berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, laengeProjektionDr,
					zinsGeneration, vg.leistungsAnpassungFlv);

			drVorDekl = Rohueberschuss.drVorDekl(zeit, drDet, vg.leLockInAggrFlv, laKapWahlXsAggr, lbwSonstErl, lbwGar,
					lambda);

			zzrJ = Rohueberschuss.zzrJ(altNeuBestand, zinsGeneration, agg.referenzZinssatz, agg.refZins2M, aufwand,
					drVorDekl, berechnung.getZeitunabhManReg().getZzrMethodeAltbestand(), korrekturZzr,
					startWertRefZins);
			lGesamt = Deklaration.lGesamt(vg.leGesamtAggr, lGarantiert, zeit, zinsGeneration, vg.bar, vg.lambda,
					kaGarXs, rkwXs, berechnung.getZeitunabhManReg().getMonatZahlung(), vg.lbwGar);

			rmZTarif = Rohueberschuss.rmZTarif(zinsaufwand, vg.leLockInAggrFlv, lbwGar, vg.lbwGar, lGarantiert, zeit,
					zinsGeneration, vg.sUeAfEntnahme, vg.bar, rkwXs, sonstigeErlebensfallLeistungen, kaGarXs,
					vg.laKapWahlXsAggr, vg.lbwSonstErl, berechnung.getZeitunabhManReg().getMonatZahlung(), vg.lambda,
					beitraegeStoch, praemien, klassikFlv);

			deltaZZR = Rohueberschuss.deltaZzr(zzrJ, vg.zzrJ);

			cashflowOptionenRzg = Bilanzpositionen.cashflowOptionenRzg(lGarantiert, vg.lambda, kostenStoch,
					beitraegeStoch, lGarantiertDet, praemien, kosten, kaGarXs, rkwXs, vg.leistungsAnpassungFlv);
		}
	}

	/**
	 * Zweite Ebene der Zeitrekursion.
	 * 
	 * @param agg
	 *            die zeitlich zugeh�rige Agg-Zeile
	 */
	public void zeitRekursionL02(final AggZeile agg) {
		if (zeit > 0) {
			cfGcrRzg = KaModellierung.cfGcrRzg(uebNueb, kostenUebStoch, agg.ueEalt, agg.ueEneu, agg.gcrUeB);

			kostenKaRzg = KaModellierung.kostenKaRzg(vg.drLockInRzg, agg.vg.drLockInAgg, agg.aufwendungenKa);
			rohuebKpRzg = Bilanzpositionen.rohuebKpRzg(agg.kapitalertragAnrechenbar, agg.keVerrechnung, agg.vg.drGesAgg,
					vg.drGesamtRzg, rmZTarif + deltaZZR, risikoUebStoch, kostenUebStoch, vg.drDet, vg.drDet, "pos");
			rohuebKpRzgBY = Bilanzpositionen.rohuebKpRzg(agg.kapitalertragAnrechenbar, agg.keVerrechnung,
					agg.vg.drGesAgg, vg.drGesamtRzg, rmZTarif + deltaZZR, risikoUebStoch, kostenUebStoch, vg.drDet,
					vg.drstKp, "pos");
			rohuebKpRzgNeg = Bilanzpositionen.rohuebKpRzg(agg.kapitalertragAnrechenbar, agg.keVerrechnung,
					agg.vg.drGesAgg, vg.drGesamtRzg, rmZTarif + deltaZZR, risikoUebStoch, kostenUebStoch, vg.drDet,
					vg.drstKp, "neg");
			sUeAf56bEntnahmeRzg = Deklaration.sUeAf56bEntnahmeRzg(agg.vg.sueAf, vg.sUeAfRzg, agg.sUeAf56bEntnahme,
					zeit);
			deklRzgRest = Deklaration.deklRzgRest(beitragRueRzg, agg.beitragRohUebAgg, agg.deklRest);
			anteilDekl = Deklaration.anteilDekl(zeit, uebNueb, beitragRueRzg, agg.beitragRohUebAgg, vg.anteilDekl,
					vg.drLockInRzg, vg.sUeAfRzg, agg.vg.drLockInAggWennLoB, agg.vg.sueAf);
			deklRzg = Deklaration.deklRzg(berechnung.getZeitunabhManReg().getDeklarationsMethode(), agg.vzGes, rmZTarif,
					vg.drLockInRzg, zeit, uebNueb, agg.deklZins, deklRzgRest);
			sueafZufFrfbUeberlauf = Deklaration.sueafZufFrfbUeberlauf(agg.dekl, deklRzg,
					berechnung.getZeitabhManReg().get(zeit).getFrfbUeberlauf(), agg.fRfBUeberlauf, zeit, uebNueb);
			sUeAfzuf = Deklaration.sUeAfzuf(deckungsStock,
					berechnung.getZeitabhManReg().get(zeit).getZielBarauszahlungFlv(),
					berechnung.getZeitabhManReg().get(zeit).getSueafZuf(),
					berechnung.getZeitabhManReg().get(zeit).getSueafZufMin(), vg.drLockInRzg, deklRzg, zeit,
					sueafZufFrfbUeberlauf, risikoErgebnis, uebrigesErgebnis, laengeProjektionDr);
			bar = Deklaration.bar(deckungsStock,
					berechnung.getZeitabhManReg().get(zeit).getZielBarauszahlungKlassisch(), praemien, deklRzg,
					sUeAfzuf, sueafZufFrfbUeberlauf);

			leSUeAf = Deklaration.leSUeAf(vg.sUeAfRzg, sUeAfzuf, sUeAf56bEntnahmeRzg, lbwGar, zeit);
			lockIn = Deklaration.lockIn(deklRzg, sUeAfzuf, sueafZufFrfbUeberlauf, bar, 0.0);
			leLockInAggr = Deklaration.leLockInAggr(vg.leLockInAggr, lockIn, lbwGar, zeit);

		}

		deltaI = Kundenverhalten.deltaI(agg.spotVnVerhaltenEsg, rmZTarif, zinsGeneration, deklRzg,
				vg != null ? vg.drLockInRzg : 0.0, zeit);

		if (zeit > 0) {
			{
				// einige Zukuftswerte werden ben�tigt:
				double nfLGarantiert = 0.0; // dies ist Spalte AN (zeit + 1)
				double nfKAGarXS = 0.0; // dies ist Spalte BR (zeit + 1)
				if (nf != null) {
					nfLGarantiert = Rohueberschuss.lGarantiert(nf.lGarantiertOSonstErl,
							nf.sonstigeErlebensfallLeistungen, Functions.nanZero(laKapWahlXsAggr));
					// man braucht BN6 (lambdaStorno) f�r das neue ...
					final double nfLambdaStorno = Kundenverhalten.lambdaStorno(nf.vnZinsSensitiv, deltaI,
							berechnung.getZeitunabhManReg().getZinsToleranz(),
							berechnung.getZeitunabhManReg().getErhoehungBasisStorno(), nf.sBasis, nf.drDet);
					// =lambda_KA(J5;BM4;zeitunabh.ManReg!B$33;zeitunabh.ManReg!B$35;W5;O5;BN5;BL5)
					final double nflambdaKa = Kundenverhalten.lambdaKa(nf.vnZinsSensitiv, deltaI,
							berechnung.getZeitunabhManReg().getZinsToleranz(),
							berechnung.getZeitunabhManReg().getErhoehungKapitalAbfindung(), nf.drDet, nf.lKa,
							nfLambdaStorno, sBasis);
					nfKAGarXS = Kundenverhalten.kaGarXs(nf.lKa, lambda, nflambdaKa);
				}
				final FlvZeile flvZeile = berechnung.getFlvZeile(lob, zinsGeneration, altNeuBestand, zeit);
				// hier brauchen wir nf.L_garantiert:
				sUeAfEntnahme = Deklaration.sUeAfEntnahme(leSUeAf, nfLGarantiert, lbwGar, vg.sUeAfRzg, sUeAfzuf,
						sUeAf56bEntnahmeRzg, nfKAGarXS, lambda, berechnung.getZeitunabhManReg().getMonatZahlung(),
						deckungsStock, zinsGeneration, flvZeile, berechnung.zeitHorizont, laengeProjektionDr, zeit);
				sueAfFlvBewegungAus = Deklaration.sueAfFlvBewegungAus(vg.sUeAfRzg, sUeAfzuf, sUeAf56bEntnahmeRzg,
						klassikFlv, deckungsStock, flvZeile, berechnung.zeitHorizont, laengeProjektionDr, zeit);
				/**
				 * wird im Excelblatt in Deklaration.SUeAF_FLV_Bewegung_aus gerechnet. Der gerechnete Wert
				 * sueAfFlvBewegungAus wird f�r Deckungsstock=Fonds nach sueafFlvBewegungIn f�r Deckungsstock=KDS
				 * geschoben danach m�ssen mit diesen Werten noch sUeAfRzg und drGesamtRzg gerechnet werden
				 */
			}
			leLockInAggrFlv = Flv.leLockInAggrFlv(leLockInAggr, leistungsAnpassungFlv);
			leGesamtAggr = Deklaration.leGesamtAggr(leLockInAggrFlv, leSUeAf);

			endZahlung = Deklaration.endZahlung(deckungsStock, sUeAfEntnahme, lockIn, bar, agg.fRfBVorEndzahlung,
					agg.vg.drLockInAggWennLoB, vg.drLockInRzg, uebNueb, zeit, laengeProjektionDr,
					berechnung.laengeProjektionDr, lbwGar);

			deltaLRzg = Bilanzpositionen.deltaLRzg(lGesamt, lGarantiert, zeit, vg.lambda, kaGarXs, rkwXs,
					vg.leistungsAnpassungFlv, endZahlung);

		}

		drLockInRzg = Deklaration.drLockInRzg(drDet, leLockInAggrFlv, lbwGar, sUeAfEntnahme, bar, lbwSonstErl,
				laKapWahlXsAggr, lambda);

		if (klassikFlv.equals("FLV") && deckungsStock.equals("Fonds")) {
			RzgZeile zeile = berechnung.getRzgZeile(lob, zinsGeneration, altNeuBestand, "KDS", zeit);
			zeile.sueafFlvBewegungIn = sueAfFlvBewegungAus;
			if (zeit > 0) {
				zeile.sUeAfRzg = Deklaration.sueafRzg(zeile.vg.sUeAfRzg, zeile.sUeAfzuf, zeile.sUeAfEntnahme,
						zeile.sUeAf56bEntnahmeRzg, zeile.sueAfFlvBewegungAus, zeile.sueafFlvBewegungIn);
			}
			sueafFlvBewegungIn = 0.0;
			zeile.drGesamtRzg = Deklaration.drGesamtRzg(zeile.drLockInRzg, zeile.sUeAfRzg);
			if (zeit > 0) {
				sUeAfRzg = Deklaration.sueafRzg(vg != null ? vg.sUeAfRzg : 0.0, sUeAfzuf, sUeAfEntnahme,
						sUeAf56bEntnahmeRzg, sueAfFlvBewegungAus, sueafFlvBewegungIn);
			}
			drGesamtRzg = Deklaration.drGesamtRzg(drLockInRzg, sUeAfRzg);
		} else if (!klassikFlv.equals("FLV")) {
			sueafFlvBewegungIn = 0.0;
			if (zeit > 0) {
				sUeAfRzg = Deklaration.sueafRzg(vg != null ? vg.sUeAfRzg : 0.0, sUeAfzuf, sUeAfEntnahme,
						sUeAf56bEntnahmeRzg, sueAfFlvBewegungAus, sueafFlvBewegungIn);
			}
			drGesamtRzg = Deklaration.drGesamtRzg(drLockInRzg, sUeAfRzg);
		} else {
			// dieser Fall wird oben bei FLV/Fonds bereits geregelt.
			// dort wird die passende Zeile ermittelt und gef�llt
		}

	}

	/**
	 * Dritte Ebene der Zeitrekursion.
	 * 
	 * @param agg
	 *            die zeitlich zugeh�rige Agg-Zeile
	 */
	public void zeitRekursionL03(final AggZeile agg) {
		if (zeit > 0) {
			jueVnKpRzg = Bilanzpositionen.jueVnKpRzg(agg.jueVnKp, agg.rohuebKpK, rohuebKpRzgBY, agg.rohuebKpN,
					rohuebKpRzgNeg, agg.vg.hgbDrAgg, vg.drDet);
		}
	}

	/**
	 * Chronologische R�ckw�rtsberechnung der Spalte surplusFondRzg.
	 * 
	 * @param aggZeile
	 *            chronologisch korrekspondierend
	 */
	public void surplusFondRueckwaerts(final AggZeile aggZeile) {
		if (zeit > 0) {
			surplusFondRzg = Bilanzpositionen.surplusfondRzg(aggZeile.cashflowSf,
					aggZeile.nf == null ? 0.0 : aggZeile.nf.deltaLAgg, aggZeile.deltaLAgg,
					nf == null ? 0.0 : nf.deltaLRzg, deltaLRzg, aggZeile.vg.drLockInAggWennLoB, vg.drLockInRzg,
					uebNueb);
		}
	}

	/**
	 * Vorg�nger (chronologisch) dieser Zeile
	 * 
	 * @return Vorg�nger
	 */
	public RzgZeile getVg() {
		return vg;
	}

	// ===========================================================================
	// get-Funktionen f�r A bis Z

	/**
	 * Zeit. D.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Rechnungszinsgeneration. E.
	 * 
	 * @return Rechnungszinsgeneration
	 */
	public int getZinsGeneration() {
		return zinsGeneration;
	}

	/**
	 * �B/N�B. G.
	 * 
	 * @return �B/N�B
	 */
	public String getUebNueb() {
		return uebNueb;
	}

	/**
	 * Deckungsstock (KDS/Fonds). I.
	 * 
	 * @return der Wert
	 */
	public String getDeckungsStock() {
		return deckungsStock;
	}

	/**
	 * Stress, in dem gestresste KA-Kostenfaktoren verwendet werden. K.
	 * 
	 * @return der Wert
	 */
	public int getKaKostenstressDerLob() {
		return kaKostenstressDerLob;
	}

	/**
	 * Kosten. L.
	 * 
	 * @return der Wert
	 */
	public double getKosten() {
		return kosten;
	}

	/**
	 * Pr�mien. M.
	 * 
	 * @return der Wert
	 */
	public double getPraemien() {
		return praemien;
	}
	
	/**
	 * Leistungen beim Tod. N.
	 * 
	 * @return der Wert
	 */
	public double getlTod() {
		return lTod;
	}
	
	/**
	 * Kapitalabfindungen. O.
	 * 
	 * @return der Wert
	 */
	public double getlKa() {
		return lKa;
	}
	
	/**
	 * sonstige Erlebensfallleistungen. P.
	 * 
	 * @return der Wert
	 */
	public double getsonstigeErlebensfallLeistungen() {
		return sonstigeErlebensfallLeistungen;
	}
	
	/**
	 * R�ckkaufswert. Q.
	 * 
	 * @return der Wert
	 */
	public double getlRkw() {
		return lRkw;
	}

	/**
	 * Risiko�bersch�sse. R.
	 * 
	 * @return der Wert
	 */
	public double getRisikoErgebnis() {
		return risikoErgebnis;
	}

	/**
	 * Kosten�bersch�sse. S.
	 * 
	 * @return der Wert
	 */
	public double getUebrigesErgebnis() {
		return uebrigesErgebnis;
	}

	/**
	 * HGB DRSt inkl. Ansammlungs-guthaben ohne ZZR. W.
	 * 
	 * @return der Wert
	 */
	public double getDrDet() {
		return drDet;
	}

	// ===========================================================================
	// get-Funktionen f�r AA bis AZ

	/**
	 * S�AF. AA.
	 * 
	 * @return der Wert
	 */
	public double getsUeAfRzg() {
		return sUeAfRzg;
	}

	/**
	 * ZZR. AB.
	 * 
	 * @return der Wert
	 */
	public double getZzrJ() {
		return zzrJ;
	}

	/**
	 * Beitr�ge, stochastisch. AF.
	 * 
	 * @return der Wert
	 */
	public double getBeitraegeStoch() {
		return beitraegeStoch;
	}

	/**
	 * Kosten, stochastisch. AG.
	 * 
	 * @return der Wert
	 */
	public double getKostenStoch() {
		return kostenStoch;
	}

	/**
	 * CF EVU -> RVU (nicht LE-abh�ngig), stochastisch. AK.
	 * 
	 * @return der Wert
	 */
	public double getCfRvStoch() {
		return cfRvStoch;
	}

	/**
	 * S�mtliche garantierte Leistungen, deterministisch. AQ.
	 * 
	 * @return der Wert
	 */
	public double getLGarantiertDet() {
		return lGarantiertDet;
	}

	/**
	 * rmZ_Tarif. AP.
	 * 
	 * @return der Wert
	 */
	public double getRmZTarif() {
		return rmZTarif;
	}

	// ===========================================================================
	// get-Funktionen f�r BA bis BZ

	/**
	 * Leistungen Gesamt. BJ.
	 * 
	 * @return der Wert
	 */
	public double getLGesamt() {
		return lGesamt;
	}

	/**
	 * Leistungen durch Endzahlung. BK.
	 * 
	 * @return der Wert
	 */
	public double getEndZahlung() {
		return endZahlung;
	}

	/**
	 * Deckungsr�ckstellung Lock-In. BL.
	 * 
	 * @return der Wert
	 */
	public double getDrLockInRzg() {
		return drLockInRzg;
	}

	/**
	 * Aufwendungen f�r KA, Anteilsm��ig. BV.
	 * 
	 * @return der Wert
	 */
	public double getKostenKaRzg() {
		return kostenKaRzg;
	}

	/**
	 * Surplusfond. BW.
	 * 
	 * @return der Wert
	 */
	public double getSurplusFondRzg() {
		return surplusFondRzg;
	}

	// ===========================================================================
	// get-Funktionen f�r CA bis CZ

	/**
	 * Jahres�berschuss geschl�sselt auf k�nftige Pr�mien. CC.
	 * 
	 * @return der Wert
	 */
	public double getJueVnKpRzg() {
		return jueVnKpRzg;
	}

	/**
	 * Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung. CF.
	 * 
	 * @return der Wert
	 */
	public double getCashflowZuebRzg() {
		return cashflowZuebRzg;
	}

	/**
	 * Optionen'=Cashflow_Optionen_rzg(AN5;BP4;AE5;AI5;AD5;AO5;M5;L5;T5;BR5;BQ5;BA4). CG.
	 * 
	 * @return der wert
	 */
	public double getCashflowOptionenRzg() {
		return cashflowOptionenRzg;
	}

	/**
	 * GCR. Spalte CH.
	 * 
	 * @return der Wert
	 */
	public double getCfGcrRzg() {
		return cfGcrRzg;
	}

	/**
	 * KBM. Spalte CI.
	 * 
	 * @return der Wert.
	 */
	public double getKbmRzg() {
		return kbmRzg;
	}

	@Override
	public String toString() {
		return "Rzg[" + szenarioId + "," + lob + "," + zeit + "," + zinsGeneration + "," + altNeuBestand + ","
				+ deckungsStock + "]";
	}
}
