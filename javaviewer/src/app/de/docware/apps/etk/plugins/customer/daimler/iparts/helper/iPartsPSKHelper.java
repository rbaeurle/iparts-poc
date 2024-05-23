/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helfer für zentrale PSK Hilfsmethoden
 */
public class iPartsPSKHelper {

    private static final Set<String> HIDDEN_FIELDS_FOR_PSK = new HashSet<>();

    static {
        HIDDEN_FIELDS_FOR_PSK.add(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE);
        HIDDEN_FIELDS_FOR_PSK.add(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE_REASON);
    }

    /**
     * Setzt bei den übergebenen {@link EtkDisplayField}s die Felder auf unsichtbar, die bei PSK Modulen nicht angezeigt
     * werden dürfen
     *
     * @param assembly
     * @param fields
     */
    public static void handlePSKDisplayFields(EtkDataAssembly assembly, List<EtkDisplayField> fields) {
        if (isPSKAssembly(assembly)) {
            fields.forEach(field -> {
                if (field.isVisible() && HIDDEN_FIELDS_FOR_PSK.contains(field.getKey().getFieldName())) {
                    field.setVisible(false);
                }
            });
        }
    }

    /**
     * Handelt es sich um ein PSK-Modul mit entsprechender PSK-Doku-Methode bzw. dazugehörigem PSK-Produkt?
     *
     * @return
     */
    public static boolean isPSKAssembly(EtkDataAssembly assembly) {
        return (assembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)assembly).isPSKAssembly();
    }
}
