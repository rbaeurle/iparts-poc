/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;


public enum ImageImportSource {

    NONE("!!Keine Bildersuche"),
    ASPLM("!!Bilder über AS-PLM empfangen"),
    RFTSX("!!Bilder aus RFTS/x Importverzeichnis ermitteln");

    private String description;

    ImageImportSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String[] getDescriptions() {
        String[] descriptions = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            descriptions[i] = values()[i].getDescription();
        }
        return descriptions;
    }

    public static ImageImportSource getFromDescription(String configValueAsString) {
        for (ImageImportSource imageImportSource : values()) {
            String importSourceValue = imageImportSource.getDescription();
            if (importSourceValue.equalsIgnoreCase(configValueAsString)) {
                return imageImportSource;
            }
        }
        return NONE;
    }

}
