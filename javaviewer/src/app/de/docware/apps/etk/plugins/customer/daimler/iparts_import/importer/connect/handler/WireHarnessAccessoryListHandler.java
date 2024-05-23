/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper.DictImportConnectTextIdHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler für den Leitungssatz-Importer, der den Inhalt des XML Tags <code>Accessory</code> verarbeitet
 */
public class WireHarnessAccessoryListHandler extends AbstractWireHarnessSubHandler {

    private static final String TRIGGER_ELEMENT = "Accessory";
    protected static final String SACHNUMMER_ZUSATZTEIL = "Sachnummer-Zusatzteil";

    private final Map<String, Map<String, AccessoryListItem>> accessoryListItems; // Zubehörteil+Leitungssatz -> REF Wert -> REF Datensatz
    private final DictImportConnectTextIdHelper dictHelper; // Helfer um Texte im Lexikon zu suchen bzw. anzulegen


    public WireHarnessAccessoryListHandler(EtkProject project, AbstractSAXPushHandlerImporter importer) {
        super(project, TRIGGER_ELEMENT, "AccessoryList", importer);
        this.accessoryListItems = new HashMap<>();
        this.dictHelper = new DictImportConnectTextIdHelper(this, getProject());
    }

    @Override
    public void doHandleCurrentRecord(Map<String, String> currentSubRecord) {
        if (currentSubRecord != null) {
            // Check, ob dis Sachnummern für den Leitungssatz und das Zubehörteil gültige Sachnummern sind
            if (!checkImportPartNumbers(getTriggerElement(), currentSubRecord, SACHNUMMER_ZUSATZTEIL, LEITUNGSSATZ)) {
                return;
            }
            // Zusatzteil
            String partNo = getValueFromSubRecord(currentSubRecord, SACHNUMMER_ZUSATZTEIL);
            // Text
            String text = getValueFromSubRecord(currentSubRecord, BENENNUNG);
            EtkMultiSprache multiLang = dictHelper.searchConnectTextInDictionary(text);
            // REF Wert
            String reference = getValueFromSubRecord(currentSubRecord, REF);
            // Leitungssatz
            String wireHarness = getValueFromSubRecord(currentSubRecord, LEITUNGSSATZ);
            // Schlüssel auf Zusatzteil und Leitungssatz
            String partNoAndWireHarnessKey = makeDataKey(partNo, wireHarness);
            // Alle einzelnen REF Datensätze pro Zusatzteil-Leitungssatz Beziehung
            Map<String, AccessoryListItem> itemsForPartNoAndWireHarness = accessoryListItems.computeIfAbsent(partNoAndWireHarnessKey, k -> new HashMap<>());
            // Datensatz für den REF Schlüssel anlegen
            itemsForPartNoAndWireHarness.computeIfAbsent(reference, k -> new AccessoryListItem(multiLang, reference, wireHarness));
        }
    }

    private String makeDataKey(String partNo, String wireHarness) {
        return partNo + "||" + wireHarness;
    }

    /**
     * Liefert für das übergebenen Zusatzteil im übergebenen Leitungssatz die zugehörigen REF Datensätze
     *
     * @param partNo
     * @param wireHarnessNo
     * @return
     */
    public List<AccessoryListItem> getAccessoryItemForWireHarnessAndPartNo(String partNo, String wireHarnessNo) {
        if (StrUtils.isValid(partNo, wireHarnessNo)) {
            Map<String, AccessoryListItem> itemsForPartNo = accessoryListItems.get(makeDataKey(partNo, wireHarnessNo));
            if ((itemsForPartNo != null) && !itemsForPartNo.isEmpty()) {
                return itemsForPartNo.values().stream().filter(itemForPartNo -> itemForPartNo.getWireHarness().equals(wireHarnessNo)).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public void clearData() {
        dictHelper.clearStoredEntries();
        accessoryListItems.clear();
    }

    public static String getTriggerElement() {
        return TRIGGER_ELEMENT;
    }

    public static class AccessoryListItem {

        private final EtkMultiSprache text;
        private final String reference;
        private final String wireHarness;

        public AccessoryListItem(EtkMultiSprache text, String reference, String wireHarness) {
            this.text = text;
            this.reference = StrUtils.isValid(reference) ? reference : "";
            this.wireHarness = wireHarness;
        }

        public EtkMultiSprache getText() {
            return text;
        }

        public String getReference() {
            return reference;
        }

        public String getWireHarness() {
            return wireHarness;
        }
    }
}
