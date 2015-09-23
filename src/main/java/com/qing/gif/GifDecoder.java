package com.qing.gif;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class GifDecoder extends Thread{

	/**状态：正在解码中*/
	public static final int STATUS_PARSING = 0;
	/**状态：图片格式错误*/
	public static final int STATUS_FORMAT_ERROR = 1;
	/**状态：打开失败*/
	public static final int STATUS_OPEN_ERROR = 2;
	/**状态：解码成功*/
	public static final int STATUS_FINISH = -1;
	
	/** gif图片数据*/
	private InputStream in;
	/** 当前的状态*/
	private int status;

	/** full image width*/
	public int width;
	/** full image height*/
	public int height;
	/** global color table used*/
	private boolean gctFlag;
	/** size of global color table*/
	private int gctSize;
	/** iterations; 0 = repeat forever*/
	private int loopCount = 1;
	
	/** global color table*/
	private int[] gct;
	/** local color table*/
	private int[] lct;
	/** active color table*/
	private int[] act;
	
	/** background color index*/
	private int bgIndex;
	/** background color*/
	private int bgColor;
	/** previous bg color*/
	private int lastBgColor;
	/** pixel aspect ratio*/
	private int pixelAspect;
	
	/** local color table flag*/
	private boolean lctFlag;
	/** interlace flag*/
	private boolean interlace;
	/** local color table size*/
	private int lctSize;

	/** current image rectangle*/
	private int ix, iy, iw, ih;
	/** */
	private int lrx, lry, lrw, lrh;
	/** current frame*/
	private Bitmap image;
	/** previous frame*/
	private Bitmap lastImage;
	/** current frame*/
	private GifFrame currentFrame = null;

	/** isShow = false*/
	private boolean isShow = false;

	/** current data block*/
	private byte[] block = new byte[256];
	/** block size*/
	private int blockSize = 0;

	/** last graphic control extension info*/
	private int dispose = 0;
	/** 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev*/
	private int lastDispose = 0;
	/** use transparent color*/
	private boolean transparent = false;
	/** delay in milliseconds*/
	private int delay = 0;
	/** transparent color index*/
	private int transIndex;
	/** MaxStackSize = 4096*/
	private static final int MaxStackSize = 4096;
	// max decoder pixel stack size

	// LZW decoder working arrays
	private short[] prefix;
	private byte[] suffix;
	private byte[] pixelStack;
	private byte[] pixels;

	/** frames read from current file*/
	private GifFrame gifFrame;
	private int frameCount;

	private GifAction action = null;
	
	private byte[] gifData = null;

	public GifDecoder(byte[] data,GifAction act){
		gifData = data;
		action = act;
	}
	
	public GifDecoder(InputStream is,GifAction act){
		in = is;
		action = act;
	}

	public void run(){
		if(in != null){
			readStream();
		}else if(gifData != null){
			readByte();
		}
	}
	
	/**
	 * 释放资源
	 */
	public void free(){
		GifFrame fg = gifFrame;
		while(fg != null){
			fg.image = null;
			fg = null;
			gifFrame = gifFrame.nextFrame;
			fg = gifFrame;
		}
		if(in != null){
			try{
			in.close();
			}catch(Exception ex){}
			in = null;
		}
		gifData = null;
	}
	
	/**
	 * 当前状态
	 * @return
	 */
	public int getStatus(){
		return status;
	}
	
	/**
	 * 解码是否成功，成功返回true
	 * @return 成功返回true，否则返回false
	 */
	public boolean parseOk(){
		return status == STATUS_FINISH;
	}
	
	/**
	 * 取某帧的延时时间
	 * @param n 第几帧 
	 * @return 延时时间，毫秒
	 */
	public int getDelay(int n) {
		delay = -1;
		if ((n >= 0) && (n < frameCount)) {
			// delay = ((GifFrame) frames.elementAt(n)).delay;
			GifFrame f = getFrame(n);
			if (f != null)
				delay = f.delay;
		}
		return delay;
	}
	
	/**
	 * 取所有帧的延时时间
	 * @return
	 */
	public int[] getDelays(){
		GifFrame f = gifFrame;
		int[] d = new int[frameCount];
		int i = 0;
		while(f != null && i < frameCount){
			d[i] = f.delay;
			f = f.nextFrame;
			i++;
		}
		return d;
	}
	

	/**
	 * 取总帧 数
	 * @return 图片的总帧数
	 */
	public int getFrameCount() {
		return frameCount;
	}

	/**
	 * 取第一帧图片
	 * @return
	 */
	public Bitmap getImage() {
		return getFrameImage(0);
	}

	public int getLoopCount() {
		return loopCount;
	}

	private void setPixels() {
		int[] dest = new int[width * height];
		// fill in starting image contents based on last image's dispose code
		if (lastDispose > 0) {
			if (lastDispose == 3) {
				// use image before last
				int n = frameCount - 2;
				if (n > 0) {
					lastImage = getFrameImage(n - 1);
				} else {
					lastImage = null;
				}
			}
			if (lastImage != null) {
				lastImage.getPixels(dest, 0, width, 0, 0, width, height);
				// copy pixels
				if (lastDispose == 2) {
					// fill last image rect area with background color
					int c = 0;
					if (!transparent) {
						c = lastBgColor;
					}
					for (int i = 0; i < lrh; i++) {
						int n1 = (lry + i) * width + lrx;
						int n2 = n1 + lrw;
						for (int k = n1; k < n2; k++) {
							dest[k] = c;
						}
					}
				}
			}
		}

		// copy each source line to the appropriate place in the destination
		int pass = 1;
		int inc = 8;
		int iline = 0;
		for (int i = 0; i < ih; i++) {
			int line = i;
			if (interlace) {
				if (iline >= ih) {
					pass++;
					switch (pass) {
					case 2:
						iline = 4;
						break;
					case 3:
						iline = 2;
						inc = 4;
						break;
					case 4:
						iline = 1;
						inc = 2;
					}
				}
				line = iline;
				iline += inc;
			}
			line += iy;
			if (line < height) {
				int k = line * width;
				int dx = k + ix; // start of line in dest
				int dlim = dx + iw; // end of dest line
				if ((k + width) < dlim) {
					dlim = k + width; // past dest edge
				}
				int sx = i * iw; // start of line in source
				while (dx < dlim) {
					// map color and insert in destination
					int index = ((int) pixels[sx++]) & 0xff;
					int c = act[index];
					if (c != 0) {
						dest[dx] = c;
					}
					dx++;
				}
			}
		}
		image = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);//Config.ARGB_4444
	}

	/**
	 * 取第几帧的图片
	 * @param n 帧数，0为第一帧
	 * @return 可画的图片，如果没有此帧或者出错，返回null
	 */
	public Bitmap getFrameImage(int n) {
		GifFrame frame = getFrame(n);	
		if (frame == null)
			return null;
		else
			return frame.image;
	}

	/**
	 * 取当前帧图片
	 * @return 当前帧可画的图片
	 */
	public GifFrame getCurrentFrame(){
		return currentFrame;
	}
	
	/**
	 * 取第几帧，每帧包含了可画的图片和延时时间
	 * @param n 帧数
	 * @return
	 */
	public GifFrame getFrame(int n) {
		GifFrame frame = gifFrame;
		int i = 0;
		while (frame != null) {
			if (i == n) {
				return frame;
			} else {
				frame = frame.nextFrame;
			}
			i++;
		}
		return null;
	}

	/**
	 * 重置，进行本操作后，会直接到第一帧
	 */
	public void reset(){
		currentFrame = gifFrame;
	}
	
	/**
	 * 下一帧，进行本操作后，通过getCurrentFrame得到的是下一帧
	 * @return 返回下一帧
	 */
	public GifFrame next() {	
		if(isShow == false){
			isShow = true;
			return gifFrame;
		}else{	
			if(status == STATUS_PARSING){
				if(currentFrame.nextFrame != null)
					currentFrame = currentFrame.nextFrame;			
				//currentFrame = gifFrame;
			}else{			
				currentFrame = currentFrame.nextFrame;
				if (currentFrame == null) {
					currentFrame = gifFrame;
				}
			}
			return currentFrame;
		}
	}

	private int readByte(){
		in = new ByteArrayInputStream(gifData);
		gifData = null;
		return readStream();
	}
	
//	public int read(byte[] data){
//		InputStream is = new ByteArrayInputStream(data);
//		return read(is);
//	}
	
	private int readStream(){
		init();
		if(in != null){
			readHeader();
			if(!error()){
				readContents();
				if(frameCount < 0){
					status = STATUS_FORMAT_ERROR;
					action.parseOk(false,-1);
				}else{
					status = STATUS_FINISH;
					action.parseOk(true,-1);
				}
			}
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}else {
			status = STATUS_OPEN_ERROR;
			action.parseOk(false,-1);
		}
		return status;
	}

	private void decodeImageData() {
		int NullCode = -1;
		int npix = iw * ih;
		int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

		if ((pixels == null) || (pixels.length < npix)) {
			pixels = new byte[npix]; // allocate new pixel array
		}
		if (prefix == null) {
			prefix = new short[MaxStackSize];
		}
		if (suffix == null) {
			suffix = new byte[MaxStackSize];
		}
		if (pixelStack == null) {
			pixelStack = new byte[MaxStackSize + 1];
		}
		// Initialize GIF data stream decoder.
		data_size = read();
		clear = 1 << data_size;
		end_of_information = clear + 1;
		available = clear + 2;
		old_code = NullCode;
		code_size = data_size + 1;
		code_mask = (1 << code_size) - 1;
		for (code = 0; code < clear; code++) {
			prefix[code] = 0;
			suffix[code] = (byte) code;
		}

		// Decode GIF pixel stream.
		datum = bits = count = first = top = pi = bi = 0;
		for (i = 0; i < npix;) {
			if (top == 0) {
				if (bits < code_size) {
					// Load bytes until there are enough bits for a code.
					if (count == 0) {
						// Read a new data block.
						count = readBlock();
						if (count <= 0) {
							break;
						}
						bi = 0;
					}
					datum += (((int) block[bi]) & 0xff) << bits;
					bits += 8;
					bi++;
					count--;
					continue;
				}
				// Get the next code.
				code = datum & code_mask;
				datum >>= code_size;
				bits -= code_size;

				// Interpret the code
				if ((code > available) || (code == end_of_information)) {
					break;
				}
				if (code == clear) {
					// Reset decoder.
					code_size = data_size + 1;
					code_mask = (1 << code_size) - 1;
					available = clear + 2;
					old_code = NullCode;
					continue;
				}
				if (old_code == NullCode) {
					pixelStack[top++] = suffix[code];
					old_code = code;
					first = code;
					continue;
				}
				in_code = code;
				if (code == available) {
					pixelStack[top++] = (byte) first;
					code = old_code;
				}
				while (code > clear) {
					pixelStack[top++] = suffix[code];
					code = prefix[code];
				}
				first = ((int) suffix[code]) & 0xff;
				// Add a new string to the string table,
				if (available >= MaxStackSize) {
					break;
				}
				pixelStack[top++] = (byte) first;
				prefix[available] = (short) old_code;
				suffix[available] = (byte) first;
				available++;
				if (((available & code_mask) == 0)
						&& (available < MaxStackSize)) {
					code_size++;
					code_mask += available;
				}
				old_code = in_code;
			}

			// Pop a pixel off the pixel stack.
			top--;
			pixels[pi++] = pixelStack[top];
			i++;
		}
		for (i = pi; i < npix; i++) {
			pixels[i] = 0; // clear missing pixels
		}
	}

	private boolean error() {
		return status != STATUS_PARSING;
	}

	private void init() {
		status = STATUS_PARSING;
		frameCount = 0;
		gifFrame = null;
		gct = null;
		lct = null;
	}

	private int read() {
		int curByte = 0;
		try {
			curByte = in.read();
		} catch (Exception e) {
			status = STATUS_FORMAT_ERROR;
		}
		return curByte;
	}

	private int readBlock() {
		blockSize = read();
		int n = 0;
		if (blockSize > 0) {
			try {
				int count = 0;
				while (n < blockSize) {
					count = in.read(block, n, blockSize - n);
					if (count == -1) {
						break;
					}
					n += count;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (n < blockSize) {
				status = STATUS_FORMAT_ERROR;
			}
		}
		return n;
	}

	private int[] readColorTable(int ncolors) {
		int nbytes = 3 * ncolors;
		int[] tab = null;
		byte[] c = new byte[nbytes];
		int n = 0;
		try {
			n = in.read(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (n < nbytes) {
			status = STATUS_FORMAT_ERROR;
		} else {
			tab = new int[256]; // max size to avoid bounds checks
			int i = 0;
			int j = 0;
			while (i < ncolors) {
				int r = ((int) c[j++]) & 0xff;
				int g = ((int) c[j++]) & 0xff;
				int b = ((int) c[j++]) & 0xff;
				tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
		return tab;
	}

	private void readContents() {
		// read GIF file content blocks
		boolean done = false;
		while (!(done || error())) {
			int code = read();
			switch (code) {
			case 0x2C: // image separator
				readImage();
				break;
			case 0x21: // extension
				code = read();
				switch (code) {
				case 0xf9: // graphics control extension
					readGraphicControlExt();
					break;
				case 0xff: // application extension
					readBlock();
					String app = "";
					for (int i = 0; i < 11; i++) {
						app += (char) block[i];
					}
					if (app.equals("NETSCAPE2.0")) {
						readNetscapeExt();
					} else {
						skip(); // don't care
					}
					break;
				default: // uninteresting extension
					skip();
				}
				break;
			case 0x3b: // terminator
				done = true;
				break;
			case 0x00: // bad byte, but keep going and see what happens
				break;
			default:
				status = STATUS_FORMAT_ERROR;
			}
		}
	}

	private void readGraphicControlExt() {
		read(); // block size
		int packed = read(); // packed fields
		dispose = (packed & 0x1c) >> 2; // disposal method
		if (dispose == 0) {
			dispose = 1; // elect to keep old image if discretionary
		}
		transparent = (packed & 1) != 0;
		delay = readShort() * 10; // delay in milliseconds
		transIndex = read(); // transparent color index
		read(); // block terminator
	}

	private void readHeader() {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (char) read();
		}
		if (!id.startsWith("GIF")) {
			status = STATUS_FORMAT_ERROR;
			return;
		}
		readLSD();
		if (gctFlag && !error()) {
			gct = readColorTable(gctSize);
			bgColor = gct[bgIndex];
		}
	}

	private void readImage() {
		ix = readShort(); // (sub)image position & size
		iy = readShort();
		iw = readShort();
		ih = readShort();
		int packed = read();
		lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
		interlace = (packed & 0x40) != 0; // 2 - interlace flag
		// 3 - sort flag
		// 4-5 - reserved
		lctSize = 2 << (packed & 7); // 6-8 - local color table size
		if (lctFlag) {
			lct = readColorTable(lctSize); // read table
			act = lct; // make local table active
		} else {
			act = gct; // make global table active
			if (bgIndex == transIndex) {
				bgColor = 0;
			}
		}
		int save = 0;
		if (transparent) {
			save = act[transIndex];
			act[transIndex] = 0; // set transparent color if specified
		}
		if (act == null) {
			status = STATUS_FORMAT_ERROR; // no color table defined
		}
		if (error()) {
			return;
		}
		decodeImageData(); // decode pixel data
		skip();
		if (error()) {
			return;
		}
		frameCount++;
		// create new image to receive frame data
		image = Bitmap.createBitmap(width, height, Config.ARGB_8888);//Config.ARGB_4444
		// createImage(width, height);
		setPixels(); // transfer pixel data to image
		if (gifFrame == null) {
			gifFrame = new GifFrame(image, delay);
			currentFrame = gifFrame;
		} else {
			GifFrame f = gifFrame;
			while(f.nextFrame != null){
				f = f.nextFrame;
			}
			f.nextFrame = new GifFrame(image, delay);
		}
		// frames.addElement(new GifFrame(image, delay)); // add image to frame
		// list
		if (transparent) {
			act[transIndex] = save;
		}
		resetFrame();
		action.parseOk(true, frameCount);
	}

	private void readLSD() {
		// logical screen size
		width = readShort();
		height = readShort();
		
		// packed fields
		int packed = read();
		gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
		// 2-4 : color resolution
		// 5 : gct sort flag
		gctSize = 2 << (packed & 7); // 6-8 : gct size
		bgIndex = read(); // background color index
		pixelAspect = read(); // pixel aspect ratio
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}

	private void readNetscapeExt() {
		do {
			readBlock();
			if (block[0] == 1) {
				// loop count sub-block
				int b1 = ((int) block[1]) & 0xff;
				int b2 = ((int) block[2]) & 0xff;
				loopCount = (b2 << 8) | b1;
			}
		} while ((blockSize > 0) && !error());
	}

	private int readShort() {
		// read 16-bit value, LSB first
		return read() | (read() << 8);
	}

	private void resetFrame() {
		lastDispose = dispose;
		lrx = ix;
		lry = iy;
		lrw = iw;
		lrh = ih;
		lastImage = image;
		lastBgColor = bgColor;
		dispose = 0;
		transparent = false;
		delay = 0;
		lct = null;
	}

	/**
	 * Skips variable length blocks up to and including next zero length block.
	 */
	private void skip() {
		do {
			readBlock();
		} while ((blockSize > 0) && !error());
	}
}
