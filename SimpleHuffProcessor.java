
/*  Student information for assignment:


 *
 *  On my honor, Ayush Patel, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1 (Student whose turning account is being used)
 *  UTEID: ap55837
 *  email address: patayush01@utexas.edu
 *  Grader name: Tony
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

	private IHuffViewer myViewer;
	private Compressor compressor;
	private Decompressor decompressor;
	private int headerFormat;
	private int[] freqStorage;
	private HuffmanTree tree;
	
	/**
	 * Preprocess data so that compression is possible --- count
	 * characters/create tree/store state so that a subsequent call to compress
	 * will work. The InputStream is <em>not</em> a BitInputStream, so wrap it
	 * int one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what
	 *                     kind of header to use, standard count format,
	 *                     standard tree format, or possibly some format added
	 *                     in the future.
	 * @return number of bits saved by compression or some other measure Note,
	 *         to determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic
	 *         number, the header format number, the header to reproduce the
	 *         tree, AND the actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat)
			throws IOException {
		this.headerFormat = headerFormat;
		freqStorage = new int[IHuffConstants.ALPH_SIZE + 1];
		compressor = new Compressor();
		int[] originalCount = new int[1];
		// newCount initialized to 2 * BITS_PER_INT because thats bits for
		// MAGIC_NUMBER and headerFormat
		int[] newCount = {
				IHuffConstants.BITS_PER_INT + IHuffConstants.BITS_PER_INT };
		// counts the the number of bits in the original file, stored in
		// originalCount[0]
		compressor.freqCount(new BitInputStream(new BufferedInputStream(in)),
				originalCount, freqStorage);
		tree = new HuffmanTree(freqStorage);
		if (headerFormat == IHuffConstants.STORE_COUNTS) {
			// if SCF, then adds to newCount the SCF header size
			newCount[0] += IHuffConstants.BITS_PER_INT
					* IHuffConstants.ALPH_SIZE;
		} else if (headerFormat == IHuffConstants.STORE_TREE) {
			// if STF, then add another BITS_PER_INT representing size of tree,
			// then stores the header size into newCount[0]
			newCount[0] += IHuffConstants.BITS_PER_INT
					+ tree.getSize(compressor);
		} else {
			// if not STF or SCF, then not implemented, so close InputStream and
			// throw an error
			in.close();
			throw new IOException("this type of compresion is not implmented");
		}
		// stores the huffman code of each value into a map (called BitMappings
		// in HuffmanTree class), value added to newCount[0] is # of bits for
		// actual compressed data
		tree.getBitStrings("", newCount);
		// subtracts original size of file with new count of file (hence
		// returning bits saved)
		return originalCount[0] - newCount[0];
	}

	/**
	 * Compresses input to output, where the same InputStream has previously
	 * been pre-processed via <code>preprocessCompress</code> storing state used
	 * by this call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger
	 *              than the input file. If this is false do not create the
	 *              output file if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file
	 *                     or writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force)
			throws IOException {
		if (force) {
			// using BufferedInputStream for runtime, same with
			// BufferedOutputStream
			BitInputStream inStream = new BitInputStream(
					new BufferedInputStream(in));
			BitOutputStream outStream = new BitOutputStream(
					new BufferedOutputStream(out));
			// every compression implemented writes the MAGIC_NUMBER and
			// headerFormat
			outStream.writeBits(IHuffConstants.BITS_PER_INT,
					IHuffConstants.MAGIC_NUMBER);
			outStream.writeBits(IHuffConstants.BITS_PER_INT, headerFormat);
			// using this array to count amount of bits in compressed file,
			// initialized to following value because of wrote MAGIC_NUMBER and
			// headerFormat
			int[] count = {
					IHuffConstants.BITS_PER_INT + IHuffConstants.BITS_PER_INT };
			// if doing SCF, then write SCF header
			if (headerFormat == IHuffConstants.STORE_COUNTS)
				compressor.writeHeaderSCF(outStream, count, freqStorage);
			// else if doing STF...
			else if (headerFormat == IHuffConstants.STORE_TREE) {
				// write the size of the tree
				outStream.writeBits(IHuffConstants.BITS_PER_INT,
						tree.getSize(compressor));
				// add to count appropriately (adding BITS_PER_INT) because
				// added BITS_PER_INT bits for tree size
				count[0] += IHuffConstants.BITS_PER_INT;
				// write the STF header
				tree.writeHeaderSTF(outStream, count, compressor);
			}
			// else, this compression is not implemented, so close streams and
			// throw error
			else {
				outStream.close();
				inStream.close();
				throw new IOException(
						"this type of compresion is not implmented");
			}
			// if got here, then doing either SCF or STF, so compress
			// appropriately
			compressor.compress(inStream, outStream, count, tree);
			return count[0];
		}

		// if got here, then force is false, meaning compression will not
		// continue
		myViewer.showError(
				"Compressed file has more bits than uncompressed file. " + '\n'
						+ "Select 'force compression' to compress");
		return 0;
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file
	 *                     or writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		BitInputStream inStream = new BitInputStream(
				new BufferedInputStream(in));
		BitOutputStream outStream = new BitOutputStream(
				new BufferedOutputStream(out));
		decompressor = new Decompressor();
		// if MAGIC_NUMBER is not first BITS_PER_INT in file, then not .hf
		// compressed, so throw error
		if (inStream.readBits(
				IHuffConstants.BITS_PER_INT) != IHuffConstants.MAGIC_NUMBER) {
			inStream.close();
			outStream.close();
			throw new IOException("not huffman compressed file");
		}
		// rebuilding freqStorage
		freqStorage = new int[IHuffConstants.ALPH_SIZE + 1];
		// storageType is int determining whether its SCF, STF or something else
		int storageType = inStream.readBits(IHuffConstants.BITS_PER_INT);
		// if its SCF..
		if (storageType == IHuffConstants.STORE_COUNTS) {
			// read SCF header and rebuildHuffmanTree and store root
			decompressor.readHeaderSCF(inStream, freqStorage);
			tree = new HuffmanTree(freqStorage);

		}
		// else if its STF..
		else if (storageType == IHuffConstants.STORE_TREE) {
			// read size of tree (I don't use)
			inStream.readBits(IHuffConstants.BITS_PER_INT);
			// read STF header and store root
			tree = new HuffmanTree(decompressor, inStream);

		}
		// else, not STF or SCF, so that type of compression is not implemented
		else {
			outStream.close();
			inStream.close();
			throw new IOException("this type of compresion is not implmented");
		}
		// if got here, then SCF or STF, so write uncompressed file
		return tree.decode(inStream, outStream, decompressor);
	}

	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	private void showString(String s) {
		if (myViewer != null)
			myViewer.update(s);
	}
}
