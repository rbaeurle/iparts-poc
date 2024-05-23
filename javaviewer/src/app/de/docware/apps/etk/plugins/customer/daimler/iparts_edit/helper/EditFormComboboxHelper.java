/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.common.EnumComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;

/**
 * Helper Klasse für das Handling/Austausch von ComboBoxen und TextFields
 */
public class EditFormComboboxHelper {

    /**
     * Den selektierten ComboBox-Text an das TextField übergeben
     *
     * @param textfield
     * @param combobox
     */
    public static void setTextFieldText(GuiTextField textfield, GuiComboBox combobox) {
        if (textfield != null) {
            int index = combobox.getSelectedIndex();
            if (index != -1) {
                textfield.setText(combobox.getItem(index));
            } else {
                textfield.setText("");
            }
        }
    }

    /**
     * TextField für den ReadOnly-Modus erzeugen und besetzen
     *
     * @param combobox
     * @return
     */
    public static GuiTextField createTextField(GuiComboBox combobox, GuiPanel parentPanel) {
        GuiTextField textfield = new GuiTextField();
        textfield.setBackgroundColor(Colors.clDesignBackground.getColor());
        textfield.setEditable(false);
        AbstractConstraints textfieldConstraints = combobox.getConstraints();
        textfield.setConstraints(textfieldConstraints);
        parentPanel.removeChild(combobox);
        parentPanel.addChild(textfield);
        setTextFieldText(textfield, combobox);

        return textfield;
    }

    /**
     * Ein ReadOnly TextField gegen ComboBox austauschen
     *
     * @param textfield
     * @param combobox
     * @return
     */
    public static void replaceTextFieldByComboBox(GuiTextField textfield, GuiComboBox combobox, GuiPanel parentPanel) {
        if (textfield != null) {
            AbstractConstraints textfieldConstraints = textfield.getConstraints();
            combobox.setConstraints(textfieldConstraints);
            parentPanel.removeChild(textfield);
            parentPanel.addChild(combobox);
        }
    }

    /**
     * Eine ComboBox gegen ReadOnly TextField austauschen
     *
     * @param combobox
     * @param textfield Kann auch {@code null} sein und wird dann neu erzeugt.
     * @return
     */
    public static GuiTextField replaceComboBoxByTextField(GuiComboBox combobox, GuiTextField textfield, GuiPanel parentPanel) {
        if (textfield == null) {
            textfield = createTextField(combobox, parentPanel);
        }
        AbstractConstraints comboboxConstraints = combobox.getConstraints();
        textfield.setConstraints(comboboxConstraints);
        parentPanel.removeChild(combobox);
        parentPanel.addChild(textfield);
        return textfield;
    }

    /**
     * Zum Clear gehört immer disablen
     *
     * @param combobox
     */
    public static void clearComboBox(GuiComboBox combobox) {
        combobox.removeAllItems();
        combobox.setEnabled(false);
    }

    /**
     * Eine ComboBox gegen eine EnumComboBox austauschen
     *
     * @param comboBoxToReplace
     * @param parentPanel
     * @param eventListener
     * @param project
     * @param tableName
     * @param fieldName
     * @param ignoreBlankTexts
     * @return
     */
    public static EnumComboBox replaceComboBoxByEnum(GuiComboBox comboBoxToReplace, GuiPanel parentPanel, EventListener eventListener,
                                                     EtkProject project, String tableName, String fieldName, boolean ignoreBlankTexts) {
        EnumComboBox enumCombo = new EnumComboBox();
        AbstractConstraints constraints = comboBoxToReplace.getConstraints();
        enumCombo.setConstraints(constraints);
        enumCombo.setName(comboBoxToReplace.getName());
        parentPanel.removeChild(comboBoxToReplace);
        if (eventListener != null) {
            enumCombo.addEventListener(eventListener);
        }

        parentPanel.addChild(enumCombo);
        enumCombo.setIgnoreBlankTexts(ignoreBlankTexts);
        enumCombo.switchOffEventListeners();
        enumCombo.setEnumTexte(project, tableName, fieldName, project.getDBLanguage(), false);
        enumCombo.switchOnEventListeners();
        return enumCombo;
    }


    // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden

    /**
     * Den selektierten ComboBox-Text an das TextField übergeben
     *
     * @param textfield
     * @param combobox
     */
    public static void setTextFieldText(GuiTextField textfield, RComboBox combobox) {
        if (textfield != null) {
            int index = combobox.getSelectedIndex();
            if (index != -1) {
                textfield.setText(combobox.getItem(index));
            } else {
                textfield.setText("");
            }
        }
    }

    /**
     * TextField für den ReadOnly-Modus erzeugen und besetzen
     *
     * @param combobox
     * @return
     */
    public static GuiTextField createTextField(RComboBox combobox, GuiPanel parentPanel) {
        GuiTextField textfield = new GuiTextField();
        textfield.setBackgroundColor(Colors.clDesignBackground.getColor());
        textfield.setEditable(false);
        AbstractConstraints textfieldConstraints = combobox.getConstraints();
        textfield.setConstraints(textfieldConstraints);
        parentPanel.removeChild(combobox);
        parentPanel.addChild(textfield);
        setTextFieldText(textfield, combobox);

        return textfield;
    }

    /**
     * Ein ReadOnly TextField gegen ComboBox austauschen
     *
     * @param textfield
     * @param combobox
     * @return
     */
    public static void replaceTextFieldByComboBox(GuiTextField textfield, RComboBox combobox, GuiPanel parentPanel) {
        if (textfield != null) {
            AbstractConstraints textfieldConstraints = textfield.getConstraints();
            combobox.setConstraints(textfieldConstraints);
            parentPanel.removeChild(textfield);
            parentPanel.addChild(combobox);
        }
    }

    /**
     * Eine ComboBox gegen ReadOnly TextField austauschen
     *
     * @param combobox
     * @param textfield Kann auch {@code null} sein und wird dann neu erzeugt.
     * @return
     */
    public static GuiTextField replaceComboBoxByTextField(RComboBox combobox, GuiTextField textfield, GuiPanel parentPanel) {
        if (textfield == null) {
            textfield = createTextField(combobox, parentPanel);
        }
        AbstractConstraints comboboxConstraints = combobox.getConstraints();
        textfield.setConstraints(comboboxConstraints);
        parentPanel.removeChild(combobox);
        parentPanel.addChild(textfield);
        ThemeManager.get().render(textfield);
        return textfield;
    }

    /**
     * Zum Clear gehört immer disablen
     *
     * @param combobox
     */
    public static void clearComboBox(RComboBox combobox) {
        combobox.removeAllItems();
        combobox.setEnabled(false);
    }
}
