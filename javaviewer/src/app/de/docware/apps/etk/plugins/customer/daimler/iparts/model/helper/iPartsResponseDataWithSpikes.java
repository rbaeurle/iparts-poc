/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ResponseDataHelper;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class iPartsResponseDataWithSpikes {

    private iPartsDataResponseData responseData;
    private List<iPartsDataResponseSpike> responseSpikes;

    public iPartsResponseDataWithSpikes(iPartsDataResponseData responseData) {
        this.responseData = responseData;
        this.responseSpikes = new DwList<>();
    }

    public void addResponseSpikes(Collection<iPartsDataResponseSpike> spikes) {
        if (spikes != null) {
            this.responseSpikes.addAll(spikes);
        }
    }

    public void addResponseSpikes(Set<String> spikes, EtkDataPartListEntry partListEntry, EtkProject project) {
        if ((responseData != null) && (spikes != null)) {
            String pem = responseData.getAsId().getPem();
            String ident = responseData.getAsId().getIdent();
            if (StrUtils.isValid(pem, ident)) {
                Set<iPartsDataResponseSpike> responseSpikes = iPartsResponseSpikes.getInstance(project).getResponseSpikes(pem, ident);
                if (responseSpikes != null) {
                    // ab hier mit geklonten Aureißern arbeiten, da sonst die Daten im Cache manipuliert werden
                    responseSpikes = iPartsResponseSpikes.cloneResponseSpikes(project, responseSpikes);

                    ResponseDataHelper.filterResponseSpikes(responseSpikes, partListEntry); // nach BR und AA filtern
                    for (String spike : spikes) {
                        for (iPartsDataResponseSpike responseSpike : responseSpikes) {
                            if (responseSpike.getAsId().getSpikeIdent().equals(spike)) {
                                this.responseSpikes.add(responseSpike);
                            }
                        }
                    }
                }
            }
        }
    }

    public iPartsDataResponseData getResponseData() {
        return responseData;
    }

    public List<iPartsDataResponseSpike> getResponseSpikes() {
        return responseSpikes;
    }


}