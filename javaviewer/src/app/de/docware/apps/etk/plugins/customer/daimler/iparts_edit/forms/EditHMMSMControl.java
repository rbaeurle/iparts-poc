/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;

/**
 * GuiPanel zur gemeinsamen Darstellung von HM M SM Knoten inkl. Button zum Absprung in den entsprechenden Editor.
 * Dieses Panel ist zum Einbinden in EditUserControls gedacht
 */
public class EditHMMSMControl extends GuiPanel {

    private GuiLabel hmLabel;
    private GuiTextField hmTextfield;
    private GuiLabel mLabel;
    private GuiTextField mTextfield;
    private GuiLabel smLabel;
    private GuiTextField smTextfield;
    private GuiButton openEditorButton;
    private String series;
    private HmMSmId startHmMSmId;

    public EditHMMSMControl(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm) {
        super();
        createGui();
        openEditorButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                HmMSmId result = EditHmMSmVDialog.showHmMSmVDialog(connector, parentForm, getHMMSMId(), hmLabel.getText(),
                                                                   mLabel.getText(), smLabel.getText());
                if (result != null) {
                    setHmMSmTextfields(result);
                    fireEvent(EventCreator.createOnChangeEvent(EditHMMSMControl.this, parentForm.getGui().getUniqueId()));
                }
            }
        });
    }

    private void createGui() {
        setName("EditHMMSMControl");
        setLayout(new LayoutGridBag(false));
        setTitle("!!DIALOG-Konstruktions Verortung");

        int inset = 4;
        int gridX = 0;
        int gridY = 0;
        setPadding(inset);

        hmLabel = new GuiLabel("Hauptmodul");
        hmLabel.setFontStyle(DWFontStyle.BOLD);
        hmLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
        addChildGridBag(hmLabel, gridX, gridY, 1, 1, 0.0, 0.0,
                        ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, 0, inset, inset);
        gridY++;

        mLabel = new GuiLabel("Modul");
        mLabel.setFontStyle(DWFontStyle.BOLD);
        mLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
        addChildGridBag(mLabel, gridX, gridY, 1, 1, 0.0, 0.0,
                        ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, 0, inset, inset);
        gridY++;

        smLabel = new GuiLabel("Submodul");
        smLabel.setFontStyle(DWFontStyle.BOLD);
        smLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
        addChildGridBag(smLabel, gridX, gridY, 1, 1, 0.0, 0.0,
                        ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, 0, inset, inset);

        gridX++;
        gridY = 0;

        hmTextfield = new GuiTextField();
        addChildGridBag(hmTextfield, gridX, gridY, 1, 1, 1.0, 0.0,
                        ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, inset, inset, 0);
        gridY++;

        mTextfield = new GuiTextField();
        addChildGridBag(mTextfield, gridX, gridY, 1, 1, 1.0, 0.0,
                        ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, inset, inset, 0);
        gridY++;

        smTextfield = new GuiTextField();
        addChildGridBag(smTextfield, gridX, gridY, 1, 1, 1.0, 0.0,
                        ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                        0, inset, 0, 0);

        gridX++;
        gridY = 0;
        openEditorButton = new GuiButton("...");
        addChildGridBag(openEditorButton, gridX, gridY, 1, 1, 0.0, 0.0,
                        ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                        0, 4, inset, 0);
    }

    public HmMSmId getHMMSMId() {
        return new HmMSmId(series, hmTextfield.getText().trim(), mTextfield.getText().trim(), smTextfield.getText().trim());
    }

    public void setEnabled(boolean textfieldsEnabled, boolean buttonEnabled) {
        hmTextfield.setEnabled(textfieldsEnabled);
        mTextfield.setEnabled(textfieldsEnabled);
        smTextfield.setEnabled(textfieldsEnabled);
        this.openEditorButton.setEnabled(buttonEnabled);
    }

    public void initHmMSmId(HmMSmId hmMSmId) {
        series = hmMSmId.getSeries();
        setHmMSmTextfields(hmMSmId);
        startHmMSmId = hmMSmId;
    }

    public void initLabels(String hm, String m, String sm) {
        hmLabel.setText(hm);
        mLabel.setText(m);
        smLabel.setText(sm);
    }

    public String getSeries() {
        return series;
    }

    public void setHmMSmTextfields(HmMSmId hmMSmId) {
        hmTextfield.setText(hmMSmId.getHm());
        mTextfield.setText(hmMSmId.getM());
        smTextfield.setText(hmMSmId.getSm());
    }

    public boolean isModified() {
        return !((startHmMSmId != null) && getHMMSMId().equals(startHmMSmId));
    }
}
