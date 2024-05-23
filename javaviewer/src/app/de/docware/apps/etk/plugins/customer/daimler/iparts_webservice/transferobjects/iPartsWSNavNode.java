/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * NavNode Data Transfer Object für die iParts Webservices
 */
public class iPartsWSNavNode extends WSRequestTransferObject implements iPartsConst {

    public enum TYPE {
        // KG/TU
        cg_group(iPartsVirtualNode.KG, 2, 0, iPartsNodeType.KGTU),
        cg_subgroup(iPartsVirtualNode.TU, 3, 1, iPartsNodeType.KGTU),

        // EINPAS
        maingroup(iPartsVirtualNode.HG, 2, 0, iPartsNodeType.EINPAS),
        group(iPartsVirtualNode.G, 2, 1, iPartsNodeType.EINPAS),
        subgroup(iPartsVirtualNode.TU, 2, 2, iPartsNodeType.EINPAS),

        // Freie SAs
        sa_number(iPartsVirtualNode.SA, 9, 1, iPartsNodeType.KGSA),

        module;

        private String iPartsVirtualNodeId = null;
        private int length = -1;
        private int level = -1; // z.B. 0 muss dann zwingend der erste Knoten in navContext sein
        private iPartsNodeType nodeType;

        TYPE() {
        }

        TYPE(String iPartsVirtualNodeId, int length, int level, iPartsNodeType nodeType) {
            this.iPartsVirtualNodeId = iPartsVirtualNodeId;
            this.length = length;
            this.level = level;
            this.nodeType = nodeType;
        }

        public String getiPartsVirtualNodeId() {
            return iPartsVirtualNodeId;
        }

        public int getLength() {
            return length;
        }

        public int getLevel() {
            return level;
        }

        public iPartsNodeType getNodeType() {
            return nodeType;
        }
    }

    private String type;
    private String id;
    private String label;
    private String labelRef;
    private List<iPartsWSNote> notes;
    private List<iPartsWSFootNote> footNotes;
    private List<iPartsWSImage> thumbNails;
    private boolean partsAvailable;
    private String saCodes;
    private List<iPartsWSSaaNavNodeInfo> saaNavNodeInfos;
    private List<iPartsWSNavNode> modules; // Gibt es nur bei TUs, wenn es mehr als ein Modul im TU gibt
    private String modelId; // Gibt es nur bei Modulen, die direkt in TUs ausgegeben werden, wenn es mehr als ein Modul im TU gibt

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSNavNode() {
    }

    /**
     * Konstruktor mit Mussfeldern für Verwendung in Request
     *
     * @param type
     * @param id
     * @param partsAvailable
     */
    public iPartsWSNavNode(TYPE type, String id, boolean partsAvailable) {
        this.type = type.name();
        this.id = id;
        this.partsAvailable = partsAvailable;
    }

    /**
     * Konstruktor mit Mussfeldern für Verwendung in Response
     *
     * @param type
     * @param id
     * @param partsAvailable
     * @param label
     */
    public iPartsWSNavNode(TYPE type, String id, boolean partsAvailable, String label) {
        this(type, id, partsAvailable);
        this.label = label;
    }

    public iPartsWSNavNode(TYPE type, String id, boolean partsAvailable, String label, String labelRef) {
        this(type, id, partsAvailable, label);
        setLabelRef(labelRef);
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // Enum von type und id müssen gültig sein
        checkAttribValid(path, "id", id);
        checkAttribEnumValid(path, "type", type, TYPE.class);

        TYPE typeAsEnum = getTypeAsEnum();
        if (typeAsEnum.getLength() != -1) { // für -1 liegt noch keine Definition vor, oder es gibt keine
            checkLengthIfAttribValid(path, "id", id, typeAsEnum.length);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ type, id };
    }

    // Getter und Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public TYPE getTypeAsEnum() {
        if (type == null) {
            return null;
        }

        try {
            return TYPE.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @JsonIgnore
    public void setType(TYPE type) {
        this.type = type.name();
    }

    @JsonIgnore
    public void setLabel(EtkProject project, EtkMultiSprache label, boolean withTextId) {
        String text = label.getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
        setLabel(text);
        if (withTextId) {
            String textId = label.getTextId();
            if (StrUtils.isValid(textId)) {
                setLabelRef(textId);
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<iPartsWSNote> getNotes() {
        return notes;
    }

    public void setNotes(List<iPartsWSNote> notes) {
        this.notes = notes;
    }

    public List<iPartsWSFootNote> getFootNotes() {
        return footNotes;
    }

    public void setFootNotes(List<iPartsWSFootNote> footNotes) {
        this.footNotes = footNotes;
    }

    public List<iPartsWSImage> getThumbNails() {
        return thumbNails;
    }

    public void setThumbNails(List<iPartsWSImage> thumbNails) {
        this.thumbNails = thumbNails;
    }

    public boolean isPartsAvailable() {
        return partsAvailable;
    }

    public void setPartsAvailable(boolean partsAvailable) {
        this.partsAvailable = partsAvailable;
    }

    public String getSaCodes() {
        return saCodes;
    }

    public void setSaCodes(String saCodes) {
        this.saCodes = saCodes;
    }

    public List<iPartsWSSaaNavNodeInfo> getSaaNavNodeInfos() {
        return saaNavNodeInfos;
    }

    public void setSaaNavNodeInfos(List<iPartsWSSaaNavNodeInfo> saaNavNodeInfos) {
        this.saaNavNodeInfos = saaNavNodeInfos;
    }

    public String getLabelRef() {
        return labelRef;
    }

    public void setLabelRef(String labelRef) {
        this.labelRef = labelRef;
    }

    public List<iPartsWSNavNode> getModules() {
        return modules;
    }

    public void setModules(List<iPartsWSNavNode> modules) {
        this.modules = modules;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}