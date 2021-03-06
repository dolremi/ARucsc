package ucsc.ar.source;

import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import ucsc.ar.structure.Pinterest;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class TwitterSource implements DataSource {

	private Pinterest anchor;
	private String hashtags;
	private Twitter twitter;
	private List<twitter4j.Status> statuses;
	private int statusIndex = 0;

	public TwitterSource(Pinterest newAnchor, String newHashtags) {
		this.anchor = newAnchor;
		this.hashtags = newHashtags;
		twitter = new TwitterFactory().getInstance();
		AsyncTask<Boolean, Void, Boolean> testUpdate = new AsyncTask<Boolean, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Boolean... bools) {
				try {
					Query query = new Query(hashtags);
					query.setGeoCode(new GeoLocation(anchor.getLatitude(),
							anchor.getLongitude()), 0.1d, Query.KILOMETERS);
					QueryResult result = twitter.search(query);
					statuses = result.getTweets();

					// gets Twitter instance with default credentials
				} catch (TwitterException te) {
					te.printStackTrace();
					System.out.println("Failed to get timeline: "
							+ te.getMessage());
					return false;
				}
				return true;
			}
		};

		testUpdate.execute();
	}

	public TwitterSource(Pinterest anchor) {
		this(anchor, "");
	}

	public TwitterSource(String hashtags) {

		this(null, hashtags);
	}

	@Override
	public Pinterest getAnchor() {
		return anchor;
	}

	@Override
	public void setAnchor(Pinterest newAnchor) {
		this.anchor = newAnchor;
	}

	@Override
	public Bitmap getCurrentImage() {

		return null;
	}

	@Override
	public String getCurrentText() {
		if (statuses != null && !statuses.isEmpty())
			return statuses.get(statusIndex).getText();
		else
			return "";
	}

	@Override
	public void next() {
		if (statuses != null && !statuses.isEmpty()) {
			statusIndex = (statusIndex + 1) % statuses.size();
		}
	}

	@Override
	public void previous() {
		if (statuses != null && !statuses.isEmpty()) {
			statusIndex = (statusIndex - 1) % statuses.size();
		}
	}

	@Override
	public String getSearchString() {
		return hashtags;
	}

}
