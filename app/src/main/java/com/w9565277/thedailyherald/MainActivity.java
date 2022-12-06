package com.w9565277.thedailyherald;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    DatabaseReference databaseReference;
    ReportedNews reportedNews;

    Button logout;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    LocationManager locationManager;
    String latitude, longitude, ipAddress;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Bitmap bitmapImage;
    String loggedInEmail = "";
    String activeMenu = "trending";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Home");

        //Default Load News List
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, new NewsListFragment());
        ft.commit();


        /**Google Sign in get data and save in shared pref**/
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String Name = account.getDisplayName();
            String Email = account.getEmail();
            loggedInEmail = Email;

            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("heraldNewsData", MODE_PRIVATE);
            SharedPreferences.Editor editDataHerald = sharedPreferences.edit();
            editDataHerald.putString("email", Email);
            editDataHerald.putString("name", Name);
            //set default location London
            editDataHerald.putString("def_lat", "51.509865");
            editDataHerald.putString("def_lon", "-0.118092");
            editDataHerald.commit();
        }

        //permission for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocationOnDevice();
        }

        //Navigation drawer
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

        //setup database for firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ReportedNews");
    }

    //clear saved shared pref when sign out
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

    //load trending api from RAPID API
    public void loadTrendingNews() {
        this.activeMenu = "trending";
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

            //set header for api calls
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("X-Rapidapi-Key", getMetadata(getApplicationContext(), "RAPID_API_KEY"));
                params.put("X-Bingapis-Sdk", "true");
                params.put("X-Rapidapi-Host", getMetadata(getApplicationContext(), "RAPID_API_HOST"));

                return params;
            }

            //Pass Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(stringRequest);
    }

    //get meta data that are saved.
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

    //menu item switch
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            SignOut();
        }
        if (id == R.id.nav_account) {
            getSupportActionBar().setTitle("My Account");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, new MyAccountFragment());
            ft.commit();
        }
        if (id == R.id.nav_citizen_journalist) {
            getSupportActionBar().setTitle("Report News");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, new ReportNewsFragment());
            ft.commit();
        }

        if (id == R.id.nav_news_list) {
            getSupportActionBar().setTitle("Home");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, new NewsListFragment());
            ft.commit();
        }
        drawerLayout.closeDrawers();
        return false;
    }

    //ask for location
    @SuppressLint("MissingPermission")
    public void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocationOnDevice();
        } else {
            getLastKnownLocation();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Toast.makeText(this, "Location : " + location.getLatitude(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        //  Toast.makeText(this, "Location changed ", Toast.LENGTH_SHORT).show();
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

    //get location data
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
        if (bestLocation == null) {
            //if best location has no data then use saved default location to get data from api
            try {
                SharedPreferences pref = getSharedPreferences("heraldNewsData", Context.MODE_PRIVATE);
                String lat = pref.getString("def_lat", "");
                String lon = pref.getString("def_lon", "");
                getLocationBasedNews(Double.parseDouble(lat), Double.parseDouble(lon));
                // getLocationBasedNews(bestLocation.getLatitude(), bestLocation.getLongitude());
                Log.d(getClass().getName(), "Default lat lon loaded ");

            } catch (Exception e) {
                Toast.makeText(this, "Sorry! Unable to get location details. Please try again.", Toast.LENGTH_LONG).show();
            }

        } else {
            try {
                //load data with accurate location lat and lon
                SharedPreferences sharedPreferences = getSharedPreferences("heraldNewsData", MODE_PRIVATE);
                SharedPreferences.Editor editDataHerald = sharedPreferences.edit();
                Double lat = bestLocation.getLatitude();
                Double lon = bestLocation.getLongitude();

                editDataHerald.putString("lat", Double.toString(lat));
                editDataHerald.putString("lon", Double.toString(lon));
                editDataHerald.commit();

                getLocationBasedNews(bestLocation.getLatitude(), bestLocation.getLongitude());
            } catch (Exception e) {
                Toast.makeText(this, "Sorry! Unable to get location. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
        return bestLocation;
    }

    //load api with lat and lon
    private void getLocationBasedNews(double lat, double longi) {
        this.activeMenu = "nearme";
        String BASE_URL = getMetadata(getApplicationContext(), "RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "news?safeSearch=Off&textFormat=Raw";
        getIPAddress();

        // Toast.makeText(this, "" + lat + " " + longi, Toast.LENGTH_LONG).show();

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
                params.put("X-Search-Location", "lat:" + String.valueOf(lat) + ";long:" + String.valueOf(longi) + ";re:" + distanceMeter);
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
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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

    //get ip address to get accurate location based news from api
    public void getIPAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ipAddress = ip;
    }

    //check camera permission
    public void getCameraImage(View view) {
        bitmapImage = null;
        Button btnCamera = findViewById(R.id.btnCamera);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmapImage = bitmap;
        }
    }

    //save news to firebase
    public void submitReportNews(View view) throws JSONException {
        TextView headline = (TextView) findViewById(R.id.headline);
        TextView desc = (TextView) findViewById(R.id.desc);

        String encodedImage = "";
        String headlineValue = headline.getText().toString();
        String descValue = desc.getText().toString();
        if ((descValue.trim()).length() != 0 && headlineValue.trim().length() != 0) {
            if (bitmapImage != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();

            reportedNews = new ReportedNews();
            reportedNews.setEmail(loggedInEmail);
            reportedNews.setHeadline(headlineValue);
            reportedNews.setDesc(descValue);
            reportedNews.setImage(encodedImage);
            reportedNews.setDateTime(ts);
            databaseReference.child(ts).setValue(reportedNews).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Success !! News Reported Successfully.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed !! Error Occurred. Please Try Again.", Toast.LENGTH_SHORT).show();

                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Toast.makeText(getApplicationContext(), "Success !! News Reported Successfully.", Toast.LENGTH_SHORT).show();
                    headline.setText("");
                    desc.setText("");
                    bitmapImage = null;
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Invalid Entry !! Please Enter the Details.", Toast.LENGTH_SHORT).show();

        }
    }


    //Search news
    public void searchNewsbyTopic() {
        this.activeMenu = "search";
        String BASE_URL = getMetadata(getApplicationContext(), "RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        EditText searchTextBox = (EditText) findViewById(R.id.searchtxt);
        String searchText = String.valueOf(searchTextBox.getText());

        if ((searchText.trim()).length() == 0) {
            Toast.makeText(this, "Please enter search text", Toast.LENGTH_SHORT).show();
        } else {
            String url = Uri.parse(BASE_URL + "news/search")
                    .buildUpon()
                    .appendQueryParameter("q", searchText)
                    .appendQueryParameter("freshness", "Month")
                    .appendQueryParameter("textFormat", "Raw")
                    .appendQueryParameter("safeSearch", "Off")
                    .build().toString();

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
                    if (newsValues.length() > 0) {
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

                    } else {
                        newsList.clear();
                        Toast.makeText(MainActivity.this, "Sorry !! No news available for the topic.", Toast.LENGTH_SHORT).show();
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
    }

    //header fragment set username
    public void setHeaderMainHeader(TextView textView) {
        SharedPreferences pref = getSharedPreferences("heraldNewsData", Context.MODE_PRIVATE);
        String name = pref.getString("name", "");
        textView.setText(name);
    }

}