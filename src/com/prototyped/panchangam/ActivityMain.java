package com.prototyped.panchangam;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
	private HttpRequestTask httpRequestTask;
	private int slots[][]={
							{8,5,7},
							{2,4,6},
							{7,3,5},
							{5,2,4},
							{6,1,3},
							{4,7,2},
							{3,6,1}
							};
	private int dayInWeek;
	private boolean timeFormat;
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        urlString="http://api.geonames.org/timezoneJSON?";
        timeFormat=android.text.format.DateFormat.is24HourFormat(this);
        textview=(TextView)findViewById(R.id.mainTextView);
    	
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1000, this);
        
        makeToast("Recieving GPS coordinates");
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


	@SuppressWarnings("deprecation")
	public void onLocationChanged(Location location) 
	{
		DateFormat dateformat=new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		Date date = new Date();
		dayInWeek=date.getDay();
		
		latitude=location.getLatitude()+"";
		longitude=location.getLongitude()+"";
		time=dateformat.format(date);
		
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
        	JSONObject jsonObject;
        	
        	try 
        	{
				jsonObject=new JSONObject(result);
				sunrise=jsonObject.getString("sunrise");
				sunset=jsonObject.getString("sunset");
				
				Date dateSunrise;
				Date dateSunset;
				SimpleDateFormat sdf;
				
				if(timeFormat)
				{
					dateSunrise=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(sunrise);
					dateSunset=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(sunset);
					sdf=new SimpleDateFormat("HH:mm", Locale.ENGLISH);
				}
				else
				{
					dateSunrise=new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.ENGLISH).parse(sunrise);
					dateSunset=new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.ENGLISH).parse(sunset);
					sdf=new SimpleDateFormat("hh:mm", Locale.ENGLISH);
				}
				
				Calendar start=Calendar.getInstance();
				Calendar end=Calendar.getInstance();
				String rakukalam="";
				String yamagundam="";
				String guligai="";
				
				long duration=dateSunset.getTime()-dateSunrise.getTime();
				duration/=8;
				
				start.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][0]-1)*duration));
				end.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][0])*duration));
				rakukalam=sdf.format(start.getTime()) + " to " + sdf.format(end.getTime());
				
				start.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][1]-1)*duration));
				end.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][1])*duration));
				yamagundam=sdf.format(start.getTime()) + " to " + sdf.format(end.getTime());
				
				start.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][2]-1)*duration));
				end.setTimeInMillis(dateSunrise.getTime()+((slots[dayInWeek][2])*duration));
				guligai=sdf.format(start.getTime()) + " to " + sdf.format(end.getTime());
				
				textview.setText("Sunrise\n" + sunrise +
									"\nSunset\n" + sunset +
									"\n\nRakukalam\n" + rakukalam +
									"\n\nYamagundam\n" + yamagundam +
									"\n\nGuligai\n" + guligai);
			} 
        	catch (JSONException e) 
        	{
				e.printStackTrace();
			}
        	catch(ParseException e)
        	{
        		
        	}
        }
    }
	
	private void makeToast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
