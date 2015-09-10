package uk.co.hpcwales.laplaceequation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ian.Grimstead
 * Date: 06/11/12
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class VizTool {

    int m_numProcessors = 1;
    int m_numDataPoints = 0;
    double m_daDataPoints[];
    
    static double readDoubleAfterEquals(String leftHandSide, BufferedReader br) throws Exception {
        String s = br.readLine();
        if (s == null)
        {
            throw new Exception("Read unexpected empty line - may be end of file");
        }

        String sa[] = s.split("=");
        if (sa.length != 2)
        {
            throw new Exception("Expected two strings back from split, input string=\"" + s + "\"");
        }
        
        if (sa[0].equalsIgnoreCase(leftHandSide))
        {
            return Double.valueOf(sa[1]);
        }
        else
        {
            throw new Exception("Left-hand side of \"=\" did not match expected \"" + leftHandSide + "\"; input string=\"" + s + "\"");
        }
    }
    
    public VizTool(String sFirstFileName) throws Exception {
        File fDatFile = new File(sFirstFileName);
        FileInputStream fis = new FileInputStream(fDatFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        int numPointsInX = (int) readDoubleAfterEquals("num points in x ", br);
        int numPointsInY = (int) readDoubleAfterEquals("num points in y ", br);

        int numProcsInX = (int) readDoubleAfterEquals("num processors in x ", br);
        int numProcsInY = (int) readDoubleAfterEquals("num processors in y ", br);

        int nLocalSizeX = (numPointsInX-1)/numProcsInX + 1;
        int nLocalSizeY = (numPointsInY-1)/numProcsInY + 1;

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        SampleModel sm = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, numPointsInX, numPointsInY, 3, 3 * numPointsInX, new int[]{2, 1, 0});

        final byte[] iba = new byte[numPointsInX * numPointsInY * 3];
        DataBuffer idb = new DataBufferByte(iba, iba.length);
        WritableRaster wr = Raster.createWritableRaster(sm, idb, null);
        BufferedImage bi = new BufferedImage(cm, wr, false, null);

        for (int m=0; m<numProcsInY; m++)
        {
            System.out.println("Processor y = " + m);
            int maxY = (int) readDoubleAfterEquals("max y ", br);

            for (int y=1; y <= maxY; y++)
            {
                for (int n=0; n<numProcsInX; n++)
                {
                    int nProcX = (int) readDoubleAfterEquals("processor x ", br);
                    int nProcY = (int) readDoubleAfterEquals("processor y ", br);
                    int maxX = (int) readDoubleAfterEquals("max x ", br);

                    for(int x=1; x<=maxX; x++)
                    {
                        // Write pixels...
                        int R, B, G, pow8 = 256;

                        double value = readDoubleAfterEquals(
                                "phi[" + Integer.toString(y) + "][" + Integer.toString(x) + "]", br);

                        if(value <= 0.5){
                            R = (int)((1.0-2.0*value)*255.0);
                            G = (int)(2.0*value*255.0);
                            B = 0;
                        }
                        else{
                            R = 0;
                            G = (int)((2.0-2.0*value)*255.0);
                            B = (int)((2.0*value-1.0)*255.0);
                        }

                        byte r = (byte) R;
                        byte g = (byte) G;
                        byte b = (byte) B;

                        // Convert from relative coords to absolute coords...
                        int iGlobalX = (n * nLocalSizeX) + (x-1);
                        int iGlobalY = (m * nLocalSizeY) + (y-1);

                        int iEntry = ((iGlobalY * numPointsInX) + iGlobalX) * 3;

                        iba[iEntry + 0] = r;
                        iba[iEntry + 1] = g;
                        iba[iEntry + 2] = b;
                    }
                }
            }
        }

        // All done.
        br.close();
        fis.close();

        String sOutputFileName = sFirstFileName.substring(0, sFirstFileName.length()-3) + "png";

        File outputFile = new File(sOutputFileName);
        ImageIO.write(bi, "PNG", outputFile);
    }

    static public void main(String args[]) throws Exception {
//        VizTool vt = new VizTool("VibratingString_output-1node-0.txt");
        VizTool vt = new VizTool("output.txt");
    }
}
