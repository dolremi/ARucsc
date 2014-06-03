package ucsc.ar.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import ucsc.ar.structure.Pinterest;
import ucsc.ar.structure.Trip;

public class TripCreator {
	public static Trip parseXml(InputStream in_s) {
		Trip t = new Trip();

		TripData TripData = new TripData();
		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			// InputStream in_s = new FileInputStream(fileName);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in_s, null);
			System.out.println("Starting to parse");
			TripData = parseXML(parser);

		} catch (XmlPullParserException e) {
			System.out.println("XmlPullParserException in line "
					+ e.getLineNumber());
			TripData = new TripData(null, "", "Invalid Trip",
					"XmlPullParserEception in line " + e.getLineNumber());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			TripData = new TripData(null, "", "Invalid Trip",
					"FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			TripData = new TripData(null, "", "Invalid Trip", "IOException");
			e.printStackTrace();
		}
		if (TripData != null) {
			t.setPinterests(TripData.Pinterests);
			t.setId(TripData.id);
			t.setName(TripData.name);
			t.setInfo(TripData.info);
			if (TripData.size < 0
					|| TripData.size != t.getAllPinterests().size())
				System.out.println("Wrong size field in meta TripData.size = "
						+ TripData.size + "this.Pinterests.size() = "
						+ t.getAllPinterests().size());
			t.setSize(t.getAllPinterests().size());
		} else
			System.out.println("Wasn't able to instantiate Trip");

		return t;
	}

	private static TripData parseXML(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		TripData TripData = null;
		int eventType = parser.getEventType();
		boolean inMeta = false;
		PinterestData currentPinterestData = new PinterestData();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				TripData = new TripData();
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				// switch between types of tags
				switch (name) {
				case "meta":
					inMeta = true;
					break;
				case "id":
					TripData.id = parser.nextText();
					break;
				case "name":
					if (inMeta) {
						TripData.name = parser.nextText();
					} else {
						currentPinterestData.name = parser.nextText();
					}
					break;
				case "info":
					if (inMeta) {
						TripData.info = parser.nextText();
					} else {
						currentPinterestData.info = parser.nextText();
					}
					break;
				case "size":
					String sizeData = parser.nextText();
					try {
						TripData.size = Integer.parseInt(sizeData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in size field");
					}
					break;
				case "latitude":
					String latData = parser.nextText();
					try {
						currentPinterestData.latitude = Double
								.parseDouble(latData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in latitude field");
					}
					break;
				case "longitude":
					String longData = parser.nextText();
					try {
						currentPinterestData.longitude = Double
								.parseDouble(longData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in longitude field");
					}
					break;
				case "hashtags":
					currentPinterestData.hashtags = parser.nextText();
					break;
				case "trip":
					inMeta = false;
					break;
				case "pinterest":
					currentPinterestData = new PinterestData();
					break;
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				switch (name) {
				case "pinterest":
					// Will add the Pinterest only if the data is full
					Pinterest newPinterest = currentPinterestData
							.createPinterest();
					if (newPinterest != null)
						TripData.Pinterests.add(newPinterest);
					break;
				case "trip":
					break;
				case "meta":
					inMeta = false;
					break;
				}
			}
			eventType = parser.next();
		} // end of while

		return TripData;
	}

	// Used to store the data while parsing
	public static class TripData {
		public List<Pinterest> Pinterests = new ArrayList<Pinterest>();
		public String id = "";
		public String name = null;
		public String info = null;
		public int size = -1;

		public TripData() {
		}

		public TripData(List<Pinterest> Pinterests, String id, String name,
				String info) {
			this.Pinterests = Pinterests;
			this.id = id;
			this.name = name;
			this.info = info;
		}
	}

	// Used to store data while parsing
	public static class PinterestData {
		public String name = null;
		public double latitude = 100.0;
		public double longitude = 100.0;
		public String info = null;
		public String hashtags = null;

		public PinterestData() {
		}

		// Returns null if not all fields have been initialized
		public Pinterest createPinterest() {
			if (name != null && latitude != 100.0 && longitude != 100.0
					&& info != null && hashtags != null) {
				if (hashtags.equalsIgnoreCase("")) {
					System.out
							.println("Creating Pinterest without TwitterSource: Pinterest("
									+ name
									+ ", "
									+ latitude
									+ ", "
									+ longitude
									+ ", " + "," + info + ")");
					return new Pinterest(name, latitude, longitude, info);
				} else {
					System.out
							.println("Creating Pinterest with TwitterSource: Pinterest("
									+ name
									+ ", "
									+ latitude
									+ ", "
									+ longitude
									+ ", "
									+ info
									+ ", new TwitterSource("
									+ hashtags + "))");
					return new Pinterest(name, latitude, longitude, info,
							new TwitterSource(hashtags));
				}
			} else {
				if (name == null)
					System.out
							.println("Not able to create Pinterest - name = null");
				if (latitude == 100.0)
					System.out
							.println("Not able to create Pinterest - latitude = 100.0");
				if (longitude == 100.0)
					System.out
							.println("Not able to create Pinterest - longitude = 100.0");
				if (info == null)
					System.out
							.println("Not able to create Pinterest - info = null");
				if (hashtags == null)
					System.out
							.println("Not able to create Pinterest - hashtags = null");
				return null;
			}

		}
	}

	/**
	 * Returns a preview of the Trip in the form of a Trip object with the right
	 * size, info, name, id and a list of Pinterests containing only the
	 * starting Pinterestnt of the Trip Requires an xml file. For the format
	 * take a look at the Trip(InputStream in_s). Will return null if there was
	 * a problem with the parsing.
	 * 
	 * @param in_s
	 * @return
	 */
	public static Trip getTripPreview(InputStream in_s) {
		TripData TripData = null;
		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			// InputStream in_s = new FileInputStream(fileName);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in_s, null);

			TripData = parseTripXMLMeta(parser);

		} catch (XmlPullParserException e) {
			TripData = null;
			System.out
					.println("Invalid Trip file - XmlPullParserException in line "
							+ e.getLineNumber());
			// TripData = new TripData(null, -1, "Invalid Trip",
			// "XmlPullParserEception in line "+e.getLineNumber());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			TripData = null;
			System.out.println("Invalid Trip file - FileNotFoundException");
			// TripData = new TripData(null, -1, "Invalid Trip",
			// "FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			TripData = null;
			System.out.println("Invalid Trip file - IOException");
			// TripData = new TripData(null, -1, "Invalid Trip",
			// "FileNotFoundException");
			e.printStackTrace();
		}
		if (TripData != null) {
			return new Trip(TripData.Pinterests, TripData.id, TripData.name,
					TripData.info, TripData.size);
		} else
			System.out.println("Wasn't able to instantiate Trip Preview");

		return null;
	}

	private static TripData parseTripXMLMeta(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		TripData TripData = null;
		int eventType = parser.getEventType();
		boolean readFirstPinterest = false; // will be set to true after the
											// first successfully read Pinterest
											// - when reaching a </Pinterest>
											// where all the data for the
											// Pinterest has been filled.
		boolean inMeta = false;
		PinterestData currentPinterestData = new PinterestData();

		while (eventType != XmlPullParser.END_DOCUMENT && !readFirstPinterest) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				TripData = new TripData();
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				// switch between types of tags
				switch (name) {
				case "meta":
					inMeta = true;
					break;
				case "id":
					TripData.id = parser.nextText();
					break;
				case "name":
					if (inMeta) {
						TripData.name = parser.nextText();
					} else {
						currentPinterestData.name = parser.nextText();
					}
					break;
				case "info":
					if (inMeta) {
						TripData.info = parser.nextText();
					} else {
						currentPinterestData.info = parser.nextText();
					}
					break;
				case "size":
					String sizeData = parser.nextText();
					try {
						TripData.size = Integer.parseInt(sizeData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in size field");
					}
					break;
				case "latitude":
					String latData = parser.nextText();
					try {
						currentPinterestData.latitude = Double
								.parseDouble(latData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in latitude field");
					}
					break;
				case "longitude":
					String longData = parser.nextText();
					try {
						currentPinterestData.longitude = Double
								.parseDouble(longData);
					} catch (NumberFormatException e) {
						throw new XmlPullParserException(
								"Invalid String in longitude field");
					}
					break;
				case "hashtags":
					currentPinterestData.hashtags = parser.nextText();
					break;
				case "trip":
					inMeta = false;
					break;
				case "pinterest":
					currentPinterestData = new PinterestData();
					break;
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				switch (name) {
				case "pinterest":
					// Will add the Pinterest only if the data is full
					if (currentPinterestData != null) {
						Pinterest newPinterest = currentPinterestData
								.createPinterest();
						if (newPinterest != null) {
							TripData.Pinterests.add(newPinterest);
							readFirstPinterest = true;
						}
					}
					break;
				case "trip":

					break;
				case "meta":
					inMeta = false;
					break;
				}
			}
			eventType = parser.next();
		} // end of while

		return TripData;
	}
}
