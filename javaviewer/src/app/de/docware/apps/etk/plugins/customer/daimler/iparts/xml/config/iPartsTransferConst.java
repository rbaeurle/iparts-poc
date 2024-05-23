/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

import de.docware.util.StrUtils;

import java.util.EnumSet;
import java.util.GregorianCalendar;

/**
 * Attribute und Konstanten für die Elemente der Transfer XML
 */
public interface iPartsTransferConst {

    // Mögliche Transfer Beteiligte
    String PARTICIPANT_IPARTS = "iParts";
    String PARTICIPANT_ASPLM = "ASPLM";

    // Parameter und Attribute für SearchMediaContainers und SearchCriterion
    int DEFAULT_SEARCH_MAX_RESULT = -1;
    String ATTR_SMC_MAX_RESULTS = "maxResults";
    String ATTR_SMC_NAME = "attrName";

    // CreateMediaOrder Parameter + Attribute
    String ASPLM_XML_NAMESPACE_PREFIX = "a:";
    int ASPLM_MC_DESCRIPTION_LENGTH = 1000;

    String CMO_MAINNGROUP = "mainGroup";
    String CMO_GROUP = "group";
    String CMO_TECHNICAL_SCOPE = "technicalScope";
    String CMO_CONSTRUCTION_GROUP = "constructionGroup";
    String CMO_PART_SCOPE_NUMBER = "partScopeNr";
    String CMO_TYPE = "type";
    String CMO_MODEL_SERIES = "modelSeries";
    String CMO_MAIN_MODULE = "mainModule";
    String CMO_MODULE = "module";
    String CMO_SUBMODULE = "submodule";
    String CMO_POS = "pos";
    String CMO_POS_VARIANT = "posVariant";
    // Neu ab DAIMLER-15068
    String CMO_OPT_PART_INDICATOR = "optionalPartIndicator";
    String CMO_PARTS_COUNTER = "partsCounter";
    String CMO_PRODUCT_VERSION = "productVersion";
    String CMO_KEM_DATE_FROM = "ecoValidFrom";
    // Ende Neu ab DAIMLER-15068
    String CMO_ZGS = "zgs";
    String CMO_RELEASE_DATE = "releaseDate";
    String CMO_PROJECTS = "projects";

    // ChangeMediaOrder Parameter + Attribute
    int ASPLM_CHANGE_REASON_MAX_LENGTH = 128;

    // Attribute aus "AttrGroupRequestResponse"
    String ATTR_GRR_FROM = "from";
    String ATTR_GRR_TO = "to";
    String ATTR_GRR_WHEN = "when";
    String ATTR_GRR_REQUEST_ID = "requestId";
    String ATTR_GRP_REQUEST_OPERATION = "requestOperation";

    // Attribut für Auftragsanlagedatum bzw. MediaOrder
    String ATTR_GRP_MEDIA_DATE_ORDERED = "dateOrdered";

    // Attribute aus "AttrGroupUser"
    String ATTR_GU_GROUP_ID = "groupId";
    String ATTR_GU_USER_ID = "userId";

    // Attribute für EventAssignmentChange
    String ATTR_EAC_WORKFLOW = "workflowName";

    // Attribute für History Timestamps
    String ATTR_TIMESTAMP_CREATOR = "creator";
    String ATTR_TIMESTAMP_EVENT = "event";

    // Success Parameter + Attribute
    String ATTR_SUC_OK = "ok";
    String ATTR_SUC_ERRORCODE = "errorcode";
    String ATTR_SUC_TARGET_ID = "targetId";
    int ATTR_SUC_DEFAULT_ERRORCODE = -1;

    // Text Parameter + Attribute
    String ATTR_LANG = "lang";
    String ATTR_TEXT_ID = "textId";

    // MediaContainer Parameter + Attribute
    String ATTR_MC_ITEM_ID = "mcItemId";
    String ATTR_MC_ITEM_REV_ID = "mcItemRevId";
    String ATTR_MC_FILETYPES = "fileTypes";
    String ATTR_IS_COPY = "copy";

    // Attribute und Parameter für ResSearchMediaContainer
    String ATT_RSMC_RESULTS_DELIVERED = "numResultsDelivered";
    String ATT_RSMC_RESULTS_FOUND = "numResultsFound";

    //Attribute und Parameter für ResGetMediaPreview
    String RGMP_BINARY = "BinaryFile";

    // Message Parameter + Attribute
    String ATTR_M_NAMESPACE = "xmlns";
    String ATTR_M_NAMESPACE_ASPLM = ATTR_M_NAMESPACE + ":a";
    String ATTR_M_NAMESPACE_SCHEMA = "xmlns:xsi";
    String ATTR_M_NAMESPACE_SCHEMA_LOCATION = "xsi:schemaLocation";
    String DEFAULT_NAMESPACE = "http://asplm.daimler.com/MediaService/v1";
    String DEFAULT_NAMESPACE_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
    String DEFAULT_NAMESPACE_SCHEMA_LOCATION = "http://asplm.daimler.com/MediaService/v1 asplm_message_flat.xsd";

    // Dialog Parameter
    String DEFAULT_ATTR_NAMESPACE_LOCATION_DIALOG = "xsi:noNamespaceSchemaLocation";
    String DEFAULT_NAMESPACE_LOCATION_DIALOG = "../dialog.xsd";

    // Tabellen Parameter + Attribute
    String ATTR_TABLE_ORIGIN = "origin";
    String ATTR_TABLE_SCHEMA_VERSION = "schemaVersion";
    String ATTR_TABLE_SOURCE_EXPORT_TIME = "sourceExportTime";
    String ATTR_TABLE_TRAFO_TIME = "trafoTime";

    // Event Attribute
    String ATTR_EVENT_NEW = "new";
    String ATTR_EVENT_OLD = "old";

    // Tabellen Datensatz Parameter + Attribute
    String ATTR_TABLE_SEQUENCE_NO = "seqNo";
    String ATTR_TABLE_KEM = "kem";
    String ATTR_TABLE_SDB_FLAG = "SDB_KZ";

    // Allgemeine Attribute
    String ATTR_NAME = "name";
    String ATTR_TIME = "time";
    String ATTR_ITEM_ID = "itemId";
    String ATTR_ITEM_REV_ID = "itemRevId";
    String ATTR_LANGUAGE = "language";
    String ATT_FILETYPE = "fileType";
    String DATE_DUE_DATEFORMAT = "yyyy-MM-dd";

    // Media Attribute
    String ATTR_MEDIA_DESIGNER = "designer";
    String ATTR_MEDIA_COLOR = "color";
    String ATTR_LAST_MODFIED = "tcDateLastModified";
    String ATTR_MEDIA_DERIVED = "derived";
    String ATTR_AUTOMATION_LEVEL = "automationLevel";
    String ATTR_IS_TEMPLATE = "isTemplate";

    // Attachment Attribute
    String ATTR_ATTACHMENT_ID = "id";
    String ATTR_ATTACHMENT_DESCRIPTION = "description";
    String ATTR_ATTACHMENT_PURPOSE = "purpose";

    // Attribute für Bildpositionen
    String ATTR_PIC_POS_SEQ_NO = "seqNrMo";

    // Aktuelle Schema Version und Default Kommentar
    String SCHEMA_VERSION = "1.2.0";
    String DEFAULT_XML_COMMENT = "\nValidated with XML Schema Version " + iPartsTransferConst.SCHEMA_VERSION + "\nXML Creation Date: " + GregorianCalendar.getInstance().getTime() + "\n";

    // Prefix für MediaContentOperationen
    String MEDIA_CONTENT_PREFIX_DELIMITER = "||";
    String MEDIA_CONTENT_PIC_REFERENCE_PREFIX = "PicReference";
    String MEDIA_CONTENT_REQUEST_PICTURES_PREFIX = "PictureRequest";

    // Suffix von Vorschaubildern für den manuellen Import der Referenzzeichnungen
    String SUFFIX_THUMBNAIL_IMAGE_IMPORT = "thumbnail";

    // Trennzeichen für Elemente und Attribute in einem TableDataset
    String ELEMENT_ATTRIBUTE_DELIMITER = "_ATTRIBUTE_";

    // Default Enum-Wert für "Realization"
    String DEFAULT_REALIZATION_VALUE = "GREYSCALE";
    String DEFAULT_REALIZATION_XML_VALUE = "Graustufenbild";

    // Unternehmenszugehörigkeit
    String COMPANY_VALUE_DTAG = "DTAG";
    String COMPANY_VALUE_MBAG = "MBAG";
    String COMPANY_VALUE_ELASTIC_SEARCH_INDEX_BOTH = "all";

    enum MediaFileTypes {
        AI("ai"),
        CGM("cgm"),
        GIF("gif"),
        JPG("jpg"),
        JPEG("jpeg"),
        JT("jt"),
        PDF("pdf"),
        PNG("png"),
        PSD("psd"),
        SEN("sen"),
        SVG("svg"),
        TIF("tif"),
        TIFF("tiff"),
        TXT("txt"),
        XML("xml"),
        ZIP("zip");


        private static EnumSet<MediaFileTypes> validPreviewExtensions = EnumSet.of(PNG, GIF, TIF, JPEG);

        private String alias;

        MediaFileTypes(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

        public static MediaFileTypes getFromAlias(String alias) {
            for (MediaFileTypes result : values()) {
                if (result.alias.equalsIgnoreCase(alias)) {
                    return result;
                }
            }
            return null;
        }

        public static String[] getAliasAsStringArray() {
            String[] result = new String[values().length];
            for (int i = 0; i < values().length; i++) {
                result[i] = values()[i].getAlias();
            }
            return result;
        }

        public static boolean isValidFileExtension(String extension) {
            if ((extension == null) || extension.isEmpty()) {
                return false;
            }

            return getFromAlias(extension) != null;
        }

        public static boolean isValidPreviewExtension(String extension) {
            if ((extension == null) || extension.isEmpty()) {
                return false;
            }

            MediaFileTypes mediaFileType = getFromAlias(extension);
            return (mediaFileType != null) && validPreviewExtensions.contains(mediaFileType);
        }

        public static boolean isHotspotFile(String extension) {
            if (StrUtils.isEmpty(extension)) {
                return false;
            }
            return getFromAlias(extension) == SEN;
        }
    }

    enum AttachmentBinaryFileTypes {
        PDF("pdf"),
        DOC("doc"),
        DOCX("docx"),
        XLS("xls"),
        XLSX("xlsx"),
        PPT("ppt"),
        PPTX("pptx"),
        MPP("mpp"),
        JPG("jpg"),
        TIF("tif"),
        JT("jt"),
        CGM("cgm"),
        ZIP("zip"),
        PNG("png"),
        SVG("svg"),
        PSD("psd"),
        FLV("flv"),
        MP4("mp4"),
        AVI("avi"),
        DWG("dwg");

        private String alias;

        AttachmentBinaryFileTypes(String alias) {
            this.alias = alias;
        }

        /**
         * Gibt alle Enum Werte als String Array zurück
         *
         * @return
         */
        public static String[] getAsExtensionArray() {
            String[] extensions = new String[values().length];
            int index = 0;
            for (AttachmentBinaryFileTypes extension : values()) {
                extensions[index] = extension.getAlias();
                index++;
            }
            return extensions;
        }

        public String getAlias() {
            return alias;
        }

        public static AttachmentBinaryFileTypes getFromAlias(String alias) {
            for (AttachmentBinaryFileTypes result : values()) {
                if (result.alias.equalsIgnoreCase(alias)) {
                    return result;
                }
            }
            return null;
        }

        public static boolean isValidFileExtension(String extension) {
            if ((extension == null) || extension.isEmpty()) {
                return false;
            }

            return getFromAlias(extension) != null;
        }
    }

    enum AttachmentTextFileTypes {
        TXT("txt"),
        XML("xml"),
        CSV("csv");

        private String alias;

        AttachmentTextFileTypes(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

        public static AttachmentTextFileTypes getFromAlias(String alias) {
            for (AttachmentTextFileTypes result : values()) {
                if (result.alias.equalsIgnoreCase(alias)) {
                    return result;
                }
            }
            return null;
        }

        /**
         * Gibt alle Enum Werte als String Array zurück
         *
         * @return
         */
        public static String[] getAsExtensionArray() {
            String[] extensions = new String[values().length];
            int index = 0;
            for (AttachmentTextFileTypes extension : values()) {
                extensions[index] = extension.getAlias();
                index++;
            }
            return extensions;
        }

        public static boolean isValidFileExtension(String extension) {
            if ((extension == null) || extension.isEmpty()) {
                return false;
            }

            return getFromAlias(extension) != null;
        }
    }
}
