/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.events.OnNewEvent;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormEvents;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.*;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkHotspotDestination;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditModuleFormInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsRetailUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.GotoEditPartWithPartialPathEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLoadEditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsCopyTUJobHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerLink;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.*;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Formular für das Editieren von Modulen in iParts.
 */
public class EditModuleForm extends AbstractJavaViewerMainFormContainer implements EditModuleFormInterface {

    public class GuiTabbedPaneEntryEdit extends GuiTabbedPaneEntry {

        private EditModuleFormConnector connector;
        public EditAssemblyListForm editAssemblyListForm;
        public EditAssemblyImageForm editImageListForm;
        private AssemblyImageFormEvents editImageEventListener;
        public EditHeaderForm editHeaderForm;
        public EditMaterialEditForm editMaterialEditForm;
        public EditModuleEditForm editModuleEditForm;
        public PanelTabbedClass panelTabbed;
        public PanelTabbedTwoClass panelTabbedTwo;

        private LazyLoadEditModuleInfo lazyLoadInfo;
        private LazyLoadEditModuleInfo preparedForLoadInfo;
        private boolean guiCreated;

        public GuiTabbedPaneEntryEdit() {
            super();
            addEventListener(new EventListener(Event.CLOSED_EVENT) {
                @Override
                public void fire(Event event) {
                    // nur beim echten Schließen von diesem Tab alle Forms schließen
                    if (event.getReceiverId().equals(getUniqueId())) {
                        close();
                    }
                }
            });
            this.lazyLoadInfo = null;
            this.preparedForLoadInfo = null;
        }

        public EditModuleFormConnector getConnector() {
            return connector;
        }

        public void close() {
            if (editImageListForm != null) {
                editImageListForm.close();
            }
            if (editAssemblyListForm != null) {
                editAssemblyListForm.close();
            }
            if (editHeaderForm != null) {
                editHeaderForm.close();
            }
            if (editMaterialEditForm != null) {
                editMaterialEditForm.close();
            }
            if (editModuleEditForm != null) {
                editModuleEditForm.close();
            }
            if (connector != null) {
                connector.dispose();
            }
            if ((lazyLoadInfo != null) && (lazyLoadInfo.getConnector() != null)) {
                lazyLoadInfo.getConnector().dispose();
            }
            if ((preparedForLoadInfo != null) && (preparedForLoadInfo.getConnector() != null)) {
                preparedForLoadInfo.getConnector().dispose();
            }
        }

        /**
         * Selektiert den Stücklisteneintrag mit der übergebenen laufenden Nummer
         *
         * @param kLfdnr
         * @return Selektierter Stücklisteneintrag bzw. {@code null} falls keiner gefunden werden konnte mit der übergebenen
         * laufenden Nummer
         */
        public EtkDataPartListEntry selectPartListEntryByLfdnr(String kLfdnr) {
            if (connector == null) {
                return null;
            }

            EtkDataPartListEntry partListEntry = connector.getCurrentAssembly().getPartListEntryFromKLfdNrUnfiltered(kLfdnr);
            if (partListEntry != null) {
                List<EtkDataPartListEntry> selectedPartListEntries = new DwList<>(1);
                selectedPartListEntries.add(partListEntry);
                connector.setSelectedPartListEntries(selectedPartListEntries);
                connector.updateAllViews(null, false);
            }
            return partListEntry;
        }

        public boolean isLazyLoading() {
            return lazyLoadInfo != null;
        }

        public void prepareForLazyLoading(EtkDataAssembly assembly, String kLfdNr,
                                          EditModuleFormConnector connector) {
            lazyLoadInfo = new LazyLoadEditModuleInfo(connector, assembly, kLfdNr);
        }

        public void doLazyLoading() {
            if (isLazyLoading()) {
                if (!lazyLoadInfo.isCancelled) {
                    loadModuleInPseudoTransActionInEvent(this, lazyLoadInfo.lazyAssembly,
                                                         lazyLoadInfo.lazyKLfdNr, lazyLoadInfo.getConnector());
                    lazyLoadInfo = null;
                }
            }
        }

        public boolean isPreparedForLoading() {
            return preparedForLoadInfo != null;
        }

        public void setPreparedForLoadInfo(EtkDataAssembly assembly, String kLfdNr,
                                           EditModuleFormConnector connector) {
            preparedForLoadInfo = new LazyLoadEditModuleInfo(connector, assembly, kLfdNr);
        }

        public void setCancelled() {
            if (isLazyLoading()) {
                lazyLoadInfo.setCancelled();
            }
            if (isPreparedForLoading()) {
                preparedForLoadInfo.setCancelled();
            }
        }

    }

    private static final String IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE = "iPartsMenuItemEditRetailModule";
    public static final String IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_EDIT = "!!TU bearbeiten";
    public static final String IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_VIEW = "!!TU laden";
    private static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE = "iPartsMenuItemCopyRetailModule";
    private static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT = "!!TU kopieren";
    public static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_MULTI = "!!TUs kopieren";
    private static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_PSK = "iPartsMenuItemCopyRetailModulePSK";
    private static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK = "!!TU in PSK-Produkt kopieren";
    public static final String IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK_MULTI = "!!TUs in PSK-Produkt kopieren";
    private static final String IPARTS_MENU_ITEM_UPDATE_PSK_MODULE = "iPartsMenuItemUpdatePSKModule";
    public static final String IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT = "!!PSK-TU mit Serien-TU abgleichen";
    public static final String IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT_MULTI = "!!PSK-TUs mit Serien-TUs abgleichen";
    private static final String IPARTS_MENU_ITEM_RENAME_PSK_KG_TU = "iPartsMenuItemRenamePSKKgTu";
    public static final String IPARTS_MENU_ITEM_RENAME_PSK_KG_TEXT = "!!PSK-KG-Knoten umbenennen";
    public static final String IPARTS_MENU_ITEM_RENAME_PSK_TU_TEXT = "!!PSK-TU-Knoten umbenennen";
    private static final String IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TU = "iPartsMenuItemRenameSpecialCatKgTu";
    public static final String IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TEXT = "!!Spezialkatalog-KG-Knoten umbenennen";
    public static final String IPARTS_MENU_ITEM_RENAME_SPECIALCAT_TU_TEXT = "!!Spezialkatalog-TU-Knoten umbenennen";
    public static final String IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KGTU_TEXT = "!!Spezialkatalog-KG/TU-Knoten umbenennen";
    private static EnumSet<iPartsModuleTypes> validForModuleCopy = EnumSet.of(iPartsModuleTypes.PRODUCT_KGTU,
                                                                              iPartsModuleTypes.KG,
                                                                              iPartsModuleTypes.DialogRetail,
                                                                              iPartsModuleTypes.EDSRetail);
    private static EnumSet<iPartsModuleTypes> validForModuleCopyToPsk = EnumSet.of(iPartsModuleTypes.PRODUCT_KGTU,
                                                                                   iPartsModuleTypes.KG,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.EDSRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.PSK_TRUCK);
    private static final String IPARTS_MENU_ITEM_BIND_SA_TU = "iPartsMenuItemBindSaTu";
    private static final String IPARTS_MENU_ITEM_BIND_SA_TU_TEXT = "!!Freie SA zuordnen";
    private static final String IPARTS_MENU_ITEM_UNBIND_SA_TU = "iPartsMenuItemUnbindSaTu";
    private static final String IPARTS_MENU_ITEM_UNBIND_SA_TU_TEXT = "!!Freie SA-Zuordnung löschen";

    private static final String IPARTS_MENU_ITEM_CAR_PERSPECTIVE = "iPartsMenuItemCarPerspective";
    private static final String IPARTS_MENU_ITEM_CAR_PERSPECTIVE_TEXT = "!!Fahrzeugnavigation Modul anlegen";
    private static final String IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL = "iPartsMenuItemCarPerspectiveDel";
    private static final String IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL_TEXT = "!!Fahrzeugnavigation Modul löschen";

    private static final boolean MULTI_SELECT_SEARCH_RESULT = true;

    private iPartsEditPlugin editPlugin;
    private boolean use2ndPanel = true;
    private boolean showCloseButtonOnHeaderForm = false;
    private boolean doLazyLoading = true;

    /**
     * Fügt die Popup-Menüpunkte für "TU bearbeiten" bei TUs, sowie "TU(s) kopieren" bei KGs und TUs, hinzu.
     *
     * @param popupMenu
     * @param connector
     */
    public static void modifyRetailPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE,
                                                                                           IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_EDIT,
                                                                                           DefaultImages.module.getImage(),
                                                                                           null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                editOrViewModule(connector);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE,
                                                                               IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT,
                                                                               EditDefaultImages.edit_copy_tu.getImage(),
                                                                               null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                copyOrUpdatePSKModule(connector, true, false, false);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_PSK,
                                                                               IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK,
                                                                               EditDefaultImages.edit_copy_tu.getImage(),
                                                                               null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                copyOrUpdatePSKModule(connector, true, true, false);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_UPDATE_PSK_MODULE,
                                                                               IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT,
                                                                               EditDefaultImages.edit_update_tu.getImage(),
                                                                               null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                copyOrUpdatePSKModule(connector, true, true, true);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_BIND_SA_TU,
                                                                               IPARTS_MENU_ITEM_BIND_SA_TU_TEXT,
                                                                               DefaultImages.module.getImage(), null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                bindSaTu(connector, true);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_UNBIND_SA_TU,
                                                                               IPARTS_MENU_ITEM_UNBIND_SA_TU_TEXT,
                                                                               DefaultImages.module.getImage(), null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                unbindSaTu(connector, true);
            }
        });

        // FahrzeugNavigationModul
        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_CAR_PERSPECTIVE,
                                                                               IPARTS_MENU_ITEM_CAR_PERSPECTIVE_TEXT,
                                                                               DefaultImages.module.getImage(), null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                createCarPerspectiveModule(connector, true);
            }
        });

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL,
                                                                               IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL_TEXT,
                                                                               DefaultImages.module.getImage(), null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                deleteCarPerspectiveModule(connector, true);
            }
        });
    }

    /**
     * Passt die Sichtbarkeit der Popup-Menüpunkte an.
     *
     * @param popupMenu
     * @param connector
     */
    public static void updateRetailPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
        // destAssembly kann null sein (z.B. falls man auf einen Stücklisteneintrag geklickt hat)
        if (destAssembly != null) {
            destAssembly = destAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
        }
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE,
                                                                                           AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly,
                                                                                                                                                    iPartsModuleTypes.getEditableModuleTypes()));
        updateRetailPartListPopupMenuItemText(menuItem, connector);
        boolean changesetInactive = true;
        EtkRevisionsHelper revisionHelper = connector.getProject().getRevisionsHelper();
        if (revisionHelper != null) {
            changesetInactive = !revisionHelper.isRevisionChangeSetActive();
        }
        boolean isVisible = iPartsRight.COPY_TUS.checkRightInSession() && changesetInactive &&
                            AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, validForModuleCopy);
        boolean isCopyToPskVisible = iPartsRight.COPY_TUS.checkRightInSession() && changesetInactive &&
                                     AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, validForModuleCopyToPsk);

        // Dokumentationstyp für weitere Sichtbarkeiten bestimmen
        iPartsDocumentationType documentationType = getDocumentationType(connector.getProject(), destAssembly);
        menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE,
                                                                               isVisible);
        updateCopyRetailPartListPopupMenuItemText(menuItem, destAssembly, false);

        boolean pskAssembly = false;
        if (destAssembly instanceof iPartsDataAssembly) {
            pskAssembly = ((iPartsDataAssembly)destAssembly).isPSKAssembly();
        }
        menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_PSK,
                                                                               isCopyToPskVisible && iPartsRight.checkPSKInSession());
        updateCopyRetailPartListPopupMenuItemText(menuItem, destAssembly, true);

        menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_UPDATE_PSK_MODULE,
                                                                               !connector.getProject().isRevisionChangeSetActiveForEdit()
                                                                               && pskAssembly && iPartsRight.checkPSKInSession());
        updateUpdatePSKPartListPopupMenuItemText(menuItem, destAssembly);

        // Autorenauftrag und DocuType prüfen
        boolean preconditionsForBindUnbindSaTu = checkCommonPreconditionsForBindUnbindSaTu(connector.getProject(), documentationType);
        // SA-TU verorten
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_BIND_SA_TU,
                                                                    preconditionsForBindUnbindSaTu &&
                                                                    isBindSaTuMenuVisible(destAssembly));
        // Verortung von SA-TU wieder auflösen
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_UNBIND_SA_TU,
                                                                    preconditionsForBindUnbindSaTu &&
                                                                    isUnbindSaTuMenuVisible(destAssembly));

        boolean preconditionsForCarPerspective = isProductEnabledForCarPerspective(connector.getProject(), destAssembly);
        if (preconditionsForCarPerspective) {
            preconditionsForCarPerspective = checkCommonPreconditionsForCarPerspective(connector.getProject(), documentationType);
        }
        // CarPerspective anlegen
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CAR_PERSPECTIVE,
                                                                    isCarPerspectiveMenuVisible(connector.getProject(), destAssembly, preconditionsForCarPerspective));
        // CarPerspective löschen
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL,
                                                                    isCarPerspectiveDelMenuVisible(connector.getProject(), destAssembly, preconditionsForCarPerspective));
    }

    /**
     * Fügt die selben Popup-Menüpunkte wie in {@link #modifyRetailPartListPopupMenu} zum Baum hinzu.
     *
     * @param menu
     * @param formWithTree
     */
    public static void modifyRetailPartListTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE,
                                                                                       IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_EDIT, null);
        if (menuItem != null) {
            menuItem.setIcon(DefaultImages.module.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    editOrViewModule(formWithTree.getConnector());
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE,
                                                                           IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT, null);
        if (menuItem != null) {
            menuItem.setIcon(EditDefaultImages.edit_copy_tu.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    copyOrUpdatePSKModule(formWithTree.getConnector(), false, false, false);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_PSK,
                                                                           IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK, null);
        if (menuItem != null) {
            menuItem.setIcon(EditDefaultImages.edit_copy_tu.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    copyOrUpdatePSKModule(formWithTree.getConnector(), false, true, false);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_UPDATE_PSK_MODULE,
                                                                           IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT, null);
        if (menuItem != null) {
            menuItem.setIcon(EditDefaultImages.edit_update_tu.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    copyOrUpdatePSKModule(formWithTree.getConnector(), false, true, true);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_RENAME_PSK_KG_TU,
                                                                           IPARTS_MENU_ITEM_RENAME_PSK_KG_TEXT, null);
        if (menuItem != null) {
            menuItem.setIcon(EditToolbarButtonAlias.IMG_EDIT.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doEditPskKgTuNode(formWithTree.getConnector());
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TU,
                                                                           IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TEXT, null);
        if (menuItem != null) {
            menuItem.setIcon(EditToolbarButtonAlias.IMG_EDIT.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doEditSpecialCatKgTuNode(formWithTree.getConnector());
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_BIND_SA_TU,
                                                                           IPARTS_MENU_ITEM_BIND_SA_TU_TEXT, null);
        if (menuItem != null) {
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    bindSaTu(formWithTree.getConnector(), false);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_UNBIND_SA_TU,
                                                                           IPARTS_MENU_ITEM_UNBIND_SA_TU_TEXT, null);
        if (menuItem != null) {
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    unbindSaTu(formWithTree.getConnector(), false);
                }
            });
        }

        // FahrzeugNavigationModul
        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_CAR_PERSPECTIVE,
                                                                           IPARTS_MENU_ITEM_CAR_PERSPECTIVE_TEXT, null);
        if (menuItem != null) {
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    createCarPerspectiveModule(formWithTree.getConnector(), false);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL,
                                                                           IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL_TEXT, null);
        if (menuItem != null) {
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    deleteCarPerspectiveModule(formWithTree.getConnector(), false);
                }
            });
        }
    }

    /**
     * Passt die Sichtbarkeit vom Popup-Menüpunkt für "Modul bearbeiten" an (nur für Retail-Module sichtbar).
     *
     * @param popupMenu
     * @param connector
     */
    public static void updateRetailPartListTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        // TU laden/ bearbeiten
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE,
                                                                                       iPartsModuleTypes.getEditableModuleTypes());
        updateRetailPartListPopupMenuItemText(menuItem, connector);

        // TU kopieren
        boolean isVisible = false;
        boolean isCopyToPskVisible = false;
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly != null) {
            boolean changesetInactive = true;
            EtkRevisionsHelper revisionHelper = connector.getProject().getRevisionsHelper();
            if (revisionHelper != null) {
                changesetInactive = !revisionHelper.isRevisionChangeSetActive();
            }
            isVisible = iPartsRight.COPY_TUS.checkRightInSession() && changesetInactive &&
                        AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(assembly, validForModuleCopy);
            isCopyToPskVisible = iPartsRight.COPY_TUS.checkRightInSession() && changesetInactive &&
                                 AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(assembly, validForModuleCopyToPsk);
        }

        // Dokumentationstyp für weitere Sichtbarkeiten bestimmen
        iPartsDocumentationType documentationType = getDocumentationType(connector.getProject(), assembly);
        menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE,
                                                                            isVisible);
        updateCopyRetailPartListPopupMenuItemText(menuItem, connector.getCurrentAssembly(), false);

        boolean pskAssembly = false;
        boolean isSpecialCat = false;
        boolean isProductNode = true;
        if (assembly instanceof iPartsDataAssembly) {
            pskAssembly = ((iPartsDataAssembly)assembly).isPSKAssembly();
            isSpecialCat = ((iPartsDataAssembly)assembly).isSpecialProduct();
            if (pskAssembly || isSpecialCat) {
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                isProductNode = iPartsVirtualNode.isProductNode(virtualNodesPath);
            }
        }
        menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_PSK,
                                                                            isCopyToPskVisible && iPartsRight.checkPSKInSession());
        updateCopyRetailPartListPopupMenuItemText(menuItem, assembly, true);

        menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_UPDATE_PSK_MODULE,
                                                                            !connector.getProject().isRevisionChangeSetActiveForEdit()
                                                                            && pskAssembly && iPartsRight.checkPSKInSession());
        updateUpdatePSKPartListPopupMenuItemText(menuItem, assembly);

        // PSK-KG/TU umbenennen
        menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_RENAME_PSK_KG_TU,
                                                                            connector.getProject().isRevisionChangeSetActiveForEdit()
                                                                            && pskAssembly && iPartsRight.checkPSKInSession() && !isProductNode);
        updateUpdatePSKRenamePopupMenuItemText(menuItem, assembly);

        // Special-Catalogue-KG/TU umbenennen
        menuItem = AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TU,
                                                                            connector.getProject().isRevisionChangeSetActiveForEdit()
                                                                            && isSpecialCat && !isProductNode);
        updateUpdateSpecialCatRenamePopupMenuItemText(connector, menuItem, assembly);

        // Autorenauftrag und DocuType prüfen
        boolean preconditionsForBindUnbindSaTu = checkCommonPreconditionsForBindUnbindSaTu(connector.getProject(), documentationType);
        // SA-TU verorten
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_BIND_SA_TU,
                                                                 preconditionsForBindUnbindSaTu && isBindSaTuMenuVisible(
                                                                     assembly));
        // Verortung von SA-TU wieder auflösen
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_UNBIND_SA_TU,
                                                                 preconditionsForBindUnbindSaTu && isUnbindSaTuMenuVisible(
                                                                     assembly));


        boolean preconditionsForCarPerspective = isProductEnabledForCarPerspective(connector.getProject(), assembly);
        if (preconditionsForCarPerspective) {
            preconditionsForCarPerspective = checkCommonPreconditionsForCarPerspective(connector.getProject(), documentationType);
        }
        // CarPerspective anlegen
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CAR_PERSPECTIVE,
                                                                    isCarPerspectiveMenuVisible(connector.getProject(), assembly, preconditionsForCarPerspective));
        // CarPerspective löschen
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CAR_PERSPECTIVE_DEL,
                                                                    isCarPerspectiveDelMenuVisible(connector.getProject(), assembly, preconditionsForCarPerspective));
    }

    private static boolean isBindSaTuMenuVisible(EtkDataAssembly destAssembly) {
        return AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, EnumSet.of(iPartsModuleTypes.KG));
    }

    private static boolean isUnbindSaTuMenuVisible(EtkDataAssembly destAssembly) {
        return AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, EnumSet.of(iPartsModuleTypes.SA_TU));
    }

    private static boolean isCarPerspectiveMenuVisible(EtkProject project, EtkDataAssembly destAssembly, boolean preconditionsForCarPerspective) {
        if (preconditionsForCarPerspective) {
            if (destAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly assembly = (iPartsDataAssembly)(destAssembly);
                if (!assembly.isVirtual()) {
                    return AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, EnumSet.of(iPartsModuleTypes.PRODUCT_KGTU));
                }
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (StrUtils.isValid(productNumberFromAssemblyId)) {
                    return !EditModuleHelper.carPerspectiveModuleExists(project, productNumberFromAssemblyId);
                }
            }
        }
        return false;
    }

    private static boolean isCarPerspectiveDelMenuVisible(EtkProject project, EtkDataAssembly destAssembly,
                                                          boolean preconditionsForCarPerspective) {
        if (preconditionsForCarPerspective) {
            if (destAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly assembly = (iPartsDataAssembly)(destAssembly);
                if (!assembly.isVirtual()) {
                    return AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly, EnumSet.of(iPartsModuleTypes.CAR_PERSPECTIVE));
                }
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (StrUtils.isValid(productNumberFromAssemblyId)) {
                    return EditModuleHelper.carPerspectiveModuleExists(project, productNumberFromAssemblyId);
                }
            }
        }
        return false;
    }

    private static iPartsDocumentationType getDocumentationType(EtkProject project, EtkDataAssembly destAssembly) {
        if (destAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)(destAssembly);
            if (assembly.isVirtual()) {
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (StrUtils.isValid(productNumberFromAssemblyId)) {
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumberFromAssemblyId));
                    return product.getDocumentationType();
                }
            } else {
                return assembly.getDocumentationType();
            }
        }
        return iPartsDocumentationType.UNKNOWN;
    }

    private static iPartsProductId getProductIdForCarPerspective(EtkDataAssembly destAssembly) {
        if (destAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)(destAssembly);
            if (!assembly.isVirtual()) {
                return assembly.getProductIdFromModuleUsage();
            }
            if (assembly.isVirtual()) {
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (StrUtils.isValid(productNumberFromAssemblyId)) {
                    return new iPartsProductId(productNumberFromAssemblyId);
                }
            }
        }
        return null;
    }

    private static boolean isProductEnabledForCarPerspective(EtkProject project, EtkDataAssembly destAssembly) {
        iPartsProductId productId = getProductIdForCarPerspective(destAssembly);
        if ((productId != null) && productId.isValidId()) {
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            return product.isCarPerspective();
        }
        return false;
    }

    private static boolean checkCommonPreconditionsForBindUnbindSaTu(EtkProject project, iPartsDocumentationType documentationType) {
        EtkRevisionsHelper revisionHelper = project.getRevisionsHelper();
        if ((revisionHelper == null) || !revisionHelper.isRevisionChangeSetActiveForEdit()) {
            return false;
        }
        // Falls Bind und Unbind unterschiedliche Rechte benötigen, dann diese Abfrage nach isBind.../isUnbind...Visible
        if (!iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            return false;
        }
        if ((documentationType == null) ||
            (documentationType == iPartsDocumentationType.UNKNOWN) ||
            documentationType.isPKWDocumentationType()) {
            return false;
        }
        return true;
    }

    private static boolean checkCommonPreconditionsForCarPerspective(EtkProject project, iPartsDocumentationType documentationType) {
        EtkRevisionsHelper revisionHelper = project.getRevisionsHelper();
        if ((revisionHelper == null) || !revisionHelper.isRevisionChangeSetActiveForEdit()) {
            return false;
        }
        if (!iPartsRight.EDIT_PARTS_DATA.checkRightInSession() || !iPartsRight.CREATE_DELETE_CAR_PERSPECTIVE.checkRightInSession()) {
            return false;
        }
        if ((documentationType == null) ||
            (documentationType == iPartsDocumentationType.UNKNOWN) ||
            !documentationType.isPKWDocumentationType()) {
            return false;
        }
        return true;
    }

    /**
     * Editiert bzw. lädt das im übergebenen {@link AbstractJavaViewerFormIConnector} aktuelle Modul.
     *
     * @param connector
     */
    public static void editOrViewModule(AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly assembly = EditModuleHelper.getAssemblyFromConnector(connector);
        if ((assembly != null) && !iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
            List<AssemblyId> assemblyIds = new DwList<>();
            assemblyIds.add(assembly.getAsId());
            editOrViewModules(assemblyIds, connector.getMainWindow());
        }
    }

    private static Runnable createMultiRunExecutionRunnableForCopyOrUpdatePSKModule(final iPartsCopyTUJobHelper.CopyOrUpdatePSKContainer copyOrUpdatePSKContainer,
                                                                                    final boolean updatePSKModule, final boolean isMultiNode,
                                                                                    final boolean targetIsPSKProduct, final String title) {
        Runnable multiRunExecutionRunnable = () -> {
            Set<iPartsAssemblyId> targetAssemblyIds = new TreeSet<>();
            for (Map.Entry<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyEntry : copyOrUpdatePSKContainer.getCopyMap().entrySet()) {
                // Verarbeitung der KGs abbrechen bei Fehler oder Abbruch
                if (copyOrUpdatePSKContainer.hasErrors() || copyOrUpdatePSKContainer.isCancelled() ||
                    copyOrUpdatePSKContainer.isStoppedByUser()) {
                    break;
                }

                String kg = copyEntry.getKey();
                String kgForMessageLog = isMultiNode ? kg : null;
                Set<iPartsCopyTUJobHelper.TUCopyContainer> copyList = copyEntry.getValue();
                copyOrUpdatePSKContainer.incKgCounter();
                boolean isLastKG = copyOrUpdatePSKContainer.isLastKg();

                iPartsCopyTUJobHelper helper = null;
                try {
                    if (updatePSKModule) {
                        helper = iPartsCopyTUJobHelper.updatePSKTUsWithJob(copyOrUpdatePSKContainer, copyList, kgForMessageLog,
                                                                           isLastKG);
                    } else {
                        helper = iPartsCopyTUJobHelper.copyTUsWithJob(copyOrUpdatePSKContainer, copyList, targetIsPSKProduct,
                                                                      kgForMessageLog, isLastKG);
                    }
                } finally {
                    Session actualSession = copyOrUpdatePSKContainer.getActualSession();

                    // Auf das Beenden vom Kopieren/Abgleich warten in einem FrameworkThread unabhängig von der Session
                    Runnable waitRunnable = createWaitThreadRunnableForCopyOrUpdatePSKModule(copyOrUpdatePSKContainer, helper,
                                                                                             targetAssemblyIds, isLastKG, title);
                    FrameworkThread waitThread = new FrameworkThread("Waiting thread for completion of "
                                                                     + TranslationHandler.translateForLanguage(title, Language.EN.getCode())
                                                                     + " for KG " + kg + " started by session "
                                                                     + actualSession.getId(), Thread.NORM_PRIORITY, waitRunnable);

                    waitThread.__internal_start();
                    waitThread.waitFinished();
                }
            }
        };
        return multiRunExecutionRunnable;
    }

    private static Runnable createWaitThreadRunnableForCopyOrUpdatePSKModule(final iPartsCopyTUJobHelper.CopyOrUpdatePSKContainer copyOrUpdatePSKContainer,
                                                                             final iPartsCopyTUJobHelper helper,
                                                                             final Set<iPartsAssemblyId> targetAssemblyIds,
                                                                             final boolean isLastKG, final String title) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (helper != null) {
                        boolean finished = helper.waitUntilFinished();
                        if (!finished) {
                            copyOrUpdatePSKContainer.setIsCancelled();
                        }
                        if (helper.isFinishedWithErrors()) {
                            copyOrUpdatePSKContainer.setHasErrors();
                        }
                        if (helper.isStoppedForFollowing()) {
                            copyOrUpdatePSKContainer.setStoppedByUser(true);
                        }

//                        // Alle relevanten Daten zum Kopieren/Abgleich merken für die nächste KG
                        copyOrUpdatePSKContainer.copyHelperResultsForNextKg(helper);
                        targetAssemblyIds.addAll(helper.getTargetAssemblyIds());

                        // Nur nach der Abarbeitung der letzten KG oder bei einem Fehler oder Abbruch
                        // die Log-Datei schreiben und nach der letzten KG eine Meldung anzeigen bei
                        // Ausführung im Hintergrund
                        if (isLastKG || copyOrUpdatePSKContainer.hasErrors() || copyOrUpdatePSKContainer.isCancelled()) {
                            copyOrUpdatePSKContainer.fireEndMessage(isLastKG);

                            // Erst nach der letzten KG die Log-Datei abschließen, wobei Abbruch mit
                            // Löschen der Log-Datei nur dann möglich ist, wenn es sich um die erste
                            // KG handelt
                            helper.finishLogFile(copyOrUpdatePSKContainer.hasErrors(), copyOrUpdatePSKContainer.isCancelled()
                                                                                       && (copyOrUpdatePSKContainer.getKgCounter() == 1));

                            // Falls die Verarbeitung tatsächlich gestartet wurde (also ein Ziel-Produkt
                            // ausgewählt ist) und diese im Hintergrund ausgeführt wurde, eine Meldung
                            // anzeigen, dass die Ausführung im Hintergrund abgeschlossen wurde
                            if (copyOrUpdatePSKContainer.isTargetProductIdValid() && helper.isExecutedInBackground()) {
                                copyOrUpdatePSKContainer.fireBackgroundEndMessage(title, finished);
                            }

                            // Session für die Ausführung vom Kopieren/Abgleich beenden
                            Session sessionForExecutionFromHelper = copyOrUpdatePSKContainer.getSessionForExecution();
                            if (sessionForExecutionFromHelper != null) {
                                SessionManager.getInstance().destroySession(sessionForExecutionFromHelper);
                            }
                        }
                    }
                } finally {
                    // Nur nach der Abarbeitung der letzten KG oder bei einem Fehler oder Abbruch
                    // den Connector und das EtkProject aufräumen sowie das Edit in der GUI-Session
                    // auf jeden Fall wieder erlauben
                    if (isLastKG || copyOrUpdatePSKContainer.hasErrors() || copyOrUpdatePSKContainer.isCancelled()) {
                        // Normales Ende oder Abbruch mit bereits abgeschlossenem KG
                        if (isLastKG || (copyOrUpdatePSKContainer.getKgCounter() > 1)) {
                            // Einmal am Ende die relevanten Caches löschen (Idents und Ausreißer können durch das Kopieren von
                            // TUs bzw. den Abgleich sich nicht verändern und das einzige Produkt, das verändert wird, ist das
                            // Ziel-Produkt; die veränderten AssemblyIds wurden in targetAssemblyIds aufgesammelt
                            Set<iPartsProductId> productIds = new HashSet<>();
                            if (helper != null) {
                                iPartsProductId productId = helper.getTargetProductId();
                                // Falls es kein Zielprodukt gibt, dann wurde die Produktauswahl abgebrochen -> Caches leeren nicht nötig
                                if (productId != null) {
                                    productIds.add(productId);
                                    ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false,
                                                                                                  false,
                                                                                                  targetAssemblyIds,
                                                                                                  productIds);
                                }
                            }
                        }

                        copyOrUpdatePSKContainer.disposeConnectorAndProjectForCopyUpdate();
                        iPartsEditPlugin.stopEditing(copyOrUpdatePSKContainer.getSessionForGUI());
                    }
                }
            }
        };
        return runnable;
    }

    private static EtkProject createProjectForCopyOrUpdatePSKModule(Session session, EtkProject originalProject) {
        // Neues EtkProject erzeugen für das Kopieren/Abgleich und dieses in einem neuen Connector verwenden
        DWFile dwkFile = (DWFile)session.getAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE);
        EtkProject projectForCopyUpdate = EtkEndpointHelper.createProject(dwkFile, true);
        if (projectForCopyUpdate == null) {
            MessageDialog.showError("!!Fehler beim Erzeugen vom EtkProject!");
        } else {
            projectForCopyUpdate.getConfig().setCurrentViewerLanguage(originalProject.getViewerLanguage());
            projectForCopyUpdate.getConfig().setCurrentDatabaseLanguage(originalProject.getDBLanguage());
        }
        return projectForCopyUpdate;
    }

    private static EditModuleFormConnector createConnectorForCopyOrUpdatePSKModule(Session session, AbstractJavaViewerFormIConnector connector) {
        EditModuleFormConnector connectorForCopyUpdate = null;
        EtkProject projectForCopyUpdate = createProjectForCopyOrUpdatePSKModule(session, connector.getProject());
        if (projectForCopyUpdate != null) {
            connectorForCopyUpdate = new EditModuleFormConnector(connector) {
                @Override
                public EtkProject getProject() {
                    return projectForCopyUpdate;
                }
            };
        }
        return connectorForCopyUpdate;
    }

    private static iPartsProductId fillCopyMapForCopyOrUpdatePSKModule(EtkProject project, iPartsDataAssembly assembly,
                                                                       boolean isMultiNode, Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyMap) {
        iPartsProductId productId = null;
        copyMap.clear();
        if (isMultiNode) {
            List<iPartsVirtualNode> virtualNodesPath = assembly.getVirtualNodesPath();
            if (iPartsVirtualNode.isProductNode(virtualNodesPath)) { // Produkt
                productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForProduct(project, productId);
                fillCopyMapForCopyOrUpdatePSKModule(copyMap, moduleList);
            } else if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) { // KG-Knoten
                productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                KgTuId kgTuId = (KgTuId)virtualNodesPath.get(1).getId();
                iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForKgTu(project, productId, kgTuId);
                fillCopyMapForCopyOrUpdatePSKModule(copyMap, moduleList);
            }
        } else { // Retail-Stückliste
            productId = assembly.getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForProductAndModule(project, productId, assembly.getAsId());
                fillCopyMapForCopyOrUpdatePSKModule(copyMap, moduleList);
            }
        }
        return productId;
    }

    public static void copyOrUpdatePSKModule(AbstractJavaViewerFormIConnector connector,
                                             boolean isPartListPopupMenu, boolean targetIsPSKProduct,
                                             boolean updatePSKModule) {
        EtkProject project = connector.getProject();
        if (project.isEditModeActive()) {
            EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, isPartListPopupMenu);
            if (assembly == null) {
                return;
            }

            boolean isMultiNode = iPartsVirtualNode.isVirtualId(assembly.getAsId());
            boolean isMultiNodeForTitle = isMultiNodeForTitle(assembly.getAsId());
            String title;
            iPartsDataAuthorOrder dataAuthorOrder = null;
            if (updatePSKModule) {
                dataAuthorOrder =
                    EditUserControlForAuthorOrder.showPartialAuthorOrderForUpdatePSKModule(connector, connector.getActiveForm(),
                                                                                           isMultiNodeForTitle);
                if (dataAuthorOrder == null) {
                    return;
                }
                title = isMultiNodeForTitle ? IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT_MULTI : IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT;
            } else if (targetIsPSKProduct) {
                title = isMultiNodeForTitle ? IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK_MULTI : IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK;
            } else {
                title = isMultiNodeForTitle ? IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_MULTI : IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT;
            }
            if (assembly instanceof iPartsDataAssembly) {
                Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyMap = new TreeMap<>();
                iPartsProductId productId = fillCopyMapForCopyOrUpdatePSKModule(project, (iPartsDataAssembly)assembly, isMultiNode, copyMap);

                if ((productId != null) && !copyMap.isEmpty()) {
                    Session session = Session.get();
                    if (!iPartsEditPlugin.startEditing(session)) {
                        return;
                    }

                    EditModuleFormConnector connectorForCopyUpdate = createConnectorForCopyOrUpdatePSKModule(session, connector);
                    if (connectorForCopyUpdate == null) {
                        return;
                    }

                    // Beim Kopieren von Modulen ist die productId vom Quell-Produkt, beim Abgleich von PSK-Modulen ist
                    // es hingegen das Ziel-Produkt
                    iPartsCopyTUJobHelper.CopyOrUpdatePSKContainer copyOrUpdatePSKContainer =
                        new iPartsCopyTUJobHelper.CopyOrUpdatePSKContainer(session, connectorForCopyUpdate,
                                                                           updatePSKModule ? null : productId,
                                                                           updatePSKModule ? productId : null, copyMap,
                                                                           project.getViewerLanguage());
                    copyOrUpdatePSKContainer.setUpdatePSKDataAuthorOrder(dataAuthorOrder);
                    Runnable multiRunExecutionRunnable = createMultiRunExecutionRunnableForCopyOrUpdatePSKModule(copyOrUpdatePSKContainer,
                                                                                                                 updatePSKModule, isMultiNode,
                                                                                                                 targetIsPSKProduct, title);

                    // FrameworkThread (der später auch unabhängig werden kann von der Session) verwenden für das Kopieren/Abgleich
                    FrameworkThread multiRunExecutionThread = new FrameworkThread("Execution wrapper thread for "
                                                                                  + TranslationHandler.translateForLanguage(title, Language.EN.getCode())
                                                                                  + " started by session "
                                                                                  + session.getId(), Thread.NORM_PRIORITY,
                                                                                  multiRunExecutionRunnable);

                    SessionManager.getInstance().registerThreadForSession(session, multiRunExecutionThread.getRealThread());
                    multiRunExecutionThread.__internal_start();
                } else if (productId == null) {
                    MessageDialog.showError("!!Es konnte kein PSK-Produkt ermittelt werden.", title);
                } else if (copyMap.isEmpty()) {
                    MessageDialog.show("!!Es wurden keine für den Abgleich relevanten TUs gefunden.", title);
                }
            }
        }
    }

    private static boolean isMultiNodeForTitle(AssemblyId assemblyId) {
        boolean isMultiNode = iPartsVirtualNode.isVirtualId(assemblyId);
        if (isMultiNode) {
            KgTuId kgTuId = iPartsVirtualNode.getKgTuFromAssemblyId(assemblyId);
            if (kgTuId != null) {
                isMultiNode = !kgTuId.isTuNode();
            }
        }
        return isMultiNode;
    }

    private static void fillCopyMapForCopyOrUpdatePSKModule(Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyMap,
                                                            iPartsDataModuleEinPASList moduleEinPASList) {
        for (iPartsDataModuleEinPAS moduleEinPAS : moduleEinPASList) {
            iPartsCopyTUJobHelper.TUCopyContainer copyContainer = new iPartsCopyTUJobHelper.TUCopyContainer(moduleEinPAS);
            Set<iPartsCopyTUJobHelper.TUCopyContainer> copyListForKG = copyMap.computeIfAbsent(copyContainer.getKgTuId().getKg(),
                                                                                               kg -> new TreeSet(Comparator.comparing(iPartsCopyTUJobHelper.TUCopyContainer::getSourceAssemblyId)));
            copyListForKG.add(copyContainer);
        }
    }

    /**
     * Editiert bzw. lädt die übergebenen Module im übergebenen Hauptfenster.
     *
     * @param assemblyIds
     * @param mainWindow
     */
    public static void editOrViewModules(List<AssemblyId> assemblyIds, JavaViewerMainWindow mainWindow) {
        List<PartListEntryId> partListEntryIds = null;
        if ((assemblyIds != null) && !assemblyIds.isEmpty()) {
            partListEntryIds = assemblyIds.stream()
                .map(assemblyId -> new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), null))
                .collect(Collectors.toList());
        }
        editOrViewModulesByPartListEntries(partListEntryIds, mainWindow);
    }

    /**
     * Editiert bzw. lädt die übergebenen Module im übergebenen Hauptfenster und markiert die TeilePos.
     * Sind für ein Modul mehrere Teilepositionen vorhanden, so wird nur die erste markiert.
     *
     * @param partListEntryIds
     * @param mainWindow
     */
    public static void editOrViewModulesByPartListEntries(List<PartListEntryId> partListEntryIds, JavaViewerMainWindow mainWindow) {
        if ((partListEntryIds != null) && !partListEntryIds.isEmpty()) {
            List<AbstractJavaViewerMainFormContainer> editModuleForms = mainWindow.getFormsFromClass(EditModuleForm.class);
            if (!editModuleForms.isEmpty()) {
                EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
                Set<AssemblyId> usedAssemblies = new HashSet<>();
                for (PartListEntryId partListEntryId : partListEntryIds) {
                    AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
                    if (!iPartsVirtualNode.isVirtualId(assemblyId)) {
                        // es wird nur der erste PartListEntry genommen
                        if (!usedAssemblies.contains(assemblyId)) {
                            editModuleForm.loadModule(assemblyId.getKVari(), partListEntryId.getKLfdnr());
                            usedAssemblies.add(assemblyId);
                        }
                    }
                }
                mainWindow.displayForm(editModuleForm);
            } else {
                MessageDialog.showError("!!Funktion \"Module bearbeiten\" nicht gefunden!");
            }
        } else {
            MessageDialog.show("!!Es ist kein Retail-Modul ausgewählt.");
        }
    }

    public static void doEditPskKgTuNode(AbstractJavaViewerFormIConnector connector) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, false);
        if (assembly instanceof iPartsDataAssembly) {
            iPartsProductId productId = null;
            KgTuId kgTuId = null;
            if (iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) {
                    productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                    kgTuId = (KgTuId)virtualNodesPath.get(1).getId();
                }
            } else { // Retail-Stückliste
                productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForProductAndModule(project, productId, assembly.getAsId());
                    if (!moduleList.isEmpty()) {
                        iPartsDataModuleEinPAS moduleEinPAS = moduleList.get(0);
                        kgTuId = new KgTuId(moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                            moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU));
                    }
                }
            }
            if ((productId != null) && (kgTuId != null)) {
                KgTuListItem kgTuListItem = EditKGTUDialog.modifyKgTuDescription(connector, null,
                                                                                 productId, kgTuId);
                if (kgTuListItem != null) {
                    // DAIMLER-11998: Speichern im Autorenauftrag
                    kgTuListItem.saveToDB(project, productId.getProductNumber());
                    KgTuForProduct.removeKgTuForProductFromCache(project, productId);

                    if (!iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                        // bei TU: auch die Modul-Bezeichnung ändern
                        EtkDataPart part = assembly.getPart();
                        EtkMultiSprache multi = kgTuListItem.getKgTuNode().getTitle().cloneMe();
                        part.setTextNrForMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multi.getTextId(),
                                                       DBActionOrigin.FROM_EDIT);
                        part.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multi, DBActionOrigin.FROM_EDIT);
                        assembly.getAttributes().markAsModified();
                        project.getRevisionsHelper().addDataObjectToActiveChangeSetForEdit(assembly);
                    }
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<AssemblyId>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                          assembly.getAsId(),
                                                                                          false), true);
                    project.fireProjectEvent(new DataChangedEvent(null), true);
                }
            }
        } else {
            MessageDialog.showError("!!Fehler beim Bestimmen des Produkt-KG Knotens.");
        }
    }

    public static void doEditSpecialCatKgTuNode(AbstractJavaViewerFormIConnector connector) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, false);
        if (assembly instanceof iPartsDataAssembly) {
            iPartsProductId productId = null;
            KgTuId kgTuId = null;
            if (iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) {
                    productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                    kgTuId = (KgTuId)virtualNodesPath.get(1).getId();
                }
            } else { // Retail-Stückliste
                productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForProductAndModule(project, productId, assembly.getAsId());
                    if (!moduleList.isEmpty()) {
                        iPartsDataModuleEinPAS moduleEinPAS = moduleList.get(0);
                        kgTuId = new KgTuId(moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                            moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU));
                    }
                }
            }
            if ((productId != null) && (kgTuId != null)) {
                VarParam<Boolean> isTuNodeHidden = new VarParam<>();
                VarParam<Boolean> isSingleModule = new VarParam<>();
                boolean productNodeFound = calcHiddenState(connector, isTuNodeHidden, isSingleModule);

                KgTuId kgTuIdForEdit = new KgTuId(kgTuId.getKg(), kgTuId.getTu());
                boolean renameKgAndTu = false;
                if (productNodeFound) {
                    if (!iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                        if (isTuNodeHidden.getValue() && kgTuId.isTuNode()) {
                            kgTuIdForEdit = new KgTuId(kgTuId.getKg(), "");
                            renameKgAndTu = true;
                        }
                    }
                }
                KgTuListItem kgTuListItem = EditKGTUDialog.modifyKgTuDescription(connector, null,
                                                                                 productId, kgTuIdForEdit);
                if (kgTuListItem != null) {
                    if (renameKgAndTu) {
                        // sowohl KG als auch TU-Knoten Benennung setzen
                        KgTuNode kgTuNode = KgTuForProduct.getInstance(connector.getProject(), productId).getNode(kgTuIdForEdit);
                        KgTuListItem kgListItem = new KgTuListItem(kgTuNode, KgTuListItem.Source.PRODUCT, true);
                        kgListItem.getKgTuNode().setTitle(kgTuListItem.getKgTuNode().getTitle());
                        kgListItem.setPskNature(KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE);

                        kgTuNode = KgTuForProduct.getInstance(connector.getProject(), productId).getNode(kgTuId);
                        KgTuListItem tuListItem = new KgTuListItem(kgTuNode, KgTuListItem.Source.PRODUCT, kgListItem, false);
                        tuListItem.getKgTuNode().setTitle(kgTuListItem.getKgTuNode().getTitle());
                        tuListItem.setPskNature(KgTuListItem.PSK_NATURE.PSK_CHANGED_TITLE);
                        kgListItem.addChild(tuListItem);

                        kgTuListItem = tuListItem;
                    }

                    // DAIMLER-11998: Speichern im Autorenauftrag
                    kgTuListItem.saveToDB(project, productId.getProductNumber());
                    KgTuForProduct.removeKgTuForProductFromCache(project, productId);

                    boolean reloadEditModule = false;
                    if (!iPartsVirtualNode.isVirtualId(assembly.getAsId()) && isSingleModule.getValue()) {
                        // bei TU: auch die Modul-Bezeichnung ändern
                        EtkDataPart part = assembly.getPart();
                        EtkMultiSprache multi = kgTuListItem.getKgTuNode().getTitle().cloneMe();
                        part.setTextNrForMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multi.getTextId(),
                                                       DBActionOrigin.FROM_EDIT);
                        part.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multi, DBActionOrigin.FROM_EDIT);
                        assembly.getAttributes().markAsModified();
                        project.getRevisionsHelper().addDataObjectToActiveChangeSetForEdit(assembly);
                        reloadEditModule = true;
                    }
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<AssemblyId>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                          assembly.getAsId(),
                                                                                          false), true);
                    project.fireProjectEvent(new DataChangedEvent(null), true);
                    if (reloadEditModule) {
                        Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();
                        modifiedAssemblyIds.add(assembly.getAsId());
                        iPartsEditPlugin.reloadModulesInEdit(modifiedAssemblyIds, connector);
                    }
                }
            }
        } else {
            MessageDialog.showError("!!Fehler beim Bestimmen des Produkt-KG Knotens.");
        }
    }

    /**
     * Überprüft den NavPath, welche Elemente versteckt sind
     * return false => Produkt-Knoten nicht gefunden
     * isTuNodeHidden = true: der TU-Knoten wird nicht angezeigt
     * isSingleModule = true: nur 1 Modul unterhalb von KG- oder KG/TU-Node
     * false: es sind mehrere Module unterhalb von KG- oder KG/TU-Node
     * <p>
     * Für Rename folgt daraus:
     *
     * @param connector
     * @param isTuNodeHidden
     * @param isSingleModule
     * @return
     */
    private static boolean calcHiddenState(AbstractJavaViewerFormIConnector connector, VarParam<Boolean> isTuNodeHidden,
                                           VarParam<Boolean> isSingleModule) {
        NavigationPath navPath = ((MechanicFormConnector)connector).getCurrentNavigationPath();
        boolean productNodeFound = false;
        isTuNodeHidden.setValue(false);
        isSingleModule.setValue(false);
        for (PartListEntryId navId : navPath) {
            if (!iPartsVirtualNode.isVirtualId(navId.getOwnerAssemblyId())) {
                continue;
            }

            List<iPartsVirtualNode> virtualNodesPath = iPartsVirtualNode.parseVirtualIds(navId.getOwnerAssemblyId());
            if (!productNodeFound) {
                productNodeFound = iPartsVirtualNode.isProductNode(virtualNodesPath);
            } else {
                if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) {
                    KgTuId kgTuId = (KgTuId)virtualNodesPath.get(1).getId();
                    EtkDataAssembly parentAssembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(), navId.getOwnerAssemblyId());
                    if (kgTuId.isKgNode()) {
                        if (parentAssembly.getEbene().isHideSingleSubAssemblyNode()) {
                            if (parentAssembly.getHiddenSingleSubAssembly(null) != null) {
                                isTuNodeHidden.setValue(true);
                            }
                        }
                    } else {
                        if (parentAssembly.getEbene().isHideSingleSubAssemblyNode()) {
                            if (parentAssembly.getHiddenSingleSubAssembly(null) != null) {
                                isSingleModule.setValue(true);
                            }
                        }
                    }
                }
            }
        }
        return productNodeFound;
    }

    public static void bindSaTu(AbstractJavaViewerFormIConnector connector, boolean isPartListPopupMenu) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, isPartListPopupMenu);
        if (assembly instanceof iPartsDataAssembly) {
            iPartsProductId productId = null;
            KgTuId kgTuId = null;
            if (iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                if (iPartsVirtualNode.isProductKgTuNode(virtualNodesPath)) {
                    productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                    kgTuId = (KgTuId)virtualNodesPath.get(1).getId();
                }
            }
            if ((productId != null) && (kgTuId != null)) {
                iPartsDataSa dataSa = SelectSAForm.showSASelectForBindSaTu(connector, productId, kgTuId);
                if (dataSa != null) {
                    iPartsProductSAsId productSAsId = new iPartsProductSAsId(productId.getProductNumber(),
                                                                             dataSa.getAsId().getSaNumber(), kgTuId.getKg());
                    iPartsDataProductSAs productSA = new iPartsDataProductSAs(project, productSAsId);
                    if (!productSA.existsInDB()) {
                        productSA.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }

                    project.getDbLayer().startTransaction();
                    try {
                        project.getRevisionsHelper().addDataObjectToActiveChangeSetForEdit(productSA);
                        markSaTuInChangeSetAsChanged(dataSa.getAsId(), project);
                        project.getDbLayer().commit();
                    } catch (Exception e) {
                        project.getDbLayer().rollback();
                        Logger.getLogger().throwRuntimeException(e);
                    }

                    DwList<iPartsProductId> productIds = new DwList<>();
                    productIds.add(productId);

                    // Events im EtkProject und Plug-ins reichen, da die Änderung ja nur im ChangeSet existiert
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                productIds, false), true);
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SA,
                                                                                iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                dataSa.getAsId(), false), true);
                    project.fireProjectEvent(new DataChangedEvent(null), true);

                    String saNumber = iPartsNumberHelper.formatPartNo(project, dataSa.getAsId().getSaNumber());
                    MessageDialog.show(TranslationHandler.translate("!!Die freie SA \"%1\" wurde erfolgreich dem Produkt \"%2\" " +
                                                                    "in KG \"%3\" zugeordnet.", saNumber, productId.getProductNumber(),
                                                                    kgTuId.getKg()));
                }
            }
        } else {
            MessageDialog.showError("!!Fehler beim Bestimmen des Produkt-KG Knotens.");
        }
    }

    private static void markSaTuInChangeSetAsChanged(iPartsSaId dataSaId, EtkProject project) {
        // SA-TU als geändert markieren im ChangeSet
        iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForSA(project, new iPartsSAId(dataSaId.getSaNumber()));
        for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
            AssemblyId saAssemblyId = new AssemblyId(dataSAModule.getFieldValue(iPartsConst.FIELD_DSM_MODULE_NO), "");
            EtkDataAssembly saAssembly = EtkDataObjectFactory.createDataAssembly(project, saAssemblyId);
            if (saAssembly.existsInDB()) {
                saAssembly.markAssemblyInChangeSetAsChanged();
            }
        }
    }

    public static void unbindSaTu(AbstractJavaViewerFormIConnector connector, boolean isPartListPopupMenu) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, isPartListPopupMenu);
        if (assembly instanceof iPartsDataAssembly) {
            List<iPartsVirtualNode> virtualNodesPath = null;
            if (iPartsVirtualNode.isVirtualId(assembly.getAsId())) {
                virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
            } else {
                // Beim Klick im Navigationsbau bekommen wir hier schon die letzte Subassembly. Da aber eine freie SA
                // in mehreren Produkten eingehängt sein kann, darf man hier nicht einfach den/ die Parents abfragen, sondern
                // muss den konkreten Produkt und KG Knoten über den NavigationPath bestimmen
                if (connector instanceof MechanicFormConnector) {
                    MechanicFormConnector mechanicFormConnector = (MechanicFormConnector)connector;
                    NavigationPath currentNavigationPath = mechanicFormConnector.getCurrentNavigationPath();
                    if ((currentNavigationPath != null) && (currentNavigationPath.size() > 2)) {
                        PartListEntryId partListEntryId = currentNavigationPath.get(currentNavigationPath.size() - 2);
                        virtualNodesPath = iPartsVirtualNode.parseVirtualIds(partListEntryId.getOwnerAssemblyId());
                    }
                }
            }
            if (iPartsVirtualNode.isKgSaNode(virtualNodesPath)) {
                iPartsProductId productId = (iPartsProductId)virtualNodesPath.get(0).getId();
                KgSaId kgSaId = (KgSaId)virtualNodesPath.get(1).getId();

                String saNumber = iPartsNumberHelper.formatPartNo(project, kgSaId.getSa());
                ModalResult modalResult = MessageDialog.showYesNo(TranslationHandler.translate(
                    "!!Soll die Zuordnung der freien SA \"%1\" zu Produkt \"%2\" in KG \"%3\" wirklich gelöscht werden?",
                    saNumber, productId.getProductNumber(), kgSaId.getKg()), IPARTS_MENU_ITEM_UNBIND_SA_TU_TEXT);
                if (modalResult == ModalResult.YES) {
                    iPartsProductSAsId productSAsId = new iPartsProductSAsId(productId.getProductNumber(),
                                                                             kgSaId.getSa(), kgSaId.getKg());
                    iPartsDataProductSAs productSA = new iPartsDataProductSAs(project, productSAsId);
                    if (productSA.existsInDB()) {
                        iPartsSaId saId = new iPartsSaId(kgSaId.getSa());
                        project.getDbLayer().startTransaction();
                        try {
                            project.getRevisionsHelper().addDeletedDataObjectToActiveChangeSetForEdit(productSA);
                            markSaTuInChangeSetAsChanged(saId, project);
                            project.getDbLayer().commit();
                        } catch (Exception e) {
                            project.getDbLayer().rollback();
                            Logger.getLogger().throwRuntimeException(e);
                        }

                        DwList<iPartsProductId> productIds = new DwList<>();
                        productIds.add(productId);

                        // Events im EtkProject und Plug-ins reichen, da die Änderung ja nur im ChangeSet existiert
                        project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                    iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                    productIds, false), true);
                        project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SA,
                                                                                    iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                    saId, false), true);
                        project.fireProjectEvent(new DataChangedEvent(null), true);


                        MessageDialog.show(TranslationHandler.translate("!!Die Zuordnung der SA \"%1\" zu Produkt \"%2\" in KG \"%3\" wurde erfolgreich gelöscht.",
                                                                        saNumber, productId.getProductNumber(), kgSaId.getKg()));
                    }
                }
            } else {
                MessageDialog.showError("!!Fehler beim Bestimmen des Produkt-SA Knotens.");
            }
        }

    }

    public static void createCarPerspectiveModule(AbstractJavaViewerFormIConnector connector,
                                                  boolean isPartListPopupMenu) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, isPartListPopupMenu);
        if (assembly instanceof iPartsDataAssembly) {
            if (assembly.isVirtual()) {
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (StrUtils.isValid(productNumberFromAssemblyId)) {
                    EtkDataAssembly carAssembly = generateNewCarPerspectiveModule(project, new iPartsProductId(productNumberFromAssemblyId));
                    if (carAssembly != null) {
                        // neu erzeugtes Modul laden
                        Set<String> selectedModuleList = new HashSet<>();
                        selectedModuleList.add(carAssembly.getAsId().getKVari());
                        iPartsLoadEditModuleHelper.doLoadModules(connector, selectedModuleList, false, null);
                    }
                }
            }
        }
    }

    public static void deleteCarPerspectiveModule(AbstractJavaViewerFormIConnector connector,
                                                  boolean isPartListPopupMenu) {
        EtkProject project = connector.getProject();
        if (!checkEditModeActive(project)) {
            return;
        }
        EtkDataAssembly assembly = getAssemblyFromConnectorForTreeOrPartlist(connector, isPartListPopupMenu);
        if (isProductEnabledForCarPerspective(project, assembly)) {
            if (assembly instanceof iPartsDataAssembly) {
                // DAIMLER-14932: Fahrzeugnavigation löschen
                iPartsProductId productId = getProductIdForCarPerspective(assembly);
                if ((productId == null) || !productId.isValidId()) {
                    return;
                }

                if (!EditModuleHelper.carPerspectiveModuleExists(project, productId.getProductNumber())) {
                    return;
                }
                iPartsDataAssembly destAssembly = (iPartsDataAssembly)EditModuleHelper.createCarPerspectiveDataAssembly(project, productId);
                if (!prepareDeletingModule(project, destAssembly)) {
                    return;
                }
                if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
                    return;
                }
                try {
                    deleteModuleInActiveChangeSet(project, destAssembly, false);
                } finally {
                    iPartsEditPlugin.stopEditing();
                }
                closeModuleInEdit(connector, destAssembly);
            }
        }
    }

    private static boolean closeModuleInEdit(AbstractJavaViewerFormIConnector connector, iPartsDataAssembly destAssembly) {
        List<AbstractJavaViewerMainFormContainer> editModuleForms = connector.getMainWindow().getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            return editModuleForm.closeModuleIfLoaded(destAssembly.getAsId().getKVari());
        }
        return false;
    }

    private static boolean checkEditModeActive(EtkProject project) {
        if (!project.isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return false;
        }
        return true;
    }

    private static void updateRetailPartListPopupMenuItemText(GuiMenuItem menuItem, AbstractJavaViewerFormIConnector connector) {
        if ((connector != null) && (connector.getProject() != null) && (menuItem != null)) {
            if (connector.getProject().isRevisionChangeSetActiveForEdit() && iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
                menuItem.setText(IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_EDIT);
            } else {
                menuItem.setText(IPARTS_MENU_ITEM_EDIT_RETAIL_MODULE_TEXT_VIEW);
            }
        }
    }

    private static void updateCopyRetailPartListPopupMenuItemText(GuiMenuItem menuItem, EtkDataAssembly destAssembly, boolean targetIsPSKProduct) {
        if ((destAssembly != null) && (menuItem != null)) {
            if (destAssembly.isVirtual()) {
                menuItem.setText(targetIsPSKProduct ? IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK_MULTI : IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_MULTI);
            } else {
                menuItem.setText(targetIsPSKProduct ? IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK : IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT);
            }
        }
    }

    private static void updateUpdatePSKPartListPopupMenuItemText(GuiMenuItem menuItem, EtkDataAssembly destAssembly) {
        if ((destAssembly != null) && (menuItem != null)) {
            if (destAssembly.isVirtual()) {
                menuItem.setText(IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT_MULTI);
            } else {
                menuItem.setText(IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT);
            }
        }
    }

    private static void updateUpdatePSKRenamePopupMenuItemText(GuiMenuItem menuItem, EtkDataAssembly destAssembly) {
        if ((destAssembly != null) && (menuItem != null)) {
            if (destAssembly.isVirtual()) {
                menuItem.setText(IPARTS_MENU_ITEM_RENAME_PSK_KG_TEXT);
            } else {
                menuItem.setText(IPARTS_MENU_ITEM_RENAME_PSK_TU_TEXT);
            }
        }
    }

    private static void updateUpdateSpecialCatRenamePopupMenuItemText(AssemblyTreeFormIConnector connector, GuiMenuItem menuItem, EtkDataAssembly destAssembly) {
        if ((destAssembly != null) && (menuItem != null) && menuItem.isVisible()) {
            if (destAssembly.isVirtual()) {
                menuItem.setText(IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KG_TEXT);
            } else {
                VarParam<Boolean> isTuNodeHidden = new VarParam<>();
                VarParam<Boolean> isSingleModule = new VarParam<>();
                boolean productNodeFound = calcHiddenState(connector, isTuNodeHidden, isSingleModule);
                if (productNodeFound) {
                    if (isTuNodeHidden.getValue()) {
                        menuItem.setText(IPARTS_MENU_ITEM_RENAME_SPECIALCAT_KGTU_TEXT);
                        if (isSingleModule.getValue()) {
                            // auch noch die Modul-Beschreibung ändern
                        }
                    } else {
                        menuItem.setText(IPARTS_MENU_ITEM_RENAME_SPECIALCAT_TU_TEXT);
                        if (isSingleModule.getValue()) {
                            // auch noch die Modul-Beschreibung ändern
                        }
                    }
                } else {
                    menuItem.setText(IPARTS_MENU_ITEM_RENAME_SPECIALCAT_TU_TEXT);
                }
            }
        }
    }

    private static EtkDataAssembly getAssemblyFromConnectorForTreeOrPartlist(AbstractJavaViewerFormIConnector connector, boolean isPartList) {
        if (connector instanceof MechanicFormConnector) {
            // DestinationAssembly im Baum kann direkt aus dem Connector ausgelesen werden
            EtkDataAssembly assembly = ((MechanicFormConnector)connector).getCurrentAssembly();

            // DestinationAssembly in der Stückliste über den Stücklisteneintrag ermitteln
            if (isPartList) {
                assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector((AssemblyListFormIConnector)connector);
            }
            return assembly;
        }
        return null;
    }

    public static void deleteModuleInActiveChangeSet(EtkProject project, EtkDataAssembly assembly, boolean useTechnicalChangeSet) {
        EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
        if (revisionsHelper == null) { // Kann bei iParts eigentlich gar nicht passieren
            MessageDialog.showError("!!RevisionHelper nicht vorhanden!", "!!Löschen");
            return;
        }

        final iPartsProductId productId;
        if (assembly instanceof iPartsDataAssembly) {
            productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
        } else {
            productId = null;
        }

        // Dialog zeigt den Fortschritt des Löschens und bleibt so lange im Vordergrund bis das Löschen abgeschlossen ist
        final EtkMessageLogForm progressForm = new EtkMessageLogForm(TranslationHandler.translate("!!Modul %1 löschen",
                                                                                                  assembly.getAsId().getKVari()),
                                                                     "!!Fortschritt", null, false);
        progressForm.disableButtons(true);
        progressForm.setMessagesTitle("");
        progressForm.showMarquee();
        progressForm.getGui().setSize(600, 250);

        progressForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                Set<iPartsRetailUsageId> deletedRetailUsageIds = new HashSet<>();
                iPartsRevisionChangeSet changeSet = null;
//                EtkProject project = getProject();
                if (useTechnicalChangeSet) {
                    // Technisches ChangeSet erzeugen und aktivieren
                    changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, iPartsChangeSetSource.DELETE_EMPTY_TU);
                    List<AbstractRevisionChangeSet> changeSets = new DwList<>(1);
                    changeSets.add(changeSet);
                    revisionsHelper.setActiveRevisionChangeSets(changeSets, changeSet, false, project);
                }
                try {
                    DBDataObjectList<EtkDataPartListEntry> partListEntries = assembly.getPartListUnfiltered(null);
                    for (EtkDataPartListEntry partListEntry : partListEntries) {
                        // Entfernte Retail-Verwendungen für den Event aufsammeln
                        iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE));
                        if (sourceType != iPartsEntrySourceType.NONE) {
                            String sourceGUID = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                            if (!sourceGUID.isEmpty()) {
                                deletedRetailUsageIds.add(new iPartsRetailUsageId(sourceType.getDbValue(), sourceGUID));
                            }
                        }
                    }

                    int partListEntriesCount = partListEntries.size();
                    if (partListEntriesCount > 0) {
                        progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche %1 Stücklisteneinträge...",
                                                                                              String.valueOf(partListEntriesCount)));
                    }

                    // Alle referenzierten Daten zu diesem Modul inkl. Stücklisteneinträge im Changeset als gelöscht markieren
                    EtkDataObjectList deletedReferencedData;
                    project.startPseudoTransactionForActiveChangeSet(true);
//                    startPseudoTransactionForActiveChangeSet(true);
                    try {
                        deletedReferencedData = assembly.deleteReferencedData(null);

                        assembly.getImages(); // Zeichnungen explizit anfordern, damit diese als Kind-Elemente geladen sind
                    } finally {
                        project.stopPseudoTransactionForActiveChangeSet();
                    }

                    int imageCount = assembly.getImageCount();
                    if (imageCount > 0) {
                        progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lösche %1 Zeichnungsreferenzen...",
                                                                                              String.valueOf(imageCount)));
                    }

                    // Änderungen sollen sofort ins Changeset gespeichert werden
                    EtkDbObjectsLayer dbLayer = project.getDbLayer();
                    dbLayer.startTransaction();
                    dbLayer.startBatchStatement();
                    try {
                        if (deletedReferencedData != null) {
                            revisionsHelper.addDataObjectListToActiveChangeSetForEdit(deletedReferencedData);
                        }

                        // Das Modul selbst im Changeset als gelöscht markieren
                        revisionsHelper.addDeletedDataObjectToActiveChangeSetForEdit(assembly);

                        dbLayer.endBatchStatement();
                        dbLayer.commit();
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        MessageDialog.showError("!!Fehler beim Speichern", "!!Löschen");
                        return;
                    }

                    progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Modul %1 gelöscht", assembly.getAsId().getKVari()));
                } finally {
                    if (useTechnicalChangeSet) {
                        // Technisches ChangeSet deaktivieren
                        revisionsHelper.clearActiveRevisionChangeSets(project, false);
                    }
                }

                //Editor schließen
//                isUndoModuleRunning = true;
//                if (closeTabFunction != null) {
//                    closeTabFunction.run(EditHeaderForm.this);
//                }

                if (useTechnicalChangeSet) {
                    // Technisches ChangeSet speichern
                    if (!changeSet.commit(true, true, null)) {
                        MessageDialog.showError("!!Fehler beim Speichern!", EditToolbarButtonAlias.EDIT_DELETE_EMPTY_MODULE.getTooltip());
                    }
                }

                AssemblyId assemblyId = assembly.getAsId();
                //jetzt erst die Caches löschen, damit der gerade geschlossene Tab nicht unnötig aktualisiert wird
                EtkDataAssembly.removeDataAssemblyFromCache(project, assemblyId);
                iPartsDataAssembly.removeAssemblyMetaDataFromCache(project, assemblyId);
                boolean fireAssemblyEvents = false;
                if (productId != null) {
                    // Bei aktivem ChangeSet und vorhandener productId muss kein CacheHelper.invalidateCaches() aufgerufen
                    // werden, sondern das Entfernen des betroffenen Produkts und Assembly aus dem Cache und ein paar nachfolgende
                    // Events reichen
                    iPartsProductStructures.removeProductFromCache(project, productId);
                    KgTuForProduct.removeKgTuForProductFromCache(project, productId);
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                productId, false), true);
                    fireAssemblyEvents = true;
                } else if ((assembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)assembly).isSAAssembly()) {
                    // eine freie SA hat noch keine Verknüpfungen und muss nur aus den Assembly Caches entfernt werden
                    fireAssemblyEvents = true;
                } else {
                    ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false, false);
                }

                if (fireAssemblyEvents) {
                    project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                assemblyId, false), true);
                    if (!deletedRetailUsageIds.isEmpty()) {
                        project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.RETAIL_USAGE,
                                                                                    iPartsDataChangedEventByEdit.Action.DELETED,
                                                                                    deletedRetailUsageIds, false));
                    }
                    project.fireProjectEvent(new DataChangedEvent(null), true);
                }

                // Primärschlüsselreservierungen löschen
                iPartsDataReservedPKList.deleteReservedPrimaryKey(project, assemblyId);
            }
        });
    }

    public static boolean checkOpenPictureOrders(EtkProject project, EtkDataAssembly currentAssembly) {
        // Modul darf keine offenen Bildaufträge enthalten
        iPartsDataPicOrderList dataPicOrderList = iPartsDataPicOrderList.loadPicOrderList(project, currentAssembly.getAsId().getKVari());
        for (iPartsDataPicOrder dataPicOrder : dataPicOrderList) {
            if (!iPartsTransferStates.isDeleteModuleAllowedState(dataPicOrder.getStatus())) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Der Bildauftrag \"%1\" ist noch offen.", dataPicOrder.getProposedName())
                                          + '\n' + TranslationHandler.translate("!!Löschen ist daher nicht möglich."),
                                          EditToolbarButtonAlias.EDIT_UNDOMODULE.getTooltip());
                return false;
            }
        }
        return true;
    }

    public static boolean prepareDeletingModule(EtkProject project, EtkDataAssembly currentAssembly) {
        if (!project.isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showError("!!Modul löschen ist ohne aktiven Autoren-Auftrag nicht möglich.");
            return false;
        }

        // Modul darf keine offenen Bildaufträge enthalten
        if (!checkOpenPictureOrders(project, currentAssembly)) {
            return false;
        }
//        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
//        iPartsDataPicOrderList dataPicOrderList = iPartsDataPicOrderList.loadPicOrderList(project, currentAssembly.getAsId().getKVari());
//        for (iPartsDataPicOrder dataPicOrder : dataPicOrderList) {
//            if (!iPartsTransferStates.isDeleteModuleAllowedState(dataPicOrder.getStatus())) {
//                MessageDialog.showWarning(TranslationHandler.translate("!!Der Bildauftrag \"%1\" ist noch offen.", dataPicOrder.getProposedName())
//                                          + '\n' + TranslationHandler.translate("!!Löschen ist daher nicht möglich."),
//                                          EditToolbarButtonAlias.EDIT_UNDOMODULE.getTooltip());
//                return false;
//            }
//        }

        //Sicherheitsabfrage
        if (MessageDialog.show(TranslationHandler.translate("!!Wollen Sie das Modul wirklich löschen?"), "!!Löschen",
                               MessageDialogIcon.CONFIRMATION,
                               MessageDialogButtons.YES, MessageDialogButtons.NO) != ModalResult.YES) {
            return false;
        }
        return true;
    }

    /**
     * Erzeugt eine Instanz von iPartsEditMainForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditModuleForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsEditPlugin editPlugin) {
        super(dataConnector, parentForm);
        this.editPlugin = editPlugin;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        editModulePanel.createModulButton.setVisible(iPartsRight.EDIT_PARTS_DATA.checkRightInSession());
        editModulePanel.createSAButton.setVisible(iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && iPartsRight.checkTruckAndBusInSession());

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, GotoEditPartWithPartialPathEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                GotoEditPartWithPartialPathEvent event = (GotoEditPartWithPartialPathEvent)call;
                if (isValidEventFromSender(event)) {
                    gotoPartWithPartialPath((GotoPartWithPartialPathEvent)call);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, iPartsExternalModifiedChangeSetEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Bei aktivem Autoren-Auftrag prüfen, ob dessen ChangeSet extern verändert wurde
                EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
                if (revisionsHelper != null) {
                    AbstractRevisionChangeSet activeChangeset = revisionsHelper.getActiveRevisionChangeSetForEdit();
                    iPartsExternalModifiedChangeSetEvent event = (iPartsExternalModifiedChangeSetEvent)call;
                    if ((activeChangeset != null) && event.getModifiedChangeSetIds().contains(activeChangeset.getChangeSetId().getGUID())) {
                        MessageDialog messageDialog = new MessageDialog(TranslationHandler.translate("!!Der aktive Autoren-Auftrag wurde extern z.B. durch einen Import verändert. Alle geöffneten TUs werden daher neu geladen.") +
                                                                        "\n" + TranslationHandler.translate("!!Details zu der externen Veränderung können über die Funktion \"Komplette Historie\" am Autoren-Auftrag angezeigt werden."),
                                                                        "!!Autoren-Auftrag extern verändert", null, MessageDialogIcon.INFORMATION.getImage(),
                                                                        new MessageDialogButtons[]{ MessageDialogButtons.OK }, false);
                        messageDialog.showModal(editModulePanel.getRootWindow());

                        // ChangeSet neu laden und Daten inkl. geöffneter Module aktualisieren
                        if (activeChangeset instanceof iPartsRevisionChangeSet) {
                            ((iPartsRevisionChangeSet)activeChangeset).loadFromDB();
                        }
                        getProject().fireProjectEvent(new DataChangedEvent());
                        iPartsEditPlugin.reloadAllModulesInEdit(getConnector());
                    }
                }
            }
        });

        editModulePanel.modulesTabbedPane.addEventListener(new BooleanResultEventListener(Event.TABBED_PANE_TAB_REQUESTS_CHANGE_EVENT) {
            @Override
            public void fire(BooleanResultEvent event) {
                GuiTabbedPaneEntryEdit paneEntry = getActualTabbedPaneEntry();
                if (paneEntry != null) {
                    if (paneEntry.editAssemblyListForm != null) {
                        if (paneEntry.editAssemblyListForm.askForClose()) {
                            // wenn ein InplaceEditor in der editAssemblyListForm offen ist, darf der Tab nicht gewechselt werden
                            event.setResult(false);
                        } else {
                            // Die Stückliste vom bisher aktiven Tab ab sofort verzögert aktualisieren
                            paneEntry.editAssemblyListForm.getAssemblyListForm().setDelayedUpdates(true);
                        }
                    }
                }
            }
        });

        // Listener, um die Statuszeile beim Wechsel des aktiven Tabs zu aktualisieren
        editModulePanel.modulesTabbedPane.addEventListener(new EventListener(Event.TABBED_PANE_TAB_CHANGED_EVENT) {
            @Override
            public void fire(Event event) {
                GuiTabbedPaneEntryEdit activeEditTabbedPaneEntry = getActualTabbedPaneEntry();
                if (activeEditTabbedPaneEntry != null) {
                    // Die Stückliste vom neuen aktiven Tab nicht mehr verzögert aktualisieren
                    if (activeEditTabbedPaneEntry.editAssemblyListForm != null) {
                        activeEditTabbedPaneEntry.editAssemblyListForm.getAssemblyListForm().setDelayedUpdates(false);
                    }

                    if (activeEditTabbedPaneEntry.isLazyLoading()) {
                        activeEditTabbedPaneEntry.doLazyLoading();
                    } else {
                        EditAssemblyImageForm editAssemblyImageForm = activeEditTabbedPaneEntry.editImageListForm;
                        if (editAssemblyImageForm != null) {
                            editAssemblyImageForm.updateStatusText();
                            return;
                        }
                    }
                }

                if ((getConnector().getMainWindow() != null) && isChildOfMasterRootWindow()) { // + Tab bzw. Fallback falls editAssemblyImageForm null ist
                    getConnector().getMainWindow().setStatusText("");
                }
            }
        });
    }

    /**
     * Darf die Form geschlossen werden?
     *
     * @return <code>true</code> bedeutet dass die Form NICHT geschlossen werden darf
     */
    protected boolean askAssemblyListForClose() {
        GuiTabbedPaneEntryEdit paneEntry = getActualTabbedPaneEntry();
        if (paneEntry != null) {
            // wenn ein InplaceEditor in der editAssemblyListForm offen ist, darf der Tab nicht gewechselt werden
            return paneEntry.editAssemblyListForm.askForClose(); // true = Veto zum schließen
        }
        return false;
    }

    private boolean askForAuthorOrder(EditModuleFormConnector editModuleFormConnector) {
        EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
        if (revisionsHelper != null) {
            if (!revisionsHelper.isRevisionChangeSetActive()) {
                // Texte nicht übersetzt weil Feature nur für DEVELOPMENT mode
                String yes = "Ja";
                String noLastAO = "Jüngsten aktivierbaren Auftrag aktivieren";
                String noOpenAO = "Auftragsübersicht anzeigen";
                String result = MessageDialog.show("Wirklich ohne aktiven Autorenauftrag weiter arbeiten?",
                                                   MessageDialogIcon.WARNING, yes, noLastAO, noOpenAO);
                if (result.equals(noLastAO)) {
                    String loginUserName = iPartsDataAuthorOrder.getLoginAcronym();
                    iPartsDataAuthorOrderList activableAuthorOrders = iPartsDataAuthorOrderList.loadAuthorOrderListByActivateable(getProject(), loginUserName);
                    if (!activableAuthorOrders.isEmpty()) {
                        iPartsDataAuthorOrder newestActivableAuthorOrder = activableAuthorOrders.get(0);
                        if (!newestActivableAuthorOrder.getChangeSetId().isEmpty()) {
                            // Ist das aktuelle ChangeSet nicht aktiv, dann setze es aktiv
                            if (!revisionsHelper.isRevisionChangeSetActive(newestActivableAuthorOrder.getChangeSetId())) {
                                iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(newestActivableAuthorOrder.getChangeSetId(), getProject());
                                revisionsHelper.setActiveRevisionChangeSet(changeSet, getProject());
                                editModuleFormConnector.setCurrentDataAuthorOrder(newestActivableAuthorOrder);
                            }
                        }
                    }
                } else if (result.equals(noOpenAO)) {
                    JavaViewerMainWindow mainWindow = editModuleFormConnector.getMainWindow();
                    List<AbstractJavaViewerMainFormContainer> authorOderForms = mainWindow.getFormsFromClass(EditWorkMainForm.class);
                    if (!authorOderForms.isEmpty()) {
                        EditWorkMainForm authorOrderForm = (EditWorkMainForm)authorOderForms.get(0);
                        mainWindow.displayForm(authorOrderForm);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void gotoPartWithPartialPath(GotoPartWithPartialPathEvent gotoPartEvent) {
        GuiTabbedPaneEntryEdit tabbedPaneEntry = getActualTabbedPaneEntry();
        if (tabbedPaneEntry != null) {
            if (tabbedPaneEntry.editAssemblyListForm != null) {
                if (tabbedPaneEntry.editAssemblyListForm.getConnector().getCurrentAssembly().getAsId().equals(gotoPartEvent.getAssemblyId())) {
                    tabbedPaneEntry.editAssemblyListForm.gotoPartListEntry(gotoPartEvent.getkLfdNr());
                    gotoPartEvent.setFound(true);
                }
            }
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return editModulePanel;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }

    private void addNewTabbedPaneEntry(final GuiTabbedPaneEntryEdit tabbedPaneEntry) {
        editModulePanel.modulesTabbedPane.switchOffEventListeners();
        editModulePanel.modulesTabbedPane.removeChild(editModulePanel.loadModuleTabEntry);
        try {
            if (showCloseButtonOnHeaderForm) {
                editModulePanel.modulesTabbedPane.addChild(tabbedPaneEntry);
            } else {
                tabbedPaneEntry.setShowCloseIcon(true);
                tabbedPaneEntry.addEventListener(new BooleanResultEventListener(Event.TABBED_PANE_TAB_REQUESTS_CLOSE_EVENT) {
                    @Override
                    public void fire(BooleanResultEvent event) {
                        if ((tabbedPaneEntry.editHeaderForm != null) && (tabbedPaneEntry.editAssemblyListForm != null)) {
                            if (tabbedPaneEntry.editHeaderForm.askForClose() || tabbedPaneEntry.editAssemblyListForm.askForClose()) {
                                event.setResult(false); // Tab darf nicht geschlossen werden
                            } else {
                                event.setResult(true); // Tab darf geschlossen werden
                                return;
                            }
                        } else {
                            event.setResult(true); // Tab darf geschlossen werden
                            return;
                        }
                        if ((getConnector().getMainWindow() != null) && isChildOfMasterRootWindow()) {
                            getConnector().getMainWindow().setStatusText("");
                        }
                    }
                });
                editModulePanel.modulesTabbedPane.addChild(tabbedPaneEntry);
            }
            ThemeManager.get().render(tabbedPaneEntry);
        } finally {
            try {
                editModulePanel.modulesTabbedPane.addChild(editModulePanel.loadModuleTabEntry);
                ThemeManager.get().render(editModulePanel.loadModuleTabEntry);
            } finally {
                editModulePanel.modulesTabbedPane.switchOnEventListeners(); // muss auf jeden Fall ausgeführt werden
            }
        }
    }

    GuiTabbedPaneEntryEdit createTabbedPaneEntry(String title) {
        GuiTabbedPaneEntryEdit tabbedPaneEntry = new GuiTabbedPaneEntryEdit();

        //tabbedPaneEntry.setName("tabbedpaneentry_0");
        tabbedPaneEntry.__internal_setGenerationDpi(96);
        tabbedPaneEntry.registerTranslationHandler(getUITranslationHandler());
        tabbedPaneEntry.setScaleForResolution(true);
        tabbedPaneEntry.setMinimumWidth(10);
        tabbedPaneEntry.setMinimumHeight(10);
        tabbedPaneEntry.setTitle(title);
        return tabbedPaneEntry;
    }

    private GuiPanel createTabbedPaneEntryPanel() {
        GuiPanel tabbedPaneEntryPanel = new GuiPanel();
        //tabbedPaneEntryPanel.setName("tabbedpaneentry_0_content_1");
        tabbedPaneEntryPanel.__internal_setGenerationDpi(96);
        tabbedPaneEntryPanel.registerTranslationHandler(getUITranslationHandler());
        tabbedPaneEntryPanel.setScaleForResolution(true);
        LayoutBorder tabbedPaneEntryContentLayout = new LayoutBorder();
        tabbedPaneEntryPanel.setLayout(tabbedPaneEntryContentLayout);
        return tabbedPaneEntryPanel;
    }

    private void createTabbedPaneEntryContent(final GuiTabbedPaneEntryEdit tabbedPaneEntry,
                                              final iPartsDocumentationType documentationType) {
        GuiPanel tabbedPaneEntryContent = createTabbedPaneEntryPanel();
        if (use2ndPanel) {
            tabbedPaneEntry.panelTabbedTwo = new PanelTabbedTwoClass(getUITranslationHandler());
            tabbedPaneEntry.panelTabbedTwo.__internal_setGenerationDpi(96);

            tabbedPaneEntry.panelTabbedTwo.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            if (!doLazyLoading) {
                EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2) {
                    @Override
                    public void fireOnce(Event event) {
                        int panelPictureHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                        int newPos = (panelPictureHeight - tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.getDividerSize() - 3) / 2;
                        tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.setDividerPosition(newPos);
//                    tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.setShowing(false);
                    }
                };
                EventListener resizeListener2 = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild) {
                    @Override
                    public boolean isFireOnceValid(Event event) {
                        return tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.isSplitPaneSizeValid();
                    }

                    @Override
                    public void fireOnce(Event event) {
                        tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.setShowing(false);
                    }
                };
                tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.addEventListener(resizeListener);
                if (documentationType.isPKWDocumentationType()) {
                    AbstractConstraints constraints = tabbedPaneEntry.panelTabbedTwo.splitpaneMaster.getConstraints();
                    tabbedPaneEntry.panelTabbedTwo.splitpaneMaster.removeFromParent();
                    tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1.removeFromParent();
                    tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1.setConstraints(constraints);
                    tabbedPaneEntry.panelTabbedTwo.addChild(tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1);
                } else {
                    tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.addEventListener(resizeListener2);
                }
            }
            tabbedPaneEntry.panelTabbedTwo.imageDockingpanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    Session.invokeThreadSafeInSession(() -> {
                        EditAssemblyImageForm editImageListFormLocal = tabbedPaneEntry.editImageListForm;
                        if (editImageListFormLocal != null) {
                            PanelTabbedTwoClass panelTabbedTwoLocal = tabbedPaneEntry.panelTabbedTwo;
                            if (panelTabbedTwoLocal != null) {
                                GuiDockingPanel imageDockingpanelLocal = panelTabbedTwoLocal.imageDockingpanel;
                                if (imageDockingpanelLocal != null) {
                                    editImageListFormLocal.handleDividerPos(!imageDockingpanelLocal.isShowing());
                                }
                            }
                        }
                    });
                }
            });
            tabbedPaneEntryContent.addChild(tabbedPaneEntry.panelTabbedTwo);
        } else {
            tabbedPaneEntry.panelTabbed = new PanelTabbedClass(getUITranslationHandler());
            tabbedPaneEntry.panelTabbed.__internal_setGenerationDpi(96);

            // Footer wird aktuell nicht benötigt
            tabbedPaneEntry.panelTabbed.separatorFooter.removeFromParent();
            tabbedPaneEntry.panelTabbed.panelFooter.removeFromParent();

            tabbedPaneEntry.panelTabbed.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbed.moduleSplitPane) {
                @Override
                public void fireOnce(Event event) {
                    int panelPictureHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                    int newPos = (panelPictureHeight - tabbedPaneEntry.panelTabbed.moduleSplitPane.getDividerSize() - 3) / 2;
                    tabbedPaneEntry.panelTabbed.moduleSplitPane.setDividerPosition(newPos);
                }
            };
            tabbedPaneEntry.panelTabbed.moduleSplitPane.addEventListener(resizeListener);
            tabbedPaneEntryContent.addChild(tabbedPaneEntry.panelTabbed);
        }
        Session.invokeThreadSafeInSession(() -> tabbedPaneEntry.addChild(tabbedPaneEntryContent));
    }


    private GuiTabbedPaneEntryEdit getActualTabbedPaneEntry() {
        return getActualTabbedPaneEntry(editModulePanel.modulesTabbedPane.getActiveTab());
    }

    private GuiTabbedPaneEntryEdit getActualTabbedPaneEntry(int index) {
        List<AbstractGuiControl> children = new DwList<>(editModulePanel.modulesTabbedPane.getChildren());
        if ((index >= 0) && (index < children.size())) {
            GuiTabbedPaneEntry tabPaneEntry = (GuiTabbedPaneEntry)children.get(index);
            if (tabPaneEntry instanceof GuiTabbedPaneEntryEdit) {
                return (GuiTabbedPaneEntryEdit)tabPaneEntry;
            }
        }
        return null;
    }

    private GuiTabbedPaneEntryEdit getActualTabbedPaneEntry(String moduleNo) {
        int index = searchLoadedModule(moduleNo);
        if (index != -1) {
            return getActualTabbedPaneEntry(index);
        }
        return null;
    }

    /**
     * Liefert alle offenen Reiter
     *
     * @return
     */
    public List<GuiTabbedPaneEntryEdit> getAllTabbedPaneEntries() {
        List<GuiTabbedPaneEntryEdit> result = new ArrayList<>();
        for (AbstractGuiControl tabEntry : editModulePanel.modulesTabbedPane.getChildren()) {
            if (tabEntry instanceof GuiTabbedPaneEntryEdit) {
                result.add((GuiTabbedPaneEntryEdit)tabEntry);
            }
        }
        return result;
    }

    /**
     * Liefert alle editierbaren Reiter. Wenn der Connector eines Reiters keinen Autoren-Auftrag hat, dann wird er nur
     * im Read-Only Modus angezeigt)
     *
     * @return
     */
    public List<GuiTabbedPaneEntryEdit> getAllEditablePaneEntries() {
        List<GuiTabbedPaneEntryEdit> result = new ArrayList<>();
        for (GuiTabbedPaneEntryEdit tabEntry : getAllTabbedPaneEntries()) {
            EditModuleFormConnector connector = tabEntry.getConnector();
            if ((connector != null) && connector.isAuthorOrderValid()) {
                result.add(tabEntry);
            }
        }
        return result;
    }

    private PanelTabbedClass getWorkPanel(GuiTabbedPaneEntryEdit tabbedPaneEntry) {
        return tabbedPaneEntry.panelTabbed;
    }

    private PanelTabbedTwoClass getWorkPanel2(GuiTabbedPaneEntryEdit tabbedPaneEntry) {
        return tabbedPaneEntry.panelTabbedTwo;
    }

    private AssemblyId getSpecialEditAssemblyId(int index) {
        GuiTabbedPaneEntry tabPaneEntry = (GuiTabbedPaneEntry)editModulePanel.modulesTabbedPane.getChildren().get(index);
        if (tabPaneEntry instanceof GuiTabbedPaneEntryEdit) {
            GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = (GuiTabbedPaneEntryEdit)tabPaneEntry;
            if (tabbedPaneEntryEdit.isLazyLoading()) {
                return tabbedPaneEntryEdit.lazyLoadInfo.getAssemblyId();
            } else if (tabbedPaneEntryEdit.isPreparedForLoading()) {
                return tabbedPaneEntryEdit.preparedForLoadInfo.getAssemblyId();
            } else {
                EditModuleFormConnector connector = getEditConnector(tabbedPaneEntryEdit);
                if (connector != null) {
                    return connector.getCurrentAssembly().getAsId();
                }
            }
        }
        return null;
    }

    private EditModuleFormConnector getEditConnector(GuiTabbedPaneEntryEdit tabbedPaneEntry) {
        if (tabbedPaneEntry != null) {
            return tabbedPaneEntry.connector;
        }
        return null;
    }

    private int searchLoadedModule(String moduleNo) {
        if (!moduleNo.isEmpty()) {
            AssemblyId searchId = new AssemblyId(moduleNo, "");
            for (int index = 0; index < (editModulePanel.modulesTabbedPane.getChildren().size() - 1); index++) {
                AssemblyId assemblyId = getSpecialEditAssemblyId(index);
                if ((assemblyId != null) && assemblyId.equals(searchId)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private void setListFormsEdit(EtkDataAssembly currentAssembly, EditModuleFormConnector connector,
                                  final GuiTabbedPaneEntryEdit tabbedPaneEntry) {
        PanelTabbedClass ptc = null;
        PanelTabbedTwoClass ptc2 = null;
        if (use2ndPanel) {
            ptc2 = getWorkPanel2(tabbedPaneEntry);
        } else {
            ptc = getWorkPanel(tabbedPaneEntry);
        }
        if (connector == null) {
            tabbedPaneEntry.connector = new EditModuleFormConnector(getConnector());
            //Daten im Connector setzen
            iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
            tabbedPaneEntry.connector.setCurrentDataAuthorOrder(dataAuthorOrder);
            addOwnConnector(tabbedPaneEntry.connector);
        } else {
            tabbedPaneEntry.connector = connector;
        }
        tabbedPaneEntry.connector.setCurrentAssembly(currentAssembly);
        //connector.setCurrentNavigationPath(currentAssemblyPath);
        tabbedPaneEntry.connector.setCurrentPartListEntries(null);
        tabbedPaneEntry.connector.updatePictureOrderList();

        if (tabbedPaneEntry.editImageListForm == null) {
            tabbedPaneEntry.editImageListForm = new EditAssemblyImageForm(tabbedPaneEntry.connector, this);
            tabbedPaneEntry.editImageListForm.modifyConnectorBeforeUpdateData(this, true); // Setzt z.B. das ThumbnailsActive-Flag

            if (use2ndPanel) {
                if (ptc2 != null) {
                    ptc2.imagePanel_2.removeAllChildren();
                    ptc2.imagePanel_2.addChild(tabbedPaneEntry.editImageListForm.getGui());
                }
            } else {
                if (ptc != null) {
                    ptc.imagePanel.removeAllChildren();
                    ptc.imagePanel.addChild(tabbedPaneEntry.editImageListForm.getGui());
                }
            }
        }

        if (tabbedPaneEntry.editAssemblyListForm == null) {
            // Jetzt erst das editAssemblyListForm erzeugen, da dieses bereits ein updateView() ausführt, was schon obige Flags
            // auswerten soll, um das erneute Laden der Stückliste weiter unten zu vermeiden
            tabbedPaneEntry.editAssemblyListForm = new EditAssemblyListForm(tabbedPaneEntry.connector, this, "EditAssemblyListForm");

            // Die Stückliste nicht verzögert aktualisieren, da diese gerade im Edit angezeigt wird
            tabbedPaneEntry.editAssemblyListForm.getAssemblyListForm().setDelayedUpdates(false);

            if (use2ndPanel) {
                if (ptc2 != null) {
                    ptc2.partListPanel_2.removeAllChildren();
                    ptc2.partListPanel_2.addChild(tabbedPaneEntry.editAssemblyListForm.getGui());
                }
            } else {
                if (ptc != null) {
                    ptc.partListPanel.removeAllChildren();
                    ptc.partListPanel.addChild(tabbedPaneEntry.editAssemblyListForm.getGui());
                }
            }
        }

        final EditAssemblyListForm editAssemblyListForm = tabbedPaneEntry.editAssemblyListForm;

        //Callbacks als Verbindung zwischen Image und AssemblyForm
        if (tabbedPaneEntry.editImageEventListener != null) {
            tabbedPaneEntry.editImageListForm.removeEventListener(tabbedPaneEntry.editImageEventListener);
        }
        tabbedPaneEntry.editImageEventListener = new AssemblyImageFormEvents() {
            @Override
            public boolean isInPartsList(EtkHotspotDestination link) {
                EtkDataPartListEntry partListEntry = editAssemblyListForm.getFirstPartListEntryForLink(link);
                return partListEntry != null;
            }

            @Override
            public List<EtkDataPartListEntry> getPartListEntriesForLink(EtkHotspotDestination link) {
                return editAssemblyListForm.getAssemblyListForm().getPartListEntriesForNormalLink(link);
            }

            @Override
            public String getColValueLink(EtkHotspotDestination link, String tableName, String fieldName) {
                EtkDataPartListEntry partListEntry = editAssemblyListForm.getFirstPartListEntryForLink(link);
                String tabFieldName = TableAndFieldName.make(tableName, fieldName);
                if (partListEntry != null) {
                    EtkHotspotLinkHelper hotspotLinkHelper = new EtkHotspotLinkHelper(getProject(), editAssemblyListForm.getAssemblyListForm().getConnector().getCurrentAssembly());
                    return hotspotLinkHelper.getHotspotFieldValue(partListEntry, tabFieldName, getProject().getDBLanguage());
                } else {
                    return "";
                }
            }

            @Override
            public boolean isAssemblyLink(EtkHotspotDestination link) {
                EtkDataPartListEntry partListEntry = editAssemblyListForm.getFirstPartListEntryForLink(link);
                return (partListEntry != null) && (partListEntry.isAssembly());
            }

            @Override
            public void OnLoaded() {
            }

            @Override
            public boolean OnLinkClick(List<GuiViewerLink> links, int imageIndex, boolean imageIs3D, int button) {
                List<EtkHotspotDestination> destLinks = new ArrayList<>();
                // im Edit hier keine Plug-ins auswerten
                if (links != null) {
                    for (GuiViewerLink link : links) {
                        if (link != null) {
                            // Normale Verarbeitung. Ist Link 'null' werden alle Stücklisteneinträge deselektiert
                            EtkHotspotDestination destLink = new EtkHotspotDestination(link, imageIs3D, imageIndex);
                            destLinks.add(destLink);
                        }
                    }
                }
                editAssemblyListForm.getAssemblyListForm().gotoPartListEntryViewer(destLinks);

                // im Editor muss nach dem Klicken auf einen Hotspot und Aufruf von gotoPartListEntryViewer()
                // nochmals zum ersten selektierten Eintrag gescrolled werden, da durch die Beibehaltung
                // der horizontalen ScrollPos die falsche y-Koordinate gesetzt wurde
                List<EtkDataPartListEntry> selectedEntries = editAssemblyListForm.getAssemblyListForm().getSelectedEntries();
                if ((selectedEntries != null) && !selectedEntries.isEmpty()) {
                    int rowIndex = editAssemblyListForm.getAssemblyListForm().getPartListTable().getSelectedRowIndex();
                    editAssemblyListForm.getAssemblyListForm().getPartListTable().scrollToCell(rowIndex, 0);
                }
                return false;
            }

            @Override
            public boolean OnLinkDblClick(GuiViewerLink link, int imageIndex, boolean imageIs3D, int button) {
                // im Edit nicht auf Doppelklicks reagieren
                return false;
            }

            @Override
            public String OnLinkHintTextNeeded(GuiViewerLink link, String hintText) {
                return null;
            }

            @Override
            public void OnZoomed(double zoomFactor) {
            }

            @Override
            public void OnScrollbarVisibilityChanged(boolean horizontalScrollbarVisible, boolean verticalScrollbarVisible) {

            }
        };
        tabbedPaneEntry.editImageListForm.addEventListener(tabbedPaneEntry.editImageEventListener);

        // Der Header ist klein. weswegen wir ihn einfach immer neu erzeugen, damit die angezeigten Daten auch korrekt sind
        if (tabbedPaneEntry.editHeaderForm != null) {
            tabbedPaneEntry.editHeaderForm.dispose();
        }
        Session session = Session.get();
        tabbedPaneEntry.editHeaderForm = new EditHeaderForm(tabbedPaneEntry.connector, this, new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                tabbedPaneEntry.close();
                if ((session != null) && session.isActive()) {
                    session.invokeThreadSafe(() -> editModulePanel.modulesTabbedPane.removeChild(getActualTabbedPaneEntry()));
                }
            }
        }, showCloseButtonOnHeaderForm);

        if (use2ndPanel) {
            if (ptc2 != null) {
                ptc2.panelHeader_2.removeAllChildren();
                ptc2.panelHeader_2.addChild(tabbedPaneEntry.editHeaderForm.getGui());
            }
        } else {
            if (ptc != null) {
                ptc.panelHeader.removeAllChildren();
                ptc.panelHeader.addChild(tabbedPaneEntry.editHeaderForm.getGui());
            }
        }

        if (ptc2 != null) {
            boolean pskMaterialsAllowed = false;
            EtkDataAssembly assembly = tabbedPaneEntry.connector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                pskMaterialsAllowed = ((iPartsDataAssembly)assembly).isPSKAssembly();
            }

            boolean editMaterialEditFormCreated = false;
            if (tabbedPaneEntry.editMaterialEditForm != null) {
                if (tabbedPaneEntry.editMaterialEditForm.isPskMaterialsAllowed() != pskMaterialsAllowed) {
                    tabbedPaneEntry.editMaterialEditForm.dispose();
                    tabbedPaneEntry.editMaterialEditForm.getGui().removeFromParent();
                } else {
                    editMaterialEditFormCreated = true;
                }
            }

            if (!editMaterialEditFormCreated) {
                tabbedPaneEntry.editMaterialEditForm = new EditMaterialEditForm(tabbedPaneEntry.connector, this, editPlugin,
                                                                                pskMaterialsAllowed);
                tabbedPaneEntry.editMaterialEditForm.setOnPartListEntryValidEvent(new EditMaterialEditForm.OnPartListEntryValidEvent() {
                    @Override
                    public void onPartListEntryValidEvent(EtkDataPartListEntry partListEntry) {
                        if (partListEntry != null) {
                            tabbedPaneEntry.connector.posNumberChanged();
                            tabbedPaneEntry.connector.dataChanged(null);
                            // dataChanged() ruft aktuell sowieso schon updateAllViews(null, false) auf
                            //tabbedPaneEntry.connector.updateAllViews(null, false);
                            tabbedPaneEntry.editAssemblyListForm.gotoPartListEntry(partListEntry);
                        }
                    }
                });
                ptc2.tabbedpaneentryMaterialContent.addChild(tabbedPaneEntry.editMaterialEditForm.getGui());
            }

            if (tabbedPaneEntry.editModuleEditForm == null) {
                tabbedPaneEntry.editModuleEditForm = new EditModuleEditForm(tabbedPaneEntry.connector, this, editPlugin,
                                                                            ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage());
                tabbedPaneEntry.editModuleEditForm.setOnPartListEntryValidEvent(new EditMaterialEditForm.OnPartListEntryValidEvent() {
                    @Override
                    public void onPartListEntryValidEvent(EtkDataPartListEntry partListEntry) {
                        if (partListEntry != null) {
                            tabbedPaneEntry.connector.posNumberChanged();
                            tabbedPaneEntry.connector.dataChanged(null);
                            // dataChanged() ruft aktuell sowieso schon updateAllViews(null, false) auf
                            tabbedPaneEntry.editAssemblyListForm.gotoPartListEntry(partListEntry);
                        }
                    }
                });
                PanelTabbedTwoClass finalPtc = ptc2;
                ptc2.tabbedpaneExtras.addEventListener(new EventListener(Event.TABBED_PANE_TAB_CHANGED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        if (finalPtc.tabbedpaneExtras.getActiveTab() == 1) {
                            tabbedPaneEntry.editModuleEditForm.doStartSearch();
                        }
                    }
                });
                ptc2.tabbedpaneentryModuleContent.addChildBorderCenter(tabbedPaneEntry.editModuleEditForm.getGui());
            }
        }

        // Zuletzt alle Views nochmal aktualisieren falls es noch offene Änderungen im Connector gibt
        tabbedPaneEntry.connector.updateAllViews(this, false);
        tabbedPaneEntry.guiCreated = true;
    }

    /**
     * nur Modul laden
     *
     * @param moduleNo
     */
    public boolean loadModule(String moduleNo) {
        return loadModule(moduleNo, null, true, false);
    }

    /**
     * Modul laden mit der Möglichkeit es explizit ReadOnly zu laden
     *
     * @param moduleNo
     * @param loadReadOnly
     */
    public boolean loadModule(String moduleNo, boolean loadReadOnly) {
        return loadModule(moduleNo, null, true, loadReadOnly);
    }

    /**
     * Modul laden mit dem fokussieren des ersten Tabs
     *
     * @param moduleNo
     * @param kLfdNr
     * @param loadReadOnly
     * @return
     */
    public boolean loadModule(String moduleNo, String kLfdNr, boolean loadReadOnly) {
        return loadModule(moduleNo, kLfdNr, true, loadReadOnly);
    }

    /**
     * Modul laden und zur Zeile springen
     *
     * @param moduleNo
     * @param kLfdNr
     */
    public boolean loadModule(String moduleNo, String kLfdNr) {
        return loadModule(moduleNo, kLfdNr, true, false);
    }

    public boolean loadModule(String moduleNo, String kLfdNr, boolean withFocus, boolean loadReadOnly) {
        return loadModule(moduleNo, kLfdNr, loadReadOnly, withFocus, null);
    }

    /**
     * Lädt das Modul mit der übergebenen Modulnummer.
     * ist kLfdNr besetzt, dann wird die Zeile selektiert
     * ist loadReadOnly = True, dann wird das Modul ReadOnly geladen (egal, ob AutoenAuftrag aktiv oder nicht)
     * ist additionalTextForHeader gesetzt, wird neben dem Header noch zusätzlich der Text ausgegeben
     * ist withFocus = true, dann wird der Fokus auf das geladene Modul gesetzt
     *
     * @param moduleNo
     * @param kLfdNr
     * @param loadReadOnly
     * @param withFocus
     * @param additionalTextForHeader
     */
    public boolean loadModule(String moduleNo, final String kLfdNr, boolean loadReadOnly, boolean withFocus, String additionalTextForHeader) {
        if (!iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            loadReadOnly = true;
        }
        if (!moduleNo.isEmpty()) {
            int index = searchLoadedModule(moduleNo);
            if (index == -1) {
                final EditModuleFormConnector connector = new EditModuleFormConnector(getConnector());
                //Daten im Connector setzen
                iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
                connector.setCurrentDataAuthorOrder(dataAuthorOrder);
                connector.setAdditionalTextForHeader(additionalTextForHeader);
                boolean doAdd = true;
                if (!loadReadOnly && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                    doAdd = askForAuthorOrder(connector);
                }
                if (doAdd) {
                    final iPartsDataAssembly currentAssembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(moduleNo, ""), false);
                    if (!currentAssembly.existsInDB()) {
                        MessageDialog.showError(TranslationHandler.translate("!!Das Modul \"%1\" existiert nicht!", moduleNo));
                        return false;
                    }

                    if (!currentAssembly.checkPSKInSession(loadReadOnly, true)) {
                        return false;
                    }

                    if (!currentAssembly.checkSAVisibilityInSession(loadReadOnly, true)) {
                        return false;
                    }

                    iPartsProductId productId = currentAssembly.getProductIdFromModuleUsage();
                    iPartsRight loadRight = loadReadOnly ? iPartsRight.VIEW_PARTS_DATA : iPartsRight.EDIT_PARTS_DATA;
                    if ((productId != null) && !iPartsRight.checkProductEditableInSession(productId, loadRight, true, getProject())) {
                        return false;
                    }
                    // ToDo freigeben wenn DAIMLER-4836 realisiert wird
//                    iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
//                    if (dataAuthorOrder != null) {
//                        List<iPartsChangeSetViewerElem.iPartsChangeSetSearchElem> changeSetList = iPartsChangeSetViewerElem.getAssemblyListFromActiveChangeSets(getProject());
//                        iPartsChangeSetViewerElem.iPartsChangeSetSearchElem changeSetElem = iPartsChangeSetViewerElem.containsAssemblyId(changeSetList, currentAssembly.getAsId(), dataAuthorOrder.getChangeSetId());
//                        if (changeSetElem != null) {
//                            if (MessageDialog.show(TranslationHandler.translate("!!Das Modul \"%1\" wird bereits in einem anderen Autoren-Auftrag benutzt!", moduleNo)
//                                                   + "\n\n" + TranslationHandler.translate("!!Fortfahren?"), "!!Module laden",
//                                                   MessageDialogIcon.CONFIRMATION, MessageDialogButtons.YES, MessageDialogButtons.NO) != ModalResult.YES) {
//                                return;
//                            }
//                        }
//                    }

                    // Modul ist nicht im Editor geladen => neues Tab erzeugen
                    GuiTabbedPaneEntryEdit tabbedPaneEntry = createTabbedPaneEntry(moduleNo);
                    createTabbedPaneEntryContent(tabbedPaneEntry, currentAssembly.getDocumentationType());
                    boolean doAddDirect = true;
                    if (doLazyLoading && StrUtils.isValid(additionalTextForHeader)) {
                        // den ersten TU direkt laden (mit LazyLoading, d.h. wenn Editor Focus kriegt)
                        doAddDirect = editModulePanel.modulesTabbedPane.getChildren().size() <= 1;
                    }
                    addNewTabbedPaneEntry(tabbedPaneEntry);

                    if (doAddDirect && withFocus) {
                        loadModuleInTab(tabbedPaneEntry, currentAssembly, kLfdNr, connector);
                        editModulePanel.modulesTabbedPane.selectTab(tabbedPaneEntry);
                    } else {
                        prepareTabbedPaneEntry(tabbedPaneEntry, currentAssembly);
                        tabbedPaneEntry.prepareForLazyLoading(currentAssembly, kLfdNr, connector);
                    }
                }
            } else {
                // Modul ist bereits im Editor geladen
                GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(index);
                if (tabbedPaneEntryEdit == null) {
                    return false;
                }
                iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSetForEdit(getProject());
                EditModuleFormConnector connector = tabbedPaneEntryEdit.connector;
                if (connector == null) {
                    if (tabbedPaneEntryEdit.isLazyLoading()) {
                        connector = tabbedPaneEntryEdit.lazyLoadInfo.getConnector();
                    } else if (tabbedPaneEntryEdit.isPreparedForLoading()) {
                        connector = tabbedPaneEntryEdit.preparedForLoadInfo.getConnector();
                    }
                }
                if (connector == null) {
                    return false;
                }
                if (connector.isAuthorOrderValid()) {
                    // aktuell geladenes Modul besitzt aktives ChangeSet (sollte nicht auftreten)
                    if (dataAuthorOrder != null) {
                        // ChangeSet ist aktiv
                        if (connector.getCurrentDataAuthorOrder().getAsId().equals(dataAuthorOrder.getAsId())) {
                            if (withFocus) {
                                if (tabbedPaneEntryEdit.isPreparedForLoading()) { // Das Modul wurde noch gar nicht geladen -> laden forcieren
                                    editModulePanel.modulesTabbedPane.removeChild(tabbedPaneEntryEdit, true);
                                    loadModule(moduleNo, kLfdNr, withFocus, loadReadOnly);
                                } else {
                                    editModulePanel.modulesTabbedPane.selectTab(index);
                                    if (StrUtils.isValid(kLfdNr)) {
                                        tabbedPaneEntryEdit.selectPartListEntryByLfdnr(kLfdNr);
                                    }
                                }
                            }
                        } else {
                            // ChangeSets sind unterschiedlich
                            // sollte nicht auftreten
                            editModulePanel.modulesTabbedPane.removeChild(tabbedPaneEntryEdit, true);
                            loadModule(moduleNo, kLfdNr, withFocus, loadReadOnly);
                        }
                    } else {
                        // ChangeSet ist nicht aktiv
                        // sollte nicht auftreten (zur Sicherheit)
                        editModulePanel.modulesTabbedPane.removeChild(tabbedPaneEntryEdit, true);
                        loadModule(moduleNo, kLfdNr, withFocus, loadReadOnly);
                    }
                } else {
                    // aktuell geladenes Modul ist im ReadOnly-Modus
                    if (dataAuthorOrder != null) {
                        // Modul war Readonly geladen, jetzt mit Aktivem Autoren-Auftrag => schließen und neu laden
                        editModulePanel.modulesTabbedPane.removeChild(tabbedPaneEntryEdit, true);
                        loadModule(moduleNo, kLfdNr, withFocus, loadReadOnly);
                    } else {
                        // kein ChangeSet aktiv => Modul anzeigen
                        editModulePanel.modulesTabbedPane.selectTab(index);
                        if (StrUtils.isValid(kLfdNr)) {
                            tabbedPaneEntryEdit.selectPartListEntryByLfdnr(kLfdNr);
                        }
                    }
                }
            }
            return true;
        }

        return false;
    }

    private void loadModuleInTab(final GuiTabbedPaneEntryEdit tabbedPaneEntry, final EtkDataAssembly assembly,
                                 final String kLfdNr,
                                 final EditModuleFormConnector connector) {
        if (doLazyLoading) {
            EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2) {
                @Override
                public void fireOnce(Event event) {
                    int panelPictureHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                    int newPos = (panelPictureHeight - tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.getDividerSize() - 3) / 2;
                    tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.setDividerPosition(newPos);
                    tabbedPaneEntry.preparedForLoadInfo = null;
                    Session.get().invokeThreadSafeWithThread(() -> loadModuleInPseudoTransAction(tabbedPaneEntry, assembly,
                                                                                                 kLfdNr, connector));
                }
            };
            tabbedPaneEntry.setPreparedForLoadInfo(assembly, kLfdNr, connector);
            Session.invokeThreadSafeInSession(() -> tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.addEventListener(resizeListener));

            prepareTabbedPaneEntry(tabbedPaneEntry, assembly);
        } else {
            loadModuleInPseudoTransAction(tabbedPaneEntry, assembly, kLfdNr, connector);
        }
    }

    private void prepareTabbedPaneEntry(GuiTabbedPaneEntryEdit tabbedPaneEntry, EtkDataAssembly assembly) {
        if (tabbedPaneEntry.guiCreated) {
            return; // Tab hat schon eine GUI und muss nicht neu aufgebaut werden
        }

        boolean isPSKOrTruckDocumentationType = false;
        boolean isCarPerspectiveModule = EditModuleHelper.isCarPerspectiveAssembly(assembly);
        if (assembly instanceof iPartsDataAssembly) {
            isPSKOrTruckDocumentationType = ((iPartsDataAssembly)assembly).isPSKAssembly() ||
                                            ((iPartsDataAssembly)assembly).getDocumentationType().isELDASDocumentationType() ||
                                            isCarPerspectiveModule;
        }
        if (!isPSKOrTruckDocumentationType) {
            AbstractConstraints constraints = tabbedPaneEntry.panelTabbedTwo.splitpaneMaster.getConstraints();
            Session.invokeThreadSafeInSession(() -> {
                tabbedPaneEntry.panelTabbedTwo.splitpaneMaster.removeFromParent();
                tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1.removeFromParent();
                tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1.setConstraints(constraints);
                tabbedPaneEntry.panelTabbedTwo.addChild(tabbedPaneEntry.panelTabbedTwo.splitpaneMaster_firstChild_1);
            });
        } else {
            EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild) {
                @Override
                public boolean isFireOnceValid(Event event) {
                    return tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.isSplitPaneSizeValid();
                }

                @Override
                public void fireOnce(Event event) {
                    tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.setShowing(false);
                }
            };

            tabbedPaneEntry.panelTabbedTwo.dockingpanelMaster_secondChild.addEventListener(resizeListener);
            if (!isCarPerspectiveModule) {
                tabbedPaneEntry.panelTabbedTwo.tabbedpaneentryModule.removeFromParent();
            }
        }

        GuiLabel label = new GuiLabel("!!Technischer Umfang wird geladen...");
        label.setFontSize(15);
        label.setFontStyle(DWFontStyle.BOLD);
        label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.CENTER);
        label.setBorderWidth(7);
        Session.invokeThreadSafeInSession(() -> tabbedPaneEntry.panelTabbedTwo.partListPanel_2.addChildBorderCenter(label));
    }

    private void loadModuleInPseudoTransActionInEvent(final GuiTabbedPaneEntryEdit tabbedPaneEntry,
                                                      final EtkDataAssembly assembly,
                                                      final String kLfdNr, final EditModuleFormConnector connector) {
        EventListener resizeListener = new EventListenerFireOnce(Event.ON_RESIZE_EVENT, tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2) {
            @Override
            public void fireOnce(Event event) {
                int panelPictureHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                int newPos = (panelPictureHeight - tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.getDividerSize() - 3) / 2;
                tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.setDividerPosition(newPos);

                if (J2EEHandler.isJ2EE()) {
                    Session.get().invokeThreadSafeWithThread(() -> loadModuleInPseudoTransAction(tabbedPaneEntry, assembly,
                                                                                                 kLfdNr, connector));
                }
            }
        };
        tabbedPaneEntry.panelTabbedTwo.module_Splitpane_2.addEventListener(resizeListener);
    }

    private void loadModuleInPseudoTransAction(GuiTabbedPaneEntryEdit tabbedPaneEntry, EtkDataAssembly assembly,
                                               String kLfdNr, EditModuleFormConnector connector) {

        startPseudoTransactionForActiveChangeSet(true);
        try {
            setListFormsEdit(assembly, connector, tabbedPaneEntry);
            if (StrUtils.isValid(kLfdNr)) {
                tabbedPaneEntry.selectPartListEntryByLfdnr(kLfdNr);
            }
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }
    }

    /**
     * Speichern eines Moduls via {@link EditModuleInfo}
     *
     * @param moduleInfo
     */
    public void saveModule(EditModuleInfo moduleInfo) {
        if (moduleInfo.isValid()) {
            String moduleNo = moduleInfo.getAssemblyId().getKVari();
            GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(moduleNo);
            if (tabbedPaneEntryEdit != null) {
                tabbedPaneEntryEdit.editHeaderForm.doSaveModule(moduleNo);
            }
        }
    }

    /**
     * Refresh für ein geladenes Modul via {@link EditModuleInfo}
     *
     * @param moduleInfo
     */
    public void reloadModule(EditModuleInfo moduleInfo) {
        if (moduleInfo.isValid()) {
            GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(moduleInfo.getAssemblyId().getKVari());
            if (tabbedPaneEntryEdit != null) {
                EtkDataAssembly currentAssembly = moduleInfo.getConnector().getCurrentAssembly();
                if ((currentAssembly instanceof iPartsDataAssembly) && (currentAssembly.getEtkProject() != null)) { // Lebt die currentAsselby überhaupt noch?
                    currentAssembly.unloadPartList();
                    moduleInfo.getConnector().clearFilteredEditPartListEntries();

                    String kLfdNr = null;
                    List<EtkDataPartListEntry> selectedPartListEntries = moduleInfo.getConnector().getSelectedPartListEntries();
                    if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                        kLfdNr = selectedPartListEntries.get(0).getAsId().getKLfdnr();
                    }
                    moduleInfo.getConnector().setCurrentPartListEntries(null);

                    // Sicherstellen, dass das Flag für currentAssemblyChanged gesetzt wird
                    moduleInfo.getConnector().setCurrentAssembly(null);
                    moduleInfo.getConnector().setCurrentAssembly(currentAssembly);

                    if (!tabbedPaneEntryEdit.guiCreated) {
                        createTabbedPaneEntryContent(tabbedPaneEntryEdit, ((iPartsDataAssembly)currentAssembly).getDocumentationType());
                    }
                    loadModuleInTab(tabbedPaneEntryEdit, currentAssembly, kLfdNr, moduleInfo.getConnector());
                }
            }
        }
    }

    public void resetPreparedAndLazyLoadForClose(Collection<EditModuleInfo> moduleInfos) {
        for (EditModuleForm.EditModuleInfo moduleInfo : moduleInfos) {
            GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(moduleInfo.getAssemblyId().getKVari());
            if (tabbedPaneEntryEdit != null) {
                tabbedPaneEntryEdit.setCancelled();
            }
        }
    }

    public void closeModule(EditModuleInfo moduleInfo) {
        if (moduleInfo.isValid()) {
            // index in EditModuleInfo ist nach dem ersten closeModule ungültig! Deshalb:
            GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(moduleInfo.getAssemblyId().getKVari());
            if (tabbedPaneEntryEdit != null) {
                if (moduleInfo.isModuleModified()) {
                    saveModule(moduleInfo);
                }
                tabbedPaneEntryEdit.removeFromParent();
            }
        }
    }

    public boolean closeModuleIfLoaded(String moduleNo) {
        if (isModuleLoaded(moduleNo)) {
            Collection<EditModuleInfo> editModuleInfos = getEditModuleInfoList(true);
            for (EditModuleInfo editModuleInfo : editModuleInfos) {
                if (editModuleInfo.getAssemblyId().getKVari().equals(moduleNo)) {
                    List<EditModuleInfo> helpModuleInfos = new DwList<>();
                    helpModuleInfos.add(editModuleInfo);
                    resetPreparedAndLazyLoadForClose(helpModuleInfos);
                    closeModule(editModuleInfo);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Selektiert den Tab für das übergebene Modul und liefert den dazugehörigen {@link GuiTabbedPaneEntryEdit} zurück
     * (falls das Modul im Edit bereits geöffnet ist).
     *
     * @param moduleNo
     * @return
     */
    public GuiTabbedPaneEntryEdit selectModuleTab(String moduleNo) {
        GuiTabbedPaneEntryEdit tabbedPaneEntryEdit = getActualTabbedPaneEntry(moduleNo);
        if (tabbedPaneEntryEdit != null) {
            editModulePanel.modulesTabbedPane.selectTab(tabbedPaneEntryEdit);
            return tabbedPaneEntryEdit;
        } else {
            return null;
        }
    }

    public boolean isModuleLoaded(String moduleNo) {
        return getActualTabbedPaneEntry(moduleNo) != null;
    }

    /**
     * Zeigt den übergebenen Bildauftrag im dazugehörigen Modul an.
     *
     * @param moduleNumber
     * @param picorderGUID
     */
    public void showPicOrderInActiveModule(String moduleNumber, String picorderGUID) {
        if (!StrUtils.isValid(moduleNumber, picorderGUID)) {
            return;
        }
        int index = searchLoadedModule(moduleNumber);
        if (index == -1) {
            loadModule(moduleNumber);
            final EditModuleForm finalEditModuleForm = this;
            Session.invokeThreadSafeInSession(() -> getConnector().getMainWindow().displayForm(finalEditModuleForm));
        } else {
            editModulePanel.modulesTabbedPane.selectTab(index);
        }
        iPartsDataPicOrder picOrder = new iPartsDataPicOrder(getProject(), new iPartsPicOrderId(picorderGUID));
        if (picOrder.existsInDB()) {
            GuiTabbedPaneEntryEdit actualTabbedPaneEntry = getActualTabbedPaneEntry();
            if (actualTabbedPaneEntry != null) {
                actualTabbedPaneEntry.editImageListForm.showPictureOrder(picOrder);
            }
        }
    }

    private void onLoadModule(Event event) {
        final VarParam<Boolean> newWasClicked = new VarParam<>(false);
        OnNewEvent onNewEvent = null;
        if (iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            onNewEvent = new OnNewEvent() {
                @Override
                public String onNewEvent(String startValue) {
                    newWasClicked.setValue(true);
                    return null;
                }

                @Override
                public String getButtonText() {
                    return TranslationHandler.translate("!!Modul anlegen");
                }
            };
        }
        SelectSearchGridModule selectSearchGridModule = new SelectSearchGridModule(this);
        selectSearchGridModule.setOnNewEvent(onNewEvent);
        if (MULTI_SELECT_SEARCH_RESULT) {
            searchWithMultiSelect(selectSearchGridModule, newWasClicked);
        } else {
            searchWithSingleSelect(selectSearchGridModule, newWasClicked);
        }
    }

    private void searchWithSingleSelect(SelectSearchGridModule selectSearchGridModule, VarParam<Boolean> newWasClicked) {
        String moduleNo = selectSearchGridModule.showGridSelectionDialog(null);
        if (!newWasClicked.getValue()) {
            loadModule(moduleNo);
        } else {
            createModule();
        }
    }

    private void searchWithMultiSelect(SelectSearchGridModule selectSearchGridModule, VarParam<Boolean> newWasClicked) {
        Set<String> moduleNoSet = selectSearchGridModule.showGridSelectionDialogMultiSelect(null);
        if (!newWasClicked.getValue()) {
            if (!moduleNoSet.isEmpty()) {
                boolean withFocus = true;  // das 1. Modul direkt laden
                for (String moduleNo : moduleNoSet) {
                    loadModule(moduleNo, "", withFocus, false);
                    withFocus = false;  // alle weiteren Module mit LazyLoading
                }
            }
        } else {
            createModule();
        }
    }

    public void onCreateSA(Event event) {
        if (!isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }
        // Dialog zur Auswahl einer SA die noch keinen TU hat anzeigen
        iPartsDataSa dataSa = SelectSAForm.showSASelectForCreateModule(getConnector(), this);
        if (dataSa != null) {
            // SA Benennung als TU Benennung übernehmen, falls vorhanden
            EtkMultiSprache saDesc = dataSa.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DS_DESC);
            if (saDesc.isEmpty()) {
                saDesc = null;
            }


            iPartsModuleId moduleId = new iPartsModuleId(iPartsConst.SA_MODULE_PREFIX + dataSa.getAsId().getSaNumber());
            AssemblyId assemblyId = new AssemblyId(moduleId.getModuleNumber(), "");
            iPartsDataModule dataModule = EditUserControlForCreateSA.showCreateSA(getConnector(), this, moduleId, assemblyId, dataSa.getAsId().getSaNumber());
            if (dataModule != null) {
                // neue Assembly erzeugen und speichern (inkl. Einträge in KATALOG, MAT, DA_MODULE, DA_SA_MODULES)
                boolean springFilter = dataModule.isSpringFilterRelevant();
                DCAggregateTypes aggTypeForSpecialZBFilter = dataModule.getAggTypeForSpecialZBFilter();
                iPartsDocumentationType docuType = dataModule.getDocuType();
                iPartsDataAssembly createdAssembly = EditModuleHelper.createAndSaveModuleWithSAAssignment(assemblyId,
                                                                                                          iPartsModuleTypes.SA_TU,
                                                                                                          saDesc,
                                                                                                          null,
                                                                                                          new iPartsSAModulesId(dataSa.getAsId().getSaNumber()),
                                                                                                          null,
                                                                                                          getProject(),
                                                                                                          docuType,
                                                                                                          iPartsImportDataOrigin.IPARTS,
                                                                                                          springFilter,
                                                                                                          aggTypeForSpecialZBFilter,
                                                                                                          true, null);

                // jetzt noch die restlichen Werte für DA_MODULE speichern
                iPartsDataModule moduleMetaData = createdAssembly.getModuleMetaData();
                moduleMetaData.setAttributes(dataModule.getAttributes(), DBActionOrigin.FROM_EDIT);
                addDataObjectToActiveChangeSetForEdit(moduleMetaData);

                // Event im EtkProject und Plug-ins reicht, da die Änderung ja nur im ChangeSet existiert
                getProject().fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.SA,
                                                                                 iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                 dataSa.getAsId(), false), true);

                // neu erzeugtes Modul laden
                loadModule(createdAssembly.getAsId().getKVari());
            }
        }
    }

    public void createModule() {
        if (!isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }
        SelectSearchGridProduct selectSearchGridProduct = new SelectSearchGridProduct(this);
        String productNo = selectSearchGridProduct.showGridSelectionDialog(null);
        if (!productNo.isEmpty()) {
            iPartsProductId productId = new iPartsProductId(productNo);

            if (!iPartsRight.checkProductEditableInSession(productId, iPartsRight.EDIT_PARTS_DATA, true, getProject())) {
                return;
            }

            //DAIMLER-490
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();

            EinPasId einPasId = null;
            KgTuListItem kgTuListItem = null;
            if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
                einPasId = EditEinPas2Dialog.showEinPasDialogWithSkip(getConnector(), this, null);
            } else {
                EditKGTUDialog.ProductKgTuSelection selection = EditKGTUDialog.showKgTuDialogWithSkipOption(getConnector(),
                                                                                                            this, productId,
                                                                                                            null, true,
                                                                                                            null, null);
                if (selection != null) {
                    kgTuListItem = selection.getKgTuListItem();
                }
            }

            iPartsDocumentationType documentationType = product.getDocumentationType();
            iPartsModuleTypes moduleType;
            if (product.isSpecialCatalog()) {
                moduleType = iPartsModuleTypes.WorkshopMaterial;
            } else {
                moduleType = documentationType.getModuleType(false);
            }
//            iPartsModuleTypes moduleType = documentationType.getModuleType(false);
            // Bei einer ungültigen Doku-Methode und/oder einem ungültigen Modultyp oder bei PSK-Produkten ist die Doku-Methode
            // auswählbar.
            boolean selectModuleType = (moduleType == iPartsModuleTypes.UNKNOWN) || product.isPSK();
            EditModulCreateForm createModulForm = new EditModulCreateForm(getConnector(), this, productId, einPasId,
                                                                          kgTuListItem, selectModuleType);
            createModulForm.setTitle("!!Modul anlegen");
            if (moduleType != iPartsModuleTypes.UNKNOWN) {
                createModulForm.setSelectedModuleType(moduleType);
            }
            ModalResult mr = createModulForm.showModal();
            if (mr == ModalResult.OK) {
                // War der Modultyp ungültig, musste der Benutzer einen Modultyp angeben -> Holen den ausgewählten Modultyp
                if (selectModuleType) {
                    moduleType = createModulForm.getModuleType();
                    documentationType = moduleType.getDefaultDocumentationType();

                    // Korrekte Edit-Dokumethoden setzen für PKW/VAN und TRUCK, da getDefaultDocumentationType() die Migrations-Dokumethoden
                    // zurückliefert für DIALOG und EDS (BCS_PLUS)
                    if (documentationType == iPartsDocumentationType.DIALOG) {
                        documentationType = iPartsDocumentationType.DIALOG_IPARTS;
                    } else if (documentationType == iPartsDocumentationType.BCS_PLUS) {
                        documentationType = iPartsDocumentationType.BCS_PLUS_GLOBAL_BM;
                    }
                }
                AssemblyId newAssemblyId = new AssemblyId(createModulForm.getModuleNumber(),
                                                          createModulForm.getModuleVersion());
                EtkDataAssembly newAssembly = generateNewModule(getProject(), newAssemblyId, createModulForm.getType(), moduleType, createModulForm.getModuleName(),
                                                                createModulForm.getProductId(), createModulForm.getEinPasId(), createModulForm.getKgTuId(),
                                                                documentationType, createModulForm.getKgTuNode());
                if (newAssembly != null) {
                    // neu erzeugtes Modul laden
                    loadModule(newAssembly.getAsId().getKVari());
                }
            }
        }
    }

    public static iPartsDataAssembly generateNewCarPerspectiveModule(EtkProject project, iPartsProductId productId) {
        AssemblyId assemblyId = EditModuleHelper.buildCarPerspectiveId(productId);
        return generateNewModule(project, assemblyId, iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU,
                                 null, null, productId,
                                 null, null, null, null);
    }

    public static iPartsDataAssembly generateNewModule(EtkProject project, AssemblyId newAssemblyId, iPartsConst.PRODUCT_STRUCTURING_TYPE type,
                                                       iPartsModuleTypes moduleType, EtkMultiSprache moduleName, iPartsProductId productId,
                                                       EinPasId einPasId, KgTuId kgTuId, iPartsDocumentationType documentationType,
                                                       KgTuListItem kgTuListItem) {

        // Modul anlegen
        iPartsDataAssembly newAssembly = null;
        if (type == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
            newAssembly = EditModuleHelper.createAndSaveModuleWithEinPASAssignment(newAssemblyId,
                                                                                   moduleType,
                                                                                   moduleName,
                                                                                   productId,
                                                                                   einPasId,
                                                                                   project,
                                                                                   documentationType,
                                                                                   true, null);
        } else if (type == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
            if (moduleType == null) {
                newAssembly = EditModuleHelper.createAndSaveModuleWithCarPerspectiveAssignment(newAssemblyId,
                                                                                               productId,
                                                                                               project,
                                                                                               true, null);
            } else if (moduleType != iPartsModuleTypes.UNKNOWN) {
                newAssembly = EditModuleHelper.createAndSaveModuleWithKgTuAssignment(newAssemblyId,
                                                                                     moduleType,
                                                                                     moduleName,
                                                                                     productId,
                                                                                     kgTuId,
                                                                                     project,
                                                                                     documentationType,
                                                                                     true, null);
            }
            if (kgTuListItem != null) { // null wenn KGTU-Zuweisung übersprungen
                kgTuListItem.saveToDB(project, productId.getProductNumber());
            }
        }
        if (newAssembly != null) {
            if (project.getDbLayer().isRevisionChangeSetActiveForEdit()) {
                // Bei aktivem ChangeSet muss kein CacheHelper.invalidateCaches() aufgerufen werden, sondern das
                // Entfernen des betroffenen Produkts aus dem Cache und ein paar nachfolgende Events reichen
                iPartsProduct.removeProductFromCache(project, productId);
                KgTuForProduct.removeKgTuForProductFromCache(project, productId);
                if (iPartsConst.ONLY_SINGLE_MODULE_PER_KGTU) {
                    iPartsDataReservedPKList.reservePrimaryKey(project, newAssemblyId);
                }
                project.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                            iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                            productId,
                                                                            false), true);
                project.fireProjectEvent(new iPartsDataChangedEventByEdit<AssemblyId>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                      iPartsDataChangedEventByEdit.Action.NEW,
                                                                                      newAssembly.getAsId(),
                                                                                      false), true);
                project.fireProjectEvent(new DataChangedEvent(null), true);
            } else {
                ClearRetailRelevantCachesEvent.invalidateRetailRelevantCaches(false, false);
            }
            return newAssembly;
        } else {
            MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Anlegen vom Modul \"%1\"!", newAssemblyId.getKVari()));
        }
        return null;
    }

    private void onCreateModule(Event event) {
        createModule();
    }

    /**
     * Springt im aktuellen Modul-Tab zum angegebenen {@link EtkDataPartListEntry}.
     *
     * @param partListEntry
     */
    public void gotoPartListEntry(EtkDataPartListEntry partListEntry) {
        GuiTabbedPaneEntryEdit tabbedPaneEntry = getActualTabbedPaneEntry();
        if (tabbedPaneEntry != null) {
            if (tabbedPaneEntry.editAssemblyListForm != null) {
                tabbedPaneEntry.editAssemblyListForm.gotoPartListEntry(partListEntry);
            }
        }
    }

    /**
     * Liefert die Liste {@link EditModuleInfo} der im Editor geöffneten Module
     *
     * @param all true: alle Module; false: nur Module, die editierbar sind oder mit aktivem Autoren-Auftrag geöffnet wurden
     * @return
     */
    public Collection<EditModuleInfo> getEditModuleInfoList(boolean all) {
        List<EditModuleInfo> result = new DwList<>();
        for (int index = 0; index < (editModulePanel.modulesTabbedPane.getChildren().size() - 1); index++) { // letzter Eintrag ist '+'
            GuiTabbedPaneEntry tabPaneEntry = (GuiTabbedPaneEntry)editModulePanel.modulesTabbedPane.getChildren().get(index);
            if (tabPaneEntry instanceof GuiTabbedPaneEntryEdit) {
                GuiTabbedPaneEntryEdit tabPaneEntryEdit = (GuiTabbedPaneEntryEdit)tabPaneEntry;
                EditModuleInfo moduleInfo = null;
                EditModuleFormConnector connector = getEditConnector(tabPaneEntryEdit);
                if (connector == null) {
                    if (tabPaneEntryEdit.isLazyLoading()) {
                        connector = tabPaneEntryEdit.lazyLoadInfo.getConnector();
                        if (connector != null) {
                            boolean isEditAllowed = connector.isAuthorOrderValid();
                            boolean isRevisionChangeSetActive = isRevisionChangeSetActive();
                            if (all || isEditAllowed || isRevisionChangeSetActive) {
                                moduleInfo = new EditModuleInfo(connector, isEditAllowed);
                                moduleInfo.assemblyId = tabPaneEntryEdit.lazyLoadInfo.getAssemblyId();
                                moduleInfo.isModuleModified = false;
                            }
                        }
                    } else if (tabPaneEntryEdit.isPreparedForLoading()) {
                        connector = tabPaneEntryEdit.preparedForLoadInfo.getConnector();
                        if (connector != null) {
                            boolean isEditAllowed = connector.isAuthorOrderValid();
                            boolean isRevisionChangeSetActive = isRevisionChangeSetActive();
                            if (all || isEditAllowed || isRevisionChangeSetActive) {
                                moduleInfo = new EditModuleInfo(connector, isEditAllowed);
                                moduleInfo.assemblyId = tabPaneEntryEdit.preparedForLoadInfo.getAssemblyId();
                                moduleInfo.isModuleModified = false;
                            }
                        }
                    }
                } else {
                    boolean isEditAllowed = tabPaneEntryEdit.editHeaderForm.isEditAllowed();
                    if (all || isEditAllowed || tabPaneEntryEdit.editHeaderForm.isAuthorOrderActive()) {
                        moduleInfo = new EditModuleInfo(connector, isEditAllowed);
                    }
                }
                if ((moduleInfo != null) && moduleInfo.isValid()) {
                    result.add(moduleInfo);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static class EditModuleInfo {

        private AssemblyId assemblyId;
        private EditModuleFormConnector connector;
        private boolean isModuleModified;
        private boolean isReadOnly;

        public EditModuleInfo(EditModuleFormConnector connector, boolean isEditAllowed) {
            if (connector != null) {
                this.connector = connector;
                this.assemblyId = connector.getCurrentAssembly().getAsId();
                this.isModuleModified = connector.getCurrentAssembly().isModifiedWithChildren();
                this.isReadOnly = !isEditAllowed;
            }
        }

        public boolean isValid() {
            return (connector != null) && (assemblyId != null);
        }

        public AssemblyId getAssemblyId() {
            return assemblyId;
        }

        public EditModuleFormConnector getConnector() {
            return connector;
        }

        public boolean isModuleModified() {
            return isModuleModified;
        }

        public boolean isReadOnly() {
            return isReadOnly;
        }
    }

    private static class LazyLoadEditModuleInfo extends EditModuleInfo {

        private String lazyKLfdNr;
        private EtkDataAssembly lazyAssembly;
        private boolean isCancelled;

        public LazyLoadEditModuleInfo(EditModuleFormConnector connector, EtkDataAssembly assembly, String kLfdNr) {
            super(connector, false);
            this.lazyAssembly = assembly;
            this.lazyKLfdNr = kLfdNr;
            this.isCancelled = false;
        }

        public AssemblyId getAssemblyId() {
            if (lazyAssembly != null) {
                return lazyAssembly.getAsId();
            }
            return null;
        }

        public void setCancelled() {
            isCancelled = true;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        editModulePanel = new EditModulePanelClass(translationHandler);
        editModulePanel.__internal_setGenerationDpi(96);
        panelTabbed = new PanelTabbedClass(translationHandler);
        panelTabbed.__internal_setGenerationDpi(96);
        panelTabbedTwo = new PanelTabbedTwoClass(translationHandler);
        panelTabbedTwo.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected EditModulePanelClass editModulePanel;

    private class EditModulePanelClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPane modulesTabbedPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry loadModuleTabEntry;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel loadModuleTabEntryContent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton loadModuleButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton createModulButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton createSAButton;

        private EditModulePanelClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(100);
            this.setMinimumHeight(100);
            this.setTitle("Module");
            de.docware.framework.modules.gui.layout.LayoutGridBag editModulePanelLayout =
                new de.docware.framework.modules.gui.layout.LayoutGridBag();
            this.setLayout(editModulePanelLayout);
            modulesTabbedPane = new de.docware.framework.modules.gui.controls.GuiTabbedPane(GuiTabbedPane.ChildrenHaveCloseIcons.INDIVIDUAL);
            modulesTabbedPane.setName("modulesTabbedPane");
            modulesTabbedPane.__internal_setGenerationDpi(96);
            modulesTabbedPane.registerTranslationHandler(translationHandler);
            modulesTabbedPane.setScaleForResolution(true);
            modulesTabbedPane.setMinimumWidth(10);
            modulesTabbedPane.setMinimumHeight(10);
            loadModuleTabEntry = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            loadModuleTabEntry.setName("loadModuleTabEntry");
            loadModuleTabEntry.__internal_setGenerationDpi(96);
            loadModuleTabEntry.registerTranslationHandler(translationHandler);
            loadModuleTabEntry.setScaleForResolution(true);
            loadModuleTabEntry.setMinimumWidth(10);
            loadModuleTabEntry.setMinimumHeight(10);
            loadModuleTabEntry.setTitle("+ ");
            loadModuleTabEntryContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            loadModuleTabEntryContent.setName("loadModuleTabEntryContent");
            loadModuleTabEntryContent.__internal_setGenerationDpi(96);
            loadModuleTabEntryContent.registerTranslationHandler(translationHandler);
            loadModuleTabEntryContent.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag loadModuleTabEntryContentLayout =
                new de.docware.framework.modules.gui.layout.LayoutGridBag();
            loadModuleTabEntryContentLayout.setCentered(false);
            loadModuleTabEntryContent.setLayout(loadModuleTabEntryContentLayout);
            loadModuleButton = new de.docware.framework.modules.gui.controls.GuiButton();
            loadModuleButton.setName("loadModuleButton");
            loadModuleButton.__internal_setGenerationDpi(96);
            loadModuleButton.registerTranslationHandler(translationHandler);
            loadModuleButton.setScaleForResolution(true);
            loadModuleButton.setMinimumWidth(100);
            loadModuleButton.setMinimumHeight(10);
            loadModuleButton.setMnemonicEnabled(true);
            loadModuleButton.setText("!!Modul laden");
            loadModuleButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onLoadModule(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag loadModuleButtonConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "h", 8, 8, 8, 8);
            loadModuleButton.setConstraints(loadModuleButtonConstraints);
            loadModuleTabEntryContent.addChild(loadModuleButton);
            createModulButton = new de.docware.framework.modules.gui.controls.GuiButton();
            createModulButton.setName("createModulButton");
            createModulButton.__internal_setGenerationDpi(96);
            createModulButton.registerTranslationHandler(translationHandler);
            createModulButton.setScaleForResolution(true);
            createModulButton.setMinimumWidth(100);
            createModulButton.setMinimumHeight(10);
            createModulButton.setMnemonicEnabled(true);
            createModulButton.setText("!!Modul anlegen");
            createModulButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onCreateModule(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag createModulButtonConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "h", 8, 8, 8, 8);
            createModulButton.setConstraints(createModulButtonConstraints);
            loadModuleTabEntryContent.addChild(createModulButton);
            createSAButton = new de.docware.framework.modules.gui.controls.GuiButton();
            createSAButton.setName("createSAButton");
            createSAButton.__internal_setGenerationDpi(96);
            createSAButton.registerTranslationHandler(translationHandler);
            createSAButton.setScaleForResolution(true);
            createSAButton.setMinimumWidth(100);
            createSAButton.setMinimumHeight(10);
            createSAButton.setMnemonicEnabled(true);
            createSAButton.setText("!!Freie SA anlegen");
            createSAButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onCreateSA(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag createSAButtonConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "h", 8, 8, 8, 8);
            createSAButton.setConstraints(createSAButtonConstraints);
            loadModuleTabEntryContent.addChild(createSAButton);
            loadModuleTabEntry.addChild(loadModuleTabEntryContent);
            modulesTabbedPane.addChild(loadModuleTabEntry);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag modulesTabbedPaneConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            modulesTabbedPane.setConstraints(modulesTabbedPaneConstraints);
            this.addChild(modulesTabbedPane);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelTabbedClass panelTabbed;

    private class PanelTabbedClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelHeader;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel headerLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separatorHeader;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCenter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane moduleSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel imagePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel imageLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel partListPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel partListLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separatorFooter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelFooter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel footerLabel;

        private PanelTabbedClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTabbedLayout =
                new de.docware.framework.modules.gui.layout.LayoutGridBag();
            this.setLayout(panelTabbedLayout);
            panelHeader = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelHeader.setName("panelHeader");
            panelHeader.__internal_setGenerationDpi(96);
            panelHeader.registerTranslationHandler(translationHandler);
            panelHeader.setScaleForResolution(true);
            panelHeader.setMinimumWidth(10);
            panelHeader.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelHeaderLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelHeader.setLayout(panelHeaderLayout);
            headerLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            headerLabel.setName("headerLabel");
            headerLabel.__internal_setGenerationDpi(96);
            headerLabel.registerTranslationHandler(translationHandler);
            headerLabel.setScaleForResolution(true);
            headerLabel.setMinimumWidth(10);
            headerLabel.setMinimumHeight(10);
            headerLabel.setText("Platzhalter für den Header");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder headerLabelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            headerLabelConstraints.setPosition("west");
            headerLabel.setConstraints(headerLabelConstraints);
            panelHeader.addChild(headerLabel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelHeaderConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 0, 8, 8, 8);
            panelHeader.setConstraints(panelHeaderConstraints);
            this.addChild(panelHeader);
            separatorHeader = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorHeader.setName("separatorHeader");
            separatorHeader.__internal_setGenerationDpi(96);
            separatorHeader.registerTranslationHandler(translationHandler);
            separatorHeader.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separatorHeaderConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            separatorHeader.setConstraints(separatorHeaderConstraints);
            this.addChild(separatorHeader);
            panelCenter = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCenter.setName("panelCenter");
            panelCenter.__internal_setGenerationDpi(96);
            panelCenter.registerTranslationHandler(translationHandler);
            panelCenter.setScaleForResolution(true);
            panelCenter.setMinimumWidth(10);
            panelCenter.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCenterLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCenter.setLayout(panelCenterLayout);
            moduleSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            moduleSplitPane.setName("moduleSplitPane");
            moduleSplitPane.__internal_setGenerationDpi(96);
            moduleSplitPane.registerTranslationHandler(translationHandler);
            moduleSplitPane.setScaleForResolution(true);
            moduleSplitPane.setMinimumWidth(10);
            moduleSplitPane.setMinimumHeight(100);
            moduleSplitPane.setHorizontal(false);
            moduleSplitPane.setDividerPosition(100);
            moduleSplitPane.setDividerSize(10);
            imagePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            imagePanel.setName("imagePanel");
            imagePanel.__internal_setGenerationDpi(96);
            imagePanel.registerTranslationHandler(translationHandler);
            imagePanel.setScaleForResolution(true);
            imagePanel.setMinimumWidth(0);
            imagePanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder imagePanelLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            imagePanel.setLayout(imagePanelLayout);
            imageLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            imageLabel.setName("imageLabel");
            imageLabel.__internal_setGenerationDpi(96);
            imageLabel.registerTranslationHandler(translationHandler);
            imageLabel.setScaleForResolution(true);
            imageLabel.setMinimumWidth(10);
            imageLabel.setMinimumHeight(10);
            imageLabel.setVerticalAlignment(de.docware.framework.modules.gui.controls.AbstractVerticalAlignmentControl.VerticalAlignment.TOP);
            imageLabel.setText("Platzhalter für das Bild");
            imageLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder imageLabelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            imageLabel.setConstraints(imageLabelConstraints);
            imagePanel.addChild(imageLabel);
            moduleSplitPane.addChild(imagePanel);
            partListPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            partListPanel.setName("partListPanel");
            partListPanel.__internal_setGenerationDpi(96);
            partListPanel.registerTranslationHandler(translationHandler);
            partListPanel.setScaleForResolution(true);
            partListPanel.setMinimumWidth(0);
            partListPanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder partListPanelLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            partListPanel.setLayout(partListPanelLayout);
            partListLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            partListLabel.setName("partListLabel");
            partListLabel.__internal_setGenerationDpi(96);
            partListLabel.registerTranslationHandler(translationHandler);
            partListLabel.setScaleForResolution(true);
            partListLabel.setMinimumWidth(10);
            partListLabel.setMinimumHeight(10);
            partListLabel.setVerticalAlignment(de.docware.framework.modules.gui.controls.AbstractVerticalAlignmentControl.VerticalAlignment.TOP);
            partListLabel.setText("Platzhalter für die Stückliste");
            partListLabel.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder partListLabelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            partListLabel.setConstraints(partListLabelConstraints);
            partListPanel.addChild(partListLabel);
            moduleSplitPane.addChild(partListPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder moduleSplitPaneConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            moduleSplitPane.setConstraints(moduleSplitPaneConstraints);
            panelCenter.addChild(moduleSplitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelCenterConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelCenter.setConstraints(panelCenterConstraints);
            this.addChild(panelCenter);
            separatorFooter = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separatorFooter.setName("separatorFooter");
            separatorFooter.__internal_setGenerationDpi(96);
            separatorFooter.registerTranslationHandler(translationHandler);
            separatorFooter.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separatorFooterConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            separatorFooter.setConstraints(separatorFooterConstraints);
            this.addChild(separatorFooter);
            panelFooter = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelFooter.setName("panelFooter");
            panelFooter.__internal_setGenerationDpi(96);
            panelFooter.registerTranslationHandler(translationHandler);
            panelFooter.setScaleForResolution(true);
            panelFooter.setMinimumWidth(10);
            panelFooter.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelFooterLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelFooter.setLayout(panelFooterLayout);
            footerLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            footerLabel.setName("footerLabel");
            footerLabel.__internal_setGenerationDpi(96);
            footerLabel.registerTranslationHandler(translationHandler);
            footerLabel.setScaleForResolution(true);
            footerLabel.setMinimumWidth(10);
            footerLabel.setMinimumHeight(10);
            footerLabel.setText("Platzhalter für den Footer");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder footerLabelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            footerLabelConstraints.setPosition("west");
            footerLabel.setConstraints(footerLabelConstraints);
            panelFooter.addChild(footerLabel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelFooterConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 100.0, 0.0, "s", "h", 8, 8, 8, 8);
            panelFooter.setConstraints(panelFooterConstraints);
            this.addChild(panelFooter);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelTabbedTwoClass panelTabbedTwo;

    private class PanelTabbedTwoClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneMaster;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMaster_firstChild_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelHeader_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_Header_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCenter_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane module_Splitpane_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel imageDockingpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel imagePanel_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel partlistDockingpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel partListPanel_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanelMaster_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedpaneExtras;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryMaterial;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryMaterialContent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryModule;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryModuleContent;

        private PanelTabbedTwoClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTabbedTwoLayout =
                new de.docware.framework.modules.gui.layout.LayoutGridBag();
            this.setLayout(panelTabbedTwoLayout);
            splitpaneMaster = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneMaster.setName("splitpaneMaster");
            splitpaneMaster.__internal_setGenerationDpi(96);
            splitpaneMaster.registerTranslationHandler(translationHandler);
            splitpaneMaster.setScaleForResolution(true);
            splitpaneMaster.setMinimumWidth(10);
            splitpaneMaster.setMinimumHeight(10);
            splitpaneMaster.setDividerPosition(755);
            splitpaneMaster.setDividerSize(10);
            splitpaneMaster_firstChild_1 = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMaster_firstChild_1.setName("splitpaneMaster_firstChild_1");
            splitpaneMaster_firstChild_1.__internal_setGenerationDpi(96);
            splitpaneMaster_firstChild_1.registerTranslationHandler(translationHandler);
            splitpaneMaster_firstChild_1.setScaleForResolution(true);
            splitpaneMaster_firstChild_1.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutGridBag splitpaneMaster_firstChild_1Layout =
                new de.docware.framework.modules.gui.layout.LayoutGridBag();
            splitpaneMaster_firstChild_1.setLayout(splitpaneMaster_firstChild_1Layout);
            panelHeader_2 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelHeader_2.setName("panelHeader_2");
            panelHeader_2.__internal_setGenerationDpi(96);
            panelHeader_2.registerTranslationHandler(translationHandler);
            panelHeader_2.setScaleForResolution(true);
            panelHeader_2.setMinimumWidth(10);
            panelHeader_2.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelHeader_2Layout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelHeader_2.setLayout(panelHeader_2Layout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelHeader_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 0, 0, 0, 0);
            panelHeader_2.setConstraints(panelHeader_2Constraints);
            splitpaneMaster_firstChild_1.addChild(panelHeader_2);
            separator_Header_2 = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_Header_2.setName("separator_Header_2");
            separator_Header_2.__internal_setGenerationDpi(96);
            separator_Header_2.registerTranslationHandler(translationHandler);
            separator_Header_2.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_Header_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_Header_2.setConstraints(separator_Header_2Constraints);
            splitpaneMaster_firstChild_1.addChild(separator_Header_2);
            panelCenter_2 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCenter_2.setName("panelCenter_2");
            panelCenter_2.__internal_setGenerationDpi(96);
            panelCenter_2.registerTranslationHandler(translationHandler);
            panelCenter_2.setScaleForResolution(true);
            panelCenter_2.setMinimumWidth(10);
            panelCenter_2.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCenter_2Layout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCenter_2.setLayout(panelCenter_2Layout);
            module_Splitpane_2 = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            module_Splitpane_2.setName("module_Splitpane_2");
            module_Splitpane_2.__internal_setGenerationDpi(96);
            module_Splitpane_2.registerTranslationHandler(translationHandler);
            module_Splitpane_2.setScaleForResolution(true);
            module_Splitpane_2.setMinimumWidth(10);
            module_Splitpane_2.setMinimumHeight(10);
            module_Splitpane_2.setHorizontal(false);
            module_Splitpane_2.setDividerPosition(133);
            module_Splitpane_2.setDividerSize(10);
            imageDockingpanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            imageDockingpanel.setName("imageDockingpanel");
            imageDockingpanel.__internal_setGenerationDpi(96);
            imageDockingpanel.registerTranslationHandler(translationHandler);
            imageDockingpanel.setScaleForResolution(true);
            imageDockingpanel.setMinimumWidth(10);
            imageDockingpanel.setMinimumHeight(10);
            imageDockingpanel.setBackgroundColor(new java.awt.Color(255, 255, 255, 255));
            imageDockingpanel.setForegroundColor(new java.awt.Color(0, 0, 0, 255));
            imageDockingpanel.setTextHide("!!Bildtafeln");
            imageDockingpanel.setTextShow("!!Bildtafeln anzeigen");
            imageDockingpanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            imageDockingpanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            imageDockingpanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            imageDockingpanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            imageDockingpanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            imageDockingpanel.setButtonFill(true);
            imageDockingpanel.setStartWithArrow(false);
            imagePanel_2 = new de.docware.framework.modules.gui.controls.GuiPanel();
            imagePanel_2.setName("imagePanel_2");
            imagePanel_2.__internal_setGenerationDpi(96);
            imagePanel_2.registerTranslationHandler(translationHandler);
            imagePanel_2.setScaleForResolution(true);
            imagePanel_2.setMinimumWidth(10);
            imagePanel_2.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder imagePanel_2Layout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            imagePanel_2.setLayout(imagePanel_2Layout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder imagePanel_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            imagePanel_2.setConstraints(imagePanel_2Constraints);
            imageDockingpanel.addChild(imagePanel_2);
            module_Splitpane_2.addChild(imageDockingpanel);
            partlistDockingpanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            partlistDockingpanel.setName("partlistDockingpanel");
            partlistDockingpanel.__internal_setGenerationDpi(96);
            partlistDockingpanel.registerTranslationHandler(translationHandler);
            partlistDockingpanel.setScaleForResolution(true);
            partlistDockingpanel.setMinimumWidth(10);
            partlistDockingpanel.setMinimumHeight(18);
            partlistDockingpanel.setBackgroundColor(new java.awt.Color(255, 255, 255, 255));
            partlistDockingpanel.setForegroundColor(new java.awt.Color(0, 0, 0, 255));
            partlistDockingpanel.setTextHide("!!Stückliste");
            partlistDockingpanel.setTextShow("!!Stückliste anzeigen");
            partlistDockingpanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            partlistDockingpanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            partlistDockingpanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            partlistDockingpanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            partlistDockingpanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            partlistDockingpanel.setButtonFill(true);
            partlistDockingpanel.setStartWithArrow(false);
            partListPanel_2 = new de.docware.framework.modules.gui.controls.GuiPanel();
            partListPanel_2.setName("partListPanel_2");
            partListPanel_2.__internal_setGenerationDpi(96);
            partListPanel_2.registerTranslationHandler(translationHandler);
            partListPanel_2.setScaleForResolution(true);
            partListPanel_2.setMinimumWidth(10);
            partListPanel_2.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder partListPanel_2Layout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            partListPanel_2.setLayout(partListPanel_2Layout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder partListPanel_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            partListPanel_2.setConstraints(partListPanel_2Constraints);
            partlistDockingpanel.addChild(partListPanel_2);
            module_Splitpane_2.addChild(partlistDockingpanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder module_Splitpane_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            module_Splitpane_2.setConstraints(module_Splitpane_2Constraints);
            panelCenter_2.addChild(module_Splitpane_2);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelCenter_2Constraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelCenter_2.setConstraints(panelCenter_2Constraints);
            splitpaneMaster_firstChild_1.addChild(panelCenter_2);
            splitpaneMaster.addChild(splitpaneMaster_firstChild_1);
            dockingpanelMaster_secondChild = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanelMaster_secondChild.setName("dockingpanelMaster_secondChild");
            dockingpanelMaster_secondChild.__internal_setGenerationDpi(96);
            dockingpanelMaster_secondChild.registerTranslationHandler(translationHandler);
            dockingpanelMaster_secondChild.setScaleForResolution(true);
            dockingpanelMaster_secondChild.setMinimumWidth(10);
            dockingpanelMaster_secondChild.setMinimumHeight(18);
            dockingpanelMaster_secondChild.setBackgroundColor(new java.awt.Color(255, 255, 255, 255));
            dockingpanelMaster_secondChild.setForegroundColor(new java.awt.Color(0, 0, 0, 255));
            dockingpanelMaster_secondChild.setTextHide("!!Extras");
            dockingpanelMaster_secondChild.setTextShow("!!Extras anzeigen");
            dockingpanelMaster_secondChild.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            dockingpanelMaster_secondChild.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            dockingpanelMaster_secondChild.setButtonFill(true);
            tabbedpaneExtras = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedpaneExtras.setName("tabbedpaneExtras");
            tabbedpaneExtras.__internal_setGenerationDpi(96);
            tabbedpaneExtras.registerTranslationHandler(translationHandler);
            tabbedpaneExtras.setScaleForResolution(true);
            tabbedpaneExtras.setMinimumWidth(0);
            tabbedpaneExtras.setMinimumHeight(10);
            tabbedpaneentryMaterial = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryMaterial.setName("tabbedpaneentryMaterial");
            tabbedpaneentryMaterial.__internal_setGenerationDpi(96);
            tabbedpaneentryMaterial.registerTranslationHandler(translationHandler);
            tabbedpaneentryMaterial.setScaleForResolution(true);
            tabbedpaneentryMaterial.setMinimumWidth(10);
            tabbedpaneentryMaterial.setMinimumHeight(10);
            tabbedpaneentryMaterial.setTitle("!!Auswahl Material");
            tabbedpaneentryMaterialContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryMaterialContent.setName("tabbedpaneentryMaterialContent");
            tabbedpaneentryMaterialContent.__internal_setGenerationDpi(96);
            tabbedpaneentryMaterialContent.registerTranslationHandler(translationHandler);
            tabbedpaneentryMaterialContent.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryMaterialContentLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryMaterialContent.setLayout(tabbedpaneentryMaterialContentLayout);
            tabbedpaneentryMaterial.addChild(tabbedpaneentryMaterialContent);
            tabbedpaneExtras.addChild(tabbedpaneentryMaterial);
            tabbedpaneentryModule = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryModule.setName("tabbedpaneentryModule");
            tabbedpaneentryModule.__internal_setGenerationDpi(96);
            tabbedpaneentryModule.registerTranslationHandler(translationHandler);
            tabbedpaneentryModule.setScaleForResolution(true);
            tabbedpaneentryModule.setMinimumWidth(10);
            tabbedpaneentryModule.setMinimumHeight(10);
            tabbedpaneentryModule.setTitle("!!Auswahl Modul");
            tabbedpaneentryModuleContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryModuleContent.setName("tabbedpaneentryModuleContent");
            tabbedpaneentryModuleContent.__internal_setGenerationDpi(96);
            tabbedpaneentryModuleContent.registerTranslationHandler(translationHandler);
            tabbedpaneentryModuleContent.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryModuleContentLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryModuleContent.setLayout(tabbedpaneentryModuleContentLayout);
            tabbedpaneentryModule.addChild(tabbedpaneentryModuleContent);
            tabbedpaneExtras.addChild(tabbedpaneentryModule);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedpaneExtrasConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedpaneExtras.setConstraints(tabbedpaneExtrasConstraints);
            dockingpanelMaster_secondChild.addChild(tabbedpaneExtras);
            splitpaneMaster.addChild(dockingpanelMaster_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitpaneMasterConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitpaneMaster.setConstraints(splitpaneMasterConstraints);
            this.addChild(splitpaneMaster);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}