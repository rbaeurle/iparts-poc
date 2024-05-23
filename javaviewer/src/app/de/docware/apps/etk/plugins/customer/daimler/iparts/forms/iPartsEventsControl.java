/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;

/**
 * Control zum Auswählen von Ereignissen ab und bis
 */
public class iPartsEventsControl extends GuiEqualDimensionPanel {

    public static final String TYPE = "iPartsEventsControl";

    private iPartsGuiEventSelectComboBox eventFromComboBox;
    private iPartsGuiEventSelectComboBox eventToComboBox;

    public iPartsEventsControl() {
        super(true);
        type = TYPE;
        addControls();
    }

    private void addControls() {
        setMinimumHeight(20);
        setScaleForResolution(true);
        GuiPanel eventPanel = new GuiPanel();
        eventPanel.setLayout(new LayoutGridBag());

        GuiLabel label = new GuiLabel(TranslationHandler.translate("!!Ereignis ab"));
        eventPanel.addChildGridBag(label, 0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE, 0, 0, 0, 8);
        eventFromComboBox = new iPartsGuiEventSelectComboBox();
        eventPanel.addChildGridBag(eventFromComboBox, 1, 0, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 8);
        addChild(eventPanel);

        eventPanel = new GuiPanel();
        eventPanel.setLayout(new LayoutGridBag());
        label = new GuiLabel(TranslationHandler.translate("!!Ereignis bis"));
        eventPanel.addChildGridBag(label, 0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE, 0, 8, 0, 8);
        eventToComboBox = new iPartsGuiEventSelectComboBox();
        eventPanel.addChildGridBag(eventToComboBox, 1, 0, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 0);
        addChild(eventPanel);
        ThemeManager.get().render(this);
        setMinimumHeight(Math.max(eventFromComboBox.getPreferredHeight(), eventToComboBox.getPreferredHeight()));
    }

    public void init(EtkProject project, iPartsSeriesId seriesId) {
        init(project, seriesId, null, null);
    }

    public void init(EtkProject project, iPartsSeriesId seriesId, String selectedEventFromId, String selectedEventToId) {
        eventFromComboBox.init(project, seriesId, selectedEventFromId);
        eventToComboBox.init(project, seriesId, selectedEventToId);
    }

    public void initForColorEvents(EtkProject project, iPartsSeriesId seriesId, String eventFromId, String eventToId) {
        eventFromComboBox.initForColorEvents(project, seriesId, eventFromId);
        eventToComboBox.initForColorEvents(project, seriesId, eventToId);
    }

    public void setSelectedEventFrom(iPartsEvent event) {
        eventFromComboBox.setSelectedEvent(event);
    }

    public void setSelectedEventTo(iPartsEvent event) {
        eventToComboBox.setSelectedEvent(event);
    }

    public void setSelectedEventFrom(String eventId) {
        eventFromComboBox.setSelectedEventByEventId(eventId);
    }

    public void setSelectedEventTo(String eventId) {
        eventToComboBox.setSelectedEventByEventId(eventId);
    }

    public iPartsEvent getSelectedEventFrom() {
        return eventFromComboBox.getSelectedEvent();
    }

    public iPartsEvent getSelectedEventTo() {
        return eventToComboBox.getSelectedEvent();
    }

    @Override
    public void setEnabled(boolean enabled) {
        eventFromComboBox.setEnabled(enabled);
        eventToComboBox.setEnabled(enabled);
    }

    public boolean isEventFromSelected() {
        return getSelectedEventFrom() != null;
    }

    public boolean isEventToSelected() {
        return getSelectedEventTo() != null;
    }
}
