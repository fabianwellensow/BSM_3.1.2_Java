package de.gdv.bsm.vu.berechnung;

import static de.gdv.bsm.vu.module.Functions.nanZero;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.applic.TableField.TestOption;
import de.gdv.bsm.intern.params.ReferenzZinssatz;
import de.gdv.bsm.intern.params.ZeitunabhManReg;
import de.gdv.bsm.intern.rechnung.CheckData;
import de.gdv.bsm.intern.rechnung.ResultNotFinite;
import de.gdv.bsm.intern.szenario.PfadZeile;
import de.gdv.bsm.vu.module.Bilanzpositionen;
import de.gdv.bsm.vu.module.Deklaration;
import de.gdv.bsm.vu.module.EsgFormeln;
import de.gdv.bsm.vu.module.Functions;
import de.gdv.bsm.vu.module.KaModellierung;
import de.gdv.bsm.vu.module.Rohueberschuss;

/**
 * Simuliert eine Zeile des Blattes agg. Weiter werden hier auch die Daten der Bl�tter <b>VT Klassik MW</b> und <b>FI
 * CFs</b> vorgehalten.
 * <p>
 * Die Daten sind in der Regel Package-Lokal. Insbesondere f�r die Berechnung der Kennzahlen, die im Package
 * <code>de.gdv.bsm.vu</code> liegt, werden get-Funktionen ben�tigt. Zur Zeit sind nur die f�r die
 * Standard-Implementierung ben�gigten get-Funktionen implementiert. Dies kann aber bei Bedarf leicht erweitert werden.
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
public class AggZeile {
	/**
	 * Initialisierung von Double-Werten. Sollte produktiv Double.NaN sein, kann zu Testzwecken aber ge�ndert werden
	 */
	public static final double DOUBLE_INIT = Double.NaN;

	/** �bergeordnete Berechnung f�r allgemeine Parameter. */
	private final Berechnung berechnung;
	/** rzg-Zeilen, die von diesem Agg Aggregiert werden */
	private final List<RzgZeile> rzgZeilen;
	/** flv-Zeilen zur selben Zeit. */
	private final List<FlvZeile> flvZeilen;

	/** Zeilicher Vorg�nger zu dieser Zeile. */
	public final AggZeile vg;
	/** Zeilicher Nachfolger zu dieser Zeile. */
	AggZeile nf = null;

	// Blatt VT Klassik MW ================================================
	// Die Spalte B ist hier nicht vorhanden, da diese MW_VT in agg (Spalte FX)
	// entsprechen.
	/** Anpassung der KA-Aufwendungen. VT Klassik MW!E, Rekursiv2. */
	public double aKaAufwendungen = DOUBLE_INIT;
	/** Anpassung durch VN-Verhalten. VT Klassik MW!D, Rekursiv2. */
	public double aVN = DOUBLE_INIT;
	/** Lock-In-Faktor. VT Klassik MW!C, Rekursiv2. */
	public double lockInFaktor = DOUBLE_INIT;

	// Blatt FI CFs =======================================================
	/**
	 * CF FIs zur selben Zeit. Indiziert �ber die Restlaufzeit. Blatt FI CFs, Spalten C bis CY.
	 */
	private double[] cfFis;
	// Blatt FI MW =======================================================
	/**
	 * CF FI Zeitschrittig zur selben Zeit. Indiziert �ber die Restlaufzeit. Blatt FI MW, Spalten C bis CY.
	 */
	private double[] cfFiZeitschrittig;
	/** MW FI (Endes des Jahres). Blatt FI MW, Spalte B. */
	public double mwFiJahresende = DOUBLE_INIT;

	// Spalte A - Z =======================================================
	/** Stressszenario. */
	@TableField(testColumn = "A")
	final String szenario;
	/** Stressszenario ID */
	@TableField(testColumn = "B")
	final int szenarioId;
	/** Zeit */
	@TableField(testColumn = "C")
	public final int zeit;
	/** Kosten. */
	@TableField(testColumn = "D", nachKomma = 2)
	final double kAgg;
	/** Pr�mien. */
	@TableField(testColumn = "E", nachKomma = 2)
	final double bAgg;
	/** Leistungen beim Tod. */
	@TableField(testColumn = "F", nachKomma = 2)
	final double lTodAgg;
	/** Kapitalabfindungen, nur Rentenversicherung. */
	@TableField(testColumn = "G", nachKomma = 2)
	final double kaAgg;
	/** Sonstige Erlebensfallleistungen. */
	@TableField(testColumn = "H", nachKomma = 2)
	final double lSonstErlAgg;
	/** R�ckkauf. */
	@TableField(testColumn = "I", nachKomma = 2)
	final double rkAgg;
	/** Risiko�bersch�sse. */
	@TableField(testColumn = "J", nachKomma = 2)
	final double rueAgg;
	/** Kosten�bersch�sse. */
	@TableField(testColumn = "K", nachKomma = 2)
	final double kueAgg;
	/** CF EVU -> RVU. */
	@TableField(testColumn = "L", nachKomma = 0)
	final double cfEvuRvu;
	/** Zinsratenzuschlag. M, */
	@TableField(testColumn = "M", nachKomma = 2)
	final double zinsratenzuschlaegeAgg;
	/** Rechnungsm�ssiger Zinsaufwand. */
	@TableField(testColumn = "N", nachKomma = 2)
	final double rmzEingabeAgg;
	/** HGB DRSt inkl. Ansammlungsguthaben ohne ZZR. */
	@TableField(testColumn = "O", nachKomma = 0)
	final double hgbDrAgg;
	/** ZZR UEB, alt. */
	@TableField(testColumn = "P", testOption = TestOption.START, nachKomma = 0)
	double zzrAlt;
	/** ZZR UEB, neu. */
	@TableField(testColumn = "Q", testOption = TestOption.START, nachKomma = 0)
	double zzrNeu;
	/** ZZR - N�B. */
	@TableField(testColumn = "R", testOption = TestOption.START, nachKomma = 0)
	double zzrNueb;
	/** ZZR. */
	@TableField(testColumn = "S", testOption = TestOption.START, nachKomma = 0)
	double zzrGesamt = DOUBLE_INIT;
	/** S�AF, alt. */
	@TableField(testColumn = "T", testOption = TestOption.START, nachKomma = 0)
	double sueAfAlt = DOUBLE_INIT;
	/** S�AF, neu. */
	@TableField(testColumn = "U", testOption = TestOption.START, nachKomma = 0)
	double sueAfNeu = DOUBLE_INIT;
	/** S�AF. */
	@TableField(testColumn = "V", testOption = TestOption.START, nachKomma = 0)
	double sueAf = DOUBLE_INIT;
	/** freie RfB. */
	@TableField(testColumn = "W", testOption = TestOption.START, nachKomma = 0)
	double fRfBFrei = DOUBLE_INIT;
	/** nicht festgelegte RfB. */
	@TableField(testColumn = "X", testOption = TestOption.START, nachKomma = 0)
	double nfRfB = DOUBLE_INIT;
	/** Eigenkapital. */
	@TableField(testColumn = "Y", testOption = TestOption.START, nachKomma = 0)
	double eigenkapitalFortschreibung = DOUBLE_INIT;
	/** Nachrangige Verbindlichkeiten. */
	@TableField(testColumn = "Z", testOption = TestOption.START, nachKomma = 0)
	double grNrd = DOUBLE_INIT;

	// =================================================================================
	// Spalten A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A

	// Spalten AA - AF stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "AA", suppress = true)
	private final char dummyAA = '?';
	@TableField(testColumn = "AB", suppress = true)
	private final char dummyAB = '?';
	@TableField(testColumn = "AC", suppress = true)
	private final char dummyAC = '?';
	@TableField(testColumn = "AD", suppress = true)
	private final char dummyAD = '?';
	@TableField(testColumn = "AE", suppress = true)
	private final char dummyAE = '?';
	@TableField(testColumn = "AF", suppress = true)
	private final char dummyAF = '?';
	/** HGB Latente Steuer, saldiert, Aktiva - Passiva. */
	@TableField(testColumn = "AG", testOption = TestOption.START)
	private double lsHgb = DOUBLE_INIT;
	// Spalten AH - AL stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "AH", suppress = true)
	private final char dummyAH = '?';
	@TableField(testColumn = "AI", suppress = true)
	private final char dummyAI = '?';
	@TableField(testColumn = "AJ", suppress = true)
	private final char dummyAJ = '?';
	@TableField(testColumn = "AK", suppress = true)
	private final char dummyAK = '?';
	@TableField(testColumn = "AL", suppress = true)
	private final char dummyAL = '?';
	/** Zinsen, Nachrangliche Verbindlichkeiten. */
	@TableField(testColumn = "AM", nachKomma = 0)
	double zinsen = DOUBLE_INIT;
	/** R�ckzahlung, Nachrangliche Verbindlichkeiten. */
	@TableField(testColumn = "AN", nachKomma = 0)
	double rueckZahlung = DOUBLE_INIT;
	/** Rechnungsabgrenzungsposten. */
	@TableField(testColumn = "AO", nachKomma = 0)
	double rapVT = DOUBLE_INIT;
	/** Korrigierter FI Buchwert, aktueller Bestand Klassik. */
	@TableField(testColumn = "AP", testOption = TestOption.START, nachKomma = 0)
	double bwFiAkt = DOUBLE_INIT;
	/** FI Buchwert Neuanlage im Zeitpunkt t. */
	@TableField(testColumn = "AQ", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn = DOUBLE_INIT;
	private double[] bwFiNeuAnArr;
	/** FI Restlaufzeit der Neuanlage. */
	@TableField(testColumn = "AR")
	int rlz = Integer.MAX_VALUE;
	/** Zeitpunkt der F�lligkeit der Neuanlage. */
	@TableField(testColumn = "AS")
	int zpFaelligkeit = Integer.MAX_VALUE;
	private int[] zpFaelligkeitArr;
	/** FI Buchwert gesamt, Verrechnung. */
	@TableField(testColumn = "AT", testOption = TestOption.START, nachKomma = 0)
	double bwFiVerrechnung = DOUBLE_INIT;
	/** FI Kupon der Neuanlage in t. */
	@TableField(testColumn = "AU", nachKomma = 3, percent = true)
	double kuponEsg = DOUBLE_INIT;
	private double[] kuponEsgArr;
	/** Korrigierter EQ Buchwert, Klassik. */
	@TableField(testColumn = "AV", testOption = TestOption.START, nachKomma = 0)
	double bwEq = DOUBLE_INIT;
	/** Neuanlage RE, Buchwert, Klassik. */
	@TableField(testColumn = "AW", testOption = TestOption.START, nachKomma = 0)
	double bwReNeuAnl = DOUBLE_INIT;
	/** Korrigierter RE Buchwert, Klassik. */
	@TableField(testColumn = "AX", testOption = TestOption.START, nachKomma = 0)
	double bwRe = DOUBLE_INIT;
	/** Korrigierter EQ Anschaffungswert. */
	@TableField(testColumn = "AY", testOption = TestOption.START, nachKomma = 0)
	double awEq = DOUBLE_INIT;
	/** Korrigierter RE Anschaffungswert. */
	@TableField(testColumn = "AZ", testOption = TestOption.START, nachKomma = 0)
	double awRe = DOUBLE_INIT;

	// =================================================================================
	// Spalten B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B

	/** Korrigierter EQ Marktwert, Klassik. */
	@TableField(testColumn = "BA", testOption = TestOption.START, nachKomma = 0)
	double mwEq = DOUBLE_INIT;
	/** Korrigierter RE Marktwert, Klassik. */
	@TableField(testColumn = "BB", testOption = TestOption.START, nachKomma = 0)
	double mwRe = DOUBLE_INIT;
	/** Korrigierter FI Marktwert, Klassik. */
	@TableField(testColumn = "BC", testOption = TestOption.START, nachKomma = 0)
	double mwFianfangJ = DOUBLE_INIT;
	// Spalten BD - BE stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "BD", suppress = true)
	private final char dummyBD = '?';
	@TableField(testColumn = "BE", suppress = true)
	private final char dummyBE = '?';
	/** Zielanteil FI. */
	@TableField(testColumn = "BF", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aFiZielDaa = DOUBLE_INIT;
	/** Mindestanteil FI. */
	@TableField(testColumn = "BG", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aFiMinDaa = DOUBLE_INIT;
	/** RE Zielanteil. BH, L 1. */
	@TableField(testColumn = "BH", nachKomma = 0, percent = true)
	double aReZielDaa = DOUBLE_INIT;
	/** EQ Preis. */
	@TableField(testColumn = "BI", nachKomma = 0, percent = true)
	double preisAktieEsg = DOUBLE_INIT;
	/** RE Preis. */
	@TableField(testColumn = "BJ", nachKomma = 0, percent = true)
	double preisImmoEsg = DOUBLE_INIT;
	/** FI Cash Flow aktueller Bestand. */
	@TableField(testColumn = "BK", nachKomma = 0)
	double cfFiAkt = DOUBLE_INIT;
	/** FI Kapitalertrag aktueller Bestand. */
	@TableField(testColumn = "BL", nachKomma = 0)
	double keFiAkt = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand. */
	@TableField(testColumn = "BM", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands. */
	@TableField(testColumn = "BN", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl = DOUBLE_INIT;
	/** FI Cash Flow gesamt. */
	@TableField(testColumn = "BO", testOption = TestOption.START, nachKomma = 0)
	double cfFi = DOUBLE_INIT;
	/** FI Kapitalertrag gesamt. */
	@TableField(testColumn = "BP", testOption = TestOption.START, nachKomma = 0)
	double keFi = DOUBLE_INIT;
	/** FI Buchwert gesamt. */
	@TableField(testColumn = "BQ", testOption = TestOption.START, nachKomma = 0)
	double bwFiGesamtJe = DOUBLE_INIT;
	/** FI Marktwert gesamt. */
	@TableField(testColumn = "BR", testOption = TestOption.START, nachKomma = 0)
	double fiMw = DOUBLE_INIT;
	/** EQ Marktwert vor Realisierung. */
	@TableField(testColumn = "BS", testOption = TestOption.START, nachKomma = 0)
	double mwEqVorRls = DOUBLE_INIT;
	/** RE Marktwert vor Realisierung. */
	@TableField(testColumn = "BT", testOption = TestOption.START, nachKomma = 0)
	double mwReVorRls = DOUBLE_INIT;
	/** Kapitalertrag aus Dividenden, */
	@TableField(testColumn = "BU", testOption = TestOption.START, nachKomma = 0)
	double keDiv = DOUBLE_INIT;
	/** Kapitalertrag aus Mieten, */
	@TableField(testColumn = "BV", testOption = TestOption.START, nachKomma = 0)
	double keMieten = DOUBLE_INIT;
	/** EQ Kapitalertrag aus Ab- und Zuschreibungen. */
	@TableField(testColumn = "BW", testOption = TestOption.START, nachKomma = 0)
	double keEqAbUndZuschreibung = DOUBLE_INIT;
	/** RE Kapitalertrag aus Ab- und Zuschreibungen. */
	@TableField(testColumn = "BX", testOption = TestOption.START, nachKomma = 0)
	double keReAbUndZuschreibung = DOUBLE_INIT;
	/** RE laufender Kapitalertrag. */
	@TableField(testColumn = "BY", testOption = TestOption.START, nachKomma = 0)
	double keEqLaufend = DOUBLE_INIT;
	/** EQ laufender Kapitalertrag. */
	@TableField(testColumn = "BZ", testOption = TestOption.START, nachKomma = 0)
	double keReLaufend = DOUBLE_INIT;

	// =================================================================================
	// Spalten C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C

	/** RE laufender Kapitalertrag. */
	@TableField(testColumn = "CA", testOption = TestOption.START, nachKomma = 0)
	double bwEqNachAbUndZuschreibung = DOUBLE_INIT;
	/** EQ laufender Kapitalertrag. */
	@TableField(testColumn = "CB", testOption = TestOption.START, nachKomma = 0)
	double bwReNachAbUndZuschreibung = DOUBLE_INIT;
	/** EQ BWR vor Realisierung in %. */
	@TableField(testColumn = "CC", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrEqVorRls = DOUBLE_INIT;
	/** RE BWR vor Realisierung in % */
	@TableField(testColumn = "CD", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrReVorRls = DOUBLE_INIT;
	/** EQ Ziel-BWR in %. */
	@TableField(testColumn = "CE", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrEqZiel = DOUBLE_INIT;
	/** RE Ziel-BWR in % */
	@TableField(testColumn = "CF", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrReZiel = DOUBLE_INIT;
	/** EQ Anteil zur planm��igen Realisierung in %. */
	@TableField(testColumn = "CG", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aEqRlsI = DOUBLE_INIT;
	/** RE Anteil zur planm��igen Realisierung in % */
	@TableField(testColumn = "CH", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aReRlsPlan = DOUBLE_INIT;
	/** EQ Buchwert nach planm��iger Realisierung (Senkung der EQ BWR) */
	@TableField(testColumn = "CI", testOption = TestOption.START, nachKomma = 0)
	double bwEqRlsI = DOUBLE_INIT;
	/** RE Buchwert nach planm��iger Realisierung (Senkung der RE BWR) */
	@TableField(testColumn = "CJ", testOption = TestOption.START, nachKomma = 0)
	double bwReRlsPlan = DOUBLE_INIT;
	/** Kapitalertrag aus der Realisierung der EQ-BWR. */
	@TableField(testColumn = "CK", testOption = TestOption.START, nachKomma = 0)
	double keEqRlsI = DOUBLE_INIT;
	/** Kapitalertrag aus der Realisierung der RE-BWR */
	@TableField(testColumn = "CL", testOption = TestOption.START, nachKomma = 0)
	double keReRlsPlan = DOUBLE_INIT;
	/** Kapitalertrag/-aufwand aus der Aufzinsung der Leistungen, Kosten, Beitr�gen, ZAG und Ertragsteuer. */
	@TableField(testColumn = "CM", testOption = TestOption.START, nachKomma = 0)
	double keCfAufzinsung = DOUBLE_INIT;
	/** Kapitalertrag, gesamt, nach planm��iger Realisierung. */
	@TableField(testColumn = "CN", testOption = TestOption.START, nachKomma = 0)
	double keRlsI = DOUBLE_INIT;
	/** Buchwert, gesamt nach planm��iger Realisierung. */
	@TableField(testColumn = "CO", testOption = TestOption.START, nachKomma = 0)
	double bwVorRls = DOUBLE_INIT;
	/** Marktwert, gesamt, nach planm��iger Realisierung. */
	@TableField(testColumn = "CP", testOption = TestOption.START, nachKomma = 0)
	double mwVorRls = DOUBLE_INIT;
	/** zu realisierender Anteil der Immobilien. */
	@TableField(testColumn = "CQ", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aReRls = DOUBLE_INIT;
	/** RE Cashflow aus der 2. Realisierung. */
	@TableField(testColumn = "CR", testOption = TestOption.START, nachKomma = 0)
	double cfReRls = DOUBLE_INIT;
	/** RE Kapitalertrag aus der 2. Realisierung. */
	@TableField(testColumn = "CS", testOption = TestOption.START, nachKomma = 0)
	double keReRls = DOUBLE_INIT;
	/** RE Buchwert nach der 2. Realisierung. */
	@TableField(testColumn = "CT", testOption = TestOption.START, nachKomma = 0)
	double bwReNachRls = DOUBLE_INIT;
	/** RE Marktwert nach der 2. Realisierung. */
	@TableField(testColumn = "CU", testOption = TestOption.START, nachKomma = 0)
	double mwRenachRls = DOUBLE_INIT;
	/** EQ zu realisierender Anteil der 2. Realisierung. */
	@TableField(testColumn = "CV", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aEqRlsII = DOUBLE_INIT;
	/** EQ Cashflow aus der 2. Realisierung. */
	@TableField(testColumn = "CW", testOption = TestOption.START, nachKomma = 0)
	double cfEqRlsII = DOUBLE_INIT;
	/** EQ Kapitalertrag aus der 2. Realisierung. */
	@TableField(testColumn = "CX", testOption = TestOption.START, nachKomma = 0)
	double keEqRlsII = DOUBLE_INIT;
	/** EQ Buchwert nach der 2. Realisierung */
	@TableField(testColumn = "CY", testOption = TestOption.START)
	double bwEqRlsII = DOUBLE_INIT;
	/** EQ Marktwert nach der 2. Realisierung */
	@TableField(testColumn = "CZ", testOption = TestOption.START)
	double mwEqRlsII = DOUBLE_INIT;

	// =================================================================================
	// Spalten D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D

	/** Kapitalertrag, gesamt, nach 2. Realisierung. */
	@TableField(testColumn = "DA", testOption = TestOption.START, nachKomma = 0)
	double keRlsII = DOUBLE_INIT;
	/** Buchwert, gesamt nach 2. Realisierung. */
	@TableField(testColumn = "DB", testOption = TestOption.START, nachKomma = 0)
	double bwRlsII = DOUBLE_INIT;
	/** Marktwert, gesamt nach 2. Realisierung */
	@TableField(testColumn = "DC", testOption = TestOption.START, nachKomma = 0)
	double mwRlsII = DOUBLE_INIT;
	/** Kapitalertragsdefizite aus den Vorjahren zum Verrechnen. */
	@TableField(testColumn = "DD", testOption = TestOption.START, nachKomma = 0)
	double kedVjVerrechnen = DOUBLE_INIT;
	double[] kedVjVerrechnenArr;
	/** Kapitalertragdefizit zum Verrechnen. */
	@TableField(testColumn = "DE", testOption = TestOption.START, nachKomma = 0)
	double kedVerrechnung = DOUBLE_INIT;
	double[] kedVerrechnungArr;
	/** Kapitalertrag mit Verrechnung. */
	@TableField(testColumn = "DF", testOption = TestOption.START, nachKomma = 0)
	double keVerrechnung = DOUBLE_INIT;
	/** Buchwert, gesamt, nach der aktuellen Verrechnung. */
	@TableField(testColumn = "DG", testOption = TestOption.START, nachKomma = 0)
	double bwVerechnungJe = DOUBLE_INIT;
	/** FI Buchwert gesamt nach der aktuellen Verrechnung. */
	@TableField(testColumn = "DH", testOption = TestOption.START, nachKomma = 0)
	double bwFiVerechnungJe = DOUBLE_INIT;
	/** Cash Flow vor Kreditaufnahme. */
	@TableField(testColumn = "DI", testOption = TestOption.START, nachKomma = 0)
	double cfVorKredit = DOUBLE_INIT;
	/** Zins f�r Kredit. */
	@TableField(testColumn = "DJ", nachKomma = 2, percent = true)
	double kuponEsgII = DOUBLE_INIT;
	/** Kredit zur Liquidit�tssicherstellung. */
	@TableField(testColumn = "DK", testOption = TestOption.START, nachKomma = 0)
	double kredit = DOUBLE_INIT;
	/** Kapitalaufwand f�r Kredit. */
	@TableField(testColumn = "DL", testOption = TestOption.START, nachKomma = 0)
	double ak = DOUBLE_INIT;
	/** R�ckzahlung Kredit. */
	@TableField(testColumn = "DM", testOption = TestOption.START, nachKomma = 0)
	double cfKredit = DOUBLE_INIT;
	/** Cash Flow zur Neuanlage. */
	@TableField(testColumn = "DN", testOption = TestOption.START, nachKomma = 0)
	double cfNeuAnlage = DOUBLE_INIT;
	/** Aufwendungen f�r KA. */
	@TableField(testColumn = "DO", testOption = TestOption.START, nachKomma = 0)
	double aufwendungenKa = DOUBLE_INIT;
	/** Nettoverzinsung ohne Zinsratenzuschl�ge. */
	@TableField(testColumn = "DP", testOption = TestOption.START, nachKomma = 2, percent = true)
	double nvz = DOUBLE_INIT;
	/** Zins einer 10 -j�hrigen Nullkupon-Anleihe - f�r ZZR Berechnung. DL, L 1. */
	@TableField(testColumn = "DQ", nachKomma = 2, percent = true)
	double zzrSpotEsg = DOUBLE_INIT;
	/** Referenzzinssatz. */
	@TableField(testColumn = "DR", nachKomma = 2, percent = true)
	double referenzZinssatz = DOUBLE_INIT;
	/** Referenzzinssatz 2M. */
	@TableField(testColumn = "DS", nachKomma = 2, percent = true)
	double refZins2M = DOUBLE_INIT;
	/** Mittlere Rechnungszins. */
	@TableField(testColumn = "DT", testOption = TestOption.START, nachKomma = 2, percent = true)
	double rzMittel = DOUBLE_INIT;
	/** delta-ZZR UEB, Altbestand. */
	@TableField(testColumn = "DU", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebAlt = DOUBLE_INIT;
	/** delta-ZZR UEB, Neubestand.. */
	@TableField(testColumn = "DV", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebNeu = DOUBLE_INIT;
	/** Delta ZZR - N�B. */
	@TableField(testColumn = "DW", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrNueb = DOUBLE_INIT;
	/** rmZ UEB Altbestand. */
	@TableField(testColumn = "DX", testOption = TestOption.START, nachKomma = 0)
	double rmzUebAlt = DOUBLE_INIT;
	/** rmZ UEB Neubestand. */
	@TableField(testColumn = "DY", testOption = TestOption.START, nachKomma = 0)
	double rmzUebNeu = DOUBLE_INIT;
	/** rmZ N�B. */
	@TableField(testColumn = "DZ", testOption = TestOption.START, nachKomma = 0)
	double rmzNueb = DOUBLE_INIT;

	// =================================================================================
	// Spalten E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E

	/** rmZ UEB Gesamt Altbestand. */
	@TableField(testColumn = "EA", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtAlt = DOUBLE_INIT;
	/** rmZ UEB Gesamt Neubestand. */
	@TableField(testColumn = "EB", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtNeu = DOUBLE_INIT;
	/** rmZ Gesamt N�B. */
	@TableField(testColumn = "EC", testOption = TestOption.START, nachKomma = 0)
	double rmzNuebGesamt = DOUBLE_INIT;
	/** �briges Ergebnis �B Altbestand stochastisch. */
	@TableField(testColumn = "ED", testOption = TestOption.START, nachKomma = 0)
	double ueEalt = DOUBLE_INIT;
	/** �briges Ergebnis �B Neubestand stochastisch. */
	@TableField(testColumn = "EE", testOption = TestOption.START, nachKomma = 0)
	double ueEneu = DOUBLE_INIT;
	/** �briges Ergebnis N�B stochastisch. */
	@TableField(testColumn = "EF", testOption = TestOption.START, nachKomma = 0)
	double ueEnueb = DOUBLE_INIT;
	/** �briges Ergebnis UEB, Bestand (ohne GCR) Altbestand stochastisch. */
	@TableField(testColumn = "EG", testOption = TestOption.START, nachKomma = 0)
	double ueEaltNoGcr = DOUBLE_INIT;
	/** �briges Ergebnis UEB, Bestand (ohne GCR) Neubestand stochastisch. */
	@TableField(testColumn = "EH", testOption = TestOption.START, nachKomma = 0)
	double ueEneuNoGcr = DOUBLE_INIT;
	/** GCR �briges Ergebnis an Neugesch�ft - �EB. */
	@TableField(testColumn = "EI", testOption = TestOption.START, nachKomma = 0)
	double gcrUeB = DOUBLE_INIT;
	/** Risikoergebnis UEB, alt, stochastisch. */
	@TableField(testColumn = "EJ", testOption = TestOption.START, nachKomma = 0)
	double reAlt = DOUBLE_INIT;
	/** Risikoergebnis UEB, neu, stochastisch. */
	@TableField(testColumn = "EK", testOption = TestOption.START, nachKomma = 0)
	double reNeu = DOUBLE_INIT;
	/** Risikoergebnis - N�B, stochastisch. */
	@TableField(testColumn = "EL", testOption = TestOption.START, nachKomma = 0)
	double risikoUebStochAgg = DOUBLE_INIT;
	/** J�_Zielerh�hung. */
	@TableField(testColumn = "EM", testOption = TestOption.START, nachKomma = 0)
	double jUeZielerhoehung = DOUBLE_INIT;
	/** J�Ziel. */
	@TableField(testColumn = "EN", testOption = TestOption.START, nachKomma = 0)
	double jueZiel = DOUBLE_INIT;
	/** Verlustvortrag. */
	@TableField(testColumn = "EO", testOption = TestOption.START, nachKomma = 0)
	double vv = DOUBLE_INIT;
	/** Mindeskapitalertrag. */
	@TableField(testColumn = "EP", testOption = TestOption.START, nachKomma = 0)
	double mindestkapitalertragLvrgAltNeu = DOUBLE_INIT;
	/** Roh�berschuss. */
	@TableField(testColumn = "EQ", testOption = TestOption.START, nachKomma = 0)
	double rohueb = DOUBLE_INIT;
	double[] rohuebArr;
	/** Mindestzuf�hrung. */
	@TableField(testColumn = "ER", testOption = TestOption.START, nachKomma = 0)
	double mindZf = DOUBLE_INIT;
	/** Deckungsr�ckstellung vor Deklaration. */
	@TableField(testColumn = "ES", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklAgg = DOUBLE_INIT;
	/** anrechenbare Kapitalertr�ge. */
	@TableField(testColumn = "ET", testOption = TestOption.START, nachKomma = 0)
	double kapitalertragAnrechenbar = DOUBLE_INIT;
	/** Mindestzuf�hrung K�rzungskonto. */
	@TableField(testColumn = "EU", testOption = TestOption.START, nachKomma = 0)
	double mindZfKk = DOUBLE_INIT;
	/** Mindestzuf�hrung gesamt. */
	@TableField(testColumn = "EV", testOption = TestOption.START, nachKomma = 0)
	double mindZfGes = DOUBLE_INIT;
	/** J�. */
	@TableField(testColumn = "EW", testOption = TestOption.START, nachKomma = 0)
	double jue = DOUBLE_INIT;
	/** RfB_Zuf. */
	@TableField(testColumn = "EX", testOption = TestOption.START, nachKomma = 0)
	double rfBZuf = DOUBLE_INIT;
	private double[] rfBZufArr;
	/** nfRfB_56b-Entnahmen. */
	@TableField(testColumn = "EY", testOption = TestOption.START, nachKomma = 0)
	double nfRfB56b = DOUBLE_INIT;
	private double[] nfRfB56bArr;
	/** ZAG, festgelegt. */
	@TableField(testColumn = "EZ", testOption = TestOption.START, nachKomma = 0)
	double zag = DOUBLE_INIT;
	/** ZAG, f�llig. */

	// =================================================================================
	// Spalten F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F

	@TableField(testColumn = "FA", testOption = TestOption.START, nachKomma = 0)
	double zagFaellig = DOUBLE_INIT;
	/** ZAG, Endzahlung. */
	@TableField(testColumn = "FB", testOption = TestOption.START, nachKomma = 0)
	double zagEndzahlung = DOUBLE_INIT;
	/** Steuer, festgelegt. */
	@TableField(testColumn = "FC", testOption = TestOption.START, nachKomma = 0)
	double ertragssteuer = DOUBLE_INIT;
	/** Steuer, festgelegt, nach der LS-Korrektur. */
	@TableField(testColumn = "FD", testOption = TestOption.START)
	double ertragsSteuerLs = DOUBLE_INIT;
	/** Mittlere Zuf�hrung zur RfB (gemittelt �ber die letzten M Jahre). */
	@TableField(testColumn = "FE", testOption = TestOption.START, nachKomma = 2, percent = true)
	double mittlRfBZufuehrung = DOUBLE_INIT;
	/** fRfB_min. */
	@TableField(testColumn = "FF", testOption = TestOption.START, nachKomma = 0)
	double fRfBMin = DOUBLE_INIT;
	/** fRfB_max. */
	@TableField(testColumn = "FG", testOption = TestOption.START, nachKomma = 0)
	double fRfBMax = DOUBLE_INIT;
	/** Zieldeklaration. */
	@TableField(testColumn = "FH", testOption = TestOption.START, nachKomma = 0)
	double zielDeklaration = DOUBLE_INIT;
	/** S�AF 56b Entnahme */
	@TableField(testColumn = "FI", testOption = TestOption.START, nachKomma = 0)
	double sUeAf56bEntnahme = DOUBLE_INIT;
	private double sUeAf56bEntnahmeArr[];
	/** fRfB 56b Entnahme. */
	@TableField(testColumn = "FJ", testOption = TestOption.START, nachKomma = 0)
	double fRfB56bEntnahme = DOUBLE_INIT;
	private double[] fRfB56bEntnahmeArr;
	/** fRfB �berlauf. */
	@TableField(testColumn = "FK", testOption = TestOption.START, nachKomma = 0)
	double fRfBUeberlauf = DOUBLE_INIT;
	/** fRfB vor Endzahlung. */
	@TableField(testColumn = "FL", testOption = TestOption.START, nachKomma = 0)
	double fRfBVorEndzahlung = DOUBLE_INIT;
	/** Deklaration. */
	@TableField(testColumn = "FM", testOption = TestOption.START, nachKomma = 0)
	double dekl = DOUBLE_INIT;
	/** Deklaration. */
	@TableField(testColumn = "FN", testOption = TestOption.START, nachKomma = 0)
	double deklZins = DOUBLE_INIT;
	/** Deklaration, rest */
	@TableField(testColumn = "FO", testOption = TestOption.START, nachKomma = 0)
	double deklRest = DOUBLE_INIT;
	/** Gesamtzins. */
	@TableField(testColumn = "FP", testOption = TestOption.START, nachKomma = 2, percent = true)
	double vzGes = DOUBLE_INIT;
	/** Deckungsr�ckstellung vor Deklaration - �B. */
	@TableField(testColumn = "FQ", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklUebAgg = DOUBLE_INIT;
	/** Deckungsr�ckstellung �B, Lockin, Altbestand */
	@TableField(testColumn = "FR", testOption = TestOption.START, nachKomma = 0)
	double drLockInAlt = DOUBLE_INIT;
	/** Deckungsr�ckstellung �B, Lockin, Neubestand */
	@TableField(testColumn = "FS", testOption = TestOption.START, nachKomma = 0)
	double drLockInNeu = DOUBLE_INIT;
	/** Deckungsr�ckstellung Lock-In - �B. */
	@TableField(testColumn = "FT", testOption = TestOption.START, nachKomma = 0)
	double drLockInAggWennLoB = DOUBLE_INIT;
	private double[] drLockInAggWennLoBArr;
	/** Deckungsr�ckstellung, Gesamtbestand Lock-In. */
	@TableField(testColumn = "FU", testOption = TestOption.START, nachKomma = 0)
	double drLockInAgg = DOUBLE_INIT;
	/** Deckungsr�ckstellung Gesamt - N�B. FQ, */
	@TableField(testColumn = "FV", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklNuebAgg = DOUBLE_INIT;
	/** Deckungsr�ckstellung gesamt. */
	@TableField(testColumn = "FW", testOption = TestOption.START, nachKomma = 0)
	double drGesAgg = DOUBLE_INIT;
	/** LBW gar. */
	@TableField(testColumn = "FX", testOption = TestOption.START, nachKomma = 0)
	double lbwGarAgg = DOUBLE_INIT;
	/** S�AF_Zuf durch RfB �berlauf. */
	@TableField(testColumn = "FY", testOption = TestOption.START, nachKomma = 0)
	double sueAFZufFRfBUeberlaufAgg = DOUBLE_INIT;
	/** S�AF_Zuf. */
	@TableField(testColumn = "FZ", testOption = TestOption.START, nachKomma = 0)
	double sueAFZufAgg = DOUBLE_INIT;
	/** S�AF Entnahme. */

	// =================================================================================
	// Spalten G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G

	@TableField(testColumn = "GA", testOption = TestOption.START, nachKomma = 0)
	double sueAFEntnahmeAgg = DOUBLE_INIT;
	/** Barauszahlung */
	@TableField(testColumn = "GB", testOption = TestOption.START, nachKomma = 0)
	double barAgg = DOUBLE_INIT;
	/** Lock-In (Garantierte Leistung. */
	@TableField(testColumn = "GC", testOption = TestOption.START, nachKomma = 0)
	double lockInAgg = DOUBLE_INIT;
	/** s�mtliche garantierte Leistungen. FY. */
	@TableField(testColumn = "GD", nachKomma = 0)
	double lGarAgg = DOUBLE_INIT;
	/** Garantierten Leistungen inkl. Lock-In. */
	@TableField(testColumn = "GE", testOption = TestOption.START, nachKomma = 0)
	double lGarStochAgg = DOUBLE_INIT;
	/** Leistungen Gesamt. */
	@TableField(testColumn = "GF", testOption = TestOption.START, nachKomma = 0)
	double lGesAgg = DOUBLE_INIT;
	/** Beitr�ge, stochastisch. */
	@TableField(testColumn = "GG", testOption = TestOption.START, nachKomma = 0)
	double bStochAgg = DOUBLE_INIT;
	/** Kosten, stochastisch. */
	@TableField(testColumn = "GH", testOption = TestOption.START, nachKomma = 0)
	double kStochAgg = DOUBLE_INIT;
	/** Cashflow EVU -> RVU, stochastisch. */
	@TableField(testColumn = "GI", testOption = TestOption.START, nachKomma = 0)
	double cfRvstochAgg = DOUBLE_INIT;
	/** Summe �ber Zinsratenzuschlag, stochastisch. */
	@TableField(testColumn = "GJ", testOption = TestOption.START, nachKomma = 0)
	double ziRaZuStochAgg = DOUBLE_INIT;
	/** Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. */
	@TableField(testColumn = "GK", nachKomma = 0)
	double cashflowGesamt = DOUBLE_INIT;
	/** Diskontfunktion. */
	@TableField(testColumn = "GL", nachKomma = 2, percent = true)
	double diskontEsg = DOUBLE_INIT;
	/** Mittlerer j�hrlicher Zins (f�r Aufzinsung). */
	@TableField(testColumn = "GM", nachKomma = 3, percent = true)
	double jaehrlZinsEsg = DOUBLE_INIT;
	/** Leistungen Gesamt, , aufgezinst. */
	@TableField(testColumn = "GN", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungGesamt = DOUBLE_INIT;
	/** Beitr�ge, aufgezinst. */
	@TableField(testColumn = "GO", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungBeitraege = DOUBLE_INIT;
	/** Kosten, aufgezinst. */
	@TableField(testColumn = "GP", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungKosten = DOUBLE_INIT;
	/** Leistungen durch Endzahlung. */
	@TableField(testColumn = "GQ", testOption = TestOption.START, nachKomma = 0)
	double endZahlungAgg = DOUBLE_INIT;
	/** ZAG, aufgezinst. */
	@TableField(testColumn = "GR", testOption = TestOption.START, nachKomma = 0)
	double zagAufgezinst = DOUBLE_INIT;
	/** Steuer aus dem Vorjahr ausgezahlt, inklusive latente HGB-Steuer, aufgezinst. */
	@TableField(testColumn = "GS", testOption = TestOption.START, nachKomma = 0)
	double steuerVjAufgezinst = DOUBLE_INIT;
	/** CF EVU -> RVU aufgezinst. */
	@TableField(testColumn = "GT", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungcfEvuRvu = DOUBLE_INIT;
	/** Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. */
	@TableField(testColumn = "GU", testOption = TestOption.START, nachKomma = 0)
	double cashflowAufgezinst = DOUBLE_INIT;
	/** Cash-out-flows, gesamt, au�er KA-Cashflows. */
	@TableField(testColumn = "GV", testOption = TestOption.START, nachKomma = 0)
	double cfOhneKa = DOUBLE_INIT;
	/** MW VT. Entspricht auch VT Klassik MW!B. */
	@TableField(testColumn = "GW", testOption = TestOption.START, nachKomma = 0)
	double mwVt = DOUBLE_INIT;
	/** Passive Reserven. */
	@TableField(testColumn = "GX", testOption = TestOption.START, nachKomma = 0)
	double bwrPas = DOUBLE_INIT;
	/** �berschussfonds: Deklaration (fRfB) */
	@TableField(testColumn = "GY", testOption = TestOption.START, nachKomma = 0)
	double deklsurplusfRfB = DOUBLE_INIT;
	private double[] deklsurplusfRfBarr;
	/** �berschussfonds: S�A-Zuf�hrungen */
	@TableField(testColumn = "GZ", testOption = TestOption.START, nachKomma = 0)
	double sUeAfZufSf = DOUBLE_INIT;
	private double[] sUeAfZufSfArr;

	// =================================================================================
	// Spalten H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H

	/** �berschussfonds: S�A-Entnahmen */
	@TableField(testColumn = "HA", testOption = TestOption.START, nachKomma = 0)
	double sUeAfEntSf = DOUBLE_INIT;
	private double[] sUeAfEntSfArr;
	/** �berschussfonds: Barauszahlung */
	@TableField(testColumn = "HB", testOption = TestOption.START, nachKomma = 0)
	double barSf = DOUBLE_INIT;
	/** �berschussfonds: Lock-in */
	@TableField(testColumn = "HC", testOption = TestOption.START, nachKomma = 0)
	double lockInSf = DOUBLE_INIT;
	/** LE durch SF, aggr. GG, Rekursiv3. */
	/** �berschussfonds: Leistungserh�hung, aggr */
	@TableField(testColumn = "HD", testOption = TestOption.START, nachKomma = 5, percent = true)
	double leAggrSf = DOUBLE_INIT;
	/** �berschussfonds: Cashflow gesamt */
	@TableField(testColumn = "HE", testOption = TestOption.START, nachKomma = 0)
	double cashflowSf = DOUBLE_INIT;
	/** �berschussfonds: Delta Leistung, gar vs. Leistung, gesamt. */
	@TableField(testColumn = "HF", testOption = TestOption.START, nachKomma = 0)
	double deltaLAgg = DOUBLE_INIT;
	/** EPIFP: Deckungsr�ckstellung aus den k�nftigen Pr�mien. */
	@TableField(testColumn = "HG", nachKomma = 0)
	double drstKPAgg = DOUBLE_INIT;
	/** EPIFP: Roh�berschuss aus den k�nftigen Pr�mien, nur positive Beitr�ge. */
	@TableField(testColumn = "HH", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpK = DOUBLE_INIT;
	/** EPIFP: Roh�berschuss aus den k�nftigen Pr�mien, nur negative Beitr�ge. */
	@TableField(testColumn = "HI", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpN = DOUBLE_INIT;
	/** EPIFP: Roh�berschuss , nur positive Beitr�ge. */
	@TableField(testColumn = "HJ", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpP = DOUBLE_INIT;
	/** EPIFP: Jahres�berschuss geschl�sselt auf k�nftige Pr�mien. */
	@TableField(testColumn = "HK", testOption = TestOption.START, nachKomma = 0)
	double jueVnKp = DOUBLE_INIT;
	/** Z�B: Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung. */
	@TableField(testColumn = "HL", testOption = TestOption.START, nachKomma = 0)
	double zuebCashflowAgg = DOUBLE_INIT;
	/** Z�B: Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung. */
	@TableField(testColumn = "HM", testOption = TestOption.START, nachKomma = 0)
	double optionenCashflowAgg = DOUBLE_INIT;
	/** Summe der positiven Beitr�ge zum Roh�berschuss, nur RE und �E. */
	@TableField(testColumn = "HN", testOption = TestOption.START, nachKomma = 0)
	double beitragRohUebAgg = DOUBLE_INIT;
	/** Anteil LoBs, deren KA-Kosten gestresst sind. */
	@TableField(testColumn = "HO", testOption = TestOption.START, nachKomma = 0)
	double anteilLobsKaStress = DOUBLE_INIT;
	/** Zins einer 10 -j�hrigen Nullkupon-Anleihe - f�r Kundenverhalten. */
	@TableField(testColumn = "HP", nachKomma = 2, percent = true)
	double spotVnVerhaltenEsg = DOUBLE_INIT;

	/**
	 * Konstruktion einer neuen Agg-Zeile.
	 * 
	 * @param berechnung
	 *            zugrunde liegende Berechnung
	 * @param rv
	 *            soll RV gerechnet werden?
	 * @param rzgZeilen
	 *            zugeh�rige rzg
	 * @param flvZeilen
	 *            zu dieser Zeit geh�rende Flv-Zeilen
	 * @param vg
	 *            die chronologische Vorg�ngerzeile
	 */
	public AggZeile(final Berechnung berechnung, final List<RzgZeile> rzgZeilen, final List<FlvZeile> flvZeilen,
			final AggZeile vg) {
		this.berechnung = berechnung;
		this.rzgZeilen = rzgZeilen;
		this.flvZeilen = flvZeilen;
		this.vg = vg;

		// Zeit muss konstant sein f�r alle Aggregationen:
		szenario = berechnung.szenarioName;
		szenarioId = berechnung.szenarioId;
		this.zeit = rzgZeilen.get(0).zeit;

		// Summation der rohen Aggregationen aus den rzg-LoB's
		double kAgg = 0.0;
		double bAgg = 0.0;
		double lTodAgg = 0.0;
		double kaAgg = 0.0;
		double lSonstErlAgg = 0.0;
		double rkAgg = 0.0;
		double rUeAgg = 0.0;
		double kUeAgg = 0.0;
		double cfEvuRvu = 0.0;
		double zinsratenzuschlaegeAgg = 0.0;
		double rmzEingabeAgg = 0.0;
		double hgbDrAgg = 0.0;

		for (RzgZeile rzgZeile : rzgZeilen) {
			kAgg += rzgZeile.kosten;
			bAgg += rzgZeile.praemien;
			lTodAgg += rzgZeile.lTod;
			kaAgg += rzgZeile.lKa;
			lSonstErlAgg += rzgZeile.sonstigeErlebensfallLeistungen;
			rkAgg += rzgZeile.lRkw;
			rUeAgg += rzgZeile.risikoErgebnis;
			kUeAgg += rzgZeile.uebrigesErgebnis;
			cfEvuRvu += rzgZeile.cfEvuRvu;
			zinsratenzuschlaegeAgg += rzgZeile.zinsratenZuschlag;
			rmzEingabeAgg += rzgZeile.zinsaufwand;
			hgbDrAgg += rzgZeile.drDet;
		}

		this.kAgg = kAgg;
		this.bAgg = bAgg;
		this.lTodAgg = lTodAgg;
		this.kaAgg = kaAgg;
		this.lSonstErlAgg = lSonstErlAgg;
		this.rkAgg = rkAgg;
		this.rueAgg = rUeAgg;
		this.kueAgg = kUeAgg;
		this.cfEvuRvu = cfEvuRvu;
		this.zinsratenzuschlaegeAgg = zinsratenzuschlaegeAgg;
		this.rmzEingabeAgg = rmzEingabeAgg;
		this.hgbDrAgg = hgbDrAgg;

		if (zeit == 0) {
			fRfBFrei = berechnung.hgbBilanzdaten.getFreieRfbBuchwert();
			eigenkapitalFortschreibung = berechnung.hgbBilanzdaten.getEkBuchwert();
			grNrd = berechnung.hgbBilanzdaten.getGrNrdBuchwert();
			lsHgb = berechnung.hgbBilanzdaten.getLatSteuerAktiva() - berechnung.hgbBilanzdaten.getLatSteuerPassiva();
		}

		if (zeit > 0) {
			this.zinsen = berechnung.genussNachrang.get(zeit).getZinsen();
			this.rueckZahlung = berechnung.genussNachrang.get(zeit).getRueckzahlung();
		}

		// f�r die Berechnung der Ausfallwahrscheinlichkeit wird FI Ausfall!$U$2
		// ben�tigt, und daf�r werden die beiden folgenden Werte f�r Zeit = 1
		// schon vorab berechnet.
		if (zeit == 1) {
			// Tempor�re vorab-Berechnung f�r die Bestimmung der Ausfallwahrscheinlichkeit
			final double aq5 = KaModellierung.bwFiNeuAn(vg.bwVerechnungJe, vg.bwFiVerechnungJe, vg.cfNeuAnlage,
					vg.aFiZielDaa, rlz, vg.kredit, zeit);
			double aw5 = KaModellierung.bwReNeuAnl(vg.aReZielDaa, vg.bwVerechnungJe, vg.bwReNachRls, vg.cfNeuAnlage,
					bwFiNeuAn, zeit, berechnung.laengeProjektionDr);
			bwEq = KaModellierung.bwEq(0.0, 0.0, aq5, aw5, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.bwSaSp, berechnung.mwSaSp);
			bwRe = KaModellierung.bwRe(0.0, aw5, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp, berechnung.mwSaSp);

			// Tempor�re vorab-Berechnung f�r die Bestimmung der Ausfallwahrscheinlichkeit
			mwEq = KaModellierung.mwEq(vg.mwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
					berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
			mwRe = KaModellierung.mwRe(vg.mwRenachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp, berechnung.mwSaSp);
			mwFianfangJ = KaModellierung.mwFiAnfangJ(vg.fiMw, bwFiNeuAn, zeit, berechnung.mwFi0, berechnung.mwEq0,
					berechnung.mwRe0, mwEq, mwRe, berechnung.mwSaSp);
		}
	}

	/**
	 * Setze den chronologischen Nachfolger dieser Zeile.
	 * 
	 * @param nf
	 *            der Nachfolger
	 */
	public void setNachfolger(final AggZeile nf) {
		this.nf = nf;
	}

	/**
	 * Durchf�hrung der Berechnung auf der ersten Ebene.
	 * 
	 * @param pfad
	 *            Nummer des Pfades, der gerechnet werden soll
	 */
	public void berechnungLevel01(final int pfad) {
		kuponEsgArr = fillArr(agg -> agg.kuponEsg, kuponEsgArr);
		zpFaelligkeitArr = fillArrInt(agg -> agg.zpFaelligkeit, zpFaelligkeitArr);

		spotVnVerhaltenEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).getSpotRlz(10);

		aReZielDaa = berechnung.getZeitunabhManReg().getZielAnteilARe();
		if (zeit != 0) {
			cfFiAkt = berechnung.getFiAusfall(zeit).cfFimitAusfallJahresende;
			keFiAkt = berechnung.getFiAusfall(zeit).keaktBestandJeR;
			rlz = KaModellierung.rlz(berechnung.getZeitabhManReg().get(zeit).getRlzNeuAnl(),
					berechnung.laengeProjektionDr, zeit);
			zpFaelligkeit = KaModellierung.zpFaelligkeit(zeit, rlz);
		} else {
			rlz = 0;
			zpFaelligkeit = 0;
		}
		zpFaelligkeitArr[zeit] = zpFaelligkeit;
		if (zeit != berechnung.zeitHorizont) {
			rapVT = berechnung.getFiAusfall(zeit + 1).rapZinsen;
		} else {
			rapVT = 0.0;
		}

		if (zeit == 0) {
			kuponEsg = 0.0;
		} else {
			kuponEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz);
		}
		kuponEsgArr[zeit] = kuponEsg;

		kuponEsgII = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).getKuponRlz(1);
		diskontEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).diskontFunktion;

		if (zeit > 0) {
			jaehrlZinsEsg = EsgFormeln.jaehrlZinsEsg(vg.diskontEsg, diskontEsg);
		}

		zzrSpotEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).spotrate10jZZR;

		{
			final ReferenzZinssatz rz = berechnung.referenzZinssatz;
			AggZeile az = this;
			double[] zinsArr = new double[10];
			double[] zzrSpotEsgArr = new double[10];
			for (int i = 0; i <= 9; ++i) {
				if (zeit - i > 0) {
					zzrSpotEsgArr[i] = az.zzrSpotEsg;
				} else {
					zinsArr[i] = rz.getZins(zeit - i);
				}
				if (az != null)
					az = az.vg;
			}
			referenzZinssatz = Rohueberschuss.referenzZinssatz(zeit, zzrSpotEsgArr, zinsArr);
		}
		if (zeit > 0) {
			refZins2M = Rohueberschuss.refZins2M(vg.referenzZinssatz, referenzZinssatz, vg.refZins2M, zzrSpotEsg, zeit,
					berechnung.getZeitunabhManReg().getParameter2M());
		}
		preisAktieEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).aktien;
		preisImmoEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).immobilien;
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 1.
	 * 
	 * @param pfad
	 *            der gerechnet werden soll
	 */
	public void zeitRekursionL01(final int pfad) {
		deklsurplusfRfBarr = fillArr(agg -> agg.deklsurplusfRfB, deklsurplusfRfBarr);
		sUeAf56bEntnahmeArr = fillArr(agg -> agg.sUeAf56bEntnahme, sUeAf56bEntnahmeArr);
		fRfB56bEntnahmeArr = fillArr(agg -> agg.fRfB56bEntnahme, fRfB56bEntnahmeArr);
		bwFiNeuAnArr = fillArr(agg -> agg.bwFiNeuAn, bwFiNeuAnArr);
		kuponEsgArr[zeit] = kuponEsg;
		kedVerrechnungArr = fillArr(agg -> agg.kedVerrechnung, kedVerrechnungArr);
		kedVjVerrechnenArr = fillArr(agg -> agg.kedVjVerrechnen, kedVjVerrechnenArr);
		rohuebArr = fillArr(agg -> agg.rohueb, rohuebArr);
		rfBZufArr = fillArr(agg -> agg.rfBZuf, rfBZufArr);
		nfRfB56bArr = fillArr(agg -> agg.nfRfB56b, nfRfB56bArr);
		sUeAfZufSfArr = fillArr(agg -> agg.sUeAfZufSf, sUeAfZufSfArr);
		sUeAfEntSfArr = fillArr(agg -> agg.sUeAfEntSf, sUeAfEntSfArr);
		drLockInAggWennLoBArr = fillArr(agg -> agg.drLockInAggWennLoB, drLockInAggWennLoBArr);

		cfFis = new double[berechnung.zeitHorizont + 1];
		cfFiZeitschrittig = new double[berechnung.zeitHorizont + 1];

		if (zeit == 0) {
			// hier kann FI-CFs und FI-MW gerechnet werden (werte aus FiAusfall)!
			cfFis[0] = 0.0;
			cfFiZeitschrittig[0] = 0.0;
			for (int rlz = 1; rlz <= berechnung.zeitHorizont; ++rlz) {
				// Achtung: in Excel wird mit agg!$AR4 als zweitem Parameter gerechnet
				// dies ist aber leer, deshalb nehmen wir hier berechnung.zeitHorizont
				cfFis[rlz] = KaModellierung.cfFis(rlz, berechnung.zeitHorizont, bwFiNeuAn, kuponEsg, zeit,
						berechnung.getFiAusfall(rlz).cfFimitAusfallJahresende);

				cfFiZeitschrittig[rlz] = cfFis[rlz];
			}

			PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);
			mwFiJahresende = KaModellierung.mwFiJahresende(rlz, cfFiZeitschrittig, pfadZeile, pfad, zeit,
					berechnung.bwAktivaFi.getMaxZeitCashflowFi(), berechnung.maxRlzNeuAnl);

		}

		if (zeit > 0) {
			lGarAgg = 0.0;
			drstKPAgg = 0.0;
			for (RzgZeile z : rzgZeilen) {
				lGarAgg += Functions.nanZero(z.lGarantiertDet);
				drstKPAgg += Functions.nanZero(z.drstKp);
			}

			cashflowGesamt = 0.0;
			if (flvZeilen != null) {
				for (FlvZeile flvZeile : flvZeilen) {
					if (flvZeile.uebNueb.equals("UEB")) {
						cashflowGesamt += flvZeile.uebrigesErgebnis;
						cashflowGesamt += flvZeile.risikoErgebnis;
					} else if (flvZeile.uebNueb.equals("NUEB")) {
						cashflowGesamt += flvZeile.uebrigesErgebnis;
						cashflowGesamt += flvZeile.risikoErgebnis;
					}
				}
			}

			bwFiNeuAn = KaModellierung.bwFiNeuAn(vg.bwVerechnungJe, vg.bwFiVerechnungJe, vg.cfNeuAnlage, vg.aFiZielDaa,
					rlz, vg.kredit, zeit);
			bwFiNeuAnArr[zeit] = bwFiNeuAn;

			// jetzt kann CF_FI_s berechnet werten:
			for (int i = 0; i <= berechnung.zeitHorizont; ++i) {
				cfFis[i] = KaModellierung.cfFis(i, rlz, bwFiNeuAn, kuponEsg, zeit, 0.0);
			}

			for (int i = 0; i < berechnung.zeitHorizont; ++i) {
				cfFiZeitschrittig[i] = cfFis[i] + vg.cfFiZeitschrittig[i + 1];
			}
			cfFiZeitschrittig[berechnung.zeitHorizont] = cfFis[berechnung.zeitHorizont];

			final PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);

			mwFiJahresende = KaModellierung.mwFiJahresende(rlz, cfFiZeitschrittig, pfadZeile, pfad, zeit,
					berechnung.bwAktivaFi.getMaxZeitCashflowFi(),
					berechnung.getZeitabhManReg().get(zeit).getRlzNeuAnl());

			fiMw = mwFiJahresende;

			// ab hier neu in 3.0:
			bwReNeuAnl = KaModellierung.bwReNeuAnl(vg.aReZielDaa, vg.bwVerechnungJe, vg.bwReNachRls, vg.cfNeuAnlage,
					bwFiNeuAn, zeit, berechnung.laengeProjektionDr);
			awEq = KaModellierung.awEq(vg.aEqRlsI, vg.awEq, vg.mwEqVorRls, vg.cfEqRlsII, vg.cfNeuAnlage, bwFiNeuAn,
					bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp,
					berechnung.mwSaSp);
			awRe = KaModellierung.awRe(bwReNeuAnl, vg.aReRls, vg.awRe, zeit, berechnung.eqBuchwertKlassic,
					berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
					berechnung.mwSaSp, vg.aReRlsPlan, vg.mwReVorRls);
			if (zeit != 1) {
				// Zeit == 1 wurde bereits im Konstruktor gerechnet!
				bwEq = KaModellierung.bwEq(vg.bwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
						berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
				bwRe = KaModellierung.bwRe(vg.bwReNachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic,
						berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
						berechnung.mwSaSp);
				mwEq = KaModellierung.mwEq(vg.mwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
						berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
				mwRe = KaModellierung.mwRe(vg.mwRenachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic,
						berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
						berechnung.mwSaSp);
				// Achtung: hier werden zwei Parameter immer aus Zeit 1 genommen!
				mwFianfangJ = KaModellierung.mwFiAnfangJ(vg.fiMw, bwFiNeuAn, zeit, berechnung.mwFi0, berechnung.mwEq0,
						berechnung.mwRe0, berechnung.getAggZeile(1).mwEq, berechnung.getAggZeile(1).mwRe,
						berechnung.mwSaSp);
			}

			mwEqVorRls = KaModellierung.mwEqVorRls(vg.preisAktieEsg, preisAktieEsg, mwEq);
			mwReVorRls = KaModellierung.mwReVorRls(vg.preisImmoEsg, preisImmoEsg, mwRe);
			keDiv = KaModellierung.keDiv(pfadZeile.dividenden, vg.preisAktieEsg, mwEq);
			keMieten = KaModellierung.keMieten(zeit, pfadZeile.mieten, vg.preisImmoEsg, mwRe, berechnung.arapMieten);
			keEqAbUndZuschreibung = KaModellierung.keEqAbUndZuschreibung(mwEqVorRls, bwEq, awEq,
					berechnung.getZeitunabhManReg().getAbschreibungsGrenzeRW());
			keReAbUndZuschreibung = KaModellierung.keReAbUndZuschreibung(mwReVorRls, bwRe, awRe,
					berechnung.getZeitunabhManReg().getAbschreibungsGrenzeRW());
			keEqLaufend = KaModellierung.keEqLaufend(keDiv, keEqAbUndZuschreibung);
			keReLaufend = KaModellierung.keReLaufend(keMieten, keReAbUndZuschreibung);
			bwEqNachAbUndZuschreibung = KaModellierung.bwEqNachAbUndZuschreibung(bwEq, keEqAbUndZuschreibung);
			bwReNachAbUndZuschreibung = KaModellierung.bwReNachAbUndZuschreibung(bwRe, keReAbUndZuschreibung);
			bwrEqVorRls = KaModellierung.bwrEqVorRls(mwEqVorRls, bwEqNachAbUndZuschreibung);
			bwrReVorRls = KaModellierung.bwrReVorRls(mwReVorRls, bwReNachAbUndZuschreibung);

			bwrEqZiel = KaModellierung.bwrEqZiel(bwrEqVorRls, berechnung.getZeitunabhManReg().getBwrGrenzeEq(),
					berechnung.getZeitunabhManReg().getAnteilEqY());
			bwrReZiel = KaModellierung.bwrReZiel(bwrReVorRls, berechnung.getZeitunabhManReg().getBwrGrenzeRe(),
					berechnung.getZeitunabhManReg().getAnteilReY());
			aEqRlsI = KaModellierung.aEqRlsI(bwrEqVorRls, bwrEqZiel, berechnung.getZeitunabhManReg().getBwrGrenzeEq());
			aReRlsPlan = KaModellierung.aReRlsPlan(bwrReVorRls, bwrReZiel,
					berechnung.getZeitunabhManReg().getBwrGrenzeRe());
			bwEqRlsI = KaModellierung.bwEqRlsI(mwEqVorRls, bwEqNachAbUndZuschreibung, aEqRlsI);
			bwReRlsPlan = KaModellierung.bwReRlsPlan(mwReVorRls, bwReNachAbUndZuschreibung, aReRlsPlan);

			keEqRlsI = KaModellierung.keEqRlsI(mwEqVorRls, bwEqNachAbUndZuschreibung, aEqRlsI);
			keReRlsPlan = KaModellierung.keReRlsPlan(mwReVorRls, bwReNachAbUndZuschreibung, aReRlsPlan);

			bwFiAkt = KaModellierung.bwFiAkt(vg.zeit, vg.bwFiAkt, vg.cfFiAkt, vg.keFiAkt, berechnung.fiBuchwertBestand,
					berechnung.eqBuchwertKlassic, berechnung.reBuchwertKlassic, berechnung.getAggZeile(1).bwEq,
					berechnung.getAggZeile(1).bwRe, berechnung.bwSaSp, vg.rapVT, (vg.vg == null ? 0.0 : vg.vg.rapVT));
			kedVjVerrechnen = KaModellierung.kedVjVerrechnen(zeit, berechnung.getZeitabhManReg(), kedVerrechnungArr,
					berechnung.laengeProjektionDr);
			kedVjVerrechnenArr[zeit] = kedVjVerrechnen;
			bwFiVerrechnung = KaModellierung.bwFiVerrechnung(zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr,
					bwFiAkt, bwFiNeuAnArr, berechnung.getZeitabhManReg().get(zeit).getFiBwr(), kedVerrechnungArr,
					kedVjVerrechnenArr);

			keFiNeuAnl = KaModellierung.keFiNeuAnl(bwFiNeuAnArr, kuponEsgArr,
					berechnung.getZeitabhManReg().get(zeit).getRlzNeuAnl(), zeit, berechnung.laengeProjektionDr,
					zpFaelligkeitArr);
			cfFiNeuAnl = KaModellierung.cfFiNeuAnl(keFiNeuAnl, zeit, zpFaelligkeitArr, bwFiNeuAnArr);
			cfFi = KaModellierung.cfFi(cfFiNeuAnl, cfFiAkt);
			keFi = KaModellierung.keFi(keFiAkt, keFiNeuAnl, kedVjVerrechnen);
			bwFiGesamtJe = KaModellierung.bwFiGesamtJe(bwFiVerrechnung, cfFi, keFi, rapVT, vg.rapVT);

			steuerVjAufgezinst = KaModellierung.aufzinsung(vg.ertragsSteuerLs, jaehrlZinsEsg, 0.0, zeit,
					berechnung.laengeProjektionDr);
			jueZiel = Rohueberschuss.jueZiel(berechnung.getZeitunabhManReg().getStrategie(), zeit,
					vg.eigenkapitalFortschreibung, berechnung.getZeitabhManReg().get(zeit).getREk(),
					berechnung.getZeitunabhManReg().getSteuersatz(), berechnung.getZeitunabhManReg().isiJuez(),
					vg.jUeZielerhoehung);
			zagFaellig = Rohueberschuss.zagFaellig(zeit, vg.zag);

			grNrd = Rohueberschuss.grNrd(zeit, vg.grNrd, rueckZahlung);

			fRfBMin = Deklaration.fRfBMin(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMin(), zeit);
			fRfBMax = Deklaration.fRfBMax(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMax(), zeit);

			zagAufgezinst = KaModellierung.aufzinsung(zagFaellig, jaehrlZinsEsg,
					berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
			cashflowAufgezinst = KaModellierung.aufzinsung(cashflowGesamt, jaehrlZinsEsg,
					berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);

			cfKredit = KaModellierung.cfKredit(vg.kredit, vg.kuponEsgII, zeit);

		}

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL01(this);
		}
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 2.
	 * 
	 * @param pfad
	 *            der gerechnet werden soll
	 */
	public void zeitRekursionL02(final int pfad) {
		final ZeitunabhManReg zeitunabhManReg = berechnung.getZeitunabhManReg();

		// pauschale Initialisierungen f�r Summationen (auch Zeit 0)
		zzrAlt = 0.0;
		zzrNeu = 0.0;
		zzrNueb = 0.0;
		drVorDeklAgg = 0.0;
		drVorDeklUebAgg = 0.0;
		lbwGarAgg = 0.0;
		fRfBVorEndzahlung = fRfBFrei;
		drVorDeklNuebAgg = 0.0;

		// pauschale Initialisierungen f�r Summationen (Zeit > 0)
		if (zeit > 0) {
			lGesAgg = 0.0;
			bStochAgg = 0.0;
			kStochAgg = 0.0;
			cfRvstochAgg = 0.0;
			ueEalt = 0.0;
			ueEneu = 0.0;
			ueEnueb = 0.0;
			zuebCashflowAgg = 0.0;
			optionenCashflowAgg = 0.0;
			beitragRohUebAgg = 0.0;
			rmzUebAlt = 0.0;
			rmzUebNeu = 0.0;
			rmzNueb = 0.0;
			ziRaZuStochAgg = 0.0;
			reAlt = 0.0;
			reNeu = 0.0;
			risikoUebStochAgg = 0.0;
			lGarStochAgg = 0.0;
		}
		for (RzgZeile z : rzgZeilen) {
			// pauschale Additionen (immer)
			drVorDeklAgg += z.drVorDekl;
			lbwGarAgg += z.lbwGar;

			// pauschale Additionen (Zeit > 0)
			if (zeit > 0) {
				lGesAgg += z.lGesamt;
				bStochAgg += z.beitraegeStoch;
				kStochAgg += z.kostenStoch;
				cfRvstochAgg += z.cfRvStoch;
				zuebCashflowAgg += z.cashflowZuebRzg;
				optionenCashflowAgg += z.cashflowOptionenRzg;
				beitragRohUebAgg += z.beitragRueRzg;
				ziRaZuStochAgg += z.ziRaZuStoch;
				lGarStochAgg += z.lGarStoch;

			}

			if (z.uebNueb.equals("UEB")) {
				// Additionen nur UEB
				if (z.altNeuBestand.equals("a")) {
					// Speuialfall "alt", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					zzrAlt += z.zzrJ;
					if (zeit > 0) {
						ueEalt += z.kostenUebStoch;
						rmzUebAlt += z.rmZTarif;
						reAlt += z.risikoUebStoch;
					}
				}
				if (z.altNeuBestand.equals("n")) {
					// Speuialfall "neu", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					zzrNeu += z.zzrJ;
					if (zeit > 0) {
						ueEneu += z.kostenUebStoch;
						rmzUebNeu += z.rmZTarif;
						reNeu += z.risikoUebStoch;
					}
				}
				drVorDeklUebAgg += z.drVorDekl;
			}
			if (z.uebNueb.equals("NUEB")) {
				// Additionen nur UEB
				zzrNueb += z.zzrJ;
				ueEnueb += z.kostenUebStoch;
				risikoUebStochAgg += z.risikoUebStoch;
				drVorDeklNuebAgg += z.drVorDekl;

				if (zeit > 0) {
					rmzNueb += z.rmZTarif;
				}
			}

		}
		aufzinsungcfEvuRvu = KaModellierung.aufzinsung(cfRvstochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungBeitraege = KaModellierung.aufzinsung(bStochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungKosten = KaModellierung.aufzinsung(kStochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungGesamt = KaModellierung.aufzinsung(lGesAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		zzrGesamt = Rohueberschuss.zzrGesamt(zzrAlt, zzrNeu, zzrNueb);

		if (zeit > 0) {
			ueEaltNoGcr = Rohueberschuss.kostenueberschussBestand(ueEalt,
					berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft());
			ueEneuNoGcr = Rohueberschuss.kostenueberschussBestand(ueEneu,
					berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft());

			deltaZzrUebAlt = Rohueberschuss.deltaZzr(zzrAlt, vg.zzrAlt);
			deltaZzrUebNeu = Rohueberschuss.deltaZzr(zzrNeu, vg.zzrNeu);
			deltaZzrNueb = Rohueberschuss.deltaZzr(zzrNueb, vg.zzrNueb);

			gcrUeB = KaModellierung.cfUebrEng(ueEalt, ueEneu, ueEaltNoGcr, ueEneuNoGcr);

			rmzUebGesamtAlt = Rohueberschuss.rmZGesamt(rmzUebAlt, deltaZzrUebAlt);
			rmzUebGesamtNeu = Rohueberschuss.rmZGesamt(rmzUebNeu, deltaZzrUebNeu);
			rmzNuebGesamt = Rohueberschuss.rmZGesamt(rmzNueb, deltaZzrNueb);

			anteilLobsKaStress = KaModellierung.anteilLobsKaStress(rzgZeilen, vg.szenarioId, vg.drLockInAgg, vg.sueAf,
					vg.zzrGesamt);

			aufwendungenKa = KaModellierung.kaAufwendungen(bwFiVerrechnung, bwRe, bwEq, vg.kredit,
					berechnung.getZeitunabhManReg().getFaktorKapitalanlagen(),
					berechnung.getZeitunabhManReg().getFaktorKapitalanlagenKostenStress(), anteilLobsKaStress);
			ak = KaModellierung.ak(vg.kredit, vg.kuponEsgII, zeit);

			keCfAufzinsung = KaModellierung.keCfAufzinsung(lGesAgg, bStochAgg, kStochAgg, cfRvstochAgg, zagFaellig,
					vg.ertragsSteuerLs, aufzinsungGesamt, aufzinsungBeitraege, aufzinsungKosten, aufzinsungcfEvuRvu,
					zagAufgezinst, steuerVjAufgezinst, zeit, berechnung.laengeProjektionDr, cashflowGesamt,
					cashflowAufgezinst);
			keRlsI = KaModellierung.keRlsI(keFi, keReLaufend, keEqLaufend, keEqRlsI, keReRlsPlan, ak, keCfAufzinsung);

			cfOhneKa = KaModellierung.cfOhneKa(aufzinsungGesamt, aufzinsungBeitraege, aufzinsungKosten, zagAufgezinst,
					steuerVjAufgezinst, aufzinsungcfEvuRvu, cashflowAufgezinst, zinsen, rueckZahlung, cfKredit, gcrUeB,
					aufwendungenKa);

			mindestkapitalertragLvrgAltNeu = Rohueberschuss.mindestkapitalertragLvrgAltNeu(
					berechnung.getZeitunabhManReg().getSchalterVerrechnungLebensversicherungsreformgesetz(), keRlsI,
					rmzUebGesamtAlt, rmzUebGesamtNeu, rmzNuebGesamt, reAlt, reNeu, risikoUebStochAgg, ueEaltNoGcr,
					ueEneuNoGcr, ueEnueb, jueZiel, ziRaZuStochAgg, zinsen);

			final double arapMieten = zeit == 1 ? berechnung.arapMieten : 0.0;
			bwVorRls = KaModellierung.bwVorRls(bwFiGesamtJe, bwReRlsPlan, bwEqRlsI, cfFi, keMieten, arapMieten, keDiv,
					cfOhneKa);
			mwVorRls = KaModellierung.mwVorRls(fiMw, mwReVorRls, mwEqVorRls, cfFi, keMieten, arapMieten, keDiv,
					cfOhneKa);
			rzMittel = KaModellierung.rzMittel(zeit, berechnung.laengeProjektionDr, rmzUebAlt, rmzUebNeu, rmzNueb,
					vg.hgbDrAgg, vg.drLockInAgg, drVorDeklAgg);
		}
		aKaAufwendungen = KaModellierung.aKaAufwendungen(zeit, hgbDrAgg, drVorDeklAgg);
		aVN = KaModellierung.aVn(zeit, kAgg, bAgg, kStochAgg, bStochAgg);
		lockInFaktor = KaModellierung.lockInFaktor(zeit, lTodAgg, kaAgg, lSonstErlAgg, rkAgg, lGarStochAgg);

		final PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);
		mwVt = KaModellierung.mwVt(rlz, berechnung.leistGar, berechnung.restGar, berechnung.aufwendungenKa0,
				lockInFaktor, aVN, aKaAufwendungen, pfadZeile, pfad, zeit, berechnung.szenario.projektionsHorizont, // szenario.projektionsHorizont,
				berechnung.getZeitunabhManReg().getMonatZahlung());

		bwrPas = KaModellierung.bwrPas(drVorDeklAgg, zzrGesamt, mwVt);

		aReZielDaa = KaModellierung.aReZielDaa(zeit, berechnung.laengeProjektionDr,
				berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
				berechnung.getZeitunabhManReg().getDaaFaktorRw(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwr(),
				berechnung.getZeitunabhManReg().getDaaFaktorRwVerluste(),
				berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReserven(),
				berechnung.getZeitunabhManReg().getZielAnteilARe(),
				berechnung.getZeitunabhManReg().getZielAnteilReDaa(), keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw,
				bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg);

		aFiZielDaa = KaModellierung.aFiZielDaa(zeit, berechnung.laengeProjektionDr,
				zeitunabhManReg.getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
				zeitunabhManReg.getDaaFaktorRw(), zeitunabhManReg.getDaaFaktorFiBwr(),
				zeitunabhManReg.getDaaFaktorRwVerluste(),
				zeitunabhManReg.getDaaFaktorUntergrenzePassiveAktiveReserven(), zeitunabhManReg.getaFiZiel(),
				zeitunabhManReg.getZielMindestDaaFi(), keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI,
				mwEqVorRls, bwrPas, drVorDeklAgg);
		aFiMinDaa = KaModellierung.aFiMinDaa(zeit, berechnung.laengeProjektionDr,
				berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
				berechnung.getZeitunabhManReg().getDaaFaktorRw(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwr(),
				berechnung.getZeitunabhManReg().getDaaFaktorRwVerluste(),
				berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReserven(),
				berechnung.getZeitunabhManReg().getaMinFi(), berechnung.getZeitunabhManReg().getZielMindestDaaFi(),
				keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg);

		if (zeit > 0) {
			aReRls = KaModellierung.aReRls(bwReRlsPlan, mwReVorRls, aReZielDaa, bwEqRlsI, mwEqVorRls, bwFiGesamtJe,
					fiMw, bwVorRls, mwVorRls, keRlsI, mindestkapitalertragLvrgAltNeu, aufwendungenKa, zeit, aFiMinDaa,
					berechnung.laengeProjektionDr);
			cfReRls = KaModellierung.cfReRls(aReRls, mwReVorRls);
			keReRls = KaModellierung.keReRls(aReRls, mwReVorRls, bwReRlsPlan);
			bwReNachRls = KaModellierung.bwReNachRls(aReRls, bwReRlsPlan);
			mwRenachRls = KaModellierung.mwReNachRls(aReRls, mwReVorRls);
		}

		if (zeit > 0) {
			aEqRlsII = KaModellierung.aEqRlsII(bwEqRlsI, mwEqVorRls, bwFiGesamtJe, fiMw, bwReNachRls, bwVorRls,
					aReZielDaa, aFiZielDaa, aFiMinDaa, keReRls, keRlsI, mindestkapitalertragLvrgAltNeu, aufwendungenKa,
					keDiv, keMieten, zeit == 1 ? berechnung.arapMieten : 0.0, cfFi, cfReRls, cfOhneKa,
					berechnung.laengeProjektionDr, zeit);
			keEqRlsII = KaModellierung.keEqRlsII(aEqRlsII, mwEqVorRls, bwEqRlsI);
			keRlsII = KaModellierung.keRlsII(keRlsI, keReRls, keEqRlsII);
			kedVerrechnung = KaModellierung.kedVerrechnung(mindestkapitalertragLvrgAltNeu, keRlsII, fiMw, bwFiGesamtJe,
					aufwendungenKa);
			kedVerrechnungArr[zeit] = kedVerrechnung;

			keVerrechnung = KaModellierung.keVerrechnung(keRlsII, kedVerrechnung, ziRaZuStochAgg);
			bwFiVerechnungJe = KaModellierung.bwFiVerechnungJe(bwFiGesamtJe, kedVerrechnung);

			nvz = KaModellierung.nvz(keVerrechnung, aufwendungenKa, ak, ziRaZuStochAgg, drVorDeklAgg, zzrGesamt, grNrd,
					vg.drLockInAgg, vg.eigenkapitalFortschreibung, vg.zzrGesamt, vg.grNrd, vg.zag, vg.nfRfB,
					vg.ertragsSteuerLs, vg.kredit, berechnung.getZeitunabhManReg().getMonatZahlung());
			mwEqRlsII = KaModellierung.mwEqRlsII(aEqRlsII, mwEqVorRls);
			cfEqRlsII = KaModellierung.cfEqRlsII(aEqRlsII, mwEqVorRls);
			kapitalertragAnrechenbar = Rohueberschuss.kapitalertragAnrechenbar(zeit, nvz, drVorDeklUebAgg,
					vg.drLockInAggWennLoB, zzrAlt + zzrNeu, vg.zzrAlt + vg.zzrNeu, vg.nfRfB);

			bwEqRlsII = KaModellierung.bwEqRlsII(aEqRlsII, bwEqRlsI);
			rohueb = Rohueberschuss.rohueb(keVerrechnung, rmzUebGesamtAlt + rmzUebGesamtNeu, rmzNuebGesamt,
					reAlt + reNeu, risikoUebStochAgg, ueEaltNoGcr + ueEneuNoGcr, ueEnueb, aufwendungenKa, zinsen);
			rohuebArr[zeit] = rohueb;

			mindZf = Rohueberschuss.mindZf(kapitalertragAnrechenbar, deltaZzrUebAlt + rmzUebAlt, reAlt, ueEaltNoGcr,
					vg.drLockInAlt, vg.zzrAlt, vg.sueAfAlt, deltaZzrUebNeu + rmzUebNeu, reNeu, ueEneuNoGcr,
					vg.drLockInNeu, vg.zzrNeu, vg.sueAfNeu);

			mindZfGes = Rohueberschuss.mindZfGes(mindZf, vg.mindZfKk);

			rfBZuf = Rohueberschuss.jUERfBZuf(2, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr,
					vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie, // zwei Parameter im Original
					berechnung.getZeitabhManReg().get(zeit).getRohUeb(),
					berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(),
					berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
					berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB,
					keVerrechnung, kapitalertragAnrechenbar);
			rfBZufArr[zeit] = rfBZuf;

			nfRfB56b = Rohueberschuss.jUERfBZuf(3, zeit, rohuebArr, mindZfGes, mindZf, jueZiel,
					ueEaltNoGcr + ueEneuNoGcr, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie, // zwei Parameter im
																								// Original
					berechnung.getZeitabhManReg().get(zeit).getRohUeb(),
					berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(),
					berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
					berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB,
					keVerrechnung, kapitalertragAnrechenbar);
			nfRfB56bArr[zeit] = nfRfB56b;

			jue = Rohueberschuss.jUERfBZuf(1, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr,
					vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie, // zwei Parameter im
					// Original
					berechnung.getZeitabhManReg().get(zeit).getRohUeb(),
					berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(),
					berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
					berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB,
					keVerrechnung, kapitalertragAnrechenbar);
			jUeZielerhoehung = Rohueberschuss.jUeZielerhoehung(zeit, berechnung.getZeitunabhManReg().isiJuez(), jueZiel,
					jue, berechnung.getZeitunabhManReg().getStrategie());

			mindZfKk = Rohueberschuss.mindZfKk(zeit, vg.mindZfKk, mindZf, rfBZuf);
			ertragssteuer = Rohueberschuss.ertragssteuer(jue, berechnung.getZeitunabhManReg().getSteuersatz(), vg.vv);
			vv = Rohueberschuss.vv(vg.vv, jue, ertragssteuer, berechnung.getZeitunabhManReg().getSteuersatz());
			ertragsSteuerLs = Rohueberschuss.ertragssteuerLs(ertragssteuer, vg.lsHgb);
			lsHgb = Rohueberschuss.lsHgb(vg.lsHgb, ertragssteuer, ertragsSteuerLs);

			mittlRfBZufuehrung = Deklaration.mittlRfBZufuehrung(
					berechnung.getZeitunabhManReg().getAnzahlJahreDurchschnittlRfbZufuehrung(), zeit, rfBZufArr,
					drLockInAggWennLoBArr, berechnung.vuHistorie);

			sUeAf56bEntnahme = Deklaration.sUeAf56bEntnahme(vg.fRfBFrei, nfRfB56b, zeit,
					berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme());
			sUeAf56bEntnahmeArr[zeit] = sUeAf56bEntnahme;
			fRfB56bEntnahme = nfRfB56b - sUeAf56bEntnahme;
			fRfB56bEntnahmeArr[zeit] = fRfB56bEntnahme;
			zielDeklaration = Deklaration.zielDeklaration(mittlRfBZufuehrung, vg.drLockInAggWennLoB);
			fRfBUeberlauf = Deklaration.fRfBUeberlauf(vg.fRfBFrei, nfRfB56b, rfBZuf, zielDeklaration, fRfBMax, zeit,
					berechnung.laengeProjektionDr);
			fRfBVorEndzahlung = Deklaration.fRfBVorEndzahlung(vg.fRfBFrei, rfBZuf, nfRfB56b, fRfBMin, fRfBMax,
					zielDeklaration, sUeAf56bEntnahme);
			dekl = Deklaration.dekl(vg.fRfBVorEndzahlung, fRfBVorEndzahlung, rfBZuf, nfRfB56b, sUeAf56bEntnahme,
					fRfBMin, zielDeklaration, berechnung.getZeitabhManReg().get(zeit).getFrfbUeberlauf(), fRfBUeberlauf,
					zeit, berechnung.laengeProjektionDr);
			if (lbwGarAgg > 0.001) {
				fRfBFrei = fRfBVorEndzahlung;
			} else {
				fRfBFrei = 0.0;
			}
			deklRest = Deklaration.deklRest(berechnung.getZeitunabhManReg().getDeklarationsMethode(), dekl,
					berechnung.getZeitabhManReg().get(zeit).getGrundUeberschuss(), reAlt, reNeu, ueEaltNoGcr,
					ueEneuNoGcr);
			deklZins = Deklaration.deklZins(berechnung.getZeitunabhManReg().getDeklarationsMethode(), dekl, deklRest);
			vzGes = Deklaration.vzGes(deklZins, rzgZeilen);
		}

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL02(this);
		}
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 3.
	 */
	public void zeitRekursionL03() {
		endZahlungAgg = 0.0;
		drLockInAgg = 0.0;
		drLockInAggWennLoB = 0.0;
		drLockInAlt = 0.0;
		drLockInNeu = 0.0;
		barAgg = 0.0;
		drGesAgg = 0.0;
		sueAFZufFRfBUeberlaufAgg = 0.0;
		sueAFZufAgg = 0.0;
		sueAFEntnahmeAgg = 0.0;
		sueAfAlt = 0.0;
		sueAfNeu = 0.0;
		lockInAgg = 0.0;
		if (zeit > 0) {
			rohuebKpK = 0.0;
			rohuebKpN = 0.0;
			rohuebKpP = 0.0;
			deltaLAgg = 0.0;
		}
		for (RzgZeile rzg : rzgZeilen) {
			endZahlungAgg += rzg.endZahlung;
			drLockInAgg += rzg.drLockInRzg;
			barAgg += nanZero(rzg.bar);
			sueAFZufFRfBUeberlaufAgg += nanZero(rzg.sueafZufFrfbUeberlauf);
			sueAFZufAgg += nanZero(rzg.sUeAfzuf);
			sueAFEntnahmeAgg += nanZero(rzg.sUeAfEntnahme);
			drGesAgg += nanZero(rzg.drGesamtRzg);
			lockInAgg += rzg.lockIn;
			if (zeit > 0) {
				rohuebKpK += rzg.rohuebKpRzgBY;
				rohuebKpN += rzg.rohuebKpRzgNeg;
				rohuebKpP += rzg.rohuebKpRzg;
				deltaLAgg += rzg.deltaLRzg;
			}
			if (rzg.uebNueb.equals("UEB")) {
				// Additionen nur UEB
				if (rzg.altNeuBestand.equals("a")) {
					// Speuialfall "alt", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInAlt += rzg.drLockInRzg;
					sueAfAlt += rzg.sUeAfRzg;
				}
				if (rzg.altNeuBestand.equals("n")) {
					// Speuialfall "neu", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInNeu += rzg.drLockInRzg;
					sueAfNeu += rzg.sUeAfRzg;
				}
				drLockInAggWennLoB += rzg.drLockInRzg;
			}
		}
		drLockInAggWennLoBArr[zeit] = drLockInAggWennLoB;

		if (zeit > 0) {
			jueVnKp = Bilanzpositionen.jueVnKp(kapitalertragAnrechenbar, keVerrechnung, aufwendungenKa, rohuebKpP,
					rohuebKpK, jue, vg.hgbDrAgg, vg.drstKPAgg);
			deklsurplusfRfB = Bilanzpositionen.deklSurplusFRfB(vg.berechnung.hgbBilanzdaten.getFreieRfbBuchwert(),
					dekl + sueAFZufFRfBUeberlaufAgg, vg.deklsurplusfRfBarr, vg.fRfB56bEntnahmeArr);
			deklsurplusfRfBarr[zeit] = deklsurplusfRfB;

			barSf = Bilanzpositionen.barSf(dekl, sueAFZufFRfBUeberlaufAgg, barAgg, deklsurplusfRfB);
			sUeAfZufSf = Bilanzpositionen.sUeAfZufSf(dekl, sueAFZufFRfBUeberlaufAgg, sueAFZufAgg, deklsurplusfRfB);
			sUeAfZufSfArr[zeit] = sUeAfZufSf;

			eigenkapitalFortschreibung = Rohueberschuss.eigenkapitalFortschreibung(zeit,
					berechnung.getZeitabhManReg().get(zeit).getEkZiel(), drGesAgg);
			zag = Rohueberschuss.zag(zeit, jue, ertragsSteuerLs, eigenkapitalFortschreibung,
					vg.eigenkapitalFortschreibung, berechnung.laengeProjektionDr);

			lockInSf = Bilanzpositionen.lockInSf(deklsurplusfRfB, barSf, sUeAfZufSf);
			leAggrSf = Bilanzpositionen.leAggrSf(vg.leAggrSf, lockInAgg, lockInSf, lbwGarAgg, vg.nfRfB,
					vg.drLockInAggWennLoB);

			{
				final double arapMieten = zeit == 1 ? berechnung.arapMieten : 0.0;
				cfVorKredit = KaModellierung.cfVorKredit(cfFi, cfReRls, cfEqRlsII, cfOhneKa, endZahlungAgg, keDiv,
						keMieten, arapMieten);
				bwRlsII = KaModellierung.bwRlsII(bwFiGesamtJe, bwReNachRls, bwEqRlsII, cfFi, keMieten, keDiv, cfReRls,
						cfEqRlsII, cfOhneKa, endZahlungAgg, arapMieten);
			}
			bwVerechnungJe = KaModellierung.bwVerrechnungJe(bwRlsII, kedVerrechnung);
			kredit = KaModellierung.k(cfVorKredit, zeit, berechnung.laengeProjektionDr);
			cfNeuAnlage = KaModellierung.cfNeuanlage(cfVorKredit, kredit, zeit, berechnung.laengeProjektionDr);

			zagEndzahlung = Rohueberschuss.zagEndzahlung(zeit, berechnung.laengeProjektionDr, cfVorKredit,
					ertragssteuer);
			mwRlsII = KaModellierung.mwRlsII(fiMw, mwRenachRls, mwEqRlsII, cfFi, keMieten, keDiv, cfReRls, cfEqRlsII,
					cfOhneKa, zeit == 1 ? berechnung.arapMieten : 0.0, endZahlungAgg);
			sUeAfEntSf = Bilanzpositionen.sueafEntSf(berechnung.getAggZeile(0).sueAf, sueAFEntnahmeAgg, sUeAfZufSfArr,
					vg.sUeAfEntSfArr, vg.sUeAf56bEntnahmeArr);
			sUeAfEntSfArr[zeit] = sUeAfEntSf;
			cashflowSf = Bilanzpositionen.cashflowSf(barSf, sUeAfEntSf, leAggrSf, lTodAgg, kaAgg, rkAgg, lSonstErlAgg,
					jaehrlZinsEsg, berechnung.getZeitunabhManReg().getMonatZahlung(), zeit,
					berechnung.laengeProjektionDr);
		}
		sueAf = Rohueberschuss.sueAf(sueAfAlt, sueAfNeu);
		nfRfB = Rohueberschuss.nfRfB(sueAf, fRfBFrei);

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL03(this);
		}

	}

	/**
	 * Pr�fe, ob die Daten hier und in RzgZeile finit sind. Falls nicht, wird eine {@link ResultNotFinite} Exception
	 * geworfen.
	 */
	public void checkFinite() {
		// in Zeit == 0 gibt es viele NaN, die lassen wir weg
		if (zeit > 0) {
			final List<String> errors = new ArrayList<>();
			errors.addAll(CheckData.checkFinite(this));
			for (RzgZeile rzg : rzgZeilen) {
				errors.addAll(CheckData.checkFinite(rzg));
			}
			if (errors.size() > 0) {
				final String header = "In Szenario " + szenarioId + " (" + szenario + "), pfad = "
						+ berechnung.getAktuellerPfad() + ", zeit = " + zeit + " traten �berl�ufe auf in den Feldern";
				final String felder = errors.stream().reduce("", (x, y) -> (x.isEmpty() ? x : x + ", ") + y);
				throw new ResultNotFinite(header + ": " + felder, header, felder);
			}
		}
	}

	/**
	 * F�lle ein Array von double rekursiv r�ckw�rts �ber die Zeit. Aus technischen Gr�nden wird als aktueller Wert NaN
	 * eingetragen.
	 * 
	 * @param f
	 *            die Funktion, die auf Zeilen angewendet den gew�nschten Wert erbigt
	 * @param data
	 *            das zu f�llende Array, es wird erzeugt, wenn es null ist
	 * @return das Array
	 */
	public double[] fillArr(final Function<AggZeile, Double> f, double[] data) {
		if (data == null) {
			data = new double[zeit + 1];
		}
		final int index;
		if (vg != null) {
			index = vg.fillArrRecursive(f, data);
		} else {
			index = 0;
		}
		if (index != zeit)
			throw new IllegalStateException("Vorg�nger und Zeiten divergieren: index = " + index + ", zeit = " + zeit);
		data[index] = Double.NaN;
		return data;
	}

	/**
	 * F�lle ein Array von ints rekursiv r�ckw�rts �ber die Zeit. Aus technischen Gr�nden wird als aktueller Wert NaN
	 * eingetragen.
	 * 
	 * @param f
	 *            die Funktion, die auf Zeilen angewendet den gew�nschten Wert erbigt
	 * @param data
	 *            das zu f�llende Array, es wird erzeugt, wenn es null ist
	 * @return das Array
	 */
	public int[] fillArrInt(final Function<AggZeile, Integer> f, int[] data) {
		if (data == null) {
			data = new int[zeit + 1];
		}
		final int index;
		if (vg != null) {
			index = vg.fillArrRecursiveInt(f, data);
		} else {
			index = 0;
		}
		if (index != zeit)
			throw new IllegalStateException("Vorg�nger und Zeiten divergieren: index = " + index + ", zeit = " + zeit);
		data[index] = Integer.MAX_VALUE;
		return data;
	}

	private final int fillArrRecursive(final Function<AggZeile, Double> f, final double[] data) {
		if (vg == null) {
			data[0] = f.apply(this);
			return 1;
		} else {
			final int index = vg.fillArrRecursive(f, data);
			data[index] = f.apply(this);
			return index + 1;
		}
	}

	private final int fillArrRecursiveInt(final Function<AggZeile, Integer> f, final int[] data) {
		if (vg == null) {
			data[0] = f.apply(this);
			return 1;
		} else {
			final int index = vg.fillArrRecursiveInt(f, data);
			data[index] = f.apply(this);
			return index + 1;
		}
	}

	// ========================================================================
	// Get-Funktionen des Blattes FI CFs.

	/**
	 * Wert zur Restlaufzeit der CF FIs zur selben Zeit. Blatt FI CFs.
	 * 
	 * @param rlz
	 *            die Restlaufzeit
	 * @return der Wert
	 */
	public double getCfFis(final int rlz) {
		return cfFis[rlz];
	}

	/**
	 * Wert zur Restlaufzeit der CF FI Zeitschrittig zur selben Zeit. Blatt FI MW.
	 * 
	 * @param rlz
	 *            die Restlaufzeit
	 * @return der Wert
	 */
	public double getCfFiZeitschrittig(final int rlz) {
		return cfFiZeitschrittig[rlz];
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten A bis Z.

	/**
	 * Kosten. D.
	 * 
	 * @return der Wert
	 */
	public double getKAgg() {
		return kAgg;
	}

	/**
	 * Pr�mien. E
	 * 
	 * @return der Wert
	 */
	public double getBAgg() {
		return bAgg;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten AA bis AZ.

	/**
	 * Zinsen, Nachrangliche Verbindlichkeiten. AM.
	 * 
	 * @return der Wert
	 */
	public double getZinsen() {
		return zinsen;
	}

	/**
	 * R�ckzahlung, Nachrangliche Verbindlichkeiten. AN.
	 * 
	 * @return der Wert
	 */
	public double getRueckZahlung() {
		return rueckZahlung;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten CA bis CZ.

	// ========================================================================
	// Get-Funktionen der agg-Spalten DA bis DZ.

	/**
	 * Aufwendungen f�r KA. DO.
	 * 
	 * @return der Wert
	 */
	public double getAufwendungenKa() {
		return aufwendungenKa;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten EA bis EZ.

	/**
	 * GCR �briges Ergebnis an Neugesch�ft - �EB. EI.
	 * 
	 * @return der Wert
	 */
	public double getGcrUeB() {
		return gcrUeB;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten FA bis FZ.

	/**
	 * ZAG, f�llig. FA.
	 * 
	 * @return der Wert
	 */
	public double getZagFaellig() {
		return zagFaellig;
	}

	/**
	 * ZAG, Endzahlung. FB.
	 * 
	 * @return der Wert
	 */
	public double getZagEndzahlung() {
		return zagEndzahlung;
	}

	/**
	 * Steuer, festgelegt. FC.
	 * 
	 * @return der Wert
	 */
	public double getErtragssteuer() {
		return ertragssteuer;
	}

	/**
	 * Steuer, festgelegt, nach der LS-Korrektur. FD.
	 * 
	 * @return der Wert
	 */
	public double getErtragsSteuerLs() {
		return ertragsSteuerLs;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten GA bis GZ.

	/**
	 * S�mtliche garantierte Leistungen. GD.
	 * 
	 * @return der Wert
	 */
	public double getLGarAgg() {
		return lGarAgg;
	}

	/**
	 * Leistungen Gesamt. GF.
	 * 
	 * @return der Wert
	 */
	public double getLGesAgg() {
		return lGesAgg;
	}

	/**
	 * Beitr�ge, stochastisch. GG.
	 * 
	 * @return der Wert
	 */
	public double getBStochAgg() {
		return bStochAgg;
	}

	/**
	 * Kosten, stochastisch. GH.
	 * 
	 * @return der Wert
	 */
	public double getKStochAgg() {
		return kStochAgg;
	}

	/**
	 * Cashflow EVU -> RVU, stochastisch. GI.
	 * 
	 * @return der Wert
	 */
	public double getCfRvstochAgg() {
		return cfRvstochAgg;
	}

	/**
	 * Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. GK.
	 * 
	 * @return der Wert
	 */
	public double getCashflowGesamt() {
		return cashflowGesamt;
	}

	/**
	 * Diskontfunktion. GL.
	 * 
	 * @return der Wert
	 */
	public double getDiskontEsg() {
		return diskontEsg;
	}

	/**
	 * Mittlerer j�hrlicher Zins (f�r Aufzinsung). GM.
	 * 
	 * @return der Wert
	 */
	public double getJaehrlZinsEsg() {
		return jaehrlZinsEsg;
	}

	/**
	 * Leistungen durch Endzahlung. GQ.
	 * 
	 * @return der Wert
	 */
	public double getEndZahlungAgg() {
		return endZahlungAgg;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten HA bis HZ.

	/**
	 * Cashflow gesamt, �berschussfonds. HE.
	 * 
	 * @return der Wert
	 */
	public double getCashflowSf() {
		return cashflowSf;
	}

	/**
	 * Jahres�berschuss geschl�sselt auf k�nftige Pr�mien. HK.
	 * 
	 * @return der Wert
	 */
	public double getJueVnKP() {
		return jueVnKp;
	}

	/**
	 * Z�B: Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung.HL.
	 * 
	 * @return der Wert
	 */
	public double getZuebCashflowAgg() {
		return zuebCashflowAgg;
	}

	/**
	 * Z�B: Cashflow gesamt, �berschussbeteiligung, ohne Endzahlung. HM.
	 * 
	 * @return der Wert
	 */
	public double getOptionenCashflowAgg() {
		return optionenCashflowAgg;
	}

	@Override
	public String toString() {
		return "[agg " + zeit + "]";
	}
}
