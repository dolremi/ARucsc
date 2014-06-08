package ucsc.ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class TripListActivity extends FragmentActivity implements
		TripListFragment.Callbacks {

	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_list);
		// Changed: DON'T show the Up button in the action bar, as this is now
		// the main activity.
		getActionBar().setDisplayHomeAsUpEnabled(false);

		if (findViewById(R.id.trip_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((TripListFragment) getSupportFragmentManager().findFragmentById(
					R.id.trip_list)).setActivateOnItemClick(true);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {

			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {

			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(TripDetailFragment.ARG_ITEM_ID, id);
			TripDetailFragment fragment = new TripDetailFragment();
			// include bundle
			fragment.setArguments(arguments);
			// pass on identity of caller activity so that AR mode can be run
			// from the fragment
			fragment.setCurrentActivity(this);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.trip_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, TripDetailActivity.class);
			detailIntent.putExtra(TripDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
}
