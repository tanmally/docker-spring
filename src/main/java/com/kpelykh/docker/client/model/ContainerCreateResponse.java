package com.kpelykh.docker.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;

/**
 * 
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 * 
 */
public class ContainerCreateResponse {

	@JsonProperty("Id")
	private String id;

	@JsonProperty("Warnings")
	private String[] warnings;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getWarnings() {
		return warnings;
	}

	public void setWarnings(String[] warnings) {
		this.warnings = warnings;
	}

	@Override
	public String toString() {
		return "ContainerCreateResponse{" + "id='" + id + '\'' + ", warnings="
				+ Arrays.toString(warnings) + '}';
	}
}
