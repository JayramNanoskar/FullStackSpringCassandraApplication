package com.jayram.bookreads.search;

import java.util.List;

//Model to represent the structure of the search result returned by the Open Library API. 
public class SearchResult {
    
    private int numFound;
    private List<SearchResultBook> docs;

    public int getNumFound() {
        return numFound;
    }
    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }
    public List<SearchResultBook> getDocs() {
        return docs;
    }
    public void setDocs(List<SearchResultBook> docs) {
        this.docs = docs;
    }
    

}
