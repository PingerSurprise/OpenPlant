package com.openplant;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<Map<String, String>> {
	private Context context;
	private List<Map<String, String>> values;
	
	public HistoryAdapter(Context context, List<Map<String, String>> values) {
		super(context, R.layout.list_elem_history, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.list_elem_history,parent,false);
		
		// Vues
		TextView time = (TextView) row.findViewById(R.id.time),
			humidity = (TextView) row.findViewById(R.id.humidity),
			luminosity = (TextView) row.findViewById(R.id.luminosity);
		
		// Init vues
		Double luminosityRate = Double.parseDouble(values.get(position).get("luminosite"))/10.;
		if(luminosityRate > 100)
			luminosityRate = 100.;
		
		time.setText(values.get(position).get("time"));
		humidity.setText("Humidité : " + values.get(position).get("humidite_terre"));
		luminosity.setText("Luminosité (%) : " + String.format("%.2f", luminosityRate));
		
		return row;
	}
}
