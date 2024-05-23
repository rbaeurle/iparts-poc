/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.MbsKemDataSheetHelper;
import de.docware.framework.modules.gui.controls.menu.GuiMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.j2ee.EC;

/**
 * Hilfsklasse für den Aufruf des Docu Browsers.
 * Dazu werden im EDS Arbeitsvorrat für KEM und SAA entsprechende Kontextmenüs eingeblendet. Die Funktion ist über eine
 * Adminoption abschaltbar.
 * Es werden für KEM und SAA jeweils unterschiedliche URIs zusammengebaut die so in DAIMLER-13630 vorgegeben wurden.
 */
public class DocuBrowserEDSHelper implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_DOCUBROWSER = "iPartsMenuItemDocuBrowser";
    public static final String IPARTS_MENU_ITEM_TEXT_DOCUBROWSER = "!!DocuBrowser anzeigen";
    public static final String IPARTS_MENU_ITEM_TEXT_DOCUBROWSER_SAA = "!!DocuBrowser für SAA anzeigen";

    private static final String KEM_WILDCARD = "{{KEM}}";
    private static final String SAA_WILDCARD = "{{SAA}}";

    private static final String ERRORMESSAGE = "!!DocuBrowser Anzeige für %1: \"%2\" nicht möglich.";
    private static final String ERRORMESSAGE_CONFIG = "!!In der URI für die Anzeige des DocuBrowser für %1 fehlt der Platzhalter \"%2\".";

    public static GuiMenuItem createDocuBrowserPopupMenuItem(EditToolbarButtonMenuHelper toolbarHelper, iPartsWorkBasketTypes wbType, TranslationHandler translationHandler,
                                                             EventListener eventListener) {
        String menuText;
        switch (wbType) {
            case EDS_SAA_WB:
                menuText = IPARTS_MENU_ITEM_TEXT_DOCUBROWSER_SAA;
                break;
            case EDS_KEM_WB:
                menuText = IPARTS_MENU_ITEM_TEXT_DOCUBROWSER;
                break;
            default:
                menuText = "!!DocuBrowser";
                break;
        }

        return toolbarHelper.createMenuEntry(DocuBrowserEDSHelper.IPARTS_MENU_ITEM_DOCUBROWSER, menuText,
                                             DefaultImages.link.getImage(), eventListener, translationHandler);
    }

    /**
     * Erzeugt die URI zum DocuBrowser für eine KEM, und öffnet diese in einem separaten Fenster.
     * Die KEM wird dazu ins SAP Format gebracht. Sollte die umformatierung Fehlschlagen wird eine Fehlermeldung ausgegeben.
     *
     * @param kemNo
     */
    public static void showDocuBrowserKEM(String kemNo) {
        String uri = iPartsEditPlugin.getDocuBrowseKemUriTemplate();
        if (!uri.contains(KEM_WILDCARD)) {
            MessageDialog.showError(TranslationHandler.translate(ERRORMESSAGE_CONFIG, "KEM", KEM_WILDCARD));
            return;
        }

        MbsKemDataSheetHelper kemHelper = new MbsKemDataSheetHelper(kemNo, 5);
        String formattedKem = kemHelper.getSAPFormat();
        if (StrUtils.isValid(formattedKem)) {
            uri = uri.replace(KEM_WILDCARD, formattedKem);
            Utils.openExternalUrl(uri);
        } else {
            MessageDialog.showError(TranslationHandler.translate(ERRORMESSAGE, "KEM", kemNo));
        }
    }

    /**
     * Erzeugt die URI zum DocuBrowser für eine SAA, und öffnet diese in einem separaten Fenster.
     * Die SAA wird dabei im DB Format verwendet und encoded, so dass enthaltene Leerzeichen kein Problem darstellen.
     * Falls die SAA nicht encoded werden kann, wird eine Fehlermeldung ausgegeben.
     *
     * @param saaBkNo
     */
    public static void showDocuBrowserSAA(String saaBkNo) {
        String uri = iPartsEditPlugin.getDocuBrowserSaaUriTemplate();
        if (!uri.contains(SAA_WILDCARD)) {
            MessageDialog.showError(TranslationHandler.translate(ERRORMESSAGE_CONFIG, "SAA", SAA_WILDCARD));
            return;
        }
        String encodedSAA = EC.encodeURIPath(saaBkNo);
        if (encodedSAA != null) {
            uri = uri.replace(SAA_WILDCARD, encodedSAA);
            Utils.openExternalUrl(uri);
        } else {
            MessageDialog.showError(TranslationHandler.translate(ERRORMESSAGE, "SAA", saaBkNo));
        }
    }

    /**
     * Fügt dem übergebenen Kontextmenü zwei Untermenüs für die SAA und die KEM hinzu, sofern diese gültig sind.
     * Die SAA wird dabei für die Anzeige formatiert, die KEM nicht.
     *
     * @param connector
     * @param docuBrowserMenu
     * @param saaBkNo
     * @param kemNo
     */
    public static void addDocuBrowserSubMenus(AbstractJavaViewerFormIConnector connector, GuiMenu docuBrowserMenu, String saaBkNo, String kemNo) {
        if (docuBrowserMenu != null) {
            docuBrowserMenu.setEnabled(StrUtils.isValid(kemNo) || StrUtils.isValid(saaBkNo));
            docuBrowserMenu.removeAllChildren();

            EditToolbarButtonMenuHelper toolbarHelper = new EditToolbarButtonMenuHelper(connector, null);
            if (StrUtils.isValid(kemNo)) {
                GuiMenuItem kemSubMenu = toolbarHelper.createMenuEntry(kemNo, kemNo, null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        DocuBrowserEDSHelper.showDocuBrowserKEM(kemNo);
                    }
                }, null);
                docuBrowserMenu.addChild(kemSubMenu);
            }

            if (StrUtils.isValid(saaBkNo)) {
                String saaFormatted = iPartsNumberHelper.formatPartNo(connector.getProject(), saaBkNo);
                GuiMenuItem saaSubMenu = toolbarHelper.createMenuEntry(saaFormatted, saaFormatted, null, new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        DocuBrowserEDSHelper.showDocuBrowserSAA(saaBkNo);
                    }
                }, null);
                docuBrowserMenu.addChild(saaSubMenu);
            }
        }
    }
}