/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO für eine Ersetzungskette
 */
public class iPartsWSReplacementInfo implements RESTfulTransferObjectInterface {

    private List<iPartsWSReplacementPart> predecessorParts;
    private List<iPartsWSReplacementPart> successorParts;

    public iPartsWSReplacementInfo() {
    }

    public List<iPartsWSReplacementPart> getPredecessorParts() {
        return predecessorParts;
    }

    public void setPredecessorParts(List<iPartsWSReplacementPart> predecessorParts) {
        this.predecessorParts = predecessorParts;
    }

    public List<iPartsWSReplacementPart> getSuccessorParts() {
        return successorParts;
    }

    public void setSuccessorParts(List<iPartsWSReplacementPart> successorParts) {
        this.successorParts = successorParts;
    }

    public void addSuccessors(List<iPartsWSReplacementPart> successors) {
        if ((successors != null) && !successors.isEmpty()) {
            if (successorParts == null) {
                successorParts = new ArrayList<>();
            }
            successorParts.addAll(successors);
        }
    }

    public void addPredecessors(List<iPartsWSReplacementPart> predecessors) {
        if ((predecessors != null) && !predecessors.isEmpty()) {
            if (predecessorParts == null) {
                predecessorParts = new ArrayList<>();
            }
            predecessorParts.addAll(predecessors);
        }
    }
}
