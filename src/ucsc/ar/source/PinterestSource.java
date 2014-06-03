/**
 * 
 */
package ucsc.ar.source;

import java.util.List;

import ucsc.ar.structure.Pinterest;

/**
 * 
 * A generalized interface for pulling Pinterests from various sources
 * 
 */
public interface PinterestSource {

	/**
	 * @return The list of all Pinterests currently available
	 */
	public List<Pinterest> loadAll();

	/**
	 * @return The next available Pinterest, or <b>null</b> if there aren't any
	 */
	public Pinterest loadNext();
}
