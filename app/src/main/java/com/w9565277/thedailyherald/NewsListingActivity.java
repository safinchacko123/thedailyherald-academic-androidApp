package com.w9565277.thedailyherald;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewsListingActivity extends AppCompatActivity {

    TextView name, email;
    Button logout;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    private Spinner menu_spinner;
    private static final String[] paths = {"Category","Sports", "Fashion", "Politics","International"};
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_listing);
        name = findViewById(R.id.user_name);
        email = findViewById(R.id.email);
        logout = findViewById(R.id.logout);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String Name = account.getDisplayName();
            String Email = account.getEmail();
            name.setText(Name);
            email.setText(Email);
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });


//        ListView simpleList;
//        String countryList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand"};
//        simpleList = (ListView)findViewById(R.id.simpleListView);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_news_list_view, R.id.textView2, countryList);
//        simpleList.setAdapter(arrayAdapter);

        ;
        menu_spinner = (Spinner)findViewById(R.id.menu_spinner);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(NewsListingActivity.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menu_spinner.setAdapter(adapter);
        menu_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    String news_category;

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        news_category = String.valueOf(adapterView.getItemAtPosition(i));
                         Toast.makeText(getBaseContext(),news_category, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }

                }
        );


        TextView menu_trending = (TextView) findViewById(R.id.menu_trending);
        menu_trending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadData();
                Toast.makeText(getBaseContext(), "Your answer is correct!", Toast.LENGTH_SHORT).show();
            }
        });

        loadData();
    }

    private void SignOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    private void loadData() {

       String BASE_URL = getMetadata(getApplicationContext(),"RAPID_API_BASE_URL");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "news?safeSearch=Off&textFormat=Raw";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("worked", response);
                        ListView simpleList;
                        //String countryList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand"};
                        ArrayList<String> countryList = new ArrayList<String>();

                        simpleList = (ListView)findViewById(R.id.simpleListView);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_news_list_view, R.id.textView2, countryList);
                        String status = null;
                        JSONObject json2 = null;
                        JSONArray newsValues = null;
                        try {
                              json2 = new JSONObject(response);
                            //status = json2.getString("status");
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
                                    //countryList[] = {};
                                    object = newsValues.getJSONObject(i);
                                    SimpleDateFormat sdf = new SimpleDateFormat("y-M-d'T'H:m:s.SSS", Locale.ENGLISH);
                                    Date date = sdf.parse(object.getString("datePublished"));

                                    countryList.add(object.getString("name")+"\n"+date.toString());


                                   // Toast.makeText(getBaseContext(), date.toString(), Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }





                        arrayAdapter.notifyDataSetChanged();
                        simpleList.setAdapter(arrayAdapter);
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
//            params.put("User", UserName);
//            params.put("Pass", PassWord);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void loadData(View view) {
//        TextView menu_trending = (TextView) findViewById(R.id.menu_trending);
//        menu_trending.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                testApi();
//                Toast.makeText(getBaseContext(), "Your answer is correct!", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public static String getMetadata(Context context, String key) {
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData;
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
}