/**
 * Contient le codages des couleurs en RGB
 */
public class RGBColor
{
       //rgb = (rouge<<16)+(vert<<8)+bleu;
      public final static int BLUE = 255;
      public final static int RED = 255<<16;
      public final static int GREEN = 255<<8;
      public final static int BLACK = 0;
      public final static int WHITE = (255<<16)+(255<<8)+255;
      public final static int YELLOW = (255<<16)+(255<<8);
      public final static int CYAN = (255<<8)+255;
      public final static int MAGENTA = (255<<16)+255;
      
      public final static int xBLUE = 0xFF0000FF;
      public final static int xRED = 0xFFFF0000;
      public final static int xGREEN = 0xFF00FF00;
      public final static int xBLACK = 0xFF000000;
      public final static int xWHITE = 0xFFFFFFFF;
      public final static int xYELLOW = 0xFFFFFF00;;
      public final static int xCYAN = 0xFF00FFFF;
      public final static int xMAGENTA = 0xFFFF00FF;
      
      public final static byte bWHITE = (byte)255;
      public final static byte bBLACK = 0;
      
      
}
