package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.file.DWFile;

/**
 * Hilfkalssse zum XML-Export eines {@link EtkDataObject}s oder einer {@link DBDataObjectList}
 * Es werden auch die Blobs (falls vorher geladen) mit exportiert.
 */
public class iPartsXmlDbExporter {

    private EtkProject project;

    public iPartsXmlDbExporter(EtkProject project) {
        this.project = project;
    }

    public boolean exportOneDataObject(DWFile destDir, EtkDataObject dataObject) {
        iPartsTableWriter tableWriter = new iPartsTableWriter(project);
        try {
            tableWriter.createRecordWriter(destDir.getAbsolutePath(), dataObject.getTableName());
            return tableWriter.write(dataObject);
        } finally {
            tableWriter.closeRecordWriter();
        }
    }

    public boolean exportOneDataObjectList(DWFile destDir, EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        iPartsTableWriter tableWriter = new iPartsTableWriter(project);
        try {
            boolean isRecordWriterCreated = false;
            for (EtkDataObject dataObject : dataObjectList) {
                if (!isRecordWriterCreated) {
                    tableWriter.createRecordWriter(destDir.getAbsolutePath(), dataObject.getTableName());
                    isRecordWriterCreated = true;
                }
                if (!tableWriter.write(dataObject)) {
                    return false;
                }
            }
        } finally {
            tableWriter.closeRecordWriter();
        }
        return true;
    }
}
