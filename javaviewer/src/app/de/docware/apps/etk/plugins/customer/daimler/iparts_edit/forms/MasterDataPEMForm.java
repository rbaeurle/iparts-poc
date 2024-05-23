/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.events.OnCreateAttributesEvent;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.forms.events.OnEditOrViewAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Stammdaten-Dialog zum Recherchieren, Anlegen und Bearbeiten von PEM Stammdaten
 */
public class MasterDataPEMForm extends SimpleMasterDataSearchFilterGrid {

    private static final String CONFIG_KEY_SEARCH_FIELDS = iPartsEditConfigConst.iPARTS_EDIT_MASTER_PEM_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS;
    private static final String CONFIG_KEY_DISPLAY_FIELDS = iPartsEditConfigConst.iPARTS_EDIT_MASTER_PEM_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;
    private static final String CONFIG_KEY_EDIT_FIELDS = iPartsEditConfigConst.iPARTS_EDIT_MASTER_PEM_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS;

    private final boolean isForEdit;
    private final PEMDataHelper.PEMDataOrigin pemDataOrigin;
    private boolean hasBothVehicleRights;

    /**
     * Zeige den Stammdaten-Dialog für den Edit an. Hierbei ist das Anlegen, Editieren und Löschen von iParts PEMs erlaubt.
     * Als Ergebnis wird die ausgewählte PEM geliefert.
     *
     * @param activeForm
     * @param factory
     * @param pemDataOrigin nur bei Truck befüllt
     * @return
     */
    public static String showMasterDataForEdit(AbstractJavaViewerForm activeForm, String factory, String pem, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        if (StrUtils.isValid(factory)) {
            MasterDataPEMForm pemForm = new MasterDataPEMForm(activeForm.getConnector(), activeForm, TABLE_DA_PEM_MASTERDATA,
                                                              createOnEditChangeEvent(), true, factory, pem, pemDataOrigin);
            if (pemForm.showModal() == ModalResult.OK) {
                return pemForm.getSelectedPEM();
            }
        }
        return null;
    }

    /**
     * Zeigt den Stammdaten-Dialog zum Recherchieren an. Hierbei ist das Editieren und Löschen von iParts PEMs erlaubt.
     *
     * @param owner
     */
    public static void showPEMMasterData(AbstractJavaViewerForm owner) {
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        MasterDataPEMForm pemForm = new MasterDataPEMForm(activeForm.getConnector(), activeForm, TABLE_DA_PEM_MASTERDATA,
                                                          createOnEditChangeEvent(), false, null, null, null);
        pemForm.showModal();
    }

    /**
     * Erzeugt den {@link OnEditChangeRecordEvent} für die Editfunktionen des PEM Stamdmaten-Dialogs
     *
     * @return
     */
    private static OnEditChangeRecordEvent createOnEditChangeEvent() {
        return new OnEditChangeRecordEvent() {
            @Override
            public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                EtkProject project = dataConnector.getProject();
                // Erzeuge aus den Daten ein iPartsDataPem Objekt (noch nicht initialisiert)
                iPartsDataPem dataPem = createPEMDataObjectFromAttributes(project, attributes);
                if (dataPem.existsInDB()) {
                    // Meldung, falls es schon existiert
                    String msg = TranslationHandler.translate("!!Der PEM Stamm zur PEM \"%1\" und zum Werk \"%2\"" +
                                                              " existiert bereits.", dataPem.getAsId().getPEM(),
                                                              dataPem.getAsId().getFactoryNo());
                    MessageDialog.show(msg, "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                    return false;
                } else {
                    dataPem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    if (!setSourceForPEM(attributes)) {
                        return false;
                    }
                }
                return storePEMMasterData(project, dataPem, attributes);
            }

            /**
             * Setzt die Quelle der PEM in Abhängigkeit von den Benutzereigenschaften und dem Gültigkeitsbereich der PEM
             *
             * @param attributes
             * @return
             */
            private boolean setSourceForPEM(DBDataObjectAttributes attributes) {
                // Quelle IPARTS setzen, wenn der Benutzer beide Eigenschaften besitzt. Ansonsten Eigenschaften-
                // abhängige Quelle setzen
                if (!attributes.fieldExists(FIELD_DPM_SOURCE) || StrUtils.isEmpty(attributes.getFieldValue(FIELD_DPM_SOURCE))) {
                    if (iPartsRight.checkUserHasBothVehicleTypeRightsInSession()) {
                        // Hat der Benutzer beide Eigenschaften
                        boolean hasPEMSource = attributes.fieldExists(FIELD_DPM_PRODUCT_NO) && StrUtils.isValid(attributes.getFieldValue(FIELD_DPM_PRODUCT_NO));
                        attributes.addField(FIELD_DPM_SOURCE, hasPEMSource ? iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin() : iPartsImportDataOrigin.IPARTS_MB.getOrigin(), DBActionOrigin.FROM_DB);
                    } else if (iPartsRight.checkCarAndVanInSession()) {
                        attributes.addField(FIELD_DPM_SOURCE, iPartsImportDataOrigin.IPARTS_MB.getOrigin(), DBActionOrigin.FROM_DB);
                    } else if (iPartsRight.checkTruckAndBusInSession()) {
                        attributes.addField(FIELD_DPM_SOURCE, iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin(), DBActionOrigin.FROM_DB);
                    } else {
                        MessageDialog.show("!!Fehlende Rechte beim Anlegen der PEM Stammdaten", "!!Erzeugen", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                EtkProject project = dataConnector.getProject();
                // Erzeuge aus den Daten ein iPartsDataPem Objekt (noch nicht initialisiert)
                iPartsDataPem dataPem = createPEMDataObjectFromAttributes(project, attributes);
                if (!dataPem.existsInDB()) {
                    String msg = TranslationHandler.translate("!!Der PEM Stamm zur PEM \"%1\" und zum Werk \"%2\"" +
                                                              " existiert nicht.", dataPem.getAsId().getPEM(),
                                                              dataPem.getAsId().getFactoryNo());
                    MessageDialog.show(msg, "!!Editieren", MessageDialogIcon.ERROR, MessageDialogButtons.OK);
                    return false;
                }
                // Wir dürfen nur PEM Stammdaten ändern, die in iParts erzeugt wurden
                if (!PEMDataHelper.isIZVPem(dataPem.getFieldValue(FIELD_DPM_PEM))) {
                    return false;
                }
                return storePEMMasterData(project, dataPem, attributes);
            }

            /**
             * Speichert den PEM Stamm, der via Stammdaten-Dialog erzeugt/verändert wurde.
             * @param project
             * @param dataPem
             * @param attributes
             * @return
             */
            private boolean storePEMMasterData(EtkProject project, iPartsDataPem dataPem, DBDataObjectAttributes attributes) {
                // Die neuen Attribute zuweisen
                dataPem.assignAttributesValues(project, attributes, true, DBActionOrigin.FROM_EDIT);
                dataPem.setFieldValueAsDateTime(FIELD_DPM_ADAT, GregorianCalendar.getInstance(), DBActionOrigin.FROM_EDIT);
                project.getDbLayer().startTransaction();
                try {
                    // Im einem technischen Changeset abspeichern
                    if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(project, dataPem, iPartsChangeSetSource.PEM)) {
                        dataPem.saveToDB();
                        project.getDbLayer().commit();

                        // Daten haben sich geändert -> Filterung muss z.B. neu durchgeführt werden
                        project.fireProjectEvent(new DataChangedEvent());
                        // Im Edit werden alle PEMs zu einem Werk in einem lokalen Cache gehalten. Dieser lokale Cache
                        // muss nach einer Neuanlage oder einer Änderung neu erzeugt werden.
                        if (StrUtils.isValid(dataPem.getAsId().getFactoryNo())) {
                            iPartsDataChangedEventByEdit.Action action
                                    = dataPem.existsInDB() ? iPartsDataChangedEventByEdit.Action.MODIFIED : iPartsDataChangedEventByEdit.Action.NEW;
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PEM,
                                                                                                                      action,
                                                                                                                      dataPem.getAsId(),
                                                                                                                      false));
                        }

                        return true;
                    } else {
                        project.getDbLayer().rollback();
                        return false;
                    }
                } catch (Exception e) {
                    project.getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }

                return false;
            }

            /**
             * Erzeugt aus den übergebenen Attributen ein {@link iPartsDataPem} Objekt, das noch nicht initialisiert wurde
             * @param project
             * @param attributes
             * @return
             */
            private iPartsDataPem createPEMDataObjectFromAttributes(EtkProject project, DBDataObjectAttributes attributes) {
                String pem = attributes.getFieldValue(FIELD_DPM_PEM);
                String factory = attributes.getFieldValue(FIELD_DPM_FACTORY_NO);
                iPartsPemId pemId = new iPartsPemId(pem, factory);
                return new iPartsDataPem(project, pemId);
            }

            @Override
            public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                if ((attributeList != null) && !attributeList.isEmpty()) {
                    String msg;
                    // Es wurde nur ein Datensatz zum Löschen ausgewählt -> Sicherheits-Check, ob es ein IPARTS Datensatz ist
                    if (attributeList.size() == 1) {
                        DBDataObjectAttributes attributes = attributeList.get(0);
                        String pem = attributes.getFieldValue(FIELD_DPM_PEM);
                        String factory = attributes.getFieldValue(FIELD_DPM_FACTORY_NO);
                        if (!PEMDataHelper.isIZVPem(attributes)) {
                            msg = TranslationHandler.translate("!!Die ausgewählte PEM \"%1\" zum Werk \"%2\" wurde importiert und darf nicht gelöscht werden!", pem, factory);
                            MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.INFORMATION, MessageDialogButtons.YES);
                            return false;
                        } else {
                            // prüfen ob es zur PEM bereits Rückmeldedaten gibt
                            List<iPartsDataResponseData> responseData = iPartsResponseData.getInstance(dataConnector.getProject()).getResponseData(pem);
                            if ((responseData == null) || responseData.isEmpty()) {
                                msg = TranslationHandler.translate("!!Möchten Sie die Stammdaten zur PEM \"%1\" und Werk \"%2\" wirklich löschen?", pem, factory);
                            } else {
                                msg = TranslationHandler.translate("!!Zur ausgewählten PEM \"%1\" zum Werk \"%2\" existieren bereits " +
                                                                   "Rückmeldedaten. Die PEM darf daher nicht gelöscht werden!", pem, factory);
                                MessageDialog.show(msg, "!!Löschen");
                                return false;
                            }
                        }

                    } else {
                        // Es wurden mehrere Datensätze zum Löschen ausgewählt -> Es werden nur die gelöscht, die in iParts angelegt wurden
                        Set<String> noDeletionSetSource = new HashSet<>();
                        Set<String> noDeletionSetResponseData = new HashSet<>();
                        Set<String> deletionSet = new HashSet<>();
                        for (DBDataObjectAttributes attributes : attributeList) {
                            String pem = attributes.getFieldValue(FIELD_DPM_PEM);
                            String factory = attributes.getFieldValue(FIELD_DPM_FACTORY_NO);
                            if (!PEMDataHelper.isIZVPem(attributes)) {
                                noDeletionSetSource.add(TranslationHandler.translate("!!PEM: \"%1\" - Werk: \"%2\"",
                                                                                     pem, factory));
                            } else {
                                String source = attributes.getFieldValue(FIELD_DPM_SOURCE);
                                // prüfen ob es zur PEM bereits Rückmeldedaten gibt
                                List<iPartsDataResponseData> responseData = iPartsResponseData.getInstance(dataConnector.getProject()).getResponseData(pem);
                                if ((responseData == null) || responseData.isEmpty()) {
                                    deletionSet.add(TranslationHandler.translate("!!PEM: \"%1\" - Werk: \"%2\" - Quelle: \"%3\"",
                                                                                 pem, factory, source));
                                } else {
                                    noDeletionSetResponseData.add(TranslationHandler.translate("!!PEM: \"%1\" - Werk: \"%2\" - Quelle: \"%3\"",
                                                                                               pem, factory, source));
                                }
                            }
                        }
                        if (noDeletionSetSource.isEmpty() && noDeletionSetResponseData.isEmpty()) {
                            // Alle Einträge wurden in iParts angelegt -> alle Löschen
                            msg = TranslationHandler.translate("!!Möchten Sie die Stammdaten zu den ausgewählten PEMs wirklich löschen?");
                        } else {
                            StringBuilder builder = new StringBuilder();
                            if (!noDeletionSetSource.isEmpty()) {
                                builder.append(TranslationHandler.translate("!!Die Stammdaten zu folgenden PEM - Werk Variationen " +
                                                                            "können nicht gelöscht werden, da sie importiert wurden!"));
                                builder.append("\n\n");
                                for (String pemFactory : noDeletionSetSource) {
                                    builder.append(pemFactory + "\n");
                                }
                                builder.append("\n\n");
                            }
                            if (!noDeletionSetResponseData.isEmpty()) {
                                builder.append(TranslationHandler.translate("!!Die Stammdaten zu folgenden PEM - Werk Variationen " +
                                                                            "können nicht gelöscht werden, da für sie bereits " +
                                                                            "Rückmeldedaten existieren!"));
                                builder.append("\n\n");
                                for (String pemFactory : noDeletionSetResponseData) {
                                    builder.append(pemFactory + "\n");
                                }
                                builder.append("\n\n");
                            }

                            builder.append(TranslationHandler.translate("!!Es werden gelöscht:"));
                            builder.append("\n\n");
                            for (String pemFactory : deletionSet) {
                                builder.append(pemFactory + "\n");
                            }
                            msg = builder.toString();
                        }
                    }
                    return MessageDialog.show(msg, "!!Löschen", MessageDialogIcon.CONFIRMATION,
                                              MessageDialogButtons.YES, MessageDialogButtons.NO) == ModalResult.YES;
                }
                return false;
            }

            @Override
            public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName,
                                                   DBDataObjectAttributesList attributeList) {
                if ((attributeList != null) && !attributeList.isEmpty()) {
                    EtkProject project = dataConnector.getProject();
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    dbLayer.startBatchStatement();
                    try {
                        Set<iPartsPemId> deletedPemIds = new TreeSet<>();

                        // Lösche alle PEM Stammdaten, die in iParts erzeugt wurden und für die es keine Rückmeldedaten gibt
                        for (DBDataObjectAttributes attributes : attributeList) {
                            iPartsDataPem dataPem = createPEMDataObjectFromAttributes(project, attributes);
                            if (dataPem.existsInDB() && (PEMDataHelper.isIZVPem(attributes))) {
                                List<iPartsDataResponseData> responseData = iPartsResponseData.getInstance(dataConnector.getProject()).getResponseData(dataPem.getAsId().getPEM());
                                if ((responseData == null) || responseData.isEmpty()) {
                                    if (iPartsRevisionChangeSet.deleteDataObjectWithChangeSet(project, dataPem, iPartsChangeSetSource.PEM)) {
                                        deletedPemIds.add(dataPem.getAsId());
                                        dataPem.deleteFromDB(true);
                                    } else {
                                        dbLayer.cancelBatchStatement();
                                        dbLayer.rollback();
                                        return false;
                                    }
                                }
                            }
                        }
                        dbLayer.endBatchStatement();
                        dbLayer.commit();

                        // Im Edit werden alle PEMs zu einem Werk in einem lokalen Cache gehalten. Dieser lokale Cache
                        // muss nach einer Neuanlage oder einer Änderung neu erzeugt werden.
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit(iPartsDataChangedEventByEdit.DataType.PEM,
                                                                                                                iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                                                deletedPemIds,
                                                                                                                false));

                        // Daten haben sich geändert -> Filterung muss z.B. neu durchgeführt werden
                        project.fireProjectEvent(new DataChangedEvent());
                        return true;

                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }

                }
                return false;
            }
        };
    }


    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param onEditChangeRecordEvent
     * @param pemDataOrigin           nur bei Truck befüllt
     */
    public MasterDataPEMForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                             String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent, boolean isForEdit,
                             String factory, String pem, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        this.isForEdit = isForEdit;
        this.pemDataOrigin = pemDataOrigin;
        this.hasBothVehicleRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
        initForm(factory, pem, pemDataOrigin);
    }

    private void initForm(final String factory, String pem, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        EtkDisplayFields searchFields = new EtkDisplayFields(); // Suchfelder
        EtkDisplayFields displayFields = new EtkDisplayFields(); // Anzeige-Felder (Suchergebnis)
        EtkEditFields editFields = new EtkEditFields(); // Editier-Felder
        // Such-, Anzeige- und Edit-Felder initialisieren
        fillFormFields(getConnector(), searchFields, displayFields, editFields);
        // Stammdatendialog für den Edit
        if (isForEdit) {
            // Der Standard für den Stammdatendialog sieht vor, dass es nur einen "OK" Button gibt. Da der PEM
            // Stammdatendialog aber zum Recherchieren verwendet wird muss für den Edit ein "OK" und ein "Cancel" Button
            // vorhanden sein.
            getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
            getButtonPanel().setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
            // Beim Erzeugen, dürfen nicht alle Editfields editierbar sein (Werk ist fest)
            setAllEditFieldsEditableForCreation(false);
            // Nachdem ein Datensatz angelegt wurde, soll die Suche mit den Werten des neuen Datensatzes starten
            setDoSearchWithNewAttributesAfterCreation(true);
            // Standardverhalten bei einem Doppelklick ist das Öffnen des ausgewählten Datensatzes zum Editieren,
            // hier soll der Doppelklick aber den Eintrag auswählen und den Dialog schließen
            setOnDblClickEvent(new OnDblClickEvent() {
                @Override
                public void onDblClick() {
                    closeWithModalResult(ModalResult.OK);
                }
            });
            // sicherstellen dass das Suchfeld für Werk immer da ist und als erstes angezeigt wird
            EtkDisplayField factoryField = searchFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, false);
            if (factoryField == null) {
                factoryField = createSearchField(getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, false, false);
            } else {
                int index = searchFields.getIndexOfFeld(factoryField);
                searchFields.removeField(index);
            }
            // Im Edit muss das Werk so gesucht werden, wie es übergeben wird. Sonst werden z.B. für Werk "050" auch
            // Werk "0501" Daten angezeigt
            factoryField.setSearchExact(true);
            searchFields.addFeld(0, factoryField);

            // Produkt Suchfeld nur anzeigen, wenn productId != null
            // bei ELDAS, bei freien SAs oder innerhalb der PEM Stammdaten die Produktnummer auch als SuchFeld mit anzeigen
            EtkDisplayField searchField = searchFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, false);
            if (pemDataOrigin != null) {
                if (searchField == null) {
                    searchField = createSearchField(getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, false, false);
                    if (pemDataOrigin.isSA()) {
                        // Bei freien SAs wird hart auf die SA eingegrenzt
                        searchField.setEditierbar(false);
                    }
                    searchFields.addFeld(searchField);
                }
            } else {
                if (searchField != null) {
                    searchFields.removeField(searchField);
                }
            }

            // Einsatztermin als EditField hinzufügen falls es nicht konfiguriert ist und als Muss-Feld setzen
            EtkEditField editField = editFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE);
            if (editField == null) {
                editField = createEditField(getConnector().getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE, false);
                editFields.addFeld(editField);
            }

            // Die Initialwerte für das Anlegen von einem neuen PEM Stamm -> Die PEM wird generiert und besteht aus
            // - Prefix IZV
            // - Die letzten zwei Ziffern des Jahres JJ
            // und einer vierstelligen Zahl, die immer um 1 erhöht wird.
            // Bsp.: IZV190001
            setOnCreateEvent(new OnCreateAttributesEvent() {
                @Override
                public DBDataObjectAttributes onCreateAttributesEvent() {
                    DBDataObjectAttributes initialValues = new DBDataObjectAttributes();
                    String lastDigitsYear = DateUtils.getCurrentDateFormatted("YY");
                    String nextASPEMNumber = getNextASPemNumber(lastDigitsYear);
                    if (StrUtils.isValid(nextASPEMNumber)) {
                        initialValues.addField(FIELD_DPM_PEM, PEMDataHelper.AS_PEM_PREFIX + lastDigitsYear + nextASPEMNumber, DBActionOrigin.FROM_DB);
                    }
                    initialValues.addField(FIELD_DPM_FACTORY_NO, factory, DBActionOrigin.FROM_DB);
                    if (pemDataOrigin != null) {
                        initialValues.addField(FIELD_DPM_PRODUCT_NO, pemDataOrigin.getPemOriginValue(), DBActionOrigin.FROM_DB);
                        // ProduktFeld nicht verändert werden
                        enableEditField(FIELD_DPM_PRODUCT_NO, false);
                    }
                    return initialValues;
                }
            });
            setOnEditOrViewEvent(new OnEditOrViewAttributesEvent() {

                @Override
                public DBDataObjectAttributes onEditOrViewAttributesEvent(DBDataObjectAttributes existingAttributes, boolean editAllowed) {
                    if (pemDataOrigin != null) {
                        // ProduktFeld nicht verändert werden
                        enableEditField(FIELD_DPM_PRODUCT_NO, false);
                    }
                    return existingAttributes;
                }
            });
        }

        if (isForEdit) {
            setSize(1130, 600);
        } else {
            doResizeWindow(SCREEN_SIZES.SCALE_FROM_PARENT);
        }

        // Such-, Anzeige- und Edit-Felder setzen
        setSearchFields(searchFields);
        setDisplayResultFields(displayFields);
        setRequiredResultFields(new EtkDisplayFields()); // damit mindestens die Primärschlüsselfelder mit geladen werden
        setEditFields(editFields);

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_DPM_PEM, false);
        sortFields.put(FIELD_DPM_FACTORY_NO, false);
        sortFields.put(FIELD_DPM_ADAT, true);
        setSortFields(sortFields);

        if (isForEdit) {
            // Suchfeld sperren für den Edit (Werk darf nicht verändert werden)
            setEditControlEnabled(FIELD_DPM_FACTORY_NO, false);
            // Das vorgegebene Werk als festen Suchwert übergeben
            DBDataObjectAttributes searchAttributes = new DBDataObjectAttributes();
            searchAttributes.addField(FIELD_DPM_FACTORY_NO, factory, DBActionOrigin.FROM_DB);

            if (pemDataOrigin != null) {
                searchAttributes.addField(FIELD_DPM_PRODUCT_NO, pemDataOrigin.getPemOriginValue(), DBActionOrigin.FROM_DB);
                if (pemDataOrigin.isSA()) {
                    // Bei freien SAs darf die SA nicht verändert werden, da wir nur PEMs innerhalb der SA betrachten
                    setEditControlEnabled(FIELD_DPM_PRODUCT_NO, false);
                }
            }

            // wenn die PEM Nummer als Suchfeld konfiguriert ist, und eine PEM als Initialwert übergeben wurde, diese setzen
            if (StrUtils.isValid(pem) && (searchFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, false) != null)) {
                searchAttributes.addField(FIELD_DPM_PEM, pem, DBActionOrigin.FROM_DB);
            }
            // starte Suche
            setSearchValues(searchAttributes);
        }

        // Rechte überprüfen
        boolean editMasterDataAllowed = iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
        boolean createAndDeleteAllowed = iPartsRight.CREATE_DELETE_PEM_DATA.checkRightInSession();

        setEditAllowed(editMasterDataAllowed || createAndDeleteAllowed);
        // Neuanlage nur im Edit und mich dem richtigen Recht möglich
        setNewAllowed(isForEdit && createAndDeleteAllowed);
        setModifyAllowed(editMasterDataAllowed);
        setDeleteAllowed(createAndDeleteAllowed);

        setTitlePrefix("!!PEM");
        setWindowName("PEMMasterData");
        setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
    }

    private void enableEditField(String fieldName, boolean isEditable) {
        EtkEditField editField = editFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, fieldName, false);
        if (editField != null) {
            editField.setEditierbar(isEditable);
        }
    }

    /**
     * Liefert die nächste vierstellige Nummer zum Erzeugen einer iParts PEM. Hierbei wird in der DB nach dem Prefix
     * "IZVJJ*" gesucht wobei "JJ" für die letzten zwei Ziffern des aktuellen Jahres steht. Es wird der Datensatz
     * mit der höchsten Zahl nach dem Prefix herangezogen. Diese wird um 1 erhöht und als String zurückgeliefert.
     *
     * @param lastDigitsYear
     * @return
     */
    private String getNextASPemNumber(String lastDigitsYear) {
        String highestASPem = iPartsNumberHelper.getHighestOrderValueFromDBField(getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, new String[]{ FIELD_DPM_PEM }, new String[]{ PEMDataHelper.AS_PEM_PREFIX + lastDigitsYear + "*" });
        if (highestASPem == null) {
            return StrUtils.leftFill("1", 4, '0');
        } else if (StrUtils.isValid(highestASPem)) {
            // Prefix IZVJJ entfernen
            String highestNumber = StrUtils.replaceFirstSubstring(highestASPem, PEMDataHelper.AS_PEM_PREFIX + lastDigitsYear, "");
            // Nummer um 1 erhöhen
            int numberValue = StrUtils.strToIntDef(highestNumber, -1);
            if (numberValue != -1) {
                numberValue++;
                highestNumber = String.valueOf(numberValue);
                highestNumber = StrUtils.leftFill(highestNumber, 4, '0');
                return highestNumber;
            }
        }
        return null;
    }

    private void modifyEditFieldsByAttributes(DBDataObjectAttributes attributes, EtkEditFields editFields) {
        // Einsatzdatum ist nur Pflichtfeld falls es kein Produkt gibt
        EtkEditField dateField = editFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE);
        if (dateField != null) {
            if (attributes.fieldExists(FIELD_DPM_PRODUCT_NO) && !StrUtils.isEmpty(attributes.getFieldValue(FIELD_DPM_PRODUCT_NO))) {
                // Produkt -> ELDAS -> Datum kein Pflichtfeld
                dateField.setMussFeld(false);
            } else {
                // kein Produkt -> DIALOG -> Datum ist Pflichtfeld
                dateField.setMussFeld(true);
            }
        }
    }

    @Override
    protected EditUserControlForCreate createUserControlForCreate(AbstractJavaViewerFormIConnector connector,
                                                                  AbstractJavaViewerForm parentForm, String searchTable, IdWithType id,
                                                                  DBDataObjectAttributes initialAttributes, EtkEditFields editNewFields) {
        modifyEditFieldsByAttributes(initialAttributes, editNewFields);
        return new EditUserControlForCreate(connector, parentForm, searchTable, id, initialAttributes, editNewFields) {

            @Override
            protected void doEnableButtons(Event event) {
                if (!readOnly) {
                    if (pemDataOrigin != null) {
                        // bei ELDAS und freien SAs den OK Button direkt freischalten
                        enableOKButton(true);
                    } else {
                        super.doEnableButtons(event);
                    }
                }
            }

            @Override
            protected boolean isModified() {
                if (pemDataOrigin != null) {
                    // PEMs sollen bei ELDAS und freien SAs ohne Modifikation angelegt werden können, da hier alle Felder vorbefüllt sind
                    return true;
                } else {
                    return super.isModified();
                }
            }
        };
    }

    @Override
    protected EditUserControls createUserControlForEditOrView(AbstractJavaViewerFormIConnector connector,
                                                              AbstractJavaViewerForm parentForm, String searchTable, IdWithType id,
                                                              DBDataObjectAttributes attributes, EtkEditFields editFields) {
        modifyEditFieldsByAttributes(attributes, editFields);
        return new EditUserControls(connector, parentForm, searchTable, id, attributes, editFields);
    }

    @Override
    protected Map<EtkDisplayField, String> getSearchFieldsAndValuesForQuery(boolean filterEmptyValues, boolean applyWildcardSettings) {
        Map<EtkDisplayField, String> searchFieldsAndValuesForQuery = super.getSearchFieldsAndValuesForQuery(filterEmptyValues, applyWildcardSettings);
        // Bei den PEM-Stammdaten ganz normal ohne Produkt suchen
        if (isForEdit) {
            // Bei der PEM-Recherche innerhalb der Werksdaten mit Produkt suchen
            // Bei ELDAS und freie SAs sind diese schon in den searchFieldsAndValues vorhanden, deswegen Feld bloß für DIALOG setzen
            if (pemDataOrigin == null) {
                EtkDisplayField productField = createSearchField(getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, false, false);
                productField.setSearchExact(true);
                searchFieldsAndValuesForQuery.put(productField, ""); // bei DIALOG muss das Feld leer sein
            }
        }
        return searchFieldsAndValuesForQuery;
    }

    @Override
    protected boolean doValidateAttributes(DBDataObjectAttributes attributes) {
        // Hat der Benutzer beide Eigenschaften, wird die PEM immer angezeigt
        if (!hasBothVehicleRights) {
            if (!isVisibleForUserProperties(attributes)) {
                return false;
            }
        }

        if (pemDataOrigin != null) {
            String value = attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
            // leere Werte dürfen bei ELDAS und freien SAs nicht angezeigt werden
            if (StrUtils.isEmpty(value)) {
                return false;
            }
            if (pemDataOrigin.isSA()) {
                // Bei freien SAs alles ignorieren, was nicht zur eigentlichen SA gehört
                return pemDataOrigin.getPemOriginValue().equalsIgnoreCase(value);
            } else {
                // Bei ELDAS Stücklisten nur Einträge zulassen, die ein Produkt haben (wird oben schon geprüft) und zu keiner SA gehören
                return !iPartsNumberHelper.isValidSa(value, true);
            }
        }

        return true;
    }

    /**
     * Liefert zurück, ob die übergebenen Attribute zu den Eigenschaften des Bernutzers passen:
     * <p>
     * - Anwendern mit Eigenschaft "PKW/VAN" werden nur PEMs mit Quelle "IPARTS-MB" und "IPARTS" angezeigt
     * - Anwendern mit Eigenschaft "Truck" werden nur PEMs mit Quelle "IPARTS-Truck" und "IPARTS" angezeigt
     *
     * @param attributes
     * @return
     */
    private boolean isVisibleForUserProperties(DBDataObjectAttributes attributes) {
        iPartsImportDataOrigin origin = iPartsImportDataOrigin.getTypeFromCode(attributes.getFieldValue(FIELD_DPM_SOURCE));
        // Flag, ob der PEM Datensatz einen Ursprung hat. Bei PKW gibt es keinen Ursprung. Bei ELDAS und freien SAs schon
        boolean hasPEMSource = attributes.fieldExists(FIELD_DPM_PRODUCT_NO) && StrUtils.isValid(attributes.getFieldValue(FIELD_DPM_PRODUCT_NO));
        if ((((origin == iPartsImportDataOrigin.IPARTS) && hasPEMSource) || (origin == iPartsImportDataOrigin.IPARTS_TRUCK))
            && iPartsRight.checkTruckAndBusInSession()) {
            return true;
        }
        if ((((origin == iPartsImportDataOrigin.IPARTS) && !hasPEMSource) || (origin == iPartsImportDataOrigin.IPARTS_MB))
            && iPartsRight.checkCarAndVanInSession()) {
            return true;
        }
        return false;
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        boolean enabled = !isForEdit;

        if (isEditAllowed() && isModifyAllowed()) {
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu(),
                                                            "!!Bearbeiten");

            DBDataObjectAttributesList multiSelection = getMultiSelection();
            // Es dürfen nur Datensätze editiert bzw. gelöscht werden, die in iParts angelegt wurden
            // -> Verhalten für die Buttons dementsprechend anpassen
            if (multiSelection != null) {
                if (multiSelection.size() == 1) {
                    if (!PEMDataHelper.isIZVPem(multiSelection.get(0))) {
                        toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu(),
                                                                        "!!Anzeigen");
                        setToolbarButtonAndMenuEnabled(EditToolbarButtonAlias.EDIT_DELETE, false);
                    } else {
                        setToolbarButtonAndMenuEnabled(EditToolbarButtonAlias.EDIT_DELETE, true);
                    }
                    enabled = true;
                } else {
                    for (DBDataObjectAttributes attributes : multiSelection) {
                        if (PEMDataHelper.isIZVPem(attributes)) {
                            return;
                        }
                    }
                    setToolbarButtonAndMenuEnabled(EditToolbarButtonAlias.EDIT_DELETE, false);
                }
            }
        } else {
            toolbarHelper.changeToolbarButtonAndMenuTooltip(EditToolbarButtonAlias.EDIT_WORK, getTable().getContextMenu(),
                                                            "!!Anzeigen");
            setToolbarButtonAndMenuEnabled(EditToolbarButtonAlias.EDIT_DELETE, false);
            DBDataObjectAttributesList multiSelection = getMultiSelection();
            if ((multiSelection != null) && (multiSelection.size() == 1)) {
                enabled = true;
            }

        }
        getButtonPanel().setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    /**
     * Befüllt die Such-, Anzeige- und Editier-Felder
     *
     * @param connector
     * @param searchFields  werden in dieser Funktion befüllt wenn nicht <code>null</code>
     * @param displayFields werden in dieser Funktion befüllt wenn nicht <code>null</code>
     * @param editFields    werden in dieser Funktion befüllt wenn nicht <code>null</code>
     */
    private void fillFormFields(AbstractJavaViewerFormIConnector connector, EtkDisplayFields searchFields, EtkDisplayFields displayFields,
                                EtkEditFields editFields) {
        EtkProject project = connector.getProject();
        // Suchfelder definieren
        if (searchFields != null) {
            searchFields.load(connector.getConfig(), CONFIG_KEY_SEARCH_FIELDS,
                              connector.getConfig().getCurrentDatabaseLanguage());
            if (searchFields.size() == 0) {
                searchFields.addFeld(createSearchField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, false, false));
                searchFields.addFeld(createSearchField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, false, false));
            }
        }

        if (displayFields != null) {
            // Anzeigefelder definieren
            displayFields.load(connector.getConfig(), CONFIG_KEY_DISPLAY_FIELDS);
            if (displayFields.size() == 0) {
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_STC, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_DESC, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_ADAT, displayFields, true);
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_SOURCE, displayFields, true);
            }
            if ((pemDataOrigin != null) || !isForEdit) {
                // bei ELDAS oder innerhalb der PEM Stammdaten die Produktnummer mit anzeigen
                addNonMultiLangDisplayField(project, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, displayFields, true);
            }
        }

        if (editFields != null) {
            // Editfelder fürs Editieren festlegen
            editFields.load(connector.getConfig(), CONFIG_KEY_EDIT_FIELDS);
            if (editFields.size() == 0) {
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM, false));
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_FACTORY_NO, false));
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO, false));
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PEM_DATE, false));
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_STC, false));
                editFields.addFeld(createEditField(connector.getProject(), TABLE_DA_PEM_MASTERDATA, FIELD_DPM_DESC, false));
            }
            // Sollten PK Felder konfiguriert worden sein, dann dürfen diese nicht editierbar sein
            disablePKFieldsForEdit(connector, editFields, TABLE_DA_PEM_MASTERDATA);
            disableAndHideEditField(editFields, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_SOURCE);
            disableAndHideEditField(editFields, TABLE_DA_PEM_MASTERDATA, FIELD_DPM_ADAT);

            // die Produktnummer darf nicht editiert werden.
            // Bei ELDAS wird sie automatisch gesetzt, bei DIALOG bleibt sie leer
            EtkEditField productField = editFields.getFeldByName(TABLE_DA_PEM_MASTERDATA, FIELD_DPM_PRODUCT_NO);
            if (productField != null) {
                productField.setEditierbar(false);
            }
        }
    }

    /**
     * Blendet das übergebene Feld in Edit-Dialog aus. Zur Sicherheit wird das Felda auf "nicht editierbar" gesetzt
     *
     * @param editFields
     * @param tableName
     * @param fieldName
     */
    private void disableAndHideEditField(EtkEditFields editFields, String tableName, String fieldName) {
        EtkEditField sourceField = editFields.getFeldByName(tableName, fieldName);
        if (sourceField != null) {
            sourceField.setMussFeld(false);
            sourceField.setEditierbar(false);
            sourceField.setVisible(false);
        }
    }

    private void addNonMultiLangDisplayField(EtkProject project, String table, String field, EtkDisplayFields displayFields, boolean columnFilterEnabled) {
        EtkDisplayField displayField = addDisplayField(table, field, false, false, null, project, displayFields);
        displayField.setColumnFilterEnabled(columnFilterEnabled);
    }


    /**
     * Liefert die PEM, die via Stammdatendialog ausgewählt wurde
     *
     * @return
     */
    private String getSelectedPEM() {
        DBDataObjectAttributes selection = getSelection();
        if (selection != null) {
            return selection.getFieldValue(FIELD_DPM_PEM);
        }
        return "";
    }

    @Override
    public boolean isModifyAllowed() {
        if (super.isModifyAllowed()) {
            DBDataObjectAttributesList multiSelection = getMultiSelection();
            // Es dürfen nur Datensätze editiert werden, die in iParts angelegt wurden
            // -> Verhalten für Bearbeiten/Anzeigen dementsprechend anpassen
            if ((multiSelection != null) && (multiSelection.size() == 1)) {
                return PEMDataHelper.isIZVPem(multiSelection.get(0));
            }
        }

        return false;
    }
}
