package net.cridland.dave.AboutTime;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class AboutTimeProvider extends AppWidgetProvider {
	final static String my_alarm = "net.cridland.dave.AboutTime.DingDong";

	public void onUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
		doUpdate(context, awm, appWidgetIds);
	}
    public static void doUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
    	final Locale locale_default = Locale.getDefault();
		final int N = appWidgetIds.length;
		final long now = System.currentTimeMillis();
		boolean locale_set = false;
		for (int i=0; i!=N; ++i) {
			int id = appWidgetIds[i];
			try {
				String langstr = AboutTimeConfig.loadLangPref(context, id);
				if (langstr.equals("def")) {
					if (locale_set) {
						Locale.setDefault(locale_default);
						Configuration config = new Configuration();
						config.locale = locale_default;
						context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
						locale_set = false;
					}
				} else {
					Locale temp = new Locale(langstr);
					Locale.setDefault(temp);
					locale_set = true;
					Log.d("AboutTime", "Updating view " + id + " with lang " + temp.getDisplayLanguage());
					Configuration config = new Configuration();
					config.locale = temp;
					context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
				}
			} catch(Throwable e) {
				Log.e("AboutTime", "Whilst setting locale:", e);
			}
			final Calendar now_cal = Calendar.getInstance();
			final int mins = now_cal.get(Calendar.MINUTE);
			final int differential = (mins % 5) < 3 ? (mins % 5) : (mins % 5) - 5; 
			final int approx_mins = mins - differential;
			int hour = now_cal.get(Calendar.HOUR);
			String first_half = context.getResources().getStringArray(R.array.prefix)[(approx_mins%60)/5];
			String last_bit = context.getResources().getStringArray(R.array.suffix)[(approx_mins%60)/5];
			hour += context.getResources().getIntArray(R.array.hour_offset)[approx_mins/5];
			int hour24 = hour;
			String hour_text = context.getResources().getStringArray(R.array.hour)[hour % 12];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			views.setTextViewText(R.id.approx, context.getResources().getStringArray(R.array.delta)[differential + 2]);
			views.setTextViewText(R.id.time, first_half + hour_text + last_bit);
			if (now_cal.get(Calendar.AM_PM) != Calendar.AM) {
				hour24 += 12;
			}
			int tod = context.getResources().getIntArray(R.array.todmap)[hour24];
			views.setTextViewText(R.id.am_pm, context.getResources().getStringArray(R.array.tod)[tod]);
			Intent click_intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("vnd.about-time", "id-" + id, null), context, AboutTimeConfig.class);
			click_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
			PendingIntent click_pending = PendingIntent.getActivity(context, 0, click_intent, 0);
			views.setOnClickPendingIntent(R.id.widget_layout, click_pending);
			views.setOnClickPendingIntent(R.id.approx, click_pending);
			views.setOnClickPendingIntent(R.id.time, click_pending);
			views.setOnClickPendingIntent(R.id.am_pm, click_pending);
			awm.updateAppWidget(id, views);
		}
		Intent intent = new Intent(my_alarm);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, now + 60000, pi);
		if (locale_set) {
			Locale.setDefault(locale_default);
			Configuration config = new Configuration();
			config.locale = locale_default;
			context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
		}
	}

	public static void updateAll(Context context) {
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		int[] ids = awm.getAppWidgetIds(new ComponentName(context, AboutTimeProvider.class));
		doUpdate(context, awm, ids);
	}
	
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.d("AboutTime", "intent is " + action);
		if (action.equals(my_alarm) ||
			action.equals(Intent.ACTION_SCREEN_ON) ||
			action.equals(Intent.ACTION_TIMEZONE_CHANGED)||
			action.equals(Intent.ACTION_TIME_CHANGED)) {
			updateAll(context);
		}
		super.onReceive(context, intent);
	}
}
