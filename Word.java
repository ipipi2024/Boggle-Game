import java.util.*;

/**
  *<p><b>You may add additional methods, but may not change existing methods</b></p>
  *
  * The Word class has two components:
  *
  *<ol>
  *  <li>the word (String)</li>
  *  <li>path: the location on the board for each consecutive letter of the word (ArrayList of Location)</li>
  *</ol>
  *
  *<p><i>Note that for words that contain "QU", the length of a word is longer
  *than its path by 1 since "QU" in a word is represented by "Q" on the board</i></p>
  *
  *<p>The constructors allocate an ArrayList of Locations for the path</p>
  *
  *
  *@author Philip Chan
  */

public class Word
{
    private String  word;  // word
    private ArrayList<Location> path;  // row and column of each consecutive letter


    /** 
     * default constructor with 8 locations as the initial capacity for the path.
     * Location objects on the path are not allocated since actual path length is not known.
     *
     * Max for a 4x4 board is 16, but rare
     */
    public Word()
    {
	word = "";
	path = new ArrayList<Location>(8);  
    }

    /** 
     * constructor with an initial maximum path length;
     * path is intialized, location objects are allocated with row 0 and column 0
     * @param initialMaxPathLength initial maximum path length
     */
    public Word(int initialMaxPathLength)
    {
	word = "";
	path = new ArrayList<Location>(initialMaxPathLength); 
	for (int index = 0; index < initialMaxPathLength; index++)
	    path.add(new Location(0, 0));
    }


    /** 
     * constructor with a word; 
     * path is intialized, location objects are not allocated
     * @param aWord the corresponding word
     */
    public Word(String aWord)
    {
	word = aWord;
	path = new ArrayList<Location>(word.length()); 
    }

    /**
     * set the word field (the path field is not modified or checked)
     * @param aWord the corresponding word
     */

    public void setWord(String aWord)
    {
	if (word != null)
	    word = aWord;
	else
	    System.out.println("Warning: Word:setPath()--aWord is null");
    }

    /**
     * set the path field (the word field is not modified or checked)
     * @param aPath the corresponding path
     */
    public void setPath(ArrayList<Location> aPath)
    {
	if (aPath != null)
	    path = aPath;
	else
	    System.out.println("Warning: Word:setPath()--aPath is null");
    }

    /**
     * add the row and column of a letter to the end of the path
     * @param row row
     * @param col column
     */
    public void addLetterRowAndCol(int row, int col)
    {
	path.add(new Location(row, col));
    }


    /**
     * set the row and column of a letter at letterIndex on the path
     * @param letterIndex index of the letter on the path
     * @param row row
     * @param col column
     */
    public void setLetterRowAndCol(int letterIndex, int row, int col)
    {
	if ((letterIndex >= 0) && (letterIndex < path.size()))
	    {
		Location loc = path.get(letterIndex);
		if (loc != null) // overwrite the location
		    {
			loc.row = row;
			loc.col = col;
		    }
		else // create a new location object
		    path.set(letterIndex, new Location(row, col));
	    }
	else
	    System.err.println("Word.setLetterRowAndCol(): letterIndex out of bound: " + letterIndex);
    }

    /**
     * return the word
     * @return the corresponding word
     */
    public String getWord()
    {
	return word;
    }

    /**
     * get the path length
     * @return length of the path
     */
    public int getPathLength()
    {
	return path.size();
    }





    


    /* I made this, probably not good */
    public ArrayList<Location> getPath() {
        return path;
    }
    /* Checks to see if row col is already on path */
    public boolean isUnique(int row, int col) {
        for (Location l : path) {
            if (l.col == col && l.row == row) return false;
        }
        return true;
    }
    /* Deep copy */
    public Word(Word other) {
        this.word = other.word;
        this.path = new ArrayList<>();
        for (Location loc : other.path) {
            this.path.add(new Location(loc.row, loc.col));
        }
    }









    /**
     * get row at letterIndex on the path;
     * return -1 if invalid Index or no row at letterIndex
     *
     * @param letterIndex index of letter on the path
     * @return row index; -1 if invalid letterIndex or no row at letterIndex
     */
    public int getLetterRow(int letterIndex)
    {
	Location loc = getLetterLocation(letterIndex);
	if (loc != null)
	    return loc.row;
	else
	    return -1;
    }

    /**
     * get column at letterIndex on the path;
     * return -1 if invalid column or no column at letterIndex
     *
     * @param letterIndex index of letter on the path
     * @return row index; -1 if invalid letterIndex or no column at letterIndex
     */
    public int getLetterCol(int letterIndex)
    {
	Location loc = getLetterLocation(letterIndex);
	if (loc != null)
	    return loc.col;
	else
	    return -1;
    }


    /**
     * get location at letterIndex on the path;
     * return null if invalid column or no location at letterIndex
     *
     * @param letterIndex index of letter on the path
     * @return row index; -1 if invalid letterIndex or no column at letterIndex
     */
    public Location getLetterLocation(int letterIndex)
    {
	if ((letterIndex >= 0) && (letterIndex < path.size()))
	    {	
		Location loc = path.get(letterIndex);
		if (loc != null)
		    return loc;
		else
		    {
			System.err.println("Word.getLetterLocation(): no location at letterIndex" + letterIndex);
			return null;
		    }
	    }
	else
	    {
		System.err.println("Word.getLetterLocation(): out of bound at letterIndex" + letterIndex);
		return null;
	    }
    }
}

