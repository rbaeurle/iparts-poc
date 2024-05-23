package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;

public class iPartsDataWorkOrder extends EtkDataObject implements EtkDbConst, iPartsConst {

    public static final String CHILDREN_NAME_TASKS = "iPartsDataWorkOrder.tasks";

    static private final String[] KEYS = new String[]{ FIELD_DWO_BST_ID };

    private iPartsDataWorkOrderTaskList taskList;

    public iPartsDataWorkOrder(EtkProject project, iPartsWorkOrderId id) {
        super(KEYS);
        tableName = TABLE_DA_WORKORDER;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsWorkOrderId(idValues[0]);
    }

    @Override
    public iPartsWorkOrderId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWorkOrderId)id;
    }

    @Override
    public iPartsDataWorkOrder cloneMe(EtkProject project) {
        iPartsDataWorkOrder clone = new iPartsDataWorkOrder(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_TASKS)) {
            taskList = (iPartsDataWorkOrderTaskList)children;
        }
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_TASKS, null);
    }

    protected synchronized void loadTasks() {
        if (taskList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_TASKS, iPartsDataWorkOrderTaskList.loadWorkOrderTaskList(getEtkProject(), getAsId().getBSTId()));
    }

    public iPartsDataWorkOrderTaskList getTasks() {
        loadTasks();
        return taskList;
    }

    public synchronized void addTask(iPartsDataWorkOrderTask task, DBActionOrigin origin) {
        if (task != null) {
            getTasks().add(task, origin);
        }
    }

    public boolean isValidForDate(String date) {
        if (getFieldValue(iPartsConst.FIELD_DWO_START_OF_WORK).compareTo(date) <= 0) {
            String deliveryDatePlannedString = getFieldValue(iPartsConst.FIELD_DWO_DELIVERY_DATE_PLANNED);
            if (deliveryDatePlannedString.isEmpty() || (date.compareTo(deliveryDatePlannedString) <= 0)) {
                return true;
            }
        }

        return false;
    }

    public boolean isValidForCurrentDate() {
        return isValidForDate(SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()));
    }

    public String getTitle() {
        return getFieldValue(iPartsConst.FIELD_DWO_TITLE);
    }

    public String getReleaseNo() {
        return getFieldValue(iPartsConst.FIELD_DWO_RELEASE_NO);
    }

    public String getSupplierNo() {
        return getFieldValue(iPartsConst.FIELD_DWO_SUPPLIER_NO);
    }

    /**
     * Überprüft, ob dieser Arbeitsauftrag zu den übergebenen Eigenschaften des Benutzers passt
     *
     * @param isCarAndVan
     * @param isTruckAndBus
     * @return
     */
    public boolean isVisibleForUserProperties(boolean isCarAndVan, boolean isTruckAndBus) {
        String workOrderBranch = getFieldValue(FIELD_DWO_BRANCH);
        return (isCarAndVan && workOrderBranch.equals(WORK_ORDER_BRANCH_PKW)) || (isTruckAndBus && workOrderBranch.equals(WORK_ORDER_BRANCH_LKW));
    }
}
