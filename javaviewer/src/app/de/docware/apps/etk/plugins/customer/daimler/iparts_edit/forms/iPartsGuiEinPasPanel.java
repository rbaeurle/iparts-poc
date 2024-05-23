/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.Collection;
import java.util.Iterator;

/**
 * Repräsentiert ein GuiEqualDimensionPanel mit der Möglichkeit einen EinPas-Knoten zu bestimmern
 */
public class iPartsGuiEinPasPanel extends GuiEqualDimensionPanel {

    public static final String TYPE = "einpasedit";

    // Defaultwerte
    private String startLanguageDefaultValue = Language.DE.getCode();

    // Spezifische Eigenschaften der Komponente
    private String startLanguage = startLanguageDefaultValue;

    // Weitere benötigte Variablen
    private GuiComboBox<Object> combobox_HG;
    private GuiComboBox<Object> combobox_G;
    private GuiComboBox<Object> combobox_TU;

    private EinPas einPas = null;
    private EinPasId startEinPasId = null;

    public iPartsGuiEinPasPanel() {
        super();
        setType(TYPE);
        initGui();
    }

    public iPartsGuiEinPasPanel(String startLanguage) {
        this();
        this.startLanguage = startLanguage;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void initGui() {
        setMinimumWidth(10);
        setMinimumHeight(20);
        setHorizontal(true);
        combobox_HG = createComboEinPas(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_HG(event);
            }
        }, "hg");
        this.addChild(combobox_HG);
        clearComboBox(combobox_HG);
        combobox_G = createComboEinPas(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_G(event);
            }
        }, "g");
        this.addChild(combobox_G);
        clearComboBox(combobox_G);
        combobox_TU = createComboEinPas(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onChange_CB_TU(event);
            }
        }, "tu");
        this.addChild(combobox_TU);
        clearComboBox(combobox_TU);
    }

    /**
     * Notwendiger Aufruf, bevor das EinPasPanel aktiv wird
     * Wegen EtkProject als eigene Routine
     *
     * @param project
     */
    public void init(EtkProject project) {
        einPas = EinPas.getInstance(project);
        Collection<EinPasNode> hgNodes = einPas.getHGNodeList();

        // besetzen der HG-Combobox + Vorbereitung G-/TU-Combobox
        fillComboBoxItems(combobox_HG, hgNodes);
        clearComboBox(combobox_G);
        clearComboBox(combobox_TU);
        setStartEinPasId(startEinPasId);
    }

    /**
     * Überprüfungsfunktion, ob EinPasPanel initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return einPas != null;
    }

    /**
     * Test, ob alle EinPas Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            return (getSelectedHGNumber() != null) && (getSelectedGNumber() != null) &&
                   (getSelectedTUNumber() != null);
        }
        return false;
    }

    /**
     * Selektierte HG-Nummer holen
     *
     * @return
     */
    public String getHG() {
        if (isValid()) {
            return getSelectedHGNumber();
        }
        return null;
    }

    /**
     * Selektierte G-Nummer holen
     *
     * @return
     */
    public String getG() {
        if (isValid()) {
            return getSelectedGNumber();
        }
        return null;
    }

    /**
     * Selektierte TU-Nummer holen
     *
     * @return
     */
    public String getTU() {
        if (isValid()) {
            return getSelectedTUNumber();
        }
        return null;
    }

    /**
     * Liefert Ergebnis als EinPasId
     *
     * @return
     */
    public EinPasId getEinPasId() {
        if (isValid()) {
            return new EinPasId(getSelectedHGNumber(), getSelectedGNumber(), getSelectedTUNumber());
        }
        return null;
    }

    public void setStartEinPasId(EinPasId startEinPasId) {
        if (startEinPasId != null) {
            if (isInit()) {
                EinPasNode node = einPas.getHGNode(startEinPasId.getHg());
                setSelectedIndexByNode(combobox_HG, node);
                node = einPas.getGNode(startEinPasId.getHg(), startEinPasId.getG());
                setSelectedIndexByNode(combobox_G, node);
                node = einPas.getTuNode(startEinPasId.getHg(), startEinPasId.getG(), startEinPasId.getTu());
                setSelectedIndexByNode(combobox_TU, node);
                this.startEinPasId = null;
            } else {
                this.startEinPasId = startEinPasId;
            }
        }
    }

    private void setSelectedIndexByNode(GuiComboBox combobox, EinPasNode node) {
        if (node != null) {
            int index = -1;
            for (int lfdNr = 0; lfdNr < combobox.getItemCount(); lfdNr++) {
                EinPasNode actNode = (EinPasNode)combobox.getUserObject(lfdNr);
                if (actNode.getNumber().equals(node.getNumber())) {
                    index = lfdNr;
                    break;
                }
            }
            if (index != -1) {
                combobox.setSelectedIndex(index);
            }
        }
    }

    /**
     * Callback für HG-Combobox
     * (=> besetzen der G-Combobox)
     *
     * @param event
     */
    private void onChange_CB_HG(Event event) {
        String numberHGNode = getSelectedHGNumber();
        if (numberHGNode != null) {
            Collection<EinPasNode> gNodes = einPas.getGNodeList(numberHGNode);
            fillComboBoxItems(combobox_G, gNodes);
            clearComboBox(combobox_TU);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für G-Combobox
     * (=> besetzen der TU-Combobox)
     *
     * @param event
     */
    private void onChange_CB_G(Event event) {
        String numberHGNode = getSelectedHGNumber();
        String numberGNode = getSelectedGNumber();
        if ((numberHGNode != null) && (numberGNode != null)) {
            Collection<EinPasNode> tuNodes = einPas.getTUNodeList(numberHGNode, numberGNode);
            fillComboBoxItems(combobox_TU, tuNodes);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für TU-Combobox
     * (i.A. not used)
     *
     * @param event
     */
    private void onChange_CB_TU(Event event) {
        fireOnChangeEvent(event);
    }

    private void fireOnChangeEvent(Event event) {
        for (EventListener listener : eventListeners.getListeners(Event.ON_CHANGE_EVENT)) {
            listener.fire(event);
        }
    }


    /* Hilfsroutinen */

    private EinPasNode getSelectedUserObject(GuiComboBox combobox) {
        int index = combobox.getSelectedIndex();
        if (index != -1) {
            return (EinPasNode)combobox.getUserObject(index);
        }
        return null;
    }

    private String getSelectedNumber(GuiComboBox combobox) {
        EinPasNode selectedNode = getSelectedUserObject(combobox);
        if (selectedNode != null) {
            return selectedNode.getNumber();
        }
        return null;
    }

    private String getSelectedHGNumber() {
        return getSelectedNumber(combobox_HG);
    }

    private String getSelectedGNumber() {
        return getSelectedNumber(combobox_G);
    }

    private String getSelectedTUNumber() {
        return getSelectedNumber(combobox_TU);
    }

    private void clearComboBox(GuiComboBox combobox) {
        combobox.removeAllItems();
        combobox.setEnabled(false);
    }

    private void fillComboBoxItems(GuiComboBox combobox, Collection<EinPasNode> nodes) {
        combobox.switchOffEventListeners();
        combobox.removeAllItems();
        if (nodes != null) {
            Iterator<EinPasNode> iter = nodes.iterator();
            while (iter.hasNext()) {
                EinPasNode node = iter.next();
                combobox.addItem(node, buildEinPasComboText(node, startLanguage));
            }
            combobox.setEnabled(true);
        } else {
            combobox.setEnabled(false);
        }
        combobox.setSelectedIndex(-1);
        combobox.switchOnEventListeners();
    }

    /**
     * hier wird der Text für die Comboboxen erzeugt
     * (kann überschrieben werden)
     *
     * @param node
     * @param language
     * @return
     */
    static public String buildEinPasComboText(EinPasNode node, String language) {
        if (node != null) {
            return node.getNumber() + " " + node.getTitle().getText(language);
        }
        return "";
    }

    /**
     * Erzeugung einer Combobox
     *
     * @param eventListener
     * @param nameSuffix
     * @return
     */
    private GuiComboBox<Object> createComboEinPas(EventListener eventListener, String nameSuffix) {
        GuiComboBox<Object> combobox = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
        combobox.setName("combobox" + nameSuffix);
        combobox.__internal_setGenerationDpi(96);
        combobox.registerTranslationHandler(translationHandler);
        combobox.setScaleForResolution(true);
        combobox.setMinimumWidth(0);
        combobox.setMinimumHeight(0);
        combobox.setMaximumWidth(2147483647);
        combobox.setMaximumHeight(2147483647);
        combobox.addEventListener(eventListener);
        de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute combobox_Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
        combobox.setConstraints(combobox_Constraints);
        return combobox;
    }
}
