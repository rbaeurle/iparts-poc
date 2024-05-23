/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Alle möglichen Elemente der Transfer XML
 */
public enum iPartsTransferNodeTypes {
    // Bildauftrag
    MESSAGE("Message"),
    REQUEST("Request"),
    RESPONSE("Response"),
    REQUESTOR("Requestor"),
    OPERATION("Operation"),
    CREATE_MEDIA_ORDER("CreateMediaOrder"),
    SUCCESS("Success"),
    ERRORTEXT("Errortext"),
    WARNING("Warning"),
    WARNINGTEXT("Warningtext"),
    RES_CREATE_MEDIA_ORDER("ResCreateMediaOrder"),
    MEDIA_CONTAINER("MediaContainer"),
    HISTORY("History"),
    SEARCH_MEDIA_CONTAINERS("SearchMediaContainers"),
    RES_SEARCH_MEDIA_CONTAINERS("ResSearchMediaContainers"),
    GET_MEDIA_PREVIEW("GetMediaPreview"),
    RES_GET_MEDIA_PREVIEW("ResGetMediaPreview"),
    PRODUCT("Product"),
    NAME("Name"),
    DESCRIPTION("Description"),
    REMARK("Remark"),
    REALIZATION("Realization"),
    DATE_DUE("DateDue"),
    ASSIGN_TO("AssignTo"),
    COMPANY("Company"),
    EINPAS("EinPAS"),
    KGTU("KGTU"),
    WORKING_CONTEXT("WorkingContext"),
    USAGE("Usage"),
    SEARCH_CRITERION("SearchCriterion"),
    RESULT_ATTRIBUTE("ResultAttribute"),
    BINARY_FILE("BinaryFile"),
    POSITION_LIST("IPartsPositionList"),
    PART_POSITION("PartPosition"),
    PART_NAME("PartName"),
    SOURCE_KEY("SourceKey"),
    SOURCE_KEY_DIALOG("SourceKeyDialog"),
    SOURCE_KEY_TRUCK("SourceKeyTruck"),
    SUPPLEMENTARY_TEXT("Supp_LI_Text"),
    QUANTITY("Quantity"),
    PICTURE_POSITION_MARKER("PicturePositionMarker"),
    HOTSPOT("Hotspot"),
    ASSEMBLY_SIGN("AssemblySign"),
    STRUCTURE_LEVEL("StructureLevel"),
    PART_NUMBER("PartNumber"),
    EXTERNAL_ID("ExternalId"),
    FAULT_LOCATION("FaultLocation"),
    GENERIC_INSTALL_LOCATION("GenVO"),
    MEDIA_ORDER("MediaOrder"),
    TIMESTAMP("Timestamp"),
    INVALID_REQUEST("CDATA"),
    EVENT("Event"),
    ATTRIBUTE("Attribute"),
    COMMENT("Comment"),
    EVENT_ASSIGNMENT_CHANGE("EventAssignmentChange"),
    EVENT_RELEASE_STATUS_CHANGE("EventReleaseStatusChange"),
    TC_OBJECT("TcObject"),
    CONTRACTOR("Contractor"),
    MEDIA_VARIANT("MediaVariant"),
    MEDIA_CONTAINER_ATTRIBUTE("Attr"),
    MEDIA("Media"),
    GET_MEDIA_CONTENTS("GetMediaContents"),
    RES_GET_MEDIA_CONTENTS("ResGetMediaContents"),
    CREATE_MC_ATTACHMENTS("CreateMcAttachments"),
    RES_CREATE_MC_ATTACHMENTS("ResCreateMcAttachments"),
    ATTACHMENT("Attachment"),
    TEXT_FILE("TextFile"),
    CORRECT_MEDIA_ORDER("CorrectMediaOrder"),
    RES_CORRECT_MEDIA_ORDER("ResCorrectMediaOrder"),
    REASON("Reason"),
    PART_SCOPE_NAME("PartScopeName"),
    CHANGE_MEDIA_ORDER("ChangeMediaOrder"),
    RES_CHANGE_MEDIA_ORDER("ResChangeMediaOrder"),
    UPDATE_MEDIA_ORDER("UpdateMediaOrder"),
    RES_UPDATE_MEDIA_ORDER("ResUpdateMediaOrder"),
    ACCEPT_MEDIA_CONTAINER("AcceptMediaContainer"),
    RES_ACCEPT_MEDIA_CONTAINER("ResAcceptMediaContainer"),
    ABORT_MEDIA_ORDER("AbortMediaOrder"),
    RES_ABORT_MEDIA_ORDER("ResAbortMediaOrder"),
    RELATION("Relation"),

    // EDS und DIALOG Import
    TABLE("Dataset"),
    MIXED_TABLE("DatasetMixed"),

    // PRIMUS und SRM Import
    PRIMUS_DATASET("PRIMUS_DATASET"), // Nur Platzhalter, da die Dateiquelle erst in einem der "unteren" Knoten ermittelt wird
    SRM_DATASET("SRM_DATASET"), // Nur Platzhalter, da die Dateiquelle erst in einem der "unteren" Knoten ermittelt wird
    MQ_PART_DATA_SOURCE("Src"),
    MQ_PART_DATA_SYSTEM("SYS"),
    MQ_PART_DATA_FIRST_ELEMENT("Req"),
    MQ_PART_DATA_HEADER("ReqHd"),
    MQ_PART_DATA_MESSAGE("Msg"),

    // Platzhalter für unbekannte Events
    EVENT_UNKNOWN("UNKNOWN_EVENT");

    private String alias;

    iPartsTransferNodeTypes(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public static iPartsTransferNodeTypes getFromAlias(String alias) {
        for (iPartsTransferNodeTypes result : values()) {
            if (result.alias.equals(alias)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Alias inkl. {@link iPartsTransferConst#ASPLM_XML_NAMESPACE_PREFIX}
     *
     * @return
     */
    public String getAliasWithASPLMNamespace() {
        return iPartsTransferConst.ASPLM_XML_NAMESPACE_PREFIX + alias;
    }


    @Override
    public String toString() {
        return alias; // zur Sicherheit falls man mal das getAlias() vergisst
    }

    public static iPartsTransferNodeTypes[] getAllEventTypes() {
        return new iPartsTransferNodeTypes[]{ EVENT_RELEASE_STATUS_CHANGE, EVENT_ASSIGNMENT_CHANGE };
    }

    public static boolean isValidFinalOperation(iPartsTransferNodeTypes currentState) {
        return (currentState == iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER) || (currentState == iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
    }
}
