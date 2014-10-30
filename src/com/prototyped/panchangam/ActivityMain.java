package com.prototyped.panchangam;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class ActivityMain extends Activity implements LocationListener
{
	private LocationManager locationManager;
	private TextView textview;
	private String latitude;
	private String longitude;
	private String time;
	private String sunrise;
	private String sunset;
	private String urlString;
	private String httpGetResult;
	private HttpRequestTask httpRequestTask;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        urlString="http://api.geonames.org/timezoneJSON?";
        
        textview=(TextView)findViewById(R.id.mainTextView);
    	
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1000, this);
        
        httpRequestTask=new HttpRequestTask();
    }

    protected void onResume()
    {
    	super.onResume();
    	
    	locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }
    
    protected void onPause()
    {
    	super.onPause();
    	
    	locationManager.removeUpdates(this);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch(item.getItemId())
    	{
    		case R.id.action_settings:
    		{
    			break;
    		}
        }
    	
        return super.onOptionsItemSelected(item);
    }


	public void onLocationChanged(Location location) 
	{
		DateFormat dateformat=new SimpleDateFormat("dd-MM-yyyy");
		Date date = new Date();
		
		latitude=location.getLatitude()+"";
		longitude=location.getLongitude()+"";
		time=dateformat.format(date);
		
		textview.setText(latitude+"\n"+longitude+"\n"+time);
		
		urlString+="lat="+latitude+
					"&lng="+longitude+
					"&date="+time+
					"&username=ranjith";
		
		locationManager.removeUpdates(this);
		
		httpRequestTask.execute(urlString);
		
		makeToast("Requesting JSON data");
	}

	public void onProviderDisabled(String provider) 
	{
		makeToast("GPS turned off");
	}


	public void onProviderEnabled(String provider) 
	{
		makeToast("GPS turned on");
	}

	public void onStatusChanged(String provider, int status, Bundle bundle) 
	{
		
	}
	
	private class HttpRequestTask extends AsyncTask<String, Void, String> 
    {
        protected String doInBackground(String... url) 
        {
        	String result="";
          
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGetRequest=new HttpGet(url[0]);
            
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
        	
        	try
        	{
        		result=httpClient.execute(httpGetRequest, responseHandler);
        	}
        	catch(ClientProtocolException e)
        	{
        		
        	}
        	catch(IOException e)
        	{
        			
        	}
        	
        	httpClient.getConnectionManager().shutdown();
        	
            return result;
        }

        protected void onPostExecute(String result) 
        {
        	httpGetResult=result;
        	JSONObject jsonObject;
        	
        	try 
        	{
				jsonObject=new JSONObject(result);
				sunrise=jsonObject.getString("sunrise");
				sunset=jsonObject.getString("sunset");
				
				textview.setText(sunrise+"\n"+sunset);
			} 
        	catch (JSONException e) 
        	{
				e.printStackTrace();
			}
        }
    }
	
	private void makeToast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
