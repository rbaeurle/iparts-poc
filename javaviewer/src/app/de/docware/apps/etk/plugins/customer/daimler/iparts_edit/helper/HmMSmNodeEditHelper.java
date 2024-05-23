/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.iPartsDataHmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;

import java.util.EnumSet;

/**
 * Hilfsklasse für Edit-Funktionen von HM/M/SM-Knoten
 */
public class HmMSmNodeEditHelper {

    public static final String CHILDREN_NAME_HMMSM_NODES = "HmMSmNodes";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_HM_Construction,
                                                                                   iPartsModuleTypes.Dialog_M_Construction,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);

    // Ab hier Methoden für die Menüpunkte in der Stückliste
    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        addPartListMenuItem(popupMenu, connector, HmMSmMenuItemType.HIDDEN);
        addPartListMenuItem(popupMenu, connector, HmMSmMenuItemType.NO_CALC);
        addPartListMenuItem(popupMenu, connector, HmMSmMenuItemType.CHANGE_DOCU_REL_OMITTED_PART);
    }

    private static void addPartListMenuItem(GuiContextMenu popupMenu, AssemblyListFormIConnector connector, HmMSmMenuItemType menuItemType) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, menuItemType.getMenuItemName(),
                                                                                           menuItemType.getMenuItemTextActive(), DefaultImages.module.getImage(),
                                                                                           null);
        addHmMSmNodeEventListener(menuItem, connector, true, menuItemType);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
        HmMSmNode hmMSmNode = null;
        if (destAssembly != null) {
            hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(destAssembly.getAsId(), connector.getProject());
        }
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der konstruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        boolean isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && (hmMSmNode != null) && iPartsRight.EDIT_MASTER_DATA.checkRightInSession()
                            && AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, VALID_MODULE_TYPES);

        updatePartListMenuItem(popupMenu, hmMSmNode, HmMSmMenuItemType.HIDDEN, isVisible);
        updatePartListMenuItem(popupMenu, hmMSmNode, HmMSmMenuItemType.NO_CALC, isVisible);
        updatePartListMenuItem(popupMenu, hmMSmNode, HmMSmMenuItemType.CHANGE_DOCU_REL_OMITTED_PART, isVisible);
    }

    private static void updatePartListMenuItem(GuiContextMenu popupMenu, HmMSmNode hmMSmNode,
                                               HmMSmMenuItemType menuItemType, boolean isVisible) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu,
                                                                                           menuItemType.getMenuItemName(),
                                                                                           isVisible);
        // hmMSmNode kann null sein, wenn man in einem gemappten EinPAS-Knoten für eine HM/M/SM-Stückliste landet.
        if (hmMSmNode != null) {
            updateHmMSmMenuItemText(menuItem, getValueForMenuItemType(hmMSmNode, menuItemType), menuItemType);
        }
    }

    // Ab hier Methoden für die Menüpunkte im Baum
    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        addTreeMenuItem(menu, formWithTree, HmMSmMenuItemType.HIDDEN);
        addTreeMenuItem(menu, formWithTree, HmMSmMenuItemType.NO_CALC);
        addTreeMenuItem(menu, formWithTree, HmMSmMenuItemType.CHANGE_DOCU_REL_OMITTED_PART);
    }

    private static void addTreeMenuItem(GuiContextMenu menu, AbstractAssemblyTreeForm formWithTree,
                                        HmMSmMenuItemType menuItemType) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree,
                                                                                       menuItemType.getMenuItemName(),
                                                                                       menuItemType.getMenuItemTextActive(),
                                                                                       null);
        addHmMSmNodeEventListener(menuItem, formWithTree.getConnector(), false, menuItemType);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        HmMSmNode hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(connector.getCurrentAssembly().getAsId(),
                                                                          connector.getProject());
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der knostruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        boolean isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && (hmMSmNode != null)
                            && iPartsRight.EDIT_MASTER_DATA.checkRightInSession();

        updateTreeMenuItem(popupMenu, connector, hmMSmNode, HmMSmMenuItemType.HIDDEN, isVisible);
        updateTreeMenuItem(popupMenu, connector, hmMSmNode, HmMSmMenuItemType.NO_CALC, isVisible);
        updateTreeMenuItem(popupMenu, connector, hmMSmNode, HmMSmMenuItemType.CHANGE_DOCU_REL_OMITTED_PART, isVisible);
    }

    private static void updateTreeMenuItem(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector,
                                           HmMSmNode hmMSmNode, HmMSmMenuItemType menuItemType, boolean isVisible) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector,
                                                                                       menuItemType.getMenuItemName(),
                                                                                       VALID_MODULE_TYPES);
        if (menuItem != null) {
            menuItem.setVisible(isVisible && menuItem.isVisible());
            if (hmMSmNode != null) {
                updateHmMSmMenuItemText(menuItem, getValueForMenuItemType(hmMSmNode, menuItemType), menuItemType);
            }
        }
    }

    // Ab hier Hilfsmethoden

    /**
     * Setzt den Text des übergebenen Menüpunkts auf Basis des übergebenen {@link HmMSmMenuItemType}
     *
     * @param menuItem
     * @param currentValue
     * @param menuItemType
     */
    private static void updateHmMSmMenuItemText(GuiMenuItem menuItem, boolean currentValue, HmMSmMenuItemType menuItemType) {
        // Diese Sichtbarkeit kann nicht so gut über die Standard-Funktion (AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible)
        // abgebildet werden, deshalb wird hier weiter eingeschränkt.
        if ((menuItem != null) && menuItem.isVisible()) {
            if (currentValue) {
                menuItem.setText(menuItemType.getMenuItemTextInactive());
            } else {
                menuItem.setText(menuItemType.getMenuItemTextActive());
            }
        }
    }

    /**
     * Liefert den Wert des Attributs zum übergebenen {@link HmMSmMenuItemType}
     *
     * @param hmMSmNode
     * @param menuItemType
     * @return
     */
    private static boolean getValueForMenuItemType(HmMSmNode hmMSmNode, HmMSmMenuItemType menuItemType) {
        if (hmMSmNode != null) {
            switch (menuItemType) {
                case HIDDEN:
                    return hmMSmNode.isHidden();
                case NO_CALC:
                    return hmMSmNode.isNoCalc();
                case CHANGE_DOCU_REL_OMITTED_PART:
                    return hmMSmNode.isChangeDocuRelOmittedPart();
            }
        }
        return false;
    }

    private static void addHmMSmNodeEventListener(GuiMenuItem menuItem, final AssemblyFormIConnector connector,
                                                  final boolean isPartListPopupMenu, HmMSmMenuItemType menuItemType) {
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                EtkDataAssembly assembly = connector.getCurrentAssembly();
                AssemblyId parentAssemblyId = null;

                // DestinationAssembly in der Stückliste
                if (isPartListPopupMenu) {
                    if (connector instanceof AssemblyListFormIConnector) {
                        parentAssemblyId = assembly.getAsId();
                        assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector((AssemblyListFormIConnector)connector);
                        if (assembly == null) {
                            return;
                        }
                    }
                } else if (assembly instanceof iPartsDataAssembly) {
                    parentAssemblyId = ((iPartsDataAssembly)assembly).getFirstParentAssemblyIdFromParentEntries();
                }

                EtkProject project = connector.getProject();
                HmMSmNode hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(assembly.getAsId(), project);
                if (hmMSmNode == null) { // Ohne HM/M/SM-Knoten können wir nichts machen
                    return;
                }

                iPartsDataHmMSm dataHmMSm = new iPartsDataHmMSm(project, hmMSmNode.getId());
                if (!dataHmMSm.existsInDB()) {
                    dataHmMSm.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }

                boolean valueForMenuItem = getValueForMenuItemType(hmMSmNode, menuItemType);
                valueForMenuItem = !valueForMenuItem;
                dataHmMSm.setFieldValueAsBoolean(menuItemType.getHmMSmDBFieldname(), valueForMenuItem, DBActionOrigin.FROM_EDIT);

                // Änderung im einem ChangeSet zur Baureihe speichern
                iPartsDataSeries dataSeries = new iPartsDataSeries(project, hmMSmNode.getSeriesId());
                if (!dataSeries.existsInDB()) {
                    // FROM_DB, weil die Baureihe selbst nicht angelegt werden soll
                    dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                }

                // HM/M/SM-Knoten als Pseudo-Kind der Baureihe hinzufügen
                GenericEtkDataObjectList<iPartsDataHmMSm> dataHmMSmList = new GenericEtkDataObjectList<>();
                dataHmMSmList.add(dataHmMSm, DBActionOrigin.FROM_EDIT);
                dataSeries.setChildren(CHILDREN_NAME_HMMSM_NODES, dataHmMSmList);

                // saveDataObjectWithChangeSet() speichert den HM/M/SM-Knoten nicht direkt in der DB ab bei Source SERIES
                // -> separat saveToDB() aufrufen
                project.getDbLayer().startTransaction();
                try {
                    if (iPartsRevisionChangeSet.saveDataObjectWithChangeSet(project, dataSeries, iPartsChangeSetSource.SERIES)
                        && dataHmMSm.saveToDB()) {
                        project.getDbLayer().commit();

                        // Cache aktualisieren
                        setValueForMenuItem(hmMSmNode, valueForMenuItem, menuItemType);

                        // Event für veränderte Stückliste in allen Cluster-Knoten feuern (es muss die Vater-Baugruppe vom
                        // veränderten HM/M/SM-Knoten im Event als verändert markiert werden, weil ein aus-/eingeblendeter
                        // HM/M/SM-Knoten ja Auswirkungen auf die Vater-Baugruppe hat)
                        if (parentAssemblyId != null) {
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                      parentAssemblyId, false));
                        }

                        // Veränderter HM/M/SM-Knoten muss ebenfalls allen Cluster-Knoten bekannt gemacht werden (dort wird
                        // intern dann auch ein DataChangedEvent gefeuert)
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.HMMSM,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  hmMSmNode.getId(), false));
                    } else {
                        project.getDbLayer().rollback();
                    }
                } catch (Exception e) {
                    project.getDbLayer().rollback();
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    MessageDialog.showError("!!Fehler beim Speichern.");
                }
            }
        });
    }

    /**
     * Setzt den Wert des Attributs zum übergebenen {@link HmMSmMenuItemType}
     *
     * @param hmMSmNode
     * @param valueForMenuItem
     * @param menuItemType
     */
    private static void setValueForMenuItem(HmMSmNode hmMSmNode, boolean valueForMenuItem, HmMSmMenuItemType menuItemType) {
        switch (menuItemType) {
            case HIDDEN:
                hmMSmNode.setHidden(valueForMenuItem);
                break;
            case NO_CALC:
                hmMSmNode.setNoCalc(valueForMenuItem);
                break;
            case CHANGE_DOCU_REL_OMITTED_PART:
                hmMSmNode.setChangeDocuRelOmittedPart(valueForMenuItem);
                break;

        }
    }
}
