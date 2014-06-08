package ucsc.ar.source;

import java.util.List;

import ucsc.ar.structure.Trip;

public interface TripSource {

	public List<Trip> loadAll();

	public Trip loadNext();
}
