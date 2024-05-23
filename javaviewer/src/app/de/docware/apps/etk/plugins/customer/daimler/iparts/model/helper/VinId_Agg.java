/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsVINModelMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Repräsentiert eine VIN-ID als Ableitung eines Aggregate-Idents mit Hilfsmethoden.
 * Um den Code nicht zu verdoppeln, wurde die VINId als local Member mit aufgenommen
 */
public class VinId_Agg extends AggregateIdent {

    public static final String TYPE = "DA_VinId";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.VIN;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.VEHICLE;

    private enum INDEX {VIN}

    @JsonProperty
    private VinId _vinId;

    /**
     * Der normale Konstruktor
     *
     * @param vin
     */
    public VinId_Agg(EtkProject project, String vin) {
        super(project, TYPE, vin, AGG_TYPE);
        _vinId = new VinId(vin);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public VinId_Agg() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getVIN() {
        return _vinId.getVIN();
    }

    @Override
    public String getIdent() {
        return _vinId.getVIN();
    }

    @Override
    public void setIdent(String ident) {
        _vinId = new VinId(ident);
        id[0] = ident;
    }

    @Override
    public String toString() {
        return _vinId.toString();
    }

    @Override
    public String getFormattedIdent() {
        return extractWMI() + " " + extractCodedSeries() + " " + extractTypeOfDrive() + " " +
               extractPartOfModel() + " " + extractRestraintSystem() + " " + extractCheckDigit() + " " +
               extractModelYearKey() + " " + extractFactorySign() + " " + extractSerialNumberString();
    }

    public List<String[]> getDisplayGridValues() {
        List<String[]> result = new DwList<String[]>();
        String helper = _vinId.getWorldManufacturerIdentifier();
        result.add(0, new String[]{ "!!WMI-Code", extractWMI(), setValueWithDefault(helper) });

        helper = _vinId.getCodedSeries();
        result.add(new String[]{ "!!Codierte Baureihe", extractCodedSeries(), setValueWithDefault(helper) });

        helper = _vinId.getTypeOfDrive();
        result.add(new String[]{ "!!Antriebsart", extractTypeOfDrive(), setValueWithDefault(helper) });

        helper = _vinId.getPartOfModel();
        result.add(new String[]{ "!!Stelle 6/7 des Baumusters", extractPartOfModel(), setValueWithDefault(helper) });

        helper = _vinId.getRestraintSystem();
        result.add(new String[]{ "!!Rückhaltesystem/Gewichtsklasse", extractRestraintSystem(), setValueWithDefault(helper) });

        helper = _vinId.getCheckDigit();
        result.add(new String[]{ "!!Prüfziffer", extractCheckDigit(), setValueWithDefault(helper) });

        helper = _vinId.getModelYearDigit();
        result.add(new String[]{ "!!Modeljahr-Schlüssel", extractModelYearKey(), setValueWithDefault(helper) });

        helper = _vinId.getFactorySign();
        result.add(new String[]{ "!!Herstellerwerk", extractFactorySign(), setValueWithDefault(helper) });

        addDisplayGridValuesForSerialNumber(result, "!!Fahrzeug-Endnummer", false);

        helper = StrUtils.stringListToString(iPartsVINModelMappingCache.getInstance(project).getAllModelsForVINPrefix(_vinId.getVIN()), ", ");
        String vinValues = setValueWithDefault(_vinId.getPrefixForModelMapping()) + " / " + setValueWithDefault(_vinId.getSuffixForModelMapping());
        result.add(new String[]{ "!!Mapping auf Baumuster", vinValues, setValueWithDefault(helper) });

        return result;
    }

    @Override
    public boolean isValidId() {
        return _vinId.isValidId();
    }

    @Override
    protected String extractModelNumber() {
        return "";
    }

    private String extractWMI() {
        return _vinId.extractWMI();
    }

    private String extractCodedSeries() {
        return _vinId.extractCodedSeries();
    }

    private String extractTypeOfDrive() {
        return _vinId.extractTypeOfDrive();
    }

    private String extractPartOfModel() {
        return _vinId.extractPartOfModel();
    }

    private String extractRestraintSystem() {
        return _vinId.extractRestraintSystem();
    }

    private String extractCheckDigit() {
        return _vinId.extractCheckDigit();
    }

    private String extractModelYearKey() {
        return _vinId.extractModelYearKey();
    }

    @Override
    public String extractFactorySign() {
        return _vinId.extractFactorySign();
    }

    @Override
    public String extractSerialNumberString() {
        return _vinId.extractSerialNumber();
    }

    @Override
    public int getSerialNumberLength() {
        return FinId.IDENT_NO_LENGTH;
    }

    @Override
    public boolean isSerialNumberValid() {
        return _vinId.isSerialNumberValid();
    }

    @Override
    public int getSerialNumber() {
        return _vinId.getSerialNumber();
    }

    @Override
    protected String getWMI() {
        return _vinId.getWorldManufacturerIdentifier();
    }

    @Override
    protected iPartsFactoryModel.SerialNoAndFactory getSerialNumberAndFactory() {
        return new iPartsFactoryModel.SerialNoAndFactory(getSerialNumber(), "", "");
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        comboboxFactory.switchOffEventListeners();
        comboboxFactory.removeAllItems();
        comboboxFactory.addItem("");
        String language = project.getViewerLanguage();

        for (iPartsDataFactories factory : iPartsFactories.getInstance(project).getDataFactories()) {
            comboboxFactory.addItem(factory.getFactoryNumber(), factory.getFactoryNumber() + " " + factory.getFactoryText(language));
        }
        comboboxFactory.switchOnEventListeners();
    }
}
