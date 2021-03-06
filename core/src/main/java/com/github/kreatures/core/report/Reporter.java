package com.github.kreatures.core.report;

import com.github.kreatures.core.internal.Entity;


/**
 * Classes implementing this interface provide methods 
 * to use the report system of KReatures to 
 * inform about changes.
 * 
 * To use more feature see the FullReporter interface.
 * @see FullReporter
 * @author Tim Janus
 *
 */
public interface Reporter {
	/**
	 * send the given message to the KReatures report mechanism.
	 * Uses a default report poster.
	 * @param message		String containing the message.
	 */
	void report(String message);
	
	/**
	 * send the given message with the given attachment ot the Angonera.
	 * Uses a default report poster.
	 * report mechanism.
	 * @param message		String containing the message.
	 * @param attachment	Reference to the entity used as an attachment.
	 */
	void report(String message, Entity attachment);
}
