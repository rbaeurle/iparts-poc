/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Repräsentiert einen Engine-(Motor-)Ident mit Hilfsmethoden.
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public class EngineIdent extends AggregateIdent {

    public static final String TYPE = "DA_EngineIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.ENGINE_NEW;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.ENGINE;

    private enum INDEX {ENGINEIDENT}

//    private boolean isNewEngineIdent;

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param engineIdent
     */
    public EngineIdent(EtkProject project, String engineIdent) {
        super(project, TYPE, engineIdent, AGG_TYPE);
//        this.isNewEngineIdent = true;
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public EngineIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getEngineIdent() {
        return id[INDEX.ENGINEIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getEngineIdent() + ") ENGINEIDENT";
    }

//
//    public void setNewEngineIdent(boolean newEngineIdent) {
//        isNewEngineIdent = newEngineIdent;
//    }

    @Override
    public String getFormattedIdent() {
        String result;
        if (isNewIdentSpecification()) {
            result = extractModelNumber() + " " + extractFactorySign() + " " + extractSerialNumberString();
        } else {
            result = extractModelNumber() + " " + extractSteeringKey() + " " + extractTransmissionKey() + " " + extractSerialNumberString();
        }
        if (isExchangeAggregate()) {
            result = result + "  (" + TranslationHandler.translate(DCAggregateTypes.EXCHANGE_ENGINE) + ")";
        }
        return result;
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        if (isNewIdentSpecification()) {
            String factorySign = extractFactorySign();
            String factoryKeyDescription = setValueWithDefault(getShowText(IDENT_TYPE, factorySign));
            result.add(new String[]{ "!!Montagewerk", factorySign, factoryKeyDescription });

            addDisplayGridValuesForSerialNumber(result, "!!Motor-Endnummer", true);
        } else {
            String steeringKey = extractSteeringKey();
            String steeringKeyDescription = setValueWithDefault(SteeringIdentKeys.getSteeringIdentKeyByValue(steeringKey).getDescription());
            result.add(new String[]{ "!!Lenkung/Zerlegungsgrad", steeringKey, steeringKeyDescription });

            String transmissionKey = extractTransmissionKey();
            String transmissionKeyDescription = setValueWithDefault(TransmissionIdentKeys.getTransmissionIdentKeyByValue(transmissionKey).getDescription());
            result.add(new String[]{ "!!Getriebe/Kupplung/Tauschmotor", transmissionKey, transmissionKeyDescription });

            addDisplayGridValuesForSerialNumber(result, "!!Motor-Zählnummer", true);
        }
        return result;
    }

    public boolean isSteeringKeyValid() {
        return SteeringIdentKeys.isValid(extractSteeringKey(), getEngineIdent(), true);
    }

    public boolean isTransmissionKeyValid() {
        String transmissionKey = extractTransmissionKey();
        if (StrUtils.isValid(transmissionKey)) {
            return TransmissionIdentKeys.getTransmissionIdentKeyByValue(transmissionKey) != TransmissionIdentKeys.TRANSMISSION_IDENT_UNKNOWN;
        }
        return false;
    }

    public String getSteeringKey() {
        if (isSteeringKeyValid()) {
            return extractSteeringKey();
        }
        return SteeringIdentKeys.STEERING_INDEPENDENT;
    }

    /**
     * Liefert den Lenkungskenner als dazugehörigen DB Kenner zurück.
     * "1" oder "5" -> Linkslenker
     * "2" oder "6" -> Rechtslenker
     *
     * @return "L" für Linkslenker
     * "R" für Rechtslenker
     */
    public String getSteeringEnumKey() {
        String steeringKey = extractSteeringKey();
        if (StrUtils.isValid(steeringKey)) {
            SteeringIdentKeys steeringIdentKeyByValue = SteeringIdentKeys.getSteeringIdentKeyByValue(steeringKey);
            return steeringIdentKeyByValue.getSteeringEnumKey();
        }
        return SteeringIdentKeys.STEERING_INDEPENDENT;
    }

    public String getTransmissionKey() {
        if (isTransmissionKeyValid()) {
            return extractTransmissionKey();
        }
        return "";
    }

    public String getTransmissionEnumKey() {
        String transmissionKey = extractTransmissionKey();
        if (StrUtils.isValid(transmissionKey)) {
            TransmissionIdentKeys transmissionIdentKeyByValue = TransmissionIdentKeys.getTransmissionIdentKeyByValue(transmissionKey);
            return transmissionIdentKeyByValue.getTransmissionEnumKey();
        }
        return "";
    }

    @Override
    public boolean isValidId() {
        if (isNewIdentSpecification()) {
            return isModelNumberValid() && isFactorySignValid() && isSerialNumberValid();
        } else {
            return isModelNumberValid() && isSteeringKeyValid() && isTransmissionKeyValid() && isSerialNumberValid();
        }
    }

    /**
     * Befüllt die Werke Combobox
     */
    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        if (isNewIdentSpecification()) {
            fillFactoryCombobox(comboboxFactory, IDENT_TYPE);
        } else {
            fillFactoryCombobox(comboboxFactory, "");
        }
    }

    @Override
    public String extractFactorySign() {
        if (isNewIdentSpecification()) {
            return StrUtils.copySubString(getEngineIdent(), 6, 1);
        }
        return "";
    }

    private String extractSteeringKey() {
        if (isOldIdentSpecification()) {
            return StrUtils.copySubString(getEngineIdent(), 6, 1);
        }
        return "";
    }

    private String extractTransmissionKey() {
        if (isOldIdentSpecification()) {
            return StrUtils.copySubString(getEngineIdent(), 7, 1);
        }
        return "";
    }

    @Override
    public String extractSerialNumberString() {
        if (isNewIdentSpecification()) {
            return StrUtils.copySubString(getEngineIdent(), 7, getSerialNumberLength());
        } else {
            return StrUtils.copySubString(getEngineIdent(), 8, getSerialNumberLength());
        }
    }

    @Override
    public int getSerialNumberLength() {
        if (isNewIdentSpecification()) {
            return 7;
        } else {
            // Bei der alten Systematik kann es vorkommen, dass Motor-Idents 15 Stellen besitzen. Ist das der Fall, dann
            // dürfen wir die letzte Stelle nicht abnscheiden.
            return (getIdent().length() == 15) ? 7 : 6;
        }
    }

    /**
     * Handelt es sich um einen Austauschmotor?
     *
     * @return
     */
    @Override
    public boolean isExchangeAggregate() {
        // mit DAIMLER-6024: beide Idents sind an der Stelle 8 als Austauschmotor gekennzeichnet
        String exchangeChar = StrUtils.copySubString(getEngineIdent(), 7, 1);
        return exchangeChar.equals("7") || exchangeChar.equals("8") || exchangeChar.equals("9");
    }
}
