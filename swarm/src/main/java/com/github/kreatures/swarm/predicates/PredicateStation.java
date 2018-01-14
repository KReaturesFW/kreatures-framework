package com.github.kreatures.swarm.predicates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kreatures.core.Perception;

import net.sf.tweety.logics.fol.syntax.FolFormula;

/**
 * SwarmStation is the basis class and allows to create new generic station.
 * Station(StationName,StattionType,freq,nec,space).
 * @author donfack
 * 
 *
 */


public class PredicateStation extends SwarmPredicate{
	// Station(StationName,StationType,freq,nec,space).
	private String name;
	private String typeName;
	private int frequency;
	private int necessity;
	private int space;
	/**
	 * true when a station has a item attribut and false otherwise.
	 */
	private boolean item=false;
	/** Default Ctor: Initialize plan and atom with null */
	protected PredicateStation() {}
	
	public PredicateStation(FolFormula desire) {
		super(desire);
		createInstance(desire);
	}

	public PredicateStation(FolFormula desire, Perception reason) {
		super(desire, reason);
		createInstance(desire);
	}

	public PredicateStation(PredicateStation other) {
		super(other);
		this.name=other.name;
		this.typeName=other.typeName;
		this.frequency=other.frequency;
		this.necessity=other.necessity;
		this.space=other.space;
	}	

	@Override
	public PredicateStation clone() {
		return new PredicateStation(this);
	}
	
	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}

	public int getFrequency() {
		return frequency;
	}



	public void incrFrequency() {
		this.frequency++;
	}



	public int getNecessity() {
		return necessity;
	}

	public int getSpace() {
		return space;
	}

	public void incrSpace(int addSpace) {
		this.space+= addSpace;
	}
	
	public void decrSpace(int substrSpace) {
		this.space-= substrSpace;
	}
	/**
	 * @return the item
	 */
	public boolean hasItem() {
		return item;
	}

	/**
	 * Station(StationName,StattionType,freq,nec,space).
	 */
	@Override
	public String toString() {
		return String.format("Station(%s,%s,%d,%d,%d)",name, typeName,frequency,necessity,space);
	}
	
	@Override
	public String getPredicatType() {
		
		return getFormulName();
	}
	
	@Override
	protected void createInstance(FolFormula atom) {
		//PredicateStation predicate=null;
		Pattern pattern=Pattern.compile("Station[(](\\w+),(\\w+),(\\d+),(\\d+),(\\d+)[)]");
		Matcher matcher=pattern.matcher(atom.toString());
		if(matcher.find()) {
			//predicate=new PredicateStation();
			this.name=matcher.group(1);
			this.typeName=matcher.group(2);
			this.frequency=Integer.parseInt(matcher.group(3));
			this.necessity=Integer.parseInt(matcher.group(4));
			this.space=Integer.parseInt(matcher.group(5));
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof PredicateStation))
			return false;
		
		PredicateStation obj=(PredicateStation)other;
		boolean isName=obj.name==null?this.name==null:obj.name.equals(this.name);
		boolean isTypeName=obj.typeName==null?this.typeName==null:obj.typeName.equals(this.typeName);
		
		return isName & isTypeName;
	}
	
	@Override
	public int hashCode() {
		return this.typeName.hashCode()* 11;
	}
	
}