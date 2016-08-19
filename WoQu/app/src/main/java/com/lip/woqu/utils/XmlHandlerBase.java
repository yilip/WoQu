package com.lip.woqu.utils;

import android.content.Context;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XmlHandlerBase extends DefaultHandler {
	public XmlHandlerBase(Context c) {
	}

	@Override
	public abstract void characters(char[] ch, int start, int length)
			throws SAXException;

	@Override
	public abstract void endElement(String uri, String localName, String qName)
			throws SAXException;

	@Override
	public abstract void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException;

}
