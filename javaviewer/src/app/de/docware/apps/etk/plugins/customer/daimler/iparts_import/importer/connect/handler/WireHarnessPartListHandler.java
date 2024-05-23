/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsWireHarnessId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;

import java.util.Map;

/**
 * Handler für den Leitungssatz-Importer, der den Inhalt des XML Tags <code>Part</code> verarbeitet
 */
public class WireHarnessPartListHandler extends AbstractWireHarnessSubHandler {

    private static final String TRIGGER_ELEMENT = "Part";

    protected static final String TYP = "Typ";
    protected static final String POS_NO = "PosNr";
    protected static final String UNTERE_SACHNUMMER = "Sachnummer";
    protected static final String USED_IN_CONTACT_LIST = "USED_IN_CONTACT_LIST";

    protected static String[] BASE_ATTRIB_NAMES = new String[]{ TYP, POS_NO, UNTERE_SACHNUMMER, BENENNUNG, LEITUNGSSATZ };

    public WireHarnessPartListHandler(EtkProject project, AbstractSAXPushHandlerImporter importer) {
        super(project, TRIGGER_ELEMENT, "PartsList", importer);
    }

    @Override
    public void doHandleCurrentRecord(Map<String, String> currentSubRecord) {
        // Hier kommt man heraus, wenn der EndTag von 'Part' kommt

        // Hier den kompletten Inhalt eines "Part" Elements (samt Unterelemente) via currentSubRecord verarbeiten
        if (currentSubRecord != null) {
            if (!checkImportPartNumbers(getTriggerElement(), currentSubRecord, LEITUNGSSATZ, UNTERE_SACHNUMMER)) {
                return;
            }
            DBDataObjectAttributes attributes = createBaseAttributes(currentSubRecord, BASE_ATTRIB_NAMES);

            // SPEZIALFALL:
            // Dieses Attribut wird bei der Verarbeitung gesetzt.
            // Daran kann man erkennen, welche Parts als "Zubehörteile" gespeichert werden müssen.
            attributes.addField(USED_IN_CONTACT_LIST, "", DBActionOrigin.FROM_DB);

            // Eine ID erzeugen
            iPartsWireHarnessId id = new iPartsWireHarnessId(attributes.getFieldValue(LEITUNGSSATZ), "", "",
                                                             attributes.getFieldValue(UNTERE_SACHNUMMER), attributes.getFieldValue(POS_NO));

            // ... und dann in die Sammelliste addieren.
            resultMap.put(id, attributes);
        }
    }

    @Override
    public void clearData() {
        clearResultMap();
    }

    public static String getTriggerElement() {
        return TRIGGER_ELEMENT;
    }
}
