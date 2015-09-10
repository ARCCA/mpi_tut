package uk.co.hpcwales.vibratingstring;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Ian.Grimstead
 * Date: 06/11/12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class FileTokeniser {
    private DataInputStream m_dis;
    private boolean m_bBinaryMode;
    private StringTokenizer m_st;
    private String m_sNextLine;

    private String getNextLineSub() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte newByte;

        try
        {
            do
            {
                do
                {
                    // Can throw EOF
                    newByte = m_dis.readByte();

                    if ((newByte != '\n') && (newByte != '\r'))
                    {
                        baos.write(newByte);
                    }
                }
                while (newByte != '\n');
            }
            while (baos.size() == 0);
        }
        catch (EOFException e)
        {
            if (baos.size() == 0)
            {
                return null;
            }
        }

        return baos.toString();
    }

    private String getNextLine() throws IOException
    {
        String line = null;
        StringTokenizer st = null;
        String firstToken = null;

        do
        {
            line = getNextLineSub();

            // EOF?
            if (line == null)
            {
                return null;
            }

            st = new StringTokenizer(line);
            // Do we have a blank line?
            if (st.hasMoreTokens())
            {
                firstToken = st.nextToken();

                // May not have a space after the "#", so not detected as
                // the first token...
                if (firstToken.startsWith("#"))
                {
                    firstToken = "#";
                }
            }
            // If so, replace it with a single comment
            // - this forces us to read the next line (i.e. skip white space)
            else
            {
                firstToken = "#";
            }
        }
        while (firstToken.equalsIgnoreCase("#") && (line != null));

        return line;
    }

//    private void getNextTokenPair(DataInputStream dis) throws IOException
//    {
//        String nextLine = DataSubset.getNextLine(dis);
//        StringTokenizer st = new StringTokenizer(nextLine);
//        m_tokenName = st.nextToken();
//
//        if (st.hasMoreTokens())
//        {
//            m_tokenValue = Float.parseFloat(st.nextToken());
//        }
//        else
//        {
//            m_tokenValue = Float.NaN;
//        }
//    }

    private String getNextToken() throws IOException
    {
        if (m_st.hasMoreTokens())
        {
            return m_st.nextToken();
        }

        m_sNextLine = getNextLine();

        // EOF?
        if (m_sNextLine == null)
        {
            throw new EOFException("End of file - can't read item");
        }

        m_st = new StringTokenizer(m_sNextLine);

        return m_st.nextToken();
    }

//    public DataLoggerInput(DataInputStream dis) throws IOException
    public void DataLoggerInput(DataInputStream dis) throws IOException
    {
        m_dis = dis;

        // First line of file:
        // bin
        // ...or...
        // ascii
        String sMode = getNextLineSub();

        if (sMode.equalsIgnoreCase("bin"))
        {
            m_bBinaryMode = true;
        }
        else if (sMode.equalsIgnoreCase("ascii"))
        {
            m_bBinaryMode = false;
        }
        else
        {
            throw new Error("!");
        }


        if (!m_bBinaryMode)
        {
            m_sNextLine = getNextLine();
            m_st = new StringTokenizer(m_sNextLine);
        }
    }

    public int getInt() throws IOException
    {
        if (m_bBinaryMode)
        {
            return m_dis.readInt();
        }
        else
        {
            return Integer.parseInt( getNextToken() );
        }
    }

    public boolean getBoolean() throws IOException
    {
        if (m_bBinaryMode)
        {
            // 1 byte for boolean from C++; 1 = true, 0 = false
            return m_dis.readBoolean();
        }
        else
        {
            return (Integer.parseInt(getNextToken()) == 1 ? true : false);
        }
    }

    public double getDouble() throws IOException
    {
        if (m_bBinaryMode)
        {
            return m_dis.readDouble();
        }
        else
        {
            return Double.parseDouble( getNextToken() );
        }
    }

    public float getFloat() throws IOException
    {
        if (m_bBinaryMode)
        {
            return m_dis.readFloat();
        }
        else
        {
            return Float.parseFloat( getNextToken() );
        }
    }

    public boolean isBinary()
    {
        return m_bBinaryMode;
    }

    public String getString() throws IOException
    {
        if (m_bBinaryMode)
        {
            return m_dis.readUTF();
        }
        else
        {
            String s = getNextToken();

            // Strip containing quotes, if present
            if (s.startsWith("\""))
            {
                return s.substring(1, s.length()-1);
            }
            else
            {
                return s;
            }
        }
    }
}
