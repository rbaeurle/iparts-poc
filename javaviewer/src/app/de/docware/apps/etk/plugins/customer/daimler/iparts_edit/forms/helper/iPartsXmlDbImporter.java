package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkXMLUpdateRecord;
import de.docware.apps.etk.ppsync.client.SyncXMLToEtkRecord;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Hilfkalssse zum XML-Import eines {@link EtkDataObject}s oder einer {@link DBDataObjectList}
 * Es werden auch die Blobs (falls vorher geladen) mit importiert.
 */
public class iPartsXmlDbImporter {

    private EtkProject project;

    public iPartsXmlDbImporter(EtkProject project) {
        this.project = project;
    }

    public boolean importOneDataObject(DWFile srcDir, EtkDataObject dataObject) {
        if (dataObject == null) {
            return false;
        }
        List<String> allFields = getAllFieldsFromDataObject(dataObject);
        if (allFields == null) {
            return false;
        }

        DWFile mainImportFile = srcDir.getChild(dataObject.getTableName() + "." + MimeTypes.EXTENSION_XML);
        if (!mainImportFile.isFile()) {
            // Exportdatei fehlt
            return false;
        }

        SyncXMLToEtkRecord xmlToEtkRecord = new SyncXMLToEtkRecord(project, false);
        boolean result = xmlToEtkRecord.open(mainImportFile.getAbsolutePath());
        if (result) {
            try {
                if (xmlToEtkRecord.getRecordCount() > 0) {
                    EtkXMLUpdateRecord rec = xmlToEtkRecord.read(allFields, null);
                    if (rec != null) {
                        DBDataObjectAttributes attribs = DBDataObjectAttributes.getFromRecord(rec, DBActionOrigin.FROM_DB);
                        dataObject.assignAttributes(project, attribs, true, DBActionOrigin.FROM_DB);
                    }
                }
            } finally {
                xmlToEtkRecord.close(); // bei result == false findet schon ein close() statt
            }
        }
        return result;
    }

    public boolean importDataObjectList(DWFile srcDir, DBDataObjectList dataObjectList,
                                        GetNewEtkDataObjectEvent onNewDataObject) {
        if ((dataObjectList == null) || (onNewDataObject == null)) {
            return false;
        }
        EtkDataObject dataObject = onNewDataObject.createNewDataObject(project);
        List<String> allFields = getAllFieldsFromDataObject(dataObject);
        if (allFields == null) {
            return false;
        }

        DWFile mainImportFile = srcDir.getChild(dataObject.getTableName() + "." + MimeTypes.EXTENSION_XML);
        if (!mainImportFile.isFile()) {
            // bei Listen ist es möglich, dass keine Exportdateien existieren (leere Liste) => kein Fehler
            return true;
        }

        dataObjectList.clear(DBActionOrigin.FROM_DB);
        SyncXMLToEtkRecord xmlToEtkRecord = new SyncXMLToEtkRecord(project, false);
        boolean result = xmlToEtkRecord.open(mainImportFile.getAbsolutePath());
        if (result) {
            try {
                if (xmlToEtkRecord.getRecordCount() > 0) {
                    EtkXMLUpdateRecord rec = xmlToEtkRecord.read(allFields, null);
                    while (rec != null) {
                        DBDataObjectAttributes attribs = DBDataObjectAttributes.getFromRecord(rec, DBActionOrigin.FROM_DB);
                        dataObject = onNewDataObject.createNewDataObject(project);
                        dataObject.assignAttributes(project, attribs, true, DBActionOrigin.FROM_DB);
                        dataObjectList.add(dataObject, DBActionOrigin.FROM_DB);
                        rec = xmlToEtkRecord.read(allFields, null);
                    }
                }
            } finally {
                xmlToEtkRecord.close(); // bei result == false findet schon ein close() statt
            }
        }
        return result;
    }

    private List<String> getAllFieldsFromDataObject(EtkDataObject dataObject) {
        List<String> result = null;
        if (dataObject != null) {
            EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(dataObject.getTableName());
            if (tableDef != null) {
                result = new DwList<>();
                result.addAll(tableDef.getAllFieldsNoBlob());
                result.addAll(tableDef.getBlobFields());
            }
        }
        return result;
    }
}
