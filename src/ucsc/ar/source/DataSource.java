package ucsc.ar.source;

import ucsc.ar.structure.Pinterest;
import android.graphics.Bitmap;

public interface DataSource {
	public Pinterest getAnchor();

	public void setAnchor(Pinterest newAnchor);

	public Bitmap getCurrentImage();

	public String getCurrentText();

	public void next();

	public void previous();

	public String getSearchString();
}
