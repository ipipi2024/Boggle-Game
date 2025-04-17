/**
  <p>Location class has the row and column index on the board</p>

  <p>The row and column fields are public.</p>
 */
public class Location
{
    public int row, // row index on the board
               col; // column index on the board

    /**
       constructor with known row and column

       @param aRow  intital row
       @param aCol  intital column
     */
    public Location(int aRow, int aCol)
    {
	row = aRow;
	col = aCol;
    }

    /**
       constructor with unknown row and column, initialize both to zero
     */
    public Location()
    {
	row = 0;
	col = 0;
    }
}
