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

	private Boolean external;

	private File source;

	private Context context;

	private List<File> loadedFiles;

	public LocalFileSource(Boolean external, Context context) {
		this.external = external;
		String state = Environment.getExternalStorageState();

		// check external storage media
		if (this.external && !(Environment.MEDIA_MOUNTED.equals(state)))
			this.external = false;
		if (this.external) {
			// Get the device's main external storage
			this.source = new File(Environment.getExternalStorageDirectory(),
					"ARucsc/Trips");
			// If the directory doesn't exist, create it
			if (!this.source.exists()) {
				this.source.mkdirs();
			}
		} else {

			// use internal storage media
			this.source = new File(this.context.getFilesDir(), "trips");
		}

		File[] files = this.source.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase(Locale.ENGLISH).endsWith(".xml");
			}
		});

		loadedFiles = Arrays.asList(files);
	}

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
