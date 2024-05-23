/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;

/**
 * Repräsentiert das "Success" Element in der Transfer XML
 */
public class iPartsXMLSuccess extends AbstractXMLObject {

    private boolean errorFree;
    private int errorCode;
    private String targetId;
    private List<iPartsXMLErrorText> errors = new DwList<iPartsXMLErrorText>();
    private List<iPartsXMLWarning> warnings = new DwList<iPartsXMLWarning>();

    public iPartsXMLSuccess(boolean errorFree) {
        this.errorFree = errorFree;
        this.errorCode = iPartsTransferConst.ATTR_SUC_DEFAULT_ERRORCODE;
    }

    public iPartsXMLSuccess(DwXmlNode node) {
        this(Boolean.parseBoolean(node.getAttribute(ATTR_SUC_OK)));
        loadFromXML(node);
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        if (isErrorFree()) {
            Logger.getLogger().throwRuntimeException("Errorcode can not be set because success is error-free!");
        }
        this.errorCode = errorCode;
    }

    public List<iPartsXMLWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<iPartsXMLWarning> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(iPartsXMLWarning warning) {
        if (warnings == null) {
            warnings = new DwList<iPartsXMLWarning>();
        }
        warnings.add(warning);
    }

    public List<iPartsXMLErrorText> getErrors() {
        return errors;
    }

    public void setErrors(List<iPartsXMLErrorText> errors) {
        this.errors = errors;
    }

    public void addError(iPartsXMLErrorText error) {
        if (errors == null) {
            errors = new DwList<iPartsXMLErrorText>();
        }
        errors.add(error);
    }

    public boolean isErrorFree() {
        return errorFree;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode successNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.SUCCESS.getAlias());
        successNode.setAttribute(ATTR_SUC_OK, String.valueOf(isErrorFree()));
        if ((targetId != null) && !targetId.isEmpty()) {
            successNode.setAttribute(ATTR_SUC_TARGET_ID, targetId);
        }
        if (getErrorCode() != ATTR_SUC_DEFAULT_ERRORCODE) {
            successNode.setAttribute(ATTR_SUC_ERRORCODE, String.valueOf(getErrorCode()));
        }
        if ((errors != null) && !errors.isEmpty()) {
            for (iPartsXMLErrorText error : errors) {
                successNode.appendChild(error.getAsDwXMLNode(namespacePrefix));
            }

        }
        if ((warnings != null) && !warnings.isEmpty()) {
            for (iPartsXMLWarning warning : warnings) {
                successNode.appendChild(warning.getAsDwXMLNode(namespacePrefix));
            }
        }
        return successNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.SUCCESS)) {
                String errorCodeString = node.getAttribute(ATTR_SUC_ERRORCODE);
                targetId = node.getAttribute(ATTR_SUC_TARGET_ID);
                if (errorCodeString != null && !errorCodeString.isEmpty()) {
                    int errorCode = Integer.parseInt(errorCodeString);
                    setErrorCode(errorCode);
                }
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    switch (nodeType) {
                        case ERRORTEXT:
                            if (isErrorFree()) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Success can not have ErrorText children while beeing errorfree. ChildType: " + nodeType + "; ErrorFree: " + isErrorFree());
                            }
                            iPartsXMLErrorText errorText = new iPartsXMLErrorText(childNode);
                            addError(errorText);
                            break;
                        case WARNING:
                            iPartsXMLWarning warning = new iPartsXMLWarning(childNode);
                            addWarning(warning);
                            break;
                        default:
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong child for Success object! Nodetype: " + nodeType);
                    }
                }

            }
        }
    }

    public String getErrorText() {
        if (!isErrorFree()) {
            List<iPartsXMLErrorText> errorTexts = getErrors();
            if ((errorTexts != null) && !errorTexts.isEmpty()) {
                HashMap<String, String> errors = new HashMap<String, String>();
                for (iPartsXMLErrorText error : errorTexts) {
                    String errorLanguage = error.getLanguage().toUpperCase();
                    if (!errors.containsKey(errorLanguage)) {
                        errors.put(errorLanguage, error.getText());
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Error textID: " + error.getTextID()
                                                                                   + ", Error language: " + error.getLanguage()
                                                                                   + ", Error text: " + error.getText());
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Errortext with the same language already exists. Error textID: " + error.getTextID()
                                                                                   + ", Error language: " + error.getLanguage()
                                                                                   + ", Error text: " + error.getText());
                    }
                }
                return XMLImportExportHelper.createCodeFromMap(errors);
            }
        }
        return "";
    }
}