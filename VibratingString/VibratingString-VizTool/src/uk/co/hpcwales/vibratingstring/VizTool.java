package uk.co.hpcwales.vibratingstring;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.GZIPInputStream;

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

        int rankRoot = (int) readDoubleAfterEquals("Rank", br);
        int numProcs = (int) readDoubleAfterEquals("Numprocs", br);
        int numPoints = (int) readDoubleAfterEquals("Numpoints", br);
        int startRoot = (int) readDoubleAfterEquals("start", br);
        int endRoot = (int) readDoubleAfterEquals("end", br);
        
        br.close();
        fis.close();

        // Let's create enough space for each processor's file...
        m_daDataPoints = new double[numPoints];
        
        // Slice off the ending "0.txt"
        String sBaseFileName = sFirstFileName.substring(0, sFirstFileName.length() - 5);
        File fDatFileArray[] = new File[numProcs];
        FileInputStream fisArray[] = new FileInputStream[numProcs];
        BufferedReader brArray[] = new BufferedReader[numProcs];
        int startArray[] = new int[numProcs];
        int endArray[] = new int[numProcs];

        for (int iProcLoop=0; iProcLoop < numProcs; iProcLoop++)
        {
            fDatFileArray[iProcLoop] = new File(sBaseFileName + Integer.toString(iProcLoop) + ".txt");
            fisArray[iProcLoop] = new FileInputStream(fDatFileArray[iProcLoop]);
            brArray[iProcLoop] = new BufferedReader(new InputStreamReader(fisArray[iProcLoop]));

            int localRank = (int) readDoubleAfterEquals("Rank", brArray[iProcLoop]);
            int localNumProcs = (int) readDoubleAfterEquals("Numprocs", brArray[iProcLoop]);
            int localNumPoints = (int) readDoubleAfterEquals("Numpoints", brArray[iProcLoop]);
            startArray[iProcLoop] = (int) readDoubleAfterEquals("start", brArray[iProcLoop]);
            endArray[iProcLoop] = (int) readDoubleAfterEquals("end", brArray[iProcLoop]);
        }

        try
        {
            int iOutputPass = 0;

            int iImageWidth = 512;
            int iImageHeight = 256;

            BufferedImage outputImage = new BufferedImage(iImageWidth, iImageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D outputGraphics2D = outputImage.createGraphics();
            while (true) {
                // Ready for the next line...
                outputGraphics2D.setColor(Color.WHITE);
                outputGraphics2D.fillRect(0, 0, iImageWidth, iImageHeight);

                int iPreviousY = iImageHeight / 2;
                int iPreviousX = 0;
                double dSampleXStep = (double)iImageWidth / (double)numPoints;
                double dSampleX = dSampleXStep;

                outputGraphics2D.setColor(Color.BLACK);
                for (int iProcLoop=0; iProcLoop < numProcs; iProcLoop++)
                {
                    for(int sampleLoop=startArray[iProcLoop]; sampleLoop<=endArray[iProcLoop]; sampleLoop++){
                        // Read in the next value...
                        double d = readDoubleAfterEquals("x[" + Integer.toString(sampleLoop)+"] ", brArray[iProcLoop]);
//                        System.out.println("d=" + d);
                        
                        int x = (int)dSampleX;
                        int y = (int) ((d * (double)iImageHeight / 2.0) + (iImageHeight / 2));

                        outputGraphics2D.drawLine(iPreviousX, iPreviousY, x, y);
                        
                        iPreviousX = x;
                        iPreviousY = y;
                        dSampleX += dSampleXStep;
                    }
                }

                outputGraphics2D.drawLine(iPreviousX, iPreviousY, iImageWidth, iImageHeight / 2);
		String scratchName = Integer.toString(iOutputPass);
		while (scratchName.length() < 3) scratchName = "0" + scratchName; 
                File outputFile = new File("image-" + scratchName + ".png");
                ImageIO.write(outputImage, "PNG", outputFile);

                iOutputPass++;
            }

        }
        catch (Exception e)
        {

        }

        // All done.
    }

    static public void main(String args[]) throws Exception {
//        VizTool vt = new VizTool("VibratingString_output-1node-0.txt");
        VizTool vt = new VizTool("output-0.txt");
    }
}
