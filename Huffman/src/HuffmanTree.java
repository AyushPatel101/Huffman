
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
import java.io.IOException;
import java.util.HashMap;

public class HuffmanTree implements IHuffConstants {
	private HashMap<Integer, String> bitMappings;
	private TreeNode root;
	private int headerSizeSTF;
	private PriorityQueue<TreeNode> queue;

	// building HuffmanTree for SCF
	public HuffmanTree(int[] freqStorage) {
		bitMappings = new HashMap<>();
		// stores root by buildingHuffmanTree with queue
		queue = fillQueue(freqStorage);
		root = buildHuffmanTree();

	}

	// building HuffmanTree for STF
	public HuffmanTree(Decompressor decompress, BitInputStream in)
			throws IOException {
		bitMappings = new HashMap<>();
		root = decompress.readHeaderSTF(in);
	}

	// pre: none
	// post: returns a PriorityQueue of TreeNodes that is sorted from highest to
	// lowest priority
	private PriorityQueue<TreeNode> fillQueue(int[] freqStorage) {
		PriorityQueue<TreeNode> queue = new PriorityQueue<>();
		for (int i = 0; i < freqStorage.length; i++)
			// for each frequency in freqStoragem if that frequency is not 0
			// (value is present in original file)..
			if (freqStorage[i] != 0)
				// enqueue a new TreeNode with index stored as value, and
				// freqStorage[i] for frequency
				queue.enqueue(new TreeNode(i, freqStorage[i]));
		return queue;
	}

	// pre: none
	// post: (SCF) returns TreeNode root
	private TreeNode buildHuffmanTree() {
		// while queue has more than 1 element
		while (queue.size() > 1) {
			// dequeue 2 nodes, and set first dequeued node to left child,
			// second to right child and set value to whatever (doesn't matter)
			TreeNode nodeOne = queue.dequeue();
			TreeNode nodeTwo = queue.dequeue();
			TreeNode root = new TreeNode(nodeOne, -1, nodeTwo);
			// enqueue that new node into priority queue
			queue.enqueue(root);
		}
		// return last node left in queue (root)
		return queue.dequeue();
	}

	// pre: none
	// post: calls getBitStrings with TreeNode added to parameters
	public void getBitStrings(String bitString, int[] count) {
		getBitStrings(bitString, root, count);
	}

	// pre: none
	// post: stores bit sequences for node into bitMappings, and adds to count #
	// of bits for compressed data
	private void getBitStrings(String bitString, TreeNode node, int[] count) {
		// if node is leaf..
		if (node.isLeaf()) {
			// put value as key and bitString for value
			bitMappings.put(node.getValue(), bitString);
			// add length of bitString (number of bits to be added) * frequency
			// of that node= total number of bits to be added for that value
			count[0] += bitString.length() * node.getFrequency();
		} else {
			// if node.getLeft() isnt null, add 0 to bitString and go to left
			// child
			if (node.getLeft() != null)
				getBitStrings(bitString + "0", node.getLeft(), count);
			// if node.getRight() isnt null, add 1 to bitString and go to right
			// child
			if (node.getRight() != null)
				getBitStrings(bitString + "1", node.getRight(), count);
		}
	}

	// pre: none
	// post: calls writeHeaderSTF with root added to parameters, which writes
	// header and updates count
	public void writeHeaderSTF(BitOutputStream out, int[] count,
			Compressor compressor) {
		getSize(compressor);
		compressor.writeHeaderSTF(out, root, count);
	}

	// pre: none
	// post: returns bitMappings
	public HashMap<Integer, String> getBitMappings() {
		return bitMappings;
	}

	// pre:none
	// post: returns size. if size is not initially set, then calculate it
	public int getSize(Compressor compressor) {
		// if headerSizeSTF is 0, then calculate size (this is not in
		// constructor because SCF doesn't use, so waste of time to calculate)
		if (headerSizeSTF == 0) {
			// stores header size of STF so don't have to recalculate when
			// called in precompress and compress
			int[] count = new int[1];
			compressor.writeHeaderSTF(null, root, count);
			headerSizeSTF = count[0];
		}
		return headerSizeSTF;
	}

	// pre: none
	// post: calls decode method with root added to parameter, which writes
	// uncompressed data and returns # of bits written
	public int decode(BitInputStream in, BitOutputStream out,
			Decompressor decompressor) throws IOException {
		return decompressor.decode(in, out, root);
	}

}
