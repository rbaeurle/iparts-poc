/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.AbstractMechanicForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoEditContext;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPK;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsReservedPKId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditModuleFormInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.EnumSet;
import java.util.List;

/**
 * Abstraktes Formular für die Related-Infos.
 * Diese Klasse enthält Hilfsroutinen, die ermitteln, ob wir uns in der richtigen Struktur befinden.
 * Für das Einhängen der Popupmenüs gibt es ebenfalls Hilfsroutinen.
 */
public abstract class AbstractRelatedInfoPartlistDataForm extends RelatedInfoBaseForm {

    private boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();

    /**
     * Ermittelt, ob sich der übergebene {@link AbstractJavaViewerFormIConnector} in einem Edit-Kontext befindet (das aktuelle
     * Form implementiert das {@link EditModuleFormInterface}), wobei optional zusätzlich das Editieren auch noch erlaubt
     * sein muss.
     *
     * @param connector
     * @param checkIfEditAllowed
     * @return
     */
    public static boolean isEditContext(AbstractJavaViewerFormIConnector connector, boolean checkIfEditAllowed) {
        AbstractJavaViewerForm activeForm = connector.getActiveForm();
        if (activeForm instanceof EditModuleFormInterface) {
            if (checkIfEditAllowed) {
                if (connector instanceof EditFormIConnector) {
                    return ((EditFormIConnector)connector).isAuthorOrderValid();
                } else if (connector instanceof RelatedInfoBaseFormIConnector) {
                    if (((RelatedInfoBaseFormIConnector)connector).getEditContext() instanceof iPartsRelatedInfoEditContext) {
                        iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)((RelatedInfoBaseFormIConnector)connector).getEditContext();
                        if (editContext != null) {
                            return editContext.getEditFormConnector().isAuthorOrderValid();
                        }
                    }
                }
            } else {
                return true;
            }
        }

        return false;
    }

    public static GuiMenuItem modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector,
                                                      String menuItemName, String menuItemText, FrameworkImage menuItemIcon,
                                                      final String relatedInfoType) {
        // Separator vor Menüeintrag hinzufügen
        GuiSeparator menuItemSeparator = new GuiSeparator();
        menuItemSeparator.setUserObject(menuItemName + "Separator");
        popupMenu.addChild(menuItemSeparator);

        // Menüeintrag hinzufügen
        GuiMenuItem menuItemShowDataObjectGrid = new GuiMenuItem();
        menuItemShowDataObjectGrid.setUserObject(menuItemName);
        menuItemShowDataObjectGrid.setName(menuItemName);
        menuItemShowDataObjectGrid.setText(menuItemText);
        menuItemShowDataObjectGrid.setIcon(menuItemIcon);
        if (StrUtils.isValid(relatedInfoType)) {
            menuItemShowDataObjectGrid.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    String actualRelatedInfoType = relatedInfoType;
                    if (isEditContext(connector, false)) {
                        actualRelatedInfoType = RelatedInfoSingleEditHelper.getActiveRelatedInfo(connector.getProject(), actualRelatedInfoType);
                    }

                    iPartsRelatedInfoEditContext editContext = iPartsRelatedInfoEditContext.createEditContext(connector, isEditContext(connector, true));
                    JavaViewerMainWindow.showRelatedInfoForCurrentPartListEntry(connector, actualRelatedInfoType, editContext);
                }
            });
        }
        popupMenu.addChild(menuItemShowDataObjectGrid);
        return menuItemShowDataObjectGrid;
    }

    /**
     * Überprüft für eine ReleatedInfo in der Stückliste, ob das Menu (menuItemName) bzgl der validModulTypes sichtbar ist und setzt das Menu
     * Damit die validModuleTypes gleich sind wie beim Aufruf von updateTreePopupMenu wird auf das DestinationAssembly zugegriffen
     *
     * @param popupMenu
     * @param connector
     * @param menuItemName
     * @param validModuleTypes
     * @return
     */
    protected static GuiMenuItem updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector,
                                                         String menuItemName, EnumSet<iPartsModuleTypes> validModuleTypes) {
        boolean isValidForShow = false;
        EtkDataAssembly destinationAssembly = getDestinationAssemblyForPartListEntryFromConnector(connector);
        if (destinationAssembly != null) {
            isValidForShow = relatedInfoIsVisible(destinationAssembly, validModuleTypes);
        }
        return updatePartListPopupMenu(popupMenu, menuItemName, isValidForShow);
    }

    /**
     * Liefert die {@link EtkDataAssembly} des ausgewählten Stücklisteneintrags via {@link AssemblyListFormIConnector}
     *
     * @param connector
     * @return
     */
    public static EtkDataAssembly getDestinationAssemblyForPartListEntryFromConnector(AssemblyListFormIConnector connector) {
        if (connector.getProject().isEditModeActive()) {
            List<EtkDataPartListEntry> selectedEntries = connector.getSelectedPartListEntries();
            if ((selectedEntries != null) && (selectedEntries.size() == 1)) {
                AssemblyId destinationAssemblyId = selectedEntries.get(0).getDestinationAssemblyId();
                if (destinationAssemblyId.isValidId()) {
                    return EtkDataObjectFactory.createDataAssembly(connector.getProject(), destinationAssemblyId);
                }
            }
        }
        return null;
    }

    /**
     * Überprüft für eine Edit-RelatedInfo in der Stückliste, ob das Menu (menuItemName) bzgl der validModulTypes sichtbar ist und setzt das Menu
     *
     * @param popupMenu
     * @param connector
     * @param menuItemName
     * @param validModuleTypes
     * @return
     */

    protected static GuiMenuItem updatePartListPopupMenuForEdit(GuiContextMenu popupMenu, AssemblyListFormIConnector connector,
                                                                String menuItemName, EnumSet<iPartsModuleTypes> validModuleTypes) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean isValidAssemblyForDataObjectGrid = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            EtkDataAssembly assembly = connector.getCurrentAssembly();
            if (validModuleTypes != null) {
                isValidAssemblyForDataObjectGrid = relatedInfoIsVisible(assembly, validModuleTypes);
            } else {
                isValidAssemblyForDataObjectGrid = true;
            }
        }

        // Separator und Menüeintrag aktualisieren
        return updatePartListPopupMenu(popupMenu, menuItemName, isValidAssemblyForDataObjectGrid);
    }

    public static GuiMenuItem updatePartListPopupMenu(GuiContextMenu popupMenu, String menuItemName, boolean visible) {
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


    public static GuiMenuItem modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree, String menuItemName,
                                                  String menuItemText, final String relatedInfoType) {
        if (formWithTree instanceof AbstractAssemblyTreeForm) {
            final AbstractAssemblyTreeForm treeForm = (AbstractAssemblyTreeForm)formWithTree;
            EventListener listener = null;
            if (StrUtils.isValid(relatedInfoType)) {
                listener = new de.docware.framework.modules.gui.event.EventListener(Event.MENU_ITEM_EVENT) {
                    public void fire(de.docware.framework.modules.gui.event.Event event) {
                        treeForm.showRelatedInfoForCurrentTreeNode(relatedInfoType, null);
                    }
                };
            }
            ToolbarButtonMenuHelper menuItemHelper = new ToolbarButtonMenuHelper(formWithTree.getConnector(), null);
            GuiMenuItem treePopupMenuEntry = menuItemHelper.createMenuEntry(menuItemName, menuItemText,
                                                                            DefaultImages.module.getImage(),
                                                                            listener, TranslationHandler.getUiTranslationHandler());
            treePopupMenuEntry.setVisible(false);
            // UserObject wird gesetzt, damit später das Anzeigeverhalten für den jeweiligen Punkt bstimmt werden kann
            treePopupMenuEntry.setUserObject(menuItemName);
            menu.addChild(treePopupMenuEntry);
            return treePopupMenuEntry;
        }

        return null;
    }

    public static GuiMenuItem updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector, String menuItemName,
                                                  EnumSet<iPartsModuleTypes> validModuleTypes) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly != null) {
            boolean isVisible;
            if (validModuleTypes != null) {
                isVisible = relatedInfoIsVisible(assembly, validModuleTypes);
            } else {
                isVisible = true;
            }
            return setVisibilityForItem(popupMenu, menuItemName, isVisible);
        }

        return null;
    }

    /**
     * Setzt die Sichtbarkeit des übergebenen Popup-Menüeintrags
     *
     * @param popupMenu
     * @param menuItemName
     * @param isVisible
     */
    public static GuiMenuItem setVisibilityForItem(GuiContextMenu popupMenu, String menuItemName, boolean isVisible) {
        for (AbstractGuiControl item : popupMenu.getChildren()) {
            if ((item.getUserObject() != null) && item.getUserObject().equals(menuItemName)) {
                item.setVisible(isVisible);
                if (item instanceof GuiMenuItem) {
                    return (GuiMenuItem)item;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assemblyParam, EnumSet<iPartsModuleTypes> validModuleTypes) {
        if (assemblyParam == null) {
            return false;
        }

        if (assemblyParam instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)assemblyParam;

            // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
            for (iPartsModuleTypes moduleType : validModuleTypes) {
                switch (moduleType) {
                    case SA_TU:
                    case SpecialCatKG:
                    case WorkshopMaterial:
                    case DialogRetail:
                    case EDSRetail:
                    case PSK_PKW:
                    case PSK_TRUCK:
                    case CAR_PERSPECTIVE:
                    case Dialog_HM_Construction:
                    case Dialog_M_Construction:
                    case Dialog_SM_Construction:
                    case EDS_SAA_GROUP_Construction:
                    case EDS_SAA_SCOPE_Construction:
                    case EDS_SAA_MODULE_Construction:
                    case EDS_SAA_SUB_MODULE_Construction:
                    case EDS_SAA_Construction:
                    case CTT_SAA_Construction:
                    case MBS_LIST_NUMBER_Construction:
                    case MBS_CON_GROUP_Construction:
                    case PRODUCT:
                    case PRODUCT_KGTU:
                    case PRODUCT_EINPAS:
                    case KG:
                    case TU:
                        if (assembly.getEbeneName().equals(moduleType.getDbValue())) {
                            return true;
                        }
                        break;
                    case PRODUCT_MODEL:
                        if (assembly.isProductModelAssembly()) {
                            return true;
                        }
                        break;
                    case CONSTRUCTION_SERIES:
                        if (assembly.isConstructionSeriesAssembly()) {
                            return true;
                        }
                        break;
                    case CONSTRUCTION_MODEL:
                    case CONSTRUCTION_MODEL_MBS:
                    case CONSTRUCTION_MODEL_CTT:
                        if (assembly.isConstructionModelAssembly()) {
                            return true;
                        }
                        break;
                    default:
                        Logger.getLogger().throwRuntimeException("Unsupported module type for relatedInfoIsVisible(): "
                                                                 + TranslationHandler.translateForLanguage(moduleType.getDescription(),
                                                                                                           Language.EN.getCode()));
                }
            }
        }

        // Falls Unterbaugruppen ausgeblendet werden, muss auch deren Gültigkeit bzgl. validModuleTypes geprüft werden
        EtkDataPartListEntry hiddenSubAssembly = assemblyParam.getHiddenSingleSubAssembly(null);
        if (hiddenSubAssembly != null) {
            return relatedInfoIsVisible(EtkDataObjectFactory.createDataAssembly(hiddenSubAssembly.getEtkProject(), hiddenSubAssembly.getDestinationAssemblyId()),
                                        validModuleTypes);
        }

        return false;
    }

    public static boolean isDIALOGConstructionPartList(EtkDataAssembly assemblyParam) {
        return relatedInfoIsVisible(assemblyParam, EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction));
    }

    protected AbstractRelatedInfoPartlistDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
    }

    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), configKey);

        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields(configKey);
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }

            displayResultFields.loadStandards(getConfig());
        }

        return displayResultFields;
    }

    /**
     * Gibt die {@link iPartsProductId} vom Produkt zurück basierend auf der aktuellen Selektion im Baugruppenbaum. Dieser
     * Aufruf liefert nur dann ein Ergebnis zurück, wenn das aktive Formular ein {@link AbstractMechanicForm} ist.
     * <br/><b>Achtung! Bei Aggregaten in einem Fahrzeugprodukt wird mit dieser Methode immer nur das Fahrzeugprodukt
     * zurückgeliefert</b>
     *
     * @return {@code null} falls das aktive Formular kein {@link AbstractMechanicForm} ist oder aufgrund der aktuellen Selektion
     * kein Produkt gefunden werden konnte
     */
    protected iPartsProductId getProductIdFromCurrentAssemblyPath() {
        // Produkt für das aktuelle Modul aus dem Baugruppenbaum heraus bestimmen
        AbstractJavaViewerForm activeForm = getConnector().getActiveForm();
        if (!(activeForm instanceof AbstractMechanicForm)) {
            return null;
        }

        NavigationPath currentAssemblyPath = ((AbstractMechanicForm)activeForm).getConnector().getCurrentNavigationPath();
        for (PartListEntryId entryId : currentAssemblyPath) {
            String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(entryId.getOwnerAssemblyId());
            if (productNumber != null) {
                return new iPartsProductId(productNumber);
            }
        }

        return null;
    }

    /**
     * Handelt es sich um ein PSK-Modul mit entsprechender PSK-Doku-Methode bzw. dazugehörigem PSK-Produkt in der RelatedInfo?
     *
     * @return
     */
    protected boolean isPSKAssembly() {
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (partListEntry instanceof iPartsDataPartListEntry) {
            return ((iPartsDataPartListEntry)partListEntry).getOwnerAssembly().isPSKAssembly();
        }

        return false;
    }

    /**
     * Handelt es sich um einen Stücklisteneintrag mit gültigem BCTE-Schlüssel in der RelatedInfo?
     *
     * @return
     */
    protected boolean isPartListEntryWithValidBCTEKey() {
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        if (partListEntry != null) {
            return iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry) != null;
        }

        return false;
    }

    /**
     * Hilfsfunktion, die die Felddefinitionen für ein {@link EtkDisplayField} anlegt und der übergebenen Liste hinzufügt.
     *
     * @param list
     * @param tableName
     * @param fieldName
     * @param multiLang
     * @param isArray
     */
    protected void addDisplayField(List<EtkDisplayField> list, String tableName, String fieldName, boolean multiLang,
                                   boolean isArray, boolean filterColumn) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, multiLang, isArray);
        if (filterColumn) {
            displayField.setColumnFilterEnabled(true);
        }
        list.add(displayField);
    }

    protected abstract List<EtkDisplayField> createDefaultDisplayFields(String configKey);

    /**
     * Liefert den Feldnamen für den Status eines Datensatzes zurück.
     *
     * @return Bei {@code null} wird der Status nicht berücksichtigt
     */
    protected String getStatusFieldName() {
        return null;
    }

    /**
     * Liefert den Feldnamen für die Quelle eines Datensatzes zurück.
     *
     * @return Bei {@code null} wird die Quelle nicht berücksichtigt
     */
    public String getSourceFieldName() {
        return null;
    }

    /**
     * Liefert die zum übergebenen {@link EtkDataObject} gehörenden {@link iPartsDataDIALOGChange}-Datensätze.
     *
     * @param dataObject
     * @return Leere Liste, falls es keinen zum übergebenen {@link EtkDataObject} gehörenden {@link iPartsDataDIALOGChange}-Datensatz gibt
     */
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        return new DwList<>();
    }

    /**
     * Speichert das übergebene {@link EtkDataObject} im aktiven Edit-{@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet},
     * markiert diese RelatedEdit als verändert und lädt die gespeicherten Daten neu zur Aktualisierung der Grids.
     *
     * @param dataObject
     * @return War das übergebene {@link EtkDataObject} verändert und wurde deswegen gespeichert?
     * @see #reloadEditableDataAndUpdateEditContext
     */
    protected boolean saveDataObjectWithUpdate(EtkDataObject dataObject) {
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        modifiedDataObjects.add(dataObject, DBActionOrigin.FROM_EDIT);
        return saveDataObjectsWithUpdate(modifiedDataObjects);
    }

    /**
     * Speichert das übergebene {@link EtkDataObjectList} im aktiven Edit-{@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet},
     * markiert diese RelatedEdit als verändert und lädt die gespeicherten Daten neu zur Aktualisierung der Grids.
     *
     * @param dataObjectList
     * @return War mindestens ein {@link EtkDataObject} der übergebenen {@link EtkDataObjectList} verändert und wurde deswegen
     * gespeichert?
     * @see #reloadEditableDataAndUpdateEditContext
     */
    protected boolean saveDataObjectsWithUpdate(EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        if (!dataObjectList.isModifiedWithChildren()) {
            return false;
        }

        // Veränderte oder gelöschte EtkDataObjects ins Changeset schreiben
        List<SerializedDBDataObject> serializedDBDataObjectList = addDataObjectListToActiveChangeSetForEdit(dataObjectList);
        if ((serializedDBDataObjectList != null) && !serializedDBDataObjectList.isEmpty()) {
            setModifiedByEdit(true);
            reloadEditableDataAndUpdateEditContext();
            getConnector().dataChanged(null); // RelatedInfo-Daten updaten
            return true;
        } else {
            return false;
        }
    }

    /**
     * Lädt alle relevanten editierbaren Daten neu nachdem diese durch Edit-Aktionen verändert wurden und aktualisiert den
     * {@link de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext} mit den
     * notwendigen Flags.
     */
    protected void reloadEditableDataAndUpdateEditContext() {
    }

    /**
     * Überprüft den Status der übergebenen {@link EtkDataObject}s und setzt diesen bei Bedarf auf nur lesend falls es
     * einen dazugehörigen Datensatz in der Tabelle {@code DA_DIALOG_CHANGES} gibt, der bereits in einem anderen Autoren-Auftrag
     * bearbeitet wird.
     *
     * @param list
     */
    protected void checkStatusValuesForReadOnly(DBDataObjectList<? extends EtkDataObject> list) {
        if (!list.isEmpty()) {
            String statusFieldName = getStatusFieldName();
            ChangeSetId changeSetId = getAuthorOrderChangeSetId();
            if (StrUtils.isValid(statusFieldName) && (changeSetId != null)) {
                for (EtkDataObject dataObject : list) {
                    // zur Sicherheit, da verschiedene DataObjects in der Liste stehen können
                    if (dataObject.attributeExists(statusFieldName)) {
                        checkStatusValueForReadOnly(statusFieldName, changeSetId, dataObject);
                    }
                }
            }
        }
    }

    public void checkStatusValuesForReadOnly(EtkDataObject... dataObjects) {
        DBDataObjectList list = new DBDataObjectList();
        for (EtkDataObject obj : dataObjects) {
            list.add(obj, DBActionOrigin.FROM_DB);
        }
        checkStatusValuesForReadOnly(list);
    }

    protected void checkStatusValueForReadOnly(String statusFieldName, ChangeSetId changeSetId, EtkDataObject dataObject) {

        if ((statusFieldName == null) || (changeSetId == null)) {
            // Ohne aktives ChangeSet ist gar keine Prüfung notwendig
            return;
        }

        String stateValue = dataObject.getFieldValue(statusFieldName);
        iPartsDataReleaseState state = iPartsDataReleaseState.getTypeByDBValue(stateValue);

        // Status auf readOnly setzen, wenn es DIALOG-Änderungen mit anderer ChangeSetGUID gibt
        state = state.getReadOnlyState();
        if (state != null) { // Ohne möglichen ReadOnly-Zustand ist gar keine Prüfung notwendig
            List<iPartsDataDIALOGChange> dataDIALOGChanges = getDataDIALOGChanges(dataObject);
            for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChanges) {
                if ((dataDIALOGChange != null) && dataDIALOGChange.existsInDB()) {
                    String dbChangeSetGUID = dataDIALOGChange.getFieldValue(iPartsConst.FIELD_DDC_CHANGE_SET_GUID);
                    if (!(dbChangeSetGUID.isEmpty() || (changeSetId.getGUID().equals(dbChangeSetGUID)))) {
                        dataObject.setFieldValue(statusFieldName, state.getDbValue(), DBActionOrigin.FROM_DB);
                        break; // Read-Only wird beim ersten fremden ChangeSet gesetzt
                    }
                }
            }
        }
    }

    /**
     * Liefert die {@link ChangeSetId} des gerade aktiven Autoren-Auftrags für Edit zurück (sofern einer aktiv ist).
     *
     * @return {@code null} falls gerade kein Autoren-Auftrag für Edit aktiv ist
     */
    public ChangeSetId getAuthorOrderChangeSetId() {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            AbstractRevisionChangeSet activeChangeSetForEdit = editContext.getAuthorOrderChangeSetForEdit();
            if (activeChangeSetForEdit != null) {
                return activeChangeSetForEdit.getChangeSetId();
            }
        }

        return null;
    }

    /**
     * Ein {@link Runnable} hinzufügen, welches beim Speichern der RelatedEdit direkt am Anfang ausgeführt werden soll.
     *
     * @param runnable
     */
    public void addSaveEditRunnable(Runnable runnable) {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            editContext.addSaveEditRunnable(runnable);
        }
    }

    /**
     * Ein {@link Runnable} hinzufügen, welches beim Abbrechen der RelatedEdit direkt am Anfang ausgeführt werden soll.
     *
     * @param runnable
     */
    public void addCancelEditRunnable(Runnable runnable) {
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = ((iPartsRelatedInfoEditContext)getConnector().getEditContext());
            editContext.addCancelEditRunnable(runnable);
        }
    }

    protected void addEditRunnablesForReservedPK(final IdWithType idToReserve) {
        // Primärschlüssel-Reservierung korrigieren beim Speichern (echtes Edit-ChangeSet referenzieren anstatt dem temporären
        // ChangeSet der RelatedEdit)
        addSaveEditRunnable(() -> {
            RelatedInfoEditContext editContext = getConnector().getEditContext();
            if (editContext instanceof iPartsRelatedInfoEditContext) {
                AbstractRevisionChangeSet authorOrderChangeSetForEdit = ((iPartsRelatedInfoEditContext)editContext).getAuthorOrderChangeSetForEdit();
                if (authorOrderChangeSetForEdit != null) {
                    iPartsDataReservedPK dataReservedPK = new iPartsDataReservedPK(getProject(), new iPartsReservedPKId(idToReserve));
                    if (!dataReservedPK.existsInDB()) { // Kann eigentlich nicht passieren
                        dataReservedPK.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                    dataReservedPK.setChangeSetId(authorOrderChangeSetForEdit.getChangeSetId().getGUID(), DBActionOrigin.FROM_EDIT);
                    dataReservedPK.saveToDB();
                }
            }
        });

        // Primärschlüssel-Reservierung wieder löschen beim Abbruch vom Edit
        addCancelEditRunnable(() -> iPartsDataReservedPKList.deleteReservedPrimaryKey(getProject(), idToReserve));
    }

    /**
     * Handelt es sich beim EditContext um einen {@link iPartsRelatedInfoEditContext}?
     *
     * @return
     */
    protected boolean isRelatedInfoEditContext() {
        return getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext;
    }

    public boolean isCarAndVanInSession() {
        return carAndVanInSession;
    }

    public boolean isTruckAndBusInSession() {
        return truckAndBusInSession;
    }
}