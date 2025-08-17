package com.microbank.clientservice.dto;

public class ClientStatistics {

    private long totalClients;
    private long activeClients;
    private long blacklistedClients;
    private long inactiveClients;
    private long suspendedClients;

    // Default constructor
    public ClientStatistics() {}

    // Constructor with all fields
    public ClientStatistics(long totalClients, long activeClients, long blacklistedClients, 
                          long inactiveClients, long suspendedClients) {
        this.totalClients = totalClients;
        this.activeClients = activeClients;
        this.blacklistedClients = blacklistedClients;
        this.inactiveClients = inactiveClients;
        this.suspendedClients = suspendedClients;
    }

    // Getters and Setters
    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
    }

    public long getActiveClients() {
        return activeClients;
    }

    public void setActiveClients(long activeClients) {
        this.activeClients = activeClients;
    }

    public long getBlacklistedClients() {
        return blacklistedClients;
    }

    public void setBlacklistedClients(long blacklistedClients) {
        this.blacklistedClients = blacklistedClients;
    }

    public long getInactiveClients() {
        return inactiveClients;
    }

    public void setInactiveClients(long inactiveClients) {
        this.inactiveClients = inactiveClients;
    }

    public long getSuspendedClients() {
        return suspendedClients;
    }

    public void setSuspendedClients(long suspendedClients) {
        this.suspendedClients = suspendedClients;
    }

    @Override
    public String toString() {
        return "ClientStatistics{" +
                "totalClients=" + totalClients +
                ", activeClients=" + activeClients +
                ", blacklistedClients=" + blacklistedClients +
                ", inactiveClients=" + inactiveClients +
                ", suspendedClients=" + suspendedClients +
                '}';
    }
}
