package org.eclipse.jgit.mavenproject;

import com.google.gson.annotations.SerializedName;

public class AuthMsg {
	// "name" which is in json what we get from url
	@SerializedName("name")
	private String repository;

	/**
	 * @return the repository
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
}
