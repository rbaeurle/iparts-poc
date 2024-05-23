/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.handler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWireHarnessSubHandler extends AbstractWireHarnessHandler {

    protected Map<IdWithType, DBDataObjectAttributes> resultMap;

    public AbstractWireHarnessSubHandler(EtkProject project, String mainXMLTag, String importName,
                                         AbstractSAXPushHandlerImporter importer) {
        super(project, mainXMLTag, importName, importer);
        resultMap = new HashMap<>();
    }

    public abstract void doHandleCurrentRecord(Map<String, String> currentSubRecord);

    public abstract void clearData();

    protected Map<IdWithType, DBDataObjectAttributes> getResultMap() {
        return resultMap;
    }

    protected void clearResultMap() {
        resultMap.clear();
    }

    protected boolean checkImportPartNumbers(String triggerName, Map<String, String> currentSubRecord, String... testElementNames) {
        if (testElementNames != null) {
            iPartsNumberHelper helper = new iPartsNumberHelper();
            for (String testElementName : testElementNames) {
                String value = currentSubRecord.get(testElementName);
                if (StrUtils.isValid(value)) {
                    if (!helper.isValidAorNSachNo(getProject(), value)) {
                        addWarning("!!Beim %1 Record <%2>: Ungültige A-/N-Sachnummer \"%3\". Der gesamte Record wird ignoriert!",
                                   triggerName, testElementName, value);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected String getValueFromSubRecord(Map<String, String> currentSubRecord, String name) {
        String value = currentSubRecord.get(name);
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * Befüllt die Basis-Attribute
     *
     * @param currentSubRecord
     * @param baseAttribNames
     * @return
     */
    protected DBDataObjectAttributes createBaseAttributes(Map<String, String> currentSubRecord, String[] baseAttribNames) {
        // Hier den kompletten Inhalt eines "Part" Elements (samt Unterelemente) verarbeiten
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        for (String name : baseAttribNames) {
            String baseAttValue = getValueFromSubRecord(currentSubRecord, name);
            attributes.addField(name, baseAttValue, DBActionOrigin.FROM_DB);
        }

        // Datenstand wird umformatiert von '2020-7-9' nach '2020-07-09'
        String originalDate = getValueFromSubRecord(currentSubRecord, DATENSTAND);
        String date = iPartsWireHarnessHelper.handleDatasetDate(originalDate);
        if (StrUtils.isEmpty(date)) {
            addWarning("!!Fehler beim Umwandeln des Datums \"%1\" ins Datenbankformat.",
                       originalDate);
        }
        attributes.addField(DATENSTAND, date, DBActionOrigin.FROM_DB);
        return attributes;
    }

    @Override
    protected void handleCurrentRecord() {
        // ausgenoppt
    }
}
