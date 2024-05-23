/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Hilfsklasse mit allen Felder, die beim Laden der Stückliste im XML Exporter mitgeladen werden sollen
 */
public class iPartsXMLExportLoadingFields implements iPartsConst {

    private static final EtkEbenenDaten PART_LIST_TYPE_FOR_EXPORT = new EtkEbenenDaten();
    private static final EtkEbenenDaten PSK_MAT_FIELDS_FOR_EXPORT = new EtkEbenenDaten();
    private static final EtkDisplayFields WIRING_HARNESS_DISPLAY_FIELDS = new EtkDisplayFields();
    private static final EtkDisplayFields ADDITIONAL_PART_INFORMATION_FIELDS = new EtkDisplayFields();

    // Auskommentierte Felder werden aktuell vom Export noch nicht benötigt (Feldliste stammt ursprünglich von den Webservices,
    // wo diese Felder alle gebraucht werden)
    static {
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_POS, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_MENGE, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_HIERARCHY, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_STEERING, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_GEARBOX_TYPE, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_AA, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_OMIT, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_CODES, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_CODES_REDUCED, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_EVENT_FROM, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_EVENT_TO, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, false, true));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SA_VALIDITY, false, true));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_ONLY_MODEL_FILTER, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_PRODUCT_GRP, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_COUNTRY_VALIDITY, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SPEC_VALIDITY, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_ADDTEXT, true, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        PART_LIST_TYPE_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));

        // PSK Felder
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_MATERIAL, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_SUPPLIER_NO, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_MANUFACTURER_NO, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_SUPPLIER_MATNR, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_MANUFACTURER_MATNR, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_IMAGE_NO_EXTERN, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_PSK_REMARK, false, false));
        PSK_MAT_FIELDS_FOR_EXPORT.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_NATO_NO, false, false));

        // Alle benötigten Felder aus DA_WIRE_HARNESS
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SUB_SNR, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_REF, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONNECTOR_NO, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR_TYPE, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT, true, false));

        // Die benötigten Felder aus MAT
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_ETKZ, false, false)); // Für den Filter
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_ADDTEXT, true, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        WIRING_HARNESS_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));

        // Gewicht, Länge, Breite, Höhe und Volumen
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WEIGHT, false, false));
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_LENGTH, false, false));
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WIDTH, false, false));
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HEIGHT, false, false));
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_VOLUME, false, false));
        ADDITIONAL_PART_INFORMATION_FIELDS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HAZARDOUS_GOODS_INDICATOR, false, false));

        WIRING_HARNESS_DISPLAY_FIELDS.addFelder(ADDITIONAL_PART_INFORMATION_FIELDS);
    }

    public static EtkEbenenDaten getPartListTypeForExport() {
        return PART_LIST_TYPE_FOR_EXPORT;
    }

    public static EtkEbenenDaten getPskMatFieldsForExport() {
        return PSK_MAT_FIELDS_FOR_EXPORT;
    }

    public static EtkEbenenDaten getAllFieldsForExport() {
        EtkEbenenDaten result = new EtkEbenenDaten();
        result.addFelder(PART_LIST_TYPE_FOR_EXPORT);
        result.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_PSK_VARIANT_VALIDITY, false, true)); // PSK-Varianten-Gültigkeit
        result.addFelder(PSK_MAT_FIELDS_FOR_EXPORT);
        return result;
    }

    public static EtkDisplayFields getAdditionalPartInformationFields() {
        return ADDITIONAL_PART_INFORMATION_FIELDS;
    }

    public static EtkDisplayFields getWiringHarnessDisplayFields() {
        return WIRING_HARNESS_DISPLAY_FIELDS;
    }
}
