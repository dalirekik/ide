/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.jdt.internal.core.search;

import com.codenvy.eclipse.core.runtime.CoreException;
import com.codenvy.eclipse.core.runtime.IPath;
import com.codenvy.eclipse.core.runtime.IProgressMonitor;
import com.codenvy.eclipse.core.runtime.OperationCanceledException;
import com.codenvy.eclipse.core.runtime.Path;
import com.codenvy.eclipse.jdt.core.search.IJavaSearchScope;
import com.codenvy.eclipse.jdt.core.search.SearchDocument;
import com.codenvy.eclipse.jdt.core.search.SearchParticipant;
import com.codenvy.eclipse.jdt.core.search.SearchPattern;
import com.codenvy.eclipse.jdt.core.search.SearchRequestor;
import com.codenvy.eclipse.jdt.internal.core.index.IndexLocation;
import com.codenvy.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;
import com.codenvy.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import com.codenvy.eclipse.jdt.internal.core.search.matching.MatchLocator;

/**
 * A search participant describes a particular extension to a generic search mechanism, allowing thus to
 * perform combined search actions which will involve all required participants
 *
 * A search scope defines which participants are involved.
 *
 * A search participant is responsible for holding index files, and selecting the appropriate ones to feed to
 * index queries. It also can map a document path to an actual document (note that documents could live outside
 * the workspace or no exist yet, and thus aren't just resources).
 */
public class JavaSearchParticipant extends SearchParticipant {

	private ThreadLocal indexSelector = new ThreadLocal();

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#beginSearching()
	 */
	public void beginSearching() {
		super.beginSearching();
		this.indexSelector.set(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#doneSearching()
	 */
	public void doneSearching() {
		this.indexSelector.set(null);
		super.doneSearching();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#getName()
	 */
	public String getDescription() {
		return "Java"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#getDocument(String)
	 */
	public SearchDocument getDocument(String documentPath) {
		return new JavaSearchDocument(documentPath, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#indexDocument(SearchDocument)
	 */
	public void indexDocument(SearchDocument document, IPath indexPath) {
		// TODO must verify that the document + indexPath match, when this is not called from scheduleDocumentIndexing
		document.removeAllIndexEntries(); // in case the document was already indexed

		String documentPath = document.getPath();
		if (com.codenvy.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(documentPath)) {
			new SourceIndexer(document).indexDocument();
		} else if (com.codenvy.eclipse.jdt.internal.compiler.util.Util.isClassFileName(documentPath)) {
			new BinaryIndexer(document).indexDocument();
		}
	}

	/* (non-Javadoc)
	 * @see SearchParticipant#locateMatches(SearchDocument[], SearchPattern, IJavaSearchScope, SearchRequestor, IProgressMonitor)
	 */
	public void locateMatches(SearchDocument[] indexMatches, SearchPattern pattern,
			IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {

		MatchLocator matchLocator =
			new MatchLocator(
				pattern,
				requestor,
				scope,
				monitor
		);

		/* eliminating false matches and locating them */
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		matchLocator.locateMatches(indexMatches);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#selectIndexes(org.eclipse.jdt.core.search.SearchQuery, org.eclipse.jdt.core.search.SearchContext)
	 */
	public IPath[] selectIndexes(SearchPattern pattern, IJavaSearchScope scope) {
		IndexSelector selector = (IndexSelector) this.indexSelector.get();
		if (selector == null) {
			selector = new IndexSelector(scope, pattern);
			this.indexSelector.set(selector);
		}
		IndexLocation[] urls = selector.getIndexLocations();
		IPath[] paths = new IPath[urls.length];
		for (int i = 0; i < urls.length; i++) {
			paths[i] = new Path(urls[i].getIndexFile().getPath());
		}
		return paths;
	}

	public IndexLocation[] selectIndexURLs(SearchPattern pattern, IJavaSearchScope scope) {
		IndexSelector selector = (IndexSelector) this.indexSelector.get();
		if (selector == null) {
			selector = new IndexSelector(scope, pattern);
			this.indexSelector.set(selector);
		}
		return selector.getIndexLocations();
	}

}