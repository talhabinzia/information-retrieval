import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

//Instructions: Run it in eclipse
//		Program process docs placed in corpus folder must be located at project base folder only .txt files suppourted.
//		stopwrod.txt must also be present at base folder including stopwors seperated by space tab or newline.
//		The program creates 2 directories for its working purpose, named improved corpus and final corpus at project base folder.
//		All required output files are created at project base folder after programe executes.
//		It can take few minutes with default corpus to complete processing. On i7 pc it took just 2 mins

public class Main
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting Processing");

		File directory = new File("improved corpus");
														
		if (directory.exists())				//Deleting old data from improved corpus if any
		{
			String[] entries = directory.list();
			for (String s : entries)
			{
				File currentFile = new File(directory.getPath(), s);
				currentFile.delete();
			}
		}

		directory = new File("final corpus");	//Deleting old data from final corpus if any
		if (directory.exists())
		{
			String[] entries = directory.list();
			for (String s : entries)
			{
				File currentFile = new File(directory.getPath(), s);
				currentFile.delete();
			}
		}
		
		//creating final corpus and improved corpus directories if they dont exist.
		File fi1 = (new File("final corpus"));
		fi1.mkdir();
		File fi2 = (new File("improved corpus"));
		fi2.mkdir();

		// Adding stopwords to a HASH from stopwords.txt
		Scanner sc = null;
		sc = new Scanner(new File("stopword.txt"));
		HashMap<String, Integer> docIDlist = new HashMap<String, Integer>();
		HashMap<Integer, Integer> docLength = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> tf = new HashMap<Integer, Integer>();
		HashMap<String, Integer> sth = new HashMap<String, Integer>();//stop word list
		for (int i = 1; sc.hasNext(); i++)
		{
			sth.put(sc.next(), i);
		}

		// Creating DocIds file
		File docids = new File("docids.txt");
		FileOutputStream outs = null;

		outs = new FileOutputStream(docids);

		OutputStreamWriter outw = new OutputStreamWriter(outs);
		Writer didsWr = new BufferedWriter(outw);

		// Traversing files one by one in directory corpus
		File dir = new File("corpus");
		Integer id = 0;
		for (File file : dir.listFiles())
		{
			id++;

			// Writing in docids file

			docIDlist.put(file.getName(), id);

			didsWr.write(id + "	" + file.getName());
			didsWr.write(System.getProperty("line.separator"));

			Document doc = null;

			// Parsing html to memory from file
			doc = Jsoup.parse(file, "UTF-8");

			String str = doc.text();

			// Applying R.E
			Matcher m = Pattern.compile("\\w+(\\.?\\w+)*").matcher(str);

			int match = 0;

			// Creating new files in folder improved
			Writer wr = null;

			File statText = new File("improved corpus/" + Integer.toString(id)
					+ ".txt");
			FileOutputStream is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			wr = new BufferedWriter(osw);

			//Removing stop words from each doc and writing it to files in improved corpus
			for (; m.find();)
			{
				String tok = m.group().toLowerCase();

				Integer tokid = null;
				tokid = sth.get(tok);

				if (tokid != null)
				{
					match = 1;
					break;
				}

				if (match == 0)
				{

					wr.append(tok + ' ');

				}
				match = 0;
			}

			wr.flush();
			wr.close();
			
			//Stemmer is modified, it will stem each file from directory improved corpus and write it to directory final corpus
			String[] arrr = new String[1];
			arrr[0] = "improved corpus/" + Integer.toString(id) + ".txt";
			Stemmer.stemFile(arrr);
		}
		didsWr.close();
		sc.close();

		// Creating TermIds File
		HashMap<String, Integer> dic = new HashMap<String, Integer>();
		File termids = new File("termids.txt");
		FileOutputStream outs1 = null;
		outs1 = new FileOutputStream(termids);
		OutputStreamWriter outw1 = new OutputStreamWriter(outs1);
		Writer twr = new BufferedWriter(outw1);

		// Constructing Dictionary
		File dir1 = new File("final corpus");
		Integer termid = 1;
		String tem;
		for (File file : dir1.listFiles())
		{

			Scanner in = new Scanner(file);

			for (; in.hasNext();)
			{
				tem = in.next();

				Integer n = null;
				n = dic.get(tem);
				if (n == null)
				{
					dic.put(tem, termid);
					tf.put(termid, 0);
					twr.write(Integer.toString(termid) + "\t" + tem);
					twr.write(System.getProperty("line.separator"));
					termid++;
				}
			}
			twr.flush();
			in.close();
		}
		twr.close();

		// Creating Farward and Inveted Index

		Writer diwr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File("doc_index.txt"))));

		Writer diwr1 = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File("index_doc.txt"))));

		File dir2 = new File("final corpus");

		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> FI = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> II = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();

		String term;
		for (File file : dir2.listFiles())
		{
			int termtotal = 0;
			int pos = 0;
			Scanner in = new Scanner(file);
			String dids = null;
			dids = file.getName().substring(0, file.getName().length() - 4);
			Integer did = Integer.parseInt(dids);

			FI.put(did, new HashMap<Integer, ArrayList<Integer>>());

			for (; in.hasNext();)
			{
				termtotal++;
				term = in.next();
				pos++;
				Integer tid = dic.get(term);
				tf.put(tid, tf.get(tid) + 1);

				// FI
				if (!(FI.get(did).containsKey(tid)))
				{
					FI.get(did).put(tid, new ArrayList<Integer>());
				}

				FI.get(did).get(tid).add(pos);

				// II
				if (!(II.containsKey(tid)))
				{
					II.put(tid, new HashMap<Integer, ArrayList<Integer>>());
				}
				if (!(II.get(tid).containsKey(did)))
				{
					II.get(tid).put(did, new ArrayList<Integer>());
				}

				II.get(tid).get(did).add(pos);

			}
			docLength.put(did, termtotal);
			in.close();
		}

		// Printing Farword Index to file
		Integer[] temar = new Integer[1];
		for (int i = 1; i <= FI.size(); i++)// change < become <= last doc was
											// missing
		{
			Integer did = i;
			Integer[] keys = FI.get(did).keySet().toArray(temar);
			Arrays.sort(keys);

			if (keys[0] != null)// change if check added empty docs were
								// crashing
			{
				for (int k = 0; k < keys.length; k++)
				{
					Integer tid = keys[k];
					diwr.write(did.toString() + "\t" + tid);
					for (int l = 0; l < FI.get(did).get(tid).size(); l++)
					{
						int pos = FI.get(did).get(tid).get(l);
						diwr.write("\t" + pos);
					}
					diwr.write(System.getProperty("line.separator"));
					diwr.flush();
				}
			}
		}
		diwr.close();

		// Printing Inverted Index to file
		for (int i = 1; i < II.size() + 1; i++)
		{
			Integer tid = i;
			diwr1.write(tid.toString());

			Integer did = null;
			Integer[] keys = II.get(tid).keySet().toArray(temar);
			Arrays.sort(keys);
			for (int k = 0; k < II.get(tid).size(); k++)
			{
				did = keys[k];
				int dif = 0;
				for (int l = 0; l < II.get(tid).get(did).size(); l++)
				{
					int pos = II.get(tid).get(did).get(l);

					if (k == 0 && l == 0)
						diwr1.write("\t" + did + ":");
					else if (k > 0 && l == 0)
					{
						dif = keys[k] - keys[k - 1];
						diwr1.write("\t" + dif + ":");
					} else
						diwr1.write("\t0:");

					if (l == 0)
					{
						diwr1.write(Integer.toString(pos));
					} else
					{
						diwr1.write(Integer.toString((II.get(tid).get(did)
								.get(l) - II.get(tid).get(did).get(l - 1))));
					}

				}
			}
			diwr1.write(System.getProperty("line.separator"));
			diwr1.write(System.getProperty("line.separator"));
			diwr1.flush();
		}

		diwr1.close();

		System.out.println("Processing Complete");

		
		
		//================The core of project ends here======================
		//Below is just user interface which uses above structures/indexes to search given query
		
		
		Scanner s = new Scanner(System.in);
		for (;;)
		{
			System.out.println();
			System.out.println();
			System.out
					.println("Enter\n1 for Doc only\n2 for Term only\n3 for Both\n4 To Exit");
			int ch = s.nextInt();

			String fname, term1;
			if (ch == 1)
			{
				int jmp = 0;
				System.out.println("Enter file name");
				fname = s.next();

				if (docIDlist.containsKey(fname))
				{
				} else if (docIDlist.containsKey(fname + ".txt"))
				{
					fname = fname + ".txt";
				} else
				{
					jmp = 1;
					System.out.println("Document: " + fname + " Not Found");
				}
				if (jmp == 0)
				{
					int docid = docIDlist.get(fname);

					System.out.println("Listing for document: " + fname);
					System.out.println("DOCID :" + docid);
					System.out.println("Distict tems :" + FI.get(docid).size());
					System.out.println("Total Terms :" + docLength.get(docid));
				}

			}
			if (ch == 2)
			{
				System.out.println("Enter term");
				term1 = s.next();

				if (dic.containsKey(term1))
				{

					System.out.println("Listing for term: " + term1);
					System.out.println("TERMID :" + dic.get(term1));
					int tid = dic.get(term1);
					System.out.println("Number of documents containing term: "
							+ II.get(tid).size());
					System.out.println("Term frequency in corpus: "
							+ tf.get(tid));
				} else
				{
					System.out.println("Term: " + term1 + " Not Found");
				}
			}
			if (ch == 3)
			{

				int jmp = 0;

				System.out.println("Enter file name");
				fname = s.next();
				System.out.println("Enter term");
				term1 = s.next();

				if (docIDlist.containsKey(fname))
				{
				} else if (docIDlist.containsKey(fname + ".txt"))
				{
					fname = fname + ".txt";
				} else
				{
					jmp = 1;
					System.out.println("Document: " + fname + " Not Found");
				}

				if (jmp == 0)
				{
					int docid = docIDlist.get(fname);
					if (dic.containsKey(term1))
					{

						int tid = dic.get(term1);

						if (II.get(tid).containsKey(docid))
						{

							System.out.println("\nInverted list for term: "
									+ fname + "\nIn Document: " + fname);
							System.out.println("TERMID :" + dic.get(term1));
							System.out.println("DOCID :" + docid);
							System.out.println("Term frequency in document: "
									+ II.get(tid).get(docid).size());
							System.out.print("Postings:");
							for (int i = 0; i < II.get(tid).get(docid).size(); i++)
							{
								System.out.print(" "
										+ II.get(tid).get(docid).get(i));
							}
							System.out.println();
						}
						else
							System.out.println("Term: "+term1+" Dont Exist in Doc: "+fname );
					} else
					{
						System.out.println("\nTerm: " + term1 + " Not Found");
					}
				}
			}
			if (ch == 4)
				break;
		}
		s.close();
		System.out.print("The End");
	}

}
