/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.hierarchycalcualtion;

import java.util.List;

/**
 * Klasse zum Halten der Information während der Korrektur der AS Strukturen innerhalb eines Berechnungsastes
 */
public class HierarchyCorrectionData {

    private final List<HierarchyNode> dataWithoutASEntries;
    private final HierarchyNode hierarchyNode;
    private int asHotSpotStart;
    private String asSeqNoStart;


    public HierarchyCorrectionData(HierarchyNode hierarchyNode, String asSeqNoStart, int asHotSpotStart, List<HierarchyNode> dataWithoutASEntries) {
        this.hierarchyNode = hierarchyNode;
        this.asHotSpotStart = asHotSpotStart;
        this.asSeqNoStart = asSeqNoStart;
        this.dataWithoutASEntries = dataWithoutASEntries;
    }

    public int getAsHotSpotStart() {
        return asHotSpotStart;
    }

    public String getAsSeqNoStart() {
        return asSeqNoStart;
    }

    public void setAsSeqNoStart(String asSeqNoStart) {
        this.asSeqNoStart = asSeqNoStart;
    }

    public List<HierarchyNode> getDataWithoutASEntries() {
        return dataWithoutASEntries;
    }

    public HierarchyNode getHierarchyData() {
        return hierarchyNode;
    }

    public void setAsHotSpotStart(int asHotSpotStart) {
        this.asHotSpotStart = asHotSpotStart;
    }

    public void addHierarchyDataForHotSpotCalculation(HierarchyNode hierarchyNode) {
        if (hierarchyNode.hasSourceEntries()) {
            dataWithoutASEntries.add(hierarchyNode);
        }
    }

    /**
     * Erzeugt ein {@link HierarchyCorrectionData} Objekt mit den gleichen Infos aber einem anderen {@link HierarchyNode}
     *
     * @param nextData
     * @return
     */
    public HierarchyCorrectionData createChildInformation(HierarchyNode nextData) {
        return new HierarchyCorrectionData(nextData, getAsSeqNoStart(), getAsHotSpotStart(), getDataWithoutASEntries());
    }
}
