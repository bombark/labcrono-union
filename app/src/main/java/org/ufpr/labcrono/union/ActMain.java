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


	/* INTERFACE =================================================================================*/

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
					ActMain.this.move_toBackup(form_name);
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
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(ActMain.this, "[ERROR - button_bkp]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
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

	/** Show
	 *
	 */
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


	/** Update
	 *
	 */
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


	/*--------------------------------------------------------------------------------------------*/


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


	/** Get the amount of the llll in the folder ./$name/results/
	 * @param name
	 * @return
	 */
	int getQtdePesquisas(String name){
		File fd_json = new File( this.root, name+"/results" );
		File listFile[] = fd_json.listFiles();
		if (listFile != null) {
			return listFile.length;
		}
		return 0;
	}

	/** Get the amount of the aaa in the folder ./$name/backup/
	 * @param name
	 * @return
	 */
	int getQtdeBackup(String name){
		File fd_json = new File( this.root, name+"/backup" );
		File listFile[] = fd_json.listFiles();
		if (listFile != null) {
			return listFile.length;
		}
		return 0;
	}





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

		Form form = new Form();
		form.load( listFile[0].getAbsolutePath() );
		this.stat = new Stat( form.getSize() );

		// Coloca o titulo das perguntas na primeira linha
		res += form.getTitle(0);
		for (int j=1; j<form.getSize(); j++){
			res += "|" + form.getTitle(j);
		}
		res += "\n";


		// Preenche com as respostas
		for (int i=0; i<listFile.length; i++) {
			form = new Form();
			form.load( listFile[i].getAbsolutePath() );
			res += form.getValue(0);
			form.addStatistic(0, this.stat);
			for (int j = 1; j < form.getSize(); j++) {
				res += "|" + form.getValue(j);
				form.addStatistic(j, this.stat);
			}
			res += "\n";
		}


		// Preenche com as Estatisticas
		res += stat.toString( form );

		// Salva todo o formulario em um arquivo
		return this.save(form_name,res);
	}


	String save(String form_name, String text){
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		//String filename = sdf.format(new Date());

		String filename = "results.txt";
		FileOutputStream outputStream;
		File fd = new File( this.root,form_name+"/"+filename );
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