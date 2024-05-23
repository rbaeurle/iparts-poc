/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLContractor;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLPartPosition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLProduct;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.utils.EtkMultiSprache;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstrakte Klasse für MediaOrder-Operationen von iParts nach AS-PLM (CreateMediaOrder, ChangeMediaOrder und UpdateMediaOrder).
 * Enthält alle Basiselemente einer Anfrage
 */
public abstract class AbstractMediaOrderRequest extends AbstractXMLRequestOperation {

    private String name;
    private String description;
    private String remark;
    private iPartsXMLContractor contractor;
    private String realization;
    private String assignedProjects;
    private Date dateDue;
    private EinPasId einPasId;
    private EtkMultiSprache einPASText;
    private KgTuId kgTuId;
    private EtkMultiSprache kgTuText;
    private String workingContext;
    private String company; // Unternehmenszugehörigkeit
    private Set<iPartsXMLProduct> products;
    private Set<iPartsXMLPartPosition> partPositions;

    public AbstractMediaOrderRequest() {
        products = new LinkedHashSet<>();
        partPositions = new LinkedHashSet<>();
        initMediaOrderRequest();
    }

    public AbstractMediaOrderRequest(DwXmlNode node) {
        this();
        loadFromXML(node);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedProjects() {
        return assignedProjects;
    }

    public void setAssignedProjects(String assignedProjects) {
        this.assignedProjects = assignedProjects;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public iPartsXMLContractor getContractor() {
        return contractor;
    }

    public void setContractor(iPartsXMLContractor contractor) {
        this.contractor = contractor;
    }

    public String getRealization() {
        return realization;
    }

    public void setRealization(String realization) {
        this.realization = realization;
    }

    public Date getDateDue() {
        return dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEinPasData(EinPasId einPasId, EtkMultiSprache einPASText) {
        setEinPasId(einPasId);
        setEinPASText(einPASText);
    }

    public EinPasId getEinPasId() {
        return einPasId;
    }

    private void setEinPasId(EinPasId einPasId) {
        this.einPasId = einPasId;
    }

    private void setEinPASText(EtkMultiSprache einPASText) {
        this.einPASText = einPASText;
    }

    public EtkMultiSprache getEinPASText() {
        return einPASText;
    }

    public void setKgTuData(KgTuId kgTuId, EtkMultiSprache einPASText) {
        setKgTuId(kgTuId);
        setKgTuText(einPASText);
    }

    public KgTuId getKgTuId() {
        return kgTuId;
    }

    private void setKgTuId(KgTuId kgTuId) {
        this.kgTuId = kgTuId;
    }

    private void setKgTuText(EtkMultiSprache kgTuText) {
        this.kgTuText = kgTuText;
    }

    public EtkMultiSprache getKgTuText() {
        return kgTuText;
    }

    public String getWorkingContext() {
        return workingContext;
    }

    public void setWorkingContext(String workingContext) {
        this.workingContext = workingContext;
    }

    public void addProduct(iPartsXMLProduct product) {
        products.add(product);
    }

    public void addPartPosition(iPartsXMLPartPosition partPosition) {
        partPositions.add(partPosition);
    }

    public Set<iPartsXMLProduct> getProducts() {
        return products;
    }

    public Set<iPartsXMLPartPosition> getPartPositions() {
        return partPositions;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaOrderNode = new DwXmlNode(namespacePrefix + getOperationType().getAlias());
        addXmlNodes(mediaOrderNode, namespacePrefix);
        return mediaOrderNode;
    }

    protected void addXmlNodes(DwXmlNode cmoNode, String namespacePrefix) {
        DwXmlNode cmoChildNode;
        // Element "name"
        if (name != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.NAME.getAlias(), name);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "description".
        if (description != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.DESCRIPTION.getAlias(), description);
            cmoNode.appendChild(cmoChildNode);
        }

        // Element "remark"
        if (remark != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.REMARK.getAlias(), remark);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "realization" -> Enum
        if (realization != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.REALIZATION.getAlias(), realization);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "Contractor"
        if (contractor != null) {
            cmoNode.appendChild(contractor.getAsDwXMLNode(namespacePrefix));
        }
        // Element "AssignTo"
        if (assignedProjects != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.ASSIGN_TO.getAlias());
            cmoChildNode.setAttribute(CMO_PROJECTS, assignedProjects);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "datedue" -> gewünschtes Fertigstellungsdatum
        if (dateDue != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_DUE_DATEFORMAT);
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.DATE_DUE.getAlias(), formatter.format(dateDue));
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "company" -> Unternehmenszugehörigkeit (DTAG oder MBAG)
        if (company != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.COMPANY.getAlias(), company);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "kgtu"
        if (kgTuId != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.KGTU.getAlias());
            cmoChildNode.setAttribute(CMO_CONSTRUCTION_GROUP, kgTuId.getKg());
            cmoChildNode.setAttribute(CMO_PART_SCOPE_NUMBER, kgTuId.getTu());
            if (kgTuText != null) {
                appendTextElements(cmoChildNode, namespacePrefix, kgTuText, iPartsTransferNodeTypes.PART_SCOPE_NAME);
            }
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "einpas"
        if (einPasId != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.EINPAS.getAlias());
            cmoChildNode.setAttribute(CMO_MAINNGROUP, einPasId.getHg());
            cmoChildNode.setAttribute(CMO_GROUP, einPasId.getG());
            cmoChildNode.setAttribute(CMO_TECHNICAL_SCOPE, einPasId.getTu());
            if (einPASText != null) {
                appendTextElements(cmoChildNode, namespacePrefix, einPASText, iPartsTransferNodeTypes.NAME);
            }
            cmoNode.appendChild(cmoChildNode);
        }
        if ((kgTuId == null) && (einPasId == null) && (getOperationType() == iPartsTransferNodeTypes.CREATE_MEDIA_ORDER)) {
            Logger.getLogger().throwRuntimeException("EinPAS and KgTu must not be null. It is an required input for \"CreateMediaOrder\"");
        }
        // Element "WorkingContext"
        if (workingContext != null) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.WORKING_CONTEXT.getAlias(), workingContext);
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "product", z.B. "Vehicle, C204"
        if ((products != null) && !products.isEmpty()) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.USAGE.getAlias());
            for (iPartsXMLProduct product : products) {
                cmoChildNode.appendChild(product.getAsDwXMLNode(namespacePrefix));
            }
            cmoNode.appendChild(cmoChildNode);
        }
        // Element "partpositionlist" bzw. "partposition" -> Stückliste
        if ((partPositions != null) && !partPositions.isEmpty()) {
            cmoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.POSITION_LIST.getAlias());
            for (iPartsXMLPartPosition partPosition : partPositions) {
                cmoChildNode.appendChild(partPosition.getAsDwXMLNode(namespacePrefix));
            }
            cmoNode.appendChild(cmoChildNode);
        }
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, getOperationType())) {
                //Kindknoten von CreateMediaOrder
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    handleChildNode(node, childNode, nodeType);
                }
            }
        }
    }

    protected void handleChildNode(DwXmlNode parentNode, DwXmlNode childNode, iPartsTransferNodeTypes nodeType) {
        switch (nodeType) {
            case NAME:
                setName(childNode.getTextContent());
                break;
            case DESCRIPTION:
                setDescription(childNode.getTextContent());
                break;
            case REMARK:
                setRemark(childNode.getTextContent());
                break;
            case REALIZATION:
                setRealization(childNode.getTextContent());
                break;
            case CONTRACTOR:
                setContractor(new iPartsXMLContractor(childNode));
                break;
            case ASSIGN_TO:
                assignedProjects = childNode.getAttribute(CMO_PROJECTS);
                break;
            case DATE_DUE:
                SimpleDateFormat formatter = new SimpleDateFormat(iPartsTransferConst.DATE_DUE_DATEFORMAT);
                try {
                    setDateDue(formatter.parse(childNode.getTextContent()));
                } catch (ParseException e) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while parsing String to Date: " + e);
                }
                break;
            case COMPANY:
                setCompany(childNode.getTextContent());
                break;
            case EINPAS:
                EinPasId einPASId = new EinPasId(childNode.getAttribute(iPartsTransferConst.CMO_MAINNGROUP),
                                                 childNode.getAttribute(iPartsTransferConst.CMO_GROUP),
                                                 childNode.getAttribute(iPartsTransferConst.CMO_TECHNICAL_SCOPE));
                setEinPasId(einPASId);
                List<DwXmlNode> einPASChildren = childNode.getChildNodes();
                if (einPASText == null) {
                    einPASText = new EtkMultiSprache();
                }
                for (DwXmlNode einPASChild : einPASChildren) {
                    einPASText.setText(Language.findLanguage(einPASChild.getAttribute(ATTR_LANGUAGE)), einPASChild.getTextContent());
                }
                break;
            case KGTU:
                KgTuId kgTuId = new KgTuId(childNode.getAttribute(iPartsTransferConst.CMO_CONSTRUCTION_GROUP),
                                           childNode.getAttribute(iPartsTransferConst.CMO_PART_SCOPE_NUMBER));
                setKgTuId(kgTuId);
                List<DwXmlNode> kgTuChildren = childNode.getChildNodes();
                if (kgTuText == null) {
                    kgTuText = new EtkMultiSprache();
                }
                for (DwXmlNode kgTuChild : kgTuChildren) {
                    kgTuText.setText(Language.findLanguage(kgTuChild.getAttribute(ATTR_LANGUAGE)), kgTuChild.getTextContent());
                }
                break;
            case WORKING_CONTEXT:
                setWorkingContext(childNode.getTextContent());
                break;
            case USAGE:
                fillProducts(childNode);
                break;
            case POSITION_LIST:
                fillPartPosition(childNode);
                break;
            default:
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong child for " + getOperationType().getAlias() + " object! Nodetype: " + nodeType);
        }
    }

    private void fillProducts(DwXmlNode node) {
        List<DwXmlNode> childNodes = node.getChildNodes();
        for (DwXmlNode childNode : childNodes) {
            if (XMLImportExportHelper.checkTagWithNamespace(childNode.getName(), iPartsTransferNodeTypes.PRODUCT)) {
                addProduct(new iPartsXMLProduct(childNode));
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid element. Usage node must only contain product nodes. Received node: " + childNode.getName());
            }
        }
    }

    private void fillPartPosition(DwXmlNode node) {
        List<DwXmlNode> childNodes = node.getChildNodes();
        for (DwXmlNode childNode : childNodes) {
            addPartPosition(new iPartsXMLPartPosition(childNode));
        }
    }

    protected abstract void initMediaOrderRequest();

}
