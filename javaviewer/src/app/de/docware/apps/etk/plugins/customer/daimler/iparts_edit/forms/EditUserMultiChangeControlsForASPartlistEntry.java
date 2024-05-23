/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiArraySelectionTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiEventSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPSKVariantsSelectTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCombTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditMultiChangeEqualizeFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditCombTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.*;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.FrameworkConstantColor;
import de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

public class EditUserMultiChangeControlsForASPartlistEntry extends EditUserMultiChangeControls implements iPartsConst {

    public static final String LABEL_FOR_AS_MULTI_EDIT = "!!Massendatenbearbeitung:";
    private static final int DEFAULT_WIDTH_ATTRIBUTE_FIELD = 200;
    private static final int DEFAULT_WIDTH_EDITOR_FIELD = 400;

    /**
     * Erzeugt eine "Platzhalter"-Stücklistenposition die für diesen Editor als zentrales Speicher-Objekt dient
     *
     * @param dataConnector
     * @param selectedEntries
     * @param externalEditFields
     * @return
     */
    private iPartsDataPartListEntry createPartlistEntryWithDefaultValues(EditModuleFormIConnector dataConnector,
                                                                         List<EtkDataPartListEntry> selectedEntries,
                                                                         EtkEditFields externalEditFields) {
        // Platzhalter-Stücklistenposition erzeugen
        EtkDataPartListEntry pseudoEntry =
                EtkDataObjectFactory.createDataPartListEntry(dataConnector.getProject(),
                                                             new PartListEntryId(dataConnector.getCurrentAssembly().getAsId().getKVari(),
                                                                                 dataConnector.getCurrentAssembly().getAsId().getKVer(),
                                                                                 ""));
        if (pseudoEntry instanceof iPartsDataPartListEntry) {
            pseudoEntry.setOwnerAssembly(dataConnector.getCurrentAssembly());
            pseudoEntry.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            iPartsDataPartListEntry iPartsPseudoEntry = (iPartsDataPartListEntry)pseudoEntry;
            // DAIMLER-10085: Produktgruppe der Teileposition wird benötigt um die Codeprüfung beim Vereinheitlichen gegen diese
            // laufen zu lassen. Deswegen hier zu den Edit Feldern, sonst wird es zum Vereinheitlichen angezeigt
            EtkEditField editField = new EtkEditField(TABLE_KATALOG, FIELD_K_PRODUCT_GRP, false);
            externalEditFields.addField(editField);
            // Hol die berechneten Initialwerte
            DBDataObjectAttributes initValues = getInitialAttributesForUnify(selectedEntries, getFieldsAndDefaultValuesForUnify(externalEditFields), true);
            // Hier wird die "Platzhalter"-Position mit den Werten der Editfields befüllt
            iPartsPseudoEntry.getAttributes().addFields(initValues, DBActionOrigin.FROM_DB);
            String dialogGuid = selectedEntries.get(0).getFieldValue(FIELD_K_SOURCE_GUID);
            iPartsPseudoEntry.getAttributes().addField(FIELD_K_SOURCE_GUID, dialogGuid, DBActionOrigin.FROM_DB);
            String sourceType = selectedEntries.get(0).getFieldValue(FIELD_K_SOURCE_TYPE);
            iPartsPseudoEntry.getAttributes().addField(FIELD_K_SOURCE_TYPE, sourceType, DBActionOrigin.FROM_DB);

            iPartsPseudoEntry.setLogLoadFieldIfNeeded(false);
            return iPartsPseudoEntry;
        }
        return null;
    }


    /**
     * Erzeugt nach dem Edit die DatenObjekte, die im Multi-Edit verändert wurden
     *
     * @param dataConnector
     * @param selectedEntries
     * @param editedValues
     * @param multiControl
     * @param modifiedAssemblyIds Alle durch das Vereinheitlichen inkl. Vererbung zusätzlich veränderten Assemblies; kann
     *                            auch {@code null} sein
     * @return
     */
    public static EtkDataObjectList createDataObjectListForSelectedEntries(EditModuleFormIConnector dataConnector, List<EtkDataPartListEntry> selectedEntries,
                                                                           DBDataObjectAttributes editedValues, EditUserMultiChangeControlsForASPartlistEntry multiControl,
                                                                           Set<AssemblyId> modifiedAssemblyIds) {
        // Weitermachen, wenn der Dialog nicht abgebrochen wurde (editedValues ist nur null, wenn abgebrochen wurde)
        if (editedValues != null) {
            EtkProject project = dataConnector.getProject();
            iPartsDataCombTextList combTexts = multiControl.getCombText();
            GenericEtkDataObjectList<EtkDataObject> footnotes = multiControl.getFootnotes();
            GenericEtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList();

            iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList = getFootnoteRefsForPartlistEntry(footnotes, dataObjectListToBeSaved);
            // Map mit allen Textarten, die für die kombinierten Texte benötigt werden
            Map<String, EditCombinedTextForm.TextKindType> textKindsForCombTexts = EditCombinedTextForm.loadTextKinds(project);
            EditMultiChangeEqualizeFieldsHelper equalizeHelper = new EditMultiChangeEqualizeFieldsHelper(project);
            List<Map<String, String>> orgCopyCache = CopyAndPasteData.getCopyCacheData();
            multiControl.setActualCopyCache();
            // Und jetzt pro Stücklistenposition Datenobjekte erzeugen und aufsammeln
            for (EtkDataPartListEntry selectedPartlistEntry : selectedEntries) {
                // Um bei den schon vorhandenen Werkseinsatzdaten aus AS den Status zu ändern, müssen diese erstmal aus der DB geladen werden
                String dialogGuid = selectedPartlistEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfiltered = iPartsDataFactoryDataList.loadAfterSalesFactoryDataListForDialogPositionsVariant(project, dialogGuid, true);
                // Falls die Checkbox zum einfügen markiert ist, wird die kopierten Werkseinsatzdaten ermittelt
                iPartsDataFactoryDataList pastedFactoryData = multiControl.getPastedFactoryData(selectedPartlistEntry, allFactoryDataForPartlistEntryUnfiltered);
                EtkDataObjectList pastedFactoryDataAndStatusChangedData;
                if (pastedFactoryData != null) {
                    // Statusänderungen durchführen
                    pastedFactoryDataAndStatusChangedData = iPartsRelatedEditHelper.updateStatusValuesFactoryData(pastedFactoryData, allFactoryDataForPartlistEntryUnfiltered.getAsList(),
                                                                                                                  iPartsConst.FIELD_DFD_ADAT, iPartsConst.FIELD_DFD_SEQ_NO,
                                                                                                                  iPartsConst.FIELD_DFD_STATUS);
                    dataObjectListToBeSaved.addAll(pastedFactoryDataAndStatusChangedData, DBActionOrigin.FROM_EDIT);
                }
                // Dann der Sonderfall "Ergänzungstexte" (andere Tabelle und virtuelles Feld)
                // Muss vor der Stücklistenposition gemacht werden, damit eine Pseudo-Änderung im virtuellen Feld eventuell
                // wieder rückgängig gemacht werden kann
                EtkDataObjectList<? extends EtkDataObject> tempCombTextObjects = checkCombTextsForPartlistEntry(project, selectedPartlistEntry,
                                                                                                                combTexts, textKindsForCombTexts,
                                                                                                                multiControl.isEditedByEditor());
                // Dann der Sonderfall "Fußnoten" (andere Tabelle ohne virtuelles Feld)
                iPartsDataFootNoteCatalogueRefList changedFootNoteCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
                boolean footnotesChanged = checkFootnotesForPartlistEntry(project, selectedPartlistEntry, footnotes,
                                                                          footNoteCatalogueRefList, changedFootNoteCatalogueRefList, dataObjectListToBeSaved);
                // Jetzt erst die veränderten und gelöschten kombinierten Texte hinzufügen
                dataObjectListToBeSaved.addOnlyModifiedAndDeletedDataObjects(tempCombTextObjects, DBActionOrigin.FROM_EDIT);

                // Für die PSK-Variantengültigkeit, Baumuster und SAA/BK Gültigkeiten gibt es die Möglichkeit anstatt die
                // Werte zu vereinheitlichen, die Werte hinzuzufügen
                // Abfragen, ob hinzufügen gewählt wurde
                IsAddOrEqualizeValidities isAddOrEqualizeValidities = new IsAddOrEqualizeValidities(multiControl);

                // Erst die Felder, die ganz normal über die Attribute befüllt werden
                boolean partListEntryChanged = checkPartlistEntryAttributes(project, selectedPartlistEntry, editedValues,
                                                                            dataObjectListToBeSaved, isAddOrEqualizeValidities);

                // Falls sich nur die Fußnoten geändert haben aber keine anderen Attribute, dann muss der Stücklisteneintrag
                // trotzdem als geändert markiert werden
                if (footnotesChanged && !partListEntryChanged) {
                    // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                    iPartsDataPartListEntry.resetAutoCreatedFlag(selectedPartlistEntry);
                    selectedPartlistEntry.getAttributes().markAsModified();
                    dataObjectListToBeSaved.add(selectedPartlistEntry, DBActionOrigin.FROM_EDIT);
                }

                equalizeHelper.addPartListEntryToWorkList(selectedPartlistEntry, tempCombTextObjects, changedFootNoteCatalogueRefList);
            }

            Set<AssemblyId> equalizedAssemblyIds = equalizeHelper.equalizeAttributesInASPartListEntriesFromWorkList(dataObjectListToBeSaved);
            if (dataObjectListToBeSaved.isModifiedWithChildren()) {
                if (!selectedEntries.isEmpty()) {
                    // Das Source-Assembly (Ausgangspunkt der Selektion im Edit) als ChangeSet-Eintrag hinzufügen
                    AssemblyId masterAssemblyId = selectedEntries.get(0).getOwnerAssemblyId();
                    if (!equalizedAssemblyIds.contains(masterAssemblyId)) {
                        // zusätzlich Eintrag für die Assembly
                        EtkDataAssembly dataAssembly = EtkDataObjectFactory.createDataAssembly(project, masterAssemblyId);
                        // anstelle von dataAssembly.markAssemblyInChangeSetAsChanged();
                        if (dataAssembly.existsInDB()) {
                            dataAssembly.getAttributes().markAsModified();
                            dataObjectListToBeSaved.add(dataAssembly, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
                modifiedAssemblyIds.addAll(equalizedAssemblyIds);
            }
            CopyAndPasteData.setCopyCacheData(orgCopyCache);
            return dataObjectListToBeSaved;
        }
        return null;
    }


    /**
     * Liefert die {@link iPartsDataFootNoteCatalogueRef} Objekte zu den Fußnoten, die editiert wurden
     *
     * @param footnotes
     * @param dataObjectListToBeSaved
     * @return
     */
    private static iPartsDataFootNoteCatalogueRefList getFootnoteRefsForPartlistEntry(GenericEtkDataObjectList<EtkDataObject> footnotes,
                                                                                      EtkDataObjectList dataObjectListToBeSaved) {
        iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
        // Alle Datenobjekte aufsammeln, die sich aus dem Edit ergeben haben
        // Erst die Fußnoten-Objekte, die bei allen gleich sind
        if (footnotes != null) {
            for (EtkDataObject footnoteObject : footnotes) {
                if (footnoteObject instanceof iPartsDataFootNoteCatalogueRef) {
                    footNoteCatalogueRefList.add((iPartsDataFootNoteCatalogueRef)footnoteObject, DBActionOrigin.FROM_DB);
                } else {
                    dataObjectListToBeSaved.add(footnoteObject, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return footNoteCatalogueRefList;
    }

    /**
     * Legt für die im Multi-Edit editierten Fußnoten pro selektierter Stücklistenposition die benötigten
     * {@link iPartsDataFootNoteCatalogueRef} Objekte an
     *
     * @param project
     * @param selectedPartlistEntry
     * @param footnotes
     * @param footNoteCatalogueRefList
     * @param changedFootNoteCatalogueRefList
     * @param dataObjectListToBeSaved
     * @return Wurden die Fußnoten verändert für den übergebenen Stücklisteneintrag?
     */
    private static boolean checkFootnotesForPartlistEntry(EtkProject project, EtkDataPartListEntry selectedPartlistEntry,
                                                          GenericEtkDataObjectList<EtkDataObject> footnotes,
                                                          iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList,
                                                          iPartsDataFootNoteCatalogueRefList changedFootNoteCatalogueRefList,
                                                          EtkDataObjectList dataObjectListToBeSaved) {
        // Fußnoten-Objekte (Referenzen). Wenn nicht "null", dann wurde die Checkbox angeklickt
        if (footnotes != null) {
            // Lade alle Fußnotenreferenzen für die Stücklistenposition
            iPartsDataFootNoteCatalogueRefList refList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project, selectedPartlistEntry.getAsId());
            boolean clearFootnotes = false;
            boolean footnotesChanged = false;
            Map<String, iPartsDataFootNoteCatalogueRef> footNoteCatalogueRefMap = new HashMap<>();
            if (!refList.isEmpty()) {
                for (iPartsDataFootNoteCatalogueRef catalogueRef : refList) {
                    String fnRefDataId = makeFootnoteRefId(catalogueRef);
                    footNoteCatalogueRefMap.put(fnRefDataId, catalogueRef);
                }
            }
            // Wurde die Combobox angeklickt und der Editor liefert keine Fußnoten, dann möchte der Autor alle
            // Fußnoten an allen Positionen leeren
            if (!footNoteCatalogueRefList.isEmpty()) {
                for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : footNoteCatalogueRefList) {
                    iPartsFootNoteCatalogueRefId id = new iPartsFootNoteCatalogueRefId(selectedPartlistEntry.getAsId(),
                                                                                       fnCatalogueRef.getAsId().getFootNoteId());
                    iPartsDataFootNoteCatalogueRef newFnCatalogueRef = new iPartsDataFootNoteCatalogueRef(project, id);
                    newFnCatalogueRef.assignAttributes(project, fnCatalogueRef.getAttributes(), false, DBActionOrigin.FROM_EDIT);

                    // ID wird durch assignAttributes() auf die ID von fnCatalogueRef geändert
                    newFnCatalogueRef.setId(id, DBActionOrigin.FROM_EDIT);
                    newFnCatalogueRef.updateOldId();

                    // Check, ob es diese Fußnote schon gibt (notfalls die Sequenznummer korrigieren)
                    iPartsDataFootNoteCatalogueRef existingFnCatalogueRef = null;
                    if (!footNoteCatalogueRefMap.isEmpty()) {
                        String fnRefId = makeFootnoteRefId(newFnCatalogueRef);
                        existingFnCatalogueRef = footNoteCatalogueRefMap.remove(fnRefId);

                        // Korrigiere die Sequenznummer falls notwendig
                        String newSeqNo = newFnCatalogueRef.getSequenceNumber();
                        if ((existingFnCatalogueRef != null) && !existingFnCatalogueRef.getSequenceNumber().equals(newSeqNo)) {
                            existingFnCatalogueRef.setFieldValue(FIELD_DFNK_FN_SEQNO, newSeqNo, DBActionOrigin.FROM_EDIT);
                            dataObjectListToBeSaved.add(existingFnCatalogueRef, DBActionOrigin.FROM_EDIT);
                            footnotesChanged = true;
                        }
                    }

                    if (existingFnCatalogueRef != null) {
                        newFnCatalogueRef = existingFnCatalogueRef; // Verwende die existierende Fußnoten-Referenz anstatt der neuen
                    } else {
                        dataObjectListToBeSaved.add(newFnCatalogueRef, DBActionOrigin.FROM_EDIT);
                        footnotesChanged = true;
                    }
                    if (changedFootNoteCatalogueRefList != null) {
                        changedFootNoteCatalogueRefList.add(newFnCatalogueRef, DBActionOrigin.FROM_EDIT);
                    }
                }
                clearFootnotes = true;
            }

            // Wenn noch welche von den alten Fußnoten existieren, dann sollen diese gelöscht werden
            if (!footNoteCatalogueRefMap.isEmpty()) {
                for (iPartsDataFootNoteCatalogueRef footNoteCatalogueRef : footNoteCatalogueRefMap.values()) {
                    dataObjectListToBeSaved.delete(footNoteCatalogueRef, true, DBActionOrigin.FROM_EDIT);
                    if (changedFootNoteCatalogueRefList != null) {
                        changedFootNoteCatalogueRefList.delete(footNoteCatalogueRef, true, DBActionOrigin.FROM_EDIT);
                    }
                    footnotesChanged = true;
                }
                clearFootnotes = true;
            }

            if (clearFootnotes) {
                if (selectedPartlistEntry instanceof iPartsDataPartListEntry) {
                    ((iPartsDataPartListEntry)selectedPartlistEntry).clearFootnotes();
                }
            }

            return footnotesChanged;
        }

        return false;
    }

    private static String makeFootnoteRefId(iPartsDataFootNoteCatalogueRef catalogueRef) {
        return catalogueRef.getAsId().getFootNoteId();
    }

    /**
     * Legt für die im Multi-Edit editierten kombinierten Texte pro selektierter Stücklistenposition die benötigten
     * {@link iPartsDataCombText} Objekte an
     *
     * @param project
     * @param selectedPartlistEntry
     * @param combTexts
     * @param textKindsForCombTexts
     */
    private static EtkDataObjectList checkCombTextsForPartlistEntry(EtkProject project, EtkDataPartListEntry selectedPartlistEntry,
                                                                    iPartsDataCombTextList combTexts,
                                                                    Map<String, EditCombinedTextForm.TextKindType> textKindsForCombTexts,
                                                                    boolean orderDefinedByEditor) {
        // Kombinierten Texte (Wenn nicht "null", dann wurde die Checkbox angeklickt)
        EtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList();
        if (combTexts != null) {
            // Lade alle kombinierten Texte für die Stücklistenposition in allen Sprachen
            iPartsDataCombTextList currentCombTexts = iPartsDataCombTextList.loadForPartListEntryAndAllLanguages(selectedPartlistEntry.getAsId(), project);

            boolean keepMultiText = false; // Kenner, ob sprachabhängige Texte erhalten bleiben sollen
            boolean keepNeutralText = false; // Kenner, ob sprachneutrale Texte erhalten bleiben sollen
            Iterator<iPartsDataCombText> combTextIterator = combTexts.iterator();
            iPartsDataCombTextList realCombTexts = new iPartsDataCombTextList();
            // Prüfen, ob ein Platzhalter für zu erhaltene Texte in den gelieferten kombinierten Texten vorhanden ist
            while (combTextIterator.hasNext()) {
                iPartsDataCombText combText = combTextIterator.next();
                if (combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getText(project.getDBLanguage()).equals(iPartsMultiLangGuiCombTextButtonField.TEXT_FOR_PLACEHOLDER_ENTRY)) {
                    keepMultiText = true;
                    continue;
                } else if (combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getText(project.getDBLanguage()).equals(iPartsNeutralGuiCombTextButtonField.TEXT_FOR_PLACEHOLDER_ENTRY)) {
                    keepNeutralText = true;
                    continue;
                }
                // Nur echte Textelemente für weitere Verarbeitung übernehmen
                realCombTexts.add(combText, DBActionOrigin.FROM_EDIT);
            }

            // Beide Textarten eines kombinierten Textes sollen bei allen Positionen erhalten bleiben
            if (keepMultiText && keepNeutralText) {
                return dataObjectListToBeSaved;
            }

            // Kombinierten Text an der Stücklistenposition in seine Bestandteile zerlegen
            Map<DictTextKindTypes, iPartsDataCombTextList> currentTextkindToCombTexts = iPartsEditCombTextHelper.getTextKindToCombTextMap(project, currentCombTexts, textKindsForCombTexts);
            // Neuen kombinierten Text in seine Bestandteile zerlegen
            Map<DictTextKindTypes, iPartsDataCombTextList> newTextkindToCombTexts = iPartsEditCombTextHelper.getTextKindToCombTextMap(project, realCombTexts, textKindsForCombTexts);
            // Check, ob alle Texte gleich sind. Wurden Texte gelöscht, dann braucht man den Check nicht durchführen
            if (combTexts.getDeletedList().isEmpty() && !textCombTextChanged(currentTextkindToCombTexts, newTextkindToCombTexts, keepMultiText, keepNeutralText)) {
                return dataObjectListToBeSaved;
            }

            // Wenn kein Pseudo-Eintrag vorkommt, dann kann es sich nur um Objekte aus dem Editor oder direkte Änderungen
            // via ComboBox handeln. Beim Editor ist die Reihenfolge vorgegeben. Bei der direkten Eingabe besteht nur die
            // Möglichkeit, dass genau ein Text anders ist (Multi-Edit via ComboBox ist ja nicht möglich)
            boolean orderIsGiven = !keepMultiText && !keepNeutralText;
            if (orderIsGiven) {
                // Wurde die Reihenfolge der Textelemente fest vom Editor vorgegeben? Falls ja, Reihenfolge direkt übernehmen
                if (orderDefinedByEditor) {
                    // Da die Reihenfolge vorgegeben ist, muss man nur noch die bestehenden Elemente durchlaufen und schauen,
                    // ob sich Texte an bestehenden Positionen verändert haben und ob bestehende Texte gelöscht bzw. neue
                    // Texte hinzugefügt werden müssen.
                    Map<String, EtkMultiSprache> textSeqNoToCombText = new LinkedHashMap<>();
                    for (iPartsDataCombText combText : realCombTexts) {
                        textSeqNoToCombText.put(combText.getAsId().getTextSeqNo(), combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT));
                    }
                    iPartsCombTextHelper.handleCombTextsWithOrder(project, selectedPartlistEntry.getAsId(), textSeqNoToCombText,
                                                                  currentCombTexts.getAsList(), dataObjectListToBeSaved);
                } else {
                    // Hier haben wir den Fall, dass alle Positionen in beiden ComboBoxen den gleichen Text haben, aber
                    // die Reihenfolge NICHT vom Editor vorgegeben wurde. Also müssen hier die neuen Text mit den bestehenden
                    // ausgetauscht werden.
                    // Man kommt hier rein, wenn man mehrere Positionen vereinheitlicht hat und dann via ComboBox den
                    // sprachneutralen oder sprachabhängigen Teil beim Vereinheitlichen im nachhinein verändert (sofern
                    // es nur ein Textelement ist).

                    // Die bisherigen spachabhängigen Texte an der Stücklistenposition
                    iPartsDataCombTextList currentMultiTexts = currentTextkindToCombTexts.get(DictTextKindTypes.ADD_TEXT);
                    // Die bisherigen sprachneutralen Texte an der Stücklistenposition
                    iPartsDataCombTextList currentNeutralTexts = currentTextkindToCombTexts.get(DictTextKindTypes.NEUTRAL_TEXT);

                    // Die neuen spachabhängigen Texte
                    iPartsDataCombTextList newMultiTexts = newTextkindToCombTexts.get(DictTextKindTypes.ADD_TEXT);
                    // Die neuen sprachneutralen Texte
                    iPartsDataCombTextList newNeutralTexts = newTextkindToCombTexts.get(DictTextKindTypes.NEUTRAL_TEXT);
                    // Wenn die Anzahl aller sprachabhängigen und sprachneutralen Texte an der Stücklistenposition gleich
                    // der Anzahl der neuen Texte (pro Textart) ist, kann man die Texte 1:1 austauschen. Ist die Anzahl
                    // unterschiedlich, müssen alle neu organisiert werden (sprachabhängige vor sprachneutrale).
                    boolean sameSizeMultiText = hasSameSize(newMultiTexts, currentMultiTexts);
                    boolean sameSizeNeutralText = hasSameSize(newNeutralTexts, currentNeutralTexts);
                    if (sameSizeMultiText && sameSizeNeutralText) {
                        // Tausch die neuen übergebenen Texte mit den bisherigen aus (sofern welche existieren).
                        handleSameSizeNewAndExistingTexts(newMultiTexts, currentMultiTexts, null, dataObjectListToBeSaved);
                        handleSameSizeNewAndExistingTexts(newNeutralTexts, currentNeutralTexts, null, dataObjectListToBeSaved);
                    } else {
                        // Anzahl Texte an Stücklistenposition passt nicht zur Anzahl neuer Texte
                        // -> Reorganisieren: zuerst sprachabhängige und dann sprachneutrale Texte
                        iPartsDataCombTextList listWithOrderedCombTexts = new iPartsDataCombTextList();
                        if (newMultiTexts != null) {
                            listWithOrderedCombTexts.addAll(newMultiTexts, DBActionOrigin.FROM_DB);
                        }
                        if (newNeutralTexts != null) {
                            listWithOrderedCombTexts.addAll(newNeutralTexts, DBActionOrigin.FROM_DB);
                        }
                        reorderAndStoreCurrentAndNewCombText(project, selectedPartlistEntry.getAsId(), listWithOrderedCombTexts,
                                                             null, currentCombTexts.getAsList(), dataObjectListToBeSaved, true);
                    }

                }

            } else {
                // Die bisherigen spachabhängigen Texte an der Stücklistenposition
                iPartsDataCombTextList currentMultiTexts = currentTextkindToCombTexts.get(DictTextKindTypes.ADD_TEXT);
                // Die bisherigen sprachneutralen Texte an der Stücklistenposition
                iPartsDataCombTextList currentNeutralTexts = currentTextkindToCombTexts.get(DictTextKindTypes.NEUTRAL_TEXT);
                // An diesem Punkt kann es nur zwei Möglichkeiten geben:
                // 1. Behalte alle sprachabhängigen Text und ersetze alle sprachneutralen
                // oder
                // 2. Behalte alle sprachneutralen Texte und ersetze alle sprachabhängigen
                // Alle anderen Fälle werden weiter oben abgefangen

                // Erster Fall: Behalte alle sprachabhängigen Texte und ersetze alle sprachneutralen
                if (keepMultiText) {
                    if ((currentNeutralTexts == null) || currentNeutralTexts.isEmpty()) {
                        // Aktuelle Position hatte bisher keine sprachneutralen Texte
                        // -> Neutralen Text hinten anstellen
                        int textSeqNo = 0;
                        if (currentMultiTexts != null) {
                            // Aktuelle Position hatte bisher sprachabhängige Text
                            // -> Speicher die Anzahl aller bisherigen Texte für die Generierung neuer Textpositionsnummern
                            textSeqNo = currentMultiTexts.size();
                            dataObjectListToBeSaved.addAll(currentMultiTexts, DBActionOrigin.FROM_EDIT);
                        }
                        // Es existierten bisher keine neutralen Texte -> Leg den neuen Text an und platziere ihn hinter
                        // die aktuellen sprachabhängigen Texte (falls welche da sind)
                        for (iPartsDataCombText combText : realCombTexts) {
                            textSeqNo++;
                            addCombTextToSaveList(project, selectedPartlistEntry.getAsId(), combText, dataObjectListToBeSaved, textSeqNo);
                        }
                        return dataObjectListToBeSaved;
                    } else if (currentNeutralTexts.size() == realCombTexts.size()) {
                        // Aktuelle Stücklistenposition hatte bisher genauso viele sprachneutralen Texte, wie hinzugefügt werden sollen
                        // -> Textposition der bisherigen neutralen Texte übernehmen
                        handleSameSizeNewAndExistingTexts(realCombTexts, currentNeutralTexts, currentMultiTexts, dataObjectListToBeSaved);
                    } else {
                        // Wir haben mehr neue sprachneutrale Texte als bisher an der Stücklistenposition vorhanden waren.
                        // Da wir hier die Reihenfolgen nicht 1:1 abbilden können, werden alle sprachneutralen Teile hinter alle
                        // sprachabhängigen Teile gehängt. Sprachneutrale Elemente folgen eigentlich immer sprachabhängigen
                        // Elementen.
                        reorderAndStoreCurrentAndNewCombText(project, selectedPartlistEntry.getAsId(), realCombTexts,
                                                             currentMultiTexts, currentCombTexts.getAsList(),
                                                             dataObjectListToBeSaved, true);
                    }
                }

                // Zweiter Fall: Behalte alle sprachneutralen Texte und ersetze alle sprachabhängigen
                if (keepNeutralText) {
                    if ((currentMultiTexts == null) || currentMultiTexts.isEmpty()) {
                        // Aktuelle Position hatte bisher keine sprachabhängigen Texte
                        // -> Sprachabhängigen Text VORNE anstellen
                        Map<String, EtkMultiSprache> textToSeqNumber = new LinkedHashMap<>();
                        // Bisherigen sprachneutralen Texte hinten anstellen
                        int textSeqNo = addTextForTextSeqNo(realCombTexts, textToSeqNumber, 0);
                        if (currentNeutralTexts != null) {
                            for (iPartsDataCombText neutralCombText : currentNeutralTexts) {
                                // Bisherige Textposition wird um die Anzahl der neuen sprachabhängigen Teile ergänzt
                                int newTextSeqNo = textSeqNo + StrUtils.strToIntDef(neutralCombText.getAsId().getTextSeqNo(), 0);
                                // Kann eigentlich nicht passieren
                                if (newTextSeqNo == textSeqNo) {
                                    continue;
                                }
                                textToSeqNumber.put(EtkDbsHelper.formatLfdNr(newTextSeqNo), neutralCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT));
                            }
                        }
                        iPartsCombTextHelper.handleCombTextsWithOrder(project, selectedPartlistEntry.getAsId(), textToSeqNumber, currentCombTexts.getAsList(), dataObjectListToBeSaved);
                    } else if (currentMultiTexts.size() == realCombTexts.size()) {
                        // Aktuelle Stücklistenposition hatte bisher genauso viele sprachabhängige Texte, wie hinzugefügt werden sollen
                        // -> Textposition der bisherigen sprachabhängigen Texte übernehmen
                        handleSameSizeNewAndExistingTexts(realCombTexts, currentMultiTexts, currentNeutralTexts, dataObjectListToBeSaved);
                    } else {
                        // Wir haben mehr neue sprachabhängige Texte als bisher an der Stücklistenposition vorhanden waren.
                        // Da wir hier die Reihenfolgen nicht 1:1 abbilden können, werden alle sprachabhängigen Teile vor alle
                        // sprachneutralen Teile angestellt. Sprachneutrale Elemente folgen eigentlich immer sprachabhängigen
                        // Elementen.
                        reorderAndStoreCurrentAndNewCombText(project, selectedPartlistEntry.getAsId(), realCombTexts,
                                                             currentNeutralTexts, currentCombTexts.getAsList(),
                                                             dataObjectListToBeSaved, false);
                    }
                }
            }

            // Flags für "Quelle GenVO" müssen zurückgesetzt werden, wenn es mehr als einen sprachabhängigen Ergänzungstext gibt
            iPartsDataCombTextList dataCombTextListAddText = newTextkindToCombTexts.get(DictTextKindTypes.ADD_TEXT);
            int addTextCount = (dataCombTextListAddText != null) ? dataCombTextListAddText.size() : 0;
            boolean forceResetSourceGenVO = addTextCount != 1;

            // Flags für "Quelle GenVO" müssen zurückgesetzt werden
            for (Object object : dataObjectListToBeSaved) {
                if (object instanceof iPartsDataCombText) {
                    iPartsDataCombText dataCombText = (iPartsDataCombText)object;
                    if (forceResetSourceGenVO || dataCombText.isModified()) {
                        dataCombText.setFieldValueAsBoolean(FIELD_DCT_SOURCE_GENVO, false, DBActionOrigin.FROM_EDIT);
                    }
                } else if (object instanceof iPartsDataCombTextList) {
                    for (iPartsDataCombText dataCombText : (iPartsDataCombTextList)object) {
                        if (forceResetSourceGenVO || dataCombText.isModified()) {
                            dataCombText.setFieldValueAsBoolean(FIELD_DCT_SOURCE_GENVO, false, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
        }

        return dataObjectListToBeSaved;
    }

    public static EtkDataObjectList checkCombTextsForPartlistEntry(EtkProject project, EtkDataPartListEntry selectedPartlistEntry,
                                                                   iPartsDataCombTextList combTexts) {
        Map<String, EditCombinedTextForm.TextKindType> textKindsForCombTexts = EditCombinedTextForm.loadTextKinds(project);
        return checkCombTextsForPartlistEntry(project, selectedPartlistEntry, combTexts, textKindsForCombTexts, true);
    }

    private static boolean hasSameSize(iPartsDataCombTextList newTexts, iPartsDataCombTextList currentTexts) {
        return (newTexts != null) && (currentTexts != null) && (newTexts.size() == currentTexts.size());
    }

    /**
     * Reorganisiert alle kombinierten Texte an eine Stücklistenposition. Bestehende werden geändert, neue erzeugt und
     * unnötige gelöscht.
     *
     * @param project
     * @param partListEntryId
     * @param newCombTexts
     * @param currentTextToKeep
     * @param allExistingTextFromEntry
     * @param dataObjectListToBeSaved
     * @param newTextAfterTextToKeep
     */
    public static void reorderAndStoreCurrentAndNewCombText(EtkProject project, PartListEntryId partListEntryId,
                                                            iPartsDataCombTextList newCombTexts,
                                                            iPartsDataCombTextList currentTextToKeep,
                                                            List<iPartsDataCombText> allExistingTextFromEntry,
                                                            EtkDataObjectList dataObjectListToBeSaved,
                                                            boolean newTextAfterTextToKeep) {
        Map<String, EtkMultiSprache> textToSeqNumber = new LinkedHashMap<>();
        int textSeqNo = 0;

        if (newTextAfterTextToKeep) {
            textSeqNo = addTextForTextSeqNo(currentTextToKeep, textToSeqNumber, textSeqNo);
            addTextForTextSeqNo(newCombTexts, textToSeqNumber, textSeqNo);
        } else {
            textSeqNo = addTextForTextSeqNo(newCombTexts, textToSeqNumber, textSeqNo);
            addTextForTextSeqNo(currentTextToKeep, textToSeqNumber, textSeqNo);
        }

        iPartsCombTextHelper.handleCombTextsWithOrder(project, partListEntryId, textToSeqNumber, allExistingTextFromEntry, dataObjectListToBeSaved);
    }

    /**
     * Fügt die übergebenen Text und ihre Textposition der übergebenen Map hinzu.
     *
     * @param combTextList
     * @param textToSeqNumber
     * @param textSeqNo
     * @return
     */
    public static int addTextForTextSeqNo(iPartsDataCombTextList combTextList, Map<String, EtkMultiSprache> textToSeqNumber, int textSeqNo) {
        if (combTextList != null) {
            for (iPartsDataCombText currentCombTextToReplace : combTextList) {
                textSeqNo++;
                textToSeqNumber.put(EtkDbsHelper.formatLfdNr(textSeqNo), currentCombTextToReplace.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT));
            }
        }
        return textSeqNo;
    }

    /**
     * Tauscht bestehende Texte durch neue Texte aus, wenn die Anzahl der aktuellen Texte an der Stücklistenposition
     * gleich der Anzahl deer neuen Texte ist.
     *
     * @param newCombTexts
     * @param currentTextsForChange
     * @param existingTextsToKeep
     * @param dataObjectListToBeSaved
     */
    private static void handleSameSizeNewAndExistingTexts(iPartsDataCombTextList newCombTexts, iPartsDataCombTextList currentTextsForChange,
                                                          iPartsDataCombTextList existingTextsToKeep, EtkDataObjectList dataObjectListToBeSaved) {
        if (existingTextsToKeep != null) {
            // Zur Sicherheit
            dataObjectListToBeSaved.addAll(existingTextsToKeep, DBActionOrigin.FROM_DB);
        }

        int size = currentTextsForChange.size();
        for (int index = 0; index < size; index++) {
            iPartsDataCombText currentMultiCombText = currentTextsForChange.get(index);
            EtkMultiSprache currentMultiText = currentMultiCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
            iPartsDataCombText newMultiCombText = newCombTexts.get(index);
            EtkMultiSprache newMultiText = newMultiCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
            if (!currentMultiText.getTextId().equals(newMultiText.getTextId())) {
                currentMultiCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, newMultiText, DBActionOrigin.FROM_EDIT);
                dataObjectListToBeSaved.add(currentMultiCombText, DBActionOrigin.FROM_EDIT);
            } else {
                // Kombinierten Texteintrag trotzdem hinzufügen, damit die Liste vollständig ist, aber mit DBActionOrigin
                // FROM_DB, damit die Liste dadurch nicht als verändert markiert wird
                dataObjectListToBeSaved.add(currentMultiCombText, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Fügrt einen neuen kombinierten Text für die übergebene Stücklistenposition der übergebenen Liste hinzu.
     *
     * @param project
     * @param partListEntryId
     * @param newCombText
     * @param dataObjectListToBeSaved
     * @param textSeqNo
     */
    private static void addCombTextToSaveList(EtkProject project, PartListEntryId partListEntryId, iPartsDataCombText newCombText,
                                              EtkDataObjectList dataObjectListToBeSaved, int textSeqNo) {
        iPartsCombTextId combTextId = new iPartsCombTextId(partListEntryId, EtkDbsHelper.formatLfdNr(textSeqNo));
        iPartsDataCombText combTextForEntry = newCombText.cloneMe(project);
        combTextForEntry.setId(combTextId, DBActionOrigin.FROM_EDIT);
        combTextForEntry.updateIdFromPrimaryKeys();
        combTextForEntry.updateOldId();
        dataObjectListToBeSaved.add(combTextForEntry, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Weist den selektierten Stücklistenpositionen die editierten Werte der "Platzhalter"-Position zu
     *
     * @param project
     * @param selectedPartlistEntry
     * @param editedValues
     * @param dataObjectListToBeSaved
     * @return Wurde der übergebene Stücklisteneintrag verändert?
     */
    private static boolean checkPartlistEntryAttributes(EtkProject project, EtkDataPartListEntry selectedPartlistEntry,
                                                        DBDataObjectAttributes editedValues, EtkDataObjectList dataObjectListToBeSaved,
                                                        IsAddOrEqualizeValidities isAddOrEqualizeValidities) {
        boolean oldEvalPemFrom = selectedPartlistEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM);
        boolean oldEvalPemTo = selectedPartlistEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_TO);
        // Die "normalen" Attribute setzen und dann die ganze Stücklistenposition aufsammeln
        for (Map.Entry<String, DBDataObjectAttribute> editedValue : editedValues.entrySet()) {
            String fieldname = editedValue.getKey();
            if (selectedPartlistEntry.attributeExists(fieldname)) {
                DBDataObjectAttribute attribute = editedValue.getValue();
                if (editedValue.getValue().getType() == DBDataObjectAttribute.TYPE.ARRAY) {
                    // Das Array wurde mit einer temporären ArrayId erzeugt. Hier muss der Inhalt des temporären Arrays auf die Arrays der Positzion verteilt werden
                    EtkDataArray array = attribute.getAsArray(selectedPartlistEntry);
                    // Wenn das Array leer ist, dann hat der Autor das explizit so gewollt, d.h. der eigentlichen Position
                    // Wird ein leeeres neue Array übergebene und dadurch wird intern, das bestehende Array gelöscht.
                    EtkDataArray arrayForEntry;
                    if (array.isEmpty()) {
                        if (!(fieldname.equals(iPartsConst.FIELD_K_SA_VALIDITY) && isAddOrEqualizeValidities.isAddSAABkValidity()) &&
                            !(fieldname.equals(iPartsConst.FIELD_K_MODEL_VALIDITY) && isAddOrEqualizeValidities.isAddModelValidity()) &&
                            !(fieldname.equals(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY) && isAddOrEqualizeValidities.isAddPSKVariantsValidity())) {
                            selectedPartlistEntry.setFieldValueAsArray(fieldname, null, DBActionOrigin.FROM_EDIT);
                            selectedPartlistEntry.setIdForArray(fieldname, "", DBActionOrigin.FROM_EDIT);
                        }
                    } else {
                        // Vorhandene Array-ID bevorzugen für die korrekte Simulation der geänderten Array-Werte
                        String arrayId = selectedPartlistEntry.getFieldValue(fieldname);
                        if (arrayId.isEmpty()) {
                            arrayId = project.getDbLayer().getNewArrayNo(TableAndFieldName.make(TABLE_KATALOG, attribute.getName()),
                                                                         selectedPartlistEntry.getAsId().toString("|"), false);
                        }
                        List<DBDataObjectAttribute> arrayAttributes = array.getAttributes();
                        if ((fieldname.equals(iPartsConst.FIELD_K_SA_VALIDITY) && isAddOrEqualizeValidities.isAddSAABkValidity())
                            || (fieldname.equals(iPartsConst.FIELD_K_MODEL_VALIDITY) && isAddOrEqualizeValidities.isAddModelValidity())
                            || (fieldname.equals(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY) && isAddOrEqualizeValidities.isAddPSKVariantsValidity())) {
                            arrayAttributes = addSelectedAttributesToAttributesOfField(selectedPartlistEntry, fieldname, arrayAttributes);
                        }

                        // Falls wegen Duplikaten nichts hinzugefügt wurde, darf sich der selektierte Stücklisteneintrag auch nicht ändern
                        if (!arrayAttributes.isEmpty()) {
                            arrayForEntry = new EtkDataArray(arrayId, arrayAttributes);
                            arrayForEntry.sortAttributes();

                            selectedPartlistEntry.setFieldValueAsArray(fieldname, arrayForEntry, DBActionOrigin.FROM_EDIT);
                            selectedPartlistEntry.setIdForArray(fieldname, arrayForEntry.getArrayId(), DBActionOrigin.FROM_EDIT);
                        }
                    }
                } else if (editedValue.getValue().getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                    selectedPartlistEntry.setFieldValueAsMultiLanguage(fieldname, attribute.getAsMultiLanguage(selectedPartlistEntry, false), DBActionOrigin.FROM_EDIT);
                } else {
                    selectedPartlistEntry.setFieldValue(fieldname, attribute.getAsString(), DBActionOrigin.FROM_EDIT);
                }
            }
        }

        handlePartListEntry(selectedPartlistEntry, oldEvalPemFrom, oldEvalPemTo);
        if (selectedPartlistEntry.isModifiedWithChildren()) {
            // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
            iPartsDataPartListEntry.resetAutoCreatedFlag(selectedPartlistEntry);
            dataObjectListToBeSaved.add(selectedPartlistEntry, DBActionOrigin.FROM_EDIT);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Bei SAA/BK Gültigkeit und bei Baumuster-Gültigkeit gibt es die Möglichkeit, die Neuen hinzuzufügen,
     * anstatt die alten Werte mit den neuen Werten zu überschreiben
     * Falls Gültigkeit schon vorhanden, wird nichts hinzugefügt
     *
     * @param selectedPartlistEntry Stücklisteneintag mit den alten Werten des Feldes
     * @param fieldname             Der Name des Feldes an das die neuen Werte gehängt werden.
     * @param arrayAttributes       Die neuen Werte
     * @return
     */
    private static List<DBDataObjectAttribute> addSelectedAttributesToAttributesOfField(EtkDataPartListEntry selectedPartlistEntry, String fieldname,
                                                                                        List<DBDataObjectAttribute> arrayAttributes) {
        List<DBDataObjectAttribute> oldAndNewAttributesJoined = new ArrayList<>();
        EtkDataArray oldDataArray = selectedPartlistEntry.getFieldValueAsArray(fieldname);
        if (!oldDataArray.isEmpty()) {
            List<DBDataObjectAttribute> oldDataArrayAttributes = oldDataArray.getAttributes();
            oldAndNewAttributesJoined.addAll(oldDataArrayAttributes);
            boolean isDuplicate = false;
            for (DBDataObjectAttribute arrayAttribute : arrayAttributes) {
                for (DBDataObjectAttribute oldDataAttribute : oldDataArrayAttributes) {
                    // Die arrayId des DBDataObjectAttributes der neuen Gültigkeiten ist unterschiedlich zu der arrayId des DBDataObjectAttributes der
                    // schon bestehenden Gültigkeiten. Deswegen nur auf den Wert gehen um auf Duplikate zu prüfen nicht auf das ganze Attribut
                    if (oldDataAttribute.getValue().equals(arrayAttribute.getValue())) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    oldAndNewAttributesJoined.add(arrayAttribute);
                }
                isDuplicate = false;
            }

            if (oldAndNewAttributesJoined.equals(oldDataArrayAttributes)) {
                oldAndNewAttributesJoined.clear();
            }
        } else {
            // Falls vorher keine Daten vorhanden waren, können die Neuen einfach hinzugefügt werden
            oldAndNewAttributesJoined.addAll(arrayAttributes);
        }
        return oldAndNewAttributesJoined;
    }

    /**
     * Überprüft, ob die bisherigen Textbausteine gleich den aktuellen sind
     *
     * @param currentTextkindToCombTexts
     * @param textkindToNewCombTexts
     * @param keepMultiText
     * @param keepNeutralText
     * @return
     */
    private static boolean textCombTextChanged(Map<DictTextKindTypes, iPartsDataCombTextList> currentTextkindToCombTexts, Map<DictTextKindTypes, iPartsDataCombTextList> textkindToNewCombTexts, boolean keepMultiText, boolean keepNeutralText) {
        if (!keepMultiText && !keepNeutralText && (currentTextkindToCombTexts.size() != textkindToNewCombTexts.size())) {
            return true;
        }
        List<iPartsDataCombText> currentMultiList = new ArrayList<>();
        List<iPartsDataCombText> currentNeutralList = new ArrayList<>();
        if (currentTextkindToCombTexts.containsKey(DictTextKindTypes.ADD_TEXT)) {
            currentMultiList.addAll(currentTextkindToCombTexts.get(DictTextKindTypes.ADD_TEXT).getAsList());
        }
        if (currentTextkindToCombTexts.containsKey(DictTextKindTypes.NEUTRAL_TEXT)) {
            currentNeutralList.addAll(currentTextkindToCombTexts.get(DictTextKindTypes.NEUTRAL_TEXT).getAsList());
        }

        List<iPartsDataCombText> newMultiList = new ArrayList<>();
        List<iPartsDataCombText> newNeutralList = new ArrayList<>();
        if (textkindToNewCombTexts.containsKey(DictTextKindTypes.ADD_TEXT)) {
            newMultiList.addAll(textkindToNewCombTexts.get(DictTextKindTypes.ADD_TEXT).getAsList());
        }
        if (textkindToNewCombTexts.containsKey(DictTextKindTypes.NEUTRAL_TEXT)) {
            newNeutralList.addAll(textkindToNewCombTexts.get(DictTextKindTypes.NEUTRAL_TEXT).getAsList());
        }

        if (!keepMultiText) {
            if (currentMultiList.size() != newMultiList.size()) {
                return true;
            }
        } else {
            currentMultiList.clear();
        }
        if (!keepNeutralText) {
            if (currentNeutralList.size() != newNeutralList.size()) {
                return true;
            }
        } else {
            currentNeutralList.clear();
        }

        // Alle Texte zusammenlegen
        currentMultiList.addAll(currentNeutralList);
        iPartsCombTextHelper.sortCombTextByTextSeqNo(currentMultiList);


        // Alle Texte zusammenlegen
        newMultiList.addAll(newNeutralList);
        iPartsCombTextHelper.sortCombTextByTextSeqNo(newMultiList);

        for (int index = 0; index < currentMultiList.size(); index++) {
            iPartsDataCombText currentText = currentMultiList.get(index);
            iPartsDataCombText newText = newMultiList.get(index);

            String currentTextId = currentText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId();
            String newTextId = newText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId();
            if (!currentTextId.equals(newTextId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zusätzliche Checks an einer Stücklistenposition nachdem sie editiert wurde (analog zum Edit in den Related-Infos)
     *
     * @param partListEntryForEdit
     * @param oldEvalPemFrom
     * @param oldEvalPemTo
     */
    private static void handlePartListEntry(EtkDataPartListEntry partListEntryForEdit, boolean oldEvalPemFrom, boolean oldEvalPemTo) {
        iPartsEditUserControlsHelper.handleCodeFieldAfterEdit(partListEntryForEdit);
        iPartsEditUserControlsHelper.handlePemFlagsAfterEdit(partListEntryForEdit, null, oldEvalPemFrom, oldEvalPemTo);
    }


    // Temporärer Stücklisteneintrag der alle editierten Werte hält. Wenn der Dialog geschlossen wird, werden die
    // seleketieren Werte an die selektieren Stücklistenpositionen weitrgegeben
    private iPartsDataPartListEntry iPartsPseudoEntry;
    private GuiCheckbox footnoteCheckbox; // Checkbox für die Fußnoten
    private GuiCheckbox pasteCheckbox;
    private iPartsGuiCombTextCompleteEditControl combTextControl; // Editor für kombinierte Texte
    private iPartsRelatedInfoInlineEditFootnoteDataForm footnoteForm;
    private iPartsFootnoteEditInlineDialog footnoteEditor; // Editor für Fußnoten
    private iPartsRelatedInfoFactoryDataForm factoryDataForm;

    public EditUserMultiChangeControlsForASPartlistEntry(EditModuleFormIConnector dataConnector,
                                                         AbstractJavaViewerForm parentForm, EtkEditFields externalEditFields,
                                                         List<EtkDataPartListEntry> selectedEntries) {
        super(dataConnector, parentForm, externalEditFields, null, false, true, true, UnifySource.AFTERSALES);
        this.tableName = TABLE_KATALOG;
        // Erst eine "Platzhalter"-Stücklistenposition mit den default Werten für die Edit-Fields erzeugen
        this.iPartsPseudoEntry = createPartlistEntryWithDefaultValues(dataConnector, selectedEntries, externalEditFields);
        postPostCreateGui();
        // Muss nach postPostCreateGui() aufgerufen werden, weil dort die Editoren initialisiert werden
        setCombTexts(selectedEntries);
        setFootnotes(selectedEntries);
        factoryDataForm.dataChanged();
    }

    /**
     * Bestimmt auf Basis aller Fußnoten den Initialwert für Fußnoten
     *
     * @param selectedEntries
     */
    private void setFootnotes(List<EtkDataPartListEntry> selectedEntries) {
        // Bestimme den Initialwert für den Fußnotendialog
        List<iPartsFootNote> currentFootnotes = null;
        iPartsDataPartListEntry partListEntryForForm = null;
        EtkDataAssembly assembly = null;
        for (EtkDataPartListEntry entry : selectedEntries) {
            if (assembly == null) {
                assembly = entry.getOwnerAssembly();
            }
            if (entry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
                Collection<iPartsFootNote> footnotes = partListEntry.getFootNotes();
                if ((currentFootnotes == null) && (footnotes != null) && !footnotes.isEmpty()) {
                    currentFootnotes = new ArrayList<>(footnotes);
                    partListEntryForForm = partListEntry;
                } else {
                    if ((footnotes != null) && !footnotes.isEmpty() && !hasSameFootnotes(currentFootnotes, new ArrayList<>(footnotes))) {
                        currentFootnotes = null;
                        partListEntryForForm = null;
                        break;
                    }
                }
            }
        }
        // Hatten alle ausgewählten Positionen die gleichen Fußnoten und/oder keine Fußnoten, dann setze diese bei der
        // "Platzhalter"-Stücklistenposition
        if (currentFootnotes != null) {
            iPartsPseudoEntry.setFootNotes(currentFootnotes);
            iPartsFootnoteEditInlineDialog dialogForExistingPartlistEntry = new iPartsFootnoteEditInlineDialog(getConnector(), getParentForm(), partListEntryForForm);
            dialogForExistingPartlistEntry.reloadFootNotes();
            List<iPartsFootnoteEditInlineDialog.FootNoteWithType> originalFootnotes = dialogForExistingPartlistEntry.getOriginalFootnotes();
            footnoteEditor.setFootnotes(originalFootnotes);
        }
    }

    @Override
    protected void postPostCreateGui() {
        super.postPostCreateGui();
        if (iPartsPseudoEntry != null) {
            // Verknüpfe die Editcontrols für die Baumuster- und die SAA/BK Gültigkeiten.
            iPartsEditUserControlsHelper.connectModelAndSaaBkValidityControls(editControls, iPartsConst.FIELD_K_MODEL_VALIDITY, iPartsConst.FIELD_K_SA_VALIDITY);

            GuiPanel panelMaster = new GuiPanel(); // Panel für die aufgeschnappten Editoren
            panelMaster.setLayout(new LayoutBorder());
            GuiSplitPane splitpane = new GuiSplitPane();
            splitpane.setHorizontal(false);
            panelMaster.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, splitpane) {
                @Override
                public void fireOnce(Event event) {
                    int value = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                    splitpane.setDividerPosition(value / 2);
                }
            });
            splitpane.setDividerSize(10);
            panelMaster.addChildBorderCenter(splitpane);

            // Fußnoten
            boolean doResize = addFootnoteEditorToMasterPanel(splitpane);
            // Werksdaten
            doResize |= addFactoryDataEditorTomasterPanel(splitpane);
            if (doResize) {
                splitpane.setDividerPosition(140);
                resizeUnifyDialog(panelMaster);
            }
        }
    }

    @Override
    public void dispose() {
        if (footnoteForm != null) {
            footnoteForm.dispose();
        }
        if (footnoteEditor != null) {
            footnoteEditor.dispose();
        }
        if (factoryDataForm != null) {
            factoryDataForm.dispose();
        }
        super.dispose();
    }

    private boolean addFactoryDataEditorTomasterPanel(GuiSplitPane masterSplitpane) {
        // Editor für Werksdaten
        AbstractGuiControl factoryControl = getFactoryDataEditorAndPreviewFromRelInfo();
        if (factoryControl != null) {
            GuiDockingPanel factoryDataDockingPanel = createDockingControl("!!Werksdaten", "!!Werksdaten anzeigen");
            GuiPanel factoryDataPanel = new GuiPanel(); // Panel für die Fußnotenverarbeitung
            factoryDataPanel.setLayout(new LayoutGridBag());
            factoryDataPanel.setConstraints(new ConstraintsBorder());
            addFactoryDataDialog(factoryDataPanel, factoryControl);
            factoryDataDockingPanel.addChild(factoryDataPanel);
            masterSplitpane.addChild(factoryDataDockingPanel);
            return true;
        }
        return false;
    }

    private boolean addFootnoteEditorToMasterPanel(GuiSplitPane masterSplitpane) {
        // Editor für Fußnoten
        AbstractGuiControl footnoteControl = getFootnoteEditorAndPreviewFromRelInfo();
        if (footnoteControl != null) {
            GuiDockingPanel footNoteDockingPanel = createDockingControl("!!Fußnoten", "!!Fußnoten anzeigen");
            GuiPanel footNotePanel = new GuiPanel(); // Panel für die Fußnotenverarbeitung
            footNotePanel.setLayout(new LayoutGridBag());
            addFootNoteDialog(footNotePanel, footnoteControl);
            footNotePanel.setConstraints(new ConstraintsBorder());
            footNoteDockingPanel.addChild(footNotePanel);
            masterSplitpane.addChild(footNoteDockingPanel);
            return true;
        }
        return false;
    }

    protected GuiDockingPanel createDockingControl(String textHide, String textShow) {
        GuiDockingPanel dockingPanel = new GuiDockingPanel();
        dockingPanel.setBackgroundColor(new java.awt.Color(255, 255, 255, 255));
        dockingPanel.setForegroundColor(new java.awt.Color(0, 0, 0, 255));
        dockingPanel.setTextHide(textHide);
        dockingPanel.setTextShow(textShow);
        dockingPanel.setImageHide(new FrameworkConstantImage("imgDesignDockingPanelSouth"));
        dockingPanel.setImageShow(new FrameworkConstantImage("imgDesignDockingPanelNorth"));
        dockingPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
        dockingPanel.setButtonBackgroundColor(new FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
        dockingPanel.setButtonForegroundColor(new FrameworkConstantColor("clDesignButtonBorderSelected"));
        dockingPanel.setButtonFill(true);
        dockingPanel.setStartWithArrow(false);
        return dockingPanel;
    }

    protected void resizeUnifyDialog(GuiPanel panel) {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        int height = (int)(screenSize.getHeight() / 100) * 100;
        height = getCalculatedHeight(height);
        int dividerPosition = getDividerPosition(height);
        dividerPosition += 30; // etwas mehr Platz, damit Divider nicht direkt anliegt
//        GuiScrollPane scrollPane = new GuiScrollPane();
//        scrollPane.addChild(panel);
//        addChildAsSplitPaneElement(scrollPane, dividerPosition, height);
        addChildAsSplitPaneElement(panel, dividerPosition, height);
    }

    protected void addFactoryDataDialog(GuiPanel panel, AbstractGuiControl factoryControl) {
        if (factoryControl != null) {
            pasteCheckbox = createCheckBox();
            // ChangeEvent einhängen
            OnChangeEvent onChangeEvent = () -> pasteCheckbox.setSelected(factoryDataForm.hasElementsToShow());
            factoryDataForm.setOnChangeEvent(onChangeEvent);

            int insetLeft = 36; // 36px für horizontale Ausrichtung mit den Checkboxen vom Vereinheitlichen
            int insetTop = 4;
            int insetBottom = 0;
            panel.addChildGridBag(pasteCheckbox, 0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_NONE, insetTop, insetLeft, insetBottom, 4);
            // Label für Fußnoten
            GuiLabel label = createLabel("factoryDataLabel", "!!Werksdaten:");
            panel.addChildGridBag(label, 1, 0, 2, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, insetTop, 4, insetBottom, 8);

            GuiPanel helpPanel = new GuiPanel();
            helpPanel.setLayout(new LayoutBorder());
            GuiScrollPane scrollPane = new GuiScrollPane();
            scrollPane.addChild(helpPanel);
            helpPanel.addChildBorderCenter(factoryControl);
            panel.addChildGridBag(scrollPane, 0, 1, 3, 1, 100, 100, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_BOTH, 0, 8, insetBottom, 8);
        }
    }

    protected void addFootNoteDialog(GuiPanel panel, AbstractGuiControl footnoteControl) {
        // Checkbox für Fußnoten
        footnoteCheckbox = createCheckBox();
        footnoteEditor.setOnGridSelectionChangedEvents(() -> footnoteCheckbox.setSelected(true));
        int insetLeft = 36; // 36px für horizontale Ausrichtung mit den Checkboxen vom Vereinheitlichen
        int insetTop = 4;
        int insetBottom = 0;
        panel.addChildGridBag(footnoteCheckbox, 0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_NORTH, ConstraintsGridBag.FILL_NONE, insetTop, insetLeft, insetBottom, 4);
        // Label für Fußnoten
        GuiLabel label = createLabel("footnoteLabel", "!!Fußnoten:");
        panel.addChildGridBag(label, 1, 0, 2, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, insetTop, 4, insetBottom, 8);

        GuiPanel helpPanel = new GuiPanel();
        helpPanel.setLayout(new LayoutBorder());
        GuiScrollPane scrollPane = new GuiScrollPane();
        scrollPane.addChild(helpPanel);
        helpPanel.addChildBorderCenter(footnoteControl);
        panel.addChildGridBag(scrollPane, 0, 1, 3, 1, 100, 100, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_BOTH, 0, 8, insetBottom, 8);
    }

    @Override
    protected void addEditControlChild(EditControl ctrl) {
        // Alle Controls sollen einheitlich breit dargestellt werden, wobei 600 Pixel aufgrund vom Fußnoten-Edit eine
        // gute Ansicht ergibt.
        AbstractGuiControl control = ctrl.getEditControl().getControl();
        control.setMinimumWidth(600);
        super.addEditControlChild(ctrl);
    }

    @Override
    protected ConstraintsGridBag createHeadingConstraints() {
        ConstraintsGridBag headingConstraints = super.createHeadingConstraints();

        // Spart Platz, indem das Label die ersten beiden Spalten überspannt
        headingConstraints.setGridwidth(2);
        headingConstraints.setAnchor(ConstraintsGridBag.ANCHOR_WEST);
        return headingConstraints;
    }

    /**
     * Bestimmt auf Basis aller kombinierten Texte den Initialwert für kombinierte Texte
     *
     * @param selectedEntries
     */
    private void setCombTexts(List<EtkDataPartListEntry> selectedEntries) {
        if ((selectedEntries == null) || selectedEntries.isEmpty()) {
            return;
        }
        if (combTextControl != null) {
            iPartsDataCombTextList currentMultiObjects = null;
            String currentMultiText = null;
            boolean differentMultiText = false;
            iPartsDataCombTextList currentNeutralObjects = null;
            String currentNeutralText = null;
            boolean differentNeutralText = false;
            for (EtkDataPartListEntry entry : selectedEntries) {
                if (entry instanceof iPartsDataPartListEntry) {
                    // Map mit nach Textart aufgeteilten Texte
                    Map<DictTextKindTypes, iPartsDataCombTextList> textKindCombTextMap
                            = iPartsEditCombTextHelper.getTextKindToCombTextMap(getProject(),
                                                                                iPartsDataCombTextList.loadForPartListEntry(entry.getAsId(), null, getProject()),
                                                                                combTextControl.getTextKinds());
                    // Überprüfen, ob der sprachabhängige Teil gleich ist
                    if (!differentMultiText) {
                        // Aktueller sprachabhängiger Text
                        currentMultiObjects = textKindCombTextMap.get(DictTextKindTypes.ADD_TEXT);
                        String tempMultiText = "";
                        // Haben wir schon einen sprachabhängigen Text von einer vorherigen Stücklistenposition
                        if (currentMultiObjects != null) {
                            // Aktueller Sprachabhängiger Teil
                            tempMultiText = currentMultiObjects.getCombTexts(null, getProject()).get(entry.getAsId().getKLfdnr());
                            if (tempMultiText == null) {
                                tempMultiText = "";
                            }
                        }
                        // Wenn es keinen voeherigen Text gab, dann den aktuellen setzen
                        if (currentMultiText == null) {
                            currentMultiText = tempMultiText;
                        } else {
                            // Es gab schon einen Text -> Check, ob gleich mit dem aktuellen
                            if (!currentMultiText.equals(tempMultiText)) {
                                differentMultiText = true;
                            }
                        }
                    }
                    // Überprüfen, ob der sprachneutrale Teil gleich ist
                    if (!differentNeutralText) {
                        currentNeutralObjects = textKindCombTextMap.get(DictTextKindTypes.NEUTRAL_TEXT);
                        String tempNeutralText = "";
                        if (currentNeutralObjects != null) {
                            tempNeutralText = textKindCombTextMap.get(DictTextKindTypes.NEUTRAL_TEXT).getCombTexts(null, getProject()).get(entry.getAsId().getKLfdnr());
                            if (tempNeutralText == null) {
                                tempNeutralText = "";
                            }
                        }
                        if (currentNeutralText == null) {
                            currentNeutralText = tempNeutralText;
                        } else {
                            if (!currentNeutralText.equals(tempNeutralText)) {
                                differentNeutralText = true;
                            }
                        }
                    }
                    // Sind sprachabhängige und sprachneutrale Texte unterschiedlich, dann können wir hier raus
                    if (differentMultiText && differentNeutralText) {
                        break;
                    }
                }
            }
            iPartsDataCombTextList combTexts = new iPartsDataCombTextList();
            // Textelemente aufsammeln, die gleich sind und weitergegeben werden sollen
            fillCombTextWithTextElements(combTexts, differentMultiText, currentMultiObjects);
            fillCombTextWithTextElements(combTexts, differentNeutralText, currentNeutralObjects);

            combTextControl.switchOffEventListeners();
            combTextControl.setPartListEntryId(iPartsPseudoEntry.getAsId(), combTexts);
            if (differentMultiText || differentNeutralText) {
                combTextControl.showKeepTextsEntries(differentMultiText, differentNeutralText);
            }
            combTextControl.switchOnEventListeners();
        }

    }

    /**
     * Sammelt die übergebenen Texte auf, sofern sie nicht unterschiedlich waren
     *
     * @param combTexts
     * @param hasDifferentTexts
     * @param currentObjects
     */
    private void fillCombTextWithTextElements(iPartsDataCombTextList combTexts, boolean hasDifferentTexts, iPartsDataCombTextList currentObjects) {
        if (!hasDifferentTexts && (currentObjects != null)) {
            for (iPartsDataCombText combText : currentObjects) {
                // Da nur als Platzhalter, kann hier das Original-Objekt genommen werden; Werte werden auch nur mit
                // "FROM_DB" verändert
                combText.setFieldValue(FIELD_DCT_SEQNO, "", DBActionOrigin.FROM_DB);
                combText.updateIdFromPrimaryKeys();
                combTexts.add(combText, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Liefert die {@link iPartsDataCombText} Objekte, die im Edit geändert oder neu angelegt wurden
     *
     * @return
     */
    public iPartsDataCombTextList getCombText() {
        if (getSelectedCheckBoxValues().contains(TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT))) {
            return combTextControl.getAllCombTexts(true);
        }
        return null;
    }

    /**
     * LIefert zurück, ob die aktuellen Textänderungen via Editor erfolgt sind
     *
     * @return
     */
    private boolean isEditedByEditor() {
        if (combTextControl != null) {
            return combTextControl.isEditedByEditor();
        }
        return false;
    }

    /**
     * Liefert die Fußnoten Objekte, die im Edit geändert oder neu angelegt wurden
     *
     * @return
     */
    public GenericEtkDataObjectList<EtkDataObject> getFootnotes() {
        if (footnoteCheckbox.isSelected()) {
            return footnoteEditor.getDataToSaveToChangeSet(false);
        }
        return null;
    }

    /**
     * Liefert die Werksdaten aus dem Session Zwischenspeicher die eingefügt werden sollen
     *
     * @return
     */
    public iPartsDataFactoryDataList getPastedFactoryData(EtkDataPartListEntry partListEntry, iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfiltered) {
        if (isPasteCheckboxSelected()) {
            return CopyAndPasteData.pasteFactoryDataOfPart(partListEntry, allFactoryDataForPartlistEntryUnfiltered, getProject());
        }
        return null;
    }

    public boolean isPasteCheckboxSelected() {
        return pasteCheckbox.isSelected();
    }

    public void setActualCopyCache() {
        if (factoryDataForm != null) {
            if (factoryDataForm.copyFactoryData(false)) {
                return;
            }
        }
        CopyAndPasteData.clearCopyCache();
    }

    @Override
    protected boolean isOneCheckBoxChecked() {
        boolean somethingSelected = false;
        if (!super.isOneCheckBoxChecked()) {
            if (footnoteCheckbox != null) {
                somethingSelected = footnoteCheckbox.isSelected();
            }
            if (pasteCheckbox != null) {
                somethingSelected |= isPasteCheckboxSelected();
            }
        } else {
            somethingSelected = true;
        }
        return somethingSelected;
    }

    /**
     * Liefert die GUI für die Bearbeitung von Fußnoten wie bei den Related-Infos. Diese beinhaltet den Editor und die
     * Preview-Anzeige.
     *
     * @return
     */
    private AbstractGuiControl getFootnoteEditorAndPreviewFromRelInfo() {
        if (iPartsPseudoEntry != null) {
            iPartsRelatedInfoFootNote relatedInfo = new iPartsRelatedInfoFootNote();
            RelatedInfoFormConnector connector = new RelatedInfoFormConnector(getConnector());
            connector.getRelatedInfoData().setKatInfosForPartList(iPartsPseudoEntry, iPartsPseudoEntry.getOwnerAssemblyId());
            connector.setEditContext(new iPartsRelatedInfoEditContext());
            final RelatedInfoBaseForm form = relatedInfo.newDisplayFormInstance(connector, getParentForm());
            form.addOwnConnector(connector);
            if (form instanceof iPartsRelatedInfoInlineEditFootnoteDataForm) {
                footnoteForm = (iPartsRelatedInfoInlineEditFootnoteDataForm)form;
                footnoteEditor = footnoteForm.getFootnoteEditor();
            }
            return form.getGui();
        }
        return null;
    }

    private AbstractGuiControl getFactoryDataEditorAndPreviewFromRelInfo() {
        if (iPartsPseudoEntry != null) {
            iPartsRelatedInfoFactoryData relatedInfo = new iPartsRelatedInfoFactoryData();
            RelatedInfoFormConnector connector = new RelatedInfoFormConnector(getConnector());
            connector.getRelatedInfoData().setKatInfosForPartList(iPartsPseudoEntry, iPartsPseudoEntry.getOwnerAssemblyId());
            iPartsRelatedInfoEditContext context = iPartsRelatedInfoEditContext.createEditContext(getConnector(), true);
            connector.setEditContext(context);

            final RelatedInfoBaseForm form = relatedInfo.newDisplayFormInstance(connector, getParentForm());
            form.addOwnConnector(connector);
            if (form instanceof iPartsRelatedInfoFactoryDataForm) {
                factoryDataForm = (iPartsRelatedInfoFactoryDataForm)form;
                factoryDataForm.setUsedInUnify(true);
            }
            return form.getGui();
        }
        return null;
    }

    @Override
    protected String calculateInitialValue(EtkEditField field, DBDataObjectAttribute attrib) {
        return iPartsPseudoEntry.getFieldValue(field.getKey().getFieldName());
    }

    @Override
    protected EtkDataArray calculateInitialDataArray(EtkEditField field, DBDataObjectAttribute attrib) {
        if (field.isArray()) {
            return iPartsPseudoEntry.getFieldValueAsArray(field.getKey().getFieldName());
        }
        return null;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
        if (iPartsPseudoEntry == null) {
            return;
        }

        int defaultWidth = DEFAULT_WIDTH_ATTRIBUTE_FIELD;
        String fieldName = field.getKey().getFieldName();
        if (field.getKey().getTableName().equals(iPartsConst.TABLE_KATALOG) &&
            field.getKey().getFieldName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiCombTextCompleteEditControl) {
                combTextControl = (iPartsGuiCombTextCompleteEditControl)ctrl.getEditControl().getControl();
                combTextControl.setConnector(getConnector());
                combTextControl.setMultiEdit(true);
                defaultWidth = DEFAULT_WIDTH_EDITOR_FIELD;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_MODEL_VALIDITY)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiModelSelectTextField) {
                iPartsGuiModelSelectTextField modelTextField = (iPartsGuiModelSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = iPartsPseudoEntry.getOwnerAssembly().getProductIdFromModuleUsage();
                modelTextField.setWithRadioButtons(true);
                modelTextField.init(parentForm);
                modelTextField.setProductId(productId);
                defaultWidth = DEFAULT_WIDTH_EDITOR_FIELD;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_SA_VALIDITY)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiSAABkSelectTextField) {
                iPartsGuiSAABkSelectTextField saaTextField = (iPartsGuiSAABkSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = iPartsPseudoEntry.getOwnerAssembly().getProductIdFromModuleUsage();
                saaTextField.setWithRadioButtons(true);
                saaTextField.init(parentForm, productId, iPartsPseudoEntry.getFieldValueAsArray(iPartsConst.FIELD_K_MODEL_VALIDITY).getArrayAsStringList());
                defaultWidth = DEFAULT_WIDTH_EDITOR_FIELD;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiPSKVariantsSelectTextField) {
                if (iPartsRight.checkPSKInSession()) {
                    if (iPartsPseudoEntry.getOwnerAssembly().isPSKAssembly()) {
                        iPartsGuiPSKVariantsSelectTextField pskVariantsTextField = (iPartsGuiPSKVariantsSelectTextField)ctrl.getEditControl().getControl();
                        iPartsProductId productId = iPartsPseudoEntry.getOwnerAssembly().getProductIdFromModuleUsage();
                        pskVariantsTextField.setWithRadioButtons(true);
                        pskVariantsTextField.init(parentForm, productId);
                        pskVariantsTextField.addDataArrayFromSelection(iPartsPseudoEntry.getFieldValueAsArray(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY).getArrayAsStringList());
                        defaultWidth = DEFAULT_WIDTH_EDITOR_FIELD;
                    }
                }
            }
        } else if (iPartsEditPlugin.isCodeField(ctrl.getEditControl().getTableFieldName())) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiCodeTextField) {
                iPartsGuiCodeTextField codeTextField = (iPartsGuiCodeTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = iPartsPseudoEntry.getOwnerAssembly().getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                    String series = "";
                    if (product.getReferencedSeries() != null) {
                        series = product.getReferencedSeries().getSeriesNumber();
                    }
                    iPartsDocumentationType documentationType = iPartsPseudoEntry.getOwnerAssembly().getDocumentationType();
                    String productGroup = iPartsPseudoEntry.getFieldValue(FIELD_K_PRODUCT_GRP);
                    codeTextField.init(getConnector().getProject(), documentationType, series, productGroup, "", "", iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
                }
                codeTextField.setText(iPartsPseudoEntry.getFieldValue(fieldName));
                defaultWidth = DEFAULT_WIDTH_EDITOR_FIELD;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_EVENT_FROM) || fieldName.equals(iPartsConst.FIELD_K_EVENT_TO)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiEventSelectComboBox) {
                iPartsGuiEventSelectComboBox eventSelectComboBox = (iPartsGuiEventSelectComboBox)ctrl.getEditControl().getControl();
                eventSelectComboBox.init(getProject(), iPartsPseudoEntry.getSeriesId(), initialValue);
            }
        }
        ctrl.getEditControl().getControl().setMaximumWidth(defaultWidth);
    }

    private static boolean hasSameFootnotes(List<iPartsFootNote> currentFootnotes, List<iPartsFootNote> footnotes) {
        if ((currentFootnotes == null) || (footnotes == null) || (currentFootnotes.size() != footnotes.size())) {
            return false;
        }
        for (int index = 0; index < footnotes.size(); index++) {
            iPartsFootNote currentFootnote = currentFootnotes.get(index);
            iPartsFootNote footnote = footnotes.get(index);
            if (!currentFootnote.getFootNoteId().equals(footnote.getFootNoteId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean checkValues() {
        boolean result = super.checkValues();
        if (result) {
            if ((attributes != null) && (tableName != null)) {
                List<EtkEditField> fields = editFields.getVisibleEditFields();
                for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
                    EtkEditField field = fields.get(fieldIndex);
                    String tablename = field.getKey().getTableName();
                    String fieldname = field.getKey().getFieldName();
                    if (iPartsEditPlugin.isCodeField(TableAndFieldName.make(tablename, fieldname))) {
                        if ((editControls != null) && (editControls.size() > 0)) {
                            EditControl ctrl = editControls.getControlByFeldIndex(fieldIndex);
                            GuiCheckbox checkBox = getCheckBoxForControl(ctrl.getEditControl());
                            // Wenn die Checkbox für das Codefeld nicht ausgewählt ist, dann brauch man den
                            // Check nicht machen.
                            if (!checkBox.isSelected()) {
                                continue;
                            }
                        }
                        iPartsGuiCodeTextField codeTextField = iPartsEditUserControlsHelper.getCodeFieldFromEditControls(fieldIndex, editControls);
                        if (codeTextField != null) {
                            if (!codeTextField.checkInput()) {
                                if (!StrUtils.isValid(iPartsPseudoEntry.getFieldValue(FIELD_K_PRODUCT_GRP))) {
                                    codeTextField.setErrorMessage("!!Code können nicht vereinheitlicht werden, da die Produktgruppen der selektierten Stücklisteneinträge unterschiedlich sind!");
                                }
                                // Falls es Fehler gibt, ist das Ergebnis auf jedenfall falsch
                                // Bei Warnungen ist es richtig
                                if (codeTextField.hasErrorMessage()) {
                                    result = false;
                                }
                                codeTextField.showErrorOrWarningMessage();
                            }
                            //Um den Fehler erneut anzeigen zu können
                            codeTextField.setX4eWarningWasShown(false);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected String getLabelText() {
        return LABEL_FOR_AS_MULTI_EDIT;
    }

    /**
     * Für die PSK-Variantengültigkeit, Baumuster und SAA/BK Gültigkeiten gibt es die Möglichkeit anstatt die Werte zu vereinheitlichen,
     * die Werte hinzuzufügen.
     * Klasse speichert für alle Gültigkeiten, ob hinzugefügt oder vereinheitlicht werden soll
     */
    private static class IsAddOrEqualizeValidities {

        private Map<String, Boolean> fieldNameToAddValidityMap = new HashMap<>();

        public IsAddOrEqualizeValidities(EditUserMultiChangeControlsForASPartlistEntry multiControl) {
            createMapEntry(multiControl, FIELD_K_MODEL_VALIDITY);
            createMapEntry(multiControl, FIELD_K_PSK_VARIANT_VALIDITY);
            createMapEntry(multiControl, FIELD_K_SA_VALIDITY);
        }

        private void createMapEntry(EditUserMultiChangeControlsForASPartlistEntry multiControl, String fieldName) {
            EditControl editControlOfFieldName = multiControl.getEditControlByFieldName(fieldName);
            if (editControlOfFieldName != null) {
                AbstractGuiControl guiControl = editControlOfFieldName.getEditControl().getControl();
                if (guiControl instanceof iPartsGuiArraySelectionTextField) {
                    iPartsGuiArraySelectionTextField arrayTextField = (iPartsGuiArraySelectionTextField)guiControl;
                    fieldNameToAddValidityMap.put(fieldName, arrayTextField.isSecondRadioButtonSelected());
                }
            }
        }

        public boolean isAddModelValidity() {
            return Utils.objectEquals(fieldNameToAddValidityMap.get(FIELD_K_MODEL_VALIDITY), Boolean.TRUE);
        }

        public boolean isAddPSKVariantsValidity() {
            return Utils.objectEquals(fieldNameToAddValidityMap.get(FIELD_K_PSK_VARIANT_VALIDITY), Boolean.TRUE);
        }

        public boolean isAddSAABkValidity() {
            return Utils.objectEquals(fieldNameToAddValidityMap.get(FIELD_K_SA_VALIDITY), Boolean.TRUE);
        }
    }

}
