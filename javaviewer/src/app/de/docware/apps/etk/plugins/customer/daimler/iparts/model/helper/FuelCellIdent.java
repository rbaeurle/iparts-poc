package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Stelle 1-6 Motorbaumuster
 * Stelle 7-9 Zählnummer
 * zusammen: IMO Baumusternummer
 * Stelle 10-15 Motor Endnummer
 * Werk soll leer bleiben
 * https://jira.docware.de/browse/DAIMLER-13404
 */
public class FuelCellIdent extends AggregateIdent {

    public static final String TYPE = "DA_FuelCellIdent";
    public static final DatacardIdentOrderTypes IDENT_TYPE = DatacardIdentOrderTypes.FUEL_CELL;
    public static final DCAggregateTypes AGG_TYPE = DCAggregateTypes.FUEL_CELL;

    private enum INDEX {FUELCELLIDENT}

    /**
     * Der normale Konstruktor
     *
     * @param project
     * @param fuelCellIdent
     */
    public FuelCellIdent(EtkProject project, String fuelCellIdent) {
        super(project, TYPE, fuelCellIdent, AGG_TYPE);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public FuelCellIdent() {
        this(null, "");
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return IDENT_TYPE;
    }

    public String getFuelCellIdent() {
        return id[INDEX.FUELCELLIDENT.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getFuelCellIdent() + ") FUELCELLIDENT";
    }

    @Override
    public String getFormattedIdent() {
        String result = extractModelNumber() + " " + extractModelCountNumber() + " " + extractSerialNumberString();
        if (isExchangeAggregate()) {
            result = result + "  (" + TranslationHandler.translate(DCAggregateTypes.EXCHANGE_AGGREGATE) + ")";
        }
        return result;
    }

    @Override
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = super.getDisplayGridValues();
        result.add(new String[]{ "!!Baumuster-Zählnummer", extractModelCountNumber(), extractModelCountNumber() });
        addDisplayGridValuesForSerialNumber(result, "!!Brennstoffzelle-Endnummer", true);
        return result;
    }

    public boolean isValidId() {
        return isModelNumberValid() && isSerialNumberValid();
    }

    @Override
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, IDENT_TYPE);
    }

    @Override
    public String extractFactorySign() {
        return "";
    }

    @Override
    public String extractSerialNumberString() {
        return StrUtils.copySubString(getFuelCellIdent(), 9, getSerialNumberLength());
    }

    @Override
    public int getSerialNumberLength() {
        return 6;
    }

    @Override
    public boolean isExchangeAggregate() {
        return false; // Aktuell gibt es keine Austausch-Brennstoffzellen
    }

    private String extractModelCountNumber() {
        return StrUtils.copySubString(getFuelCellIdent(), 6, 3);
    }
}
