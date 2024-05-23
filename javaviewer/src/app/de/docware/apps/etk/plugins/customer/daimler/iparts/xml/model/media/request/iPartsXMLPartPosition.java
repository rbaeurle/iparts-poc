/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractSourceKey;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert das "PartPosition" Element in der Transfer XML
 */
public class iPartsXMLPartPosition extends AbstractXMLObject {

    private String hotspot;
    private String externalId;
    private iPartsXMLPartNumber partNumber;
    private AbstractSourceKey sourceKey;
    private EtkMultiSprache partName; // Teilebenennung AS
    private EtkMultiSprache supplementaryText; // Ergänzungstext (Stücklistenposition)
    private String quantity; // Menge (Stücklistenposition)
    private String structureLevel; // Strukturstufe AS (Stücklistenposition)
    private String picturePositionMarker; // Bildpositionskenner
    private String faultLocation; // Fehlerorte
    private String genericInstallLocation; // Generischer Verbauort
    private int picPosSeqNo; // Sequenznummer für Bildpositionen
    private String assemblySign;  // Zusammenbau-Kennzeichen (M_ASSEMBLYSIGN aus der MAT-Tabelle) zur Teileposition

    public iPartsXMLPartPosition() {

    }

    public iPartsXMLPartPosition(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    public String getHotspot() {
        return hotspot;
    }

    public void setHotspot(String hotspot) {
        this.hotspot = hotspot;
    }

    public String getAssemblySign() {
        return assemblySign;
    }

    public void setAssemblySign(String assemblySign) {
        this.assemblySign = assemblySign;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public iPartsXMLPartNumber getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(iPartsXMLPartNumber partNumber) {
        this.partNumber = partNumber;
    }

    public AbstractSourceKey getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(AbstractSourceKey sourceKey) {
        this.sourceKey = sourceKey;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getStructureLevel() {
        return structureLevel;
    }

    public void setStructureLevel(String structureLevel) {
        this.structureLevel = structureLevel;
    }

    public String getPicturePositionMarker() {
        return picturePositionMarker;
    }

    public void setPicturePositionMarker(String picturePositionMarker) {
        this.picturePositionMarker = picturePositionMarker;
    }

    public int getPicPosSeqNo() {
        return picPosSeqNo;
    }

    public void setPicPosSeqNo(int picPosSeqNo) {
        this.picPosSeqNo = picPosSeqNo;
    }

    public EtkMultiSprache getPartName() {
        return partName;
    }

    public void setPartName(EtkMultiSprache partName) {
        this.partName = partName;
    }

    private void addPartName(String langCode, String text) {
        if (partName == null) {
            partName = new EtkMultiSprache();
        }
        addTextElement(langCode, text, partName, iPartsTransferNodeTypes.PART_NAME);
    }

    public EtkMultiSprache getSupplementaryText() {
        return supplementaryText;
    }

    public void setSupplementaryText(EtkMultiSprache supplementaryText) {
        this.supplementaryText = supplementaryText;
    }

    private void addSupplementaryText(String langCode, String text) {
        if (supplementaryText == null) {
            supplementaryText = new EtkMultiSprache();
        }
        addTextElement(langCode, text, supplementaryText, iPartsTransferNodeTypes.SUPPLEMENTARY_TEXT);
    }

    private void addTextElement(String langCode, String text, EtkMultiSprache multiText, iPartsTransferNodeTypes nodeType) {
        Language lang = Language.getFromCode(langCode);
        if (lang == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while parsing String language code attribute for " + nodeType.getAlias() + ". Invalid value: " + langCode);
            return;
        }
        multiText.setText(lang, text);
    }

    public String getFaultLocation() {
        return faultLocation;
    }

    public void setFaultLocation(String faultLocation) {
        this.faultLocation = faultLocation;
    }

    public String getGenericInstallLocation() {
        return genericInstallLocation;
    }

    public void setGenericInstallLocation(String genericInstallLocation) {
        this.genericInstallLocation = genericInstallLocation;
    }

    /**
     * Baut die Ergänzungstexte zusammen und legt sie in der Klassenvariable ab.
     *
     * @param combinedMultiTextList
     * @param neutralText
     */
    public void setSupplementaryTexts(List<iPartsDataCombText> combinedMultiTextList, String neutralText) {
        if ((combinedMultiTextList != null) && !combinedMultiTextList.isEmpty()) {
            if (supplementaryText == null) {
                supplementaryText = new EtkMultiSprache();
            }
            // Durchlaufe alle Teile der Ergänzungstexte und baue den Ergänzungstext zusammen. Zusätzlich werden
            // die Text-Ids aller Einzelteile gesammelt.
            for (int i = 0; i < combinedMultiTextList.size(); i++) {
                EtkMultiSprache currentToken = combinedMultiTextList.get(i).getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
                boolean isLastTextElement = (i == (combinedMultiTextList.size() - 1));
                for (Language language : iPartsLanguage.getASPLMPrimaryLanguages()) {
                    String currentText = supplementaryText.getText(language.getCode());
                    String additionalTextElement = currentToken.getText(language.getCode());
                    if (StrUtils.isEmpty(additionalTextElement)) {
                        continue;
                    }
                    supplementaryText.setText(language, currentText + " " + additionalTextElement);
                    if (isLastTextElement && StrUtils.isValid(neutralText) && !additionalTextElement.equals(neutralText)) {
                        currentText = supplementaryText.getText(language.getCode());
                        if (StrUtils.isValid(currentText)) {
                            currentText = currentText + "; " + neutralText;
                        } else {
                            currentText = neutralText;
                        }
                        supplementaryText.setText(language, currentText);
                    }
                }
            }
        } else if (StrUtils.isValid(neutralText)) {
            // Falls nur ein sprachneutraler Text am Teil existiert
            if (supplementaryText == null) {
                supplementaryText = new EtkMultiSprache();
            }
            for (Language language : iPartsLanguage.getASPLMPrimaryLanguages()) {
                supplementaryText.setText(language, neutralText);
            }
        }
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode partPositionNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.PART_POSITION.getAlias());
        DwXmlNode partPositionChildNode;
        // Stücklisteneintrag
        if (sourceKey != null) {
            partPositionChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.SOURCE_KEY.getAlias());
            partPositionChildNode.appendChild(sourceKey.getAsDwXMLNode(namespacePrefix));
            partPositionNode.appendChild(partPositionChildNode);
        }
        // Bildpositionskenner
        appendElementWithTextContent(partPositionNode, picturePositionMarker, iPartsTransferNodeTypes.PICTURE_POSITION_MARKER, namespacePrefix, false);
        // Hotspot
        appendElementWithTextContent(partPositionNode, hotspot, iPartsTransferNodeTypes.HOTSPOT, namespacePrefix, false);
        // assemblySign
        appendElementWithTextContent(partPositionNode, assemblySign, iPartsTransferNodeTypes.ASSEMBLY_SIGN, namespacePrefix, false);
        // Teilenummer
        partPositionNode.appendChild(partNumber.getAsDwXMLNode(namespacePrefix));
        // Teilebenennung
        appendTextElements(partPositionNode, namespacePrefix, partName, iPartsTransferNodeTypes.PART_NAME);
        // Ergänzungstexte
        appendTextElements(partPositionNode, namespacePrefix, supplementaryText, iPartsTransferNodeTypes.SUPPLEMENTARY_TEXT);
        // Menge
        appendElementWithTextContent(partPositionNode, quantity, iPartsTransferNodeTypes.QUANTITY, namespacePrefix, false);
        // Strukturstufe
        appendElementWithTextContent(partPositionNode, structureLevel, iPartsTransferNodeTypes.STRUCTURE_LEVEL, namespacePrefix, false);
        // ExternalId (Von uns vergebene ID)
        appendElementWithTextContent(partPositionNode, externalId, iPartsTransferNodeTypes.EXTERNAL_ID, namespacePrefix, true);
        // Fehlerorte
        appendElementWithTextContent(partPositionNode, faultLocation, iPartsTransferNodeTypes.FAULT_LOCATION, namespacePrefix, false);
        // Generischer Verbauort
        appendElementWithTextContent(partPositionNode, genericInstallLocation, iPartsTransferNodeTypes.GENERIC_INSTALL_LOCATION, namespacePrefix, false);
        // Attribut für die Sequenznummer der Bildpposition
        if (picPosSeqNo > 0) {
            partPositionNode.setAttribute(ATTR_PIC_POS_SEQ_NO, String.valueOf(picPosSeqNo));
        }
        return partPositionNode;
    }

    /**
     * Erzeugt mit den übergebenen Objekten einen neuen Kind-Knoten und hängt diesen an das übergebene <code>partPositionNode</code>
     * Objekt.
     *
     * @param partPositionNode
     * @param textValue
     * @param nodeTypes
     * @param namespacePrefix
     */
    private void appendElementWithTextContent(DwXmlNode partPositionNode, String textValue, iPartsTransferNodeTypes nodeTypes,
                                              String namespacePrefix, boolean isEmptyAllowed) {
        if ((textValue != null) && (isEmptyAllowed || !textValue.isEmpty())) {
            DwXmlNode partPositionChildNode = new DwXmlNode(namespacePrefix + nodeTypes.getAlias());
            partPositionChildNode.setTextContent(textValue);
            partPositionNode.appendChild(partPositionChildNode);
        }
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.PART_POSITION)) {
                List<DwXmlNode> childNodes = node.getChildNodes();
                String seqNoPicPos = node.getAttribute(ATTR_PIC_POS_SEQ_NO);
                if (seqNoPicPos != null) {
                    this.picPosSeqNo = StrUtils.strToIntDef(seqNoPicPos, 0);
                }
                for (DwXmlNode childNode : childNodes) {
                    String name = StrUtils.removeFirstCharacterIfCharacterIs(childNode.getName(), iPartsTransferConst.ASPLM_XML_NAMESPACE_PREFIX);
                    iPartsTransferNodeTypes type = iPartsTransferNodeTypes.getFromAlias(name);
                    if (type != null) {
                        switch (type) {
                            case SOURCE_KEY:
                                DwXmlNode sourceKeyChildNode = childNode.getFirstChild();
                                AbstractSourceKey sourceKey = null;
                                if (XMLImportExportHelper.checkTagWithNamespace(sourceKeyChildNode.getName(), iPartsTransferNodeTypes.SOURCE_KEY_DIALOG)) {
                                    sourceKey = new iPartsXMLSourceKeyDialog(sourceKeyChildNode);
                                } else if (XMLImportExportHelper.checkTagWithNamespace(sourceKeyChildNode.getName(), iPartsTransferNodeTypes.SOURCE_KEY_TRUCK)) {
                                    sourceKey = new iPartsXMLSourceKeyTruck(sourceKeyChildNode);
                                }
                                setSourceKey(sourceKey);
                                break;
                            case PICTURE_POSITION_MARKER:
                                setPicturePositionMarker(childNode.getTextContent());
                                break;
                            case HOTSPOT:
                                setHotspot(childNode.getTextContent());
                                break;
                            case ASSEMBLY_SIGN:
                                setAssemblySign(childNode.getTextContent());
                                break;
                            case PART_NUMBER:
                                iPartsXMLPartNumber partNumber = new iPartsXMLPartNumber(childNode);
                                setPartNumber(partNumber);
                                break;
                            case PART_NAME:
                                addPartName(childNode.getAttribute(ATTR_LANGUAGE), childNode.getTextContent());
                                break;
                            case SUPPLEMENTARY_TEXT:
                                addSupplementaryText(childNode.getAttribute(ATTR_LANGUAGE), childNode.getTextContent());
                                break;
                            case QUANTITY:
                                setQuantity(childNode.getTextContent());
                                break;
                            case STRUCTURE_LEVEL:
                                setStructureLevel(childNode.getTextContent());
                                break;
                            case EXTERNAL_ID:
                                setExternalId(childNode.getTextContent());
                                break;
                            case FAULT_LOCATION:
                                setFaultLocation(childNode.getTextContent());
                                break;
                            case GENERIC_INSTALL_LOCATION:
                                setGenericInstallLocation(childNode.getTextContent());
                                break;
                        }
                    }
                }
            }
        }
    }
}