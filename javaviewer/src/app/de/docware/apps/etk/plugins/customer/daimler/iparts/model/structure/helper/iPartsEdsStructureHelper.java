/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsEDSModelContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.Ops;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureModelNode;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Helfer, über den die Strukturinformation zur EDS-Struktur bestimmt bzw. geladen werden
 */
public class iPartsEdsStructureHelper implements iPartsConst {

    private static iPartsEdsStructureHelper instance;

    public static iPartsEdsStructureHelper getInstance() {
        if (instance == null) {
            instance = new iPartsEdsStructureHelper();
        }
        return instance;
    }

    private boolean isNewStructureActive;

    private iPartsEdsStructureHelper() {
        checkEdsStructureAdminOption();
    }

    private void checkEdsStructureAdminOption() {
        isNewStructureActive = iPartsPlugin.isEditPluginActive() && de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.isNewEDSBCSStructureActive();
    }

    public boolean isNewStructureActive() {
        return isNewStructureActive;
    }

    public String getStructureTableName() {
        return isNewStructureActive ? TABLE_DA_MODEL_ELEMENT_USAGE : TABLE_DA_EDS_MODEL;
    }

    public String getModelNumberField() {
        return isNewStructureActive ? FIELD_DMEU_MODELNO : FIELD_EDS_MODEL_MODELNO;
    }

    public String getUpperStructureValueField() {
        return isNewStructureActive ? FIELD_DMEU_MODULE : FIELD_EDS_MODEL_GROUP;
    }

    public String getLowerStructureValueField() {
        return isNewStructureActive ? FIELD_DMEU_SUB_MODULE : FIELD_EDS_MODEL_SCOPE;
    }

    public String getSubElementField() {
        return isNewStructureActive ? FIELD_DMEU_SUB_ELEMENT : FIELD_EDS_MODEL_MSAAKEY;
    }

    public String getRevisionFromField() {
        return isNewStructureActive ? FIELD_DMEU_REVFROM : FIELD_EDS_MODEL_REVFROM;
    }

    public String getKemFromField() {
        return isNewStructureActive ? FIELD_DMEU_KEMFROM : FIELD_EDS_MODEL_KEMFROM;
    }

    public String getReleaseFromField() {
        return isNewStructureActive ? FIELD_DMEU_RELEASE_FROM : FIELD_EDS_MODEL_RELEASE_FROM;
    }

    public String getReleaseToField() {
        return isNewStructureActive ? FIELD_DMEU_RELEASE_TO : FIELD_EDS_MODEL_RELEASE_TO;
    }

    public String getCodeField() {
        return isNewStructureActive ? FIELD_DMEU_CODE : FIELD_EDS_MODEL_CODE;
    }

    public String getPlantSupplyField() {
        return isNewStructureActive ? FIELD_DMEU_PLANTSUPPLY : FIELD_EDS_MODEL_PLANTSUPPLY;
    }

    public String getPosField() {
        return isNewStructureActive ? FIELD_DMEU_POS : FIELD_EDS_MODEL_POS;
    }

    public String getVirtualProductGroupSignField() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_PGKZ : iPartsDataVirtualFieldsDefinition.OPS_SCOPE_PGKZ;
    }

    public String getVirtualReleaseFromField() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_RELEASE_FROM : iPartsDataVirtualFieldsDefinition.OPS_SCOPE_RELEASE_FROM;
    }

    public String getVirtualCodeField() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_CODE : iPartsDataVirtualFieldsDefinition.OPS_SCOPE_CODE;
    }

    public String getVirtualPosField() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_POS : iPartsDataVirtualFieldsDefinition.OPS_SCOPE_POS;
    }

    public String getVirtualQuantityField() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_QUANTITY : iPartsDataVirtualFieldsDefinition.OPS_SCOPE_QUANTITY;
    }

    public String getVirtualFieldPrefix() {
        return isNewStructureActive ? iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE
                                    : iPartsDataVirtualFieldsDefinition.OPS_SCOPE;
    }

    public void addDefaultDisplayFieldsForEdsLowerStructure(EtkDisplayFields defaultDisplayFields) {
        EtkDisplayField etkDisplayField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, getVirtualCodeField(), false, false);
        defaultDisplayFields.addFeld(etkDisplayField);
        etkDisplayField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, getVirtualQuantityField(), false, false);
        defaultDisplayFields.addFeld(etkDisplayField);
        if (isNewStructureActive) {
            etkDisplayField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_SAA_BK_DESC, true, false);
            defaultDisplayFields.addFeld(etkDisplayField);
        }
    }

    public iPartsNodeType getEdsModelNodeType() {
        return isNewStructureActive ? iPartsNodeType.EDS_MODEL_ELEMENT_USAGE : iPartsNodeType.EDS_OPS;
    }

    public iPartsNodeType getEdsStructureNodeType() {
        return isNewStructureActive ? iPartsNodeType.MODEL_ELEMENT_USAGE : iPartsNodeType.OPS;
    }

    public EdsSaaId getEdsStructureSaaFromAssemblyId(AssemblyId assemblyId) {
        return isNewStructureActive ? iPartsVirtualNode.getModelElementUsageSAAFromAssemblyId(assemblyId)
                                    : iPartsVirtualNode.getOpsSAAFromAssemblyId(assemblyId);
    }

    public iPartsVirtualNode createEdsModelVirtualNode(iPartsModelId modelId) {
        return new iPartsVirtualNode(getEdsModelNodeType(), modelId);
    }

    public iPartsVirtualNode createEdsStructureVirtualNode(String upperValue, String lowerValue) {
        return new iPartsVirtualNode(getEdsStructureNodeType(), createStructureId(upperValue, lowerValue));
    }

    public iPartsVirtualNode createEdsStructureVirtualNode(IdWithType structureId) {
        return new iPartsVirtualNode(getEdsStructureNodeType(), structureId);
    }

    public iPartsStructureModelNode createStructureNode(iPartsModelId modelId) {
        return new iPartsStructureModelNode(modelId, getEdsModelNodeType());
    }

    public EtkDataObject createPathToSaaWithValidModelObject(EtkProject project, String modelNo, String upperStructureValue,
                                                             String lowerStructureValue) {
        if (isNewStructureActive) {
            iPartsModelElementUsageId modelElementUsageId = new iPartsModelElementUsageId(modelNo, upperStructureValue,
                                                                                          lowerStructureValue, "", "", "");
            return new iPartsDataModelElementUsage(project, modelElementUsageId);
        } else {
            iPartsEDSModelContentId modelContentId = new iPartsEDSModelContentId(modelNo, upperStructureValue,
                                                                                 lowerStructureValue, "", "", "", "");
            return new iPartsDataEDSModelContent(project, modelContentId);
        }
    }

    public EtkDataObjectList<? extends EtkDataObject> loadAllStructureEntriesForSaaOrBK(EtkProject project, String saaBkNo) {
        return isNewStructureActive ? iPartsDataModelElementUsageList.loadAllSaaBkEntries(project, saaBkNo)
                                    : iPartsDataEDSModelContentList.loadAllSaaBkEntries(project, saaBkNo);
    }

    public EtkDataObjectList<? extends EtkDataObject> loadAllStructureEntriesForModelAndSubElement(EtkProject project,
                                                                                                   String modelNumber,
                                                                                                   HierarchicalIDWithType structureId,
                                                                                                   String saaBKNumber) {
        if (isNewStructureActive && structureId.getType().equals(ModelElementUsageId.TYPE)) {
            ModelElementUsageId modelElementUsageId = (ModelElementUsageId)structureId;
            return iPartsDataModelElementUsageList.loadAllModelSaaEntriesWithStructure(project,
                                                                                       modelNumber,
                                                                                       modelElementUsageId.getModule(),
                                                                                       modelElementUsageId.getSubModule(),
                                                                                       saaBKNumber);

        } else if (!isNewStructureActive && structureId.getType().equals(OpsId.TYPE)) {
            OpsId opsId = (OpsId)structureId;
            return iPartsDataEDSModelContentList.loadAllMatchingEntries(project,
                                                                        modelNumber,
                                                                        opsId.getGroup(),
                                                                        opsId.getScope(),
                                                                        saaBKNumber,
                                                                        new String[]{ FIELD_EDS_MODEL_MSAAKEY });
        }
        return null;
    }

    public EtkDataObjectList<? extends EtkDataObject> loadAllStructureEntriesForPosition(EtkProject project,
                                                                                         String modelNo,
                                                                                         HierarchicalIDWithType structureId,
                                                                                         String position) {
        if (isNewStructureActive) {
            return iPartsDataModelElementUsageList.loadStructureEntriesForPOS(project, modelNo,
                                                                              getUpperValueFromStructureId(structureId),
                                                                              getLowerValueFromStructureId(structureId),
                                                                              position, true);
        } else {
            return iPartsDataEDSModelContentList.loadAllMatchingEntriesForPOS(project, modelNo,
                                                                              getUpperValueFromStructureId(structureId),
                                                                              getLowerValueFromStructureId(structureId),
                                                                              position,
                                                                              new String[]{ iPartsConst.FIELD_EDS_REVFROM },
                                                                              true);
        }
    }

    public EtkDataObjectList<? extends EtkDataObject> loadAllStructureEntriesForModelAndSaaBk(EtkProject project,
                                                                                              String modelNumber,
                                                                                              String saaBkNo) {
        if (isNewStructureActive) {
            return iPartsDataModelElementUsageList.loadAllModelSaaEntries(project,
                                                                          modelNumber,
                                                                          saaBkNo);
        } else {
            return iPartsDataEDSModelContentList.loadAllModelSaaEntries(project,
                                                                        modelNumber,
                                                                        saaBkNo);
        }
    }

    public List<? extends AbstractiPartsNode> searchInEdsStructure(EtkProject project, iPartsModelId modelId,
                                                                   EtkDisplayFields selectFields, List<String> selectValues,
                                                                   EtkDisplayFields whereFields, List<String> andOrWhereValues,
                                                                   boolean andOrSearch, WildCardSettings wildCardSettings) {
        if (isNewStructureActive) {
            ModelElementUsage modelElementUsage = ModelElementUsage.getInstance(project, modelId);
            return modelElementUsage.search(selectFields, selectValues,
                                            whereFields,
                                            andOrWhereValues,
                                            andOrSearch,
                                            wildCardSettings,
                                            project.getDBLanguage(),
                                            project.getDataBaseFallbackLanguages());
        } else {
            Ops ops = Ops.getInstance(project, modelId);
            return ops.search(selectFields, selectValues, whereFields, andOrWhereValues,
                              andOrSearch, wildCardSettings, project.getDBLanguage(),
                              project.getDataBaseFallbackLanguages());
        }
    }

    public HierarchicalIDWithType createStructureIdFromDataObject(EtkDataObject dataObject) {
        String upperStructureValue = dataObject.getFieldValue(getUpperStructureValueField());
        String lowerStructureValue = dataObject.getFieldValue(getLowerStructureValueField());
        return createStructureId(upperStructureValue, lowerStructureValue);
    }

    public HierarchicalIDWithType createStructureIdFromOwnerAssemblyId(iPartsAssemblyId ownerAssemblyId) {
        return isNewStructureActive ? iPartsVirtualNode.getModelElementUsageNodeFromAssemblyId(ownerAssemblyId)
                                    : iPartsVirtualNode.getOPSFromAssemblyId(ownerAssemblyId);
    }

    public HierarchicalIDWithType createStructureId(String upperValue, String lowerValue) {
        return isNewStructureActive ? new ModelElementUsageId(upperValue, lowerValue)
                                    : new OpsId(upperValue, lowerValue);
    }

    public HierarchicalIDWithType createEmptyStructureId() {
        return isNewStructureActive ? new ModelElementUsageId() : new OpsId();
    }

    public String getUpperValueFromStructureId(HierarchicalIDWithType structureId) {
        return getValueFromStructureId(structureId, true);
    }

    public String getLowerValueFromStructureId(HierarchicalIDWithType structureId) {
        return getValueFromStructureId(structureId, false);
    }

    private String getValueFromStructureId(HierarchicalIDWithType structureId, boolean upperValue) {
        if (structureId == null) {
            return "";
        }
        String structureType = structureId.getType();
        if (isNewStructureActive && structureType.equals(ModelElementUsageId.TYPE)) {
            ModelElementUsageId modelElementUsageId = (ModelElementUsageId)structureId;
            return upperValue ? modelElementUsageId.getModule() : modelElementUsageId.getSubModule();
        } else if (!isNewStructureActive && structureType.equals(OpsId.TYPE)) {
            OpsId opsId = (OpsId)structureId;
            return upperValue ? opsId.getGroup() : opsId.getScope();
        }
        return "";
    }

    public static boolean isEdsStructureSaaNode(List<iPartsVirtualNode> virtualNodesPath) {
        return iPartsVirtualNode.isOpsSaaNode(virtualNodesPath) || iPartsVirtualNode.isModelElementUsageSaaNode(virtualNodesPath);
    }

    public static boolean isEdsStructureLowerElementNode(List<iPartsVirtualNode> virtualNodesPath) {
        return iPartsVirtualNode.isOpsScopeNode(virtualNodesPath) || iPartsVirtualNode.isModelElementUsageSubModuleNode(virtualNodesPath);
    }

    public static boolean isEdsStructureNode(List<iPartsVirtualNode> virtualNodesPath) {
        return iPartsVirtualNode.isOpsNode(virtualNodesPath) || iPartsVirtualNode.isModelElementUsageNode(virtualNodesPath);
    }

    public static boolean isEdsModelStructureNode(iPartsNodeType nodeType) {
        return (nodeType == iPartsNodeType.EDS_OPS) || (nodeType == iPartsNodeType.EDS_MODEL_ELEMENT_USAGE);
    }

    public static boolean isEdsStructurePartListType(String partListType) {
        return partListType.equals(iPartsConst.PARTS_LIST_TYPE_OPS_SCOPE) || partListType.equals(iPartsConst.PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_SUB_MODULE);
    }

    public static boolean isSaaBkDescAttributeForLowerStructure(String attributeName) {
        return attributeName.equals(iPartsDataVirtualFieldsDefinition.OPS_SCOPE_SAA_BK_DESC) || attributeName.equals(iPartsDataVirtualFieldsDefinition.MEU_SUB_MODULE_SAA_BK_DESC);
    }

    public static void setConfigurationValue() {
        iPartsEdsStructureHelper.getInstance().checkEdsStructureAdminOption();
    }
}
