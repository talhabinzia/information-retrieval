# information-retrieval
The code is written in Main.java
This project indexed a corpus of 40,000 files after tokenization, stop word removal, html parsing, R.E matching and 3rd party stemmer. Plus constructing forward and inverted indexes using hashmap for efficient retrieval.

The crux of the project is the construction of backward and inverted indexes using hashmaps, so with knowing only a term we can list all the documents in which it occurs in constant time.

You can download the code and view the main file.

Steps
1. remove stop words 
2. Stemming 
3. Create Forward and inverted indexes for retrieving purpose 
4. Search and retrieve information based on given query
