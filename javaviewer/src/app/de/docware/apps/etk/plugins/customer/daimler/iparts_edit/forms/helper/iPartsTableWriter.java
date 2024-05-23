package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.db.SyncEtkRecord;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.ppsync.client.SyncEtkDbsTableWriter;

public class iPartsTableWriter extends SyncEtkDbsTableWriter {

    public iPartsTableWriter(EtkProject project) {
        super(project);
    }

    @Override
    public boolean createRecordWriter(String rootPath, String tableName) {
        tableDef = project.getConfig().getDBDescription().findTable(tableName);
        return super.createRecordWriter(rootPath, tableName);
    }

    public boolean write(EtkDataObject dataObject) {
        if (dataObject != null) {
            SyncEtkRecord syncRecord = new SyncEtkRecord(dataObject.getAttributes().getAsRecord(false));
            return writeRecordWriter(syncRecord, false);
        }
        return false;
    }
}
