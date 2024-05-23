/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelPropertiesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;

import java.util.Collection;

public class iPartsGuiConstModelSelectTextField extends iPartsGuiModelSelectTextField {

    public static final String TYPE = "iPartsGuiConstModelSelectTextField";

    private iPartsDataModelList asModelList;
    private iPartsDataModelPropertiesList constModelList;

    public iPartsGuiConstModelSelectTextField(EtkProject project, iPartsProductId productId) {
        super(project);
        setType(TYPE);
        asModelList = new iPartsDataModelList();
        constModelList = new iPartsDataModelPropertiesList();
        this.productId = productId;
    }

    @Override
    public void init(final AbstractJavaViewerForm parentForm) {
        if (parentForm != null) {
            addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    String seriesNo = getPartialModelNumberWithWildCard();
                    if (StrUtils.isValid(seriesNo)) {
                        seriesNo = StrUtils.replaceSubstring(seriesNo, "*", "");
                        Collection<String> modelNumbers = EditSelectConstModelsForm.showSelectionConstModels(parentForm, productId,
                                                                                                             seriesNo,
                                                                                                             isModelNumberSearchFieldVisible(),
                                                                                                             dataArray.getArrayAsStringList(),
                                                                                                             asModelList, constModelList);
                        if (modelNumbers != null) {
                            String arrayId = dataArray.getArrayId();
                            EtkDataArray newDataArray = new EtkDataArray(arrayId);
                            newDataArray.add(modelNumbers);
                            if (!setArray(newDataArray) && getASModelList().isModifiedWithChildren()) {
                                fireEvent(EventCreator.createOnChangeEvent(eventHandlerComponent, uniqueId));
                            }
                        }
                    }
                }
            });
        } else {
            removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    public void setPartialModelNumberWithWildCard(String partialModelNumberWithWildCard) {
        super.setPartialModelNumberWithWildCard(partialModelNumberWithWildCard);
    }

    public iPartsDataModelList getASModelList() {
        return asModelList;
    }

    public iPartsDataModelPropertiesList getConstModelList() {
        return constModelList;
    }
}
