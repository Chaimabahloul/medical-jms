package com.medical.jms.rmi;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MedicalRecord implements Serializable {
    private String patientName;
    private String testType;
    private double value;
    private String unit;
    private boolean critical;
    private Date timestamp;

    public MedicalRecord(String patientName, String testType, double value,
                         String unit, boolean critical) {
        this.patientName = patientName;
        this.testType = testType;
        this.value = value;
        this.unit = unit;
        this.critical = critical;
        this.timestamp = new Date();
    }

    // Getters
    public String getPatientName() { return patientName; }
    public String getTestType() { return testType; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public boolean isCritical() { return critical; }
    public Date getTimestamp() { return timestamp; }

    // Setters
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setTestType(String testType) { this.testType = testType; }
    public void setValue(double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCritical(boolean critical) { this.critical = critical; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(timestamp);
        String status = critical ? " CRITIQUE" : "Normal";

        return String.format("[%s] %s - %s: %.2f %s - %s",
                time, patientName, testType, value, unit, status);
    }
}