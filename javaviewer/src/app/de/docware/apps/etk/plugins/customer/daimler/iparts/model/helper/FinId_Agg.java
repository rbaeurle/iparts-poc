/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;

import java.util.List;

/**
 * Repräsentiert eine FIN-ID als Ableitung eines Aggregate-Idents mit Hilfsmethoden.
 * Um den Code nicht zu verdoppeln, wurde die FINId als local Member mit aufgenommen
 */
public class FinId_Agg extends AggregateIdent {

    public static final String TYPE = "DA_FinId";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.FIN;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.VEHICLE;

    private enum INDEX {FIN}

    @JsonProperty
    private FinId _finId;

    /**
     * Der normale Konstruktor
     *
     * @param fin
     */
    public FinId_Agg(EtkProject project, String fin) {
        super(project, TYPE, fin, AGG_TYPE);
        _finId = new FinId(fin);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public FinId_Agg() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getFIN() {
        return _finId.getFIN();
    }

    @Override
    public String getIdent() {
        return _finId.getFIN();
    }

    @Override
    public void setIdent(String ident) {
        _finId = new FinId(ident);
        id[0] = ident;
    }

    @Override
    public String toString() {
        return _finId.toString();
    }

    @Override
    public String getFormattedIdent() {
        return extractWMI() + " " + extractModelNumber() + " " + extractSteering() + " " + extractFactorySign() + " " + extractSerialNumberString();
    }

    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        String wmi = getWorldManufacturerIdentifier();
        result.add(0, new String[]{ "!!WMI-Code", wmi, setValueWithDefault(wmi) });

        String steeringKey = getSteering();
        String steeringDescr = setValueWithDefault(SteeringIdentKeys.getSteeringIdentKeyByValue(steeringKey).getDescription());
        result.add(new String[]{ "!!Lenkung", steeringKey, steeringDescr });

        String factorySign = getFactorySign();
        String factoryNumber = null;
        if (isWMIValid() && isFactorySignValid()) {
            factoryNumber = iPartsFactoryModel.getInstance(project).getFactoryNumberForWMI(extractWMI(),
                                                                                           getFactorySign(),
                                                                                           new iPartsModelId(getFullModelNo()), getSteering());
        }
        if (factoryNumber == null) {
            factoryNumber = "";
        }
        if (!factoryNumber.isEmpty()) {
            iPartsDataFactories dataFactories = iPartsFactories.getInstance(project).getDataFactoryByFactoryNumber(factoryNumber);
            if (dataFactories != null) {
                factoryNumber = factoryNumber + " " + dataFactories.getFactoryText(project.getViewerLanguage());
            }
        }
        result.add(new String[]{ "!!Werkskennbuchstabe", factorySign, setValueWithDefault(factoryNumber) });

        addDisplayGridValuesForSerialNumber(result, "!!Seriennummer", true);
        return result;
    }

    public String getWorldManufacturerIdentifier() {
        return _finId.getWorldManufacturerIdentifier();
    }

    public String getModelType() {
        return _finId.getModelType();
    }

    @Override
    public String getFullModelNo() {
        return _finId.getFullModelNumber();
    }

    public String getSteering() {
        return _finId.getSteering();
    }

    @Override
    public String extractFactorySign() {
        return _finId.extractFactorySign();
    }

    @Override
    public int getSerialNumber() {
        return _finId.getSerialNumber();
    }

    public boolean isWMIValid() {
        return _finId.isWMIValid();
    }

    public boolean isModelTypeValid() {
        return _finId.isModelTypeValid();
    }

    public boolean isSteeringValid(boolean checkOnlyIsLeftOrRightSteering) {
        return _finId.isSteeringValid(checkOnlyIsLeftOrRightSteering);
    }

    @Override
    public boolean isFactorySignValid() {
        return _finId.isFactorySignValid();
    }

    @Override
    public boolean isSerialNumberValid() {
        return _finId.isSerialNumberValid();
    }

    /**
     * Liefert den Lenkungskenner als dazugehörigen DB Kenner zurück.
     * "1" oder "5" -> Linkslenker
     * "2" oder "6" -> Rechtslenker
     *
     * @return "L" für Linkslenker
     * "R" für Rechtslenker
     */
    public String getLeftOrRightSteeringAsEnumKey() {
        return _finId.getLeftOrRightSteeringAsEnumKey();
    }


    /**
     * Überprüfung, ob alle Bestandteile der FIN syntaktisch korrekt sind
     *
     * - Gesamtlänge = 17
     * - ersten 3 Stellen sind Buchstaben
     * - gefolgt von 6 Ziffern
     * - die 10. Stelle darf nur "1","2","5", und "6" enthalten (Rechtslenker = 2,6, Linkslenker = 1,5)
     * - 11. Stelle = Buchstabe oder Ziffer
     * - 12-17 Ziffern
     *
     * @return
     */
    @Override
    public boolean isValidId() {
        return _finId.isValidId();
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


    private String extractWMI() {
        return _finId.extractWMI();
    }

    private String extractModelType() {
        return _finId.extractModelType();
    }

    @Override
    protected String extractModelNumber() {
        return _finId.extractModelNumber();
    }

    private String extractSteering() {
        return _finId.extractSteering();
    }

    @Override
    public String extractSerialNumberString() {
        return _finId.extractSerialNumber();
    }

    @Override
    public int getSerialNumberLength() {
        return FinId.IDENT_NO_LENGTH;
    }

    @Override
    protected String getWMI() {
        return _finId.getWorldManufacturerIdentifier();
    }
}