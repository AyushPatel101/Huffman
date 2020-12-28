import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class Compressor implements IHuffConstants {

	// pre: none
	// post: counts the the number of bits in the original file, stored in
	// count[0], also increments freqStorage[value] (adding to frequency of that
	// value)
	public void freqCount(BitInputStream in, int[] count, int[] freqStorage)
			throws IOException {
		int bit = in.readBits(IHuffConstants.BITS_PER_WORD);
		// reads through all the bits in file until get to end (bit==-1)
		while (bit != -1) {
			// incrementing count[0] by BITS_PER_WORD (amount of bits just
			// scanned in)
			count[0] += IHuffConstants.BITS_PER_WORD;
			// adding to frequency of that bit
			freqStorage[bit]++;
			bit = in.readBits(IHuffConstants.BITS_PER_WORD);
		}
		// once get to end, set frequency of PSEUDO_EOF to 1 (Because there will
		// be one PSEUDO_EOF at the end of file)
		freqStorage[IHuffConstants.PSEUDO_EOF]++;

	}

	// pre: none
	// post: writes compressed data using new bit sequence (stored in
	// bitMappings)
	public void compress(BitInputStream in, BitOutputStream out, int[] count,
			HuffmanTree tree) throws IOException {
		int bit = in.readBits(IHuffConstants.BITS_PER_WORD);
		while (bit != -1) {
			// uses fillBits method to store bit sequence string into compressed
			// file
			fillBits(out, tree.getBitMappings().get(bit), count);
			bit = in.readBits(IHuffConstants.BITS_PER_WORD);
		}
		// once all bits from BitInputStream are scanned, add the PSEUDO_EOF to
		// compressed file
		fillBits(out, tree.getBitMappings().get(IHuffConstants.PSEUDO_EOF),
				count);
		out.close();
	}

	// pre: none
	// post: writes String code into BitOutputStream out, and counts how many
	// bits added (count[0])
	private void fillBits(BitOutputStream out, String code, int[] count) {
		// goes through each character in code, translates that character into
		// an integer and writes it to out
		for (int i = 0; i < code.length(); i++) {
			out.writeBits(1, Integer.parseInt(code.substring(i, i + 1)));
			count[0]++;
		}
	}

	// pre: none
	// post: counts the length of the header, and if(out!=null) writes the SCF
	// header into out
	public void writeHeaderSCF(BitOutputStream out, int[] count,
			int[] freqStorage) {
		// goes through each frequency in valueStorage, and writes BITS_PER_INT
		// amount of bits for that frequency
		for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
			out.writeBits(IHuffConstants.BITS_PER_INT, freqStorage[k]);
			count[0] += IHuffConstants.BITS_PER_INT;
		}
	}

	// pre: none
	// post: updates count to store size of header, and if out is not null,
	// writes header to compressed file
	public void writeHeaderSTF(BitOutputStream out, TreeNode node,
			int[] count) {
		// if got here than add to count, cause node is not null
		count[0]++;
		if (node.isLeaf()) {
			// if out isn't null..
			if (out != null) {
				// write 1 to out because at a leaf node
				out.writeBits(1, 1);
				// write BITS_PER_WORD + 1 (+1 to account for PSEUDO_EOF which
				// will be that many bits) bits for node's value
				out.writeBits(IHuffConstants.BITS_PER_WORD + 1,
						node.getValue());
			}
			// update count to account for node's value being added
			count[0] += IHuffConstants.BITS_PER_WORD + 1;
		} else {
			// if out isn't null, then write 0 to out
			if (out != null)
				out.writeBits(1, 0);
			// if left child isn't null, recursive step passing in left child
			if (node.getLeft() != null)
				writeHeaderSTF(out, node.getLeft(), count);
			// if right child isn't null, recursive step passing in right child
			if (node.getRight() != null)
				writeHeaderSTF(out, node.getRight(), count);

		}
	}
}
