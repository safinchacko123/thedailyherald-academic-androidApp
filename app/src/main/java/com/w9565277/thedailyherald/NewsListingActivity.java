package com.w9565277.thedailyherald;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.net.InetAddress;
import java.net.NetworkInterface;


public class NewsListingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    TextView name, email;
    Button logout;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    LocationManager locationManager;
    String latitude, longitude, ipAddress;

    private Spinner menu_spinner;
    private static final String[] paths = {"-Select Topic-", "Sports", "Fashion", "Politics", "International"};
    private ListView listview;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_listing);
        name = findViewById(R.id.user_name);
        //email = findViewById(R.id.email);
        //logout = findViewById(R.id.logout);


        /**Google Sign in**/
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String Name = account.getDisplayName();
            String Email = account.getEmail();
            name.setText(Name);
            //email.setText(Email);

            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("heraldNewsData", MODE_PRIVATE);
            SharedPreferences.Editor editDataHerald = sharedPreferences.edit();
            editDataHerald.putString("email", Email);
            editDataHerald.putString("name", Name);
            editDataHerald.commit();
        }
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SignOut();
//            }
//        });

        /**Dropdown**/
        menu_spinner = (Spinner) findViewById(R.id.menu_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewsListingActivity.this, android.R.layout.simple_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menu_spinner.setAdapter(adapter);
        menu_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String news_category;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                news_category = String.valueOf(adapterView.getItemAtPosition(i));
                Toast.makeText(getBaseContext(), news_category, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });


        //Menu Trending Default Loading
        TextView menu_trending = (TextView) findViewById(R.id.menu_trending);
        menu_trending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadTrendingNews();
                Toast.makeText(getBaseContext(), "Trending News Loaded !", Toast.LENGTH_SHORT).show();
            }
        });


        //location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        TextView menu_near_me = (TextView) findViewById(R.id.menu_near_me);
        menu_near_me.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    enableLocationOnDevice();
                } else {
                    getLocation();
                }
            }
        });


        loadTrendingNews();


        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }
    }


    private void SignOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                SharedPreferences sharedPreferences = getSharedPreferences("heraldNewsData", MODE_PRIVATE);
                SharedPreferences.Editor editDataHerald = sharedPreferences.edit();

                editDataHerald.putString("email", "");
                editDataHerald.putString("name", "");

                editDataHerald.commit();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    private void loadTrendingNews() {
        String BASE_URL = getMetadata(getApplicationContext(), "RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "news?safeSearch=Off&textFormat=Raw";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ListView newslistview;
                ArrayList<NewsListSubjectData> newsList = new ArrayList<NewsListSubjectData>();
                newslistview = (ListView) findViewById(R.id.newslist);
                newsList.clear();
                String status = null;
                JSONObject json2 = null;
                JSONArray newsValues = null;

                try {
                    json2 = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray school = null;
                try {
                    newsValues = json2.getJSONArray("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < newsValues.length(); i++) {
                    JSONObject object = null;
                    try {
                        object = newsValues.getJSONObject(i);
                        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d'T'H:m:s.SSS", Locale.ENGLISH);
                        Date date = sdf.parse(object.getString("datePublished"));
                        String image = object.getJSONObject("image").getJSONObject("thumbnail").getString("contentUrl");
                        int imageWidth = object.getJSONObject("image").getJSONObject("thumbnail").getInt("width");
                        int imageHeight = object.getJSONObject("image").getJSONObject("thumbnail").getInt("height");
                        String dateVal = date.toString();

                        newsList.add(new NewsListSubjectData(object.getString("name") + "<br/><font weight='1dp'><i>" + dateVal + "</i></font>", image.toString(), imageWidth, imageHeight, object.getString("url")));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                NewsListAdapter newsListAdapter = new NewsListAdapter(getApplicationContext(), newsList);
                newslistview.setAdapter(newsListAdapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("That didn't work!", "That didn't work!");
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("X-Rapidapi-Key", getMetadata(getApplicationContext(), "RAPID_API_KEY"));
                params.put("X-Bingapis-Sdk", "true");
                params.put("X-Rapidapi-Host", getMetadata(getApplicationContext(), "RAPID_API_HOST"));

                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(stringRequest);
    }


    public static String getMetadata(Context context, String key) {
        try {
            Bundle metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            return metaData.get(key).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                break;

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            SignOut();
        }
        return false;
    }


    private void loadNearMeNews() {

        String BASE_URL = getMetadata(getApplicationContext(), "RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "news?safeSearch=Off&textFormat=Raw";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Log.d("worked", response);
                ListView newslistview;

                ArrayList<NewsListSubjectData> newsList = new ArrayList<NewsListSubjectData>();

                newslistview = (ListView) findViewById(R.id.newslist);
                String status = null;
                JSONObject json2 = null;
                JSONArray newsValues = null;


                try {
                    json2 = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray school = null;
                try {
                    newsValues = json2.getJSONArray("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < newsValues.length(); i++) {
                    JSONObject object = null;
                    try {
                        object = newsValues.getJSONObject(i);
                        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d'T'H:m:s.SSS", Locale.ENGLISH);
                        Date date = sdf.parse(object.getString("datePublished"));
                        String image = object.getJSONObject("image").getJSONObject("thumbnail").getString("contentUrl");
                        int imageWidth = object.getJSONObject("image").getJSONObject("thumbnail").getInt("width");
                        int imageHeight = object.getJSONObject("image").getJSONObject("thumbnail").getInt("height");
                        String dateVal = date.toString();

                        newsList.add(new NewsListSubjectData(object.getString("name") + "<br/><font weight='1dp'><i>" + dateVal + "</i></font>", image.toString(), imageWidth, imageHeight, object.getString("url")));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                NewsListAdapter newsListAdapter = new NewsListAdapter(getApplicationContext(), newsList);
                newslistview.setAdapter(newsListAdapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("That didn't work!", "That didn't work!");
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("X-Rapidapi-Key", "8a13e7d2d4msh914d61b12bb5b6bp19f258jsn1e5059e69ede");
                params.put("X-Bingapis-Sdk", "true");
                params.put("X-Rapidapi-Host", "bing-news-search1.p.rapidapi.com");
                params.put("X-Rapidapi-Host", "bing-news-search1.p.rapidapi.com");

                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(stringRequest);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        getLastKnownLocation();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "" + location.getLatitude(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }


    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private Location getLastKnownLocation() {
        Location l = null;
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        //Toast.makeText(this, "" + bestLocation.getLongitude()+" "+bestLocation.getLatitude(), Toast.LENGTH_LONG).show();
        getLocationBasedNews(bestLocation.getLatitude(), bestLocation.getLongitude());
        return bestLocation;
    }

    private void getLocationBasedNews(double lat, double longi) {
        String BASE_URL = getMetadata(getApplicationContext(), "RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "news?safeSearch=Off&textFormat=Raw";
        getIPAddress();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ListView newslistview;
                ArrayList<NewsListSubjectData> newsList = new ArrayList<NewsListSubjectData>();
                newslistview = (ListView) findViewById(R.id.newslist);
                String status = null;
                JSONObject json2 = null;
                JSONArray newsValues = null;
                //newsList.clear();
                try {
                    json2 = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray school = null;
                try {
                    newsValues = json2.getJSONArray("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < newsValues.length(); i++) {
                    JSONObject object = null;
                    try {
                        object = newsValues.getJSONObject(i);
                        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d'T'H:m:s.SSS", Locale.ENGLISH);
                        Date date = sdf.parse(object.getString("datePublished"));
                        String image = object.getJSONObject("image").getJSONObject("thumbnail").getString("contentUrl");
                        int imageWidth = object.getJSONObject("image").getJSONObject("thumbnail").getInt("width");
                        int imageHeight = object.getJSONObject("image").getJSONObject("thumbnail").getInt("height");
                        String dateVal = date.toString();

                        newsList.add(new NewsListSubjectData(object.getString("name") + "<br/><font weight='1dp'><i>" + dateVal + "</i></font>", image.toString(), imageWidth, imageHeight, object.getString("url")));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                NewsListAdapter newsListAdapter = new NewsListAdapter(getApplicationContext(), newsList);
               // newslistview.setAdapter(null);
                newslistview.setAdapter(newsListAdapter);
             }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("That didn't work!", "That didn't work!");
            }
        }) {

            String distanceMeter = String.valueOf(500);

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("X-Rapidapi-Key", getMetadata(getApplicationContext(), "RAPID_API_KEY"));
                params.put("X-Bingapis-Sdk", "true");
                params.put("X-Rapidapi-Host", getMetadata(getApplicationContext(), "RAPID_API_HOST"));
                params.put("X-Search-Location", "lat:"+String.valueOf(lat)+";long:"+String.valueOf(longi)+";re:"+distanceMeter);
                params.put("X-Msedge-Clientip", ipAddress);

                //X-Msedge-Clientip
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(stringRequest);
    }


    private void enableLocationOnDevice() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public   void getIPAddress() {
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ipAddress = ip;
        //Toast.makeText(getBaseContext(), ipAddress, Toast.LENGTH_SHORT).show();

    }

}