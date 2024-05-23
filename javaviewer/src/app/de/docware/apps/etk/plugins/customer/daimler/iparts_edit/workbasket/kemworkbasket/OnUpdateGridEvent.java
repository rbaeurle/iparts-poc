package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.table.TableCellControlWithTextRepresentation;

/**
 * Interface für die Berechnung der virtuellen Felder und Erzeugung des Gui-Elements für eine Grid-Zelle
 * beim Update nach Vereinheitlichen in den Arbeitsvorräten
 */
public interface OnUpdateGridEvent {

    DBDataObjectAttributes doCalculateVirtualFields(EtkProject project, DBDataObjectAttributes attributes);

    TableCellControlWithTextRepresentation doCalcGuiElemForCell(EtkProject project, EtkDisplayField field, DBDataObjectAttributes attributes);

}
