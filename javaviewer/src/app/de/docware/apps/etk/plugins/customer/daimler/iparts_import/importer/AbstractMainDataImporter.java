/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractEtkFunctionImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.List;
import java.util.Map;

/**
 * Abstrakte Klasse für "Main-Importer" mit Methoden, die von allen genutzt werden
 */
public abstract class AbstractMainDataImporter extends AbstractDataImporter {

    protected Map<String, DWFile> importList;
    protected Map<String, EtkFunction> importerList;
    protected AbstractEtkFunctionImportHelper currentFunctionImportHelper;

    public AbstractMainDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
    }

    /**
     *
     */
    protected void checkImporter() {
        List<String> warningList = new DwList<String>();
        for (String key : importList.keySet()) {
            EtkFunction functionImportHelper = importerList.get(key);
            if (functionImportHelper == null) {
                DWFile file = importList.get(key);
                warningList.add(file.extractFileName(true));
            }
        }

        if (!warningList.isEmpty()) {
            if (warningList.size() == 1) {
                getMessageLog().fireMessage(translateForLog("!!Kein Importer für \"%1\" definiert", warningList.get(0)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            } else {
                StringBuilder str = new StringBuilder();
                for (String string : warningList) {
                    if (str.length() > 0) {
                        str.append(", ");
                    }
                    str.append("\"");
                    str.append(string);
                    str.append("\"");
                }
                getMessageLog().fireMessage(translateForLog("!!Keine Importer für [%1] definiert", str.toString()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
        }
    }

    @Override
    public void cancelImport() {
        super.cancelImport();
        if (currentFunctionImportHelper != null) {
            currentFunctionImportHelper.cancelImport();
        }
    }
}
