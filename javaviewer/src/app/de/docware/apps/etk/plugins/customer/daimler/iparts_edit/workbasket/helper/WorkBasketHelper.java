package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLStringConvert;

/**
 * Sammel-Container für Routinen, die alle WorkBaskets benutzen
 */
public class WorkBasketHelper {

    private EtkProject project;

    public WorkBasketHelper(EtkProject project) {
        this.project = project;
    }

    /**
     * Hint zusammensetzen
     *
     * @param str
     * @param key
     * @param placeHolderTexts
     */
    public void appendToDocuRelReason(StringBuilder str, String key, String... placeHolderTexts) {
        if (str.length() > 0) {
            str.append(OsUtils.NEWLINE);
        }
        str.append(TranslationHandler.translate(key, placeHolderTexts));
    }


    public void appendToDocuRelReason(DBDataObjectAttributes attributes, String fieldName, String key, String... placeHolderTexts) {
        StringBuilder str = new StringBuilder(attributes.getFieldValue(fieldName));
        appendToDocuRelReason(str, key, placeHolderTexts);
        attributes.addField(fieldName, str.toString(), DBActionOrigin.FROM_DB);
    }

    public void appendToDocuRelReason(DBDataObjectAttributes attributes, String fieldName, WorkBasketHintMsgs key, String... placeHolderTexts) {
        StringBuilder str = new StringBuilder(attributes.getFieldValue(fieldName));
        appendToDocuRelReason(str, key.getKey(), placeHolderTexts);
        attributes.addField(fieldName, str.toString(), DBActionOrigin.FROM_DB);
    }

    public void appendToDocuRelReason(StringBuilder str, WorkBasketHintMsgs key, String... placeHolderTexts) {
        appendToDocuRelReason(str, key.getKey(), placeHolderTexts);
    }

    /**
     * Überprüft, dass der manuelle Status nicht gesetzt ist
     *
     * @param manualStatus
     * @param docuRel
     * @param strDocuRelReason
     * @return false: manueller Status gesetzt
     */
    public boolean isManualStatusValid(iPartsDocuRelevantTruck manualStatus, VarParam<iPartsDocuRelevantTruck> docuRel,
                                       StringBuilder strDocuRelReason) {
        if (manualStatus != iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED) {
            // Manueller Status gesetzt
            appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MANUAL_STATUS);
            docuRel.setValue(manualStatus);
            return false;
        }
        return true;
    }

    /**
     * Überprüft, ob der berechnete Status=O (DOCU_RELEVANT_TRUCK_YES) und ob das Produkt ausgelaufen ist
     * Setzt in diesem Fall die relevanten Attribut-Werte
     *
     * @param attributes
     * @param productNo
     * @param fieldNameDocuRel
     * @param fieldNameCase
     * @param fieldNameDocuRelReason
     */
    public void checkProductStatus(DBDataObjectAttributes attributes, String productNo,
                                   String fieldNameDocuRel, String fieldNameCase, String fieldNameDocuRelReason) {
        String currentDocuRel = attributes.getFieldValue(fieldNameDocuRel);
        // Ist der berechnete Status = O mit einem gültigen Produkt und der Auswertung via ProductSupplier
        if (iPartsDocuRelevantTruck.getFromDBValue(currentDocuRel).equals(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES) &&
            StrUtils.isValid(productNo) && iPartsPlugin.isUseProductSupplier()) {
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
            // Hat der ProductSupplier den Wert "Produkt_ausgelaufen"
            if (!product.getDocumentationType().isPKWDocumentationType() && (product.getProductSupplier() != null) &&
                product.getProductSupplier().equals("Produkt_ausgelaufen")) {
                // setze berechneten Status auf ANR; den Geschäftsfall zurücksetzen und Hint erweitern
                setAttribValue(attributes, fieldNameDocuRel, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_YET.getDbValue());
                setAttribValue(attributes, fieldNameCase, iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED.getDbValue());
                appendToDocuRelReason(attributes, fieldNameDocuRelReason, WorkBasketHintMsgs.WBH_PRODUCT_EXPIRED, productNo);
            }
        }
    }

    public void setAttribValue(DBDataObjectAttributes attributes, String fieldName, String value) {
        attributes.addField(fieldName, value, VirtualFieldsUtils.isVirtualField(fieldName), DBActionOrigin.FROM_DB);
    }

    public void setAttribValue(DBDataObjectAttributes attributes, String fieldName, boolean value) {
        setAttribValue(attributes, fieldName, SQLStringConvert.booleanToPPString(value));
    }
}
