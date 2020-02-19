import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


public class XorInputStream extends FilterInputStream {
	private byte xor=113;
	
	public XorInputStream(InputStream in) {
		super(in);
	}
	public XorInputStream(InputStream arg0,byte xor) {
		super(arg0);
		this.xor=xor;
	}
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret= super.read(b, off, len);
		for (int i=0; i<b.length; i++) {
			 b[i]=(byte) (b[i] ^ xor);
		}
		return ret;
	}
	@Override
	public int read(byte[] b) throws IOException {
		int ret= super.read(b);
		for (int i=0; i<b.length; i++) {
			 b[i]=(byte) (b[i] ^ xor);
		}
		return ret;
	}


	
}
