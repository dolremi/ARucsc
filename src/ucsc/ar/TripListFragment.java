package ucsc.ar;

import geo.GeoUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import ucsc.ar.source.TripCreator;
import ucsc.ar.structure.Pinterest;
import ucsc.ar.structure.Trip;
import util.Log;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TripListFragment extends ListFragment {

	// The serialization (saved instance state) Bundle key representing the
	// activated item position.

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	// The fragment's current callback object, which is notified of list item
	// clicks.
	private Callbacks mCallbacks = sDummyCallbacks;

	private int mActivatedPosition = ListView.INVALID_POSITION;

	private Activity currentActivity;

	// A callback interface that all activities containing this fragment must
	// implement. This mechanism allows activities to be notified of item
	// selections.

	public interface Callbacks {

		public void onItemSelected(String id);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	private ArrayList<Trip> TripList;
	private ArrayList<String> TripNameList;
	public static HashMap<String, Trip> TripMap = new HashMap<String, Trip>();

	private String sortOrder = "Filename"; // activates default case

	private OnNavigationListener mOnNavigationListener;

	public TripListFragment() {
	}

	public void initialiseTrips() {
		// create the list!

		Field[] fields = R.raw.class.getFields();
		Trip[] Trips = new Trip[fields.length];
		int k = 0;

		for (int i = 0; i < Trips.length; i++) {

			try {
				int resourceID = fields[i].getInt(fields[i]);
				String resourceName = fields[i].getName();
				// current protocol: Trip filenames begin with Trip_
				String parts[] = resourceName.split("_");
				if (parts[0].equals("trip"))
					Trips[k++] = TripCreator.parseXml(this.getResources()
							.openRawResource(resourceID));

			} catch (IllegalAccessException e) {
				Log.e("REFLECTION",
						String.format("%s threw IllegalAccessException.",
								fields[i].getName()));
			}

		}

		TripList = new ArrayList<Trip>();

		for (int i = 0; i < k; i++) {
			TripList.add(Trips[i]);
			TripMap.put(Trips[i].getName(), Trips[i]);
		}
		sortTrips(); // must come BEFORE TripNameList is initialised

		initialiseTripNameList();

	}

	private void initialiseTripNameList() {
		TripNameList = new ArrayList<String>();
		for (int i = 0; i < TripList.size(); i++) {
			TripNameList.add(TripList.get(i).getName());
		}
	}

	/* Method for sorting the list of Trips according to user preferences. */
	public void sortTrips() {
		// The string "sortOrder" is initialized with the user's choice for a
		// sorting criterion. */
		String criteria[] = getResources().getStringArray(
				R.array.sorting_criteria_list);
		// order: default, alphabetical, proximity
		if (sortOrder.equals(criteria[1])) {
			AlphaComparator alphaComp = new AlphaComparator();
			Collections.sort(TripList, alphaComp);
		} else {
			if (sortOrder.equals(criteria[2])) {
				ProxComparator proxComp = new ProxComparator();
				Collections.sort(TripList, proxComp);
			}
		}
	}

	// Different comparator classes for sorting the list of Trips according to
	// user preferences .

	private class AlphaComparator implements Comparator<Trip> {

		@Override
		public int compare(Trip a, Trip b) {
			return a.getName().compareTo(b.getName());
		}

	}

	private class ProxComparator implements Comparator<Trip> {

		@Override
		public int compare(Trip a, Trip b) {

			// Setting the next Pinterests for the Trip "a" and the Trip "b"
			Pinterest aPinterest = a.getNextPinterest();
			Location aNextLocation = new Location("aNextLocation");
			aNextLocation.setLatitude(aPinterest.getLatitude());
			aNextLocation.setLongitude(aPinterest.getLongitude());

			Pinterest bPinterest = b.getNextPinterest();
			Location bNextLocation = new Location("bNextLocation");
			bNextLocation.setLatitude(bPinterest.getLatitude());
			bNextLocation.setLongitude(bPinterest.getLongitude());

			// Calculating the distance to the closest Pinterest for each of the
			// Trips.
			Location l = GeoUtils.getCurrentLocation(getActivity());

			Float aDistance = l.distanceTo(aNextLocation);
			Float bDistance = l.distanceTo(bNextLocation);

			if (aDistance > bDistance)
				return 1;
			else
				return -1;

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// add action bar menu
		setHasOptionsMenu(true);

		// create dropdown for sorting criterion
		createSortingDropdown();

		// populate local list
		initialiseTrips();

		// create and set list adapter
		resetAdapter();
	}

	private void createSortingDropdown() {
		// create spinner adapter
		ArrayAdapter<CharSequence> mSpinnerAdapter = ArrayAdapter
				.createFromResource(getActivity(),
						R.array.sorting_criteria_list,
						R.layout.custom_spinner_dropdown_item); // or
																// android.R.layout.simple_spinner..
		mSpinnerAdapter
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		// create navigation listener
		mOnNavigationListener = new OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter
			String[] strings = getResources().getStringArray(
					R.array.sorting_criteria_list);

			// action when selected.
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				// change sorting criterion and refresh as required

				sortOrder = strings[position];
				refresh();
				Toast.makeText(getActivity(),
						"Trip sort order: " + strings[position],
						Toast.LENGTH_SHORT).show();
				return true;
			}
		};

		// assign to action bar
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

	}

	/* Reset the list adapter used for the UI. Uses TripNameList. */
	private void resetAdapter() {
		// Create and set list adapter, using list of Trip names. Style as
		// required.

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, TripNameList);
		setListAdapter(adapter);
	}

	private void refresh() {
		initialiseTrips();
		resetAdapter();
	}

	private void help() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		View view = View.inflate(getActivity(), R.layout.help_layout, null);
		builder.setView(view);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void about() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		View view = View.inflate(getActivity(), R.layout.about_layout, null);
		builder.setView(view);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.trip_list_activity_actions, menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {

		case R.id.action_refresh:
			refresh();

			Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.action_help:
			help();

			Toast.makeText(getActivity(), "Help", Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_about:
			about();

			break;
		}

		return getActivity().onOptionsItemSelected(item);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(TripList.get(position).getName());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
