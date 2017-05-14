package com.cor31.letterbetter;

public class LoadDictionaryTask implements Runnable
{
    int startIndex;

    public LoadDictionaryTask(int i)
    {
        startIndex = i;
    }

    @Override
    public void run()
    {/*
        // TODO Auto-generated method stub
		try
		{
			InputStream is = ((AndroidGame) game).getResources()
					.openRawResource(R.raw.us_dict_new);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(is));
			//StringPatriciaTrie dict = new StringPatriciaTrie();
			int i = 0;
			while ((line = br.readLine()) != null)
			{
				// dict.addWord(line);
				if (line.length() <= 12)
					i++;
			}
			is.close();
			br = new BufferedReader(new InputStreamReader(
					((AndroidGame) game).getResources()
							.openRawResource(R.raw.us_dict_new)));
			longArray = new long[i];
			i = 0;
			while ((line = br.readLine()) != null)
			{
				if (line.length() <= 12)
				{
					Long l = stringToLong(line);
					longArray[i] = l;
					if (i % 1000 == 0)
					{
						Arrays.sort(longArray);
						((WordleGame) game).setLongs(longArray);
					}
					i++;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println(line);
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			Arrays.sort(longArray);
			((WordleGame) game).setLongs(longArray);
			// ((WordleGame) game).setDict(dict);
		}*/
    }
}
