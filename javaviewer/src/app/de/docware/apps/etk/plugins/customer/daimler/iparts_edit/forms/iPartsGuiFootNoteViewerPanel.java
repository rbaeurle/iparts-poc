/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiScrollPane;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.color.FrameworkConstantColor;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * Panel für Fußnotenanzeige - ersetzt GuiTextArea
 */
public class iPartsGuiFootNoteViewerPanel extends GuiPanel {

    public static final String TYPE = "footnoteviewerpanel";

    private static final String DEFAULT_FONTNAME = "Monospaced";
    private static final int DEFAULT_FONTSIZE = 13;
    private static final String PREFIX_FOR_PART_FOOTNOTE = "T";
    private static final String PREFIX_FOR_PARTLISTENTRY_FOOTNOTE = "PV";
    private static final String PREFIX_FOR_CONSTRUCTION_FOOTNOTE = PREFIX_FOR_PARTLISTENTRY_FOOTNOTE + " (D)";
    private static final int PREFIX_LENGTH = 7;

    private GuiPanel masterPanel;
    private GuiScrollPane scrollpane;
    private String fontName;
    private int fontSize;
    private Color markColor;
    private Color markBackColor;
    private Color selectedBackColor;

    public iPartsGuiFootNoteViewerPanel() {
        super();
        setType(TYPE);
        fontName = DEFAULT_FONTNAME;
        fontSize = DEFAULT_FONTSIZE;
        markColor = iPartsEditPlugin.clPlugin_iPartsEdit_ColorFootnoteMarkedLine.getColor();
        markBackColor = iPartsEditPlugin.clPlugin_iPartsEdit_ColorFootnoteBackgroundMarkedLine.getColor();
        selectedBackColor = iPartsEditPlugin.clPlugin_iPartsEdit_ColorFootnoteBackgroundSelectedLine.getColor();
        initGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void initGui() {
        setMinimumWidth(10);
        setMinimumHeight(10);
        LayoutBorder myLayout = new LayoutBorder();
        setLayout(myLayout);

        scrollpane = new GuiScrollPane();
        scrollpane.setName(TYPE + "scrollpane");
        scrollpane.__internal_setGenerationDpi(96);
        scrollpane.registerTranslationHandler(translationHandler);
        scrollpane.setScaleForResolution(true);
        scrollpane.setMinimumWidth(10);
        scrollpane.setMinimumHeight(10);
        masterPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
        masterPanel.setName(TYPE + "masterPanel");
        masterPanel.__internal_setGenerationDpi(96);
        masterPanel.registerTranslationHandler(translationHandler);
        masterPanel.setScaleForResolution(true);
        masterPanel.setMinimumWidth(10);
        masterPanel.setMinimumHeight(10);
        masterPanel.setBackgroundColor(new FrameworkConstantColor("clTransparent"));
        LayoutGridBag masterPanelLayout = new LayoutGridBag();
        masterPanel.setLayout(masterPanelLayout);
        scrollpane.addChild(masterPanel);
        ConstraintsBorder scrollpaneConstraints = new ConstraintsBorder();
        scrollpane.setConstraints(scrollpaneConstraints);
        addChild(scrollpane);
    }

    public int getPreferredHeightForMainPanel() {
        return masterPanel.getPreferredHeight();
    }

    public int getMainPanelWidth() {
        return masterPanel.getPreferredWidth();
    }

    @Override
    public String getFontName() {
        return fontName;
    }

    @Override
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Color getMarkColor() {
        return markColor;
    }

    public Color getMarkBackColor() {
        return markBackColor;
    }

    public void setMarkBackColor(Color markBackColor) {
        this.markBackColor = markBackColor;
    }

    public void setMarkColor(Color markColor) {
        this.markColor = markColor;
    }

    public void clear() {
        masterPanel.removeAllChildren();
    }

    public void setFootNotes(EtkProject project, Collection<iPartsFootNote> footNotes) {
        clear();
        if (footNotes != null) {
            int y = 0;
            boolean setBackColor = !markBackColor.toString().equals(Colors.clWhite.getColor().toString());
            for (iPartsFootNote footNote : footNotes) {
                String footNoteName = StrUtils.leftFill(footNote.getFootNoteName(project), 3, ' ');
                for (String footNoteText : footNote.getFootNoteTexts(project)) {
                    List<String> lines = StrUtils.toStringList(footNoteText, "\n", true);
                    for (String line : lines) {
                        GuiLabel textField = createLabelField(y);
                        String prefix = PREFIX_FOR_PARTLISTENTRY_FOOTNOTE;
                        switch (footNote.getFootnoteType()) {
                            case CONSTRUCTION_FOOTNOTE:
                                prefix = PREFIX_FOR_CONSTRUCTION_FOOTNOTE;
                                break;
                            case PART:
                                prefix = PREFIX_FOR_PART_FOOTNOTE;
                                break;
                        }
                        prefix = StrUtils.padStringWithCharsUpToLength(prefix, ' ', PREFIX_LENGTH);
                        String s = prefix + footNoteName + " " + line;
                        textField.setText(s);
                        textField.setUserObject(footNote);
                        setColorFootNoteMarked(footNote, textField, setBackColor);
                        masterPanel.addChild(textField);
                        y++;
                    }
                }
            }
            masterPanel.addChild(createEndLabel(y));
        }
    }

    private boolean setColorFootNoteMarked(iPartsFootNote footNote, AbstractGuiControl control, boolean setBackColor) {
        if ((footNote.getFootnoteType() == iPartsFootnoteType.COLOR_TABLEFOOTNOTE) && footNote.isMarked()) {
            control.setForegroundColor(markColor);
            if (setBackColor) {
                control.setBackgroundColor(markBackColor);
            }
            return true;
        }
        return false;
    }

    public void setSelection(Collection<iPartsFootNote> selectedFootNotes) {
        if (selectedFootNotes != null) {
            AbstractGuiControl selectedControl = null;
            boolean setBackColor = !markBackColor.toString().equals(Colors.clWhite.getColor().toString());
            for (AbstractGuiControl control : masterPanel.getChildren()) {
                Object obj = control.getUserObject();
                if (obj instanceof iPartsFootNote) {
                    iPartsFootNote currentFootNote = (iPartsFootNote)obj;
                    boolean setSelectedColor = false;
                    if (!setColorFootNoteMarked(currentFootNote, control, setBackColor)) {
                        setSelectedColor = true;
                    } else if (!setBackColor) {
                        setSelectedColor = true;
                    }
                    if (setSelectedColor) {
                        if (contains(selectedFootNotes, currentFootNote)) {
                            control.setBackgroundColor(selectedBackColor);
                            selectedControl = control;
                        } else {
                            control.setBackgroundColor(Colors.clWhite);
                        }
                    }
                }
            }
            if (selectedControl != null) {
                scrollpane.scrollToControl(selectedControl);
            }
        }
    }

    private boolean contains(Collection<iPartsFootNote> selectedFootNotes, iPartsFootNote currentFootNote) {
        for (iPartsFootNote footNote : selectedFootNotes) {
            if (footNote.getFootNoteId().equals(currentFootNote.getFootNoteId())) {
                return true;
            }
        }
        return false;
    }

    private GuiLabel createLabelField(int y) {
        GuiLabel label = new GuiLabel();
        label.setName(TYPE + "label" + y);
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(translationHandler);
        label.setScaleForResolution(true);
        label.setMinimumWidth(200);
        label.setMinimumHeight(10);
        label.setFontName(fontName);
        label.setFontSize(fontSize);
        ConstraintsGridBag labelConstraints = new ConstraintsGridBag(0, y, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
        label.setConstraints(labelConstraints);
//        label.setBorderWidth(0);
        return label;
    }

    private GuiLabel createEndLabel(int y) {
        GuiLabel label = new GuiLabel();
        label.setName(TYPE + "label");
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(translationHandler);
        label.setScaleForResolution(true);
        label.setMinimumWidth(10);
        label.setMinimumHeight(10);
        ConstraintsGridBag labelConstraints = new ConstraintsGridBag(4, y, 1, 1, 100.0, 100.0, "c", "n", 0, 0, 0, 0);
        label.setConstraints(labelConstraints);
        return label;
    }
}
