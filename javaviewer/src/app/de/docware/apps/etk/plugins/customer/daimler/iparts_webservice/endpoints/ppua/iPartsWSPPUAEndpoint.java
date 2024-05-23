/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ppua;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPPUAList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext.iPartsWSSearchPartsWOContextEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPPUAInformation;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResultWithIdent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Endpoint für den PPUA-Webservice
 */
public class iPartsWSPPUAEndpoint extends iPartsWSAbstractEndpoint<iPartsWSPPUARequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/ppuaData";
    public static final int DEFAULT_MAX_PART_NUMBERS_INPUT = 100;

    public iPartsWSPPUAEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if (dataType == iPartsDataChangedEventByEdit.DataType.MATERIAL) {
            clearCaches();
        }
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSPPUARequest requestObject) throws RESTfulWebApplicationException {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        iPartsWSPPUAResponse iPartsWSPPUAResponse = new iPartsWSPPUAResponse();
        List<iPartsWSPartResultWithIdent> iPartsWSPartResultList = new DwList<>();

        // partNos ist hier bereits geprüft, nicht null und nicht leer -> LinkedHashSet verwenden, um doppelte Teilenummern
        // zu eliminieren, aber die ursprüngliche Reihenfolge zu erhalten
        Set<String> partNos = new LinkedHashSet<>(requestObject.getParts());
        Map<String, List<iPartsWSPPUAInformation>> ppuaInformationMap = new LinkedHashMap<>();
        partNos.stream()
                .filter(StrUtils::isValid)
                .forEach((partNo) -> {
                    // PPUA Informationen für eine Teilenummer ermitteln und einer Map hinzufügen
                    List<iPartsWSPPUAInformation> ppuaInformationList = searchPPUADataToPartNo(project, partNo, requestObject);
                    if (!ppuaInformationList.isEmpty()) {
                        ppuaInformationMap.put(partNo, ppuaInformationList);
                    }
                });

        // Merge Stammdaten mit PPUA Informationen, falls es zu einer Teilenummer PPUA Informationen gibt
        if (!ppuaInformationMap.isEmpty()) {
            ppuaInformationMap.forEach((partNo, ppuaInformationList) -> {
                iPartsWSPartResultList.addAll(searchPartMasterDataToPartNo(project, partNo, ppuaInformationList));
            });
        }

        iPartsWSPPUAResponse.setResults(iPartsWSPartResultList);

        return iPartsWSPPUAResponse;
    }

    /**
     * DB-Suche nach Stammdaten zu einer Materialnummer.
     * Befüllt das übergebene Response Objekt mit bereits gefundenen PPUA Informationen
     *
     * @param project
     * @param partNo
     * @param ppuaInformationList
     * @return
     */
    private List<iPartsWSPartResultWithIdent> searchPartMasterDataToPartNo(EtkProject project, String partNo, List<iPartsWSPPUAInformation> ppuaInformationList) {
        List<iPartsWSPartResultWithIdent> partResultList = iPartsWSSearchPartsWOContextEndpoint.searchPartMasterDataToPartNo(project, partNo);
        for (iPartsWSPartResultWithIdent partResult : partResultList) {
            // PPUA Informationen
            partResult.setPpuaInformation(ppuaInformationList);
        }

        return partResultList;
    }

    /**
     * DB-Suche nach PPUA-Daten zu einer Materialnummer.
     *
     * @param project
     * @param partNo
     * @param requestObject
     * @return
     */
    private List<iPartsWSPPUAInformation> searchPPUADataToPartNo(EtkProject project, String partNo, iPartsWSPPUARequest requestObject) {
        iPartsDataPPUAList ppuaList = new iPartsDataPPUAList();

        // ArrayList ist eine geordnete Collection. Die Reihenfolge entspricht der Reihenfolge des Hinzufügens.
        // So wird das lästige Festlegen der Array-Größe vermieden.
        List<String> whereFields = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();

        whereFields.add(iPartsConst.FIELD_DA_PPUA_PARTNO);
        whereValues.add(partNo);

        // Typkennzahl-Filter
        if (StrUtils.isValid(requestObject.getModelTypeId())) {
            whereFields.add(iPartsConst.FIELD_DA_PPUA_SERIES);
            whereValues.add(requestObject.getModelTypeId());
        }

        // Divisions-Filter
        if (StrUtils.isValid(requestObject.getDivision())) {
            whereFields.add(iPartsConst.FIELD_DA_PPUA_ENTITY);
            whereValues.add(requestObject.getDivision());
        }

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_PARTNO, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_SERIES, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_REGION, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_ENTITY, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_YEAR, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PPUA, iPartsConst.FIELD_DA_PPUA_VALUE, false, false));

        ppuaList.searchSortAndFill(project, iPartsConst.TABLE_DA_PPUA, ArrayUtil.toStringArray(whereFields), ArrayUtil.toStringArray(whereValues),
                                   new String[]{ iPartsConst.FIELD_DA_PPUA_REGION, iPartsConst.FIELD_DA_PPUA_SERIES, iPartsConst.FIELD_DA_PPUA_ENTITY,
                                                 iPartsConst.FIELD_DA_PPUA_YEAR, iPartsConst.FIELD_DA_PPUA_TYPE },
                                   DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);

        List<iPartsWSPPUAInformation> ppuaInformationList = new DwList<>(ppuaList.size());
        ppuaList.getAsList().stream()
                .filter((ppuaData) -> {
                    // Filterung der Regionen, nur wenn vorhanden
                    if (Utils.isValid(requestObject.getRegions())) {
                        return requestObject.getRegions().contains(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_REGION));
                    }
                    return true;
                }).forEach((ppuaData) -> {
                    iPartsWSPPUAInformation ppuaInformation = new iPartsWSPPUAInformation();
                    ppuaInformation.setModelTypeId(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_SERIES));
                    ppuaInformation.setRegion(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_REGION));
                    ppuaInformation.setDivision(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_ENTITY));
                    ppuaInformation.setType(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_TYPE));
                    ppuaInformation.setYear(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_YEAR));
                    ppuaInformation.setQuantity(ppuaData.getFieldValue(iPartsConst.FIELD_DA_PPUA_VALUE));
                    ppuaInformationList.add(ppuaInformation);
                });

        return ppuaInformationList;
    }
}