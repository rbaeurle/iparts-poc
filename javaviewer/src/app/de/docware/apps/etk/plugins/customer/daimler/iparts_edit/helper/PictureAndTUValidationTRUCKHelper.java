/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Erweitert die Hilfsklasse um TRUCK-spezifische Prüfungen
 * 1.: TU/freie SA hat nur unterdrückte Teilepositionen -> Fehler (Diese Prüfung ist ein Teil der Prüfung der Teilepositionen aus der Superklasse)
 * 2.: TU/freie SA SA-Benennung einer SAA aus der SAA-/BK-Gültigkeit fehlt -> Hinweis
 */
public class PictureAndTUValidationTRUCKHelper extends PictureAndTUValidationHelper {

    public PictureAndTUValidationTRUCKHelper(EditModuleFormIConnector editConnector, EtkMessageLogFormHelper messageLogHelper,
                                             boolean simplifiedQualityCheck) {
        super(editConnector, messageLogHelper, simplifiedQualityCheck);
    }

    @Override
    public int getNumberOfChecks() {
        // Es werden 5 Prüfungen (fireProgress()) durchgeführt
        // 5 in der Superklasse, 1 hier.
        return 6;
    }

    @Override
    public PictureAndTUValidationEntryList startPictureAndTUValidation() {
        PictureAndTUValidationEntryList validationEntryList = super.startPictureAndTUValidation();

        if (!isSimplifiedQualityCheck()) {
            // Prüfung, ob SA-Benennungen einer SAA aus den SAA-/BK-Gültigkeiten fehlt
            List<EtkDataPartListEntry> partListEntries = getConnector().getUnfilteredPartListEntries();
            fireMessage("!!Prüfung der SAAs in den SAA-/BK-Gültigkeiten");
            Map<PartListEntryId, Set<String>> partListToSaNumberMap = getSASOfSAValidityWithNoDesc(partListEntries);
            if (!partListToSaNumberMap.isEmpty()) {
                for (Map.Entry<PartListEntryId, Set<String>> mapEntry : partListToSaNumberMap.entrySet()) {
                    String saNumberList = StrUtils.stringListToString(mapEntry.getValue(), ", ");
                    validationEntryList.addWarning(mapEntry.getKey(), null, TranslationHandler.translate("!!Teileposition besitzt SAA-/BK-Gültigkeiten ohne Benennung"),
                                                   TranslationHandler.translate("!!SAs ohne Benennung: %1", saNumberList));
                }
            }
        }
        fireProgress();

        return validationEntryList;
    }

    /**
     * Prüfung der offenen Bestätigungen nicht durchführen für ELDAS
     */
    @Override
    public void executeOpenConfirmationsForChangeSetCheck() {
    }

    @Override
    protected void doPartListEntryChecks(EditHotSpotHelper editHotSpotHelper, PictureAndTUValidationEntryList validationEntries) {
        super.doPartListEntryChecks(editHotSpotHelper, validationEntries);
        if (isSimplifiedQualityCheck()) {
            return;
        }

        // Gehört noch zur Prüfung der Teileposition
        // Prüfung, ob alle Teilepositionen im TU unterdrückt sind
        List<EtkDataPartListEntry> partListEntries = getConnector().getUnfilteredPartListEntries();
        boolean allPLEAreOmitted = hasTUOnlyOmittedPLEs(partListEntries);
        if (allPLEAreOmitted) {
            validationEntries.addErrorT(getAssemblyId(), "!!Alle Teilepositionen sind unterdrückt");
        }

    }

    /**
     * Prüfung, ob alle Teilepostionen unterdrückt sind
     *
     * @param partListEntries
     * @return
     */
    private boolean hasTUOnlyOmittedPLEs(List<EtkDataPartListEntry> partListEntries) {
        if (!partListEntries.isEmpty()) {
            boolean allPLEAreOmitted = true;
            for (EtkDataPartListEntry partListEntry : partListEntries) {
                if (!isOmittedPartListEntry(partListEntry)) {
                    allPLEAreOmitted = false;
                    break;
                }
            }
            return allPLEAreOmitted;
        }
        return false;
    }

    /**
     * Sammelt pro Teileposition alle SAAs aus der SAA-/BK- Gültigkeit, die keine Benennung haben
     * SAAs werden formatiert
     *
     * @param partListEntries
     * @return
     */
    private Map<PartListEntryId, Set<String>> getSASOfSAValidityWithNoDesc(List<EtkDataPartListEntry> partListEntries) {
        Map<PartListEntryId, Set<String>> partListToSaNumberMap = new HashMap<>();
        Map<String, Boolean> saDescriptionMissingMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            // Unterdrückte Positionen nicht betrachten (analog zu PKW)
            if (isOmittedPartListEntry(partListEntry)) {
                continue;
            }
            EtkDataArray saaValues = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);
            for (String saaValidity : saaValues.getArrayAsStringList()) {
                String saNumber = iPartsNumberHelper.convertSAAtoSANumber(saaValidity);
                // Falls ein Baukasten in de SAA-/BK-Liste vorhanden ist, kommt nach der Formatierung null zurück
                if (saNumber != null) {
                    boolean saDescriptionMissing = saDescriptionMissingMap.computeIfAbsent(saNumber, saKey -> {
                        iPartsSaId saId = new iPartsSaId(saKey);
                        iPartsDataSa sa = new iPartsDataSa(getProject(), saId);

                        if (sa.existsInDB()) {
                            String saDescription = sa.getFieldValue(iPartsConst.FIELD_DS_DESC);
                            if (!saDescription.isEmpty()) {
                                return false;
                            }
                        }
                        return true;
                    });

                    if (saDescriptionMissing) {
                        Set<String> sasWithoutDescription = partListToSaNumberMap.computeIfAbsent(partListEntry.getAsId(), k -> new TreeSet<>());
                        String formattedSANumber = iPartsNumberHelper.formatPartNo(getProject(), saNumber);
                        sasWithoutDescription.add(formattedSANumber);
                    }
                }
            }
        }
        return partListToSaNumberMap;
    }

}
