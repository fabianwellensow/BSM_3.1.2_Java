package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.gdv.bsm.intern.params.VUHistorie;
import de.gdv.bsm.intern.params.VUHistorieZeile;
import de.gdv.bsm.vu.berechnung.FlvZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;

/**
 * Funktionen des Excel-Moduls <code>Deklaration</code>.
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
public class Deklaration {

	/**
	 * Berechnet den Barwert der zuk�nftigen, deterministisch projezierten Leistungen bez�glich des Rechnungszinses zum
	 * Ende des Jahres T (deterministischer Leistungsbarwert). Dabei wird davon ausgegangen, dass die zuk�nfftigen
	 * Zahlungen in den Jahren T+1,..., omega im Monat monat eingehen.<br/>
	 * Funktionsname in Excel: LBW_gar.
	 * 
	 * @param lbwGarOSonstErl
	 *            Leistungsbarwert ohne SonstErl, deterministisch
	 * @param lbwSonstErl
	 *            Leistungsbarwert SonstErll, deterministisch
	 * @param laSonstErlAgg
	 *            Leistungsanpassung Kapitalwahl
	 * @param lambda
	 *            Gesamt Storno
	 * @return Barwert
	 */
	public static double lbwGar(final double lbwGarOSonstErl, final double lbwSonstErl, final double laSonstErlAgg,
			final double lambda) {
		return lambda * (lbwGarOSonstErl + (1 + (Double.isNaN(laSonstErlAgg) ? 0.0 : laSonstErlAgg)) * lbwSonstErl);
	}

	/**
	 * Berechnet zum Zeitpunkt T die mittlere Zuf�hrung zur RfB der letzten m Jahre (d.h der Jahre T-(m-1),..., T) in
	 * Prozent der Deckungsr�ckstellung des �berschussberechtigten Gesch�fts. <br/>
	 * Funktionsname in Excel: MittlRfBZufuehrung.
	 * 
	 * @param m
	 *            Anzahl der Jahre �ber die gemittelt werden soll
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rfbZufArr
	 *            Vektor der RfBZuf�hrungen der vergangenene Jahre
	 * @param drLockInArr
	 *            Deckungsr�ckstellungen des �berschussberechtigten Gesch�ftes der vergangenen Jahre
	 * @param vuHistorie
	 *            Historie der letzten 10 Jahre vor Projektionsstart (in Excel zwei Parameter)
	 * @return mittlere Zuf�hrung zur RfB
	 */
	public static double mittlRfBZufuehrung(final int m, final int t, final double[] rfbZufArr,
			final double[] drLockInArr, final VUHistorie vuHistorie) {
		int vuHistZeit = 0;
		double mittlRfBZufuehrung = 0;
		int k = m;
		for (int i = 0; i < m; ++i) {
			double rfbZufJ = 0.0;
			double drLockInJ = 0.0;
			// Zuweisung DrSt und RfB Zuf�hrung aus der Vergangenheit
			if (t - i >= 1) {
				rfbZufJ = rfbZufArr[t - i];
				drLockInJ = drLockInArr[t - i - 1];
			} else {
				final VUHistorieZeile histCurr = vuHistorie.get(vuHistZeit);
				final VUHistorieZeile histPrev = vuHistorie.get(vuHistZeit - 1);
				rfbZufJ = histCurr.getRfBZufuehrung();
				drLockInJ = histPrev.getHgbDrst();
				--vuHistZeit;
			}
			double rfbZufMittlJ;

			if (drLockInJ > 0.001) {
				rfbZufMittlJ = rfbZufJ / drLockInJ;
			} else {
				rfbZufMittlJ = 0.0;
				k = Math.max(k - 1, 1);
			}
			mittlRfBZufuehrung += rfbZufMittlJ;
		}

		mittlRfBZufuehrung = mittlRfBZufuehrung / k;

		return mittlRfBZufuehrung;
	}

	/**
	 * Berechnet die zul�ssige Untergrenze der freien RfB zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: fRfBMin.
	 * 
	 * @param drLockInUeb
	 *            Garantierte Deckungsr�ckstellung zum Zeitpunkt T-1 f�r das �berschussberechtigte Gesch�ft
	 * @param pFRfBMin
	 *            Untergrenze der freien RfB in Prozent der garantierten Deckungsr�ckstellung des �berschussberechtigten
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @return zul�ssige Untergrenze der freien RfB
	 */
	public static double fRfBMin(final double drLockInUeb, final double pFRfBMin, final int t) {
		if (t != 0 && nanZero(drLockInUeb) > 0.001) {
			return nanZero(drLockInUeb) * pFRfBMin;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die zul�ssige Untergrenze der freien RfB zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: fRfBMax.
	 * 
	 * @param drLockIn
	 *            Garantierte Deckungsr�ckstellung zum Zeitpunkt T-1 f�r das �berschussberechtigte Gesch�ft
	 * @param pFRfBMax
	 *            Untergrenze der freien RfB in Prozent der garantierten Deckungsr�ckstellung
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @return zul�ssige Untergrenze der freien RfB
	 */
	public static double fRfBMax(final double drLockIn, final double pFRfBMax, final int t) {
		if (t != 0 && nanZero(drLockIn) > 0.001) {
			return nanZero(drLockIn) * pFRfBMax;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die Zieldeklaration im Jahre T. <br/>
	 * Funktionsname in Excel: ZielDeklaration.
	 * 
	 * @param rm
	 *            Mittlere Zuf�hrung zur RfB der letzten Jahre m Jahre (m ist ein Steuerungsparameter)
	 * @param drLockIn
	 *            Garantierte Deckungsr�ckstellung des �berschussberechtigten Gesch�ftes im Jahr T-1
	 * @return Zieldeklaration
	 */
	public static double zielDeklaration(final double rm, final double drLockIn) {
		if (drLockIn > 0.001) {
			return rm * drLockIn;
		}
		return 0.0;
	}

	/**
	 * Berechnet welcher Teil der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T auf den S�AF
	 * entf�llt. Funktionsname in Excel: S�AF_56b_Entnahme.
	 * 
	 * @param vgFRfBFrei
	 *            H�he der freien RfB zum Zeitpunkt T-1
	 * @param nfRfB56b
	 *            H�he der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rfBEntnahme
	 *            Managementregel (Soll im Fall einer negativen Roh�berschussbeteiligung eine Entnahme aus der freien
	 *            RfB vorgenommen werden)
	 * @return Entnahme
	 */
	public static double sUeAf56bEntnahme(final double vgFRfBFrei, final double nfRfB56b, final int t,
			final double rfBEntnahme) {
		if (t > 0) {
			return Math.max(nfRfB56b - rfBEntnahme * Math.max(vgFRfBFrei, 0.0), 0.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die freie RfB zum Ende des Jahres T, ohne Ber�cksichtigung der Aussch�ttung aus der RfB, wenn der
	 * Bestand ausl�uft.<br/>
	 * Funktionsname in Excel: fRfB_vor_Endzahlung.
	 * 
	 * @param frfbV
	 *            Wert der freien RfB im Vorjahr T-1
	 * @param rfBZuf
	 *            Zuf�hrung zur gesamten RfB im Jahr T
	 * @param nfRfB56bEntnahme
	 *            Entnahme aus der nicht festgelegten RfB gem�� �56b VAG im Jahre T
	 * @param fRfBMin
	 *            Zul�ssige Untergrenze der freien RfB im Jahr T
	 * @param fRfBMax
	 *            Zul�ssige Obergrenze der freien RfB im Jahr T
	 * @param zielDeklaration
	 *            Stochastische Ziel-Deklaration (S�AF und Lock-In zusammen) im Jahr T
	 * @param sUeAf56bEntnahme
	 *            Anteil der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T der auf den S�AF
	 *            entf�llt
	 * @return freie RfB
	 */
	public static double fRfBVorEndzahlung(final double frfbV, final double rfBZuf, final double nfRfB56bEntnahme,
			final double fRfBMin, final double fRfBMax, final double zielDeklaration, final double sUeAf56bEntnahme) {

		final double fRfB56bEntn = nfRfB56bEntnahme - sUeAf56bEntnahme;

		if (rfBZuf < fRfBMin - (frfbV - fRfB56bEntn)) {
			// Falls die Untergrenze f�r die freie RfB nicht eingehalten
			// werden kann
			return frfbV - fRfB56bEntn + rfBZuf;
		} else {
			return Math.min(fRfBMax, Math.max(fRfBMin, frfbV - fRfB56bEntn + rfBZuf - zielDeklaration));
		}

	}

	/**
	 * Berechnet den Anteil der RfB Zuf�hrung der nach Deklaration und Auff�llen der fRfB auf ihre. <br/>
	 * Funktionsname in Excel: fRfBUeberlauf.
	 * 
	 * @param fRfBV
	 *            Freie RfB zum Zeitpunkt T-1
	 * @param nfRfB56b
	 *            Entnahme aus der nicht festgelegten RfBgem�� �56b VAG
	 * @param rfBZuf
	 *            Zuf�hrung zur gesamten RfB im Jahr T
	 * @param zielDeklaration
	 *            Stochastische Ziel-Deklaration (S�AF und Lock-In zusammen)
	 * @param fRfBMax
	 *            Zul�ssige Obergrenze der freien RfB
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            L�nge der Projektion der Deckungsr�ckstellung
	 * @return RfB Zuf�hrung
	 */
	public static double fRfBUeberlauf(final double fRfBV, final double nfRfB56b, final double rfBZuf,
			final double zielDeklaration, final double fRfBMax, final int t, final int omega) {

		if (t <= omega) {
			return Math.max(Math.max(fRfBV - nfRfB56b, 0.0) + rfBZuf - zielDeklaration - fRfBMax, 0.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die Deklaration der �berschussanteile (S�AF und Lock-In gesamt) zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: Dekl.
	 * 
	 * @param fRfBV
	 *            freie RfB zum Zeitpunkt T-1
	 * @param fRfB
	 *            freie RfB zum Zeitpunkt T
	 * @param rfBZuf
	 *            Zuf�hrung zur gesamten RfB zum Zeitpunkt T
	 * @param nfRfB56bEntnahme
	 *            Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T
	 * @param sUeAf56bEntnahme
	 *            Anteil der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T der auf den S�AF
	 *            entf�llt
	 * @param fRfBMin
	 *            Untergrenze der freien RfB
	 * @param zielDeklaration
	 *            Zieldeklaration (S�AF und Lock-In)
	 * @param pFRfBUeberlauf
	 *            Anteil an dem �berlauf der freien RfB der in die Deklaration flie�t
	 * @param fRfBUeberlauf
	 *            Rest der Zuf�hrung zur gesamten RfB, nachdem die Zieldeklaration gew�hrt und die fRfB auf ihr Maximum
	 *            erh�ht wurde
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            L�nge der Projektion der Deckungsr�ckstellung
	 * @return Deklaration der �berschussanteile
	 */
	public static double dekl(final double fRfBV, final double fRfB, final double rfBZuf, final double nfRfB56bEntnahme,
			final double sUeAf56bEntnahme, final double fRfBMin, final double zielDeklaration,
			final double pFRfBUeberlauf, final double fRfBUeberlauf, final int t, final int omega) {
		if (t > omega) {
			return 0.0;
		} else {
			if (fRfB < fRfBMin) {
				return 0.0;
			} else {
				return Math.min(Math.max(fRfBV - nfRfB56bEntnahme + sUeAf56bEntnahme, 0.0) - fRfB + rfBZuf,
						zielDeklaration + pFRfBUeberlauf * fRfBUeberlauf);
			}
		}
	}

	/**
	 * Ermittelt die zu verteilende Deklaration <br/>
	 * Funktionsname in Excel: Dekl_zins.
	 * 
	 * @param deklMethode
	 *            Deklarationsmethode
	 * @param dekl
	 *            Deklaration
	 * @param deklRest
	 *            Restliche Deklaration
	 * @return der Wert
	 */
	public static double deklZins(final int deklMethode, final double dekl, final double deklRest) {
		if (deklMethode == 1) {
			return dekl;
		} else {
			return dekl - deklRest;
		}
	}

	/**
	 * Ermittelt die restliche Deklaration <br/>
	 * Funktionsname in Excel: Dekl_rest.
	 * 
	 * @param Dekl_methode
	 *            Deklarationsmethode
	 * @param Dekl
	 *            Deklaration Gesamt
	 * @param a_rest_dekl
	 *            Man.Parameter f�r rest. Deklaration
	 * @param RE_alt
	 *            Risikoergebnis, Altbestand
	 * @param RE_neu
	 *            Risikoergebnis, Neubestand
	 * @param uE_alt
	 *            �briges Ergebnis, Altbestand
	 * @param uE_neu
	 *            �briges Ergebnis, Neubestand
	 * @return Wert
	 */
	public static double deklRest(final int Dekl_methode, final double Dekl, final double a_rest_dekl,
			final double RE_alt, final double RE_neu, final double uE_alt, final double uE_neu) {
		if (Dekl_methode == 2) {
			return Math.min(Dekl, Math.max(a_rest_dekl * (RE_alt + RE_neu + uE_alt + uE_neu), 0));
		}
		return 0.0;
	}

	/**
	 * Berechnet die Gesamtverzinsung im Zeitpunkt T. <br/>
	 * Funktionsname in Excel: VZ_Ges.
	 * 
	 * @param deklZins
	 *            Zu verteilende Deklaration zum Zeitpunkt T
	 * @param rzgZeilen
	 *            rzg-Zeilen, die vom entsprechenden Agg Aggregiert werden
	 * @return Gesamtverzinsung
	 */
	public static double vzGes(final double deklZins, final List<RzgZeile> rzgZeilen) {
		// Wenn es nichts zu verteilen gibt ist die H�he der Gesamtverzinsung Null. In jedem anderen Fall
		// muss die Gesamtverzinsung h�her als der kleinste Rechnungszins sein sein
		if (deklZins < 0.001) {
			return 0.0;
		}

		// Mappt zinsGenerationen zu den passenden RzgZeilen:
		final Map<Double, List<RzgZeile>> rzgZuSortValue = new TreeMap<>();
		for (RzgZeile z : rzgZeilen) {
			if (z.getUebNueb().equals("UEB")) {
				final double sortValue = z.getVg().getDrLockInRzg() > 0.0 ? z.getRmZTarif() / z.getVg().getDrLockInRzg()
						: 0.0;

				if (!rzgZuSortValue.containsKey(sortValue)) {
					rzgZuSortValue.put(sortValue, new ArrayList<>());
				}
				rzgZuSortValue.get(sortValue).add(z);
			}
		}
		double dr_summe = 0.0;
		double gew_dr_summe = 0.0;
		double vzGes = 0.0;
		for (double sortValue : rzgZuSortValue.keySet()) {
			for (RzgZeile z : rzgZuSortValue.get(sortValue)) {
				if (vzGes < sortValue && dr_summe > 0.01) {
					return vzGes;
				}
				final double dr = z.getVg().getDrLockInRzg();
				if (dr > 0.01) {
					dr_summe += dr;
					gew_dr_summe += z.getRmZTarif();
					vzGes = (deklZins + gew_dr_summe) / dr_summe;
				}
			}
		}
		return vzGes;
	}

	/**
	 * Berechnet die Deklaration f�r eine Bestandsgruppe. <br/>
	 * Funktionsname in Excel: Dekl_rzg.
	 * 
	 * @param Methode
	 *            Deklarationsmethode
	 * @param vzGes
	 *            Gesamtverzinsung
	 * @param rz
	 *            Rechnungszins in Basispunkten
	 * @param drLockin
	 *            Lock-In Deckungsr�ckstellung Vorjahr
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param uebLob
	 *            Kennzeichen �berschussberechtigtes / nicht �berschussberechtigtes Gesch�ft
	 * @param deklZinsGes
	 *            Deklaration des Gesamtbestands
	 * @param deklRest
	 *            Deklaration, rest
	 * @return der Wert
	 */
	public static double deklRzg(final int Methode, final double vzGes, final double rz, final double drLockin,
			final int t, final String uebLob, final double deklZinsGes, final double deklRest) {
		if (t < 1) {
			return 0.0;
		}
		if (uebLob.equals("NUEB")) {
			return 0.0;
		}
		double deklRzg = 0.0;
		if (drLockin > 0) {
			deklRzg = Math.max(vzGes * drLockin - rz, 0);
		}
		if (Methode == 2) {
			deklRzg = deklRzg + deklRest;
		}
		return deklRzg;
	}

	/**
	 * Berechnet Deklaration aus �brigem und Risikoergebnis auf Bestandsgruppenebene<br/>
	 * Funktionsname in Excel: Dekl_rzg_rest.
	 * 
	 * @param rUeRrestRzg
	 *            Summe der Beitr�ge zum Roh�berschuss, nur positive Teile, �ber m Jahre
	 * @param rUeRest
	 *            Summe der positiven Beitr�ge zum Roh�berschuss
	 * @param deklRest
	 *            Deklaration, rest
	 * @return der Wert
	 */
	public static double deklRzgRest(final double rUeRrestRzg, final double rUeRest, final double deklRest) {
		if (rUeRest > 0.001) {
			return rUeRrestRzg / rUeRest * deklRest;
		}
		return 0.0;
	}

	/**
	 * Berechnet den Anteil der Beitr�ge einer einzelnen Bestandsgruppe zum Roh�berschuss (Deklaration 2. Methode).
	 * <br/>
	 * Funktionsname in Excel: Anteil_Dekl.
	 * 
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param ueBlob
	 *            Kennzeichen, �B/N�B
	 * @param beitragRueRzg
	 *            Beitrag der Bestandsgruppe zum Roh�berschuss
	 * @param beitragRueAgg
	 *            Summe aller Beitr�ge zum Roh�berschuss
	 * @param anteilDeklV
	 *            Anteil der Bestandsgruppe an der Deklaration Vorjahr
	 * @param drLockInV
	 *            Lock-In Deckungsr�ckstellung der Bestandsgruppe Vorjahr
	 * @param sueAfV
	 *            Schluss�berschussanteilsfonds der Bestandsgruppe Vorjahr
	 * @param drLockInUebAggV
	 *            Deckungsr�ckstellung des �berschussberechtigten Gesch�fts Vorjahr
	 * @param sueAfAggV
	 *            Schluss�berschussanteilsfonds Vorjahr
	 * @return der Wert
	 */
	public static double anteilDekl(final int t, final String ueBlob, final double beitragRueRzg,
			final double beitragRueAgg, final double anteilDeklV, final double drLockInV, final double sueAfV,
			final double drLockInUebAggV, final double sueAfAggV) {
		if (t < 1) {
			return 0.0;
		}
		if (ueBlob.equals("NUEB")) {
			return 0.0;
		}
		if (beitragRueAgg > 0.001) {
			return beitragRueRzg / beitragRueAgg;
		} else {
			if (t == 1) {
				return (drLockInV + sueAfV) / (drLockInUebAggV + sueAfAggV);
			} else {
				return nanZero(anteilDeklV);
			}
		}
	}

	/**
	 * Berechnet die Zuf�hrung zum S�AF f�r eine Rechnungszinsgeneration, die im Jahre T durch �berlauf der freien RfB
	 * verursacht wird. <br/>
	 * Funktionsname in Excel: SUeAF_Zuf_fRfBUeberlauf.
	 * 
	 * @param dekl
	 *            Deklaration Gesamt
	 * @param deklRz
	 *            Deklaration Bestandsgruppe
	 * @param pRfBUeberlauf
	 *            Anteil an dem �berlauf der freien RfB der in die Deklaration flie�t
	 * @param fRfBUeberlauf
	 *            Anteil der RfB Zuf�hrung der nach Deklaration und Auff�llen der fRfB auf ihre Maximalgr��e
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param uebLob
	 *            Angabe, ob die Bestandsgruppe �berschussberechtigt ist (UEB) oder nicht (NUEB)
	 * @return der Wert
	 */
	public static double sueafZufFrfbUeberlauf(final double dekl, final double deklRz, final double pRfBUeberlauf,
			final double fRfBUeberlauf, final int t, final String uebLob) {
		if (t != 0 && dekl > 0.001 && uebLob.equals("UEB")) {
			return (1 - pRfBUeberlauf) * fRfBUeberlauf * deklRz / dekl;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die Zuf�hrung zum Schluss�berschussfonds f�r eine Rechnungszinsgeneration zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: SUEAF_ZUF.
	 * 
	 * @param deckStock
	 *            Deckungsstock (Klassisch oder Fonds)
	 * @param pBarAuszAufschub
	 *            Zielbarauszahlung in Anteilen des Risikoergebnisses FLV Aufschubphase
	 * @param pSUeAFZuf
	 *            Zielzuf�hrung zum S�AF in Prozent der Deklaration
	 * @param pSUeAFZufMin
	 *            Mindestzuf�hrung zum S�AF in Prozent der garantierten Deckungsr�ckstellung
	 * @param drLockin
	 *            Lock-In-Deckungsr�ckstellung f�r die Bestandsgruppe Vorjahr
	 * @param dekl
	 *            Deklaration f�r die Bestandsgruppe
	 * @param t
	 *            Zeitpunkt
	 * @param sUeAFZuffRfBUeberlauf
	 *            Zuf�hrung zum S�AF f�r eine Bestandsgruppe, die durch �berlauf der freien RfB verursacht wird
	 * @param reDet
	 *            deterministisches Risikoergebnis Folgejahr
	 * @param uEdet
	 *            ?
	 * @param omega
	 *            Projektionsl�nge VT
	 * @return der Wert
	 */
	public static double sUeAfzuf(final String deckStock, final double pBarAuszAufschub, final double pSUeAFZuf,
			final double pSUeAFZufMin, final double drLockin, final double dekl, final int t,
			final double sUeAFZuffRfBUeberlauf, final double reDet, final double uEdet, final int omega) {
		if (deckStock.equals("Fonds")) {
			if (t >= omega) {
				return 0.0;
			} else {
				return Math.max(dekl - Math.max(pBarAuszAufschub * (reDet + uEdet), 0), 0) + sUeAFZuffRfBUeberlauf;
			}
		} else {
			if (t != 0) {
				return Math.min(Math.max(pSUeAFZufMin * drLockin, pSUeAFZuf * dekl), dekl) + sUeAFZuffRfBUeberlauf;
			}
		}
		return 0.0;
	}

	/**
	 * Nur bei Deckungsstock Fonds: Berechnet die Entnahme aus dem S�AF, die durch den �bergang in ide Rentenphase
	 * ausgel�st wird. <br/>
	 * Funktionsname in Excel: SUeAF_FLV_Bewegung_aus.
	 * <p/>
	 * In die urspr�ngliche Excel-Funktion wurden FLV-Ranges �bergeben. Das waren:
	 * <ul>
	 * <li>LoBWert - Erste Zelle zum gleichen Zeitpunkt mit Bezeichnung LoB im Blatt flv, flv!C5;</li>
	 * <li>RZGWert - Erste Zelle zum gleichen Zeitpunkt mit Bezeichnung RZG im Blatt flv, flv!E5;</li>
	 * <li>alt_neuWert - Erste Zelle zum gleichen Zeitpunkt mit alt/neu-Kennzeichen im Blatt flv, flv!F5;</li>
	 * <li>Beitrag_stoch - Erste Zelle zum gleichen Zeitpunkt mit stoch. Einmal-Rentenbeitrag im Blatt flv; flv!AE5;
	 * </li>
	 * <li>L_KA_Aufschub_stoch - Erste Zeile zum gleichen Zeitpunkt mit stoch. Kapitalabfindung im Blatt flv; flv!AB5;
	 * </li>
	 * <li>Fondwert_stoch - Erste Zelle zum gleichen Zeitpunkt mit stoch. Fondwert im Blatt flv; flv!W5;</li>
	 * </ul>
	 * 
	 * @param sueAfV
	 *            Schluss�berschussanteilsfonds
	 * @param sueAfZuf
	 *            ?
	 * @param sueAf56bEntnahme
	 *            ?
	 * @param flvFlag
	 *            Kennzeichen Klassik oder FLV
	 * @param kdsFlag
	 *            Kennzeichen klassischer Deckungsstock (KDS) oder Fonds
	 * @param flv
	 *            FlvZeile mit den Daten, die urspr�nglich direkt �bergeben wurden (siehe oben)
	 * @param omega
	 *            max. Projektionslaenge (Zeitpunkte)
	 * @param omegaRzg
	 *            max. Projektionslaenge (Zeitpunkte), Bestandsgruppe
	 * @param t
	 *            Zeit
	 * @return der Wert
	 */
	public static double sueAfFlvBewegungAus(final double sueAfV, final double sueAfZuf, final double sueAf56bEntnahme,
			final String flvFlag, final String kdsFlag, final FlvZeile flv, final int omega, final int omegaRzg,
			final int t) {
		if (flvFlag.equals("FLV") && kdsFlag.equals("Fonds")) {
			if (flv.getFondguthabenStochZp() + flv.getlKaAufschubStoch() > 0.0 && t < omegaRzg) {
				return flv.getBeitragRenteStoch() / (flv.getFondguthabenStochZp() + flv.getlKaAufschubStoch())
						* (sueAfV + sueAfZuf - sueAf56bEntnahme);
			}
			if (flv.getBeitragRenteStoch() + flv.getlKaAufschubStoch() > 0.0 && t == omegaRzg) {
				return flv.getBeitragRenteStoch() / (flv.getFondguthabenStochZp() + flv.getlKaAufschubStoch())
						* (sueAfV + sueAfZuf - sueAf56bEntnahme);
			}
		}
		return 0.0;
	}

	/**
	 * Berechnet die Sonderzahlung, welche im Jahr T anfallen, wenn T das letzte Jahr ist, indem Leistungen f�r diese
	 * Bestandsgruppe zu erf�llen sind. Zahlt in diesem Fall den restlichen S�AF, die LockIn und die Barauszahlung aus
	 * dem jahr T aus. <br/>
	 * Funktionsname in Excel: EndZahlung.
	 * 
	 * @param deckungsStock
	 *            ?
	 * @param sUeAfEntnahme
	 *            Entnahme aus dem S�AF
	 * @param lockIn
	 *            Stochastische Lock-In der �berschussbeteiligung zum Zeitpunkt T
	 * @param bar
	 *            H�he der Barauszahlung zum Zeitpunkt T
	 * @param fRfB
	 *            Freie RfB zum Zeitpunkt T
	 * @param drLockInUebV
	 *            �ber die �berschussberechtigten Best�nde aggregierte Deckungsr�ckstellung (inkl. Lock-In) zum
	 *            Zeitpunkt T-1
	 * @param drLockInRzgV
	 *            Deckungsr�ckstellung der Bestandsgruppe (inkl. Lock-In) zum Zeitpunkt T-1
	 * @param uebLob
	 *            Angabe ob die Bestandsgruppe �berschussberechtigt (UEB) oder nicht �berschussberechtigt (NUEB) ist
	 * @param t
	 *            Zeitpunkt
	 * @param omegaRzg
	 *            Projektionshorizont der Bestandsgruppe
	 * @param omega
	 *            Projektionshorizont
	 * @param lbw
	 *            Leistungsbarwert n�chstes Jahr
	 * @return der Wert
	 */
	public static double endZahlung(final String deckungsStock, final double sUeAfEntnahme, final double lockIn,
			final double bar, final double fRfB, final double drLockInUebV, final double drLockInRzgV,
			final String uebLob, final int t, final int omegaRzg, final int omega, final double lbw) {
		double endZahlung = 0.0;
		if (lbw <= 0.001 && deckungsStock.equals("KDS")) {
			endZahlung = sUeAfEntnahme + lockIn + bar;
		}
		if (t == omega && deckungsStock.equals("KDS") && uebLob.equals("UEB")
				&& Functions.nanZero(drLockInUebV) > 0.001) {
			endZahlung += fRfB * Functions.nanZero(drLockInRzgV) / drLockInUebV;
		}
		if (deckungsStock.equals("Fonds")) {
			endZahlung = sUeAfEntnahme + bar;
		}
		return endZahlung;
	}

	/**
	 * Berechnet die H�he der Barauszahlung zum Zeitpunkt T <br/>
	 * Funktionsname in Excel: Bar
	 * 
	 * @param deckStock
	 *            Deckungsstock (Klassisch oder Fonds)
	 * @param pBarAuszKlassik
	 *            Zielanteil der Barauszahlung an der Deklaration
	 * @param beitrag
	 *            deterministisch projezierte Beitrag zum VU-Zeitpunkt in t
	 * @param dekl
	 *            Stochastische Deklaration der �berschussanteile (S�AF und Lock gesamt) f�r die Rechnungszinsgeneration
	 *            zum Zeitpunkt T
	 * @param sueafZuf
	 *            Die Zuf�hrung zum Schluss�berschussfonds f�r die Rechnungszinsgeneration zum Zeitpunkt T
	 * @param sueafZuffRfB�berlauf
	 *            Zuf�hrung zum S�AF f�r eine Rechnungszinsgeneration, die im Jahre T durch �berlauf der freien RfB
	 *            verursacht wird
	 * @return der Wert
	 */
	public static double bar(final String deckStock, final double pBarAuszKlassik, final double beitrag,
			final double dekl, final double sueafZuf, final double sueafZuffRfB�berlauf) {

		if (deckStock.equals("Fonds")) {
			return Math.max(dekl - (sueafZuf - sueafZuffRfB�berlauf), 0);
		} else {
			return Math.min(pBarAuszKlassik * beitrag, Math.max(dekl - (sueafZuf - sueafZuffRfB�berlauf), 0));
		}
	}

	/**
	 * Berechnet die stochastische Lock-In der �berschussbeteiligung zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: LockIn.
	 * 
	 * @param dekl
	 *            Stochastische Deklaration der �berschussanteile (S�AF und Lock gesamt) f�r die Rechnungszinsgeneration
	 *            zum Zeitpunkt T
	 * @param sUeAfzuf
	 *            Die Zuf�hrung zum Schluss�berschussfonds f�r die Rechnungszinsgeneration zum Zeitpunkt T
	 * @param sUeAfZufFRfBUeberlauf
	 *            Zuf�hrung zum S�AF f�r eine Rechnungszinsgeneration, die im Jahre T durch �berlauf der freien RfB
	 *            verursacht wird
	 * @param bar
	 *            H�he der Barauszahlung zum Zeitpunkt T
	 * @param barRe
	 *            Going Concern reserve aus dem Risikoergebnis
	 * @return Lock-In der �berschussbeteiligung
	 */
	public static double lockIn(final double dekl, final double sUeAfzuf, final double sUeAfZufFRfBUeberlauf,
			final double bar, final double barRe) {
		return Math.max(dekl - bar - Math.min(sUeAfzuf - sUeAfZufFRfBUeberlauf, dekl), 0);
	}

	/**
	 * Berechnet die (aggregierte) garantierte Erh�hung der anf�nglich garantierten Leistungen zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: LE_LockIn_Aggr.
	 * 
	 * @param vgLeLockInAggr
	 *            Garantierte Leistungserh�hung durch LockIn zum Zeitpunkt T-1
	 * @param lockIn
	 *            Die stochastische Lock-In der �berschussbeteiligung zum Zeitpunkt T
	 * @param lbwGar
	 *            Der Barwert der (ab T f�lligen) garantierten Leistungscashflows, gebildet mit dem Rechnungszins
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @return garantierten Leistungen
	 */
	public static double leLockInAggr(final double vgLeLockInAggr, final double lockIn, final double lbwGar,
			final int t) {
		if (t != 0 && lbwGar > 0.001) {
			return Functions.nanZero(vgLeLockInAggr) + lockIn / lbwGar;
		} else {
			return 0.0;

		}
	}

	/**
	 * Berechnet die garantierten Leistungen zum Zeitpunkt T, inklusive garantierter Leistungserh�hungen (Lock-In).
	 * <br/>
	 * Funktionsname in Excel: L_gar_stoch.
	 * 
	 * @param lGarantiert
	 *            Deterministisch projezierte, garantierte Leistungen im Jahr T unter Ber�cksichtigung der
	 *            stochastischen Kapitalwahl
	 * @param leLockInAggrFLV
	 *            Aggregierte garantierte Erh�hung der anf�nglich garantierten Leistungen zum Zeitpunkt T
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return garantierten Leistungen
	 */
	public static double lGarStoch(final double lGarantiert, final double leLockInAggrFLV, final double vgLambda) {
		return lGarantiert * (1.0 + nanZero(leLockInAggrFLV)) * vgLambda;

	}

	/**
	 * Berechnet den Betrag des im Folgejahr auszuzahlenden Schluss�berschussanteils. <br/>
	 * Funktionsname in Excel: SUEAF_Entnahme.
	 * <p/>
	 * Statt der flvZeilen werden in Excel einzelne Paramter �bergeben und im Programm als Ranges definiert. Die
	 * folgende Liste enth�lt die Ranges, die Parameter, die Spalten in Rzg und die passenden getter in flv. Dabei
	 * werden von vornherein nur Flv mit gleichem LoB, RZG und alt/neu �bergeben, so dass die entsprechende Selektion
	 * hier entfallen kann.
	 * <ul>
	 * <li>ran_LoBWert = LoBWert: flv!C4; vg.getLob (Achtung: zeitlicher Vorg�nger!)</li>
	 * <li>ran_RZGWert = RZGWert: flv!E5; getZinsGeneration</li>
	 * <li>ran_alt_neu_Wert = alt_neuWert: flv!F5; getAltNeu</li>
	 * <li>ran_Beitrag_stoch = Beitrag_stoch: flv!AE5; getBeitragRenteStoch</li>
	 * <li>ran_L_KA_stoch = L_KA_Aufschub_stoch: flv!AB5; getlKaAufschubStoch</li>
	 * <li>ran_Fondwert_stoch = Fondwert_stoch: flv!W5 getFondguthabenStochZp</li>
	 * </ul>
	 * 
	 * 
	 * @param leSueAf
	 *            Die (aggregierte) garantierte Erh�hung der anf�nglich garantierten Leistungen
	 * @param lGarN
	 *            Deterministisch projezierten, garantierten Leistungen im Jahr T+1
	 * @param lbwGar
	 *            Leistungsbarwert der zuk�nftigen Leistungen Ende Jahr T
	 * @param sueAfV
	 *            Wert des S�AF im Jahr T-1
	 * @param sueAfZuf
	 *            S�AF Zuf�hrung im Jahr T
	 * @param sueAf56bEntnahme
	 *            S�AF 56b-Entnahme im Jahr T
	 * @param kaGarXsN
	 *            Excess-Betrag der garantietren Kapitalabfindung im jahr T+1
	 * @param lambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T
	 * @param monat
	 *            Monat der Zahlungseing�nge (VU-Zeitpunkt)
	 * @param kdsFlag
	 *            Kennzeichen klassischer Deckungsstock (KDS) oder Fonds
	 * @param rzg
	 *            Rechnungszinsgeneration
	 * @param flvZeile
	 *            Flv-Zeile zu den n�chsten sechs Excel-Parametern, siehe Doku
	 * @param omega
	 *            max. Projektionslaenge (Zeitpunkte)
	 * @param omegaRzg
	 *            Projektionsl�nge, Versicherungstechnik
	 * @param t
	 *            Zeitpunkt
	 * @return der Wert
	 */
	public static double sUeAfEntnahme(final double leSueAf, final double lGarN, final double lbwGar,
			final double sueAfV, final double sueAfZuf, final double sueAf56bEntnahme, final double kaGarXsN,
			final double lambda, final double monat, final String kdsFlag, final int rzg, final FlvZeile flvZeile,
			final int omega, final int omegaRzg, final int t) {
		switch (kdsFlag) {
		case "KDS":
			if (lbwGar != 0.0) { // Wenn LBW_gar = 0 ist ist auch L_gar_n = 0 (keine S�AF_Entnahme)
				return leSueAf * (lGarN * lambda + kaGarXsN) / Math.pow(1 + inProzent(rzg), monat / 12.0);
			} else {
				return sueAfV + sueAfZuf - sueAf56bEntnahme;
			}
		case "Fonds":
			if (flvZeile.getFondguthabenStochZp() + flvZeile.getlKaAufschubStoch() > 0.0 && t < omegaRzg) {
				return flvZeile.getlKaAufschubStoch()
						/ (flvZeile.getFondguthabenStochZp() + flvZeile.getlKaAufschubStoch())
						* (sueAfV + sueAfZuf - sueAf56bEntnahme);
			}
			if (flvZeile.getBeitragRenteStoch() + flvZeile.getlKaAufschubStoch() > 0.0 && t == omegaRzg) {
				return flvZeile.getlKaAufschubStoch()
						/ (flvZeile.getBeitragRenteStoch() + flvZeile.getlKaAufschubStoch())
						* (sueAfV + sueAfZuf - sueAf56bEntnahme);
			}
		}
		return 0.0;

	}

	/**
	 * Berechnet welcher Betrag der Entnahme aus dem S�AF nach �56b VAG, auf die einzelne Rechnungszinsgenerationen
	 * entf�llt. <br/>
	 * Funktionsname in Excel: S�AF_56b_Entnahme_rzg.
	 * 
	 * @param sueAfGes
	 *            S�AF zum Zeitpunkt T-1, aggregiert �ber alle Rechnungszinsgenerationen
	 * @param sueAf
	 *            S�AF zum Zeitpunkt T-1 (f�r die Rechnungszinsgeneration)
	 * @param sUeAf56bEntnahmeGes
	 *            Betrag der Entnahme aus der nicht festgelegten RfB gem�� �56b VAG zum Zeitpunkt T der auf den
	 *            (gesamten) S�AF entf�llt
	 * @param t
	 *            Zeitpunkt
	 * @return Betrag
	 */
	public static double sUeAf56bEntnahmeRzg(final double sueAfGes, final double sueAf,
			final double sUeAf56bEntnahmeGes, final int t) {
		if (t != 0) {
			if (sueAfGes < 0.001) {
				return 0.0;
			} else {
				return (sueAf / sueAfGes) * sUeAf56bEntnahmeGes;
			}
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die (�ber die Zeit aggregierte) garantierte Erh�hung der anf�nglich garantierten Leistungen zum
	 * Zeitpunkt T. <br/>
	 * Funktionsname in Excel: LE_SUEAF.
	 * 
	 * @param sueAfV
	 *            S�AF im Vorjahr (Zeitpunkt T-1) auf Rechnungszinsgenerationsebene.
	 * @param sUeAfzuf
	 *            Die Zuf�hrung zum S�AF auf Rechnungszinsgenerationsebene.
	 * @param sUeAf56bEntnahme
	 *            Betrag der Entnahme aus dem S�AF nach �56b VAG, der auf die einzelne Rechnungszinsgenerationen
	 *            entf�llt
	 * @param lbwGar
	 *            Der Barwert der (ab T f�lligen) garantierten Leistungscashflows, gebildet mit dem Rechnungszins
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @return garantierten Leistungen
	 */
	public static double leSUeAf(final double sueAfV, final double sUeAfzuf, final double sUeAf56bEntnahme,
			final double lbwGar, final int t) {
		if (t != 0 && lbwGar > 0.001) {
			return (sueAfV + sUeAfzuf - sUeAf56bEntnahme) / lbwGar;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den S�AF auf Rechnungszinsebene zum Zeitpunkt T <br/>
	 * Funktionsname in Excel: SUEAF_rzg.
	 * 
	 * @param sueafV
	 *            S�AF auf Rechnungszinsebene zum Zeitpunkt T-1
	 * @param sueafZuf
	 *            Zuf�hrung zum S�AF zum Zeitpunkt T
	 * @param sueafEnt
	 *            Entnahme aus dem S�AF zur Erh�hung der garantierten Leistungen
	 * @param sueaf56bEntnahme
	 *            Entnahme aus dem S�AF nach �56b VAG
	 * @param bewegungAus
	 *            Entnahme aus dem S�AF bei Eintritt in Rentenphase
	 * @param bewegungIn
	 *            Zuf�hrung zum S�AF durch Eintritt in Rentenphase
	 * @return der Wert
	 */
	public static double sueafRzg(final double sueafV, final double sueafZuf, final double sueafEnt,
			final double sueaf56bEntnahme, final double bewegungAus, final double bewegungIn) {
		return sueafV + sueafZuf - sueafEnt - sueaf56bEntnahme + bewegungIn - bewegungAus;

	}

	/**
	 * Berechnet die gesamte Erh�hung der anf�nglich garantierten Leistungen duch Deklartion (Lock-In und S�AF). <br/>
	 * Funktionsname in Excel: LE_Gesamt_Aggr.
	 * 
	 * @param leLockInAggr
	 *            Garantierte Erh�hung der anf�nglich garantierten Leistungen
	 * @param leSUeAf
	 *            Erh�hung der anf�nglich garantierten Leistungen durch Schluss�berschussanteil
	 * @return garantierten Leistungen
	 */
	public static double leGesamtAggr(final double leLockInAggr, final double leSUeAf) {
		return leLockInAggr + leSUeAf;
	}

	/**
	 * Berechnet die H�he des gesamten Leistungscashflows, nach Erh�hungen (aus Lock-In und S�AF). <br/>
	 * Funktionsname in Excel: L_gesamt.
	 * 
	 * @param leGesamt
	 *            Die gesamte Erh�hung der anf�nglich garantierten Leistungen duch Deklartion (Lock-In und S�AF) zum
	 *            Zeitpunkt T-1
	 * @param lGar
	 *            Wert des anf�nglich garantierter Leistungs-Cashflow zum Zeitpunkt T
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rz
	 *            Rechnungszins
	 * @param vgBar
	 *            Barauszahlung im Vorjahr
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @param kaGarXs
	 *            Excess-Betrag der garantierten Kapitalabfindung im Jahr T
	 * @param rkwXs
	 *            Excess-Betrag der R�ckk�ufe im Jahr T
	 * @param monat
	 *            Monat der Zahlungseing�nge
	 * @param lbw
	 *            ?
	 * @return Leistungscashflow
	 */
	public static double lGesamt(final double leGesamt, final double lGar, final int t, final int rz,
			final double vgBar, final double vgLambda, final double kaGarXs, final double rkwXs, final double monat,
			final double lbw) {
		double lGesamt;
		if (t != 0) {
			lGesamt = (1.0 + nanZero(leGesamt)) * (vgLambda * lGar + kaGarXs) + rkwXs;

			if (lbw > 0.001) {
				lGesamt += nanZero(vgBar) * Math.pow(1.0 + inProzent(rz), monat / 12.0);
			}
		} else {
			lGesamt = lGar;
		}
		return lGesamt;
	}

	/**
	 * Berechnet die ben�tigte Deckungsr�ckstellung f�r die garantierten Leistungen inkl. Lock-In f�r eine
	 * Rechnungszinsgeneration zum Ende des Jahres T. <br/>
	 * Funktionsname in Excel: DR_LockIn_rzg.
	 * 
	 * @param drHgb
	 *            Deckungsr�ckstellung f�r �nf�nglich garantierte Leistungen im Jahr T
	 * @param leLockInAggrFLV
	 *            Garantierte Erh�hung der anf�nglich garantierten Leistungen im Jahr T
	 * @param lbwGar
	 *            Barwert der ab T+1 f�lligen garantierten Leistungscashflows, diskontiert mit dem Rechnungszins
	 * @param sUeAfEntnahme
	 *            Entnahme aus dem S�AF zur Erh�hung der garantierten Leistungen
	 * @param bar
	 *            H�he der Barauszahlung
	 * @param lbwSonstErl
	 *            Barwert der ab T+1 f�lligen, garantierten sonstigen Erlebensfallleistungen, diskontiert mit dem
	 *            Rechnungszins
	 * @param laKapWahlXsAggr
	 *            Leistungsanpassung der sonstigen Erlebensfallleistungen durch pfadabh�ngige Kapitalwahl�nderung im
	 *            Jahr T
	 * @param lambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T
	 * @return Deckungsr�ckstellung
	 */
	public static double drLockInRzg(final double drHgb, final double leLockInAggrFLV, final double lbwGar,
			final double sUeAfEntnahme, final double bar, final double lbwSonstErl, final double laKapWahlXsAggr,
			final double lambda) {
		if (lbwGar > 0.001) {
			return (drHgb + nanZero(laKapWahlXsAggr) * nanZero(lbwSonstErl)) * lambda
					+ nanZero(leLockInAggrFLV) * nanZero(lbwGar) + nanZero(sUeAfEntnahme) + nanZero(bar);
		} else {
			return 0.0;
		}
	}

	/**
	 * Gesamte Deckungsr�ckstellung, inklusive aller Leistungserh�hungen duch S�AF und LockIn zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: DR_Gesamt_rzg.
	 * 
	 * @param drLockInRzg
	 *            Deckungsr�ckstellung f�r garantierte Leistungen, inklusive garantierter Leistungserh�hung zum
	 *            Zeitpunkt T
	 * @param sUeAfRzg
	 *            Verbleibender Teil des Schluss�berschussfond zum Projektionsbeginn zum Zeitpunkt T
	 * @return Deckungsr�ckstellung
	 */
	public static double drGesamtRzg(final double drLockInRzg, final double sUeAfRzg) {
		return nanZero(drLockInRzg) + nanZero(sUeAfRzg);
	}

	/**
	 * Berechnet die Summe der Beitr�ge zum Roh�berschuss, nur positive Teile, �ber m Jahre. <br/>
	 * Funktionsname in Excel: Beitrag_RUe_rzg.
	 * 
	 * @param ueblob
	 *            Kennzeichen �berschussberechtigtes / nicht �berschussberechtigtes Gesch�ft
	 * @param rE
	 *            stoch. Risikoergebnis der Bestandsgruppe
	 * @param uE
	 *            stoch. �briges Ergebnis der Bestandsgruppe
	 * @return der Wert
	 */
	public static double beitragRueRzg(final String ueblob, final double rE, final double uE) {
		if (ueblob.equals("UEB")) {
			return Math.max(rE + uE, 0.0);
		}
		return 0.0;
	}
}
