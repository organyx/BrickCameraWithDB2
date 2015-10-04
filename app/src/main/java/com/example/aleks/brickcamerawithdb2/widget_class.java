package com.example.aleks.brickcamerawithdb2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;

import android.widget.RemoteViews;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Aleks on 03-Oct-15.
 * Widget Class
 */
public class widget_class extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int i = 0; i<appWidgetIds.length; i++)
        {
            int currentWidgetId = appWidgetIds[i];

//            DateFormat df = new SimpleDateFormat("HH:mm:ss");
//            String timetext = df.format(new Date());
//            timetext = currentWidgetId+")\n" + timetext;

            Intent myIntent = new Intent(context, widget_class.class);
            myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, currentWidgetId);
            myIntent.setAction("update");

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.btnUpdateFeed, pendingIntent);
//            views.setTextViewText(R.id.update, timetext);


            Intent launcher_intent = new Intent(context, MainActivity.class);
            PendingIntent pending_launcher_intent = PendingIntent.getActivity(context, 0, launcher_intent, 0);
            views.setOnClickPendingIntent(R.id.btnLaunchApp, pending_launcher_intent);


            new RetrieveRssFeed().execute(context);

            appWidgetManager.updateAppWidget(currentWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        if (intent.getAction().equals("update"))
        {
            new RetrieveRssFeed().execute(context);
        }
    }

    class RetrieveRssFeed extends AsyncTask<Context, Void, List<StackOverflowFeed>>
    {
        private Exception exception;
        private Context context;
        private RemoteViews remoteViews;

        @Override
        protected List<StackOverflowFeed> doInBackground(Context... params) {
            try {
                context = params[0];

                FeedParser fp = new FeedParser("http://www.stackoverflow.com/feeds");
                List<StackOverflowFeed> feed = fp.parse();
                return feed;
            }
            catch (Exception e)
            {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(List<StackOverflowFeed> feed)
        {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            if (exception != null || feed == null || feed.size() < 5)
            {
                remoteViews.setTextViewText(R.id.tvFeed1, "");
                remoteViews.setTextViewText(R.id.tvFeed2, "");
                remoteViews.setTextViewText(R.id.tvFeed3, "");
                remoteViews.setTextViewText(R.id.tvFeed4, "");
                remoteViews.setTextViewText(R.id.tvFeed5, "");
            }
            else if(feed != null && feed.size() >= 5)
            {
                remoteViews.setTextViewText(R.id.tvFeed1, "1. " + feed.get(0).title);
                remoteViews.setTextViewText(R.id.tvFeed2, "2. " + feed.get(1).title);
                remoteViews.setTextViewText(R.id.tvFeed3, "3. " + feed.get(2).title);
                remoteViews.setTextViewText(R.id.tvFeed4, "4. " + feed.get(3).title);
                remoteViews.setTextViewText(R.id.tvFeed5, "5. " + feed.get(4).title);
            }

            appWidgetManager.updateAppWidget(new ComponentName(context, widget_class.class), remoteViews);
        }
    }

    public class FeedParser
    {
        private URL feedUrl;

        public FeedParser(String feedUrl)
        {
            try {
                this.feedUrl = new URL(feedUrl);
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }
        }

        public InputStream getInputStream()
        {
            try {
                return feedUrl.openConnection().getInputStream();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public List<StackOverflowFeed> parse()
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            List<StackOverflowFeed> feed = new ArrayList<StackOverflowFeed>();
            try
            {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(this.getInputStream());
                Element root = dom.getDocumentElement();
                NodeList items = root.getElementsByTagName("entry");

                if (items.getLength() >= 5)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        Node item = items.item(i);
                        if(item.getNodeType() == Node.ELEMENT_NODE)
                        {
                            StackOverflowFeed question = new StackOverflowFeed();

                            Element el = (Element) item;

                            NodeList idNodes = el.getElementsByTagName("id");
                            if(idNodes != null && idNodes.getLength() > 0)
                            {
                                question.id = idNodes.item(0).getFirstChild().getNodeValue();
                            }

                            NodeList titleNodes = el.getElementsByTagName("title");
                            if(titleNodes != null && titleNodes.getLength() > 0)
                            {
                                question.title = titleNodes.item(0).getFirstChild().getNodeValue();
                            }

                            NodeList linkNodes = el.getElementsByTagName("link");
                            if(linkNodes != null && linkNodes.getLength() > 0)
                            {
                                question.link = linkNodes.item(0).getAttributes().getNamedItem("href").getNodeValue();
                            }

                            feed.add(question);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            return feed;
        }


    }

    public class StackOverflowFeed
    {
        public String id;
        public String title;
        public String link;

        public StackOverflowFeed()
        {
            id = null;
            title = null;
            link = null;
        }

    }
}
