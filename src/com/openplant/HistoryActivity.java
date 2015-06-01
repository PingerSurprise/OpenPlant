package com.openplant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class HistoryActivity extends Activity implements
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
	private EditText dateText;
	private ListView historyList;
	private Button showButton;
	
	public Spinner getPlantsSpinner() {
		return plantsSpinner;
	}
	
	public EditText getDateText() {
		return dateText;
	}
	
	public ListView getHistoryList() {
		return historyList;
	}

	public Button getShowButton() {
		return showButton;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

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
		dateText = (EditText)findViewById(R.id.dateText);
		historyList = (ListView)findViewById(R.id.upList);
		showButton = (Button)findViewById(R.id.showButton);
		
		// SPINNER
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.plant_names,
				android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		plantsSpinner.setAdapter(spinnerAdapter);
		plantsSpinner.setSelection(spinnerAdapter.getPosition(getResources().getString(R.string.plant_1)));
		plantsSpinner.setOnItemSelectedListener(this);
		
		// DATE TEXT
		Calendar now = Calendar.getInstance();
		dateText.setText(String.format("%04d", now.get(Calendar.YEAR)) + "-"
				+ String.format("%02d", now.get(Calendar.MONTH) + 1) + "-"
				+ String.format("%02d", now.get(Calendar.DAY_OF_MONTH)));
		
		// SHOW BUTTON
		showButton.setOnClickListener(new View.OnClickListener() {
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
	
	public void asyncTask() {
		plantsSpinner.setClickable(false);
		showButton.setClickable(false);
		CouchClientTask task = new CouchClientTask();
		task.setContext(this);
		task.execute(plantsSpinner.getSelectedItem().toString(), dateText.getText().toString());
	}
	
	@Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing happens
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
		mTitle = getString(R.string.title_section3);
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
			getMenuInflater().inflate(R.menu.history, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_history,
					container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((HistoryActivity) activity).onSectionAttached(getArguments()
					.getInt(ARG_SECTION_NUMBER));
		}
	}
	
	// TACHE ASYNCHRONE
	private class CouchClientTask extends AsyncTask<String, Void, List<String>> {
		
		private static final String namespace = "http://tempuri.org/";
		
		private static final String wsdlPath =  "http://10.145.128.97:8734/NoSQLProject/Service/";
		
		private static final String action = "GetHistory";
		
		private HistoryActivity ctx;
		
		protected void setContext(HistoryActivity ctx) {
			this.ctx = ctx;
		}
		
		@Override
		protected List<String> doInBackground(String... params) {
			// ASSERTION
			if(params.length < 2)
				return null;
			
			String plantName = params[0];
			String date = params[1];
			
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			SoapObject request = new SoapObject(namespace, action);
			
			// DEFINITION DE LA REQUETE
			envelope.dotNet = true;
			request.addProperty("plantName", plantName);
			request.addProperty("date", date);
			
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
			ctx.getShowButton().setClickable(true);
			
			List<Map<String, String>> history = new ArrayList<>();
			Map<String, String> tmp = null;
			JSONObject entity = null;
			Iterator<?> keys = null;
			String key = null;
			String value = null;
			
			if(result == null || result.size() == 0) {
				MessageManager.showError(ctx);
				return;
			}
			
			// Désérialisation
			for(String doc: result) {
				try {
					entity = new JSONObject(doc);
				} catch (JSONException e) {
					result = null;
					break;
				}
				
				tmp = new HashMap<>();
				keys = entity.keys();
				while(keys.hasNext()) {
					key = (String)keys.next();
					try {
						value = entity.getString(key);
					} catch (JSONException e) {
						result = null;
						break;
					}
					tmp.put(key, value);
				}
				
				if(result == null)
					break;
				
				history.add(tmp);
			}
			
			// AFFICHAGE
			if(result == null || result.size() == 0) {
				MessageManager.showError(ctx);
			} else {
				ListView historyList = (ListView)ctx.getHistoryList();
				historyList.setAdapter(new HistoryAdapter(ctx, history));
			}
		}
	}
}
