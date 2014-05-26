package ucsc.ar;

import geo.GeoObj;
import gl.Color;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gui.GuiSetup;

import java.util.List;
import java.util.Stack;

import system.EventManager;
import system.Setup;
import ucsc.ar.source.TripCreator;
import ucsc.ar.structure.Pinterest;
import ucsc.ar.structure.Trip;
import util.Log;
import worldData.SystemUpdater;
import worldData.World;
import actions.Action;
import actions.ActionCalcRelativePos;
import actions.ActionRotateCameraBuffered;
import actions.ActionWaitForAccuracy;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import commands.Command;
import commands.ui.CommandShowToast;

public class CustomARSetup extends Setup {
	public int activeTripID; // set by caller module
	private Trip theActiveTrip;
	private Pinterest theCurrentPinterest;

	private boolean minAccuracyReached; // Whether the GPS location is accurate,
										// initially false
	private String nextPlace; // Name of the next location
	private Location nextLocation; // Pinterest of the next location
	private int distanceAway; // Distance to the next Pinterest in metres
	private GLCamera camera;
	private World world;
	private GLFactory objectFactory;
	private ActionWaitForAccuracy minAccuracyAction;
	private Action rotateGLCameraAction;
	private GuiSetup guiSetup;
	private TextView distanceInfo;
	private Stack<GeoObj> markers; // A stack of the markers corresponding to
									// the displayed Pinterests
	private Stack<Pinterest> markedPinterests; // A Stack of the displayed
												// Pinterests

	private static final String LOG_TAG = "CustomARSetup";

	public Context context;

	// public MapView mapView;
	// public CustomARSetup(Context mainActivity){
	// this.context = mainActivity;
	// }

	public CustomARSetup() {

	}

	public void setTrip(int id) {
		activeTripID = id;
		theActiveTrip = TripCreator.parseXml(context.getResources()
				.openRawResource(activeTripID));
	}

	public void setTrip(Trip Trip) {
		theActiveTrip = Trip;
		theActiveTrip.setIndex(0);
	}

	@Override
	public void _a_initFieldsIfNecessary() {
		camera = new GLCamera();
		world = new World(camera);
		markers = new Stack<GeoObj>();
		markedPinterests = new Stack<Pinterest>();
		minAccuracyReached = false;
		distanceInfo = new TextView(getActivity());

		// PRECONDITION: setTrip(int) or setTrip(Trip) has already been called
		// before creating this Setup

		Trip Trip = theActiveTrip;

		// Trip Trip = new
		// Trip(context.getResources().openRawResource(activeTripID));
		List<Pinterest> PinterestsTest = Trip.getAllPinterests();
		System.out.println("TripData");
		System.out.println("ID: " + Trip.getID());
		System.out.println("Name: " + Trip.getName());
		System.out.println("Info: " + Trip.getInfo());
		System.out.println("Size: " + Trip.getSize());
		for (int i = 0; i < Trip.getSize(); i++) {
			System.out.println("Pinterest " + i);
			System.out.println("Name: " + PinterestsTest.get(i).getName());
			System.out.println("Latitude: "
					+ PinterestsTest.get(i).getLatitude());
			System.out.println("Longitude: "
					+ PinterestsTest.get(i).getLongitude());
			System.out.println("Info: " + PinterestsTest.get(i).getInfo());
			if (PinterestsTest.get(i).getDataSource() != null)
				System.out.println("HashTags: "
						+ PinterestsTest.get(i).getDataSource()
								.getSearchString());
		}
		// theActiveTrip = Trip;
		distanceInfo.setText("Trip loaded: " + Trip.getName());

	}

	private void addNextPinterest() {
		if (theActiveTrip.hasNext()) {
			final Pinterest p = theActiveTrip.getCurrentPinterest();
			theActiveTrip.incrementIndex();
			theCurrentPinterest = p;
			updateDistanceInfo();

			final GeoObj o = new GeoObj(p.getLatitude(), p.getLongitude());
			o.setComp(objectFactory.newDiamond(Color.green())); // Green
																// represents
																// unvisited

			o.setOnClickCommand(new Command() {
				@Override
				public boolean execute() {
					String data = p.getDataSourceInfo();
					if (p.hasDataSource() && data != "") {
						displayDataSourceInfo(p.getName(), data);
					} else {
						displayDataSourceInfo(p.getName(),
								"No social media data for this location");
					}
					return true;
				}
			});

			o.setOnDoubleClickCommand(new Command() {
				@Override
				public boolean execute() {
					displayInfo(p.getName(), p.getInfo());
					if (theCurrentPinterest == p) { // If current Pinterest add
													// the next
						addNextPinterest();
						o.setColor(Color.blue()); // Blue represents visited
					}
					return true;
				}
			});

			world.add(o); // Add the next marker to the world
			markers.push(o); // Push it to the stack of markers
			markedPinterests.push(p); // Push the Pinterest to the stack of
										// displayed Pinterests
		}

	}

	private void updateDistanceInfo() {
		nextPlace = theCurrentPinterest.getName();
		nextLocation = new Location("nextLocation");
		nextLocation.setLatitude(theCurrentPinterest.getLatitude());
		nextLocation.setLongitude(theCurrentPinterest.getLongitude());
	}

	/* Display Help dialog screen */
	private void help() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		View view = View.inflate(getActivity(), R.layout.help_ar_layout, null);
		builder.setView(view);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void skipPinterest() {
		markers.peek().setColor(Color.blue());
		displayInfo(theCurrentPinterest.getName(),
				theCurrentPinterest.getInfo());
		addNextPinterest();
	}

	private void previousPinterest() {
		if (markers.size() > 1) {
			guiSetup.getTopView().removeAllViews();
			GeoObj m = markers.peek();
			// A rather crude test
			if (m.getGraphicsComponent().getColor().equals(Color.green())) {
				world.remove(markers.pop());
				theActiveTrip.decrementIndex();
				markedPinterests.pop();
			}
			m = markers.peek();
			m.setColor(Color.green());
			theCurrentPinterest = markedPinterests.peek();
			updateDistanceInfo();
		}
	}

	// Display a TextView stating a name of a location and the data source's
	// information on it
	private void displayDataSourceInfo(String name, String info) {
		final TextView v = (TextView) View.inflate(getActivity(),
				R.layout.pinterest_layout, null);
		v.setText(name + "\n" + info);
		v.setTextColor(Color.white().toIntARGB());
		v.setBackgroundColor(Color.blueTransparent().toIntARGB());
		v.setTextSize(24f);
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				guiSetup.getTopView().removeAllViews();
			}

		});

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				guiSetup.getTopView().removeAllViews();
				guiSetup.addViewToTop(v);
			}
		});

	}

	// Display the name of a location and the defined information about it
	private void displayInfo(String name, String info) {
		/**
		 * InfoScreenSettings i = new InfoScreenSettings(getActivity());
		 * i.addText(name); i.addText(info);
		 * ActivityConnector.getInstance().startActivity(getActivity(),
		 * InfoScreen.class, i);
		 **/
		// Using DataSourceDisplay since InfoScreen doesn't display as intended
		displayDataSourceInfo(name, info);
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer glRenderer,
			GLFactory objectFactory, GeoObj currentPosition) {
		this.objectFactory = objectFactory;
		glRenderer.addRenderElement(world);
		// addNextPinterest();
	}

	@Override
	public void _c_addActionsToEvents(final EventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {

		rotateGLCameraAction = new ActionRotateCameraBuffered(camera);
		eventManager.addOnOrientationChangedAction(rotateGLCameraAction);
		eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(
				world, camera));

		minAccuracyAction = new ActionWaitForAccuracy(getActivity(), 24.0f, 10) {
			@Override
			public void minAccuracyReachedFirstTime(Location l,
					ActionWaitForAccuracy a) { // Add the first Pinterest once
												// there is good signal
				minAccuracyReached = true;
				addNextPinterest();
				if (!eventManager.getOnLocationChangedAction().remove(a)) {
					Log.e(LOG_TAG,
							"Could not remove minAccuracyAction from the onLocationChangedAction list");
				}
			}
		};
		eventManager.addOnLocationChangedAction(minAccuracyAction);
		eventManager.addOnLocationChangedAction(new Action() {

			@Override
			public boolean onLocationChanged(Location location) {
				if (minAccuracyReached) {
					Location l = camera.getGPSLocation();
					distanceAway = (int) l.distanceTo(nextLocation);
					if (distanceAway < 5) {
						// Add the next Pinterest if within 5m of the current
						markers.peek().setColor(Color.blue());
						displayInfo(theCurrentPinterest.getName(),
								theCurrentPinterest.getInfo());
						addNextPinterest();
					}
					updateDistanceInfo();
					distanceInfo.setText("Next location: " + nextPlace
							+ ", Distance: " + distanceAway + "m");
				}
				return true; // So that it is never removed from the list
			}

		});
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		updater.addObjectToUpdateCycle(world);
		updater.addObjectToUpdateCycle(rotateGLCameraAction);
	}

	@Override
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, Activity activity) {
		this.guiSetup = guiSetup;
		guiSetup.addViewToTop(minAccuracyAction.getView());

		// Help Button
		guiSetup.addImangeButtonToRightView(R.drawable.ic_action_help,
				new Command() {

					@Override
					public boolean execute() {
						help();
						return true;
					}
				});

		// Next Pinterest Button
		guiSetup.addButtonToBottomView(new Command() {

			@Override
			public boolean execute() {
				if (minAccuracyReached) {
					skipPinterest();
				} else {
					CommandShowToast.show(getActivity(),
							"Waiting on GPS accuracy");
				}
				return true;
			}

		}, "Next");

		// Previous Pinterest Button
		guiSetup.addButtonToBottomView(new Command() {

			@Override
			public boolean execute() {
				if (minAccuracyReached) {
					previousPinterest();
				} else {
					CommandShowToast.show(getActivity(),
							"Waiting on GPS accuracy");
				}
				return true;

			}

		}, "Previous");

		// Show Map Button
		guiSetup.addButtonToBottomView(new Command() {

			@Override
			public boolean execute() {
				if (isOnline()) {
					Intent intent = new Intent(getActivity(), MapActivity.class);
					Pinterest[] p = theActiveTrip.getAllPinterestsAsArray();
					int n = p.length;
					double[] lats = new double[n];
					double[] longs = new double[n];
					String[] names = new String[n];
					for (int i = 0; i < n; i++) {
						lats[i] = p[i].getLatitude();
						longs[i] = p[i].getLongitude();
						names[i] = p[i].getName();
					}
					intent.putExtra("LATS", lats);
					intent.putExtra("LONGS", longs);
					intent.putExtra("NAMES", names);
					intent.putExtra("CURRENT",
							Math.max(0, theActiveTrip.getIndex() - 1));

					getActivity().startActivity(intent);
					return true;
				}
				Log.d("CustomARSetup.Show map",
						"No internet connection, not displaying map.");
				CommandShowToast.show(getActivity(), "No internet connection");
				return false;
			}

		}, "Show map");

		// Text stating the next location and the distance to it
		guiSetup.addViewToBottom(distanceInfo);
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

}