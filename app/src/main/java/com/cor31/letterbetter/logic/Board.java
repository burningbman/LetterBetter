package com.cor31.letterbetter.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Board implements Serializable
{
    int numTilesHorizontal;
    int numTilesVertical;
    private int score;
    private Random rand = new Random();

    private static String[][] diceBigBoggle = {{"A", "A", "A", "F", "R", "S"}, {"A", "A", "E",
            "E", "E", "E"}, {"A", "A", "F", "I", "R", "S"}, {"A", "D", "E", "N", "N", "N"}, {"A",
            "E", "E", "E", "E", "M"}, {"A", "E", "E", "G", "M", "U"}, {"A", "E", "G", "M", "N",
            "N"}, {"A", "F", "I", "R", "S", "Y"}, {"B", "J", "K", "Qu", "X", "Z"}, {"C", "C",
            "E", "N", "S", "T"},

            {"C", "E", "I", "I", "L", "T"}, {"C", "E", "I", "L", "P", "T"}, {"C", "E", "I", "P",
            "S", "T"}, {"D", "D", "H", "N", "O", "T"}, {"D", "H", "H", "L", "O", "R"},

            {"D", "H", "L", "N", "O", "R"}, {"D", "H", "L", "N", "O", "R"}, {"E", "I", "I", "I",
            "T", "T"}, {"E", "M", "O", "T", "T", "T"}, {"E", "N", "S", "S", "S", "U"},

            {"F", "I", "P", "R", "S", "Y"}, {"G", "O", "R", "R", "V", "W"}, {"I", "P", "R", "R",
            "R", "Y"}, {"N", "O", "O", "T", "U", "W"}, {"O", "O", "O", "T", "T", "U"}};

    private static String[][] diceBoggle = {{"A", "A", "E", "E", "G", "N"}, {"E", "L", "R", "T",
            "T", "Y"}, {"A", "O", "O", "T", "T", "W"}, {"A", "B", "B", "J", "O", "O"}, {"E", "H",
            "R", "T", "V", "W"}, {"C", "I", "M", "O", "T", "U"}, {"D", "I", "S", "T", "T", "Y"},
            {"E", "I", "O", "S", "S", "T"}, {"D", "E", "L", "R", "V", "Y"}, {"A", "C", "H", "O",
            "P", "S"}, {"H", "I", "M", "N", "Qu", "U"}, {"E", "E", "I", "N", "S", "U"}, {"E",
            "E", "G", "H", "N", "W"}, {"A", "F", "F", "K", "P", "S"}, {"H", "L", "N", "N", "R",
            "Z"}, {"D", "E", "I", "L", "R", "X"}};
    /*
     * private static char[] LETTER_ARRAY = new char[] { 'A', 'B', 'C', 'D',
     * 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
     * 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
     */
    private String[][] boardArray;
    private ArrayList<String> selectedLetters;

    private ArrayList<String> arrayListSubmittedWords;

    public Board(int width)
    {
        numTilesVertical = width;
        numTilesHorizontal = width;
        boardArray = new String[width][width];
        initLists();
        populateBoardArray();
    }

    public Board (String initBoard)
    {
        String[] boardArray = new String[initBoard.length()];

        for (int i = 0; i < initBoard.length(); i++)
        {
            boardArray[i] = String.valueOf(initBoard.charAt(i));
        }

        new Board(boardArray);
    }

    public Board(String[] initBoard)
    {
        numTilesVertical = (int) Math.sqrt(initBoard.length);
        numTilesHorizontal = numTilesVertical;

        for (int i = 0; i < numTilesVertical; i++)
        {
            for (int j = 0; j < numTilesHorizontal; j++)
            {
                boardArray[i][j] = initBoard[numTilesHorizontal * i + j];
            }
        }

        initLists();
    }

    private void initLists()
    {
        selectedLetters = new ArrayList<>();
        arrayListSubmittedWords = new ArrayList<>();

        // find possible words
        /*
        PossibleWordFinderTask taskFindPossibleWords = new PossibleWordFinderTask(
				numTilesHorizontal, numTilesVertical, boardArray, possibleWords);
		taskFindPossibleWords.execute();*/
    }

    public String[][] getBoardArray()
    {
        return boardArray;
    }

    public int getNumTilesHorizontal()
    {
        return numTilesHorizontal;
    }

    public int getNumTilesVertical()
    {
        return numTilesVertical;
    }

    private void populateBoardArray()
    {
        boolean[] dieHasBeenRolled = new boolean[numTilesHorizontal * numTilesVertical];

        for (int i = 0; i < dieHasBeenRolled.length; i++)
        {
            dieHasBeenRolled[i] = false;
        }
        for (int intYIndex = 0; intYIndex < numTilesVertical; intYIndex++)
        {
            for (int intXIndex = 0; intXIndex < numTilesHorizontal; intXIndex++)
            {
                int intDieNum = rand.nextInt(numTilesHorizontal * numTilesVertical - 1);
                while (dieHasBeenRolled[intDieNum])
                {
                    intDieNum = rand.nextInt(numTilesHorizontal * numTilesVertical - 1);
                }
                boardArray[intXIndex][intYIndex] = rollDice(intDieNum, intXIndex, intYIndex);
            }
        }
    }

    // Returns true if a word is submitted
    public String letterClicked(int[] letterCoords)
    {
        String strClicked = boardArray[letterCoords[0]][letterCoords[1]];

        // Blank Linked List, start a new word
        if (selectedLetters.isEmpty())
        {
            selectedLetters.add(strClicked);
            return null;
        }

        // Make sure this letter can be selected
        else
        {
            String lastChar = selectedLetters.get(selectedLetters.size() - 1);

            // User clicked same letter twice, submit the letter
            if (lastChar.equals(strClicked))
            {
                return submitWord();
            }

            // Letter must be within exactly one spot of the last letter clicked
            selectedLetters.add(strClicked);
        }
        return null;
    }

    public void letterDragged(int[] letterCoords)
    {
        return;
    }

    private String rollDice(int dieIndex, int x, int y)
    {
        int intRand = rand.nextInt(5);
        String strLetter = "A";
        if (numTilesHorizontal == 5)
            strLetter = diceBigBoggle[dieIndex][intRand];
        else if (numTilesHorizontal == 4)
            strLetter = diceBoggle[dieIndex][intRand];
        return strLetter;
    }

    protected String submitWord()
    {
        Iterator<String> iterSelectedLetters = selectedLetters.iterator();

        String strSubmittedWord = "";

        while (iterSelectedLetters.hasNext())
        {
            String tempLetter = iterSelectedLetters.next();
            strSubmittedWord += tempLetter;
        }

        if (strSubmittedWord.length() >= 3 && Arrays.binarySearch(Dictionary.getInstance(null)
                .getDictionary(), strSubmittedWord) > 0 && !arrayListSubmittedWords.contains
                (strSubmittedWord))
        {
            arrayListSubmittedWords.add(strSubmittedWord);
            int len = strSubmittedWord.length();
            switch (len)
            {
                case 3:
                    score += 1;
                    break;
                case 4:
                    score += 1;
                    break;
                case 5:
                    score += 2;
                    break;
                case 6:
                    score += 3;
                    break;
                case 7:
                    score += 5;
                    break;
                default:
                    score += 11;
                    break;
            }

            selectedLetters.clear();
            return strSubmittedWord;
        }

        selectedLetters.clear();
        return null;
    }

    public String getScore()
    {
        return String.valueOf(score);
    }

    public String getLetterAt(int x, int y)
    {
        return boardArray[x][y];
    }

    public String getWord()
    {
        String ret = "";
        Iterator<String> iterator = selectedLetters.iterator();
        while (iterator.hasNext())
        {
            ret += iterator.next();
        }
        return ret;
    }

    public void shuffle()
    {
        String[][] tempBoardArray = new String[numTilesHorizontal][numTilesVertical];
        boolean[][] boolArrTileUsed = new boolean[][] {{false, false, false, false}, {false,
                false, false, false}, {false, false, false, false}, {false, false, false, false}};

        for (int i = 0; i < numTilesHorizontal; i++)
        {
            for (int j = 0; j < numTilesVertical; j++)
            {
                boolean newTileFound = false;

                while (!newTileFound)
                {
                    int iTemp = rand.nextInt(numTilesHorizontal);
                    int jTemp = rand.nextInt(numTilesVertical);
                    if (!boolArrTileUsed[iTemp][jTemp])
                    {
                        newTileFound = true;
                        tempBoardArray[i][j] = boardArray[iTemp][jTemp];
                    }
                    boolArrTileUsed[iTemp][jTemp] = true;
                }
            }
        }

        boardArray = tempBoardArray;
    }

    public void swap(int[] dest, int[] src)
    {
        String strTemp = boardArray[dest[0]][dest[1]];
        boardArray[dest[0]][dest[1]] = boardArray[src[0]][src[1]];
        boardArray[src[0]][src[1]] = strTemp;
    }

    public String toString()
    {
        String strBoard = "";

        for (int i = 0; i < numTilesVertical; i++)
        {
            for (int j = 0; j < numTilesHorizontal; j++)
            {
                strBoard += boardArray[i][j];
            }
        }

        return strBoard;
    }
}
