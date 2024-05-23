/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Alle möglichen Ergebnis-Attribute für die Bildrecherche
 */
public enum iPartsTransferSMCAttributes {

    //       1234567890   <== maximal 10 Zeichen in der DB für die Benennung der Enummeration
    SMC_NAME("SMC_NAME", "!!Name", "name"),
    SMC_DESCRIPTION("SMC_DESC", "!!Bezeichnung", "description"),
    SMC_ALTERNATE_ID("SMC_A_ID", "!!Alternative ID", "alternateID"),
    SMC_DIFFICULTY("SMC_DIFF", "!!Schwierigkeit", "difficulty"), // Was ist hiermit gemeint?
    SMC_EINPAS_HG("SMC_HG", "!!EinPAS HG", "einpasMainGroup"),
    SMC_EINPAS_G("SMC_G", "!!EinPAS G", "einpasGroup"),
    SMC_EINPAS_TU("SMC_TU", "!!EinPAS TU", "einpasTechnicalScope"),
    SMC_IMAGE_FORMAT("SMC_IM_FO", "!!Bildformat", "imageFormat"),
    SMC_PRESENTATION("SMC_SHOW", "!!Umsetzungsart", "presentation"),
    SMC_PURPOSE("SMC_PURP", "!!Verwendung", "purpose"),
    SMC_REALIZATION("SMC_REAL", "!!Darstellungsart", "realization"),
    SMC_REMARKS("SMC_REMA", "!!Bemerkungen", "remarks");

    private String alias;
    private String description;
    private String asplmValue;

    iPartsTransferSMCAttributes(String alias, String description, String asplmValue) {
        this.alias = alias;
        this.description = description;
        this.asplmValue = asplmValue;
    }

    public String getAlias() {
        return alias;
    }

    public static iPartsTransferSMCAttributes getFromAlias(String alias) {
        for (iPartsTransferSMCAttributes result : values()) {
            if (result.alias.equals(alias)) {
                return result;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getAsASPLMValue() {
        return asplmValue;
    }

    public static iPartsTransferSMCAttributes getValidatedAttributeByDescription(String description) {
        for (iPartsTransferSMCAttributes result : values()) {
            if (result.asplmValue.equals(description)) {
                return result;
            }
        }
        return null;
    }


}


