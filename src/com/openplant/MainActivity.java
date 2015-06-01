package com.openplant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, OnItemSelectedListener {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
	private Spinner plantsSpinner;
	private Button refreshButton;
	
	public Spinner getPlantsSpinner() {
		return plantsSpinner;
	}
	
	public Button getRefreshButton() {
		return refreshButton;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// VIEWS
		plantsSpinner = (Spinner)findViewById(R.id.plantNameSpinner);
		refreshButton = (Button)findViewById(R.id.refreshButton);
		
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.plant_names,
				android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		plantsSpinner.setAdapter(spinnerAdapter);
		plantsSpinner.setSelection(spinnerAdapter.getPosition(getResources().getString(R.string.plant_1)));
		plantsSpinner.setOnItemSelectedListener(this);
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				asyncTask();
			}
		});
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		View spinner = (View)parent;
		if(spinner.getId() == R.id.plantNameSpinner) {
			asyncTask();
		}
    }
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// null		
	}
	
	public void asyncTask() {
		plantsSpinner.setClickable(false);
		refreshButton.setClickable(false);
		CouchClientTask task = new CouchClientTask();
		task.setContext(this);
		task.execute(plantsSpinner.getSelectedItem().toString());
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		mTitle = getString(R.string.title_section1);
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	
	// TACHE ASYNCHRONE
	private class CouchClientTask extends AsyncTask<String, Void, List<String>> {
		
		private static final String namespace = "http://tempuri.org/";
		
		private static final String wsdlPath =  "http://10.145.128.97:8734/NoSQLProject/Service/";
		
		private static final String action = "GetAverages";
		
		private MainActivity ctx;
		
		protected void setContext(MainActivity ctx) {
			this.ctx = ctx;
		}
		
		@Override
		protected List<String> doInBackground(String... params) {
			// ASSERTION
			if(params.length < 1)
				return null;
			
			String plantName = params[0];
			
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			SoapObject request = new SoapObject(namespace, action);
			
			// DEFINITION DE LA REQUETE
			envelope.dotNet = true;
			request.addProperty("plantName", plantName);
			
			// Requête
			envelope.bodyOut = request;
			HttpTransportSE transport = new HttpTransportSE(wsdlPath);
			try {
				transport.call(namespace + "IService/" + action, envelope);
			} catch (HttpResponseException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return null;
			}
			
			// Récup depuis le service
			SoapObject results = null;
			try {
				results = (SoapObject)envelope.getResponse();
			} catch (SoapFault e) {
				e.printStackTrace();
				return null;
			}
			
			if(results == null)
				return null;
			
			List<String> data = new ArrayList<String>();
			
			// Conversion en liste
			for(int i = 0; i < results.getPropertyCount(); i++)
				data.add(results.getPropertyAsString(i));
			
			return data;
		}
		
		// Fin de l'async
		@Override
		protected void onPostExecute(List<String> result) {
			ctx.getPlantsSpinner().setClickable(true);
			ctx.getRefreshButton().setClickable(true);
			TextView humidityText = (TextView)ctx.findViewById(R.id.humidityText);
			TextView luminosityText = (TextView)ctx.findViewById(R.id.luminosityText);
			TextView plantType = (TextView)ctx.findViewById(R.id.plantType);
			
			// AFFICHAGE
			if(result != null && result.size() == 4) {
				String humidity = result.get(2).replace(',', '.');
				String luminosity = result.get(3).replace(',', '.');
				
				Double humidityRate = Double.parseDouble(humidity);
				Double luminosityRate = Double.parseDouble(luminosity)/10.;
				if(luminosityRate > 100)
					luminosityRate = 100.;
				
				humidityText.setText(String.format("%.2f", humidityRate));
				luminosityText.setText(String.format("%.2f", luminosityRate));
				plantType.setText(result.get(1));
			} else {
				humidityText.setText("");
				luminosityText.setText("");
				plantType.setText("");
				MessageManager.showError(ctx);
			}
		}
	}
}
