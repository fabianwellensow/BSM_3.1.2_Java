package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

/**
 * VU-Funktionen f�r die Bilanzpositionen.
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
public class Bilanzpositionen {

	/**
	 * Berechnet die Deklaration die f�r die Berechnung des stochastischen Surplusfonds relevant ist zum Zeitpunkt T.
	 * <br/>
	 * Funktionsname in Excel: Dekl_Surplus_fRfB.
	 * 
	 * @param fRfBHgb
	 *            Startwert der freien RfB (zum Zeitpunkt 0)
	 * @param dekl
	 *            Gesamtdeklaration und S�AF-Zuf�hrung wegen fRfB-�berlauf zum Zeitpunkt T
	 * @param deklV
	 *            Historie der Deklaration (Vektor der Werte [Dekl(0), ..., Dekl(T-1)
	 * @param fRfB56bEntnahmeV
	 *            Historie der Entnahme aus der freien RfB (Vektor der Werte [nfRfB_56b_Entnahme(0), ...,
	 *            nfRfB_56b_Entnahme(T-1)])
	 * @return Deklaration
	 */
	public static double deklSurplusFRfB(final double fRfBHgb, final double dekl, final double[] deklV,
			final double[] fRfB56bEntnahmeV) {
		return Math.max(Math.min(dekl, fRfBHgb - Functions.sum(deklV) - Functions.sum(fRfB56bEntnahmeV)), 0);
	}

	/**
	 * Berechnet die SUAEF_Entnahmen zum Zeitpunkt T, die f�r die Berechnung des stochastischen Surplusfonds relevant
	 * sind. <br/>
	 * Funktionsname in Excel: SUEAF_Ent_SF.
	 * 
	 * @param sueafHgb
	 *            Startwert S�AF (zum Zeitpunkt 0)
	 * @param sueafEnt
	 *            S�AF-Entnahme zum Zeitpunkt T
	 * @param sueafZufSF
	 *            S�AF-Zuf�hrung aus dem �berschussfonds
	 * @param sueafEntSFv
	 *            Historie der S�AF-Entnahmen (Vektor der Werte bis t-1)
	 * @param sueafEnt56bv
	 *            Historie der 56b-Entnahmen aus anf�nglichen S�AF (Vektor der Werte bis t-1)
	 * @return der Wert
	 */
	public static double sueafEntSf(final double sueafHgb, final double sueafEnt, final double sueafZufSF[],
			final double sueafEntSFv[], final double sueafEnt56bv[]) {
		return Math.max(Math.min(
				sueafHgb + Functions.sum(sueafZufSF) - Functions.sum(sueafEntSFv) - Functions.sum(sueafEnt56bv),
				sueafEnt), 0.0);
	}

	/**
	 * Berechnet die SUAEF_Zuf�hrungen zum Zeitpunkt T, die f�r die Berechnung des stochastischen Surplusfonds relevant
	 * sind. Funktionsname in Excel: SUEAF_Zuf_SF.
	 * 
	 * @param dekl
	 *            Gesamtdeklaration zum Zeitpunkt T
	 * @param sUeAFRfbUeberlauf
	 *            fRfB-�berlauf -> S�AF Zuf�hrung
	 * @param sUeAFZuf
	 *            S�AF-Zuf�hrung
	 * @param deklSf
	 *            Deklaration aus dem �berschussfonds
	 * @return SUAEF_Zuf�hrungen
	 */
	public static double sUeAfZufSf(final double dekl, final double sUeAFRfbUeberlauf, final double sUeAFZuf,
			final double deklSf) {
		if (dekl + sUeAFRfbUeberlauf > 0.001) {
			return deklSf * sUeAFZuf / (dekl + sUeAFRfbUeberlauf);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet das LockIn zum Zeitpunkt T, die f�r die Berechnung des stochastischen Surplusfonds relevant sind.<br/>
	 * Funktionsname in Excel: LockIn_SF.
	 * 
	 * @param deklSf
	 *            Deklaration aus dem �berschussfonds
	 * @param barSf
	 *            Barauszahlung aus dem �berschussfonds
	 * @param sUeAfZufSf
	 *            S�AF-Zuf�hrung aus dem �berschussfonds
	 * @return LockIn
	 */
	public static double lockInSf(final double deklSf, final double barSf, final double sUeAfZufSf) {
		return deklSf - barSf - sUeAfZufSf;
	}

	/**
	 * Berechnet die Leistungserh�hung, aggregiert, zum Zeitpunkt T, die f�r die Berechnung des stochastischen
	 * Surplusfonds relevant sind. <br/>
	 * Funktionsname in Excel: LE_AGGR_SF.
	 * 
	 * @param leAggrSfV
	 *            aggr. Leistungserh�hung durch den �berschussfonds
	 * @param lockIn
	 *            LockIn, gesamt
	 * @param lockInSf
	 *            LockIn aus dem �berschussfonds
	 * @param lbw
	 *            Leistungsbarwert
	 * @param nfRfBV
	 *            nicht festgelegte RfB (fRfB + S�AF)
	 * @param vgDrLockInUebV
	 *            Deckungsr�ckstellung mit LockIn f�r �b. Gesch�ft
	 * @return Leistungserh�hung
	 */
	public static double leAggrSf(final double leAggrSfV, final double lockIn, final double lockInSf, final double lbw,
			final double nfRfBV, final double vgDrLockInUebV) {
		final double zinsesZins;

		if ((nanZero(nfRfBV) + nanZero(vgDrLockInUebV)) > 0.001) {
			zinsesZins = (lockIn - lockInSf) / (nanZero(nfRfBV) + nanZero(vgDrLockInUebV));
		} else {
			zinsesZins = 0.0;
		}

		if (lbw > 0.001) {
			return nanZero(leAggrSfV) * (1 + zinsesZins) + lockInSf / lbw;
		} else {
			return nanZero(leAggrSfV) * (1 + zinsesZins);
		}
	}

	/**
	 * Berechnet die Barauszahlung zum Zeitpunkt T, die f�r die Berechnung des stochastischen Surplusfonds relevant
	 * sind. <br/>
	 * Funktionsname in Excel: Bar_SF.
	 * 
	 * @param dekl
	 *            Gesamtdeklaration zum Zeitpunkt T
	 * @param sUeAFZuffRfBUeberlaufAgg
	 *            fRfB-�berlauf -> S�AF Zuf�hrung
	 * @param barAgg
	 *            Barauszahlung
	 * @param deklsurplusfRfB
	 *            Deklaration aus dem �berschussfonds
	 * @return Barauszahlung
	 */
	public static double barSf(final double dekl, final double sUeAFZuffRfBUeberlaufAgg, final double barAgg,
			final double deklsurplusfRfB) {
		if (dekl + sUeAFZuffRfBUeberlaufAgg > 0.001) {
			return deklsurplusfRfB * barAgg / (dekl + sUeAFZuffRfBUeberlaufAgg);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Cashflows, der in die Berechnung von �berschussfond einfliessen. <br/>
	 * Funktionsname in Excel: Cashflow_SF.
	 * 
	 * @param barSf
	 *            Barauszahlung aus dem �berschussfonds
	 * @param sUeAfEnt
	 *            S�AF-Entnahme aus dem �berschussfonds
	 * @param leSf
	 *            aggr. Leistungeserh�hung aus dem �berschussfonds
	 * @param lTod
	 *            Leistung, Tod
	 * @param lka
	 *            Leistung, Kapitalabfindung
	 * @param lRkw
	 *            Leistung, R�ckkaufswert
	 * @param lSonstErl
	 *            Leistung, sonstiger Erlebensfall
	 * @param jaehrlZins
	 *            j�hrlicher ESG-Zins zum Aufzinsen
	 * @param monat
	 *            F�lligekeitmonat, VT-Cashflows
	 * @param t
	 *            Projektionszeitpunkt
	 * @param omega
	 *            L�nge der Projektion der DRSt
	 * @return Cashflow
	 */
	public static double cashflowSf(final double barSf, final double sUeAfEnt, final double leSf, final double lTod,
			final double lka, final double lRkw, final double lSonstErl, final double jaehrlZins, final double monat,
			final int t, final int omega) {
		final double lockinAufgezinst;

		if (t <= omega) {
			lockinAufgezinst = leSf * (lTod + lka + lRkw + lSonstErl) * Math.pow(1 + jaehrlZins, 1.0 - monat / 12.0);
		} else {
			lockinAufgezinst = 0.0;
		}
		return barSf + sUeAfEnt + lockinAufgezinst;
	}

	/**
	 * Berechnet Differenz zw. Gesamt- und garantierten Leistungen. <br/>
	 * Funktionsname in Excel: Delta_L_rzg.
	 * 
	 * @param lGesamt
	 *            Leistung, Gesamt
	 * @param lGar
	 *            Wert des anf�nglich garantierter Leistungs-Cashflow zum Zeitpunkt T
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param lambdaV
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @param kaGarXs
	 *            Excess-Betrag der garantierten Kapitalabfindung im Jahr T
	 * @param rkwXs
	 *            Excess-Betrag der R�ckk�ufe im Jahr T
	 * @param laFlvV
	 *            Leistungsanpassung FLV
	 * @param lEnd
	 *            Leistungen durch Endzahlung
	 * @return Differenz
	 */
	public static double deltaLRzg(final double lGesamt, final double lGar, final int t, final double lambdaV,
			final double kaGarXs, final double rkwXs, final double laFlvV, final double lEnd) {
		if (t == 0) {
			return 0.0;
		} else {
			final double lGarKv = (1.0 + nanZero(laFlvV)) * (lambdaV * lGar + kaGarXs) + rkwXs;
			return lGesamt + lEnd - lGarKv;
		}
	}

	/**
	 * Berechnet die Deklaration, die f�r die Berechnung des stochastischen Surplusfonds relevant ist, zum Zeitpunkt T
	 * auf Bestandsgruppenebene. <br/>
	 * Funktionsname in Excel: Surplusfond_rzg
	 * 
	 * @param cfSurplus
	 *            Cashflow, der f�r die Berechnung des stochastischen Surplusfonds relevant ist
	 * @param deltaLn
	 *            Delta zw. gar. und ges. Leistunge im Jahr t+1
	 * @param deltaL
	 *            Delta zw. gar. und ges. Leistunge im Jahr t
	 * @param deltaLRzgN
	 *            Delta zw. gar. und ges. Leistunge im Jahr t+1 f�r die rzg-Bestandsgruppe
	 * @param deltaLRzg
	 *            Delta zw. gar. und ges. Leistunge im Jahr t f�r die rzg-Bestandsgruppe
	 * @param DR_ges
	 *            Lock-In-Deckungsr�ckstellung �berschussberechtigtes Gesch�ft
	 * @param DR_ges_rz
	 *            Lock-In-Deckungsr�ckstellung der Bestandsgruppe
	 * @param Flag_ueb
	 *            Kennzeichen �berschussberechtigtes Gesch�ft
	 * @return der Wert
	 */
	public static double surplusfondRzg(final double cfSurplus, final double deltaLn, final double deltaL,
			final double deltaLRzgN, final double deltaLRzg, final double DR_ges, final double DR_ges_rz,
			final String Flag_ueb) {
		if (!Flag_ueb.equals("UEB")) {
			return 0.0;
		}
		if (deltaLn > 0.001) {
			return cfSurplus * deltaLRzgN / deltaLn;
		} else {
			if (deltaL > 0.001) {
				return cfSurplus * deltaLRzg / deltaL;
			} else {
				if (DR_ges > 0.001) {
					return cfSurplus * DR_ges_rz / DR_ges;
				} else {
					return 0.0;
				}
			}
		}
	}

	/**
	 * Berechnet Roh�berschuss, geschl�sselt auf k�nftige Pr�mien. <br/>
	 * Funktionsname in Excel: Rohueb_kp_rzg.
	 * 
	 * @param aKe
	 *            anrechenbare Kapitalertr�ge
	 * @param ke
	 *            Kapitalertrag abzgl. Aufwand Kapitalanlage
	 * @param drGes
	 *            Lock-In-Deckungsr�ckstellung aggregiert inkl. S�AF
	 * @param drGesRzg
	 *            Lock-In-Deckungsr�ckstellung pro RZG inkl. S�AF
	 * @param rmz
	 *            rechnungsm��iger Zinsaufwand zzgl. Aufwand ZZR
	 * @param re
	 *            Risikoergebnis
	 * @param ueE
	 *            �briges Ergebnis
	 * @param drV
	 *            Deckungsr�ckstellung Vorjahr
	 * @param drKpV
	 *            Deckungsr�ckstellung aus k�nftigen Pr�mien, Vorjahr
	 * @param minMax
	 *            ?
	 * @return Roh�berschuss
	 */
	public static double rohuebKpRzg(final double aKe, final double ke, final double drGes, final double drGesRzg,
			final double rmz, final double re, final double ueE, final double drV, final double drKpV,
			final String minMax) {
		if (nanZero(drV) < 0.01 || nanZero(drGes) < 0.01) {
			return 0.0;
		} else {
			final double rohuebRzg;
			if (minMax.equals("pos")) {
				rohuebRzg = Math.max((nanZero(drGesRzg) / drGes) * aKe - rmz + re + ueE, 0.0);
			} else {
				rohuebRzg = Math.min((nanZero(drGesRzg) / drGes) * aKe - rmz + re + ueE, 0.0);
			}
			return rohuebRzg * nanZero(drKpV) / drV;
		}
	}

	/**
	 * Berechnet die Deckungsr�ckstellung, die auf die k�nftigen Pr�mien zur�ckzuf�hren ist, f�r EPIFP. <br/>
	 * Funktionsname in Excel: DRST_KP.
	 * 
	 * @param vgDrstKp
	 *            Deckungsr�ckstellung, Vorjahr, die auf die k�nftige Pr�mien zur�ckzuf�hren sind
	 * @param praemien
	 *            Pr�mie
	 * @param zinsratenZuschlag
	 *            Zinsratenzuschlag
	 * @param zinsGeneration
	 *            Rechnungszins
	 * @param vuZeit
	 *            F�lligekeitmonat, VT-Cashflows
	 * @param zinsaufwand
	 *            rechnungsm��iger Zinsaufwand
	 * @param drDet
	 *            Deckungsr�ckstellung
	 * @param vgDrDet
	 *            Deckungsr�ckstellung, Vorjahr
	 * @param t
	 *            Projektionszeitpunkt
	 * @return Deckungsr�ckstellung
	 */
	public static double drstKp(final double vgDrstKp, final double praemien, final double zinsratenZuschlag,
			final double zinsGeneration, final double vuZeit, final double zinsaufwand, final double drDet,
			final double vgDrDet, final int t) {
		if (t == 0 || vgDrDet + zinsaufwand <= 0) {
			return 0.0;
		} else {
			final double aDrst = (drDet
					- (praemien - zinsratenZuschlag) * Math.pow((1 + inProzent(zinsGeneration)), (1.0 - vuZeit / 12.0)))
					/ (vgDrDet + zinsaufwand);
			return aDrst * vgDrstKp * (1 + inProzent(zinsGeneration))
					+ (praemien - zinsratenZuschlag) * Math.pow((1 + inProzent(zinsGeneration)), (1.0 - vuZeit / 12.0));
		}
	}

	/**
	 * Jahres�berschuss, geschl�sselt auf k�nftige Pr�mien. <br/>
	 * Funktionsname in Excel: JUE_VN_KP.
	 * 
	 * @param aKe
	 *            anrechenbare Kapitalertr�ge
	 * @param ke
	 *            Kapitalanlagenaufwendungen
	 * @param kaAufwendungen
	 *            Zinsaufwendungen f�r Liqudit�tskredit aus dem Vorjahr
	 * @param rohUeb
	 *            Roh�berschuss
	 * @param rohuebKp
	 *            Roh�berschuss, k�nftige Pr�mien
	 * @param jue
	 *            Jahres�berschuss
	 * @param drV
	 *            Deckungsr�ckstellung Vorjahr
	 * @param drKpV
	 *            Deckungsr�ckstellung aus k�nftigen Pr�mien, Vorjahr
	 * @return der Wert
	 */
	public static double jueVnKp(final double aKe, final double ke, final double kaAufwendungen, final double rohUeb,
			final double rohuebKp, final double jue, final double drV, final double drKpV) {
		final double JUE_VU = ke - kaAufwendungen - aKe;
		final double JUE_VN = jue - JUE_VU;
		final double a_VN_kp;
		if (nanZero(drV) < 0.001) {
			a_VN_kp = 0.0;
		} else {
			if ((rohUeb - JUE_VU) < 1.0 || JUE_VN < 1.0) {
				a_VN_kp = nanZero(drKpV) / drV;
			} else {
				a_VN_kp = rohuebKp / (rohUeb - JUE_VU);
			}
		}

		return a_VN_kp * JUE_VN;
	}

	/**
	 * berechnet Jahres�berschuss geschl�sselt auf k�nftige Pr�mien f�r einzelne Bestandsgruppen. <br/>
	 * Funktionsname in Excel: JUE_VN_KP_rzg.
	 * 
	 * @param JUE_VN_KP
	 *            Jahres�berschuss geschl�sselt auf k�nftige Pr�mien
	 * @param Rohueb_kp_pos
	 *            Roh�berschuss, k�nftige Pr�mien
	 * @param Rohueb_kp_pos_rzg
	 *            Roh�berschuss, k�nftige Pr�mien f�r einzelne Bestandsgruppen
	 * @param Rohueb_kp_neg
	 *            ?
	 * @param Rohueb_kp_neg_rzg
	 *            ?
	 * @param DR
	 *            ?
	 * @param DR_rzg
	 *            ?
	 * @return der Wert
	 */
	public static double jueVnKpRzg(final double JUE_VN_KP, final double Rohueb_kp_pos, final double Rohueb_kp_pos_rzg,
			final double Rohueb_kp_neg, final double Rohueb_kp_neg_rzg, final double DR, final double DR_rzg) {
		if (JUE_VN_KP > 0.01) {
			if (Rohueb_kp_pos > 0.01) {
				return JUE_VN_KP * Rohueb_kp_pos_rzg / Rohueb_kp_pos;
			} else {
				if (DR > 0.01) {
					return JUE_VN_KP * DR_rzg / DR;
				}
			}
		}
		if (JUE_VN_KP < -0.01) {
			if (Rohueb_kp_neg < -0.01) {
				return JUE_VN_KP * Rohueb_kp_neg_rzg / Rohueb_kp_neg;
			} else {
				if (DR > 0.01) {
					return JUE_VN_KP * DR_rzg / DR;
				}
			}
		}
		return 0.0;
	}

	/**
	 * Berechnet den Cashflows, der in die Berechnung von Z�B einfliessen. <br/>
	 * Funktionsname in Excel: Cashflow_ZUEB_rzg.
	 * 
	 * @param bar
	 *            Barauszahlung
	 * @param le
	 *            Leistungeserh�hung, gesamt
	 * @param lGarKv
	 *            s�mtliche garantierte Leistungen, mit Kapitalwahl, ohne Storno
	 * @param lambdaV
	 *            Gesamtstorno, Vorjahr
	 * @param kaGarXs
	 *            Kapitalabfindung Excess Betrag
	 * @param lbwGarV
	 *            Leistungsbarwert, garantiert, Vorjahr
	 * @param monat
	 *            F�lligekeitmonat, VT-Cashflows
	 * @param t
	 *            Zeit
	 * @param omega
	 *            Projektionsl�nge
	 * @param rz
	 *            Rechnungszinsgeneration
	 * @param laFlv
	 *            Leistungsanpassung, FLV
	 * @return der Wert
	 */
	public static double cashflowZuebRzg(final double bar, final double le, final double lGarKv, final double lambdaV,
			final double kaGarXs, final double lbwGarV, final double monat, final double t, final double omega,
			final int rz, final double laFlv) {
		final double cashflowZuebRzg = (nanZero(le) - nanZero(laFlv)) * (lambdaV * lGarKv + kaGarXs);
		if (nanZero(lbwGarV) > 0.001) {
			return cashflowZuebRzg + nanZero(bar) * Math.pow(1.0 + inProzent(rz), monat / 12.0);
		} else {
			return cashflowZuebRzg;
		}
	}

	/**
	 * Berechnet den Cashflows, der in die Berechnung von Optionen einfliessen. <br/>
	 * Funktionsname in Excel: Cashflow_Optionen_rzg.
	 * 
	 * @param lGarKV
	 *            s�mtliche garantierte Leistungen, mit Kapitalwahl, ohne Storno
	 * @param lambdaV
	 *            Gesamtstorno, Vorjahr
	 * @param kStoch
	 *            Kosten, stochastisch
	 * @param bStoch
	 *            Beitr�ge, stochastisch
	 * @param lGar
	 *            s�mtliche garantierte Leistungen, deterministisch
	 * @param b
	 *            Beitr�ge
	 * @param k
	 *            Kosten
	 * @param kaGarXs
	 *            Kapitalabfindung, Excess Betrag
	 * @param rkwXs
	 *            R�ckkaufswerte, Excess Betrag
	 * @param laFlv
	 *            Leistungsanpassung, FLV
	 * @return der Wert
	 */
	public static double cashflowOptionenRzg(final double lGarKV, final double lambdaV, final double kStoch,
			final double bStoch, final double lGar, final double b, final double k, final double kaGarXs,
			final double rkwXs, final double laFlv) {

		return ((1.0 + nanZero(laFlv)) * (lambdaV * lGarKV + kaGarXs) + rkwXs + kStoch - bStoch) - (lGar + k - b);
	}

	/**
	 * Funktionsname in Excel: KbmRzg.
	 * 
	 * @param deckungsStock
	 * @param rE
	 * @param uE
	 * @return der Wert
	 */
	public static double KbmRzg(final String deckungsStock, final double rE, final double uE) {
		if (deckungsStock.equals("Fonds")) {
			return rE + uE;
		}
		return 0.0;
	}

}
