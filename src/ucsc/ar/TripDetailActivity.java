package ucsc.ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class TripDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(TripDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(TripDetailFragment.ARG_ITEM_ID));
			TripDetailFragment fragment = new TripDetailFragment();
			fragment.setArguments(arguments);
			// pass on identity of caller activity so that AR mode can be run
			// from the fragment
			fragment.setCurrentActivity(this);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.trip_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this,
					new Intent(this, TripListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
