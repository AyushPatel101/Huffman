import java.io.IOException;

public class Decompressor implements IHuffConstants {

	// pre: none
	// post: rebuilds freqStorage by scanning in header from in
	public void readHeaderSCF(BitInputStream in, int[] freqStorage)
			throws IOException {
		// scanning in header...
		for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
			int bits = in.readBits(IHuffConstants.BITS_PER_INT);
			// if bits==-1, header was formed incorrectly
			if (bits == -1)
				throw new IOException("header formed incorrectly");
			// store bits (frequency) into freqStorage at index k
			freqStorage[k] = bits;
		}
		// incrementing PSEUDO_EOF to build tree correctly (and to know when
		// decompression is done)
		freqStorage[PSEUDO_EOF]++;
	}

	// pre: none
	// post: returns TreeNode root of tree
	public TreeNode readHeaderSTF(BitInputStream in) throws IOException {
		int bit = in.readBits(1);
		if (bit == 0) {
			// recursive step: if bit=0, then at internal node, so continue
			// building tree by reading in header and storing left and right
			// child
			return new TreeNode(readHeaderSTF(in), -1, readHeaderSTF(in));
		} else if (bit == 1) {
			// if bit=1, then at a leaf node, so scan in next BITS_PER_WORD + 1
			// bits which is value, and frequency as 1 (doesn't matter never
			// use)
			return new TreeNode(in.readBits(IHuffConstants.BITS_PER_WORD + 1),
					1);
		}
		// if bit doesn't = 0 or 1, then header formed incorrectly
		else
			throw new IOException("header formed incorrectly");
	}

	// pre: none
	// post: Writes uncompressed data into out and returns an int indicating how
	// many bits written.
	public int decode(BitInputStream in, BitOutputStream out, TreeNode root)
			throws IOException {
		// get ready to walk tree, start at root
		TreeNode node = root;
		int count = 0;
		boolean done = false;
		// while haven't reached PSEUDO_EOF
		while (!done) {
			int bit = in.readBits(1);
			// if get to end of compressed file without getting to PSEUDO_EOF,
			// then throw error
			if (bit == -1) {
				in.close();
				out.close();
				throw new IOException("Error reading compressed file. \n"
						+ "unexpected end of input. No PSEUDO_EOF value.");
			}
			// if scanned bit is a 0, move to left child
			else if (bit == 0)
				node = node.getLeft();
			// if scanned bit is a 1, move to right child
			else if (bit == 1)
				node = node.getRight();
			// if at a leaf node..
			if (node.isLeaf()) {
				// if at PSEUDO_EOF, end decompression and close streams
				if (node.getValue() == IHuffConstants.PSEUDO_EOF) {
					done = true;
					out.close();
					in.close();
				}
				// if not at PSEUDO_EOF..
				else {
					// write those that value into uncompressed file
					out.writeBits(IHuffConstants.BITS_PER_WORD,
							node.getValue());
					// increment count accordingly
					count += IHuffConstants.BITS_PER_WORD;
					// reset to beginning of tree
					node = root;
				}
			}
		}
		return count;
	}
}
