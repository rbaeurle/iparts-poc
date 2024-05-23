/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

/**
 * Sammlung aller XML Tags (inkl. kompletten Pfad) für die XML Teilestamm Importer (SRM und PRIMUS)
 */
public interface iPartsXMLPartImportTags {

    String MSG_PART = "<Msg><Res><Part>";
    String MSG_ACTION = "<Msg><ACT>";
    String MSG_TIMESTAMP = "<Msg><MsgHd><DTI>";
    String MSG_PRTID = MSG_PART + "<PrtId>";  //"<Msg><Res><Part><PrtId>";
    String MSG_ATT = MSG_PART + "<Att>";  //"<Msg><Res><Part><Att>";

    String MSG_BRD = MSG_PRTID + "<BRD>"; //"<Msg><Res><Part><PrtId><BRD>";
    String MSG_PTN = MSG_PRTID + "<PTN>"; //"<Msg><Res><Part><PrtId><PTN>";

    String MSG_PTN_NEW = MSG_ATT + "<PTNNew>";  //"<Msg><Res><Part><Att><PTNNew>";
    String MSG_PSS_CODE_FORW = MSG_ATT + "<PSSCodeForw>";  //"<Msg><Res><Part><Att><PSSCodeForw>";
    String MSG_PSS_CODE_BACK = MSG_ATT + "<PSSCodeBack>";  //"<Msg><Res><Part><Att><PSSCodeBack>";
    String MSG_PSS_INFO_TYPE = MSG_ATT + "<PSSInfoType>";  //"<Msg><Res><Part><Att><PSSInfoType>";
    String MSG_STATE_OF_LIFECYCLE = MSG_ATT + "<StateOfLivecycle>";  //"<Msg><Res><Part><Att><StateOfLivecycle>";
    String MSG_TERMID = MSG_ATT + "<TermID>";  //"<Msg><Res><Part><Att><TermID>";
    String MSG_STATE = MSG_ATT + "<State>";  //"<Msg><Res><Part><Att><State>";
    String MSG_QUANTUNIT = MSG_ATT + "<QuantityUnit>";  //"<Msg><Res><Part><Att><QuantityUnit>";
    String MSG_REMAN = MSG_ATT + "<RemanRelevance>";  //"<Msg><Res><Part><Att><RemanRelevance>";

    // Wahlweise-Teile
    String MSG_WW_ID = MSG_ATT + "<WWID>"; //"<Msg><Res><Part><Att><WWID>";
    String MSG_WW_TYPE = MSG_ATT + "<WWType>"; //"<Msg><Res><Part><Att><WWType>";
    String MSG_WW_STATUS = MSG_ATT + "<WWStatus>"; //"<Msg><Res><Part><Att><WWStatus>";
    String MSG_WW_PARTS = MSG_ATT + "<WWParts>"; //"<Msg><Res><Part><Att><WWParts>";
    String MSG_WW_PART = MSG_WW_PARTS + "<WWPart>"; //"<Msg><Res><Part><Att><WWParts><WWPart>"";
    String MSG_WW_PART_NUMBER = MSG_WW_PART + "<WWPartNumber>"; //"<Msg><Res><Part><Att><WWParts><WWPart><WWPartNumber>"";
    String MSG_WW_LEADING_PART = MSG_WW_PART + "<WWLeadingPart>"; //"<Msg><Res><Part><Att><WWParts><WWPart><WWLeadingPart>"";

    String MSG_PART_DESCRIPTIONS = MSG_ATT + "<PartDescriptions>";
    String MSG_LANGUAGE = MSG_PART_DESCRIPTIONS + "<PartDescription><Language>";  //"<Msg><Res><Part><Att><PartDescriptions><PartDescription><Language>";
    String MSG_DESCRIPTION = MSG_PART_DESCRIPTIONS + "<PartDescription><Description>";  //"<Msg><Res><Part><Att><PartDescriptions><PartDescription><Description>";

    String MSG_CHINA = MSG_ATT + "<ChinaClassificator>";  //"<Msg><Res><Part><Att><ChinaClassificator>";
    String MSG_NATO = MSG_ATT + "<NatoPTN>";  //"<Msg><Res><Part><Att><NatoPTN>";
    String MSG_SVHC = MSG_ATT + "<SVHCIndicator>";  //"<Msg><Res><Part><Att><SVHCIndicator>";
    String MSG_TECH_INFO = MSG_ATT + "<TechInfo>";  //"<Msg><Res><Part><Att><TechInfo>";
    // Mitlieferteile
    String MSG_ADDITIONAL_PARTS = MSG_ATT + "<AdditionalParts>";  //"<Msg><Res><Part><Att><AdditionalParts>";
    String MSG_ADDITIONAL_PART = MSG_ADDITIONAL_PARTS + "<AdditionalPart>";  //"<Msg><Res><Part><Att><AdditionalParts><AdditionalPart>";
    String MSG_ADDITIONAL_PART_NUMBER = MSG_ADDITIONAL_PART + "<AdditionalPartNumber>";  //"<Msg><Res><Part><Att><AdditionalParts><AdditionalPart><AdditionalPartNumber>";
    String MSG_ADDITIONAL_PART_QUANTITY = MSG_ADDITIONAL_PART + "<Quantity>";  //"<Msg><Res><Part><Att><AdditionalParts><AdditionalPart><Quantity>";

    String MSG_PTN_PRINT = MSG_ATT + "<PTNPrintFormat>";  //"<Msg><Res><Part><Att><PTNPrintFormat>";
    // Attribute, die redundant in Primus sind
    String MSG_SECURITY = MSG_ATT + "<SecurityRelevance>";  //"<Msg><Res><Part><Att><SecurityRelevance>";
    String MSG_CERTIFICATION = MSG_ATT + "<CertificationRelevance>";  //"<Msg><Res><Part><Att><CertificationRelevance>";
    String MSG_THEFT_REL = MSG_ATT + "<TheftRelevance>";  //"<Msg><Res><Part><Att><TheftRelevance>";
    String MSG_THEFT_REL_INFO = MSG_ATT + "<TheftRelevanceInformation>";  //"<Msg><Res><Part><Att><TheftRelevanceInformation>";
    String MSG_FDOK_REL = MSG_ATT + "<FDOKRelevance>";  //"<Msg><Res><Part><Att><FDOKRelevance>";
    String MSG_ESD_IND = MSG_ATT + "<ESDIndicator>";  //"<Msg><Res><Part><Att><ESDIndicator>";
    // DAIMLER-12220, Neue Teilestammattribute aus PRIMUS
    String MSG_WEIGHT = MSG_ATT + "<GrossWeight>";  //"<Msg><Res><Part><Att><GrossWeight>";
    String MSG_LENGTH = MSG_ATT + "<VolKeyL>";  //"<Msg><Res><Part><Att><VolKeyL>";
    String MSG_WIDTH = MSG_ATT + "<VolKeyW>";  //"<Msg><Res><Part><Att><VolKeyW>";
    String MSG_HEIGHT = MSG_ATT + "<VolKeyH>";  //"<Msg><Res><Part><Att><VolKeyH>";
    String MSG_VOLUME = MSG_ATT + "<Volume>";  //"<Msg><Res><Part><Att><Volume>";
    // DAIMLER-14252, Import Gefahrgutkenner aus PRIMUS
    String MSG_HAZ_GOOD_IND = MSG_ATT + "<HazGoodInd>";  //"<Msg><Res><Part><Att><HazGoodInd>";

}
