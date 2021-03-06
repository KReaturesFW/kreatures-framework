package com.github.kreatures.core.util;

/**
 * Classes implementing this interface allow observer to monitor maps.
 * 
 * @author Tim Janus
 */
public interface MapObservable {
	void addMapObserver(MapObserver observer);
	
	void removeMapObserver(MapObserver observer);
}
