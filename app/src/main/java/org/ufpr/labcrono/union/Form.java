package org.ufpr.labcrono.union;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by fgbombardelli on 03/03/16.
 */
public class Form {
	Vector< JSONObject > plane;

	JSONArray original;


	void load(String url) throws JSONException {
		String text = loadJSON( url );
		original = new JSONArray( text );
		this.plane = new Vector<>();
		this.planify();
	}


	String loadJSON(String url){
		String json = null;
		try {
			File fd_json = new File( url );
			if ( fd_json.exists()) {
				FileInputStream is = new FileInputStream (fd_json);
				int size = is.available();
				byte[] buffer;
				buffer = new byte[size];
				is.read(buffer);
				is.close();
				json = new String(buffer, "UTF-8");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;

	}


	String getTitle(int id){
		String res;
		JSONObject obj = this.plane.get(id);
		try {
			res = obj.getString("num") + " " + obj.getString("title").replace('|', ' ').replace('\n', ' ');;
		} catch (JSONException e) {
			res = Integer.toString(id);
		}
		return res;
	}

	int getSize(){
		return plane.size();
	}


	String getValue(int id) throws JSONException {
		JSONObject issue = this.plane.get(id);

		if ( issue.has("ishidden") && issue.getBoolean("ishidden") )
			return "";


		String classe = issue.getString("class");
		if ( classe.equals("Boolean") ){
			int value = issue.getInt("value");
			return (value==2)?"sim":"não";
		}


		if ( classe.equals("Text") || classe.equals("Int") || classe.equals("Date") )
			return issue.getString("value").replace('|', ' ').replace('\n', ' ');


		if ( classe.equals("Enum") ){
			JSONArray box = issue.getJSONArray("box");
			int option_id = issue.getInt("value") - 1;
			return box.getJSONObject(option_id).getString("title");
		}


		if ( classe.equals("Checkbox") ){
			String res = "";
			JSONArray box = issue.getJSONArray("box");
			JSONArray value = issue.getJSONArray("value");
			if (value.length() > 0) {
				res = box.getJSONObject(0).getString("title");
				for (int i = 1; i < value.length(); i++) {
					if (value.getBoolean(i)) {
						res += ", " + box.getJSONObject(i).getString("title");
					}
				}
			}
			return res;
		}

		return "ERRO - Tipo de Pergunta nao implementada pelo Aplicativo Union";
	}




	void addStatistic(int id, Stat stat) throws JSONException {
		String res;
		JSONObject issue = this.plane.get(id);
		String classe = issue.getString("class");

		if (classe.equals("Boolean")) {
			int option = issue.getInt("value") - 1;
			stat.add(id, 2, option);

		} else if ( classe.equals("Enum") ){
			JSONArray box = issue.getJSONArray("box");
			int option_id = issue.getInt("value") - 1;
			stat.add(id, box.length(), option_id);
		}


	}


	/*String getValues(JSONObject issue, int id, boolean is_visible) throws JSONException {



		// Booklin
		if ( classe.equals("Boolean") ){
			boolean sub_is_visible = is_visible;
			if ( is_visible ) {
				int value = issue.getInt("value");
				if (value == 1) {
					sub_is_visible = false;
					//this.stat.add(id, 2, 0);
					res += "não";
				} else if (value == 2) {
					sub_is_visible = true;
					//this.stat.add(id, 2, 1);
					res += "sim";
				}
			}

			JSONArray box = issue.getJSONArray("box");
			for (int i=0; i<box.length(); i++){
				res += "|" + this.getValues(box.getJSONObject(i), id+i, sub_is_visible);
			}
		}

		if ( !is_visible ){
			return res;
		}


		if ( classe.equals("Text") || classe.equals("Int") || classe.equals("Date") ){

			res += issue.getString("value").replace('|', ' ').replace('\n', ' ');

		} else if ( classe.equals("Enum") ){

			JSONArray box = issue.getJSONArray("box");
			int option_id = issue.getInt("value") - 1;
			//this.stat.add(id, box.length(), option_id);
			res += box.getJSONObject(option_id).getString("title");

		} else if ( classe.equals("Checkbox") ){

			JSONArray box = issue.getJSONArray("box");
			JSONArray value = issue.getJSONArray("value");
			if (value.length() > 0) {
				res = box.getJSONObject(0).getString("title");
				for (int i = 1; i < value.length(); i++) {
					if (value.getBoolean(i)) {
						res += ", " + box.getJSONObject(i).getString("title");
					}
				}
			}

		}

		return res;
	}*/




	private void planify() throws JSONException {
		for (int i=0; i<this.original.length(); i++){
			this.planify( original.getJSONObject(i), false );
		}
	}


	private void planify(JSONObject issue, boolean mother_is_hidden) throws JSONException {
		this.plane.add( issue );
		String classe = issue.getString("class");

		boolean is_hidden = false;
		if ( issue.has("ishidden") ) {
			is_hidden = issue.getBoolean("ishidden");
		}

		if (mother_is_hidden) {
			is_hidden = true;
			issue.put("ishidden", is_hidden);
		} /*else {
			if ( classe.equals("Boolean") ) {
				int value = issue.getInt("value");
				if (value == 1) {        // NAO
					is_hidden = true;
				} else if (value == 2) { // SIM
					is_hidden = false;
				}
			}
		}*/

		if ( classe.equals("Boolean") && issue.has("box") ){
			JSONArray box = issue.getJSONArray("box");
			for (int i=0; i<box.length(); i++){
				this.planify( box.getJSONObject(i), is_hidden );
			}
		}
	}

}
