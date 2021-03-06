package com.commonsware.android.frw.filesdemo;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.File;

/**
 * I wanted to show the demo having this class separate rather than being an internal one at EditorFragment
 */
@EBean
public class SaveTask
{
    @Bean
    BusHandler busHandler;

    private Exception e = null;
    private String text;
    private File target;


    @Background
    void execute( String text, File target )
    {
        this.text = text;
        this.target = target;

        try
        {
            FileHelper.save(text, target);
        }
        catch (Exception e)
        {
            this.e = e;
        }
        finally {
            onPostExecute();
        }
    }

    @UiThread
    protected void onPostExecute()
    {
        if (e != null)
        {
            busHandler.requestException(e);
        }
    }
}
