/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler.AbstractWireHarnessHandler;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helfer, um Texte, die via Connect Import kommen im Lexikon zu suchen oder anzulegen
 */
public class DictImportConnectTextIdHelper extends DictImportTextIdHelper {

    private final AbstractWireHarnessHandler wireHarnessHandler;
    private final Map<String, EtkMultiSprache> storedMultiLangMap;

    public DictImportConnectTextIdHelper(AbstractWireHarnessHandler wireHarnessHandler, EtkProject project) {
        super(project);
        this.wireHarnessHandler = wireHarnessHandler;
        this.storedMultiLangMap = new HashMap<>();
    }

    private EtkMultiSprache handleConnectDictTextId(String text, EtkMultiSprache storedMultiLang) {
        if (storedMultiLang != null) {
            return storedMultiLang.cloneMe();
        }
        EtkMultiSprache multiLang = new EtkMultiSprache();
        if (StrUtils.isValid(text)) {
            multiLang.setText(Language.DE, text);
            // Neue Texte hinzufügen und alle Texte berücksichtigen (unabhängig vom Status)
            boolean dictSuccessful = handleConnectTextId(multiLang, true, true);
            if (!dictSuccessful || hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : getWarnings()) {
                    wireHarnessHandler.addWarning("!!Benennung wegen \"%1\" übersprungen", str);
                }
                multiLang = new EtkMultiSprache();
            }
        } else {
            // Meldung ausgeben
        }
        return multiLang;
    }

    /**
     * Sucht den übergebenen Connect Text im Lexikon. Fall es nicht vorhanden ist, wird der Lexikon-Eintrag angelegt
     *
     * @param text
     * @return
     */
    public EtkMultiSprache searchConnectTextInDictionary(String text) {
        EtkMultiSprache multiLang;
        if (StrUtils.isValid(text)) {
            EtkMultiSprache storedMultiLang = storedMultiLangMap.get(text);
            multiLang = handleConnectDictTextId(text, storedMultiLang);
            if (storedMultiLang == null) {
                storedMultiLangMap.put(text, multiLang);
            }
        } else {
            multiLang = new EtkMultiSprache();
        }
        return multiLang;
    }

    public void clearStoredEntries() {
        storedMultiLangMap.clear();
    }
}
