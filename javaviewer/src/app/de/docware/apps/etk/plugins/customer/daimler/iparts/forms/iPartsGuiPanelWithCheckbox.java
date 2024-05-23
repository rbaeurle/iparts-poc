/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

/**
 * Klasse die eine Checkbox in einer Tabellen-Zelle darstellt. Die Checkbox muss zwecks zentraler Platzierung in einem Panel liegen
 * Bei Übergabe von tableAndFieldName und row {@link DataObjectGrid.GuiTableRowWithObjects} wird der Wert der CheckBox
 * direkt im {@link EtkDataObject} gespeichert
 * Via {@link OnChangeEvent} kann eine Benachrichtigung stattfinden.
 */
public class iPartsGuiPanelWithCheckbox extends GuiPanel {

    public static final String TYPE = "ipartspanelwithcheckbox";

    private GuiCheckbox checkbox;
    private DataObjectGrid.GuiTableRowWithObjects row;
    private String tableAndFieldName;
    private OnChangeEvent onChangeEvent;

    public iPartsGuiPanelWithCheckbox(boolean selected, OnChangeEvent onChangeEvent) {
        this(selected, "", null, onChangeEvent);
    }

    public iPartsGuiPanelWithCheckbox(boolean selected, String tableAndFieldName, DataObjectGrid.GuiTableRowWithObjects row, OnChangeEvent onChangeEvent) {
        super();
        setType(TYPE);
        checkbox = new GuiCheckbox("", selected);
        this.row = row;
        this.tableAndFieldName = tableAndFieldName;
        this.onChangeEvent = onChangeEvent;
        initPanel();
    }

    private void initPanel() {
        setBackgroundColor(Colors.clTransparent.getColor());
        setLayout(new LayoutGridBag());
        checkbox.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE, 0, 0, 0, 0));
        addChild(checkbox);

        checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                setValue();
                if (onChangeEvent != null) {
                    onChangeEvent.onChange();
                }
            }
        });
    }

    private void setValue() {
        if (StrUtils.isValid(tableAndFieldName) && (row != null)) {
            EtkDataObject dataObject = row.getObjectForTable(TableAndFieldName.getTableName(tableAndFieldName));
            dataObject.setFieldValueAsBoolean(TableAndFieldName.getFieldName(tableAndFieldName), isSelected(), DBActionOrigin.FROM_DB);
        }
    }

    public void setSelected(boolean selected) {
        checkbox.setSelected(selected);
        setValue();
    }

    public boolean isSelected() {
        return checkbox.isSelected();
    }

    @Override
    public void switchOffEventListeners() {
        super.switchOffEventListeners();
        checkbox.switchOffEventListeners();
    }

    @Override
    public void switchOnEventListeners() {
        super.switchOnEventListeners();
        checkbox.switchOnEventListeners();
    }

    @Override
    public String getTextRepresentation(boolean onlyLabelTexts) {
        return TranslationHandler.translate(checkbox.isSelected() ? "!!Ja" : "!!Nein");
    }
}
