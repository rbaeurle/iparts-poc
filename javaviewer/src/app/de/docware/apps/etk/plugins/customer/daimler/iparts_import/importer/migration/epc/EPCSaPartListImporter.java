/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteSaRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteSaRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.*;

public class EPCSaPartListImporter extends AbstractEPCPartListImporter {

    private final static String SA_SUBGRP = "SUBGRP";
    private final static String SA_SANUM = "SANUM";
    private final static String SA_OMPARTN = "OMPARTN";
    private final static String SA_STEER = "STEER";
    private final static String SA_AN_INSERT = "AN_INSERT";
    private final static String SA_OMPARTT = "OMPARTT";
    private final static String SA_FOOTNOT = "FOOTNOT";
    private final static String SA_ITEMNO = "ITEMNO";
    private final static String SA_PARTTYP = "PARTTYP";
    private final static String SA_STROKE = "STROKE";
    private final static String SA_QUANTSA = "QUANTSA";

    private EPCSaPartListImportHelper helper;
    private String currentSAaNumber;
    private Set<String> invalidSas;
    private boolean importToDB = true;

    public EPCSaPartListImporter(EtkProject project) {
        super(project, "EPC SA-Parts", "!!EPC SA-Teilepositionen (SA Parts)", ImportType.IMPORT_SA_PARTS);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                SA_SUBGRP,
                SA_SANUM,
                SEQNUM,
                SA_OMPARTN,
                SA_STEER,
                SA_AN_INSERT,
                DESCIDX,
                NOUNIDX,
                SEQNO,
                SA_OMPARTT,
                SA_FOOTNOT,
                REPTYPE,
                OPTPART,
                REPLFLG,
                SA_ITEMNO,
                OPTFLAG,
                TUVSIGN,
                REPPNO,
                SA_PARTTYP,
                PARTNUM,
                SA_STROKE,
                NEUTRAL,
                SA_QUANTSA,
                REPPART
        };
    }

    @Override
    protected Set<iPartsFootNoteId> handleFootNotes(Map<String, String> importRec, boolean isVTextPos) {
        Set<iPartsFootNoteId> footNoteIds = null;
        if (!isVTextPos) { // Fußnoten nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
            List<String> footnoteNumbers = helper.getAsArray(helper.handleValueOfSpecialField(SA_FOOTNOT, importRec), 3, true, false);
            String tuvValue = helper.handleValueOfSpecialField(TUVSIGN, importRec);
            footNoteIds = getFootnotesHandler().handleFootnotesForSA(footnoteNumbers, tuvValue, getSaNumber(importRec), false);
        }
        return footNoteIds;
    }

    private String getSaNumber(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(SA_SANUM, importRec);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        helper = new EPCSaPartListImportHelper(getProject());
        invalidSas = new HashSet<>();
    }

    private boolean isDifferentSa(String saNumber) {
        return (currentSAaNumber == null) || !currentSAaNumber.equals(saNumber);
    }

    @Override
    protected boolean checkIfAlreadyCreatedFromMAD(Map<String, String> importRec) {
        String saNumber = helper.handleValueOfSpecialField(SA_SANUM, importRec);
        if (!isDifferentSa(saNumber)) {
            return false;
        }
        return !helper.isSARelevantForImport(this, saNumber, invalidSas, getCurrentRecordNo());
    }

    @Override
    protected void checkAndCreateNewAssembly(Map<String, String> importRec) {
        String saNumber = getSaNumber(importRec);
        checkIfNewSaNumber(saNumber);
        if (getCurrentAssembly() == null) {
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Erzeugen des TUs \"%1\" für die " +
                                                        "freie SA \"%2\"", getCurrentAssembly().getAsId().getKVari(), saNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
        }


    }

    private void checkIfNewSaNumber(String saNumber) {
        boolean makeNewAssembly = true;
        if ((getCurrentAssembly() != null)) {
            // Assembly existiert schon -> Check, ob eine neue angelegt werden soll
            if ((currentSAaNumber != null) && currentSAaNumber.equals(saNumber)) {
                makeNewAssembly = false;
            } else {
                // Unterschiedliche AssemblyIds -> alte Speichern
                finishAssembly();
            }
        }

        if (makeNewAssembly) {
            createNewAssembly(saNumber);
        }
    }

    private void createNewAssembly(String saNumber) {
        AssemblyId newAssemblyId = new AssemblyId(SA_MODULE_PREFIX + saNumber, "");
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), newAssemblyId);
        boolean oldModuleHidden = deleteAssemblyIfExists(assembly);

        iPartsSAModulesId saModulesId = new iPartsSAModulesId(saNumber);
        iPartsSA sa = iPartsSA.getInstance(getProject(), saModulesId);
        iPartsDataAssembly currentAssembly = EditModuleHelper.createAndSaveModuleWithSAAssignment(newAssemblyId,
                                                                                                  iPartsModuleTypes.SA_TU,
                                                                                                  sa.getTitle(getProject()),
                                                                                                  null,
                                                                                                  saModulesId,
                                                                                                  null,
                                                                                                  getProject(),
                                                                                                  iPartsModuleTypes.SA_TU.getDefaultDocumentationType(),
                                                                                                  iPartsImportDataOrigin.EPC,
                                                                                                  true, null);
        setCurrentAssembly(currentAssembly);
        // Soll das das SA-Modul ausgeblendet werden?
        if (oldModuleHidden || iPartsMigrationHelper.isSAHidden(saModulesId, getProject())) {
            getCurrentAssembly().getModuleMetaData().setFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
        }
        boolean saChanged = (currentSAaNumber == null) || !currentSAaNumber.equals(saNumber);
        if (saChanged) {
            getMessageLog().fireMessage(translateForLog("!!Importiere SA \"%1\"", saNumber),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            handleSaChanged(saNumber);
            currentSAaNumber = saNumber;
            clearPartsDoneCache();
            getModuleVariantsVisibleMap().clear();
        }
    }

    private void handleSaChanged(String saNumber) {
        if (currentSAaNumber != null) {
            // todo: Zusätzlich alle Fußnoten zur SA speichern? Oder wird das schon in finishAssembly gemacht?
//            getFootnotesHandler().saveCurrentFootNotesForPartListEntries(getAllCurrentColorTablefootnotes()); // alle Fußnoten für KG speichern
        }
        loadFootnotesForCurrentSaNumber(saNumber);
    }

    private void loadFootnotesForCurrentSaNumber(String saNumber) {
        iPartsDataEPCFootNoteSaRefList allFootnotesRefsForSa
                = iPartsDataEPCFootNoteSaRefList.loadAllRefsForSaWithPlaceholderSigns(getProject(), saNumber);
        for (iPartsDataEPCFootNoteSaRef footnote : allFootnotesRefsForSa) {
            if (isCancelled()) {
                return;
            }
            FootnoteObject footnoteObject = new FootnoteObject(footnote.getFieldValue(FIELD_DEFS_SA_NO),
                                                               footnote.getFieldValue(FIELD_DEFS_FN_NO),
                                                               footnote.getFieldValue(FIELD_DEFS_TEXT_ID),
                                                               footnote.getFieldValue(FIELD_DEFS_GROUP),
                                                               footnote.getFieldValue(FIELD_DEFC_ABBR));
            importFootNote(footnoteObject);
        }
    }

    @Override
    protected String getPartTypeFieldname() {
        return SA_PARTTYP;
    }

    @Override
    protected void checkSaaValidity(Set<String> saaBkValidity, iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA, String currentQuantity, boolean isTextPos) {
        if (!isTextPos && (getImportType() == ImportType.IMPORT_SA_PARTS)) { // Mengen pro SAA nicht bei Textpositionen importieren
            // Bei den SAA-Katalogen ist hier für jede Strichausführung die Menge gespeichert
            // Damit ist dieser Stücklisteneintrag bei dieser Menge nur gültig, bei den bestimmten SAA-Nummern
            saaBkValidity.clear();
            saaBkValidity.addAll(quantityForModelOrSAA.getNumbers(currentQuantity));
        }
    }

    @Override
    protected String getHotspotNumber(Map<String, String> importRec) {
        return StrUtils.removeLeadingCharsFromString(helper.handleValueOfSpecialField(SA_ITEMNO, importRec), '0');
    }

    @Override
    protected void setPartlistEntryValidities(EtkDataPartListEntry destPartListEntry, iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA, String quantityValues, boolean isTextPos) {
        // Nur bei Model-Partlist Importer
    }

    @Override
    protected Set<String> getSaaBkValidityValues(Map<String, String> importRec, boolean isTextPos) {
        // Nur bei Model
        return new LinkedHashSet<>();
    }

    @Override
    protected iPartsMigrationHelper.QuantityForModelOrSAA getQuantityForModelOrSAA(Map<String, String> importRec, List<String> quantityValues) {
        iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA = new iPartsMigrationHelper.QuantityForModelOrSAA();

        String saNumber = getSaNumber(importRec);
        String saValidity = helper.handleValueOfSpecialField(SA_STROKE, importRec);
        List<String> saValidityValues = helper.getAsArray(saValidity, 2, true, true);

        for (int i = 0; i < saValidityValues.size(); i++) {
            String strichAusfuehrungString = saNumber + saValidityValues.get(i);
            String quantity = "";
            if (i < quantityValues.size()) {
                quantity = quantityValues.get(i);
            }
            quantity = iPartsMigrationHelper.formatQuantityValue(quantity);
            quantityForModelOrSAA.add(quantity, strichAusfuehrungString);
        }
        return quantityForModelOrSAA;
    }

    @Override
    protected String getCodes(Map<String, String> importRec) {
        // Nur für Model-Partlist
        return null;
    }

    @Override
    protected String getIndentValue(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(SA_AN_INSERT, importRec);
    }

    @Override
    protected String getSteeringAndGearboxFieldname() {
        return SA_STEER;
    }

    @Override
    protected void clearQuantityValue(Map<String, String> importRec) {
        importRec.put(SA_QUANTSA, "");
    }

    @Override
    protected List<String> getQuantityValues(Map<String, String> importRec) {
        String quantityPerModel = helper.handleValueOfSpecialField(SA_QUANTSA, importRec);
        return helper.getAsArray(quantityPerModel, 3, false, true);
    }

    @Override
    protected String getShelfLife(Map<String, String> importRec, List<String> quantityValues) {
        // Ist nur bei Model-Partlist möglich. Hier haben wir kein Produkt.
        return null;
    }

    @Override
    protected String getSteeringAndGearboxValue(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(SA_STEER, importRec);
    }

    @Override
    protected void storeFinishedAssembly() {
        iPartsMigrationHelper.storeFinishedAssemblyForSa(this, getCurrentAssembly(), null, getFootnotesHandler(), getAllCurrentColorTablefootnotes(), false);
    }

    @Override
    protected String getOmittedPartNumber(Map<String, String> importRec) {
        return getPartNumber(helper, importRec, SA_OMPARTT, SA_OMPARTN, true);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        if (!importToDB) {
            cancelImport(translateForLog("!!Import nicht aktiv!"));
        }
    }


    /**
     * Der allgemeine Helper
     */
    protected class EPCSaPartListImportHelper extends EPCPartListImportHelper {

        public EPCSaPartListImportHelper(EtkProject project) {
            super(project, DEST_TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = super.handleValueOfSpecialField(sourceField, value);
            if (sourceField.equals(SA_SANUM)) {
                value = makeSANumberFromEPCValue(value);
            } else if (sourceField.equals(SA_QUANTSA) || sourceField.equals(SA_STROKE)) {
                value = StrUtils.replaceSubstring(value, ",", "");
            }
            return value;
        }
    }
}
