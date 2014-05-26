package ucsc.ar.source;

import java.util.List;

import ucsc.ar.structure.Trip;

/**
 * 
 * @author Piotr Nosalik
 * 
 *         A generalized interface for pulling Trips from various sources
 * 
 */
public interface TripSource {
	/**
	 * @return The list of all Trips currently available
	 */
	public List<Trip> loadAll();

	/**
	 * @return The next available Trip, or <b>null</b> if there aren't any
	 */
	public Trip loadNext();
}
