import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Offline bitmap conversion of the emulator's Apache-2.0 RobotoStatic; no runtime font engine. */
public final class PrepareUiFont {
    public static void main(String[] args)throws Exception {
        Font font=Font.createFont(Font.TRUETYPE_FONT,Path.of(args[0]).toFile()).deriveFont(15f);
        Path out=Path.of(args[1]);Files.createDirectories(out);
        BufferedImage atlas=new BufferedImage(512,256,BufferedImage.TYPE_INT_ARGB);Graphics2D g=atlas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setFont(font);g.setColor(Color.WHITE);FontRenderContext frc=g.getFontRenderContext();FontMetrics fm=g.getFontMetrics();
        SortedSet<Integer> chars=new TreeSet<>();for(int c=32;c<=126;c++)chars.add(c);for(int c=0xc0;c<=0xff;c++)chars.add(c);
        for(char c:"\u0102\u0103\u0110\u0111\u0128\u0129\u0168\u0169\u01a0\u01a1\u01af\u01b0".toCharArray())chars.add((int)c);for(int c=0x1ea0;c<=0x1ef9;c++)chars.add(c);
        StringBuilder records=new StringBuilder();int x=1,y=1,row=0;
        for(int c:chars) {
            if(!font.canDisplay(c))throw new IllegalStateException("Missing source glyph U+"+Integer.toHexString(c));
            GlyphVector glyph=font.createGlyphVector(frc,new char[]{(char)c});Rectangle b=glyph.getPixelBounds(frc,0,0);
            int w=Math.max(0,b.width),h=Math.max(0,b.height),advance=Math.round(glyph.getGlyphMetrics(0).getAdvanceX());
            if(x+w+1>512) {x=1;y+=row+2;row=0;}
            if(y+h+1>256)throw new IllegalStateException("Font page overflow");
            g.drawGlyphVector(glyph,x-b.x,y-b.y);
            records.append(String.format(Locale.ROOT,"char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d xadvance=%d page=0 chnl=15%n",c,x,y,w,h,b.x,fm.getAscent()+b.y,advance));
            x+=w+2;row=Math.max(row,h);
        }
        g.dispose();ImageIO.write(atlas,"png",out.resolve("game-ui.png").toFile());
        Files.writeString(out.resolve("game-ui.fnt"),"info face=\"Roboto\" size=15 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=0,0,0,0 spacing=1,1\n"
            +"common lineHeight="+fm.getHeight()+" base="+fm.getAscent()+" scaleW=512 scaleH=256 pages=1 packed=0\npage id=0 file=\"game-ui.png\"\nchars count="+chars.size()+"\n"+records);
        System.out.println("Font glyphs="+chars.size()+" page=512x256 RGBA=524288 font="+font.getFontName()+" lineHeight="+fm.getHeight());
    }
}
