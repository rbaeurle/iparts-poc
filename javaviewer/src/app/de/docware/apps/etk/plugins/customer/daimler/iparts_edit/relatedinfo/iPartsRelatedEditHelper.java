/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.restrictions.RestrictionsHandlers;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDialogChangesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Hilfsklasse für RelatedEdit-Seiten in iParts.
 */
public class iPartsRelatedEditHelper {

    /**
     * Muss aufgerufen werden, wenn sich der Status von den übergebenen {@link EtkDataObject}s geändert hat, damit die
     * notwendigen Änderungen in der Tabelle {@code DA_DIALOG_CHANGES} nachgezogen werden können.
     *
     * @param dataObjectsWithChangedStatus
     * @param relatedInfoForm
     */
    public static void statusChanged(final EtkDataObjectList<? extends EtkDataObject> dataObjectsWithChangedStatus,
                                     final AbstractRelatedInfoPartlistDataForm relatedInfoForm, final boolean isReplacement) {
        final ChangeSetId changeSetId = relatedInfoForm.getAuthorOrderChangeSetId();
        if (changeSetId != null) {
            // Speichern in der DB erst beim Speichern der RelatedEdit machen
            relatedInfoForm.addSaveEditRunnable(new Runnable() {
                @Override
                public void run() {
                    // Direkt in der DB das aktuelle Edit-ChangeSet vom Autoren-Auftrag in Datensätzen von DA_DIALOG_CHANGES setzen
                    final iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
                    final Set<iPartsDialogChangesId> dialogChangesIdsInActiveAuthorOrder = new HashSet<>();
                    final AbstractRevisionChangeSet changeSetForEdit;
                    EtkRevisionsHelper revisionsHelper = relatedInfoForm.getRevisionsHelper();
                    if (revisionsHelper != null) {
                        changeSetForEdit = revisionsHelper.getActiveRevisionChangeSetForEdit();
                    } else {
                        changeSetForEdit = null;
                    }

                    // Die DialogChanges-Einträge noch mit aktivem Changeset von der RelatedEdit abfragen, da es
                    // sich insbesondere bei Ersetzungen auch um Einträge handeln kann, die nur im Changeset existieren
                    String sourceFieldName = relatedInfoForm.getSourceFieldName();
                    final Map<IdWithType, List<iPartsDataDIALOGChange>> dataDIALOGChangesMap = new HashMap<>();
                    for (EtkDataObject dataObjectWithChangedStatus : dataObjectsWithChangedStatus) {
                        // Replacements unabhängig von der Quelle behandeln
                        if ((sourceFieldName == null) || dataObjectWithChangedStatus.getFieldValue(sourceFieldName).equals(iPartsImportDataOrigin.DIALOG.getOrigin())
                            || dataObjectWithChangedStatus.getFieldValue(sourceFieldName).equals(iPartsImportDataOrigin.IPARTS.getOrigin()) || isReplacement) {
                            dataDIALOGChangesMap.put(dataObjectWithChangedStatus.getAsId(), relatedInfoForm.getDataDIALOGChanges(dataObjectWithChangedStatus));
                        }
                    }

                    relatedInfoForm.getProject().executeWithoutActiveChangeSets(new Runnable() {
                        @Override
                        public void run() {
                            String changeSetGUID = changeSetId.getGUID();
                            String conflictingAuthorOrderName = "";
                            String conflictingAuthorOrderUser = "";
                            Set<IdWithType> conflictingIDs = new LinkedHashSet<>();
                            for (Map.Entry<IdWithType, List<iPartsDataDIALOGChange>> dialogChanges : dataDIALOGChangesMap.entrySet()) {
                                IdWithType dataObjectIdWithChangedStatus = dialogChanges.getKey();
                                List<iPartsDataDIALOGChange> dataDIALOGChanges = dialogChanges.getValue();
                                for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChanges) {
                                    if (dataDIALOGChange != null) {
                                        if (dataDIALOGChange.existsInDB()) {
                                            String dbChangeSetGUID = dataDIALOGChange.getFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID);
                                            if (dbChangeSetGUID.isEmpty()) { // Bearbeitung möglich
                                                dataDIALOGChange.setFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID, changeSetGUID,
                                                                               DBActionOrigin.FROM_EDIT);
                                                dataDIALOGChange.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
                                                dataDIALOGChangeList.delete(dataDIALOGChange, true, DBActionOrigin.FROM_EDIT);
                                            } else if (!dbChangeSetGUID.equals(changeSetGUID)) { // Parallele Bearbeitung
                                                if (conflictingAuthorOrderUser.isEmpty()) {
                                                    iPartsDataAuthorOrder authorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSetId(relatedInfoForm.getProject(),
                                                                                                                                              new iPartsChangeSetId(dbChangeSetGUID));
                                                    if (authorOrder != null) {
                                                        conflictingAuthorOrderName = authorOrder.getAuthorOrderName();
                                                        conflictingAuthorOrderUser = authorOrder.getCurrentUser();
                                                        if (StrUtils.isEmpty(conflictingAuthorOrderUser)) {
                                                            conflictingAuthorOrderUser = authorOrder.getCreationUser();
                                                        }
                                                    }
                                                }
                                                conflictingIDs.add(dataObjectIdWithChangedStatus);
                                                if (changeSetForEdit != null) { // Änderungen im ChangeSet rückgängig machen
                                                    changeSetForEdit.setSerializedDataObject(dataObjectIdWithChangedStatus, null);
                                                }
                                            }
                                        } else {
                                            // Wenn der DialogChanges-Eintrag nicht in der DB existiert, dann muss es einer sein, der
                                            // im aktuellen Changeset angelegt wurde. Dieser muss dann mit aktivem Autorenauftrag nochmal
                                            // geprüft werden
                                            dialogChangesIdsInActiveAuthorOrder.add(dataDIALOGChange.getAsId());
                                        }
                                    }
                                }
                            }

                            // Konflikte durch parallele Änderungen melden
                            if (!conflictingIDs.isEmpty()) {
                                String conflictingString = "";
                                for (IdWithType conflictingID : conflictingIDs) {
                                    conflictingString += "\n- " + TranslationHandler.translate(conflictingID.getDescription())
                                                         + " " + conflictingID.toStringForLogMessages();
                                }

                                MessageDialog.showWarning(TranslationHandler.translate("!!Die folgenden Datensätze wurden vom Benutzer \"%1\" parallel in einem anderen Autoren-Auftrag \"%2\" bearbeitet:",
                                                                                       conflictingAuthorOrderUser, conflictingAuthorOrderName) + conflictingString + "\n\n"
                                                          + TranslationHandler.translate("Die gerade vorgenommenen Änderungen mussten daher rückgängig gemacht werden."));

                            }
                        }
                    }, false);

                    if (isReplacement) {
                        handleReplacments(dataObjectsWithChangedStatus, sourceFieldName, changeSetForEdit, relatedInfoForm);
                    }


                    // Alle DialogChanges, die potentiell nur in Autorenaufträgen existieren, nochmal prüfen und ggf. löschen
                    if (!dialogChangesIdsInActiveAuthorOrder.isEmpty()) {
                        String changeSetGUID = changeSetId.getGUID();
                        for (iPartsDialogChangesId dataDIALOGChangeId : dialogChangesIdsInActiveAuthorOrder) {
                            iPartsDataDIALOGChange dataDIALOGChange = new iPartsDataDIALOGChange(relatedInfoForm.getProject(),
                                                                                                 dataDIALOGChangeId);
                            if (dataDIALOGChange.existsInDB()) {
                                String dbChangeSetGUID = dataDIALOGChange.getFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID);
                                if (dbChangeSetGUID.isEmpty() || dbChangeSetGUID.equals(changeSetGUID)) { // Bearbeitung möglich
                                    dataDIALOGChangeList.delete(dataDIALOGChange, true, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }

                    // Im ChangeSet die entsprechenden Datensätze von DA_DIALOG_CHANGES löschen
                    if (dataDIALOGChangeList.isModifiedWithChildren()) {
                        relatedInfoForm.addDataObjectListToActiveChangeSetForEdit(dataDIALOGChangeList);
                    }
                }
            });
        }
    }

    /**
     * Verarbeitet Änderungen an Ersetzungen, z.B. VTNV Vererbung
     *
     * @param dataObjectsWithChangedStatus
     * @param sourceFieldName
     * @param changeSetForEdit
     * @param relatedInfoForm
     */
    private static void handleReplacments(EtkDataObjectList<? extends EtkDataObject> dataObjectsWithChangedStatus,
                                          String sourceFieldName, AbstractRevisionChangeSet changeSetForEdit,
                                          AbstractRelatedInfoPartlistDataForm relatedInfoForm) {
        // Bei Dialog und Ersetzungen mit Status neu
        // BCTE- Schlüssel zum Vorgänger und Nachfolger ermitteln. Dann Ersetzungen in DA_REPLACE_PARTS ermitteln, die zu den BCTE-Schlüssel (AA unabhängig)
        // einen Vorgänger und Nachfolger besitzen
        GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();
        for (EtkDataObject dataObjectWithChangedStatus : dataObjectsWithChangedStatus) {
            if (dataObjectWithChangedStatus.getFieldValue(sourceFieldName).equals(iPartsReplacement.Source.VTNV.getDbValue())) {
                // Falls eine IPARTS Ersetzung zur VTNV Ersetzung besteht, darf der Status nicht weitervererbt werden
                // IPARTS Ersetzung und die dazugehörige VTNV Ersetzung sind Ersetzungen zum selben Stücklisteneintrag
                if (isSameReplacementAndNotDuplicateOf(dataObjectWithChangedStatus, dataObjectsWithChangedStatus)) {
                    continue;
                }
                String bcteKeyPredecessor = dataObjectWithChangedStatus.getFieldValue(iPartsConst.FIELD_DRP_SOURCE_GUID);
                String bcteKeySuccessor = dataObjectWithChangedStatus.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_SOURCE_GUID);
                if (StrUtils.isValid(bcteKeyPredecessor, bcteKeySuccessor)) {
                    if (changeSetForEdit != null) {
                        // der neue Status ist schon im Changeset gespeichert, deswegen muss man über das Changeset an den alten Status kommen
                        // Ist dieser Neu wird der neue Status weitervererbt
                        String oldStatus = changeSetForEdit.getSerializedDataObject(dataObjectWithChangedStatus.getAsId()).getAttribute(iPartsConst.FIELD_DRP_STATUS).getOldValue();
                        if (Utils.objectEquals(oldStatus, iPartsDataReleaseState.NEW.getDbValue())) {
                            iPartsDataReplacePartList list = iPartsDataReplacePartList.loadReplacementsWithSuccessorAndPredecessorGUID(relatedInfoForm.getProject(), bcteKeyPredecessor,
                                                                                                                                       bcteKeySuccessor);
                            String newStatus = dataObjectWithChangedStatus.getFieldValue(iPartsConst.FIELD_DRP_STATUS);
                            String dataObjectWithChangedStatusKVari = dataObjectWithChangedStatus.getFieldValue(iPartsConst.FIELD_DRP_VARI);
                            for (iPartsDataReplacePart replacement : list) {
                                // auch nur auf VTNV Ersetzungen vererben und wenn es keine dazugehörige IPARTS Ersetzung gibt
                                if (replacement.getFieldValue(sourceFieldName).equals(iPartsReplacement.Source.VTNV.getDbValue())) {
                                    if (isSameReplacementAndNotDuplicateOf(replacement, list)) {
                                        continue;
                                    }
                                    if (!replacement.getAsId().equals(dataObjectWithChangedStatus.getAsId())) {
                                        String replacementStatus = replacement.getFieldValue(iPartsConst.FIELD_DRP_STATUS);
                                        if (replacementStatus.equals(iPartsDataReleaseState.NEW.getDbValue())) {
                                            replacement.setFieldValue(iPartsConst.FIELD_DRP_STATUS, newStatus, DBActionOrigin.FROM_EDIT);
                                            changeSetDataObjectList.add(replacement, DBActionOrigin.FROM_EDIT);
                                            // Fremde TUs als geändert markieren
                                            markModifiedTUAsChanged(replacement, dataObjectWithChangedStatusKVari, relatedInfoForm);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (changeSetDataObjectList.isModifiedWithChildren()) {
            relatedInfoForm.addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);
        }
    }


    /**
     * Prüft, ob die übergebene Ersetzung gleich einer Ersetzung aus der übergebenen Liste ist ohne ein Dublikat davon
     * zu sein.
     *
     * @param replacePartToTest
     * @param list
     * @return
     */
    private static boolean isSameReplacementAndNotDuplicateOf(EtkDataObject replacePartToTest,
                                                              EtkDataObjectList<? extends EtkDataObject> list) {
        boolean isSameReplacement = false;
        iPartsDataReplacePart replacePartWithChangedStatus = (iPartsDataReplacePart)replacePartToTest;
        for (EtkDataObject otheDataObject : list) {
            iPartsDataReplacePart otherReplacePartObject = (iPartsDataReplacePart)otheDataObject;
            if (otherReplacePartObject.isSameReplacement(replacePartWithChangedStatus) && !otherReplacePartObject.isDuplicateOf(replacePartWithChangedStatus)) {
                isSameReplacement = true;
                break;
            }
        }
        return isSameReplacement;
    }

    /**
     * Markiert den TU zur übergebenen Ersetzung als "geändert" sofern es nicht der TU der eigentlichen Ersetzung ist
     *
     * @param replacement
     * @param dataObjectWithChangedStatusKVari
     * @param relatedInfoForm
     */
    private static void markModifiedTUAsChanged(iPartsDataReplacePart replacement, String dataObjectWithChangedStatusKVari,
                                                AbstractRelatedInfoPartlistDataForm relatedInfoForm) {
        String replacementKVari = replacement.getFieldValue(iPartsConst.FIELD_DRP_VARI);
        if (!replacementKVari.equals(dataObjectWithChangedStatusKVari)) {
            AssemblyId assemblyId = new AssemblyId(replacementKVari, "");
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(relatedInfoForm.getProject(), assemblyId);
            if (assembly.existsInDB()) {
                assembly.markAssemblyInChangeSetAsChanged();
            }
        }
    }

    public static <E extends EtkDataObject> EtkDataObjectList<E> updateStatusValuesVariants(EtkDataObjectList<E> selectedDataObjects,
                                                                                            List<E> allDataObjects, String adatFieldName,
                                                                                            String statusFieldName, boolean isColorTableProductionData) {
        return updateStatusValues(selectedDataObjects, allDataObjects, adatFieldName, "", statusFieldName, true,
                                  isColorTableProductionData);
    }

    public static <E extends EtkDataObject> EtkDataObjectList<E> updateStatusValuesResponseAndSpikes(EtkDataObjectList<E> selectedDataObjects,
                                                                                                     List<E> allDataObjects, String adatFieldName,
                                                                                                     String statusFieldName) {
        return updateStatusValues(selectedDataObjects, allDataObjects, adatFieldName, "", statusFieldName, false,
                                  false);
    }

    public static <E extends EtkDataObject> EtkDataObjectList<E> updateStatusValuesFactoryData(EtkDataObjectList<E> selectedDataObjects,
                                                                                               List<E> allDataObjects, String adatFieldName,
                                                                                               String seqNoFieldName, String statusFieldName) {
        return updateStatusValues(selectedDataObjects, allDataObjects, adatFieldName, seqNoFieldName, statusFieldName, true,
                                  false);
    }

    /**
     * Für jedes Datenobjekt in {@param selectedDataObjects}:
     * Bei Änderung vom Status auf "freigegeben" alle Datenobjekte mit älterem ADAT und Status "neu" suchen und bei diesen den Status
     * auf "nicht relevant" setzen, bzw. bei Änderung auf Status "gelöscht" alle Datenobjekte mit älterem ADAT suchen.
     *
     * @param selectedDataObjects
     * @param allDataObjects
     * @param adatFieldName
     * @param statusFieldName
     * @return Alle Datenobjekte, an denen der Status geändert wurde
     */
    public static <E extends EtkDataObject> EtkDataObjectList<E> updateStatusValues(EtkDataObjectList<E> selectedDataObjects,
                                                                                    List<E> allDataObjects, String adatFieldName, String seqNoFieldName,
                                                                                    String statusFieldName, boolean forFactoryData, boolean isColorTableProductionData) {
        EtkDataObjectList<E> changedDataObjects = new GenericEtkDataObjectList<>();
        for (E selectedObject : selectedDataObjects) {
            changedDataObjects.add(selectedObject, DBActionOrigin.FROM_EDIT);
            // Abhängig vom neuen Status müssen die vorherigen Datensätze angepasst werden
            iPartsDataReleaseState releaseState = iPartsDataReleaseState.getTypeByDBValue(selectedObject.getFieldValue(statusFieldName));
            if (releaseState.isFinalReleaseState()) {
                for (E otherDataObject : allDataObjects) {
                    if (otherDataObject != selectedObject) {
                        boolean isNotRelevant = isNotRelevant(selectedObject, otherDataObject, adatFieldName, seqNoFieldName, statusFieldName,
                                                              (releaseState == iPartsDataReleaseState.DELETED), forFactoryData,
                                                              isColorTableProductionData);
                        if (isNotRelevant) {
                            // Status auf "nicht relevant" setzen
                            otherDataObject.setFieldValue(statusFieldName, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(),
                                                          DBActionOrigin.FROM_EDIT);
                            changedDataObjects.add(otherDataObject, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
        }
        return changedDataObjects;
    }


    /**
     * Das gleiche wie updateStatusValues nur eine andere "nicht relevant" Auswertung für Ersetzungen
     *
     * @param selectedDataObjects
     * @param allDataObjects
     * @param statusFieldName
     * @param <E>
     * @return
     */
    public static <E extends EtkDataObject> EtkDataObjectList<E> updateStatusValuesForReplacements(EtkDataObjectList<E> selectedDataObjects,
                                                                                                   List<E> allDataObjects, String statusFieldName) {
        EtkDataObjectList<E> changedDataObjects = new GenericEtkDataObjectList<>();
        for (E selectedObject : selectedDataObjects) {
            changedDataObjects.add(selectedObject, DBActionOrigin.FROM_EDIT);
            // Abhängig vom neuen Status müssen die vorherigen Datensätze angepasst werden
            iPartsDataReleaseState releaseState = iPartsDataReleaseState.getTypeByDBValue(selectedObject.getFieldValue(statusFieldName));
            if (releaseState.isFinalReleaseState()) {
                for (E otherDataObject : allDataObjects) {
                    if (otherDataObject != selectedObject) {
                        iPartsReplacement.Source otherDataObjectSource = iPartsReplacement.Source.getFromDBValue(otherDataObject.getFieldValue(iPartsConst.FIELD_DRP_SOURCE));
                        if (otherDataObjectSource != iPartsReplacement.Source.PRIMUS) {
                            boolean isNotRelevant = isNotRelevant(selectedObject, otherDataObject, statusFieldName);
                            if (isNotRelevant) {
                                // Status auf "nicht relevant" setzen
                                otherDataObject.setFieldValue(statusFieldName, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(),
                                                              DBActionOrigin.FROM_EDIT);
                                changedDataObjects.add(otherDataObject, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }
                }
            }
        }
        return changedDataObjects;
    }


    /**
     * Liefert zurück, ob der Datensatz {@param otherDataObject} auf "nicht relevant" gesetzt werden muss, weil er ein früheres
     * ADAT hat, aber ansonsten vom Primärschlüssel her gleich ist.
     * Bei Werkseinsatzdaten muss auch die Sequenznummer aus der PK Prüfung rausgenommen werden, da Datensätze mit gleichem Adat,
     * aber anderem Inhalt durch die steigende Sequenznummer abgebildet werden.
     *
     * @param dataObject
     * @param otherDataObject
     * @param adatFieldName
     * @param statusFieldName
     * @param forDeletion
     * @param isColorTableProductionData
     * @return
     */
    private static boolean isNotRelevant(EtkDataObject dataObject, EtkDataObject otherDataObject, String adatFieldName, String seqNoFieldName,
                                         String statusFieldName, boolean forDeletion, boolean forFactoryData,
                                         boolean isColorTableProductionData) {
        // Prüfung ob alle Primärschlüsselwerte gleich sind, bis auf ADAT
        boolean isSameWithoutADAT = true;
        for (int i = 0; i < dataObject.getPKFields().length; i++) {
            String pkFieldName = dataObject.getPKFields()[i];
            // Bei Werkseinsatzdaten darf noch die Sequenznummer verschieden sein und es gilt trotzdem als gleicher Datensatz
            if (!pkFieldName.equals(adatFieldName) && !pkFieldName.equals(seqNoFieldName)) {
                String pkValue = dataObject.getFieldValue(pkFieldName);
                String otherPkValue = otherDataObject.getFieldValue(pkFieldName);
                if (!pkValue.equals(otherPkValue)) {
                    isSameWithoutADAT = false;
                    break;
                }
            }
        }

        boolean isOlderDuplicate = false;
        if (isSameWithoutADAT) {
            // Werkseinsatzdaten aus der Produktion müssen nach ihrem Original SDA sortiert werden. Haben zwei Datensätze
            // das gleiche Original-SDA, dann muss nach ihrem ADATs sortiert werden
            if (forFactoryData && isColorTableProductionData) {
                String originalSDADataObject = dataObject.getFieldValue(iPartsConst.FIELD_DCCF_ORIGINAL_SDATA);
                String originalSDAOtherDataObject = otherDataObject.getFieldValue(iPartsConst.FIELD_DCCF_ORIGINAL_SDATA);
                int compareResult = originalSDAOtherDataObject.compareTo(originalSDADataObject);
                if (compareResult == 0) {
                    compareResult = otherDataObject.getFieldValue(adatFieldName).compareTo(dataObject.getFieldValue(adatFieldName));
                }
                isOlderDuplicate = compareResult < 0;
            } else {
                isOlderDuplicate = (otherDataObject.getFieldValue(adatFieldName).compareTo(dataObject.getFieldValue(adatFieldName)) < 0);
            }
        }
        if (forDeletion) {
            // Wenn der Status auf "gelöscht" gesetzt wird, dann setze alle älteren Datensätze auf "nicht relevant",
            // unabhängig vom vorherigen Status
            // Prüfung: Schlüssel identisch und älteres ADAT?
            return isOlderDuplicate;
        } else {
            iPartsDataReleaseState currentStatus = iPartsDataReleaseState.getTypeByDBValue(otherDataObject.getFieldValue(statusFieldName));
            if (forFactoryData) { // für Werkseinsatzdaten (auch Varianten)
                // Wenn der Status auf "freigegeben" gesetzt wird, dann setze alle Datensätze mit gleichem Schlüssel, die "neu"
                // oder "freigegeben" waren auf "nicht relevant" unabhängig vom ADAT
                // Prüfung: Schlüssel identisch, Status="neu" oder "freigegeben"?
                return isSameWithoutADAT && ((currentStatus == iPartsDataReleaseState.NEW) || (currentStatus == iPartsDataReleaseState.RELEASED));
            } else { // für Rückmeldedaten und Ausreißer
                // Wenn der Status auf "freigegeben" gesetzt wird, dann setze alle Datensätze mit gleichem Schlüssel, die "neu"
                // waren und ein älteres ADAT haben auf "nicht relevant"
                // Schlüssel identisch, Status="neu" und älteres ADAT?
                return isOlderDuplicate && (currentStatus == iPartsDataReleaseState.NEW);
            }
        }
    }

    /**
     * Für Ersetzungen
     * Liefert zurück, ob der Datensatz auf nicht relevant gesetzt werden muss, weil der Vorgänger/Nachfolger identisch ist
     *
     * @param dataObject
     * @param otherDataObject
     * @return
     */
    private static boolean isNotRelevant(EtkDataObject dataObject, EtkDataObject otherDataObject, String statusFieldName) {
        if ((dataObject instanceof iPartsDataReplacePart) && (otherDataObject instanceof iPartsDataReplacePart)) {
            iPartsDataReplacePart dataObjectReplacePart = (iPartsDataReplacePart)dataObject;
            iPartsDataReplacePart otherDataReplacePart = (iPartsDataReplacePart)otherDataObject;
            iPartsDataReleaseState currentStatus = iPartsDataReleaseState.getTypeByDBValue(otherDataObject.getFieldValue(statusFieldName));
            // Gleicher Vorgänger und Nachfolger, aber nicht die gleiche Ersetzung
            if (dataObjectReplacePart.isSameReplacement(otherDataReplacePart) && !dataObjectReplacePart.isDuplicateOf(otherDataReplacePart)) {
                return (currentStatus == iPartsDataReleaseState.NEW) || (currentStatus == iPartsDataReleaseState.RELEASED) || (currentStatus == iPartsDataReleaseState.CHECK_NOT_RELEVANT);
            }
        }
        return false;
    }

    /**
     * Erzeugt falls notwendig einen {@link EditModuleFormIConnector} aus dem übergebenen {@code dataConnector} und setzt
     * die DisplayFields im {@link EditModuleFormIConnector} basierend auf der übergebenen {@link EtkDataAssembly}.
     *
     * @param dataConnector
     * @param assembly
     * @return
     */
    public static EditModuleFormIConnector createEditConnector(AbstractJavaViewerFormIConnector dataConnector, EtkDataAssembly assembly) {
        EditModuleFormIConnector editConnector;
        if (!(dataConnector instanceof EditModuleFormIConnector)) {
            editConnector = new EditModuleFormConnector(dataConnector);
        } else {
            editConnector = (EditModuleFormIConnector)dataConnector;
        }
        if (editConnector.getAssemblyListDisplayFields() == null) {
            editConnector.setCurrentAssembly(assembly);

            List<EtkDisplayField> result = new ArrayList<>();
            String ebeneStr = assembly.getEbeneName();
            if (ebeneStr.length() > 0) {
                EtkEbenenDaten ebene = dataConnector.getConfig().getPartsDescription().getEbene(ebeneStr);
                if (ebene != null) {
                    for (EtkDisplayField field : ebene.getFields()) {
                        if (field.isVisible()) {
                            if (RestrictionsHandlers.get().isAllowedColumn(field.getKey().getName(), dataConnector.getProject())) {
                                if (TableAndFieldName.isEqualTable(field.getKey().getName(), EtkDbConst.TABLE_PREISE)) {
                                    if (dataConnector.getProject().isPriceEnabled()) {
                                        result.add(field.cloneMe());
                                    }
                                } else {
                                    result.add(field.cloneMe());
                                }
                            }
                        }
                    }
                }
            }
            EtkDisplayFields displayFields = new EtkDisplayFields();
            for (EtkDisplayField df : result) {
                displayFields.addFeld(df);
            }
            editConnector.setAssemblyListDisplayFields(displayFields);
        }
        return editConnector;
    }
}
