/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;

import java.util.Set;

/**
 * Connector-Interface für das Editieren von Modulen in iParts.
 */
public interface EditModuleFormIConnector extends EditFormIConnector, AssemblyListFormIConnector, AssemblyImageFormIConnector {

    EditModuleFormIConnector cloneMe(boolean cloneSelectedPartListEntries);

    String getPosFieldName();

    Set<String> getHotspotSet();

    iPartsDataAuthorOrder getCurrentDataAuthorOrder();

    void setCurrentDataAuthorOrder(iPartsDataAuthorOrder currentDataAuthorOrder);

    String getAdditionalTextForHeader();

    void setAdditionalTextForHeader(String kemNo);

    Set<String> getPosNumberSet();

    void updatePictureOrderList();

    void savePartListEntries(AbstractJavaViewerForm changeEventSender, boolean fireDataChangedEvent);

    void setPictureOrderList(iPartsDataPicOrderList pictrureOrderList);

    iPartsDataPicOrderList getPictureOrderList();

    boolean isFlagPictureOrderChanged();

    /**
     * Setzt das Flag, dass eine Positionsnummer verändert wurde. Danach muss
     * {@link #updateAllViews(de.docware.apps.etk.base.forms.AbstractJavaViewerForm, boolean)}.
     * aufgerufen werden
     */
    void posNumberChanged();

    boolean isFlagPosNumberChanged();

    boolean isAlphaNumHotspotAllowed();

    EtkDisplayFields getAssemblyListDisplayFields();

    void setAssemblyListDisplayFields(EtkDisplayFields displayFields);

    Set<String> getHotSpotSetPerImage(EtkDataImage image);
}
