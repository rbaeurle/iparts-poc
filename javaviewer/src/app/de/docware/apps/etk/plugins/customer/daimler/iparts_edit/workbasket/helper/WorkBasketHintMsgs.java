/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

/**
 * Enums und Texte für die Hint-Nachrichten in den Arbeitsvorräten
 */
public enum WorkBasketHintMsgs {

    WBH_MANUAL_STATUS("!!manueller Status gesetzt"),
    WBH_MANUAL_STATUS_SET("!!durch manuellen Wert überschrieben"),
    WBH_USED_IN_RETAIL("!!im Retail benutzt"),
    WBH_NOT_USED_IN_RETAIL("!!im Retail nicht benutzt"),
    WBH_MODEL_NOT_DOCU_REL("Baumuster \"%1\" ist als nicht-dokurelevant gekennzeichnet"),
    WBH_MODEL_NOT_USED_IN_PRODUCT("!!Baumuster \"%1\" ist keinem Produkt zugeordnet"),
    WBH_SAA_NOT_DOCU_REL("!!SAA/BK \"%1\" ist als nicht-dokurelevant gekennzeichnet"),
    WBH_SAA_GS_NOT_DOCU_REL("!!SAA/BK/GS \"%1\" ist als nicht-dokurelevant gekennzeichnet"),
    WBH_SAA_NO_MODEL_USAGE("!!SAA/BK \"%1\" hat noch keine Baumuster-Verwendung"),
    WBH_USED_IN_NUTZDOK("!!Eintrag in NutzDok"),
    WBH_SAA_NO_NOT_FOUND("!!keine SAA/BK-Nummer gefunden"),
    WBH_SAA_GS_NO_NOT_FOUND("!!keine SAA/BK/GS-Nummer gefunden"),
    WBH_USED_ALSO_IN_OTHER_PRODUCTS("!!wird auch in weiteren Produkten (%1) benutzt"),
    WBH_NO_RELATED_PRODUCT_FOUND("!!keine zugewiesene Produktnummer gefunden"),
    WBH_USED_ONLY_IN_OTHER_PRODUCTS("!!wird nur in weiteren Produkten (%1) benutzt"),
    WBH_USED_IN_OTHER_PRODUCTS("!!wird in weiteren Produkten (%1) benutzt"),
    WBH_USED_IN_NOT_RELEASED_ORDERS("!!ist in nicht freigegebenen Autoren-Aufträgen dokumentiert"),
    WBH_SA_TU_EXISTS("!!SA-TU \"%1\" vorhanden"),
    WBH_HAS_NUTZDOK_ENTRY("!!Eintrag in NutzDok"),
    WBH_BASIC_PARTLIST("!!Grundstückliste"),
    WBH_MODEL_PRODUCT_RELATION_EXISTS("!!Baumuster-Produkt-Beziehung vorhanden"),
    WBH_ET_VIEW_EXTENSION("!!ET-Sichtenerweiterung"),
    WBH_PRODUCT_EXPIRED("!!Produkt \"%1\" ist ausgelaufen"),
    WBH_RETAIL_USAGE_IN_SATU("!!Retail-Verwendung in SA-TU \"%1\""),
    WBH_NO_USAGE_IN_SATU("!!Kein SA-TU vorhanden \"%1\""),
    WBH_NO_SAA_IN_SATU("!!SAA im SA-TU \"%1\" nicht dokumentiert"),
    WBH_SATU_NOT_CONNECTED("!!SA-TU \"%1\" ist keiner KG in diesem Produkt zugewiesen"),
    WBH_UNDOCUMENTED_PRODUCTS("!!Produkte zu denen der SA-TU noch nicht zugeordnet ist: \"%1\""),
    WBH_MODEL_KEM_INTERVALL_NO_OVERLAPPING("!!Baumuster und KEM-Intervall überschneiden sich nicht"),
    WBH_MODEL_RELEASE_DATE_EXCEEDED("!!Baumuster-Freigabetermin \"%1\" bereits überschritten"),
    WBH_MODEL_START_DATE_NOT_REACHED("!!Baumuster-Starttermin \"%1\" noch nicht erreicht"),
    WBH_RELEASE_DATE_STARTS_LATER("!!Freigabetermin \"%1\" beginnt erst"),
    WBH_RELEASE_DATE_EXCEEDED("!!Freigabetermin \"%1\" bereits überschritten"),
    WBH_MODEL_EXISTS_IN("!!Baumuster \"%1\" in \"%2\" vorhanden"),
    WBH_MODEL_EXISTS_NOT_IN("!!Baumuster \"%1\" nicht in \"%2\" vorhanden"),
    WBH_MODEL_IMPLICIT_IN("!!Baumuster \"%1\" implizit in \"%2\" vorhanden");

    private String key;

    WorkBasketHintMsgs(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
