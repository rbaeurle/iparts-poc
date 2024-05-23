package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

/**
 * Helper zum vergleichen von sortStrings bestehend aud HotSpot und GUID
 * Alphanumerische Hotspots können mit numerischen Hotspots verglichen werden
 */
public class EditSortStringforSourceGUIDHelper {

    public static String rightFill(String sourceStr, int newLen, char fillChar) {
        int addLength = newLen - sourceStr.length();
        StringBuilder sb = new StringBuilder(sourceStr);
        for (int i = 0; i < addLength; i++) {
            sb.append(fillChar);
        }
        return sb.toString();
    }

    private SearchContainer searchStringOne;
    private SearchContainer searchStringTwo;

    public EditSortStringforSourceGUIDHelper() {
        searchStringOne = new SearchContainer();
        searchStringTwo = new SearchContainer();
    }

    public EditSortStringforSourceGUIDHelper cloneMe() {
        EditSortStringforSourceGUIDHelper clone = new EditSortStringforSourceGUIDHelper();
        clone.searchStringOne = this.searchStringOne;
        clone.searchStringTwo = this.searchStringTwo;
        return clone;
    }

    public void clear() {
        clearCompareOne();
        clearCompareTwo();
    }

    public void clearCompareOne() {
        searchStringOne.clear();
    }

    public void clearCompareTwo() {
        searchStringTwo.clear();
    }

    public void setCompareOne(String sourceGUID, String hotspot) {
        searchStringOne = new SearchContainer(getHotspotSearchString(hotspot), getGUIDSearchString(sourceGUID), sourceGUID);
    }

    public void setCompareTwo(String sourceGUID, String hotspot) {
        searchStringTwo = new SearchContainer(getHotspotSearchString(hotspot), getGUIDSearchString(sourceGUID), sourceGUID);
    }

    public void setCompareTwoDirect(String sourceGUID, String hotspot) {
        searchStringTwo = new SearchContainer(getHotspotSearchString(hotspot), getGUIDSearchString(sourceGUID), sourceGUID);
    }

    private String getHotspotSearchString(String hotspot) {
        // Hotspot ist schon ein sortString
        if (hotspot.startsWith(".")) {
            return hotspot;
        }
        if (!hotspot.isEmpty()) {
            return Utils.toSortString(hotspot); // ggf. mit 0 auffüllen damit 190 auch nach 19 kommt
        }
        return "";
    }

    private String getGUIDSearchString(String sourceGUID) {
        if (!sourceGUID.isEmpty()) {
            iPartsDialogBCTEPrimaryKey key = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
            if (key != null) {
                return "|" + key.toString("|", true);
            }
        }
        return "";
    }

    public String getSortStringforSourceGUIDOne() {
        return searchStringOne.getSortStringforSourceGUIDDirect();
    }

    public String getSortStringforSourceGUIDTwo() {
        return searchStringTwo.getSortStringforSourceGUIDDirect();
    }

    public String getHotspotSearchStringSourceOne() {
        return searchStringOne.hotspotSearchString;
    }

    public String getGUIDSearchStringSourceOne() {
        return searchStringOne.guidSearchString;
    }

    public String getGUIDUnformattedSearchStringSourceOne() {
        return searchStringOne.guidSearchStringUnformatted;
    }

    public String getHotspotSearchStringSourceTwo() {
        return searchStringTwo.hotspotSearchString;
    }

    public String getGUIDSearchStringSourceTwo() {
        return searchStringTwo.guidSearchString;
    }

    public String getGUIDUnformattedSearchStringSourceTwo() {
        return searchStringTwo.guidSearchStringUnformatted;
    }

    private int prepareForCompare() {
        String hotspotSearchStringOne = searchStringOne.getHotspotSortString();
        String hotspotSearchStringTwo = searchStringTwo.getHotspotSortString();
        if (StrUtils.isValid(hotspotSearchStringOne, hotspotSearchStringTwo)) {
            return Math.max(hotspotSearchStringOne.length(), hotspotSearchStringTwo.length());
        }
        return -1;
    }


    /**
     * 10 wird zu ".00...10" und 10a zu ".00...10a" => 10a wird vor 10 einsortiert
     * Die Routine holt den konvertierten Hotspot aus dem sortString (von getHotSpotSearchString()) und füllt ggf die Strings rechts mit Blanks auf
     * 10 wird zu ".00...10 " und 10a zu ".00...10a" => compareTo funktioniert richtig: 10a wird nach 10 einsortiert
     *
     * @return
     */
    public int compareOneWithTwo() {
        int maxLen = prepareForCompare();
        return searchStringOne.getSortStringforSourceGUID(maxLen).compareTo(searchStringTwo.getSortStringforSourceGUID(maxLen));
    }

    /**
     * Zum Vergleich wird die unformatierte GUID herangezogen
     *
     * @return
     */
    public int compareOneWithTwoUnformatted() {
        int maxLen = prepareForCompare();
        return searchStringOne.getSortStringforSourceGUIDUnformatted(maxLen).compareTo(searchStringTwo.getSortStringforSourceGUIDUnformatted(maxLen));
    }

    public int compareTwoWithOne() {
        int maxLen = prepareForCompare();
        return searchStringTwo.getSortStringforSourceGUID(maxLen).compareTo(searchStringOne.getSortStringforSourceGUID(maxLen));
    }

    /**
     * Zum Vergleich wird die unformatierte GUID herangezogen
     *
     * @return
     */
    public int compareTwoWithOneUnformatted() {
        int maxLen = prepareForCompare();
        return searchStringOne.getSortStringforSourceGUIDUnformatted(maxLen).compareTo(searchStringTwo.getSortStringforSourceGUIDUnformatted(maxLen));
    }


    private class SearchContainer {

        private String hotspotSearchString;
        private String guidSearchString;
        private String guidSearchStringUnformatted;

        public SearchContainer(String hotspotSearchString, String guidSearchString, String guidunformattedSearchString) {
            this.hotspotSearchString = hotspotSearchString;
            this.guidSearchString = guidSearchString;
            this.guidSearchStringUnformatted = guidunformattedSearchString;
        }

        public SearchContainer() {
            this("", "", "");
        }

        public void clear() {
            this.hotspotSearchString = "";
            this.guidSearchString = "";
        }

        public String getSortStringforSourceGUIDDirect() {
            return hotspotSearchString + guidSearchString;
        }

        public String getSortStringforSourceGUID(int maxLen) {
            return getHotspotSortString(maxLen) + guidSearchString;
        }

        public String getSortStringforSourceGUIDUnformatted(int maxLen) {
            return getHotspotSortString(maxLen) + guidSearchStringUnformatted;
        }

        public String getHotspotSortString() {
            return hotspotSearchString;
        }

        public String getHotspotSortString(int maxLen) {
            if (maxLen <= 0) {
                return hotspotSearchString;
            }
            return rightFill(hotspotSearchString, maxLen, ' ');
        }

        public String getGUIDSortString() {
            return guidSearchString;
        }

        public String getGuidSearchStringUnformatted() {
            return guidSearchStringUnformatted;
        }
    }
}
