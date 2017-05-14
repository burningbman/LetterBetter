package com.cor31.letterbetter.logic;

import android.content.Context;

import com.cor31.letterbetter.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Dictionary
{
    private static Dictionary instance;
    static String[] dictionary = null;
    private static Context context;

    protected Dictionary(Context context)
    {
    }

    public static Dictionary getInstance(Context con)
    {
        if (instance == null)
        {
            context = con;
            instance = new Dictionary(context);
            instance.loadDict();
        }

        return instance;
    }

    public String[] getDictionary()
    {
        if (dictionary == null)
            loadDict();
        return dictionary;
    }

    private void loadDict()
    {
        String[] strArray = new String[0];
        String line = "";

        try
        {
            InputStream is = context.getResources().openRawResource(R.raw.us_dict_new);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            while ((line = br.readLine()) != null)
            {
                i++;
            }
            is.close();
            strArray = new String[i];
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource
                    (R.raw.us_dict_new)));
            i = 0;
            while ((line = br.readLine()) != null)
            {
                strArray[i] = line;
                i++;
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            Arrays.sort(strArray);
        }
        dictionary = strArray;
    }
}
