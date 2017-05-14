package com.cor31.letterbetter;

import com.cor31.letterbetter.logic.Board;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

public class MatchDataAdapter extends TypeAdapter<MatchData>
{
    @Override
    public MatchData read(JsonReader reader)
            throws IOException
    {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        MatchData md = new MatchData(new Board(reader.nextString()));

        JsonToken token = reader.peek();
        while (token.equals(JsonToken.BEGIN_OBJECT))
        {
            reader.beginObject();
            String name = reader.nextString();
            int score = reader.nextInt();
            md.addPlayer(name, score);
            reader.endObject();
        }

        md.setBet(reader.nextInt()); //writer.name("bet").value(md.getBet());
        md.setComplete(reader.nextBoolean()); //writer.name("complete").value(md.isComplete());
        md.setLettersCollected(reader.nextBoolean()); //writer.name("lettersCollected").value(md.isLettersCollected());

        return md;
    }

    @Override
    public void write(JsonWriter writer, MatchData md)
            throws IOException
    {
        if (md == null) {
            writer.nullValue();
            return;
        }

        writer.value(md.getBoard().toString());

        for (String key : md.getScores().keySet())
        {
            writer.beginObject();
            writer.name("name").value(key);
            writer.name("score").value(md.getScores().get(key));
            writer.endObject();
        }

        writer.value(md.getBet());
        writer.value(md.isComplete());
        writer.value(md.isLettersCollected());
    }
}
