/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataObjectWithPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Viewer Representation eines kompletten {@link SerializedDBDataObject}s.
 */
public class iPartsChangeSetViewerElem {

    /**
     * Überprüft, ob ein Module in der Liste von {@link iPartsChangeSetSearchElem}s enthalten ist.
     * Dabei kann eine excludeChangeSetId {@link iPartsChangeSetId} übergeben werden
     *
     * @param viewerElemList
     * @param assemblyId
     * @param excludeChangeSetId
     * @return
     */
    public static iPartsChangeSetSearchElem containsAssemblyId(List<iPartsChangeSetSearchElem> viewerElemList, AssemblyId assemblyId,
                                                               iPartsChangeSetId excludeChangeSetId) {
        if ((assemblyId != null) && assemblyId.isValidId()) {
            for (iPartsChangeSetSearchElem viewerElem : viewerElemList) {
                if (viewerElem.getType().equals(AssemblyId.TYPE) && viewerElem.createId().equals(assemblyId)) {
                    if (excludeChangeSetId != null) {
                        if (!excludeChangeSetId.equals(viewerElem.getChangeSetId())) {
                            return viewerElem;
                        }
                    } else {
                        return viewerElem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Liefert alle in in der Liste von {@link iPartsChangeSetSearchElem} eingetragenen Module als Set von {@link AssemblyId}s
     *
     * @param viewerElemList
     * @return
     */
    public static Set<AssemblyId> getAssemblyListFromViewerList(List<iPartsChangeSetSearchElem> viewerElemList) {
        Set<AssemblyId> result = new TreeSet<AssemblyId>();
        for (iPartsChangeSetViewerElem viewerElem : viewerElemList) {
            IdWithType id = viewerElem.createId();
            AssemblyId assemblyId = new AssemblyId(id.getValue(1), id.getValue(2));
            result.add(assemblyId);
        }
        return result;
    }

    /**
     * Liefert alle in aktiven ChangeSets eingetragenen Module als Set von {@link AssemblyId}s
     *
     * @param project
     * @return
     */
    public static Set<AssemblyId> getAssemblyListFromFromActiveChangeSets(EtkProject project) {
        return getAssemblyListFromViewerList(getAssemblyListFromActiveChangeSets(project));
    }

    /**
     * Liefert die in allen aktiven ChangeSets eingetragenen Module als Liste von {@link iPartsChangeSetSearchElem}
     *
     * @param project
     * @return
     */
    public static List<iPartsChangeSetSearchElem> getAssemblyListFromActiveChangeSets(EtkProject project) {
        List<iPartsChangeSetSearchElem> result = new DwList<>();
        iPartsDataChangeSetList changeSetList = iPartsDataChangeSetList.loadChangeSetsForSourceAndStatus(project, iPartsChangeSetSource.AUTHOR_ORDER,
                                                                                                         iPartsChangeSetStatus.IN_PROCESS);
        for (iPartsDataChangeSet changeSet : changeSetList) {
            iPartsRevisionChangeSet changeSetRevision = new iPartsRevisionChangeSet(changeSet.getAsId(), project);
            for (SerializedDBDataObject serializedDBDataObject : changeSetRevision.getSerializedDataObjectsMap().values()) {
                if ((serializedDBDataObject != null) && !serializedDBDataObject.isRevertedWithoutKeepState()) {
                    if (serializedDBDataObject.getType().equals(AssemblyId.TYPE)) {
                        result.add(new iPartsChangeSetSearchElem(serializedDBDataObject, changeSet.getAsId(), project));
                    }
                }
            }
        }
        return result;
    }

    private String tableName;
    private String type;
    private String[] pkValues;
    private String timeStamp;
    private SerializedDBDataObjectState state;
    private String userId;
    private String dateTime;
    private String viewingId;
    private String picOrderState; // StatusWert eines Bildauftrags (als DBWert)
    private String mediaContainer;
    private String viewingDescription; // Objektbenennung im Info-Grid

    public iPartsChangeSetViewerElem(SerializedDBDataObject serializedDBDataObject, EtkProject project) {
        this.tableName = serializedDBDataObject.getTableName();
        this.type = serializedDBDataObject.getType();
        this.pkValues = serializedDBDataObject.getPkValues();
        this.timeStamp = serializedDBDataObject.getTimeStamp();
        this.state = serializedDBDataObject.getState();
        this.userId = serializedDBDataObject.getUserIdWithFallback();
        this.dateTime = serializedDBDataObject.getDateTime();
        this.viewingId = "";
        this.picOrderState = "";
        this.mediaContainer = "";
        this.viewingDescription = "";
        IdWithType id = serializedDBDataObject.createId();
        if (isAssembly()) {
            initAssembly(project, id);
        } else if (isPart()) {
            initPart(project, id);
        } else if (isPictureOrder()) {
            initPictureOrderData(project, id);
        }
    }

    /**
     * Initialisiert das Element als Assembly-Element
     *
     * @param project
     * @param id
     */
    private void initAssembly(EtkProject project, IdWithType id) {
        AssemblyId assemblyId = new AssemblyId(id.getValue(1), id.getValue(2));
        viewingId = project.getVisObject().asText(this.tableName, EtkDbConst.FIELD_K_VARI, assemblyId.getKVari(), project.getViewerLanguage());
    }

    /**
     * Initialisiert das Element als Teil-Element
     *
     * @param project
     * @param id
     */
    private void initPart(EtkProject project, IdWithType id) {
        iPartsPartId partId = new iPartsPartId(id.getValue(1), id.getValue(2));
        viewingId = project.getVisObject().asText(this.tableName, EtkDbConst.FIELD_M_BESTNR, partId.getMatNr(), project.getViewerLanguage());
        if (!partId.getMVer().isEmpty()) {
            viewingId += ", " + project.getVisObject().asText(this.tableName, EtkDbConst.FIELD_M_VER, partId.getMVer(), project.getViewerLanguage());
        }
    }

    /**
     * Initialisiert das Element als Bildauftrag-Element. Weil hier nur die Bildauftrag GUID und die Modulnummer im
     * Changeset vorhanden sind, wird der Bildauftrag komplett geladen und ausgelesen.
     *
     * @param project
     * @param id
     */
    private void initPictureOrderData(EtkProject project, IdWithType id) {
        iPartsDataPicOrder picOrder = new iPartsDataPicOrder(project, new iPartsPicOrderId(id.getValue(1)));
        if (picOrder.existsInDB()) {
            String picOrderIdExtern = picOrder.getOrderIdExtern();
            iPartsTransferStates status = picOrder.getStatus();
            picOrderState = "";
            if (status != null) {
                picOrderState = status.getDBValue();
            }
            if (StrUtils.isValid(picOrderIdExtern)) {
                mediaContainer = picOrderIdExtern;
                viewingId = StrUtils.makeDelimitedString(" / ", id.getValue(2), picOrderIdExtern + " " + picOrder.getOrderRevisionExtern());
            } else {
                String statusText = "(" + project.getEnumText(iPartsConst.ENUM_KEY_PICORDER_STATES, picOrderState, project.getViewerLanguage(), true) + ")";
                viewingId = id.getValue(2) + " " + statusText;
            }
            viewingDescription = picOrder.getProposedName();
        } else {
            viewingId = id.toStringForLogMessages();
        }
    }

    // ===== Getter =====//
    public String getTableName() {
        return tableName;
    }

    public String getType() {
        return type;
    }

    public boolean isAssembly() {
        return type.equals(AssemblyId.TYPE);
    }

    public boolean isPart() {
        return type.equals(PartId.TYPE);
    }

    public boolean isPictureOrder() {
        return type.equals(iPartsPicOrderModulesId.TYPE);
    }

    public String getVisualType() {
        if (isAssembly()) {
            return TranslationHandler.translate("!!AS-Stückliste");
        } else if (isPart()) {
            return TranslationHandler.translate("!!Teilestamm");
        } else if (isPictureOrder()) {
            return TranslationHandler.translate("!!Bildauftrag");
        }
        return "";
    }

    public String[] getPkValues() {
        return pkValues;
    }

    public String getViewingId() {
        return viewingId;
    }

    public String getDescription(EtkProject project) {
        if (isAssembly() || isPart()) {
            if (StrUtils.isEmpty(viewingDescription)) {
                IdWithType id = createId();
                if (state == SerializedDBDataObjectState.DELETED) {
                    // Benennung kann nur im ChangeSet im SerializedDBDataObject gefunden werden
                    EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
                    if (revisionsHelper != null) {
                        AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
                        if (activeChangeSet != null) {
                            SerializedDBDataObject deletedDbDataObject = activeChangeSet.getSerializedDataObject(id);
                            if (deletedDbDataObject != null) {
                                if (isAssembly()) { // Bei einer Baugruppe ist die Benennung am Material als CompositeChild gespeichert
                                    if (deletedDbDataObject.getCompositeChildren() != null) {
                                        for (SerializedDBDataObjectList<SerializedDBDataObject> serializedDBDataObjectList : deletedDbDataObject.getCompositeChildren()) {
                                            if (serializedDBDataObjectList.getChildName().equals(EtkDataObjectWithPart.AGGREGATE_NAME_PART)) {
                                                if (!serializedDBDataObjectList.isEmpty()) {
                                                    deletedDbDataObject = serializedDBDataObjectList.getList().get(0);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                SerializedDBDataObjectAttribute attribute = deletedDbDataObject.getAttribute(EtkDbConst.FIELD_M_TEXTNR);
                                if (attribute != null) {
                                    SerializedEtkMultiSprache multiLanguage = attribute.getMultiLanguage();
                                    if (multiLanguage != null) {
                                        EtkMultiSprache etkMultiLang = multiLanguage.createMultiLanguage(EtkDataObject.getExtendedDataTypeProviderForTextIds(project));
                                        viewingDescription = etkMultiLang.getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                   project.getDataBaseFallbackLanguages());
                                        if (StrUtils.isValid(viewingDescription)) {
                                            return viewingDescription;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Nichts gefunden
                    viewingDescription = "??";
                } else {
                    iPartsPartId partId = new iPartsPartId(id.getValue(1), id.getValue(2));
                    if (!iPartsVirtualNode.isVirtualId(partId)) { // Virtuelle Materialien können nicht aus der DB geladen werden
                        EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(project, partId);
                        if (dataPart.loadFromDB(partId)) {
                            viewingDescription = dataPart.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR).getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
                        } else {
                            viewingDescription = "??";
                        }
                    }
                }
            }
            return viewingDescription;
        } else if (isPictureOrder()) {
            return viewingDescription;
        }
        return "";
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public SerializedDBDataObjectState getState() {
        return state;
    }

    public String getUserId() {
        return userId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getPicOrderState() {
        return picOrderState;
    }

    public String getMediaContainer() {
        return mediaContainer;
    }

// ===== Getter End =====//

    /**
     * Erzeugt aus dem {{@link #type}} und den {@link #pkValues} eine {@link IdWithType}.
     *
     * @return
     */
    public IdWithType createId() {
        if (getPkValues() != null) {
            return new IdWithType(getType(), getPkValues());
        } else {
            return null;
        }
    }

    /**
     * Erweiterung des {@link iPartsChangeSetViewerElem}s um die {@link iPartsChangeSetId}.
     */
    public static class iPartsChangeSetSearchElem extends iPartsChangeSetViewerElem {

        private iPartsChangeSetId changeSetId;

        public iPartsChangeSetSearchElem(SerializedDBDataObject serializedDBDataObject, iPartsChangeSetId changeSetId, EtkProject project) {
            super(serializedDBDataObject, project);
            this.changeSetId = changeSetId;
        }

        public iPartsChangeSetId getChangeSetId() {
            return changeSetId;
        }
    }
}
