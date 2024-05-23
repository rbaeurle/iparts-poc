/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlDateTimeEditPanel;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.misc.observer.CallbackBinder;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstrakte Klasse für Werkseinsatzdaten-Editoren, die den PEM Stamm für das Anlegen, Recherchieren und Löschen von PEMs
 * nutzen.
 */
public abstract class AbstractEditUserControlsForFactoryData extends EditUserControlForCreate implements iPartsConst {

    protected boolean isNewForm;
    private Map<String, Object> specialFields;
    private Map<String, Map<String, iPartsDataPem>> factoryToPEMMap;
    private iPartsGuiPEMSelectionButtonTextField pemFromSelectionButtonTextField; // Control für PEMA
    private iPartsGuiPEMSelectionButtonTextField pemToSelectionButtonTextField; // Control für PEMB
    private GuiComboBox factoryControl; // Control für Werke
    private EditControlDateTimeEditPanel pemFromDatePanel; // Control für PEMTA
    private EditControlDateTimeEditPanel pemToDatePanel; // Control für PEMTB
    private GuiTextField codeFromTextField; // Control für STCA
    private GuiTextField codeToTextField; // Control für STCB
    private CallbackBinder callbackBinder = new CallbackBinder();
    private ObserverCallback pemChangeEventListener; // Listener für Änderungen an PEMs bzw Neuanlage einer PEM
    private PEMDataHelper.PEMDataOrigin pemDataOrigin;

    public AbstractEditUserControlsForFactoryData(AbstractJavaViewerFormIConnector dataConnector,
                                                  AbstractJavaViewerForm parentForm, String tableName, IdWithType id,
                                                  DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                                  boolean isNewForm, String windowName, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        this.isNewForm = isNewForm;
        this.pemDataOrigin = pemDataOrigin; // Nur bei ELDAS und freien SAs befüllt
        // Verknüpfe die von einander abhängigen Controls (PEMA, PEMB, PEMTA, PEMTB, SCTA, SCTB und Werk)
        linkFactoryAndPemControls();
        setWindowName(windowName);
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        callbackBinder = new CallbackBinder();
        pemChangeEventListener = new ObserverCallback(callbackBinder, iPartsDataChangedEventByEdit.class) {
            @Override
            public void callback(ObserverCall call) {
                // Callback um die PEMs für ein Werk zu aktualisieren, wenn PEMs angelegt oder verändert wurden
                if (call instanceof iPartsDataChangedEventByEdit) {
                    iPartsDataChangedEventByEdit editEvent = (iPartsDataChangedEventByEdit)call;
                    if ((editEvent.getDataType() == iPartsDataChangedEventByEdit.DataType.PEM) && (editEvent.getElementIds() != null)) {
                        // Ist die Session noch vorhanden und aktiv?
                        Session session = Session.get();
                        if ((session == null) || !session.isActive()) {
                            // Session ist tot -> EventListener entfernen (z.B. wenn das modale Fenster geöffnet war und
                            // eine neue Session gestartet wurde)
                            ApplicationEvents.clearEventListeners(callbackBinder);
                            return;
                        }

                        for (Object elementId : editEvent.getElementIds()) {
                            if (elementId instanceof iPartsPemId) {
                                String factory = ((iPartsPemId)elementId).getFactoryNo();
                                if (factoryToPEMMap.remove(factory) != null) {
                                    // Die PEMs zum Werk neu laden
                                    checkIfPEMsForFactoryLoaded(factory);
                                    // Falls die aktuellen Textfields schon PEMs zum Werk hatten, müssen diese neu
                                    // initialisiert werden
                                    initPemTextFields(factory, true);
                                    pemFromSelectionButtonTextField.doValidation();
                                    pemToSelectionButtonTextField.doValidation();
                                }
                            }
                        }
                    }
                }
            }
        };
        ApplicationEvents.addEventListener(pemChangeEventListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (callbackBinder != null) {
            // Der Listener muss wieder deregistriert werden
            ApplicationEvents.clearEventListeners(callbackBinder);
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
        if (specialFields == null) {
            specialFields = new LinkedHashMap<>();
            specialFields.put(getFactoryFieldName(), null);
        }

        String fieldName = field.getKey().getFieldName();
        if (specialFields.containsKey(fieldName)) {
            if (fieldName.equals(getFactoryFieldName())) {
                iPartsEditUserControlsHelper.modifyFactoryEditControl(getConnectorForSpecialFields(), ctrl, specialFields, initialValue);
            }
        }

        String tableName = field.getKey().getTableName();
        // Die Controls bestimmen und setzen
        if (tableName.equals(getFactoryTableName())) {
            if (fieldName.equals(getPEMFromFieldName())) {
                // Daimler-7784 Muss Feld, wenn Dokutyp DIALOG/DIALOG IParts
                field.setMussFeld(partListEntryIsDialogDocuType());
                pemFromSelectionButtonTextField = (iPartsGuiPEMSelectionButtonTextField)getControlIfInstanceOf(ctrl, iPartsGuiPEMSelectionButtonTextField.class);
                pemFromSelectionButtonTextField.setUsePEMMasterData(true);
            } else if (fieldName.equals(getPEMToFieldName())) {
                pemToSelectionButtonTextField = (iPartsGuiPEMSelectionButtonTextField)getControlIfInstanceOf(ctrl, iPartsGuiPEMSelectionButtonTextField.class);
                pemToSelectionButtonTextField.setUsePEMMasterData(true);
            } else if (fieldName.equals(getPEMFromDateFieldName())) {
                // Daimler-7784 Muss Feld, wenn Dokutyp DIALOG/DIALOG IParts
                field.setMussFeld(partListEntryIsDialogDocuType());
                pemFromDatePanel = (EditControlDateTimeEditPanel)getControlIfInstanceOf(ctrl, EditControlDateTimeEditPanel.class);
            } else if (fieldName.equals(getPEMToDateFieldName())) {
                pemToDatePanel = (EditControlDateTimeEditPanel)getControlIfInstanceOf(ctrl, EditControlDateTimeEditPanel.class);
            } else if (fieldName.equals(getCodeFromFieldName())) {
                codeFromTextField = (GuiTextField)getControlIfInstanceOf(ctrl, GuiTextField.class);
            } else if (fieldName.equals(getCodeToFieldName())) {
                codeToTextField = (GuiTextField)getControlIfInstanceOf(ctrl, GuiTextField.class);
            } else if (fieldName.equals(getFactoryFieldName())) {
                factoryControl = (GuiComboBox)getControlIfInstanceOf(ctrl, GuiComboBox.class);
            }
        }
    }

    protected abstract AbstractJavaViewerFormIConnector getConnectorForSpecialFields();

    /**
     * Check, für Werkseinsatzdaten-spetifische Daten (Werke, PEMs und PEM-Termine)
     *
     * @return
     */
    public boolean checkValidFactoryDataEntries() {
        String pemFrom = "";
        String pemTo = "";
        long pemDateFrom = -1;
        long pemDateTo = -1;

        if (pemToSelectionButtonTextField != null) {
            pemTo = pemToSelectionButtonTextField.getText();
        }
        if (pemFromSelectionButtonTextField != null) {
            pemFrom = pemFromSelectionButtonTextField.getText();
        }

        if (pemToDatePanel != null) {
            pemDateTo = iPartsFactoryData.getFactoryDateFromDateString(pemToDatePanel.getDateTimeAsRawString(), "AbstractEditUserControlsForFactoryData.pemDateTo");
        }

        if (pemFromDatePanel != null) {
            pemDateFrom = iPartsFactoryData.getFactoryDateFromDateString(pemFromDatePanel.getDateTimeAsRawString(), "AbstractEditUserControlsForFactoryData.pemDateFrom");
        }


        // Werksnummer muss aus Werke zu Produkt sein -> durch Combobox abgedeckt
        // Keine ungültigen Werke oder Werke die nicht am Produkt hinterlegt sind zulassen
        if (factoryControl != null) {
            Object selectedUserObject = factoryControl.getSelectedUserObject();
            String selectedItem = factoryControl.getSelectedItem();
            if (!selectedUserObject.equals(selectedItem)) {
                return false;
            }
        }

        // PEM ab und PEM bis dürfen nicht gleichzeitig leer sein
        if (!isPEMValid(pemFrom) && !isPEMValid(pemTo)) {
            setOKButtonTooltip("!!PEMs dürfen nicht leer sein.");
            return false;
        }

        // wenn es ein Datum gibt, dann muss es auch die zugehörige PEM geben
        if ((pemDateFrom > 0) && !isPEMValid(pemFrom)) {
            setOKButtonTooltip(TranslationHandler.translate("!!\"%1\" ist nicht besetzt.",
                                                            getLabelText(getPEMFromFieldName(), "PEM-Ab")));
            return false;
        }
        if ((pemDateTo > 0) && !isPEMValid(pemTo)) {
            setOKButtonTooltip(TranslationHandler.translate("!!\"%1\" ist nicht besetzt.",
                                                            getLabelText(getPEMToFieldName(), "PEM-Bis")));
            return false;
        }

        // PEM Datum ab < bis
        if ((pemDateFrom > pemDateTo) && (pemDateTo != 0)) {
            setOKButtonTooltip(TranslationHandler.translate("!!\"%1\" ist größer als \"%2\".",
                                                            getLabelText(getPEMFromDateFieldName(), "PEM Termin ab"),
                                                            getLabelText(getPEMToDateFieldName(), "PEM Termin bis")));
            return false;
        }
        setOKButtonTooltip("");
        return true;
    }

    private String getLabelText(String fieldName, String defaultText) {
        EditControl control = getEditControlByTableAndFieldName(tableName, fieldName);
        if (control != null) {
            return control.getLabel().getText();
        }
        return defaultText;
    }

    // Prüft nur ob die PEM nicht leer ist (Leerzeichen). PEM Stamm wird in doEnableButtons gecheckt
    private static boolean isPEMValid(String pem) {
        return ((pem != null) && (!pem.trim().isEmpty()));
    }


    @Override
    protected void doEnableButtons(Event event) {
        boolean enabled = !checkMustFieldsHaveValues() && checkValidFactoryDataEntries();
        if (!isNewForm && enabled) {
            enabled = checkForModified();
        }
        // Check, ob die PEMs auch in der DB existieren (PEMA und PEMB)
        if (enabled) {
            String factory = getRealFactoryFromControl();
            enabled = pemFromSelectionButtonTextField.isPemInputValidForFactory(factory) && pemToSelectionButtonTextField.isPemInputValidForFactory(factory);
        }
        enableOKButton(readOnly || enabled);
    }


    private void initPemTextFields(String factory, boolean initOnlyIfCurrentFactory) {
        if (!initOnlyIfCurrentFactory || pemFromSelectionButtonTextField.getCurrentFactory().equals(factory)) {
            pemFromSelectionButtonTextField.init(getParentForm(), factory, factoryToPEMMap.get(factory), pemDataOrigin);
        }
        if (!initOnlyIfCurrentFactory || pemToSelectionButtonTextField.getCurrentFactory().equals(factory)) {
            pemToSelectionButtonTextField.init(getParentForm(), factory, factoryToPEMMap.get(factory), pemDataOrigin);
        }
    }

    /**
     * Verknüpft alle Controls, die bei einer Werksänderungen angepasst werden müssen
     */
    private void linkFactoryAndPemControls() {
        // Listener zum Verteilen von PEMTA und SCTA
        pemFromSelectionButtonTextField.addEventListener(getListenerForPemButtonTextField(pemFromSelectionButtonTextField, pemFromDatePanel, codeFromTextField));
        // Listener zum Verteilen von PEMTB und SCTB
        pemToSelectionButtonTextField.addEventListener(getListenerForPemButtonTextField(pemToSelectionButtonTextField, pemToDatePanel, codeToTextField));
        // Listener um die PEM Controls zu initialisieren
        factoryControl.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                // Bei jeder Werksänderung müssen die PEM Controls mit den neuen Werken initialisiert werden. Die anderen
                // Controls müssen geleert werden (PEMTX und SCTX)
                setCurrentFactoryForPEMControls(false);
            }
        });
        // Den initialen Wert setzen
        setCurrentFactoryForPEMControls(true);
    }

    /**
     * Check, ob die PEMs zum übergebenen Werk schon vorhanden sind. Falls nicht, werden alle PEMs zum Werk aus der DB
     * geladen.
     *
     * @param factory
     */
    private void checkIfPEMsForFactoryLoaded(String factory) {
        if (factoryToPEMMap == null) {
            factoryToPEMMap = new HashMap<>();
        }
        if (!factoryToPEMMap.containsKey(factory)) {
            Map<String, iPartsDataPem> pemMap = PEMDataHelper.getPEMsForPEMOrigin(getProject(), factory, pemDataOrigin);
            factoryToPEMMap.put(factory, pemMap);
        }
    }

    /**
     * Erzeugt einen Listener, der das Datum und den Steuercode setzt, abhängig von der ausgewählten PEM
     *
     * @param pemSelectionButtonTextField
     * @param pemDatePanel
     * @param codeTextField
     * @return
     */
    private EventListener getListenerForPemButtonTextField(final iPartsGuiPEMSelectionButtonTextField pemSelectionButtonTextField,
                                                           final EditControlDateTimeEditPanel pemDatePanel,
                                                           final GuiTextField codeTextField) {
        return new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                // Hole das aktuelle PEM DataObject und prüfe, ob Daten vorhanden sind. Falls ja, werden die Daten gesetzt
                iPartsDataPem dataPem = getDataPemForCurrentInput(getRealFactoryFromControl(), pemSelectionButtonTextField.getText());
                if (dataPem != null) {
                    codeTextField.setText(dataPem.getFieldValue(FIELD_DPM_STC));
                    Calendar dateTime = dataPem.getFieldValueAsDateTime(FIELD_DPM_PEM_DATE);
                    if (dateTime == null) {
                        pemDatePanel.clearDateTime();
                    } else {
                        pemDatePanel.setDateTime(dateTime);
                    }
                }
            }

            /**
             * Liefert das {@link iPartsDataPem} Objekt zum übergebenen Werk und dem aktuellen Text <code>currentInput</code>
             *
             * @param factory
             * @param currentInput
             * @return
             */
            public iPartsDataPem getDataPemForCurrentInput(String factory, String currentInput) {
                if (StrUtils.isValid(factory, currentInput)) {
                    // Check, ob die PEMs für das Werk schon geladen wurden
                    checkIfPEMsForFactoryLoaded(factory);
                    // Alle PEMs für das übergebene Werk
                    if (factoryToPEMMap != null) {
                        Map<String, iPartsDataPem> pemsForFactory = factoryToPEMMap.get(factory);
                        if (pemsForFactory != null) {
                            return pemsForFactory.get(currentInput);
                        }
                    }
                }
                return null;
            }
        };
    }

    private String getRealFactoryFromControl() {
        return (String)factoryControl.getSelectedUserObject();
    }

    /**
     * Initialisiert die Controls für beide PEMs und leert bei Bedarf die abhängigen Controls
     *
     * @param isInit
     */
    private void setCurrentFactoryForPEMControls(boolean isInit) {
        String factory = getRealFactoryFromControl();
        if (StrUtils.isValid(factory)) {
            checkIfPEMsForFactoryLoaded(factory);
            initPemTextFields(factory, false);
            if (!isInit) {
                // Wenn es keine Initialisierung ist, dann müssen die Werte bei einer Werksänderung geleert werden
                pemFromSelectionButtonTextField.setText("");
                pemToSelectionButtonTextField.setText("");
            } else {
                // Beim initialisieren einmal die Validierung erzwingen nachdem das Werk gesetzt wurde
                pemFromSelectionButtonTextField.doValidation();
                pemToSelectionButtonTextField.doValidation();
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            // Im ReadOnly die PEM Controls explizit deaktivieren
            pemFromSelectionButtonTextField.setEnabled(false);
            pemToSelectionButtonTextField.setEnabled(false);
        }
    }

    /**
     * Liefert das Controls zurück, wenn es eine Instanz der übergebenen Klasse ist
     *
     * @param ctrl
     * @param classObject
     * @return
     */
    protected AbstractGuiControl getControlIfInstanceOf(EditControl ctrl, Class classObject) {
        AbstractGuiControl control = ctrl.getEditControl().getControl();
        if (classObject.equals(control.getClass())) {
            return control;
        }
        return null;
    }

    protected abstract boolean partListEntryIsDialogDocuType();

    protected abstract String getCodeToFieldName();

    protected abstract String getCodeFromFieldName();

    protected abstract String getPEMToDateFieldName();

    protected abstract String getPEMFromDateFieldName();

    protected abstract String getPEMToFieldName();

    protected abstract String getPEMFromFieldName();

    protected abstract String getFactoryTableName();

    protected abstract String getFactoryFieldName();
}
