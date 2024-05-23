/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoDescription;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditHotSpotHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist.EditImportPSKForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.sap_ctt.EditImportCTTForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.NotImplementedCode;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Form zur Darstellung des Headers zu einem Modul für Edit
 */
public class EditHeaderForm extends AbstractJavaViewerForm implements iPartsConst {

    private EtkFunction closeTabFunction;
    private EditToolbarButtonMenuHelper toolbarHelper;
    private boolean isEditAllowed;
    private boolean isAuthorOrderActive;
    private GuiMultiLangEdit nameMultiLangEdit;
    private boolean isUndoModuleRunning = false;

    /**
     * Erzeugt eine Instanz von EditHeaderForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditHeaderForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EtkFunction closeTabFunction, boolean showCloseButton) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.closeTabFunction = closeTabFunction;
        this.isEditAllowed = getConnector().isAuthorOrderValid();
        this.isAuthorOrderActive = isRevisionChangeSetActive();
        postCreateGui(showCloseButton);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(boolean showCloseButton) {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarEditHeader);
        toolbarManager = toolbarHelper.getToolbarManager();
        //toolbarManager = new GuiToolbarManager(mainWindow.toolbarEditHeader);
        //die EvoBus-Felder raus
        mainWindow.labelValidFrom.setVisible(false);
        mainWindow.dateedittextfieldValidFrom.setVisible(false);
        mainWindow.labelValidTo.setVisible(false);
        mainWindow.dateedittextfieldValidTo.setVisible(false);
        mainWindow.labelStatus.setVisible(false);
        mainWindow.labelStatusShow.setVisible(false);

        //textField für Name gegen MultiLangEdit austauschen
        AbstractConstraints constraints = mainWindow.textfieldName.getConstraints();
        nameMultiLangEdit = new GuiMultiLangEdit();
        nameMultiLangEdit.setName("moduleheader_name");
        nameMultiLangEdit.setConstraints(constraints);
        nameMultiLangEdit.setMinimumWidth(mainWindow.textfieldName.getMinimumWidth());
        nameMultiLangEdit.setMinimumHeight(mainWindow.textfieldName.getMinimumHeight());
        nameMultiLangEdit.setReadOnly(true); // Benennung soll nicht editiert werden können (DAIMLER-5901)
        nameMultiLangEdit.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                nameChanged(event);
            }
        });
        mainWindow.panelWork.removeChild(mainWindow.textfieldName);
        mainWindow.panelWork.addChild(nameMultiLangEdit);

        createToolbarButtons(showCloseButton);
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        if (isEditAllowed != editAllowed) {
            isEditAllowed = editAllowed;
            nameMultiLangEdit.setReadOnly(!isEditAllowed);
            enableButtons();
        }
    }

    public boolean isAuthorOrderActive() {
        return isAuthorOrderActive;
    }

    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }


    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        try {
            // Falls eine große Änderung, dann muss alles neu gemacht werden
            forceUpdateAll = forceUpdateAll || getConnector().isFlagFilterChanged() || getConnector().isFlagCurrentAssemblyChanged()
                             || getConnector().isFlagRootAssemblyChanged() || getConnector().isAnyLanguageChanged();
            if (forceUpdateAll || nameMultiLangEdit.getMultiLanguage().allStringsAreEmpty()) {
                updateHeaderForm();
            } else {
                enableButtons();
            }
        } finally {

        }
    }

    private void updateHeaderForm() {
        // EtkMultiSprache mit allen DB-Sprachen aus dem Material erzeugen
        DBDataObjectAttribute textNrAttribute = getConnector().getCurrentAssembly().getPart().getAttribute(EtkDbConst.FIELD_M_TEXTNR);
        EtkMultiSprache multiLanguage;
        if (textNrAttribute != null) {
            EtkMultiSprache assemblyDBMultiLang = getConnector().getCurrentAssembly().getPart().loadMultiLanguageFromDB(textNrAttribute);
            multiLanguage = new EtkMultiSprache();

            // Alle eingestellten Sprachen mit den Werten aus dem Material versorgen.
            // ACHTUNG! Es können dadurch auch leere Einträge entstehen.
            for (String language : getConnector().getConfig().getDatabaseLanguages()) {
                multiLanguage.setText(language, assemblyDBMultiLang.getText(language));
            }
        } else {
            multiLanguage = new EtkMultiSprache("??", getConnector().getConfig().getDatabaseLanguages());
        }

        nameMultiLangEdit.setMultiLanguage(multiLanguage);
        nameMultiLangEdit.setStartLanguage(getProject().getDBLanguage());

        updateNameAndTypeLabel();
        enableButtons();
    }

    private void updateNameAndTypeLabel() {
        // DAIMLER-15041: setzen
        boolean showBold = true;
        StringBuilder str = new StringBuilder();
        if (showBold) {
            str.append("<html>");
        }
        str.append(TranslationHandler.translate("!!Benennung"));
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        iPartsModuleTypes moduleType = iPartsModuleTypes.getType(currentAssembly.getEbeneName());
        if (moduleType != iPartsModuleTypes.UNKNOWN) {
            str.append(" (");
            str.append(TranslationHandler.translate("!!Technischer Umfang"));

            // Dokumentationsmethode
            if (currentAssembly instanceof iPartsDataAssembly) {
                str.append(" - ");
                if (showBold) {
                    str.append("<b>");
                }
                str.append(TranslationHandler.translate("!!Dokumentationsmethode:"));
                str.append(" ");
                String docuTypeToken = ((iPartsDataAssembly)currentAssembly).getModuleMetaData().getDocumentationType().getDBValue();
                str.append(getProject().getVisObject().asText(TABLE_DA_MODULE, FIELD_DM_DOCUTYPE,
                                                              docuTypeToken, getProject().getDBLanguage()));
                if (showBold) {
                    str.append("</b>");
                }
            }

            str.append(") ");
            if (showBold) {
                str.append("</html>");
            }
        }
        mainWindow.labelName.setText(str.toString());
        String labelText = "";
        if (StrUtils.isValid(getConnector().getAdditionalTextForHeader())) {
            labelText = getConnector().getAdditionalTextForHeader();
        }
        mainWindow.labelKEM.setText(labelText);
        AbstractGuiToolComponent guiToolComponent = getToolbarManager().getButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL.getAlias());
        if (guiToolComponent != null) {
            if (EditModuleHelper.isCarPerspectiveAssembly(getConnector().getCurrentAssembly())) {
                guiToolComponent.setTooltip("!!Import NaviCar Excel-Datei");
            } else {
                guiToolComponent.setTooltip(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL.getTooltip());
            }
        }
    }

    private void createToolbarButtons(boolean showCloseButton) {
        if ((closeTabFunction != null) && showCloseButton) {
            toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_CLOSE_TAB, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doCloseTab(event);
                }
            });
            toolbarHelper.addSeparator(EditToolbarButtonAlias.EDIT_CLOSE_TAB.getAlias() + "_separator");
        }

        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_SAVE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doSave(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_UNDO, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doUndo(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_UNDOMODULE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doDeleteModule(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doDeleteEmptyModuleInTechnicalChangeSet(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_HDR_USAGE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doUsage(event);
            }
        });
//        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_HDR_PRINT, new EventListener(Event.ACTION_PERFORMED_EVENT) {
//            @Override
//            public void fire(Event event) {
//                doPrint(event);
//            }
//        });
//        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_SETTING, new EventListener(Event.ACTION_PERFORMED_EVENT) {
//            @Override
//            public void fire(Event event) {
//                doSetting(event);
//            }
//        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_MODULE_MASTER_DATA, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doShowRelatedInfo(event);
            }
        });

        if (iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            GuiToolButton moduleInEditButton = toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_MODULE_IN_EDIT, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doShowModuleInEdit(event);
                }
            });
            updateModuleInEditButton(moduleInEditButton);
        }

        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_MODULE_HISTORY, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                EditAuthorOrderView.showModuleHistoryView(getConnector(), EditHeaderForm.this, getConnector().getCurrentAssembly());

            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_TEST, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doTest(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                iPartsEditValidationHelper.validateAssembly(getConnector(), EditHeaderForm.this);
            }
        });

        toolbarHelper.addSeparator(EditToolbarButtonAlias.EDIT_IMPORT_CTT.getAlias() + "_separator");
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doImportSapCTT(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doImportPSKorExcel(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doImportPSKorExcel(event);
            }
        });

    }

    private void updateModuleInEditButton(GuiToolButton moduleInEditButton) {
        iPartsDataAuthorOrderList authorOrderList = EditModuleHelper.getActiveAuthorOrderListForModule(getConnector().getCurrentAssembly().getAsId(),
                                                                                                       getProject());
        moduleInEditButton.setEnabled(!authorOrderList.isEmpty());
        if (authorOrderList.isEmpty()) {
            moduleInEditButton.setGlyph(EditDefaultImages.edit_btn_module_in_edit.getImage());
            moduleInEditButton.setTooltip("!!TU wird nicht bearbeitet");
        } else {
            boolean otherAuthorOrderFound = false;
            iPartsDataAuthorOrder currentDataAuthorOrder = getConnector().getCurrentDataAuthorOrder();
            if (currentDataAuthorOrder != null) {
                // Wird das Modul aktuell gerade noch in einem anderen nicht-freigegebenen Autoren-Auftrag bearbeitet?
                for (iPartsDataAuthorOrder dataAuthorOrder : authorOrderList) {
                    if (!dataAuthorOrder.getAsId().equals(currentDataAuthorOrder.getAsId())) {
                        otherAuthorOrderFound = true;
                        break;
                    }
                }
            }

            if (otherAuthorOrderFound) {
                moduleInEditButton.setGlyph(EditDefaultImages.edit_btn_module_in_conflict.getImage());
                moduleInEditButton.setTooltip("!!TU wird in einem anderen Autoren-Auftrag bearbeitet");
            } else {
                moduleInEditButton.setGlyph(EditDefaultImages.edit_btn_module_in_edit.getImage());
                moduleInEditButton.setTooltip(EditToolbarButtonAlias.EDIT_MODULE_IN_EDIT.getTooltip());
            }
        }
    }

    private void enableButtons() {
        if (isEditAllowed) {
            boolean isModified = getConnector().getCurrentAssembly().isModifiedWithChildren();
            boolean isDeletable = iPartsModuleTypes.isModuleDeletable(getConnector().getCurrentAssembly().getEbeneName());
            // isTableSorting/FilteringActive berücksichtigen
            toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_SAVE, isModified);
            //isModified
            toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNDO, isModified);
            //isModified
            if (checkIfModuleCanBeDeleted()) {
                toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNDOMODULE, isDeletable);
                toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_UNDOMODULE);
            } else {
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_UNDOMODULE);
            }
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE);
            toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_HDR_USAGE, true);
            toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_HDR_PRINT, true);
            toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_TEST, true);

            // Excel Importe nur zulassen, wenn der User dazu berechtigt ist, und ein Changeset aktiv hat
            if (iPartsRight.EXECUTE_EXCEL_IMPORT.checkRightInSession() && isRevisionChangeSetActiveForEdit()) {
                // EDIT_IMPORT_CTT disablen bei DIALOG
                iPartsDataAssembly assembly = (iPartsDataAssembly)getConnector().getCurrentAssembly();
                iPartsDocumentationType documentationType = assembly.getDocumentationType();
                boolean isCTTVisible = !documentationType.isPKWDocumentationType();
                boolean isPSKVisible = documentationType.isPSKDocumentationType() && iPartsRight.checkPSKInSession() &&
                                       iPartsProduct.getInstance(getProject(), assembly.getProductIdFromModuleUsage()).isPSK();
                boolean isExcelVisible = true; // Excel-Import ist IMMER möglich
                if (isCTTVisible && isPSKVisible) {
                    // bei PSK ist der SAP.CTT Import nicht erlaubt
                    isCTTVisible = false;
                }
                if (isExcelVisible && isPSKVisible) {
                    // bei PSK ist der Excel Import inklusive
                    isExcelVisible = false;
                }
                // nachdem einer der Importbuttons immer sichtbar ist => Separator ist sichtbar
                // Sichtbarkeit des Separators steuern
                toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT.getAlias() + "_separator");

                if (isCTTVisible) {
                    toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT);
                    toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT, true);
                } else {
                    toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT);
                }
                if (isPSKVisible) {
                    toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK);
                    toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK, true);
                } else {
                    toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK);
                }
                if (isExcelVisible) {
                    toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL);
                    toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL, true);
                } else {
                    toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL);
                }
            } else {
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT);
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT.getAlias() + "_separator");
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK);
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL);
            }
        } else {
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_SAVE);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_UNDO);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_UNDOMODULE);
            if (!isRevisionChangeSetActive() && iPartsRight.DELETE_EMPTY_TUS.checkRightInSession()) {
                toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE);
            } else {
                toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE);
            }
            toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_HDR_USAGE);
            toolbarHelper.showToolbarButton(EditToolbarButtonAlias.EDIT_HDR_PRINT);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_TEST);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_CTT.getAlias() + "_separator");
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_PSK);
            toolbarHelper.hideToolbarButton(EditToolbarButtonAlias.EDIT_IMPORT_EXCEL);
        }
    }


    /**
     * Überprüft, ob das aktuelle Modul gelöscht werden darf.
     * <p>
     * Das Modul darf gelöscht werden, wenn
     * - keine Stücklisteneinträge vorhanden sind und
     * - entweder keine Bildaufträge oder alle Bildaufträge im Status ungültig oder abgeschlossen sind
     * - wenn das Benutzer-Recht es erlaubt
     * - oder das Modul im aktiven Autorenauftrag erzeugt wurde
     * <p>
     * <p>
     * Das Modul darf nur gelöscht werden, wenn kein Autorenauftrag erzeugt wurde. Enthält das Modul keine Bildaufträge,
     * dann darf es gelöscht werden, wenn das Benutzer-Recht es erlaubt oder das Modul im aktiven Autorenauftrag erzeugt wurde.
     *
     * @return
     */
    private boolean checkIfModuleCanBeDeleted() {
//        EditModuleFormIConnector editModuleFormIConnector = getConnector();
//        boolean result;
//        // Check, ob ein Bildauftrag vorhanden ist
//        if (editModuleFormIConnector.getPictureOrderList().isEmpty()) {
//            result = true;
//        } else {
//            // besitzen alle Bildaufträge den Status  ungültig oder abgeschlossen
//        }
//
//
//

        // Recht prüfen
        if (iPartsRight.DELETE_MASTER_DATA.checkRightInSession()) {
            return true;
        }
        EditModuleFormIConnector editModuleFormIConnector = getConnector();
        // Check, ob ein Bildauftrag vorhanden ist
        if (!editModuleFormIConnector.getPictureOrderList().isEmpty()) {
            return false;
        }
        // Check, ob das Modul via aktuellen Autorenauftrag erzeugt wurde
        if (isRevisionChangeSetActiveForEdit()) {
            return (getRevisionsHelper() != null) && getRevisionsHelper().checkIfObjectCreatedInActiveEditChangeSet(getConnector().getCurrentAssembly().getAsId());
        }
        return false;
    }

    private void doCloseTab(Event event) {
        if (closeTabFunction != null) {
            if (askForClose()) {
                return;
            }
            closeTabFunction.run(this);
        }
    }

    public boolean askForClose() {
        if (!isUndoModuleRunning && getConnector().getCurrentAssembly().isModifiedWithChildren()) {
            if (MessageDialog.showYesNo("!!Sollen die Änderungen wirklich verworfen werden?", "!!Änderungen verwerfen") != ModalResult.YES) {
                return true; // Veto zum Schließen
            }
        }
        return false;
    }

    private void doSave(Event event) {
        if (isRevisionChangeSetActiveForEdit()) {
            getConnector().getCurrentAssembly().getAttributes().markAsModified(); // Modul auf jeden Fall als verändert markieren
            addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());
            EtkDataAssembly.removeDataAssemblyFromCache(getProject(), getConnector().getCurrentAssembly().getAsId());
            getProject().fireProjectEvent(new DataChangedEvent(null), true);
        } else {
            getDbLayer().startTransaction();
            try {
                getConnector().getCurrentAssembly().saveToDB();
                getDbLayer().commit();
                EtkDataAssembly.removeDataAssemblyFromCache(getProject(), getConnector().getCurrentAssembly().getAsId());
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<AssemblyId>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                                    iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                    getConnector().getCurrentAssembly().getAsId(), false));
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
                //TODO freischalten, wenn DAIMLER-938 bearbeitet ist
                //MessageDialog.show(TranslationHandler.translate("!!Das Modul /"%1/" wurde abgespeichert", nameMultiLangEdit.getMultiText(getProject().getViewerLanguage())));
            } catch (Exception e) {
                getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }

        getConnector().dataChanged(null);
    }

    /**
     * Speichern des aktuellen Moduls (von außen anstoßbar)
     *
     * @param moduleNo
     */
    public void doSaveModule(String moduleNo) {
        if (StrUtils.isValid(moduleNo) && getConnector().getCurrentAssembly().getAsId().getKVari().equals(moduleNo)) {
            doSave(null);
        }
    }

    private void doUndo(Event event) {
        if (MessageDialog.showYesNo(TranslationHandler.translate("!!Wollen Sie wirklich die Änderungen rückgängig machen?")) == ModalResult.YES) {
            AssemblyId currentAssembly = getConnector().getCurrentAssembly().getAsId();
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), currentAssembly, false);
            getConnector().setCurrentAssembly(assembly);
            getConnector().updateAllViews(this, true);
        }
    }

    private void doDeleteModule(Event event) {
        if (closeTabFunction != null) {
            // prüfen ob es in der Stückliste noch offenen Bearbeitungen gibt
            if (parentForm instanceof EditModuleForm) {
                EditModuleForm editModuleForm = (EditModuleForm)parentForm;
                if (editModuleForm.askAssemblyListForClose()) {
                    return;
                }
            }
            if (!EditModuleForm.prepareDeletingModule(getProject(), getConnector().getCurrentAssembly())) {
                return;
            }

            if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
                return;
            }

            try {
                EditModuleForm.deleteModuleInActiveChangeSet(getProject(), getConnector().getCurrentAssembly(), false);
                //Editor schließen
                isUndoModuleRunning = true;
                if (closeTabFunction != null) {
                    closeTabFunction.run(EditHeaderForm.this);
                }

            } finally {
                iPartsEditPlugin.stopEditing();
            }
        } else {
            showNotImplemented("DeleteModule");
        }
    }

    private void doDeleteEmptyModuleInTechnicalChangeSet(Event event) {
        if (isRevisionChangeSetActive()) {
            MessageDialog.showError("!!Sofortiges Löschen ist mit aktivem Autoren-Auftrag nicht möglich.");
            return;
        }

        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();

        // Modul darf keine Stücklisteneinträge enthalten
        if (!currentAssembly.getPartListUnfiltered(null).isEmpty()) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Der TU enthält noch Stücklisteneinträge.")
                                      + '\n' + TranslationHandler.translate("!!Sofortiges Löschen ist daher nicht möglich."),
                                      EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE.getTooltip());
            return;
        }

        // Modul darf keine offenen Bildaufträge enthalten
        if (!EditModuleForm.checkOpenPictureOrders(getProject(), currentAssembly)) {
            return;
        }

        // Modul darf nicht in nicht freigegebenen Autoren-Aufträgen bearbeitet werden
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, false, false));

        iPartsDataAuthorOrderList authorOrderList = new iPartsDataAuthorOrderList();
        authorOrderList.searchSortAndFillWithJoin(getProject(), null, selectFields,
                                                  new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                                TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID) },
                                                  new String[]{ AssemblyId.TYPE, currentAssembly.getAsId().toDBString() },
                                                  new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) },
                                                  new String[]{ iPartsChangeSetStatus.COMMITTED.name() },
                                                  false, null, false, false, null,
                                                  new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                 new String[]{ FIELD_DAO_CHANGE_SET_ID },
                                                                                 new String[]{ FIELD_DCS_GUID },
                                                                                 false, false),
                                                  new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET_ENTRY,
                                                                                 new String[]{ FIELD_DAO_CHANGE_SET_ID },
                                                                                 new String[]{ FIELD_DCE_GUID },
                                                                                 false, false));
        if (!authorOrderList.isEmpty()) {
            Set<String> authorOrderNames = new TreeSet<>();
            for (iPartsDataAuthorOrder dataAuthorOrder : authorOrderList) {
                authorOrderNames.add(dataAuthorOrder.getAuthorOrderName());
            }
            MessageDialog.showWarning(TranslationHandler.translate("!!Der TU wird noch in folgenden nicht freigegebenen Autoren-Aufträgen bearbeitet: %1",
                                                                   StrUtils.stringListToString(authorOrderNames, ", "))
                                      + '\n' + TranslationHandler.translate("!!Sofortiges Löschen ist daher nicht möglich."),
                                      EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE.getTooltip());
            return;
        }

        // Sicherheitsabfrage
        if (MessageDialog.showYesNo("!!Soll der TU wirklich sofort gelöscht werden?",
                                    EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE.getTooltip()) != ModalResult.YES) {
            return;
        }

        // Modul in einem technischen ChangeSet sofort löschen
        EditModuleForm.deleteModuleInActiveChangeSet(getProject(), currentAssembly, true);
        //Editor schließen
        isUndoModuleRunning = true;
        if (closeTabFunction != null) {
            closeTabFunction.run(EditHeaderForm.this);
        }

    }

    private void doUsage(Event event) {
        AssemblyId id = getConnector().getCurrentAssembly().getAsId();
        RelatedInfoFormConnector relatedConnector = createRelatedInfoConnector(id);
        relatedConnector.getRelatedInfoData().setActiveInfo(EtkRelatedInfoDescription.RELATEDINFO_USAGE);
        getConnector().showRelatedInfo(relatedConnector);
        relatedConnector.dispose();
    }

    private RelatedInfoFormConnector createRelatedInfoConnector(AssemblyId id) {
        RelatedInfoFormConnector relatedConnector = new RelatedInfoFormConnector(getConnector());

        PartId partId = getConnector().getCurrentAssembly().getPart().getAsId();
        AssemblyId rootAssemblyId = getConnector().getRootAssemblyId();
        relatedConnector.getRelatedInfoData().setKatInfosForAssembly(id, id, partId, rootAssemblyId);
        return relatedConnector;
    }

    private void doPrint(Event event) {
        showNotImplemented("Print");
    }

    private void doImportSapCTT(Event event) {
        if (!isRevisionChangeSetActiveForEdit()) {
            // Kann eigentlich nicht passieren, weil die Buttons im Nicht-Edit verriegelt sind
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }
        EditImportCTTForm dlg = new EditImportCTTForm(getConnector());
        if (ModalResult.OK == dlg.showModal()) {
            saveAndRefresh(dlg.getDataObjectsToBeSaved(), false);
        }
    }

    private void doImportPSKorExcel(Event event) {
        if (!isRevisionChangeSetActiveForEdit()) {
            // Kann eigentlich nicht passieren, weil die Buttons im Nicht-Edit verriegelt sind
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }

        iPartsDataAssembly assembly = (iPartsDataAssembly)getConnector().getCurrentAssembly();
        iPartsDocumentationType documentationType = assembly.getDocumentationType();
        boolean isSAAssembly = assembly.isSAAssembly();
        boolean isRealPSK;
        iPartsImportDataOrigin dataOrigin;
        if (isSAAssembly) {
            // PSK- und Firmenzugehörigkeit des SA TUs anhand aller Produkte bestimmen
            iPartsSA saData = iPartsSA.getInstance(getProject(), assembly.getSAId());
            isRealPSK = documentationType.isPSKDocumentationType() && iPartsRight.checkPSKInSession() &&
                        saData.isOnlyInPSKProducts(getProject());
            dataOrigin = calcForeignSourceForCreate(saData.isCarAndVanSA(getProject()), saData.isTruckAndBusSA(getProject()));
        } else {
            // PSK- und Firmenzugehörigkeit des Produktes TUs anhand des verknüpften Produkts bestimmen
            iPartsProduct product = iPartsProduct.getInstance(getProject(), assembly.getProductIdFromModuleUsage());
            isRealPSK = documentationType.isPSKDocumentationType() && iPartsRight.checkPSKInSession() &&
                        product.isPSK();
            dataOrigin = calcForeignSourceForCreate(product.isCarAndVanProduct(), product.isTruckAndBusProduct());
        }
        EditImportPSKForm dlg = new EditImportPSKForm(getConnector(), isRealPSK, documentationType.isTruckDocumentationType(), dataOrigin);
        if (ModalResult.OK == dlg.showModal()) {
            // Event verschicken
            saveAndRefresh(dlg.getDataObjectsToBeSaved(), dlg.isMatTextChanged());
        }
    }

    private iPartsImportDataOrigin calcForeignSourceForCreate(boolean isCarAndVan, boolean isTruckAndBus) {
        iPartsImportDataOrigin source = iPartsImportDataOrigin.getiPartsSourceForRights(isCarAndVan, isTruckAndBus);
        return (source != null) ? source : iPartsImportDataOrigin.getTypeFromCode(DictHelper.getIPartsSourceForCurrentSession());
    }

    private void saveAndRefresh(GenericEtkDataObjectList<EtkDataObject> dataObjectsToBeSaved, boolean reloadEditModule) {
        if (!dataObjectsToBeSaved.isEmpty() && isRevisionChangeSetActiveForEdit()) {
            addDataObjectListToActiveChangeSetForEdit(dataObjectsToBeSaved);
            getConnector().getCurrentAssembly().clearPartListEntriesForEdit();

            // Den AssemblyCache für das aktuelle Changeset und Assembly löschen
            EtkDataAssembly.removeDataAssemblyFromCache(getProject(), getConnector().getCurrentAssembly().getAsId());

            // Änderungen ins Changeset gespeichert werden
            getConnector().savePartListEntries(null, true);

            getConnector().dataChanged(null);
            if (reloadEditModule) {
                Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();
                modifiedAssemblyIds.add(getConnector().getCurrentAssembly().getAsId());
                iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblyIds, getConnector());
            }
        }
    }

    private void doShowRelatedInfo(Event event) {
        AssemblyId id = getConnector().getCurrentAssembly().getAsId();
        RelatedInfoFormConnector relatedConnector = createRelatedInfoConnector(id);
        relatedConnector.getRelatedInfoData().setActiveInfo(CONFIG_KEY_RELATED_INFO_MASTER_DATA);
        relatedConnector.setEditContext(iPartsRelatedInfoEditContext.createEditContext(relatedConnector, isEditAllowed));
        getConnector().showRelatedInfo(relatedConnector);
        relatedConnector.dispose();
        updateNameAndTypeLabel();
    }

    private void doShowModuleInEdit(Event event) {
        EditAuthorOrderListForm.showAuthorOrderListForModuleInEdit(getConnector(), this, getConnector().getCurrentAssembly().getAsId());
    }

    private void doTest(Event event) {
        List<String> warnings = new DwList<String>();
        getWarnings(warnings);

        if (warnings.isEmpty()) {
            MessageDialog.show("!!Es sind keine Warnungen vorhanden.");
        } else {
            MessageDialog.showWarning(warnings);
        }
    }

    public boolean getWarnings(List<String> warnings) {
        EditHotSpotHelper hotSpotHelper = new EditHotSpotHelper(getConnector());
        return hotSpotHelper.getFaultyHotspotsAndPOS(warnings);
    }

    private void nameChanged(Event event) {
        getConnector().getCurrentAssembly().getPart().setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, nameMultiLangEdit.getMultiLanguage(),
                                                                                   DBActionOrigin.FROM_EDIT);
        enableButtons();
    }

    private void showNotImplemented(String extraText) {
        NotImplementedCode.execute(NotImplementedCode.IPARTS_EDITOR + " " + extraText);
        MessageDialog.show("!!Funktion noch nicht implementiert.");
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel toolbarHolderPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarEditHeader;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelWork;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelKEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldName;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelValidFrom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelValidTo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelStatus;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.formattedfields.GuiDateEditTextField dateedittextfieldValidFrom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel JDS287735;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.formattedfields.GuiDateEditTextField dateedittextfieldValidTo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel VXC287737;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelStatusShow;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            toolbarHolderPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            toolbarHolderPanel.setName("toolbarHolderPanel");
            toolbarHolderPanel.__internal_setGenerationDpi(96);
            toolbarHolderPanel.registerTranslationHandler(translationHandler);
            toolbarHolderPanel.setScaleForResolution(true);
            toolbarHolderPanel.setMinimumWidth(10);
            toolbarHolderPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder toolbarHolderPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            toolbarHolderPanel.setLayout(toolbarHolderPanelLayout);
            toolbarEditHeader = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarEditHeader.setName("toolbarEditHeader");
            toolbarEditHeader.__internal_setGenerationDpi(96);
            toolbarEditHeader.registerTranslationHandler(translationHandler);
            toolbarEditHeader.setScaleForResolution(true);
            toolbarEditHeader.setMinimumWidth(10);
            toolbarEditHeader.setMinimumHeight(10);
            toolbarEditHeader.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarEditHeaderConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarEditHeader.setConstraints(toolbarEditHeaderConstraints);
            toolbarHolderPanel.addChild(toolbarEditHeader);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarHolderPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarHolderPanelConstraints.setPosition("north");
            toolbarHolderPanel.setConstraints(toolbarHolderPanelConstraints);
            panelMain.addChild(toolbarHolderPanel);
            panelWork = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelWork.setName("panelWork");
            panelWork.__internal_setGenerationDpi(96);
            panelWork.registerTranslationHandler(translationHandler);
            panelWork.setScaleForResolution(true);
            panelWork.setMinimumWidth(10);
            panelWork.setMinimumHeight(10);
            panelWork.setPaddingTop(4);
            panelWork.setPaddingLeft(4);
            panelWork.setPaddingRight(4);
            panelWork.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelWorkLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelWork.setLayout(panelWorkLayout);
            labelName = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelName.setName("labelName");
            labelName.__internal_setGenerationDpi(96);
            labelName.registerTranslationHandler(translationHandler);
            labelName.setScaleForResolution(true);
            labelName.setMinimumWidth(10);
            labelName.setMinimumHeight(10);
            labelName.setText("!!Benennung");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "n", 4, 0, 4, 0);
            labelName.setConstraints(labelNameConstraints);
            panelWork.addChild(labelName);
            labelKEM = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelKEM.setName("labelKEM");
            labelKEM.__internal_setGenerationDpi(96);
            labelKEM.registerTranslationHandler(translationHandler);
            labelKEM.setScaleForResolution(true);
            labelKEM.setMinimumWidth(10);
            labelKEM.setMinimumHeight(10);
            labelKEM.setText("!!Bezug zur KEM");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelKEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            labelKEM.setConstraints(labelKEMConstraints);
            panelWork.addChild(labelKEM);
            textfieldName = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldName.setName("textfieldName");
            textfieldName.__internal_setGenerationDpi(96);
            textfieldName.registerTranslationHandler(translationHandler);
            textfieldName.setScaleForResolution(true);
            textfieldName.setMinimumWidth(400);
            textfieldName.setMinimumHeight(10);
            textfieldName.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    nameChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldNameConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "n", 0, 0, 4, 4);
            textfieldName.setConstraints(textfieldNameConstraints);
            panelWork.addChild(textfieldName);
            labelValidFrom = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelValidFrom.setName("labelValidFrom");
            labelValidFrom.__internal_setGenerationDpi(96);
            labelValidFrom.registerTranslationHandler(translationHandler);
            labelValidFrom.setScaleForResolution(true);
            labelValidFrom.setMinimumWidth(10);
            labelValidFrom.setMinimumHeight(10);
            labelValidFrom.setText("!!Gültig von:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelValidFromConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "w", "n", 0, 4, 4, 4);
            labelValidFrom.setConstraints(labelValidFromConstraints);
            panelWork.addChild(labelValidFrom);
            labelValidTo = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelValidTo.setName("labelValidTo");
            labelValidTo.__internal_setGenerationDpi(96);
            labelValidTo.registerTranslationHandler(translationHandler);
            labelValidTo.setScaleForResolution(true);
            labelValidTo.setMinimumWidth(10);
            labelValidTo.setMinimumHeight(10);
            labelValidTo.setText("!!Gültig bis:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelValidToConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "w", "n", 0, 4, 4, 4);
            labelValidTo.setConstraints(labelValidToConstraints);
            panelWork.addChild(labelValidTo);
            labelStatus = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelStatus.setName("labelStatus");
            labelStatus.__internal_setGenerationDpi(96);
            labelStatus.registerTranslationHandler(translationHandler);
            labelStatus.setScaleForResolution(true);
            labelStatus.setMinimumWidth(10);
            labelStatus.setMinimumHeight(10);
            labelStatus.setText("!!Modulstatus");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelStatusConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "n", 0, 4, 0, 4);
            labelStatus.setConstraints(labelStatusConstraints);
            panelWork.addChild(labelStatus);
            dateedittextfieldValidFrom = new de.docware.framework.modules.gui.controls.formattedfields.GuiDateEditTextField();
            dateedittextfieldValidFrom.setName("dateedittextfieldValidFrom");
            dateedittextfieldValidFrom.__internal_setGenerationDpi(96);
            dateedittextfieldValidFrom.registerTranslationHandler(translationHandler);
            dateedittextfieldValidFrom.setScaleForResolution(true);
            dateedittextfieldValidFrom.setMinimumWidth(10);
            dateedittextfieldValidFrom.setMinimumHeight(10);
            JDS287735 = new de.docware.framework.modules.gui.controls.GuiLabel();
            JDS287735.setName("JDS287735");
            JDS287735.__internal_setGenerationDpi(96);
            JDS287735.registerTranslationHandler(translationHandler);
            JDS287735.setScaleForResolution(true);
            JDS287735.setText("Tag.Monat.Jahr");
            dateedittextfieldValidFrom.setTooltip(JDS287735);
            dateedittextfieldValidFrom.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag dateedittextfieldValidFromConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 4, 0);
            dateedittextfieldValidFrom.setConstraints(dateedittextfieldValidFromConstraints);
            panelWork.addChild(dateedittextfieldValidFrom);
            dateedittextfieldValidTo = new de.docware.framework.modules.gui.controls.formattedfields.GuiDateEditTextField();
            dateedittextfieldValidTo.setName("dateedittextfieldValidTo");
            dateedittextfieldValidTo.__internal_setGenerationDpi(96);
            dateedittextfieldValidTo.registerTranslationHandler(translationHandler);
            dateedittextfieldValidTo.setScaleForResolution(true);
            dateedittextfieldValidTo.setMinimumWidth(10);
            dateedittextfieldValidTo.setMinimumHeight(10);
            VXC287737 = new de.docware.framework.modules.gui.controls.GuiLabel();
            VXC287737.setName("VXC287737");
            VXC287737.__internal_setGenerationDpi(96);
            VXC287737.registerTranslationHandler(translationHandler);
            VXC287737.setScaleForResolution(true);
            VXC287737.setText("Tag.Monat.Jahr");
            dateedittextfieldValidTo.setTooltip(VXC287737);
            dateedittextfieldValidTo.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag dateedittextfieldValidToConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 2, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 4, 0);
            dateedittextfieldValidTo.setConstraints(dateedittextfieldValidToConstraints);
            panelWork.addChild(dateedittextfieldValidTo);
            labelStatusShow = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelStatusShow.setName("labelStatusShow");
            labelStatusShow.__internal_setGenerationDpi(96);
            labelStatusShow.registerTranslationHandler(translationHandler);
            labelStatusShow.setScaleForResolution(true);
            labelStatusShow.setMinimumWidth(10);
            labelStatusShow.setMinimumHeight(10);
            labelStatusShow.setText("keiner");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelStatusShowConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 3, 1, 1, 0.0, 0.0, "w", "n", 0, 4, 0, 0);
            labelStatusShow.setConstraints(labelStatusShowConstraints);
            panelWork.addChild(labelStatusShow);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 1, 1, 3, 100.0, 0.0, "c", "b", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panelWork.addChild(label_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelWorkConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelWork.setConstraints(panelWorkConstraints);
            panelMain.addChild(panelWork);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}