package ucsc.ar.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ucsc.ar.structure.Trip;
import android.content.Context;
import android.os.Environment;

public class LocalFileSource implements TripSource {
	/** Whether we read from the SD card or from internal device memory */
	private Boolean external;
	/** The directory we're reading from */
	private File source;
	/** App context, necessary for getting the current internal memory directory */
	private Context context;
	/** List of currently loaded files */
	private List<File> loadedFiles;

	public LocalFileSource(Boolean external, Context context) {
		this.external = external;
		String state = Environment.getExternalStorageState();
		/**
		 * If we're trying to use the external storage, try confirming whether
		 * it's available
		 */
		if (this.external && !(Environment.MEDIA_MOUNTED.equals(state)))
			this.external = false;
		if (this.external) {
			/** Get the device's main external storage */
			this.source = new File(Environment.getExternalStorageDirectory(),
					"ARucsc/Trips");
			/** If the directory doesn't exist, create it */
			if (!this.source.exists()) {
				this.source.mkdirs();
			}
		} else {
			/**
			 * If we're using internal storage, get our directory, and the Trips
			 * folder from it
			 * */
			this.source = new File(this.context.getFilesDir(), "trips");
		}
		/** Load only XML files */
		File[] files = this.source.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase(Locale.ENGLISH).endsWith(".xml");
			}
		});
		/** Store the files to read them one by one on demand */
		loadedFiles = Arrays.asList(files);
	}

	/** Default to external storage */
	public LocalFileSource(Context context) {
		this(true, context);
	}

	@Override
	public List<Trip> loadAll() {
		List<Trip> list = new ArrayList<Trip>();
		Trip t;
		do {
			t = loadNext();
			if (t != null)
				list.add(t);
		} while (t != null);
		return list;
	}

	@Override
	public Trip loadNext() {

		if (loadedFiles.isEmpty())
			return null;
		File f = loadedFiles.get(0);
		Trip t = null;
		/** Try reading the files one by one */
		do {
			loadedFiles.remove(0);
			try {
				t = TripCreator.parseXml(new FileInputStream(f));
			} catch (FileNotFoundException e) {
				t = null;
				e.printStackTrace();
			}
		} while (t != null && !loadedFiles.isEmpty());
		return t;
	}

}
