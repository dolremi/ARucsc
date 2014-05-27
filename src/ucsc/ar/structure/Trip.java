package ucsc.ar.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trip {
	private List<Pinterest> Pinterests = new ArrayList<Pinterest>();// All the
																	// Pinterests
																	// in this
																	// Trip
	private int index = 0;// Current position on the Trip
	private String id;// Need to implement an ID generating scheme that would
						// work between resets and also with multiple users
	private String name;
	private String info;
	private int size; // Useful when parsing only the metadata

	/**
	 * When there are sizes difference, use the copy constructor
	 * 
	 * @param Pinterests
	 * @param id
	 * @param name
	 * @param info
	 * @param size
	 */
	public Trip(List<Pinterest> Pinterests, String id, String name,
			String info, int size) {
		this.Pinterests = Pinterests;
		this.id = id;
		this.name = name;
		this.info = info;
		this.size = size;
	}

	/**
	 * Another copy constructor to transfer Array to ArrayList
	 * 
	 * @param Pinterests
	 * @param id
	 * @param name
	 * @param info
	 */
	// Another copy constructor to transfer Array to ArrayList
	public Trip(Pinterest[] Pinterests, String id, String name, String info) {
		this(Arrays.asList(Pinterests), id, name, info);
	}

	/** A no-argument constructor for use with different methods */
	public Trip() {
	}

	/**
	 * Creates a Trip with the following parameters. If the ArrayList is null or
	 * empty creates it so that it has a single Pinterest("Empty Trip", 0.0,
	 * 0.0, "Empty Trip")
	 * 
	 * @param Pinterests
	 * @param id
	 * @param name
	 * @param info
	 */
	// Constructor for ArrayList
	public Trip(List<Pinterest> Pinterests, String id, String name, String info) {
		this.id = id;
		this.name = name;
		this.info = info;
		if (Pinterests != null && Pinterests.size() != 0) {
			this.Pinterests = Pinterests;
		} else {
			this.Pinterests.add(new Pinterest("Empty Trip", 0.0, 0.0,
					"Empty Trip"));
		}
		this.size = this.Pinterests.size();
		System.out.println("Trip " + id + " of size " + this.Pinterests.size()
				+ " created");
	}

	/**
	 * Creates a Trip using an InputStream from an xml file. Format of the xml:
	 * 
	 * @param fileName
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getID() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void incrementIndex() {
		index++;
	}

	public void decrementIndex() {
		index--;
	}

	/**
	 * Returns the Pinterest at that index. If the index is invalid will return
	 * the first Pinterest of the Trip.
	 * 
	 * @param ind
	 * @return Pinterest at that index
	 */
	// Returns the first Pinterest if supplied an invalid index
	public Pinterest getPinterest(int ind) {
		try {
			return Pinterests.get(ind);
		} catch (ArrayIndexOutOfBoundsException e) {
			return Pinterests.get(0);
		}
	}

	/**
	 * Returns the Pinterest at the current index
	 * 
	 * @return
	 */
	public Pinterest getCurrentPinterest() {
		return Pinterests.get(index);
	}

	/**
	 * Moves to the next Pinterest, incrementing the index (mod sizeOfTheTrip)
	 * 
	 * @return the next Pinterest (mod the size of the Trip)
	 */
	// Increments index (mod Pinterests.length) and returns the Pinterest with
	// that index
	public Pinterest moveToNextPinterest() {
		index = (index + 1) % size;
		if (index == 0)
			System.out.println("Trip finished");
		return Pinterests.get(index);
	}

	/**
	 * Returns the Pinterest in the next position(mod size). Doesn't increment
	 * the index. Will print out "No next Pinterest" if it has wrapped around.
	 * 
	 * @return the Pinterest in the next position
	 */
	public Pinterest getNextPinterest() {
		int nextIndex = (index + 1) % Pinterests.size();
		if (nextIndex == 0)
			System.out.println("No next Pinterest.");
		return Pinterests.get(nextIndex);
	}

	/**
	 * Returns the Pinterests converted into an Array
	 * 
	 * @return Pinterests as a Pinterest[]
	 */
	public Pinterest[] getAllPinterestsAsArray() {
		Pinterest[] p = new Pinterest[size];
		Pinterests.toArray(p);
		return p;
	}

	/**
	 * Returns the Pinterests in a list
	 * 
	 * @return Pinterests
	 */
	public List<Pinterest> getAllPinterests() {
		return Pinterests;
	}

	public void setPinterests(List<Pinterest> Pinterests) {
		this.Pinterests = Pinterests;
	}

	public boolean hasNext() {
		return (index < Pinterests.size());
	}

}
