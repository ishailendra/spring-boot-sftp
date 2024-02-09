package dev.techsphere.sftpconfigurer.model;

public class IntegrationFlowRequest {
    private String serverName;
    private String localDir;
    private String remoteDir;
    private String schedule;
    private String outboundRegex;
    private String tempDir;
    private String archDir;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getOutboundRegex() {
        return outboundRegex;
    }

    public void setOutboundRegex(String outboundRegex) {
        this.outboundRegex = outboundRegex;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getArchDir() {
        return archDir;
    }

    public void setArchDir(String archDir) {
        this.archDir = archDir;
    }
}