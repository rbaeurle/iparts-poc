/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helfer für den Import der Teilestammdaten (MAD, BOM-DB, DIALOG, PRIMUS)
 */
public class iPartsMaterialImportHelper {

    enum DIALOGMaterialTypes {
        TS1, TS2, TS6, TS7, GEWS, VTNR
    }

    private static final Map<String, Set<String>> materialFieldnames = new HashMap<>();

    static {
        // BOM-DB Teilestammimporter
        Set<String> bomDbMaterialFieldnames = new HashSet<>();
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_ASSEMBLYSIGN);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_CONST_DESC);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_IMAGESTATE);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_IMAGEDATE);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_RELATEDPIC);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_REFSER);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_RELEASESTATE);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_QUANTUNIT);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_VARIANT_SIGN);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_DOCREQ);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_WEIGHTCALC);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_SECURITYSIGN);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_CERTREL);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_NOTEONE);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_VEDOCSIGN);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_THEFTREL);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_THEFTRELINFO);
        bomDbMaterialFieldnames.add(iPartsConst.FIELD_M_FACTORY_IDS);
        materialFieldnames.put(iPartsImportDataOrigin.EDS.getOrigin(), bomDbMaterialFieldnames);

        // DIALOG TS1
        Set<String> dialogTS1MaterialFieldnames = new HashSet<>();
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_BESTNR);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_ASSEMBLYSIGN);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_IMAGESTATE);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_IMAGEDATE);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_RELATEDPIC);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_REFSER);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_RELEASESTATE);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_QUANTUNIT);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_VARIANT_SIGN);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_DOCREQ);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_WEIGHTCALC);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_SECURITYSIGN);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_ESD_IND);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_CERTREL);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_VEDOCSIGN);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_THEFTREL);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_THEFTRELINFO);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_ETKZ);
        dialogTS1MaterialFieldnames.add(iPartsConst.FIELD_M_VERKSNR);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.TS1), dialogTS1MaterialFieldnames);

        // DIALOG TS2
        Set<String> dialogTS2MaterialFieldnames = new HashSet<>();
        dialogTS2MaterialFieldnames.add(iPartsConst.FIELD_M_BESTNR);
        dialogTS2MaterialFieldnames.add(iPartsConst.FIELD_M_CONST_DESC);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.TS2), dialogTS2MaterialFieldnames);

        // DIALOG TS6
        Set<String> dialogTS6MaterialFieldnames = new HashSet<>();
        dialogTS6MaterialFieldnames.add(iPartsConst.FIELD_M_BESTNR);
        dialogTS6MaterialFieldnames.add(iPartsConst.FIELD_M_MATERIALFINITESTATE);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.TS6), dialogTS6MaterialFieldnames);

        // DIALOG TS7
        Set<String> dialogTS7MaterialFieldnames = new HashSet<>();
        dialogTS7MaterialFieldnames.add(iPartsConst.FIELD_M_BESTNR);
        dialogTS7MaterialFieldnames.add(iPartsConst.FIELD_M_CHANGE_DESC);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.TS7), dialogTS7MaterialFieldnames);

        // DIALOG VTNR
        Set<String> dialogVTNRMaterialFieldnames = new HashSet<>();
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_BESTNR);
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_ADDTEXT);
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_LAYOUT_FLAG);
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_TEXTNR);
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_INTERNAL_TEXT);
        dialogVTNRMaterialFieldnames.add(iPartsConst.FIELD_M_BASKET_SIGN);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.VTNR), dialogVTNRMaterialFieldnames);

        // DIALOG GEWS
        Set<String> dialogGEWSMaterialFieldnames = new HashSet<>();
        dialogGEWSMaterialFieldnames.add(iPartsConst.FIELD_M_WEIGHTREAL);
        dialogGEWSMaterialFieldnames.add(iPartsConst.FIELD_M_WEIGHTPROG);
        materialFieldnames.put(makeDIALOGSourceTypeKey(DIALOGMaterialTypes.GEWS), dialogGEWSMaterialFieldnames);

        // SAP MBS
        Set<String> mbsMaterialFieldnames = new HashSet<>();
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_ASSEMBLYSIGN);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_CONST_DESC);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_IMAGESTATE);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_IMAGEDATE);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_REFSER);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_QUANTUNIT);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_VARIANT_SIGN);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_SECURITYSIGN);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_CERTREL);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_NOTEONE);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_NOTETWO);
        mbsMaterialFieldnames.add(iPartsConst.FIELD_M_ETKZ_MBS);
        materialFieldnames.put(iPartsImportDataOrigin.SAP_MBS.getOrigin(), mbsMaterialFieldnames);
    }

    public static Set<String> getImportAffectedMaterialFieldnames(iPartsImportDataOrigin source) {
        if ((source == iPartsImportDataOrigin.DIALOG) && (materialFieldnames.get(source.getOrigin()) == null)) {
            Set<String> allMatFieldsForDIALOG = new HashSet<>();
            for (DIALOGMaterialTypes dialogMaterialTyp : DIALOGMaterialTypes.values()) {
                allMatFieldsForDIALOG.addAll(materialFieldnames.get(makeDIALOGSourceTypeKey(dialogMaterialTyp)));
            }
            materialFieldnames.put(iPartsImportDataOrigin.DIALOG.getOrigin(), allMatFieldsForDIALOG);
        }
        return materialFieldnames.get(source.getOrigin());
    }

    public static Set<String> getDIALOGMaterialSubFields(DIALOGMaterialTypes dialogMaterialTypes) {
        return materialFieldnames.get(makeDIALOGSourceTypeKey(dialogMaterialTypes));
    }

    public static boolean isAffectedMaterialField(iPartsImportDataOrigin source, String fieldname) {
        Set<String> importAffectedMaterialFieldnames = getImportAffectedMaterialFieldnames(source);
        if (importAffectedMaterialFieldnames == null) {
            return false;
        }
        return importAffectedMaterialFieldnames.contains(fieldname);
    }

    private static String makeDIALOGSourceTypeKey(DIALOGMaterialTypes subType) {
        return iPartsImportDataOrigin.DIALOG.getOrigin() + "_" + subType;
    }

    /**
     * Liefert zurück, ob der Teilestamm durch SRM verändert wurde (Text-ID) gesetzt
     *
     * @param part
     * @return
     */
    public static boolean hasSRMTextId(EtkDataPart part) {
        return part.containsFieldValueSetOfEnumValue(iPartsConst.FIELD_M_SOURCE, iPartsImportDataOrigin.SRM.getOrigin());
    }


}
