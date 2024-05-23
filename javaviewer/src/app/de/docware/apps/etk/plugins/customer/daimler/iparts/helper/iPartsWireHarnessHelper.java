/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsWireHarness;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;
import java.util.Date;

public class iPartsWireHarnessHelper {

    public static final String WIRE_HARNESS_VALID_ETKZ = "E";
    public static final String WIRE_HARNESS_SIMPLIFIED_PART_ETKZ_VALUE = "V";
    public static final String WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK = "K";
    public static final String WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG = "LA";


    /**
     * Das Datenstandsdatum kommt in verschiedenen Formaten.
     * '2010-12-31', '2012-1-31', '2014-11-5', '2016-2-4'
     * Das Jahr ist IMMER vierstellig.
     * Der Monat kann einstellig sein.
     * Der Tag kann ebenfalls einstellig sein.
     * <p>
     * Hier werden Tag und Monat ggf. um eine führende Null erweitert.
     *
     * @param dateStr
     * @return
     */
    public static String handleDatasetDate(String dateStr) /*throws DateException*/ {
        try {
            Date date = DateUtils.toDate(dateStr, "yy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return SQLStringConvert.calendarToPPDateString(cal);
        } catch (DateException e) {

        }
        return "";
    }

    public static String getDummyWireHarnessPartNoFromConfig() {
        return iPartsPlugin.getWireHarnessDummyPartNo();
    }

    /**
     * Check, ob der Hauptschalter aktiv ist und ob die Adminoption gesetzt wurde
     *
     * @param filter
     * @param dataAssembly
     * @return
     */
    public static boolean isWireHarnessFilterActive(iPartsFilter filter, iPartsDataAssembly dataAssembly) {
        return isWireHarnessFilterConfigActive() && filter.getSwitchboardState().isMainSwitchActive()
               && filter.isWireHarnessFilterActive(dataAssembly);
    }

    public static boolean isWireHarnessFilterConfigActive() {
        return iPartsPlugin.isFilterWireHarnessParts();
    }

    public static boolean isWireHarnessDummyPart(EtkDataPart part) {
        return part.getAsId().getMatNr().equals(getDummyWireHarnessPartNoFromConfig());
    }

    /**
     * Liefert zurück, ob das {@link EtkDataPart} des {@link EtkDataPartListEntry} den sonstige-Kenner "LA" (M_LAYOUT_FLAG)
     * gesetzt hat
     *
     * @param partListEntry
     * @return
     */
    public static boolean hasValidAdditionalWireHarnessFlag(EtkDataPartListEntry partListEntry) {
        return hasValidAdditionalWireHarnessFlag(partListEntry.getPart());
    }

    /**
     * Liefert zurück, ob das {@link EtkDataPart} den sonstige-Kenner "LA" (M_LAYOUT_FLAG) gesetzt hat
     *
     * @param part
     * @return
     */
    public static boolean hasValidAdditionalWireHarnessFlag(EtkDataPart part) {
        String layoutFlag = part.getFieldValue(iPartsConst.FIELD_M_LAYOUT_FLAG);
        return layoutFlag.equals(iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG);
    }

    /**
     * Liefert zurück, ob das {@link EtkDataPart} des {@link EtkDataPartListEntry} eine Leitungsatz-BK Sachnummer ist
     *
     * @param project
     * @param partListEntry
     * @return
     */
    public static boolean isWireHarnessPartListEntry(EtkProject project, EtkDataPartListEntry partListEntry) {
        iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(project);
        return wireHarnessCache.isWireHarness(partListEntry.getPart().getAsId());

    }

    /**
     * Liefert zurück, ob das {@link EtkDataPart} des {@link EtkDataPartListEntry} eine Leitungsatz-BK Sachnummer ist
     * und ob das {@link EtkDataPart} des {@link EtkDataPartListEntry} den sonstige-Kenner "LA" (M_LAYOUT_FLAG)
     * gesetzt hat
     *
     * @param project       ETkProject
     * @param partListEntry Stücklisteneintrag für den geprüft wird
     * @return
     */
    public static boolean isWireHarnessPartListEntryWithAdditionalWireHarnessFlag(EtkProject project, EtkDataPartListEntry partListEntry) {
        iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(project);
        return wireHarnessCache.isWireHarness(partListEntry.getPart().getAsId()) && hasValidAdditionalWireHarnessFlag(partListEntry);

    }
}
