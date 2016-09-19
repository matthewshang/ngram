import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Ngram
{
	private static String DELIMITER_SPACE = " ";
	private static String NEWLINE = System.getProperty("line.separator");
	private static String[] PUNCTUATION = {",", ".", "?", "!", ";"};

	public static void main(String[] args)
	{
		String[] words = new String[1];
		try
		{
			words = readFileToWords(args[0]);
			// Arrays.stream(words).forEach(s -> System.out.print(s + " "));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Map<List<String>, ArrayList<String>> model = buildModel(words, 3);
		// model.entrySet()
		// 	 .stream()
		// 	 .forEach(entry -> {
		// 	 	 System.out.print("(");
		// 	     entry.getKey().stream().forEach(s -> System.out.print("'" + s + "', "));
		// 	     System.out.print("): [");
		// 	     entry.getValue().stream().forEach(s -> System.out.print("'" + s + "', "));
		// 	     System.out.print("]\n");
		// 	 });

		ArrayList<String> generated = generate(model, 3, 500);
		System.out.println(formatOutput(generated));

		// Map<List<String>, Integer> ngrams = countNGrams(words, 2);
		// Map<List<String>, Integer> sorted = 
		// ngrams.entrySet()
		// 	  .stream()
		// 	  .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		// 	  .collect(Collectors.toMap(
		// 	      Map.Entry::getKey,
		// 		  Map.Entry::getValue,
		// 		  (e1, e2) -> e1,
		// 		  LinkedHashMap::new));
		// sorted.entrySet()
		//       .stream()
		//       .limit(20)
		//       .forEach(entry -> {
		// 	      for (int i = 0; i < entry.getKey().size(); i++)
		// 		  {
		// 		      System.out.print(entry.getKey().get(i) + " ");
		// 	      }
		// 		  System.out.println(": " + entry.getValue());
		// 	  });
	}

	private static String formatOutput(ArrayList<String> generated)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < generated.size(); i++)
		{
			if (i != 0 && !isPunct(generated.get(i)))
			{
				builder.append(" ");
			}
			builder.append(generated.get(i));
		}
		return builder.toString();
	}

	private static boolean isPunct(String token)
	{
		for (int i = 0; i < PUNCTUATION.length; i++)
		{
			if (token.equals(PUNCTUATION[i]))
			{
				return true;
			}
		}
		return false;
	}

	private static ArrayList<String> generate(Map<List<String>, ArrayList<String>> model, int n, int maxIterations)
	{
		ArrayList<String> output = new ArrayList<>();
		Random random = new Random();
		List<List<String>> keys = new ArrayList<>(model.keySet());
		List<String> seed = keys.get(random.nextInt(keys.size()));
		List<String> current = Collections.unmodifiableList(seed);

		output.addAll(seed);
		for (int i = 0; i < maxIterations; i++)
		{

			if (model.containsKey(current))
			{
				List<String> possibleTokens = model.get(current);

				String nextToken = possibleTokens.get(random.nextInt(possibleTokens.size()));
				if (nextToken.equals(""))
				{
					break;
				}
				output.add(nextToken);
				current = Collections.unmodifiableList(output.subList(output.size() - n, output.size()));
			}
			else
			{
				break;
			}
		}
		return output;
	}

	private static Map<List<String>, ArrayList<String>> buildModel(String[] tokens, int n)
	{
		Map<List<String>, ArrayList<String>> model = new HashMap<>();
		if (tokens.length < n)
		{
			return model;
		}
		for (int i = 0; i < tokens.length - n; i++)
		{
			List<String> gram = new ArrayList<>();
			for (int j = 0; j < n; j++)
			{
				gram.add(tokens[i + j]);
			}
			String nextToken = tokens[i + n];
			List<String> gramKey = Collections.unmodifiableList(gram);
			ArrayList<String> nextGrams = model.get(gramKey);
			if (nextGrams == null)
			{
				nextGrams = new ArrayList<>();
				model.put(gramKey, nextGrams);
			}
			nextGrams.add(nextToken);
		}
		List<String> finalGram = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			finalGram.add(tokens[tokens.length - n + i]);
		}
		List<String> gramKey = Collections.unmodifiableList(finalGram);
		ArrayList<String> nextGrams = model.get(gramKey);
		if (nextGrams == null)
		{
			nextGrams = new ArrayList<>();
			model.put(gramKey, nextGrams);
		}
		nextGrams.add("");
		return model;
	}

	private static Map<List<String>, Integer> countNGrams(String[] tokens, int n)
	{
		Map<List<String>, Integer> ngrams = new HashMap<List<String>, Integer>();

		for (int i = 0; i < tokens.length - n + 1; i++)
		{
			List<String> tuple = new ArrayList<>();
			for (int j = 0; j < n; j++)
			{
				tuple.add(tokens[i + j]);
			}
			List<String> keyTuple = 
				Collections.unmodifiableList(tuple);
			Integer amount = ngrams.get(keyTuple);
			if (amount == null)
			{
				amount = 0;
			}
			ngrams.put(keyTuple, amount + 1);
		}

		return ngrams;
	}
	
	private static String[] readFileToWords(String filename) throws IOException
	{
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		ArrayList<String> words = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null)
		{
			String[] lineWords = line.split(DELIMITER_SPACE);
			for (int i = 0; i < lineWords.length; i++)
			{
				boolean punctFound = false;
				for (int j = 0; j < PUNCTUATION.length; j++)
				{
					int index = lineWords[i].indexOf(PUNCTUATION[j]);
					if (index != -1)
					{
						words.add(lineWords[i].substring(0, index));
						words.add(PUNCTUATION[j]);
						punctFound = true;
						break;
					}
				}
				if (!punctFound)
				{
					words.add(lineWords[i]);
				}
			}
			words.add(NEWLINE);
		}
		bufferedReader.close();
		return words.toArray(new String[words.size()]);
	}
}