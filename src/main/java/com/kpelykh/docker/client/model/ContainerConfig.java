package com.kpelykh.docker.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
public class ContainerConfig {

	@JsonProperty("Hostname")     private String    hostName = "";
    @JsonProperty("Name")         private String    name;
    @JsonProperty("Domainname")   private String    domainName = "";
    @JsonProperty("PortSpecs")    private String[]  portSpecs;
    @JsonProperty("ExposedPorts") private Map<String, Map<String, HostPortBinding>> exposedPorts = new HashMap<String, Map<String, HostPortBinding>>();
    @JsonProperty("User")         private String    user = "";
    @JsonProperty("Tty")          private boolean   tty = false;
    @JsonProperty("OpenStdin")    private boolean   stdinOpen = false;
    @JsonProperty("StdinOnce")    private boolean   stdInOnce = false;
    @JsonProperty("Memory")       private long      memoryLimit = 0;
    @JsonProperty("MemorySwap")   private long      memorySwap = 0;
    @JsonProperty("CpuShares")    private int       cpuShares = 0;
    @JsonProperty("AttachStdin")  private boolean   attachStdin = false;
    @JsonProperty("AttachStdout") private boolean   attachStdout = false;
    @JsonProperty("AttachStderr") private boolean   attachStderr = false;
    @JsonProperty("Env")          private String[]  env;
    @JsonProperty("Cmd")          private String[]  cmd;
    @JsonProperty("Dns")          private String[]  dns;
    @JsonProperty("Image")        private String    image;
    @JsonProperty("Volumes")      private Object    volumes;
    @JsonProperty("VolumesFrom")  private String    volumesFrom = "";
    @JsonProperty("Entrypoint")   private String[]  entrypoint = new String[]{};
    @JsonProperty("NetworkDisabled") private boolean networkDisabled = false;
    @JsonProperty("Privileged")   private boolean privileged = false;
    @JsonProperty("WorkingDir")   private String workingDir = "";


    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public boolean getNetworkDisabled() {
        return networkDisabled;
    }

    public void setNetworkDisabled(boolean networkDisabled) {
        this.networkDisabled = networkDisabled;
    }

    public String getHostName() {
    	return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDomainName() {
    	return domainName;
    }
    
    public void setDomainName(String domainName) {
    	this.domainName = domainName;
    }
    
    public String[] getPortSpecs() {
        return portSpecs;
    }

    public void setPortSpecs(String[] portSpecs) {
        this.portSpecs = portSpecs;
    }

    public Map<String, Map<String, HostPortBinding>> getExposedPorts() {
    	return exposedPorts;
    }
    
    public void setExposedPorts(Map<String, Map<String, HostPortBinding>> exposedPorts) {
    	this.exposedPorts = exposedPorts;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isTty() {
        return tty;
    }

    public void setTty(boolean tty) {
        this.tty = tty;
    }

    public boolean isStdinOpen() {
        return stdinOpen;
    }

    public void setStdinOpen(boolean stdinOpen) {
        this.stdinOpen = stdinOpen;
    }

    public boolean isStdInOnce() {
        return stdInOnce;
    }

    public void setStdInOnce(boolean stdInOnce) {
        this.stdInOnce = stdInOnce;
    }

    public long getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public long getMemorySwap() {
        return memorySwap;
    }

    public void setMemorySwap(long memorySwap) {
        this.memorySwap = memorySwap;
    }

    public int getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(int cpuShares) {
        this.cpuShares = cpuShares;
    }

    public boolean isAttachStdin() {
        return attachStdin;
    }

    public void setAttachStdin(boolean attachStdin) {
        this.attachStdin = attachStdin;
    }

    public boolean isAttachStdout() {
        return attachStdout;
    }

    public void setAttachStdout(boolean attachStdout) {
        this.attachStdout = attachStdout;
    }

    public boolean isAttachStderr() {
        return attachStderr;
    }

    public void setAttachStderr(boolean attachStderr) {
        this.attachStderr = attachStderr;
    }

    public String[] getEnv() {
        return env;
    }

    public void setEnv(String[] env) {
        this.env = env;
    }

    public String[] getCmd() {
        return cmd;
    }

    public void setCmd(String[] cmd) {
        this.cmd = cmd;
    }

    public String[] getDns() {
        return dns;
    }

    public void setDns(String[] dns) {
        this.dns = dns;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Object getVolumes() {
        return volumes;
    }

    public void setVolumes(Object volumes) {
        this.volumes = volumes;
    }

    public String getVolumesFrom() {
        return volumesFrom;
    }

    public void setVolumesFrom(String volumesFrom) {
        this.volumesFrom = volumesFrom;
    }

    public String[] getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String[] entrypoint) {
        this.entrypoint = entrypoint;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ContainerConfig [hostName=" + hostName + ", name=" + name + ", domainName=" + domainName
				+ ", portSpecs=" + Arrays.toString(portSpecs) + ", exposedPorts=" + exposedPorts + ", user=" + user
				+ ", tty=" + tty + ", stdinOpen=" + stdinOpen + ", stdInOnce=" + stdInOnce + ", memoryLimit="
				+ memoryLimit + ", memorySwap=" + memorySwap + ", cpuShares=" + cpuShares + ", attachStdin="
				+ attachStdin + ", attachStdout=" + attachStdout + ", attachStderr=" + attachStderr + ", env="
				+ Arrays.toString(env) + ", cmd=" + Arrays.toString(cmd) + ", dns=" + Arrays.toString(dns) + ", image="
				+ image + ", volumes=" + volumes + ", volumesFrom=" + volumesFrom + ", entrypoint="
				+ Arrays.toString(entrypoint) + ", networkDisabled=" + networkDisabled + ", privileged=" + privileged
				+ ", workingDir=" + workingDir + "]";
	}

}
