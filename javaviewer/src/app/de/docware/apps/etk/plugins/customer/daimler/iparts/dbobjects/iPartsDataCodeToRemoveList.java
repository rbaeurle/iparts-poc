package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class iPartsDataCodeToRemoveList extends EtkDataObjectList<iPartsDataCodeToRemove> implements iPartsConst {

    private iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables table;

    public iPartsDataCodeToRemoveList(iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables table) {
        this.table = table;
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataCodeToRemove getNewDataObject(EtkProject project) {
        return new iPartsDataCodeToRemove(project, table, null);
    }

    public void load(EtkProject project) {
        super.searchAndFill(project, table.getTableName(), null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Alle Codes aus einer Tabellenart holen
     *
     * @param table
     * @param project
     * @return
     */
    public static final Set<String> getCodesToRemove(iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables table, EtkProject project) {
        iPartsDataCodeToRemoveList dataCodeToRemoveList = new iPartsDataCodeToRemoveList(table);
        dataCodeToRemoveList.load(project);
        List<iPartsDataCodeToRemove> codesToRemoveList = dataCodeToRemoveList.getAsList();
        Set<String> codesToRemove = new HashSet<String>();
        for (iPartsDataCodeToRemove code : codesToRemoveList) {
            codesToRemove.add(code.getAttribute(table.getCodeFieldName()).getAsString());
        }
        return codesToRemove;
    }
}
