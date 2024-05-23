/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.Utils;
import de.docware.util.misc.id.Id;
import de.docware.util.misc.id.IdWithType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Ein virtueller Baugruppen-Knoten im iParts Plug-in.
 */
public class iPartsVirtualNode {

    private iPartsNodeType type;
    private IdWithType id;

    public static final String VIRTUAL_INDICATOR = Id.VIRTUAL_ID_PREFIX + "ip@@";
    public static final String VIRTUAL_TYPE = "type";
    public static final String NO = "no";
    public static final String HG = "hg"; // EinPAS 1. Stufe
    public static final String G = "g";   // EinPAS 2. Stufe
    public static final String TU = "tu"; // EinPAS 3. Stufe, KG/TU 2. Stufe
    public static final String KG = "kg"; // KG/TU 1. Stufe
    public static final String SA = "sa"; // KG/TU 2. Stufe für SA (-> KG/SA)
    public static final String GROUP = "group";
    public static final String SCOPE = "scope";
    public static final String MEU_MODULE = "meuM";
    public static final String MEU_SUB_MODULE = "meuSM";
    public static final String LIST_NUMBER = "listnumber";
    public static final String CON_GROUP = "congroup";
    public static final String SERIES = "series";
    public static final String HM = "hm";
    public static final String M = "m";
    public static final String SM = "sm";
    public static final String STRUCTURE = "structure";

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * SM-Knoten von DIALOG HM/M/SM handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isHmMSmNode(List<iPartsVirtualNode> virtualNodesPath) {
        // schneller Check für DIALOG HM/M/SM
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(0).getType() == iPartsNodeType.DIALOG_HMMSM);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * Produkt-Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isProductNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 1) && virtualNodesPath.get(0).getId().getType().equals(iPartsProductId.TYPE);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * Produkt-KGTU-Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isProductKgTuNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && virtualNodesPath.get(0).getType().isProductKgTuType()
               && (virtualNodesPath.get(1).getType() == iPartsNodeType.KGTU);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * KGSA Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isKgSaNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && virtualNodesPath.get(0).getType().isProductKgTuType()
               && (virtualNodesPath.get(1).getType() == iPartsNodeType.KGSA);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * Struktur-Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isStructureNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 1) && virtualNodesPath.get(0).getId().getType().equals(iPartsStructureId.TYPE);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * Baureihen-Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isSeriesNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 1) && virtualNodesPath.get(0).getId().getType().equals(iPartsSeriesId.TYPE);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * Baumuster-Knoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isModelNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 1) && virtualNodesPath.get(0).getId().getType().equals(iPartsModelId.TYPE);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * EDS Konstruktions-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isEdsConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (isStructureNode(virtualNodesPath)) {
            iPartsStructureId structureId = (iPartsStructureId)virtualNodesPath.get(0).getId();
            return structureId.getStructureName().equals(iPartsConst.STRUCT_EDS_LKW_NAME) || structureId.getStructureName().equals(iPartsConst.STRUCT_EDS_AGGREGATE_NAME);
        }
        return false;
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * MBS Konstruktions-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isMBSConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isStructureNode(virtualNodesPath) && (isMBSAggConstNode(virtualNodesPath) || isMBSVehicleConstNode(virtualNodesPath));
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * CTT Konstruktions-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isCTTConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isStructureNode(virtualNodesPath) && (isCTTVehicleConstNode(virtualNodesPath) || isCTTAggConstNode(virtualNodesPath));
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * MBS Aggregate-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isMBSAggConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (isStructureNode(virtualNodesPath)) {
            iPartsStructureId structureId = (iPartsStructureId)virtualNodesPath.get(0).getId();
            return structureId.getStructureName().equals(iPartsConst.STRUCT_MBS_AGGREGATE_NAME);
        }
        return false;
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * MBS Fahrzeug-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isMBSVehicleConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (isStructureNode(virtualNodesPath)) {
            iPartsStructureId structureId = (iPartsStructureId)virtualNodesPath.get(0).getId();
            return structureId.getStructureName().equals(iPartsConst.STRUCT_MBS_LKW_NAME);
        }
        return false;
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * CTT Fahrzeug-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isCTTVehicleConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (isStructureNode(virtualNodesPath)) {
            iPartsStructureId structureId = (iPartsStructureId)virtualNodesPath.get(0).getId();
            return structureId.getStructureName().equals(iPartsConst.STRUCT_CTT_LKW_NAME);
        }
        return false;
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * CTT Aggregate-Subknoten handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isCTTAggConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (isStructureNode(virtualNodesPath)) {
            iPartsStructureId structureId = (iPartsStructureId)virtualNodesPath.get(0).getId();
            return structureId.getStructureName().equals(iPartsConst.STRUCT_CTT_AGGREGATE_NAME);
        }
        return false;
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s beim letzten Knoten um einen
     * SAA-Knoten von OPS SAA handelt.
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isOpsSaaNode(List<iPartsVirtualNode> virtualNodesPath) {
        // schneller Check für EDS OPS SAA
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 3) && (virtualNodesPath.get(0).getType() == iPartsNodeType.EDS_OPS);
    }

    public static boolean isModelElementUsageSaaNode(List<iPartsVirtualNode> virtualNodesPath) {
        // schneller Check für EDS ModelElementUsage SAA
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 3) && (virtualNodesPath.get(0).getType() == iPartsNodeType.EDS_MODEL_ELEMENT_USAGE);
    }

    public static boolean isOpsNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.OPS);
    }

    public static boolean isOpsScopeNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.OPS) &&
               !virtualNodesPath.get(1).getId().getValue(2).isEmpty();
    }

    public static boolean isModelElementUsageNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.MODEL_ELEMENT_USAGE);
    }

    public static boolean isModelElementUsageSubModuleNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.MODEL_ELEMENT_USAGE) &&
               !virtualNodesPath.get(1).getId().getValue(2).isEmpty();
    }

    public static boolean isMBSNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.MBS);
    }

    public static boolean isNodeWithinEdsBcsConstStructure(List<iPartsVirtualNode> virtualNodesPath) {
        if (!Utils.isValid(virtualNodesPath)) {
            return false;
        }
        iPartsNodeType nodeType = virtualNodesPath.get(0).getType();
        boolean mainCheck = (nodeType == iPartsNodeType.EDS_OPS) || (nodeType == iPartsNodeType.EDS_EINPAS) || (nodeType == iPartsNodeType.EDS_MODEL_ELEMENT_USAGE);
        return (mainCheck || isEdsConstNode(virtualNodesPath) || iPartsEdsStructureHelper.isEdsStructureNode(virtualNodesPath));
    }

    public static boolean isNodeWithinMBSConstStructure(List<iPartsVirtualNode> virtualNodesPath) {
        if (!Utils.isValid(virtualNodesPath)) {
            return false;
        }
        boolean mainCheck = (virtualNodesPath.get(0).getType() == iPartsNodeType.MBS_STRUCTURE);
        return (mainCheck || isMBSConstNode(virtualNodesPath) || isMBSNode(virtualNodesPath));
    }

    public static boolean isCTTNode(List<iPartsVirtualNode> virtualNodesPath) {
        return (virtualNodesPath != null) && (virtualNodesPath.size() == 2) && (virtualNodesPath.get(1).getType() == iPartsNodeType.CTT);
    }

    public static boolean isNodeWithinCTTConstStructure(List<iPartsVirtualNode> virtualNodesPath) {
        if (!Utils.isValid(virtualNodesPath)) {
            return false;
        }
        boolean mainCheck = (virtualNodesPath.get(0).getType() == iPartsNodeType.CTT_MODEL);
        return (mainCheck || isCTTConstNode(virtualNodesPath) || isCTTNode(virtualNodesPath));
    }

    public static boolean isCTTSaaConstNode(List<iPartsVirtualNode> virtualNodesPath) {
        if (!Utils.isValid(virtualNodesPath) || (virtualNodesPath.size() < 2)) {
            return false;
        }
        return (virtualNodesPath.get(0).getType() == iPartsNodeType.CTT_MODEL) && (virtualNodesPath.get(1).getType() == iPartsNodeType.EDS_SAA);
    }

    /**
     * Schnelle Überprüfung, ob es sich beim übergebenen Pfad von {@link iPartsVirtualNode}s um eine für die Konstruktion
     * relevante Stückliste handelt (der erste virtuelle Knoten also ein Konstruktions-Rootknoten ist).
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isConstructionRelevant(List<iPartsVirtualNode> virtualNodesPath) {
        if ((virtualNodesPath == null) || virtualNodesPath.isEmpty()) {
            return false;
        }

        // Schneller Check für einen Konstruktions-Rootknoten als ersten virtuellen Knoten
        return virtualNodesPath.get(0).getType().isConstructionRootNode();
    }

    public static boolean isEinPASConstructionNode(List<iPartsVirtualNode> path) {
        if ((path != null) && (path.size() >= 1)) {
            iPartsNodeType type = path.get(0).getType();
            if ((type == iPartsNodeType.DIALOG_EINPAS) || (type == iPartsNodeType.EDS_EINPAS)) {
                return true;
            }
        }
        return false;
    }


    public iPartsVirtualNode(iPartsNodeType type, IdWithType id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Erstellen eines Nodes aus den virtuellen Parametern
     *
     * @param nodeType
     * @param parameter
     */
    public iPartsVirtualNode(iPartsNodeType nodeType, Map<String, String> parameter) {
        this.type = nodeType;
        switch (type) {
            case ROOT:
                id = new iPartsAssemblyId(EtkConfigConst.ROOTKNOTEN, EtkConfigConst.ROOTKNOTENVER);
                break;
            case STRUCTURE:
                id = new iPartsStructureId(getSafeParam(parameter, STRUCTURE));
                break;
            case PRODUCT_EINPAS:
            case PRODUCT_EINPAS_AGGS:
            case PRODUCT_EINPAS_COMMON:
            case PRODUCT_KGTU:
            case PRODUCT_KGTU_AGGS:
            case PRODUCT_KGTU_COMMON:
                id = new iPartsProductId(getSafeParam(parameter, NO));
                break;
            case DIALOG_EINPAS:
            case DIALOG_HMMSM:
                id = new iPartsSeriesId(getSafeParam(parameter, NO));
                break;
            case MBS_STRUCTURE:
            case EDS_OPS:
            case EDS_EINPAS:
            case EDS_MODEL_ELEMENT_USAGE:
            case CTT_MODEL:
                id = new iPartsModelId(getSafeParam(parameter, NO));
                break;
            case EDS_SAA:
                id = new EdsSaaId(getSafeParam(parameter, NO));
                break;
            case EINPAS:
                id = new EinPasId(getSafeParam(parameter, HG), getSafeParam(parameter, G), getSafeParam(parameter, TU));
                break;
            case KGTU:
                id = new KgTuId(getSafeParam(parameter, KG), getSafeParam(parameter, TU));
                break;
            case KGSA:
                id = new KgSaId(getSafeParam(parameter, KG), getSafeParam(parameter, SA));
                break;
            case OPS:
                id = new OpsId(getSafeParam(parameter, GROUP), getSafeParam(parameter, SCOPE));
                break;
            case MODEL_ELEMENT_USAGE:
                id = new ModelElementUsageId(getSafeParam(parameter, MEU_MODULE), getSafeParam(parameter, MEU_SUB_MODULE));
                break;
            case MBS:
                id = new MBSStructureId(getSafeParam(parameter, LIST_NUMBER), getSafeParam(parameter, CON_GROUP));
                break;
            case HMMSM:
                id = new HmMSmId(getSafeParam(parameter, SERIES), getSafeParam(parameter, HM), getSafeParam(parameter, M), getSafeParam(parameter, SM));
                break;
            default:
                Logger.getLogger().throwRuntimeException("Unknown type " + nodeType.toString() + " to parse.");
        }
    }

    /**
     * Liefert zurück, ob es sich um den MBS Konstruktionsknoten handelt
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isMBSConstStructNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isSpecificStructNode(virtualNodesPath, iPartsConst.STRUCT_MBS);
    }

    /**
     * Liefert zurück, ob es sich um den EDS/BCS Konstruktionsknoten handelt
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isEDSConstStructNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isSpecificStructNode(virtualNodesPath, iPartsConst.STRUCT_EDS);
    }

    /**
     * Liefert zurück, ob es sich um den CTT Konstruktionsknoten handelt
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isCTTConstStructNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isSpecificStructNode(virtualNodesPath, iPartsConst.STRUCT_CTT);
    }

    /**
     * Liefert zurück, ob es sich um den DIALOG Konstruktionsknoten handelt
     *
     * @param virtualNodesPath
     * @return
     */
    public static boolean isDIALOGConstStructNode(List<iPartsVirtualNode> virtualNodesPath) {
        return isSpecificStructNode(virtualNodesPath, iPartsConst.STRUCT_DIALOG);
    }

    private static boolean isSpecificStructNode(List<iPartsVirtualNode> virtualNodesPath, String structType) {
        return isStructureNode(virtualNodesPath) && ((iPartsStructureId)virtualNodesPath.get(0).getId()).getStructureName().equals(structType);
    }

    private Map<String, String> getVirtualParameters() {
        // Linked ist wichtig, damit die Parameter nicht unschön durchwürfelt werden
        Map<String, String> result = new LinkedHashMap<String, String>();

        result.put(VIRTUAL_TYPE, getType().getAlias());
        switch (getType()) {
            case ROOT:
                break;
            case STRUCTURE:
                result.put(STRUCTURE, ((iPartsStructureId)id).getStructureName());
                break;
            case PRODUCT_EINPAS:
            case PRODUCT_EINPAS_AGGS:
            case PRODUCT_EINPAS_COMMON:
            case PRODUCT_KGTU:
            case PRODUCT_KGTU_AGGS:
            case PRODUCT_KGTU_COMMON:
                result.put(NO, ((iPartsProductId)id).getProductNumber());
                break;
            case DIALOG_EINPAS:
            case DIALOG_HMMSM:
                result.put(NO, ((iPartsSeriesId)id).getSeriesNumber());
                break;
            case MBS_STRUCTURE:
            case EDS_EINPAS:
            case EDS_OPS:
            case EDS_MODEL_ELEMENT_USAGE:
            case CTT_MODEL:
                result.put(NO, ((iPartsModelId)id).getModelNumber());
                break;
            case EDS_SAA:
                result.put(NO, ((EdsSaaId)id).getSaaNumber());
                break;
            case EINPAS:
                EinPasId actId = (EinPasId)id;
                result.put(HG, actId.getHg());
                if (!actId.getG().isEmpty()) {
                    result.put(G, actId.getG());
                }
                if (!actId.getTu().isEmpty()) {
                    result.put(TU, actId.getTu());
                }
                break;
            case KGTU:
                KgTuId kgId = (KgTuId)id;
                result.put(KG, kgId.getKg());
                if (!kgId.getTu().isEmpty()) {
                    result.put(TU, kgId.getTu());
                }
                break;
            case KGSA:
                KgSaId kgSaId = (KgSaId)id;
                result.put(KG, kgSaId.getKg());
                result.put(SA, kgSaId.getSa());
                break;
            case OPS:
                OpsId opsId = (OpsId)id;
                result.put(GROUP, opsId.getGroup());
                if (!opsId.getScope().isEmpty()) {
                    result.put(SCOPE, opsId.getScope());
                }
                break;
            case MODEL_ELEMENT_USAGE:
                ModelElementUsageId modelElementUsageId = (ModelElementUsageId)id;
                result.put(MEU_MODULE, modelElementUsageId.getModule());
                if (!modelElementUsageId.getSubModule().isEmpty()) {
                    result.put(MEU_SUB_MODULE, modelElementUsageId.getSubModule());
                }
                break;
            case MBS:
                MBSStructureId mbsStructureId = (MBSStructureId)id;
                result.put(LIST_NUMBER, mbsStructureId.getListNumber());
                if (!mbsStructureId.getConGroup().isEmpty()) {
                    result.put(CON_GROUP, mbsStructureId.getConGroup());
                }
                break;
            case HMMSM:
                HmMSmId hmId = (HmMSmId)id;
                result.put(SERIES, hmId.getSeries());
                result.put(HM, hmId.getHm());
                if (!hmId.getM().isEmpty()) {
                    result.put(M, hmId.getM());
                }
                if (!hmId.getSm().isEmpty()) {
                    result.put(SM, hmId.getSm());
                }
                break;
            default:
                Logger.getLogger().throwRuntimeException("Unknown type " + type.toString() + " to get parameters.");
        }

        return result;
    }

    private String getSafeParam(Map<String, String> parameter, String key) {
        String result = parameter.get(key);
        if (result != null) {
            return result;
        }
        return "";
    }

    public iPartsNodeType getType() {
        return type;
    }

    public IdWithType getId() {
        return id;
    }

    /**
     * Handelt es sich um einen Knoten, der Retail-Stücklisten als Kind-Knoten haben kann?
     *
     * @return
     */
    public boolean isParentNodeForRetailPartLists() {
        switch (type) {
            case KGTU:
                return ((KgTuId)id).isTuNode(); // KG/TU ist gesetzt
            case KGSA:
                return !id.isEmpty(); // KG ist gesetzt
            case EINPAS:
                return ((EinPasId)id).isTuNode(); // HG/G/TU ist gesetzt
        }

        return false;
    }

    /**
     * Liefert den k_vari-Anteil der Assembly-ID über die virtuellen Knoten
     *
     * @param nodes
     * @return z.B. @@ip@@type=PRODUCT_KGTU&no=C05?type=KGTU&kg=03&tu=015
     */
    public static String getVirtualIdString(iPartsVirtualNode... nodes) {
        try {
            StringBuilder result = new StringBuilder();
            result.append(VIRTUAL_INDICATOR);

            boolean isFirstNode = true;

            for (iPartsVirtualNode node : nodes) {
                Map<String, String> nodeParams = node.getVirtualParameters();

                if (!isFirstNode) {
                    result.append("?");
                } else {
                    isFirstNode = false;
                }

                boolean isFirstKey = true;
                for (Map.Entry<String, String> nodeEntry : nodeParams.entrySet()) {
                    if (!isFirstKey) {
                        result.append("&");
                    } else {
                        isFirstKey = false;
                    }
                    result.append(nodeEntry.getKey());
                    result.append("=");

                    result.append(URLEncoder.encode(nodeEntry.getValue(), "UTF-8"));
                }
            }

            return result.toString();
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger().throwRuntimeException(e);
            return null;
        }

    }

    /**
     * Liefert den k_vari-Anteil der Assembly-ID über das Produkt
     *
     * @param product
     * @param isProductStructureWithAggregates
     * @param project
     * @return z.B. "@@ip@@type=PRODUCT_KGTU&no=C05"
     */
    public static String getVirtualIdString(iPartsProduct product, boolean isProductStructureWithAggregates, EtkProject project) {
        iPartsNodeType nodeType = null;
        if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
            nodeType = iPartsNodeType.getProductKgTuType(product.isCommonProduct(project), isProductStructureWithAggregates);
        } else if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
            nodeType = iPartsNodeType.getProductEinPASType(product.isCommonProduct(project), isProductStructureWithAggregates);
        } else {
            Logger.getLogger().throwRuntimeException("productStructuringType=" + product.getProductStructuringType());
        }
        iPartsVirtualNode virtualNode = new iPartsVirtualNode(nodeType, product.getAsId());
        return iPartsVirtualNode.getVirtualIdString(virtualNode);
    }

    /**
     * Liefert den k_vari-Anteil der Assembly-ID über den angegebenen HM/M/SM-Knoten
     *
     * @param hmMSmId
     * @return
     */
    public static String getVirtualIdString(HmMSmId hmMSmId) {
        return iPartsVirtualNode.getVirtualIdString(new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM,
                                                                          new iPartsSeriesId(hmMSmId.getSeries())),
                                                    new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmId));
    }

    public static boolean isVirtualId(AssemblyId assemblyId) {
        if (assemblyId.isRootNode()) {
            // Root ist immer virtuell, da muss ich meine Sachen ja reinhängen
            return true;
        }

        return assemblyId.getKVari().startsWith(VIRTUAL_INDICATOR);
    }

    public static boolean isVirtualId(PartId partId) {
        return partId.getMatNr().startsWith(VIRTUAL_INDICATOR);
    }

    public static List<iPartsVirtualNode> parseVirtualIds(AssemblyId assemblyId) {
        return parseVirtualIds(assemblyId.getKVari());
    }

    public static List<iPartsVirtualNode> parseVirtualIds(String virtualIdString) {
        List<iPartsVirtualNode> result = new ArrayList<>();
        try {
            if (virtualIdString.equals(EtkConfigConst.ROOTKNOTEN)) {
                // Für den Rootknoten eine Sonderbehandlung, da hier kein Kenner dabei ist
                result.add(new iPartsVirtualNode(iPartsNodeType.ROOT, (Map<String, String>)null));
                return result;
            }

            if (!virtualIdString.startsWith(VIRTUAL_INDICATOR)) {
                return result;
            }
            virtualIdString = virtualIdString.substring(VIRTUAL_INDICATOR.length());

            // Jeder Knoten kann mehrere Parameter haben, bei Parameter TYPE mit vorherigem ? beginnt ein neuer Block,
            // deshalb eine Liste auf Maps
            // Bsp.: type=SERIES_EINPASKGTU&no=C204?type=EINPAS&hg=15&g=15&tu=30

            String[] paramSetStrings = virtualIdString.split("\\?");

            for (String paramSetString : paramSetStrings) {
                Map<String, String> parameterList = new HashMap<>();

                String[] params = paramSetString.split("&");

                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        parameterList.put(key, value);
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Virtual ID parameter \"" + param
                                                                                  + "\" of virtual ID \"" + virtualIdString
                                                                                  + "\" is invalid because it doesn't have a key value pair:"
                                                                                  + "\n" + Utils.getStackTrace());
                    }
                }

                // Jetzt dieses neue Parameterset in den Result übernehmen, aber nur, wenn ein type angegeben ist,
                // das ist Pflicht in einem Parameterset
                iPartsNodeType nodeType = iPartsNodeType.getFromAlias(parameterList.get(VIRTUAL_TYPE));
                if (nodeType != null) {
                    iPartsVirtualNode newNode = new iPartsVirtualNode(nodeType, parameterList);
                    result.add(newNode);
                }
            }

            return result;
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger().throwRuntimeException(e);
            return result; // egal, weil durch RuntimeException sowieso unerreichbar -> nur für die Code-Analyse
        }
    }

    /**
     * Liefert für die angegebene {@link AssemblyId} das aktuelle Produkt zurück falls dieses bestimmt werden kann.
     *
     * @param assemblyId
     * @return {@code null} falls die angegebene {@link AssemblyId} kein Produkt enthält.
     */
    public static String getProductNumberFromAssemblyId(AssemblyId assemblyId) {
        iPartsVirtualNode virtualRootNode = getVirtualRootNodeFromAssemblyId(assemblyId);
        if ((virtualRootNode != null) && (virtualRootNode.getType().isProductEinPASType()
                                          || virtualRootNode.getType().isProductKgTuType())) {
            return ((iPartsProductId)virtualRootNode.getId()).getProductNumber();
        }

        return null;
    }

    /**
     * Liefert die KG/TU-ID für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static KgTuId getKgTuFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isProductKgTuNode(virtualNodes)) {
                return (KgTuId)virtualNodes.get(1).getId();
            }
        }
        return null;
    }

    /**
     * Liefert für die angegebene {@link AssemblyId} die aktuelle Baureihe zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return {@code null} falls die angegebene {@link AssemblyId} keine Baureihe enthält.
     */
    public static String getSeriesNumberFromAssemblyId(AssemblyId assemblyId) {
        iPartsVirtualNode virtualRootNode = getVirtualRootNodeFromAssemblyId(assemblyId);
        if ((virtualRootNode != null) && ((virtualRootNode.getType() == iPartsNodeType.DIALOG_EINPAS)
                                          || (virtualRootNode.getType() == iPartsNodeType.DIALOG_HMMSM))) {
            return ((iPartsSeriesId)virtualRootNode.getId()).getSeriesNumber();
        }

        return null;
    }

    /**
     * Liefert für die angegebene {@link AssemblyId} das aktuelle Baumuster zurück falls dieses bestimmt werden kann.
     *
     * @param assemblyId
     * @return {@code null} falls die angegebene {@link AssemblyId} kein Baumuster enthält.
     */
    public static String getModelNumberFromAssemblyId(AssemblyId assemblyId) {
        iPartsVirtualNode virtualRootNode = getVirtualRootNodeFromAssemblyId(assemblyId);
        if ((virtualRootNode != null) &&
            ((virtualRootNode.getType() == iPartsNodeType.EDS_EINPAS)
             || (virtualRootNode.getType() == iPartsNodeType.MBS_STRUCTURE)
             || (virtualRootNode.getType() == iPartsNodeType.CTT_MODEL)
             || iPartsEdsStructureHelper.isEdsModelStructureNode(virtualRootNode.getType()))) {
            return ((iPartsModelId)virtualRootNode.getId()).getModelNumber();
        }

        return null;
    }

    /**
     * Liefert für die angegebene {@link AssemblyId} die SAA/BK-Nummer zurück falls dieses bestimmt werden kann.
     *
     * @param assemblyId
     * @return {@code null} falls die angegebene {@link AssemblyId} keine SAA/BK-Nummer enthält.
     */
    public static String getSaaBKNumberFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodes)) {
                return ((EdsSaaId)virtualNodes.get(2).getId()).getSaaNumber();
            }
        }
        return null;
    }

    /**
     * Liefert für den {@link iPartsVirtualNode}-Wurzelknoten für die angegebene {@link AssemblyId} zurück falls dieser
     * bestimmt werden kann.
     *
     * @param assemblyId
     * @return {@code null} falls die angegebene {@link AssemblyId} keinen {@link iPartsVirtualNode}-Wurzelknoten enthält.
     */
    public static iPartsVirtualNode getVirtualRootNodeFromAssemblyId(AssemblyId assemblyId) {
        iPartsAssemblyId iPartsAssemblyId;
        if (assemblyId instanceof iPartsAssemblyId) {
            iPartsAssemblyId = (iPartsAssemblyId)assemblyId;
        } else { // Wir müssen eine iPartsAssemblyId aus der AssemblyId machen, um zu überprüfen, ob sie virtuell ist
            iPartsAssemblyId = new iPartsAssemblyId(assemblyId.getKVari(), assemblyId.getKVer());
        }

        if (iPartsAssemblyId.isVirtual()) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(iPartsAssemblyId);
            if (!virtualNodes.isEmpty()) {
                return virtualNodes.get(0);
            }
        }

        return null;
    }

    /**
     * Liefert die Baureihe für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static iPartsSeriesId getSeriesIdForAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isSeriesNode(virtualNodes)) {
                return (iPartsSeriesId)virtualNodes.get(0).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die {@Link HmMSmId} für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static HmMSmId getHmMSmIdForAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isHmMSmNode(virtualNodes)) {
                return (HmMSmId)virtualNodes.get(1).getId();
            }
        }
        return null;
    }

    /**
     * Liefert den HM/M/SM-Knoten für die übergebene {@link AssemblyId} zurück falls dieser bestimmt werden kann.
     *
     * @param assemblyId
     * @param project
     * @return
     */
    public static HmMSmNode getHmMSmNodeForAssemblyId(AssemblyId assemblyId, EtkProject project) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isHmMSmNode(virtualNodes)) {
                iPartsSeriesId seriesId = (iPartsSeriesId)virtualNodes.get(0).getId();
                HmMSm hmMSmStructure = HmMSm.getInstance(project, seriesId);
                HmMSmId hmMSmId = (HmMSmId)virtualNodes.get(1).getId();
                return hmMSmStructure.getNode(hmMSmId);
            }
        }
        return null;
    }

    /**
     * Liefert die OPS-ID (NICHT OPS-SAA!) für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static OpsId getOPSFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isOpsNode(virtualNodes)) {
                return (OpsId)virtualNodes.get(1).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die ModelElementUsage-ID (NICHT ModelElementUsage-SAA!) für die übergebene {@link AssemblyId} zurück falls
     * diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static ModelElementUsageId getModelElementUsageNodeFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isModelElementUsageNode(virtualNodes)) {
                return (ModelElementUsageId)virtualNodes.get(1).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die OPS-SAA-ID für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static EdsSaaId getOpsSAAFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isOpsSaaNode(virtualNodes)) {
                return (EdsSaaId)virtualNodes.get(2).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die ModelElementUsage-SAA-ID für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static EdsSaaId getModelElementUsageSAAFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isModelElementUsageSaaNode(virtualNodes)) {
                return (EdsSaaId)virtualNodes.get(2).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die CTT-SAA-ID für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static EdsSaaId getCTTSaaFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isCTTSaaConstNode(virtualNodes)) {
                return (EdsSaaId)virtualNodes.get(1).getId();
            }
        }
        return null;
    }

    /**
     * Liefert die MBS-SAA-ID für die übergebene {@link AssemblyId} zurück falls diese bestimmt werden kann.
     *
     * @param assemblyId
     * @return
     */
    public static iPartsSaaId getMbsSAAFromAssemblyId(AssemblyId assemblyId) {
        if (iPartsVirtualNode.isVirtualId(assemblyId)) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyId);
            if (iPartsVirtualNode.isMBSNode(virtualNodes)) {
                MBSStructureId mbsStructureId = (MBSStructureId)virtualNodes.get(1).getId();
                if (mbsStructureId.getListNumber().startsWith(iPartsConst.SAA_NUMBER_PREFIX)) {
                    return new iPartsSaaId(mbsStructureId.getListNumber());
                }
            }
        }
        return null;
    }
}