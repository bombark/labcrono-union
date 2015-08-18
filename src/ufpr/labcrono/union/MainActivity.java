package ufpr.labcrono.union;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/* Para fazer:
 * - Verificar se o arquivo foi criado
 * - Criar as pastas, se nao existirem
 */

public class MainActivity extends Activity {
	TextView tv1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

    	tv1 = (TextView) findViewById(R.id.textView1);
    	tv1.setText("Quantidade de Pesquisas: "+Integer.toString( this.getQtdePesquisas() ));
    	
    	Button button = (Button) findViewById(R.id.button1);
    	button.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			String filename = MainActivity.this.generate_and_save();
        		Toast.makeText(MainActivity.this, "relatorio gerado em "+filename, Toast.LENGTH_SHORT).show();
    		}
    	});
	}

	@Override
	protected void onResume(){
		super.onResume();
		tv1.setText("Quantidade de Pesquisas: "+Integer.toString( this.getQtdePesquisas() ));
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
	
    
    int getQtdePesquisas(){
    	File sdcard = Environment.getExternalStorageDirectory();
    	File fd_json = new File( sdcard,"/ufpr.labcrono.proj1/pesquisas" );
    	File listFile[] = fd_json.listFiles();
    	if (listFile != null) {
    		return listFile.length;
    	}
    	return 0;
    }
	  
    
    String getValues(JSONObject issue){
    	String res = "";
    	try {
			String classe = issue.getString("class");
			
			if ( classe.equals("Text") || classe.equals("Int") || classe.equals("Date") || classe.equals("Boolean") ){
				res += issue.getString("value").replace('|', ' ').replace('\n', ' ');
			} else if ( classe.equals("Enum") ){
				String id = issue.getString("value").replace('|', ' ').replace('\n', ' ');
				res += issue.getJSONArray("box").getJSONObject( Integer.parseInt(id) ).getString("title");
			} else if ( classe.equals("Checkbox") ){
				JSONArray box = issue.getJSONArray("box");
				JSONArray value = issue.getJSONArray("value");
				if ( value.length() > 0 ){
					res = box.getJSONObject(0).getString("title");
					for (int i=1; i<value.length(); i++){
						if ( value.getBoolean(i) ){
							res += ", "+box.getJSONObject(i).getString("title");
						}
					}
				}
			}
			
			
			if ( classe.equals("Boolean") && issue.has("box") ){
			   	JSONArray box = issue.getJSONArray("box");
		    	for (int i=0; i<box.length(); i++){
		    		res += "|" + this.getValues( box.getJSONObject(i) );
		    	}
			}
		} catch (JSONException e) {
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
		    		res += "|" + this.getTitles( box.getJSONObject(i) );
		    	}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return res;
    }
    
    
    
    String generate_and_save(){
    	try {
	    	File sdcard = Environment.getExternalStorageDirectory();
	    	File fd_json = new File( sdcard,"/ufpr.labcrono.proj1/pesquisas" );
	    	File listFile[] = fd_json.listFiles();
	    	if (listFile != null && listFile.length > 0) {
				String res  = new String();
				
				// Coloca os titulos das perguntas na primeira linha
				String text = loadJSON( listFile[0].getAbsolutePath() );
				JSONArray  form = new JSONArray( text );
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
	        			res += this.getValues( form.getJSONObject(0) );
						for (int j=1; j<form.length(); j++){
		        			res += "|" + this.getValues( form.getJSONObject(j) );	
			        	}
					}
		        	res += "\n";
				}
	    		
	    		// Salva todo o formulario em um arquivo
	    		return this.save(res);
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return "";
    }
    
    
    String save(String text){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmm");
    	String filename = sdf.format(new Date());
    	
    	FileOutputStream outputStream;
    	File sdcard = Environment.getExternalStorageDirectory();
    	File fd = new File( sdcard,"/ufpr.labcrono.proj1/resultados/"+filename+".txt" );
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
    
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

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
}
