/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.texteditor.codeassistant;

import com.codenvy.ide.api.icon.Icon;
import com.google.gwt.user.client.ui.Widget;


/**
 * The interface of completion proposals generated by content assist processors. A completion proposal contains information used
 * to present the proposed completion to the user, to insert the completion should the user select it, and to present context
 * information for the chosen completion once it has been inserted.
 * <p>
 * This interface can be implemented by clients
 * </p>
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public interface CompletionProposal {


    /**
     * Returns optional additional information about the proposal. The additional information will be presented to assist the user
     * in deciding if the selected proposal is the desired choice.
     *
     * @return the additional information or <code>null</code>
     */
    Widget getAdditionalProposalInfo();

    /**
     * Returns the string to be displayed in the list of completion proposals.
     *
     * @return the string to be displayed
     */
    String getDisplayString();

    /**
     * Returns the image to be displayed in the list of completion proposals. The image would typically be shown to the left of the
     * display string.
     *
     * @return the image to be shown or <code>null</code> if no image is desired
     */
    Icon getIcon();

    /**
     * Returns the characters which trigger the application of this completion proposal.
     *
     * @return the completion characters for this completion proposal or <code>null</code> if no completion other than the new line
     *         character is possible
     */
    char[] getTriggerCharacters();

    /**
     * Returns <code>true</code> if the proposal may be automatically inserted, <code>false</code> otherwise. Automatic insertion
     * can happen if the proposal is the only one being proposed, in which case the content assistant may decide to not prompt the
     * user with a list of proposals, but simply insert the single proposal. A proposal may veto this behavior by returning
     * <code>false</code> to a call to this method.
     *
     * @return <code>true</code> if the proposal may be inserted automatically, <code>false</code> if not
     */
    boolean isAutoInsertable();

    void getCompletion(CompletionCallback callback);

    public interface CompletionCallback{
        void onCompletion(Completion completion);
    }
}