Groovy Document Builder
================
[ ![Codeship Status for craigburke/document-builder](https://codeship.com/projects/c4c04780-74d2-0132-8185-6662d475f668/status?branch=master)](https://codeship.com/projects/55079)

A document builder for Groovy for PDF or Word documents. This is still very much a work in progress.

**Example:**
```
DocumentBuilder builder = new WordDocumentBuilder()
// or DocumentBuilder builder = new PdfDocumentBuilder()

builder.document(font: [family: 'Helvetica', size: 14], marginTop: 144) {
      paragraph {
        text "Hello"
        font.size = 20
        text "World"
      }
      table(columns: 1) {
        row {
          cell("Table")
        }
      }
  }
```
