package net.cridland.dave.AboutTime;

import java.util.Locale;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioButton;

public class AboutTimeConfig extends Activity {
	static final String TAG = "AboutTimeConfig";
	private static final String PREFS = "net.cridland.dave.AboutTime";
	private static final String PREF_PREFIX_KEY = "prefix_key";
	int m_AppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public AboutTimeConfig() {
        super();
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.prefs);

        // Bind the action for the save button.
        findViewById(R.id.save).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            m_AppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (m_AppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        String current = loadLangPref(AboutTimeConfig.this, m_AppWidgetId);
        RadioGroup lang = (RadioGroup)findViewById(R.id.pref_lang);
        Locale cy = new Locale("cy");
        ((RadioButton)findViewById(R.id.pref_cy)).setText(cy.getDisplayLanguage(cy) + " [" + cy.getDisplayName() + "]");
        Locale de = new Locale("de");
        ((RadioButton)findViewById(R.id.pref_de)).setText(de.getDisplayLanguage(de) + " [" + de.getDisplayName() + "]");
        Locale en = new Locale("en");
        ((RadioButton)findViewById(R.id.pref_en)).setText(en.getDisplayLanguage(en) + " [" + en.getDisplayName() + "]");
        Locale es = new Locale("es");
        ((RadioButton)findViewById(R.id.pref_es)).setText(es.getDisplayLanguage(es) + " [" + es.getDisplayName() + "]");
        Locale fr = new Locale("fr");
        ((RadioButton)findViewById(R.id.pref_fr)).setText(fr.getDisplayLanguage(fr) + " [" + fr.getDisplayName() + "]");
        Locale nl = new Locale("nl");
        ((RadioButton)findViewById(R.id.pref_nl)).setText(nl.getDisplayLanguage(nl) + " [" + nl.getDisplayName() + "]");
        if (current.equals("cy")) {
        	lang.check(R.id.pref_cy);
        } else if (current.equals("de")) {
        	lang.check(R.id.pref_de);
        } else if (current.equals("en")) {
        	lang.check(R.id.pref_en);
        } else if (current.equals("es")) {
        	lang.check(R.id.pref_es);
        } else if (current.equals("fr")) {
        	lang.check(R.id.pref_fr);
        } else if (current.equals("nl")) {
        	lang.check(R.id.pref_nl);
        } else {
        	lang.check(R.id.pref_def);
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AboutTimeConfig.this;

            RadioGroup lang = (RadioGroup)findViewById(R.id.pref_lang);
            String langstr = "def";
            switch (lang.getCheckedRadioButtonId()) {
            case R.id.pref_cy:
            	langstr = "cy";
            	break;
            case R.id.pref_de:
            	langstr = "de";
            	break;
            case R.id.pref_en:
            	langstr = "en";
            	break;
            case R.id.pref_es:
            	langstr = "es";
            	break;
            case R.id.pref_fr:
            	langstr = "fr";
            	break;
            case R.id.pref_nl:
            	langstr = "nl";
            	break;
            }
            saveLangPref(context, m_AppWidgetId, langstr);

            // Push widget update to surface with newly set prefix
            AboutTimeProvider.updateAll(context);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_AppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveLangPref(Context context, int appWidgetId, String lang) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, lang);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadLangPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
        String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, "def");
        return prefix;
    }
}
