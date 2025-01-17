// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.editor.impl.view;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.AbstractEditorTest;
import com.intellij.openapi.editor.impl.EditorTextFieldRendererDocument;
import com.intellij.testFramework.EditorTestUtil;

public class EditorTextFieldDocumentTest extends AbstractEditorTest {
  public void testSelection() {
    EditorTextFieldRendererDocument document = new EditorTextFieldRendererDocument();
    document.setText("""
                         12345
                         67890
                         """);
    EditorEx editor = (EditorEx)EditorFactory.getInstance().createViewer(document, getProject());
    setEditor(editor);
    try {
      EditorTestUtil.setEditorVisibleSize(getEditor(), 100, 100);
      mouse().pressAt(0, 0).dragTo(1, 3).release();
      assertEquals(new VisualPosition(1, 3), getEditor().getCaretModel().getVisualPosition());
      assertEquals("""
                     12345
                     678""",editor.getSelectionModel().getSelectedText());
    } finally {
      EditorFactory.getInstance().releaseEditor(editor);
    }

  }
}
