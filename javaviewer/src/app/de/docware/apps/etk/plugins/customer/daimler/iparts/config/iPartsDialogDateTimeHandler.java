/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPEMPseudoDateCache;
import de.docware.util.StrUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Behandelt die Spezialfälle eines DateTime Feldes aus DIALOG
 */
public class iPartsDialogDateTimeHandler {

    public final static String MINIMUM_STATE_DATETIME = "18991230235959";

    // Bei Prüfungen isFinalStateDateTime() benutzen, weil das finale Datum auch andere Formen haben kann!
    public final static String FINAL_STATE_DATETIME = "999999999999999";

    private static final String DATE_TIME_MILLIS_FOR_AS_DATA_IN_DB = "yyyyMMddHHmmssSSS";

    private String dateTimeDIALOG;

    public iPartsDialogDateTimeHandler(String dateTimeDIALOG) {
        this.dateTimeDIALOG = dateTimeDIALOG;
    }

    /**
     * überprüft ob DIALOG-DateTime leer oder 15stellig ist
     *
     * @return
     */
    public boolean isValid() {
        return (dateTimeDIALOG != null) && (dateTimeDIALOG.isEmpty() || (dateTimeDIALOG.length() == 15));
    }

    /**
     * Überprüft, ob DIALOG-DateTime Kennzeichen für zuletzt freigegeben hat (leer oder startet mit '9' oder das lustige 40-er Datum)
     *
     * @return
     */
    public boolean isFinalStateDateTime() {
        if (isValid()) {
            // Einige Spezialfälle werden hier auf ein leeres DateTime umgebogen.
            return dateTimeDIALOG.isEmpty() || dateTimeDIALOG.startsWith("9") || dateTimeDIALOG.equals("404040404040404");
        }
        return false;
    }

    /**
     * Wandelt eine DIALOG DateTime in ein iParts DateTime um
     * (FinalStateDateTime wird zu Leerstring)
     *
     * @return
     */
    public String getDBDateTime() {
        if (isValid()) {
            if (!dateTimeDIALOG.isEmpty()) {
                // Einige Spezialfälle von 'FinalStateDateTime' werden auf ein leeres DateTime umgebogen.
                if (!isFinalStateDateTime()) {
                    return StrUtils.copySubString(dateTimeDIALOG, 1, 14);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }
        return null;
    }

    /**
     * Wandelt eine PEMT spezifisches DIALOG DateTime in ein iParts Date um
     * Eigentlich ist das PemDatum immer Datum + Stunde. Aber nirgendwo wird die Stunde gefüllt oder angezeigt, deshalb machen wir ein Date draus
     * (FinalStateDateTime wird zu Leerstring)
     *
     * @param project
     * @return
     */
    public String getDBDateForPEMT(EtkProject project) {
        if (isPEMTValid()) {
            if (!dateTimeDIALOG.isEmpty()) {
                //der Spezialfall (15mal '9') wird auf leeres DateTime umgebogen
                if (!isPEMTFinalStateDateTime(project)) {
                    // einfaches Date mit 8 Stellen
                    return dateTimeDIALOG.substring(0, 8);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }
        return null;
    }

    /**
     * überprüft ob PEMT DIALOG-DateTime leer oder 10 stellig ist
     *
     * @return
     */
    public boolean isPEMTValid() {
        return (dateTimeDIALOG != null) && (dateTimeDIALOG.isEmpty() || (dateTimeDIALOG.length() == 10));
    }

    /**
     * Überprüft, ob DIALOG PEM Einsatztermin ungültig bzw. unendlich ist
     *
     * @param project
     * @return
     */
    public boolean isPEMTFinalStateDateTime(EtkProject project) {
        if (isPEMTValid()) {
            // DAIMLER-13269: Pseudo PEM Termine werden nun via Cache geprüft
            return dateTimeDIALOG.isEmpty() || dateTimeDIALOG.startsWith("9999")
                   || iPartsPEMPseudoDateCache.getInstance(project).isPEMPseudoDate(dateTimeDIALOG);
        }
        return false;
    }

    public static String getNextDBDateTimeForExistingDateTimes(Set<Long> setWithDateTimesAsLong) {
        long currentTime = System.currentTimeMillis();
        if (setWithDateTimesAsLong != null) {
            while (setWithDateTimesAsLong.contains(currentTime)) {
                // der Import lief so schnell, dass ein Datensatz in der selben Millisekunde angelegt werden würde
                // Oder wir haben aus dem Grund schon adats angelegt, die leicht in der Zukunft liegen --> künstlich um 1ms erhöhen
                currentTime++;
            }
            setWithDateTimesAsLong.add(currentTime);
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_MILLIS_FOR_AS_DATA_IN_DB);
        return sdf.format(new Date(currentTime));
    }

}
