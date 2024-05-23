/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.PartsSearchSqlSelect;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataSetCancelable;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstrakte Klasse für die Suche in virtuellen Materialtabellen in iParts.
 */
public abstract class iPartsVirtualMaterialSearchDataset extends iPartsSearchVirtualDatasetWithDBDataset implements EtkDbConst, iPartsConst {

    protected String materialTable;
    protected String partNumberField;
    protected AssemblyId optionalRootAssemblyId;
    protected boolean isSearchValuesDisjunction;
    protected List<String> selectValues;
    protected EtkDisplayFields whereFields;
    protected List<String> whereValues;
    protected boolean andOrSearch;
    protected iPartsVirtualNode virtualRootNode;
    protected EtkDisplayFields selectFieldsWithoutKatalog;
    protected List<String> selectValuesWithoutKatalog;
    protected WildCardSettings wildCardSettings;

    public iPartsVirtualMaterialSearchDataset(String materialTable, String partNumberField,
                                              AssemblyId optionalRootAssemblyId, boolean isSearchValuesDisjunction,
                                              EtkDisplayFields selectFields, List<String> selectValues,
                                              EtkDisplayFields whereFields, List<String> whereValues,
                                              boolean andOrSearch, EtkProject project, WeakKeysMap<String, String> multiLanguageCache,
                                              WildCardSettings wildCardSettings) {
        super(selectFields, project, multiLanguageCache);
        this.materialTable = materialTable;
        this.partNumberField = partNumberField;
        this.optionalRootAssemblyId = optionalRootAssemblyId;
        this.isSearchValuesDisjunction = isSearchValuesDisjunction;
        this.selectValues = selectValues;
        this.whereFields = whereFields;
        this.whereValues = whereValues;
        this.andOrSearch = andOrSearch;
        this.wildCardSettings = wildCardSettings;
    }

    @Override
    public DBDataSetCancelable createDBDataSet() throws CanceledException {
        virtualRootNode = iPartsVirtualNode.getVirtualRootNodeFromAssemblyId(optionalRootAssemblyId);

        // alle selectFields für die Tabelle KATALOG entfernen (wird später virtuell dazugemappt)
        selectFieldsWithoutKatalog = new EtkDisplayFields();
        selectValuesWithoutKatalog = new ArrayList<String>();
        int i = 0;
        for (EtkDisplayField selectField : dsSelectFields.getFields()) {
            if (!selectField.getKey().getTableName().equals(TABLE_KATALOG)) {
                selectFieldsWithoutKatalog.addFeld(selectField);
                selectValuesWithoutKatalog.add(selectValues.get(i));
            }
            i++;
        }

        addAdditionalSelectFields(selectFieldsWithoutKatalog, selectValuesWithoutKatalog);

        List<String> doNotJoinList = null;

        // Teilesuche über Stücklistenfelder
        PartsSearchSqlSelect partsSearchSqlSelect = new PartsSearchSqlSelect(materialTable, partNumberField,
                                                                             null, null, null, project);
        partsSearchSqlSelect.buildPartsSearchSqlSelectForSearchByFields(isSearchValuesDisjunction,
                                                                        selectFieldsWithoutKatalog, selectValuesWithoutKatalog,
                                                                        whereFields, whereValues,
                                                                        andOrSearch, wildCardSettings,
                                                                        project.getDBLanguage(), true);

        addNeededJoins(partsSearchSqlSelect, doNotJoinList);

        return partsSearchSqlSelect.createAbfrageCancelable();
    }

    @Override
    public boolean next() throws CanceledException {
        return getDBDataSet().next();
    }

    @Override
    public EtkDataPartListEntry[] get() {
        List<String> values = getDBDataSet().getStringList();
        String partNumber = getPartNumber(values);
        HierarchicalIDWithType parentId = createParentId(values);

        List<EtkDataPartListEntry> resultPartListEntries = searchResultPartListEntries(values, partNumber, parentId);
        if ((resultPartListEntries != null) && !resultPartListEntries.isEmpty()) {
            return resultPartListEntries.toArray(new EtkDataPartListEntry[resultPartListEntries.size()]);
        } else {
            return null;
        }
    }

    protected void createAndAddSearchResultPartListEntry(AssemblyId assemblyId, String kLfdnr, String partNumber, List<EtkDataPartListEntry> resultPartListEntries) {
        PartListEntryId partListEntryId = new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), kLfdnr);
        EtkDataPartListEntry entry = EtkDataObjectFactory.createDataPartListEntry(project, partListEntryId);
        entry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        // K_VARI, K_VER und K_LFDNR werden bereits durch die partListEntryId gesetzt
        entry.setFieldValue(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        entry.setFieldValue(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        entry.setFieldValue(FIELD_K_MATNR, partNumber, DBActionOrigin.FROM_DB);
        entry.setFieldValue(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        resultPartListEntries.add(entry);
    }

    protected abstract void addNeededJoins(PartsSearchSqlSelect partsSearchSqlSelect, List<String> doNotJoinList);

    protected abstract void addAdditionalSelectFields(EtkDisplayFields selectFieldsWithoutKatalog, List<String> selectValuesWithoutKatalog);

    protected abstract String getPartNumber(List<String> values);

    protected abstract HierarchicalIDWithType createParentId(List<String> values);

    protected abstract List<EtkDataPartListEntry> searchResultPartListEntries(List<String> values, String partNumber, HierarchicalIDWithType parentId);
}