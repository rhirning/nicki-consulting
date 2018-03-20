package org.mgnl.nicki.consulting.core.model;

import java.util.List;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;

@Table(name = "CUSTOMERS")
public class Customer {
	
	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;

	@Attribute(name = "NAME")
	private String name;

	@Attribute(name = "STREET")
	private String street;

	@Attribute(name = "ZIP")
	private String zip;

	@Attribute(name = "CITY")
	private String city;
	
	@SubTable(foreignKey = "customerId")
	private List<Project> projects;

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getStreet() {
		return street;
	}


	public void setStreet(String street) {
		this.street = street;
	}


	public String getZip() {
		return zip;
	}


	public void setZip(String zip) {
		this.zip = zip;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public List<Project> getProjects() {
		return projects;
	}


	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}


	public Customer() {
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
}
