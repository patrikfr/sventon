/*
 * ====================================================================
 * Copyright (c) 2005-2009 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.cache.logmessagecache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.sventon.cache.CacheException;
import org.sventon.model.LogMessage;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains cached log messages.
 * This implementation uses <a href="http://lucene.apache.org">Lucene</a> internally.
 *
 * @author jesper@sventon.org
 */
public final class LogMessageCacheImpl implements LogMessageCache {

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The <i>lucene</i> directory.
   */
  private final Directory directory;

  /**
   * Lucene Analyzer to use.
   *
   * @see org.apache.lucene.analysis.Analyzer
   */
  private final Class<? extends Analyzer> analyzer;

  private final SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span class=\"searchHit\">", "</span>");

  /**
   * Constructor.
   *
   * @param directory The <i>lucene</i> directory.
   * @param analyzer  Analyzer to use.
   */
  public LogMessageCacheImpl(final Directory directory, final Class<? extends Analyzer> analyzer) {
    this.analyzer = analyzer;
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  public void init() throws CacheException {
    logger.debug("Initializing cache");

    IndexWriter writer = null;
    try {
      if (!IndexReader.indexExists(directory)) {
        writer = new IndexWriter(directory, analyzer.newInstance(), true);
        writer.close();
      }
    } catch (final Exception ioex) {
      throw new CacheException("Unable to startup lucene index", ioex);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (final IOException ioex) {
          logger.error("Unable to startup lucene index", ioex);
        }
      }
    }
  }

  /**
   * Gets the index Searcher.
   *
   * @return Index searcher.
   * @throws IOException if unable to create searcher.
   */
  private Searcher getIndexSearcher() throws IOException {
    return new IndexSearcher(directory);
  }

  /**
   * Gets the index Reader.
   *
   * @return Index reader.
   * @throws IOException if unable to create reader.
   */
  private IndexReader getIndexReader() throws IOException {
    return IndexReader.open(directory);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<LogMessage> find(final String queryString) throws CacheException {
    final List<LogMessage> result = new ArrayList<LogMessage>();
    Searcher searcher = null;
    IndexReader reader = null;
    try {
      logger.debug("Searching for: [" + queryString + "]");
      Query query = new QueryParser("content", analyzer.newInstance()).parse(queryString);
      searcher = getIndexSearcher();
      reader = getIndexReader();
      query = query.rewrite(reader);
      final Hits hits = searcher.search(query);

      final Highlighter highlighter = new Highlighter(getFormatter(), new QueryScorer(query));
      highlighter.setTextFragmenter(new NullFragmenter());
      highlighter.setEncoder(new SimpleHTMLEncoder());

      final int hitCount = hits.length();
      logger.debug("Hit count: " + hitCount);

      if (hitCount > 0) {
        for (int i = 0; i < hitCount; i++) {
          final Document document = hits.doc(i);
          final String content = document.get("content");
          final TokenStream tokenStream = analyzer.newInstance().tokenStream("content", new StringReader(content));
          final String highlightedContent = highlighter.getBestFragment(tokenStream, content);
          result.add(new LogMessage(Long.parseLong(document.get("revision")), highlightedContent));
        }
      }
    } catch (final Exception ex) {
      throw new CacheException("Unable to perform lucene search", ex);
    } finally {
      try {
        if (searcher != null) {
          searcher.close();
        }
        if (reader != null) {
          reader.close();
        }
      } catch (final IOException ioex) {
        logger.error("Unable to close lucene index", ioex);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void add(final LogMessage logMessage) throws CacheException {
    IndexWriter writer = null;

    try {
      writer = new IndexWriter(directory, analyzer.newInstance(), false);

      final Document document = new Document();
      document.add(new Field("revision", String.valueOf(logMessage.getRevision()), Field.Store.YES, Field.Index.NO));
      document.add(new Field("content", logMessage.getMessage() == null ? ""
          : logMessage.getMessage(), Field.Store.YES, Field.Index.TOKENIZED));
      writer.addDocument(document);
    } catch (final Exception ioex) {
      throw new CacheException("Unable to add content to lucene cache", ioex);
    } finally {
      if (writer != null) {
        // Optimize and close the writer to finish building the index
        try {
          writer.optimize();
        } catch (final IOException ioex) {
          logger.error("Unable to optimize lucene index", ioex);
        } finally {
          try {
            writer.close();
          } catch (final IOException ioex) {
            logger.error("Unable to close lucene index", ioex);
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws CacheException {
    int count = -1;
    IndexWriter writer = null;

    try {
      writer = new IndexWriter(directory, analyzer.newInstance(), false);
      count = writer.docCount();
      writer.close();
    } catch (final Exception ioex) {
      throw new CacheException("Unable to get lucene cache size", ioex);
    } finally {
      if (writer != null) {
        // Close the writer
        try {
          writer.close();
        } catch (final IOException ioex) {
          logger.error("Unable to close lucene index", ioex);
        }
      }
    }
    return count;
  }

  /**
   * {@inheritDoc}
   */
  public void clear() throws CacheException {
    logger.debug("Clearing log message cache");
    IndexWriter writer;
    try {
      writer = new IndexWriter(directory, analyzer.newInstance(), true);
      writer.close();
    } catch (Exception ex) {
      throw new CacheException("Unable to close lucene index", ex);
    }
  }

  public Formatter getFormatter() {
    return htmlFormatter;
  }
}