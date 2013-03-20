/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.google.collide.client.editor.folding;

import com.google.collide.client.Resources;
import com.google.collide.client.editor.Buffer;
import com.google.collide.client.editor.Editor;
import com.google.collide.client.editor.gutter.Gutter;
import com.google.collide.client.editor.gutter.Gutter.ClickListener;
import com.google.collide.json.shared.JsonArray;
import com.google.collide.shared.document.Document;
import com.google.collide.shared.document.Line;
import com.google.collide.shared.document.TextChange;
import com.google.collide.shared.document.anchor.Anchor;
import com.google.collide.shared.document.anchor.AnchorManager;
import com.google.collide.shared.util.JsonCollections;
import com.google.collide.shared.util.ListenerManager;
import com.google.collide.shared.util.ListenerManager.Dispatcher;
import com.google.collide.shared.util.ListenerRegistrar;

import org.exoplatform.ide.editor.shared.text.BadLocationException;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.IDocumentInformationMapping;
import org.exoplatform.ide.editor.shared.text.IRegion;
import org.exoplatform.ide.editor.shared.text.ISlaveDocumentManager;
import org.exoplatform.ide.editor.shared.text.projection.IProjectionPosition;
import org.exoplatform.ide.editor.shared.text.projection.ProjectionDocument;
import org.exoplatform.ide.editor.shared.text.projection.ProjectionDocumentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class to manage the editor's code folding functionality.
 * 
 * The lifecycle of this class is tied to the {@link Editor} that owns it.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: FoldingManager.java Mar 2, 2013 6:39:46 PM azatsarynnyy $
 *
 */
public class FoldingManager implements Document.TextListener
{
   /**
    * A listener that is called when the user collapse or expand text block.
    */
   public interface FoldingListener
   {
      /**
       * @param lineNumber the line number of the first item in {@code linesToCollapse}
       * @param linesToCollapse a contiguous list of lines that should be collapsed
       */
      void onCollapse(int lineNumber, JsonArray<Line> linesToCollapse);

      /**
       * @param lineNumber the previous line number of the first item in {@code linesToExpand}
       * @param linesToExpand a contiguous list of lines that should be expanded
       */
      void onExpand(int lineNumber, JsonArray<Line> linesToExpand);

      /**
       * Called when any fold mark added/removed or it state changed (collapsed/expanded).
       */
      void onFoldMarksStateChaged();
   }

   /**
    * Manager for {@link FoldingListener}s.
    */
   private final ListenerManager<FoldingListener> foldingListenerManager;

   /**
    * Manager for anchors within a document.
    */
   private AnchorManager anchorManager;

   private final JsonArray<Anchor> anchorsInCollapsedRangeToRemove = JsonCollections.createArray();

   private final JsonArray<Anchor> anchorsInCollapsedRangeToShift = JsonCollections.createArray();

   private final JsonArray<Anchor> anchorsLeftoverFromLastLine = JsonCollections.createArray();

   /**
    * Resources.
    */
   private Resources resources;

   /**
    * Gutter for folding markers.
    */
   private final Gutter gutter;

   /**
    * Editor's buffer.
    */
   private final Buffer buffer;

   private Map<FoldMarker, DefaultFoldRange> markerToPositionMap = new HashMap<FoldMarker, DefaultFoldRange>();

   private Document document;

   protected IDocument masterDocument;

   protected ProjectionDocument slaveDocument;

   /**
    * The slave document manager.
    */
   private ISlaveDocumentManager slaveDocumentManager;

   /**
    * The mapping between model and visible document.
    */
   private IDocumentInformationMapping informationMapping;

   private FoldOccurrencesFinder foldOccurrencesFinder;

   /**
    * Creates new 'empty' {@link FoldingManager}.
    */
   public FoldingManager()
   {
      this(null, null, null);
   }

   /**
    * Constructs and returns new {@link FoldingManager} instance.
    * 
    * @param gutter {@link Gutter}
    * @param buffer {@link Buffer}
    * @param resources {@link Resources}
    */
   public FoldingManager(Gutter gutter, Buffer buffer, Resources resources)
   {
      this.gutter = gutter;
      this.buffer = buffer;
      this.resources = resources;
      foldingListenerManager = ListenerManager.create();
      initializeGutter();
   }

   private void initializeGutter()
   {
      if (gutter == null)
      {
         return;
      }

      gutter.setWidth(11);
      gutter.getClickListenerRegistrar().add(new ClickListener()
      {
         @Override
         public void onClick(int y)
         {
            final int lineNumber = buffer.convertYToLineNumber(y, true);
            FoldMarker foldMarker = findFoldMarker(lineNumber, false);
            if (foldMarker != null)
            {
               if (!foldMarker.isCollapsed())
               {
                  foldMarker = findFoldMarker(lineNumber, true);
                  if (foldMarker == null)
                  {
                     return;
                  }
               }
               toggleExpansionState(foldMarker);
            }
         }
      });
   }

   public ListenerRegistrar<FoldingListener> getFoldingListenerRegistrar()
   {
      return foldingListenerManager;
   }

   /**
    * Expand the specified <code>foldMarker</code>.
    * 
    * @param foldMarker the {@link FoldMarker} to expand
    */
   public void expand(FoldMarker foldMarker)
   {
      if (foldMarker.isCollapsed())
      {
         toggleExpansionState(foldMarker);
      }
   }

   /**
    * Collapse all expanded fold markers.
    */
   public void collapseAll()
   {
      for (FoldMarker marker : markerToPositionMap.keySet())
      {
         if (!marker.isCollapsed())
         {
            toggleExpansionState(marker);
         }
      }
   }

   /**
    * Expand all collapsed fold markers.
    */
   public void expandAll()
   {
      for (FoldMarker marker : markerToPositionMap.keySet())
      {
         if (marker.isCollapsed())
         {
            toggleExpansionState(marker);
         }
      }
   }

   /**
    * Collapse the specified <code>foldMarker</code>.
    * 
    * @param foldMarker the {@link FoldMarker} to collapse
    */
   public void collapse(FoldMarker foldMarker)
   {
      if (!foldMarker.isCollapsed())
      {
         toggleExpansionState(foldMarker);
      }
   }

   /**
    * @see com.google.collide.shared.document.Document.TextListener#onTextChange(com.google.collide.shared.document.Document, com.google.collide.json.shared.JsonArray)
    */
   @Override
   public void onTextChange(Document document, JsonArray<TextChange> textChanges)
   {
      updateFoldingStructure(foldOccurrencesFinder.computePositions(masterDocument));
   }

   /**
    * @param newDocument new {@link Document}
    */
   public void handleDocumentChanged(final Document newDocument)
   {
      if (foldOccurrencesFinder == null)
      {
         return;
      }
      markerToPositionMap.clear();
      if (document != null)
      {
         document.getTextListenerRegistrar().remove(this);
      }
      document = newDocument;
      anchorManager = document.getAnchorManager();
      document.getTextListenerRegistrar().add(this);

      freeSlaveDocument(slaveDocument);
      masterDocument = document.<IDocument> getTag("IDocument");
      initializeProjection(masterDocument);

      updateFoldingStructure(foldOccurrencesFinder.computePositions(masterDocument));
   }

   /**
    * Toggles the expansion state of the given fold marker.
    * 
    * @param foldMarker the fold marker 
    */
   private void toggleExpansionState(FoldMarker foldMarker)
   {
      if (foldMarker.isCollapsed())
      {
         foldMarker.markExpanded();
      }
      else
      {
         foldMarker.markCollapsed();
      }
      modifyFoldMarker(foldMarker);
      dispatchStateChaged();
   }

   /**
    * Modifies the given <code>foldMarker</code> if the <code>foldMarker</code>
    * is managed by this {@link FoldingManager}.
    * 
    * @param foldMarker {@link FoldMarker} to modify
    */
   private void modifyFoldMarker(FoldMarker foldMarker)
   {
      try
      {
         IProjectionPosition position = markerToPositionMap.get(foldMarker);
         IRegion[] regions = position.computeProjectionRegions(masterDocument);
         for (int i = 0; i < regions.length; i++)
         {
            final int startOffset = regions[i].getOffset();
            final int length = regions[i].getLength();

            if (foldMarker.isCollapsed())
            {
               slaveDocument.removeMasterDocumentRange(startOffset, length);
            }
            else
            {
               slaveDocument.addMasterDocumentRange(startOffset, length);
            }

            int firstLineNumber = masterDocument.getLineOfOffset(startOffset);
            int lineCount = masterDocument.getNumberOfLines(startOffset, length); // length-1 for expand (???)
            Line beginLine = document.getLineFinder().findLine(firstLineNumber).line();

            JsonArray<Line> linesToCollapse = JsonCollections.createArray();
            Line nextLine = beginLine;
            linesToCollapse.add(nextLine);
            for (int j = 0; j < lineCount - 2; j++)
            {
               nextLine = nextLine.getNextLine();
               linesToCollapse.add(nextLine);
            }

            if (foldMarker.isCollapsed())
            {
               collapseInternally(firstLineNumber - 1, linesToCollapse);
            }
            else
            {
               expandInternally(firstLineNumber - 1, linesToCollapse);
            }
         }
      }
      catch (BadLocationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void collapseInternally(int lineNumber, JsonArray<Line> linesToCollapse)
   {
      if (linesToCollapse.isEmpty())
      {
         return;
      }

      for (Line line : linesToCollapse.asIterable())
      {
         anchorManager.handleTextPredeletionForLine(line, 0, line.getText().length(), anchorsInCollapsedRangeToRemove,
            anchorsInCollapsedRangeToShift, linesToCollapse.indexOf(line) == 0);
      }

      //      if (true)
      //      {
      //         anchorManager.handleTextDeletionLastLineLeftover(anchorsLeftoverFromLastLine, linesToCollapse.get(0),
      //            linesToCollapse.peek(), lastLineFirstUntouchedColumn);
      //      }

      anchorManager.handleTextDeletionFinished(anchorsInCollapsedRangeToRemove, anchorsInCollapsedRangeToShift,
         anchorsLeftoverFromLastLine, linesToCollapse.get(0), lineNumber, 0, 0, linesToCollapse.peek().getText()
            .length());

      dispatchCollapse(lineNumber, linesToCollapse);
   }

   private void expandInternally(int lineNumber, JsonArray<Line> linesToExpand)
   {
      if (linesToExpand.isEmpty())
      {
         return;
      }

      // TODO manage anchors
      //      anchorManager.handleMultilineTextInsertion(line, lineNumber, column, newLine, newLineNumber, secondChunkColumnInNewLine);

      dispatchExpand(lineNumber, linesToExpand);
   }

   private void dispatchCollapse(final int lineNumber, final JsonArray<Line> linesToCollapse)
   {
      foldingListenerManager.dispatch(new Dispatcher<FoldingManager.FoldingListener>()
      {
         @Override
         public void dispatch(FoldingListener listener)
         {
            listener.onCollapse(lineNumber, linesToCollapse);
         }
      });
   }

   private void dispatchExpand(final int lineNumber, final JsonArray<Line> linesToExpand)
   {
      foldingListenerManager.dispatch(new Dispatcher<FoldingManager.FoldingListener>()
      {
         @Override
         public void dispatch(FoldingListener listener)
         {
            listener.onExpand(lineNumber, linesToExpand);
         }
      });
   }

   private void dispatchStateChaged()
   {
      foldingListenerManager.dispatch(new Dispatcher<FoldingManager.FoldingListener>()
      {
         @Override
         public void dispatch(FoldingListener listener)
         {
            listener.onFoldMarksStateChaged();
         }
      });
   }

   /**
    * Returns gutter for fold marks.
    * 
    * @return fold marks gutter
    */
   public Gutter getGutter()
   {
      return gutter;
   }

   /**
    * Returns the fold marker that contains the given line or <code>null</code>.
    * 
    * @param lineNumber the line number
    * @param exact <code>true</code> if the fold range must match exactly
    * @return the fold marker contains the given line or <code>null</code>
    */
   public FoldMarker findFoldMarker(int lineNumber, boolean exact)
   {
      FoldMarker previousFoldMarker = null;
      int previousDistance = Integer.MAX_VALUE;

      for (Entry<FoldMarker, DefaultFoldRange> entry : markerToPositionMap.entrySet())
      {
         FoldMarker foldMarker = entry.getKey();
         DefaultFoldRange position = entry.getValue();
         if (position == null)
         {
            continue;
         }
         int distance = getDistance(foldMarker, position, masterDocument, lineNumber);
         if (distance == -1)
            continue;
         if (!exact)
         {
            if (distance < previousDistance)
            {
               previousFoldMarker = foldMarker;
               previousDistance = distance;
            }
         }
         else if (distance == 0)
         {
            previousFoldMarker = foldMarker;
         }
      }
      return previousFoldMarker;
   }

   /**
    * Returns the distance of the given line to the start line of the given position in the given document. The distance is
    * <code>-1</code> when the line is not included in the given position.
    *
    * @param annotation the annotation
    * @param position the position
    * @param document the document
    * @param line the line
    * @return <code>-1</code> if line is not contained, a position number otherwise
    */
   private int getDistance(FoldMarker annotation, DefaultFoldRange position, IDocument document, int line)
   {
      if (position.getOffset() > -1 && position.getLength() > -1)
      {
         try
         {
            int startLine = document.getLineOfOffset(position.getOffset());
            int endLine = document.getLineOfOffset(position.getOffset() + position.getLength());
            if (startLine <= line && line < endLine)
            {
               if (annotation.isCollapsed())
               {
                  int captionOffset = position.computeCaptionOffset(document);
                  int captionLine = document.getLineOfOffset(position.getOffset() + captionOffset);
                  if (startLine <= captionLine && captionLine < endLine)
                     return Math.abs(line - captionLine);
               }
               return line - startLine;
            }
         }
         catch (BadLocationException x)
         {
         }
      }
      return -1;
   }

   /**
    * Updates folding structure according to he given <code>positions</code>.
    * 
    * @param positions list of the positions that describes the folding structure
    */
   private void updateFoldingStructure(List<DefaultFoldRange> positions)
   {
      markerToPositionMap.clear();
      for (DefaultFoldRange position : positions)
      {
         markerToPositionMap.put(new FoldMarker(resources), position);
      }
      dispatchStateChaged();
   }

   /**
    * Initializes the projection document from the master document based on
    * the master's fragments.
    * 
    * @param masterDocument
    */
   private void initializeProjection(IDocument masterDocument)
   {
      try
      {
         initializeDocumentInformationMapping(masterDocument);
         slaveDocument.addMasterDocumentRange(0, masterDocument.getLength());
         document.putTag("ProjectionDocument", slaveDocument);
      }
      catch (BadLocationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Initializes the document information mapping between the given master document and
    * created slave document.
    */
   private void initializeDocumentInformationMapping(IDocument masterDocument)
   {
      initializeSlaveDocumentManager();
      slaveDocument = (ProjectionDocument)slaveDocumentManager.createSlaveDocument(masterDocument);
      informationMapping = slaveDocumentManager.createMasterSlaveMapping(slaveDocument);
   }

   /**
    * Initializes the slave document manager.
    */
   private void initializeSlaveDocumentManager()
   {
      if (slaveDocumentManager == null)
      {
         slaveDocumentManager = new ProjectionDocumentManager();
      }
   }

   /**
    * Frees the given document if it is a slave document.
    *
    * @param slave the potential slave document
    */
   private void freeSlaveDocument(IDocument slave)
   {
      if (slaveDocumentManager != null && slaveDocumentManager.isSlaveDocument(slave))
      {
         slaveDocumentManager.freeSlaveDocument(slave);
      }
   }

   public IDocumentInformationMapping getInformationMapping()
   {
      return informationMapping;
   }

   public ProjectionDocument getSlaveDocument()
   {
      return slaveDocument;
   }

   public void setFoldFinder(FoldOccurrencesFinder foldOccurrencesFinder)
   {
      this.foldOccurrencesFinder = foldOccurrencesFinder;
   }

}
