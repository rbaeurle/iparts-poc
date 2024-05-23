/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SelectSearchGridMaterial;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchSpringMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsSpringMapping;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;


public class iPartsSpringProperties extends AbstractJavaViewerForm {

    // Enum und Schalter für die mehrfach verwendete Klasse zur Unterscheidung, welche Daten gerade angezeigt werden
    // und welche Aktionen beim onChangeEvent ausgelöst werden sollen.
    public enum TYPE {
        springLegFront, springLegRear, springShimRear
    }

    private TYPE propertyType;

    // Zur Erzeugung des eindeutigen Namens für QF-Test
    private static final String LOCATION_TOP = "Top";
    private static final String LOCATION_BOTTOM = "Bottom";

    // Schalter, der die Textfelder steuert. Editierbar oder nicht editierbar.
    private boolean isInViewOnlyMode;

    /**
     * Erzeugt eine Instanz von iPartsSpringProperties.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsSpringProperties(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                  boolean viewOnlyMode, TYPE springPropertyType) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        isInViewOnlyMode = viewOnlyMode;
        propertyType = springPropertyType;
        postCreateGui();
    }

    /**
     * Liefert den inneren Kontainer mit den Textfeldern zum Einschnappen auf einer anderen Form zurück.
     *
     * @return
     */
    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    private String getEditFieldName(String initVal) {
        return propertyType.toString() + "_" + initVal;
    }

    /**
     * Hier kann eigener Code stehen der ausgeführt wird wenn die Instanz erzeugt wurde.
     */
    private void postCreateGui() {
        // Fill me with init Code
        GuiControlValidator controlValidator = new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                boolean isValid = true;
                if (control instanceof GuiTextField) {
                    iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                    String text = ((GuiTextField)control).getTrimmedText();
                    isValid = text.isEmpty();
                    if (!isValid) {
                        isValid = numberHelper.isValidASachNo(getProject(), text);
                        if (isValid && ((propertyType == TYPE.springLegFront) || (propertyType == TYPE.springLegRear))) {
                            // Zugriff auf den Cache
                            iPartsSpringMapping springMappingCache = iPartsSpringMapping.getInstance(getProject());
                            // Liste aus dem Cache holen
                            isValid = springMappingCache.getSpringLegExists(numberHelper.unformatASachNoForDB(getProject(), text));
                        }
                    }
                }
                return new ValidationState(isValid);
            }
        };

        mainWindow.textfieldTop = exchangeTextField(mainWindow.textfieldTop, controlValidator);
        mainWindow.buttonTop.setVisible(!isInViewOnlyMode);
        mainWindow.labelTop.setText("");
        // Zur Erzeugung des eindeutigen Namens für QF-Test
        mainWindow.textfieldTop.setName(getEditFieldName(LOCATION_TOP));
        // Die Beschriftung neben dem oberen Eingabefeld aktualisieren
        mainWindow.textfieldTop.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                addLabelText(getTopTextFieldText(), true);
            }
        });

        //
        mainWindow.textfieldBottom = exchangeTextField(mainWindow.textfieldBottom, controlValidator);
        mainWindow.buttonBottom.setVisible(!isInViewOnlyMode);
        mainWindow.labelBottom.setText("");
        // Zur Erzeugung des eindeutigen Namens für QF-Test
        mainWindow.textfieldBottom.setName(getEditFieldName(LOCATION_BOTTOM));
        // Die Beschriftung neben dem unteren Eingabefeld aktualisieren
        mainWindow.textfieldBottom.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                addLabelText(getBottomTextFieldText(), false);
            }
        });

        // Der Dummy ist nur für die Anordnung der visuellen Komponenten vorhanden.
        mainWindow.labelAbstandshalter.setText("");
    }

    /**
     * Ersetzt zur Laufzeit das visuelle Element [GuiTextField] aus dem GUI-Designer
     * durch das Plugin-spezifische [iPartsGuiTextFieldBackgroundToggle]
     *
     * @param currentTextfield
     * @param controlValidator
     * @return
     */
    private GuiTextField exchangeTextField(GuiTextField currentTextfield, GuiControlValidator controlValidator) {
        iPartsGuiTextFieldBackgroundToggle textfield = new iPartsGuiTextFieldBackgroundToggle();
        AbstractConstraints constraints = currentTextfield.getConstraints();
        currentTextfield.removeFromParent();
        ((ConstraintsGridBag)constraints).setInsetsRight(isInViewOnlyMode ? 4 : 0);
        textfield.setConstraints(constraints);
        textfield.setMinimumWidth(currentTextfield.getMinimumWidth());
        mainWindow.panelElements.addChild(textfield);
        textfield.setText("");
        textfield.setName(currentTextfield.getName());
        textfield.setEditable(!isInViewOnlyMode);
        textfield.setValidator(controlValidator);
        textfield.setCaseMode(GuiTextField.CaseMode.UPPERCASE);
        return textfield;
    }

    /**
     * Die übergebene Liste kann per Definition maximal ZWEI Elemente enthalten.
     * Diese Werte werden in die dafür vorgesehenen Textfelder geschrieben.
     *
     * @param list
     */
    public void setValues(List<String> list) {
        if (list != null) {
            String springNo = "";
            if (list.size() > 0) {
                springNo = list.get(0);
                mainWindow.textfieldTop.setText(formatSpringNo(springNo));
                if (isInViewOnlyMode) {
                    addLabelText(getTopTextFieldText(), true);
                }
            }
            if (list.size() > 1) {
                springNo = list.get(1);
                mainWindow.textfieldBottom.setText(formatSpringNo(springNo));
                if (isInViewOnlyMode) {
                    addLabelText(getBottomTextFieldText(), false);
                }
            }
        } else {
            clearLabelText();
        }
    }

    /**
     * Formatierung der Feder entsprechend den Anzeigeeinstellungen.
     *
     * @param springNo
     * @return
     */
    private String formatSpringNo(String springNo) {
        // Als Materialnummer formatieren
        return iPartsNumberHelper.formatPartNo(getProject(), springNo);
    }

    /**
     * - ohne Worte -
     */
    private void clearLabelText() {
        addLabelText("", true);
        addLabelText("", false);
    }

    /**
     * Setzt die Beschreibung zu den eingegebenen Werten auf dem zum Eingabefeld gehörenden Label.
     *
     * @param springNo
     * @param top
     */
    private void addLabelText(String springNo, boolean top) {
        String labelText = "";
        if (!springNo.isEmpty()) {
            PartId partId = new PartId(springNo, "");
            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partId);
            if (dataPart.existsInDB()) {
                labelText = dataPart.getUsageDisplayText();
            }
        }
        if (top) {
            mainWindow.labelTop.setText(labelText);
        } else {
            mainWindow.labelBottom.setText(labelText);
        }
    }

    /**
     * Getter für den Inhalt des OBEREN Textfeldes
     *
     * @return
     */
    public String getTopTextFieldText() {
        return getFieldText(mainWindow.textfieldTop);
    }

    /**
     * Getter für den Inhalt des UNTEREN Textfeldes
     *
     * @return
     */
    public String getBottomTextFieldText() {
        return getFieldText(mainWindow.textfieldBottom);
    }

    private String getFieldText(GuiTextField textField) {
        // Zurückwandeln in eine unformatierte Sachnummer
        iPartsNumberHelper helper = new iPartsNumberHelper();
        return helper.unformatASachNoForDB(getProject(), textField.getTrimmedText());
    }

    public boolean isTopTextFieldValid() {
        return ((iPartsGuiTextFieldBackgroundToggle)mainWindow.textfieldTop).isValueValid();
    }

    public boolean isBottomTextFieldValid() {
        return ((iPartsGuiTextFieldBackgroundToggle)mainWindow.textfieldBottom).isValueValid();
    }

    /**
     * Routine, die die veränderten Daten wieder als Stringliste zurückgibt.
     *
     * @return
     */
    public List<String> getValues() {
        List<String> result = new DwList<String>();
        String springNo = getTopTextFieldText();
        if (!springNo.isEmpty()) {
            result.add(springNo);
        }
        springNo = getBottomTextFieldText();
        if (!springNo.isEmpty()) {
            result.add(springNo);
        }
        return result;
    }

    /**
     * Für das Mapping einen Change-Listener einhängen, der aufgerufen wird, wenn sich am OBEREN Textfeld was tut.
     *
     * @param eventListener
     */
    public void addOnChangeListenerTopTextField(de.docware.framework.modules.gui.event.EventListener eventListener) {

        mainWindow.textfieldTop.addEventListener(eventListener);
    }

    /**
     * Für das Mapping einen Change-Listener einhängen, der aufgerufen wird, wenn sich am UNTEREN Textfeld was tut.
     *
     * @param eventListener
     */
    public void addChangeListenerBottomTextField(de.docware.framework.modules.gui.event.EventListener eventListener) {

        mainWindow.textfieldBottom.addEventListener(eventListener);
    }

    private void onActionPerformedEvent(Event event, boolean top) {
        String matNo = "";
        if (propertyType == TYPE.springLegFront) {
            String searchValue = top ? getTopTextFieldText() : getBottomTextFieldText();
            matNo = SelectSearchSpringMapping.doSelectSpringMappingDialog(this, searchValue, "!!Federbeine...");
        } else {
            String title = (propertyType == TYPE.springLegRear) ? "!!Federn hinten..." : "!!Federbeilagen hinten...";
            String searchValue = top ? getTopTextFieldText() : getBottomTextFieldText();
            SelectSearchGridMaterial selectSearchGridMaterial = new iPartsSelectSearchGridMaterial(this, false, false);
            selectSearchGridMaterial.setTitle(title);
            matNo = selectSearchGridMaterial.showGridSelectionDialog(searchValue);
        }
        if (!matNo.isEmpty()) {
            List<String> list = new DwList<String>();
            if (top) {
                list.add(matNo);
                list.add(getBottomTextFieldText());
            } else {
                list.add(getTopTextFieldText());
                list.add(matNo);
            }
            setValues(list);
        }
    }

    private void onActionPerformedTopEvent(Event event) {
        onActionPerformedEvent(event, true);
    }

    private void onActionPerformedBottomEvent(Event event) {
        onActionPerformedEvent(event, false);
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
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelElements;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelAbstandshalter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

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
            scrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane.setName("scrollpane");
            scrollpane.__internal_setGenerationDpi(96);
            scrollpane.registerTranslationHandler(translationHandler);
            scrollpane.setScaleForResolution(true);
            scrollpane.setMinimumWidth(10);
            scrollpane.setMinimumHeight(10);
            panelElements = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelElements.setName("panelElements");
            panelElements.__internal_setGenerationDpi(96);
            panelElements.registerTranslationHandler(translationHandler);
            panelElements.setScaleForResolution(true);
            panelElements.setMinimumWidth(10);
            panelElements.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelElementsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelElements.setLayout(panelElementsLayout);
            textfieldTop = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldTop.setName("textfieldTop");
            textfieldTop.__internal_setGenerationDpi(96);
            textfieldTop.registerTranslationHandler(translationHandler);
            textfieldTop.setScaleForResolution(true);
            textfieldTop.setMinimumWidth(200);
            textfieldTop.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 4, 4);
            textfieldTop.setConstraints(textfieldTopConstraints);
            panelElements.addChild(textfieldTop);
            buttonTop = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonTop.setName("buttonTop");
            buttonTop.__internal_setGenerationDpi(96);
            buttonTop.registerTranslationHandler(translationHandler);
            buttonTop.setScaleForResolution(true);
            buttonTop.setMinimumWidth(10);
            buttonTop.setMinimumHeight(10);
            buttonTop.setMaximumWidth(20);
            buttonTop.setMaximumHeight(22);
            buttonTop.setMnemonicEnabled(true);
            buttonTop.setText("...");
            buttonTop.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onActionPerformedTopEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 0, 4, 4);
            buttonTop.setConstraints(buttonTopConstraints);
            panelElements.addChild(buttonTop);
            labelTop = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTop.setName("labelTop");
            labelTop.__internal_setGenerationDpi(96);
            labelTop.registerTranslationHandler(translationHandler);
            labelTop.setScaleForResolution(true);
            labelTop.setMinimumWidth(10);
            labelTop.setMinimumHeight(10);
            labelTop.setText("labelTop");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 4, 8);
            labelTop.setConstraints(labelTopConstraints);
            panelElements.addChild(labelTop);
            textfieldBottom = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldBottom.setName("textfieldBottom");
            textfieldBottom.__internal_setGenerationDpi(96);
            textfieldBottom.registerTranslationHandler(translationHandler);
            textfieldBottom.setScaleForResolution(true);
            textfieldBottom.setMinimumWidth(200);
            textfieldBottom.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldBottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 8, 8, 4);
            textfieldBottom.setConstraints(textfieldBottomConstraints);
            panelElements.addChild(textfieldBottom);
            buttonBottom = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonBottom.setName("buttonBottom");
            buttonBottom.__internal_setGenerationDpi(96);
            buttonBottom.registerTranslationHandler(translationHandler);
            buttonBottom.setScaleForResolution(true);
            buttonBottom.setMinimumWidth(10);
            buttonBottom.setMinimumHeight(10);
            buttonBottom.setMaximumWidth(20);
            buttonBottom.setMaximumHeight(22);
            buttonBottom.setMnemonicEnabled(true);
            buttonBottom.setText("...");
            buttonBottom.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onActionPerformedBottomEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonBottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "n", 4, 0, 8, 4);
            buttonBottom.setConstraints(buttonBottomConstraints);
            panelElements.addChild(buttonBottom);
            labelBottom = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelBottom.setName("labelBottom");
            labelBottom.__internal_setGenerationDpi(96);
            labelBottom.registerTranslationHandler(translationHandler);
            labelBottom.setScaleForResolution(true);
            labelBottom.setMinimumWidth(10);
            labelBottom.setMinimumHeight(10);
            labelBottom.setText("labelBottom");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelBottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 8, 8);
            labelBottom.setConstraints(labelBottomConstraints);
            panelElements.addChild(labelBottom);
            labelAbstandshalter = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelAbstandshalter.setName("labelAbstandshalter");
            labelAbstandshalter.__internal_setGenerationDpi(96);
            labelAbstandshalter.registerTranslationHandler(translationHandler);
            labelAbstandshalter.setScaleForResolution(true);
            labelAbstandshalter.setMinimumWidth(10);
            labelAbstandshalter.setMinimumHeight(10);
            labelAbstandshalter.setText("dummy");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelAbstandshalterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 2, 1, 1, 100.0, 100.0, "c", "n", 0, 0, 0, 0);
            labelAbstandshalter.setConstraints(labelAbstandshalterConstraints);
            panelElements.addChild(labelAbstandshalter);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelElementsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelElements.setConstraints(panelElementsConstraints);
            scrollpane.addChild(panelElements);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane.setConstraints(scrollpaneConstraints);
            panelMain.addChild(scrollpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}