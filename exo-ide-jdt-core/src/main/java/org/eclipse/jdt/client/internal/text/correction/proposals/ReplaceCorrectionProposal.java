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
package org.eclipse.jdt.client.internal.text.correction.proposals;

import com.google.gwt.user.client.ui.Image;

import org.eclipse.jdt.client.JdtClientBundle;
import org.eclipse.jdt.client.runtime.CoreException;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.edits.ReplaceEdit;
import org.exoplatform.ide.editor.shared.text.edits.TextEdit;

public class ReplaceCorrectionProposal extends CUCorrectionProposal {

    private String fReplacementString;

    private int fOffset;

    private int fLength;

    public ReplaceCorrectionProposal(String name, int offset, int length, String replacementString, int relevance,
                                     IDocument document) {
        super(name, relevance, document, new Image(JdtClientBundle.INSTANCE.correction_change()));
        fReplacementString = replacementString;
        fOffset = offset;
        fLength = length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jface.text.IDocument)
     */
    @Override
    protected void addEdits(IDocument doc, TextEdit rootEdit) throws CoreException {
        super.addEdits(doc, rootEdit);

        TextEdit edit = new ReplaceEdit(fOffset, fLength, fReplacementString);
        rootEdit.addChild(edit);
    }

}
