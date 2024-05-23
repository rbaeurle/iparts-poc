/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageForm;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserSettingsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;

import java.util.HashSet;
import java.util.Set;

/**
 * iParts-spezifische Erweiterung vom {@link AssemblyImageForm}.
 */
public class iPartsAssemblyImageForm extends AssemblyImageForm {

    /**
     * Falls in den Benutzereinstellungen gespeichert bzw. über das Produkt festgelegt ist, dass keine SVGs bevorzugt werden
     * sollen, muss über {@link AssemblyImageFormIConnector#setPreferSVGImages(boolean)} beim Laden der Zeichnungen indirekt
     * {@code IMAGE_USAGE_2D_FILLED} anstatt {@code IMAGE_USAGE_SVG} verwendet werden.
     *
     * @param form
     */
    protected static void modifyConnectorForPreferSVGImages(AssemblyImageForm form) {
        // SVG bevorzugen?
        AssemblyImageFormIConnector connector = form.getConnector();
        EtkProject project = form.getProject();
        EtkDataAssembly currentAssembly;
        boolean preferSVGs = false;
        if (connector instanceof AssemblyListFormIConnector) {
            currentAssembly = ((AssemblyListFormIConnector)connector).getCurrentAssembly();
            if (EditModuleHelper.isCarPerspectiveAssembly(currentAssembly)) {
                // Bei Navigationsmodulen sollen SVGs immer bevorzugt werden
                preferSVGs = true;
            } else {
                if (project.getUserSettings().getBoolValues(iPartsUserSettingsConst.REL_SVG_IS_PREFERRED)) {
                    if (currentAssembly instanceof iPartsDataAssembly) {
                        iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)currentAssembly;
                        // Prüfen, ob das Flag "SVGs bevorzugen" am Produkt bzw. allen Produkten einer freien SA gesetzt ist
                        iPartsProductId productId = iPartsAssembly.getProductIdFromModuleUsage();
                        if (productId != null) { // Prüfung für normale TUs
                            preferSVGs = iPartsProduct.getInstance(project, productId).isPreferSVGs();
                        } else { // Prüfungen für freie SAs
                            iPartsSAId saId = iPartsAssembly.getSAId();
                            if (saId != null) {
                                Set<iPartsProductId> saProductIds = iPartsSA.getInstance(project, saId).getProductIdsToKGsMap(project).keySet();
                                Set<Boolean> productsPreferSVGsSet = new HashSet<>();
                                for (iPartsProductId saProductId : saProductIds) {
                                    productsPreferSVGsSet.add(iPartsProduct.getInstance(project, saProductId).isPreferSVGs());
                                    if (productsPreferSVGsSet.size() > 1) {
                                        break;
                                    }
                                }

                                // Wenn an allen Produkten, zu denen die freie SA zugeordnet ist, der Wert für SVG bevorzugen
                                // einheitlich ist, soll dieser verwendet werden.
                                // Wenn der Wert nicht einheitlich ist, dann soll die Benutzereinstellung ziehen (hier immer true).
                                if (productsPreferSVGsSet.size() == 1) {
                                    preferSVGs = productsPreferSVGsSet.iterator().next();
                                } else {
                                    preferSVGs = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        connector.setPreferSVGImages(preferSVGs);
    }

    public iPartsAssemblyImageForm(AssemblyImageFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
    }

    public iPartsAssemblyImageForm(AssemblyImageFormIConnector dataConnector, AbstractJavaViewerForm parentForm, int maxNumberOfPixels,
                                   int hotspotPadding) {
        super(dataConnector, parentForm, maxNumberOfPixels, hotspotPadding);
    }

    @Override
    public void modifyConnectorBeforeUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.modifyConnectorBeforeUpdateData(sender, forceUpdateAll);
        modifyConnectorForPreferSVGImages(this);
    }
}