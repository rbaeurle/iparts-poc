/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 * Aggregate Idents mit Beschreibung
 */
public class DatacardIdentOrderElem {

    private DatacardIdentOrderTypes type;
    private AggregateIdent aggregateIdent;

    public DatacardIdentOrderElem(EtkProject project, DatacardIdentOrderTypes type) {
        this.type = type;
        switch (type) {
            case VIN:
                aggregateIdent = new VinId_Agg(project, "");
                break;
            case FIN:
                aggregateIdent = new FinId_Agg(project, "");
                break;
            case ENGINE_NEW:
            case ENGINE_OLD:
                aggregateIdent = new EngineIdent(project, "");
                aggregateIdent.setIdentSpecification(type.getSpecification());
                break;
            case TRANSMISSION:
            case TRANSMISSION_AUTOMATED:
            case TRANSMISSION_MECHANICAL:
            case TRANSFER_CASE:
                aggregateIdent = new TransmissionIdent(project, "");
                break;
            case AXLE_REAR_NEW:
            case AXLE_REAR_OLD:
            case AXLE_FRONT_NEW:
            case AXLE_FRONT_OLD:
                aggregateIdent = new AxleIdent(project, "");
                aggregateIdent.setIdentSpecification(type.getSpecification());
                break;
            case CAB:
                aggregateIdent = new CabIdent(project, "");
                break;
            case AFTER_TREATMENT:
                aggregateIdent = new ATSIdent(project, "");
                break;
            case STEERING:
                aggregateIdent = new SteeringIdent(project, "");
                break;
            case PLATFORM:
                aggregateIdent = new PlatformIdent(project, "");
                break;
            case HIGH_VOLTAGE_BATTERY:
                aggregateIdent = new HighVoltageBatIdent(project, "");
                break;
            case ELECTRO_ENGINE:
                aggregateIdent = new ElectroEngineIdent(project, "");
                break;
            case FUEL_CELL:
                aggregateIdent = new FuelCellIdent(project, "");
                break;
            default:
                aggregateIdent = new SimpleIdent(project, "", DCAggregateTypes.UNKNOWN, DatacardIdentOrderTypes.UNKNOWN);
        }
    }

    public DatacardIdentOrderTypes getType() {
        return type;
    }

    public AggregateIdent getAggregateIdent() {
        return aggregateIdent;
    }

    public String getDescription() {
        if (aggregateIdent != null) {
            switch (type) {
                case VIN:
                    return TranslationHandler.translate("!!VIN");
                case FIN:
                    return TranslationHandler.translate("!!FIN");
                default:
                    String descr = aggregateIdent.project.getEnumText(iPartsConst.ENUM_KEY_AGGREGATE_TYPE,
                                                                      type.getDbValue(), aggregateIdent.project.getViewerLanguage(), true);
                    switch (aggregateIdent.getIdentSpecification()) {
                        case 1:
                            descr = TranslationHandler.translate("!!%1 (%2)", descr, TranslationHandler.translate("!!neu"));
                            break;
                        case 2:
                            descr = TranslationHandler.translate("!!%1 (%2)", descr, TranslationHandler.translate("!!alt"));
                            break;
                    }
                    return descr;
            }
        }
        return "";
    }

    public boolean isOldIdent() {
        return type.isOldIdent();
    }

    public boolean isNewIdent() {
        return type.isNewIdent();
    }

    public boolean isVIScallAllowed() {
        return type.isVIScallAllowed();
    }
}
