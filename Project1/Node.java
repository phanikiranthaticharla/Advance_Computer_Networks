package network;

import java.io.*;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

public class Node {

	static ArrayList<String> uniDirNeighborList = new ArrayList<String>();
	static ArrayList<String> biDirNeighborList = new ArrayList<String>();
	static ArrayList<String> mprList = new ArrayList<String>();
	static int NodeId;
	static int[] seqNum = new int[10];

	public static void parseDataMsg(String msg, FileWriter received) {
		try {
			Pattern D = Pattern.compile("(\\d+)\\s(\\d+)\\s([a-zA-Z]+)\\s(\\d+)\\s(\\d+)\\s(.*)");
			Matcher DataMatch = D.matcher(msg);
			// System.out.println("nodeid "+NodeId);
			if (DataMatch.find()) {
				/*
				 * Check if the data received is intended for the NodeX, if yes,
				 * write the data to Xreceived.txt, else drop it
				 */
				if (Integer.parseInt(DataMatch.group(5)) == NodeId) {

					// Writes the content to the file
					received.write(DataMatch.group(6) + "\n");
					System.out.println("written to file " + received);
				} else {
					/*
					 * Check if the nodeId is present in the routing table and
					 * route accordingly
					 */
				}
			}
		} catch (IOException e) {
			System.out.println("Exception while writing to file " + received);
			e.printStackTrace();
		}
	}

	public static void parseHelloMsg(String msg) {
		Pattern P = Pattern.compile("\\*\\s(\\d+)\\sHELLO\\sUNIDIR(.*)BIDIR(.*)MPR(.*)");
		System.out.println("msg " + msg);

		Matcher HelloMatch = P.matcher(msg);
		if (HelloMatch.find()) {
			String neighbor = HelloMatch.group(1);
			System.out.println("neighbor " + neighbor);
			String[] uniDirNeighbors = HelloMatch.group(2).split(" ");
			String[] biDirNeighbors = HelloMatch.group(3).split(" ");
			String nodeId = Integer.toString(NodeId);
			if (Arrays.asList(uniDirNeighbors).contains(nodeId) || Arrays.asList(biDirNeighbors).contains(nodeId)) {
				if (!(biDirNeighborList
						.contains(neighbor))) { /*
												 * If I'm there in his
												 * uniDirNeighborList or
												 * biDirNeighborList, i'll add
												 * him to my biDirNeighborList
												 */
					System.out.println("Adding " + neighbor + " to birDirNeighborList");
					biDirNeighborList.add(neighbor);

				}

			} else if (!(Arrays.asList(uniDirNeighbors).contains(neighbor))) {
				System.out.println("Adding " + neighbor + " to uniDirNeighbor list");
				// Adding nodeX to my uniDirNeighborList
				uniDirNeighborList.add(neighbor);
			} else {

			}
		}
	}

	public static void parseTCMsg(String msg) {
		Pattern P = Pattern.compile("\\*\\s(\\d+)\\sTC\\s(\\d+)\\s(\\d+) MS (.*)");
		System.out.println("msg " + msg);

		Matcher TCMatch = P.matcher(msg);
		if (TCMatch.find()) {
			int srcNode = Integer.parseInt(TCMatch.group(2));
			int curSeqNum = Integer.parseInt(TCMatch.group(3));
			if (curSeqNum > seqNum[srcNode]) {
				seqNum[srcNode] = curSeqNum;
				System.out.println("process the TC message as seq num is greater than previous");
				/*
				 * If the MPR selector set of NodeX is not null, then broadcast
				 * the current TC message to all its neighbors
				 */

			} else {
				System.out.println("discard msg");
			}
		}

	}

	public static void main(String[] args) {

		String input_args = "";
		for (String s : args) {
			input_args += s + " ";
		}

		NodeId = Integer.parseInt(args[0]);
		File FromX = new File("From" + NodeId + ".txt");
		File Xreceived = new File(NodeId + "Received.txt");
		File toX = new File("to" + NodeId + ".txt");

		try {

			// creates the required files for the NodeX
			FromX.createNewFile();
			Xreceived.createNewFile();
			toX.createNewFile();

			// creates a FileWriter Object for FromX.txt and toX.txt files
			FileWriter writer = new FileWriter(FromX, true);
			FileWriter received = new FileWriter(Xreceived, true);
			// Writes the input argument to the file fromX.txt
			writer.write(input_args + "\n");

			// creates a FileReader Object for ToX.txt file
			FileReader fileReader = new FileReader(toX);
			BufferedReader br = new BufferedReader(fileReader); // creates a
																// BufferedReader
																// Object

			// Pattern to check the type of msg: DATA or HELLO or TC
			Pattern P = Pattern.compile("(\\*|\\d+)\\s(\\d+)\\s([a-zA-Z]+)");

			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				Matcher M = P.matcher(line);
				if (M.find()) {
					if (M.group(3).equals("DATA")) {
						// If msg type is DATA, process the DATA msg
						parseDataMsg(line, received);

					} else if (M.group(3).equals("HELLO")) {
						System.out.println("process hello message");
						parseHelloMsg(line);

					} else if (M.group(3).equals("TC")) {
						System.out.println("process TC message");
						parseTCMsg(line);
					} else {
						System.out.println("Invalid message");
					}
				}
			}

			br.close();
			received.flush();
			received.close();
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to open file '" + toX + "'");
		} catch (IOException e) {
			System.out.println("IOException " + e);
		}

	}
}
