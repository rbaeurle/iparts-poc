/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlist;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsMBSPartlistId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDistributionHandler;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler für die Stücklistendaten von SAP-MBS
 */
public class MBSPartlistStructureHandler extends AbstractMBSStructureHandler {

    private static final Set<String> STRUCTURE_SNR_ELEMENTS = new HashSet<>();

    // Mögliche XML Elemente in denen die obere Sachnummer stehen könnte
    static {
        STRUCTURE_SNR_ELEMENTS.add(BASE_LIST_CON_GROUP_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(BASE_LIST_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(PARTS_LIST_CON_GROUP_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(PARTS_LIST_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(CTT_LIST_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(PART_NUMBER);
    }

    private static final String REMARK_DIGIT = "RemarkDigit";
    private static final String REMARK_TEXT = "RemarkText";
    private static final String ALTERNATIVE_FLAG = "AlternativeFlag";
    private static final String ALTERNATIVE_TEXT = "AlternativeText";
    private static final String SERVICE_CONST_FLAG = "FlagServiceConstruction";

    public MBSPartlistStructureHandler(EtkProject project, MBSDataImporter importer, MBSDistributionHandler tagHandler) {
        super(project, importer, "!!SAP-MBS Stückliste", TABLE_DA_PARTSLIST_MBS, tagHandler);
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {
        mapping.put(FIELD_DPM_KEM_TO, ECO_TO);
        mapping.put(FIELD_DPM_SUB_SNR_SUFFIX, ITEM_SUFFIX);
        mapping.put(FIELD_DPM_QUANTITY_FLAG, QUANTITY_UNIT);
        mapping.put(FIELD_DPM_CODE, CODE);
        mapping.put(FIELD_DPM_SNR_TEXT, SNR_TEXT);
        mapping.put(FIELD_DPM_REMARK_ID, REMARK_DIGIT);
        mapping.put(FIELD_DPM_REMARK_TEXT, REMARK_TEXT);
        mapping.put(FIELD_DPM_WW_FLAG, ALTERNATIVE_FLAG);
        mapping.put(FIELD_DPM_WW_TEXT, ALTERNATIVE_TEXT);
        mapping.put(FIELD_DPM_SERVICE_CONST_FLAG, SERVICE_CONST_FLAG);
        mapping.put(FIELD_DPM_CTT_QUANTITY_FLAG, CTT_QUANTITY_FLAG);
    }

    @Override
    protected String getStructureSNRValue(String currentNoXMLElement) {
        // Check, ob das XML Element gültig ist
        if (StrUtils.isValid(currentNoXMLElement)) {
            // Jetzt die Nummer bestimmen, wie sie in der XML Datei steht
            String snrNumber = getCurrentRecord().get(currentNoXMLElement);
            if (currentNoXMLElement.equals(BASE_LIST_NUMBER) || currentNoXMLElement.equals(PARTS_LIST_NUMBER)) {
                // Bei echten SAA/GS sollen nur die Daten übernommen werden, bei denen die untere Sachnummer leer
                // oder eine Teilenummer ist
                String item = getItemValue();
                if (item.startsWith(MODEL_NUMBER_PREFIX_CAR) || item.startsWith(MODEL_NUMBER_PREFIX_AGGREGATE)
                    || item.startsWith(SAA_NUMBER_PREFIX) || item.startsWith(BASE_LIST_NUMBER_PREFIX)) {
                    return null;
                }
            }

            // Wenn es sich nicht um den eine CTTListNumber (Element mit den W Sachnummern), dann gleich importieren
            if (!currentNoXMLElement.equals(CTT_LIST_NUMBER)) {
                return getImportHelper().getRetailSAA(snrNumber);
            } else {
                // Wenn es sich um eine CTTListNumber handelt, müssen alle Daten übernommen werden, da wir auch obere Sachnummern
                // haben, die direkt auf Teilenummern zeigen
                return getImportHelper().addSaaPrefixIfNeeded(snrNumber, true);
            }
        }
        return null;
    }

    @Override
    protected String getQuantityFieldname() {
        return FIELD_DPM_QUANTITY;
    }

    @Override
    protected String getItemFieldname() {
        return FIELD_DPM_SUB_SNR;
    }

    @Override
    protected String getReleaseDateFromFieldname() {
        return FIELD_DPM_RELEASE_FROM;
    }

    @Override
    protected String getReleaseDateToFieldname() {
        return FIELD_DPM_RELEASE_TO;
    }

    @Override
    protected EtkDataObject getSpecificDataObject(String snrValue, String position, String sortValue, String kemFrom) {
        iPartsMBSPartlistId mbsPartlistId = new iPartsMBSPartlistId(snrValue, position, sortValue, kemFrom);
        return new iPartsDataMBSPartlist(getProject(), mbsPartlistId);
    }

    @Override
    protected Set<String> getStructureElements() {
        return STRUCTURE_SNR_ELEMENTS;
    }
}
