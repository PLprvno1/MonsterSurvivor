import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream;
import com.badlogic.gdx.utils.*;
import javazoom.jl.decoder.*;

/** Offline read-only PCM audit using cached project decoders. Never opens an audio device or writes audio. */
public final class AudioProbe {
    static final class Measure {
        int rate,channels,peak;long frames,decodedFrames,fullScale,nearScale,firstAudible=-1,lastAudible=-1;
        double squares,sum;int[] first,last,tail;int tailCount,tailAt;
        long limit=Long.MAX_VALUE;
        void setup(int rate,int channels,long limit) {
            if(rate<=0 || channels<1 || channels>8)throw new IllegalArgumentException("invalid PCM format");
            this.rate=rate;this.channels=channels;this.limit=limit>0?limit:Long.MAX_VALUE;
            first=new int[channels];last=new int[channels];tail=new int[2048*channels];
        }
        void accept(byte[] data,int length) {
            if(length%(channels*2)!=0)throw new IllegalArgumentException("partial PCM frame");
            for(int i=0;i<length;i+=channels*2) {
                decodedFrames++;if(frames>=limit)continue;
                int framePeak=0;
                for(int c=0;c<channels;c++) {
                    int at=i+c*2,value=(short)((data[at]&255)|(data[at+1]<<8));int absolute=Math.abs(value);
                    if(frames==0)first[c]=value;last[c]=value;
                    peak=Math.max(peak,absolute);framePeak=Math.max(framePeak,absolute);sum+=value;squares+=(double)value*value;
                    if(absolute>=32767)fullScale++;if(absolute>=32760)nearScale++;
                    tail[tailAt*channels+c]=value;
                }
                if(framePeak>32) {if(firstAudible<0)firstAudible=frames;lastAudible=frames;}
                tailAt=(tailAt+1)%2048;tailCount=Math.min(2048,tailCount+1);frames++;
            }
        }
        Map<String,Object> result() {
            if(frames==0)throw new IllegalArgumentException("no decoded PCM");
            var out=new LinkedHashMap<String,Object>();out.put("decode","OK");out.put("sampleRate",rate);out.put("channels",channels);
            out.put("analyzedFrames",frames);out.put("decodedFrames",decodedFrames);out.put("decodedSeconds",decodedFrames/(double)rate);
            out.put("pcm16Bytes",frames*channels*2);out.put("samplePeakDbfs",peak==0?null:20*Math.log10(peak/32768.0));
            double rms=Math.sqrt(squares/(frames*channels))/32768.0;out.put("rmsDbfs",rms==0?null:20*Math.log10(rms));
            out.put("dcOffset",sum/(frames*channels)/32768.0);out.put("fullScaleSamples",fullScale);out.put("nearScaleSamples",nearScale);
            out.put("leadingSilenceSeconds",(firstAudible<0?frames:firstAudible)/(double)rate);
            out.put("trailingSilenceSeconds",(lastAudible<0?frames:frames-1-lastAudible)/(double)rate);
            int jump=0;for(int c=0;c<channels;c++)jump=Math.max(jump,Math.abs(last[c]-first[c]));out.put("endpointJump",jump/32768.0);
            int n=Math.min(tailCount,Math.max(1,rate/100));double tailSquares=0;
            for(int f=0;f<n;f++)for(int c=0;c<channels;c++){int value=tail[((tailAt-1-f+2048)%2048)*channels+c];tailSquares+=(double)value*value;}
            double tailRms=Math.sqrt(tailSquares/(n*channels))/32768.0;out.put("last10msRmsDbfs",tailRms==0?null:20*Math.log10(tailRms));return out;
        }
    }
    static Map<String,Object> probe(JsonValue entry) throws Exception {
        Path path=Path.of(entry.getString("absolute"));Measure m=new Measure();String codec;
        if(path.toString().toLowerCase(Locale.ROOT).endsWith(".ogg")) {
            codec="Vorbis";
            try(var input=new OggInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
                m.setup(input.getSampleRate(),input.getChannels(),entry.getLong("granule",-1));byte[] data=new byte[16384];int n;
                while((n=input.read(data))>0)m.accept(data,n);
            }
        } else if(path.toString().toLowerCase(Locale.ROOT).endsWith(".mp3")) {
            codec="MP3";Bitstream input=new Bitstream(new BufferedInputStream(Files.newInputStream(path)));
            try {
                MP3Decoder decoder=new MP3Decoder();OutputBuffer buffer=null;Header h;int bitrate=0;boolean variable=false;
                while((h=input.readFrame())!=null) {
                    int channels=h.mode()==Header.SINGLE_CHANNEL?1:2;
                    if(buffer==null) {m.setup(h.getSampleRate(),channels,-1);buffer=new OutputBuffer(channels,false);decoder.setOutputBuffer(buffer);bitrate=h.bitrate_instant();}
                    if(channels!=m.channels || h.getSampleRate()!=m.rate)throw new IOException("changing MP3 format");
                    variable|=h.vbr() || h.bitrate_instant()!=bitrate;
                    decoder.decodeFrame(h,input);m.accept(buffer.getBuffer(),buffer.reset());input.closeFrame();
                }
                var out=m.result();out.put("codec",codec);out.put("firstFrameBitrate",bitrate);out.put("variableBitrate",variable);return out;
            } finally {input.close();}
        } else throw new IllegalArgumentException("unsupported audit decoder");
        var out=m.result();out.put("codec",codec);return out;
    }
    public static void main(String[] args)throws Exception {
        var input=new JsonReader().parse(Files.readString(Path.of(args[0])));var output=new ArrayList<Map<String,Object>>();
        for(var entry=input.child;entry!=null;entry=entry.next) {
            Map<String,Object> result;
            try {result=probe(entry);}catch(Throwable failure) {result=new LinkedHashMap<>();result.put("decode","ERROR");result.put("error",failure.toString());}
            result.put("path",entry.getString("path"));output.add(result);
        }
        var text=new StringWriter();try(var json=new JsonWriter(text)) {json.setOutputType(JsonWriter.OutputType.json);write(json,output);}
        Files.writeString(Path.of(args[1]),text.toString());
        System.out.println("Full decode audit: "+output.size()+" files; errors="+output.stream().filter(r->!"OK".equals(r.get("decode"))).count()+"; no playback/output audio");
    }
    static void write(JsonWriter json,Object value)throws IOException {
        if(value instanceof Map<?,?> map) {json.object();for(var entry:map.entrySet()){json.name(entry.getKey().toString());write(json,entry.getValue());}json.pop();}
        else if(value instanceof Collection<?> list) {json.array();for(var item:list)write(json,item);json.pop();}
        else json.value(value);
    }
}
