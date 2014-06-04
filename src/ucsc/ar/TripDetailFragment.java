package ucsc.ar;

import java.util.List;

import system.ArActivity;
import ucsc.ar.structure.Pinterest;
import ucsc.ar.structure.Trip;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A fragment representing a single Trip detail screen. This fragment is either
 * contained in a {@link TripListActivity} in two-pane mode (on tablets) or a
 * {@link TripDetailActivity} on handsets.
 */
public class TripDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The Trip this fragment is presenting.
	 */
	private Trip mItem;

	private Activity currentActivity;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TripDetailFragment() {
	}

	public void setCurrentActivity(Activity activity) {
		this.currentActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the content specified by the Trip, accessed from the hash
			// map.
			mItem = TripListFragment.TripMap.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_trip_detail,
				container, false);

		/*
		 * Show each Trip's details when it is selected. These are passed to the
		 * child Views within rootView. These are declared in
		 * fragment_Trip_detail.xml
		 */
		if (mItem != null) {

			// text information
			((TextView) rootView.findViewById(R.id.trip_detail_name))
					.setText(mItem.getName());
			((TextView) rootView.findViewById(R.id.trip_detail_info))
					.setText(mItem.getInfo());
			((TextView) rootView
					.findViewById(R.id.trip_detail_heading_pinterests))
					.setText("Below are the places you may be interested in on this trip ("
							+ mItem.getSize() + " total):");
			((TextView) rootView.findViewById(R.id.trip_detail_pinterests))
					.setText(listToText(mItem.getAllPinterests()));

			// button to start Trip
			((Button) rootView.findViewById(R.id.trip_detail_button_start))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							CustomARSetup custom = new CustomARSetup();
							// RelativePositionSetup custom = new
							// RelativePositionSetup(); //For Pinterest markers
							// which don't use GPS coordinates
							// custom.context = currentActivity;
							// edited to use getActivity(), avoiding NPEs when
							// restarting Trip, when currentActivity is not
							// initialised
							custom.context = getActivity();
							currentActivity = getActivity();
							/*
							 * custom.mapView = (MapView)
							 * getSupportFragmentManager
							 * ().findFragmentByTag("FragmentContainingMap"
							 * ).getView().findViewById(R.id.map); ((ViewGroup)
							 * custom
							 * .mapView.getParent()).removeView(custom.mapView);
							 */
							custom.setTrip(mItem);
							ArActivity.startWithSetup(currentActivity, custom);
							/*
							 * CustomARSetup custom = new CustomARSetup();
							 * custom.context = currentActivity;
							 * custom.setTrip(mItem);
							 * ARActivityPlusMaps.startWithSetup
							 * (currentActivity, custom); //changed to include
							 * map
							 */
						}
					});

			// button to start demo Trip, using RelativePositionSetup
			((Button) rootView
					.findViewById(R.id.trip_detail_button_start_relative))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// CustomARSetup custom = new CustomARSetup();
							RelativePositionSetup custom = new RelativePositionSetup(); // For
																						// Pinterest
																						// markers
																						// coordinates
							// custom.context = currentActivity;
							// edited to use getActivity(), avoiding NPEs when
							// restarting Trip, when currentActivity is not
							// initialised
							custom.context = getActivity();
							currentActivity = getActivity();
							custom.setTrip(mItem);
							ArActivity.startWithSetup(currentActivity, custom);
						}
					});

		}
		return rootView;
	}

	/*
	 * Converts list of Pinterests into String containing numbered Pinterest
	 * names.
	 */
	private String listToText(List<Pinterest> Pinterests) {
		String result = "";
		int count = 1; // normal counting starts at 1!
		for (Pinterest p : Pinterests) {
			result += (count + ". " + p.getName() + "\n");
			count++;
		}
		return result;
	}
}
