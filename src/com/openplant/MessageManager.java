package com.openplant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MessageManager {
	public static void showError(Activity activity) {
		new AlertDialog.Builder(activity)
			.setTitle("Results")
			.setMessage("Une erreur est survenue lors de la récupération des données sur le serveur.\nVeuillez réessayer ultérieurement.")
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// null
				}
			})
			.show();
	}
}
