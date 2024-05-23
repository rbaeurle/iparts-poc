/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokAnnotationList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemark;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataNutzDokRemarkList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.MultipleInputToOutputStream;
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.controls.menu.GuiMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.io.IOException;
import java.util.*;

/**
 * HilfsKlasse für alle WorkBaskets, die NutzDok_Remark als virtuelles Feld und UpLoad-Menu einblenden
 */
public class WorkBasketNutzDokRemarkHelper {

    public static final String NUTZDOK_ANNOTATION_MENU_TEXT = "!!Anzeige Nutzdok-Bemerkungen";

    public static final String DELIMITER_FOR_PKVALUES = "|";
    private static final int MAX_REMARKS_ELEMENTS_FOR_SUB_MENUS = 1;

    private AbstractJavaViewerForm parent;        // benötigt für UploadDialog
    private EtkProject project;               // nie verkehrt
    private AbstractJavaViewerFormIConnector dataConnector;
    private iPartsWSWorkBasketItem.TYPE type; // TYPE des Helpers (KEM oder SAA)
    // baut damit den Remark-Cache gleich im Callback von searchSortAndFill() auf
    private Set<String> remarkRefIdSet;       // Set um doppelte Remark-Einträge zu bestimmen
    protected Map<String, iPartsDataNutzDokRemarkList> nutzDokRemarkMap;  // NutzDok_Remark-Cache
    protected Map<String, iPartsDataNutzDokAnnotationList> nutzDokAnnotationMap;  // NutzDok_Annotation-Cache

    public WorkBasketNutzDokRemarkHelper(AbstractJavaViewerForm parent, EtkProject project, iPartsWSWorkBasketItem.TYPE type) {
        this.parent = parent;
        if (parent != null) {
            this.dataConnector = parent.getConnector();
        }
        this.project = project;
        this.type = type;
        this.remarkRefIdSet = Collections.synchronizedSet(new HashSet<>());
        this.nutzDokRemarkMap = Collections.synchronizedMap(new HashMap<>());
        this.nutzDokAnnotationMap = Collections.synchronizedMap(new HashMap<>());
    }

    public EtkProject getProject() {
        return project;
    }

    public AbstractJavaViewerForm getParent() {
        return parent;
    }

    public iPartsWSWorkBasketItem.TYPE getType() {
        return type;
    }

    public String getTypeDbValue() {
        if (type != null) {
            return type.name();
        }
        return "";
    }

    /**
     * Set und Cache zurücksetzen
     */
    public void clear() {
        remarkRefIdSet.clear();
        nutzDokRemarkMap.clear();
        nutzDokAnnotationMap.clear();
    }

    /**
     * Holt den Wert von fieldName aus den Attributen und prüft vorher, ob das Attribute existiert
     *
     * @param attributes
     * @param fieldName
     * @return
     */
    public String getFieldValueWithCheck(DBDataObjectAttributes attributes, String fieldName) {
        DBDataObjectAttribute attribute = attributes.getField(fieldName, false);
        if (attribute != null) {
            return attribute.getAsString();
        }
        return null;
    }

    /**
     * existiert die KEM/SAA Nummer in der NutzDok Remark Tabelle
     * (ohne Join, sondern durch Abfrage und Aufbau eines Caches)
     *
     * @param remarkRefId
     * @return
     */
    public boolean isNutzDokRemarkRefIdAvailabe(String remarkRefId) {
        iPartsDataNutzDokRemarkList dataNutzDokRemarkList = getNutzDokRemarkList(remarkRefId, getType().name());
        iPartsDataNutzDokAnnotationList dataNutzDokAnnotationList = getNutzDokAnnotationList(remarkRefId, getType().name());
        return !dataNutzDokRemarkList.isEmpty() || !dataNutzDokAnnotationList.isEmpty();
    }

    /**
     * Liefert zu refId und refType die Liste der Remark-Einträge
     *
     * @param refId
     * @param refType
     * @return
     */
    public iPartsDataNutzDokRemarkList getNutzDokRemarkList(String refId, String refType) {
        String remarkKey = buildRemarkKey(refId, refType);
        iPartsDataNutzDokRemarkList remarkList = nutzDokRemarkMap.get(remarkKey);
        if (remarkList == null) {
            remarkList = iPartsDataNutzDokRemarkList.loadRemarksForReferenceFromDB(getProject(), refId, refType);
            nutzDokRemarkMap.put(remarkKey, remarkList);
        }
        return remarkList;
    }

    public iPartsDataNutzDokAnnotationList getNutzDokAnnotationList(String refId, String refType) {
        String remarkKey = buildRemarkKey(refId, refType);
        iPartsDataNutzDokAnnotationList annotationList = nutzDokAnnotationMap.get(remarkKey);
        if (annotationList == null) {
            annotationList = iPartsDataNutzDokAnnotationList.getAllEntriesForType(getProject(), refId, refType);
            nutzDokAnnotationMap.put(remarkKey, annotationList);
        }
        return annotationList;
    }

    public boolean prepareNutzDokSubMenus(GuiMenu nutzDokMenu, String remarkRefId) {
        if (getType() != null) {
            return prepareNutzDokSubMenus(nutzDokMenu, remarkRefId, getType());
        }
        return false;
    }

    public boolean isNutzDokAnnotationMenuEnabled(String remarkRefId) {
        if (getType() != null) {
            return !getNutzDokAnnotationList(remarkRefId, getType().name()).isEmpty();
        }
        return false;
    }

    /**
     * Überprüft, ob in den attributes eine gültige Remard-RefId vorhanden ist und baut mit der Liste der Remark-Einträge
     * die SubMenus (incl Callback) auf
     * Falls es mehr als einen Remark gibt, wird der neueste als SubMenu angezeigt und alle Bemerkungstexte in einer Tabelle
     *
     * @param nutzDokMenu
     * @param refId
     * @param refType
     * @return
     */
    public boolean prepareNutzDokSubMenus(GuiMenu nutzDokMenu, String refId, iPartsWSWorkBasketItem.TYPE refType) {
        nutzDokMenu.removeAllChildren();
        if (StrUtils.isValid(refId)) {
            iPartsDataNutzDokRemarkList remarkList = getNutzDokRemarkList(refId, refType.name());
            if ((remarkList != null) && !remarkList.isEmpty()) {
                List<iPartsDataNutzDokRemark> sortedRemarkList = remarkList.getAsList();
                // Nach Datum sortieren
                sortedRemarkList.sort((iPartsDataNutzDokRemark o1, iPartsDataNutzDokRemark o2)
                                              -> o2.getFieldValue(iPartsConst.FIELD_DNR_LAST_MODIFIED).compareTo(o1.getFieldValue(iPartsConst.FIELD_DNR_LAST_MODIFIED)));

                if (remarkList.size() <= MAX_REMARKS_ELEMENTS_FOR_SUB_MENUS) {
                    for (iPartsDataNutzDokRemark dataNutzDokRemark : sortedRemarkList) {
                        addRemarkSubMenu(nutzDokMenu, dataNutzDokRemark);
                    }
                } else {
                    // Neuester Bemerkungstext in ein SubMenu, zusätzlich alle im Dialog zeigen
                    addRemarkSubMenu(nutzDokMenu, sortedRemarkList.get(0));
                    addSpecialSubMenu(nutzDokMenu, sortedRemarkList);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Ein SubMenu erzeugen und einhängen
     *
     * @param nutzDokMenu
     * @param dataNutzDokRemark
     */
    private void addRemarkSubMenu(GuiMenu nutzDokMenu, final iPartsDataNutzDokRemark dataNutzDokRemark) {
        final String text = getSubMenuTextRemark(dataNutzDokRemark);

        GuiMenu menu = createMenuEntry(dataNutzDokRemark.getAsId().toString(),
                                       text, null, nutzDokMenu.getTranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                          EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        downloadNutzDokRemark(getProject(), getParent().getGui(), dataNutzDokRemark);
                    }
                });
        nutzDokMenu.addChild(menu);
    }

    /**
     * Spezial SubMenu für die Anzeige als Tabelle erzeugen und einhängen
     *
     * @param nutzDokMenu
     * @param remarkList
     */
    private void addSpecialSubMenu(GuiMenu nutzDokMenu, List<iPartsDataNutzDokRemark> remarkList) {
        GuiMenu menu = createMenuEntry("specialRemarksSubMenu", "!!Alle Bemerkungstexte anzeigen",
                                       null, nutzDokMenu.getTranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                    EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        iPartsDataNutzDokRemark nutzDokRemark = iPartsShowDataObjectsDialog.showNutzDokRemarks(dataConnector, parent, remarkList);
                        if (nutzDokRemark != null) {
                            downloadNutzDokRemark(project, parent.getGui(), nutzDokRemark);
                        }
                    }
                });
        nutzDokMenu.addChild(menu);
    }

    /**
     * ein SubMenu erzeugen
     *
     * @param name
     * @param text
     * @param image
     * @param translationHandler
     * @param listener
     * @return
     */
    private GuiMenuItem createMenuEntry(String name, String text, FrameworkImage image, TranslationHandler translationHandler, EventListener listener) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setName(name);
        menuItem.__internal_setGenerationDpi(96);
        menuItem.registerTranslationHandler(translationHandler);
        menuItem.setScaleForResolution(true);
        menuItem.setText(text);
        menuItem.setIcon(image);
        menuItem.setUserObject(name);
        if (listener != null) {
            menuItem.addEventListener(listener);
        }
        return menuItem;
    }

    /**
     * Callback zum Download der Remarks
     *
     * @param project
     * @param parent
     * @param nutzDokRemark
     */
    protected void downloadNutzDokRemark(EtkProject project, AbstractGuiControl parent, iPartsDataNutzDokRemark nutzDokRemark) {
        if (nutzDokRemark != null) {
            String fileName = buildFileName(nutzDokRemark) + "." + DWFileFilterEnum.RTFFILES.getExtensionsArray()[0];
            //Attribute ohne geladenen Blob merken
            DBDataObjectAttributes nutzDokRemarkAttributesWithoutBlob = nutzDokRemark.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
            byte[] remarkBlob = nutzDokRemark.getFieldValueFromZippedBlob(iPartsConst.FIELD_DNR_REMARK);
            try {
                if (remarkBlob != null) {
                    MultipleInputToOutputStream stream = new MultipleInputToOutputStream(remarkBlob, fileName);
                    GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(parent, FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_FILES, null, false);
                    fileChooserDialog.addChoosableFileFilter(DWFileFilterEnum.RTFFILES.getDescription(), DWFileFilterEnum.RTFFILES.getExtensions());
                    fileChooserDialog.setActiveFileFilter(DWFileFilterEnum.RTFFILES.getDescription());
                    fileChooserDialog.setVisible(stream);
                } else {
                    MessageDialog.showError(TranslationHandler.translate("!!Datei \"%1\" ist in der Datenbank nicht vorhanden.", fileName));
                }

            } catch (IOException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
            // Den geladenen Blob wieder entfernen
            nutzDokRemark.assignAttributes(project, nutzDokRemarkAttributesWithoutBlob, false, DBActionOrigin.FROM_DB);
        }
    }

    protected String buildFileName(iPartsDataNutzDokRemark dataNutzDokRemark) {
        return dataNutzDokRemark.getAsId().toString("_");
    }

    protected String getSubMenuTextRemark(iPartsDataNutzDokRemark dataNutzDokRemark) {
        String lastModifiedDateString = project.getVisObject().asString(iPartsConst.TABLE_DA_NUTZDOK_REMARK, iPartsConst.FIELD_DNR_LAST_MODIFIED,
                                                                        dataNutzDokRemark.getFieldValue(iPartsConst.FIELD_DNR_LAST_MODIFIED), project.getDBLanguage());
        return TranslationHandler.translate("!!Aktuellste Bemerkung (%1)", lastModifiedDateString);
    }

    protected String getNutzDokRemarkRefId(DBDataObjectAttributes attributes) {
        return getFieldValueWithCheck(attributes, iPartsConst.FIELD_DNR_REF_ID);
    }

    /**
     * Key für den Cache bilden
     *
     * @param refId
     * @param refType
     * @return
     */
    private String buildRemarkKey(String refId, String refType) {
        return refId + "&" + refType;
    }
}
