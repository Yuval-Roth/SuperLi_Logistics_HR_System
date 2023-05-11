package businessLayer.employeeModule.Controllers;

import businessLayer.employeeModule.Employee;
import businessLayer.employeeModule.Role;
import businessLayer.employeeModule.Shift;
import businessLayer.employeeModule.Shift.ShiftType;
import dataAccessLayer.dalUtils.DalException;
import dataAccessLayer.employeeModule.ShiftDAO;
import utils.employeeUtils.DateUtils;
import utils.employeeUtils.EmployeeException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.next;

public class ShiftsController {
    private ShiftDAO shiftDAO;

    public ShiftsController(ShiftDAO shiftDAO) {
        this.shiftDAO = shiftDAO;
    }

    public void resetData() {
        try {
            this.shiftDAO.clearTable();
        } catch (Exception ignore) {}
    }

    public Shift getShift(String branchId, LocalDate shiftDate, ShiftType shiftType) throws EmployeeException {
        Shift shift;
        try {
            shift = shiftDAO.select(branchId, shiftDate,shiftType);
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
        if (shift == null) {
            throw new EmployeeException("The shift " + shiftDate + shiftType + " does not exist in the branch " + branchId + ".");
        }
        return shift;
    }

    public boolean shiftExists(String branchId, LocalDate shiftDate, ShiftType shiftType) {
        try {
            getShift(branchId, shiftDate, shiftType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Shift createShift(String branchId, LocalDate shiftDate, ShiftType shiftType) throws Exception {
        Shift shift = new Shift(branchId, shiftDate, shiftType);
        shiftDAO.insert(shift);
        return shift;
    }

    public void createWeekShifts(String branchId, LocalDate weekStart) throws Exception {
        // Get dates until the end of the week
        List<LocalDate> dates = weekStart.datesUntil(weekStart.with(next(DayOfWeek.SUNDAY))).toList();
        // Check if any shift in the week already exists
        for (LocalDate date : dates) {
            if (shiftExists(branchId, date, ShiftType.Morning) || shiftExists(branchId, date, ShiftType.Evening)) {
                throw new Exception("Invalid week start date, there are already existing shifts during this week, please edit them or delete them to start over.");
            }
        }
        // Create the week shifts
        for (LocalDate date : dates) {
            createShift(branchId, date, ShiftType.Morning);
            createShift(branchId, date, ShiftType.Evening);
        }
    }

    public void deleteShift(String branchId, LocalDate shiftDate, ShiftType shiftType) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        shiftDAO.delete(shift);
    }

    public List<Shift[]> getWeekShifts(String branchId, LocalDate weekStart) {
        // Get dates until the end of the week
        List<LocalDate> dates = weekStart.datesUntil(weekStart.with(next(DayOfWeek.SUNDAY))).toList();
        // Add the corresponding shifts to a list
        List<Shift[]> result = new ArrayList<>();
        for (LocalDate date : dates) {
            Shift[] dayShifts = new Shift[2];
            try {
                Shift morningShift = getShift(branchId, date, ShiftType.Morning);
                dayShifts[0] = morningShift;
            } catch (Exception ignore) {}
            try {
                Shift eveningShift = getShift(branchId, date, ShiftType.Evening);
                dayShifts[1] = eveningShift;
            } catch (Exception ignore) {}
            result.add(dayShifts);
        }
        return result;
    }

    public void setShiftNeededAmount(String branchId, LocalDate shiftDate, ShiftType shiftType, Role role, int amount) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        shift.setNeededRole(role, amount);
        shiftDAO.update(shift);
    }

    public void requestShift(Employee employee, String branchId, LocalDate shiftDate, ShiftType shiftType, Role role) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        if (!employee.getRoles().contains(role)) {
            throw new Exception("Invalid shift request, the employee is not certified for this role: " + role + ".");
        }
        if (shift.isEmployeeWorking(employee)) {
            throw new Exception("Invalid shift request, the employee is already working in this shift.");
        }
        if (shift.isEmployeeRequesting(employee)) {
            throw new Exception("Invalid shift request, the employee has already requested to work in this shift.");
        }
        if (employeeRequestedInDate(employee, shiftDate)) {
            throw new Exception("Invalid shift request, the employee has already requested to work in this day.");
        }
        if (numEmployeeShiftRequestsInWeek(employee, shift.getShiftDate()) >= 6 && !employeeRequestedInDate(employee,shift.getShiftDate())) {
            throw new Exception("Invalid shift request, the employee has already requested to work in 6 days of this week.");
        }
        shift.addShiftRequest(role, employee);
        shiftDAO.update(shift);
    }

    public void cancelShiftRequest(Employee employee, String branchId, LocalDate shiftDate, ShiftType shiftType, Role role) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        if (!shift.isEmployeeRequestingForRole(employee, role)) {
            throw new Exception("Invalid shift request, the employee hasn't requested to work in this shift and this role.");
        }
        shift.removeShiftRequest(role, employee);
        shiftDAO.update(shift);
    }

    private List<Shift> employeeShiftRequestsInWeek(Employee employee, LocalDate dayInWeek) throws Exception {
        List<Shift> morningShifts = getEmployeeRequests(employee).stream().map(s->s[0]).toList();
        List<Shift> eveningShifts = getEmployeeRequests(employee).stream().map(s->s[1]).toList();
        List<Shift> shifts = Stream.concat(morningShifts.stream(),eveningShifts.stream()).toList();
        return shifts.stream().filter(s-> s != null && DateUtils.getWeekNumber(s.getShiftDate()) == DateUtils.getWeekNumber(dayInWeek)).toList();
    }

    private int numEmployeeShiftRequestsInWeek(Employee employee, LocalDate dayInWeek) throws Exception {
        return employeeShiftRequestsInWeek(employee, dayInWeek).size();
    }

    private boolean employeeRequestedInDate(Employee employee, LocalDate dayInWeek) throws Exception {
        return !getEmployeeRequests(employee).stream().filter(s-> s[0].getShiftDate().equals(dayInWeek)).toList().isEmpty();
    }

    public void setShiftEmployees(String branchId, LocalDate shiftDate, ShiftType shiftType, Role role, List<Employee> employees) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        for (Employee employee : employees) {
            if (!employee.getRoles().contains(role)) {
                throw new Exception("Invalid shift request, the employee " + employee.getId() + " is not certified for this role: " + role + ".");
            }
        }
        for (Employee employee : employees) {
            shift.addShiftWorker(role, employee);
        }
        shiftDAO.update(shift);
    }

    /**
     * This method returns a list containing the shift days that belong to the given employee
     * @param employee The employee that the shifts belong to
     * @return a list containing the shift days that belong to the employee
     * @throws Exception if any database error has occurred
     */
    public List<Shift[]> getEmployeeShifts(Employee employee) throws Exception {
        List<Shift[]> result = new ArrayList<>();
        for (Shift shift : shiftDAO.getEmployeeShifts(employee)) {
            boolean foundDate = false;
            for (Shift[] shiftDay : result) {
                if(shiftDay[0] != null && shiftDay[0].getShiftDate().equals(shift.getShiftDate())) {
                    if(shift.getShiftType().equals(ShiftType.Evening)) {
                        shiftDay[1] = shift;
                        foundDate = true;
                        break;
                    }
                }
                else if(shiftDay[1] != null && shiftDay[1].getShiftDate().equals(shift.getShiftDate())) {
                    if(shift.getShiftType().equals(ShiftType.Morning)) {
                        shiftDay[0] = shift;
                        foundDate = true;
                        break;
                    }
                }
            }
            if (!foundDate) {
                Shift[] shiftDay = new Shift[2];
                if(shift.getShiftType().equals(ShiftType.Morning)) {
                    shiftDay[0] = shift;
                } else if(shift.getShiftType().equals(ShiftType.Evening)) {
                    shiftDay[1] = shift;
                }
                result.add(shiftDay);
            }
        }
        return result;
    }

    public List<Shift[]> getEmployeeRequests(Employee employee) throws Exception {
        List<Shift[]> result = new ArrayList<>();
        for (Shift shift : shiftDAO.getEmployeeRequests(employee)) {
            boolean foundDate = false;
            for (Shift[] shiftDay : result) {
                if(shiftDay[0] != null && shiftDay[0].getShiftDate().equals(shift.getShiftDate())) {
                    if(shift.getShiftType().equals(ShiftType.Evening)) {
                        shiftDay[1] = shift;
                        foundDate = true;
                        break;
                    }
                }
                else if(shiftDay[1] != null && shiftDay[1].getShiftDate().equals(shift.getShiftDate())) {
                    if(shift.getShiftType().equals(ShiftType.Morning)) {
                        shiftDay[0] = shift;
                        foundDate = true;
                        break;
                    }
                }
            }
            if (!foundDate) {
                Shift[] shiftDay = new Shift[2];
                if(shift.getShiftType().equals(ShiftType.Morning)) {
                    shiftDay[0] = shift;
                } else if(shift.getShiftType().equals(ShiftType.Evening)) {
                    shiftDay[1] = shift;
                }
                result.add(shiftDay);
            }
        }
        return result;
    }

    public void approveShift(String branchId, LocalDate shiftDate, ShiftType shiftType) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        shift.approve();
        shiftDAO.update(shift);
    }

    public void applyCancelCard(String branchId, LocalDate shiftDate, ShiftType shiftType, String employeeId, String productId) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        shift.useCancelCard(employeeId, productId);
        shiftDAO.update(shift);
    }

    public void reportShiftActivity(String branchId, LocalDate shiftDate, ShiftType shiftType, String reportingEmployeeId, String activity) throws Exception {
        Shift shift = getShift(branchId, shiftDate, shiftType);
        shift.reportActivity(reportingEmployeeId, activity);
        shiftDAO.update(shift);
    }
}
