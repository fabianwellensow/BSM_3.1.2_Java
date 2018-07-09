package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

/**
 * Funktionen des Excel-Moduls <code>Kundenverhalten</code>.
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
 *
 */
public class Kundenverhalten {

	/**
	 * Berechnet die durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T einer Rechnungszinsgeneration. <br/>
	 * Funktionsname in Excel: s_basis.
	 * 
	 * @param lRkw
	 *            Garantierte R�ckkaufleistungen im Jahr T
	 * @param drDet
	 *            Deterministische Deckungsr�ckstellung im Jahr T
	 * @param zinsGeneration
	 *            Rechnungszins in Basispunkten
	 * @param monat
	 *            Monat indem die R�ckkaufleistungen anfallen (VU-Zeitpunkt)
	 * @return Stornowahrscheinlichkeit
	 */
	public static double sBasis(final double lRkw, final double drDet, final int zinsGeneration, final double monat) {
		if (drDet > 0.001) {
			return lRkw * Math.pow(1 + inProzent(zinsGeneration), 1 - monat / 12.0) / drDet;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Zinsabstand zwischen der Gesamtverzinsung einer Bestandsgruppe und dem 10 j�hrigen Zins einer
	 * Neuanlage zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: Delta_i.
	 * 
	 * @param zinsZehnjaehr
	 *            10 j�hriger Zins einer Neuanlage
	 * @param rmZ
	 *            rechnungsm��iger Zins
	 * @param rz
	 *            Rechnungszins in Basispunkten
	 * @param dekl
	 *            Deklaration
	 * @param drLockInV
	 *            Lock-In-Deckungsr�ckstellung Vorjahr
	 * @param t
	 *            Zeitpunkt
	 * @return der Wert
	 */
	public static double deltaI(final double zinsZehnjaehr, final double rmZ, final double rz, final double dekl,
			final double drLockInV, int t) {
		if (t == 0) {
			return zinsZehnjaehr - inProzent(rz);
		}
		if (drLockInV > 0.001) {
			return zinsZehnjaehr - (rmZ + dekl) / drLockInV;
		}
		return 0.0;
	}

	/**
	 * Berechnet die prozentuale Ver�nderung des Basisstornos zum Ende des Jahres T. <br/>
	 * Funktionsname in Excel: lambda_storno.
	 * 
	 * @param zinsSensitiv
	 *            Kennzeichen zinssensitiv
	 * @param vgDeltaI
	 *            Zinsabstand zwischen der Gesamtverzinsung einer Rechnungszinsgeneration ' und dem 10 j�hrigen Zins
	 *            einer Neuanlage zum Zeitpunkt T-1
	 * @param deltaTol
	 *            Toleranzschwelle f�r den Zinsausschlag
	 * @param deltaStorno
	 *            Intensit�t der Stornover�nderung in Abh�ngigkeit vom Zinsumfeld einer Rechnungszinsgeneration
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T
	 * @param drDet
	 *            Deterministische Deckungsr�ckstellung zum Zeitpunkt T
	 * @return Basisstorno
	 */
	public static double lambdaStorno(final String zinsSensitiv, final double vgDeltaI, final double deltaTol,
			final double deltaStorno, final double sBasis, final double drDet) {
		if (drDet <= 0.001 || !zinsSensitiv.equals("j")) {
			return 0.0;
		}
		double lambdaStorno = 100 * Math.signum(vgDeltaI) * Math.max(Math.abs(vgDeltaI) - deltaTol, 0.0) * deltaStorno;
		// ..und projeziere auf erlaubtes Intervall
		lambdaStorno = Math.max(lambdaStorno, -1.0);
		if (sBasis > 0.0001) {
			lambdaStorno = Math.min(lambdaStorno, 1 / sBasis);
		} else {
			lambdaStorno = 0.0;
		}

		return lambdaStorno;
	}

	/**
	 * Berechnet die prozentuale Ver�nderung der Kapitalwahl zum Ende des Jahres T. <br/>
	 * Funktionsname in Excel: lambda_KA.
	 * 
	 * @param zinssensitiv
	 *            Kennzeichen zinssensitiv
	 * @param deltaIV
	 *            Zinsabstand zwischen der Gesamtverzinsung einer Rechnungszinsgeneration und dem 10 j�hrigen Zins einer
	 *            Neuanlage zum Zeitpunkt T-1
	 * @param deltaTol
	 *            Toleranzschwelle f�r den Zinsausschlag
	 * @param DeltaKa
	 *            Intensit�t der Ver�nderung der Kapitalwahl in Abh�ngigkeit vom Zinsumfeld
	 * @param drDet
	 *            Deterministische Deckungsr�ckstellung im Jahr T-1
	 * @param ka
	 *            Kapitalabfindungen (nur von Rentenversicherungen) zum Zeitpunkt T
	 * @param lambdaStorno
	 *            Prozentuale Ver�nderung des Basisstornos zum Ende des Jahres T
	 * @param sBasis
	 *            ?
	 * @return den Wert
	 */
	public static double lambdaKa(final String zinssensitiv, final double deltaIV, final double deltaTol,
			final double DeltaKa, final double drDet, final double ka, final double lambdaStorno, final double sBasis) {
		double lambdaKa = 0.0;
		if (drDet <= 0.001) {
			return 0.0;
		}
		if (zinssensitiv.equals("j")) {
			lambdaKa = 100.0 * Math.signum(deltaIV) * Math.max(Math.abs(deltaIV) - deltaTol, 0.0) * DeltaKa;
			lambdaKa = Math.max(-1.0, lambdaKa);
			if (ka > 0.001) {
				lambdaKa = Math.min(lambdaKa, Math.max(((1.0 - lambdaStorno * sBasis) * drDet) / ka, 0.0));
			}
		}
		return lambdaKa;
	}

	/**
	 * Berechnet die relative Bestands�nderung durch Storno zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: Lambda.
	 * 
	 * @param zinsSensitiv
	 *            Kennzeichen zinssensitiv
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @param lambdaStorno
	 *            Prozentuale Ver�nderung des Basisstornos zum Ende des Jahres T
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T einer Rechnungszinsgeneration
	 * @return Bestands�nderung
	 */
	public static double lambda(final String zinsSensitiv, final double vgLambda, final double lambdaStorno,
			final double sBasis) {
		final double lambda;
		if (zinsSensitiv.equals("j")) {
			lambda = vgLambda * (1.0 - lambdaStorno * sBasis);
		} else {
			lambda = vgLambda;
		}
		return lambda < 0.00001 ? 0.0 : lambda;
	}

	/**
	 * Gibt den Startwert f�r die relative Bestands�nderung durch Storno aus (Startpunkt =Zeitpunkt 0). <br/>
	 * Funktionsname in Excel: Lambda_Startwert
	 * 
	 * @return den Wert
	 */
	public static double lambdaStartwert() {
		return 1.0;
	}

	/**
	 * Berechnet den Excess Betrag f�r die garantierten R�ckkaufsleistunen zum Zeitpunkt T, d.h. den durch zus�tzliche
	 * Stornierung im Jahr T zu zahlenden Betrag an garantierten R�ckkaufsleistungen. <br/>
	 * Funktionsname in Excel: RKW_XS.
	 * 
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T
	 * @param lambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @param lambdaStorno
	 *            Prozentuale Ver�nderung des Basisstornos zum Ende des Jahres T
	 * @param drDet
	 *            Deckungsr�ckstellung f�r �nf�nglich garantierte Leistungen im Jahr T
	 * @param vgLeLockInAggrFLV
	 *            Garantierte Erh�hung der anf�nglich garantierten Leistungen zum Zeitpunkt T
	 * @param lbwGar
	 *            Barwert der zuk�nftigen, deterministisch projezierten Leistungen zum Ende des Jahres T
	 * @param vgLaKapWahlXsAggr
	 *            Leistungsanpassung der zuk�nftigen Erlebensfallleistungen durch pfadabh�ngige Kapitalwahl�nderung bis
	 *            zum Jahr T
	 * @param lbwSonstErl
	 *            Barwert der zuk�nftigen, deterministisch projezierten sonstigen Erlebensfallleistungen zum Ende des
	 *            Jahres T
	 * @param zinsGeneration
	 *            Rechnungszins in Basispunkten
	 * @param monat
	 *            Monat der Zahlungseing�nge (VU-Zeitpunkt)
	 * @return Betrag
	 */
	public static double rkwXs(final double sBasis, final double lambda, final double vgLambda,
			final double lambdaStorno, final double drDet, final double vgLeLockInAggrFLV, final double lbwGar,
			final double vgLaKapWahlXsAggr, final double lbwSonstErl, final double zinsGeneration, final double monat) {
		if (lambda != 0.0) {
			return vgLambda * lambdaStorno * sBasis
					* (drDet + (Double.isNaN(vgLeLockInAggrFLV) ? 0.0 : vgLeLockInAggrFLV) * lbwGar / lambda
							+ (Double.isNaN(vgLaKapWahlXsAggr) ? 0.0 : vgLaKapWahlXsAggr) * lbwSonstErl)
					/ Math.pow(1 + inProzent(zinsGeneration), 1 - monat / 12.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Excess Betrag f�r die garantierten Leistungen f�r Kapitalabfindungen zum Zeitpunkt T, d.h. den
	 * durch zus�tzliche Kapitalwahl im Jahr T zu zahlenden Betrag an garantierten Kapitalabfindungen. <br/>
	 * Funktionsname in Excel: KA_gar_XS.
	 * 
	 * @param kaGar
	 *            Garantierte Leistungen f�r Kapitalabfindungen im Jahr T
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T
	 * @param lambdaKa
	 *            Prozentuale Ver�nderung der Kapitalwahl zum Ende des Jahres T
	 * @return Betrag
	 */
	public static double kaGarXs(final double kaGar, final double vgLambda, final double lambdaKa) {
		return kaGar * vgLambda * lambdaKa;
	}

	/**
	 * Berechnet die relative Leistungsanpassung der zuk�nftigen Erlebensfallleistungen durch pfadabh�ngige
	 * Kapitalwahl�nderung bis zum Jahr T. <br/>
	 * Funktionsname in Excel: LA_KapWahlXS_Aggr.
	 * 
	 * @param vgLaKapWahlXsAggr
	 *            Leistungsanpassung der zuk�nftigen Erlebensfallleistungen durch pfadabh�ngige Kapitalwahl�nderung bis
	 *            zum Jahr T-1
	 * @param kaGarXs
	 *            Excess Betrag f�r die garantierten R�ckkaufsleistunen zum Zeitpunkt T
	 * @param lbwGarSonstErl
	 *            Barwert der zuk�nftigen, deterministisch projezierten sonstigen Erlebensfallleistungen
	 * @param rz
	 *            Rechnungszins in Basispunkten
	 * @param lambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T
	 * @param monat
	 *            Monat der Zahlungseing�nge (VU-Zeitpunkt)
	 * @return Leistungsanpassung
	 */
	public static double laKapWahlXsAggr(final double vgLaKapWahlXsAggr, final double kaGarXs,
			final double lbwGarSonstErl, final double rz, final double lambda, final double monat) {
		if (lbwGarSonstErl > 0.001 && lambda != 0) {
			return nanZero(vgLaKapWahlXsAggr)
					- kaGarXs * Math.pow(1.0 + inProzent(rz), 1.0 - monat / 12.0) / (lambda * lbwGarSonstErl);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Cashflow der stochastischen Kosten im Jahr T. <br/>
	 * Funktionsname in Excel: Kosten_stoch.
	 * 
	 * @param kostenDet
	 *            Kosten im Jahr T, deterministisch
	 * @param lambdaV
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double kostenStoch(final double kostenDet, final double lambdaV) {
		return kostenDet * lambdaV;
	}

	/**
	 * Berechnet den Cashflow der stochastischen Risiko�bersch�sse im Jahr T. <br/>
	 * Funktionsname in Excel: RisikoUeb_stoch.
	 * 
	 * @param risikoUebDet
	 *            Risiko�bersch�sse im Jahr T, deterministisch
	 * @param lambdaV
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double risikoUebStoch(final double risikoUebDet, final double lambdaV) {
		return risikoUebDet * lambdaV;
	}

	/**
	 * Berechnet den Cashflow der stochastischen Kosten�bersch�sse im Jahr T. <br/>
	 * Funktionsname in Excel: KostenUeb_stoch.
	 * 
	 * @param uebrigesErgebnis
	 *            Kosten�bersch�sse im Jahr T, deterministisch
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double kostenUebStoch(final double uebrigesErgebnis, final double vgLambda) {
		return uebrigesErgebnis * vgLambda;
	}

	/**
	 * Berechnet den stochastischen CF von Erstversicherer an den R�ckversicherer im Jahr T.<br/>
	 * Funktionsname in Excel: CF_RV_Stoch.
	 * 
	 * @param cfRvDet
	 *            CF EVU -> RVU (nicht LE-abh�ngig)
	 * @param lambdaV
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return der Wert
	 */
	public static double cfRvStoch(final double cfRvDet, final double lambdaV) {
		return cfRvDet * lambdaV;
	}

	/**
	 * Berechnet den stochastischen Zinsratenzuschl�ge im Jahr T. <br/>
	 * Funktionsname in Excel: ZiRaZu_stoch.
	 * 
	 * @param zinsratenZuschlag
	 *            Zinsratenzuschl�ge im Jahr T, deterministisch
	 * @param vgLambda
	 *            Relative Bestands�nderung durch Storno zum Zeitpunkt T-1
	 * @return Zinsratenzuschl�ge
	 */
	public static double ziRaZuStoch(final double zinsratenZuschlag, final double vgLambda) {
		return zinsratenZuschlag * vgLambda;
	}

	/**
	 * Berechnet den Barwert der zuk�nftigen, deterministisch projezierten sonstigen Erlebensfallleistungen bez�glich
	 * des Rechnungszinses zum Ende des Jahres T (deterministischer Leistungsbarwert). Dabei wird davon ausgegangen,
	 * dass die zuk�nfftigen Zahlungen in den Jahren T+1,..., omega im Monat monat eingehen. <br/>
	 * Funktionsname in Excel: LBW_sonstErl.
	 * 
	 * @param rz
	 *            Rechnungszins
	 * @param nfLbwSonstErl
	 *            Deterministischer Leistungsbarwert im Folgejahr (T+1)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            Projektionsende
	 * @param lwbSonstErlN
	 *            Deterministisch projezierte, garantierte sonstigen Erlebensfallleistungen im Folgejahr (T+1)
	 * @param monat
	 *            Monat der Zahlungseing�nge
	 * @return Barwert
	 */
	public static double lbwSonstErl(final double rz, final double nfLbwSonstErl, final int t, final int omega,
			final double lwbSonstErlN, final double monat) {
		return SonstigeFormeln.barwert(rz, nfLbwSonstErl, t, omega, lwbSonstErlN, monat);
	}

	/**
	 * Berechnet die garantierten Leistungen, ohne sonstige Erlebensfallleistungen. <br/>
	 * Funktionsname in Excel: L_garantiert_o_sonstErl.
	 * 
	 * @param lTod
	 *            Garantierte Leistungen im Todesfall
	 * @param lKa
	 *            Garantierte Leistungen f�r Kapitalabfindungen
	 * @param lRkw
	 *            Garantierte R�ckkaufswerte
	 * @return garantierte Leistungen
	 */
	public static double lGarantiertOSonstErl(final double lTod, final double lKa, final double lRkw) {
		return lTod + lKa + lRkw;
	}

	/**
	 * Berechnet den Barwert der zuk�nftigen, deterministisch projezierten garantierten Leistungen, ohne sonstige
	 * Erlebensfallleistungen bez�glich des Rechnungszinses zum Ende des Jahres T (deterministischer Leistungsbarwert).
	 * Dabei wird davon ausgegangen, dass die zuk�nfftigen Zahlungen in den Jahren T+1,..., omega im Monat monat
	 * eingehen. <br/>
	 * Funktionsname in Excel: LBW_gar_o_SonstErl.
	 * 
	 * @param rz
	 *            Rechnungszins
	 * @param lwbSonstErlB
	 *            Deterministischer Leistungsbarwert im Folgejahr (T+1)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            Projektionsende
	 * @param lGarantiertOSonstErl
	 *            Deterministisch projezierte, garantierte Leistungen, ohne sonstige Erlebensfallleistungen im Folgejahr
	 *            (T+1)
	 * @param monat
	 *            Monat der Zahlungseing�nge
	 * @return Barwert
	 */
	public static double lbwGarOSonstErl(final double rz, final double lwbSonstErlB, final int t, final int omega,
			final double lGarantiertOSonstErl, final double monat) {
		return SonstigeFormeln.barwert(rz, lwbSonstErlB, t, omega, lGarantiertOSonstErl, monat);
	}

}
