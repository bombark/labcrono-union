package org.ufpr.labcrono.union;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;







/* Para fazer:
 * - Verificar se o arquivo foi criado
 * - Criar as pastas, se nao existirem
 */

public class ActMain extends AppCompatActivity {
	static String url_base = "ufpr.labcrono.issue";
	LinearLayout ll;
	File root;
	RadioGroup rg;

	Stat stat;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_act_main);

		File sdcard = Environment.getExternalStorageDirectory();
		this.root = new File( sdcard, this.url_base );
		this.ll = (LinearLayout) findViewById(R.id.body);


		this.show();

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int selectedId = rg.getCheckedRadioButtonId();
				RadioButton formchoosed = (RadioButton) findViewById(selectedId);
				String form_name = formchoosed.getPrivateImeOptions();

				try {
					String filename = ActMain.this.generate_and_save(form_name,"results");
					//ActMain.this.move_toBackup(form_name);
					ActMain.this.update();
					Toast.makeText(ActMain.this, "relatorio gerado em " + form_name + "/" + filename, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					Toast.makeText(ActMain.this, "[ERROR - button]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(ActMain.this, "[ERROR - button]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});


		Button button_bkp = (Button) findViewById(R.id.button_bkp);
		button_bkp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int selectedId = rg.getCheckedRadioButtonId();
				RadioButton formchoosed = (RadioButton) findViewById(selectedId);
				String form_name = formchoosed.getPrivateImeOptions();

				try {
					String filename = ActMain.this.generate_and_save(form_name, "backup");
					ActMain.this.update();
					Toast.makeText(ActMain.this, "relatorio gerado em " + form_name + "/" + filename, Toast.LENGTH_SHORT).show();
				} catch ( JSONException e ){
					e.printStackTrace();
					Toast.makeText(ActMain.this, "[ERROR - button_bkp]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				} catch ( IOException e ){
					e.printStackTrace();
					Toast.makeText(ActMain.this, "[ERROR - button_bkp]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});


	}

	@Override
	protected void onResume(){
		super.onResume();
		//tv1.setText("Quantidade de Pesquisas: "+Integer.toString( this.getQtdePesquisas() ));
		this.update();
	}

	public String loadJSON(String url) {
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


	public void show(){

		ArrayList<String> formpkg;
		try {
			formpkg = this.listForms();

			this.rg = new RadioGroup(this);
			final RadioButton[] rb = new RadioButton[50];
			rg.setOrientation(RadioGroup.VERTICAL);
			for (int i=0; i<formpkg.size(); i++) {
				String name = formpkg.get(i);


				int qtde = this.getQtdePesquisas(formpkg.get(i));
				int qtde_bkp = this.getQtdeBackup(formpkg.get(i));

				rb[i]  = new RadioButton(this);
				rb[i].setPrivateImeOptions( name );
				rb[i].setText(name + " : " + Integer.toString(qtde) + " : " + qtde_bkp);

				this.rg.addView(rb[i]);


			}
			ll.addView(this.rg);


		} catch (IOException e ){
			Toast.makeText(this, "[ERROR - show] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}



	public void update(){
		int size = this.rg.getChildCount();
		for (int i=0; i<size; i++) {
			RadioButton rb = (RadioButton) this.rg.getChildAt(i);
			String form_name = rb.getPrivateImeOptions();
			int qtde = this.getQtdePesquisas(form_name);
			int qtde_bkp = this.getQtdeBackup(form_name);
			rb.setText(form_name + " : " + Integer.toString(qtde) + " : " + qtde_bkp);
		}
	}



	public ArrayList<String> listForms() throws IOException {
		ArrayList<String> res = new ArrayList<>();
		File[] files = this.root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				res.add( file.getName() );
			}
		}
		return res;
	}



	int getQtdePesquisas(String name){
		File fd_json = new File( this.root, name+"/results" );
		File listFile[] = fd_json.listFiles();
		if (listFile != null) {
			return listFile.length;
		}
		return 0;
	}

	int getQtdeBackup(String name){
		File fd_json = new File( this.root, name+"/backup" );
		File listFile[] = fd_json.listFiles();
		if (listFile != null) {
			return listFile.length;
		}
		return 0;
	}


	String getValues(JSONObject issue, int id, boolean is_visible){
		String res = "";
		try {
			String classe = issue.getString("class");


			// Booklin
			if ( classe.equals("Boolean") ){
				boolean sub_is_visible = is_visible;
				if ( is_visible ) {
					int value = issue.getInt("value");
					if (value == 1) {
						sub_is_visible = false;
						this.stat.add(id, 2, 0);
						res += "não";
					} else if (value == 2) {
						sub_is_visible = true;
						this.stat.add(id, 2, 1);
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
				this.stat.add(id, box.length(), option_id);
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


		} catch (JSONException e) {
			Toast.makeText(this, "[ERROR - getValues] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return res;
	}

	String getTitles(JSONObject issue){
		String res = "";
		try {
			res += issue.getString("title").replace('|', ' ');
			String classe = issue.getString("class");
			if ( classe.equals("Boolean") && issue.has("box") ){
				JSONArray box = issue.getJSONArray("box");
				for (int i=0; i<box.length(); i++){
					res += "|" + this.getTitles(box.getJSONObject(i));
				}
			}
		} catch (JSONException e) {
			Toast.makeText(this, "[ERROR - getTitles] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return res;
	}


	String getLabels(JSONObject issue){
		String res = "";
		try {
			res += issue.getString("num").replace('|', ' ');
			String classe = issue.getString("class");
			if ( classe.equals("Boolean") && issue.has("box") ){
				JSONArray box = issue.getJSONArray("box");
				for (int i=0; i<box.length(); i++){
					res += "|" + this.getLabels(box.getJSONObject(i));
				}
			}
		} catch (JSONException e) {
			Toast.makeText(this, "[ERROR - getLabels] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return res;
	}


	int getQtdeIssues(JSONObject issue, int total){
		try {
			String classe = issue.getString("class");
			if ( classe.equals("Boolean") && issue.has("box") ){
				JSONArray box = issue.getJSONArray("box");
				for (int i=0; i<box.length(); i++){
					total = getQtdeIssues(box.getJSONObject(i), total);
				}
			}
		} catch (JSONException e) {
			Toast.makeText(this, "[ERROR - getQtdeIssues] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return total+1;
	}



	// form = ["backup","results"];
	String generate_and_save(String form_name, String from) throws JSONException,IOException {

		File fd_json = new File( this.root,form_name+"/"+from );
		File listFile[] = fd_json.listFiles();
		if (listFile == null ) {
			throw new IOException("Nenhum formulario para gerar");
		}
		if ( listFile.length == 0 ) {
			throw new IOException("Nenhum formulario para gerar");
		}
		String res  = new String();

		// Carrega o formulario
		String text = loadJSON( listFile[0].getAbsolutePath() );
		JSONArray  form = new JSONArray( text );

		// Cria um Vetor para guardar as estatisticas
		int size = this.getQtdeIssues(form.getJSONObject(0), 0);
		for (int j=1; j<form.length(); j++){
			size += this.getQtdeIssues(form.getJSONObject(j), 0);
		}
		this.stat = new Stat(size);


		// Coloca os numeros das perguntas na primeira linha
		res += this.getLabels(form.getJSONObject(0));
		for (int j=1; j<form.length(); j++){
			res += "|" + this.getLabels(form.getJSONObject(j));
		}
		res += "\n";

		// Coloca os titulos das perguntas na segunda linha
		res += this.getTitles( form.getJSONObject(0) );
		for (int j=1; j<form.length(); j++){
			res += "|" + this.getTitles( form.getJSONObject(j) );
		}
		res += "\n";

		// Preenche com as respostas
		for (int i = 0; i < listFile.length; i++) {
			text = loadJSON( listFile[i].getAbsolutePath() );
			form = new JSONArray( text );
			if ( form.length() > 0 ){
				res += this.getValues( form.getJSONObject(0), 0, true );
				for (int j=1; j<form.length(); j++){
					res += "|" + this.getValues( form.getJSONObject(j), j, true );
				}
			}
			res += "\n";
		}

		res += stat.toString(form);


		// Salva todo o formulario em um arquivo
		return this.save(form_name,res);
	}


	String save(String form_name, String text){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		String filename = sdf.format(new Date());

		FileOutputStream outputStream;

		File fd = new File( this.root,form_name+"/"+filename+".txt" );
		try {
			outputStream = new FileOutputStream( fd );
			try {
				outputStream.write( text.getBytes() );
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return filename;
	}



	public void move_toBackup(String form_name){
		File fd_res = new File( this.root,form_name+"/results" );
		File fd_bkp = new File( this.root,form_name+"/backup" );

		createDirIfNotExists(fd_bkp);

		File listFile[] = fd_res.listFiles();


		for (int i = 0; i < listFile.length; i++) {
			File from = listFile[i];
			File to = new File(fd_bkp, from.getName());


			int j = 1;
			while ( to.exists() ) {
				String name = from.getName();
				int pos = name.lastIndexOf('.');
				name = name.substring(0,pos-1) + Integer.toString(i) + ".json";
				to = new File(fd_bkp, name);
				j += 1;
			}

			from.renameTo(to);
		}
	}


	/*void move_to_backup(String form_name){

	}*/


	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/


	public boolean createDirIfNotExists(File file) {
		if (!file.exists()) {
			if (!file.mkdirs()) {
				return false;
			}
		}
		return true;
	}

}