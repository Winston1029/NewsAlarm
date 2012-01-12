package com.moupress.app.rssReader;


public class FeedParserFactory {

	public static FeedParser getParser(String feedUrl){
		
		return new AndroidSaxFeedParser(feedUrl);
	}
}
