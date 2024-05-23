package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.sql.TableAndFieldName;

/**
 * Helfer zum Laden von Texten für MBS Baumuster
 */
public class iPartsMBSModelTextHelper implements iPartsConst {

    private iPartsModelId modelId;
    private boolean modelSubTextsLoaded;

    public iPartsMBSModelTextHelper(iPartsModelId modelId) {
        this.modelId = modelId;
    }

    /**
     * Lädt die Texte zu allen Subknoten zum übergebenen Baumuster
     *
     * @param project
     */
    public void loadSubTextsForModelId(EtkProject project) {
        if (modelSubTextsLoaded) {
            return;
        }
        MBSStructure mbsStructure = MBSStructure.getInstance(project, modelId);
        iPartsDataMBSStructureList structureList = loadTextSubNodes(project, modelId.getModelNumber());
        for (iPartsDataMBSStructure structure : structureList) {
            String listNumber = structure.getFieldValue(FIELD_DSM_SUB_SNR);
            MBSStructureNode node = mbsStructure.getListNumberNode(listNumber);
            if (node != null) {
                setDescription(node, structure);
                loadSubTextsForStructureId(project, new MBSStructureId(listNumber, ""));
            }
        }
        modelSubTextsLoaded = true;
    }

    /**
     * Setzt die Konstruktionsbenennung aus der GS, SAA oder Teilenummer
     *
     * @param node
     * @param structure
     */
    private void setDescription(MBSStructureNode node, iPartsDataMBSStructure structure) {
        if (node.isTextsLoaded()) {
            return;
        }
        EtkMultiSprache text = null;
        if (structure.attributeExists(FIELD_DS_CONST_DESC)) {
            text = structure.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
        } else if (structure.attributeExists(FIELD_M_TEXTNR)) {
            text = structure.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
        }
        if (text != null) {
            node.setTitle(text);
            node.setTextLoaded(true);
        }
    }

    /**
     * Lädt die Texte zu allen Subknoten zur ListNumber (SAA/GS/Teilenummer) der MBS Struktur
     *
     * @param project
     * @param structureId
     */
    public void loadSubTextsForStructureId(EtkProject project, MBSStructureId structureId) {
        MBSStructure mbsStructure = MBSStructure.getInstance(project, modelId);
        MBSStructureNode listNumberNode = mbsStructure.getListNumberNode(structureId.getListNumber());
        if ((listNumberNode == null) || listNumberNode.isSubTextsLoaded()) {
            return;
        }
        iPartsDataMBSStructureList structureList = loadTextSubNodes(project, structureId.getListNumber());
        for (iPartsDataMBSStructure structure : structureList) {
            String conGroup = structure.getFieldValue(FIELD_DSM_SUB_SNR);
            MBSStructureNode node = listNumberNode.getOrCreateChild(MBSStructureType.CON_GROUP, conGroup, listNumberNode);
            setDescription(node, structure);
        }
        listNumberNode.setSubTextsLoaded(true);
    }

    /**
     * Lädt alle Texte zur übergebenen <code>listNumber</code> (SAA/GS/Teilenummer)
     *
     * @param project
     * @param listNumber
     * @return
     */
    private iPartsDataMBSStructureList loadTextSubNodes(EtkProject project, String listNumber) {
        iPartsDataMBSStructureList structureList = loadTextBaseListData(project, listNumber);
        structureList.addAll(loadTextPartData(project, listNumber), DBActionOrigin.FROM_DB);
        return structureList;
    }

    /**
     * Lädt die Texte zur übergebenen Teilenummer
     *
     * @param project
     * @param partNumber
     * @return
     */
    private iPartsDataMBSStructureList loadTextPartData(EtkProject project, String partNumber) {
        return loadTextData(project, partNumber, TABLE_MAT, FIELD_M_TEXTNR, FIELD_M_MATNR);
    }

    /**
     * Lädt die Texte zur übergebenen SAA oder GS
     *
     * @param project
     * @param listNumber
     * @return
     */
    private iPartsDataMBSStructureList loadTextBaseListData(EtkProject project, String listNumber) {
        return loadTextData(project, listNumber, TABLE_DA_SAA, FIELD_DS_CONST_DESC, FIELD_DS_SAA);
    }


    /**
     * Lädt die Texte zu den übergebenen Parameter (Teilenummer, SAA oder GS)
     *
     * @param project
     * @param listNumber
     * @param textTable
     * @param textField
     * @param joinTableField
     * @return
     */
    private iPartsDataMBSStructureList loadTextData(EtkProject project, String listNumber, String textTable, String textField, String joinTableField) {
        iPartsDataMBSStructureList structureList = new iPartsDataMBSStructureList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR, false, false));
        structureList.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, selectFields, TableAndFieldName.make(textTable, textField),
                                                                                new String[]{ TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR),
                                                                                              TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR) },
                                                                                new String[]{ listNumber, EtkDataObjectList.getNotWhereValue("") },
                                                                                false,
                                                                                new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR },
                                                                                false,
                                                                                false,
                                                                                new EtkDataObjectList.JoinData(textTable,
                                                                                                               new String[]{ FIELD_DSM_SUB_SNR },
                                                                                                               new String[]{ joinTableField },
                                                                                                               false, false));
        return structureList;
    }

}
