/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.combimodules.useradmin.db.Right;
import de.docware.framework.combimodules.useradmin.db.RightDbObject;
import de.docware.framework.combimodules.useradmin.db.UserAdminDbActions;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStatement;
import de.docware.util.sql.pool.ConnectionPool;

import java.sql.SQLException;

/**
 * Alle Rechte in iParts.
 */
public class iPartsRight extends Right {

    public static final iPartsRight EDIT_MASTER_DATA = new iPartsRight("EditMasterData", "!!Stammdaten bearbeiten", null, false);
    public static final iPartsRight DELETE_MASTER_DATA = new iPartsRight("DeleteMasterData", "!!Stammdaten löschen", null, false);
    public static final iPartsRight IMPORT_MASTER_DATA = new iPartsRight("ImportMasterData", "!!Stammdaten importieren", null, false);
    public static final iPartsRight EDIT_VEHICLE_PARTS_DATA = new iPartsRight("EditVehiclePartsData", "!!Fahrzeug- und Teiledaten bearbeiten", null, false);
    public static final iPartsRight ASSIGN_VEHICLE_AGGS_DATA = new iPartsRight("AssignVehicleAggsData", "!!Zuordnung FBM/ABM (AS)", null, false);
    public static final iPartsRight EDIT_PARTS_DATA = new iPartsRight("EditPartsData", "!!Stücklisten bearbeiten", null, false);
    public static final iPartsRight VIEW_PARTS_DATA = new iPartsRight("ViewPartsData", "!!Stücklisten zeigen", null, false);
    public static final iPartsRight EXPORT_PARTS_DATA = new iPartsRight("ExportPartsData", "!!Stücklisten exportieren", null, false);
    public static final iPartsRight CREATE_MAILBOX_MESSAGES = new iPartsRight("CreateMailboxMessages", "!!Nachrichten im Postkorb erstellen", null, true);
    public static final iPartsRight VIEW_LOG_DATA = new iPartsRight("ViewLogData", "!!Log-Dateien zeigen", null, false);
    public static final iPartsRight EDIT_TEXT_PRODUCT_ADMIN = new iPartsRight("EditTextProductAdmin", "!!Text anlegen - Produktadministrator", null, false);
    public static final iPartsRight EDIT_TEXT_DATA_ADMIN = new iPartsRight("EditTextDataAdmin", "!!Text anlegen - Datenadministrator", null, false);
    public static final iPartsRight ASSIGN_USER_OR_GROUP = new iPartsRight("AssignUserOrGroup", "!!Benutzer oder Gruppe zuweisen", null, true);
    public static final iPartsRight FORCE_ASSIGN_USER_OR_GROUP = new iPartsRight("ForceAssignUserOrGroup", "!!Autoren-Auftrag übernehmen bzw. zuweisen", null, true);
    public static final iPartsRight VIEW_AUTHOR_ORDERS = new iPartsRight("ViewAuthorOrders", "!!Autoren-Aufträge zeigen", null, true);
    public static final iPartsRight REQUEST_TRANSLATIONS = new iPartsRight("RequestTranslations", "!!Übersetzungen anfordern", null, false);
    public static final iPartsRight IGNORE_ERRORS_IN_AO_RELEASE_CHECKS = new iPartsRight("IgnoreErrorsInAOReleaseChecks", "!!Fehler in Freigabeprüfungen ignorieren", null, false);
    public static final iPartsRight SUPPLY_AUTHOR_ORDER_TO_BST = new iPartsRight("SupplyAuthorOrderToBST", "!!Autoren-Auftrag an BST versorgen", null, false);
    public static final iPartsRight ADD_SERIES_TO_AUTO_CALC_AND_EXPORT = new iPartsRight("AddSeriesToAutoCalcAndExport", "!!Baureihen für autom. Berechnung markieren", null, false);
    public static final iPartsRight COPY_TUS = new iPartsRight("CopyTUs", "!!TUs kopieren", null, false);
    public static final iPartsRight DELETE_EMPTY_TUS = new iPartsRight("DeleteEmptyTUs", "!!Leere TUs sofort löschen", null, false);
    public static final iPartsRight EXPORT_IMPORT_AUTHOR_ORDER = new iPartsRight("ExportImportAuthorOrder", "!!Autoren-Aufträge ex- und importieren", null, false);
    public static final iPartsRight VIEW_CONSTRUCTION_PARTS_DATA = new iPartsRight("ViewConstructionPartsData", "!!Konstruktions-Stücklisten zeigen", null, false);
    public static final iPartsRight EDIT_INTERNAL_TEXT_CONSTRUCTION = new iPartsRight("EditInternalTextConstruction", "!!Internen Text in DIALOG-Konstruktion editieren", null, false);
    public static final iPartsRight VIEW_EDS_WORK_BASKET = new iPartsRight("ViewEDSWorkBasket", "!!EDS Arbeitsvorrat zeigen", null, false);
    public static final iPartsRight DELETE_RETAIL_CACHES = new iPartsRight("DeleteRetailCaches", "!!Retail-Caches zurücksetzen", null, false);
    public static final iPartsRight RETRIEVE_PICTURES = new iPartsRight("RetrievePictures", "!!Zeichnungen nachfordern", null, false);
    public static final iPartsRight RETRIEVE_PICTURES_FROM_PIC_ORDER = new iPartsRight("RetrievePicturesFromPicOrder", "!!Bilder eines Bildauftrags nachfordern", null, false);
    public static final iPartsRight CREATE_DELETE_PEM_DATA = new iPartsRight("CreateAndDeletePemData", "!!PEMs erzeugen und löschen", null, false);
    public static final iPartsRight AUTO_MODEL_VALIDITY_EXTENSION = new iPartsRight("AutoModelValidityExtension", "!!Automatisierte Baumustererweiterung", null, false);
    public static final iPartsRight VIEW_DATABASE_TOOLS = new iPartsRight("viewDatabaseTools", "!!Datenbank-Tools zeigen", null, false);
    public static final iPartsRight AUTO_TRANSFER_PART_LIST_ENTRIES = new iPartsRight("AutoTransferPartListEntries", "!!Automatisierte AS-Übernahme", null, false);
    public static final iPartsRight AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED = new iPartsRight("autoTransferPartListEntriesExtended", "!!Automatische Submodul-Verarbeitung", null, false);
    public static final iPartsRight AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT = new iPartsRight("autoTransferPLESExtendedWholeProduct", "!!Automatische Submodul-Verarbeitung übers ganze Produkt", null, false);
    public static final iPartsRight LOCK_PART_LIST_ENTRIES_FOR_EDIT = new iPartsRight("lockPartListEntries", "!!Stücklistenpositionen (ent-)sperren", null, false);
    public static final iPartsRight EDIT_DELETE_INTERNAL_TEXT = new iPartsRight("EditDeleteInternalText", "!!Internen Text in TUs löschen", null, false);
    public static final iPartsRight CREATE_FOOTNOTE = new iPartsRight("CreateFootnote", "!!Fußnote anlegen", null, false);
    public static final iPartsRight EXECUTE_EXCEL_IMPORT = new iPartsRight("ExecuteExcelImport", "!!Excel Importe durchführen", null, false);
    public static final iPartsRight EDIT_OMIT_FOR_SPECIAL_PARTS = new iPartsRight("EditOmitForSpecialParts", "!!Unterdrücken für Sonderschutz-Teile bearbeiten", null, false);
    public static final iPartsRight CREATE_DELETE_CAR_PERSPECTIVE = new iPartsRight("CreateDeleteCarPerspective", "!!Fahrzeugperspektive erzeugen und löschen", null, false);
    public static final iPartsRight REPORT_EDIT_OF_AUTO_TRANSFER_ENTRIES = new iPartsRight("ReportEditOfAutoTransferEntries", "!!Auswertung von Änderungen an autom. erzeugten Teilepos.", null, false);
    public static final iPartsRight REPORT_TU_VALIDATION_FOR_PRODUKT = new iPartsRight("ReportTuValidationForProdukt", "!!TU-Prüfung über ganzes Produkt", null, false);
    public static final iPartsRight SIMPLIFIED_QUALITY_CHECKS = new iPartsRight("SimplifiedQualityChecks", "!!Vereinfachte Qualitätsprüfungen für Spezial-TUs", null, false);
    public static final iPartsRight PURGE_DATABASE = new iPartsRight("PurgeDatabase", "!!Datenbank bereinigen", null, false);

    private static boolean initialized;

    /**
     * Initialisiert die Rechte und schreibt sie in die Datenbank falls notwendig.
     *
     * @param userAdminConnectionPool
     * @param userAdminStatement
     * @throws SQLException
     */
    public static synchronized void init(ConnectionPool userAdminConnectionPool, SQLStatement userAdminStatement) throws SQLException {
        if (initialized) {
            return;
        }

        // Sicherstellen, dass alle Rechte in der DB sind
        for (Right right : iPartsRight.values()) {
            if (right.getAppId().equals(iPartsUserAdminDb.APP_ID)) {
                RightDbObject.addRight(userAdminConnectionPool, userAdminStatement, true, right.getAlias(),
                                       iPartsUserAdminDb.APP_ID, right.getCaption(), right.hasScope());
            }
        }

        initialized = true;
    }

    /**
     * Überprüft, ob das angegebene Produkt für den eingeloggten Benutzer der aktuellen Session editiert werden darf bzgl.
     * des übergebenen optionalen Rechts sowie PSK-Erlaubnis und zeigt optional einen Dialog falls dafür keine Rechte vorhanden
     * sind.
     *
     * @param productId
     * @param right
     * @param showDialog
     * @param project
     * @return
     */
    public static boolean checkProductEditableInSession(iPartsProductId productId, iPartsRight right, boolean showDialog,
                                                        EtkProject project) {
        boolean editProductAllowed = false;
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        // PSK-Produkte erlaubt und es ist auch ein PSK-Produkt? Ansonsten bei Nicht-PSK-Produkten die Sichtbarkeit anhand
        // der Benutzer-Eigenschaften prüfen
        if ((product.isPSK() && checkPSKInSession()) || (!product.isPSK() && iPartsFilterHelper.isProductVisibleForUserInSession(product))) {
            if (right == null) {
                editProductAllowed = true;
            } else if (right.checkRightInSession()) {
                String editableProducts = iPartsUserAdminDb.getPropertyValueAsStringForSession(iPartsUserAdminDb.PROPERTY_ID_EDITABLE_PRODUCTS);
                if (!editableProducts.isEmpty()) {
                    String[] editableProductsArray = StrUtils.toStringArray(editableProducts, "|");
                    for (String editableProductPattern : editableProductsArray) {
                        if (StrUtils.matchesSqlLike(editableProductPattern, productId.getProductNumber(), false)) {
                            editProductAllowed = true;
                            break;
                        }
                    }
                }
            }
        }

        // Optional Dialog anzeigen, wenn das Editieren nicht erlaubt ist
        if (!editProductAllowed && showDialog) {
            boolean isEdit = (right != null) && (right != VIEW_PARTS_DATA) && (right != EXPORT_PARTS_DATA);
            MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum %1 vom Produkt \"%2\" für den Benutzer \"%3\".",
                                                                   TranslationHandler.translate(isEdit ? "!!Editieren" : "!!Anzeigen"), productId.getProductNumber(),
                                                                   iPartsUserAdminDb.getLoginUserFullName()));
        }

        return editProductAllowed;
    }

    /**
     * Überprüft, ob der eingeloggte Benutzer der aktuellen Session PSK-Produkte sehen und editieren darf.
     *
     * @return
     */
    public static boolean checkPSKInSession() {
        return Boolean.valueOf(iPartsUserAdminDb.getPropertyValueAsStringForSession(iPartsUserAdminDb.PROPERTY_ID_PSK));
    }

    private static boolean sessionCanHandleGui() {
        Session session = Session.get();
        if (session != null) {
            return session.canHandleGui();
        } else {
            return false;
        }
    }

    /**
     * Überprüft, ob der eingeloggte Benutzer der aktuellen Session PKW/Van-Daten sehen und editieren darf.
     *
     * @return
     */
    public static boolean checkCarAndVanInSession() {
        return Boolean.valueOf(iPartsUserAdminDb.getPropertyValueAsStringForSession(iPartsUserAdminDb.PROPERTY_ID_CAR_VAN))
               || !sessionCanHandleGui(); // Notwendig z.B. für die Sichtbarkeit von Konstruktions-Knoten in Hintergrund-Jobs
    }

    /**
     * Überprüft, ob der eingeloggte Benutzer der aktuellen Session Truck/Bus-Daten sehen und editieren darf.
     *
     * @return
     */
    public static boolean checkTruckAndBusInSession() {
        return Boolean.valueOf(iPartsUserAdminDb.getPropertyValueAsStringForSession(iPartsUserAdminDb.PROPERTY_ID_TRUCK_BUS))
               || !sessionCanHandleGui(); // Notwendig z.B. für die Sichtbarkeit von Konstruktions-Knoten in Hintergrund-Jobs
    }

    /**
     * Überprüft, ob der eingeloggte Benutzer der aktuellen Session PKW/Van- und Truck/Bus-Daten sehen und editieren darf.
     *
     * @return
     */
    public static boolean checkUserHasBothVehicleTypeRightsInSession() {
        return iPartsRight.checkTruckAndBusInSession() && iPartsRight.checkCarAndVanInSession();
    }


    /**
     * Konstruktor für ein Recht in iParts.
     *
     * @param alias
     * @param captionKey
     * @param requiredRights
     * @param hasScope
     */
    protected iPartsRight(String alias, String captionKey, Right[] requiredRights, boolean hasScope) {
        super(iPartsUserAdminDb.APP_ID, alias, captionKey, requiredRights, hasScope);
    }

    @Override
    public boolean checkRightInSession(boolean withLicenseCheck) {
        String loginUserId = UserAdminDbActions.getLoginUserIdForSession();
        if (loginUserId == null) {
            return false;
        }

        // withLicenseCheck ignorieren und immer true annehmen in isUserRight() (ist für iParts sowieso egal)
        return iPartsUserAdminCache.getInstance(loginUserId).isUserRight(this);
    }
}