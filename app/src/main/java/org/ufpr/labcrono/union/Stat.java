package org.ufpr.labcrono.union;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by fgbombardelli on 05/02/16.
 */
public class Stat {
	Vector<Integer>[] issuepkg;


	Stat(int size){
		this.issuepkg = (Vector<Integer>[]) new Vector[size];
		for(int i = 0; i < issuepkg.length; i++)
			issuepkg[i] = null;
	}

	void add(int id, int option_size, int option_choosed){
		Log.d("c", Integer.toString(option_choosed));
		if ( issuepkg[id] == null ) {
			Vector<Integer> option = new Vector<Integer>();
			option.setSize(option_size);
			for (int i = 0; i < option_size; i++) {
				option.set(i, 0);
			}
			option.set(option_choosed, 1);
			issuepkg[id] = option;
		} else {
			int val = issuepkg[id].get(option_choosed) + 1;
			issuepkg[id].set(option_choosed, val);
		}
	}


	public String toString(Form form) throws JSONException {
		String res = "\n\n\n\n\nEstatisticas\n";
		for (int i=0; i<issuepkg.length; i++){
			if ( issuepkg[i] != null){
				res += form.getTitle(i);
				for (int j=0; j<issuepkg[i].size(); j++){
					res += "|" + Integer.toString(issuepkg[i].get(j));
				}
				res += "\n";
			}
		}
		return res;
	}

}
