package com.moupress.app.rssReader;

import java.util.List;


public class RssManager {
	
	FeedParser feedParser;
	private List<Message> messages;
	
	public List<Message> LoadFeed(String feedUri)
	{
		FeedParser parser = FeedParserFactory.getParser(feedUri);
		messages = parser.parse();
		
		return messages;
	}
}
