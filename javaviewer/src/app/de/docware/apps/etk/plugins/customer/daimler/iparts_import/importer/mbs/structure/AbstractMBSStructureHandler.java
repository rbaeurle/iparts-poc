/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDistributionHandler;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 * Abstrakter Handler für die SAP-MBS Structure-Importer
 */
public abstract class AbstractMBSStructureHandler extends AbstractMBSDataHandler {

    // Elemente, die nur für die <structure> Importer relevant sind
    protected static final String POSITION = "Position";
    protected static final String ECO_FROM = "EcoFrom";
    protected static final String ECO_TO = "EcoTo";
    protected static final String ITEM_SUFFIX = "ItemSuffix";
    protected static final String SORT = "Sort";
    protected static final String QUANTITY = "Quantity";
    public static final String CODE = "CodeRule";
    protected static final String SNR_TEXT = "Text";
    protected static final String CTT_QUANTITY_FLAG = "CTTQuantityFlag";

    // ITEM soll nicht direkt erreichbar sein, sondern nur über eine Hilfsmethode, weil der Sachnummernkennbuchstabe "Z"
    // bei manchen unteren Sachnummern ergänzt werden muss
    private static final String ITEM = "Item";

    public AbstractMBSStructureHandler(EtkProject project, MBSDataImporter importer, String importName, String tableName,
                                       MBSDistributionHandler mainHandler) {
        super(project, null, importer, importName, tableName);
        setDistributionHandler(mainHandler);
    }

    @Override
    public void handleCurrentRecord() {
        if (getCurrentRecord() == null) {
            return;
        }
        // Abhängig von Typ die SNR bestimmen
        String snrValue = getStructureSNRValue(getCurrentNoXMLElement());
        // Check, ob es sich um eine INSERT oder UPDATE Operation handelt
        if (StrUtils.isEmpty(snrValue) || !isValidAction(snrValue)) {
            return;
        }
        // Position wird von allen Sub-Handler benötigt
        String position = getValueFromCurrentRecord(POSITION);
        // Sortierung wird von allen Sub-Handler benötigt
        String sortValue = getValueFromCurrentRecord(SORT);
        // KEM ab wird von allen Sub-Handler benötigt
        String kemFrom = getValueFromCurrentRecord(ECO_FROM);
        // Sub-Handler sollen das konkrete Objekt erzeugen
        EtkDataObject dataObject = getSpecificDataObject(snrValue, position, sortValue, kemFrom);
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        // Nach dem vorgegebenen Mapping befüllen
        getImportHelper().fillOverrideCompleteDataForSAPMBS(dataObject, getCurrentRecord());
        // Freigabedatum ab und bis setzen
        dataObject.setFieldValue(getReleaseDateFromFieldname(), getReleaseDateFrom(), DBActionOrigin.FROM_EDIT);
        dataObject.setFieldValue(getReleaseDateToFieldname(), getReleaseDateTo(), DBActionOrigin.FROM_EDIT);
        // Menge setzen
        setQuantityValue(dataObject, getQuantityFieldname());
        // Untere Sachnummer setzen
        dataObject.setFieldValue(getItemFieldname(), getItemValue(), DBActionOrigin.FROM_EDIT);
        saveDataObject(dataObject);
    }

    /**
     * Liefert die untere Sachnummer aus dem Importdatensatz
     *
     * @return
     */
    protected String getItemValue() {
        String item = getValueFromCurrentRecord(ITEM);
        return getImportHelper().addSaaPrefixIfNeeded(item, true);
    }

    /**
     * Setzt die Menge aus dem SAP-MBS XML Dokument.
     *
     * @param dataObject
     */
    private void setQuantityValue(EtkDataObject dataObject, String quantityFieldname) {
        String quantity = convertMBSQuantityValue();
        dataObject.setFieldValue(quantityFieldname, quantity, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert das spezifische XML Element aus dem die aktuelle SAA oder GS Nummer gelesen wird
     *
     * @return
     */
    protected String getCurrentNoXMLElement() {
        return getStructureElements().stream().filter(element -> getCurrentRecord().get(element) != null).findFirst().orElse(null);
    }

    /**
     * Konvertiert den SAP-MBS Wert für Menge in das iParts-spezifische Format.
     *
     * @return
     */
    private String convertMBSQuantityValue() {
        String quantity = getValueFromCurrentRecord(QUANTITY);
        if (StrUtils.isValid(quantity) && (quantity.length() >= 4)) {
            int beforeComma = StrUtils.strToIntDef(quantity.substring(0, 4), -1);
            if (beforeComma > -1) {
                int afterComma = StrUtils.strToIntDef(quantity.substring(4), -1);
                quantity = String.valueOf(beforeComma);
                if (afterComma > 0) {
                    quantity += "." + afterComma;
                    quantity = StrUtils.removeAllLastCharacterIfCharacterIs(quantity, "0");
                }
            }
        }
        return quantity;
    }

    /**
     * Bestimmt den Wert der oberen Sachnummer aus dem XML Elemente. Das konkrete XML Element variiert und wird anhand
     * des statischen Sets <code>STRUCTURE_SNR_ELEMENTS</code> bestimmt.
     *
     * @param currentNoXMLElement
     * @return
     */
    protected abstract String getStructureSNRValue(String currentNoXMLElement);

    protected abstract String getQuantityFieldname();

    protected abstract String getItemFieldname();

    protected abstract String getReleaseDateFromFieldname();

    protected abstract String getReleaseDateToFieldname();

    protected abstract Set<String> getStructureElements();

    protected abstract EtkDataObject getSpecificDataObject(String snrValue, String position, String sortValue, String kemFrom);
}
