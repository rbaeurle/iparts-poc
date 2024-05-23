/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;

import java.util.List;

/**
 * Erzeugt anhand der virtualIds die passende Klasse, die für diese Ids die Inhalte implementiert
 */
public class iPartsVirtualAssemblyFactory {

    public static iPartsVirtualAssembly createVirtualAssembly(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        if (assemblyId.isVirtual()) {
            if (!virtualIds.isEmpty()) {
                iPartsVirtualNode rootNode = virtualIds.get(0);

                switch (rootNode.getType()) {
                    case ROOT:
                        return new iPartsVirtualAssemblyRoot(project, virtualIds, assemblyId);
                    case STRUCTURE:
                        return new iPartsVirtualAssemblyStructure(project, virtualIds, assemblyId);

                    case PRODUCT_EINPAS:
                    case PRODUCT_EINPAS_AGGS:
                    case PRODUCT_EINPAS_COMMON:
                        return new iPartsVirtualAssemblyProductEinPasKgTu(project, virtualIds, assemblyId);
                    case PRODUCT_KGTU:
                    case PRODUCT_KGTU_AGGS:
                    case PRODUCT_KGTU_COMMON:
                        return new iPartsVirtualAssemblyProductKgTu(project, virtualIds, assemblyId);

                    case DIALOG_EINPAS:
                        return new iPartsVirtualAssemblyDialogEinPas(project, virtualIds, assemblyId);
                    case DIALOG_HMMSM:
                        return new iPartsVirtualAssemblyDialogHmMSm(project, virtualIds, assemblyId);

                    case EDS_EINPAS:
                        return new iPartsVirtualAssemblyEdsEinPas(project, virtualIds, assemblyId);
                    case EDS_OPS:
                    case EDS_MODEL_ELEMENT_USAGE:
                        return new iPartsVirtualAssemblyEdsStructure(project, virtualIds, assemblyId);
                    case CTT_MODEL:
                        return new iPartsVirtualAssemblyCTT(project, virtualIds, assemblyId);
                    case MBS_STRUCTURE:
                        return new iPartsVirtualAssemblyMBS(project, virtualIds, assemblyId);
                }
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, assemblyId.toString() + " is not a valid virtual node ID.");
        }

        // Ist nicht virtuell
        return null;
    }
}
