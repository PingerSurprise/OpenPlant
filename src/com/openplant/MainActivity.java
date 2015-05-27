package com.openplant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

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
		
		// EXEMPLE
		CouchClientTask task = new CouchClientTask();
		task.execute("test", /*"8da4c108fcf9fc6a1b8c4f6792000450", */"huhuhu");
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
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
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
		if (id == R.id.action_settings) {
			return true;
		}
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
	
	// EXEMPLE DE TACHE ASYNCHRONE
	private class CouchClientTask extends AsyncTask<String, Void, List<String>> {
		
		private static final String namespace = "http://tempuri.org/";
		
		private static final String wsdlPath =  "http://10.145.128.97:8734/NoSQLProject/Service/";
		
		// POUR UN OBJET PRIMITIF
//		private static final String action = "GetJsonDocumentById";
		
		// POUR UNE LISTE D'OBJETS
		private static final String action = "GetJsonDocumentsByTitle";
		
		@Override
		protected List<String> doInBackground(String... params) {
			// ASSERTION
			if(params.length < 2)
				return null;
			
			String baseName = params[0];
//			String id = params[1];		// UN OBJET
			String title = params[1];	// UNE LISTE D'OBJETS
			
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			SoapObject request = new SoapObject(namespace, action);
			
			// DEFINITION DE LA REQUETE
			envelope.dotNet = true;
			request.addProperty("baseName", baseName);
			
			// POUR UN OBJET PRIMITIF
//			request.addProperty("id", id);
			
			// POUR UNE LISTE D'OBJETS
			request.addProperty("title", title);
			
			// REQUETE
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
			
			// POUR UN OBJET PRIMITIF
//			SoapPrimitive results = null;
//			try {
//				results = (SoapPrimitive)envelope.getResponse();
//			} catch (SoapFault e) {
//				e.printStackTrace();
//			}
			
			// POUR UNE LISTE D'OBJETS
			SoapObject results = null;
			try {
				results = (SoapObject)envelope.getResponse();
			} catch (SoapFault e) {
				e.printStackTrace();
				return null;
			}
			
			List<String> data = new ArrayList<String>();
			
			// POUR UN OBJET PRIMITIF
//			if(results != null && results.getValue() != null)
//				data.add(results.getValue().toString());
			
			// POUR UNE LISTE D'OBJETS
			if(results != null)
				for(int i = 0; i < results.getPropertyCount(); i++)
					data.add(results.getPropertyAsString(i));
			
			return data;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			if(result == null)
				return;
			
			StringBuilder sb = new StringBuilder();
			
			for(String s: result)
				sb.append(s + "\n");
			
			new AlertDialog.Builder(MainActivity.this)
				.setTitle("Results")
				.setMessage(sb.toString())
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// null
					}
				})
				.show();
		}
	}
}
