/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;

public class iPartsPartlistTypes implements iPartsConst {

    // Stücklisten Config die hauptsächlich von den GetParts Webservices verwendet wird.
    // Allerdings auch von einigen anderen Webservices und von der iParts Suche
    public static final EtkEbenenDaten PART_LIST_TYPE_FOR_GET_PARTS = new EtkEbenenDaten();

    // Hier müssen nur die ZUSÄTZLICH zu iPartsDataAssembly.NEEDED_DISPLAY_FIELDS benötigten Felder aufgelistet werden
    static {
        // Von GetParts benötigte Felder aus der KATALOG-Tabelle (teilweise auch indirekt für die Filterung)
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_FAIL_LOCLIST, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_ACC_CODE, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_STEERING, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_GEARBOX_TYPE, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, false, true));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SA_VALIDITY, false, true));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_PCLASSES_VALIDITY, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_ONLY_MODEL_FILTER, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, false, false)); // multiLang muss false sein, weil es ein virtuelles Feld ist

        // Von GetPartInfo benötigte Felder aus der KATALOG-Tabelle -> hier schon mitladen, damit die Felder dann schon im
        // Cache von EtkDataAssembly sind
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_PRODUCT_GRP, false, false));

        // Von GetParts benötigte Felder aus der MAT-Tabelle
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_SECURITYSIGN_REPAIR, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WEIGHT, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_LENGTH, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_WIDTH, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HEIGHT, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_VOLUME, false, false));
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_HAZARDOUS_GOODS_INDICATOR, false, false));

        // Von GetMaterialParts benötigte Felder
        PART_LIST_TYPE_FOR_GET_PARTS.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_SHELF_LIFE, false, false));
    }
}
