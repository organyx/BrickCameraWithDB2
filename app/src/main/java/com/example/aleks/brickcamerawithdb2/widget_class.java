package com.example.aleks.brickcamerawithdb2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aleks on 03-Oct-15.
 * Widget Class
 */
public class widget_class extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int i = 0; i<appWidgetIds.length; i++)
        {
            int currentWidgetId = appWidgetIds[i];

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String timetext = df.format(new Date());
            timetext = currentWidgetId+")\n" + timetext;

            Intent myIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.btn, pendingIntent);
            views.setTextViewText(R.id.update, timetext);

            appWidgetManager.updateAppWidget(currentWidgetId, views);
        }
    }
}
