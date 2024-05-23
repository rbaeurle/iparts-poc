/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.MechanicFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction.AbstractConstModelSelectionForm;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.session.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helfer-Klasse für die Navigation durch Baumuster-basierte Konstruktionsdaten
 */
public class iPartsConstModelHelper implements iPartsConst {

    enum ConstModelType {
        MODEL_EDS,
        MODEL_MBS,
        MODEL_CTT
    }


    public static void handleFilterValuesAfterPathChanged(MechanicFormIConnector connector, List<iPartsVirtualNode> virtNodes) {
        // Sind noch keine Baumuster für den Filter gesetzt, soll der Dialog direkt beim Anzeigen der leeren
        // Stückliste geöffnet werden -> Check, ob es sich um ein EDS oder MBS Konstruktionsbaumuster handelt und ob schon
        // Filterwerte gesetzt sind
        if (connector == null) {
            return;
        }
        // Bestimmen, ob es sich um MBS oder EDS handelt
        ConstModelType constModelType = getConstModeltype(virtNodes);
        if (constModelType == null) {
            return;
        }

        EtkProject project = connector.getProject();
        iPartsStructureId structureId = (iPartsStructureId)virtNodes.get(0).getId();
        String prefix = getPrefixForModels(structureId);
        Session session = Session.get();

        Map<String, Set<String>> filterValues;
        if (constModelType == ConstModelType.MODEL_MBS) {
            filterValues = handleMBSFilterValues(project, session, prefix);
        } else if (constModelType == ConstModelType.MODEL_CTT) {
            filterValues = handleCTTFilterValues(project, session, prefix);
        } else {
            filterValues = handleEDSFilterValues(project, session, prefix);
        }
        // Sind keine Filterwerte gesetzt (Stückliste ist leer), zeige den Auswahldialog
        if ((filterValues == null) || ((filterValues.get(prefix) == null) || filterValues.get(prefix).isEmpty())) {
            showModelSelection(connector);
        }

    }

    /**
     * Liefert aus Basis der <code>virtNodes</code> den aktuellen {@link ConstModelType}
     *
     * @param virtNodes
     * @return
     */
    private static ConstModelType getConstModeltype(List<iPartsVirtualNode> virtNodes) {
        if (iPartsVirtualNode.isNodeWithinCTTConstStructure(virtNodes) && iPartsVirtualNode.isCTTConstNode(virtNodes)) {
            return ConstModelType.MODEL_CTT;
        }
        if (iPartsVirtualNode.isNodeWithinMBSConstStructure(virtNodes) && iPartsVirtualNode.isMBSConstNode(virtNodes)) {
            return ConstModelType.MODEL_MBS;
        }
        if (iPartsVirtualNode.isNodeWithinEdsBcsConstStructure(virtNodes) && iPartsVirtualNode.isEdsConstNode(virtNodes)) {
            return ConstModelType.MODEL_EDS;
        }
        return null;
    }

    private static boolean checkSaveUserFilterSettings(Map<String, Set<String>> userFilterValues, Map<String, Set<String>> filterValues) {
        boolean saveUserFilterSettings = false;
        for (Map.Entry<String, Set<String>> entry : userFilterValues.entrySet()) {
            Set<String> bmSet = entry.getValue();
            if (bmSet != null) {
                saveUserFilterSettings = true;
                Set<String> filterBmSet = filterValues.get(entry.getKey());
                if (filterBmSet == null) {
                    filterValues.put(entry.getKey(), bmSet);
                } else {
                    filterBmSet.addAll(bmSet);
                }
            }
        }
        return saveUserFilterSettings;
    }

    private static Map<String, Set<String>> handleEDSFilterValues(EtkProject project, Session session, String prefix) {
        String sessionKey = iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL;
        Map<String, Set<String>> filterValues = getFilterValuesFromSession(session, sessionKey);
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            if ((filterValues == null) || ((filterValues.get(prefix) == null) || filterValues.get(prefix).isEmpty())) {
                Map<String, Set<String>> userFilterValues = iPartsUserSettingsHelper.getSelectedEDSConstModels(project);
                if (filterValues == null) {
                    filterValues = new HashMap<>();
                }
                boolean saveUserFilterSettings = checkSaveUserFilterSettings(userFilterValues, filterValues);
                if (saveUserFilterSettings) {
                    iPartsUserSettingsHelper.setSelectedEDSConstModels(project, filterValues);
                }
                handleFilterValues(project, session, sessionKey, filterValues, prefix);
            }
        }

        return filterValues;
    }

    private static Map<String, Set<String>> getFilterValuesFromSession(Session session, String sessionKey) {
        return (Map<String, Set<String>>)session.getAttribute(sessionKey);
    }


    private static Map<String, Set<String>> handleMBSFilterValues(EtkProject project, Session session, String prefix) {
        String sessionKey = iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL;
        Map<String, Set<String>> filterValues = getFilterValuesFromSession(session, sessionKey);
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            if ((filterValues == null) || ((filterValues.get(prefix) == null) || filterValues.get(prefix).isEmpty())) {
                Map<String, Set<String>> userFilterValues = iPartsUserSettingsHelper.getSelectedMBSConstModels(project);
                if (filterValues == null) {
                    filterValues = new HashMap<>();
                }
                boolean saveUserFilterSettings = checkSaveUserFilterSettings(userFilterValues, filterValues);
                if (saveUserFilterSettings) {
                    iPartsUserSettingsHelper.setSelectedMBSConstModels(project, filterValues);
                }
                handleFilterValues(project, session, sessionKey, filterValues, prefix);
            }
        }

        return filterValues;
    }

    private static Map<String, Set<String>> handleCTTFilterValues(EtkProject project, Session session, String prefix) {
        String sessionKey = iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL;
        Map<String, Set<String>> filterValues = getFilterValuesFromSession(session, sessionKey);
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            if ((filterValues == null) || ((filterValues.get(prefix) == null) || filterValues.get(prefix).isEmpty())) {
                Map<String, Set<String>> userFilterValues = iPartsUserSettingsHelper.getSelectedCTTConstModels(project);
                if (filterValues == null) {
                    filterValues = new HashMap<>();
                }
                boolean saveUserFilterSettings = checkSaveUserFilterSettings(userFilterValues, filterValues);
                if (saveUserFilterSettings) {
                    iPartsUserSettingsHelper.setSelectedCTTConstModels(project, filterValues);
                }
                handleFilterValues(project, session, sessionKey, filterValues, prefix);
            }
        }

        return filterValues;
    }

    private static void handleFilterValues(EtkProject project, Session session, String sessionKey, Map<String, Set<String>> filterValues, String prefix) {
        session.setAttribute(sessionKey, filterValues);
        notifyApplication(project, session, filterValues, prefix);
    }

    private static void notifyApplication(EtkProject project, Session session, Map<String, Set<String>> filterValues, String prefix) {
        if ((filterValues.get(prefix) != null) && !filterValues.get(prefix).isEmpty()) {
            session.invokeThreadSafeWithThread(() -> {
                // Refresh auf der Ansicht
                project.fireProjectEvent(new FilterChangedEvent(), true);
            });
        }
    }

    private static String getPrefixForModels(iPartsStructureId structureId) {
        if (structureId.getStructureName().equals(STRUCT_EDS_AGGREGATE_NAME) || structureId.getStructureName().equals(STRUCT_MBS_AGGREGATE_NAME) ||
            structureId.getStructureName().equals(STRUCT_CTT_AGGREGATE_NAME)) {
            return MODEL_NUMBER_PREFIX_AGGREGATE;
        }
        return MODEL_NUMBER_PREFIX_CAR;
    }

    private static void showModelSelection(final AssemblyListFormIConnector connector) {
        AbstractConstModelSelectionForm.showSelectionForm(connector);
    }


    /**
     * Elemnte für Structure Model zum PartListPanel hinzufügen
     * EbeneName == PARTS_LIST_TYPE_STRUCTURE_MODEL
     *
     * @param northPanel
     * @param connector
     * @return
     */
    public static GuiPanel modifyForStructureModelPartListPanel(GuiPanel northPanel, final AssemblyListFormIConnector connector) {
        GuiButton button = new GuiButton("!!Baumusterauswahl");
        button.setName("buttonLimitModels");
        button.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                showModelSelection(connector);
            }
        });
        northPanel.addChild(button);

        return northPanel;
    }
}
