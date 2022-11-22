package com.w9565277.thedailyherald;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class NewsListAdapter implements ListAdapter {
    ArrayList<NewsListSubjectData> arrayList;
    Context context;

    public NewsListAdapter(Context context, ArrayList<NewsListSubjectData> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public NewsListSubjectData getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NewsListSubjectData subjectData = arrayList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.activity_news_list_view, null);


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NewsListSubjectData item = getItem(position);
                    String url = item.NewsUrl;

                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }


            });


            TextView tittle = convertView.findViewById(R.id.item_head);
            ImageView imag = convertView.findViewById(R.id.item_image);
            // imag.getLayoutParams().width= 500;//subjectData.ImageWidth;
            // imag.getLayoutParams().height= 375;//subjectData.ImageHeight;
            //imag.getLayoutParams().height= subjectData.ImageHeight;
            // imag.getLayoutParams().width= subjectData.ImageWidth;
            tittle.setText(HtmlCompat.fromHtml(subjectData.SubjectName, HtmlCompat.FROM_HTML_MODE_LEGACY));
            //tittle.setText(subjectData.SubjectName);
            Picasso.with(context)
                    .load(subjectData.Image)
                    .into(imag);

        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public void updateNewsList(ArrayList<NewsListSubjectData> newsList) {
        // arrayList.clear();
        // arrayList.addAll(newsList);
    }


}
