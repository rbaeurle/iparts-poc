/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.dwr.DocwareDwrLogger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.security.PasswordString;

/**
 * Hilfsklasse für die Congree Web Interface Integration
 */
public class CongreeWebInterfaceHelper {

    public static final String CONGREE_TEXT_CONTROL_CSS_CLASS = "CongreeTextControl";
    private static final String SESSION_KEY_CONGREE_LANGUAGE = "congree_language";

    /**
     * Erzeugt das JavaScript zum Einbetten der Congree Web Interface JavaScript-Dateien.
     *
     * @return
     */
    public static String createJavaScriptToInitCongree() {
        if (!iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_CONGREE_ACTIVE)) {
            return null;
        }

        // Congree Web Interface Integration
        String congreeBaseURI = StrUtils.removeLastCharacterIfCharacterIs(iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_CONGREE_URI).trim(), '/');
        String congreeJSPath = StrUtils.removeFirstCharacterIfCharacterIs(iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_CONGREE_JS_PATH).trim(), '/');
        return "<script type=\"text/javascript\" src=\"" + congreeBaseURI + "/" + congreeJSPath + "\"></script>";
    }

    private static String getCongreeLanguage() {
        String congreeLanguage = null;
        Session session = Session.get();
        if (session != null) {
            congreeLanguage = (String)session.getAttribute(SESSION_KEY_CONGREE_LANGUAGE);
            if (congreeLanguage == null) {
                // Beim ersten Aufruf wird die aktuelle DB-Sprache als Congree-Sprache festgelegt
                EtkProject project = (EtkProject)session.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
                if (project != null) {
                    congreeLanguage = project.getConfig().getCurrentDatabaseLanguage();
                    session.setAttribute(SESSION_KEY_CONGREE_LANGUAGE, congreeLanguage);
                }
            }
        }
        return congreeLanguage;
    }

    private static boolean selectedLanguageMatches(GuiMultiLangEdit multiLangEdit) {
        String congreeLanguage = getCongreeLanguage();
        if (congreeLanguage == null) {
            return false;
        }
        return ((multiLangEdit.getSelectedLanguage() == Language.DE) && congreeLanguage.equals(Language.DE.getCode()))
               || ((multiLangEdit.getSelectedLanguage() == Language.EN) && congreeLanguage.equals(Language.EN.getCode()));
    }

    /**
     * Macht die Congree Sidebar für das übergebene {@link GuiMultiLangEdit} verfügbar, falls prinzipiell die Congree Sidebar
     * aktiviert und das {@link GuiMultiLangEdit}-Control nicht readOnly ist.
     *
     * @param multiLangEdit
     * @param project
     */
    public static void enableCongreeSidebarForMultiLangEdit(GuiMultiLangEdit multiLangEdit, EtkProject project) {
        // Congree Web Interface Integration nur anzeigen falls aktiv und Text editierbar
        if (multiLangEdit.isReadOnly() || !iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_CONGREE_ACTIVE)) {
            return;
        }

        // Congree Sidebar nur bei Deutsch anzeigen und auch nur bei Deutsch das GuiMultiLangEdit für die Prüfung zulassen
        if (selectedLanguageMatches(multiLangEdit)) {
            multiLangEdit.setTextControlCssClass(CONGREE_TEXT_CONTROL_CSS_CLASS);
            multiLangEdit.addAdditionalJavascript(CongreeWebInterfaceHelper.createJavaScriptToShowCongreeSidebar(project));
        }
        multiLangEdit.addEventListener(new EventListener(GuiMultiLangEdit.EVENT_SELECTED_LANGUAGE_CHANGED) {
            @Override
            public void fire(Event event) {
                DocwareDwrLogger guiLogger = multiLangEdit.getGuiLogger();
                if (guiLogger != null) {
                    if (selectedLanguageMatches(multiLangEdit)) {
                        Session.get().invokeThreadSafeWithThread(() -> {
                            multiLangEdit.setTextControlCssClass(CONGREE_TEXT_CONTROL_CSS_CLASS);

                            // Congree Sidebar erst anzeigen nachdem die CSS-Klasse gesetzt wurde
                            guiLogger.addAjaxCommand_evaluateJavascript(CongreeWebInterfaceHelper.createJavaScriptToShowCongreeSidebar(project));
                        });
                    } else {
                        // Congree Sidebar sofort ausblenden
                        guiLogger.addAjaxCommand_evaluateJavascript(CongreeWebInterfaceHelper.createJavaScriptToHideCongreeSidebar());

                        Session.get().invokeThreadSafeWithThread(() -> multiLangEdit.setTextControlCssClass(null));
                    }
                }
            }
        });

        GuiWindow parentWindow = multiLangEdit.getParentWindow();
        if (parentWindow != null) {
            // Änderung durch Congree in das GuiMultiLangEdit übernehmen
            parentWindow.addEventListener(new EventListener(Event.OPENED_EVENT) {
                @Override
                public void fire(Event event) {
                    AbstractGuiControl textControl = multiLangEdit.getTextControl();
                    DocwareDwrLogger guiLogger = textControl.getGuiLogger();
                    if (guiLogger != null) {
                        // Änderung durch Congree kann nur durch einen onfocus-Event erkannt werden, der sich dem Server
                        // gegenüber aber wie ein onchange-Event verhalten muss
                        String textControlUniqueId = textControl.getUniqueId();
                        guiLogger.addAjaxCommand_addEvent(textControlUniqueId,
                                                          "onfocus", "try {dwFwPreEv(event,false,this);dwFwAsync(event,true);dwAjaxDefaultOnChange(event, '"
                                                                     + textControlUniqueId + "', '" + textControlUniqueId
                                                                     + "', '', true, true);dwFwPostEv(event)}catch(ex){try{dwFwExH(ex);}catch(ex2){}}",
                                                          true);
                    }
                }
            });

            // Congree Sidebar wieder ausblenden, wenn das Fenster mit dem GuiMultiLangEdit geschlossen wird
            parentWindow.addEventListener(new EventListener(Event.WINDOW_CLOSING_EVENT) {
                @Override
                public void fire(Event event) {
                    DocwareDwrLogger guiLogger = parentWindow.getGuiLogger();
                    if (guiLogger != null) {
                        guiLogger.addAjaxCommand_evaluateJavascript(CongreeWebInterfaceHelper.createJavaScriptToHideCongreeSidebar());

                        // TextControl leeren -> dadurch verschwinden auch alte Prüfergebnisse in der Congree Sidebar
                        multiLangEdit.clearTexts();
                    }
                }
            });
        }
    }

    /**
     * Erzeugt das JavaScript zum Laden der Congree Sidebar als Overlay.
     *
     * @return
     */
    public static String createJavaScriptToLoadCongreeSidebar() {
        // JavaScript-Code zum erstmaligen Laden der Congree Sidebar
        return "if (!window.Congree || !window.Congree.sidebarIPARTS || !window.Congree.sidebarOverlayIPARTS) { try { "
               + createJavaScriptForLoadCongreeSidebar() + " } catch (e) { alert(\""
               + TranslationHandler.translate("!!Fehler beim %1 der Congree Web Interface Integration.",
                                              TranslationHandler.translate("!!Laden"))
               + "\"); } };";
    }

    /**
     * Erzeugt das JavaScript zum Anzeigen der Congree Sidebar als Overlay.
     *
     * @param project
     * @return
     */
    public static String createJavaScriptToShowCongreeSidebar(EtkProject project) {
        // JavaScript-Code zum Anzeigen der Congree Sidebar
        return createJavaScriptToLoadCongreeSidebar() + "try { window.Congree.sidebarIPARTS.setUiLanguage(\"" + getCongreeUILanguage(project) + "\");"
               + "var sidebarDiv = $(\"congreeSidebar\"); if (sidebarDiv && sidebarDiv.parentNode) { dwDomRemoveStyle(sidebarDiv.parentNode, \"display\"); };" // Sidebar im DOM sichtbar machen
               + "window.Congree.sidebarOverlayIPARTS.show(); } catch (e) { alert(\"" // Sidebar über Congree-Funktion einblenden
               + TranslationHandler.translate("!!Fehler beim %1 der Congree Web Interface Integration.",
                                              TranslationHandler.translate("!!Anzeigen"))
               + "\"); };";
    }

    /**
     * Erzeugt das JavaScript zum Ausblenden der Congree Sidebar als Overlay.
     *
     * @return
     */
    public static String createJavaScriptToHideCongreeSidebar() {
        return "try { window.Congree.sidebarOverlayIPARTS.hide();" // Sidebar über Congree-Funktion ausblenden
               + "var sidebarDiv = $(\"congreeSidebar\"); if (sidebarDiv && sidebarDiv.parentNode) { dwDomSetStyle(sidebarDiv.parentNode, \"display\", \"none\"); }; } catch (e) { alert(\"" // Sidebar im DOM unsichtbar machen
               + TranslationHandler.translate("!!Fehler beim %1 der Congree Web Interface Integration.",
                                              TranslationHandler.translate("!!Ausblenden"))
               + "\"); };";
    }

    private static String createJavaScriptForLoadCongreeSidebar() {
        // Congree-Parameter
        UniversalConfiguration pluginConfig = iPartsEditPlugin.getPluginConfig();
        String congreeBaseURI = StrUtils.removeLastCharacterIfCharacterIs(pluginConfig.getConfigValueAsString(iPartsEditPlugin.CONFIG_CONGREE_URI).trim(), '/');
        String congreeUserName = pluginConfig.getConfigValueAsString(iPartsEditPlugin.CONFIG_CONGREE_USER_NAME).trim();
        if (congreeUserName.isEmpty()) {
            congreeUserName = iPartsUserAdminDb.getLoginUserName();
        }
        PasswordString congreePassword = pluginConfig.getConfigValueAsPassword(iPartsEditPlugin.CONFIG_CONGREE_PASSWORD);
        String congreeLoginType = pluginConfig.getConfigValueAsString(iPartsEditPlugin.CONFIG_CONGREE_LOGIN_TYPE).trim();

        boolean isEN = Utils.objectEquals(getCongreeLanguage(), Language.EN.getCode());

        // System-ID und Regel-Set basierend auf den Benutzereigenschaften auswählen
        UniversalConfigOption systemIdentifierConfigOption = null;
        UniversalConfigOption ruleSetConfigOption = null;
        if (iPartsRight.checkCarAndVanInSession()) { // PKW gewinnt vor Truck falls beide Benutzereigenschaften vorhanden sind
            systemIdentifierConfigOption = iPartsEditPlugin.CONFIG_CONGREE_SYSTEMS_IDENTIFIER_MBAG;
            ruleSetConfigOption = isEN ? iPartsEditPlugin.CONFIG_CONGREE_RULE_SET_MBAG_EN : iPartsEditPlugin.CONFIG_CONGREE_RULE_SET_MBAG_DE;
        } else if (iPartsRight.checkTruckAndBusInSession()) {
            systemIdentifierConfigOption = iPartsEditPlugin.CONFIG_CONGREE_SYSTEMS_IDENTIFIER_DTAG;
            ruleSetConfigOption = isEN ? iPartsEditPlugin.CONFIG_CONGREE_RULE_SET_DTAG_EN : iPartsEditPlugin.CONFIG_CONGREE_RULE_SET_DTAG_DE;
        }

        String congreeSystemIdentifier;
        if (systemIdentifierConfigOption != null) {
            congreeSystemIdentifier = pluginConfig.getConfigValueAsString(systemIdentifierConfigOption).trim();
        } else {
            congreeSystemIdentifier = "";
        }

        String congreeRuleSet;
        if (ruleSetConfigOption != null) {
            congreeRuleSet = pluginConfig.getConfigValueAsString(ruleSetConfigOption).trim();
        } else {
            congreeRuleSet = "";
        }

        String congreeCulture = pluginConfig.getConfigValueAsString(isEN ? iPartsEditPlugin.CONFIG_CONGREE_CULTURE_EN
                                                                         : iPartsEditPlugin.CONFIG_CONGREE_CULTURE_DE).trim();
        String congreeSemantics = pluginConfig.getConfigValueAsString(isEN ? iPartsEditPlugin.CONFIG_CONGREE_SEMANTICS_EN
                                                                           : iPartsEditPlugin.CONFIG_CONGREE_SEMANTICS_DE).trim();
        if (!congreeSemantics.isEmpty()) {
            congreeSemantics = ", '" + congreeSemantics + "'";
        }

        return "var congree = window.Congree || {};\n" +
               "window.Congree = congree;\n" +
               "congree.sidebarIPARTS = new Congree.Sidebar({\n" +
               "    serverAddress: \"" + congreeBaseURI + "/Congree\",\n" +
               "    authentication: {\n" +
               "        userName: \"" + congreeUserName + "\",\n" +
               "        password: \"" + congreePassword.decrypt() + "\",\n" +
               "        accessTokenURL: \"" + congreeBaseURI + "/congreeidentityserver/connect/token\",\n" +
               "        loginType: \"" + congreeLoginType + "\",\n" +
               "        thirdPartySystemsIdentifier: \"" + congreeSystemIdentifier + "\",\n" +
               "    },\n" +
               "    initialRuleSetName: \"" + congreeRuleSet + "\",\n" +
               "    requestValidTerms: false,\n" +
               "    culture: \"" + congreeCulture + "\",\n" +
               "    editors: [\n" +
               "        Congree.AdapterFor.TextInput('." + CONGREE_TEXT_CONTROL_CSS_CLASS + "'" + congreeSemantics + ")\n" +
//               "        Congree.AdapterFor.TextInput('." + CONGREE_TEXT_CONTROL_CSS_CLASS + "', {domSelector: 'textarea:not([style*=\"visibility:hidden\"]):not([style*=\"display:none\"]):not([style*=\"visibility: hidden\"]):not([style*=\"display: none\"])'}"
//               + congreeSemantics + "),\n" +
//               "        Congree.AdapterFor.TextInput('." + CONGREE_TEXT_CONTROL_CSS_CLASS + "', {domSelector: 'input[type=\"text\"]:not([style*=\"visibility:hidden\"]):not([style*=\"display:none\"]):not([style*=\"visibility: hidden\"]):not([style*=\"display: none\"])'}"
//               + congreeSemantics + ")\n" +
               "    ]\n" +
               "});\n" +
               "congree.sidebarOverlayIPARTS = congree.sidebarIPARTS.asOverlay({\n" +
               "        overlayPosition: \"right\",\n" +
               "        autoShowSidebarOnEditorError: false,\n" +
               "        autoShowSidebarOnEditorFocus: true,\n" +
               "        autoHideSidebarOnEditorFocusLost: false\n" +
               "});";
    }

    private static String getCongreeUILanguage(EtkProject project) {
        if (project.getViewerLanguage().equals(Language.DE.getCode())) {
            return "de";
        } else {
            return "en";
        }
    }
}