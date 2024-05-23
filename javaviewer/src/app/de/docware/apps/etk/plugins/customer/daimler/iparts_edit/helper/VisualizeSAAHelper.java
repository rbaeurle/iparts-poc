/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt.iPartsCTTModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Fügt den Kontextmenüeintrag "SAA 3D Visualisierung" für Stücklisten und den Baugruppenbaum hinzu und bietet Hilfsmethoden
 * für die SAA Visualisierung an.
 */
public class VisualizeSAAHelper implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_VISUALIZE_SAA_3D = "iPartsMenuItemVisualizeSAA3D";
    public static final String IPARTS_MENU_ITEM_TEXT_VISUALIZE_SAA_3D = "!!SAA 3D Visualisierung";

    public static GuiMenuItem createVisualizeSAAPopupMenuItem(EditToolbarButtonMenuHelper toolbarHelper, TranslationHandler translationHandler,
                                                              EventListener eventListener) {
        return toolbarHelper.createMenuEntry(VisualizeSAAHelper.IPARTS_MENU_ITEM_VISUALIZE_SAA_3D, VisualizeSAAHelper.IPARTS_MENU_ITEM_TEXT_VISUALIZE_SAA_3D,
                                             DefaultImages.link.getImage(), eventListener, translationHandler);
    }

    /**
     * Liefert anhand des übergebenen Feldnamens für eine SAA alle SAAs der {@link DBDataObjectAttributesList} zurück.
     *
     * @param attributesList
     * @param fieldName
     * @return
     */
    public static Set<String> getSelectedSAAsFromAttributesList(DBDataObjectAttributesList attributesList, String fieldName) {
        Set<String> selectedSAAs = new LinkedHashSet<>();
        if (attributesList != null) {
            for (DBDataObjectAttributes attributes : attributesList) {
                String saaNo = attributes.getFieldValue(fieldName);
                if (saaNo.startsWith(SAA_NUMBER_PREFIX)) {
                    selectedSAAs.add(saaNo);
                }
            }
        }
        return selectedSAAs;
    }

    /**
     * Visualisiert die übergebenen SAAs in Smaragd.
     *
     * @param saaNumbers Die SAAs können formatiert oder unformatiert sein.
     */
    public static void visualizeSAAs(Set<String> saaNumbers) {
        SmaragdVisHelper.openSmaragdURLSilent(saaNumbers);
    }

    /**
     * Visualisiert die übergebenen SAA in Smaragd.
     *
     * @param saaNumber Die SAA kann formatiert oder unformatiert sein.
     */
    public static void visualizeSAA(String saaNumber) {
        if (saaNumber != null) {
            Set<String> saaNumbers = new HashSet<>();
            saaNumbers.add(saaNumber);
            visualizeSAAs(saaNumbers);
        }
    }

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // Menüeintrag hinzufügen
        GuiMenuItem menuItemVisualize3D = addVisualizeSAAPopupMenuItem(popupMenu);
        menuItemVisualize3D.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                SmaragdVisHelper.openSmaragdURLSilent(getSelectedSaasFromPartList(connector));
            }
        });
    }

    public static void modifyTreePopupMenu(GuiContextMenu popupmenu, AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItemVisualize3D = addVisualizeSAAPopupMenuItem(popupmenu);
        menuItemVisualize3D.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                visualizeSAA(getSelectedSaaFromTree(formWithTree.getConnector()));
            }
        });
    }

    private static GuiMenuItem addVisualizeSAAPopupMenuItem(GuiContextMenu popupMenu) {
        // Menüeintrag hinzufügen
        GuiMenuItem menuItemVisualize3D = new GuiMenuItem();
        menuItemVisualize3D.setUserObject(IPARTS_MENU_ITEM_VISUALIZE_SAA_3D);
        menuItemVisualize3D.setName(IPARTS_MENU_ITEM_VISUALIZE_SAA_3D);
        menuItemVisualize3D.setText("!!SAA 3D Visualisierung");
        menuItemVisualize3D.setIcon(DefaultImages.link.getImage());
        popupMenu.addChild(menuItemVisualize3D);
        return menuItemVisualize3D;
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_VISUALIZE_SAA_3D,
                                                                                        !getSelectedSaasFromPartList(connector).isEmpty());
        if (menuItem != null) {
            updateSelectSaaMenuEntriesForRetail(connector, menuItem);
        }
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_VISUALIZE_SAA_3D, getSelectedSaaFromTree(connector) != null);
    }

    private static Set<String> getSelectedSaasFromPartList(AssemblyListFormIConnector connector) {
        Set<String> selectedSAAs = new LinkedHashSet<>();
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
            if (dataAssembly.isEdsLowerStructureConstructionAssembly()) { // untere EDS Struktur
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if (selectedPartListEntries != null) {
                    for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
                        String saa = selectedPartListEntry.getPart().getFieldValue(FIELD_M_BESTNR);
                        if (saa.startsWith(SAA_NUMBER_PREFIX)) {
                            selectedSAAs.add(saa);
                        }
                    }
                }
            } else if (dataAssembly.isSaaPartsListConstructionAssembly()) { // SAA
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if (selectedPartListEntries != null) {
                    List<iPartsVirtualNode> nodesPath = dataAssembly.getVirtualNodesPath();

                    iPartsCTTModel cttModel = iPartsVirtualNode.isCTTSaaConstNode(nodesPath)
                                              ? iPartsCTTModel.getInstance(dataAssembly.getEtkProject(), new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(dataAssembly.getAsId())))
                                              : null;
                    for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
                        String saa = selectedPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_SAAKEY);

                        // Bei CTT steht die HMO Nummer in der Stückliste. Hier muss die HMO Nummer auf die SAA gemappt werden.
                        if (cttModel != null) {
                            String saaForHmo = cttModel.getSaaForHmo(saa);
                            if (StrUtils.isValid(saaForHmo)) {
                                saa = saaForHmo;
                            }
                        }
                        if (saa.startsWith(SAA_NUMBER_PREFIX)) {
                            selectedSAAs.add(saa);
                        }
                    }
                }
            }
        }
        return selectedSAAs;
    }

    private static String getSelectedSaaFromTree(AssemblyTreeFormIConnector connector) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            if (((iPartsDataAssembly)assembly).isSaaPartsListConstructionAssembly()) { // EDS-SAA
                boolean isCTTSaa = iPartsVirtualNode.isCTTSaaConstNode(((iPartsDataAssembly)assembly).getVirtualNodesPath());
                EdsSaaId saaId;
                if (isCTTSaa) {
                    saaId = iPartsVirtualNode.getCTTSaaFromAssemblyId(assembly.getAsId());
                } else {
                    saaId = iPartsEdsStructureHelper.getInstance().getEdsStructureSaaFromAssemblyId(assembly.getAsId());
                }
                if (saaId != null) {
                    return saaId.getSaaNumber();
                }
            } else if (((iPartsDataAssembly)assembly).isMBSStructureConstructionAssembly()) { // MBS-SAA
                iPartsSaaId saaId = iPartsVirtualNode.getMbsSAAFromAssemblyId(assembly.getAsId());
                if (saaId != null) {
                    return saaId.getSaaNumber();
                }
            }
        }

        return null;
    }

    /**
     * Fügt bei Retail-Stücklisten für jede SAA aus den SAA-Gültigkeiten einen neuen Untermenüeintrag für die Visualisierung
     * dieser SAA hinzu und setzt die Sichtbarkeit des Menüeintrags. Vorhandene Einträge werden vorher entfernt.
     *
     * @param connector
     * @param visualizeSAAMenuItem
     */
    private static void updateSelectSaaMenuEntriesForRetail(AssemblyListFormIConnector connector, GuiMenuItem visualizeSAAMenuItem) {
        visualizeSAAMenuItem.removeAllChildren();

        // Falls keine Retail-Stückliste, dann nur Untermenüeinträge entfernen, aber die Sichtbarkeit beibehalten
        boolean retailPartList = false;
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            retailPartList = ((iPartsDataAssembly)assembly).isRetailPartList();
        }
        if (!retailPartList) {
            return;
        }

        // Nur Einfachselektion ist zulässig
        EtkDataPartListEntry selectedPartListEntry = null;
        List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
        if ((selectedPartListEntries != null) && (selectedPartListEntries.size() == 1)) {
            selectedPartListEntry = selectedPartListEntries.get(0);
        }
        if (selectedPartListEntry == null) {
            visualizeSAAMenuItem.setVisible(false);
            return;
        }

        EditToolbarButtonMenuHelper toolbarHelper = new EditToolbarButtonMenuHelper(connector, null);
        EtkDataArray saaValidity = selectedPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY);
        visualizeSAAMenuItem.setVisible(!saaValidity.isEmpty());

        // Die Anzahl SAAs im Pop-up Menü beschränken, damit die Liste nicht mehr oben und unten über den Bildschirmrand hinausragt.
        List<String> saaList = saaValidity.getArrayAsStringList();
        if (saaList.size() <= iPartsMainImportHelper.MAX_ELEMS_FOR_SHOW) {
            for (String saa : saaList) {
                String formattedSAA = iPartsNumberHelper.formatPartNo(connector.getProject(), saa);
                GuiMenuItem menuItem = toolbarHelper.createMenuEntry(saa, formattedSAA, null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        visualizeSAA(saa);
                    }

                }, null);
                visualizeSAAMenuItem.addChild(menuItem);
            }
        } else {
            // Hier den iPartsShowDataObjectsDialog verwenden, wenn die Anzahl Elemente zu groß wird.
            GuiMenuItem menuItem = toolbarHelper.createMenuEntry("selectSaaMenu", "!!SAA-Auswahl...", null, new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    iPartsSaaId selectedSaaId = iPartsShowDataObjectsDialog.showGotoSaaElements(connector, connector.getActiveForm(),
                                                                                                saaList, false);
                    if (selectedSaaId != null) {
                        visualizeSAA(selectedSaaId.getSaaNumber());
                    }
                }
            }, null);
            visualizeSAAMenuItem.addChild(menuItem);
        }
    }
}