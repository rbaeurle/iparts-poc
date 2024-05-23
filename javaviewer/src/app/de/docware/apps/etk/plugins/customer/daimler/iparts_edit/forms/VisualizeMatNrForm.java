/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.SmaragdVisHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.forms.CopyTextWindow;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Fügt den Kontextmenüeintrag "Teilenummer visualisieren" für Teile hinzu, der die Materialnummer ohne Leerzeichen anzeigt
 */
public class VisualizeMatNrForm {

    private static final String IPARTS_MENU_ITEM_VISUALIZE_2D = "iPartsMenuItemVisualize2D";
    private static final String IPARTS_MENU_ITEM_VISUALIZE_3D = "iPartsMenuItemVisualize3D";

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {

        // Separator vor Menüeintrag hinzufügen
        GuiSeparator menuItemSeparator = new GuiSeparator();
        menuItemSeparator.setUserObject(IPARTS_MENU_ITEM_VISUALIZE_3D + "Separator");
        popupMenu.addChild(menuItemSeparator);

        final String keyDescription = "!!Standardeinstellungen für";
        final String closeDescription = "!!Anschließend beenden Sie den Dialog mit:";

        // Menüeintrag hinzufügen
        GuiMenuItem menuItemVisualize2D = new GuiMenuItem();
        menuItemVisualize2D.setUserObject(IPARTS_MENU_ITEM_VISUALIZE_2D);
        menuItemVisualize2D.setName(IPARTS_MENU_ITEM_VISUALIZE_2D);
        menuItemVisualize2D.setText("!!2D Visualisierung");
        menuItemVisualize2D.setIcon(DefaultImages.link.getImage());
        menuItemVisualize2D.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                iPartsDataPart selectedPart = getSelectedPart(connector);
                if (selectedPart != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append(TranslationHandler.translate("!!2D-Modell über VIS-Tray-Shortcuts öffnen"));
                    sb.append("<br>");
                    sb.append(TranslationHandler.translate(keyDescription));
                    sb.append(" 2D: <b>[");
                    sb.append(TranslationHandler.translate("!!STRG"));
                    sb.append("]+[2]</b><br>");
                    sb.append(TranslationHandler.translate(closeDescription));
                    sb.append(" <b>[");
                    sb.append(TranslationHandler.translate("!!ESC"));
                    sb.append("]</b>.</html>");
                    CopyTextWindow window = new CopyTextWindow(getMatNr2D(selectedPart));
                    window.setTitle("!!2D Visualisierung", null);
                    window.setLabelText(sb.toString());
                    window.showModal();
                }
            }
        });
        popupMenu.addChild(menuItemVisualize2D);

        // Menüeintrag hinzufügen
        GuiMenuItem menuItemVisualize3D = new GuiMenuItem();
        menuItemVisualize3D.setUserObject(IPARTS_MENU_ITEM_VISUALIZE_3D);
        menuItemVisualize3D.setName(IPARTS_MENU_ITEM_VISUALIZE_3D);
        menuItemVisualize3D.setText("!!3D Visualisierung");
        menuItemVisualize3D.setIcon(DefaultImages.link.getImage());
        menuItemVisualize3D.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                List<iPartsDataPart> selectedPartsMultiSelection = getSelectedPartsMultiSelection(connector);
                Set<String> matNr3D = getMatNr3D(selectedPartsMultiSelection);
                if (!matNr3D.isEmpty()) {
                    SmaragdVisHelper.openSmaragdURLSilent(matNr3D);
                }
            }
        });
        popupMenu.addChild(menuItemVisualize3D);
    }

    private static String getMatNr2D(iPartsDataPart part) {
        String matNr2d = part.getFieldValue(iPartsConst.FIELD_M_REFSER);
        if (StrUtils.isValid(matNr2d) && isValidPartNumber(matNr2d)) {
            // wenn es eine "Siehe-SNR" gibt, diese nur ausgeben wenn es eine gültige Teilenummer ist
            return matNr2d;
        }
        return part.getAsId().getMatNr();
    }

    private static Set<String> getMatNr3D(List<iPartsDataPart> parts) {
        Set<String> result = new TreeSet<>();
        if (parts != null) {
            for (iPartsDataPart part : parts) {
                result.add(part.getFieldValue(EtkDbConst.FIELD_M_MATNR));
            }
        }
        return result;
    }

    private static boolean isValidPartNumber(String partNo) {
        if (StrUtils.isValid(partNo)) {
            // hier zusätzlich noch prüfen ob mindestens eine Ziffer in der Materialnummer enthalten ist
            // unglücklicherweise ist "NORMBLATT" im NumberHelper eine gültige Teilenummer
            boolean hasDigit = false;
            for (int i = 0; i < partNo.length(); i++) {
                if (Character.isDigit(partNo.charAt(i))) {
                    hasDigit = true;
                    break;
                }
            }
            if (hasDigit) {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                String formattedPartNo = numberHelper.checkNumberInputFormat(partNo, null);
                return StrUtils.isValid(formattedPartNo);
            }
        }
        return false;
    }

    private static iPartsDataPart getSelectedPart(AssemblyListFormIConnector connector) {
        List<iPartsDataPart> selectedPartsMultiSelection = getSelectedPartsMultiSelection(connector);
        if (selectedPartsMultiSelection.size() == 1) {
            return selectedPartsMultiSelection.get(0);
        }
        return null;
    }

    private static List<iPartsDataPart> getSelectedPartsMultiSelection(AssemblyListFormIConnector connector) {
        List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
        List<iPartsDataPart> selectedParts = new DwList<>();
        if ((selectedPartListEntries != null)) {
            for (EtkDataPartListEntry selectedPartListEntry : selectedPartListEntries) {
                if (selectedPartListEntry.getPart() instanceof iPartsDataPart) {
                    iPartsDataPart part = (iPartsDataPart)(selectedPartListEntry.getPart());
                    if (!part.getAsId().isVirtual() && !selectedPartListEntry.isAssembly()) {
                        selectedParts.add(part);
                    }
                }
            }
        }
        return selectedParts;
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        iPartsDataPart selectedPart = getSelectedPart(connector);

        // Teilenummer nicht visualisierbar, wenn es kein Teil ist oder wenn das Teil keine Teilenummer hat
        boolean active2Dvis = iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_2D_VIS_ACTIVE);
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_VISUALIZE_2D, active2Dvis && (selectedPart != null) && !getMatNr2D(selectedPart).isEmpty());

        // Die 3D Visualisierung darf auch mit mehreren Teilenummern aufgerufen werden
        boolean vis3Denabled = false;
        if (AbstractApplication.isOnline()) {
            List<iPartsDataPart> selectedPartsMultiSelection = getSelectedPartsMultiSelection(connector);
            Set<String> selectedMatNrs = getMatNr3D(selectedPartsMultiSelection);
            vis3Denabled = !selectedMatNrs.isEmpty();
        }
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_VISUALIZE_3D, vis3Denabled);
    }

    protected static GuiMenuItem updatePartListPopupMenu(GuiContextMenu popupMenu, String menuItemName, boolean visible) {
        GuiMenuItem result = null;

        // Separator und Menüeintrag aktualisieren
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (child.getUserObject() != null) {
                if (child.getUserObject().equals(menuItemName + "Separator")) {
                    child.setVisible(visible);
                } else if (child.getUserObject().equals(menuItemName)) {
                    child.setVisible(visible);
                    result = (GuiMenuItem)child;
                }
            }
        }

        return result;
    }

}
