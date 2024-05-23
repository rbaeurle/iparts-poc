/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicOrderPartSourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractSourceKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsASPLMGroupId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsASPLMUserId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMUser;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrderPartlistEntriesForm;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import org.apache.commons.codec.binary.Base64;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER.
 */
public class iPartsDataPicOrder extends EtkDataObject implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DA_PO_ORDER_GUID };
    private static final String FAKE_ORIGINAL_PIC_ORDER = "FAKE_ORIGINAL_PIC_ORDER";
    private static final String RETAIL_SA_REMARK_PREFIX = "Freie SA:";

    public static final long MAX_ATTACHMENT_FILE_SIZE_IN_BYTES = 20971520;
    public static final long MAX_ATTACHMENT_FILE_SIZE_IN_MB = 20;
    public static final long MAX_AMOUNT_ATTACHMENT_FILES = 20;

    public enum ASPLM_ERROR_IDENTIFIER {
        NO_ERROR(""),
        ERROR(""),
        WARNING("<warning>"),
        COMMENT("<comment>"),
        DEFAULT("-1");

        private final String prefix;

        ASPLM_ERROR_IDENTIFIER(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public static ASPLM_ERROR_IDENTIFIER getIdentifierFromDBValue(String dbValue) {
            if (!dbValue.isEmpty()) {
                if (dbValue.startsWith(WARNING.getPrefix())) {
                    return WARNING;
                } else if (dbValue.startsWith(COMMENT.getPrefix())) {
                    return COMMENT;
                } else if (dbValue.equals(DEFAULT.getPrefix())) {
                    return NO_ERROR;
                }
                return ERROR;
            }
            return NO_ERROR;
        }
    }

    public static final String CHILDREN_NAME_MODULES = "iPartsDataPicOrder.modules";
    public static final String CHILDREN_NAME_USAGES = "iPartsDataPicOrder.usages";
    public static final String CHILDREN_NAME_PICTURES = "iPartsDataPicOrder.pictures";
    public static final String CHILDREN_NAME_PARTS = "iPartsDataPicOrder.parts";
    public static final String CHILDREN_NAME_ATTACHMENTS = "iPartsDataPicOrder.attachments";
    public static final String CHILDREN_NAME_ATTACHMENTS_REFERENCES = "iPartsDataPicOrder.attachmentsReferences";

    protected iPartsDataPicOrderModulesList modulesList;
    protected iPartsDataPicOrderUsageList usagesList;
    protected iPartsDataPicOrderPicturesList picturesList;
    protected iPartsDataPicOrderPicturesList picturesListWithPicturesFromPredecessor;
    protected iPartsDataPicOrderPartsList partsList;
    protected iPartsDataPicOrderAttachmentList attachmentsList;
    protected iPartsDataPicOrderAttachmentReferenceList attachmentsReferenceList;

    public iPartsDataPicOrder(EtkProject project, iPartsPicOrderId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_MODULES)) {
            modulesList = (iPartsDataPicOrderModulesList)children;
        } else if (childrenName.equals(CHILDREN_NAME_USAGES)) {
            usagesList = (iPartsDataPicOrderUsageList)children;
        } else if (childrenName.equals(CHILDREN_NAME_PICTURES)) {
            picturesList = (iPartsDataPicOrderPicturesList)children;
        } else if (childrenName.equals(CHILDREN_NAME_PARTS)) {
            partsList = (iPartsDataPicOrderPartsList)children;
        } else if (childrenName.equals(CHILDREN_NAME_ATTACHMENTS)) {
            attachmentsList = (iPartsDataPicOrderAttachmentList)children;
        } else if (childrenName.equals(CHILDREN_NAME_ATTACHMENTS_REFERENCES)) {
            attachmentsReferenceList = (iPartsDataPicOrderAttachmentReferenceList)children;
        }
    }

    @Override
    public iPartsDataPicOrder cloneMe(EtkProject project) {
        iPartsDataPicOrder clone = new iPartsDataPicOrder(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsPicOrderId createId(String... idValues) {
        return new iPartsPicOrderId(idValues[0]);
    }

    @Override
    public iPartsPicOrderId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderId)id;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_MODULES, null);
        setChildren(CHILDREN_NAME_USAGES, null);
        setChildren(CHILDREN_NAME_PICTURES, null);
        setChildren(CHILDREN_NAME_PARTS, null);
        setChildren(CHILDREN_NAME_ATTACHMENTS_REFERENCES, null);
        setChildren(CHILDREN_NAME_ATTACHMENTS, null);
    }

    protected synchronized void loadAttachments() {
        if (attachmentsList != null) {
            return;
        }
        setChildren(CHILDREN_NAME_ATTACHMENTS, iPartsDataPicOrderAttachmentList.loadAttachmentsForPicOrder(getEtkProject(), getAsId()));
    }

    protected synchronized void loadAttachmentReferences() {
        if (attachmentsReferenceList != null) {
            return;
        }
        setChildren(CHILDREN_NAME_ATTACHMENTS_REFERENCES, iPartsDataPicOrderAttachmentReferenceList.loadAttachmentsForPicOrder(getEtkProject(), getAsId()));
    }

    public void clearExternIds() {
        setFieldValue(FIELD_DA_PO_ORDER_ID_EXTERN, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DA_PO_ORDER_REVISION_EXTERN, "", DBActionOrigin.FROM_EDIT);
    }

    public List<iPartsDataPicOrderAttachment> getAttachments() {
        loadAttachmentReferences();
        loadAttachments();
        return Collections.unmodifiableList(attachmentsList.getAsList());
    }

    private iPartsDataPicOrderAttachmentList getAttachmentsBDObjectList() {
        loadAttachments();
        return attachmentsList;
    }

    private iPartsDataPicOrderAttachmentReferenceList getAttachmentReferencesBDObjectList() {
        loadAttachmentReferences();
        return attachmentsReferenceList;
    }

    public synchronized void addAttachment(iPartsDataPicOrderAttachment attachment, DBActionOrigin origin) {
        String picOrderGuid = getAsId().getOrderGuid();
        if (StrUtils.isValid(picOrderGuid)) {
            iPartsPicOrderAttachmentReferenceId attachmentReferenceId = new iPartsPicOrderAttachmentReferenceId(picOrderGuid,
                                                                                                                attachment.getAsId().getAttachmentGuid());
            iPartsDataPicOrderAttachmentReference attachmentReference = new iPartsDataPicOrderAttachmentReference(getEtkProject(),
                                                                                                                  attachmentReferenceId);
            attachmentReference.initAttributesWithEmptyValues(origin);
            getAttachmentReferencesBDObjectList().add(attachmentReference, origin);
            getAttachmentsBDObjectList().add(attachment, origin);
        }
    }

    public void addAttachments(List<iPartsDataPicOrderAttachment> picOrderAttachments, DBActionOrigin origin) {
        String picOrderGuid = getAsId().getOrderGuid();
        if (StrUtils.isValid(picOrderGuid)) {
            for (iPartsDataPicOrderAttachment attachment : picOrderAttachments) {
                addAttachment(attachment, origin);
            }
        }
    }

    public void deleteAttachment(iPartsDataPicOrderAttachment attachment, DBActionOrigin origin) {
        getAttachmentsBDObjectList().delete(attachment, origin);
        Iterator<iPartsDataPicOrderAttachmentReference> iterator = getAttachmentReferencesBDObjectList().iterator();
        String attachmentGuid = attachment.getAsId().getAttachmentGuid();
        while (iterator.hasNext()) {
            iPartsDataPicOrderAttachmentReference reference = iterator.next();
            if (reference.getAsId().getAttachmentGuid().equals(attachmentGuid)) {
                iterator.remove();
                break;
            }
        }
    }

    public void setOriginalOrder(iPartsPicOrderId originalOrder) {
        setFieldValue(FIELD_DA_PO_ORIGINAL_PICORDER, originalOrder.getOrderGuid(), DBActionOrigin.FROM_EDIT);
    }

    protected synchronized void loadModules() {
        if (modulesList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_MODULES, iPartsDataPicOrderModulesList.loadPicOrderModulesListForOrder(getEtkProject(), getAsId().getOrderGuid()));
    }

    public iPartsDataPicOrderModulesList getModules() {
        loadModules();
        return modulesList;
    }

    public synchronized void addModule(String moduleNo, DBActionOrigin origin) {
        iPartsPicOrderModulesId picOrderModulesId = new iPartsPicOrderModulesId(getAsId().getOrderGuid(), moduleNo);
        iPartsDataPicOrderModules picOrderModules = new iPartsDataPicOrderModules(getEtkProject(), picOrderModulesId);
        picOrderModules.initAttributesWithEmptyValues(origin);
        getModules().add(picOrderModules, origin);
    }

    public synchronized void addModule(iPartsDataPicOrderModules picOrderModule, DBActionOrigin origin) {
        if (picOrderModule != null) {
            getModules().add(picOrderModule, origin);
        }
    }

    protected synchronized void loadUsages() {
        if (usagesList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_USAGES, iPartsDataPicOrderUsageList.loadPicOrderUsageList(getEtkProject(), getAsId().getOrderGuid()));
    }

    public iPartsDataPicOrderUsageList getUsages() {
        loadUsages();
        return usagesList;
    }

    public synchronized void addUsage(iPartsProductId productId, EinPasId einPasId, DBActionOrigin origin) {
        iPartsPicOrderUsageId picOrderUsageId = new iPartsPicOrderUsageId(getAsId().getOrderGuid(), productId, einPasId);
        iPartsDataPicOrderUsage picOrderUsage = new iPartsDataPicOrderUsage(getEtkProject(), picOrderUsageId);
        picOrderUsage.initAttributesWithEmptyValues(origin);
        getUsages().add(picOrderUsage, origin);
    }

    public synchronized void addUsage(iPartsProductId productId, KgTuId kgTuId, DBActionOrigin origin) {
        iPartsPicOrderUsageId picOrderUsageId = new iPartsPicOrderUsageId(getAsId().getOrderGuid(), productId, kgTuId);
        iPartsDataPicOrderUsage picOrderUsage = new iPartsDataPicOrderUsage(getEtkProject(), picOrderUsageId);
        picOrderUsage.initAttributesWithEmptyValues(origin);
        getUsages().add(picOrderUsage, origin);
    }

    public synchronized void addUsage(iPartsDataPicOrderUsage picOrderUsage, DBActionOrigin origin) {
        if (picOrderUsage != null) {
            getUsages().add(picOrderUsage, origin);
        }
    }

    protected synchronized void loadPictures() {
        if (picturesList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_PICTURES, iPartsDataPicOrderPicturesList.loadPicOrderPicturesList(getEtkProject(), getAsId().getOrderGuid()));
    }

    public iPartsDataPicOrderPicturesList getPictures() {
        loadPictures();
        return picturesList;
    }

    public synchronized iPartsDataPicOrderPicture addPicture(String picItemId, String picItemRevId, DBActionOrigin origin) {
        iPartsPicOrderPictureId picOrderPictureId = new iPartsPicOrderPictureId(getAsId().getOrderGuid(), picItemId, picItemRevId);
        iPartsDataPicOrderPicture picOrderPicture = new iPartsDataPicOrderPicture(getEtkProject(), picOrderPictureId);
        picOrderPicture.initAttributesWithEmptyValues(origin);
        getPictures().add(picOrderPicture, origin);
        return picOrderPicture;
    }

    public synchronized void addPicture(iPartsDataPicOrderPicture picOrderPicture, DBActionOrigin origin) {
        if (picOrderPicture != null) {
            getPictures().add(picOrderPicture, origin);
        }
    }

    /**
     * Liefert alle Bilder des Bildauftrags. Sollte es sich um einen Änderungsauftrag handeln, dann werden die Bilder
     * der vorangegangenen Bild- bzw. Änderungsaufträge ebenfalls zurückgeliefert. Außerdem wird jeweils nur die aktuellste
     * Version eines Bildes zurückgeliefert (aktuell = jüngster Bild- bzw. Änderungsauftrag)
     *
     * @return
     */
    public iPartsDataPicOrderPicturesList getPicturesWithPredecessors(boolean distinct) {
        if (picturesListWithPicturesFromPredecessor == null) {
            Map<String, iPartsDataPicOrderPicture> resultMap = new LinkedHashMap<>();
            addPicturesWithDistinctPictureIds(getPictures(), resultMap, distinct);
            if (isChangeOrder() && !hasFakeOriginalPicOrder()) {
                iPartsDataPicOrder previousOrder = new iPartsDataPicOrder(getEtkProject(), new iPartsPicOrderId(getOriginalPicOrder()));
                if (previousOrder.existsInDB()) {
                    addPicturesWithDistinctPictureIds(previousOrder.getPicturesWithPredecessors(distinct), resultMap, distinct);
                }
            }
            picturesListWithPicturesFromPredecessor = new iPartsDataPicOrderPicturesList();
            picturesListWithPicturesFromPredecessor.addAll(resultMap.values(), DBActionOrigin.FROM_DB);
        }
        return picturesListWithPicturesFromPredecessor;
    }

    private void addPicturesWithDistinctPictureIds(iPartsDataPicOrderPicturesList pictures,
                                                   Map<String, iPartsDataPicOrderPicture> resultMap,
                                                   boolean distinct) {
        List<iPartsDataPicOrderPicture> picturesToCheck;
        if (distinct) {
            // Wenn nur die Bildnummer relevant ist, müssen die Zeichnungen vorher sortiert werden (höchste Revision zuerst)
            picturesToCheck = pictures.getAsList().stream()
                    .sorted(Comparator.comparing((iPartsDataPicOrderPicture o) -> o.getAsId().getPicItemId()).thenComparing(o -> o.getAsId().getPicItemRevId()).reversed())
                    .collect(Collectors.toList());
        } else {
            picturesToCheck = pictures.getAsList();
        }

        // Hier die Zeichnungen in der Map ablegen
        for (iPartsDataPicOrderPicture picture : picturesToCheck) {
            String picItemId = picture.getAsId().getPicItemId();
            String key = distinct ? picItemId : (picItemId + "|" + picture.getAsId().getPicItemRevId());
            if (resultMap.containsKey(key)) {
                continue;
            }
            resultMap.put(key, picture);
        }
    }

    protected synchronized void loadParts() {
        if (partsList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_PARTS, iPartsDataPicOrderPartsList.loadPicOrderPartsList(getEtkProject(), getAsId().getOrderGuid()));
    }

    public iPartsDataPicOrderPartsList getParts() {
        loadParts();
        return partsList;
    }

    public synchronized void addPart(PartListEntryId partListEntryId, String hotspot, String partNumber, DBActionOrigin origin) {
        iPartsPicOrderPartId picOrderPartId = new iPartsPicOrderPartId(getAsId().getOrderGuid(), partListEntryId, hotspot, partNumber);
        iPartsDataPicOrderPart picOrderPart = new iPartsDataPicOrderPart(getEtkProject(), picOrderPartId);
        picOrderPart.initAttributesWithEmptyValues(origin);
        getParts().add(picOrderPart, origin);
    }

    public synchronized void addPart(iPartsDataPicOrderPart picOrderPart, DBActionOrigin origin) {
        if (picOrderPart != null) {
            getParts().add(picOrderPart, origin);
        }
    }


    // Convenience Methods
    public String getOrderIdExtern() {
        return getFieldValue(FIELD_DA_PO_ORDER_ID_EXTERN);
    }

    public String getOrderRevisionExtern() {
        return getFieldValue(FIELD_DA_PO_ORDER_REVISION_EXTERN);
    }

    public String getProposedName() {
        return getFieldValue(FIELD_DA_PO_PROPOSED_NAME);
    }

    public void setOrderIdExtern(String value, DBActionOrigin origin) {
        setAttributeValue(FIELD_DA_PO_ORDER_ID_EXTERN, value, origin);
    }

    public boolean isTemplate() {
        return getFieldValueAsBoolean(FIELD_DA_PO_IS_TEMPLATE);
    }

    public void setIsTemplate(boolean isTemplate, DBActionOrigin origin) {
        setFieldValueAsBoolean(FIELD_DA_PO_IS_TEMPLATE, isTemplate, origin);
    }

    public int getAutomationLevel() {
        return getFieldValueAsInteger(FIELD_DA_PO_AUTOMATION_LEVEL);
    }

    public void setAutomationLevel(String automationLevel, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_PO_AUTOMATION_LEVEL, automationLevel, origin);
    }

    public iPartsASPLMItemId getOrderItemId() {
        return new iPartsASPLMItemId(getOrderIdExtern(), getOrderRevisionExtern());
    }

    public boolean isASPLMError() {
        return ASPLM_ERROR_IDENTIFIER.getIdentifierFromDBValue(getLastErrorCode()) == ASPLM_ERROR_IDENTIFIER.ERROR;
    }

    public boolean isASPLMWarning() {
        return ASPLM_ERROR_IDENTIFIER.getIdentifierFromDBValue(getLastErrorCode()) == ASPLM_ERROR_IDENTIFIER.WARNING;
    }

    public boolean isASPLMComment() {
        return ASPLM_ERROR_IDENTIFIER.getIdentifierFromDBValue(getLastErrorCode()) == ASPLM_ERROR_IDENTIFIER.COMMENT;
    }

    public String getLastErrorCode() {
        return getFieldValue(FIELD_DA_PO_LAST_ERROR_CODE);
    }

    public String getChangeReason() {
        return getFieldValue(FIELD_DA_PO_CHANGE_REASON);
    }

    public void setChangeReason(String changeReason) {
        if (StrUtils.isValid(changeReason)) {
            setFieldValue(FIELD_DA_PO_CHANGE_REASON, changeReason, DBActionOrigin.FROM_EDIT);
        }
    }

    public void setLastErrorCode(int errorCode) {
        setFieldValue(FIELD_DA_PO_LAST_ERROR_CODE, String.valueOf(errorCode), DBActionOrigin.FROM_EDIT);
    }

    public String getLastErrorTextForAllLanguages() {
        return getFieldValue(FIELD_DA_PO_LAST_ERROR_TEXT);
    }

    public String getLastErrorText(String language) {
        return XMLImportExportHelper.getErrorText(getLastErrorTextForAllLanguages(), language);
    }

    public void setLastErrorTextForAllLanguages(String code) {
        setAttributeValue(FIELD_DA_PO_LAST_ERROR_TEXT, code, DBActionOrigin.FROM_EDIT);
    }

    public void setLastWarningTextForAllLanguages(String code) {
        setFieldValue(FIELD_DA_PO_LAST_ERROR_CODE, ASPLM_ERROR_IDENTIFIER.WARNING.getPrefix(), DBActionOrigin.FROM_EDIT);
        setAttributeValue(FIELD_DA_PO_LAST_ERROR_TEXT, code, DBActionOrigin.FROM_EDIT);
    }

    public void setInfoTextForAllLanguages(String info) {
        setFieldValue(FIELD_DA_PO_LAST_ERROR_CODE, ASPLM_ERROR_IDENTIFIER.COMMENT.getPrefix(), DBActionOrigin.FROM_EDIT);
        setAttributeValue(FIELD_DA_PO_LAST_ERROR_TEXT, info, DBActionOrigin.FROM_EDIT);
    }

    public void setLastCommentTextForAllLanguages(String userId, String code) {
        String userIdValue = userId;
        if (userIdValue == null) {
            userIdValue = "";
        }
        setFieldValue(FIELD_DA_PO_LAST_ERROR_CODE, ASPLM_ERROR_IDENTIFIER.COMMENT.getPrefix() + userIdValue, DBActionOrigin.FROM_EDIT);
        setAttributeValue(FIELD_DA_PO_LAST_ERROR_TEXT, code, DBActionOrigin.FROM_EDIT);
    }

    public iPartsTransferStates getStatus() {
        return iPartsTransferStates.getFromDB(getFieldValue(FIELD_DA_PO_STATUS));
    }

    public void setStatus(iPartsTransferStates status, DBActionOrigin origin) {
        if (status != null) {
            setFieldValue(FIELD_DA_PO_STATUS, status.getDBValue(), origin);
        }
    }

    public void setEventNameForInvalidState(iPartsEventStates eventState) {
        // Hier muss der eigentliche Name des Enums verwendet werden und nicht der AS-PLM Wert, weil AS-PLM bei einem
        // zurückgewiesenen Bildauftrag einen "leeren" Wert schickt. D.h. wenn das Feld geleert und der Wert aus
        // der Db geladen wird, kann man so nicht unterscheiden, ob es ein Event von AS-PLM ist oder einfach nur ein
        // leeres Feld.
        setFieldValue(FIELD_DA_PO_EVENTNAME, eventState.name(), DBActionOrigin.FROM_EDIT);
    }

    public void clearEventNameForInvalidState() {
        setFieldValue(FIELD_DA_PO_EVENTNAME, "", DBActionOrigin.FROM_EDIT);
    }

    public boolean hasEventForInvalidState() {
        return getEventNameForInvalidState() != null;
    }

    public iPartsEventStates getEventNameForInvalidState() {
        // valueOf(), weil siehe setEventNameForInvalidState()
        return iPartsEventStates.getFromEnumName(getFieldValue(FIELD_DA_PO_EVENTNAME));
    }

    /**
     * Liefert zurück, ob der Bildauftrag als "ungültig" markiert wurde
     *
     * @return
     */
    public boolean isInvalid() {
        return getFieldValueAsBoolean(FIELD_DA_PO_ORDER_INVALID);
    }

    /**
     * Liefert zurück, ob ein Bildauftrag ungültige Zeichnungen besitzt
     *
     * @return
     */
    public boolean hasInvalidImageData() {
        return getFieldValueAsBoolean(FIELD_PO_INVALID_IMAGE_DATA);
    }

    /**
     * Liefert zurück, ob der Bildauftrag storniert wurde
     *
     * @return
     */
    public boolean isCancelled() {
        return getStatus() == iPartsTransferStates.CANCEL_CONFIRMATION;
    }

    /**
     * Liefert zurück, ob der Bildauftrag "gültig" ist, bzw. nicht ungültig markiert ist.
     *
     * @return
     */
    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isCopy() {
        return getFieldValueAsBoolean(FIELD_PO_IS_COPY);
    }

    public static iPartsDataPicOrder createEmptyDataPicOrder(EtkProject project, AssemblyId assemblyId, Collection<EtkDataPartListEntry> selectedPartListEntries) {
        iPartsPicOrderId id = new iPartsPicOrderId(StrUtils.makeGUID());

        iPartsDataPicOrder dataPicOrder = new iPartsDataPicOrder(project, id);
        dataPicOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        if ((assemblyId != null) && assemblyId.isValidId()) {
            dataPicOrder.addModule(assemblyId.getKVari(), DBActionOrigin.FROM_EDIT);
            if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                for (EtkDataPartListEntry dataPartListEntry : selectedPartListEntries) {
                    iPartsDataPicOrderPart picOrderPart = PicOrderPartlistEntriesForm.createDataPicOrderPart(project, dataPicOrder, dataPartListEntry);
                    dataPicOrder.getParts().add(picOrderPart, DBActionOrigin.FROM_EDIT);
                }
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Could not add assembly \""
                                                                   + ((assemblyId == null) ? "NULL" : assemblyId.toStringForLogMessages())
                                                                   + "\" to picture order \"" + dataPicOrder.getAsId().getOrderGuid() + "\"."
                                                                   + (((selectedPartListEntries == null) || selectedPartListEntries.isEmpty()) ? "" : " Part list entries could not be added as well."));
        }

        return dataPicOrder;
    }

    public static iPartsDataPicOrder fillDataPicOrderNoDoubles(EtkProject project, iPartsDataPicOrder dataPicOrder, List<EtkDataPartListEntry> selectedPartListEntries) {
        if ((dataPicOrder != null) && (selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
            iPartsDataPicOrderPartsList partsList = dataPicOrder.getParts();
            List<PartListEntryId> partListEntryIdList = new DwList<>();
            for (iPartsDataPicOrderPart dataPicOrderPart : partsList) {
                partListEntryIdList.add(dataPicOrderPart.getAsId().getPartListEntryId());
            }

            for (EtkDataPartListEntry dataPartListEntry : selectedPartListEntries) {
                if (!partListEntryIdList.contains(dataPartListEntry.getAsId())) {
                    iPartsDataPicOrderPart picOrderPart = PicOrderPartlistEntriesForm.createDataPicOrderPart(project, dataPicOrder, dataPartListEntry);
                    dataPicOrder.getParts().add(picOrderPart, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return dataPicOrder;
    }

    // Convenience Methods End

    /**
     * Erstellt und befüllt ein {@link iPartsXMLMediaMessage} Objekt mit den Daten dieses Bildauftrags. Über den <i>isChangeOrder</i>
     * wird entschieden, ob es sich um einen Bildauftrag oder einen Änderungsauftrag handelt.
     *
     * @param connector
     * @param structType
     * @param nodeDesc
     * @param isChangeOrder Handelt es sich um einen Änderungsauftrag?
     * @param reason        Grund für einen Änderungsauftrag
     * @return
     */
    public iPartsXMLMediaMessage getAsMessageObject(EditModuleFormIConnector connector, PRODUCT_STRUCTURING_TYPE structType,
                                                    EtkMultiSprache nodeDesc, boolean isChangeOrder, String reason) throws ParseException, DateException {
        if (!MQHelper.checkTransmissionToASPLMConfigWithMessage()) {
            return null;
        }
        // Erstellen der CreateMediaOrder
        iPartsDataPicOrderUsage picOrderUsage = getUsages().get(0);
        AbstractMediaOrderRequest mediaOrderOperation;
        mediaOrderOperation = getMediaOrderOperation(structType, nodeDesc, isChangeOrder, reason);
        boolean isRejected = iPartsTransferStates.isRejectedState(getStatus());
        if (!(connector.getCurrentAssembly() instanceof iPartsDataAssembly)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while creating picture order for assembly: "
                                                                       + connector.getCurrentAssembly());
            return null;
        }
        iPartsDataAssembly currentAssembly = (iPartsDataAssembly)connector.getCurrentAssembly();
        if ((mediaOrderOperation.getKgTuId() == null) && (mediaOrderOperation.getEinPasId() == null)) {
            iPartsTransferNodeTypes operationType;
            if (isRejected) {
                operationType = iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER;
            } else {
                operationType = isChangeOrder ? iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER : iPartsTransferNodeTypes.CREATE_MEDIA_ORDER;
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Product structure type is not valid for creating MQ "
                                                                       + operationType + " message. Structure Type: "
                                                                       + structType);
            return null;
        }
        mediaOrderOperation.setName(getFieldValue(FIELD_DA_PO_PROPOSED_NAME));
        // Die bisherige "Description" soll als "Remark" ausgegeben werden
        String remark = createRemark(connector);
        if (StrUtils.isValid(remark)) {
            mediaOrderOperation.setRemark(remark);
        }
        String productNo = picOrderUsage.getAsId().getProductNo();
        iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), new iPartsProductId(productNo));
        // Modeltypen als AssignedProjects setzen
        String modelTypesAsAssignedProjects = XMLImportExportHelper.createAssignedProjectsFromModelTypes(getEtkProject(), product);
        mediaOrderOperation.setAssignedProjects(modelTypesAsAssignedProjects);

        // Check, ob die Unternehmenszugehörigkeit bestimmt werden kann und setzt diese für diesen CreateMedia- oder
        // ChangeMedia- oder UpdateMedia-Auftrag
        if (!setCompany(mediaOrderOperation, product, currentAssembly)) {
            if (Session.get().canHandleGui()) {
                MessageDialog.showError("!!Der Bildauftrag kann nicht abgeschlossen werden. Bitte wenden Sie sich an den Systemadministrator.",
                                        "!!Bildauftrag");
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not determine a company value " +
                                                                       "for given pic order values: " + productNo + "; "
                                                                       + currentAssembly.getAsId().toStringForLogMessages()
                                                                       + "; " + mediaOrderOperation.getOperationType());
            return null;
        }

        // ProductType bestimmen
        iPartsProductTypes productType = getASPLMProductType(product);
        iPartsXMLProduct productXML = new iPartsXMLProduct(productNo, productType);
        mediaOrderOperation.addProduct(productXML);

        // IPartsPositionList (inkl. aufsammeln der SourceContext Elemente für den "WorkingContext")
        fillPositionList(connector.getCurrentPartListEntries(), mediaOrderOperation, isRejected);
        // WorkingContext
        boolean isPKW = currentAssembly.getDocumentationType().isPKWDocumentationType();
        boolean collectWorkingContextElements = !isPKW && isOperationWithWorkingContext(mediaOrderOperation) && (getParts() != null) && !getParts().isEmpty();
        if (collectWorkingContextElements) {
            String workingContext = getWorkingContextFromParts(connector.getCurrentPartListEntries());
            if (StrUtils.isValid(workingContext)) {
                mediaOrderOperation.setWorkingContext(workingContext);
            }
        }
        // Hier werden die Bilder mitgeschickt, die angepasst werden sollen
        if (isChangeOrder && !isRejected) {
            addPicturesToOperation(mediaOrderOperation);
        }
        // Bauen eines Default Bildauftrags als iPartsXMLMessage Objekt
        return XMLObjectCreationHelper.getInstance().createDefaultPicOrderXMLMessage(mediaOrderOperation, getRequestor(), getAsId().getOrderGuid());
    }

    /**
     * Setzt die Unternehmenszugehörigkeit in Abhängigkeit des Produkts und der aktuellen Stückliste
     *
     * @param mediaOrderOperation
     * @param product
     * @param currentAssembly
     * @return
     */
    private boolean setCompany(AbstractMediaOrderRequest mediaOrderOperation, iPartsProduct product, iPartsDataAssembly currentAssembly) {
        boolean isCarProduct = product.isCarAndVanProduct();
        boolean isTruckProduct = product.isTruckAndBusProduct();
        // Wenn über das Produkt nicht eindeutig bestimmt werden kann, ob es ein PKW- oder Truck-Produkt ist, dann wird
        // die Zugehörigkeit über den Bearbeitungsauftrag bestimmt (LKW oder PKW)
        if ((isCarProduct && isTruckProduct) || (!isCarProduct && !isTruckProduct) || currentAssembly.isSAAssembly()) {
            return setCompanyWithAuthorOrder(mediaOrderOperation);
        }

        // Ist es ein PKW-Produkt → MBAG, ansonsten ist es ein Truck-Produkt → DTAG
        if (isCarProduct) {
            mediaOrderOperation.setCompany(iPartsTransferConst.COMPANY_VALUE_MBAG);
        } else {
            mediaOrderOperation.setCompany(iPartsTransferConst.COMPANY_VALUE_DTAG);
        }
        return true;
    }

    /**
     * Bestimmt die Unternehmenszugehörigkeit über die Werte im Bearbeitungsauftrag
     *
     * @param mediaOrderOperation
     * @return
     */
    private boolean setCompanyWithAuthorOrder(AbstractMediaOrderRequest mediaOrderOperation) {
        // Bearbeitungsauftrag via Edit-ChangeSet bestimmen
        iPartsDataWorkOrder currentWorkOrder = iPartsWorkOrderCache.getWorkOrderForAuthorOrder(getEtkProject());
        if (currentWorkOrder == null) {
            return false;
        }

        // Handelt es sich um einen PKW-Bearbeitungsauftrag → MBAG
        // Handelt es sich um einen Truck-Bearbeitungsauftrag → DTAG
        if (currentWorkOrder.isVisibleForUserProperties(true, false)) {
            mediaOrderOperation.setCompany(iPartsTransferConst.COMPANY_VALUE_MBAG);
        } else if (currentWorkOrder.isVisibleForUserProperties(false, true)) {
            mediaOrderOperation.setCompany(iPartsTransferConst.COMPANY_VALUE_DTAG);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Liefert den Requestor für den Medien-Auftrag
     *
     * @return
     */
    private iPartsXMLRequestor getRequestor() {
        // Den Requestor
        iPartsXMLRequestor requestor = new iPartsXMLRequestor(getFieldValue(FIELD_DA_PO_USER_GUID));
        requestor.setGroupId(getFieldValue(FIELD_DA_PO_USER_GROUP_GUID));
        return requestor;
    }

    /**
     * Hängt die Bilder an den Bildauftrag, sofern welche selektiert wurden
     *
     * @param mediaOrderOperation
     */
    private void addPicturesToOperation(AbstractMediaOrderRequest mediaOrderOperation) {
        iPartsXMLChangeMediaOrder changeMediaOperation = (iPartsXMLChangeMediaOrder)mediaOrderOperation;
        for (iPartsDataPicOrderPicture picture : getPictures()) {
            if (picture.isUsed()) {
                changeMediaOperation.addMediaVariant(picture.getAsMediaVariant(""));
            }
        }
    }

    private iPartsProductTypes getASPLMProductType(iPartsProduct product) {
        if (product.isAggregateProduct(getEtkProject())) {
            return iPartsProductTypes.AGGREGATE;
        } else {
            return iPartsProductTypes.VEHICLE;
        }
    }

    /**
     * Liefert den Bearbeitungskontext zu allen Positionen an diesem Bild-/Änderungsauftrag
     *
     * @param partListEntries
     * @return
     */
    public String getWorkingContextFromParts(List<EtkDataPartListEntry> partListEntries) {
        Set<String> workingContextElements = new TreeSet<>();
        String previousPicOrder = getOriginalPicOrder();
        // Die neuen Positionen zum aktuellen Auftrag bekommen wir, wenn wir die Positionen aus dem vorherigen Auftrag
        // entfernen.
        // 1. Positionen zum alten Auftrag bestimmen
        iPartsDataPicOrderPartsList partsFromPreviousOrder = iPartsDataPicOrderPartsList.loadPicOrderPartsList(getEtkProject(), previousPicOrder);
        // Existieren keine Positionen zum vorherigen Auftrag, dann sind alle Positionen am aktuellen Auftrag "neu"
        boolean checkPreviousParts = !partsFromPreviousOrder.isEmpty();
        // 2. Alle Positionen zum aktuellen Auftrag durchlaufen und prüfen, ob die Position auch schon am vorherigen Auftrag hing
        for (iPartsDataPicOrderPart part : getParts()) {
            if (checkPreviousParts) {
                // ID erzeugen mit der GUID des alten Auftrags
                iPartsPicOrderPartId partId = part.getAsId();
                iPartsPicOrderPartId picOrderPartId = new iPartsPicOrderPartId(previousPicOrder, partId.getKatalogVari(),
                                                                               partId.getKatalogVer(), partId.getKatalogLfdNr(),
                                                                               partId.getHotSpot(), partId.getPartNumber());
                // Check, ob die Position am alten Auftrag hing
                if (partsFromPreviousOrder.containsId(picOrderPartId)) {
                    continue;
                }
            }
            // Die echte Position bestimmen
            EtkDataPartListEntry partListEntry = getPartListEntryForPart(part, partListEntries);
            if (partListEntry == null) {
                continue;
            }
            // K_SOURCE_CONTEXT bearbeiten
            addTruckWorkingContextElement(partListEntry, workingContextElements);

        }
        return String.join(", ", workingContextElements);
    }

    private EtkDataPartListEntry getPartListEntryForPart(iPartsDataPicOrderPart part, List<EtkDataPartListEntry> partListEntries) {
        EtkDataPartListEntry partListEntry = findPartListEntry(part, partListEntries);
        if (partListEntry == null) {
            partListEntry = part.getStoredRelatedPartListEntry();
        }
        return partListEntry;
    }

    /**
     * Befüllt das XML Element "IPartsPositionList" mit den Informationen der selektierten Stücklistenposition
     *
     * @param partListEntries
     * @param mediaOrderOperation
     * @param isRejected
     */
    private void fillPositionList(List<EtkDataPartListEntry> partListEntries, AbstractMediaOrderRequest mediaOrderOperation, boolean isRejected) {
        iPartsDataPicOrderPartsList parts = getParts();
        if ((parts != null) && !parts.isEmpty()) {
            // Sequenznummer für neue Bildpositionen bestimmen. Bei zurückgewiesenen Aufträgen soll die Sequenznummer
            // nicht erhöht werden
            int nextPicPartSeqNo = getNextPicPartSeqNo(!isRejected);
            for (iPartsDataPicOrderPart part : parts) {
                iPartsPicOrderPartSourceType sourceType = iPartsPicOrderPartSourceType.getFromDbValue(part.getSourceKey());
                if (sourceType != iPartsPicOrderPartSourceType.NONE) {
                    // Check, ob schon ein Entry gespeichert wurde
                    EtkDataPartListEntry entry = part.getStoredRelatedPartListEntry();
                    if (entry == null) {
                        // Erst beim Senden das iPartsDataPicOrderPart Objekt mit den aktuellen Werten befüllen
                        entry = findPartListEntry(part, partListEntries);
                        part.fillFromRealEntry(entry);
                    }

                    // Handelt es sich bei dem Bildauftrag um eine Kopie, so sollen alle Bildpositionen aus dem Originalauftrag
                    // den Sequenzzähler 1 bekommen. Falls eine neue Position hinzugefügt bekommt diese folglich die Sequenznummer 2
                    if (isCopy()) {
                        if (part.getPicPosSeqNo() > 0) {
                            part.setFieldValueAsInteger(FIELD_DA_PPA_SEQ_NO, 1, DBActionOrigin.FROM_EDIT);
                            nextPicPartSeqNo = 2;
                        }
                    }
                    // Ist schon eine Sequenznummer > 0 enthalten, dann handelt es sich um eine in einem früheren Auftrag
                    // verschickte Position. Neue Positionen haben die Sequenznummer "0".
                    if (part.getPicPosSeqNo() <= 0) {
                        part.setFieldValueAsInteger(FIELD_DA_PPA_SEQ_NO, nextPicPartSeqNo, DBActionOrigin.FROM_EDIT);
                    }

                    iPartsXMLPartPosition xmlPartPosition = new iPartsXMLPartPosition();
                    xmlPartPosition.setExternalId(part.getExternalId());
                    xmlPartPosition.setHotspot(part.getHotSpot());
                    xmlPartPosition.setAssemblySign(part.getAssemblySign());
                    xmlPartPosition.setPicturePositionMarker(part.getPicturePositionMarker());
                    xmlPartPosition.setPicPosSeqNo(part.getPicPosSeqNo());
                    iPartsXMLPartNumber partNumber = new iPartsXMLPartNumber(part.getZGS(),
                                                                             XMLImportExportDateHelper.getDateFromDBDateTime(part.getReleaseDate()),
                                                                             part.getPartNumber());
                    xmlPartPosition.setPartNumber(partNumber);

                    // Bei gelöschten Entries kann in der PartList das Entry Objekt nicht gefunden werden
                    // → Schauen, ob ein gespeichertes Entry existiert
                    if (entry == null) {
                        entry = part.getStoredRelatedPartListEntry();
                    }

                    if ((entry == null) || (entry.getAttributes() == null)) { // Stücklisteneintrag nicht gefunden
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Parts list entry " + part.getAsId().getPartListEntryId().toStringForLogMessages()
                                                                                   + " for picture order parts information could not be found in the database or in the picture order \""
                                                                                   + getAsId().getOrderGuid() + "\" and will be skipped");
                        continue;
                    }
                    // Fehlerorte setzen
                    if (!entry.attributeExists(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION) && (entry instanceof iPartsDataPartListEntry)) {
                        ((iPartsDataPartListEntry)entry).calculateOriginalFailLocation();
                        ((iPartsDataPartListEntry)entry).calculateInheritedFailLocation(true, null);
                    }
                    xmlPartPosition.setFaultLocation(entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION));
                    // Generischen Verbauort setzen
                    xmlPartPosition.setGenericInstallLocation(entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO));

                    if (entry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
                        EtkDataPart dataPart = partListEntry.getPart();
                        if (dataPart.getAttributes() != null) {
                            // Teilebenennung
                            xmlPartPosition.setPartName(dataPart.loadMultiLanguageFromDB(dataPart.getAttribute(FIELD_M_TEXTNR)));
                            // Ergänzungstexte
                            // Diese dürfen nicht aus der DB geladen werden, weil nur der Zustand zum Zeitpunkt des Versendens
                            // zählt. Diese ganz speziellen kombinierten Texte hängen am iPartsDataPicOrderPart.
                            String neutralTextFromPart = dataPart.getFieldValue(FIELD_M_ADDTEXT, Language.DE.getCode(), false);
                            xmlPartPosition.setSupplementaryTexts(part.getCombText(), neutralTextFromPart);
                        } else { // Material nicht gefunden
                            xmlPartPosition.setPartName(new EtkMultiSprache());
                            xmlPartPosition.setSupplementaryTexts(null, "");
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Material \"" + dataPart.getAsId().getMatNr()
                                                                                       + "\" for picture order parts information of parts list entry "
                                                                                       + part.getAsId().getPartListEntryId().toStringForLogMessages()
                                                                                       + " could not be found in the database or in the picture order \""
                                                                                       + getAsId().getOrderGuid() + "\"");
                        }
                        // Menge
                        xmlPartPosition.setQuantity(partListEntry.getFieldValue(FIELD_K_MENGE));
                        // Strukturstufe
                        xmlPartPosition.setStructureLevel(partListEntry.getFieldValue(FIELD_K_HIERARCHY));
                        // Hotspot kann sich innerhalb vom Autoren-Auftrag geändert haben!
                        xmlPartPosition.setHotspot(partListEntry.getFieldValue(FIELD_K_POS));

                        boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
                        partListEntry.setLogLoadFieldIfNeeded(false);
                        try {
                            AbstractSourceKey sourceKey = null;
                            // Unterscheidung PKW ↔ Truck
                            if (partListEntry.getOwnerAssembly().getDocumentationType().isPKWDocumentationType()) {
                                String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
                                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);

                                // Wenn der Schlüssel nichts Vernünftiges enthielt, Versuch via K_SOURCE_CONTEXT.
                                if (bctePrimaryKey == null) {
                                    String context = partListEntry.getFieldValue(FIELD_K_SOURCE_CONTEXT);
                                    iPartsSeriesId seriesId = EditConstructionToRetailHelper.getSeriesIdFromDIALOGSourceContext(context);
                                    HmMSmId hmMSmId = HmMSmId.getHmMSmIdFromDIALOGSourceContext(context);
                                    if ((seriesId != null) && (hmMSmId != null)) {
                                        bctePrimaryKey = new iPartsDialogBCTEPrimaryKey(seriesId.getSeriesNumber(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(),
                                                                                        partListEntry.getFieldValue(FIELD_K_SOURCE_REF1),
                                                                                        partListEntry.getFieldValue(FIELD_K_SOURCE_REF2),
                                                                                        "", "", "", "");
                                    }
                                }
                                if (bctePrimaryKey != null) {
                                    sourceKey = new iPartsXMLSourceKeyDialog(bctePrimaryKey);
                                }
                            } else {
                                EtkDataArray saaValidity = partListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY);
                                String saaValidityString = saaValidity.getArrayAsString(",");
                                if (StrUtils.isValid(saaValidityString)) {
                                    sourceKey = new iPartsXMLSourceKeyTruck(saaValidityString);
                                }
                            }
                            xmlPartPosition.setSourceKey(sourceKey);
                        } finally {
                            partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                        }
                    }
                    mediaOrderOperation.addPartPosition(xmlPartPosition);
                }
            }
        }
    }

    /**
     * Liefert zurück, ob es sich um eine Operation handelt, die ein "WorkingContext" Element haben kann
     *
     * @param mediaOrderOperation
     * @return
     */
    private boolean isOperationWithWorkingContext(AbstractMediaOrderRequest mediaOrderOperation) {
        return (mediaOrderOperation.getOperationType() == iPartsTransferNodeTypes.CREATE_MEDIA_ORDER)
               || (mediaOrderOperation.getOperationType() == iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER)
               || (mediaOrderOperation.getOperationType() == iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER);
    }


    /**
     * Extrahiert aus dem SourceContext der Stücklistenposition die SAA/GS um damit für Trucks das Element "WorkingContext"
     * zu befüllen.
     *
     * @param entry
     * @param workingContextElements
     */
    private void addTruckWorkingContextElement(EtkDataPartListEntry entry, Set<String> workingContextElements) {
        if ((entry instanceof iPartsDataPartListEntry)) {
            boolean isPKWEntry = ((iPartsDataPartListEntry)entry).getOwnerAssembly().getDocumentationType().isPKWDocumentationType();
            if (!isPKWEntry) {
                String sourceContext = entry.getFieldValue(FIELD_K_SOURCE_CONTEXT);
                if (!sourceContext.isEmpty()) {
                    List<String> sourceContextElements = EditConstructionToRetailHelper.getSourceContextElements(sourceContext);
                    if (sourceContextElements == null) {
                        // Fehler beim Zerlegen des SourceContext
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Working context for partlistentry " + entry.getAsId().toStringForLogMessages()
                                                                                   + " could not be created for picture order \""
                                                                                   + getAsId().getOrderGuid() + "\" and will receive an empty value.");

                    } else if (sourceContextElements.size() > 1) {
                        // MBS hat als letzten Teil des Context die SAA/GS samt KG. Bei SAAs muss die darüberliegende
                        // SAA verwendet werden. Bei GS wird das Element samt KG genommen.
                        for (int i = sourceContextElements.size() - 1; i >= 0; i--) {
                            String workingContextElement = sourceContextElements.get(i);
                            // Entweder haben wir ein Element, dass den Delimiter "KG" nicht besitzt oder wir haben
                            // ein Element mit "KG" und das mit "G" beginnt (Grundstückliste)
                            if (!workingContextElement.contains(MBS_CON_GROUP_DELIMITER)
                                || workingContextElement.startsWith(BASE_LIST_NUMBER_PREFIX)) {
                                workingContextElements.add(workingContextElement);
                                break;
                            }
                        }
                    } else {
                        // EDS und Positionen aus dem CTT Importer haben nur die SAA als SourceContext
                        workingContextElements.add(sourceContextElements.get(0));
                    }
                }
            }
        }
    }

    /**
     * Liefert die nächste Sequenznummer für neu hinzugefügte Bildpositionen. Es werden alle bisherigen Bildpositionen
     * durchlaufen und die höchste Nummer bestimmt. Zurückgeliefert wird die höchste Nummer + 1, sofern eine Erhöhung
     * gewünscht ist
     *
     * @return
     */
    private int getNextPicPartSeqNo(boolean increaseSeqNo) {
        int result = 1;
        for (iPartsDataPicOrderPart part : getParts()) {
            int picPartSeqNo = part.getPicPosSeqNo();
            if (picPartSeqNo >= result) {
                result = picPartSeqNo;
                if (increaseSeqNo) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Erzeugt die Bemerkung für den Bildauftrag
     *
     * @param connector
     * @return
     */
    private String createRemark(EditModuleFormIConnector connector) {
        String remark = getFieldValue(FIELD_DA_PO_DESCRIPTION);
        StringBuilder completeRemark = new StringBuilder(remark);
        String code = getFieldValue(FIELD_DA_PO_CODES);
        addTextToRemark(completeRemark, code, TranslationHandler.translate("!!Code:"));
        String eventIdFrom = getFieldValue(FIELD_DA_PO_EVENT_FROM);
        addTextToRemark(completeRemark, eventIdFrom, TranslationHandler.translate("!!Ereignis ab:"));
        String eventIdTo = getFieldValue(FIELD_DA_PO_EVENT_TO);
        addTextToRemark(completeRemark, eventIdTo, TranslationHandler.translate("!!Ereignis bis:"));
        addRetailSaRemark(connector, completeRemark);
        return completeRemark.toString();
    }

    /**
     * Fügt einen für freie SAs spezifischen Kommentar hinzu
     *
     * @param connector
     * @param completeRemark
     */
    private void addRetailSaRemark(EditModuleFormIConnector connector, StringBuilder completeRemark) {
        if (iPartsPicOrderEditHelper.isRetailSa(connector)) {
            iPartsModuleId moduleId = new iPartsModuleId(connector.getCurrentAssembly().getAsId().getKVari());
            EtkProject project = connector.getProject();
            iPartsDataSAModulesList saModules = iPartsDataSAModulesList.loadDataForModule(project, moduleId);
            if (!saModules.isEmpty()) {
                String saNumber = saModules.get(0).getAsId().getSaNumber();
                saNumber = project.getVisObject().asText(TABLE_DA_SA, FIELD_DS_SA, saNumber, project.getDBLanguage());
                String saText = RETAIL_SA_REMARK_PREFIX + " " + saNumber;
                String remark = completeRemark.toString();
                if (remark.isEmpty() || !remark.contains(saText)) {
                    addTextToRemark(completeRemark, saNumber, RETAIL_SA_REMARK_PREFIX);
                }
            }
        }
    }

    private void addTextToRemark(StringBuilder completeRemark, String additionalText, String prefix) {
        if (completeRemark != null) {
            if (StrUtils.isValid(additionalText)) {
                if (completeRemark.length() != 0) {
                    completeRemark.append("; ");
                }
                completeRemark.append(prefix).append(" ").append(additionalText);
            }
        }
    }

    /**
     * Liefert den Wert für das Element "Realization" im XML Dokument.
     */
    private String getRealizationValue() {
        String realizationDBValue = getFieldValue(FIELD_DA_PO_PICTURE_TYPE);
        if (StrUtils.isValid(realizationDBValue)) {
            // Wir müssen für die MQ-Nachricht an AS-PLM immer den deutschen Enum-Text verwenden
            return getEtkProject().getEnumText(ENUM_KEY_PICTURE_ORDER_TYPE, realizationDBValue, Language.DE.getCode(), true);
        }
        return "";
    }

    /**
     * Erstellt einen Bild-, Änderungs- oder Update-Auftrag, abhängig von den Werten, die übergeben werden.
     *
     * @param structType
     * @param nodeDesc
     * @param isChangeOrder
     * @param reason
     * @return
     */
    private AbstractMediaOrderRequest getMediaOrderOperation(PRODUCT_STRUCTURING_TYPE structType, EtkMultiSprache nodeDesc,
                                                             boolean isChangeOrder, String reason) throws ParseException, DateException {
        iPartsDataPicOrderUsage picOrderUsage = getUsages().get(0);
        // Fertigstellungsdatum (bei beiden Pflicht)
        String targetDate = getFieldValue(FIELD_DA_PO_TARGETDATE);
        Date dateDue;
        if (!targetDate.isEmpty()) {
            dateDue = DateUtils.toCalendar_yyyyMMdd(targetDate).getTime();
        } else {
            // Fallback auf jetzt, falls kein Fertigstellungsdatum in der DB existiert
            dateDue = new Date();
        }

        // Realization Wert (ist bei CreateMediaOrder Pflicht)
        String realizationValue = getRealizationValue();
        // Contractor (ist bei ChangMediaOrder Pflicht)
        iPartsXMLContractor contractor = getASPLMContractor();
        AbstractMediaOrderRequest operation;
        // Unterscheidung zwischen CreateMediaOrder, ChangeMediaOrder and UpdateMediaOrder
        if (iPartsTransferStates.isRejectedState(getStatus())) {
            // UpdateMediaOrder ist im Prinzip eine Mischung aus CreateMediaOrder und ChangeMediaOrder
            operation = new iPartsXMLUpdateMediaOrder(getOrderIdExtern(), getOrderRevisionExtern());
            if (StrUtils.isValid(realizationValue)) {
                operation.setRealization(realizationValue);
            }
            operation.setContractor(contractor);
            operation.setDateDue(dateDue);
        } else if (isChangeOrder) {
            operation = new iPartsXMLChangeMediaOrder(getOrderIdExtern(), getOrderRevisionExtern(), reason, contractor,
                                                      dateDue);
            if (StrUtils.isValid(realizationValue)) {
                operation.setRealization(realizationValue);
            }
            if (isCopy()) {
                ((iPartsXMLChangeMediaOrder)operation).setCopy(true);
            }
        } else {
            operation = new iPartsXMLCreateMediaOrder(realizationValue, dateDue);
            operation.setContractor(contractor);
        }
        // Jetzt die richtige Strukturinformation setzen
        switch (structType) {
            case EINPAS:
                operation.setEinPasData(picOrderUsage.getEinPASId(), nodeDesc);
                break;
            case KG_TU:
                operation.setKgTuData(picOrderUsage.getKgTuId(), nodeDesc);
                break;
        }
        return operation;
    }

    public iPartsXMLContractor getASPLMContractor() {
        // Contractor (Gruppen und Benutzer IDs aus DB bestimmen für Gruppen und Benutzer GUIDs)
        String groupGuid = getAttribute(FIELD_DA_PO_JOB_GROUP).getAsString();
        iPartsDataASPLMGroup groupData = new iPartsDataASPLMGroup(getEtkProject(), new iPartsASPLMGroupId(groupGuid));
        iPartsXMLContractor contractorXML = new iPartsXMLContractor(groupData.getASPLMGroupId());
        String userGuid = getAttribute(FIELD_DA_PO_JOB_USER).getAsString();
        String userId = "";
        if (!userGuid.isEmpty()) {
            iPartsDataASPLMUser userData = new iPartsDataASPLMUser(getEtkProject(), new iPartsASPLMUserId(userGuid));
            userId = userData.getUserASPLMId();
        }
        contractorXML.setUserId(userId);
        return contractorXML;
    }

    private EtkDataPartListEntry findPartListEntry(iPartsDataPicOrderPart part, List<EtkDataPartListEntry> list) {
        PartListEntryId searchId = part.getAsId().getPartListEntryId();
        for (EtkDataPartListEntry partListEntry : list) {
            if (partListEntry.getAsId().equals(searchId)) {
                return partListEntry;
            }
        }
        return null;
    }

    /**
     * Erstellt und befüllt ein {@link iPartsXMLGetMediaContents} Objekt mit den Daten dieses Bildauftrags
     *
     * @return
     * @throws ParseException
     */
    public iPartsXMLMediaMessage getAsMediaContentsObject() {
        iPartsXMLGetMediaContents mediaContents = new iPartsXMLGetMediaContents(getOrderIdExtern(), getOrderRevisionExtern());

        // Den Requestor
        iPartsXMLRequestor requestor = new iPartsXMLRequestor(getAttribute(FIELD_DA_PO_USER_GUID).getAsString());
        requestor.setGroupId(getAttribute(FIELD_DA_PO_USER_GROUP_GUID).getAsString());
        // Bauen eines Default GetMediaOrder als iPartsXMLMessage Objekt
        return XMLObjectCreationHelper.getInstance().createDefaultGetPicMediaXMLMessage(mediaContents, requestor, getAsId().getOrderGuid(),
                                                                                        iPartsTransferNodeTypes.GET_MEDIA_CONTENTS);
    }

    /**
     * Erstellt und befüllt ein {@link iPartsXMLCreateMcAttachments} Objekt mit den Daten dieses Bildauftrags und gibt es als {@link iPartsXMLMediaMessage} Objekt zurück
     *
     * @return
     * @throws ParseException
     */
    public iPartsXMLMediaMessage getAttachmentMessage() {
        List<iPartsDataPicOrderAttachment> list = getAttachments();
        if (!list.isEmpty()) {
            iPartsXMLCreateMcAttachments cma = new iPartsXMLCreateMcAttachments(getOrderIdExtern(), getOrderRevisionExtern());
            for (iPartsDataPicOrderAttachment attachmentDataObject : list) {
                if (attachmentDataObject.getStatus() == iPartsAttachmentStatus.NEW) {
                    cma.addAttachment(createXMLAttachmentFromDBObject(attachmentDataObject));
                    attachmentDataObject.setFieldValue(FIELD_DPA_STATUS, iPartsAttachmentStatus.SENT.getDBStatus(), DBActionOrigin.FROM_EDIT);
                }
            }
            iPartsXMLRequest request = new iPartsXMLRequest(getAsId().getOrderGuid(), iPartsTransferConst.PARTICIPANT_IPARTS, iPartsTransferConst.PARTICIPANT_ASPLM);
            iPartsXMLRequestor requestor = new iPartsXMLRequestor(iPartsConst.AS_PLM_USER_ID);
            request.setRequestor(requestor);
            request.setOperation(cma);
            iPartsXMLMediaMessage xmlMediaMessage = new iPartsXMLMediaMessage(true);
            xmlMediaMessage.setTypeObject(request);
            return xmlMediaMessage;
        }

        return null;
    }

    /**
     * Erstellt ein {@link iPartsXMLAttachment} Objekt aus dem übergebenen {@link iPartsDataPicOrderAttachment} Objekt
     *
     * @param attachmentDataObject
     * @return
     */
    private iPartsXMLAttachment createXMLAttachmentFromDBObject(iPartsDataPicOrderAttachment attachmentDataObject) {
        if (attachmentDataObject == null) {
            return null;
        }
        iPartsXMLAttachment attachment = new iPartsXMLAttachment(attachmentDataObject.getAsId().getAttachmentGuid(), attachmentDataObject.getFieldValue(FIELD_DPA_NAME));
        attachment.setDescription(attachmentDataObject.getFieldValue(FIELD_DPA_DESC));
        String fileType = attachmentDataObject.getFieldValue(FIELD_DPA_FILETYPE);
        if (iPartsTransferConst.AttachmentBinaryFileTypes.isValidFileExtension(fileType)) {
            iPartsXMLAttachmentBinaryFile binaryFile = new iPartsXMLAttachmentBinaryFile(fileType, Base64.encodeBase64String(attachmentDataObject.getFieldValueAsBlob(FIELD_DPA_CONTENT)));
            attachment.setBinaryFile(binaryFile);
        } else if (iPartsTransferConst.AttachmentTextFileTypes.isValidFileExtension(fileType)) {
            String content = new String(attachmentDataObject.getFieldValueAsBlob(FIELD_DPA_CONTENT));
            iPartsXMLAttachmentTextFile textFile = new iPartsXMLAttachmentTextFile(content, fileType);
            attachment.setTextFile(textFile);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid filetype for MQ attachment.");
            attachment = null;
        }
        return attachment;
    }

    /**
     * Löscht den aktuellen Kommentar
     */
    public void deleteComments() {
        String tag = getFieldValue(FIELD_DA_PO_LAST_ERROR_CODE);
        if (ASPLM_ERROR_IDENTIFIER.getIdentifierFromDBValue(tag) == ASPLM_ERROR_IDENTIFIER.COMMENT) {
            deleteErrorAndCommentValues();
        }
    }

    /**
     * Löscht Fehler und Warnungen samt Codes
     */
    public void deleteErrorAndCommentValues() {
        setFieldValue(FIELD_DA_PO_LAST_ERROR_CODE, "", DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DA_PO_LAST_ERROR_TEXT, "", DBActionOrigin.FROM_EDIT);
    }


    public boolean hasAttachments() {
        return getFieldValueAsBoolean(FIELD_DA_PO_HAS_ATTACHMENTS);
    }

    public boolean hasAttachmentsReadyToBeSent() {
        if (!hasAttachments()) {
            return false;
        }
        // Wenn mind. ein Anhang auf "NEW" steht, kann eine Anfrage losgeschickt werden.
        for (iPartsDataPicOrderAttachment attachment : getAttachments()) {
            if (attachment.getStatus() == iPartsAttachmentStatus.NEW) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zurück, ob die Größe der Anhänge valide ist (Nicht größer als der vorgegebene Wert MAX_ATTACHMENT_FILE_SIZE_IN_MB)
     *
     * @return
     */
    public boolean isAttachmentSizeValid() {
        return getAttachmentsMBSize() <= (MAX_ATTACHMENT_FILE_SIZE_IN_MB);
    }

    /**
     * Gibt die Gesamtsumme der Dateigrößen der Attachments in MB mit Nachkommastellen zurück
     *
     * @return
     */
    public double getAttachmentsMBSize() {
        return FrameworkUtils.getByteAsMegabyte(getAttachmentsByteSize());
    }

    /**
     * Dateigröße der Attachments in Bytes als Ganzzahl
     *
     * @return
     */
    public long getAttachmentsByteSize() {
        List<iPartsDataPicOrderAttachment> attachments = getAttachments();
        long attachmentsSize = 0;
        for (iPartsDataPicOrderAttachment attachment : attachments) {
            attachmentsSize += Long.parseLong(attachment.getFieldValue(FIELD_DPA_SIZE));
        }
        return attachmentsSize;
    }

    public boolean isChangeOrder() {
        return StrUtils.isValid(getOriginalPicOrder());
    }

    public boolean isChangeOrCopy() {
        return isChangeOrder() || isCopy();
    }

    public String getOriginalPicOrder() {
        return getFieldValue(FIELD_DA_PO_ORIGINAL_PICORDER);
    }

    public String getOriginalOrderForCopy() {
        return getFieldValue(FIELD_PO_ORIGINAL_ORDER_FOR_COPY);
    }

    public boolean isReplacedByChange() {
        return iPartsTransferStates.isReplacedByChangeOrder(getStatus());
    }

    public void resetUsedPictures() {
        for (iPartsDataPicOrderPicture picture : getPictures()) {
            picture.setIsUsed(false);
        }
    }

    public iPartsDataPicOrder createChangeOrder(boolean isCopy) {
        iPartsDataPicOrder newDataPicOrder = createPicOrderWithNewGUID();
        // Unterscheidung Kopierauftrag <> Änderungsauftrag
        if (isCopy) {
            // Kopie: Kein Vorgänger, Kopie Flag und Kopierauftrag Status setzen
            newDataPicOrder.setStatus(iPartsTransferStates.COPY_CREATED, DBActionOrigin.FROM_EDIT);
            newDataPicOrder.setFieldValue(FIELD_DA_PO_ORIGINAL_PICORDER, "", DBActionOrigin.FROM_EDIT);
            newDataPicOrder.setFieldValueAsBoolean(FIELD_PO_IS_COPY, true, DBActionOrigin.FROM_EDIT);
            newDataPicOrder.setCopyOriginalOrder(getAsId().getOrderGuid());
        } else {
            // Normaler Änderungsauftrag: Vorgänger und Änderungsauftrag Status setzen. Ist es ein Änderungsauftrag auf
            // einer Kopie, dann das Kopierauftrag Flag und GUID des kopierten Bildauftrags entfernen zurücksetzen
            newDataPicOrder.setStatus(iPartsTransferStates.CHANGE_CREATED, DBActionOrigin.FROM_EDIT);
            newDataPicOrder.setOriginalOrder(getAsId());
            newDataPicOrder.setFieldValueAsBoolean(FIELD_PO_IS_COPY, false, DBActionOrigin.FROM_EDIT);
            newDataPicOrder.setCopyOriginalOrder("");
        }
        return newDataPicOrder;
    }

    /**
     * Gleicht die Gültigkeiten der Bilder und die Gültigkeiten des Bildauftrags ab. Falls es Unterschiede gibt, werden
     * die Gültigkeiten der Bilder an den Bildauftrag geschrieben
     * <p>
     * Hinweis:
     * Eigentlich kommt von AS-PLM nur ein Bild pro Bildauftrag. Da rein technisch doch mehrere kommen können,
     * wird beim ersten Bild, das auch am Modul hängt und Unterschiede hat, aufgehört
     *
     * @param currentAssembly
     */
    public void alignPictureAndPicOrderValidities(EtkDataAssembly currentAssembly) {
        for (iPartsDataPicOrderPicture picture : getPictures()) {
            List<EtkDataImage> sortedPictures = currentAssembly.getImages().stream()
                    .filter(image -> image.getImagePoolNo().equals(picture.getAsId().getPicItemId()))
                    .sorted((current, next) -> current.getImagePoolVer().compareTo(next.getVer()))
                    .collect(Collectors.toList());
            if (!sortedPictures.isEmpty()) {
                EtkDataImage image = sortedPictures.get(sortedPictures.size() - 1);
                // Falls das Bild Gültigkeiten hat → an den Auftrag schreiben und raus aus dem Loop. Via AS-PLM kann es
                // nur ein Bild pro Auftrag geben, obwohl die Implementierung theoretisch mehrere erlaubt. Wir springen
                // bei ersten raus, weil der Bildauftrag auch nur Gültigkeiten für ein Bild halten kann
                if (copyValuesFromImageToPicOrder(image)) {
                    break;
                }
            }
        }
    }

    /**
     * Setzt die ID des Auftrags, der als Basis für die Kopie dient
     *
     * @param originalOrderId
     */
    private void setCopyOriginalOrder(String originalOrderId) {
        setFieldValue(FIELD_PO_ORIGINAL_ORDER_FOR_COPY, originalOrderId, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Erstellt aus dem aktuellen {@link iPartsDataPicOrder} Objekt eine Kopie mit einer neuen GUID. Die Kopie wird als
     * Änderungsauftrag im weiteren Workflow verwendet.
     *
     * @return
     */
    public iPartsDataPicOrder createPicOrderWithNewGUID() {
        String changeOrderGUID = StrUtils.makeGUID();
        iPartsPicOrderId newPicOrderId = new iPartsPicOrderId(changeOrderGUID);
        iPartsDataPicOrder newDataPicOrder = new iPartsDataPicOrder(getEtkProject(), newPicOrderId);
        newDataPicOrder.assign(project, this, DBActionOrigin.FROM_EDIT);
        setNewId(newDataPicOrder, newPicOrderId);

        // Modul-Verknüpfungen kopieren
        for (iPartsDataPicOrderModules module : getModules()) {
            iPartsDataPicOrderModules newModule = module.cloneMe(getEtkProject());
            iPartsPicOrderModulesId modulesId = new iPartsPicOrderModulesId(changeOrderGUID, module.getAsId().getModuleNo());
            setNewId(newModule, modulesId);
            newDataPicOrder.addModule(newModule, DBActionOrigin.FROM_EDIT);
        }

        // Verwendungen kopieren
        for (iPartsDataPicOrderUsage usage : getUsages()) {
            iPartsDataPicOrderUsage newUsage = usage.cloneMe(getEtkProject());
            iPartsPicOrderUsageId usageId;
            if (usage.getEinPASId() != null) {
                usageId = new iPartsPicOrderUsageId(changeOrderGUID, new iPartsProductId(usage.getAsId().getProductNo()),
                                                    usage.getEinPASId());
            } else if (usage.getKgTuId() != null) {
                usageId = new iPartsPicOrderUsageId(changeOrderGUID, new iPartsProductId(usage.getAsId().getProductNo()),
                                                    usage.getKgTuId());
            } else {
                continue;
            }
            setNewId(newUsage, usageId);
            newDataPicOrder.addUsage(newUsage, DBActionOrigin.FROM_EDIT);
        }

        // Verknüpfung zu mitgegebenen Teilen
        for (iPartsDataPicOrderPart part : getParts()) {
            // Positionen die am vorherigen Auftrag hängen und auf "D" gesetzt wurden, sollen nicht übernommen werden
            if (part.hasDeletedPicturePositionMarker()) {
                continue;
            }
            iPartsDataPicOrderPart newPart = part.cloneMe(getEtkProject());
            iPartsPicOrderPartId picOrderPartId = new iPartsPicOrderPartId(changeOrderGUID, part.getAsId().getPartListEntryId(),
                                                                           part.getHotSpot(), part.getPartNumber());
            setNewId(newPart, picOrderPartId);
            newDataPicOrder.addPart(newPart, DBActionOrigin.FROM_EDIT);
        }

        // Verknüpfung zu den aktuellen Zeichnungen
        for (iPartsDataPicOrderPicture picture : getPictures()) {
            iPartsDataPicOrderPicture newPicture = picture.cloneMe(getEtkProject());
            iPartsPicOrderPictureId pictureId = new iPartsPicOrderPictureId(changeOrderGUID, picture.getAsId().getPicItemId(),
                                                                            picture.getAsId().getPicItemRevId());
            setNewId(newPicture, pictureId);
            newDataPicOrder.addPicture(newPicture, DBActionOrigin.FROM_EDIT);
        }

        // Verknüpfung zu Anhängen
        for (iPartsDataPicOrderAttachment attachment : getAttachments()) {
            iPartsDataPicOrderAttachment newAttachment = attachment.cloneMe(getEtkProject());
            newDataPicOrder.addAttachment(newAttachment, DBActionOrigin.FROM_EDIT);
        }

        return newDataPicOrder;
    }

    private void setNewId(EtkDataObject dbObject, IdWithType newId) {
        dbObject.setId(newId, DBActionOrigin.FROM_EDIT);
        dbObject.setDeleteOldId(false);
    }

    public void addPictures(List<iPartsDataPicOrderPicture> selectedPictures) {
        for (iPartsDataPicOrderPicture picture : selectedPictures) {
            if (!getPictures().containsId(picture.getAsId())) {
                addPicture(picture, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    /**
     * Sobald ein vorheriger Änderungsauftrag zu einem Bildauftrag gültig ist, darf kein neuer erstellt werden
     * Wenn es fehlerhafte oder abgeschlossene Änderungsaufträge zu dem jetzigen Originalbild gibt,
     * die nicht als ungültig markiert sind oder storniert wurden, kann kein weiterer Änderungsauftrag erstellt werden.
     *
     * @return
     */
    public boolean hasOnlyInvalidatedChangeOrder() {
        iPartsDataPicOrderList changeOrderToPicOrderList = iPartsDataPicOrderList.loadChangeOrdersForOriginalPicOrder(getEtkProject(),
                                                                                                                      getAsId().getOrderGuid());

        for (iPartsDataPicOrder changeOrder : changeOrderToPicOrderList) {
            if (changeOrder.isValid() && !changeOrder.isCancelled()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean saveToDB(boolean checkIfPKExistsInDB, PrimaryKeyExistsInDB forcePKExistsInDB) {
        EtkRevisionsHelper revisionsHelper = getEtkProject().getEtkDbs().getRevisionsHelper();
        boolean isRevisionChangeSetForEdit = (revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActiveForEdit();

        // Temporäre Liste, falls mehrere DataObjects gespeichert werden sollen
        EtkDataObjectList tempDataObjectList = new GenericEtkDataObjectList();

        // Referenz Modul zu Bildauftrag müssen ins Changeset, sofern eins aktiv ist
        iPartsDataPicOrderModulesList modulesList = getModules();
        if ((modulesList != null) && !modulesList.isEmpty()) {
            tempDataObjectList.addAll(modulesList, DBActionOrigin.FROM_EDIT);

            // Bei aktivem Changeset auch alle betroffenen Module als geändert markieren und zum Changeset hinzufügen
            if (isRevisionChangeSetForEdit) {
                for (iPartsDataPicOrderModules dataPicOrderModules : modulesList) {
                    AssemblyId assemblyId = new AssemblyId(dataPicOrderModules.getAsId().getModuleNo(), "");
                    EtkDataAssembly tempAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), assemblyId);
                    if (tempAssembly.existsInDB()) {
                        tempAssembly.getAttributes().markAsModified();
                        tempDataObjectList.add(tempAssembly, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            setChildren(CHILDREN_NAME_MODULES, null); // damit die Module (children) nicht auf dem normalen Weg gespeichert werden
        } else {
            // Bei asynchronen Operationen wird das eigentliche Modul nicht gefunden, weil es noch im ChangeSet liegt.
            // Hier also vorher prüfen, ob wir im Edit sind (ChangeSet vorhanden)
            if (isRevisionChangeSetForEdit) {
                AbstractRevisionChangeSet changeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Picture order \""
                                                                       + getAsId().getOrderGuid()
                                                                       + "\" has no referenced assembly to store. Edit " +
                                                                       "changeset: " + changeSet.getChangeSetId());
            }
        }
        // Wurde der Status geändert, dann muss auch der Zeitpunkt gesetzt werden
        if (getAttribute(FIELD_DA_PO_STATUS).isModified()) {
            setFieldValueAsDateTime(FIELD_PO_STATUS_CHANGE_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
        }

        // Erst den Bildauftrag samt Kinder normal in der DB speichern
        boolean saveOK = super.saveToDB(checkIfPKExistsInDB, forcePKExistsInDB);
        if (saveOK) {
            // Bildauftrag und Kinder erfolgreich gespeichert → Check, ob Changeset aktiv
            if (isRevisionChangeSetForEdit) {
                // Changeset aktiv, speichern in Changeset
                revisionsHelper.addDataObjectListToActiveChangeSetForEdit(tempDataObjectList);
            } else {
                // Changeset nicht aktiv, speichern in DB
                tempDataObjectList.saveToDB(getDBProject());
            }
        } else {
            setChildren(CHILDREN_NAME_MODULES, modulesList);
        }
        return saveOK;
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        // Temporäre Liste, falls mehrere DataObjects gelöscht werden sollen
        EtkDataObjectList tempDataObjectList = new GenericEtkDataObjectList();

        // Referenz Modul zu Bildauftrag müssen aus dem Changeset entfernt werden, sofern eins aktiv ist
        iPartsDataPicOrderModulesList modulesList = getModules();
        StringBuilder modules = new StringBuilder();
        if ((modulesList != null) && !modulesList.isEmpty()) {
            for (iPartsDataPicOrderModules moduleData : modulesList) {
                if (modules.length() != 0) {
                    modules.append(", ");
                }
                modules.append(moduleData.getAsId().getModuleNo());
                tempDataObjectList.delete(moduleData, true, DBActionOrigin.FROM_EDIT);
            }
        }

        // Erst den Bildauftrag samt Kinder normal in der DB löschen
        super.deleteFromDB(forceDelete);

        // Bildauftrag und Kinder erfolgreich gelöscht → Lösche die Verknüpfung aus dem Changeset
        EtkRevisionsHelper revisionsHelper = getEtkProject().getEtkDbs().getRevisionsHelper();
        if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActiveForEdit()) {
            revisionsHelper.addDataObjectListToActiveChangeSetForEdit(tempDataObjectList);
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Picture order \""
                                                                   + getAsId().getOrderGuid()
                                                                   + "\" deleted. Module relation \"" + modules + "\" removed from changeset \""
                                                                   + revisionsHelper.getActiveRevisionChangeSetForEdit().getChangeSetId().getGUID() + "\"");
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Picture order \"" + getAsId().getOrderGuid() + "\" deleted");
        }

    }

    /**
     * Liefert den {@link PRODUCT_STRUCTURING_TYPE}
     * des aktuellen Bildauftrags.
     *
     * @return
     */
    public PRODUCT_STRUCTURING_TYPE getProductStructuringType() {
        if (getUsages().size() > 1) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Picture order has more than one usage.");
        }
        iPartsDataPicOrderUsage usage = getUsages().get(0);
        return usage.getProductStructuringType();
    }

    public void setFakeOriginalPicOrder() {
        setFieldValue(iPartsConst.FIELD_DA_PO_ORIGINAL_PICORDER, FAKE_ORIGINAL_PIC_ORDER, DBActionOrigin.FROM_EDIT);
    }

    public boolean hasFakeOriginalPicOrder() {
        return getFieldValue(FIELD_DA_PO_ORIGINAL_PICORDER).equals(FAKE_ORIGINAL_PIC_ORDER);
    }

    /**
     * Liefert zurück, ob der Bildauftrag erfolgreich abgeschlossen wurde.
     *
     * @return
     */
    public boolean finishedSucessfully() {
        // Erfolgreich abgeschlossen bedeutet, dass wir im aktuellen Zustand Änderungsaufträge erzeugen könnten
        return iPartsTransferStates.canRequestChange(getStatus());
    }

    /**
     * Kopiert die Gültigkeiten des Bildes in den Bildauftrag (Code und Ereignisse)
     *
     * @param image
     */
    public boolean copyValuesFromImageToPicOrder(EtkDataImage image) {
        if ((image == null)) {
            return false;
        }
        String code = image.getFieldValue(FIELD_I_CODES);
        String eventFromId = image.getFieldValue(FIELD_I_EVENT_FROM);
        String eventToId = image.getFieldValue(FIELD_I_EVENT_TO);
        boolean onlyFINVisibleImage = image.getFieldValueAsBoolean(FIELD_I_ONLY_FIN_VISIBLE);
        boolean onlyFINVisiblePicOrder = getFieldValueAsBoolean(FIELD_PO_ONLY_FIN_VISIBLE);
        // Wenn mind eine Gültigkeit existiert oder "nur bei FIN ausgeben" unterschiedlich ist, die Werte vom Bild
        // an den Bildauftrag schreiben
        if (!StrUtils.isEmpty(code, eventFromId, eventToId) || (onlyFINVisiblePicOrder != onlyFINVisibleImage)) {
            setFieldValue(FIELD_DA_PO_CODES, code, DBActionOrigin.FROM_EDIT);
            setFieldValue(FIELD_DA_PO_EVENT_FROM, eventFromId, DBActionOrigin.FROM_EDIT);
            setFieldValue(FIELD_DA_PO_EVENT_TO, eventToId, DBActionOrigin.FROM_EDIT);
            setFieldValueAsBoolean(FIELD_PO_ONLY_FIN_VISIBLE, onlyFINVisibleImage, DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }
}
